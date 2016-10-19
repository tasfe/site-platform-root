package com.netfinworks.site.core.common.util;

/**
 * NeteaseSignUtil
 * Version 1.0
 * Copyright 2006-2007.
 * All Rights Reserved.
 * <p>本程序是个签名的工具类。
 * 用于各个产品和网银系统传递数据时使用。
 * 包含的功能包括产生需要的公私钥；产生签名和验证签名的过程。
 * 
 */
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

public class RSASignUtil {

	// /**
	// * 本方法用于产生1024位RSA公私钥对。
	// *
	// */
	public static void genRSAKeyPair() {
		KeyPairGenerator rsaKeyGen = null;
		KeyPair rsaKeyPair = null;
		try {
			System.out.println("Generating a pair of RSA key ... ");
			rsaKeyGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = new SecureRandom();
			random.setSeed(("" + System.currentTimeMillis() * Math.random() * Math.random()).getBytes());
			rsaKeyGen.initialize(1024, random);
			rsaKeyPair = rsaKeyGen.genKeyPair();
			PublicKey rsaPublic = rsaKeyPair.getPublic();
			PrivateKey rsaPrivate = rsaKeyPair.getPrivate();
			System.out.println("公钥：" + bytesToHexStr(rsaPublic.getEncoded()));
			System.out.println("私钥：" + bytesToHexStr(rsaPrivate.getEncoded()));
			System.out.println("1024-bit RSA key GENERATED.");
		} catch (Exception e) {
			System.out.println("genRSAKeyPair：" + e);
		}
	}

