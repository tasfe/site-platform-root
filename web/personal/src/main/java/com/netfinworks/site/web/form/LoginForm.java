/**
 * file name    LoginForm.java
 * description  
 * @author      tiantao
 * @version     1.0
 * create at    2014年5月27日 上午10:20:19
 * copyright 2013 weihui 
 */
package com.netfinworks.site.web.form;

import java.io.Serializable;

/**
 * className		LoginForm    
 * description	
 * @author		Tiantao
 * @version 	2014年5月27日 上午10:20:19
 */
public class LoginForm implements Serializable {

	/**
	* @Fields serialVersionUID : 
	*/
	private static final long serialVersionUID = 1L;
	
	private String username;// 登录名
	private String password;// 密码
	private String vercode;// 验证码
	private String memberIdentity;// 登陆类型

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVercode() {
		return vercode;
	}

	public void setVercode(String vercode) {
		this.vercode = vercode;
	}

	public String getMemberIdentity() {
		return memberIdentity;
	}

	public void setMemberIdentity(String memberIdentity) {
		this.memberIdentity = memberIdentity;
	}
    
}
