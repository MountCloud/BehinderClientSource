// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils;

import java.io.IOException;
import java.security.MessageDigest;
import java.io.InputStream;
import javax.crypto.Mac;
import javax.crypto.NullCipher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.KeyFactory;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Key;
import javax.crypto.Cipher;
import java.security.PublicKey;

public class CipherUtils
{
    public static final String TAG = "CipherUtils";
    
    static byte[] RSA_OAEPPaddingPublicKeyEncrpt(final byte[] data, final PublicKey publicKey) {
        if (data == null || publicKey == null) {
            return new byte[0];
        }
        try {
            final Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
            cipher.init(1, publicKey);
            return cipher.doFinal(data);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    static byte[] RSA_OAEPPaddingPrivateKeyDecrpt(final byte[] data, final PrivateKey privateKey) {
        if (data == null || privateKey == null) {
            return new byte[0];
        }
        try {
            final Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
            cipher.init(2, privateKey);
            return cipher.doFinal(data);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    static PublicKey generatePublicKey(final BigInteger modulus, final BigInteger publicExponent) {
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static PrivateKey generatePrivateKey(final BigInteger modulus, final BigInteger publicExponent) {
        try {
            final RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, publicExponent);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static byte[] AES_CBC_PKCS5PaddingDecrypt(final byte[] data, final byte[] key, final byte[] IV) {
        if (key == null || key.length == 0) {
            return new byte[0];
        }
        try {
            final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, skeySpec, ivParameterSpec);
            return cipher.doFinal(data);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    static byte[] AES_CBC_PKCS5PaddingEncrypt(final byte[] data, final byte[] key, final byte[] IV) {
        if (key == null || key.length == 0) {
            return new byte[0];
        }
        try {
            final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, skeySpec, ivParameterSpec);
            return cipher.doFinal(data);
        }
        catch (NoSuchAlgorithmException ex) {}
        catch (NoSuchPaddingException ex2) {}
        catch (InvalidKeyException ex3) {}
        catch (InvalidAlgorithmParameterException ex4) {}
        catch (IllegalBlockSizeException ex5) {}
        catch (BadPaddingException ex6) {}
        return new byte[0];
    }
    
    static Cipher generateAES_CFB_NoPaddingEncryptCipher(final byte[] key, final byte[] IV) {
        try {
            final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            final Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(1, skeySpec, ivParameterSpec);
            return cipher;
        }
        catch (NoSuchAlgorithmException ex) {}
        catch (NoSuchPaddingException ex2) {}
        catch (InvalidKeyException ex3) {}
        catch (InvalidAlgorithmParameterException ex4) {}
        return new NullCipher();
    }
    
    static Cipher generateAES_CFB_NoPaddingDecryptCipher(final byte[] key, final byte[] IV) {
        try {
            final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            final Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(2, skeySpec, ivParameterSpec);
            return cipher;
        }
        catch (NoSuchAlgorithmException ex) {}
        catch (NoSuchPaddingException ex2) {}
        catch (InvalidKeyException ex3) {}
        catch (InvalidAlgorithmParameterException ex4) {}
        return new NullCipher();
    }
    
    static byte[] hmacSha256(final byte[] data, final byte[] key) {
        if (key == null || key.length == 0) {
            return new byte[0];
        }
        try {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            final Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        }
        catch (NoSuchAlgorithmException ex) {}
        catch (InvalidKeyException ex2) {}
        return new byte[0];
    }
    
    public static String bytesToHexStr(final byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        final String hexStr = "0123456789ABCDEF";
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            builder.append(hexStr.charAt((data[i] & 0xF0) >>> 4));
            builder.append(hexStr.charAt(data[i] & 0xF));
        }
        return builder.toString();
    }
    
    public static byte[] hexStrToBytes(final String hexStr) {
        byte[] result = null;
        try {
            if (hexStr == null || hexStr.length() == 0) {
                return new byte[0];
            }
            final char[] hexChars = hexStr.toCharArray();
            if ((hexChars.length & 0x1) != 0x0) {
                throw new DecodeHexStrException("hexStr is Odd number");
            }
            result = new byte[hexChars.length / 2];
            for (int i = 0, j = 0; i < hexChars.length; ++i, ++j) {
                final int h = Character.digit(hexChars[i], 16);
                final int l = Character.digit(hexChars[++i], 16);
                if (h == -1 || l == -1) {
                    throw new DecodeHexStrException("Illegal hexStr");
                }
                result[j] = (byte)(h << 4 | l);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    static byte[] intToByte(final int i) {
        final byte[] result = { (byte)(i >>> 0 & 0xFF), (byte)(i >>> 8 & 0xFF), (byte)(i >>> 16 & 0xFF), (byte)(i >>> 24 & 0xFF) };
        return result;
    }
    
    public static byte[] mergeByteArray(final byte[]... byteArray) {
        int totalLength = 0;
        for (int i = 0; i < byteArray.length; ++i) {
            if (byteArray[i] != null) {
                totalLength += byteArray[i].length;
            }
        }
        final byte[] result = new byte[totalLength];
        int cur = 0;
        for (int j = 0; j < byteArray.length; ++j) {
            if (byteArray[j] != null) {
                System.arraycopy(byteArray[j], 0, result, cur, byteArray[j].length);
                cur += byteArray[j].length;
            }
        }
        return result;
    }
    
    public static String sha256Hex(final InputStream is) {
        final byte[] buffer = new byte[1024];
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            int read;
            while ((read = is.read(buffer)) > -1) {
                digest.update(buffer, 0, read);
            }
            final byte[] result = digest.digest();
            return bytesToHexStr(result);
        }
        catch (IOException ex) {}
        catch (NoSuchAlgorithmException ex2) {}
        return "";
    }
    
    public static String sha256Hex(final byte[] data) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(data);
            final byte[] result = digest.digest();
            return bytesToHexStr(result);
        }
        catch (NoSuchAlgorithmException ex) {
            return "";
        }
    }
    
    public static byte[] bytesXor(final byte[] b1, final byte[] b2) {
        byte[] longbytes;
        byte[] shortbytes;
        if (b1.length >= b2.length) {
            longbytes = b1;
            shortbytes = b2;
        }
        else {
            longbytes = b2;
            shortbytes = b1;
        }
        final byte[] xorstr = new byte[longbytes.length];
        int i;
        for (i = 0; i < shortbytes.length; ++i) {
            xorstr[i] = (byte)(shortbytes[i] ^ longbytes[i]);
        }
        while (i < longbytes.length) {
            xorstr[i] = longbytes[i];
            ++i;
        }
        return xorstr;
    }
    
    static class DecodeHexStrException extends Exception
    {
        private static final long serialVersionUID = 938776570614030665L;
        
        DecodeHexStrException(final String string) {
            super(string);
        }
    }
}
