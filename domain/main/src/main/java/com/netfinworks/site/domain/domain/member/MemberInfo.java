package com.netfinworks.site.domain.domain.member;

import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 *
 * <p>查询会员系统后，组装的会员信息</p>
 * @author leelun
 * @version $Id: MemberInfo.java, v 0.1 2013-11-22 上午11:06:35 lilun Exp $
 */
public class MemberInfo {

    String      memberId;//会员编号
    String      baseAccountId;// 基本户账户编号
    String      memberIdentity;
    String      platformType;
    Long        memberType;    //会员类型 
    String      memberName;
    AccountInfo accountInfo;//基本账户信息
    String      operatorId;
    List<com.netfinworks.ma.service.response.AccountInfo>accounts;//会员账户集合
    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBaseAccountId() {
        return baseAccountId;
    }

    public void setBaseAccountId(String baseAccountId) {
        this.baseAccountId = baseAccountId;
    }

    public String getMemberIdentity() {
        return memberIdentity;
    }

    public void setMemberIdentity(String memberIdentity) {
        this.memberIdentity = memberIdentity;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }

    public void setAccountInfo(String accountType, String accountId) {
        this.accountInfo = new AccountInfo(accountType, accountId);
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public boolean hasValidAccountInfo() {
        return null != this.accountInfo && accountInfo.isValid();
    }

    public Long getMemberType() {
        return memberType;
    }

    public void setMemberType(Long memberType) {
        this.memberType = memberType;
    }

	public List<com.netfinworks.ma.service.response.AccountInfo> getAccounts() {
		return accounts;
	}

	public void setAccounts(
			List<com.netfinworks.ma.service.response.AccountInfo> accounts) {
		this.accounts = accounts;
	}

	public void setAccountInfo(AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}
    
}
