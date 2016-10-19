package com.netfinworks.site.web.action.email;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.BankCardUtil;
import com.netfinworks.site.web.util.LogUtil;

/**
 * <p>
 * 绑定邮箱
 * </p>
 * 
 * @author liangzhizhuang.m
 * @version $Id: EmailSetAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m Exp $
 */
@Controller
public class EmailSetAction extends BaseAction {

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	protected static final Logger logger = LoggerFactory.getLogger(EmailSetAction.class);

	/**
	 * 跳至绑定邮箱页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-set-email.htm", method = RequestMethod.GET)
	public ModelAndView goSetEmail(HttpServletRequest request, HttpServletResponse resP) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		data.put("bizType", BizType.SET_EMAIL.getCode());

		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/email/set-email", "response", restP);
	}

	/**
	 * 跳至邮箱地址输入页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-set-email-input.htm", method = RequestMethod.POST)
	public ModelAndView goSetEmailInput(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String bizType = request.getParameter("bizType");
		String isCertActive = request.getParameter("isCertActive");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		data.put("bizType", bizType);
		data.put("isCertActive", isCertActive);
		restP.setData(data);
		try {
			String password = decrpPassword(request, request.getParameter("password"));
			deleteMcrypt(request);
			// 校验加盐支付密码
			EnterpriseMember user = getUser(request);
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getCurrentOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(password);
			payPasswsReq.setValidateType(1);
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (!checkResult.isSuccess()) {
				int remainNum = checkResult.getRemainNum();
				Map<String, String> errors = new HashMap<String, String>();
				errors.put("remainNum", remainNum + "");
				if (checkResult.isLocked()) {
					errors.put("error_passwd_is_locked", "error_passwd_is_locked");
                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
				} else {
					errors.put("error_passwd_not_right", "error_passwd_not_right");
				}
				restP.setErrors(errors);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/email/set-email", "response", restP);
			}
			request.getSession().setAttribute(CommonConstant.SET_EMAIL_TOKEN, CommonConstant.SET_EMAIL_TOKEN);
			request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
		} catch (Exception e) {
			logger.error("对不起，支付密码验证失败：{}", e);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/email/set-email", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/email/set-email-input", "response", restP);
	}

	/**
	 * 跳至绑定邮箱激活页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-set-email-active.htm", method = RequestMethod.POST)
	public ModelAndView goSetEmailActive(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String email = request.getParameter("email");
		String bizType = request.getParameter("bizType");
		RestResponse restP = new RestResponse();
		if (!CommonConstant.REFRESH_TOKEN.equals(request.getSession().getAttribute(CommonConstant.REFRESH_TOKEN))) {
			return new ModelAndView("redirect:/my/go-set-email.htm");
		}
		Map<String, Object> data = initOcx();
		data.put("bizType", bizType);
		restP.setData(data);
		try {
			data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
			if (sendMail(request, env)) {
				data.put("email", email);
				request.getSession().removeAttribute(CommonConstant.REFRESH_TOKEN);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/email/set-email-active", "response", restP);
			}
		} catch (Exception e) {
			logger.error("对不起，邮件发送失败：{}", e);
			restP.setMessage("邮件发送失败!");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/email/set-email-input", "response", restP);
	}

	/**
	 * 绑定邮箱
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/set-email.htm", method = RequestMethod.GET)
	public ModelAndView setEmail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String emailToken = (String) request.getSession().getAttribute(CommonConstant.SET_EMAIL_TOKEN);
		if (!emailToken.equals(CommonConstant.SET_EMAIL_TOKEN)) {
			return new ModelAndView("redirect:/my/go-set-email.htm");
		}
		String token = request.getParameter("token");
		RestResponse restP = new RestResponse();
		Map<String, Object> data =initOcx();
		restP.setData(data);
		try {
			EnterpriseMember user = getUser(request);
			String email = payPassWordCacheService.load(token + user.getMemberId());
			if (!StringUtils.isEmpty(email)) {
				AuthVerifyInfo info = new AuthVerifyInfo();
				info.setMemberId(user.getMemberId());
				info.setVerifyType(VerifyType.EMAIL.getCode());
				info.setVerifyEntity(email);
				ResponseCode verifyResult = authVerifyService.createVerify(info, env);
				if (ResponseCode.SUCCESS == verifyResult) {
				    logger.info(LogUtil.generateMsg(OperateTypeEnum.BIND_MAIL, user, env, email));
					payPassWordCacheService.clear(token + user.getMemberId());
					restP.setSuccess(true);
					restP.setMessage("set_email_sucess");
					updateSessionObject(request);
					AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.EMAIL, env);
					data.put("email", verifyInfo.getVerifyEntity());
				} else if (ResponseCode.DUPLICATE_VERIFY == verifyResult) {
					logger.error("会员认证信息重复!");
				} else {
					logger.error("set_email_fail");
				}
				data.put("email", email);
			} else {
				logger.error("verify_email_timeout");
			}
		} catch (Exception e) {
			logger.error("绑定邮箱错误：{}", e);
		}
		request.getSession().removeAttribute(CommonConstant.SET_EMAIL_TOKEN);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/email/set-result", "response", restP);
	}

	/**
	 * 发送邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	private boolean sendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		EnterpriseMember user = getUser(request);
		String bizType = request.getParameter("bizType");
		String token = BankCardUtil.getUuid();
		String email = request.getParameter("email");
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/my/set-email.htm?token=" + token;
		logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
		Map<String, Object> objParams = new HashMap<String, Object>();
		objParams.put("userName", StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
		objParams.put("activeUrl", activeUrl);
		boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType, objParams);
		logger.info("发送至{}邮箱的结果：{}", email, emailResult);
		if (emailResult) {
			// 调用统一缓存
			String keyStr = token + user.getMemberId();
			payPassWordCacheService.put(keyStr, email);
		}
		return emailResult;
	}
}
