package com.netfinworks.site.web.action.email;

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
import com.netfinworks.site.domain.domain.request.UnionmaUnBindEmailRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.unionma.BindFacadeService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.util.BankCardUtil;

/**
 * <p>
 * 解绑邮箱
 * </p>
 * 
 * @author liangzhizhuang.m
 * @version $Id: EmailUnsetAction.java, v 0.1 2014年5月20日 下午4:45:48
 *          liangzhizhuang.m Exp $
 */
@Controller
public class EmailUnsetAction extends BaseAction {

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

	@Resource(name = "bindFacadeService")
	private BindFacadeService bindFacadeService;
	
	protected static final Logger logger = LoggerFactory
			.getLogger(EmailUnsetAction.class);

	/**
	 * 跳至解绑邮箱页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-unset-email.htm", method = RequestMethod.GET)
	public ModelAndView goUnsetEmail(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env)
																									throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		PersonMember user = getUser(request);
		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(user.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				data.put("mobile", authVerifyInfo.getVerifyEntity());
				data.put("mobileType", BizType.UNSET_EMAIL_USEMOBILE.getCode());
				data.put("hasPhoneBind", "Y");
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				data.put("email", authVerifyInfo.getVerifyEntity());
				data.put("emailType", BizType.UNSET_EMAIL_USEEMAIL.getCode());
				data.put("hasEmailBind", "Y");
			}
		}
		data.put("useType", "useEmail");
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_unbind", "response", restP);
	}

	/**
	 * 通过短信验证码解绑邮箱
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/my/unset-email-usemobilephone.htm", method = RequestMethod.POST)
	public RestResponse unsetEmailByMobile(HttpServletRequest request,
			OperationEnvironment env) throws Exception {
		String mobileCaptcha = request.getParameter("data[mobileCaptcha]");
		String bizType = request.getParameter("data[bizType]");
		String useType = request.getParameter("data[useType]");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			deleteMcrypt(request);
			PersonMember user = getUser(request);
			String email = "";
			String mobile = "";
			long verifyId = -1L;
			AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
			authVerifyInfo.setMemberId(user.getMemberId());
			List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
			for (int i = 0; i < infos.size(); i++) {
				authVerifyInfo = infos.get(i);
				if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
					mobile = authVerifyInfo.getVerifyEntity();
				} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
					email = authVerifyInfo.getVerifyEntity();
					verifyId = authVerifyInfo.getVerifyId();
				}
			}
			data.put("email", email);
			data.put("emailType", BizType.UNSET_EMAIL_USEEMAIL.getCode());
			data.put("mobile", mobile);
			data.put("mobileType", bizType);
			data.put("useType", useType);
			logger.info("解绑邮箱，mobile:{}，mobileCaptcha:{}，password：{}", mobile, mobileCaptcha);
			mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env); // 获取解密手机号码进行验证码校验
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
			boolean messageResult = defaultSmsService.verifyMobileAuthCode(req,env);
			if (!messageResult) {
				logger.info("手机短信验证码验证失败！");
				data.put("mobileCaptcha_error", "验证码错误");
				restP.setSuccess(false);
				return restP;
			} else {
				// 解绑邮箱
				UnionmaUnBindEmailRequest unBindReq=new UnionmaUnBindEmailRequest();
				unBindReq.setMemberId(user.getMemberId());
				UnionmaBaseResponse response=bindFacadeService.unBindEmail(unBindReq);
				if (response.getIsSuccess()) {
					restP.setSuccess(true);
					restP.setMessage("unset_email_sucess");
					updateSessionObject(request);
					if (logger.isInfoEnabled()) {
		                logger.info(LogUtil.appLog(OperateeTypeEnum.UNSETEMAIL.getMsg(), user, env));
					}
				} else {
					restP.setMessage("unset_email_fail");
					restP.setSuccess(false);
					return restP;
				}
			}
		} catch (Exception e) {
			logger.error("通过短信验证码解绑邮箱错误：{}", e);
			e.printStackTrace();
			restP.setMessage("unset_email_fail");
			restP.setSuccess(false);
			return restP;
		}
		
		restP.setSuccess(true);
		return restP;
	}
	
	
	@RequestMapping(value = "/my/go-unset-emailResult.htm", method = RequestMethod.GET)
	public ModelAndView goUbindEmailByPhone(HttpServletRequest request){
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind_unbind2");
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
		String email = getEncryptInfo(request, DeciphedType.EMAIL, env);
		String activeUrl = request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort()+request.getContextPath()+
				"/my/unset-email-useemail.htm?token=" + token;
		logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
		Map<String, Object> objParams = new HashMap<String, Object>();
		objParams.put("userName",StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
		objParams.put("activeUrl", activeUrl);
		boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType,objParams);
		logger.info("发送至{}邮箱的结果：{}", email, emailResult);
		if (emailResult) {
			// 调用统一缓存
			String keyStr = token + user.getMemberId();
			payPassWordCacheService.put(keyStr, email);
		}
		return emailResult;
	}

}
