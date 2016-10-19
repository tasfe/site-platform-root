package com.netfinworks.site.web.action.paypasswd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.KaptchaImageAction;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.form.PayPasswordForm;
import com.netfinworks.site.web.form.ResetPayPasswordForm;
import com.netfinworks.site.web.processor.ValidationProcessor;
import com.netfinworks.site.web.util.BankCardUtil;

/**
 * <p>找回支付密码（短信方式、邮件方式）</p>
 * @author zhangyun.m
 * @version $Id: RefindPaypasswd.java, v 0.1 2014年5月22日 下午3:45:29 zhangyun.m Exp $
 */
@Controller
public class RefindPaypasswdAction extends BaseAction {

    protected Logger                log = LoggerFactory.getLogger(getClass());

    @Resource(name = "defaultCertService")
    private DefaultCertService      defaultCertService;

    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;

    @Resource
    private ValidationProcessor     validationProcessor;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    @Resource(name = "webResource")
    private WebDynamicResource      webResource;

    @Resource(name = "memberService")
    private MemberService           memberService;
    
    @Resource(name = "authVerifyService")
    private AuthVerifyService    authVerifyService;
    
    
    /**
     * 支付密码找回首页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/go-get-paypasswd-index.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
	public ModelAndView getPayPasswdIndex(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
        PersonMember user = getUser(request);
        RestResponse restP = new RestResponse();
        AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();

		boolean isBindPhone = false;
		boolean isBindEmail = false;
		Map<String, Object> data = new HashMap<String, Object>();
		// 判断邮箱或者手机的认证
        authVerifyInfo.setMemberId(user.getMemberId());
    	List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
    	for (int i = 0; i < infos.size(); i++) {
    		authVerifyInfo = infos.get(i);
            if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("mobile", authVerifyInfo.getVerifyEntity());
				isBindPhone = true;
            } else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("email", authVerifyInfo.getVerifyEntity());
				isBindEmail = true;
            }
    	}
    	if (isBindPhone && !isBindEmail) {
			return new ModelAndView("redirect:/my/go-get-paypasswd-phone-first.htm");
		}else if(!isBindPhone && isBindEmail) {
			return new ModelAndView("redirect:/my/go-get-paypasswd-email-send.htm");
		}

    	data.put("loginName", user.getLoginName());
        restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/get-pay-passwd-opt",
            "response", restP);
    }
    
    

    /**通过什么方式找回支付密码（检测证件是否正确）
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/check-certificates.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView checkCertificates(HttpServletRequest request, OperationEnvironment env,
                                     ModelMap model) throws Exception {
    	String type = request.getParameter("type");
        RestResponse restP = new RestResponse();
        PersonMember user = getUser(request);
        Map<String, Object> data = new HashMap<String, Object>();
        //获取证件号
        String certificates = request.getParameter("certificates");

		String authcert = "";

		if (user.getMemberType().getCode().equals("1")) {
			// authcert = getEncryptInfo(user.getMemberId(), DeciphedType.ID_CARD, env);
			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.IDENTITY);
			authInfoReq.setOperator(user.getOperatorId());
			AuthInfo info = defaultCertService.queryRealName(authInfoReq);
			authcert = info.getAuthNo();
			if (StringUtils.isEmpty(authcert)) {
				authInfoReq.setMemberId(user.getMemberId());
				authInfoReq.setAuthType(AuthType.REAL_NAME);
				authInfoReq.setOperator(user.getOperatorId());
				info = defaultCertService.queryRealName(authInfoReq);
				authcert = info.getAuthNo();
			}
		} else {
			EnterpriseMember enterpriseMember = new EnterpriseMember();
			enterpriseMember.setMemberId(user.getMemberId());
			CompanyMemberInfo compInfo = memberService.queryCompanyInfo(enterpriseMember, env);
			authcert = compInfo.getLicenseNo();
		}

		if (certificates.equals(authcert)) {
			return new ModelAndView("redirect:/my/go-get-paypasswd-phone-first.htm?type=" + type);
		}
		restP.setMessage("实名认证证件号不正确");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success", "response", restP);
    }

    /**go短信找回支付密码页面
     * @param request
     * @param resp
     * @param model
     * @return
     * @throws ServiceException
     * @throws MemberNotExistException
     */
    @RequestMapping(value = "/my/go-get-paypasswd-phone-first.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
	public ModelAndView getPayPasswdPhone(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {


        PersonMember user = getUser(request);
//        String selection = request.getParameter("type");
        RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();

//		if ((user.getNameVerifyStatus() == AuthResultStatus.PASS)
//				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V0)
//				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V1)
//				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V2)) {
//			String token = (String) request.getSession().getAttribute(CommonConstant.REFIND_PAYPWD_TOKEN);
//			if (!token.equals(CommonConstant.REFIND_PAYPWD_TOKEN)) {
//				return new ModelAndView("redirect:/error.htm");
//			}
//		}

//		if ("1".equals(selection) || "2".equals(selection)) {
//			if (!KaptchaImageAction.validateRandCode(request, request.getParameter("captcha_value"))) {
//				restP.setMessage("验证码输入有误！");
//				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success", "response",
//						restP);
//			}
//		}
//
//        AuthInfoRequest info = new AuthInfoRequest();
//        info.setMemberId(user.getMemberId());
//        info.setAuthType(AuthType.RESET_PAYPWD);
//        info.setOperator(user.getOperatorId());
//
//        //认证结果
//        AuthResultStatus state = defaultCertService.queryAuthType(info);
		EncryptData encryptData = memberService.decipherMember(user.getMemberId(), DeciphedType.CELL_PHONE,
				DeciphedQueryFlag.ALL, env);
//        data.put("state", state.getCode());
        data.put("member", user);
        data.put("bizType",  BizType.REFIND_PAYPASSWD.getCode());
		data.put("mobile", encryptData.getPlaintext());
//        data.put("selection", selection);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getbyphone-first",
            "response", restP);
    }

    /**检测验证码是否正确
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/check-paypasswd-captcha.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView checkCaptcha(HttpServletRequest request, OperationEnvironment env,
                                     ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
//        String selection = request.getParameter("selection");
        String mobileCaptcha = request.getParameter("mobileCaptcha");
        Map<String, Object> data = new HashMap<String, Object>();
        restP.setData(data);

        PersonMember user = getUser(request);
        EncryptData  edata = memberService.decipherMember(user.getMemberId(),
            DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
        String mobile = edata.getPlaintext();
        log.info("mobile:{},mobileCaptcha:{}", mobile, mobileCaptcha);
        request.getSession().setAttribute(CommonConstant.REFIND_PAYPWD_TOKEN_TWO, CommonConstant.REFIND_PAYPWD_TOKEN_TWO);
        if ((user.getNameVerifyStatus() == AuthResultStatus.PASS)
				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V0)
				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V1)
				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V2)) {
        	// 实名认证
        	data.put("realName", "true");
        	request.getSession().setAttribute(CommonConstant.REFIND_PAYPWD_TOKEN,
					CommonConstant.REFIND_PAYPWD_TOKEN);
        }
    
        //封装校验短信验证码请求
        AuthCodeRequest req = new AuthCodeRequest();
        req.setMemberId(user.getMemberId());
        req.setMobile(mobile);
        String ticket = defaultUesService.encryptData(mobile);
        req.setMobileTicket(ticket);
        req.setAuthCode(mobileCaptcha);
        req.setBizId(user.getMemberId());
        req.setBizType(BizType.REFIND_PAYPASSWD.getCode());
        //校验短信验证码
        boolean verifyResult = defaultSmsService.verifyMobileAuthCode(req, env);
        
        restP.setSuccess(verifyResult);
        data.put("member", user);
        if (verifyResult) {//如果验证码正确
    		return new ModelAndView(CommonConstant.URL_PREFIX+ "/paypasswd/set-pay-passwd", "response",restP);

        } else {
			data.put("bizType", BizType.REFIND_PAYPASSWD.getCode());
//			data.put("selection", selection);
			data.put("mobile", mobile);
			data.put("message", "您输入的验证码不正确!");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getbyphone-first", "response", restP);
        }
      
    }
    
    
    
    /**
     * 重设支付密码 (设置新密码验证)
     * @param request
     * @param form
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/set-back-paypasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setBackPayPassWord(HttpServletRequest request, HttpServletResponse resp,
                                           @Validated ResetPayPasswordForm form, ModelMap model,OperationEnvironment env)
                                                                                                throws Exception {
        RestResponse restP = new RestResponse();
        PersonMember user = getUser(request);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("member", user);
        restP.setData(data);
        if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.SET_BACK_PAYPASSWD.getMsg(), user, env));
		}
        String token = request.getParameter("token");
		if ((user.getNameVerifyStatus() == AuthResultStatus.PASS)
				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V0)
				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V1)
				|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V2)) {
			data.put("realName", "true");
			String certificates = request.getParameter("certificates");
	        String authcert = "";

			if (user.getMemberType().getCode().equals("1")) {
				// authcert = getEncryptInfo(user.getMemberId(), DeciphedType.ID_CARD, env);
				AuthInfoRequest authInfoReq = new AuthInfoRequest();
				authInfoReq.setMemberId(user.getMemberId());
				authInfoReq.setAuthType(AuthType.IDENTITY);
				authInfoReq.setOperator(user.getOperatorId());
				AuthInfo info = defaultCertService.queryRealName(authInfoReq);
				authcert = info.getAuthNo();
				if (StringUtils.isEmpty(authcert)) {
					authInfoReq.setMemberId(user.getMemberId());
					authInfoReq.setAuthType(AuthType.REAL_NAME);
					authInfoReq.setOperator(user.getOperatorId());
					info = defaultCertService.queryRealName(authInfoReq);
					authcert = info.getAuthNo();
				}
			} else {
				EnterpriseMember enterpriseMember = new EnterpriseMember();
				enterpriseMember.setMemberId(user.getMemberId());
				CompanyMemberInfo compInfo = memberService.queryCompanyInfo(enterpriseMember, env);
				authcert = compInfo.getLicenseNo();
			}

			if (!certificates.equals(authcert)) {
				restP.setMessage("身份证号码填写错误!");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response",
						restP);
			}
			String pwdToken = (String) request.getSession().getAttribute(CommonConstant.REFIND_PAYPWD_TOKEN);
			if (!pwdToken.equals(CommonConstant.REFIND_PAYPWD_TOKEN)) {
				return new ModelAndView("redirect:/error.htm");
			}
		} else {
			String pwdToken = (String) request.getSession().getAttribute(CommonConstant.REFIND_PAYPWD_TOKEN_TWO);
			if (!pwdToken.equals(CommonConstant.REFIND_PAYPWD_TOKEN_TWO)) {
				return new ModelAndView("redirect:/error.htm");
			}
		}

        String password = decrpPassword(request, form.getNewPasswd());
        String repassword = decrpPassword(request, form.getRenewPasswd());

        
        Map<String, String> errorMap = new HashMap<String, String>();

        //清空随机因子
        deleteMcrypt(request);
		data.put("token", token);
        
        if ((password == null) || (repassword == null)) {
			errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
			restP.setErrors(errorMap);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response",
					restP);
		} else if ((password.length() < 6) || (password.length() > 32)) {
			errorMap.put("error_passwd_parten", "error_passwd_parten");
			restP.setErrors(errorMap);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response",
					restP);
        } else if (!password.equals(repassword)) {
			errorMap.put("repassword_password_not_same", "repassword_password_not_same");
			restP.setErrors(errorMap);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response",
					restP);
        }

        try {
            PayPasswdRequest req = new PayPasswdRequest();
            req.setPassword(password);
            req.setMemberId(user.getMemberId());
            req.setOperator(user.getOperatorId());
            req.setAccountId(user.getDefaultAccountId());
            req.setClientIp(request.getRemoteAddr());
            defaultPayPasswdService.resetPayPasswordLock(req);
            CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
            if (commRep.isSuccess()) {
                restP.setSuccess(true);
                payPassWordCacheService.clear(token);
				request.getSession().removeAttribute(CommonConstant.REFIND_PAYPWD_TOKEN);
				request.getSession().removeAttribute(CommonConstant.REFIND_PAYPWD_TOKEN_TWO);
                return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                    "response", restP);
            } else {
                restP.setSuccess(false);
                if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                    commRep.getResponseCode())) {
					errorMap.put("password_equal_login_password", "password_equal_login_password");
					restP.setErrors(errorMap);
					return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response", restP);
                } else {
                    restP.setMessage("支付密码设置失败");
                }
				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response", restP);
            }
        } catch (ServiceException e) {
            restP.setMessage(e.getMessage());
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response", restP);
        }
    }

    
    
    
    /**短信找回支付密码：重置支付密码
     * @param request
     * @param resp
     * @param form
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/go-get-paypasswd-phone-second.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView goRefindPayPasswordSecond(HttpServletRequest request,
                                                  HttpServletResponse resp,
                                                  @Validated PayPasswordForm form) throws Exception {

        RestResponse restP = new RestResponse();
        PersonMember user = getUser(request);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("member", user);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/getbyphone-second",
            "response", restP);
    }

    /**找回支付密码（发送邮件）
     * @param request
     * @param resp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/go-get-paypasswd-email-send.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView goRefindPayPasswordSendEmail(HttpServletRequest request,
                                                     OperationEnvironment env,
                                                     HttpServletResponse resp) throws Exception {
        RestResponse restP = new RestResponse();
        PersonMember user = getUser(request);

//		String selection = request.getParameter("type");
//		if ("1".equals(selection)) {
//			if (!KaptchaImageAction.validateRandCode(request, request.getParameter("captcha_value"))) {
//				restP.setMessage("验证码输入有误！");
//				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success", "response",
//						restP);
//			}
//		}
        
        String token = BankCardUtil.getUuid();
        String email = getEncryptInfo(request, DeciphedType.EMAIL, env);
        if (StringUtils.isEmpty(email)) {
            ModelAndView mv = new ModelAndView();
            logger.error("通过邮箱找回支付密码链接已经失效");
            mv.addObject("message", "对不起，通过邮箱找回支付密码链接已经失效!");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        String activeUrl =  request.getScheme()+"://"+ request.getServerName() + ":" + request.getServerPort()
                           + request.getContextPath()+"/my/resetPayPassWord.htm?token=" + token;
        logger.info("邮箱找回支付密码的地址：{},发送邮件找回支付密码链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName",
            StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
        objParams.put("activeUrl", activeUrl);
        boolean emailResult = defaultPayPasswdService.sendEmail(email, BizType.REFIND_SENDEMAIL.getCode(), objParams);
        logger.info("发送至{}邮箱的结果：{}", email, emailResult);
        if (emailResult) {
            // 调用统一缓存
            payPassWordCacheService.put(token, JSONArray.toJSONString(user));
            restP.setMessage("发送邮件成功");
        }else{
            restP.setMessage("发送邮件失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success", "response", restP);
        }

        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX +"/paypasswd/sendMailResult","emailResult",emailResult);
        modelAndView.addObject("email", user.getEmail());
        modelAndView.addObject("bizType",BizType.REFIND_PAYPASSWD.getCode());
        String loginMailUrl=LoginEmailAddress.getEmailLoginUrl(email);
        modelAndView.addObject("loginMailUrl", loginMailUrl);
        return modelAndView;

    }
    
    
    /** 修改支付密码页面 (邮箱缓存跳转到页面)
     * @param request
     * @param model
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/resetPayPassWord.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView getSetPayPassWordUrl(HttpServletRequest request, ModelMap model) {
        RestResponse restP = new RestResponse();
        try {
            String key = request.getParameter("token");
            String info = payPassWordCacheService.load(key);
            Map<String, Object> map = JSONObject.parseObject(info);

            Map<String, Object> data = new HashMap<String, Object>();

            data.put("token", key);
            request.getSession().setAttribute(CommonConstant.REFIND_PAYPWD_TOKEN_TWO,
                    CommonConstant.REFIND_PAYPWD_TOKEN_TWO);
            PersonMember user = getUser(request);
            if ((user.getNameVerifyStatus() == AuthResultStatus.PASS)
                    || (user.getCertifyLevel() == CertifyLevel.CERTIFY_V0)
                    || (user.getCertifyLevel() == CertifyLevel.CERTIFY_V1)
                    || (user.getCertifyLevel() == CertifyLevel.CERTIFY_V2)) {
                // 实名认证
                data.put("realName", "true");
                request.getSession().setAttribute(CommonConstant.REFIND_PAYPWD_TOKEN,
                        CommonConstant.REFIND_PAYPWD_TOKEN);
            }
            data.put("member", user);
            restP.setData(data);

            if ((null != info) && !"".equals(info)) {
                if (!map.get("memberId").equals(user.getMemberId())) {
                    restP.setMessage("找回支付密码连接失效");
                    return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/setpaypasswd-success",
                            "response", restP);
                }
                
                return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd", "response", restP);
            } else {
                restP.setMessage("找回支付密码连接失效");
                return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/setpaypasswd-success", "response",
                        restP);
            }
        } catch (Exception e) {
            logger.error("找回支付密码异常", e);
            restP.setMessage("找回支付密码连接失效");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/setpaypasswd-success", "response",
                    restP);
        }
        


    }



	/**异步发送邮件
     * @param request
     * @param resp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/refindPwd/ajaxSendMail.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    @ResponseBody
    public RestResponse ajaxSendMail(HttpServletRequest request,
                                                     OperationEnvironment env,
                                                     HttpServletResponse resp) throws Exception {

        RestResponse restP = new RestResponse();
        PersonMember user = getUser(request);
        String token = BankCardUtil.getUuid();
        String email = getEncryptInfo(request, DeciphedType.EMAIL, env);
        String activeUrl =  request.getScheme()+"://" + request.getServerName() + ":" + request.getServerPort()
                           + request.getContextPath() +"/my/resetPayPassWord.htm?token=" + token;
        logger.info("邮箱找回支付密码的地址：{},发送邮件找回支付密码链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName",
            StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
        objParams.put("activeUrl", activeUrl);
        boolean emailResult = defaultPayPasswdService.sendEmail(email, BizType.REFIND_SENDEMAIL.getCode(), objParams);
        logger.info("发送至{}邮箱的结果：{}", email, emailResult);
        if (emailResult) {
            // 调用统一缓存
            payPassWordCacheService.put(token, JSONArray.toJSONString(user));
            restP.setMessage("发送邮件成功");
        }else{
            restP.setMessage("发送邮件失败");
        }
        return restP;

    }
    
    /**
     * 异步验证图片校验码
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/refindPwd/ajaxValidateCode.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    @ResponseBody
    public RestResponse ajaxValidateCode(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	RestResponse restP = new RestResponse();
    	String code = request.getParameter("code");
    	if (!KaptchaImageAction.validateRandCode(request, code)) {
			restP.setMessage("验证码输入有误！");
			return restP;
		}
    	restP.setSuccess(true);
    	return restP;
    }
    
}
