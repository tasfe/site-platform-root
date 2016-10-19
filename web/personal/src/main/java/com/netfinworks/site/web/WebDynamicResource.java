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

    /**静态资源地址*/
    private String            staticAddress;
    /**静态资源地址*/
    private String            staticVersion;
    private String            walletAddress;
    private String            resetPaypassWordUrl;
    /**企业钱包地址*/
    private String            enterWalletAddr;
    private String 			  registerPersonAddress;
    private String 			  registerEnterpriseAddress;
    /**钱包域名*/
    private String            staticAddressDomain;
    
    /** 验签地址 */
    private String			  signatureAddress;
    
    private String            vfssoDomain;
    
    /** 商户证书accountHash*/
    private String 		      enterpriseAccountHash;
    
    /** 会员个人证书accountHash*/
    private String			  personalPAccountHash;
    
    /** 会员企业证书accountHash*/
    private String			  personalEAccountHash;
    
    /** 网关证书accountHash */
    private String			  gatewayAccountHash;

	/** 文件上传路径 */
	private String uploadFilePath;
	
	/**话费充值异步调用地址*/
	private String rechargeReturnUrl;
	/**话费充值同步调用地址*/
	private String rechargeNotifyUrl;
	
	/**网关跳转地址*/
	private String gatewayUrl;
	/**话费充值商户ID地址*/
	private String rechargeEnterpriseMemberId;

    public String getRechargeEnterpriseMemberId() {
		return rechargeEnterpriseMemberId;
	}

	public void setRechargeEnterpriseMemberId(String rechargeEnterpriseMemberId) {
		this.rechargeEnterpriseMemberId = rechargeEnterpriseMemberId;
	}

	public String getGatewayUrl() {
		return gatewayUrl;
	}

	public void setGatewayUrl(String gatewayUrl) {
		this.gatewayUrl = gatewayUrl;
	}

	public String getRechargeReturnUrl() {
		return rechargeReturnUrl;
	}

	public void setRechargeReturnUrl(String rechargeReturnUrl) {
		this.rechargeReturnUrl = rechargeReturnUrl;
	}

	public String getRechargeNotifyUrl() {
		return rechargeNotifyUrl;
	}

	public void setRechargeNotifyUrl(String rechargeNotifyUrl) {
		this.rechargeNotifyUrl = rechargeNotifyUrl;
	}

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

    public String getEnterWalletAddr() {
        return enterWalletAddr;
    }

    public void setEnterWalletAddr(String enterWalletAddr) {
        this.enterWalletAddr = enterWalletAddr;
    }

	/**
	 * @return the registerPersonAddress
	 */
	public String getRegisterPersonAddress() {
		return registerPersonAddress;
	}

	/**
	 * @param registerPersonAddress the registerPersonAddress to set
	 */
	public void setRegisterPersonAddress(String registerPersonAddress) {
		this.registerPersonAddress = registerPersonAddress;
	}

	/**
	 * @return the registerEnterpriseAddress
	 */
	public String getRegisterEnterpriseAddress() {
		return registerEnterpriseAddress;
	}

	/**
	 * @param registerEnterpriseAddress the registerEnterpriseAddress to set
	 */
	public void setRegisterEnterpriseAddress(String registerEnterpriseAddress) {
		this.registerEnterpriseAddress = registerEnterpriseAddress;
	}

    public String getStaticAddressDomain() {
        return staticAddressDomain;
    }

    public void setStaticAddressDomain(String staticAddressDomain) {
        this.staticAddressDomain = staticAddressDomain;
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

	public String getSignatureAddress() {
		return signatureAddress;
	}

	public void setSignatureAddress(String signatureAddress) {
		this.signatureAddress = signatureAddress;
	}

	public String getUploadFilePath() {
		return uploadFilePath;
	}

	public void setUploadFilePath(String uploadFilePath) {
		this.uploadFilePath = uploadFilePath;
	}

}
