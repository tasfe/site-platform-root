package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * <p>会员查询</p>
 * @author qinde
 * @version $Id: MemberAllInfo.java, v 0.1 2013-11-19 下午3:41:32 qinde Exp $
 */
public class MemberRequest extends OperationEnvironment implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2393418922180711911L;
    private String            memberId;                               //钱包会员id
    private String            memberName;                             //钱包会员名称
    private String            plateFormId;                            //永达互联网金融平台的userID
    private String            plateName;                              //永达互联网金融平台的loginName
    private String            mobile;                                 //手机号
    private String            email;                                  //电子邮件
    private boolean           nameVerifyed;                           //是否实名认证
    private String            nameVerifyInfo;                         //实名认证信息
    private String            accountBalance;                         //账户余额
    private int               cardCount;                              //会员卡数量
    private Date              LastLoginTime;                          //最后登录时间
    private String            operatorId;                             //默认的操作员
    private String            accountId;                              //账户Id
    private String            paypasswd;                              //支付密码

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isNameVerifyed() {
        return nameVerifyed;
    }

    public void setNameVerifyed(boolean nameVerifyed) {
        this.nameVerifyed = nameVerifyed;
    }

    public String getNameVerifyInfo() {
        return nameVerifyInfo;
    }

    public void setNameVerifyInfo(String nameVerifyInfo) {
        this.nameVerifyInfo = nameVerifyInfo;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public Date getLastLoginTime() {
        return LastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        LastLoginTime = lastLoginTime;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPaypasswd() {
        return paypasswd;
    }

    public void setPaypasswd(String paypasswd) {
        this.paypasswd = paypasswd;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
