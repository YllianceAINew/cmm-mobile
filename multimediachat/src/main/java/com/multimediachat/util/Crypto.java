package com.multimediachat.util;


import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;


/**
 * Referenced from android-pbe by Nelenkov
 * https://github.com/nelenkov/android-pbe/blob/master/src/org/nick/androidpbe/Crypto.java
 */
public class Crypto {

    private static final String TAG = Crypto.class.getSimpleName();

    public static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    private final static int KEY_LENGTH = 256;
    // minimum values recommended by PKCS#5, increase as necessary
    private final static int ITERATION_COUNT = 1000;
    private static final int PKCS5_SALT_LENGTH = 8;

    private final static SecureRandom random = new SecureRandom();

    private Crypto() {
    }

    public static SecretKey deriveKeyPbkdf2(byte[] salt, String password) {
        try {
            long start = System.currentTimeMillis();
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                    ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory
                    .getInstance(PBKDF2_DERIVATION_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            //Log.d(TAG, "key bytes: " + toHex(keyBytes));

            SecretKey result = new SecretKeySpec(keyBytes, "AES");
            long elapsed = System.currentTimeMillis() - start;
            Log.d(TAG, String.format("PBKDF2 key derivation took %d [ms].",
                    elapsed));

            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);

        return b;
    }

    public static String toHex(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }

    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

}