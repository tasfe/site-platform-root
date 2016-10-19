package com.netfinworks.site.ext.integration.auth.inner.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.authorize.ws.clientservice.IMemberService;
import com.netfinworks.authorize.ws.dataobject.OperatorRole;
import com.netfinworks.authorize.ws.request.impl.AddRoleToMemberRequest;
import com.netfinworks.authorize.ws.request.impl.GetFunctionListFromMemberRequest;
import com.netfinworks.authorize.ws.request.impl.GetOperatorRoleListFromMemberRequest;
import com.netfinworks.authorize.ws.response.impl.AddRoleToMemberResponse;
import com.netfinworks.authorize.ws.response.impl.GetFunctionListFromMemberResponse;
import com.netfinworks.authorize.ws.response.impl.GetOperatorRoleListFromMemberResponse;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.auth.convert.AuthVOConvert;
import com.netfinworks.site.ext.integration.auth.convert.FunctionVOConvert;
import com.netfinworks.site.ext.integration.auth.inner.MemberAuthInnerService;

@Service("memberAuthInnerService")
public class MemberAuthInnerServiceImpl implements MemberAuthInnerService {
private Logger        logger = LoggerFactory.getLogger(getClass());
    
    @Resource(name = "memberInnerClient")
    private IMemberService memberInnerClient;
    
    @Override
    public List<FunctionVO> getFunctionListFromMember(AuthVO authVO) throws BizException {
        String opMsg="查询某个会员拥有功能列表(对内服务)";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：authVO:{}", authVO);
            }
            GetFunctionListFromMemberRequest  request=AuthVOConvert.createGetFuncList4MemberRequest(authVO);
            GetFunctionListFromMemberResponse response = memberInnerClient.getFunctionListFromMember(request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            return FunctionVOConvert.covertFunctionVOList(response.getFunctionList());
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"参数：authVO:{},异常信息{}", authVO, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

	@Override
	public List<OperatorRole> getOperatorRoleListFromMember(AuthVO authVO) throws BizException {
		String opMsg = "查询会员角色列表";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			GetOperatorRoleListFromMemberRequest request = AuthVOConvert
					.createGetOperatorRoleList4MemberRequest(authVO);
			GetOperatorRoleListFromMemberResponse response = memberInnerClient.getOperatorRoleListFromMember(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			return response.getOperatorRoleList();
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public boolean addRoleToMember(AuthVO authVO) throws BizException {
		String opMsg = "添加会员角色";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：authVO:{}", authVO);
			}
			AddRoleToMemberRequest request = AuthVOConvert.createAddRoleToMemberRequest(authVO);
			AddRoleToMemberResponse response = memberInnerClient.addRoleToMember(request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			if (ResponseCode.SUCCESS.getCode().equals(response.getResponse().getResponseCode())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "参数：authVO:{},异常信息{}", authVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

}
