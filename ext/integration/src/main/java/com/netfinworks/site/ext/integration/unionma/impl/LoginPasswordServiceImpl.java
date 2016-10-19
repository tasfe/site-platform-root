package com.netfinworks.site.ext.integration.unionma.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.kjt.unionma.api.login.request.LoginRequestParam;
import com.kjt.unionma.api.login.response.LoginRequestResponse;
import com.kjt.unionma.api.password.request.LoginPasswordEditRequestParam;
import com.kjt.unionma.api.password.response.LoginPasswordEditResponse;
import com.kjt.unionma.api.password.service.LoginPasswordFacdeWS;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.request.loginPasswordEditReq;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.unionma.LoginPasswordService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;

@Service("loginPasswordService")
public class LoginPasswordServiceImpl implements LoginPasswordService {
	private Logger        logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private LoginPasswordFacdeWS loginPasswordFacdeWSC;
	
	@Override
	public UnionmaBaseResponse loginPasswordEdit(loginPasswordEditReq req)
			throws BizException {
		UnionmaBaseResponse response = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            LoginPasswordEditRequestParam request = UnionmaConvert.createLoginPasswordEditRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("登录密码修改，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            LoginPasswordEditResponse resp = loginPasswordFacdeWSC.loginPasswordEdit(request);
            BeanUtils.copyProperties(resp, response);
            if(resp.getIsSuccess()){
            	response.setSuccess(resp.getIsSuccess());
            }
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("登录密码修改， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("登录密码修改异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return response;
	}
	
	private void validate(loginPasswordEditReq req) {
        // 参数验证
        Assert.notNull(req.getOperatorId());
        Assert.notNull(req.getOldPassword());
        Assert.notNull(req.getNewPassword());
        
        Assert.notNull(req.getEnsureNewPassword());
        Assert.notNull(req.getMemberType());
        Assert.notNull(req.getLoginName());
    }

}
