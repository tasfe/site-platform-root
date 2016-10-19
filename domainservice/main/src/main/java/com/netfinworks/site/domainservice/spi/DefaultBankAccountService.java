/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.spi;

import java.util.List;

import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;

/**
 * 通用说明： 会员银行卡信息
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午9:17:08
 *
 */
public interface DefaultBankAccountService {

    /**
     * 查询银行卡信息
     * @param env
     * @param request
     * @return List<BankAccountInfo> 银行卡list
     */
    public List<BankAccountInfo> queryBankAccount(BankAccRequest request) throws ServiceException;

    /**
	 * 查询银行卡详细信息
	 * 
	 * @param env
	 * @param request
	 * @return int 银行卡数量
	 */
	public BankAcctDetailInfo queryBankAccountDetail(String bankcardId) throws ServiceException;

	/**
	 * 查询银行卡数量
	 * 
	 * @param env
	 * @param request
	 * @return int 银行卡数量
	 */
	public int queryBankAccountNum(BankAccRequest request) throws ServiceException;

    /**
     * 添加银行卡信息
     * @param env
     * @param request
     * @return boolean
     */
    public String addBankAccount(BankAccRequest req) throws ServiceException;

    /**
     * 修改会员银行卡信息
     * @param environment
     * @param request
     * @return
     */
    public boolean updateBankAccount(BankAccRequest request) throws ServiceException;

    /**
     * 解除会员银行卡绑定
     * @param env
     * @param request
     * @return boolean
     */
    public boolean removeBankAccount(BankAccRequest req) throws ServiceException;

	/**
	 * 更新默认银行卡
	 * 
	 * @param env
	 * @param request
	 * @return boolean
	 */
	public boolean updateDefaultAccount(BankAccRequest req) throws ServiceException;

}
