package com.netfinworks.site.ext.integration.unionma.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.kjt.unionma.api.login.request.LoginNameEditRequestParam;
import com.kjt.unionma.api.login.request.LoginRequestParam;
import com.kjt.unionma.api.login.service.LoginFacadeWS;
import com.netfinworks.site.domain.domain.request.LoginNameEditRequest;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
import com.kjt.unionma.api.login.response.LoginNameEditResponse;
import com.kjt.unionma.api.login.response.LoginRequestResponse;
import com.kjt.unionma.core.common.enumes.SysResponseCode;

@Service("loginFacadeService")
public class LoginFacadeServiceImpl implements LoginFacadeService {
    private Logger        logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private LoginFacadeWS loginFacadeWSC;
    
    @Override
    public LoginNameEditResp login(LoginRequest req) throws BizException{
    	LoginNameEditResp loginResp = new LoginNameEditResp();
    	try {
            validate(req);
            long beginTime = 0L;
            LoginRequestParam request = UnionmaConvert.createLoginRequestParam(req);
            if (logger.isInfoEnabled()) {
            	logger.info("统一账户登录，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            LoginRequestResponse resp = loginFacadeWSC.login(request);
            if(resp.getIsSuccess()){
            	loginResp.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, loginResp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("统一账户登录， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("统一账户登录异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return loginResp;
    }

    @Override
    public UnionmaBaseResponse loginNameEdit(LoginNameEditRequest req) throws BizException {
    	UnionmaBaseResponse editResp=new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            LoginNameEditRequestParam request = UnionmaConvert.createLoginNameEditRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("修改账户名，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            LoginNameEditResponse resp = loginFacadeWSC.loginNameEdit(request);
            if(resp.getIsSuccess()){
            	editResp.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, editResp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("修改账户名， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("修改账户名异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return editResp;
    }

    private void validate(LoginRequest req) {
        // 参数验证
        Assert.notNull(req.getLoginName());
        Assert.hasText(req.getLoginPassowrd());
        Assert.notNull(req.getLoginType());
    }
    private void validate(LoginNameEditRequest req) {
        // 参数验证
        Assert.notNull(req.getMemberId());
        Assert.hasText(req.getMemberId());
        Assert.notNull(req.getOperatorId());
        Assert.notNull(req.getLoginName());
        Assert.notNull(req.getOldName());
        Assert.notNull(req.getLoginNameType());
    }
}
