package com.yongda.site.wallet.app.verify;

import com.yongda.site.wallet.app.util.itrus.ITrus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


/**
 * @author 
 * @version 
 * 验签
 */
public class verifyClient {
	
	private static final Logger logger = LoggerFactory.getLogger(verifyClient.class);
	
	static ITrus itrus = new ITrus();
 
    static{
    	itrus.setCvmConfigFile("/opt/pay/config/basis/site/personal/cert/cvm.xml");
    	itrus.setKeyPassword("123456");
    	//测试
        //itrus.setPfxFileName("/opt/pay/config/basis/site/personal/cert/suka.pfx");
        //开发
        itrus.setPfxFileName("/opt/pay/config/basis/site/personal/cert/200000050543.pfx");

    	itrus.init();
    }
	/**
     * 验签
     * @param charset
     * @param formattedParameters
     */
    public static VerifyResult verifyBasic(String charset,Map<String, String> formattedParameters) throws Exception {
    	logger.info("验证签名");
    	
        //拼接签名字符串
        String signContent = createLinkString(paraFilter(formattedParameters),
            false);

        //传过来的签名
        String signMsg = formattedParameters.get("sign");
        if (logger.isInfoEnabled()) {
            logger.info("verify signature: { content:" + signContent + ", signMsg:"+ signMsg+ "}");
        }

        //传入签名字符串、密钥、字符集、签名方式
        VerifyResult result = verifyParameters(signContent, signMsg, charset);
        if (!result.isSuccess()) {
            //验签未通过
            logger.error(";request dosen't pass verify.");
            throw new Exception("验签未通过");
        }

        String identityNo = formattedParameters.get("identity_no");
        //验证结果为需要进行证书持有者校验 且 identityNo不为空，则校验会员标识是否是证书的持有者
        if (result.isNeedPostCheck() && StringUtils.isNotBlank(identityNo)) {
            Map<String, Object> map = result.getInfo();
            if (map != null) {
                if (!identityNo.equals(map.get(VerifyResult.identityNo))) {
                    logger.error("会员标识与证书持有者不匹配,会员标识："+identityNo+"，证书持有者："+map.get(VerifyResult.identityNo));
                    throw new Exception("会员标识与证书持有者不匹配");
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("invoke verify end:" + result.isSuccess());
        }
        
        return result;
    }
    
    public  static VerifyResult verifyParameters(String content, String signature, String charset) throws Exception {
		if (signature != null) {
			signature = signature.replace(' ', '+');
		}
		
		VerifyResult result = new VerifyResult();

		try {
			
			result = itrus.verify(content, signature, null, charset);
			
		} catch (Exception e) {
			logger.error("verify failure for content:" + content + ",signature:" + signature , e);
			throw new Exception("签名失败");
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
    public static String createLinkString(Map<String, String> params, boolean encode) {

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

            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }
    
    /**
     * 除去数组中的空值和签名参数
     *
     * @param sArray
     *            签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }
}
