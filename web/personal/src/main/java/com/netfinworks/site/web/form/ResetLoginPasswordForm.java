package com.netfinworks.site.web.form;

import java.io.Serializable;

/**
 * <p>设置登录密码form</p>
 * @author zhangyun.m
 * @version $Id: ResetLoginPasswordForm.java, v 0.1 2014年5月21日 下午6:00:14 zhangyun.m Exp $
 */
public class ResetLoginPasswordForm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4106758896593219651L;
    private String            oldPasswdKey;                            //old登录密码key
    private String            oldPasswd;                               //old登录密码
    private String            newPasswdKey;                            //新登录密码key
    private String            newPasswd;                               //新登录密码
    private String            renewPasswdKey;                          //登录密码确认key
    private String            renewPasswd;                             //登录密码确认
    private String            passwdMode;                              //登录密码设置模式

    public String getOldPasswdKey() {
        return oldPasswdKey;
    }

    public void setOldPasswdKey(String oldPasswdKey) {
        this.oldPasswdKey = oldPasswdKey;
    }

    public String getOldPasswd() {
        return oldPasswd;
    }

    public void setOldPasswd(String oldPasswd) {
        this.oldPasswd = oldPasswd;
    }

    public String getNewPasswdKey() {
        return newPasswdKey;
    }

    public void setNewPasswdKey(String newPasswdKey) {
        this.newPasswdKey = newPasswdKey;
    }

    public String getNewPasswd() {
        return newPasswd;
    }

    public void setNewPasswd(String newPasswd) {
        this.newPasswd = newPasswd;
    }

    public String getRenewPasswdKey() {
        return renewPasswdKey;
    }

    public void setRenewPasswdKey(String renewPasswdKey) {
        this.renewPasswdKey = renewPasswdKey;
    }

    public String getRenewPasswd() {
        return renewPasswd;
    }

    public void setRenewPasswd(String renewPasswd) {
        this.renewPasswd = renewPasswd;
    }

    public String getPasswdMode() {
        return passwdMode;
    }

    public void setPasswdMode(String passwdMode) {
        this.passwdMode = passwdMode;
    }

}
