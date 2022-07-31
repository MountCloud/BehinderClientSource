package net.rebeyond.behinder.utils;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SSLSocketClient {
   public static SSLSocketFactory getSSLSocketFactory() {
      try {
         SSLContext sslContext = SSLContext.getInstance("SSL");
         sslContext.init((KeyManager[])null, getTrustManager(), new SecureRandom());
         return sslContext.getSocketFactory();
      } catch (Exception var1) {
         throw new RuntimeException(var1);
      }
   }

   private static TrustManager[] getTrustManager() {
      return new TrustManager[]{new X509TrustManager() {
         public void checkClientTrusted(X509Certificate[] chain, String authType) {
         }

         public void checkServerTrusted(X509Certificate[] chain, String authType) {
         }

         public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
         }
      }};
   }

   public static HostnameVerifier getHostnameVerifier() {
      return (s, sslSession) -> {
         return true;
      };
   }

   public static X509TrustManager getX509TrustManager() {
      X509TrustManager trustManager = null;

      try {
         TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         trustManagerFactory.init((KeyStore)null);
         TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
         if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
         }

         trustManager = (X509TrustManager)trustManagers[0];
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return trustManager;
   }
}
