package com.yongda.site.service.personal.facade.response;
/**
 * 
 * @author csl 快速注册响应
 *
 */
public class FastRegisterResponse extends BaseResponse{

	
	//private static final long serialVersionUID = -6665680426973692840L;
	/**
	 * 商户号
	 */
	private String memberId;
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public FastRegisterResponse(String memberId) {
		super();
		this.memberId = memberId;
	}
	public FastRegisterResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
}
