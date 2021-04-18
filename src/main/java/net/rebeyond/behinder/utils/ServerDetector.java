package net.rebeyond.behinder.utils;

public class ServerDetector {
   public static final String GERONIMO_ID = "geronimo";
   public static final String GLASSFISH_ID = "glassfish";
   public static final String JBOSS_ID = "jboss";
   public static final String JETTY_ID = "jetty";
   public static final String JONAS_ID = "jonas";
   public static final String OC4J_ID = "oc4j";
   public static final String RESIN_ID = "resin";
   public static final String TOMCAT_ID = "tomcat";
   public static final String WEBLOGIC_ID = "weblogic";
   public static final String WEBSPHERE_ID = "websphere";
   private static ServerDetector _instance = new ServerDetector();
   private String _serverId;
   private Boolean _geronimo;
   private Boolean _glassfish;
   private Boolean _glassfish2;
   private Boolean _glassfish3;
   private Boolean _jBoss;
   private Boolean _jetty;
   private Boolean _jonas;
   private Boolean _oc4j;
   private Boolean _resin;
   private Boolean _tomcat;
   private Boolean _webLogic;
   private Boolean _webSphere;

   public static String getServerId() {
      ServerDetector sd = _instance;
      if (sd._serverId == null) {
         if (isGeronimo()) {
            sd._serverId = "geronimo";
         } else if (isGlassfish()) {
            sd._serverId = "glassfish";
         } else if (isJBoss()) {
            sd._serverId = "jboss";
         } else if (isJOnAS()) {
            sd._serverId = "jonas";
         } else if (isOC4J()) {
            sd._serverId = "oc4j";
         } else if (isResin()) {
            sd._serverId = "resin";
         } else if (isWebLogic()) {
            sd._serverId = "weblogic";
         } else if (isWebSphere()) {
            sd._serverId = "websphere";
         }

         if (isJetty()) {
            if (sd._serverId == null) {
               sd._serverId = "jetty";
            } else {
               sd._serverId = sd._serverId + "-jetty";
            }
         } else if (isTomcat()) {
            if (sd._serverId == null) {
               sd._serverId = "tomcat";
            } else {
               sd._serverId = sd._serverId + "-tomcat";
            }
         }

         if (sd._serverId == null) {
            throw new RuntimeException("Server is not supported");
         }
      }

      return sd._serverId;
   }

   public static boolean isGeronimo() {
      ServerDetector sd = _instance;
      if (sd._geronimo == null) {
         sd._geronimo = _detect("/org/apache/geronimo/system/main/Daemon.class");
      }

      return sd._geronimo;
   }

   public static boolean isGlassfish() {
      ServerDetector sd = _instance;
      if (sd._glassfish == null) {
         String value = System.getProperty("com.sun.aas.instanceRoot");
         if (value != null) {
            sd._glassfish = Boolean.TRUE;
         } else {
            sd._glassfish = Boolean.FALSE;
         }
      }

      return sd._glassfish;
   }

   public static boolean isGlassfish2() {
      ServerDetector sd = _instance;
      if (sd._glassfish2 == null) {
         if (isGlassfish() && !isGlassfish3()) {
            sd._glassfish2 = Boolean.TRUE;
         } else {
            sd._glassfish2 = Boolean.FALSE;
         }
      }

      return sd._glassfish2;
   }

   public static boolean isGlassfish3() {
      ServerDetector sd = _instance;
      if (sd._glassfish3 == null) {
         String value = "";
         if (isGlassfish()) {
            value = getString(System.getProperty("product.name"));
         }

         if (value.equals("GlassFish/v3")) {
            sd._glassfish3 = Boolean.TRUE;
         } else {
            sd._glassfish3 = Boolean.FALSE;
         }
      }

      return sd._glassfish3;
   }

   public static boolean isJBoss() {
      ServerDetector sd = _instance;
      if (sd._jBoss == null) {
         sd._jBoss = _detect("/org/jboss/Main.class");
      }

      return sd._jBoss;
   }

   public static boolean isJetty() {
      ServerDetector sd = _instance;
      if (sd._jetty == null) {
         sd._jetty = _detect("/org/mortbay/jetty/Server.class");
      }

      return sd._jetty;
   }

   public static boolean isJOnAS() {
      ServerDetector sd = _instance;
      if (sd._jonas == null) {
         sd._jonas = _detect("/org/objectweb/jonas/server/Server.class");
      }

      return sd._jonas;
   }

   public static boolean isOC4J() {
      ServerDetector sd = _instance;
      if (sd._oc4j == null) {
         sd._oc4j = _detect("oracle.oc4j.util.ClassUtils");
      }

      return sd._oc4j;
   }

   public static boolean isResin() {
      ServerDetector sd = _instance;
      if (sd._resin == null) {
         sd._resin = _detect("/com/caucho/server/resin/Resin.class");
      }

      return sd._resin;
   }

   public static boolean isSupportsComet() {
      return false;
   }

   public static boolean isTomcat() {
      ServerDetector sd = _instance;
      if (sd._tomcat == null) {
         sd._tomcat = _detect("/org/apache/catalina/startup/Bootstrap.class");
      }

      if (sd._tomcat == null) {
         sd._tomcat = _detect("/org/apache/catalina/startup/Embedded.class");
      }

      return sd._tomcat;
   }

   public static boolean isJakarta() {
      return _detect("/javax/servlet/http/HttpServlet.class");
   }

   public static boolean isWebLogic() {
      ServerDetector sd = _instance;
      if (sd._webLogic == null) {
         sd._webLogic = _detect("/weblogic/Server.class");
      }

      return sd._webLogic;
   }

   public static boolean isWebSphere() {
      ServerDetector sd = _instance;
      if (sd._webSphere == null) {
         sd._webSphere = _detect("/com/ibm/websphere/product/VersionInfo.class");
      }

      return sd._webSphere;
   }

   private static Boolean _detect(String className) {
      try {
         ClassLoader.getSystemClassLoader().loadClass(className);
         return Boolean.TRUE;
      } catch (ClassNotFoundException var4) {
         ServerDetector sd = _instance;
         Class c = sd.getClass();
         return c.getResource(className) != null ? Boolean.TRUE : Boolean.FALSE;
      }
   }

   public static String getString(String value) {
      return getString(value, "");
   }

   public static String getString(String value, String defaultValue) {
      return get(value, defaultValue);
   }

   public static String get(String value, String defaultValue) {
      if (value != null) {
         value = value.trim();
         value = replace(value, "\r\n", "\n");
         return value;
      } else {
         return defaultValue;
      }
   }

   public static String replace(String s, String oldSub, String newSub) {
      if (s != null && oldSub != null && newSub != null) {
         int y = s.indexOf(oldSub);
         if (y < 0) {
            return s;
         } else {
            StringBuilder sb = new StringBuilder(s.length() + 5 * newSub.length());
            int length = oldSub.length();

            int x;
            for(x = 0; x <= y; y = s.indexOf(oldSub, x)) {
               sb.append(s.substring(x, y));
               sb.append(newSub);
               x = y + length;
            }

            sb.append(s.substring(x));
            return sb.toString();
         }
      } else {
         return null;
      }
   }
}
