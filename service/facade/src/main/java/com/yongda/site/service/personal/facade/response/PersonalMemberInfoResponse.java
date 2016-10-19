package com.yongda.site.service.personal.facade.response;

public class PersonalMemberInfoResponse {


	/**钱包会员名称*/
    private String                memberName; 
    
    /**手机号*/
    private String                mobile;
    
    /**会员等级*/
    private String                level;
    
    
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
