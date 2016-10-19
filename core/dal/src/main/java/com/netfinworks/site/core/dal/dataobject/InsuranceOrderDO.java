package com.netfinworks.site.core.dal.dataobject;

import java.util.Date;

public class InsuranceOrderDO {
    private Long id;

    private String memberId;

    private String bxgsid;

    private String company;
    
    private String companyName;

    private String name;

    private String policytype;

    private String securitymoney;

    private String currencytype;

    private String applicant;

    private String atype;

    private String aidcard;

    private String aphone;

    private String binsured;

    private String btype;

    private String bidcard;

    private String bphone;

    private Date startdate;

    private Date enddate;

    private String status;

    private String speciltype;

    private String pid;

    private String liabilitys;

    private String otherinfo;

    private Date createTime;

    private String menucon;

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
        this.menucon = menucon == null ? null : menucon.trim();
    }
}