package com.netfinworks.site.domain.domain.response;

public class UnionmaSendEmailResponse extends UnionmaBaseResponse {
	/**
	 * 链接或者是验证码
	 */
	private String activeUrl;

	public String getActiveUrl() {
		return activeUrl;
	}

	public void setActiveUrl(String activeUrl) {
		this.activeUrl = activeUrl;
	}
	
}
