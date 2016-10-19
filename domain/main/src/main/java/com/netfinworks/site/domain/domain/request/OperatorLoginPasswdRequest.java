package com.netfinworks.site.domain.domain.request;

import java.io.Serializable;

import com.netfinworks.common.domain.OperationEnvironment;

public class OperatorLoginPasswdRequest extends OperationEnvironment implements Serializable {

    private static final long serialVersionUID = -1L;
    
    /** 操作员编号 */
    private String            operatorId;

    /** 新的登录密码 */
    private String            password;

    /**原来的登录密码*/
    private String            oldPassword;

    /** 接口扩展字段Json 字符串 */
    private String            extention;

    /** 会员平台类型（UID:1 ；手机:2 ； 登录名:3；公司ID:4）  */
    private String            platformType;
    /** 会员标识  */
    private String            memberIdentity;

    /**
     * 验证类型
     * 0: 是否设置登录密码，密码是否被锁定，1：是否设置登录密码，2：登录密码是否被锁定
     */
    private int               validateMode;
    /**
     * 验证类型
     * 1：验证原来的登录密码，2：验证新登录密码
     */
    private int               validateType;

    /**操作员登录名平台类型  */
    private String            loginNamePlatformType;

    /**操作员登录名 */
    private String            loginName;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getExtention() {
        return extention;
    }

    public void setExtention(String extention) {
        this.extention = extention;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public String getMemberIdentity() {
        return memberIdentity;
    }

    public void setMemberIdentity(String memberIdentity) {
        this.memberIdentity = memberIdentity;
    }

    public int getValidateMode() {
        return validateMode;
    }

    public void setValidateMode(int validateMode) {
        this.validateMode = validateMode;
    }

    public int getValidateType() {
        return validateType;
    }

    public void setValidateType(int validateType) {
        this.validateType = validateType;
    }

    public String getLoginNamePlatformType() {
        return loginNamePlatformType;
    }

    public void setLoginNamePlatformType(String loginNamePlatformType) {
        this.loginNamePlatformType = loginNamePlatformType;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
