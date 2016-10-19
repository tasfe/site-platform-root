package com.yongda.site.app.filter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.client.cas.AuthService;
import com.netfinworks.vfsso.client.cas.CasResult;
import com.netfinworks.vfsso.client.cas.domain.Audit;
import com.netfinworks.vfsso.client.cas.domain.Auth;
import com.netfinworks.vfsso.client.common.CasCookie;
import com.netfinworks.vfsso.client.common.CasEvent;
import com.netfinworks.vfsso.client.common.CasEvent.CasEventType;
import com.netfinworks.vfsso.client.common.HttpClientFactory;
import com.netfinworks.vfsso.client.common.ResourceStyle;
import com.netfinworks.vfsso.client.common.VfSsoClientConfig;
import com.netfinworks.vfsso.client.common.VfSsoClientConfig.ResourceConfig;
import com.netfinworks.vfsso.client.common.VfSsoClientConfigParser;
import com.netfinworks.vfsso.client.core.IVfSsoCasAuditListener;
import com.netfinworks.vfsso.client.core.IVfSsoCasListener;

/**
 * 重写VfSsoCasFilter  
 * 目的：支持点车成金web（cookie跨域问题）
 * @author admin
 *
 */
public class CxVfSsoCasFilter implements Filter{
	
	 private static Logger logger = LoggerFactory.getLogger(CxVfSsoCasFilter.class);
	 private HttpClient    httpClient;

	    @Override
	    public void init(FilterConfig filterConfig) throws ServletException {
	        VfSsoClientConfig.init(VfSsoClientConfigParser.parse(filterConfig));

	        // init httpClient
	        initHttpClient(VfSsoClientConfig.getDefaultConfig());
	    }

	    private void initHttpClient(VfSsoClientConfig conf) {
	        httpClient = HttpClientFactory.factory(conf);
	    }

	    @Override
	    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	                                                                                             throws IOException,
	                                                                                             ServletException {
	        try {
	            VfSsoUser.clear();
	            logger.debug("Clear vfssouser in.");
	            doCasFilter((HttpServletRequest) request, response, chain);
	        } finally {
	            VfSsoUser.clear();
	            logger.debug("Clear vfssouser out.");
	        }
	    }

