package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.site.domain.enums.LoginNameTypeEnum;
import com.netfinworks.site.domain.enums.OperatorLockStatusEnum;
import com.netfinworks.site.domain.enums.OperatorStatusEnum;
import com.netfinworks.site.domain.enums.OperatorTypeEnum;
import com.netfinworks.site.domain.enums.PlatformTypeEnum;

/**
 * 
 * <p>操作员显示类</p>
 * @author luoxun
 * @version $Id: Operator.java, v 0.1 2014-5-20 下午5:27:57 luoxun Exp $
 */
public class OperatorVO implements Serializable {
    private static final long      serialVersionUID  = 5158976259910785914L;

    private String                 memberId;                                    //会员编号                                                     
    private String                 operatorId;                                  //操纵员编号                                                   
    private String                 nickName;                                    //昵称                                                         
    private OperatorTypeEnum       operatorTypeEnum;                            //操作员类型                        
    private Boolean                isDefaultOperator;                           //是否默认操作员                                               
    private OperatorStatusEnum     statusEnum;                                  //操作员状态                      
    private OperatorLockStatusEnum lockStatusEnum;                              //操作员锁定状态
    private Date                   createTime;                                  //操作员创建时间
    private String                 extention;                                   //扩展信息
    private String                 loginName;                                   //登陆名
    private LoginNameTypeEnum      loginNameTypeEnum = LoginNameTypeEnum.NORMAL; //登陆名类型
    private boolean                defaultActive     = true;                    //默认的激活状态
    private String                 loginPwd;                                    //登陆密码（SHA-256明文）
    private PlatformTypeEnum       platformTypeEnum  = PlatformTypeEnum.UID;    //所属平台
	/**
	 * 联系方式
	 */
	private String contact;

    public OperatorVO() {

    }

    public OperatorVO(String memberId, String operatorId, String nickName,
                      OperatorTypeEnum operatorTypeEnum, Boolean isDefaultOperator,
                      OperatorStatusEnum statusEnum, OperatorLockStatusEnum lockStatusEnum,
                      Date createTime, String extention, String loginName,
                      LoginNameTypeEnum loginNameTypeEnum, boolean defaultActive, String loginPwd,
                      PlatformTypeEnum platformTypeEnum) {
        super();
        this.memberId = memberId;
        this.operatorId = operatorId;
        this.nickName = nickName;
        this.operatorTypeEnum = operatorTypeEnum;
        this.isDefaultOperator = isDefaultOperator;
        this.statusEnum = statusEnum;
        this.lockStatusEnum = lockStatusEnum;
        this.createTime = createTime;
        this.extention = extention;
        this.loginName = loginName;
        this.loginNameTypeEnum = loginNameTypeEnum;
        this.defaultActive = defaultActive;
        this.loginPwd = loginPwd;
        this.platformTypeEnum = platformTypeEnum;
    }

    public PlatformTypeEnum getPlatformTypeEnum() {
        return platformTypeEnum;
    }

    public void setPlatformTypeEnum(PlatformTypeEnum platformTypeEnum) {
        this.platformTypeEnum = platformTypeEnum;
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public OperatorTypeEnum getOperatorTypeEnum() {
        return operatorTypeEnum;
    }

    public void setOperatorTypeEnum(OperatorTypeEnum operatorTypeEnum) {
        this.operatorTypeEnum = operatorTypeEnum;
    }

    public Boolean getIsDefaultOperator() {
        return isDefaultOperator;
    }

    public void setIsDefaultOperator(Boolean isDefaultOperator) {
        this.isDefaultOperator = isDefaultOperator;
    }

    public OperatorStatusEnum getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(OperatorStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public OperatorLockStatusEnum getLockStatusEnum() {
        return lockStatusEnum;
    }

    public void setLockStatusEnum(OperatorLockStatusEnum lockStatusEnum) {
        this.lockStatusEnum = lockStatusEnum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getExtention() {
        return extention;
    }

    public void setExtention(String extention) {
        this.extention = extention;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public LoginNameTypeEnum getLoginNameTypeEnum() {
        return loginNameTypeEnum;
    }

    public void setLoginNameTypeEnum(LoginNameTypeEnum loginNameTypeEnum) {
        this.loginNameTypeEnum = loginNameTypeEnum;
    }

    public boolean getDefaultActive() {
        return defaultActive;
    }

    public void setDefaultActive(boolean defaultActive) {
        this.defaultActive = defaultActive;
    }

    public String getLoginPwd() {
        return loginPwd;
    }

    public void setLoginPwd(String loginPwd) {
        this.loginPwd = loginPwd;
    }

    public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}