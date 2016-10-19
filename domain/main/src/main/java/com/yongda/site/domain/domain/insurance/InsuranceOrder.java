package com.yongda.site.domain.domain.insurance;

import java.util.Date;

public class InsuranceOrder {
	private Long id;
    /** 会员ID */
    private String memberId;

    /** 保单号 */
    private String bxgsid;

    /** 保险公司  TPY－太平洋财险、RB－人保财险、PA－平安产险、YG－阳光财险、ZHLH－中华联合、ASTP－安盛天平、ZSCX－浙商财险、TACX－天安财险*/
    private String company;

    private String companyName;
    
    /** 保险名称 */
    private String name;

    /** 保险产品类型 */
    private String policytype;

    /** 保险保额 */
    private String securitymoney;

    /** 货币类型 */
    private String currencytype;

    /** 投保人姓名 */
    private String applicant;

    /** 投保人证件类型 */
    private String atype;

    /** 投保人证件号 */
    private String aidcard;

    /** 投保人手机号 */
    private String aphone;

    /** 被保人姓名 */
    private String binsured;

    /** 被保人证件类型 */
    private String btype;

    /** 被保人证件号 */
    private String bidcard;

    /** 被保人手机号 */
    private String bphone;

    /** 保单开始时间 */
    private Date startdate;

    /** 保单结束时间 */
    private Date enddate;

    /** 保单状态 */
    private String status;

    /** 特殊保单类型 0普通险种 1个人车险 */
    private String speciltype;

    /** 产品ID */
    private String pid;

    /** 责任信息 json */
    private String liabilitys;
    
    private String menucon;

    /** 其他 */
    private String otherinfo;

    private Date createTime;
    
    public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId == null ? null : memberId.trim();
    }

    public String getBxgsid() {
        return bxgsid;
    }

    public void setBxgsid(String bxgsid) {
        this.bxgsid = bxgsid == null ? null : bxgsid.trim();
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company == null ? null : company.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPolicytype() {
        return policytype;
    }

    public void setPolicytype(String policytype) {
        this.policytype = policytype == null ? null : policytype.trim();
    }

    public String getSecuritymoney() {
        return securitymoney;
    }

    public void setSecuritymoney(String securitymoney) {
        this.securitymoney = securitymoney == null ? null : securitymoney.trim();
    }

    public String getCurrencytype() {
        return currencytype;
    }

    public void setCurrencytype(String currencytype) {
        this.currencytype = currencytype == null ? null : currencytype.trim();
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant == null ? null : applicant.trim();
    }

    public String getAtype() {
        return atype;
    }

    public void setAtype(String atype) {
        this.atype = atype == null ? null : atype.trim();
    }

    public String getAidcard() {
        return aidcard;
    }

    public void setAidcard(String aidcard) {
        this.aidcard = aidcard == null ? null : aidcard.trim();
    }

    public String getAphone() {
        return aphone;
    }

    public void setAphone(String aphone) {
        this.aphone = aphone == null ? null : aphone.trim();
    }

    public String getBinsured() {
        return binsured;
    }

    public void setBinsured(String binsured) {
        this.binsured = binsured == null ? null : binsured.trim();
    }

    public String getBtype() {
        return btype;
    }

    public void setBtype(String btype) {
        this.btype = btype == null ? null : btype.trim();
    }

    public String getBidcard() {
        return bidcard;
    }

    public void setBidcard(String bidcard) {
        this.bidcard = bidcard == null ? null : bidcard.trim();
    }

    public String getBphone() {
        return bphone;
    }

    public void setBphone(String bphone) {
        this.bphone = bphone == null ? null : bphone.trim();
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getSpeciltype() {
        return speciltype;
    }

    public void setSpeciltype(String speciltype) {
        this.speciltype = speciltype == null ? null : speciltype.trim();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid == null ? null : pid.trim();
    }

    public String getLiabilitys() {
        return liabilitys;
    }

    public void setLiabilitys(String liabilitys) {
        this.liabilitys = liabilitys == null ? null : liabilitys.trim();
    }

    public String getOtherinfo() {
        return otherinfo;
    }

    public void setOtherinfo(String otherinfo) {
        this.otherinfo = otherinfo == null ? null : otherinfo.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

	public String getMenucon() {
		return menucon;
	}

	public void setMenucon(String menucon) {
		this.menucon = menucon;
	}
}