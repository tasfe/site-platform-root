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
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.PayPasswordForm;
import com.netfinworks.site.web.action.form.ResetPayPasswordForm;
import com.netfinworks.site.web.action.processor.ValidationProcessor;
import com.netfinworks.site.web.action.validator.PasswordValidator;
import com.netfinworks.site.web.util.BankCardUtil;
import com.netfinworks.site.web.util.LogUtil;


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
    private MemberService     memberService;

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
    public ModelAndView getPayPasswdPhone(HttpServletRequest request, HttpServletResponse resp,
                                          ModelMap model) throws ServiceException,
                                                         MemberNotExistException {
        EnterpriseMember user = getUser(request);
        RestResponse restP = new RestResponse();
        AuthInfoRequest info = new AuthInfoRequest();
        info.setMemberId(user.getMemberId());
        info.setAuthType(AuthType.RESET_PAYPWD);
        info.setOperator(user.getOperatorId());
		info.setMessage("merchant");

        Map<String, Object> data = new HashMap<String, Object>();
        //认证结果
        AuthResultStatus state = defaultCertService.queryAuthType(info);
        data.put("state", state.getCode());
        data.put("member", user);
        data.put("bizType", "REFIND_PAYPASSWD");
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/getbyphone-first",
            "response", restP);
    }

    /**检测验证码是否正确
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/check-ent-paypasswd-captcha.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView checkCaptcha(HttpServletRequest request, OperationEnvironment env,
                                     ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();

        EnterpriseMember user = getUser(request);
        EncryptData  edata = memberService.decipherMember(user.getMemberId(),
            DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
        String mobile = edata.getPlaintext();
        String mobileCaptcha = request.getParameter("mobileCaptcha");
        log.info("mobile:{},mobileCaptcha:{}", mobile, mobileCaptcha);
        Map<String, String> errorMap = new HashMap<String, String>();
        data.put("member", user);

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
        if (verifyResult) {//如果验证码正确，到第二步修改支付密码
            restP.setMessage("验证码正确");
            restP.setData(data);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + "/paypasswd/getback/getbyphone-second", "response", restP);
        } else {
            errorMap.put("paypwdCaptcha_error", "paypwdCaptcha_error");
            restP.setErrors(errorMap);
            restP.setData(data);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/getbyphone-first",
                "response", restP);
        }

    }

    /**
     * 特约商户找回支付密码:短信方式
     *
     * @param model
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/resetpaypasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setPayPassword(HttpServletRequest request,
                                       @Validated ResetPayPasswordForm form) throws Exception {

        RestResponse restP = new RestResponse();

        //查询session用户
        EnterpriseMember user = getUser(request);

        Map<String, String> errorMap = new HashMap<String, String>();
        Map<String, Object> info = new HashMap<String, Object>();

        PasswordValidator validator = new PasswordValidator(request);
        List<ObjectError> errors = validationProcessor.validateForm(form, validator);
        if ((errors != null) && (errors.size() > 0)) {
            Map<String, String> errorInfo = new HashMap<String, String>();

            for (ObjectError error : errors) {
                errorInfo.put(error.getCode(), error.getDefaultMessage());
            }
            restP.setErrors(errorInfo);
            return new ModelAndView("redirect:/my/go-get-paypasswd-phone-second.htm");
        }
        String password = decrpPassword(request, form.getNewPasswd());
        String repassword = decrpPassword(request, form.getRenewPasswd());
        deleteMcrypt(request);
        try {
            PayPasswdRequest req = new PayPasswdRequest();
            if (password == null) {
                throw new ServiceException("支付密码不能为空!");
			} else if ((password.length() < 6) || (password.length() > 32)) {
                //String cert = defaultPkiService.getCertification();
                info.put("mobile", user.getMobileStar());
                info.put("userName", user.getPlateName());
                //info.put(_base64_cert, cert);
                //info.put("salt", DEFAULT_SALT);
                errorMap.put("error_passwd_parten", "error_passwd_parten");
                restP.setErrors(errorMap);
                restP.setData(info);
                return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember",
                    "response", restP);
            } else if (!password.equals(repassword)) {
                //String cert = defaultPkiService.getCertification();
                info.put("mobile", user.getMobileStar());
                info.put("userName", user.getPlateName());
                //info.put(_base64_cert, cert);
                //info.put("salt", DEFAULT_SALT);
                errorMap.put("repassword_password_not_same", "repassword_password_not_same");
                restP.setErrors(errorMap);
                restP.setData(info);
                return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember",
                    "response", restP);
            }
            req.setPassword(password);
            req.setAccountId(user.getDefaultAccountId());
			req.setOperator(user.getCurrentOperatorId());
            req.setClientIp(request.getRemoteAddr());

            restP.setSuccess(false);
            restP.setMessage("设置支付密码失败");
            //1.验证旧密码是否锁住
            boolean oldPayPwdClocked = defaultPayPasswdService.isPayPwdClocked(req);
            boolean unlocked = false;
            if (oldPayPwdClocked) {
                //2.如果密码锁住，则解锁。
                unlocked = defaultPayPasswdService.resetPayPasswordLock(req);
            }
            //a.旧支付密码没有锁住，则设置新的支付密码。 b. 旧的支付密码锁住，解锁成功，则设置新的支付密码。
            if (!oldPayPwdClocked || unlocked) {
                CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
                if (commRep.isSuccess()) {
                    restP.setSuccess(true);
                    restP.setMessage("设置支付密码成功");
                }else{
                    if(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(commRep.getResponseCode())){
                        restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                    }else{
                        restP.setMessage("支付密码设置失败");
                    }
                }
            }

        } catch (ServiceException e) {
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());

        }
        info.put("member", user);
        restP.setData(info);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/refind-paypasswd-phone-success",
            "response", restP);

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
        EnterpriseMember user = getUser(request);
        Map<String, Object> data = initOcx();

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
        EnterpriseMember user = getUser(request);
        
		if (!CommonConstant.REFRESH_TOKEN.equals(request.getSession().getAttribute(CommonConstant.REFRESH_TOKEN))) {
			return new ModelAndView("redirect:/my/go-get-paypasswd-index.htm");
		}

        String token = BankCardUtil.getUuid();
        String email = getEncryptInfo(request, DeciphedType.EMAIL, env);
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/my/reset-paywd-index.htm?token=" + token;
        logger.info("邮箱找回支付密码的地址：{},发送邮件找回支付密码链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName",
            StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
        objParams.put("activeUrl", activeUrl);
		boolean emailResult = defaultPayPasswdService.sendEmail(email, BizType.REFIND_SENDEMAIL.getCode(), objParams);
        logger.info("发送至{}邮箱的结果：{}", email, emailResult);
        if (emailResult) {
            // 调用统一缓存
            payPassWordCacheService.put(token, user.getMemberId() + "," +email);
            restP.setMessage("发送邮件成功");
        }else{
            restP.setMessage("发送邮件失败");
        }

        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX +"/paypasswd/sendMailResult","emailResult",emailResult);
        modelAndView.addObject("email", user.getEmail());
        String loginMailUrl=LoginEmailAddress.getEmailLoginUrl(email);
        modelAndView.addObject("loginMailUrl", loginMailUrl);
		request.getSession().removeAttribute(CommonConstant.REFRESH_TOKEN);
        return modelAndView;

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
        EnterpriseMember user = getUser(request);

        String token = BankCardUtil.getUuid();
        String email = getEncryptInfo(request, DeciphedType.EMAIL, env);
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/my/reset-paywd-index.htm?token=" + token;
        logger.info("邮箱找回支付密码的地址：{},发送邮件找回支付密码链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName",
            StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
        objParams.put("activeUrl", activeUrl);
        boolean emailResult = defaultPayPasswdService.sendEmail(email, BizType.REFIND_SENDEMAIL.getCode(), objParams);
        logger.info("发送至{}邮箱的结果：{}", email, emailResult);
        if (emailResult) {
            // 调用统一缓存
            payPassWordCacheService.put(token, user.getMemberId() + "," +email);
            restP.setMessage("发送邮件成功");
        }else{
            restP.setMessage("发送邮件失败");
        }
        return restP;

    }

	@RequestMapping(value = "/my/reset-paywd-index.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView goRestPayPassWd(HttpServletRequest request) throws Exception {
		String key = request.getParameter("token");
		RestResponse restP = new RestResponse();
		String info = payPassWordCacheService.load(key);
		Map<String, Object> data = initOcx();
		EnterpriseMember user = getUser(request);
		data.put("member", user);
		restP.setData(data);
		if ((null != info) && !"".equals(info)) {
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/reset-pay-passwd", "response",
					restP);
		}
		logger.info("[找回支付密码连接失效]");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/reset-paypasswd-result", "response",
				restP);
	}

	@RequestMapping(value = "/my/reset-paypasswd-check.htm")
	public ModelAndView checkTelMsgCode(String phoneVerifyCode, HttpServletRequest request, OperationEnvironment env)
			throws BizException, ServiceException {
	    ModelAndView mv = new ModelAndView();
		EnterpriseMember enterpriseMember = getUser(request);
		logger.info(LogUtil.generateMsg(OperateTypeEnum.FIND_PAY_PWD, enterpriseMember, env, "营业执照+手机验证码"));
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);

		String isNameVerify = request.getParameter("isNameVerify");
		String isBindPhone = request.getParameter("isBindPhone");

		if ("true".equals(isBindPhone)) {
		    if (StringUtils.isNotEmpty(phoneVerifyCode)) {
    			// 1、验证短信验证码是否正确
    			AuthCodeRequest req = new AuthCodeRequest();
    			EncryptData encryptData = memberService.decipherMember(enterpriseMember.getMemberId(),
    					DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
    			String mobile = encryptData.getPlaintext(); // 获取解密手机号码进行验证码校验
    			req.setMemberId(enterpriseMember.getMemberId());
    			req.setMobile(mobile);
    			String ticket = defaultUesService.encryptData(mobile);
    			req.setMobileTicket(ticket);
    			req.setAuthCode(phoneVerifyCode);
    			req.setBizId(enterpriseMember.getMemberId());
    			req.setBizType(BizType.REFIND_PAYPASSWD.getCode());
    			// 校验短信验证码
    			boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
    			if (!messageResult) {
    				data.put("isBindPhone", isBindPhone);
    				data.put("isNameVerify", isNameVerify);
    				data.put("mobileError", "手机验证码错误！");
    				data.put("bizType", BizType.REFIND_PAYPASSWD.getCode());
    				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/get-pay-passwd", "response",
    						restP);
    			}
		    }
		}
		if ("true".equals(isNameVerify)) {
			String licenseNo = request.getParameter("licenseNo");
			if (!licenseNo.equals(enterpriseMember.getLicenseNo())) {
				data.put("isNameVerify", isNameVerify);
				data.put("isBindPhone", isBindPhone);
				data.put("licenseError", "营业执照号错误！");
				data.put("bizType", BizType.REFIND_PAYPASSWD.getCode());
				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/get-pay-passwd", "response",
						restP);
			}
		}
		if ("true".equals(isNameVerify) && "true".equals(isBindPhone)) {
			request.getSession().setAttribute(CommonConstant.REFIND_PAYPWD_TOKEN, CommonConstant.REFIND_PAYPWD_TOKEN);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/reset-pay-passwd","response",
					restP);
		}
		request.getSession().setAttribute(CommonConstant.REFIND_PAYPWD_TOKEN, CommonConstant.REFIND_PAYPWD_TOKEN);
		request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
		return new ModelAndView("redirect:/my/go-get-paypasswd-email-send.htm");
	}

	/**
	 * 重置支付密码
	 * 
	 * @param request
	 * @param resp
	 * @param model
	 * @return
	 * @throws ServiceException
	 * @throws MemberNotExistException
	 * @throws BizException
	 */
	@RequestMapping(value = "/my/reSetPayPwd.htm")
	public ModelAndView reSetLoginPwd(HttpServletRequest request,
			OperationEnvironment env) throws ServiceException, BizException, MemberNotExistException {

		RestResponse restP = new RestResponse();

		String token = (String) request.getSession().getAttribute(CommonConstant.REFIND_PAYPWD_TOKEN);
		if (!token.equals(CommonConstant.REFIND_PAYPWD_TOKEN)) {
			return new ModelAndView("redirect:/my/go-get-paypasswd-index.htm");
		}

		String payPwd = decrpPassword(request, request.getParameter("payPwd"));
		String repayPwd = decrpPassword(request, request.getParameter("repayPwd"));
		if (StringUtils.isEmpty(payPwd) || StringUtils.isEmpty(repayPwd)) {
			restP.setMessage("支付密码和重复支付密码均不能为空");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/reset-pay-passwd", "response",
					restP);
		}
		// 登陆密码和确认密码不一致
		if (!payPwd.equals(repayPwd)) {
			restP.setMessage("支付密码和确认密码不一致");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/reset-pay-passwd", "response",
					restP);
		}
		deleteMcrypt(request);
		EnterpriseMember user = getUser(request);

		try {
            PayPasswdRequest req = new PayPasswdRequest();
            req.setPassword(payPwd);
            req.setAccountId(user.getDefaultAccountId());
			req.setOperator(user.getCurrentOperatorId());
            req.setClientIp(request.getRemoteAddr());

            restP.setSuccess(false);
            restP.setMessage("设置支付密码失败");
            //1.验证旧密码是否锁住
            boolean oldPayPwdClocked = defaultPayPasswdService.isPayPwdClocked(req);
            boolean unlocked = false;
            if (oldPayPwdClocked) {
                //2.如果密码锁住，则解锁。
                unlocked = defaultPayPasswdService.resetPayPasswordLock(req);
            }
            //a.旧支付密码没有锁住，则设置新的支付密码。 b. 旧的支付密码锁住，解锁成功，则设置新的支付密码。
            if (!oldPayPwdClocked || unlocked) {
                CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
                if (commRep.isSuccess()) {
                    restP.setSuccess(true);
                    restP.setMessage("设置支付密码成功");
					request.getSession().removeAttribute(CommonConstant.REFIND_PAYPWD_TOKEN);
                }else{
                    if(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(commRep.getResponseCode())){
                        restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                    }else{
                        restP.setMessage("支付密码设置失败");
                    }
					return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/reset-pay-passwd",
							"response", restP);
                }
			}
		} catch (ServiceException e) {
			log.error(e.getMessage(), e.getCause());
			restP.setMessage(e.getMessage());
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/reset-paypasswd-result", "response",
				restP);
	}




}
