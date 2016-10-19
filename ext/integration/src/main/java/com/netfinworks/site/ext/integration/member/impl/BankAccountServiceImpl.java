/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.ma.service.base.model.BankAcctInfo;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.IBankAccountFacade;
import com.netfinworks.ma.service.request.BankAccInfoRequest;
import com.netfinworks.ma.service.request.BankAccRemoveRequest;
import com.netfinworks.ma.service.request.BankAccountRequest;
import com.netfinworks.ma.service.response.BankAccInfoResponse;
import com.netfinworks.ma.service.response.BankAccountInfoResponse;
import com.netfinworks.ma.service.response.BankAccountResponse;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.member.BankAccountService;
import com.netfinworks.site.ext.integration.member.convert.BankConvert;

/**
 * 通用说明：会员查询接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:54:27
 *
 */
@Service("bankAccountService")
public class BankAccountServiceImpl extends ClientEnvironment implements BankAccountService {

    private Logger             logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    @Resource(name = "bankAccountFacade")
    private IBankAccountFacade bankAccountFacade;

    /**
     * 查询银行卡信息
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public List<BankAccountInfo> queryBankAccount(BankAccRequest request) throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询银行卡信息，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            BankAccountRequest req = BankConvert.createBankAccountRequest(request);
            BankAccountResponse response = bankAccountFacade.queryBankAccount(
                getEnv(request.getClientIp()), req);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询银行卡信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                return response.getBankAccountInfos();
            }
            if (ResponseCode.NO_QUERY_RESULT.getCode().equals(response.getResponseCode())) {
                return new ArrayList<BankAccountInfo>(0);
            } else {
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("查询银行卡信息{}绑定 失败", request.getMemberId());
                throw new BizException(ErrorCode.SYSTEM_ERROR,"查询银行卡信息绑定 失败", e);
            }
        }
    }

    @Override
    public BankAcctDetailInfo queryBankAccount(String bankcardId) throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询银行卡详细信息，请求参数：{}", bankcardId);
                beginTime = System.currentTimeMillis();
            }

            BankAccountInfoResponse response = bankAccountFacade.queryBankAccountDetail(getEnv(""), bankcardId);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询银行卡信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                return response.getBankAcctInfo();
            }
            if (ResponseCode.NO_QUERY_RESULT.getCode().equals(response.getResponseCode())) {
                return null;
            } else {
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("查询银行卡信息失败,bankcardId=" + bankcardId,e);
                throw new BizException(ErrorCode.SYSTEM_ERROR,"查询银行卡信息失败", e);
            }
        }
    }

    /**
     * 添加银行卡信息
     * @param env
     * @param request
     * @return BankAccInfoResponse
     */
    @Override
    public String addBankAccount(BankAccRequest req) throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("添加银行卡信息，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            BankAccInfoRequest request = BankConvert.createBankAccInfoRequest(req);
            BankAccInfoResponse response = bankAccountFacade.addBankAccount(
                getEnv(req.getClientIp()), request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程添加银行卡信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                return response.getBankcardId();
            } else {
                throw new BizException(ErrorCode.SYSTEM_ERROR,response.getResponseMessage());
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("添加银行卡信息会员ID：{}绑定 失败", req.getMemberId(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    /**
     * 解除会员银行卡绑定
     * @param env
     * @param request
     * @return Response
     */
    @Override
    public boolean removeBankAccount(BankAccRequest req) throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("删除银行卡信息，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            BankAccRemoveRequest request = BankConvert.createBankAccRemoveRequest(req);
            Response response = bankAccountFacade.removeBankAccount(getEnv(req.getClientIp()),
                request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程调用删除银行卡， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                return true;
            } else {
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("解除银行卡{}绑定失败", req.getBankcardId(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
    /**
     * 更新会员银行卡信息
     * @param env
     * @param request
     * @return Response
     */

    @Override
    public boolean updateBankAccount(BankAccRequest req) throws BizException {
        logger.error("更新会员银行卡信息request:"+req.toString());
        long beginTime = 0L;
        try {
            BankAccInfoRequest request = BankConvert.updateBankAccInfoRequest(req);
            BankAccInfoResponse response=bankAccountFacade.updateBankAccount(getEnv(req.getClientIp()), request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程调用更新银行卡信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                return true;
            } else {
                logger.error("更新会员银行卡信息失败");
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("更新银行卡信息会员ID：{}更新 失败", req.getMemberId(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

	/**
	 * 更新默认银行卡
	 * 
	 * @param env
	 * @param request
	 * @return Response
	 */

	@Override
	public boolean updateDefaultAccount(BankAccRequest req) throws BizException {
		logger.error("更新会员银行卡信息request:" + req.toString());
		long beginTime = 0L;
		try {
			BankAccInfoRequest request = new BankAccInfoRequest();
			BankAcctInfo info = new BankAcctInfo();
			info.setMemberId(req.getMemberId());
			info.setBankcardId(req.getBankcardId());
			info.setExtention(req.getExtInfo());
			request.setBankInfo(info);
			BankAccInfoResponse response = bankAccountFacade.updateBankAccount(getEnv(req.getClientIp()), request);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用更新银行卡信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				return true;
			} else {
				logger.error("更新会员银行卡信息失败");
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("更新银行卡信息会员ID：{}更新 失败", req.getMemberId(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
	}



}
