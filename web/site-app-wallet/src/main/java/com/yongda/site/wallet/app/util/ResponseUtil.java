package com.yongda.site.wallet.app.util;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;

import com.meidusa.fastjson.JSON;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.exception.ErrorCodeException;

public class ResponseUtil {
	
	public static void buildExpResponse(ErrorCodeException.CommonException exp,ServletResponse response)throws  IOException, ServletException {
        RestResponse jsonResp = buildExpResponse(exp);
        /* 设置格式为text/json */
        response.setContentType("text/json"); 
        /*设置字符集为'UTF-8'*/
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSON.toJSONString(jsonResp));
    }
	
	public static RestResponse buildExpResponse(ErrorCodeException.CommonException exp) {
		return buildExpResponse(exp.getErrorCode(),exp.getErrorMsg());
    }
	
	public static RestResponse buildExpResponse(ErrorCodeException.CommonException exp,String errorMsg) {
		return buildExpResponse(exp.getErrorCode(),errorMsg);
    }
	
	public static RestResponse buildExpResponse(String errorCode,String errorMsg) {
        RestResponse jsonResp = new RestResponse();
        jsonResp.setSuccess(false);
        jsonResp.setCode(errorCode);
        jsonResp.setMessage(errorMsg);
		return jsonResp;
    }
	
	public static RestResponse buildSuccessResponse() {
        RestResponse jsonResp = new RestResponse();
        jsonResp.setData(new HashMap<String, Object>());
        jsonResp.setSuccess(true);
		return jsonResp;
    }
}
