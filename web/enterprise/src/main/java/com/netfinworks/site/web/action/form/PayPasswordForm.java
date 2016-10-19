package com.netfinworks.site.web.action.form;

import java.io.Serializable;

/**
 * 通用说明：设置支付密码form
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午3:00:04
 *
 */
public class PayPasswordForm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -8759724529839121090L;
//    private String            mobile;                                  //手机号
    private String            password;                                //支付密码
    private String            repassword;                              //支付密码确认
//    private String            paypwdCaptcha;                           //手机验证码
//    private String            username;                                //用户名
    private String            passwordKey;                             //支付密码Key
    private String            repasswordKey;                           //支付密码确认Key
    private String            email;
    private String            agree;                                   //是否同意
    private String            merState;
    private String            model;                                   //处理模式
    private String            passwdMode;                              //支付密码处理模式
    
    private String            newPasswd;                               //新支付密码
    private String            renewPasswd;                             //重新输入支付密码

//    public String getMobile() {
//        return mobile;
//    }
//
//    public void setMobile(String mobile) {
//        this.mobile = mobile;
//    }

    public String getPassword() {
        return password;
    }

    public String getPasswordKey() {
        return passwordKey;
    }

    public void setPasswordKey(String passwordKey) {
        this.passwordKey = passwordKey;
    }

    public String getRepasswordKey() {
        return repasswordKey;
    }

    public void setRepasswordKey(String repasswordKey) {
        this.repasswordKey = repasswordKey;
    }

    public String getPasswdMode() {
        return passwdMode;
    }

    public void setPasswdMode(String passwdMode) {
        this.passwdMode = passwdMode;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepassword() {
        return repassword;
    }

    public void setRepassword(String repassword) {
        this.repassword = repassword;
    }

//    public String getPaypwdCaptcha() {
//        return paypwdCaptcha;
//    }
//
//    public void setPaypwdCaptcha(String paypwdCaptcha) {
//        this.paypwdCaptcha = paypwdCaptcha;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAgree() {
        return agree;
    }

    public void setAgree(String agree) {
        this.agree = agree;
    }

    public String getMerState() {
        return merState;
    }

    public void setMerState(String merState) {
        this.merState = merState;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getNewPasswd() {
        return newPasswd;
    }

    public void setNewPasswd(String newPasswd) {
        this.newPasswd = newPasswd;
    }

    public String getRenewPasswd() {
        return renewPasswd;
    }

    public void setRenewPasswd(String renewPasswd) {
        this.renewPasswd = renewPasswd;
    }
    
    

}
