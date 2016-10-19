package com.netfinworks.site.domain.domain.response;


public class UnionmaBaseResponse implements Response {
private static final long serialVersionUID = 4931969088107731436L;
	
	private String responseCode ; // 响应代码
	private String responseMessage; // 响应消息
	private String resultJSON;  // 返回JSON字符串
	private boolean isSuccess ;
	
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public String getResultJSON() {
		return resultJSON;
	}
	public void setResultJSON(String resultJSON) {
		this.resultJSON = resultJSON;
	}
	public boolean getIsSuccess() {
		return isSuccess;
	}
}
