package com.netfinworks.site.domain.domain.comm;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>返回信息（公用）</p>
 * @author zhangyun.m
 * @version $Id: CommResponse.java, v 0.1 2014年5月29日 下午6:16:36 zhangyun.m Exp $
 */
public class CommResponse implements Serializable{
    
    private static final long serialVersionUID = -1L;

    private String            responseCode;                            //返回码
    private String            responseMessage;                         //返回消息
    private String            extention;
    
    private boolean      success;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getExtention() {
        return extention;
    }

    public void setExtention(String extention) {
        this.extention = extention;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
