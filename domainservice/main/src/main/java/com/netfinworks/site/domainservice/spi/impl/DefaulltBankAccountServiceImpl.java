/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.spi.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.ext.integration.member.BankAccountService;

/**
 * 通用说明：会员银行账户接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:54:27
 *
 */
@Service("defaultBankAccountService")
public class DefaulltBankAccountServiceImpl implements DefaultBankAccountService {
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Resource(name = "bankAccountService")
    private BankAccountService bankAccountService;

    /**
     * 查询银行卡信息
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public List<BankAccountInfo> queryBankAccount(BankAccRequest request) throws ServiceException {
        try {
            return bankAccountService.queryBankAccount(request);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 查询银行卡数量
     * @param env
     * @param request
     * @return int 银行卡数量
     */
    @Override
	public int queryBankAccountNum(BankAccRequest request) throws ServiceException {
        try {
            List<BankAccountInfo> list =  bankAccountService.queryBankAccount(request);
            return list != null?list.size():0;
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

	/**
	 * 查询银行卡详细信息
	 * 
	 * @param env
	 * @param request
	 * @return int 银行卡数量
	 */
	@Override
	public BankAcctDetailInfo queryBankAccountDetail(String bankcardId) throws ServiceException {
		try {
			BankAcctDetailInfo detailInfo = bankAccountService.queryBankAccount(bankcardId);
			return detailInfo;
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

    /**
     * 添加银行卡信息
     * @param env
     * @param request
     * @return BankAccInfoResponse
     */
    @Override
    public String addBankAccount(BankAccRequest req) throws ServiceException {
        try {
            return bankAccountService.addBankAccount(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 解除会员银行卡绑定
     * @param env
     * @param request
     * @return Response
     */
    @Override
    public boolean removeBankAccount(BankAccRequest req) throws ServiceException {
        try {
            return bankAccountService.removeBankAccount(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

	/**
	 * 更新银行卡信息
	 */
	@Override
	public boolean updateBankAccount(BankAccRequest request) throws ServiceException {
		log.info("完善银行卡信息需要的参数：" + request.toString());
		try {
			return bankAccountService.updateBankAccount(request);
		} catch (BizException e) {
			log.info("完善银行卡信息，调用接口异常!");
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * 更新默认银行卡
	 */
    @Override
	public boolean updateDefaultAccount(BankAccRequest request) throws ServiceException {
        log.info("完善银行卡信息需要的参数："+request.toString());
        try {
			return bankAccountService.updateDefaultAccount(request);
        } catch (BizException e) {
            log.info("完善银行卡信息，调用接口异常!");
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

}
