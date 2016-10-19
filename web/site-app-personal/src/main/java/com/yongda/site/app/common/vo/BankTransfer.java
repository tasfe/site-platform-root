/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月31日
 */
package com.yongda.site.app.common.vo;

/**
 * 银行转账
 * @author xuwei
 * @date 2014年7月31日
 */
public class BankTransfer {
	/** 序号 */
	private int orderNo;
	
	/** 姓名 */
	private String name;

	/** 银行名称 */
	private String bankName;
	
	/** 支行名称 */
	private String branchName;
	
	/** 账号掩码 */
	private String accountNoMask;
	
	/** 金额 */
	private String money;
	
	/** 省名称  */
    private String provName;
    
    /** 城市名称 */
    private String cityName;
    
    /** 卡属性(0对公 1对私) */
    private int cardAttribute;
    
    /** 手机号码 */
    private String mobile;
	
	/** 备注 */
	private String remark;
	
	/** 是否成功，false-失败，true-成功 */
	private boolean success;
	
	/** 错误信息 */
	private String errorMsg;

	public int getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getAccountNoMask() {
		return accountNoMask;
	}

	public void setAccountNoMask(String accountNoMask) {
		this.accountNoMask = accountNoMask;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getProvName() {
		return provName;
	}

	public void setProvName(String provName) {
		this.provName = provName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public int getCardAttribute() {
		return cardAttribute;
	}

	public void setCardAttribute(int cardAttribute) {
		this.cardAttribute = cardAttribute;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
