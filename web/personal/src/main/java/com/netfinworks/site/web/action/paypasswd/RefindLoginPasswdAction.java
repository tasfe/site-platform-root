package com.netfinworks.site.web.action.paypasswd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import com.meidusa.fastjson.JSONArray;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.KaptchaImageAction;
import com.netfinworks.site.web.action.RegisterAction.RegisterPageTitleOper;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.processor.ValidationProcessor;
import com.netfinworks.site.web.util.BankCardUtil;

/**
 * 
 * <p>
 * 找回登陆密码（短信方式、邮件方式）
 * </p>
 * 
 * @author luoxun
 * @version $Id: RefindLoginPasswdAction.java, v 0.1 2014-5-31 下午12:51:49 luoxun
 *          Exp $
 */
@Controller
@RequestMapping(value = "/my/refind/loginPwd")
public class RefindLoginPasswdAction extends BaseAction {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource
	private DefaultLoginPasswdService defaultLoginPasswdService;

	@Resource(name = "memberService")
	private MemberService memberService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource
	private ValidationProcessor validationProcessor;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	@Resource(name = "webResource")
	private WebDynamicResource webResource;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

	/**
	 * 找回登陆密码首页
	 * 
	 * @param request
	 * @param resp
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/index.htm")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse resp, ModelMap model) {
		RegisterPageTitleOper.setRegisterPageTitleFind(request);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index");
	}

	@RequestMapping(value = "/checkpicture.htm")
	@ResponseBody
	// 1、验证登录账号图片验证码
	public RestResponse checkpicture(HttpServletRequest request, String code,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		if (KaptchaImageAction.validateRandCode(request, code)) {
			restP.setSuccess(true);
		} else {
			restP.setSuccess(false);
		}
		return restP;
	}

	/**
	 * 跳转找回密码主页
	 * 
	 * @param request
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception 
	 * @throws Exception
	 * @throws MemberNotExistException
	 */
	@RequestMapping(value = "/goMain.htm")
	public ModelAndView goMain(HttpServletRequest request, String selectlogin,
			String loginName, String imageVerifyCode, OperationEnvironment env) throws Exception
			 {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		// 检验参数
		if (StringUtils.isEmpty(loginName)) {
			restP.setMessage("请输入您的会员账号！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
		}

		if (StringUtils.isEmpty(imageVerifyCode)) {
			restP.setMessage("请输入验证码！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
		}
		String membertype = null;

		if (!KaptchaImageAction.validateRandCode(request, imageVerifyCode)) {
			restP.setMessage("验证码输入有误！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
		}

		// 1.验证用户名是否存在
		PersonMember personMember = new PersonMember();
		personMember.setLoginName(loginName);
		EnterpriseMember enterpriseMember =new EnterpriseMember();
		enterpriseMember.setLoginName(loginName);
	    boolean isPerson = false;
	    boolean isEnterPrise = false;
	    //注册会员是否已激活
		boolean isActiveMember=false;
	    try {
			personMember = memberService.queryMemberIntegratedInfo(personMember, env);
			if(personMember.getMemberType() != MemberType.PERSONAL){
				throw new MemberNotExistException(ErrorCode.MEMBER_NOT_EXIST);
			}
			isPerson = true;
			isActiveMember=personMember.getStatus()==MemberStatus.NOT_ACTIVE?false:true;
		} catch (BizException e1) {
			throw new Exception("系统出错，请重试！");
		} catch (MemberNotExistException e2) {
			try {
				enterpriseMember = memberService.queryCompanyMember(enterpriseMember, env);
				if (enterpriseMember.getMemberType() != MemberType.ENTERPRISE) {
					throw new MemberNotExistException(ErrorCode.MEMBER_NOT_EXIST);
				}
				isEnterPrise = true;
				personMember.setMemberId(enterpriseMember.getMemberId());
			} catch (BizException e3) {
				throw new Exception("系统出错，请重试！");
			} catch (MemberNotExistException e4) {
				restP.setMessage("输入的会员账号不存在！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
			}
		} 
	    //登录名加星处理
	    loginName=getLoginNameStar(loginName);

	    if(isPerson){
	    	if(!isActiveMember){
	    		restP.setMessage("您的账户尚未激活,请先<a href='../../../register/accountActive.htm' class='blue' style='text-decoration:underline; padding:0 2px;'>激活</a>您的账户");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
	    	}
	    	membertype = "1"; 
			super.setJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER, personMember);
			personMember.setLoginName(loginName);
	    }else if(isEnterPrise){
	    	membertype = "2";	    	
			super.setJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,enterpriseMember);
			enterpriseMember.setLoginName(loginName);
	    }
	    
	    AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(personMember.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(
				authVerifyInfo, env);
		boolean isBindPhone = false;		
		boolean isBindEmail = false;
		String  bindPhone="";
		String  bindEmail="";
		
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo
					.getVerifyType()) {
				isBindPhone = true;
				bindPhone=authVerifyInfo.getVerifyEntity();
			}

			if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				isBindEmail = true;
				bindEmail=authVerifyInfo.getVerifyEntity();
			}
		}
		
		request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
		request.getSession().setAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN,CommonConstant.REFIND_LOGINPWD_TOKEN);
		//如果手机与邮箱都绑定时跳转
		if(isBindPhone&&isBindEmail){
			data.put("bindPhone", bindPhone);
			data.put("bindEmail", bindEmail);	
			data.put("membertype", membertype);			
			data.put("loginName", loginName);				
			restP.setData(data);			
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/findLoginPwdMain", "response", restP);
		}
		//如果只绑定手机跳转
		if(isBindPhone){
			ModelAndView modelAndView = new ModelAndView(
					"redirect:/my/refind/loginPwd/phoneFirst.htm?membertype="+ membertype);
			return modelAndView;
		}
		//如果只绑定邮箱跳转
		if(isBindEmail){
			ModelAndView modelAndView = new ModelAndView(
					"redirect:/my/refind/loginPwd/goRefindEmail.htm?membertype="+ membertype);
			request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
			return modelAndView;
		}
		
		
		restP.setMessage("找回登陆密码失败，请重试！");
		return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
	}	

	/**
	 * 跳转手机找回第一步
	 * 
	 * @param request
	 * @param resp
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/phoneFirst.htm")
	public ModelAndView goPhoneFirst(HttpServletRequest request,
			HttpServletResponse resp) {
		String membertype = request.getParameter("membertype");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		if(membertype.equals("1")){
			if (super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
					PersonMember.class) == null) {
				restP.setMessage("账号信息错误，请重试！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
			}
			PersonMember personMember = super.getJsonAttribute(
					request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
					PersonMember.class);
			personMember.setLoginName(getLoginNameStar(personMember.getLoginName()));
			ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX
					+ "/loginPwd/refind/phoneFirst", "personMember", personMember);
			modelAndView.addObject("membertype",membertype);
			return modelAndView;
		}
		if(membertype.equals("2")){
			if (super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class) == null) {
				restP.setMessage("账号信息错误，请重试！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
			}
			EnterpriseMember enterpriseMember = super.getJsonAttribute(
					request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class);
			enterpriseMember.setLoginName(getLoginNameStar(enterpriseMember.getLoginName()));
			ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX
					+ "/loginPwd/refind/phoneFirst", "personMember", enterpriseMember);
			modelAndView.addObject("membertype",membertype);
			return modelAndView;
		}

		return new ModelAndView("redirect:/my/refind/loginPwd/index.htm");
	}

	/**
	 * 跳转手机-验证手机短信码
	 * 
	 * @param request
	 * @param resp
	 * @param model
	 * @return
	 * @throws BizException
	 * @throws ServiceException
	 * @throws MemberNotExistException 
	 */
	@RequestMapping(value = "/checkTelMsgCode.htm")
	public ModelAndView checkTelMsgCode(String phoneVerifyCode,
			HttpServletRequest request, OperationEnvironment env)
			throws BizException, ServiceException, MemberNotExistException {
		String membertype=request.getParameter("membertype");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		if(membertype.equals("1")){
			if (super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
					PersonMember.class) == null) {
				restP.setMessage("账号信息错误，请重试！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
			}
			PersonMember personMember = super.getJsonAttribute(
					request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
					PersonMember.class);			
			// 1、验证短信验证码是否正确			
			AuthCodeRequest req = new AuthCodeRequest();
			EncryptData encryptData = memberService.decipherMember(
					personMember.getMemberId(), DeciphedType.CELL_PHONE,
					DeciphedQueryFlag.ALL, env);
			String mobile = encryptData.getPlaintext(); // 获取解密手机号码进行验证码校验
			req.setMemberId(personMember.getMemberId());
			req.setMobile(mobile);
			String ticket = defaultUesService.encryptData(mobile);
			req.setMobileTicket(ticket);
			req.setAuthCode(phoneVerifyCode);
			req.setBizId(personMember.getMemberId());
			req.setBizType(BizType.REFIND_LOGIN_SMS.getCode());
			// 校验短信验证码
			boolean messageResult = defaultSmsService
					.verifyMobileAuthCode(req, env);
			if (!messageResult) {
				ModelAndView modelAndView = new ModelAndView(
						CommonConstant.URL_PREFIX + "/loginPwd/refind/phoneFirst");
				modelAndView.addObject("personMember", personMember);
				modelAndView.addObject("errorMsg", "手机验证码错误！");
				modelAndView.addObject("membertype", membertype);
				logger.info("手机短信验证码验证失败！");
				return modelAndView;
			}
			
			//判断会员是否激活 
			if(personMember.getStatus()==MemberStatus.NOT_ACTIVE){
				ModelAndView modelAndView=this.isActiveMember(personMember, env);				
				return modelAndView;
			}
			
			boolean isNameVerify = false;
			// 查询个人实名认证级别
			String level = "";			
			level = defaultMemberService.getMemberVerifyLevel(personMember.getMemberId(), env);
			if (level.equals(CertifyLevel.CERTIFY_V0.getCode())) {
				isNameVerify = true;
			}			
			 
			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(personMember.getMemberId());			
			info.setOperator(personMember.getOperatorId());
			info.setAuthType(AuthType.IDENTITY);
			AuthInfo authInfo = defaultCertService.queryRealName(info);	
			if(null==authInfo.getAuthType()){
				info.setAuthType(AuthType.REAL_NAME);
				authInfo = defaultCertService.queryRealName(info);
			}
			//如果用户通过实名认证,将认证类型传到页面
			if(null!=authInfo.getCertType() && AuthResultStatus.CHECK_PASS.getCode().equals(authInfo.getResult().getCode())){				
				data.put("certType", authInfo.getCertType().getCode());
			}
					
			data.put("loginName", getLoginNameStar(personMember.getLoginName()));			
			data.put("membertype", membertype);
			data.put("isNameVerify", isNameVerify);	
			
			request.getSession().setAttribute(
					CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG, true);
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/loginPwd/refind/goReSetLoginPwd","response", restP);
		}
		if(membertype.equals("2")){
			if (super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class) == null) {
				restP.setMessage("账号信息错误，请重试！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
			}
			EnterpriseMember enterpriseMember = super.getJsonAttribute(
					request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class);
			// 1、验证短信验证码是否正确			
			AuthCodeRequest req = new AuthCodeRequest();
			EncryptData encryptData = memberService.decipherMember(
					enterpriseMember.getMemberId(), DeciphedType.CELL_PHONE,
					DeciphedQueryFlag.ALL, env);
			String mobile = encryptData.getPlaintext(); // 获取解密手机号码进行验证码校验
			req.setMemberId(enterpriseMember.getMemberId());
			req.setMobile(mobile);
			String ticket = defaultUesService.encryptData(mobile);
			req.setMobileTicket(ticket);
			req.setAuthCode(phoneVerifyCode);
			req.setBizId(enterpriseMember.getMemberId());
			req.setBizType(BizType.REFIND_LOGIN_SMS.getCode());
			// 校验短信验证码
			boolean messageResult = defaultSmsService
					.verifyMobileAuthCode(req, env);
			if (!messageResult) {
				ModelAndView modelAndView = new ModelAndView(
						CommonConstant.URL_PREFIX + "/loginPwd/refind/phoneFirst");
				modelAndView.addObject("personMember", enterpriseMember);
				modelAndView.addObject("errorMsg", "手机验证码错误！");
				modelAndView.addObject("membertype", membertype);
				logger.info("手机短信验证码验证失败！");
				return modelAndView;
			}
			
			//营业执照号
			boolean isBindLicenseNo = false;		
			//组织机构代码证号
			boolean isBindOrganizationNo = false;
			String certType="";			
			enterpriseMember=queryEntMemberNo(enterpriseMember.getLoginName(),env);
			if(null!=enterpriseMember && StringUtils.isNotBlank(enterpriseMember.getLicenseNo())){
				isBindLicenseNo=true;
			}
			if(null!=enterpriseMember && StringUtils.isNotBlank(enterpriseMember.getOrganizationNo())){
				isBindOrganizationNo=true;
			}
			//如果营业执照和组织机构代码同时存在 取营业执照
			if(isBindLicenseNo&&isBindOrganizationNo){
				certType="licenseNo";
			}else if(isBindLicenseNo){
				certType="licenseNo";
			}else if(isBindOrganizationNo){
				certType="organizationNo";
			}			
			//企业验证类型
			data.put("certType", certType);
			//会员类型
			data.put("membertype", membertype);
			data.put("loginName", getLoginNameStar(enterpriseMember.getLoginName()));			
			request.getSession().setAttribute(
					CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT, true);
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/loginPwd/refind/goReSetLoginPwd","response",restP);
		}
		return new ModelAndView("redirect:/my/refind/loginPwd/index.htm");
	}

	/**
	 * 重置登陆密码
	 * 
	 * @param request	
	 * @param resp
	 * @param model
	 * @return
	 * @throws ServiceException
	 * @throws MemberNotExistException
	 * @throws BizException
	 */
	@RequestMapping(value = "/reSetLoginPwd.htm")
	@ResponseBody
	public RestResponse reSetLoginPwd(String loginPwd, String reLoginPwd,String certificateNo,String certType,
			HttpServletRequest request, OperationEnvironment env)
			throws ServiceException, BizException, MemberNotExistException {
		String membertype = request.getParameter("membertype");
		RestResponse restP = new RestResponse();
		restP.setSuccess(false);
		if (membertype.equals("1")) { 
			if ((request.getSession().getAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG) == null)
					|| !(Boolean) request.getSession().getAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG)) {
				restP.setSuccess(true);
				restP.setRedirect(request.getContextPath() + "/my/refind/loginPwd/index.htm");
				return restP;				
			}
			if (super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
					PersonMember.class) == null) {
				restP.setSuccess(true);
				restP.setRedirect(request.getContextPath() + "/my/refind/loginPwd/index.htm");
				return restP;				
			}

			if (StringUtils.isEmpty(loginPwd) || StringUtils.isEmpty(reLoginPwd)) {				
				restP.setMessage("登陆密码和重复登陆密码均不能为空！");			
				return restP;				
			}
			// 登陆密码和确认密码不一致
			if (!loginPwd.equals(reLoginPwd)) {			
				restP.setMessage("登陆密码和确认密码不一致！");				
				return restP;				
			}			
			
			PersonMember personMember = super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER, PersonMember.class);
			if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.RESET_LOGIN_PWD.getMsg(), personMember, env));
			}

			// 查询个人实名认证级别
			String level = defaultMemberService.getMemberVerifyLevel(personMember.getMemberId(), env);

			if (level.equals(CertifyLevel.CERTIFY_V0.getCode()) || level.equals(CertifyLevel.CERTIFY_V1.getCode())
					|| level.equals(CertifyLevel.CERTIFY_V2.getCode())) {
				String token = (String) request.getSession().getAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN);
				if (!token.equals(CommonConstant.REFIND_LOGINPWD_TOKEN)) {
					restP.setMessage("账号信息错误，请重试！");
					return restP;	
				}				
				if (StringUtils.isEmpty(certificateNo)) {				
					restP.setMessage("证件号码不能为空！");			
					return restP;				
				}				
				AuthInfoRequest authInfoReq = new AuthInfoRequest();
				authInfoReq.setMemberId(personMember.getMemberId());
				authInfoReq.setAuthType(AuthType.IDENTITY);
				authInfoReq.setOperator(personMember.getOperatorId());				
				AuthInfo info = defaultCertService.queryRealName(authInfoReq);				
				if(null==info.getAuthType()){
					authInfoReq.setAuthType(AuthType.REAL_NAME);
					info = defaultCertService.queryRealName(authInfoReq);
				}				
				if(!info.getAuthNo().equals(certificateNo)){
					restP.setMessage("您输入的证件号和您的实名认证信息不一致,请您重新输入！");
					return restP;
				}				
			}
			
			// 解密
			loginPwd = decrpPassword(request, loginPwd);
			// 清空随机因子
			super.deleteMcrypt(request);
			LoginPasswdRequest req = new LoginPasswdRequest();
			req.setValidateType(2);
			req.setPassword(loginPwd);
			req.setClientIp(request.getRemoteAddr());
			req.setOperatorId(personMember.getOperatorId());
			// 会员标识
			req.setMemberIdentity(personMember.getMemberIdentity());
			// 会员标识平台类型 1.UID
			req.setPlatformType(personMember.getPlatformType());
			// 重置登陆密码锁
			defaultLoginPasswdService.resetLoginPasswordLock(req);
			// 重置登陆密码
			CommResponse commResponse = defaultLoginPasswdService.setLoginPassword(req);
			String responseCode = commResponse.getResponseCode();
			if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(responseCode)) {				
				restP.setMessage("登陆密码不能和支付密码相同！");
				return restP;
			}
			request.getSession().removeAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER);
			request.getSession().removeAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG);
			request.getSession().removeAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN);
			restP.setSuccess(true);
			restP.setRedirect(request.getContextPath() + "/my/refind/loginPwd/reSetLoginPwdResult.htm");
			return restP;

		}
		if (membertype.equals("2")) {
			if ((request.getSession().getAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT) == null)
					|| !(Boolean) request.getSession()
							.getAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT)) {
				restP.setSuccess(true);
				restP.setRedirect(request.getContextPath() + "/my/refind/loginPwd/index.htm");
			}

			if (super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class) == null) {
				restP.setSuccess(true);
				restP.setRedirect(request.getContextPath() + "/my/refind/loginPwd/index.htm");
			}
			
			if (StringUtils.isEmpty(loginPwd) || StringUtils.isEmpty(reLoginPwd)) {
				restP.setMessage("登陆密码和重复登陆密码均不能为空！");			
				return restP;
			}
			// 登陆密码和确认密码不一致
			if (!loginPwd.equals(reLoginPwd)) {
				restP.setMessage("登陆密码和确认密码不一致！");				
				return restP;	
			}
			
			EnterpriseMember enterPriseMember = super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class);
			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(enterPriseMember.getMemberId());
			info.setAuthType(AuthType.REAL_NAME);
			info.setOperator(enterPriseMember.getOperatorId());
			AuthResultStatus state = defaultCertService.queryAuthType(info);
			if (state.getCode().equals("PASS")) {
				String token = (String) request.getSession().getAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN);
				if (!token.equals(CommonConstant.REFIND_LOGINPWD_TOKEN)) {
					restP.setMessage("账号信息错误，请重试！");
					return restP;	
				}
				//验证企业的营业执照号或组织机构代码号
				EnterpriseMember enterpriseMember=queryEntMemberNo(enterPriseMember.getLoginName(),env);				
				if(StringUtils.isBlank(certType)||StringUtils.isBlank(certificateNo)){
					restP.setMessage("证件类型或证件号码不能为空！");			
					return restP;	
				}				
				if("licenseNo".equals(certType) && !certificateNo.equals(enterpriseMember.getLicenseNo())){
					restP.setMessage("您输入的证件号和您的实名认证信息不一致,请您重新输入！");			
					return restP;	
				}else if("organizationNo".equals(certType) && !certificateNo.equals(enterpriseMember.getOrganizationNo())){
					restP.setMessage("您输入的证件号和您的实名认证信息不一致,请您重新输入！");			
					return restP;	
				}				
			}
			
			// 解密
			loginPwd = decrpPassword(request, loginPwd);
			// 清空随机因子
			super.deleteMcrypt(request);

			LoginPasswdRequest req = new LoginPasswdRequest();
			req.setValidateType(2);
			req.setPassword(loginPwd);
			req.setClientIp(request.getRemoteAddr());
			req.setOperatorId(enterPriseMember.getOperatorId());
			// 这里有变化
			// 重置登陆密码锁
			defaultLoginPasswdService.resetLoginPasswordLock(req);
			// 重置登陆密码
			CommResponse commResponse = defaultLoginPasswdService.setLoginPassword(req);
			String responseCode = commResponse.getResponseCode();
			if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(responseCode)) {
				restP.setMessage("登陆密码不能和支付密码相同！");
				return restP;
			}
			request.getSession().removeAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT);
			request.getSession().removeAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT);
			request.getSession().removeAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN);
			restP.setSuccess(true);
			restP.setRedirect(request.getContextPath() + "/my/refind/loginPwd/reSetLoginPwdResult.htm");
			}
		return restP;
		//return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/goReSetLoginPwd");
	}
	
	 /**
     * 重置登陆密码结果页
     * @param request
     * @param response
     * @return
     */
	@RequestMapping(value = "/reSetLoginPwdResult.htm")
	public ModelAndView result(HttpServletRequest request, HttpServletResponse response) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);			
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/reSetLoginPwdResult", "response", restP);
	}
	
	

	@RequestMapping(value = "/checkLoginNameIsExist.htm", method = RequestMethod.POST)
	@ResponseBody
	public boolean checkLoginNameIsExist(String loginName,
			HttpServletResponse response, OperationEnvironment env)
			throws BizException {
		try {
			PersonMember personMember = queryPersonMember(loginName, env);
			return personMember != null;
		} catch (MemberNotExistException e) {
			return false;
		}
	}

	/**
	 * 异步发送短信验证码
	 * 
	 * @param request
	 * @return
	 * @throws BindException
	 */
	@RequestMapping(value = "/ajaxSendSms.htm")
	public @ResponseBody
	RestResponse sendMessageByMobile(HttpServletRequest request,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String membertype = request.getParameter("membertype");
		if(membertype.equals("1")){
			try {
				if (super.getJsonAttribute(request.getSession(),
						CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
						PersonMember.class) == null) {
					restP.setSuccess(false);
					return restP;
				}
				PersonMember personMember = super.getJsonAttribute(
						request.getSession(),
						CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
						PersonMember.class);
				String mobile = "";
				if (StringUtils.isNotEmpty(personMember.getMemberId())) {
					EncryptData data = memberService.decipherMember(
							personMember.getMemberId(), DeciphedType.CELL_PHONE,
							DeciphedQueryFlag.ALL, env);
					mobile = data.getPlaintext();
					
				}
				if (StringUtils.isNotEmpty(mobile)) {
					// 封装发送短信请求
					AuthCodeRequest req = new AuthCodeRequest();
					req.setMemberId(personMember.getMemberId());
					req.setMobile(mobile);
					req.setBizId(personMember.getMemberId());
					req.setBizType(BizType.REFIND_LOGIN_SMS.getCode());
					String ticket = defaultUesService.encryptData(mobile);
					req.setMobileTicket(ticket);
					req.setValidity(CommonConstant.VALIDITY);
					// 发送短信
					if (defaultSmsService.sendMessage(req, env)) {
						restP.setSuccess(true);
					} else {
						restP.setSuccess(false);
					}

				} else {
					restP.setSuccess(false);
				}
				return restP;
			} catch (Exception e) {
				log.error("发送短信失败:" + e.getMessage(), e.getCause());
				restP.setSuccess(false);
				return restP;
			}
		}
		if(membertype.equals("2")){
			try {
				if (super.getJsonAttribute(request.getSession(),
						CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
						EnterpriseMember.class) == null) {
					restP.setSuccess(false);
					return restP;
				}
				EnterpriseMember enterpriseMember = super.getJsonAttribute(
						request.getSession(),
						CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
						EnterpriseMember.class);
				String mobile = "";
				if (StringUtils.isNotEmpty(enterpriseMember.getMemberId())) {
					EncryptData data = memberService.decipherMember(
							enterpriseMember.getMemberId(), DeciphedType.CELL_PHONE,
							DeciphedQueryFlag.ALL, env);
					mobile = data.getPlaintext();	
				}
				if (StringUtils.isNotEmpty(mobile)) {
					// 封装发送短信请求
					AuthCodeRequest req = new AuthCodeRequest();
					req.setMemberId(enterpriseMember.getMemberId());
					req.setMobile(mobile);
					req.setBizId(enterpriseMember.getMemberId());
					req.setBizType(BizType.REFIND_LOGIN_SMS.getCode());
					String ticket = defaultUesService.encryptData(mobile);
					req.setMobileTicket(ticket);
					req.setValidity(CommonConstant.VALIDITY);
					// 发送短信
					if (defaultSmsService.sendMessage(req, env)) {
						restP.setSuccess(true);
					} else {
						restP.setSuccess(false);
					}

				} else {
					restP.setSuccess(false);
				}
				return restP;
			} catch (Exception e) {
				log.error("发送短信失败:" + e.getMessage(), e.getCause());
				restP.setSuccess(false);
				return restP;
			}
		}
		
		return restP;
		
	}

	/**
	 * 通过邮件找回密码->发送激活邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/goRefindEmail.htm")
	public ModelAndView sendMail(HttpServletRequest request,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		if (!CommonConstant.REFRESH_TOKEN.equals(request.getSession().getAttribute(CommonConstant.REFRESH_TOKEN))) {
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
		}

		String membertype = request.getParameter("membertype");
		if( membertype.equals("1") ){
			if (super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
					PersonMember.class) == null) {
				restP.setMessage("账号信息错误，请重试！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
			}
			PersonMember personMember = super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER, PersonMember.class);
			String email = "";
			EncryptData data1 = memberService.decipherMember(personMember.getMemberId(), DeciphedType.EMAIL,
					DeciphedQueryFlag.ALL, env);
			email = data1.getPlaintext();

			if (StringUtils.isEmpty(email)) {
				restP.setMessage("邮箱地址错误,请重试！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/index", "response", restP);
			}
			String token = UUID.randomUUID().toString().replace("-", "");
			String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath() + "/my/refind/loginPwd/checkRefindEmail.htm?token=" + token
					+ "&membertype=" + membertype;
			logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
			Map<String, Object> objParams = new HashMap<String, Object>();
			objParams.put("userName",
					StringUtils.isEmpty(personMember.getMemberName()) ? email : personMember.getMemberName());

			objParams.put("activeUrl", activeUrl);
			boolean emailResult = defaultPayPasswdService.sendEmail(email, BizType.REFIND_LOGIN_EMAIL.getCode(),
					objParams);
			logger.info("发送至{}邮箱的结果：{}", email, emailResult);
			String sb = JSONArray.toJSONString(personMember);

			if (emailResult) {				
				// 调用统一缓存
				payPassWordCacheService.put(token, JSONArray.toJSONString(personMember));
				//保存会员最后一次发送的token
				this.saveLastToken(personMember, token);
			}

			ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/sendMailResult",
					"emailResult", emailResult);
			modelAndView.addObject("email", personMember.getEmail());
			String loginMailUrl = LoginEmailAddress.getEmailLoginUrl(email);
			modelAndView.addObject("loginMailUrl", loginMailUrl);
			modelAndView.addObject("membertype", membertype);

			request.getSession().removeAttribute(CommonConstant.REFRESH_TOKEN);
			return modelAndView;
		}
		if(membertype.equals("2")){
			if (super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class) == null) {
				return new ModelAndView("redirect:/my/refind/loginPwd/index.htm");
			}
			EnterpriseMember enterpriseMember = super.getJsonAttribute(
					request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class);
			String email = "";
			EncryptData data2 = memberService.decipherMember(
					enterpriseMember.getMemberId(), DeciphedType.EMAIL,
					DeciphedQueryFlag.ALL, env);
			email = data2.getPlaintext();
			if (StringUtils.isEmpty(email)) {
				return new ModelAndView("redirect:/my/refind/loginPwd/index.htm");
			}
			String token = UUID.randomUUID().toString().replace("-", "");
			String activeUrl = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath()+"/my/refind/loginPwd/checkRefindEmailEnt.htm?token=" + token+"&membertype="+membertype;
			logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
			Map<String, Object> objParams = new HashMap<String, Object>();
			objParams.put("userName", StringUtils.isEmpty(enterpriseMember
					.getMemberName()) ? email : enterpriseMember.getMemberName());
			
			objParams.put("activeUrl", activeUrl);
			boolean emailResult = defaultPayPasswdService.sendEmail(email,
					BizType.REFIND_LOGIN_EMAIL.getCode(), objParams);
			logger.info("发送至{}邮箱的结果：{}", email, emailResult);

			if (emailResult) {
				// 调用统一缓存
				payPassWordCacheService.put(token,JSONArray.toJSONString(enterpriseMember));
				//保存会员最后一次发送的token
				this.saveLastToken(enterpriseMember, token);
			}
			
			ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX
					+ "/loginPwd/refind/sendMailResult", "emailResult", emailResult);
			modelAndView.addObject("email", enterpriseMember.getEmail());
			String loginMailUrl = LoginEmailAddress.getEmailLoginUrl(email);
			modelAndView.addObject("loginMailUrl", loginMailUrl);
			modelAndView.addObject("membertype",membertype);

			request.getSession().removeAttribute(CommonConstant.REFRESH_TOKEN);
			return modelAndView;
		}
		return new ModelAndView("redirect:/my/refind/loginPwd/index.htm");
	}

	/**
	 * ajax 重发邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/ajaxResendMail.htm")
	@ResponseBody
	public RestResponse ajaxResendMail(HttpServletRequest request,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String membertype = request.getParameter("membertype");
		if(membertype.equals("1")){
		if (super.getJsonAttribute(request.getSession(),
				CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
				PersonMember.class) == null) {
			restP.setSuccess(false);
			return restP;
		}
		PersonMember personMember = super.getJsonAttribute(
				request.getSession(),
				CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER,
				PersonMember.class);
		String email = "";
		EncryptData data = memberService.decipherMember(
				personMember.getMemberId(), DeciphedType.EMAIL,
				DeciphedQueryFlag.ALL, env);
		email = data.getPlaintext();
		if (StringUtils.isEmpty(email)) {
			restP.setSuccess(false);
			return restP;
		}
		String token = UUID.randomUUID().toString().replace("-", "");
		String activeUrl = request.getScheme() + "://"
				+ request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath()+"/my/refind/loginPwd/checkRefindEmail.htm?token=" + token+"&membertype="+membertype;
		logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
		Map<String, Object> objParams = new HashMap<String, Object>();
		objParams.put("userName", StringUtils.isEmpty(personMember
				.getMemberName()) ? email : personMember.getMemberName());
		objParams.put("activeUrl", activeUrl);
		boolean emailResult = defaultPayPasswdService.sendEmail(email,
				BizType.REFIND_LOGIN_EMAIL.getCode(), objParams);
		logger.info("发送至{}邮箱的结果：{}", email, emailResult);
		if (emailResult) {
			// 调用统一缓存
			payPassWordCacheService.put(token,JSONArray.toJSONString(personMember));
			//保存会员最后一次发送的token
			this.saveLastToken(personMember, token);
		}
		restP.setSuccess(true);
		return restP;
		}
		if(membertype.equals("2")){
			if (super.getJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class) == null) {
				restP.setSuccess(false);
				return restP;
			}
			EnterpriseMember enterpriseMember = super.getJsonAttribute(
					request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT,
					EnterpriseMember.class);
			String email = "";
			EncryptData data = memberService.decipherMember(
					enterpriseMember.getMemberId(), DeciphedType.EMAIL,
					DeciphedQueryFlag.ALL, env);
			email = data.getPlaintext();
			if (StringUtils.isEmpty(email)) {
				restP.setSuccess(false);
				return restP;
			}
			String token = UUID.randomUUID().toString().replace("-", "");
			String activeUrl = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath()+"/my/refind/loginPwd/checkRefindEmailEnt.htm?token=" + token+"&membertype="+membertype;
			logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
			Map<String, Object> objParams = new HashMap<String, Object>();
			objParams.put("userName", StringUtils.isEmpty(enterpriseMember
					.getMemberName()) ? email : enterpriseMember.getMemberName());
			objParams.put("activeUrl", activeUrl);
			boolean emailResult = defaultPayPasswdService.sendEmail(email,
					BizType.REFIND_LOGIN_EMAIL.getCode(), objParams);
			logger.info("发送至{}邮箱的结果：{}", email, emailResult);
			if (emailResult) {
				// 调用统一缓存
				payPassWordCacheService.put(token,JSONArray.toJSONString(enterpriseMember));
				//保存会员最后一次发送的token
				this.saveLastToken(enterpriseMember, token);
			}
			restP.setSuccess(true);
			return restP;
		}
		return restP;
	}

	/**
	 * 个人会员通过邮件找回密码->邮件校验
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checkRefindEmail.htm")
	public ModelAndView checkRefindEmail(HttpServletRequest request,
			OperationEnvironment env) throws Exception {
		String token = request.getParameter("token");
		String membertype = request.getParameter("membertype");
		if (StringUtils.isEmpty(token)) {
			return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
		}
		
		if(membertype.equals("1")){			
			PersonMember personMember = null;
			String jsonStr = payPassWordCacheService.load(token);
			if (StringUtils.isNotBlank(jsonStr)) {
				personMember = JSONArray.parseObject(jsonStr, PersonMember.class);
			}
			if (personMember == null) {
				return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
			}
			//判断是否是最后一次发送的邮件
			if(!this.isLastToken(personMember, token)){				
				return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
			}
			
			//判断会员是否激活 
			if(personMember.getStatus()==MemberStatus.NOT_ACTIVE){
				ModelAndView modelAndView=this.isActiveMember(personMember, env);
				payPassWordCacheService.clear(token);
				return modelAndView;
			}
			
			RestResponse restP = new RestResponse();
			Map<String, Object> data = initOcx();
			restP.setData(data);
			
			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(personMember.getMemberId());			
			info.setOperator(personMember.getOperatorId());
			info.setAuthType(AuthType.IDENTITY);
			AuthInfo authInfo = defaultCertService.queryRealName(info);
			if(null==authInfo.getAuthType()){
				info.setAuthType(AuthType.REAL_NAME);
				authInfo = defaultCertService.queryRealName(info);
			}
			//如果用户实名认证通过,将认证类型传到页面
			if(null!=authInfo.getCertType() && AuthResultStatus.CHECK_PASS.getCode().equals(authInfo.getResult().getCode())){				
				data.put("certType", authInfo.getCertType().getCode());
			}
			//会员类型
			data.put("membertype", membertype);
			data.put("loginName", getLoginNameStar(personMember.getLoginName()));
			request.getSession().setAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN,
					CommonConstant.REFIND_LOGINPWD_TOKEN);
			
			super.setJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER, personMember);
			request.getSession().setAttribute(
					CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG, true);
			payPassWordCacheService.clear(token);
			ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX
					+ "/loginPwd/refind/goReSetLoginPwd","response",restP);		
			return modelAndView;
		}
		
		if(membertype.equals("2")){
			EnterpriseMember enterpriseMember = null;
			String jsonStr = payPassWordCacheService.load(token);
			if (StringUtils.isNotBlank(jsonStr)) {
				enterpriseMember = JSONArray.parseObject(jsonStr, EnterpriseMember.class);
			}
			if (enterpriseMember == null) {
				return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
			}
			
			//判断是否是最后一次发送的邮件
			if(!this.isLastToken(enterpriseMember, token)){				
				return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
			}
			
			super.setJsonAttribute(request.getSession(),
					CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, enterpriseMember);
			request.getSession().setAttribute(
					CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT, true);
			payPassWordCacheService.clear(token);
			ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX
					+ "/loginPwd/refind/goReSetLoginPwd");
			modelAndView.addObject("membertype",membertype);
			return modelAndView;
		}
		return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
		
	}
	/**
	 * 企业会员通过邮件找回密码->邮件校验
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checkRefindEmailEnt.htm")
	public ModelAndView checkRefindEmail1(HttpServletRequest request,
			OperationEnvironment env) throws Exception {
		String token = request.getParameter("token");
		String membertype = request.getParameter("membertype");
		if (StringUtils.isEmpty(token)) {
			return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
		}
		PersonMember personMember = null;
		String jsonStr = payPassWordCacheService.load(token);
		if (StringUtils.isNotBlank(jsonStr)) {
			personMember = JSONArray.parseObject(jsonStr, PersonMember.class);
		}
		if (personMember == null) {
			return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
		}
		//判断是否是最后一次发送的邮件
		if(!this.isLastToken(personMember, token)){				
			return new ModelAndView(CommonConstant.URL_PREFIX+"/loginPwd/refind/tokenTimeOut");
		}
		//营业执照号
		boolean isBindLicenseNo = false;		
		//组织机构代码证号
		boolean isBindOrganizationNo = false;
		String certType="";
		if("2".equals(membertype)){
			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(personMember.getMemberId());
			info.setAuthType(AuthType.REAL_NAME);
			info.setOperator(personMember.getOperatorId());
			AuthResultStatus state = defaultCertService.queryAuthType(info);
			if (state.getCode().equals("PASS")) {
				EnterpriseMember enterpriseMember=queryEntMemberNo(personMember.getLoginName(),env);
				if(null!=enterpriseMember && StringUtils.isNotBlank(enterpriseMember.getLicenseNo())){
					isBindLicenseNo=true;
				}
				if(null!=enterpriseMember && StringUtils.isNotBlank(enterpriseMember.getOrganizationNo())){
					isBindOrganizationNo=true;
				}
				//如果营业执照和组织机构代码同时存在 取营业执照
				if(isBindLicenseNo&&isBindOrganizationNo){
					certType="licenseNo";
				}else if(isBindLicenseNo){
					certType="licenseNo";
				}else if(isBindOrganizationNo){
					certType="organizationNo";
				}			
			}
		}
		request.getSession().setAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN,
				CommonConstant.REFIND_LOGINPWD_TOKEN);
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		//企业验证类型
		data.put("certType", certType);
		//会员类型
		data.put("membertype", membertype);
		data.put("loginName", getLoginNameStar(personMember.getLoginName()));
		super.setJsonAttribute(request.getSession(),
				CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, personMember);
		request.getSession().setAttribute(
				CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT, true);
		payPassWordCacheService.clear(token);
		ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX
				+ "/loginPwd/refind/goReSetLoginPwd","response",restP);
		
		return modelAndView;
	}


	private PersonMember queryPersonMember(String loginName,
			OperationEnvironment env) throws BizException,
			MemberNotExistException {
		if (StringUtils.isEmpty(loginName)) {
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT);
		}
		PersonMember queryCondition = new PersonMember();
		if (loginName.contains("@")) {
			queryCondition.setMemberIdentity("EMAIL");
			queryCondition.setEmail(loginName);
		} else {
			queryCondition.setMemberIdentity("MOBILE");
			queryCondition.setMobile(loginName);
		}
		queryCondition.setLoginName(loginName);
		return memberService.queryMemberIntegratedInfo(queryCondition, env);
	}
	
	/*
	 * 查询企业认证证件号
	 */
	private EnterpriseMember queryEntMemberNo(String loginName,
			OperationEnvironment env) throws BizException,
			MemberNotExistException {
		EnterpriseMember enterpriseMember = new EnterpriseMember();
		enterpriseMember.setLoginName(loginName);
		enterpriseMember = memberService.queryCompanyMember(enterpriseMember, env);

		CompanyMemberInfo compInfo = memberService.queryCompanyInfo(enterpriseMember, env);
		enterpriseMember.setLicenseNo(compInfo.getLicenseNo());
		enterpriseMember.setOrganizationNo(compInfo.getOrganizationNo());

		return enterpriseMember;
	}
	
	/**
	 * 对登录名做星号处理
	 */
	private String getLoginNameStar(String loginName){		 
		   Pattern pattern = Pattern.compile(CommonConstant.PATTERN_MOBILE);
			Matcher matcher = pattern.matcher(loginName);
			if (matcher.matches()) {
				return StarUtil.mockMobile(loginName);
			} else {
				return StarUtil.mockEmail(loginName);
			}	  
	}	
	
	
	/**
	 * 保存会员发送邮件的最近一次Token
	 * @param member
	 * @param token
	 */
	public void saveLastToken(BaseMember member,String token){
		String emailSendKey=member.getMemberType().getCode()+"_"+member.getMemberId()+"findLoginPass";
		payPassWordCacheService.saveOrUpdate(emailSendKey, token);
		
	}		
		
	/**
	 * 判断邮箱是否发送过
	 * @param member
	 * @param token
	 * @return
	 */
	private boolean isLastToken(BaseMember member,String token){
		String emailSendKey=member.getMemberType().getCode()+"_"+member.getMemberId()+"findLoginPass";
		String info=payPassWordCacheService.load(emailSendKey);
		if(token.equals(info)){
			return true;
		}else{
			payPassWordCacheService.clear(token);
			return false;
		}
		
	}
	
	/**
	 * Q账户进行引导到实名认证页面
	 * @param memberById
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws MemberNotExistException
	 * @throws ServiceException
	 */
	public ModelAndView isActiveMember(BaseMember baseMember,OperationEnvironment env) throws BizException, MemberNotExistException, ServiceException {
		AuthInfo authInfo=new AuthInfo();
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);		
		//查看激活会员是否通过实名认证
		AuthInfoRequest info=new AuthInfoRequest();
		info.setAuthType(AuthType.IDENTITY);
		info.setMemberId(baseMember.getMemberId());
		authInfo=defaultCertService.queryRealName(info);
		String token = BankCardUtil.getUuid();
		String keyStr ="register_"+token+"_"+ env.getClientIp();
		payPassWordCacheService.put(keyStr, baseMember.getMemberId());			
		data.put("username", baseMember.getLoginName());
		data.put("token", token);		
		if(StringUtils.isNotBlank(authInfo.getMemberId())){	
			data.put("authCode", authInfo.getCertType().getCode());
			data.put("authName", authInfo.getAuthName());
			data.put("authNo", StarUtil.mockCommon(authInfo.getAuthNo()));
			return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input-person-auth", "response", restP);
		}else{
			return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input-person", "response", restP);
		}
	}
	
	
}
