package com.netfinworks.site.ext.integration.auth.convert;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.netfinworks.authorize.ws.request.impl.AddFunctionListToOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.AddFunctionToOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.AddRoleToMemberRequest;
import com.netfinworks.authorize.ws.request.impl.AddRoleToOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.CheckFunctionFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.CheckRoleFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.CreateOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.GetFunctionListFromMemberRequest;
import com.netfinworks.authorize.ws.request.impl.GetFunctionListFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.GetFunctionListFromOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.GetOperatorRoleListFromMemberRequest;
import com.netfinworks.authorize.ws.request.impl.GetRoleListFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveFunctionFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveFunctionListFromOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveOperatorRoleRequest;
import com.netfinworks.authorize.ws.request.impl.RemoveRoleFromOperatorRequest;
import com.netfinworks.authorize.ws.request.impl.RequestHeader;
import com.netfinworks.authorize.ws.request.impl.UpdateOperatorRoleNameRequest;
import com.netfinworks.site.domain.domain.auth.AuthVO;

public class AuthVOConvert {

    public static GetFunctionListFromMemberRequest createGetFuncList4MemberRequest(AuthVO authVO){
        if(authVO==null){
            return null;
        }
        GetFunctionListFromMemberRequest request=new GetFunctionListFromMemberRequest();
        BeanUtils.copyProperties(authVO, request);
        request.setHeader(createRequestHeader(authVO.getSourceId()));
        return request;
        
    }
    
    public static CheckFunctionFromOperatorRequest createCheckFuncOpRequest(AuthVO authVO){
        if(authVO==null){
            return null;
        }
        CheckFunctionFromOperatorRequest request=new CheckFunctionFromOperatorRequest();
        BeanUtils.copyProperties(authVO, request);
        request.setHeader(createRequestHeader(authVO.getSourceId()));
        return request;
    }
    
    public static RemoveFunctionFromOperatorRequest createRemoveFuncOpRequest(AuthVO authVO){
        if(authVO==null){
            return null;
        }
        RemoveFunctionFromOperatorRequest request=new RemoveFunctionFromOperatorRequest();
        BeanUtils.copyProperties(authVO, request);
        request.setHeader(createRequestHeader(authVO.getSourceId()));
        if(StringUtils.isBlank(request.getMemo())){
            request.setMemo("memo");
        }
        return request;
    }
    
    public static AddFunctionToOperatorRequest createAddFuncOpRequest(AuthVO authVO){
        if(authVO==null){
            return null;
        }
        AddFunctionToOperatorRequest request=new AddFunctionToOperatorRequest();
        BeanUtils.copyProperties(authVO, request);
        request.setHeader(createRequestHeader(authVO.getSourceId()));
        if(StringUtils.isBlank(request.getMemo())){
            request.setMemo("memo");
        }
        return request;
    }
    
    public static GetFunctionListFromOperatorRequest createGetFuncList4OpRequest(AuthVO authVO){
        if(authVO==null){
            return null;
        }
        GetFunctionListFromOperatorRequest request=new GetFunctionListFromOperatorRequest();
        BeanUtils.copyProperties(authVO, request);
        request.setHeader(createRequestHeader(authVO.getSourceId()));
        return request;
    }
    
	public static GetOperatorRoleListFromMemberRequest createGetOperatorRoleList4MemberRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		GetOperatorRoleListFromMemberRequest request = new GetOperatorRoleListFromMemberRequest();
		BeanUtils.copyProperties(authVO, request);
		RequestHeader header = createRequestHeader(authVO.getSourceId());
		header.setOperatorType(authVO.getOperatorType());
		header.setRequestOperator(authVO.getRequestOperator());
		request.setHeader(header);
		return request;
	}

	public static CreateOperatorRoleRequest createOperatorRole4OperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		CreateOperatorRoleRequest request = new CreateOperatorRoleRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static AddFunctionListToOperatorRoleRequest createAddFunctionListToOperatorRole4OperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		AddFunctionListToOperatorRoleRequest request = new AddFunctionListToOperatorRoleRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static UpdateOperatorRoleNameRequest createUpdateOperatorRoleName4OperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		UpdateOperatorRoleNameRequest request = new UpdateOperatorRoleNameRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static RemoveOperatorRoleRequest createRemoveOperatorRole4OperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		RemoveOperatorRoleRequest request = new RemoveOperatorRoleRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static GetFunctionListFromOperatorRoleRequest createGetFunctionListFromOperatorRole4OperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		GetFunctionListFromOperatorRoleRequest request = new GetFunctionListFromOperatorRoleRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static RemoveFunctionListFromOperatorRoleRequest createRemoveFunctionListFromOperatorRole4OperRequest(
			AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		RemoveFunctionListFromOperatorRoleRequest request = new RemoveFunctionListFromOperatorRoleRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static GetRoleListFromOperatorRequest createGetRoleListFromOperator4OperRequest(
			AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		GetRoleListFromOperatorRequest request = new GetRoleListFromOperatorRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static RemoveRoleFromOperatorRequest createRemoveRoleFromOperator4OperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		RemoveRoleFromOperatorRequest request = new RemoveRoleFromOperatorRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static CheckRoleFromOperatorRequest createCheckRoleFromOperator4OperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		CheckRoleFromOperatorRequest request = new CheckRoleFromOperatorRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static AddRoleToOperatorRequest createAddRoleToOperatorOperRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		AddRoleToOperatorRequest request = new AddRoleToOperatorRequest();
		BeanUtils.copyProperties(authVO, request);
		request.setHeader(createRequestHeader(authVO.getSourceId()));
		return request;
	}

	public static AddRoleToMemberRequest createAddRoleToMemberRequest(AuthVO authVO) {
		if (authVO == null) {
			return null;
		}
		AddRoleToMemberRequest request = new AddRoleToMemberRequest();
		BeanUtils.copyProperties(authVO, request);
		RequestHeader header = createRequestHeader(authVO.getSourceId());
		header.setOperatorType(authVO.getOperatorType());
		header.setRequestOperator(authVO.getRequestOperator());
		request.setHeader(header);
		return request;
	}

    public static RequestHeader createRequestHeader(String sourceId){
        RequestHeader header = new RequestHeader();
        header.setSourceId(sourceId);
        header.setRequestTime(new Date());
        header.setVersion("1.0");
        header.setRequestNo(UUID.randomUUID().toString().replace("-", ""));
        return header;
        
    }
    
}
