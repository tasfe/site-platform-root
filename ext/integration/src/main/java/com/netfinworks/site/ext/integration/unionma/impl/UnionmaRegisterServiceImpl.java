package com.netfinworks.site.ext.integration.unionma.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.kjt.unionma.api.password.request.PayPasswordSetRequestParam;
import com.kjt.unionma.api.password.response.PayPasswordSetResponse;
import com.kjt.unionma.api.register.request.RegisterPerfectionIdentityRequestParam;
import com.kjt.unionma.api.register.request.RegisterRequestParam;
import com.kjt.unionma.api.register.request.RegisterSetPaymentRequestParam;
import com.kjt.unionma.api.register.response.RegisterPerfectionIdentityResponse;
import com.kjt.unionma.api.register.response.RegisterResponse;
import com.kjt.unionma.api.register.response.RegisterSetPaymentResponse;
import com.kjt.unionma.api.register.service.RegisterFacadeWS;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.request.UnionmaPerfectionIdentityReq;
import com.netfinworks.site.domain.domain.request.UnionmaRegisterRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSetPaymentReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaRegisterResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.unionma.UnionmaRegisterService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;

@Service("unionmaRegisterService")
public class UnionmaRegisterServiceImpl implements UnionmaRegisterService {

	private Logger        logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private RegisterFacadeWS registerFacadeWSC;
	@Override
	public UnionmaRegisterResponse register(UnionmaRegisterRequest req)
			throws BizException {
		UnionmaRegisterResponse response = new UnionmaRegisterResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            RegisterRequestParam request = UnionmaConvert.createRegisterRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("注册激活，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            RegisterResponse resp = registerFacadeWSC.register(request);
            if(resp.getIsSuccess()){
            	response.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, response);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("注册激活， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("注册激活异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return response;
	}
	private void validate(UnionmaRegisterRequest req) {
        // 参数验证
        Assert.notNull(req.getMemberId());
        Assert.notNull(req.getRegisterType());
        Assert.notNull(req.getLoginName());
        Assert.notNull(req.getPersonIdentiy());
    }
	@Override
	public UnionmaBaseResponse perfectionIdentity(
			UnionmaPerfectionIdentityReq req) throws BizException {
		UnionmaBaseResponse response = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            RegisterPerfectionIdentityRequestParam request = UnionmaConvert.createRegisterPerfectionIdentityRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("完善身份信息，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            RegisterPerfectionIdentityResponse resp = registerFacadeWSC.perfectionIdentity(request);
            if(resp.getIsSuccess()){
            	response.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, response);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("完善身份信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("完善身份信息异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return response;
	}
	private void validate(UnionmaPerfectionIdentityReq req) {
        // 参数验证
        Assert.notNull(req.getMemberId());
        Assert.notNull(req.getLoginPassword());
        Assert.notNull(req.getEnsureLoginPassword());
        Assert.notNull(req.getPayPassword());
        
        Assert.notNull(req.getEnsurePayPassword());
    }
	@Override
	public UnionmaBaseResponse setPayment(UnionmaSetPaymentReq req)
			throws BizException {
		UnionmaBaseResponse response = new UnionmaBaseResponse();
    	try {
            validate(req);

            long beginTime = 0L;
            RegisterSetPaymentRequestParam request = UnionmaConvert.createRegisterSetPaymentRequestParam(req);

            if (logger.isInfoEnabled()) {
                logger.info("设置支付方式，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            RegisterSetPaymentResponse resp = registerFacadeWSC.setPayment(request);
            if(resp.getIsSuccess()){
            	response.setSuccess(resp.getIsSuccess());
            }
            BeanUtils.copyProperties(resp, response);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("设置支付方式， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }
        catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            else {
                logger.error("设置支付方式异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return response;
	}
	private void validate(UnionmaSetPaymentReq req) {
        // 参数验证
        Assert.notNull(req.getMemberId());
        Assert.notNull(req.getRealName());
        Assert.notNull(req.getPersonName());
        Assert.notNull(req.getBankCode());
        Assert.notNull(req.getBankName());
        Assert.notNull(req.getBankBranch());
        Assert.notNull(req.getBankBranchNo());
        Assert.notNull(req.getBankAccountNo());
        Assert.notNull(req.getProvince());
        Assert.notNull(req.getCity());
        Assert.notNull(req.getMemberIdentity());
        Assert.notNull(req.getOperatorId());
    }
}
