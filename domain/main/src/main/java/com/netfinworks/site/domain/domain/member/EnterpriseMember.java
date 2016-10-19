/**
 *
 */
package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>企业会员</p>
 * @author qinde
 * @version $Id: EnterpriseMember.java, v 0.1 2013-12-10 上午10:50:49 qinde Exp $
 */
public class EnterpriseMember extends BaseMember implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7173593895779461514L;
    /**企业名称*/
	private String enterpriseName;
    /**企业法人*/
	private String legalPerson;
    /**法人手机号码*/
	private String legalPersonPhone;
    /**企业网址*/
	private String website;
    /**企业地址*/
	private String address;
    /**执照号*/
	private String licenseNo;
    /**营业执照所在地*/
	private String licenseAddress;
    /**执照过期日（营业期限）*/
	private Date licenseExpireDate;
    /**营业范围*/
	private String businessScope;
    /**联系电话*/
	private String telephone;
    /**组织机构代码*/
	private String organizationNo;
    /**企业简介*/
	private String summary;
    /**退出地址*/
	private String logoutUrl;
	/** 操作员登录名 */
	private String operator_login_name;
	/** 营业执照副本扫描件地址 */
	private String licenseNoPath;
	/** 组织机构代码证扫描件地址 */
	private String organizationNoPath;
	/** 税务登记证扫描件地址 */
	private String taxNoPath;
	/** ICP证（备案）号 */
	private String icpLicensePath;
	/** 经营网址 */
	private String businessWebsite;
	/** 身份证有效期 */
	private Date legalPersonIdValidDate;
	/** 企业类型 */
	private Integer companyType;
	/** 登记证号名称 */
	private String licenseName;
	/** 代办人名称 */
	private String proxyPerson;
	/** 企业网址 */
	private String companyName;

	public String getEnterpriseName() {
		return enterpriseName;
	}

	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
	}

	public String getLegalPerson() {
		return legalPerson;
	}

	public void setLegalPerson(String legalPerson) {
		this.legalPerson = legalPerson;
	}

	public String getLegalPersonPhone() {
		return legalPersonPhone;
	}

	public void setLegalPersonPhone(String legalPersonPhone) {
		this.legalPersonPhone = legalPersonPhone;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLicenseNo() {
		return licenseNo;
	}

	public void setLicenseNo(String licenseNo) {
		this.licenseNo = licenseNo;
	}

	public String getLicenseAddress() {
		return licenseAddress;
	}

	public void setLicenseAddress(String licenseAddress) {
		this.licenseAddress = licenseAddress;
	}

	public Date getLicenseExpireDate() {
		return licenseExpireDate;
	}

	public void setLicenseExpireDate(Date licenseExpireDate) {
		this.licenseExpireDate = licenseExpireDate;
	}

	public String getBusinessScope() {
		return businessScope;
	}

	public void setBusinessScope(String businessScope) {
		this.businessScope = businessScope;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getOrganizationNo() {
		return organizationNo;
	}

	public void setOrganizationNo(String organizationNo) {
		this.organizationNo = organizationNo;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Override
	public String getLogoutUrl() {
		return logoutUrl;
	}

	@Override
	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getOperator_login_name() {
		return operator_login_name;
	}

	public void setOperator_login_name(String operator_login_name) {
		this.operator_login_name = operator_login_name;
	}

	public String getLicenseNoPath() {
		return licenseNoPath;
	}

	public void setLicenseNoPath(String licenseNoPath) {
		this.licenseNoPath = licenseNoPath;
	}

	public String getOrganizationNoPath() {
		return organizationNoPath;
	}

	public void setOrganizationNoPath(String organizationNoPath) {
		this.organizationNoPath = organizationNoPath;
	}

	public String getTaxNoPath() {
		return taxNoPath;
	}

	public void setTaxNoPath(String taxNoPath) {
		this.taxNoPath = taxNoPath;
	}

	public String getIcpLicensePath() {
		return icpLicensePath;
	}

	public void setIcpLicensePath(String icpLicensePath) {
		this.icpLicensePath = icpLicensePath;
	}

	public String getBusinessWebsite() {
		return businessWebsite;
	}

	public void setBusinessWebsite(String businessWebsite) {
		this.businessWebsite = businessWebsite;
	}

	public Date getLegalPersonIdValidDate() {
		return legalPersonIdValidDate;
	}

	public void setLegalPersonIdValidDate(Date legalPersonIdValidDate) {
		this.legalPersonIdValidDate = legalPersonIdValidDate;
	}

	public Integer getCompanyType() {
		return companyType;
	}

	public void setCompanyType(Integer companyType) {
		this.companyType = companyType;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	public String getProxyPerson() {
		return proxyPerson;
	}

	public void setProxyPerson(String proxyPerson) {
		this.proxyPerson = proxyPerson;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
