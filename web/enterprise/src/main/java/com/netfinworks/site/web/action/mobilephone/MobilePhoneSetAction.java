package com.netfinworks.site.web.action.mobilephone;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;

/**
 * <p>
 * 绑定手机号码
 * </p>
 * 
 * @author liangzhizhuang.m
 * @version $Id: MobilePhoneSetAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m Exp $
 */
@Controller
public class MobilePhoneSetAction extends BaseAction {

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;
	
	@Resource(name = "operatorService")
	private OperatorService operatorService;

	protected static final Logger logger = LoggerFactory.getLogger(MobilePhoneSetAction.class);

	/**
	 * 跳至绑定手机号码页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-set-mobilephone-verify.htm")
	public ModelAndView goSetMobileVerify(HttpServletRequest request, HttpServletResponse resP) throws Exception {

		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		data.put("bizType", BizType.SET_MOBILE.getCode());

		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}

		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/set-mobilephone-verify", "response", restP);
	}

	/**
	 * 跳至绑定手机号码页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-set-mobilephone.htm")
	public ModelAndView goSetMobile(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env) throws Exception {

		String bizType = request.getParameter("bizType");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		data.put("bizType", bizType);
		data.put("isCertActive", request.getParameter("isCertActive"));
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
                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
					errors.put("error_passwd_is_locked", "error_passwd_is_locked");
				} else {
					errors.put("error_passwd_not_right", "error_passwd_not_right");
				}
				restP.setErrors(errors);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/set-mobilephone-verify", "response",
						restP);
			}
		} catch (Exception e) {
			logger.error("对不起，支付密码验证失败：{}", e);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/set-mobilephone-verify", "response",
					restP);
		}
		request.getSession().setAttribute(CommonConstant.SET_MOBILE_TOKEN, CommonConstant.SET_MOBILE_TOKEN);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/set-mobilephone", "response", restP);

	}

	/**
	 * 绑定手机号码
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/set-mobilephone.htm", method = RequestMethod.POST)
	public ModelAndView setMobile(HttpServletRequest request, OperationEnvironment env, ModelMap map) throws Exception {
		String mobile = request.getParameter("mobile");
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		String bizType = request.getParameter("bizType");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			String token = (String) request.getSession().getAttribute(CommonConstant.SET_MOBILE_TOKEN);
			if (!token.equals(CommonConstant.SET_MOBILE_TOKEN)) {
				return new ModelAndView("redirect:/my/go-set-mobilephone-verify.htm");
			}

			map.put("bizType", bizType);
			EnterpriseMember user = getUser(request);
			// 封装校验短信验证码请求
			AuthCodeRequest req = new AuthCodeRequest();
			req.setMemberId(user.getMemberId());
			req.setMobile(mobile);
			String ticket = defaultUesService.encryptData(mobile);
			req.setMobileTicket(ticket);
			req.setAuthCode(mobileCaptcha);
			req.setBizId(user.getMemberId());
			req.setBizType(bizType);
			// 校验短信验证码
			boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
			if (!messageResult) {
				logger.info("手机短信验证码验证失败！");
				data.put("mobileCaptcha_error", "您输入的验证码不正确，请重新输入");
				data.put("mobile", mobile);
				data.put("bizType", bizType);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/set-mobilephone", "response", restP);
			}
			AuthVerifyInfo info = new AuthVerifyInfo();
			info.setMemberId(user.getMemberId());
			info.setVerifyType(VerifyType.CELL_PHONE.getCode());
			info.setVerifyEntity(mobile);
			ResponseCode verifyResult = authVerifyService.createVerify(info, env);
			if (ResponseCode.SUCCESS == verifyResult) {
			    logger.info(LogUtil.generateMsg(OperateTypeEnum.BIND_PHONE, user, env, mobile));
				map.put("success", true);
				map.put("message", "set_mobile_sucess");
				updateSessionObject(request);
				AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);
				map.put("mobile", verifyInfo.getVerifyEntity());
				//修改默认操作员联系方式
				OperatorVO adminOperator = operatorService.getDefaultOperator(this.getUser(request).getMemberId(), env);
				adminOperator.setContact(mobile);
				operatorService.updateOperator(adminOperator, env);
			} else if (ResponseCode.DUPLICATE_VERIFY == verifyResult) {
				map.put("success", false);
				map.put("message", "set_mobile_repeat");
				map.put("mobile", mobile);
			} else {
				map.put("success", false);
				map.put("message", "set_mobile_fail");
			}
		} catch (Exception e) {
			logger.error("绑定手机错误：{}", e);
			e.printStackTrace();
			map.put("message", "set_mobile_fail");
		}
		request.getSession().removeAttribute(CommonConstant.SET_MOBILE_TOKEN);
		return new ModelAndView("redirect:/my/set-mobilephone-result.htm");
	}

	/**
	 * 跳至绑定结果页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/set-mobilephone-result.htm")
	public ModelAndView setResult(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> error = new HashMap<String, String>();
		data.put("mobile", request.getParameter("mobile"));
		restP.setMessage(request.getParameter("message"));
		restP.setSuccess(Boolean.valueOf(request.getParameter("success")));
		restP.setErrors(error);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/set-result", "response", restP);
	}

}
