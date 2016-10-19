package com.netfinworks.site.domain.domain.response;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * 登陆响应
 * @author zhaozq
 * @date 2015年6月25日
 */
public class LoginNameEditResp extends UnionmaBaseResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1960362288444719109L;

	private String memberId; // 会员Id
	private String accountId; // 账户Id
	private String operatorId; // 操作员Id
	private String hryId; // 海融易Id
	private String email; // 电子邮件
	private String mobile; // 手机号
	private String memberType; // 账户类型
	private boolean isUnionAccount = false; // 是否是统一账户
	private boolean isBindKjt = false; // 是否绑定永达互联网金融账户
	private boolean accountUpgrading = false; // 统一账户首次登陆做账户升级时,设置该flag为true
	private Integer accountStatus; // 账户状态
	private String registerSource; // 注册来源   KJT HRY
	private Integer accountType; // 这里账户的状态主要是原账户装状态 账户首次登录时才有 以后默认都是0
	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public boolean getIsUnionAccount() {
		return isUnionAccount;
	}

	public void setIsUnionAccount(boolean isUnionAccount) {
		this.isUnionAccount = isUnionAccount;
	}

	public String getHryId() {
		return hryId;
	}

	public void setHryId(String hryId) {
		this.hryId = hryId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public boolean isBindKjt() {
		return isBindKjt;
	}

	public void setBindKjt(boolean isBindKjt) {
		this.isBindKjt = isBindKjt;
	}

	public boolean isAccountUpgrading() {
		return accountUpgrading;
	}

	public void setAccountUpgrading(boolean accountUpgrading) {
		this.accountUpgrading = accountUpgrading;
	}

	public Integer getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(Integer accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getRegisterSource() {
		return registerSource;
	}

	public void setRegisterSource(String registerSource) {
		this.registerSource = registerSource;
	}

	public void setUnionAccount(boolean isUnionAccount) {
		this.isUnionAccount = isUnionAccount;
	}

	public Integer getAccountType() {
		return accountType;
	}

	public void setAccountType(Integer accountType) {
		this.accountType = accountType;
	}

	public String getMemberType() {
		return memberType;
	}

	public void setMemberType(String memberType) {
		this.memberType = memberType;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	
}
