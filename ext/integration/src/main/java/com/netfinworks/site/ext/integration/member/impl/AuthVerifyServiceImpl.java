package com.netfinworks.site.ext.integration.member.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.facade.IVerifyFacade;
import com.netfinworks.ma.service.request.VerifyInputRequest;
import com.netfinworks.ma.service.request.VerifyQueryRequest;
import com.netfinworks.ma.service.response.VerifyInfoResponse;
import com.netfinworks.ma.service.response.VerifyResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.convert.AuthVerifyConvert;

/**
 * <p>认证验证接口实现类</p>
 * @author liangzhizhuang.m
 * @version $Id: AuthVerifyServiceImpl.java, v 0.1 2014年5月23日 上午10:29:45 liangzhizhuang.m Exp $
 */
@Service("authVerifyService")
public class AuthVerifyServiceImpl implements AuthVerifyService {

    private Logger        logger = LoggerFactory.getLogger(AuthVerifyServiceImpl.class);

    @Resource(name = "verifyFacade")
    private IVerifyFacade verifyFacade;

    @Resource(name = "memberFacade")
    private IMemberFacade memberFacade;

    @Override
    public ResponseCode createVerify(AuthVerifyInfo info, OperationEnvironment env) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("创建认证接口请求参数：{},{}", info, env);
            }
            info.setStatus(1); //已认证
            VerifyInputRequest request = new AuthVerifyConvert().toVefifyInputConvert(info);
            VerifyResponse response = verifyFacade.createVerify(env, request);
            if (logger.isInfoEnabled()) {
                logger.info("创建认证接口返回参数：{}", response);
            }
            return ResponseCode.getByCode(response.getResponseCode());
        } catch (Exception e) {
            logger.error("创建认证接口调用失败：{}", e);
            e.printStackTrace();
            return ResponseCode.INVOK_FAILURE;
        }
    }

    @Override
    public ResponseCode updateVerify(AuthVerifyInfo info, OperationEnvironment env) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("修改认证接口请求参数：{},{}", info, env);
            }
            info.setStatus(1); //已认证
            VerifyInputRequest request = new AuthVerifyConvert().toVefifyInputConvert(info);
            VerifyResponse response = verifyFacade.updateVerify(env, request);
            if (logger.isInfoEnabled()) {
                logger.info("修改认证接口返回参数：{}", response);
            }
            return ResponseCode.getByCode(response.getResponseCode());
        } catch (Exception e) {
            logger.error("修改认证接口调用失败：{}", e);
            e.printStackTrace();
            return ResponseCode.INVOK_FAILURE;
        }
    }

    @Override
    public ResponseCode deleteVerify(long verifyId, OperationEnvironment env) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("移除认证接口请求参数：{},{}", verifyId, env);
            }
            Response response = verifyFacade.deleteVerify(env, verifyId);
            if (logger.isInfoEnabled()) {
                logger.info("移除认证接口返回参数：{}", response);
            }
            return ResponseCode.getByCode(response.getResponseCode());
        } catch (Exception e) {
            logger.error("移除认证接口调用失败：{}", e);
            e.printStackTrace();
            return ResponseCode.INVOK_FAILURE;
        }
    }

    @Override
    public List<AuthVerifyInfo> queryVerify(AuthVerifyInfo info, OperationEnvironment env) {
        List<AuthVerifyInfo> authVerifyInfos = new ArrayList<AuthVerifyInfo>();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("查询本会员的认证信息接口请求参数：{},{}", info, env);
            }
            AuthVerifyConvert convert = new AuthVerifyConvert();
            VerifyQueryRequest request = convert.toVefifyQueryConvert(info);
            VerifyInfoResponse response = verifyFacade.queryVerify(env, request);
            if (logger.isInfoEnabled()) {
                logger.info("查询本会员的认证信息接口返回参数：{}", response);
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                authVerifyInfos = convert.toAuthVerifyInfoConvert(response.getVerifyInfos());
            }
        } catch (Exception e) {
            logger.error("查询本会员的认证信息接口调用失败：{}", e);
            e.printStackTrace();
        }
        return authVerifyInfos;
    }

}
