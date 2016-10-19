package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * <p>登录密码校验结果</p>
 * @author zhangyun.m
 * @version $Id: LoginPasswdCheck.java, v 0.1 2014年5月21日 下午1:23:36 zhangyun.m Exp $
 */
public class LoginPasswdCheck implements Serializable {

    private static final long serialVersionUID = -1L;
    private boolean           success;
    private int               remainNum;
    private boolean           locked;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getRemainNum() {
        return remainNum;
    }

    public void setRemainNum(int remainNum) {
        this.remainNum = remainNum;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
