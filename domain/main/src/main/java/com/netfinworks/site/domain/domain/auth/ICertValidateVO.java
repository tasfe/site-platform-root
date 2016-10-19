package com.netfinworks.site.domain.domain.auth;


/** 
 * 
 * @author tangL
 * @date 2014年12月26日
 * @since 1.6
 */ 
public class ICertValidateVO{
	
	private String name; // 人员姓名
	private String idCard; // 人员身份证
	private String proxyPersonName; // 代理人名字
	private String proxyPersonIdCard; // 代理人身份证号
	private boolean isDbrFlag; // 是否添加代办人信息
	private String cardType1; // 身份证类型1
	private String cardType2; // 身份证类型2
	
	
	public String getCardType1() {
		return cardType1;
	}
	public void setCardType1(String cardType1) {
		this.cardType1 = cardType1;
	}
	public String getCardType2() {
		return cardType2;
	}
	public void setCardType2(String cardType2) {
		this.cardType2 = cardType2;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIdCard() {
		return idCard;
	}
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public String getProxyPersonName() {
		return proxyPersonName;
	}
	public void setProxyPersonName(String proxyPersonName) {
		this.proxyPersonName = proxyPersonName;
	}
	public String getProxyPersonIdCard() {
		return proxyPersonIdCard;
	}
	public void setProxyPersonIdCard(String proxyPersonIdCard) {
		this.proxyPersonIdCard = proxyPersonIdCard;
	}
	public boolean getIsDbrFlag() {
		return isDbrFlag;
	}
	public void setIsDbrFlag(boolean isDbrFlag) {
		this.isDbrFlag = isDbrFlag;
	}
	
}

