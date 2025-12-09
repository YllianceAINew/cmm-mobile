package com.multimediachat.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class EncDecDes {

	private String KEY = "BJMRcEvr7YJGBJMRcEvr7YJG";
	private Cipher encryptCipher = null;
	private Cipher decryptCipher = null;

	public static EncDecDes self = null;

	public EncDecDes(String key) {
		if (key != null && !key.equals(""))
			this.KEY = key;
		init();
	}

	public static EncDecDes getInstance() {
		if (self == null) {
			self = new EncDecDes(null);
			self.init();
		}
		return self;
	}

	private void init() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(KEY.getBytes("UTF-8"));
			byte[] keyBytes = new byte[32];
			System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
			// The initialization vector needed by the CBC mode
			byte[] IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };

			// 1. create the cipher using Bouncy Castle Provider
			encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// 2. create the key
			SecretKeySpec keyValue = new SecretKeySpec(keyBytes, "AES");
			// 3. create the IV
			AlgorithmParameterSpec IVspec = new IvParameterSpec(IV);
			// 4. init the cipher
			encryptCipher.init(Cipher.ENCRYPT_MODE, keyValue, IVspec);

			// 1 create the cipher
			decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// 2. the key is already created
			// 3. the IV is already created
			// 4. init the cipher
			decryptCipher.init(Cipher.DECRYPT_MODE, keyValue, IVspec);

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		} catch (NoSuchPaddingException e) {

			e.printStackTrace();
		} catch (InvalidKeyException e) {

			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {

			e.printStackTrace();

		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
	}

	public String encrypt(String data) {

		String encryptedData = "";

		byte[] encrypted;

		try {
			encrypted = encryptCipher.doFinal(data.getBytes("UTF-8"));
			encryptedData = new String(Base64.encodeBase64(encrypted), "UTF-8");
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return encryptedData;
	}

	public String generateFileName(String data) {
		String fileName = encrypt(data);
		if (fileName != null) {
			fileName = fileName.replaceAll("[^a-zA-Z0-9]+", "");
		}
		return fileName;
	}

	public String decrypt(String data) {
		String decyptedData = "";

		try {
			byte[] bytes = decryptCipher.doFinal(Base64.decodeBase64(data.getBytes("UTF-8")));
			decyptedData = new String(bytes, "UTF-8");

		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decyptedData;
	}
}