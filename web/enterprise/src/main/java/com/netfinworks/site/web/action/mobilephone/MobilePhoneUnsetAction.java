package com.netfinworks.site.web.action.mobilephone;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.BankCardUtil;
import com.netfinworks.site.web.util.LogUtil;

/**
 * <p>
 * 解绑手机号码
 * </p>
 * 
 * @author liangzhizhuang.m
 * @version $Id: MobilePhoneUnsetAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m Exp $
 */
@Controller
public class MobilePhoneUnsetAction extends BaseAction {

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	protected static final Logger logger = LoggerFactory.getLogger(MobilePhoneUnsetAction.class);

	/**
	 * 跳至解绑手机号码页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-unset-mobilephone.htm", method = RequestMethod.GET)
	public ModelAndView goUnsetMobile(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		EnterpriseMember user = getUser(request);
		// AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		// authVerifyInfo.setMemberId(user.getMemberId());
		// List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		// for (int i = 0; i < infos.size(); i++) {
		// authVerifyInfo = infos.get(i);
		// if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
		// data.put("mobile", authVerifyInfo.getVerifyEntity());
		// data.put("mobileType", BizType.UNSET_MOBILE_USEMOBILE.getCode());
		// } else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
		// data.put("email", authVerifyInfo.getVerifyEntity());
		// data.put("emailType", BizType.UNSET_MOBILE_USEEMAIL.getCode());
		// }
		// }
		// data.put("useType", "useMobile");

		AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);

		if ((null == verifyInfo.getVerifyEntity()) || "".equals(verifyInfo.getVerifyEntity())) {
			restP.setMessage("您尚未绑定手机！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}

		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}

		data.put("bizType", BizType.UNSET_MOBILE_USEMOBILE.getCode());
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-phone", "response", restP);
	}

	/**
	 * 解绑-手机号码不可用
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/useLicenseNo.htm", method = RequestMethod.GET)
	public ModelAndView useLicenseNo(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		EnterpriseMember user = getUser(request);
		restP.setData(data);
		// 实名认证
		if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
			restP.setMessage("您的账户尚未实名认证!");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/business-error", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-phone-licenseNo", "response", restP);
	}

	/**
	 * 通过短信验证码解绑手机号码
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/unset-mobilephone-usemobilephone.htm", method = RequestMethod.POST)
	public ModelAndView unsetMobileByMobile(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		data.put("isCertActive", request.getParameter("isCertActive"));
		restP.setData(data);
		try {
			// 校验支付密码
			String password = decrpPassword(request, request.getParameter("password"));
			deleteMcrypt(request);
			EnterpriseMember user = getUser(request);

			String useType = request.getParameter("useType");

			data.put("useType", useType);
			data.put("bizType", BizType.UNSET_MOBILE_USEMOBILE.getCode());

			// 校验加盐支付密码
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
                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
					errors.put("error_passwd_is_locked", "error_passwd_is_locked");
				} else {
					errors.put("error_passwd_not_right", "error_passwd_not_right");
				}
				restP.setErrors(errors);
				if (useType.equals("usePhone")) {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-phone", "response", restP);
				} else {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-phone-licenseNo",
							"response", restP);
				}
			}
			AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
			authVerifyInfo.setMemberId(user.getMemberId());
			List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
			String mobile = "";
			long verifyId = -1L;
			for (int i = 0; i < infos.size(); i++) {
				authVerifyInfo = infos.get(i);
				if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
					mobile = authVerifyInfo.getVerifyEntity();
					verifyId = authVerifyInfo.getVerifyId();
				}
			}

			if (useType.equals("usePhone")) {
				String mobileCaptcha = request.getParameter("mobileCaptcha");
				logger.info("解绑手机号码，mobile:{}，mobileCaptcha:{}，password：{}", mobile, mobileCaptcha, password);
				// 封装校验短信验证码请求
				mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
				AuthCodeRequest req = new AuthCodeRequest();
				req.setMemberId(user.getMemberId());
				req.setMobile(mobile);
				String ticket = defaultUesService.encryptData(mobile);
				req.setMobileTicket(ticket);
				req.setAuthCode(mobileCaptcha);
				req.setBizId(user.getMemberId());
				req.setBizType(BizType.UNSET_MOBILE_USEMOBILE.getCode());
				// 校验短信验证码
				boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
				if (!messageResult) {
					logger.info("手机短信验证码验证失败！");
					data.put("mobileCaptcha_error", "您输入的验证码不正确，请重新输入");
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-phone", "response", restP);
				}
			} else if (useType.equals("useLicenseNo")) {
				String licenseNo = request.getParameter("busCode");
				if (!licenseNo.equals(user.getLicenseNo())) {
					logger.info("营业执照号验证失败！");
					data.put("licenseNo_error", "营业执照号验证失败！");
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-phone-licenseNo",
							"response", restP);
				}
			}
			// 移除认证信息
			ResponseCode verifyResult = authVerifyService.deleteVerify(verifyId, env);

			data.put("mobile", mobile);
			if (ResponseCode.SUCCESS == verifyResult) {
				restP.setSuccess(true);
				restP.setMessage("unset_mobile_sucess");
				updateSessionObject(request);
			} else {
				restP.setMessage("unset_mobile_fail");
			}
		} catch (Exception e) {
			logger.error("解绑手机错误：{}", e);
			e.printStackTrace();
			restP.setMessage("unset_mobile_fail");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-result", "response", restP);
	}

	/**
	 * 跳至解绑手机号码邮箱激活页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-unset-mobilephone-active.htm", method = RequestMethod.POST)
	public ModelAndView goUnsetMobileActive(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String bizType = request.getParameter("bizType");
		String useType = request.getParameter("useType");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		try {
			String password = decrpPassword(request, request.getParameter("email_password"));
			deleteMcrypt(request);
			EnterpriseMember user = getUser(request);
			String email = "";
			String mobile = "";
			AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
			authVerifyInfo.setMemberId(user.getMemberId());
			List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
			for (int i = 0; i < infos.size(); i++) {
				authVerifyInfo = infos.get(i);
				if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
					mobile = authVerifyInfo.getVerifyEntity();
				} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
					email = authVerifyInfo.getVerifyEntity();
				}
			}
			data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
			data.put("email", email);
			data.put("mobile", mobile);
			data.put("mobileType", BizType.UNSET_MOBILE_USEMOBILE.getCode());
			data.put("emailType", bizType);
			data.put("bizType", bizType);
			data.put("useType", useType);
			// 校验加盐支付密码
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getCurrentOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(password);
			payPasswsReq.setValidateType(1);
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (!checkResult.isSuccess()) {
				int remainNum = checkResult.getRemainNum();
				Map<String, String> errors = new HashMap<String, String>();
				errors.put("confirmpasswd_remainNum", remainNum + "");
				if (checkResult.isLocked()) {
					errors.put("error_confirmpasswd_is_locked", "error_passwd_is_locked");
                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
				} else {
					errors.put("error_confirmpasswd_not_right", "error_passwd_not_right");
				}
				restP.setErrors(errors);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-mobilephone", "response", restP);
			}
			if (sendMail(request, env)) {
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-mobilephone-active",
						"response", restP);
			}
		} catch (Exception e) {
			logger.error("通过邮箱验证解绑手机错误：{}", e);
			e.printStackTrace();
			restP.setMessage("unset_mobile_fail");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-result", "response", restP);

	}

	/**
	 * 通过邮箱验证解绑手机号码
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/unset-mobilephone-useemail.htm", method = RequestMethod.GET)
	public ModelAndView unsetMobileByEmail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String token = request.getParameter("token");
		RestResponse restP = new RestResponse();
		restP.setData(initOcx());
		try {
			EnterpriseMember user = getUser(request);
			String email = payPassWordCacheService.load(token + user.getMemberId());
			if (!StringUtils.isEmpty(email)) {
				// 移除认证信息
				AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);
				ResponseCode verifyResult = authVerifyService.deleteVerify(verifyInfo.getVerifyId(), env);
				if (ResponseCode.SUCCESS == verifyResult) {
					payPassWordCacheService.clear(token + user.getMemberId());
					restP.setSuccess(true);
					restP.setMessage("unset_mobile_sucess");
					updateSessionObject(request);
				} else {
					restP.setMessage("unset_mobile_fail");
				}
			} else {
				restP.setMessage("verify_email_timeout");
			}
		} catch (Exception e) {
			logger.error("通过邮箱验证解绑手机错误：{}", e);
			e.printStackTrace();
			restP.setMessage("unset_mobile_fail");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-result", "response", restP);
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
		String email = getEncryptInfo(request, DeciphedType.EMAIL, env);
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ "/my/unset-mobilephone-useemail.htm?token=" + token;
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
