package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.Date;

/**
 * 证书详情
 * @author chengwen
 *
 */
public class CertificationDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6439475625111508766L;
	
	/**
     * 会员号
     */
    private String memberId;
    /**
     *操作员
     */
    private String operatorId;
    /**
     *证书请求
     */
    private String csr;
    /**
     *地点
     */
    private String position;
    /**
     *用户ip
     */
    private String userIp;
    /**
     *用户mac
     */
    private String userMac;
    /**
     *申请时间
     */
    private Date requestTime;
    /**
     *手机号
     */
    private String mobileNo;
    /**
     *证书类型
     */
    private String certificationType;
    /**
     *证书序列号
     */
    private String certSn;
    /**
     *证书状态
     */
    private String status;
    /**
     *扩展信息
     */
    private String extension;
    /**
     * 是否挂失
     */
    private Boolean isReportLoss;
    /**
     * 挂失时间
     */
    private Date reportLossDate;
    /**
     * 注销时间
     */
    private Date revokeDate;
    /**
     * 证书内容
     */
    private String certData;

    /**
     * 有效期开始
     */
    private Date effectiveBegin;
    /**
     * 有效期结束
     */
    private Date effectiveEnd;
    /**
     * 账号hash
     */
    private String accountHash;
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
	public String getCertSn() {
		return certSn;
	}
	public void setCertSn(String certSn) {
		this.certSn = certSn;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public Boolean getIsReportLoss() {
		return isReportLoss;
	}
	public void setIsReportLoss(Boolean isReportLoss) {
		this.isReportLoss = isReportLoss;
	}
	public Date getReportLossDate() {
		return reportLossDate;
	}
	public void setReportLossDate(Date reportLossDate) {
		this.reportLossDate = reportLossDate;
	}
	public Date getRevokeDate() {
		return revokeDate;
	}
	public void setRevokeDate(Date revokeDate) {
		this.revokeDate = revokeDate;
	}
	public String getCertData() {
		return certData;
	}
	public void setCertData(String certData) {
		this.certData = certData;
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
	public String getAccountHash() {
		return accountHash;
	}
	public void setAccountHash(String accountHash) {
		this.accountHash = accountHash;
	}
    
    
}
