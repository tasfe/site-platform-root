/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.paypasswd;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
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
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberSetStatus;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.form.PayBackPassWordForm;
import com.netfinworks.site.web.form.PayPasswordForm;
import com.netfinworks.site.web.form.ResetPayPasswordForm;
import com.netfinworks.site.web.processor.ValidationProcessor;
import com.netfinworks.site.web.util.BankCardUtil;
import com.netfinworks.site.web.validator.SetPayPasswordValidator;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * 通用说明：支付密码control
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午10:04:25
 *
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
    
	@Resource(name = "loginPasswdService")
	private LoginPasswdService loginPasswdService;
    

    /**
	 * 设置支付密码-首页
	 * 
	 * @param request
	 * @param resp
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/go-set-pay-passwd.htm", method = RequestMethod.GET)
	public ModelAndView goSetPayPasswd(HttpServletRequest request, HttpServletResponse resp)
			throws Exception {

		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(request);

			data.put("member", user);

			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd-first", "response", restP);

		} catch (Exception e) {
			logger.error("设置支付密码首页：", e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 设置支付密码
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/do-set-paypasswd.htm", method = RequestMethod.POST)
	public ModelAndView doSetPayPasswd(HttpServletRequest request, HttpServletResponse resp, OperationEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(request);

			String loginPasswd = decrpPassword(request, request.getParameter("loginPasswd"));// 登录密码

			if (StringUtils.isEmpty(loginPasswd)) {
				restP.setMessage("登录密码不能为空！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}

			// 验证登录密码
			try {
				loginPasswdService.checkMemberLoginPwd(user.getLoginName(), loginPasswd, user.getMemberIdentity(),
						DEFAULT_SALT, env.getClientIp());
			} catch (BizException e) {
				restP.setMessage(e.getMessage());
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}

			String newPasswd = decrpPassword(request, request.getParameter("newPasswd"));// 新支付密码

			// 清空随机因子
			deleteMcrypt(request);

			if (StringUtils.isEmpty(newPasswd)) {
				restP.setMessage("支付密码不能为空！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}

			PayPasswdRequest req = new PayPasswdRequest();
			req.setPassword(newPasswd);
			req.setAccountId(user.getDefaultAccountId());
			req.setOperator(user.getOperatorId());
			req.setClientIp(env.getClientIp());

			CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
			if (!commRep.isSuccess()) {
				restP.setMessage(commRep.getResponseMessage());
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
			// 更新本地session
			user.setPaypasswdstatus(MemberPaypasswdStatus.DEFAULT);
			request.getSession().setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER,
					JsonUtils.toJson(user));

			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd-first-success", "response",
					restP);

		} catch (ServiceException e) {
			logger.error("设置支付密码：", e.getMessage());
			restP.setMessage(e.getMessage());
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		} catch (Exception e) {
			logger.error("设置支付密码：", e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 重置支付密码
	 * 知道原始密码的情况下，修改支付密码
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
    @RequestMapping(value = "/my/reset-pay-passwd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView resetPayPasswd(HttpServletRequest request, HttpServletResponse resp,
                                       ModelMap model) throws Exception {
        PersonMember user = getUser(request);
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();

        data.put("member", user);
        restP.setErrors(errorMap);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_PAYPASSWD.getUrl(),
            "response", restP);
    }

    /**
     * 修改支付密码
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/do-reset-paypasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setNewPayPasswd(HttpServletRequest request, HttpServletResponse resp,
                                        @Validated ResetPayPasswordForm form, ModelMap model,OperationEnvironment env)
                                                                                             throws Exception {
        PersonMember user = getUser(request);
    	if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.RESET_PAY_PASSWD.getMsg(), user, env));
		}
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        //        return new ModelAndView("redirect:/my/reset-pay-passwd.htm");
        String oldPasswd = decrpPassword(request, form.getOldPasswd());
        String newPasswd = decrpPassword(request, form.getNewPasswd());
        String renewPasswd = decrpPassword(request, form.getRenewPasswd());
        //清空随机因子
        deleteMcrypt(request);
        Map<String, String> errorMap = new HashMap<String, String>();

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
            data.put("member", user);
            errorMap.put("repassword_password_not_same", "repassword_password_not_same");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response", restP);
        }

        PayPasswdRequest req = new PayPasswdRequest();
        req.setOperator(user.getOperatorId());
        req.setAccountId(user.getDefaultAccountId());
        req.setOldPassword(oldPasswd);
        req.setValidateType(1);
        req.setPassword(newPasswd);
        req.setClientIp(request.getRemoteAddr());
        PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(req);

		if (!checkResult.isSuccess()) {
			if (checkResult.isLocked()) {
				if (logger.isInfoEnabled()) {
	                logger.info(LogUtil.appLog(OperateeTypeEnum.LOCKEDPAYPASS.getMsg(), user, env));
				}
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
				return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_PAYPASSWD.getUrl(), "response",
						restP);
			}

			CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
			if (commRep.isSuccess()) {
				restP.setSuccess(true);
			} else {
				restP.setSuccess(false);
				if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(commRep.getResponseCode())) {
					errorMap.put("password_equal_login_password", "password_equal_login_password");
					restP.setErrors(errorMap);
					return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_PAYPASSWD.getUrl(),
							"response", restP);
				} else {
					restP.setMessage("支付密码设置失败");
				}
			}
			return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/reset/reset-paypasswd-success", "response",
					restP);
		}
    }



    
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
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> error;
        PersonMember user = getUser(request);
        if (user == null) {
            error = new HashMap<String, String>();
            error.put("noLogin", "用户没有登录,请先登录");
            restP.setErrors(error);

        }
        data.put("mobile", user.getMobile());
        data.put("userName", user.getMemberName());
        data.put("member", user);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd",
            "response", restP);
    }

    /**
     * 个人会员设置支付密码
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
        if ((errors != null) && (errors.size() > 0)) {
            Map<String, String> errorInfo = new HashMap<String, String>();

            for (ObjectError error : errors) {
                errorInfo.put(error.getCode(), error.getDefaultMessage());
            }
            Map<String, Object> info = new HashMap<String, Object>();
            info.put("mobile", form.getMobile());
            info.put("userName", form.getUsername());
            restP.setData(info);
            restP.setErrors(errorInfo);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/set-pay-passwd",
                "response", restP);
        }
        try {
            PersonMember user = getUser(request);
            PayPasswdRequest req = new PayPasswdRequest();
            req.setPassword(form.getPassword());
            req.setAccountId(user.getDefaultAccountId());
            req.setOperator(user.getOperatorId());
            req.setClientIp(request.getRemoteAddr());
            CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
            if (commRep.isSuccess()) {
                restP.setSuccess(true);
            } else {
                restP.setSuccess(false);
                if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                    commRep.getResponseCode())) {
                    restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                } else {
                    restP.setMessage("设置支付密码失败");
                }
            }
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        } catch (ServiceException e) {
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        }
    }

    /**
     * 个人会员设置支付密码
     *
     * @param model
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/active/setpaypasswdMobile.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public @ResponseBody
    RestResponse setPayPasswordMobile(HttpServletRequest request, OperationEnvironment env)
                                                                                           throws Exception {
        String passwd = request.getParameter("password");
        String msg = request.getParameter("msg");
        String model = request.getParameter("model");
        RestResponse restP = new RestResponse();

        try {
            PersonMember user = getUser(request);

            if (StringUtils.isBlank(passwd)) {
                restP.setSuccess(false);
                restP.setMessage("支付密码为空");
                return restP;
            }
            //验证短信
            AuthCodeRequest authreq = new AuthCodeRequest();
            authreq.setMemberId(user.getMemberId());
            authreq.setMobile(user.getMobile());
            authreq.setAuthCode(msg);
            authreq.setBizId(user.getMemberId());
            authreq.setBizType(BizType.SET_PAYPASSWD.getCode());
            String ticket = defaultUesService.encryptData(user.getMobile());
            authreq.setMobileTicket(ticket);
            if (!defaultSmsService.verifyMobileAuthCode(authreq, env)) {
                restP.setSuccess(false);
                restP.setMessage("验证码错误");
                return restP;
            }
            if (model.equals(MemberSetStatus.ACTIVE.getCode())) {
                user.setPaypasswd(passwd);
                if (defaultMemberService.activatePersonalMemberInfo(user, env)) {
                    restP.setSuccess(true);
                    //更新session
                    updateSessionObject(request);
                } else {
                    restP.setSuccess(false);
                    log.error("用户激活失败:{}", user);
                }
            } else {
                //设置支付密码
                PayPasswdRequest req = new PayPasswdRequest();
                req.setPassword(passwd);
                req.setAccountId(user.getDefaultAccountId());
                req.setOperator(user.getOperatorId());
                req.setClientIp(request.getRemoteAddr());
                CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
                if (commRep.isSuccess()) {
                    restP.setSuccess(true);
                } else {
                    restP.setSuccess(false);
                    if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                        commRep.getResponseCode())) {
                        restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                    } else {
                        restP.setMessage("支付密码设置失败");
                    }
                }
            }
            return restP;
        } catch (ServiceException e) {
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage("系统繁忙，请等待");
            return restP;
        }
    }

//    /**
//     * 重设支付密码 zhangyun.m modify 20140619
//     * @param request
//     * @param form
//     * @return
//     * @throws BindException
//     */
//    @RequestMapping(value = "/my/set-back-paypasswd.htm", method = { RequestMethod.POST,
//            RequestMethod.GET })
//    public ModelAndView setBackPayPassWord(HttpServletRequest request, HttpServletResponse resp,
//                                           @Validated ResetPayPasswordForm form, ModelMap model)
//                                                                                                throws Exception {
//        RestResponse restP = new RestResponse();
//        PersonMember user = getUser(request);
//        String token = request.getParameter("token");
//        String errorUrl = "redirect:/my/resetPayPassWord.htm?token=" + token;
//
//        String password = decrpPassword(request, form.getNewPasswd());
//        String repassword = decrpPassword(request, form.getRenewPasswd());
//
//        Map<String, Object> data = new HashMap<String, Object>();
//        Map<String, String> errorMap = new HashMap<String, String>();
//
//        //清空随机因子
//        deleteMcrypt(request);
//
//        data.put("member", user);
//        restP.setData(data);
//        if (password == null || repassword == null) {
//            return new ModelAndView(errorUrl, "error", ERROR_NOTRIGHT);
//        } else if (password.length() < 6 || password.length() > 16) {
//            return new ModelAndView(errorUrl, "error", ERROR_PARTEN);
//        } else if (!password.equals(repassword)) {
//            return new ModelAndView(errorUrl, "error", ERROR_NOT_SAME);
//        }
//
//        try {
//            PayPasswdRequest req = new PayPasswdRequest();
//            req.setPassword(password);
//            req.setMemberId(user.getMemberId());
//            req.setOperator(user.getOperatorId());
//            req.setAccountId(user.getDefaultAccountId());
//            req.setClientIp(request.getRemoteAddr());
//            defaultPayPasswdService.resetPayPasswordLock(req);
//            CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
//            if (commRep.isSuccess()) {
//                restP.setSuccess(true);
//                payPassWordCacheService.clear(token);
//                return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/safetyPay_find6",
//                    "response", restP);
//            } else {
//                restP.setSuccess(false);
//                if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
//                    commRep.getResponseCode())) {
//                    restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
//                    return new ModelAndView(errorUrl, "error", ERROR_PAYPWD_EQUAL_LOGINPWD);
//                } else {
//                    restP.setMessage("支付密码设置失败");
//                }
//                return new ModelAndView(errorUrl, "response", restP);
//            }
//        } catch (ServiceException e) {
//            e.printStackTrace();
//            log.error(e.getMessage(), e.getCause());
//            restP.setSuccess(false);
//            restP.setMessage(e.getMessage());
//            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
//                "response", restP);
//        }
//    }

    /**
     * 个人会员激活
     *
     * @param model
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/active/active-person-member.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView activePersonMember(HttpServletRequest request, HttpServletResponse resp,
                                           @Validated PayPasswordForm form, ModelMap model,
                                           OperationEnvironment env) throws Exception {

        RestResponse restP = new RestResponse();
        Map<String, String> errorMap = new HashMap<String, String>();
        Map<String, Object> info = new HashMap<String, Object>();
        logger.info("个人会员激活,表单信息:" + form.toString());
        String backUrl = request.getParameter("backUrl");
        //查询session用户
        PersonMember user = getUser(request);
        
        if (user.getPaypasswdstatus().getCode() == MemberPaypasswdStatus.DEFAULT.getCode()) {
                //已经激活，直接调整到首页
                return new ModelAndView("redirect:/index.htm");
            }

        String password = decrpPassword(request, form.getNewPasswd());
        String repassword = decrpPassword(request, form.getRenewPasswd());

        Map<String, Object> data = new HashMap<String, Object>();

        //清空随机因子
        deleteMcrypt(request);

        data.put("member", user);
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

            user.setPaypasswd(password);
            if (StringUtils.isNotBlank(form.getEmail())) {
                user.setEmail(form.getEmail());
            }
            if (form.getModel().equals(MemberSetStatus.ACTIVE.getCode())) {
                if (defaultMemberService.activatePersonalMemberInfo(user, env)) {
                    restP.setSuccess(true);
                    updateSessionObject(request);
                } else {
                    restP.setSuccess(false);
                    log.error("用户激活失败:{}", user);
                    restP.setMessage("设置支付密码失败");
                    return new ModelAndView(ResourceInfo.ERROR.getUrl());
                }
            } else {
                PayPasswdRequest payPasswdRequest = new PayPasswdRequest();
                payPasswdRequest.setPassword(password);
                payPasswdRequest.setOperator(user.getOperatorId());
                payPasswdRequest.setAccountId(user.getDefaultAccountId());
                CommResponse commRep = defaultPayPasswdService.setPayPassword(payPasswdRequest);

                if (commRep.isSuccess()) {
                    restP.setSuccess(true);
                    //更新session
                    updateSessionObject(request);
                    return new ModelAndView(CommonConstant.URL_PREFIX
                                            + "/paypasswd/set-pay-passwd-result", "response", restP);
                } else {
                    restP.setSuccess(false);
                    log.error("用户激活支付密码失败:{}", user);
                    if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(
                        commRep.getResponseCode())) {
                        restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                        errorMap.put("password_equal_login_password",
                            "password_equal_login_password");
                        restP.setErrors(errorMap);
                    } else {
                        restP.setMessage("设置支付密码失败");
                    }
                    return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember",
                        "response", restP);
                }
            }
            info.put("member", user);
            restP.setData(info);
            if (StringUtil.isNotBlank(backUrl)) {
                logger.info("用户激活：返回调用页面：{}" + backUrl);
                return new ModelAndView("redirect:" + backUrl);
            }
            return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/setpaypasswd-success",
                "response", restP);
        } catch (ServiceException e) {
            log.error("用户激活失败,失败信息：{}", e.getMessage(), e);
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return new ModelAndView(ResourceInfo.ERROR.getUrl());
        }
    }


    
    
    
    /**
     * 支付密码找回-人工
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
    @RequestMapping(value = "/my/go-get-paypasswd-manual.htm", method = RequestMethod.GET)
    public ModelAndView getPayPasswdManual(HttpServletRequest request, HttpServletResponse resp,
                                           ModelMap model) throws ServiceException,
                                                          MemberNotExistException {
        PersonMember user = getUser(request);
        RestResponse restP = new RestResponse();
        AuthInfoRequest info = new AuthInfoRequest();
        info.setMemberId(user.getMemberId());
        info.setAuthType(AuthType.RESET_PAYPWD);
        info.setOperator(user.getOperatorId());

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
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> errorMap = new HashMap<String, String>();
        log.info("图片路径：" + frontImagUrl);
        /**
         * 判断用户是否登录
         */
        PersonMember user = getUser(request);
        data.put("member", user);
        /**
         * 判断用户是否已经提交申请
         */
        AuthInfoRequest info = new AuthInfoRequest();
        info.setMemberId(user.getMemberId());
        info.setAuthType(AuthType.RESET_PAYPWD);
        info.setOperator(user.getOperatorId());
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
                //authRequest.setCertType(CertType.ID_CARD.getMessage());
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
                    restP.setSuccess(true);
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

   
}
