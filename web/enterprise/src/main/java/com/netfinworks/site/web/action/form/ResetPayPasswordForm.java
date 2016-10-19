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
public class ResetPayPasswordForm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4106758896593219651L;
    private String            oldPasswdKey;                            //old支付密码key
    private String            oldPasswd;                               //old支付密码
    private String            newPasswdKey;                            //新支付密码key
    private String            newPasswd;                               //新支付密码
    private String            renewPasswdKey;                          //支付密码确认key
    private String            renewPasswd;                             //支付密码确认
    private String            passwdMode;                              //支付密码设置模式

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
