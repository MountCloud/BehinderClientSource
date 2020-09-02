// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.core;

import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Decrypt
{
    public static byte[] Encrypt(final byte[] bs, final String key) throws Exception {
        final byte[] raw = key.getBytes("utf-8");
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        final byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
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
    
    public static byte[] EncryptForPhp(final byte[] bs, final String key) throws Exception {
        final byte[] raw = key.getBytes("utf-8");
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, skeySpec, new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }));
        final byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
    
    public static byte[] EncryptForAsp(final byte[] bs, final String key) throws Exception {
        for (int i = 0; i < bs.length; ++i) {
            bs[i] ^= key.getBytes()[i + 1 & 0xF];
        }
        return bs;
    }
    
    public static void main(final String[] args) throws Exception {
        final String res = new String(EncryptForAsp("ffff".getBytes(), "1234567887654321"));
    }
}
