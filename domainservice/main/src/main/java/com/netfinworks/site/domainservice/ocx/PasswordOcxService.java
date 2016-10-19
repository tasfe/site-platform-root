package com.netfinworks.site.domainservice.ocx;

import java.util.Random;

import ocx.CryptoException;
import ocx.GetRandom;
import ocx.Rijndael;
import ocxrz.AESWithJCE;

import org.apache.commons.codec.binary.Base64;


/**
 * <p>密码控件service</p>
 * @author yangshihong
 * @version $Id: PasswordOcxService.java, v 0.1 2014年5月30日 下午7:54:30 hdfs Exp $
 */
public class PasswordOcxService {
    public static final String allChar = "xlzxhxjnyj5u0evam0cmc8zkpxvg28ok"; 
    
    /**
     * 生成32位随机数
     * @param length
     * @return
     */
    public String generateRandomKey(int length){
        StringBuffer sb = new StringBuffer(); 
        Random random = new Random(); 
        for (int i = 0; i < length; i++) { 
        	sb.append(allChar.charAt(random.nextInt(allChar.length()))); 
        //	sb.append(allChar.charAt(i));
        }
        return sb.toString();
    }
  
    /**
     * 密码解密
     * @param mcrypt_key 随机因子
     * @param encryPassword 控件已加密密码
     * @return
     */
    public String decrpPassword(String mcrypt_key,String encryPassword){
		if (mcrypt_key != null) {
				return ocx.AESWithJCE.getResult(mcrypt_key, encryPassword);
		} else {
			return "";
		}
    }
    
    
    /**
     * 通过随机数获取加密串
     * @param skey
     * @return
     */
    public String getCipher(String skey){
    	return AESWithJCE.getCipher(skey,skey);
    }
    
    public String getSkey(){
    	return GetRandom.generateString(32);
    }
    
    public static void main(String[] args) {
    	System.out.println(GetRandom.generateString(32));
    	System.out.println(ocx.AESWithJCE.getResult("85697293716979432441496878482363", "2yzhCfP66Pg8JqHxZjxwsg=="));
	}
}
