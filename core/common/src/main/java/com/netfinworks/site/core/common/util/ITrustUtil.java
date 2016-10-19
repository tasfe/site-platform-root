package com.netfinworks.site.core.common.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itrus.cryptorole.CryptoException;
import com.itrus.cryptorole.NotSupportException;
import com.itrus.cryptorole.bc.RecipientBcImpl;
import com.itrus.cryptorole.bc.SenderBcImpl;
import com.itrus.util.Base64;

public class ITrustUtil {

	private final Logger logger = LoggerFactory.getLogger(ITrustUtil.class);

	public static SenderBcImpl sender;
	public static RecipientBcImpl recipient;

	/**
	 * 无参构造函数
	 */
	public ITrustUtil() {
		super();
	}

	/**
	 * 初始化
	 * 
	 * @param pfxFileName
	 * @param certFileName
	 * @param certFileName
	 * @throws CryptoException
	 * @throws NotSupportException
	 */
	public ITrustUtil(String pfxFileName, String certFileName, String keyPassword) throws Exception {

		sender = new SenderBcImpl();
		recipient = new RecipientBcImpl();
		sender.initCertWithKey(pfxFileName, keyPassword);
		recipient.initCertWithKey(pfxFileName, keyPassword);

		InputStream streamCert = new FileInputStream(certFileName);
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate X509Cert = (X509Certificate) factory.generateCertificate(streamCert);
		sender.addRecipientCert(X509Cert);
	}

	/**
	 * 除去数组中的空值和签名参数
	 * 
	 * @param sArray
	 *            签名参数组
	 * @return 去掉空值与签名参数后的新签名参数组
	 */
	public Map<String, String> paraFilter(Map<String, String> sArray) {

		Map<String, String> result = new HashMap<String, String>();

		if ((sArray == null) || (sArray.size() <= 0)) {
			return result;
		}

		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if ((value == null) || value.equals("") || key.equalsIgnoreCase("sign")
					|| key.equalsIgnoreCase("sign_type")) {
				continue;
			}
			result.put(key, value);
		}

		return result;
	}

	/**
	 * 转码
	 * 
	 * @param sArray
	 * @return
	 */
	public Map<String, String> encode(Map<String, String> sArray) {

		Map<String, String> result = new HashMap<String, String>();

		if ((sArray == null) || (sArray.size() <= 0)) {
			return result;
		}
		String charset = sArray.get("_input_charset");
		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if ((value != null) && !value.equals("")) {
				try {
					value = URLEncoder.encode(value, charset);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			result.put(key, value);
		}

		return result;
	}

	/**
	 * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
	 * 
	 * @param params
	 *            需要排序并参与字符拼接的参数组
	 * @param encode 是否需要urlEncode
	 * @return 拼接后字符串
	 */
	public String createLinkString(Map<String, String> params, boolean encode) {

		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		String charset = params.get("_input_charset");
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if (encode) {
				try {
					value = URLEncoder.encode(value, charset);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			if (i == (keys.size() - 1)) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}

		return prestr;
	}

	public String buildRequest(Map<String, String> sPara) throws Exception {
		String prestr = createLinkString(sPara, false); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
		String mysign = sender.signMessage(prestr);
		return mysign;
	}

	/**
	 * 生成要请求给钱包的参数数组
	 * 
	 * @param sParaTemp 请求前的参数数组
	 * @return 要请求的参数数组
	 */
	public Map<String, String> buildRequestPara(Map<String, String> sParaTemp, String signType, String key,
			String inputCharset) throws Exception {
		// 除去数组中的空值和签名参数
		Map<String, String> sPara = paraFilter(sParaTemp);
		if (StringUtils.isBlank(signType)) {
			return sPara;
		}
		// 生成签名结果
		String mysign = buildRequest(sPara);
		// 签名结果与签名方式加入请求提交参数组中
		sPara.put("sign", mysign);
		sPara.put("sign_type", signType);

		return encode(sPara);
	}

	// 天威加密
	public String encryptData(String oriMessage, String inputCharset) throws Exception {
		byte[] encryMsg = sender.encryptMessage(oriMessage.getBytes(inputCharset));
		return Base64.encode(encryMsg);
	}

	// 天威解密
	public String decryptData(String oriMessage, String inputCharset) throws Exception {
		byte[] decryMsg = recipient.decryptMessage(Base64.decode(oriMessage.getBytes(inputCharset)));
		return new String(decryMsg, "UTF-8");
	}
	
	// 判断快捷盾是否在可用范围内
	public static boolean isInRange(String serialNo){
	    if((serialNo.compareTo("TW00511506007200")>=0 && (serialNo.compareTo("TW00511506007401")<=0))){
	        return true;//在范围
	    }else{
	        return false;//不在范围
	    }
	}
}
