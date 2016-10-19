package com.netfinworks.site.web.common.resolver;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * <p>WEB层异常解释器</p>
 * @author eric
 * @version $Id: WebExceptionResolver.java, v 0.1 2010-3-26 下午02:44:47  Exp $
 */
public class WebExceptionResolver extends SimpleMappingExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(WebExceptionResolver.class);

    /**
     * (non-Javadoc)
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver#logException(java.lang.Exception, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void logException(Exception ex, HttpServletRequest request) {
        logger.error(buildLogMessage(ex, request), ex);
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver#buildLogMessage(java.lang.Exception, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected String buildLogMessage(Exception ex, HttpServletRequest request) {
    	ex.printStackTrace();
        StringBuffer message = new StringBuffer("页面操作异常:");
        message.append("页面路径==").append(request.getRequestURI());
        logger.error(message.toString());
        request.setAttribute("error", ex);
        PersonMember user = null;
        String userString = request.getSession().getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER) != null 
        		? request.getSession().getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER).toString() : null;
        if (StringUtils.isNotBlank(userString)) {
        	user = JsonUtils.parse(userString, PersonMember.class);
        }
        if(user != null) {
            RestResponse restP = new RestResponse();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("member", user);
            restP.setData(data);
            request.setAttribute("response", restP);
        }
        return message.toString();
    }
}
