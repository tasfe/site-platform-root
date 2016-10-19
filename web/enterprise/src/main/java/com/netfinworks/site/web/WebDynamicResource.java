/**
 *
 */
package com.netfinworks.site.web;

import java.io.Serializable;

/**
 * @title
 * @description
 * @usage
 * @company		上海微汇信息技术有限公司
 * @author		TQSUMMER
 * @create		2012-12-14 下午7:06:17
 */
/**
 * @author TQSUMMER
 */
public class WebDynamicResource implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6728626598811865624L;

    private String            staticAddress;
    private String            staticVersion;
    private String            walletAddress;
    private String            resetPaypassWordUrl;
    /**个人钱包地址*/
    private String            personalWalletAddr;

    private String            sourceId;
    /**钱包域名*/
    private String            staticAddressDomain;

    private String            vfssoDomain;
    
    /** 验签地址 */
    private String			  signatureAddress;
    
    /** 商户证书accountHash*/
    private String 		      enterpriseAccountHash;
    
    /** 会员个人证书accountHash*/
    private String			  personalPAccountHash;
    
    /** 会员企业证书accountHash*/
    private String			  personalEAccountHash;
    
    /** 网关证书accountHash */
    private String			  gatewayAccountHash;
    
	/** 申请角色 */
	private String applyrole;

	/** 审核角色 */
	private String authrole;

	/** 文件上传路径 */
	private String uploadFilePath;
	
	/** 电子签章证书路径*/
	private String signaturePath;
	
	/** 电子签章密码*/
	private String keystorePwd1;
	
	/** 电子签章密码*/
	private String keystorePwd2;
	
	/** 签章服务器地址*/
	private String keystoreUrl;
	
	/**
	 * 云POS服务地址
	 */
	private String yunPosUrl;
	/**连连POS会员id */
	private String llPOSMemberId;
    public String getVfssoDomain() {
        return vfssoDomain;
    }

    public void setVfssoDomain(String vfssoDomain) {
        this.vfssoDomain = vfssoDomain;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getStaticAddress() {
        return staticAddress;
    }

    public void setStaticAddress(String staticAddress) {
        this.staticAddress = staticAddress;
    }

    public String getStaticVersion() {
        return staticVersion;
    }

    public void setStaticVersion(String staticVersion) {
        this.staticVersion = staticVersion;
    }

    public String getResetPaypassWordUrl() {
        return resetPaypassWordUrl;
    }

    public void setResetPaypassWordUrl(String resetPaypassWordUrl) {
        this.resetPaypassWordUrl = resetPaypassWordUrl;
    }

    public String getPersonalWalletAddr() {
        return personalWalletAddr;
    }

    public void setPersonalWalletAddr(String personalWalletAddr) {
        this.personalWalletAddr = personalWalletAddr;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getStaticAddressDomain() {
        return staticAddressDomain;
    }

    public void setStaticAddressDomain(String staticAddressDomain) {
        this.staticAddressDomain = staticAddressDomain;
    }

	public String getSignatureAddress() {
		return signatureAddress;
	}

	public void setSignatureAddress(String signatureAddress) {
		this.signatureAddress = signatureAddress;
	}

	public String getEnterpriseAccountHash() {
		return enterpriseAccountHash;
	}

	public void setEnterpriseAccountHash(String enterpriseAccountHash) {
		this.enterpriseAccountHash = enterpriseAccountHash;
	}

	public String getPersonalPAccountHash() {
		return personalPAccountHash;
	}

	public void setPersonalPAccountHash(String personalPAccountHash) {
		this.personalPAccountHash = personalPAccountHash;
	}

	public String getPersonalEAccountHash() {
		return personalEAccountHash;
	}

	public void setPersonalEAccountHash(String personalEAccountHash) {
		this.personalEAccountHash = personalEAccountHash;
	}

	public String getGatewayAccountHash() {
		return gatewayAccountHash;
	}

	public void setGatewayAccountHash(String gatewayAccountHash) {
		this.gatewayAccountHash = gatewayAccountHash;
	}

	public String getApplyrole() {
		return applyrole;
	}

	public void setApplyrole(String applyrole) {
		this.applyrole = applyrole;
	}

	public String getAuthrole() {
		return authrole;
	}

	public void setAuthrole(String authrole) {
		this.authrole = authrole;
	}

	public String getUploadFilePath() {
		return uploadFilePath;
	}

	public void setUploadFilePath(String uploadFilePath) {
		this.uploadFilePath = uploadFilePath;
	}

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public String getKeystorePwd1() {
        return keystorePwd1;
    }

    public void setKeystorePwd1(String keystorePwd1) {
        this.keystorePwd1 = keystorePwd1;
    }

    public String getKeystorePwd2() {
        return keystorePwd2;
    }

    public void setKeystorePwd2(String keystorePwd2) {
        this.keystorePwd2 = keystorePwd2;
    }

    public String getKeystoreUrl() {
        return keystoreUrl;
    }

    public void setKeystoreUrl(String keystoreUrl) {
        this.keystoreUrl = keystoreUrl;
    }

	public String getYunPosUrl() {
		return yunPosUrl;
	}

	public void setYunPosUrl(String yunPosUrl) {
		this.yunPosUrl = yunPosUrl;
	}

	public String getLlPOSMemberId() {
		return llPOSMemberId;
	}

	public void setLlPOSMemberId(String llPOSMemberId) {
		this.llPOSMemberId = llPOSMemberId;
	}
	
}
