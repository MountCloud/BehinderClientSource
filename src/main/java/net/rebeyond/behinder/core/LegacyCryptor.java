package net.rebeyond.behinder.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.rebeyond.behinder.dao.TransProtocolDao;
import net.rebeyond.behinder.entity.TransProtocol;
import net.rebeyond.behinder.utils.Utils;

public class LegacyCryptor implements ICrypt {
   private TransProtocolDao transProtocolDao = new TransProtocolDao();
   private Map transProtocolMap = new HashMap();
   private String type;
   private int encryptType;
   private String key;
   private TransProtocol localTransProtocol;
   private Class encodeCls;
   private Class decodeCls;

   public LegacyCryptor(String type, int encryptType, String key) throws Exception {
      this.type = type;
      this.encryptType = encryptType;
      this.key = key;
      if (this.type.equals("asp")) {
         this.encryptType = Constants.ENCRYPT_TYPE_XOR;
      }

      if (this.encryptType == Constants.ENCRYPT_TYPE_AES) {
         this.localTransProtocol = this.transProtocolDao.findLegacyTransProtocolByTypeAndName("jsp", type + "_aes");
      } else if (this.encryptType == Constants.ENCRYPT_TYPE_XOR) {
         this.localTransProtocol = this.transProtocolDao.findLegacyTransProtocolByTypeAndName("jsp", type + "_xor");
      }

      this.updateKey(this.localTransProtocol);
      List transProtocolList = this.transProtocolDao.findTransProtocolsById(this.localTransProtocol.getId());
      Iterator var5 = transProtocolList.iterator();

      while(var5.hasNext()) {
         TransProtocol transProtocol = (TransProtocol)var5.next();
         this.transProtocolMap.put(transProtocol.getType(), transProtocol);
         this.updateKey(transProtocol);
      }

   }

   private void updateKey(TransProtocol transProtocol) {
      transProtocol.setCryptor(this);
      if (transProtocol.getEncode() != null) {
         transProtocol.setEncode(transProtocol.getEncode().replace("e45e329feb5d925b", this.key));
      }

      if (transProtocol.getDecode() != null) {
         transProtocol.setDecode(transProtocol.getDecode().replace("e45e329feb5d925b", this.key));
      }

   }

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

   private String paddingCode(String fuctionCode) {
      return String.format(Constants.JAVA_CODE_TEMPLATE_SHORT, fuctionCode);
   }

   public byte[] encrypt(byte[] clearContent) throws Exception {
      Class encodeCls = this.getEncodeCls();
      Method encodeMethod = encodeCls.getDeclaredMethod("Encrypt", byte[].class);
      encodeMethod.setAccessible(true);
      byte[] result = (byte[])encodeMethod.invoke(encodeCls.newInstance(), clearContent);
      return result;
   }

   public byte[] decrypt(byte[] decryptContent) throws Exception {
      Class decodeCls = this.getDecodeCls();
      Method decodeMethod = decodeCls.getDeclaredMethod("Decrypt", byte[].class);
      decodeMethod.setAccessible(true);
      byte[] result = (byte[])decodeMethod.invoke(decodeCls.newInstance(), decryptContent);
      return result;
   }

   public boolean verify() throws Exception {
      return true;
   }

   public byte[] encryptCompatible(byte[] clearContent) throws Exception {
      return new byte[0];
   }

   public byte[] decryptCompatible(byte[] clearContent) throws Exception {
      return new byte[0];
   }

   public TransProtocol getTransProtocol(String type) {
      return (TransProtocol)this.transProtocolMap.get(type);
   }

   public byte[] getDecodeClsBytes() throws Exception {
      byte[] payload = Utils.getClassFromSourceCode(this.paddingCode(this.localTransProtocol.getDecode()));
      payload[7] = 50;
      return payload;
   }

   public boolean isCustomized() {
      return false;
   }

   public String getKey() {
      return this.key;
   }
}
