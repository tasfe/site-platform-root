package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>支付密码校验结果</p>
 * @author dexter.qin
 * @version $Id: PayPasswdCheck.java, v 0.1 2013-12-31 上午10:50:18 qinde Exp $
 */
public class PayPasswdCheck implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4786873572605248845L;
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
