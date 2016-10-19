/**
 * file name    LoginForm.java
 * description  
 * @author      tiantao
 * @version     1.0
 * create at    2014年5月27日 上午10:20:19
 * copyright 2013 weihui 
 */
package com.netfinworks.site.web.action.form;

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
	
	private String 			  manageUser;
	private String            passwdKey;                            //密码key
    private String            passwd;                               //密码
    private String 			  operator_loginName;
    private String 			  captcha_value;
    private String 			  operatorId;
    
	/**
	 * @return the manageUser
	 */
	public String getManageUser() {
		return manageUser;
	}
	/**
	 * @param manageUser the manageUser to set
	 */
	public void setManageUser(String manageUser) {
		this.manageUser = manageUser;
	}
	/**
	 * @return the passwdKey
	 */
	public String getPasswdKey() {
		return passwdKey;
	}
	/**
	 * @param passwdKey the passwdKey to set
	 */
	public void setPasswdKey(String passwdKey) {
		this.passwdKey = passwdKey;
	}
	/**
	 * @return the passwd
	 */
	public String getPasswd() {
		return passwd;
	}
	/**
	 * @param passwd the passwd to set
	 */
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public String getOperator_loginName() {
		return operator_loginName;
	}
	public void setOperator_loginName(String operator_loginName) {
		this.operator_loginName = operator_loginName;
	}
	public String getCaptcha_value() {
		return captcha_value;
	}
	public void setCaptcha_value(String captcha_value) {
		this.captcha_value = captcha_value;
	}
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	
	
    
}
