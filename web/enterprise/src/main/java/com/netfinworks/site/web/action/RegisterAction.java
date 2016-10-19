package com.netfinworks.site.web.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.authorize.ws.dataobject.OperatorRole;
import com.netfinworks.cert.service.model.enums.UdcreditType;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.ma.service.base.model.CompanyInfo;
import com.netfinworks.ma.service.base.model.MerchantInfo;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.sars.SarsResponse;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.auth.inner.MemberAuthInnerService;
import com.netfinworks.site.ext.integration.auth.inner.OperatorAuthInnerService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.sars.DefaultSarsService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.BankCardUtil;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;

@Controller
@RequestMapping(value = "/register")
public class RegisterAction extends BaseAction {
	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource
	private MemberAuthInnerService memberAuthInnerService;

	@Resource
	private OperatorAuthInnerService operatorAuthInnerService;

	@Resource(name = "webResource")
	private WebDynamicResource webResource;

	@Resource(name = "cachedSessionService")
	private IVfSsoSession<SsoUser> userSessionService;

	@Resource(name = "memberService")
	private MemberService memberService;
	
	//@Resource(name = "passwordOcxService")
   // private PasswordOcxService passwordOcxService;
	
	@Resource(name = "defaultSarsService")
	private DefaultSarsService defaultSarsService;

