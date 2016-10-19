/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.impl;


import java.util.List;

import javax.annotation.Resource;

import org.scf.facade.service.api.IScfUserFacade;
import org.scf.facade.service.response.MemberOrganizationResponse;
import org.scf.facade.service.response.MemberTypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.Account;
import com.netfinworks.ma.service.facade.IAccountFacade;
import com.netfinworks.ma.service.request.AccRelationRequest;
import com.netfinworks.ma.service.request.AccountRequest;
import com.netfinworks.ma.service.response.AccRelationResponse;
import com.netfinworks.ma.service.response.AccountInfoResponse;
import com.netfinworks.ma.service.response.AccountResponse;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.member.AccountService;

/**
 * 通用说明：账户查询接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:54:27
 *
 */
@Service("accountService")
public class AccountServiceImpl extends ClientEnvironment implements AccountService {

    private Logger         logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    @Resource(name = "accountFacade")
    private IAccountFacade accountFacade;
    
    @Resource(name = "scfUserFacade")
    private IScfUserFacade scfUserFacade;
    /**
     * 查询账户
     *
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public MemberAccount queryAccountById(String accountId, OperationEnvironment env)
                                                                                     throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询会员账户，请求参数：{}", accountId);
                beginTime = System.currentTimeMillis();
            }
            AccountInfoResponse response = accountFacade.queryAccountById(env, accountId);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询会员余额， 耗时:{} (ms); 响应结果:{} ",
                        new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                MemberAccount account = new MemberAccount();
                BeanUtils.copyProperties(response.getAccount(), account);
                return account;
            }
            else {
                throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResponseMessage());
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("查询会员账户: {}信息异常:，异常信息：{},{}", accountId, e.getMessage(),e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * scf保理账户 已经废弃，本方法废除 2015.12.15 毛
     */
    @Override
    public String queryUserType(String memberId) throws Exception {
        MemberTypeResponse memberTypeResponse = new MemberTypeResponse();
        try{
            memberTypeResponse = scfUserFacade.queryMemberTypeByMemberId(memberId);
        }catch(Exception e){
            logger.error("查询memberId: " + memberId + "信息异常:" + e.getMessage(), e);
        }
        String memberType = null;
        if (memberTypeResponse.getCode().equals("00")){
            memberType = memberTypeResponse.getMemberType();
        }else {
            logger.info(memberTypeResponse.getMsg());
        }
        return memberType;
    }
    
    /**
     * @param memberId
     * @return
     * @throws BizException
     */
    @Override
    public String queryMemberOrganization(String memberId) throws BizException {
        MemberOrganizationResponse response = new MemberOrganizationResponse();
        try{
            response = scfUserFacade.queryMemberOrganizationByMemberId(memberId);
        }catch(Exception e){
            logger.error("查询海尔小贷组织机构代号异常，memberId: " + memberId + "异常信息:" + e.getMessage(), e);
            return null;
        }
        String organizationId = null;
        if (response.getCode().equals("00")){
            organizationId = response.getOrganizationId();
        }else {
            logger.info("查询海尔小贷组织机构代号异常，memberId: " + memberId + "异常信息:" + response.getMsg());
        }
        return organizationId;
    }


    /* *//** 验证支付密码是否设置与锁定
          * @param env
          * @param request
          * @return BankAccountResponse
          */
    /*
    @Override
    public ValidatePayPwdResponse queryAccountById(OperationEnvironment env,
                                      ValidatePayPwdRequest request) throws BizException{
    accountFacade.queryAccountByMemberId(paramOperationEnvironment, paramAccountRequest);
    accountFacade.queryAccountById(paramOperationEnvironment, paramString);
    return payPwdFacade.validatePayPwd(env, request);
    }*/

    
    @Override
	public MemberAccount queryAccountByMemberId(String memberId, Long accountType, OperationEnvironment env) {
		// 创建请求参数 
		AccountRequest request = new AccountRequest();
		request.setMemberId(memberId);
		request.setAccountType(accountType);
		
		// 调用远程接口返回信息
		AccountResponse response = accountFacade.queryAccountByMemberId(env, request);
		String responseCode 	= response.getResponseCode(),
			   responseMessage  = response.getResponseMessage();
		boolean isSuccess = ResponseCode.SUCCESS.getCode().equals(responseCode);
		// 获取查询结果失败
		if (!isSuccess){
			Object eArg[] = {
				responseCode,
				responseMessage,
				memberId,
				accountType
			};
			logger.warn(String.format("获取账号信息失败, 错误代码: %s, 错误信息: %s,  会员号:%s, 会员类型%s", eArg));
		} else {
			/**
			 * 查询成功
			 * 如果账号数为0 或则大于1 则 返回空
			 */
			List<Account> accounts = response.getAccounts();
			int size = accounts == null ? 0 : accounts.size();
			if (size != 1) {
				Object eArg[] = {
					size,
					memberId,
					accountType
				};
				logger.warn(String.format("获取账号信息失败, 账号数: %s, 会员号:%s, 会员类型:%s", eArg));
			}
			
			Account account = accounts.get(0);
			MemberAccount memberAccount = new MemberAccount();
	        BeanUtils.copyProperties(account, memberAccount);
	        return memberAccount;
		}
		
		return null;

	}

    @Override
    public String queryAccountType(String memberId, OperationEnvironment env) {
        AccRelationRequest req = new AccRelationRequest();
        req.setMemberId(memberId);
        req.setAccountType(205L);
        AccRelationResponse resp = accountFacade.queryAccountRelation(env, req);
        if(resp.getResponseCode().equals("0")){
            logger.info(resp.getResponseMessage());
            return resp.getAccountRelations().get(0).getAccountType().toString();
        }else{
            return null;
        }
    }
}
