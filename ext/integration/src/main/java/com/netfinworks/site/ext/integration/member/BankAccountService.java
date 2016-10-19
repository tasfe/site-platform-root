/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member;

import java.util.List;

import com.meidusa.venus.annotations.Service;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 通用说明：银行卡查询接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:54:27
 *
 */

public interface  BankAccountService {


    /**
     * 查询银行卡信息
     * @param env
     * @param request
     * @return List<BankAccountInfo> 银行卡list
     */
    public List<BankAccountInfo> queryBankAccount(BankAccRequest request) throws BizException;

    /**
     * 根据bankcardId 查询银行卡
     * @param bankcardId
     * @return
     * @throws BizException
     */
    public BankAcctDetailInfo queryBankAccount(String bankcardId) throws BizException ;
    /**
     * 修改会员银行卡信息
     * @param environment
     * @param request
     * @return
     */
     public boolean updateBankAccount(BankAccRequest request) throws BizException;

    /**
     * 添加银行卡信息
     * @param env
     * @param request
     * @return String 银行卡ID
     */
    public String addBankAccount(BankAccRequest req) throws BizException;

    /**
     * 解除会员银行卡绑定
     * @param env
     * @param request
     * @return boolean
     */
    public boolean removeBankAccount(BankAccRequest req) throws BizException;

	/**
	 * 更新默认银行卡
	 * 
	 * @param req
	 * @return
	 * @throws BizException
	 */
	public boolean updateDefaultAccount(BankAccRequest req) throws BizException;

}