	/**
	 * 企业会员注册
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/register-index.htm", method = RequestMethod.GET)
	public ModelAndView register(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-index", "response", restP);
	}

	/**
	 * 企业会员激活邮件发送
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/register-active.htm", method = RequestMethod.POST)
	public ModelAndView registerActive(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			String captcha_value = request.getParameter("captcha_value");
			String email = request.getParameter("username");
			
			String rs = KaptchaImageAction.validateRandCode(request, captcha_value);
			if (!"success".equals(rs)) {
				if("expire".equals(rs))
					restP.setMessage("验证码失效");
				else
					restP.setMessage("验证码错误");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-index", "response", restP);
			}
			
			EnterpriseMember queryCondition = new EnterpriseMember();
			queryCondition.setLoginName(email);
			try {
				memberService.queryCompanyMember(queryCondition, env);
				restP.setMessage("账户名已注册！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-index", "response", restP);
			} catch (MemberNotExistException e) {
				logger.info("会员不存在，可以注册");
			}
			

			request.getSession().setAttribute("SESSION_KAPTCHA_SESSION_KEY", "" + RadomUtil.createRadom());

			data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
			data.put("username", email);
			
			registRiskCtrl(email, request, env);
			
			if (sendMail(request, env)) {
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-active", "response", restP);
			}
		} catch (Exception e) {
			logger.error("注册激活失败:" + e.getMessage());
			restP.setMessage(getErrMsg(e.getMessage()));
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-result", "response", restP);
	}

	/**
	 * 企业会员信息录入
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/register-input.htm", method = RequestMethod.GET)
	public ModelAndView registerInput(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		try {
			String token = request.getParameter("token");
			String email = payPassWordCacheService.load(token + env.getClientIp());
			if ("".equals(email)) {
				logger.error("注册链接失效，邮箱地址为空");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-result", "response", restP);
			}
			data.put("email", email);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input", "response", restP);
		} catch (Exception e) {
			logger.error("注册激活失败:" + e.getMessage());
			restP.setMessage(getErrMsg(e.getMessage()));
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-result", "response", restP);

	}

	/**
	 * 企业会员注册
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/do-register.htm", method = RequestMethod.POST)
	public ModelAndView doRegister(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			String username = StringUtils.trim(request.getParameter("username"));
			String registerType = request.getParameter("registerType");
			String comname = StringUtils.trim(request.getParameter("comname"));
			String message = request.getParameter("message");

			String login_pw = decrpPassword(request, request.getParameter("login_pw"));
			String re_login_pw = decrpPassword(request, request.getParameter("re_login_pw"));
			String newPayPasswd = decrpPassword(request, request.getParameter("pay_pw"));
			String renewPayPasswd = decrpPassword(request, request.getParameter("pay_pw2"));

			// 清空随机因子
			deleteMcrypt(request);

			Map<String, String> errorMap = new HashMap<String, String>();
			if (!login_pw.equals(re_login_pw)) {
				errorMap.put("login_passwd_not_same", "login_passwd_not_same");
				restP.setErrors(errorMap);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input", "response", restP);
			} else if (!newPayPasswd.equals(renewPayPasswd)) {
				errorMap.put("pay_passwd_not_same", "pay_passwd_not_same");
				restP.setErrors(errorMap);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input", "response", restP);
			} else if (login_pw.equals(newPayPasswd)) {
				errorMap.put("login_passwd_eq_pay", "login_passwd_eq_pay");
				restP.setErrors(errorMap);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input", "response", restP);
			}

			// 创建未激活的会员
			EnterpriseMember enterprise = new EnterpriseMember();
			enterprise.setMemberType(MemberType.MERCHANT);
			enterprise.setLoginName(username);
			enterprise.setMemberName(comname);
			enterprise.setLoginPasswd(login_pw);
			enterprise.setEnterpriseName(comname);

			CompanyInfo comInfo = new CompanyInfo();
			comInfo.setCompanyName(comname);
			comInfo.setSummary(message);
			comInfo.setMemberName(comname);
			comInfo.setCompanyType(Integer.parseInt(registerType));// 0：企业，1：其他

			MerchantInfo merInfo = new MerchantInfo();
			merInfo.setMerchantType(0L);// 0：外部商户，1：内部商户，2：关联商户
			merInfo.setMerchantName(comname);

			MemberAndAccount member = defaultMemberService.createIntegratedCompanyMember(enterprise, comInfo, merInfo,
					env);

			// 支付密码设置
			PayPasswdRequest req = new PayPasswdRequest();
			req.setPassword(newPayPasswd);
			req.setAccountId(member.getAccountId());
			req.setOperator(member.getOperatorId());
			req.setClientIp(request.getRemoteAddr());
			CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
			if (!commRep.isSuccess()) {
				throw new Exception("会员支付密码设置失败");
			}

			// 默认分配权限
			AuthVO authVO = new AuthVO();
			authVO.setMemberId(member.getMemberId());
			authVO.setMemberRoleId("MR_EWALLET_ALL");
			authVO.setMemo("默认分配权限");
			authVO.setSourceId(super.getSourceId());
			if (!defaultMemberService.addRoleToMember(authVO)) {
				throw new Exception("默认分配权限失败");
			}
			
			// 默认分配权限
			AuthVO authVO2 = new AuthVO();
			authVO2.setMemberId(member.getMemberId());
			authVO2.setMemberRoleId("MR_POS_APP_ALL");
			authVO2.setMemo("默认分配权限");
			authVO2.setSourceId(POS_APP);
			if (!defaultMemberService.addRoleToMember(authVO2)) {
				throw new Exception("默认分配权限失败");
			}
			
			// 默认分配权限
			AuthVO authVO3 = new AuthVO();
			authVO3.setMemberId(member.getMemberId());
			authVO3.setMemberRoleId("MR_POS_CASHIER_APP_ALL");
			authVO3.setMemo("默认分配权限");
			authVO3.setSourceId(POS_APP);
			if (!defaultMemberService.addRoleToMember(authVO3)) {
				throw new Exception("默认分配权限失败");
			}

			// 创建默认角色
			createRole(member);

			// 默认绑定邮箱
			AuthVerifyInfo info = new AuthVerifyInfo();
			info.setMemberId(member.getMemberId());
			info.setVerifyType(VerifyType.EMAIL.getCode());
			info.setVerifyEntity(username);
			try {
				authVerifyService.createVerify(info, env);
			} catch (Exception e) {
				logger.error("", e);
			}

			EnterpriseMember enterpriseMember = new EnterpriseMember();
			enterpriseMember.setMemberId(member.getMemberId());
			enterpriseMember = memberService.queryCompanyMember(enterpriseMember, env);
			
			enterpriseMember.setOperator_login_name("admin");
			this.saveToken(enterpriseMember, request, response, CommonConstant.USERTYPE_MERCHANT);


			restP.setSuccess(true);
			
			// 增加注册成功的日志
			logger.info(LogUtil.generateMsg(OperateTypeEnum.REGISTER, 
			        enterpriseMember, env, "注册时间：" + DateUtil.format(new Date(), DateUtil.newFormat)));

		} catch (Exception e) {
			logger.error(e.getMessage());
			restP.setMessage(getErrMsg(e.getMessage()));
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-result", "response", restP);
	}
	
	private List<String> addFunctionsToRoleByAppId(String appId, AuthVO authVO) throws BizException
	{
		authVO.setSourceId(appId);
		List<FunctionVO> funcList = memberAuthInnerService.getFunctionListFromMember(authVO);
		List<String> funcIdList = new ArrayList<String>();
		for (FunctionVO funcVO : funcList) {
			funcIdList.add(funcVO.getFunctionId());
		}
		authVO.setFunctionList(funcIdList);
		operatorAuthInnerService.addFunctionListToOperatorRole(authVO);
		return funcIdList;
	}

	private void createRole(MemberAndAccount member) throws BizException {

		//创建管理员角色
		AuthVO authVO1 = new AuthVO();
		authVO1.setMemberId(member.getMemberId());
		authVO1.setOperatorRoleName("管理");
		authVO1.setMemo("管理");
		authVO1.setSourceId(super.getSourceId());
		OperatorRole role1 = operatorAuthInnerService.createOperatorRole(authVO1);
		authVO1.setOperatorRoleId(role1.getRoleId());
		//添加钱包权限
		List<String> funcIdList = addFunctionsToRoleByAppId(super.getSourceId(), authVO1);		
		//增加pos权限
		List<String> posFuncIdList = addFunctionsToRoleByAppId(POS_APP, authVO1);
		//增加POS收银权限
		List<String> posCashierFuncIdList = addFunctionsToRoleByAppId(POS_CASHIER_APP, authVO1);


		// 分配默认管理员角色
		AuthVO assignVO = new AuthVO();
		assignVO.setMemberId(member.getMemberId());
		assignVO.setOperatorId(member.getOperatorId());
		assignVO.setMemo("分配操作员角色");
		assignVO.setSourceId(super.getSourceId());
		assignVO.setOperatorRoleId(role1.getRoleId());
		operatorAuthInnerService.addRoleToOperator(assignVO);

		// 申请角色
		List<String> funcIdList2 = new ArrayList<String>();
		funcIdList2.addAll(funcIdList);
		String applyRole = webResource.getApplyrole();
		if (applyRole != "") {
			String[] applyroles = applyRole.split(",");
			for (String role : applyroles) {
				if (funcIdList2.contains(role)) {
					funcIdList2.remove(role);
				}
			}
		}
		AuthVO authVO2 = new AuthVO();
		authVO2.setMemberId(member.getMemberId());
		authVO2.setOperatorRoleName("申请");
		authVO2.setMemo("申请");
		authVO2.setSourceId(super.getSourceId());
		OperatorRole role2 = operatorAuthInnerService.createOperatorRole(authVO2);
		authVO2.setOperatorRoleId(role2.getRoleId());
		authVO2.setFunctionList(funcIdList2);
		operatorAuthInnerService.addFunctionListToOperatorRole(authVO2);

		// 审核角色
		List<String> funcIdList3 = new ArrayList<String>();
		funcIdList3.addAll(funcIdList);
		String authRole = webResource.getAuthrole();
		if (authRole != "") {
			String[] authroles = authRole.split(",");
			for (String role : authroles) {
				if (funcIdList3.contains(role)) {
					funcIdList3.remove(role);
				}
			}
		}
		AuthVO authVO3 = new AuthVO();
		authVO3.setMemberId(member.getMemberId());
		authVO3.setOperatorRoleName("审核");
		authVO3.setMemo("审核");
		authVO3.setSourceId(super.getSourceId());
		OperatorRole role3 = operatorAuthInnerService.createOperatorRole(authVO3);
		authVO3.setOperatorRoleId(role3.getRoleId());
		authVO3.setFunctionList(funcIdList3);
		operatorAuthInnerService.addFunctionListToOperatorRole(authVO3);
		
		// POS管理员
		AuthVO authVO4 = new AuthVO();
		authVO4.setMemberId(member.getMemberId());
		authVO4.setOperatorRoleName("POS管理员");
		authVO4.setMemo("POS管理员");

		authVO4.setSourceId(super.getSourceId());
		OperatorRole role4 = operatorAuthInnerService.createOperatorRole(authVO4);
		authVO4.setOperatorRoleId(role4.getRoleId());
		authVO4.setSourceId(POS_APP);
		authVO4.setFunctionList(posFuncIdList);
		operatorAuthInnerService.addFunctionListToOperatorRole(authVO4);
		
		authVO4.setSourceId(POS_CASHIER_APP);
		authVO4.setFunctionList(posCashierFuncIdList);
		operatorAuthInnerService.addFunctionListToOperatorRole(authVO4);
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
		String bizType = BizType.REGISTER_EMAIL.getCode();
		String token = BankCardUtil.getUuid();
		String email = request.getParameter("username");
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + ""
				+ request.getContextPath() + "/register/register-input.htm?token=" + token;
		logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
		Map<String, Object> objParams = new HashMap<String, Object>();
		objParams.put("userName", email);
		objParams.put("activeUrl", activeUrl);
		boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType, objParams);
		logger.info("发送至{}邮箱的结果：{}", email, emailResult);
		if (emailResult) {
			// 调用统一缓存
			String keyStr = token + env.getClientIp();
			payPassWordCacheService.put(keyStr, email);
		}
		return emailResult;
	}

	/**
	 * 异步发送邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/send-mail.htm", method = RequestMethod.GET)
	@ResponseBody
	private RestResponse ajaxSendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String bizType = BizType.REGISTER_EMAIL.getCode();
		String token = BankCardUtil.getUuid();
		String email = request.getParameter("username");
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + ""
				+ request.getContextPath() + "/register/register-input.htm?token=" + token;
		logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
		Map<String, Object> objParams = new HashMap<String, Object>();
		objParams.put("userName", email);
		objParams.put("activeUrl", activeUrl);
		boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType, objParams);
		logger.info("发送至{}邮箱的结果：{}", email, emailResult);
		if (emailResult) {
			// 调用统一缓存
			String keyStr = token + env.getClientIp();
			payPassWordCacheService.put(keyStr, email);
		}
		restP.setSuccess(true);
		return restP;
	}

	/*
	 * 保存SsoUser并将其对应的token保存至cookie(单点登录用)
	 */
	private void saveToken(EnterpriseMember member, HttpServletRequest request, HttpServletResponse response,
			String userType) {
		try {
			if ((null != member) && (null != member.getMemberId())) {
				SsoUser ssoUser = new SsoUser();
				ssoUser.setId(member.getMemberId());
				ssoUser.setLoginName(member.getLoginName());
				ssoUser.setName(member.getMemberName());
				ssoUser.setOpId(member.getOperatorId());
				ssoUser.setOpName(member.getOperator_login_name());
				ssoUser.setUserType(userType);
				String token = userSessionService.create(ssoUser);
				if (logger.isInfoEnabled()) {
					logger.info("[ent]saveToken,token={}", token);
				}
				if (token != null) {
					VfSsoCookie.setCookie(token, response);
					logger.info("vfssoDomain: " + webResource.getVfssoDomain());
					for (String domain : webResource.getVfssoDomain().split(",")) {
						VfSsoCookie.setCookie(token, response, domain, null);
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * 风险控制(发送邮件之前)
	 * @param member
	 * @param request
	 * @param env
	 * @throws Exception
	 */
	private void registRiskCtrl(String email,HttpServletRequest request,OperationEnvironment env) throws Exception{
		String device_id = "";
		for(Cookie cookie:request.getCookies()){
			if(StringUtils.equalsIgnoreCase(cookie.getName(), "UDCREDIT_DEVICEID")){
				device_id = cookie.getValue();
				break;
			}
		}
		Map<String,String> params = new HashMap<String, String>();
		params.put("user_ip_address", env.getClientIp());
		params.put("device_id", device_id);
		params.put("email", email);
		SarsResponse resp = null;
		try {
			resp = defaultSarsService.riskControl(UdcreditType.REGISTER.getStrategyCode(), UdcreditType.REGISTER.getScenarioCode(), params, env);
		} catch (BizException e) {
			logger.error("风控调用异常",e);
		}
		if(resp != null && !StringUtils.equalsIgnoreCase(resp.getResult(),"0000")){
			throw new Exception("风控拦截,您的注册异常");
		}
	}
}
