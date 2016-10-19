package com.netfinworks.site.ext.integration.unionma.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.kjt.unionma.api.common.response.BaseResponse;
import com.kjt.unionma.api.password.request.PayPasswordEditRequestParam;
import com.kjt.unionma.api.password.request.PayPasswordSetRequestParam;
import com.kjt.unionma.api.password.request.PayPasswordVaildRequestParam;
import com.kjt.unionma.api.password.response.PayPasswordEditResponse;
import com.kjt.unionma.api.password.response.PayPasswordSetResponse;
import com.kjt.unionma.api.password.service.PayPasswordFacadeWS;
import com.netfinworks.site.domain.domain.request.PayPasswordCheckReq;
import com.netfinworks.site.domain.domain.request.PayPasswordEditReq;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;

@Service("payPasswordService")
public class PayPasswordServiceImpl implements PayPasswordService {
	private Logger        logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private PayPasswordFacadeWS payPasswordFacadeWSC;
	
	@Override
	public UnionmaBaseResponse payPasswordEdit(PayPasswordEditReq req)
			throws BizException {
		UnionmaBaseResponse response = new UnionmaBaseResponse();
    	try {
            long beginTime = 0L;
            PayPasswordEditRequestParam request = UnionmaConvert.createPayPasswordEditRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("支付密码修改，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            PayPasswordEditResponse resp = payPasswordFacadeWSC.payPasswordEdit(request);
            if(resp.getIsSuccess()){
            	response.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, response);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("支付密码修改， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("支付密码修改异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    	return response;
	}
	
	@Override
	public UnionmaBaseResponse payPasswordSet(PayPasswordSetReq req)
			throws BizException {
		UnionmaBaseResponse response = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            PayPasswordSetRequestParam request = UnionmaConvert.createPayPasswordSetRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("支付密码设置，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            PayPasswordSetResponse resp = payPasswordFacadeWSC.payPasswordSet(request);
            if(resp.getIsSuccess()){
            	response.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, response);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("支付密码设置， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("支付密码设置异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return response;
	}
	

	@Override
	public UnionmaBaseResponse payPasswordCheck(PayPasswordCheckReq req)
			throws BizException {
		UnionmaBaseResponse response = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            PayPasswordVaildRequestParam request = UnionmaConvert.createPayPasswordCheckRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("支付密码检验，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            BaseResponse resp = payPasswordFacadeWSC.payPasswordValid(request);
            if(resp.getIsSuccess()){
            	response.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, response);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("支付密码检验， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("支付密码检验异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return response;
	}



	private void validate(PayPasswordSetReq req) {
        // 参数验证
        Assert.notNull(req.getOperatorId());
        Assert.notNull(req.getPayPassword());
        Assert.notNull(req.getAccountId());
    }
	
	private void validate(PayPasswordCheckReq req) {
        // 参数验证
        Assert.notNull(req.getOperatorId());
        Assert.notNull(req.getPayPassword());
        Assert.notNull(req.getAccountId());
    }
}
