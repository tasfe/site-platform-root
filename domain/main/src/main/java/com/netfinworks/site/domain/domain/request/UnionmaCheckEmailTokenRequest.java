package com.netfinworks.site.domain.domain.request;

public class UnionmaCheckEmailTokenRequest extends UnionmaRequestParam {
	// 会员编号
    private String            memberId;
    // 邮箱地址
    private String            token;
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
    
    
}
