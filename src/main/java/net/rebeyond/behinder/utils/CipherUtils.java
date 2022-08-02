package net.rebeyond.behinder.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.NullCipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {
   public static final String TAG = "CipherUtils";

   static byte[] RSA_OAEPPaddingPublicKeyEncrpt(byte[] data, PublicKey publicKey) {
      if (data != null && publicKey != null) {
         try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
            cipher.init(1, publicKey);
            return cipher.doFinal(data);
         } catch (Exception var3) {
            var3.printStackTrace();
            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   static byte[] RSA_OAEPPaddingPrivateKeyDecrpt(byte[] data, PrivateKey privateKey) {
      if (data != null && privateKey != null) {
         try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
            cipher.init(2, privateKey);
            return cipher.doFinal(data);
         } catch (Exception var3) {
            var3.printStackTrace();
            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   static PublicKey generatePublicKey(BigInteger modulus, BigInteger publicExponent) {
      try {
         KeyFactory keyFactory = KeyFactory.getInstance("RSA");
         return keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   static PrivateKey generatePrivateKey(BigInteger modulus, BigInteger publicExponent) {
      try {
         RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, publicExponent);
         KeyFactory keyFactory = KeyFactory.getInstance("RSA");
         PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
         return privateKey;
      } catch (Exception var5) {
         var5.printStackTrace();
         return null;
      }
   }

   static byte[] AES_CBC_PKCS5PaddingDecrypt(byte[] data, byte[] key, byte[] IV) {
      if (key != null && key.length != 0) {
         try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, skeySpec, ivParameterSpec);
            return cipher.doFinal(data);
         } catch (Exception var6) {
            var6.printStackTrace();
            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   static byte[] AES_CBC_PKCS5PaddingEncrypt(byte[] data, byte[] key, byte[] IV) {
      if (key != null && key.length != 0) {
         try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, skeySpec, ivParameterSpec);
            return cipher.doFinal(data);
         } catch (NoSuchAlgorithmException var6) {
         } catch (NoSuchPaddingException var7) {
         } catch (InvalidKeyException var8) {
         } catch (InvalidAlgorithmParameterException var9) {
         } catch (IllegalBlockSizeException var10) {
         } catch (BadPaddingException var11) {
         }

         return new byte[0];
      } else {
         return new byte[0];
      }
   }

   static Cipher generateAES_CFB_NoPaddingEncryptCipher(byte[] key, byte[] IV) {
      try {
         SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
         IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
         Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
         cipher.init(1, skeySpec, ivParameterSpec);
         return cipher;
      } catch (NoSuchAlgorithmException var5) {
      } catch (NoSuchPaddingException var6) {
      } catch (InvalidKeyException var7) {
      } catch (InvalidAlgorithmParameterException var8) {
      }

      return new NullCipher();
   }

   static Cipher generateAES_CFB_NoPaddingDecryptCipher(byte[] key, byte[] IV) {
      try {
         SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
         IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
         Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
         cipher.init(2, skeySpec, ivParameterSpec);
         return cipher;
      } catch (NoSuchAlgorithmException var5) {
      } catch (NoSuchPaddingException var6) {
      } catch (InvalidKeyException var7) {
      } catch (InvalidAlgorithmParameterException var8) {
      }

      return new NullCipher();
   }

   static byte[] hmacSha256(byte[] data, byte[] key) {
      if (key != null && key.length != 0) {
         try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
            mac.init(secretKeySpec);
            return mac.doFinal(data);
         } catch (NoSuchAlgorithmException var4) {
         } catch (InvalidKeyException var5) {
         }

         return new byte[0];
      } else {
         return new byte[0];
      }
   }

   public static String bytesToHexStr(byte[] data) {
      if (data != null && data.length != 0) {
         String hexStr = "0123456789ABCDEF";
         StringBuilder builder = new StringBuilder();

         for(int i = 0; i < data.length; ++i) {
            builder.append(hexStr.charAt((data[i] & 240) >>> 4));
            builder.append(hexStr.charAt(data[i] & 15));
         }

         return builder.toString();
      } else {
         return "";
      }
   }

   public static byte[] hexStrToBytes(String hexStr) {
      byte[] result = null;

      try {
         if (hexStr != null && hexStr.length() != 0) {
            char[] hexChars = hexStr.toCharArray();
            if ((hexChars.length & 1) != 0) {
               throw new CipherUtils.DecodeHexStrException("hexStr is Odd number");
            } else {
               result = new byte[hexChars.length / 2];
               int i = 0;

               for(int j = 0; i < hexChars.length; ++j) {
                  int h = Character.digit(hexChars[i], 16);
                  ++i;
                  int l = Character.digit(hexChars[i], 16);
                  if (h == -1 || l == -1) {
                     throw new CipherUtils.DecodeHexStrException("Illegal hexStr");
                  }

                  result[j] = (byte)(h << 4 | l);
                  ++i;
               }

               return result;
            }
         } else {
            return new byte[0];
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         return result;
      }
   }

   static byte[] intToByte(int i) {
      byte[] result = new byte[]{(byte)(i >>> 0 & 255), (byte)(i >>> 8 & 255), (byte)(i >>> 16 & 255), (byte)(i >>> 24 & 255)};
      return result;
   }

   public static byte[] mergeByteArray(byte[]... byteArray) {
      int totalLength = 0;

      for(int i = 0; i < byteArray.length; ++i) {
         if (byteArray[i] != null) {
            totalLength += byteArray[i].length;
         }
      }

      byte[] result = new byte[totalLength];
      int cur = 0;

      for(int i = 0; i < byteArray.length; ++i) {
         if (byteArray[i] != null) {
            System.arraycopy(byteArray[i], 0, result, cur, byteArray[i].length);
            cur += byteArray[i].length;
         }
      }

      return result;
   }

   public static String sha256Hex(InputStream is) {
      byte[] buffer = new byte[1024];

      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");

         int read;
         while((read = is.read(buffer)) > -1) {
            digest.update(buffer, 0, read);
         }

         byte[] result = digest.digest();
         return bytesToHexStr(result);
      } catch (IOException var5) {
      } catch (NoSuchAlgorithmException var6) {
      }

      return "";
   }

   public static String sha256Hex(byte[] data) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         digest.update(data);
         byte[] result = digest.digest();
         return bytesToHexStr(result);
      } catch (NoSuchAlgorithmException var3) {
         return "";
      }
   }

   public static byte[] bytesXor(byte[] b1, byte[] b2) {
      byte[] longbytes;
      byte[] shortbytes;
      if (b1.length >= b2.length) {
         longbytes = b1;
         shortbytes = b2;
      } else {
         longbytes = b2;
         shortbytes = b1;
      }

      byte[] xorstr = new byte[longbytes.length];

      int i;
      for(i = 0; i < shortbytes.length; ++i) {
         xorstr[i] = (byte)(shortbytes[i] ^ longbytes[i]);
      }

      while(i < longbytes.length) {
         xorstr[i] = longbytes[i];
         ++i;
      }

      return xorstr;
   }

   static class DecodeHexStrException extends Exception {
      private static final long serialVersionUID = 938776570614030665L;

      DecodeHexStrException(String string) {
         super(string);
      }
   }
}