	    /**
	     * @param request
	     * @param response
	     * @param chain
	     * @throws IOException
	     * @throws ServletException
	     */
	    public void doCasFilter(HttpServletRequest hsr, ServletResponse response, FilterChain chain)
	                                                                                                throws IOException,
	                                                                                                ServletException {

	        preIntercept(hsr);
	        String url = hsr.getRequestURI().substring(hsr.getContextPath().length());
	        if (url.startsWith("/") && url.length() > 1) {
	            url = url.substring(1);
	        }
	        String token = getToken(hsr);
	        logger.info("Start SSO filter for url:{}, cookie token:{},hsrURI:{},hsrURL", url, token, hsr.getRequestURI(),hsr.getRequestURL());

	        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
	        Cookie cookie = CasCookie.makeCasCookieFromQueryString(hsr.getQueryString());
	        if (cookie != null) {
	            httpServletResponse.addCookie(cookie);
	            // 优先取的queryString中的token
	            token = cookie.getValue();
	            logger.info("VfSso Cas use new token for querystring:{}", token);
	        }

	        VfSsoUser.setCurrentToken(token);
	        logger.debug("VfSso Cas use token:{}", token);

	        // 准备审计信息对象
	        Audit audit = new Audit();
	        audit.setRequestTime(new Date());
	        audit.setSystemCode(VfSsoClientConfig.getDefaultConfig().getAppId());
	        audit.setUrl(url);
	        audit.setToken(token);
	        audit.setAuthResult(CasResult.AUTH_DIRECT_PASS);
	        // 业务处理过程中可以使用AuditContext.addInfo方法设置特定的审计信息
	        AuditContext.setAudit(audit);

	        if (mustBeAunthenticated(url)) {
	            if (logger.isDebugEnabled()) {
	                logger.debug("VF SSO Filter start to authenticate the url : {}, with token : {}",
	                    new Object[] { url, token });
	            }

	            CasResult authencateResult = authencate(token, url, hsr, audit);

	            if (logger.isInfoEnabled()) {
	                logger.info("VfSso Filter has auth the url :{}({}), with token : {}, result is {}",
	                    new Object[] { url, hsr.getServerName(), token, authencateResult });
	            }

	            if (authencateResult.equals(CasResult.AUTH_NOT_LOGIN)) {
	                audit.setResponseTime(new Date());

	                CasEvent event = new CasEvent(CasEventType.AUTH_NOT_LOGIN, hsr,
	                    httpServletResponse, VfSsoClientConfig.getDefaultConfig());
	                fireEvent(event);

	                // 记录审计日志
	                onAudit(audit);
	                return;
	            }

	            try {
	                // 如果鉴权OK
	                if (authencateResult.equals(CasResult.AUTH_OK)) {
	                    if (cookie != null) {
	                        /*String redirectUrl = DefaultListener
	                            .removeCookieParam(hsr.getQueryString());
	                        redirectUrl = redirectUrl != null && redirectUrl.length() > 0 ? hsr
	                            .getRequestURL().append("?").append(redirectUrl).toString() : hsr
	                            .getRequestURI();
	                        logger.info("VfSso Cas pass the url but need remove token url : {}",
	                            redirectUrl);
	                        httpServletResponse.sendRedirect(redirectUrl);*/
	                        chain.doFilter(hsr, httpServletResponse);
	                    } else {
	                        chain.doFilter(hsr, httpServletResponse);
	                    }
	                } else if (CasResult.EXCEPTION.equals(authencateResult)
	                           || CasResult.AUTH_SESSION_ERR.equals(authencateResult)) {
	                    // 鉴权异常
	                    logger.info("VfSso Cas auth the url exception: {}", url);

	                    // 触发鉴权异常事件
	                    CasEvent event = new CasEvent(CasEventType.AUTH_EXCEPTION, hsr,
	                        httpServletResponse, VfSsoClientConfig.getDefaultConfig());
	                    fireEvent(event);

	                } else if (CasResult.AUTH_RESOURCE_FORBIDDEN.equals(authencateResult)) {
	                    // 禁止访问
	                    logger.info("VfSso Cas denied the url : {}", url);

	                    // 触发资源禁止访问异常
	                    CasEvent event = new CasEvent(CasEventType.AUTH_FORBIDDEN, hsr,
	                        httpServletResponse, VfSsoClientConfig.getDefaultConfig());
	                    fireEvent(event);
	                } else if (CasResult.AUTH_PENDING.equals(authencateResult)) {
	                    // 禁止访问
	                    logger.info("VfSso Cas pending the url : {}", url);

	                    // 触发资源禁止访问异常
	                    CasEvent event = new CasEvent(CasEventType.AUTH_NOT_LOGIN, hsr,
	                        httpServletResponse, VfSsoClientConfig.getDefaultConfig());
	                    fireEvent(event);
	                } else if (CasResult.AUTH_KICKED.equals(authencateResult)) {
	                    // 禁止访问
	                    logger.info("VfSso Cas kicked the url : {}", url);

	                    // 触发资源禁止访问异常
	                    CasEvent event = new CasEvent(CasEventType.AUTH_NOT_LOGIN, hsr,
	                        httpServletResponse, VfSsoClientConfig.getDefaultConfig());
	                    fireEvent(event);
	                } else {
	                    throw new RuntimeException("unknown AuthResult");
	                }

	            } catch (IOException e) {
	                // 增加一条异常审计信息
	                audit.addInfo(e.getMessage());
	                throw e;
	            } catch (ServletException e) {
	                // 增加一条异常审计信息
	                audit.addInfo(e.getMessage());
	                throw e;
	            } finally {
	                audit.setResponseTime(new Date());
	                // 记录审计信息
	                try {
	                    onAudit(audit);
	                } catch (Exception e) {
	                    logger.warn("VfSso Cas audit failed, token:" + token, e);
	                }
	            }

	            return;
	        }
	        // 放行
	        if (logger.isInfoEnabled()) {
	            logger.info("VfSso Cas SSO Filter directly passed the url : {}", url);
	        }

	        chain.doFilter(hsr, response);
	        // 记录直接放行的审计信息
	        if (mustBeAudit(url)) {
	            try {
	                onAudit(audit);
	            } catch (Exception e) {
	                logger.warn("VfSso Cas direct audit failed, token:" + token, e);
	            }
	        }
	    }

