package com.netfinworks.site.domain.domain.request;

public class UnionmaUnBindEmailRequest extends UnionmaRequestParam {
	/**
     * 会员编号
     */
    private String            memberId;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
    
}
