package com.netfinworks.site.web.action.email;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.request.UnionmaBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSendEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaUnBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaVerifyAuthCodeRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaBindEmailResponse;
import com.netfinworks.site.domain.domain.response.UnionmaSendEmailResponse;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.SendType;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.unionma.BindFacadeService;
import com.netfinworks.site.ext.integration.unionma.SmsFacadeService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.util.BankCardUtil;

/**
 * <p>
 * 修改绑定邮箱
 * </p>
 * 
 * @author huangyajun
 * @version $Id: MobileAction.java, v 0.1 2014年5月20日 下午4:45:48 huangyajun
 *          Exp $
 */
@Controller
public class EmailResetAction extends BaseAction {

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "smsFacadeService")
    private SmsFacadeService smsFacadeService;
	
	@Resource(name = "bindFacadeService")
	private BindFacadeService bindFacadeService;
	
	protected static final Logger logger = LoggerFactory.getLogger(EmailResetAction.class);

	 /**
     * 选择修改邮箱的方式
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/check-update-email.htm")
    public ModelAndView checksetEmail(HttpServletRequest request,OperationEnvironment env) throws Exception{
    	
    	String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
    	RestResponse restP = new RestResponse();
    	PersonMember user = getUser(request);
    	String gopage = request.getParameter("gopage");
    	Map<String,Object>data = new HashMap<String, Object>();
    	restP.setData(data);
    	data.put("gopage", gopage);
    	data.put("loginName", user.getLoginName());
    	// 是否设置支付密码
    	boolean hasPayPwd=false;
    	if(user.getPaypasswdstatus() != MemberPaypasswdStatus.NOT_SET_PAYPASSWD){
    		hasPayPwd=true;
    	}
		if (!hasPayPwd) {//没有支付密码
	        return new ModelAndView("redirect:/my/go-reset-email.htm?type=dxyx&gopage="+gopage);
		}
		if (mobile==null) {//没有手机
	        return new ModelAndView("redirect:/my/go-reset-email.htm?type=yxmm&gopage="+gopage);
		}
        data.put("Secretmobile", user.getMobileStar());
        data.put("email", user.getEmailStar());
    	return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/check-update-email","response",restP);
    }
	
	
	/**
	 * 跳至修改绑定邮箱页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-reset-email.htm", method = RequestMethod.GET)
	public ModelAndView goResetEmail(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String gopage = request.getParameter("gopage");
		Map<String, Object> data = new HashMap<String, Object>();
		AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.EMAIL, env);
		data.put("email", verifyInfo.getVerifyEntity());
		// 查询该用户是否已手机
		AuthVerifyInfo Info = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);
		if (!StringUtils.isEmpty(Info.getVerifyEntity())) {
			data.put("mobile", Info.getVerifyEntity());
			data.put("hasBindPhone", "true");
		}
		String type = request.getParameter("type");
		data.put("type", type);
		data.put("gopage", gopage);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend", "response", restP);
	}

	/**
	 * 跳至修改绑定邮箱激活页面("支付密码+邮箱确认信"方式)
	 * 
	 * @param request
	 * @param resp
	 * @return 第一步
	 */
	@RequestMapping(value = "/my/resetEmailByPwdAndEmailConfirm-active.htm", method = RequestMethod.POST)
	public ModelAndView goResetEmailActive(HttpServletRequest request, OperationEnvironment env) throws Exception {
		
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		HttpSession session = request.getSession();
		if(null==session.getAttribute("mcrypt_key")){//防止页面按F5重复提交表单
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend", "response", restP);
		}
		try {
			String password = decrpPassword(request, request.getParameter("password"));
			String hasBindPhone = request.getParameter("hasBindPhone");
			String mobilePhone = request.getParameter("mobilePhone");
			String gopage = request.getParameter("gopage");
			data.put("gopage", gopage);
			deleteMcrypt(request);
			AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.EMAIL, env);
			String email = verifyInfo.getVerifyEntity();
			data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
			data.put("bizType", request.getParameter("bizType"));
			// 校验加盐支付密码
			PersonMember user = getUser(request);
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(password);
			payPasswsReq.setValidateType(1);
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (!checkResult.isSuccess()) {
				int remainNum = checkResult.getRemainNum();
				Map<String, String> errors = new HashMap<String, String>();
				errors.put("remainNum", remainNum + "");
				if (checkResult.isLocked()) {
					errors.put("error_passwd", "密码被锁定");
				} else {
					errors.put("error_passwd", "密码错误");
				}
				restP.setErrors(errors);
				data.put("hasBindPhone", hasBindPhone);
				data.put("mobile", mobilePhone);
				String type = request.getParameter("type");
				data.put("type", type);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend", "response", restP);
			}

			if (newSendMail(request, env)) {
				data.put("email",getEncryptInfo(request, DeciphedType.EMAIL, env));
				request.getSession().setAttribute(CommonConstant.RESET_EMAIL_TOKEN, CommonConstant.RESET_EMAIL_TOKEN);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend2", "response", restP);
			}
		} catch (Exception e) {
			logger.error("修改绑定邮箱错误：{}", e);
			restP.setMessage("修改绑定邮箱错误");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
		}
		restP.setMessage("修改绑定邮箱错误");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
	}

	/**
	 * 跳至重新修改绑定邮箱页面("输入新邮箱页面")
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-reset-set-email.htm", method = RequestMethod.GET)
	public ModelAndView goResetSetEmail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String token = request.getParameter("token");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String gopage = request.getParameter("gopage");
		data.put("gopage", gopage);
		restP.setData(data);
		try {
			data.put("bizType", BizType.RESET_EMAIL.getCode());
			PersonMember user = getUser(request);
			String email = payPassWordCacheService.load(token + user.getMemberId());
			if (!StringUtils.isEmpty(email)) {
				data.put("email", user.getEmailStar());
				request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend3", "response", restP);
			} else {
				restP.setMessage("verify_email_timeout");
			}
		} catch (Exception e) {
			logger.error("修改绑定邮箱错误：{}", e);
			restP.setMessage("修改绑定邮箱错误");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
	}

	/**
	 * 跳至重新修改绑定邮箱激活页面( 新邮箱激活操作)
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-reset-set-email-active.htm", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView goResetSetEmailActive(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String email = request.getParameter("email");
		String bizType = request.getParameter("bizType");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		if (!CommonConstant.REFRESH_TOKEN.equals(request.getSession().getAttribute(CommonConstant.REFRESH_TOKEN))) {
			return new ModelAndView("redirect:/my/go-reset-email.htm");
		}
		try {
			data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
			if (newSendMail(request, env)) {
				data.put("email", email);
				data.put("bizType", bizType);
				request.getSession().removeAttribute(CommonConstant.REFRESH_TOKEN);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind3", "response", restP);
			}
		} catch (Exception e) {
			logger.error("对不起，邮件发送失败：{}", e);
			restP.setMessage("对不起，邮件发送失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
		}
		restP.setMessage("对不起，邮件发送失败");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
	}

	/**
	 * 修改绑定邮箱(新邮箱绑定)
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/reset-email.htm", method = RequestMethod.GET)
	public ModelAndView resetEmail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String token = request.getParameter("token");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String gopage = request.getParameter("gopage");
		data.put("gopage", gopage);
		restP.setData(data);
		try {
			PersonMember user = getUser(request);
			String emailToken = (String) request.getSession().getAttribute(CommonConstant.RESET_EMAIL_TOKEN);
			if (!emailToken.equals(CommonConstant.RESET_EMAIL_TOKEN)) {
				return new ModelAndView("redirect:/my/go-reset-email.htm");
			}
			String email = payPassWordCacheService.load(token + user.getMemberId());
			if (!StringUtils.isEmpty(email)) {
				UnionmaUnBindEmailRequest req=new UnionmaUnBindEmailRequest();
				req.setMemberId(user.getMemberId());
				UnionmaBaseResponse response=bindFacadeService.unBindEmail(req);
				if (!response.getIsSuccess()) {
					restP.setMessage("绑定新邮箱失败");
					return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
				}
				UnionmaBindEmailRequest bindReq=new UnionmaBindEmailRequest();
				bindReq.setMemberId(user.getMemberId());
				bindReq.setToken(token);
				UnionmaBindEmailResponse result=bindFacadeService.bindEmail(bindReq);
				if (result.getIsSuccess()) {
					String newEmail=payPassWordCacheService.load(token + user.getMemberId());
					payPassWordCacheService.clear(token + user.getMemberId());
					restP.setSuccess(true);
					restP.setMessage("reset_email_sucess");
					updateSessionObject(request);
					data.put("email", newEmail);
					request.getSession().removeAttribute(CommonConstant.RESET_EMAIL_TOKEN);
					if (logger.isInfoEnabled()) {
		                logger.info(LogUtil.appLog(OperateeTypeEnum.RESETEMAIL.getMsg(), user, env));
					}
				}else if ("EMAIL_ALREADY_SET".equals(result.getResponseCode())) {
					restP.setMessage("认证信息重复");
                    restP.setSuccess(false);
                }  else {
					restP.setMessage("绑定新邮箱失败");
				}
			} else {
				restP.setMessage("验证邮箱超时"); // verify_email_timeout
			}
		} catch (Exception e) {
			logger.error("修改绑定邮箱错误：{}", e);
			restP.setMessage("绑定新邮箱失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);

	}

	/**
	 * 验证短信验证码(手机验证码+邮箱确认信、支付密码+手机验证码)
	 */
	@RequestMapping(value = "/my/newValidMobileCaptcha.htm", method = RequestMethod.POST)
	public @ResponseBody
	RestResponse newValidMobileCaptcha(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		String bizType = request.getParameter("bizType");

		UnionmaVerifyAuthCodeRequest codeReq=new UnionmaVerifyAuthCodeRequest();
		codeReq.setAuthCode(mobileCaptcha);
		codeReq.setBizType(bizType);
		codeReq.setMemberId(user.getMemberId());
		codeReq.setVerifyValue(mobile);
		UnionmaBaseResponse result=bindFacadeService.verifyAuthCode(codeReq,env);
		if (!result.getIsSuccess()) {
			logger.info("手机短信验证码验证失败！");
			restP.setSuccess(false);
			return restP;
		}
		request.getSession().setAttribute("authcode", "true");
		restP.setSuccess(true);
		return restP;
	}
	/**
	 * 验证短信验证码(手机验证码+邮箱确认信、支付密码+手机验证码)
	 */
	@RequestMapping(value = "/my/validMobileCaptcha.htm", method = RequestMethod.POST)
	public @ResponseBody
	RestResponse validMobileCaptcha(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		String bizType = request.getParameter("bizType");

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
			restP.setSuccess(false);
			return restP;
		}
		request.getSession().setAttribute("authcode", "true");
		restP.setSuccess(true);
		return restP;
	}
	/**
	 * 发送邮件至原邮箱(手机验证码+邮箱确认信)
	 */
	@RequestMapping(value = "/my/sendEmailToOrigBox.htm", method = RequestMethod.POST)
	public ModelAndView sendEmailToOrigBox(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String email = getEncryptInfo(request, DeciphedType.EMAIL, env);
		String bizType = request.getParameter("bizType");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			if (!"true".equals(request.getSession().getAttribute("authcode"))) {
				return new ModelAndView("redirect:/error.htm");
			}
			deleteMcrypt(request);
			data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
			data.put("bizType", bizType);
			if (newSendMail(request, env)) {
				request.getSession().setAttribute(CommonConstant.RESET_EMAIL_TOKEN, CommonConstant.RESET_EMAIL_TOKEN);
				data.put("email", getEncryptInfo(request, DeciphedType.EMAIL, env));
				restP.setData(data);
				request.getSession().removeAttribute("authcode");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend2", "response", restP);
			}
		} catch (Exception e) {
			logger.error("对不起，邮件发送失败：{}", e);
			restP.setMessage("发送邮件失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
		}
		restP.setMessage("发送邮件失败");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);

	}

	/**
	 * 重置邮箱(支付密码+手机校验码) 验证支付密码，跳转到验证手机验证码页面
	 * 
	 */

	@RequestMapping(value = "/my/resetEmailByPwdAndMobile.htm", method = RequestMethod.POST)
	public ModelAndView resetEmailByPwdAndMobile(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		String gopage = request.getParameter("gopage");
		data.put("gopage", gopage);
		try {
			String password = decrpPassword(request, request.getParameter("password"));
			String hasBindPhone = request.getParameter("hasBindPhone");
			String mobilePhone = request.getParameter("mobilePhone");
			deleteMcrypt(request);
			AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.EMAIL, env);
			String email = verifyInfo.getVerifyEntity();
			// 校验加盐支付密码
			PersonMember user = getUser(request);
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(password);
			payPasswsReq.setValidateType(1);
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			data.put("email", email); // 邮箱密文回显
			if (!checkResult.isSuccess()) {
				int remainNum = checkResult.getRemainNum();
				Map<String, String> errors = new HashMap<String, String>();
				errors.put("remainNum", remainNum + "");
				if (checkResult.isLocked()) {
					errors.put("error_passwd", "密码被锁定");
				} else {
					errors.put("error_passwd", "密码错误");
				}
				restP.setErrors(errors);
				data.put("hasBindPhone", hasBindPhone);
				data.put("mobile", mobilePhone);
				String type = request.getParameter("type");
				data.put("type", type);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend", "response", restP);
			}

		} catch (Exception e) {
			logger.error("支付密码+手机校验码修改绑定邮箱验证支付密码错误：{}", e);
			restP.setMessage("验证支付密码错误");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
			
		}
		AuthVerifyInfo Info = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);// 手机密文
		data.put("mobile", Info.getVerifyEntity());
		data.put("bizType", BizType.RESET_EMAIL.getCode());
		request.getSession().setAttribute(CommonConstant.RESET_EMAIL_TOKEN, CommonConstant.RESET_EMAIL_TOKEN);
		request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend3", "response", restP);

	}

	/**
	 * 重置邮箱(支付密码+手机校验码) 跳到绑定新邮箱页面
	 */
	@RequestMapping(value = "/my/goNewEmailForm.htm", method = RequestMethod.POST)
	public ModelAndView goNewEmailForm(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("bizType", BizType.RESET_EMAIL.getCode());
		restP.setData(data);
		if (request.getSession().getAttribute("authcode") != "true") {
			return new ModelAndView("redirect:/error.htm");
		}
		request.getSession().removeAttribute("authcode");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_amend3", "response", restP);
	}

	/**
	 * 重置手机(原手机不可用，通过邮箱)
	 * 			
	 */
	@RequestMapping(value = " /my/resetMobileByEmail.htm", method = RequestMethod.GET)
	public ModelAndView resetMobileByEmail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.EMAIL, env);
		String email = verifyInfo.getVerifyEntity();
		String bizType = request.getParameter("bizType");

		deleteMcrypt(request);
		data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
		data.put("bizType", bizType);

		restP.setData(data);
		try {
			if (newSendMail(request, env)) {
				data.put("Secretemail", email);
				data.put("email",getEncryptInfo(request, DeciphedType.EMAIL, env));
				restP.setData(data);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse2", "response", restP);
			}
		} catch (Exception e) {
			logger.error("对不起，邮件发送失败：{}", e);
			restP.setMessage("对不起，邮件发送失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response", restP);
		}
		restP.setMessage("对不起，邮件发送失败");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response", restP);
	}

	/**
	 * 输入新手机页面(原手机不可用,通过邮箱找回,处理发至邮箱的激活链接)
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/go-new-mobile.htm" ,method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView goNewmobileForm(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		
		String token = request.getParameter("token");
		PersonMember user = getUser(request);
		String email = payPassWordCacheService.load(token + user.getMemberId());
		if (!StringUtils.isEmpty(email)) {
			data.put("bizType", BizType.RESET_MOBILE.getCode());
			restP.setData(data);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse3", "response", restP);
		}else{
			restP.setSuccess(false);
			restP.setMessage("获取用户绑定邮箱异常");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response", restP);
		}
		
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
		PersonMember user = getUser(request);
		String bizType = request.getParameter("bizType");
		String token = BankCardUtil.getUuid();
		String email = request.getParameter("email");
		String useType = request.getParameter("useType");
		String gopage = request.getParameter("gopage");
		String activeUrl = "";
		String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()+request.getContextPath();
		if (StringUtils.isEmpty(email) && (useType==null)) {
			email = getEncryptInfo(request, DeciphedType.EMAIL, env);
			activeUrl = host + "/my/go-reset-set-email.htm?token=" + token+"&gopage="+gopage;// 跳到输入新邮箱页面
		} else if ("LisenceNo".equals(useType)) {
			if(StringUtils.isEmpty(email)){
				email = getEncryptInfo(request, DeciphedType.EMAIL, env);
			}
			activeUrl = host + "/my/go-new-mobile.htm?token=" + token;// 跳到输入新手机号码页面
		}

		else {
			activeUrl = host + "/my/reset-email.htm?token=" + token+"&gopage="+gopage;// 跳到新邮箱绑定Action--绑定结果页面
		}
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

	
	/**
	 * 发送邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	private boolean newSendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		PersonMember user = getUser(request);
		String bizType = request.getParameter("bizType");
		String token = BankCardUtil.getUuid();
		String email = request.getParameter("email");
		String useType = request.getParameter("useType");
		String gopage = request.getParameter("gopage");
		String activeUrl = "";
		String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()+request.getContextPath();
		if (StringUtils.isEmpty(email) && (useType==null)) {
			email = getEncryptInfo(request, DeciphedType.EMAIL, env);
			activeUrl = host + "/my/go-reset-set-email.htm?token=" + token+"&gopage="+gopage;// 跳到输入新邮箱页面
		} else if ("LisenceNo".equals(useType)) {
			if(StringUtils.isEmpty(email)){
				email = getEncryptInfo(request, DeciphedType.EMAIL, env);
			}
			activeUrl = host + "/my/go-new-mobile.htm?token=" + token;// 跳到输入新手机号码页面
		}

		else {
			activeUrl = host + "/my/reset-email.htm?token=" + token+"&gopage="+gopage;// 跳到新邮箱绑定Action--绑定结果页面
		}
		logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
		String userName=StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName();
		UnionmaSendEmailRequest req=new UnionmaSendEmailRequest();
		req.setMemberId(user.getMemberId());
		req.setEmail(email);
		req.setUserName(userName);
		req.setActiveUrl(activeUrl);
		req.setToken(token);
		req.setSendType(SendType.AUTHURL);
		req.setBizType(bizType);
		UnionmaSendEmailResponse result=smsFacadeService.sendEmail(req);
//		boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType, objParams);
		logger.info("发送至{}邮箱的结果：{}", email, result.getIsSuccess());
		if (result.getIsSuccess()) {
			// 调用统一缓存
			String keyStr = token + user.getMemberId();
			payPassWordCacheService.put(keyStr, email);
		}
		return result.getIsSuccess();
	}
}
