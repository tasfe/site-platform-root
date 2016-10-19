/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.paypasswd;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSON;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberSetStatus;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.PayBackPassWordForm;
import com.netfinworks.site.web.action.form.PayPasswordForm;
import com.netfinworks.site.web.action.form.ResetPayPasswordForm;
import com.netfinworks.site.web.action.processor.ValidationProcessor;
import com.netfinworks.site.web.action.validator.SetPayPasswordValidator;
import com.netfinworks.site.web.util.BankCardUtil;
import com.netfinworks.site.web.util.LogUtil;

/**
 *
 * <p>支付密码controller</p>
 * @author Guan Xiaoxu
 * @version $Id: PaypasswdAction.java, v 0.1 2013-12-6 下午4:55:43 Guanxiaoxu Exp $
 */
@Controller
public class PaypasswdAction extends BaseAction {

    protected Logger                log                = LoggerFactory.getLogger(getClass());

    public final static String      CAPTCHA_SET_PAYPWD = "_SET_PAYPWD";
    @Resource(name = "defaultMemberService")
    private DefaultMemberService    defaultMemberService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

    @Resource
    private ValidationProcessor     validationProcessor;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;
    @Resource(name = "webResource")
    private WebDynamicResource      webResource;

    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultCertService")
    private DefaultCertService      defaultCertService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

