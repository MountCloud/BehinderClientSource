<?php

$host = "127.0.0.1";
$port = "1433";
$database = "master";
$user = "sa";
$pass = "rebeyond";
$sql = "SELECT name FROM  master..sysdatabases";


$Server = $host . "," . $port;
$conInfo = array(
    'Database' => $database,
    'UID' => $user,
    'PWD' => $pass
);
$link = sqlsrv_connect($Server, $conInfo);

if ($link) {
    $stmt = sqlsrv_query($link, $sql);
    $fieldinfo = array();
    
    foreach (sqlsrv_field_metadata($stmt) as $fieldMetadata) {
        foreach ($fieldMetadata as $name => $value) {
            array_push($fieldinfo, array(
                "name" => $name
            ));
        }
    }
    echo json_encode($fieldinfo);
    while ($row = sqlsrv_fetch_array($stmt, SQLSRV_FETCH_ASSOC)) {
        echo $row['name'] . "\n";
    }
} else {
    if (($errors = sqlsrv_errors()) != null) {
        foreach ($errors as $error) {
            echo "SQLSTATE: " . $error['SQLSTATE'] . "<br />";
            echo "code: " . $error['code'] . "<br />";
            echo "message: " . $error['message'] . "<br />";
        }
    }
}
?>