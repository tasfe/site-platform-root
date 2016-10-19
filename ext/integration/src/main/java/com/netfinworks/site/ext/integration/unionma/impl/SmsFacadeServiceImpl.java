package com.netfinworks.site.ext.integration.unionma.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.kjt.unionma.api.login.request.LoginRequestParam;
import com.kjt.unionma.api.login.response.LoginRequestResponse;
import com.kjt.unionma.api.login.service.LoginFacadeWS;
import com.kjt.unionma.api.sms.request.SendEmailRquestParam;
import com.kjt.unionma.api.sms.request.SendMessageRquestParam;
import com.kjt.unionma.api.sms.response.SendEmailResponse;
import com.kjt.unionma.api.sms.response.SendMessageResponse;
import com.kjt.unionma.api.sms.service.SmsFacadeWS;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.request.LoginNameEditRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSendEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSendMessageRequest;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaSendEmailResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.unionma.SmsFacadeService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
@Service("smsFacadeService")
public class SmsFacadeServiceImpl implements SmsFacadeService {
	 private Logger        logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SmsFacadeWS SmsFacadeWSC;
    
	@Override
	public UnionmaSendEmailResponse sendEmail(UnionmaSendEmailRequest req)
			throws BizException {
		UnionmaSendEmailResponse resp = new UnionmaSendEmailResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            SendEmailRquestParam request = UnionmaConvert.createSendEmailRquestParam(req);

            if (logger.isInfoEnabled()) {
            	logger.info("发送邮箱邮件，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            SendEmailResponse result = SmsFacadeWSC.sendEmail(request);
            if(result.getIsSuccess()){
            	resp.setSuccess(result.getIsSuccess());
            }
            BeanUtils.copyProperties(result, resp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("发送邮箱邮件， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("发送邮箱邮件异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return resp;
	}

	@Override
	public UnionmaBaseResponse sendMessage(UnionmaSendMessageRequest req,OperationEnvironment env)
			throws BizException {
		UnionmaBaseResponse resp = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            SendMessageRquestParam request = UnionmaConvert.createSendMessageRquestParam(req,env);

            if (logger.isInfoEnabled()) {
            	logger.info("发送手机短信，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            SendMessageResponse result = SmsFacadeWSC.sendMessage(request);
            if(result.getIsSuccess()){
            	resp.setSuccess(result.getIsSuccess());
            }
            BeanUtils.copyProperties(result, resp);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("发送手机短信， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("发送手机短信异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return resp;
	}
	 private void validate(UnionmaSendEmailRequest req) {
	        // 参数验证
	        Assert.hasText(req.getEmail());
	        Assert.notNull(req.getUserName());
	        Assert.notNull(req.getActiveUrl());
	        Assert.notNull(req.getToken());
	        Assert.notNull(req.getSendType());
	        Assert.notNull(req.getBizType());
	    }
	 
	 private void validate(UnionmaSendMessageRequest req) {
	        // 参数验证
	        Assert.notNull(req.getBizType());
	        Assert.hasText(req.getMobile());
	    }
}
