/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domain.domain.request;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * 通用说明： 支付密码查询请求
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-25 下午8:15:25
 *
 */
public class PayPasswdRequest extends OperationEnvironment implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4718361781233432133L;
    /**会员ID*/
    private String            memberId;
    /**账户ID*/
    private String            accountId;
    /**支付密码*/
    private String            password;
    /**原来的支付密码*/
    private String            oldPassword;
    /**操作员*/
    private String            operator;
    /**
     * 验证类型
     * 0: 是否设置支付密码，密码是否被锁定，1：是否设置支付密码，2：支付密码是否被锁定
     */
    private int               validateMode;
    /**
     * 验证类型
     * 1：验证原来的支付密码，2：验证支付密码
     */
    private int               validateType;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getValidateMode() {
        return validateMode;
    }

    public void setValidateMode(int validateMode) {
        this.validateMode = validateMode;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public int getValidateType() {
        return validateType;
    }

    public void setValidateType(int validateType) {
        this.validateType = validateType;
    }

    @Override
    public String toString() {
        return "PayPasswdRequest [memberId=" + memberId + ", accountId=" + accountId
               + ", operator=" + operator + ", validateMode=" + validateMode + ", validateType="
               + validateType + "]";
    }

}
