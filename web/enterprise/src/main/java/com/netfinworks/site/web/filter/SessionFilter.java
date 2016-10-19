package com.netfinworks.site.web.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberSetStatus;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.client.authapi.domain.User;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 *
 * <p>
 * 注释
 * </p>
 *
 * @author Guan Xiaoxu
 * @version $Id: SessionFilter.java, v 0.1 2013-12-26 下午3:41:34 Guanxiaoxu Exp $
 */
public class SessionFilter implements Filter, InitializingBean {
    private static final Logger       logger      = LoggerFactory.getLogger(SessionFilter.class);

    @Resource(name = "webResource")
    private WebDynamicResource        webResource;

    @Resource(name = "accountService")
    private AccountService            accountService;

    @Resource
    private WebDynamicResource        webDynamicResource;

    @Resource
    private OperatorAuthOuterService  operatorAuthOuterService;

    @Resource(name = "memberService")
    private MemberService             commMemberService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name = "memberService")
    private MemberService             memberService;
    
    @Resource(name = "cachedSessionService")
    private IVfSsoSession<SsoUser>    userSessionService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

    @Value("${enterprise_pass_url}")
    private String                    enterprisePassUrl;

    private String[]                  enterprisePassUrls;

    protected static PathMatcher      pathMatcher = new AntPathMatcher();
    
    protected static List<Pattern>      patterns = new ArrayList<Pattern>();

    /**
     * 初始化.
     *
     * @param filterConfig
     *            servlet filter config
     * @throws ServletException
     *             sevlet exception when error
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        enterprisePassUrls = enterprisePassUrl.split(",");
        for(String url : enterprisePassUrls){
            Pattern p = Pattern.compile(url);
            patterns.add(p);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 获取cas-sso返回信息，查询会员信息
     *
     * @param request
     *            http request
     * @param response
     *            http response
     * @param chain
     *            filer chain
     * @throws IOException
     *             io exception
     * @throws ServletException
     *             servlet exception
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                                                                                             throws IOException,
                                                                                             ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String url = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if(logger.isInfoEnabled()) {
        	logger.info("[ent-site]url={}", url);
        }
        if (url.startsWith("/") && (url.length() > 1)) {
            url = url.substring(1);
        }
        
        // 设置当前链接导航
        setCurNavButton(httpRequest);

        if (httpRequest.getRequestURI().contains("/login/page.htm")) {
            logger.info("跳转到首页");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/login/page.htm");
            dispatcher.forward(request, response);
            return;
        }
        if (isInclude(url)) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        } else {
            EnterpriseMember user = null;

            if (session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER) == null) {//本地session没有值，从单点登录获取用户信息
            	if(logger.isInfoEnabled()) {
                	logger.info("[ent-site]session is null,go to get from sso!");
                }
            	try {
                    User vfSsoUser = VfSsoUser.get();//从单点登录获取用户信息，报异常则表示用户未登录，不报异常则表示用户已登录
                    if(logger.isInfoEnabled()) {
                    	logger.info("[ent-site]vfSsoUser={}",vfSsoUser);
                    }
                    //判断单点登录里已登录的用户是否是特约商户，如果不是特约商户则跳转到错误页面，如果是特约商户则通过已登录的用户信息更新本地session
                    if(vfSsoUser == null) {
						throw new BizException(ErrorCode.SSO_NO_LOGIN);
					}
					if (CommonConstant.USERTYPE_PERSON.equals(vfSsoUser.getUserType())
							|| CommonConstant.USERTYPE_ENTERPRISE.equals(vfSsoUser.getUserType())) {
                        httpResponse.sendRedirect(webResource.getPersonalWalletAddr()
                                                  + "/loginTypeError.htm");
                        //chain.doFilter(request, response);
                        return;
                    }
                    // 更新session
                    String token = VfSsoUser.getCurrentToken();
                    SsoUser ssoUser = userSessionService.get(token);
                    if(logger.isInfoEnabled()) {
                    	logger.info("[ent-site]token={}, ssoUser={}", token, ssoUser);
                    }
                    user = this.refreshSession(httpRequest, session, vfSsoUser.getId(),
                        vfSsoUser.getLoginName(), ssoUser.getOpId(), vfSsoUser.getOpName());
                    if (user != null) {
                        String path = this.needSetPaypasswd(httpRequest, user);
                        if (StringUtils.isNotBlank(path)) {
                            RequestDispatcher dispatcher = request.getRequestDispatcher(path);
                            dispatcher.forward(request, response);
                            return;
                        }
                    }

                    chain.doFilter(request, response);
                    return;
                } catch (Exception e) {//报异常则表示用户未登录
                    logger.error("单点登录获取用户登录信息失败，跳转到登录页面");
                    this.assemblyUrl(request, response, chain, httpRequest, httpResponse);
                    return;
                }
            } else {//本地session有值，直接刷新本地session
            	if(logger.isInfoEnabled()) {
                	logger.info("[ent-site] user session has values!");
                }
                String userString = session
                    .getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER) != null ? session
                    .getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER)
                    .toString() : null;
                if (StringUtils.isNotBlank(userString)) {
                    user = JsonUtils.parse(userString, EnterpriseMember.class);
                }
                if(logger.isInfoEnabled()) {
                	logger.info("[ent-site] user={}", user);
                }
                if (StringUtils.isBlank(user.getPlateFormId())) {
                    logger.error("sso返回平台user ID为空,无法进入企业钱包");
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/error.htm");
                    dispatcher.forward(request, response);
                    return;
                }

                user = this
                    .refreshSession(httpRequest, session, user.getMemberId(), user.getLoginName(),
                        user.getCurrentOperatorId(), user.getOperator_login_name());
                String path = this.needSetPaypasswd(httpRequest, user);
                if (StringUtils.isNotBlank(path)) {
                    RequestDispatcher dispatcher = request.getRequestDispatcher(path);
                    dispatcher.forward(request, response);
                    return;
                }
            }

        }

        chain.doFilter(httpRequest, httpResponse);
    }

    /*
     * 判断用户是否需要设置支付密码
     */
    private String needSetPaypasswd(HttpServletRequest httpRequest, EnterpriseMember user) {
        String path = null;
        //if (!httpRequest.getRequestURI().contains("/logout.htm")) {
            // 非激活状态下
            if (!httpRequest.getRequestURI().contains("/active/")) {
                // 验证会员是否设置了支付密码
                if (user.getPaypasswdstatus() != null) {
                    if (user.getPaypasswdstatus().getCode() == MemberPaypasswdStatus.NOT_SET_PAYPASSWD
                        .getCode()) {
                        if (logger.isInfoEnabled()) {
                            logger.info("用户: [" + user.getMemberId() + " ] 未设置支付密码, 进入设置支付密码页面!");
                        }
                        path = "/my/active/avtiveMember.htm?merState="
                               + MemberSetStatus.SET_PASSWD.getCode();
                    }
                }
            }
        //}
        return path;
    }

    /*
     * 刷新session里的值
     */
    private EnterpriseMember refreshSession(HttpServletRequest request, HttpSession session,
                                            String memberId, String loginName, String opId,
                                            String opName) {
        EnterpriseMember enterpriseMember = new EnterpriseMember();
        // 更新session
        try {
            OperationEnvironment env = new OperationEnvironment();
            enterpriseMember.setMemberId(memberId);
            enterpriseMember.setLoginName(loginName);
            enterpriseMember.setOperator_login_name(opName);
            enterpriseMember.setCurrentOperatorId(opId);
            enterpriseMember = commMemberService.queryCompanyMember(enterpriseMember, env);
            session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER_ID,
                enterpriseMember.getMemberId());
            session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_USER_LOGINNAME,
                enterpriseMember.getLoginName());

			CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(enterpriseMember, env);

			enterpriseMember.setLicenseNo(compInfo.getLicenseNo());
			enterpriseMember.setEnterpriseName(compInfo.getCompanyName());
			enterpriseMember.setCompanyType(compInfo.getCompanyType());
			enterpriseMember.setSummary(compInfo.getSummary());

			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(enterpriseMember.getMemberId());
			info.setAuthType(AuthType.REAL_NAME);
			info.setOperator(enterpriseMember.getOperatorId());
			info.setMessage("merchant");
			AuthResultStatus state = defaultCertService.queryAuthType(info);
			enterpriseMember.setNameVerifyStatus(state);

            /*查询账户信息*/
            /*enterpriseMember.setAccount(accountService.queryAccountById(enterpriseMember.getDefaultAccountId(), env));*/
            // 查询绑定银行卡
            BankAccRequest reqAcc = new BankAccRequest();
            reqAcc.setMemberId(enterpriseMember.getMemberId());
            reqAcc.setClientIp(request.getRemoteAddr());
            List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(reqAcc);
            enterpriseMember.setBankCardCount(list.size());

            session.setAttribute(CommonConstant.KJT_ENTERPRISE_USER_NAME,
                enterpriseMember.getMemberName());
            session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER,
                JsonUtils.toJson(enterpriseMember));
            session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
            logger.info("Session 更新成功");
        } catch (Exception e) {
            logger.info("Filter Session 更新失败");
            logger.error("", e);
        }
        try {
            //获取菜单权限开始
            Map<String, FunctionVO> authRights = new HashMap<String, FunctionVO>();
            AuthVO authVO = new AuthVO(enterpriseMember.getMemberId(),
                enterpriseMember.getOperatorId(), "", webDynamicResource.getSourceId(), "");
            List<FunctionVO> assignFunctions = operatorAuthOuterService
                .getFunctionListFromOperator(authVO);

            if (assignFunctions != null) {
                //转化成TreeNode list
                for (FunctionVO functionVO : assignFunctions) {
                    authRights.put(functionVO.getFunctionId(), functionVO);
                }
            }
            //获取菜单权限结束
        } catch (Exception e) {
            logger.error("查询菜单[" + enterpriseMember.getPlateName() + "]信息失败," + e.getMessage());
        }
        return enterpriseMember;
    }
    
    private void assemblyUrl(ServletRequest request, ServletResponse response,
                             FilterChain chain, HttpServletRequest httpRequest,
                             HttpServletResponse httpResponse)
                             throws  IOException, ServletException {
        String backUrl = request.getParameter("backUrl");
        String token = request.getParameter("token");
        String memberType = request.getParameter("memberType");
        if (StringUtils.isNotBlank(backUrl)
            && StringUtils.isNotBlank(token)
            && StringUtils.isNotBlank(memberType)) {
            String returnUrl = MessageFormat.format("{0}?token={1}&memberType={2}",
                backUrl, token, memberType);
            returnUrl = URLEncoder.encode(returnUrl, request.getCharacterEncoding());

            String wallet_url = MessageFormat.format(
                "{0}?cashier_memberType={1}&returnUrl={2}",
                webResource.getWalletAddress() + "/index.htm", memberType,
                returnUrl);
            httpResponse.sendRedirect(wallet_url);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * destroy.
     */
    @Override
    public void destroy() {

    }
    
    /**
     * 判断url是否需要鉴权
     * 
     * @param url
     * @return
     */
    private boolean isInclude(String url) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 设置当前导航栏
     * @param request
     */
    private void setCurNavButton(HttpServletRequest request) {
    	String codeName = "link";
    	String curLinkCode = request.getParameter(codeName);
    	HttpSession session = request.getSession();
    	String linkCode = (String) session.getAttribute(codeName);
    	if (StringUtils.isNotEmpty(curLinkCode) && !curLinkCode.equals(linkCode)) {
    		session.setAttribute(codeName, curLinkCode);
    	}
    }

//    protected boolean isInclude(String uri, String[] includes) {
//        boolean isInclude = false;
//        if (includes != null) {
//            for (String resource : includes) {
//                if (pathMatcher.match(resource, uri)) {
//                    isInclude = true;
//                    break;
//                }
//            }
//        }
//        return isInclude;
//    }
}