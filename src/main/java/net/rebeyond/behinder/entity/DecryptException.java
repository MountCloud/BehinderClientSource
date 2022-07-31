package net.rebeyond.behinder.entity;

public class DecryptException extends Exception {
   private String statusCode;
   private String responseBody;

   public DecryptException(String statusCode, String responseBody) {
      this.statusCode = statusCode;
      this.responseBody = responseBody;
   }

   public String getMessage() {
      return "解密错误，HTTP响应值为：" + this.statusCode;
   }

   public String getResponseBody() {
      return this.responseBody;
   }
}
