package com.yongda.site.service.personal.facade.request;

public class FastRegisterRequest extends BaseRequest{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1175977245926646411L;
	/*注册手机号**/
	private String account;
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
}
