package net.rebeyond.behinder.utils;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLContext;
import net.rebeyond.behinder.core.Constants;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class HttpClientUtils {
   private static CloseableHttpClient httpClient = null;
   public static HttpHost proxy;
   private static PoolingHttpClientConnectionManager cm;

   public static JSONObject doGet(String url) throws Exception {
      return doGet(url, (Map)null);
   }

   public static JSONObject doGet(String url, Map headers) throws Exception {
      HttpGet httpGet = null;
      CloseableHttpResponse response = null;

      JSONObject responseObj;
      try {
         httpGet = new HttpGet(new URI(url));
         if (headers != null) {
            Iterator var5 = headers.keySet().iterator();

            while(var5.hasNext()) {
               String key = (String)var5.next();
               httpGet.setHeader(key, (String)headers.get(key));
            }
         }

         response = httpClient.execute(httpGet);
         int statusCode = response.getStatusLine().getStatusCode();
         responseObj = new JSONObject();
         responseObj.put("statusCode", statusCode);
         responseObj.put("headers", response.getAllHeaders());
         HttpEntity entity = response.getEntity();
         if (null != entity) {
            byte[] body = EntityUtils.toByteArray(response.getEntity());
            responseObj.put("body", body);
         }
      } finally {
         try {
            if (null != response) {
               response.close();
            }
         } catch (IOException var16) {
            var16.printStackTrace();
         }

         try {
            if (null != httpGet) {
               httpGet.releaseConnection();
            }
         } catch (Exception var15) {
            var15.printStackTrace();
         }

      }

      return responseObj;
   }

   public static Map doPost(String url, byte[] requstBody) throws Exception {
      return doPost(url, (Map)null, requstBody);
   }

   public static Map doPost(String url, Map headers, byte[] requstBody) throws Exception {
      Map result = new HashMap();
      HttpPost httpPost = null;
      CloseableHttpResponse response = null;

      try {
         httpPost = new HttpPost(url);
         if (headers != null) {
            Iterator var6 = headers.keySet().iterator();

            while(var6.hasNext()) {
               String key = (String)var6.next();
               httpPost.setHeader(key, (String)headers.get(key));
            }
         }

         httpPost.setEntity(new ByteArrayEntity(requstBody));
         response = httpClient.execute(httpPost);
         HttpEntity entity = response.getEntity();
         if (null != entity) {
            byte[] body = EntityUtils.toByteArray(response.getEntity());
            result.put("data", body);
         }

         Map responseHeader = new HashMap();
         Header[] var8 = response.getAllHeaders();
         int var9 = var8.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            Header header = var8[var10];
            responseHeader.put(header.getName(), header.getValue());
         }

         responseHeader.put("status", response.getStatusLine().getStatusCode() + "");
         result.put("header", responseHeader);
         HashMap var29 = (HashMap)result;
         return var29;
      } catch (Exception var24) {
         var24.printStackTrace();
      } finally {
         try {
            if (null != response) {
               response.close();
            }
         } catch (IOException var23) {
            var23.printStackTrace();
         }

         try {
            if (null != httpPost) {
               httpPost.releaseConnection();
            }
         } catch (Exception var22) {
            var22.printStackTrace();
         }

      }

      return result;
   }

   public static void main(String[] args) {
      try {
         JSONObject result = doGet("https://www.baidu.com/a.txt", (Map)null);
         System.out.println(new String((byte[])((byte[])result.get("body"))));
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public static CloseableHttpClient getHttpClient() {
      return httpClient;
   }

   public static void httpPoolStats() {
      Set routes = cm.getRoutes();
      routes.forEach((e) -> {
         PoolStats stats = cm.getStats((HttpRoute) e);
      });
      PoolStats totalStats = cm.getTotalStats();
   }

   static {
      LayeredConnectionSocketFactory sslsf = null;

      try {
         sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
      } catch (NoSuchAlgorithmException var7) {
         var7.printStackTrace();
      }

      Registry socketFactoryRegistry = RegistryBuilder.create().register("https", sslsf).register("http", new PlainConnectionSocketFactory()).build();
      PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
      cm = connectionManager;
      connectionManager.setMaxTotal(200);
      connectionManager.setDefaultMaxPerRoute(1);
      SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoReuseAddress(true).setSoLinger(60).setSoTimeout(500).setSoKeepAlive(true).build();
      RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.HTTP_TIME_OUT * 1000).setConnectionRequestTimeout(Constants.HTTP_TIME_OUT * 1000).setSocketTimeout(Constants.HTTP_TIME_OUT * 1000).build();
      HttpRequestRetryHandler requestRetryHandler = new DefaultHttpRequestRetryHandler(5, true);
      ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
         public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
            return 30000L;
         }
      };
      httpClient = HttpClients.custom().setConnectionManager(connectionManager).setConnectionManagerShared(true).setRetryHandler(requestRetryHandler).setKeepAliveStrategy(connectionKeepAliveStrategy).addInterceptorLast(new HttpRequestInterceptor() {
         public void process(HttpRequest request, HttpContext context) {
            Header[] headerArr = request.getAllHeaders();
            Arrays.sort(headerArr, new MyComprator());
            request.setHeaders(headerArr);
         }
      }).build();
   }

   static class MyComprator implements Comparator {
      public int compare(Object arg0, Object arg1) {
         String t1 = ((Header)arg0).getName();
         String t2 = ((Header)arg1).getValue();
         if (t1.equals("Host")) {
            return -2;
         } else {
            int delta = t1.compareTo(t2);
            return delta < 0 ? -1 : 0;
         }
      }
   }
}
