package com.netfinworks.site.ext.integration.auth.outer.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.authorize.ws.clientservice.IOperatorService;
import com.netfinworks.authorize.ws.request.impl.AddFunctionToOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.CheckFunctionFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.GetFunctionListFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveFunctionFromOperatorRequest;
import com.netfinworks.authorize.ws.response.impl.AddFunctionToOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.CheckFunctionFromOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.GetFunctionListFromOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.RemoveFunctionFromOperatorResponse;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.auth.convert.AuthVOConvert;
import com.netfinworks.site.ext.integration.auth.convert.FunctionVOConvert;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;

@Service
public class OperatorAuthOuterServiceImpl implements OperatorAuthOuterService {

    private Logger           logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "operatorOuterClient")
    private IOperatorService operatorOuterClient;

    @Override
    public boolean addFunctionToOperator(AuthVO authVO) throws BizException {
        String opMsg = "给某个操作员的添加某个功能(对内服务)";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg + "，请求参数：AuthVO:{}", authVO);
            }
            AddFunctionToOperatorRequest request = AuthVOConvert.createAddFuncOpRequest(authVO);
            AddFunctionToOperatorResponse response = operatorOuterClient
                .addFunctionToOperator(request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponse().getResponseCode())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg + "参数：AuthVO:{}，异常信息{}", authVO, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public boolean removeFunctionFromOperator(AuthVO authVO) throws BizException {
        String opMsg = "删除某个操作员的某个功能(对内服务)";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg + "，请求参数：AuthVO:{}", authVO);
            }
            RemoveFunctionFromOperatorRequest request = AuthVOConvert
                .createRemoveFuncOpRequest(authVO);
            RemoveFunctionFromOperatorResponse response = operatorOuterClient
                .removeFunctionFromOperator(request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponse().getResponseCode())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg + "参数：AuthVO:{}，异常信息{}", authVO, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public boolean checkFunctionFromOperator(AuthVO authVO) throws BizException {
        String opMsg = "验证操作员是否拥有某个功能(对外服务)";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg + "，请求参数：{}", authVO);
            }
            CheckFunctionFromOperatorRequest request = AuthVOConvert
                .createCheckFuncOpRequest(authVO);
            CheckFunctionFromOperatorResponse response = operatorOuterClient
                .checkFunctionFromOperator(request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            return response.isAuthorized();
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg + "{}信息异常:异常信息{}", authVO, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

	@Override
	public List<FunctionVO> getFunctionListFromOperator(AuthVO authVO) throws BizException {
		String opMsg = "查询某个操作员拥有的功能列表(对内服务)";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			GetFunctionListFromOperatorRequest request = AuthVOConvert.createGetFuncList4OpRequest(authVO);
			GetFunctionListFromOperatorResponse response = operatorOuterClient.getFunctionListFromOperator(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return FunctionVOConvert.covertFunctionVOList(response.getFunctionList());
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},{}，异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}



}
