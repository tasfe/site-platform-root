package com.yongda.site.service.personal.facade.response;

import java.util.List;

import com.netfinworks.site.service.facade.model.PersonalAccount;


/**
 * 账户余额查询响应
 * @author yp
 *
 */
public class BalancesResponse{
	
    /**
     * 账户信息列表
     */
    private List<PersonalAccount>     accounts;

   


	public List<PersonalAccount> getAccounts() {
		return accounts;
	}




	public void setAccounts(List<PersonalAccount> accounts) {
		this.accounts = accounts;
	}



}
