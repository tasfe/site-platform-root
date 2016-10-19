package com.yongda.site.service.personal.facade.response;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.meidusa.fastjson.annotation.JSONField;

public class BaseResponse{

	 /** 是否成功 */
    protected boolean         success          = false;

    /** 错误编码 */
    @JSONField(name="error_code")
    protected String          errorCode;
    /** 结果返回信息 */
    @JSONField(name="error_message")
    protected String          errorMessage;
    
    @JSONField(name="partner_id")
    protected String		  partnerId;
    
    @JSONField(name="_input_charset")
    protected String		  inputCharset;
    
	/**
	 * 业务响应
	 */
	private Object data;
	
	
	public boolean isSuccess() {
		return success;
	}




	public void setSuccess(boolean success) {
		this.success = success;
	}




	public String getErrorCode() {
		return errorCode;
	}




	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}



	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getPartnerId() {
		return partnerId;
	}


	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}




	public String getInputCharset() {
		return inputCharset;
	}




	public void setInputCharset(String inputCharset) {
		this.inputCharset = inputCharset;
	}




	public Object getData() {
		return data;
	}




	public void setData(Object data) {
		this.data = data;
	}


	@Override
	public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