	/**
	 * 公钥加密
	 * 
	 * @param srcToEncode
	 *            加密的明文
	 * @param pubKey
	 *            公钥
	 * @return  Base64编码后的密文
	 */
	public static String encode(String srcToEncode, String pubKey) {

		try {
			byte[] pubbyte = hexStrToBytes(pubKey.trim());
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubbyte);
			KeyFactory fac = KeyFactory.getInstance("RSA");
			RSAPublicKey rsaPubKey = (RSAPublicKey) fac.generatePublic(keySpec);

			BigInteger e = rsaPubKey.getPublicExponent();
			BigInteger n = rsaPubKey.getModulus();

			// 获得明文 ming
			byte[] plainText = srcToEncode.getBytes("UTF-8");
			BigInteger ming = new BigInteger(plainText);
			// 计算密文 coded
			BigInteger coded = ming.modPow(e, n);
			
			BigInteger decimal = new BigInteger(coded.toString());
			byte[] bytes = decimal.toByteArray();
			return Base64.encodeBase64String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 私钥解密
	 * 
	 * @param encodedSrc
	 *            base64编码过得解密的明文
	 * @param prikey
	 *            私钥
	 * @return
	 */
	public static String decode(String encodedSrc, String priKey) {
		try {
			byte[] pribyte = hexStrToBytes(priKey.trim());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pribyte);
			KeyFactory fac = KeyFactory.getInstance("RSA");
			RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
			
			// 获得私钥参数
			BigInteger n = privateKey.getModulus();
			BigInteger d = privateKey.getPrivateExponent();
			BigInteger big = new BigInteger(Base64.decodeBase64(encodedSrc));
			// 密文
			BigInteger coded = new BigInteger(big.toByteArray());
			BigInteger m = coded.modPow(d, n);
			// 打印解密结果
			byte[] result = m.toByteArray();
			String str = new String(result, "UTF-8");

			return str;
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将字节数组转换为16进制字符串的形式.
	 */
	private static final String bytesToHexStr(byte[] bcd) {
		StringBuffer s = new StringBuffer(bcd.length * 2);

		for (int i = 0; i < bcd.length; i++) {
			s.append(bcdLookup[(bcd[i] >>> 4) & 0x0f]);
			s.append(bcdLookup[bcd[i] & 0x0f]);
		}

		return s.toString();
	}

	/**
	 * 将16进制字符串还原为字节数组.
	 */
	private static final byte[] hexStrToBytes(String s) {
		byte[] bytes;

		bytes = new byte[s.length() / 2];

		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
		}

		return bytes;
	}

	private static final char[] bcdLookup = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	/**
	 * 本方法使用SHA1withRSA签名算法产生签名
	 * 
	 * @param String priKey 签名时使用的私钥(16进制编码)
	 * @param String src 签名的原字符串
	 * @return String 签名的返回结果(16进制编码)。当产生签名出错的时候，返回null。
	 */
	public static String generateSHA1withRSASigature(String priKey, String src) {
		try {

			Signature sigEng = Signature.getInstance("SHA1withRSA");

			byte[] pribyte = hexStrToBytes(priKey.trim());

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pribyte);
			KeyFactory fac = KeyFactory.getInstance("RSA");

			RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
			sigEng.initSign(privateKey);
			sigEng.update(src.getBytes());

			byte[] signature = sigEng.sign();
			return bytesToHexStr(signature);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 本方法使用SHA1withRSA签名算法验证签名
	 * 
	 * @param String
	 *            pubKey 验证签名时使用的公钥(16进制编码)
	 * @param String
	 *            sign 签名结果(16进制编码)
	 * @param String
	 *            src 签名的原字符串
	 * @return String 签名的返回结果(16进制编码)
	 */
	public static boolean verifySHA1withRSASigature(String pubKey, String sign, String src) {
		try {
			Signature sigEng = Signature.getInstance("SHA1withRSA");

			byte[] pubbyte = hexStrToBytes(pubKey.trim());

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubbyte);
			KeyFactory fac = KeyFactory.getInstance("RSA");
			RSAPublicKey rsaPubKey = (RSAPublicKey) fac.generatePublic(keySpec);

			sigEng.initVerify(rsaPubKey);
			sigEng.update(src.getBytes());

			byte[] sign1 = hexStrToBytes(sign);
			return sigEng.verify(sign1);

		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) {
		try {
			// genRSAKeyPair();
			 //String publicKey ="30819f300d06092a864886f70d010101050003818d00308189028181009844aba29bccb15cfc302029f0781930287e70ed4a09efb3dc61431e07dc10a482da36e805a97551b240b42db386d4e0302b22eabe5882c3dc798e56d7c3ded3770c35189a4c3cd3dbcbb8d79909d14345e36789d891af1129cb6481bd4ef1cf6c29d834f77d17d25590221082efc1046adfac3b475722d1c4706d34cc079df70203010001";
		     //String privateKey = "30820276020100300d06092a864886f70d0101010500048202603082025c020100028181009844aba29bccb15cfc302029f0781930287e70ed4a09efb3dc61431e07dc10a482da36e805a97551b240b42db386d4e0302b22eabe5882c3dc798e56d7c3ded3770c35189a4c3cd3dbcbb8d79909d14345e36789d891af1129cb6481bd4ef1cf6c29d834f77d17d25590221082efc1046adfac3b475722d1c4706d34cc079df7020301000102818002d50c283b23a48ce937eff137c2d41d1c33b5e004078948dc2986f911065bdce37e3195413db4abef57dac21adbc02be759760dcdba91bc18e6bdea099b6012a15c1b19e71d8722d690817a030995d0479f56945b0b1ead3f1500d69ab8a4038eecbdcca251c22d3cee71ee5df973197fb9d3ab28398e5e062dc48363181b59024100f31e09f4c0be68d622fd651f7b50bc933d9316be53f6c68a233cc092e2681a6ada54bc762cf51674a235cdda3b03244d99b3dc4e22e63de4cb37b0270b8be37b024100a0563c5be064d2dd3bc7635c71cf953db24a64999d7f0936391de1f7c496e2458056bdcaf259b3089fe37e40cd1ba371f89592d13bcfe752ec3eaef35f90d8b5024100a575cd3ad92efdf4b72d93f4201e2ba95e1fcc3bceb3461e5fab0a392d83c26ef496f508ccc224fd4e24a62c1f3b9d2a7f302d00b4b9d467f172be1d64bb591f024072aa598e4763339e3e43311f0ad1aee4a99e2465ebc9cf37b50f56ec4feeac31923ff689ae5a51fc880434e5790a1c10c4fb8b3d11ae74bbcbf26441ef0572c5024047e06b3835c1f76d4f5f0f0efc1a3e1aa0b4229a3f384764d78737f46e33163d8f5dba1546ccf49016a2befd9600c9c6c9591daa102eb52bd5f06b05a47d148a";
			
			//点车成金
		    String publicKey = "30819f300d06092a864886f70d010101050003818d00308189028181009bb9b0240bbaacd5e408b5a74d206e7eeae149f3c9e708dc5641574532296dde1893b03db02bdfb19f5960c4662509f3c1587b58aa8f1d3489ea8ac63f04156de6680000cd049884c764edfb43aff0fde253d000b28443f4001dce3f3970f50f3c9780914232ac32c4419096831c0cf85bd4972d640cf31026ff98223bd6489f0203010001";
			String privateKey = "30820276020100300d06092a864886f70d0101010500048202603082025c020100028181009bb9b0240bbaacd5e408b5a74d206e7eeae149f3c9e708dc5641574532296dde1893b03db02bdfb19f5960c4662509f3c1587b58aa8f1d3489ea8ac63f04156de6680000cd049884c764edfb43aff0fde253d000b28443f4001dce3f3970f50f3c9780914232ac32c4419096831c0cf85bd4972d640cf31026ff98223bd6489f02030100010281801f1514244db707f2755e8bab860878259b0a36b193562afd97b5d90e75b1b13d48588a2ff5eefbea3f2d1ed474b2e5e6a26bdfcad5d854f2fca834e4d70520a58c387b7af58ce67e1ed3e6803ff5149ee34ca445b12723b2f2004a5d8f8e9a948823550322577da2d10f29577c373140353f4a3aff3cfca28a7ed19b1ab48831024100cf9785929a3a645a1751e0acc286f1f4c0d0e5a19d8c51a3729e194690d2590c87e0eeabdcff1e5a3e00d6a888ac5bf8c5d2bfb8006e8144ae246f96962d3fc9024100c009ec52455866b77380f3e6c0c6e5f2734e1f7f997b996ca45f3b8540f3bce2d9d23d4dc6d8654f3e8721f3f9b7efa2a2f3f9f99df05e59e214343b02d789270240095dcfd40e8b65edbeb19e0e8d74634464d2c819a3af2a1bd2d71952dac3f2eaa2d2de51f8d5b5fbe2624d4d2b65837cd5082e485214aa567bf8fee3ef80b92902401ec538459270f0bc7258763c42255c90f5a2cdef3f238bd8d9999ccae43669cc9b84516855f5347e7711660256bfff38bb0d86bf556c3f61fd94a92dcf6dc3bf024100970fc4faffb504280e72040b91b69b875ed80e319831ebf537f2a5b9ede5692a370db2f21140b75314817cb6dff7ae2c34d6369fb8285ff24d84c1e7edfa1f37";
			

			// 加密
			String srcToEncode ="{\"mobileNo\":\"13575775451\",\"token\":\"XMDDXC2016030911073916a1f7e85df6\",\"cusId\":\"ofej_-3Y_iC6oh6RUVKKPzwwTwuY\",\"ch\":\"001\"}";
			String encoded = RSASignUtil.encode(srcToEncode, publicKey);
			// 解密
			String decode = RSASignUtil.decode(encoded, privateKey);
			System.out.println(decode);
			
			
			/*String sign=generateSHA1withRSASigature(privateKey,"123456");
			System.out.println(sign);
			boolean verify=verifySHA1withRSASigature(publicKey,sign,"123456");
			System.out.println(verify);*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
