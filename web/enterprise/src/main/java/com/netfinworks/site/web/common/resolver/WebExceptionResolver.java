package com.netfinworks.site.web.common.resolver;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 * <p>WEB层异常解释器</p>
 * @author eric
 * @version $Id: WebExceptionResolver.java, v 0.1 2010-3-26 下午02:44:47  Exp $
 */
public class WebExceptionResolver extends SimpleMappingExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(WebExceptionResolver.class);

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver#logException(java.lang.Exception, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void logException(Exception ex, HttpServletRequest request) {
        logger.error(buildLogMessage(ex, request), ex);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver#buildLogMessage(java.lang.Exception, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected String buildLogMessage(Exception ex, HttpServletRequest request) {
        StringBuffer message = new StringBuffer("页面操作异常");
        message.append("页面路径").append(request.getRequestURI());
        logger.error(message.toString(),ex);
        request.setAttribute("error", ex);
        return message.toString();
    }
}