	    /**
	     * @param hsr
	     */
	    private void preIntercept(HttpServletRequest hsr) {
	        //TODO 过滤前处理，如果有多系统判定再实现
	        // VfSsoClientConfig.getDefaultConfig().getCasPreInterceptors();
	    }

	    @Override
	    public void destroy() {

	    }

	    /**
	     * 判断url是否需要鉴权
	     * 
	     * @param url
	     * @return
	     */
	    private boolean mustBeAunthenticated(String url) {
	        for (Pattern pattern : VfSsoClientConfig.getDefaultConfig().getNotAuthencatedPattern()) {
	            Matcher matcher = pattern.matcher(url);
	            if (matcher.matches()) {
	                return false;
	            }
	        }
	        return true;
	    }

	    /**
	     * 判断URL是否需要审计
	     */
	    private boolean mustBeAudit(String url) {
	        for (Pattern pattern : VfSsoClientConfig.getDefaultConfig().getNotAuditPattern()) {
	            Matcher matcher = pattern.matcher(url);
	            if (matcher.matches()) {
	                return false;
	            }
	        }
	        return true;
	    }

	    /**
	     * 获取token
	     * 
	     * @param hsr
	     * @return
	     */
	    private String getToken(HttpServletRequest hsr) {
	        Cookie cookie = CasCookie.getCookie(hsr);
	        logger.debug("Get cookie:{}", cookie);
	        return cookie == null ? null : cookie.getValue();
	    }

	    /**
	     * 鉴权
	     * 
	     * @param token
	     * @return
	     */
	    private CasResult authencate(String token, String url, HttpServletRequest hsr, Audit audit) {
	        Auth auth = new Auth();
	        auth.setToken(token);
	        auth.setUrl(url);

	        auth.setStyle(VfSsoClientConfig.getDefaultConfig().getDefaultStyle());
	        auth.setMethodName(VfSsoClientConfig.getDefaultConfig().getDefaultMethodParamName());

	        // spec
	        ResourceConfig rc = VfSsoClientConfig.getDefaultConfig().getResourceConfigByUrl(url);
	        if (rc != null) {
	            String style = rc.getStyle();
	            if (style != null) {
	                auth.setStyle(style);
	            }
	            String methodParamName = rc.getMethodParamName();
	            if (methodParamName != null) {
	                auth.setMethodName(methodParamName);
	            }
	        }
	        // 非rest即rpc
	        String method = ResourceStyle.REST.toString().equals(auth.getStyle()) ? hsr.getMethod()
	            : hsr.getParameter(auth.getMethodName());

	        auth.setMethod(method == null ? rc == null ? null : rc.getDefaultMethod() : method);

	        audit.setMethod(auth.getMethod());

	        CasResult result = AuthService.doAuth(httpClient, VfSsoClientConfig.getDefaultConfig(),
	            auth);
	        audit.setAuthResult(result);
	        return result;
	    }

	    private void onAudit(Audit audit) {
	        if (audit == null) {
	            return;
	        }
	        if (logger.isDebugEnabled()) {
	            logger.debug("记录审计信息：url = {}, requestTime = {}, reponseTime = {}", new Object[] {
	                    audit.getUrl(), audit.getRequestTime(), audit.getResponseTime() });
	        }
	        if (audit.getResponseTime() == null) {
	            audit.setResponseTime(new Date());
	        }
	        List<IVfSsoCasAuditListener> listeners = VfSsoClientConfig.getDefaultConfig()
	            .getCasAuditListeners();
	        if (listeners != null) {
	            for (IVfSsoCasAuditListener listener : listeners) {
	                listener.onEvent(audit);
	            }
	        }
	    }

	    private void fireEvent(CasEvent event) {
	        List<IVfSsoCasListener> listeners = VfSsoClientConfig.getDefaultConfig()
	            .getCasClientListeners();
	        if (listeners != null) {
	            for (IVfSsoCasListener listener : listeners) {
	                listener.onEvent(event);
	            }
	        }
	    }
}
