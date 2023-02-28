package net.rebeyond.behinder.utils;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.rebeyond.behinder.ui.controller.MainController;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.internal.http.RealResponseBody;
import okio.GzipSource;
import okio.Okio;

public class OKHttpClientUtil {
   private static ConcurrentHashMap cookieStore = new ConcurrentHashMap();
   private static ConnectionSpec spec;
   private static OkHttpClient client;

   public static Map post(String url, Map headers, byte[] requestBody) throws IOException {
      HashMap result = new HashMap();
      String host = (new URL(url)).getHost();
      RequestBody body = RequestBody.create(requestBody);
      Request.Builder builder = (new Request.Builder()).url(url).post(body);
      builder.addHeader("Host", host);
      headers.remove("Host");
      headers.put("Content-Length", body.contentLength() + "");
      String[] headerKeys = (String[])headers.keySet().toArray(new String[0]);
      Arrays.sort(headerKeys);
      String[] var8 = headerKeys;
      int var9 = headerKeys.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         String key = var8[var10];

         try {
            builder.addHeader(key, (String)headers.get(key));
         } catch (Exception var15) {
            var15.printStackTrace();
         }
      }

      Request request = builder.build();

      try {
         Response response = client.newCall(request).execute();

         HashMap var22;
         try {
            Map responseHeader = new HashMap();
            Headers resheaders = response.headers();
            Iterator var12 = resheaders.names().iterator();

            while(true) {
               if (!var12.hasNext()) {
                  result.put("data", response.body().bytes());
                  responseHeader.put("status", response.code() + "");
                  result.put("header", responseHeader);
                  var22 = result;
                  break;
               }

               String header = (String)var12.next();
               responseHeader.put(header, resheaders.get(header));
            }
         } catch (Throwable var16) {
            if (response != null) {
               try {
                  response.close();
               } catch (Throwable var14) {
                  var16.addSuppressed(var14);
               }
            }

            throw var16;
         }

         if (response != null) {
            response.close();
         }

         return var22;
      } catch (Exception var17) {
         throw var17;
      }
   }

   public static void setProxy(Proxy proxy) {
      client = client.newBuilder().proxy(proxy).build();
   }

   public static void clearSession(String url) {
      try {
         String host = (new URL(url)).getHost();
         cookieStore.remove(host);
      } catch (Exception var2) {
      }

   }

   static {
      spec = (new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)).tlsVersions(new TlsVersion[]{TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_3}).build();
      client = (new OkHttpClient()).newBuilder().connectTimeout(10L, TimeUnit.SECONDS).sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager()).hostnameVerifier(SSLSocketClient.getHostnameVerifier()).connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT, spec)).readTimeout(60L, TimeUnit.SECONDS).proxy((Proxy)MainController.currentProxy.get("proxy")).connectionPool(new ConnectionPool(100, 30L, TimeUnit.SECONDS)).cookieJar(new CookieJar() {
         public void saveFromResponse(HttpUrl url, List cookiesx) {
            Set cookieSet = new HashSet(cookiesx);
            List cookies = new ArrayList(cookieSet);
            OKHttpClientUtil.cookieStore.put(url.host(), cookies);
         }

         public List loadForRequest(HttpUrl url) {
            List cookies = (List)OKHttpClientUtil.cookieStore.get(url.host());
            return (List)(cookies != null ? cookies : new ArrayList());
         }
      }).build();
   }

   private static class UnzippingInterceptor implements Interceptor {
      public Response intercept(Interceptor.Chain chain) throws IOException {
         Response response = chain.proceed(chain.request());
         return this.unzip(response);
      }

      private Response unzip(Response response) throws IOException {
         if (response.body() == null) {
            return response;
         } else {
            String contentEncoding = response.headers().get("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equals("gzip")) {
               Long contentLength = response.body().contentLength();
               GzipSource responseBody = new GzipSource(response.body().source());
               Headers strippedHeaders = response.headers().newBuilder().removeAll("Content-Encoding").removeAll("Content-Length").build();
               return response.newBuilder().headers(strippedHeaders).body(new RealResponseBody(response.body().contentType().toString(), -1L, Okio.buffer(responseBody))).build();
            } else {
               return response;
            }
         }
      }
   }
}
