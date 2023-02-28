package net.rebeyond.behinder.core;

import net.rebeyond.behinder.entity.TransProtocol;

public interface ICrypt {
   byte[] encrypt(byte[] var1) throws Exception;

   byte[] decrypt(byte[] var1) throws Exception;

   boolean verify() throws Exception;

   byte[] encryptCompatible(byte[] var1) throws Exception;

   byte[] decryptCompatible(byte[] var1) throws Exception;

   TransProtocol getTransProtocol(String var1);

   byte[] getDecodeClsBytes() throws Exception;

   boolean isCustomized();
}
