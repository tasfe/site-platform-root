package com.netfinworks.site.service.facade.model;

import java.io.Serializable;

import com.netfinworks.common.util.money.Money;

public class PersonalAccount implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3175564183427767680L;
	/**
     * 账户类型
     */
    private String            account_type;

    /**
     * 账户id
     */
    private String            account_id;

    /**
     * 账户余额
     */
    private String             balance;

    /**
     * 账户冻结余额
     */
    private String             frozenBalance;

	

	public String getAccount_type() {
		return account_type;
	}



	public void setAccount_type(String account_type) {
		this.account_type = account_type;
	}



	public String getAccount_id() {
		return account_id;
	}



	public void setAccount_id(String account_id) {
		this.account_id = account_id;
	}



	public String getBalance() {
		return balance;
	}



	public void setBalance(String balance) {
		this.balance = balance;
	}



	public String getFrozenBalance() {
		return frozenBalance;
	}



	public void setFrozenBalance(String frozenBalance) {
		this.frozenBalance = frozenBalance;
	}



	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}
