package com.netfinworks.site.ext.integration.unionma.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.kjt.unionma.api.bind.request.BindEmailRequestParam;
import com.kjt.unionma.api.bind.request.BindMobileEditRequestParam;
import com.kjt.unionma.api.bind.request.CheckEmailTokenRequestParam;
import com.kjt.unionma.api.bind.request.UnBindEmailRequestParam;
import com.kjt.unionma.api.bind.request.VerifyAuthCodeRequestParam;
import com.kjt.unionma.api.bind.response.BindEmailResponse;
import com.kjt.unionma.api.bind.response.BindMobileEditResponse;
import com.kjt.unionma.api.bind.response.CheckEmailTokenResponse;
import com.kjt.unionma.api.bind.response.UnBindEmailResponse;
import com.kjt.unionma.api.bind.response.VerifyAuthCodeResponse;
import com.kjt.unionma.api.bind.service.BindFacadeWS;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.request.UnionmaBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaBindMobileEditRequest;
import com.netfinworks.site.domain.domain.request.UnionmaCheckEmailTokenRequest;
import com.netfinworks.site.domain.domain.request.UnionmaUnBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaVerifyAuthCodeRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaBindEmailResponse;
import com.netfinworks.site.domain.domain.response.UnionmaBindMobileEditResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.unionma.BindFacadeService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;

@Service("bindFacadeService")
public class BindFacadeServiceImpl implements BindFacadeService {
	private Logger        logger = LoggerFactory.getLogger(this.getClass());

	@Resource
    private BindFacadeWS bindFacadeWSC;
	
	@Override
	public UnionmaBindMobileEditResponse bindMobileEdit(UnionmaBindMobileEditRequest req) throws BizException{
		
		UnionmaBindMobileEditResponse resp = new UnionmaBindMobileEditResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            BindMobileEditRequestParam request = UnionmaConvert.createBindMobileEditRequestParam(req);

            if (logger.isInfoEnabled()) {
            	logger.info("修改绑定手机，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            BindMobileEditResponse result = bindFacadeWSC.bindMobileEdit(request);
            if(result.getIsSuccess()){
            	resp.setSuccess(result.getIsSuccess());
            }
            BeanUtils.copyProperties(result, resp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("修改绑定手机， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("修改绑定手机异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
		return resp;
	}
	 
	@Override
	public UnionmaBaseResponse verifyAuthCode(
			UnionmaVerifyAuthCodeRequest req,OperationEnvironment env) throws BizException {
		UnionmaBaseResponse resp = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            VerifyAuthCodeRequestParam request = UnionmaConvert.createVerifyAuthCodeRequestParam(req,env);

            if (logger.isInfoEnabled()) {
            	logger.info("校验验证码，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            VerifyAuthCodeResponse result = bindFacadeWSC.verifyAuthCode(request);
            if(result.getIsSuccess()){
            	resp.setSuccess(result.getIsSuccess());
            }
            BeanUtils.copyProperties(result, resp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("校验验证码， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("校验验证码异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
		return resp;
	}
	 
	@Override
	public UnionmaBindEmailResponse bindEmail(UnionmaBindEmailRequest req)
			throws BizException {
		UnionmaBindEmailResponse resp = new UnionmaBindEmailResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            BindEmailRequestParam request = UnionmaConvert.createBindEmailRequestParam(req);

            if (logger.isInfoEnabled()) {
            	logger.info("绑定邮箱，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            BindEmailResponse result = bindFacadeWSC.bindEmail(request);
            if(result.getIsSuccess()){
            	resp.setSuccess(result.getIsSuccess());
            }
            BeanUtils.copyProperties(result, resp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("绑定邮箱， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("绑定邮箱异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
		return resp;
	}
	
	 
	@Override
	public UnionmaBaseResponse checkEmailToken(
			UnionmaCheckEmailTokenRequest req) throws BizException {
		UnionmaBaseResponse resp = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            CheckEmailTokenRequestParam request = UnionmaConvert.createCheckEmailTokenRequestParam(req);

            if (logger.isInfoEnabled()) {
            	logger.info("验证邮箱凭证，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            CheckEmailTokenResponse result = bindFacadeWSC.checkEmailToken(request);
            if(result.getIsSuccess()){
            	resp.setSuccess(result.getIsSuccess());
            }
            BeanUtils.copyProperties(result, resp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("验证邮箱凭证， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("验证邮箱凭证异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
		return resp;
	}
	
	
	@Override
	public UnionmaBaseResponse unBindEmail(UnionmaUnBindEmailRequest req)
			throws BizException {
		UnionmaBaseResponse resp = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            UnBindEmailRequestParam request = UnionmaConvert.createUnBindEmailRequestParam(req);

            if (logger.isInfoEnabled()) {
            	logger.info("解绑邮箱，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            UnBindEmailResponse result = bindFacadeWSC.unBindEmail(request);
            if(result.getIsSuccess()){
            	resp.setSuccess(result.getIsSuccess());
            }
            BeanUtils.copyProperties(result, resp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("解绑邮箱， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("解绑邮箱异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
		return resp;
	}
	
	private void validate(UnionmaBindMobileEditRequest req) {
		// 参数验证
		Assert.notNull(req.getMemberId());
		Assert.notNull(req.getNewPhoneNumber());
	}
	private void validate(UnionmaVerifyAuthCodeRequest req) {
       // 参数验证
       Assert.notNull(req.getMemberId());
       Assert.notNull(req.getVerifyValue());
       Assert.notNull(req.getAuthCode());
       Assert.notNull(req.getBizType());
	}
	private void validate(UnionmaBindEmailRequest req) {
	    // 参数验证
	    Assert.notNull(req.getMemberId());
	    Assert.notNull(req.getToken());
	}
	
	private void validate(UnionmaCheckEmailTokenRequest req) {
        // 参数验证
        Assert.notNull(req.getMemberId());
        Assert.notNull(req.getToken());
}
	private void validate(UnionmaUnBindEmailRequest req) {
        // 参数验证
        Assert.notNull(req.getMemberId());
	}
}
