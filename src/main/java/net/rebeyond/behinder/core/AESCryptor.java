package net.rebeyond.behinder.core;

import net.rebeyond.behinder.entity.TransProtocol;

public class AESCryptor implements ICrypt {
   public byte[] encrypt(byte[] clearContent) throws Exception {
      return new byte[0];
   }

   public byte[] decrypt(byte[] clearContent) throws Exception {
      return new byte[0];
   }

   public boolean verify() throws Exception {
      return false;
   }

   public byte[] encryptCompatible(byte[] clearContent) throws Exception {
      return new byte[0];
   }

   public byte[] decryptCompatible(byte[] clearContent) throws Exception {
      return new byte[0];
   }

   public TransProtocol getTransProtocol(String type) {
      return null;
   }

   public byte[] getDecodeClsBytes() throws Exception {
      return new byte[0];
   }
}
