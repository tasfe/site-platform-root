package com.netfinworks.site.domain.domain.request;

/**
 * 登录请求
 * @author zhaozq
 * @date 2015年6月29日
 */
public class LoginRequest extends UnionmaRequestParam {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7562198519113547786L;
	
	private String loginName; // 登录名
	private String loginPassowrd; // 登录密码
	private String loginType; // 登录类型
	
	public LoginRequest() {}

	public LoginRequest(String loginName, String loginPassowrd,
			String loginType) {
		super();
		this.loginName = loginName;
		this.loginPassowrd = loginPassowrd;
		this.loginType = loginType;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getLoginPassowrd() {
		return loginPassowrd;
	}

	public void setLoginPassowrd(String loginPassowrd) {
		this.loginPassowrd = loginPassowrd;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
}
