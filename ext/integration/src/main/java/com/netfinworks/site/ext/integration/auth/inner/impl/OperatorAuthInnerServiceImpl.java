package com.netfinworks.site.ext.integration.auth.inner.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.authorize.ws.clientservice.IOperatorRoleService;
import com.netfinworks.authorize.ws.clientservice.IOperatorService;
import com.netfinworks.authorize.ws.dataobject.OperatorRole;
import com.netfinworks.authorize.ws.request.impl.AddFunctionListToOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.AddRoleToOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.CheckRoleFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.CreateOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.GetFunctionListFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.GetFunctionListFromOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.GetRoleListFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveFunctionListFromOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveRoleFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.UpdateOperatorRoleNameRequest;
import com.netfinworks.authorize.ws.response.impl.AddFunctionListToOperatorRoleResponse;
import com.netfinworks.authorize.ws.response.impl.AddRoleToOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.CheckRoleFromOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.CreateOperatorRoleResponse;
import com.netfinworks.authorize.ws.response.impl.GetFunctionListFromOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.GetFunctionListFromOperatorRoleResponse;
import com.netfinworks.authorize.ws.response.impl.GetRoleListFromOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.RemoveFunctionListFromOperatorRoleResponse;
import com.netfinworks.authorize.ws.response.impl.RemoveOperatorRoleResponse;
import com.netfinworks.authorize.ws.response.impl.RemoveRoleFromOperatorResponse;
import com.netfinworks.authorize.ws.response.impl.ResponseHeader;
import com.netfinworks.authorize.ws.response.impl.UpdateOperatorRoleNameResponse;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.auth.convert.AuthVOConvert;
import com.netfinworks.site.ext.integration.auth.convert.FunctionVOConvert;
import com.netfinworks.site.ext.integration.auth.inner.OperatorAuthInnerService;

@Service
public class OperatorAuthInnerServiceImpl implements OperatorAuthInnerService {

    private Logger        logger = LoggerFactory.getLogger(getClass());
    
    @Resource(name = "operatorInnerClient")
    private IOperatorService operatorInnerClient;
    
	@Resource(name = "operatorRoleClient")
	private IOperatorRoleService operatorRoleClient;

    @Override
    public List<FunctionVO> getFunctionListFromOperator(AuthVO authVO)
                                                                                           throws BizException {
        String opMsg="查询某个操作员拥有的功能列表(对内服务)";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：authVO:{}", authVO);
            }
            GetFunctionListFromOperatorRequest  request=AuthVOConvert.createGetFuncList4OpRequest(authVO);
            GetFunctionListFromOperatorResponse response = operatorInnerClient.getFunctionListFromOperator(request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            return FunctionVOConvert.covertFunctionVOList(response.getFunctionList());
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"参数：authVO:{},{}，异常信息{}", authVO, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

	@Override
	public OperatorRole createOperatorRole(AuthVO authVO) throws BizException {
		String opMsg = "创建操作员角色";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			CreateOperatorRoleRequest request = AuthVOConvert.createOperatorRole4OperRequest(authVO);
			CreateOperatorRoleResponse response = operatorRoleClient.createOperatorRole(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			OperatorRole role = new OperatorRole();
			role.setRoleId(response.getOperatorroleId());
			return role;
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},{}，异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public ResponseHeader addFunctionListToOperatorRole(AuthVO authVO) throws BizException {
		String opMsg = "为操作员角色添加功能";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			AddFunctionListToOperatorRoleRequest request = AuthVOConvert
					.createAddFunctionListToOperatorRole4OperRequest(authVO);
			AddFunctionListToOperatorRoleResponse response = operatorRoleClient.addFunctionListToOperatorRole(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getResponse();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},{}，异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public ResponseHeader updateOperatorRoleName(AuthVO authVO) throws BizException {
		String opMsg = "更新操作员角色名称";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			UpdateOperatorRoleNameRequest request = AuthVOConvert.createUpdateOperatorRoleName4OperRequest(authVO);
			UpdateOperatorRoleNameResponse response = operatorRoleClient.updateOperatorRoleName(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getResponse();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},{}，异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public ResponseHeader removeOperatorRole(AuthVO authVO) throws BizException {
		String opMsg = "删除操作员角色";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			RemoveOperatorRoleRequest request = AuthVOConvert.createRemoveOperatorRole4OperRequest(authVO);
			RemoveOperatorRoleResponse response = operatorRoleClient.removeOperatorRole(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getResponse();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},{}，异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public List<FunctionVO> getFunctionListFromOperatorRole(AuthVO authVO) throws BizException {
		String opMsg = "获取操作员角色功能列表";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			GetFunctionListFromOperatorRoleRequest request = AuthVOConvert
					.createGetFunctionListFromOperatorRole4OperRequest(authVO);
			GetFunctionListFromOperatorRoleResponse response = operatorRoleClient
					.getFunctionListFromOperatorRole(request);
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

	@Override
	public ResponseHeader removeFunctionListFromOperatorRole(AuthVO authVO) throws BizException {
		String opMsg = "从操作员角色删除功能";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			RemoveFunctionListFromOperatorRoleRequest request = AuthVOConvert
					.createRemoveFunctionListFromOperatorRole4OperRequest(authVO);
			RemoveFunctionListFromOperatorRoleResponse response = operatorRoleClient
					.removeFunctionListFromOperatorRole(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getResponse();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},{}，异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public ResponseHeader removeRoleFromOperator(AuthVO authVO) throws BizException {
		String opMsg = "从操作员删除角色";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			RemoveRoleFromOperatorRequest request = AuthVOConvert.createRemoveRoleFromOperator4OperRequest(authVO);
			RemoveRoleFromOperatorResponse response = operatorInnerClient.removeRoleFromOperator(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getResponse();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public List<OperatorRole> getRoleListFromOperator(AuthVO authVO) throws BizException {
		String opMsg = "获取操作员角色列表";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			GetRoleListFromOperatorRequest request = AuthVOConvert.createGetRoleListFromOperator4OperRequest(authVO);
			GetRoleListFromOperatorResponse response = operatorInnerClient.getRoleListFromOperator(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getOperatorRoleList();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public boolean checkRoleFromOperator(AuthVO authVO) throws BizException {
		String opMsg = "验证操作员是否拥有某个角色";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			CheckRoleFromOperatorRequest request = AuthVOConvert.createCheckRoleFromOperator4OperRequest(authVO);
			CheckRoleFromOperatorResponse response = operatorInnerClient.checkRoleFromOperator(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.isAuthorized();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public ResponseHeader addRoleToOperator(AuthVO authVO) throws BizException {
		String opMsg = "为操作员添加角色";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			AddRoleToOperatorRequest request = AuthVOConvert.createAddRoleToOperatorOperRequest(authVO);
			AddRoleToOperatorResponse response = operatorInnerClient.addRoleToOperator(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getResponse();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}


}
