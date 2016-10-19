package com.netfinworks.site.web.action;

import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.cert.service.model.enums.UdcreditType;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.response.MerchantListResponse;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.sars.SarsResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.OperatorLockStatusEnum;
import com.netfinworks.site.domain.enums.OperatorStatusEnum;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.ext.integration.sars.DefaultSarsService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.LoginForm;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.ues.util.UesUtils;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.vfsso.session.enums.SessionStatusKind;
import com.netfinworks.vfsso.session.exceptions.SessionException;
import com.netfinworks.vfsso.session.spec.IMutexSessionControl;

/**
 * <p>登录</p>
 * @author eric
 * @version $Id: LoginAction.java, v 0.1 2013-7-18 下午6:07:43  Exp $
 */
@Controller
public class LoginAction extends BaseAction implements InitializingBean {

    protected Logger                  log = LoggerFactory.getLogger(getClass());

    @Resource(name = "memberService")
    private MemberService             memberService;

    @Resource(name = "operatorService")
    private OperatorService           operatorService;

    @Resource(name = "loginPasswdService")
    private LoginPasswdService        loginPasswdService;

    @Resource(name = "accountService")
    private AccountService            accountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name = "webResource")
    private WebDynamicResource        webResource;

    @Resource(name = "cachedSessionService")
    private IVfSsoSession<SsoUser>    userSessionService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Resource(name = "defaultSarsService")
	private DefaultSarsService defaultSarsService;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		userSessionService.setMutexSessionControl(new IMutexSessionControl<SsoUser>() {
			
			@Override
			public String getMutexId(SsoUser user) {
				return user.getOpId();
			}
		});

	}

    /**
     * 进入登录页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/login/page.htm", method = RequestMethod.GET)
    public ModelAndView enter(boolean error, ModelMap model) {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        try {
            data.put("salt", DEFAULT_SALT);
            restP.setData(data);
        } catch (Exception e) {
        }
        return new ModelAndView("login", "response", restP);
    }

    /**
     * 进入登录页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/login/loginSelect.htm")
    public ModelAndView loginSelect(boolean error, ModelMap model) {
        RestResponse restP = new RestResponse();
        return new ModelAndView("/common/exception/loginSelect", "response", restP);
    }

    /**
     * 进入登录页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/login/forceLogin.htm")
    @ResponseBody
    public RestResponse forceLogin(boolean error, ModelMap model, HttpServletRequest request,
                                   HttpServletResponse response) {
        RestResponse restP = new RestResponse();
        String returnUrl = request.getParameter("returnUrl");
        try {
            String token = VfSsoUser.getCurrentToken();
            log.info("forceLogin get token: "+token);
            SsoUser session = userSessionService.get(token);
            log.info("forceLogin get SsoUser: "+session);
            if (session != null) {
                switch (session.getSessionStatus()) {
                    case login:
                        //pass
                        break;
                    case pending:
                        //TODO 验证
                        userSessionService.forceIn(token, session);
                    default:
                        break;
                }
            }
            if (StringUtils.isNotBlank(returnUrl)) {
            	log.info("forceLogin redirect to "+returnUrl);
                restP.setRedirect(URLDecoder.decode(returnUrl, request.getCharacterEncoding()));
            } else {
            	log.info("forceLogin redirect to /my/home.htm ");
                restP.setRedirect(request.getContextPath() + "/my/home.htm");
            }
            restP.setSuccess(true);
        } catch (Exception e) {
        	log.error("forceLogin exception:", e);
            restP.setRedirect(request.getContextPath() + "/index.htm");
        }
        return restP;
    }

    /**
     * 进入登录页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/login/main.htm", method = RequestMethod.POST)
    @ResponseBody
	public RestResponse login(boolean error, ModelMap model, HttpServletRequest request, HttpServletResponse response,
			@Validated LoginForm form, OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        try {
        	String rs = KaptchaImageAction.validateRandCode(request, form.getCaptcha_value());
			if (!"success".equals(rs)) {
				restP.setSuccess(false);
				if("expire".equals(rs))
					restP.setMessage("验证码失效");
				else
					restP.setMessage("验证码错误");
				return restP;
			}
         
            //登录密码解密
            String passwd=request.getParameter("unSafePwInput");
			if (passwd != null) {
				passwd = MD5Util.MD5(passwd);
			}
            String safepw=request.getParameter("checkbox_safepw");
            if (!(null==safepw))
            {
            	passwd = decrpPassword(request, request.getParameter("password"));          	 
            }    
         
            if (null == passwd) {
                restP.setSuccess(false);
                restP.setMessage("登录密码输入有误！");
                return restP;
            }

			String memberId = memberService.queryMemberIntegratedInfo(StringUtils.trim(form.getManageUser()), env);
			
			OperatorVO oper = new OperatorVO();

			oper = operatorService.getOperByLoginName(memberId, StringUtils.trim(form.getOperator_loginName()), "1",
					env);
			
			if (oper == null) {
				restP.setMessage("您输入的操作员不存在！");
				return restP;
			}
			
			loginRiskCtrl(passwd,memberId,form.getManageUser(),request,env);
			
			if (oper.getLockStatusEnum() == OperatorLockStatusEnum.LOCK) {
				restP.setMessage("您输入的操作员被禁用，请联系管理员！");
				return restP;
			} else if (oper.getStatusEnum() == OperatorStatusEnum.CLOSE) {
				restP.setMessage("您输入的操作员被注销，请联系管理员！");
				return restP;
			}
			EnterpriseMember member = null;
			try {
    			 member = loginPasswdService.checkOperatorLoginPwd(
    					StringUtils.trim(form.getOperator_loginName()), oper.getOperatorId(), passwd,
    					StringUtils.trim(form.getManageUser()), DEFAULT_SALT, request.getRemoteAddr());
			} catch(BizException e) {
			    /*if(e.getMessage().contains("登录密码被锁定")) {
			        logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_LOGIN_PWD, member, env,
			                DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
			    }*/
			    throw e;
			}

            if (null == member) {
                PersonMember member_query = new PersonMember();
                member_query.setLoginName(form.getManageUser());
                member_query = memberService.queryMemberIntegratedInfo(member_query, env);
                restP.setSuccess(true);
                restP.setRedirect(webResource.getPersonalWalletAddr()
                                  + "/register/sendRegisterEmailLogin.htm?email="
                                  + form.getManageUser() + "&memberId="
                                  + member_query.getMemberId() + "&loginName="
                                  + form.getManageUser());
                return restP;
            }
            if (MemberLockStatus.LOCKED.name().equals(member.getLockStatus().name())) {
                restP.setSuccess(false);
                restP.setMessage("会员已被锁定，请联系客服！");
                return restP;
            }
			member.setCurrentOperatorId(oper.getOperatorId());

            MerchantListResponse merchantListResponse = memberService
                .queryMerchantByMemberId(member.getMemberId());
            if ((null == merchantListResponse.getMerchantInfos())
                || ((null != merchantListResponse.getMerchantInfos()) && (merchantListResponse
                    .getMerchantInfos().size() < 0))) {
                restP.setMessage("未开通商户的企业会员！");
                restP.setSuccess(false);
                return restP;
            }

			this.saveToken(member, request, response, restP, CommonConstant.USERTYPE_MERCHANT);
			
			// 记录登录成功的日志
			logger.info(LogUtil.generateMsg(OperateTypeEnum.LOGIN, member, env, "登录成功"));
			updateLastLoginInfo(request,member.getMemberId(),member.getOperatorId());
        } catch (Exception e) {
            logger.error("", e);
            restP.setMessage(getErrMsg(e.getMessage()));
        }
        return restP;
    }
    
    

    /*
     * 保存SsoUser并将其对应的token保存至cookie(单点登录用)
     */
	private void saveToken(EnterpriseMember member, HttpServletRequest request, HttpServletResponse response,
			RestResponse restP, String userType) {
        try {
            if ((null != member) && (null != member.getMemberId())) {
				clearLoginInfo(request, response);
                SsoUser ssoUser = new SsoUser();
                ssoUser.setId(member.getMemberId());
                logger.info("LoginName: " + member.getLoginName());
                ssoUser.setLoginName(member.getLoginName());
                ssoUser.setName(member.getMemberName());
                ssoUser.setOpId(member.getCurrentOperatorId());
                ssoUser.setOpName(member.getOperator_login_name());
				ssoUser.setUserType(userType);
                String token = userSessionService.create(ssoUser);
                logger.info("token: " + token);
                if(logger.isInfoEnabled()) {
                	logger.info("[ent]saveToken,token={}", token);
                }
                if (token != null) {
                    VfSsoCookie.setCookie(token, response);
                    logger.info("vfssoDomain: " + webResource.getVfssoDomain());
                    for (String domain : webResource.getVfssoDomain().split(",")) {
                        VfSsoCookie.setCookie(token, response, domain, null);
                    }
                    String redirect_url = request.getParameter("returnUrl");
                    if (StringUtils.isBlank(redirect_url)) {
						redirect_url = request.getParameter("redirect_url");
					}
                    logger.info("单点登录用户状态: " + ssoUser.getSessionStatus());
                    if (SessionStatusKind.pending.equals(ssoUser.getSessionStatus())) {
                        if (StringUtils.isNotEmpty(redirect_url) && !"".equals(redirect_url)) {
                            restP.setRedirect(request.getContextPath()
                                              + "/login/loginSelect.htm?returnUrl=" + redirect_url);
                        } else {
                            restP.setRedirect(request.getContextPath() + "/login/loginSelect.htm");
                        }
                    } else if (StringUtils.isNotEmpty(redirect_url) && !"".equals(redirect_url)) {
                        restP.setRedirect(URLDecoder.decode(redirect_url,
                            request.getCharacterEncoding()));
                    } else {
                        restP.setRedirect(request.getContextPath() + "/my/home.htm");
                    }
                    restP.setSuccess(true);
                } else {
                    logger.error("登录失败！");
                    restP.setMessage("登录失败");
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            restP.setMessage(getErrMsg(e.getMessage()));
        }
    }


    @RequestMapping(value = "/login/query.htm")
    @ResponseBody
    public RestResponse queryOperator(HttpServletRequest request, HttpServletResponse response,
                                      OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        try {
            List<OperatorVO> vos = new ArrayList<OperatorVO>();
            Map<String, Object> data = new HashMap<String, Object>();
            String memberIdentity = request.getParameter("manageUser");
            String memberId = memberService.queryMemberIntegratedInfo(memberIdentity, env);
            List<OperatorVO> operatorVOs = operatorService.queryOperatorsByMemberId(memberId, true,
                false, env);
            for (OperatorVO operatorVO : operatorVOs) {
                if (!operatorVO.getDefaultActive()
                    || "LOCK".equals(operatorVO.getLockStatusEnum().name())
                    || "NOT_ACTIVE".equals(operatorVO.getStatusEnum().name())
                    || "CLOSE".equals(operatorVO.getStatusEnum().name())) {
                    continue;
                }
				OperatorVO vo = operatorService.getOperatorById(operatorVO.getOperatorId(), env);
				OperatorVO vo2 = new OperatorVO();
				vo2.setOperatorId(vo.getOperatorId());
				vo2.setLoginName(vo.getLoginName());
				vos.add(vo2);
            }
            data.put("operators", vos);
            restP.setData(data);
            restP.setSuccess(true);
        } catch (Exception e) {
            logger.error("", e);
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
        }
        return restP;
    }

    /**
     * 进入退出页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/logout.htm", method = RequestMethod.GET)
    public String logout(HttpServletRequest req,HttpServletResponse resp) {
        log.info("cas-sso企业钱包退出");
        HttpSession session = req.getSession();
        session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER);
        session.invalidate();
        try {
            VfSsoCookie.removeCookie(resp);
            VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
            if(VfSsoUser.getCurrentToken()!=null) {
				userSessionService.remove(VfSsoUser.getCurrentToken());
			}
        } catch (SessionException e) {
            logger.error("单点登录注销用户信息失败");
        }
		return "redirect:/index.htm";
    }

    /**
     * 进入退出页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/logoutAndRedirect.htm", method = RequestMethod.GET)
    public String logoutAndRedirect(HttpServletRequest req,HttpServletResponse resp) {
        log.info("cas-sso企业钱包退出");
        HttpSession session = req.getSession();
        session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER);
        session.invalidate();
        try {
            VfSsoCookie.removeCookie(resp);
            VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
            if(VfSsoUser.getCurrentToken()!=null) {
				userSessionService.remove(VfSsoUser.getCurrentToken());
			}
        } catch (SessionException e) {
            logger.error("单点登录注销用户信息失败");
        }
        return "redirect:" + webResource.getPersonalWalletAddr() + "/index.htm";
    }

	/**
	 * 异步校验登陆名
	 * 
	 * @param req
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/login/checkLoginName.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse checkLoginNameIsExist(HttpServletRequest req, OperationEnvironment env) throws BizException {
		RestResponse restP = new RestResponse();

		EnterpriseMember queryCondition = new EnterpriseMember();
		queryCondition.setLoginName(req.getParameter("username"));
		EnterpriseMember enterpriseMember;
		try {
			enterpriseMember = memberService.queryCompanyMember(queryCondition, env);
			if ("login".equals(req.getParameter("cmd"))) {
				if (enterpriseMember.getLockStatus() == MemberLockStatus.LOCKED) {
					restP.setMessage("2");// 用户名被锁定
				} else if (enterpriseMember.getMemberType() != MemberType.MERCHANT) {
					restP.setMessage("1");// 用户名不存在
				} else {
					restP.setSuccess(true);
				}

			} else {
				restP.setMessage("1");// 用户名已存在
			}

		} catch (MemberNotExistException e) {
			if ("register".equals(req.getParameter("cmd"))) {
				restP.setSuccess(true);
			} else {
				restP.setMessage("1");// 用户名不存在
			}

		}
		return restP;
	}

	/**
	 * 异步校验验证码
	 * 
	 * @param req
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/login/checkVerifiCode.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse checkVerifiCode(HttpServletRequest req) throws BizException {
		RestResponse restP = new RestResponse();

		try {
			String rs = KaptchaImageAction.validateRandCode(req, req.getParameter("code"));
			if (!"success".equals(rs)) {
				if("expire".equals(rs))
					restP.setMessage("验证码失效");
				else
					restP.setMessage("验证码错误");
			}else
				restP.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
		}

		return restP;
	}

	private void clearLoginInfo(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession();
		session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER);
		session.invalidate();
		try {
			VfSsoCookie.removeCookie(resp);
			VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
			if (VfSsoUser.getCurrentToken() != null) {
				userSessionService.remove(VfSsoUser.getCurrentToken());
			}
		} catch (SessionException e) {
			logger.error("单点登录注销用户信息失败");
		}
	}
	
	/**
	 * 风险控制
	 * @param member
	 * @param request
	 * @param env
	 * @throws Exception
	 */
	private void loginRiskCtrl(String passwd,String memberId,String loginName,HttpServletRequest request,OperationEnvironment env) throws Exception{
		String device_id = "";
		for(Cookie cookie:request.getCookies()){
			if(StringUtils.equalsIgnoreCase(cookie.getName(), "UDCREDIT_DEVICEID")){
				device_id = cookie.getValue();
				break;
			}
		}
		//加密
		String saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(passwd)
				+ "ac5e940b94a51609788f38c6db70f39b");
		Map<String,String> params = new HashMap<String, String>();
		params.put("user_ip_address", env.getClientIp());
		params.put("device_id", device_id);
		params.put("user_id", memberId);
//		params.put("email", loginName);
		params.put("account_password",saltPasswd);
		SarsResponse resp = null;
		try {
			resp = defaultSarsService.riskControl(UdcreditType.LOGIN.getStrategyCode(), UdcreditType.LOGIN.getScenarioCode(), params, env);
		} catch (BizException e) {
			logger.error("风控调用异常",e);
		}
		if(resp != null && !StringUtils.equalsIgnoreCase(resp.getResult(),"0000")){
			throw new Exception("风控拦截,您的登陆异常");
		}
	}

}
