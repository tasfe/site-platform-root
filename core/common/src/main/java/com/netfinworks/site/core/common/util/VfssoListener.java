package com.netfinworks.site.core.common.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.meidusa.fastjson.JSON;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.client.authapi.domain.User;
import com.netfinworks.vfsso.client.cas.listener.DefaultListener;
import com.netfinworks.vfsso.client.common.CasEvent;
import com.netfinworks.vfsso.client.common.CasEvent.CasEventType;
import com.netfinworks.vfsso.client.common.VfSsoClientLogger;
import com.netfinworks.vfsso.client.exception.CasServiceException;

public class VfssoListener extends DefaultListener {
    private static Logger logger = VfSsoClientLogger.getLogger();

    @Override
    public void handleExceptionEvent(CasEvent event) {
    	// TODO Auto-generated method stub
        super.handleExceptionEvent(event);
    }
    
    @Override
	public void handleNotLoginEvent(CasEvent event) {
    	String appId = event.getConfig().getApiConfig().getAppId();
		if(event.getType() == CasEventType.AUTH_NOT_LOGIN && "H5Person".equals(appId)){
    		RestResponse jsonResp = new RestResponse();
            jsonResp.setSuccess(false);
            jsonResp.setCode("NOT_LOGIN");
            jsonResp.setMessage("未登录");
	        /* 设置格式为text/json */
    		event.getResponse().setContentType("text/json"); 
	        /*设置字符集为'UTF-8'*/
    		event.getResponse().setCharacterEncoding("UTF-8");
    		try {
				event.getResponse().getWriter().write(JSON.toJSONString(jsonResp));
			} catch (IOException e) {
			}
        }else{
        	super.handleNotLoginEvent(event);
        }
	}



	@Override
    public void handleForbiddenEvent(CasEvent event) {
        String appId = event.getConfig().getApiConfig().getAppId();
        
        if ("post".equalsIgnoreCase(event.getRequest().getMethod())){//post请求  输出Json 	
        	writeRedirectResponse(event.getResponse());
        	return ;
        }
        
        String loginUrl = event.getConfig().getApiConfig().getLoginUrl();
		String forbiddenUrl = loginUrl.substring(0, loginUrl.lastIndexOf("/") + 1) + "authReject.htm";
        try {
            User vfSsoUser = VfSsoUser.get();
			if ("EWEnterprise".equals(appId)
					&& ("person".equals(vfSsoUser.getUserType()) || "enterprise".equals(vfSsoUser.getUserType()))) {// 个人用户已登录的情况下访问特约商户网站，跳转到登录页面
                this.redirectToLogin(event);
			} else if ("EWPersonal".equals(appId) && "merchant".equals(vfSsoUser.getUserType())) {// 特约商户已登录的情况下访问个人用户网站，跳转到登录页面
                this.redirectToLogin(event);
            } else {
                this.redirectToUrl(event.getRequest(), event.getResponse(), forbiddenUrl);
            }
        } catch (CasServiceException e) {
            this.redirectToLogin(event);
        }
        //super.handleForbiddenEvent(event);
    }

    private void redirectToLogin(CasEvent event) {
        String returnUrl = event.getRequest().getRequestURL().toString();
        String queryString = event.getRequest().getQueryString();
        // 去掉queryString中的GuardianCookie信息
        queryString = removeCookieParam(queryString);
        if (queryString != null) {
            returnUrl = returnUrl + "?" + queryString;
        }
        // 未登录 跳转到登录页面
        try {
            event.getResponse().sendRedirect(
                event.getConfig().getApiConfig().buildLoginUrl(returnUrl));
        } catch (IOException e) {
            logger.error("重定向异常", e);
        }
    }

    private void redirectToUrl(HttpServletRequest request, HttpServletResponse response, String url) {
        try {
            //response.sendRedirect(request.getContextPath() + url);
            response.sendRedirect(url);
        } catch (IOException e) {
            logger.error("重定向到" + url + "页面出错", e);
        }
    }

	private void writeRedirectResponse(HttpServletResponse response) {
		RestResponse restP = new RestResponse();
    	restP.setSuccess(false);
    	restP.setMessage("没有权限");
		try {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().write(JSON.toJSONString(restP));
		} catch (IOException e) {
			logger.error("输出Json 重定向消息出错", e);
		}
	}
    //	
    //	private void writeForbiddenResponse(HttpServletResponse response) {
    //		Map<String, Object> resp = new HashMap<String, Object>();
    //		resp.put("filterFoundForbidden", true);
    //		try {
    //			response.setCharacterEncoding("UTF-8");
    //			response.setContentType("application/json;charset=utf-8");
    //			response.getWriter().write(JsonUtil.serialize(resp));
    //		} catch (IOException e) {
    //			logger.error("输出Json禁止消息出错", e);
    //		}
    //	}

    private boolean isDialogRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return "ria-dlg".equals(accept);
    }

    private boolean isModuleRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return "ria-module".equals(accept);
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestWith);
    }
}
