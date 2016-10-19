package com.netfinworks.site.domain.domain.info;

import com.netfinworks.deposit.api.domain.DepositInfo;

 public class DepositBasicInfo extends DepositInfo{
    /**
     * 支付银行
     * */
    private String bank;
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
}