    /**
     * 支付密码找回首页
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/setpaypasswd-index.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setPayPasswdIndex(HttpServletRequest request, HttpServletResponse resp,
                                          ModelMap model) {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> error;
        EnterpriseMember user = getUser(request);
        if (user == null) {
            error = new HashMap<String, String>();
            error.put("noLogin", "用户没有登录,请先登录");
            restP.setErrors(error);

        }
        data.put("mobile", user.getMobile());
        data.put("userName", user.getMemberName());
        data.put("email", user.getEmail());
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd",
            "response", restP);
    }

    /**
     * 企业会员设置支付密码
     *
     * @param model
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/setpaypasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setPayPassword(HttpServletRequest request, @Validated PayPasswordForm form)
                                                                                                   throws Exception {

        RestResponse restP = new RestResponse();

        SetPayPasswordValidator validator = new SetPayPasswordValidator(request);
        List<ObjectError> errors = validationProcessor.validateForm(form, validator);
        Map<String, String> errorMap = new HashMap<String, String>();
        if ((errors != null) && (errors.size() > 0)) {
            Map<String, String> errorInfo = new HashMap<String, String>();

            for (ObjectError error : errors) {
                errorInfo.put(error.getCode(), error.getDefaultMessage());
            }
            Map<String, Object> info = initOcx();
            //            info.put("mobile", form.getMobile());
            //            info.put("userName", form.getUsername());
            info.put("email", form.getEmail());
            restP.setData(info);
            restP.setErrors(errorInfo);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd",
                "response", restP);
        }
        try {
            EnterpriseMember user = getUser(request);
            PayPasswdRequest req = new PayPasswdRequest();
            req.setPassword(form.getPassword());
            req.setAccountId(user.getDefaultAccountId());
			req.setOperator(user.getCurrentOperatorId());
            req.setClientIp(request.getRemoteAddr());
            CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
            if (commRep.isSuccess()) {
                restP.setSuccess(true);
                restP.setMessage("设置支付密码成功");
            } else {
                restP.setSuccess(false);
                if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                    commRep.getResponseCode())) {
                    restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                    errorMap.put("password_equal_login_password", "password_equal_login_password");
                    restP.setErrors(errorMap);
                } else {
                    restP.setMessage("支付密码设置失败");
                }
            }
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/result", "response",
                restP);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        }
    }

    /**
     * 重设支付密码 modify by zhangyun.m 20140619
     * @param request
     * @param form
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/set-back-paypasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setBackPayPassWord(HttpServletRequest request, HttpServletResponse resp,
                                           @Validated ResetPayPasswordForm form, ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        EnterpriseMember user = getUser(request);
        String token = request.getParameter("token");
        String errorUrl = "redirect:/my/resetPayPassWord.htm?token=" + token;

        String password = decrpPassword(request, form.getNewPasswd());
        String repassword = decrpPassword(request, form.getRenewPasswd());

        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();

        //清空随机因子
        deleteMcrypt(request);

        data.put("member", user);
        restP.setData(data);
        if ((password == null) || (repassword ==null)) {
            return new ModelAndView(errorUrl, "error", ERROR_NOTRIGHT);
		} else if ((password.length() < 6) || (password.length() > 32)) {
            return new ModelAndView(errorUrl, "error", ERROR_PARTEN);
        } else if (!password.equals(repassword)) {
            return new ModelAndView(errorUrl, "error", ERROR_NOT_SAME);
        }
        
        try {
            PayPasswdRequest req = new PayPasswdRequest();
            req.setPassword(password);
            req.setMemberId(user.getMemberId());
			req.setOperator(user.getCurrentOperatorId());
            req.setAccountId(user.getDefaultAccountId());
            req.setClientIp(request.getRemoteAddr());
            defaultPayPasswdService.resetPayPasswordLock(req);
            CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
            if (commRep.isSuccess()) {
                restP.setSuccess(true);
                payPassWordCacheService.clear(token);
            } else {
                restP.setSuccess(false);
                if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                    commRep.getResponseCode())) {
                    restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                    restP.setErrors(errorMap);
                    return new ModelAndView(errorUrl, "error", ERROR_PAYPWD_EQUAL_LOGINPWD);
                } else {
                    restP.setMessage("支付密码设置失败");
                    return new ModelAndView(errorUrl);
                }
            }
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        }
    }

    /**
     * 企业会员激活
     *
     * @param model
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/active/active-member.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView activePersonMember(HttpServletRequest request,
                                           @Validated PayPasswordForm form, OperationEnvironment env)
                                                                                                     throws Exception {

        RestResponse restP = new RestResponse();
        Map<String, String> errorMap = new HashMap<String, String>();
        //查询session用户
        EnterpriseMember user = getUser(request);
        
        if (user.getPaypasswdstatus().getCode() == MemberPaypasswdStatus.DEFAULT.getCode()) {
            //已经激活，直接调整到首页
            return new ModelAndView("redirect:/index.htm");
        }

        String password = decrpPassword(request, form.getNewPasswd());
        String repassword = decrpPassword(request, form.getRenewPasswd());

        Map<String, Object> data = initOcx();

        //清空随机因子
        deleteMcrypt(request);

        data.put("member", user);
        data.put("merState", form.getMerState());
        restP.setData(data);
        if (password == null) {
            errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember", "response",
                restP);
		} else if ((password.length() < 6) || (password.length() > 32)) {
            restP.setErrors(errorMap);
            errorMap.put("error_passwd_parten", "error_passwd_parten");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember", "response",
                restP);
        } else if (!password.equals(repassword)) {
            errorMap.put("repassword_password_not_same", "repassword_password_not_same");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember", "response",
                restP);
        }

        try {

            //激活会员
            EnterpriseMember enterprise = new EnterpriseMember();
            enterprise.setMemberId(user.getMemberId());
            enterprise.setPaypasswd(password);
            if (MemberSetStatus.ACTIVE.getCode().equals(form.getMerState())) {
                if (defaultMemberService.activateCompanyMember(enterprise, env)) {
                    restP.setSuccess(true);
                    updateSessionObject(request);
                } else {
                    logger.error("企业会员[" + enterprise.getMemberId() + "]激活失败！");
                    restP.setSuccess(false);
                    restP.setMessage("设置支付密码失败");
                }
            } else if (MemberSetStatus.SET_PASSWD.getCode().equals(form.getMerState())) {
                PayPasswdRequest payPasswdRequest = new PayPasswdRequest();
                payPasswdRequest.setPassword(password);
				payPasswdRequest.setOperator(user.getCurrentOperatorId());
                payPasswdRequest.setAccountId(user.getDefaultAccountId());
                CommResponse commRep = defaultPayPasswdService.setPayPassword(payPasswdRequest);
                if (commRep.isSuccess()) {
                    restP.setSuccess(true);
                    updateSessionObject(request);
                    return new ModelAndView(CommonConstant.URL_PREFIX
                                            + "/paypasswd/set-pay-passwd-result", "response", restP);
                } else {
                    restP.setSuccess(false);
                    if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                        commRep.getResponseCode())) {
                        restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                        errorMap.put("password_equal_login_password",
                            "password_equal_login_password");
                        restP.setErrors(errorMap);
                        return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember",
                            "response", restP);
                    } else {
                        restP.setMessage("支付密码设置失败");
                    }
                    log.error("用户激活支付密码失败:{}", user);
                }
            }
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        }
    }

    /**
     * 发送验证码
     *
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/active/sendMessage.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public @ResponseBody
    RestResponse sendMessage(HttpServletRequest request, OperationEnvironment env) {

        RestResponse restP = new RestResponse();
        try {
            Map<String, String> error = new HashMap<String, String>();
            EnterpriseMember user = getUser(request);
            if (user == null) {
                logger.error("用户没有登录,请先登录");
                error = new HashMap<String, String>();
                error.put("noLogin", "用户没有登录,请先登录");
                restP.setErrors(error);
                restP.setSuccess(false);
                return restP;
            }
            //封装发送短信请求
            AuthCodeRequest req = new AuthCodeRequest();
            String ticket = defaultUesService.encryptData(user.getMobile());
            req.setMobileTicket(ticket);
            req.setValidity(CommonConstant.VALIDITY);
            req.setBizId(user.getMemberId());
            req.setMemberId(user.getMemberId());
            //发送短信
            if (defaultSmsService.sendMessage(req, env)) {
                restP.setSuccess(true);
                return restP;
            } else {
                log.error("发送短信失败");
                restP.setSuccess(false);
                return restP;
            }
        } catch (Exception e) {
            log.error("发送短信异常:" + e.getMessage(), e.getCause());
            restP.setSuccess(false);
            return restP;
        }
    }

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
    public ModelAndView getPayPasswdIndex(HttpServletRequest request, HttpServletResponse resp,
                                          ModelMap model) throws ServiceException {
		EnterpriseMember enterpriseMember = getUser(request);
		RestResponse restP = new RestResponse();
		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(enterpriseMember.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		boolean isBindPhone = false;
		boolean isBindEmail = false;
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				isBindPhone = true;
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				isBindEmail = true;
			}
		}
		boolean isNameVerify = false;
		AuthInfoRequest info = new AuthInfoRequest();
		info.setMemberId(enterpriseMember.getMemberId());
		info.setAuthType(AuthType.REAL_NAME);
		info.setOperator(enterpriseMember.getOperatorId());
		info.setMessage("merchant");
		AuthResultStatus state = defaultCertService.queryAuthType(info);
		if (state.getCode().equals("PASS")) {
			isNameVerify = true;
		}

		if ((isNameVerify != isBindPhone) && !(isNameVerify && isBindPhone)) {
			if (!isBindEmail) {
				restP.setMessage("请先绑定邮箱！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
		}

		if (!isNameVerify && !isBindPhone) {
			if (!isBindEmail) {
				restP.setMessage("请先绑定邮箱！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
			request.getSession().setAttribute(CommonConstant.REFIND_PAYPWD_TOKEN, CommonConstant.REFIND_PAYPWD_TOKEN);
			request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
			return new ModelAndView("redirect:/my/go-get-paypasswd-email-send.htm");
		}

		Map<String, Object> data = initOcx();
		data.put("isBindPhone", isBindPhone);
		data.put("isNameVerify", isNameVerify);
		data.put("bizType", BizType.REFIND_PAYPASSWD.getCode());
		
		// 硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            data.put("isCertActive", "yes");
        } else {
            data.put("isCertActive", "no");
        }
        
		restP.setData(data);

		return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/get-pay-passwd", "response", restP);
    }

    /**
     * 手机验证码+密保
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/go-get-paypasswd-phonePP.htm", method = RequestMethod.GET)
    public String getPayPasswdPhoneAndProtected(HttpServletRequest request, ModelMap model) {
        return CommonConstant.URL_PREFIX + "/paypasswd/getback/phone-and-protected";
    }

    /**
     * 支付密码找回--验证手机验证码+密保
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/confirm-phone-protected.htm", method = RequestMethod.POST)
    public String confirmPhoneAndProtected(HttpServletRequest request, ModelMap model) {
        return CommonConstant.URL_PREFIX + "/paypasswd/getback/set-new-paypasswd";
    }

    /**
     * 手机验证码+证件号码
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/go-get-paypasswd-phoneCard.htm", method = RequestMethod.GET)
    public String getPayPasswdPhoneCard(HttpServletRequest request, ModelMap model) {
        return CommonConstant.URL_PREFIX + "/paypasswd/getback/phone-and-card";
    }

    /**
     * 支付密码找回--验证手机验证码+证件号码
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/confirm-phone-card.htm", method = RequestMethod.POST)
    public String confirmPhoneAndCard(HttpServletRequest request, ModelMap model) {
        return CommonConstant.URL_PREFIX + "/paypasswd/getback/set-new-paypasswd";
    }

    /**
     * go支付密码找回-人工
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     * @throws MemberNotExistException
     */
    @RequestMapping(value = "/my/go-get-paypasswd-manual.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView getPayPasswdManual(HttpServletRequest request, HttpServletResponse resp,
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
        AuthResultStatus state = defaultCertService.queryAuthType(info);
        data.put("state", state.getCode());
        data.put("email", user.getEmail());
        data.put("member", user);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/getback/getbymanual",
            "response", restP);
    }

    /**
     * 支付密码找回-人工
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/confirm-manual.htm", method = RequestMethod.POST)
    public ModelAndView doPayBackPassWord(HttpServletRequest request, HttpServletResponse resp,
                                          String frontImagUrl, String backImagUrl,
                                          @Valid PayBackPassWordForm form, BindingResult result) {
        log.info("图片路径：" + frontImagUrl);
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> errorMap = new HashMap<String, String>();
        /**
         * 判断用户是否登录
         */
        EnterpriseMember user = getUser(request);
        checkUser(user, errorMap, restP);
        /**
         * 判断用户是否已经提交申请
         */
        AuthInfoRequest info = new AuthInfoRequest();
        info.setMemberId(user.getMemberId());
        info.setAuthType(AuthType.RESET_PAYPWD);
        info.setOperator(user.getOperatorId());
		info.setMessage("merchant");
        AuthResultStatus state = null;
        try {
            state = defaultCertService.queryAuthType(info);
            log.info("用户申请状态：" + state.getCode());
        } catch (ServiceException e) {
            log.info("判断用户是否已经提交申请时异常！");
            e.printStackTrace();
        }
        if ("init".equals(state.getCode())) {
            restP.setSuccess(false);
            restP.setMessage("1");
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + "/paypasswd/getback/set-new-passwd-success", "response",
                restP);
        } else {
            /**
             * 申请表信息校验
             */
            Map<String, String> errors = new HashMap<String, String>();
            boolean hasError = false;
            data.put("member", user);
            if (!"2".equals(form.getTimeType())) {
                if ("".equals(request.getParameter("overDate"))
                    || (null == request.getParameter("overDate"))) {
                    logger.error("[用户身份证有效期选择有误]");
                    errors.put("date_format_is_not_right", "date_format_is_not_right");
                    hasError = true;
                } else if ((null == form.getEmail()) || "".equals(form.getEmail())) {
                    logger.error("邮箱不能为空.");
                    errors.put("email_is_not_empty", "email_is_not_empty");
                    hasError = true;
                }
            } else {
                if ((null == form.getEmail()) || "".equals(form.getEmail())) {
                    errors.put("email_is_not_empty", "email_is_not_empty");
                    hasError = true;
                }
            }

            /**
             * 信息填写有误返回申请表，显示错误信息
             */
            if (result.hasErrors() || hasError) {
                if (result.hasErrors()) {
                    errors.putAll(initError(result.getAllErrors()));
                }
                Calendar calendar = Calendar.getInstance();
                data.put("year", calendar.get(Calendar.YEAR));
                data.put("frontUrl", "/site/getFile.htm?filePath=" + frontImagUrl);
                data.put("backUrl", "/site/getFile.htm?filePath=" + backImagUrl);
                restP.setData(data);
                restP.setErrors(errors);
                return new ModelAndView(CommonConstant.URL_PREFIX
                                        + "/paypasswd/getback/getbymanual", "response", restP);
            }

            /**
             * 申请表信息校验通过，提交申请
             */
            try {
                String keyStr = BankCardUtil.getUuid() + user.getMemberId();
                AuthInfoRequest authRequest = new AuthInfoRequest();
                authRequest.setAuthName(request.getParameter("authName"));
                authRequest.setAuthType(AuthType.RESET_PAYPWD);
                authRequest.setMemberId(user.getMemberId());
                authRequest.setOperator(user.getOperatorId());
                Map<String, Object> emap = new HashMap<String, Object>();
                emap.put(CommonConstant.WITHDRAWAL_CARDID, form.getWithdrawalCardId());
                emap.put(CommonConstant.RECHARGE_CARDID, form.getRechargeCardId());
                emap.put(CommonConstant.NOTIFY_EMAIL, request.getParameter("email"));
                emap.put(CommonConstant.NOTIFY_MESSAGE,
                    user.getMemberName() + "," + webResource.getResetPaypassWordUrl() + "?token="
                            + keyStr);
                String jsn = JSON.toJSONString(emap);
                authRequest.setMessage(jsn);
                String ordNo = defaultPayPasswdService.payBackPassWord(frontImagUrl, backImagUrl,
                    authRequest);
                String valueStr = user.getMemberId() + "," + user.getOperatorId() + "," + ordNo;
                // 调用统一缓存
                payPassWordCacheService.put(keyStr, valueStr);
                restP.setData(data);
                if (!"".equals(ordNo)) {
                    restP.setSuccess(true);
                    return new ModelAndView(CommonConstant.URL_PREFIX
                                            + "/paypasswd/getback/set-new-passwd-success",
                        "response", restP);
                } else {
                    restP.setSuccess(false);
                    return new ModelAndView(CommonConstant.URL_PREFIX
                                            + "/paypasswd/getback/set-new-passwd-success",
                        "response", restP);
                }
            } catch (Exception e1) {
                errorMap = new HashMap<String, String>();
                restP.setSuccess(false);
                restP.setData(data);
                errorMap.put("error", e1.getMessage());
                restP.setErrors(errorMap);
                return new ModelAndView(CommonConstant.URL_PREFIX
                                        + "/paypasswd/getback/set-new-passwd-success", "response",
                    restP);
            }
        }
    }

    /**
     * 设置新支付密码
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/send-email.htm", method = RequestMethod.POST)
    public String sendEmail(HttpServletRequest request, ModelMap model) {
        return CommonConstant.URL_PREFIX + "/paypasswd/getback/go-to-email";
    }

    /**
     * 重置支付密码
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/reset-pay-passwd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView resetPayPasswd(HttpServletRequest request, HttpServletResponse resp,
                                       ModelMap model) throws Exception {
        EnterpriseMember user = getUser(request);
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();

        data.put("member", user);
        // 硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            data.put("isCertActive", "yes");
        } else {
            data.put("isCertActive", "no");
        }
        restP.setErrors(errorMap);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_PAYPASSWD.getUrl(),
            "response", restP);
    }

    /**
     * 重置新支付密码
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/do-reset-paypasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setNewPayPasswd(HttpServletRequest request, HttpServletResponse resp,
                                        @Validated ResetPayPasswordForm form, ModelMap model, OperationEnvironment env)
                                                                                             throws Exception {
        EnterpriseMember user = getUser(request);
        RestResponse restP = new RestResponse();
        
        logger.info(LogUtil.generateMsg(OperateTypeEnum.MOD_PAY_PWD, user, env, StringUtils.EMPTY));

        String oldPasswd = decrpPassword(request, form.getOldPasswd());
        String newPasswd = decrpPassword(request, form.getNewPasswd());
        String renewPasswd = decrpPassword(request, form.getRenewPasswd());
        //清空随机因子
        deleteMcrypt(request);

        Map<String, String> errorMap = new HashMap<String, String>();
        Map<String, Object> data = initOcx();

        data.put("member", user);
        restP.setData(data);
        if (oldPasswd == null) {
            errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response", restP);
        } else if (newPasswd == null) {
            errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response", restP);
        } else if (renewPasswd == null) {
            errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response", restP);
		} else if ((newPasswd.length() < 6) || (newPasswd.length() > 32)) {
            errorMap.put("error_passwd_parten", "error_passwd_parten");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response", restP);
        } else if (!newPasswd.equals(renewPasswd)) {
            errorMap.put("repassword_password_not_same", "repassword_password_not_same");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response", restP);
        }

        PayPasswdRequest req = new PayPasswdRequest();
		req.setOperator(user.getCurrentOperatorId());
        req.setAccountId(user.getDefaultAccountId());
        req.setOldPassword(oldPasswd);
        req.setValidateType(1);
        req.setPassword(newPasswd);
        req.setClientIp(request.getRemoteAddr());
        PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(req);

		if (!checkResult.isSuccess()) {
			if (checkResult.isLocked()) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                        DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
				errorMap.put("pay_password_is_locked", "pay_password_is_locked");
				restP.setErrors(errorMap);
				return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response",
						restP);
			}
			errorMap.put("pay_password_is_error", "pay_password_is_error");
			restP.setErrors(errorMap);
			data.put("remainNum", String.valueOf(checkResult.getRemainNum()));
			return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response",
					restP);
		} else {
			if (newPasswd.equals(oldPasswd)) {// 新支付密码不能和旧支付密码相同
                errorMap.put("newpaypwd_equal_oldpaypwd", "newpaypwd_equal_oldpaypwd");
                restP.setErrors(errorMap);
                return new ModelAndView(CommonConstant.URL_PREFIX
                                        + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response", restP);
            }
            CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
            if (commRep.isSuccess()) {
                restP.setSuccess(true);
            } else {
                if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                    commRep.getResponseCode())) {
                    errorMap.put("password_equal_login_password", "password_equal_login_password");
                    restP.setErrors(errorMap);
					return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_PAYPASSWD.getUrl(),
							"response", restP);
                } else {
                    restP.setMessage("支付密码设置失败");
                }
            }
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
		}
    }

    /** modifyby zhangyun.m 20140619
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/resetPayPassWord.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView getSetPayPassWordUrl(HttpServletRequest request) throws Exception {
        String key = request.getParameter("token");
        RestResponse restP = new RestResponse();
        String info = payPassWordCacheService.load(key);
        restP.setMessage(key);
        String error = request.getParameter("error");
        Map<String, String> errorMap = new HashMap<String, String>();
        if (error != null) {
            if (ERROR_LOCKED.equals(error)) {
                errorMap.put("error_passwd_is_locked", "error_passwd_is_locked");
            } else if (ERROR_PARTEN.equals(error)) {
                errorMap.put("error_passwd_parten", "error_passwd_parten");
            } else if (ERROR_SAME.equals(error)) {
                errorMap.put("error_passwd_same", "error_passwd_same");
            } else if (ERROR_NOT_SAME.equals(error)) {
                errorMap.put("repassword_password_not_same", "repassword_password_not_same");
            }else if(ERROR_PAYPWD_EQUAL_LOGINPWD.equals(error)){
                errorMap.put("password_equal_login_password", "password_equal_login_password");
            } else {
                errorMap.put("error_passwd_not_right", "error_passwd_not_right");
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        restP.setErrors(errorMap);
        EnterpriseMember user = getUser(request);
        data.put("member", user);
        restP.setData(data);
        if (user == null) {
            Map<String, String> err = new HashMap<String, String>();
            err.put("noLogin", "用户没有登录,请先登录");
            restP.setErrors(err);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + "/paypasswd/getback/set-back-paypasswd", "response", restP);
        } else {
            if ((null != info) && !"".equals(info)) {
                String[] strArry = info.split(",");
                String memId = strArry[0];
                if (memId.equals(user.getMemberId())) {
                    return new ModelAndView(CommonConstant.URL_PREFIX
                                            + "/paypasswd/getback/set-back-paypasswd", "response",
                        restP);
                } else {
                    restP.setSuccess(true);
                    return new ModelAndView(CommonConstant.URL_PREFIX
                                            + "/paypasswd/getback/set-new-passwd-sessionDis",
                        "response", restP);
                }
            }
            logger.info("[找回支付密码连接失效]");
            restP.setSuccess(false);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + "/paypasswd/getback/set-new-passwd-sessionDis", "response",
                restP);
        }
    }

}
