package com.netfinworks.site.domain.domain.request;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * 数字证书请求参数
 * @author chengwen
 *
 */
public class CertificationInfoRequest extends OperationEnvironment implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6626671447333771405L;
	
	/**	会员号 */
	private String memberId;
	
	/** 操作员 :
	 * 		非网关证书必填
	 */
	private String operatorId;
	
	/** 证书请求:
	 *		必填 
	 */
	private String csr;
	
	/**
	 *  地点: 
	 * 		非硬证书,网关证书必填
	 */
	private String position;
	
	/**	
	 * 用户ip:
	 * 		非网关证书必填
	 */
	private String userIp;
	
	/** 
	 * 用户mac:
	 * 		非网关证书必填
	 */
	private String userMac;
	
	/** 申请时间 */
	private Date requestTime;
	
	/**
	 * 手机号:
	 * 		硬证书必填
	 */
	private String mobileNo;
	
	/**
	 * 证书类型:
	 * soft:软证书,
	 * hard:硬证书,
	 * gateway:网关证书
	 */
	private String certificationType;
	
	/**
	 * 证书状态
	 * 		激活：ACTIVATED
	 * 		解绑：UNBIND 
	 * 		注销：REVOKED）
	 */
	private String certificationStatus;
	
	/**
	 * 硬证书序列号:
	 * 		硬证书必填
	 */
	private String certSn;
	
	/**
	 * 账号hash
	 */
	private String accountHash;
	
	/**
	 * 原因
	 */
	private String reason;
	
	/**
	 * 有效期开始
	 */
	private Date effectiveBegin;
	
	/**
	 * 有效期结束
	 */
	private Date effectiveEnd;
	
	/**
	 * 扩展信息
	 */
	private String ext;
	
	/**
	 * 组织(开发环境默认填itrus)
	 */
	private String org;
	
	/**
	 * 组织单位(开发环境默认填RA)
	 */
	private String orgUnit;
	
	/**
	 * 旧证书信息(objOldCert.GetEncodedCert(2))
	 */
	private String origCert;
	
	/**
	 * 旧证书对CSR签名
	 */
	private String pkcsInfo;
	
	/**
	 * 新证书有效期天数
	 */
	private String certValidity;
	
	/**
	 * 硬证书序列号
	 */
	private String serialNo;
	
	public String getExt() {
		return ext;
	}


	public void setExt(String ext) {
		this.ext = ext;
	}


	public String getMemberId() {
		return memberId;
	}


	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}


	public String getOperatorId() {
		return operatorId;
	}


	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}


	public String getCsr() {
		return csr;
	}


	public void setCsr(String csr) {
		this.csr = csr;
	}


	public String getPosition() {
		return position;
	}


	public void setPosition(String position) {
		this.position = position;
	}


	public String getUserIp() {
		return userIp;
	}


	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}


	public String getUserMac() {
		return userMac;
	}


	public void setUserMac(String userMac) {
		this.userMac = userMac;
	}


	public Date getRequestTime() {
		return requestTime;
	}


	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}


	public String getMobileNo() {
		return mobileNo;
	}


	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}


	public String getCertificationType() {
		return certificationType;
	}


	public void setCertificationType(String certificationType) {
		this.certificationType = certificationType;
	}
	

	public String getCertificationStatus() {
		return certificationStatus;
	}


	public void setCertificationStatus(String certificationStatus) {
		this.certificationStatus = certificationStatus;
	}


	public String getCertSn() {
		return certSn;
	}


	public void setCertSn(String certSn) {
		this.certSn = certSn;
	}


	public String getAccountHash() {
		return accountHash;
	}


	public void setAccountHash(String accountHash) {
		this.accountHash = accountHash;
	}


	public Date getEffectiveBegin() {
		return effectiveBegin;
	}


	public void setEffectiveBegin(Date effectiveBegin) {
		this.effectiveBegin = effectiveBegin;
	}


	public Date getEffectiveEnd() {
		return effectiveEnd;
	}


	public void setEffectiveEnd(Date effectiveEnd) {
		this.effectiveEnd = effectiveEnd;
	}

	public String getReason() {
		return reason;
	}


	public void setReason(String reason) {
		this.reason = reason;
	}
	

	public String getOrg() {
		return org;
	}


	public void setOrg(String org) {
		this.org = org;
	}


	public String getOrgUnit() {
		return orgUnit;
	}


	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	
	public String getOrigCert() {
		return origCert;
	}


	public void setOrigCert(String origCert) {
		this.origCert = origCert;
	}


	public String getPkcsInfo() {
		return pkcsInfo;
	}


	public void setPkcsInfo(String pkcsInfo) {
		this.pkcsInfo = pkcsInfo;
	}


	public String getCertValidity() {
		return certValidity;
	}


	public void setCertValidity(String certValidity) {
		this.certValidity = certValidity;
	}

	
	public String getSerialNo() {
		return serialNo;
	}


	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}


	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
