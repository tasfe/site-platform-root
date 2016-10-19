package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;

/**
 *
 * <p>会员基本信息</p>
 * @author qinde
 * @version $Id: BaseMember.java, v 0.1 2013-12-5 下午8:04:40 qinde Exp $
 */
public class BaseMember implements Serializable {
    /**
     *
     */
    private static final long     serialVersionUID = 9203206221294683039L;
    /**钱包会员id*/
    private String                memberId;
    /**钱包会员名称*/
    private String                memberName;
    /**平台的userID*/
    private String                plateFormId;
    /**平台的loginName*/
    private String                plateName;
    /**手机号*/
    private String                mobile;
    /**手机号加星*/
    private String                mobileStar;
    /**手机号加密*/
    private String                mobileTicket;
    /**电子邮件*/
    private String                email;
    /**账户余额*/
    private MemberAccount         account;
    /**会员卡数量*/
    private int                   cardCount;
    /**最后登录时间*/
    private String                lastLoginTime;
    /**会员状态*/
    private MemberStatus          status;
    /**会员锁定状态*/
    private MemberLockStatus      lockStatus;
    /**会员支付密码状态*/
    private MemberPaypasswdStatus paypasswdstatus;
    /**默认的操作员Id*/
    private String                operatorId;
    /**当前操作员Id*/
    private String                currentOperatorId;
    /**缺省账户Id*/
    private String                defaultAccountId;
    /**支付密码*/
    private String                paypasswd;
    /**会员类型 */
    private MemberType            memberType;
    /**头像路径*/
    private String                faceImageUrl;
    /**退出地址*/
    private String                logoutUrl;
    /**注册地址*/
    private String                registUrl;
    /**卡数量*/
    private int                bankCardCount;
    /**loginName*/
    private String 			  	  loginName;
    /**login pwd*/
    private String 				  loginPasswd;
    private String 			  emailStar;
    /**实名认证信息*/
    private AuthResultStatus  nameVerifyStatus;
    /**邀请码*/
    private  String            invitCode; 
    
    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPlateFormId() {
        return plateFormId;
    }

    public void setPlateFormId(String plateFormId) {
        this.plateFormId = plateFormId;
    }

    public String getPlateName() {
        return plateName;
    }

    public void setPlateName(String plateName) {
        this.plateName = plateName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobileStar() {
        return mobileStar;
    }

    public void setMobileStar(String mobileStar) {
        this.mobileStar = mobileStar;
    }

    public String getMobileTicket() {
        return mobileTicket;
    }

    public void setMobileTicket(String mobileTicket) {
        this.mobileTicket = mobileTicket;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberAccount getAccount() {
        return account;
    }

    public void setAccount(MemberAccount account) {
        this.account = account;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDefaultAccountId() {
        return defaultAccountId;
    }

    public void setDefaultAccountId(String defaultAccountId) {
        this.defaultAccountId = defaultAccountId;
    }

    public MemberLockStatus getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(MemberLockStatus lockStatus) {
        this.lockStatus = lockStatus;
    }

    public MemberPaypasswdStatus getPaypasswdstatus() {
        return paypasswdstatus;
    }

    public void setPaypasswdstatus(MemberPaypasswdStatus paypasswdstatus) {
        this.paypasswdstatus = paypasswdstatus;
    }

    public String getPaypasswd() {
        return paypasswd;
    }

    public void setPaypasswd(String paypasswd) {
        this.paypasswd = paypasswd;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public String getFaceImageUrl() {
        return faceImageUrl;
    }

    public void setFaceImageUrl(String faceImageUrl) {
        this.faceImageUrl = faceImageUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getRegistUrl() {
        return registUrl;
    }

    public void setRegistUrl(String registUrl) {
        this.registUrl = registUrl;
    }

    public int getBankCardCount() {
        return bankCardCount;
    }

    public void setBankCardCount(int bankCardCount) {
        this.bankCardCount = bankCardCount;
    }

    /**
	 * @return the loginName
	 */
	public String getLoginName() {
		return loginName;
	}

	/**
	 * @param loginName the loginName to set
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	

	/**
	 * @return the loginPasswd
	 */
	public String getLoginPasswd() {
		return loginPasswd;
	}

	/**
	 * @param loginPasswd the loginPasswd to set
	 */
	public void setLoginPasswd(String loginPasswd) {
		this.loginPasswd = loginPasswd;
	}
	

	public String getEmailStar() {
		return emailStar;
	}

	public void setEmailStar(String emailStar) {
		this.emailStar = emailStar;
	}
	

	public AuthResultStatus getNameVerifyStatus() {
		return nameVerifyStatus;
	}

	public void setNameVerifyStatus(AuthResultStatus nameVerifyStatus) {
		this.nameVerifyStatus = nameVerifyStatus;
	}
	
	

	public String getCurrentOperatorId() {
		return currentOperatorId;
	}

	public void setCurrentOperatorId(String currentOperatorId) {
		this.currentOperatorId = currentOperatorId;
	}

	public boolean isPersonal() {
		return MemberType.PERSONAL.getCode().equals(this.getMemberType().getCode());
	}
	
	public String getInvitCode() {
		return invitCode;
	}

	public void setInvitCode(String invitCode) {
		this.invitCode = invitCode;
	}

	@Override
    public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
