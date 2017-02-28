package com.yongda.site.wallet.app.filter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.netfinworks.site.ext.integration.member.MemberAppService;
import com.yongda.site.wallet.app.util.MD5Util;
import com.yongda.site.wallet.app.util.MapUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.client.authapi.domain.User;
import com.netfinworks.voucher.common.utils.JsonUtils;
import com.yongda.site.wallet.app.WebDynamicResource;
import com.yongda.site.wallet.app.util.ConvertObject;
import com.yongda.site.wallet.app.util.ResponseUtil;

/**
 *
 * <p>
 * session过滤器，统一获取用户信息
 * </p>
 *
 * @author netfinworks
 * @version $Id: SessionFilter.java, v 0.1 2013-11-19 上午9:17:11 qinde Exp $
 */
public class SessionFilter implements Filter, InitializingBean {
    private static final Logger       logger      = LoggerFactory.getLogger(SessionFilter.class);

    @Resource(name = "memberService")
    private MemberService             commMemberService;

	@Resource(name = "memberAppService")
	private MemberAppService memberAppService;

    @Resource(name = "accountService")
    private AccountService            accountService;

    @Resource(name = "webResource")
    private WebDynamicResource        webResource;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name = "memberService")
    private MemberService             memberService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

    @Value("${prerson_pass_url}")
    private String                    prersonPassUrl;

    private String[]                  prersonPassUrls;

    protected static PathMatcher      pathMatcher = new AntPathMatcher();
    
    protected static List<Pattern>      patterns = new ArrayList<Pattern>();

	@Value("${set_passwd_url}")
	private String setPasswdUrl;// 需要设置支付密码的url
	@Value("${bind_phone_url}")
	private String bindPhoneUrl;// 需要绑定手机的url
	@Value("${certifi_v0_url}")
	private String certifiV0Url;// 需要满足实名校验的url
	@Value("${certifi_v1_url}")
	private String certifiV1Url;// 需要满足实名认证V1的url
	@Value("${certifi_v2_url}")
	private String certifiV2Url;// 需要满足实名认证V2的url

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
        prersonPassUrls = prersonPassUrl.split(",");
        for(String url : prersonPassUrls){
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
        HttpServletResponse httpResponse = (HttpServletResponse) response;
		//app加签验证
		if(!verifySign(httpRequest,response)  ){
			return;
		}

        String url = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (url.startsWith("/") && (url.length() > 1)) {
            url = url.substring(1);
        }

        if (isInclude(url)) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        } else {
            HttpSession session = httpRequest.getSession();
            PersonMember user = null;
            if (session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER) == null) {//本地session没有值，从单点登录获取用户信息
                try {
                	if(StringUtils.isBlank(VfSsoUser.getCurrentToken())){//尝试从头信息获取token,可用于非web登陆校验
                		String token = httpRequest.getHeader("com.vfsso.cas.token");
						String cookieToken = null;
						Cookie[] cookies = httpRequest.getCookies();
						for(Cookie c :cookies ){
							if (c.getName().equals("com.vfsso.cas.token")){
								cookieToken = c.getValue();
							}
						}
                		if(StringUtils.isNotBlank(token)){
                			VfSsoUser.setCurrentToken(token);
                		}

                		if (StringUtils.isNotBlank(cookieToken)) {
							VfSsoUser.setCurrentToken(token);
						}
                	}
                    User vfSsoUser = VfSsoUser.get();//从单点登录获取用户信息，报异常则表示用户未登录，不报异常则表示用户已登录
                    //判断单点登录里已登录的用户是否是特约商户，如果是特约商户则跳转到错误页面，如果不是特约商户则通过已登录的用户信息更新本地session
					if (CommonConstant.USERTYPE_MERCHANT.equals(vfSsoUser.getUserType())) {
                        httpResponse.getWriter().write("memType_error");
                        return;
                    }
					logger.info("vfSsoUser Id"+vfSsoUser.getId());
                    // 更新session
                    user = this.refreshSession(httpRequest, session, vfSsoUser.getId());
					// 根据条件判断URL转向--仅针对个人会员
					if(!urlForword(request, response, url, user))
						chain.doFilter(request, response);
                    return;
                } catch (Exception e) {//报异常则表示用户未登录
                    logger.error("单点登录获取用户登录信息失败");
                    ResponseUtil.buildExpResponse(CommonDefinedException.NOT_LOGIN,httpResponse);
                    return;
                }
            } else {//本地session有值，直接刷新本地session
                String memberId = session.getAttribute(
                    CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER_ID).toString();
                logger.info("Session ID   " + session.getId());
                // 更新session
                user = this.refreshSession(httpRequest, session, memberId);
				//user = this.refreshLocalSession(httpRequest, session, memberId);
				// 根据条件判断URL转向--仅针对个人会员
				if(urlForword(request, response, url, user)) return;
            }

        }
        chain.doFilter(httpRequest, httpResponse);
    }

	/**
	 * URL转向
	 * 
	 * @param
	 * @param url
	 * @param user
	 * @throws IOException
	 * @throws ServletException
	 */
	private boolean urlForword(ServletRequest request, ServletResponse response, String url, PersonMember user)
			throws ServletException, IOException {
		if (user != null) {
			// 判断是否需要绑定手机
			String path = this.needBindPhone(url, user);
			if (StringUtils.isNotBlank(path)) {
				ResponseUtil.buildExpResponse(CommonDefinedException.NOT_BIND_PHONE,response);
				return true;
			}
			// 判断是否需要设置支付密码
			path = this.needSetPaypasswd(url, user);
			if (StringUtils.isNotBlank(path)) {
				ResponseUtil.buildExpResponse(CommonDefinedException.NOT_SET_PAYPASSWORD,response);
				return true;
			}
			if (user.getMemberId().startsWith("1")) {
				// 判断是否需要实名认证
				path = this.needCertification(url, user);
				if (StringUtils.isNotBlank(path)) {
					if(StringUtils.equalsIgnoreCase("v0", path))
						ResponseUtil.buildExpResponse(CommonDefinedException.NOT_MEMBER_V0,response);
					else if(StringUtils.equalsIgnoreCase("v1", path))
						ResponseUtil.buildExpResponse(CommonDefinedException.NOT_MEMBER_V1,response);
					else if(StringUtils.equalsIgnoreCase("v2", path))
						ResponseUtil.buildExpResponse(CommonDefinedException.NOT_MEMBER_V2,response);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * md5验签
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
     */
	private Boolean verifySign(HttpServletRequest request, ServletResponse response)throws ServletException, IOException{
        String sign = request.getParameter("sign");
		if (StringUtils.isBlank(sign)){
			return true;
		}
	    Map<String,Object> requestMap = request.getParameterMap();
		Map<String,Object> map = new HashMap<String,Object>();
		map.putAll(requestMap);
		map.remove("sign");
		map.remove("sign_type");
		String signStr = MapUtil.createLinkString2(map,false);
		if (StringUtils.isBlank(sign)) {
			ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_SIGN, response);
			return false;
		}
		try {
           if (StringUtils.isNotBlank(signStr)){
               //MD5验签
               if(!MD5Util.verify(signStr,sign,webResource.getMagSignkey(), "UTF-8")){
                   logger.error("app请求验签未通过:{},签名结果：{}",signStr,sign);
                   ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_SIGN,response);
                   return false;
               }
           }else {
               if(!MD5Util.verify("",sign,webResource.getMagSignkey(),"utf-8")){
				   logger.error("app请求验签未通过:{},签名结果：{}",signStr,sign);
                   ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_SIGN,response);
                   return false;
               }
           }

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

    /*
     * 判断用户是否需要设置支付密码
     */
	private String needSetPaypasswd(String url, PersonMember user) {
		if (user.getPaypasswdstatus() != null) {
			if (user.getPaypasswdstatus().getCode() != MemberPaypasswdStatus.NOT_SET_PAYPASSWD.getCode()) {
				return StringUtils.EMPTY;
			}
		}
		String[] urlArray = StringUtils.split(setPasswdUrl, ",");

		if (!checkUrl(url, urlArray)) {
			return StringUtils.EMPTY;
		}
		return "true";
    }

	/*
	 * 判断用户是否需要绑定手机
	 */
	private String needBindPhone(String url, PersonMember user) {
		if (user.isBindPhone()) {
			return StringUtils.EMPTY;
		}

		String[] urlArray = StringUtils.split(bindPhoneUrl, ",");

		if (!checkUrl(url, urlArray)) {
			return StringUtils.EMPTY;
		}

		return "true";
	}

	/*
	 * 判断用户是否需要实名认证
	 */
	private String needCertification(String url, PersonMember user) {
		String[] urlArray;

		if (user.getCertifyLevel() == CertifyLevel.CERTIFY_V2) {
			return StringUtils.EMPTY;
		} else if (user.getCertifyLevel() == CertifyLevel.CERTIFY_V1) {
			urlArray = StringUtils.split(certifiV2Url, ",");
			if (!checkUrl(url, urlArray)) {
				return StringUtils.EMPTY;
			}
			return "V2";
		} else if (user.getCertifyLevel() == CertifyLevel.CERTIFY_V0) {
			urlArray = StringUtils.split(certifiV1Url, ",");
			if (checkUrl(url, urlArray)) {
				return "V1";
			}
			urlArray = StringUtils.split(certifiV2Url, ",");
			if (!checkUrl(url, urlArray)) {
				return StringUtils.EMPTY;
			}
			return "V2";
		} else {
			urlArray = StringUtils.split(certifiV0Url, ",");
			if (checkUrl(url, urlArray)) {
				return "V0";
			}
			urlArray = StringUtils.split(certifiV1Url, ",");
			if (checkUrl(url, urlArray)) {
				return "V1";
			}
			urlArray = StringUtils.split(certifiV2Url, ",");
			if (!checkUrl(url, urlArray)) {
				return StringUtils.EMPTY;
			}
			return "V2";
		}
	}

	/**
	 * 判断URL是否符合条件
	 * 
	 * @param myUrl
	 * @param urlArray
	 * @return
	 */
	private boolean checkUrl(String myUrl, String[] urlArray) {
		for (String url : urlArray) {
			Pattern p = Pattern.compile(url);
			Matcher matcher = p.matcher(myUrl);
			if (matcher.matches()) {
				return true;
			}
		}
		return false;
	}

    /*
     * 刷新session里的值
     */
    private PersonMember refreshSession(HttpServletRequest request, HttpSession session,
                                        String memberId) {
        try {
            // 更新session
            OperationEnvironment env = new OperationEnvironment();
            PersonMember personMember = new PersonMember();
            if (memberId.startsWith("1")) {
            	logger.info("SessionFilter refreshSession:{}",memberId);
                BaseMember baseMember = commMemberService.queryMemberById(memberId, env);
                BeanUtils.copyProperties(baseMember, personMember);
              //personMember = commMemberService.queryMemberIntegratedInfo(personMember, env);
				personMember = memberAppService.queryAPPMemberIntegratedInfo(personMember,env);
				personMember.setMemberName(personMember.getRealName().getPlaintext());
            } else if (memberId.startsWith("2")) {
                EnterpriseMember enterpriseMember = new EnterpriseMember();
                enterpriseMember.setMemberId(memberId);
                enterpriseMember = commMemberService.queryCompanyMember(enterpriseMember, env);

				CompanyMemberInfo compInfo = memberService.queryCompanyInfo(enterpriseMember, env);
				enterpriseMember.setMemberName(compInfo.getCompanyName());
                ConvertObject.convert(enterpriseMember, personMember);
            }

			if (memberId.startsWith("2")) {
				AuthInfoRequest info = new AuthInfoRequest();
				info.setMemberId(personMember.getMemberId());
				info.setAuthType(AuthType.REAL_NAME);
				info.setOperator(personMember.getOperatorId());
				AuthResultStatus state = defaultCertService.queryAuthType(info);
				personMember.setNameVerifyStatus(state);
			} else {
				personMember.setNameVerifyStatus(AuthResultStatus.NOT_FOUND);
			}

			// 查询是否绑定手机
			AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
			authVerifyInfo.setMemberId(personMember.getMemberId());
			List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, new OperationEnvironment());
			for (int i = 0; i < infos.size(); i++) {
				authVerifyInfo = infos.get(i);
				if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
					personMember.setBindPhone(true);
				}
			}

			// 查询实名认证等级--仅针对个人会员
			if (memberId.startsWith("1")) {
				String level = defaultMemberService.getMemberVerifyLevel(personMember.getMemberId(), env);
				personMember.setCertifyLevel(CertifyLevel.getByCode(level));
			}

            /* 查询账户信息 */
            /*personMember.setAccount(accountService.queryAccountById(
                    personMember.getDefaultAccountId(), env));*/
            // 查询绑定银行卡
            BankAccRequest reqAcc = new BankAccRequest();
            reqAcc.setMemberId(personMember.getMemberId());
            reqAcc.setClientIp(request.getRemoteAddr());
            List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(reqAcc);
            personMember.setBankCardCount(list.size());
            session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER_ID,
                personMember.getMemberId());
            session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_USER_LOGINNAME,
                personMember.getLoginName());
            session.setAttribute(CommonConstant.KJT_PERSON_USER_NAME, personMember.getMemberName());
            session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER,
                JsonUtils.toJson(personMember));
            session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER);
            return personMember;
        } catch (Exception e) {
            logger.error("Filter更新Session失败！");
            logger.error("", e);
        }
        return null;
    }
    
	/*
	 * 刷新本地session里的值
	 */
	private PersonMember refreshLocalSession(HttpServletRequest request, HttpSession session, String memberId) {
		try {
			// 更新session
			PersonMember personMember = new PersonMember();
			try {
				personMember = JsonUtils.parse(
						(String) session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER),
						PersonMember.class);
			} catch (Exception e) {
				personMember = this.refreshSession(request, session, memberId);
				return personMember;
			}

			OperationEnvironment env = new OperationEnvironment();

			if (memberId.startsWith("2")) {
				AuthInfoRequest info = new AuthInfoRequest();
				info.setMemberId(personMember.getMemberId());
				info.setAuthType(AuthType.REAL_NAME);
				info.setOperator(personMember.getOperatorId());
				AuthResultStatus state = defaultCertService.queryAuthType(info);
				personMember.setNameVerifyStatus(state);
			} else {
				personMember.setNameVerifyStatus(AuthResultStatus.NOT_FOUND);
			}

			// 查询是否绑定手机
			AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
			authVerifyInfo.setMemberId(personMember.getMemberId());
			List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, new OperationEnvironment());
			for (int i = 0; i < infos.size(); i++) {
				authVerifyInfo = infos.get(i);
				if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
					personMember.setBindPhone(true);
				}
			}

			// 查询实名认证等级--仅针对个人会员
			if (memberId.startsWith("1")) {
				String level = defaultMemberService.getMemberVerifyLevel(personMember.getMemberId(), env);
				personMember.setCertifyLevel(CertifyLevel.getByCode(level));
			}

			// 查询绑定银行卡
			BankAccRequest reqAcc = new BankAccRequest();
			reqAcc.setMemberId(personMember.getMemberId());
			reqAcc.setClientIp(request.getRemoteAddr());
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(reqAcc);
			personMember.setBankCardCount(list.size());

			session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER, JsonUtils.toJson(personMember));

			logger.info(MessageFormat.format("Session用户值： member_id={0},loginName={1}", personMember.getMemberId(),
					personMember.getLoginName()));
			return personMember;
		} catch (Exception e) {
			logger.error("Filter更新Session失败！");
			logger.error("", e);
		}
		return null;
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

}