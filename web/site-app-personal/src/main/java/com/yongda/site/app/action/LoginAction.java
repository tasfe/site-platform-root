package com.yongda.site.app.action;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kjt.unionma.api.common.model.PlatformInfo;
import com.kjt.unionma.api.register.request.RegisterRequestParam;
import com.kjt.unionma.api.register.response.RegisterResponse;
import com.kjt.unionma.api.register.service.RegisterFacadeWS;
import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.kjt.unionma.core.common.utils.MD5Util;
import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.cert.service.model.enums.UdcreditType;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.sars.SarsResponse;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.SarsTypeEnum;
import com.netfinworks.site.domain.enums.VerifyStatus;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.sars.DefaultSarsService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.site.ext.integration.unionma.LoginPasswordService;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
import com.netfinworks.ues.util.UesUtils;
import com.netfinworks.urm.domain.info.UserAccreditInfo;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.vfsso.session.enums.SessionStatusKind;
import com.netfinworks.vfsso.session.exceptions.SessionException;
import com.yongda.site.app.WebDynamicResource;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.form.wx.MobileMessageForm;
import com.yongda.site.app.form.wx.RegisterForm;
import com.yongda.site.app.form.wx.WxBindingForm;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.app.util.wx.LogUtil;
import com.yongda.site.app.util.wx.WeixinUtil;
import com.yongda.site.app.validator.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;

/**
 * <p>
 * 登录
 * </p>
 * 
 * @author eric
 * @version $Id: LoginAction.java, v 0.1 2013-7-18 下午6:07:43 Exp $
 */
@Controller
public class LoginAction extends BaseAction {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "memberService")
	private MemberService memberService;

	@Resource(name = "loginPasswdService")
	private LoginPasswdService loginPasswdService;

	@Resource(name = "webResource")
	private WebDynamicResource webResource;

	@Resource(name = "accountService")
	private AccountService accountService;

	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "cachedSessionService")
	private IVfSsoSession<SsoUser> userSessionService;

	@Resource(name = "defaultSarsService")
	private DefaultSarsService defaultSarsService;

	@Resource(name = "loginFacadeService")
	private LoginFacadeService loginFacadeService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "memberService")
	private MemberService commMemberService;

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "loginPasswordService")
	private LoginPasswordService loginPasswordService;

	@Resource(name = "payPasswordService")
	private PayPasswordService payPasswordService;

	@Resource(name = "passwordOcxService")
	private PasswordOcxService passwordOcxService;

	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;

	@Resource(name = "personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;

	@Resource(name = "xuCache")
	private XUCache<String> loginCache;

	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;

	@Autowired
	private RegisterFacadeWS registerFacadeWS;

	@Resource(name = "defaultLoginPasswdService")
	private DefaultLoginPasswdService defaultLoginPasswdService;
	
	@Value("${sendMessageNumber}")
	private String sendMessageNumber;

	@Value("${wx_appid}")
	private String wxappid;

	@Value("${wx_appsecret}")
	private String wxappsecret;

	private static final String MEMBER_ID_ENTITY = "mobile";

	private static final String UNIONID = "unionid";

	private static final String OPENID = "openid";

	private static final String DEFAULT_SALT = "ac5e940b94a51609788f38c6db70f39b";
	/**
	 * 登陆/绑定
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/wxlogin", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse login(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) {
		log.info("wxlogin...start");
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		HttpSession session = request.getSession();
		try {
			Map<String, String> paramMap = request.getParameterMap();
			logger.info("微信登录/绑定请求参数：{}", paramMap.toString());
			WxBindingForm wxform = new WxBindingForm();
			BeanUtils.populate(wxform, paramMap);
			// 校验提交参数
			String errorMsg = commonValidator.validate(wxform);

			if (StringUtils.isNotEmpty(errorMsg)) {
				restP.setMessage(errorMsg);
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("缺少必要的查询参数！" + errorMsg);
				return restP;
			}
			
			//校验图片验证码
			if(!validateRandCode(request,wxform.getVercode(),restP))
			{
				return restP;
			}
			
			/**
			 * 校验手机验证码 memberIdentity 会员身份 mobile loginName 登录名 otpValue 验证码
			 */
			Boolean flag = validateOtpValue("mobile", wxform.getUsername(),
					wxform.getMobileVercode(), request, env);
			if (!flag) {
				restP.setMessage("短信验证码有误");
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("短信验证码有误");
				session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER_ID);
				return restP;
			}
			
			/** 校验用户是否存在 *********************/
			if (!checkLoginUserName(wxform.getUsername(), restP, session, "0")) {
				restP = registerActive(request,response,wxform,env);
				if(!restP.isSuccess()){
					return restP;
				}
			}
			
			PersonMember person = new PersonMember();
			person.setLoginName(wxform.getUsername());
			person = memberService.queryMemberIntegratedInfo(person, env);

			PersonMember member = new PersonMember();
			member.setMemberId(person.getMemberId());
			member.setMemberName(person.getMemberName());
			member.setLoginName(person.getLoginName());
			member.setLockStatus(person.getLockStatus());
			if (MemberLockStatus.LOCKED.name().equals(
					member.getLockStatus().name())) {
				if (logger.isInfoEnabled()) {
					logger.info(LogUtil.appLog(
							OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(), member,
							env));
				}
				restP.setMessage("会员已被锁定，请联系客服！");
				return restP;
			}
			
			//注册会员是否已激活    针对云+用户
			boolean isActiveMember=person.getStatus()==MemberStatus.NOT_ACTIVE?false:true;		
			if(!isActiveMember)
			{	
				logger.error("注册会员未激活!");
				//进行会员激活	
				PersonMember per=new PersonMember();
				per.setMemberId(person.getMemberId());
				try{
					memberService.activatePersonalMemberInfo(per, env);	
				}catch(BizException e){
					log.error("激活会员失败：{}");
				}
				
				//新增一条认证信息
			    AuthVerifyInfo info = new AuthVerifyInfo();
	            info.setMemberId(person.getMemberId());
	            info.setVerifyType(VerifyType.CELL_PHONE.getCode());
	            info.setVerifyEntity(person.getLoginName());
	            info.setStatus(VerifyStatus.YES.getCode());
	            logger.error("认证请求：{}",info.toString());
	            ResponseCode verifyResult = authVerifyService.createVerify(info, env);
	            
	            if (ResponseCode.SUCCESS == verifyResult) {
	            	logger.error("绑定手机成功");
	            } else if (ResponseCode.DUPLICATE_VERIFY == verifyResult) {
	            	logger.error("认证信息重复");
	            } else {
	            	logger.error("绑定手机失败");
	            }
			}
			
			String unionid = "";
			try {
				unionid = (String) session.getAttribute(UNIONID);
			} catch (Exception e) {
				log.info("unionid:为空"+e);
			}
			String openid = (String) session.getAttribute(OPENID);
			List<UserAccreditInfo> list = personalFindBindThirdAccount
					.getUserInfo(person.getMemberId(), "weixin", openid, 1);
			if (list == null || list.size() == 0) {
				logger.info("客户未绑定 .......");
				/** 绑定账号 *********************/
				if (personalFindBindThirdAccount.addAccreditInfo(
						person.getMemberId(), "weixin", openid, unionid)) {
					restP.setMessage("绑定账号成功");
					logger.info("绑定账号成功");
				}
			}else{
				restP.setSuccess(false);
				restP.setMessage("该手机号已经绑定，请更换手机号");
				logger.info("绑定失败");
				return restP;
			}
			// 保存登录信息到session
			this.saveToken(member, request, response, restP,
					CommonConstant.USERTYPE_PERSON);
			if (logger.isInfoEnabled()) {
				logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
						member, env));
			}
			restP.setMessage("绑定成功");
			logger.info("绑定成功");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			restP.setSuccess(false);
			restP.setMessage("系统错误"+e);
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			logger.info("登录系统错误");
		}
		return restP;
	}

	/**
	 * 注册
	 * 
	 * @throws IOException
	 ********/
	public RestResponse registerActive(HttpServletRequest request,
			HttpServletResponse response,WxBindingForm rfm, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		String memberId = null;
		try {
			String personIdentiy = ""; // 人员身份 1.个人会员 2.企业会员
			if ("mobile".equals(MEMBER_ID_ENTITY)) {
				personIdentiy = MemberType.PERSONAL.getCode();
			}
			
			PlatformInfo platformInfo = UnionmaConvert.createKjtPlatformInfo();
			RegisterRequestParam requestParam = new RegisterRequestParam();
			String registerType = MEMBER_ID_ENTITY.toUpperCase(); // 注册类别
			String loginName = rfm.getUsername();// 登录名
			/**
			 * 设置注册参数
			 */
			requestParam.setPlatformInfo(platformInfo);

			requestParam.setRegisterType(registerType);
			requestParam.setLoginName(loginName);
			requestParam.setPersonIdentiy(personIdentiy);
			requestParam.setExtention(StringUtils.EMPTY);
			logger.info("requestParam : {},loginName: {}", requestParam,
					loginName);
			RegisterResponse registerResponse = registerFacadeWS
					.register(requestParam);
			logger.info("registerResponse : {}", registerResponse);
			memberId = registerResponse.getMemberId();
			String rstMsg = "";
			/**
			 * 注册失败
			 */
			if (!registerResponse.getIsSuccess()) {
				String code = registerResponse.getResponseCode();
				rstMsg = registerResponse.getResponseMessage();

				if (SysResponseCode.ILLEGAL_ARGUMENT.getCode().equals(code)) {
					rstMsg = "注册参数验证失败";
					logger.error("注册失败：{}" + rstMsg);
					restP.setSuccess(false);
					return restP;
				} else if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(
						code)) {
				}
			} else {
			}
			
			restP.setMessage(rstMsg);
			if (StringUtils.isNotBlank(memberId)) {
				String token = UUID.randomUUID().toString().replace("-", "");
				String keyStr = "register_" + token + "_" + env.getClientIp();
				logger.info("注册registerActive放入缓存->key={}, memberId={}",
						keyStr, memberId);
				payPassWordCacheService.put(keyStr, memberId);
			}
			
			if(!sendRiskCtrl(request,restP,env,rfm.getUsername(),"1")){
				return restP;
			}
			restP.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage("操作失败");
		}
		return restP;
	}

	// 微信回调 传递code参数
	@RequestMapping(value = "/callback")
	@ResponseBody
	public RestResponse callback(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) {
		logger.info("---callback授權回調---");
		RestResponse restP = new RestResponse();
		try {
			HttpSession session = request.getSession();
			String code = request.getParameter("code");
			// 传递code参数去获取我们的openid和token*************//*
			JSONObject resultAccesstoken = WeixinUtil.getAccreditAccesstoken(
					code, wxappid, wxappsecret);
			/*
			 * 通过token和openId拉取用户信息监测用户是否绑定了微信公众号
			 */
			JSONObject resultUserInfo = WeixinUtil.getUserInfo(
					resultAccesstoken.getString("access_token"),
					resultAccesstoken.getString("openid"));
			String headimgurl = null;// 用户头像的url地址
			session.setAttribute("wxUserInfo", resultUserInfo);// 微信的个人信息
			if (resultUserInfo != null
					&& !StringUtils.isBlank(resultUserInfo.getString("openid"))) {
				String openid = resultUserInfo.getString("openid");
				String unionid = "";
				try {
					unionid = resultUserInfo.getString("unionid");
				} catch (Exception e) {
					log.info("unionid:为空");
				}
				session.setAttribute(UNIONID, unionid);
				session.setAttribute(OPENID, openid);
				headimgurl = resultUserInfo.getString("headimgurl");
				CacheRespone cityWeatherData = loginCache.get(openid
						+ CommonConstant.CITY_WEATHER);
				String cityWeather = "";
				/** 城市天气 ***/
				headimgurl = StringUtils.isBlank(headimgurl) ? "暂无头像"
						: headimgurl;
				StringBuffer sb = new StringBuffer();
				sb.append(headimgurl + "&");
				if (!StringUtils.isBlank((String) cityWeatherData.get())) {
					cityWeather = (String) cityWeatherData.get();
					sb.append(cityWeather);
				} else {
					sb.append("no_data");
				}
				logger.info("cityWeather值："+sb.toString());
				Cookie cookie = new Cookie("cityWeather", URLEncoder.encode(
						sb.toString(), "utf-8"));
				cookie.setMaxAge(7200);
				response.addCookie(cookie);
				/** 查询是否绑定 *************/
				List<UserAccreditInfo> list = personalFindBindThirdAccount
						.getUserInfo(null, "weixin", openid, 2);
				if (CollectionUtils.isNotEmpty(list)) {
					String memberid = list.get(0).getMemberId();
					BaseMember person = new BaseMember();
					try{
						person = memberService.queryMemberById(memberid, env);
						PersonMember member = new PersonMember();
						member.setMemberId(person.getMemberId());
						member.setMemberName(person.getMemberName());
						member.setLoginName(person.getLoginName());
						member.setLockStatus(person.getLockStatus());
						if (MemberLockStatus.LOCKED.name().equals(
								member.getLockStatus().name())) {
							if (logger.isInfoEnabled()) {
								logger.info(LogUtil.appLog(
										OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(),
										member, env));
							}
							restP.setSuccess(true);
							restP.setMessage("会员已被锁定，请联系客服！");
							logger.info("会员已被锁定，请联系客服！");
							return restP;
						}
						
						//风控检查
						if(!sendRiskCtrl(request,restP,env,person.getLoginName(),"0")){
							return restP;
						}
						
						this.saveToken(member, request, response, restP,
								CommonConstant.USERTYPE_PERSON);
						if (logger.isInfoEnabled()) {
							logger.info(LogUtil.appLog(
									OperateeTypeEnum.LOGIN.getMsg(), member, env));
						}
					}catch(BizException e){
						logger.info("根据会员ID查询会员信息失败："+memberid);
					}catch(MemberNotExistException e){
						logger.info("根据会员ID查询会员信息失败："+memberid);
					}
				}
				// ** 已经绑定成功 跳转到公众号h5页面 *****//*
				restP.setSuccess(true);
			}
		} catch (BizException e) {
			logger.info("通过code获取token失败");
			restP.setSuccess(false);
			restP.setMessage(e.getMessage());
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
		} catch (ServiceException e) {
			restP.setSuccess(false);
			restP.setMessage("绑定账号失败");
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			logger.info("绑定账号失败");
		} catch (Exception e) {
			restP.setSuccess(false);
			restP.setMessage("系统错误"+e);
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			logger.info("调用微信系统错误"+e);
		}
		return restP;
	}

	/**
	 * setLoginPwd
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws BindException
	 */
	@RequestMapping(value = "/setLoginPwd", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse setLoginPwd(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		String loginPassword = (String) request.getParameter("loginPassword");
		if (StringUtils.isBlank(loginPassword)) {
			restP.setSuccess(false);
			restP.setMessage("登录密码不能为空");
			return restP;
		}
		if (!loginPassword.matches(RegexRule.PAY_PWD_VERIFY)) {
			restP.setSuccess(false);
			restP.setMessage("请输入7-23位数字,字母或特殊字符的组合密码");
			return restP;
		}
		PersonMember user = getUser(request);
		LoginPasswdRequest req = new LoginPasswdRequest();
		req.setOperatorId(user.getOperatorId());
		req.setValidateType(1);
		req.setPassword(MD5Util.MD5(loginPassword));
		CommResponse commRep;
		try {
			commRep = defaultLoginPasswdService.setLoginPassword(req);
			if (commRep.isSuccess()) {
				logger.info("设置登录密码成功");
				restP.setSuccess(true);
				restP.setMessage("注册成功！");
			} else {
				restP.setSuccess(false);
				restP.setMessage("设置登录密码失败");
			}
		} catch (ServiceException e) {
			restP.setSuccess(false);
			restP.setMessage("设置登录密码失败");
		}
		return restP;
	}

	/**
	 * 根据页面获取的手机号码，发送短信验证码
	 * 
	 * @param request
	 * @return
	 * @throws  
	 * @throws IOException
	 * @throws BindException
	 */
	@RequestMapping(value = "/sendMobileMessage", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendMobileMessage(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		RestResponse restP = new RestResponse();
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		String registertoken = null;
		try {
			MobileMessageForm mobileMessage = new MobileMessageForm();
			HttpSession session = request.getSession();

			Map<String, String> map = request.getParameterMap();
			BeanUtils.populate(mobileMessage, map);

			// 校验提交参数
			String errorMsg = commonValidator.validate(mobileMessage);

			if (StringUtils.isNotEmpty(errorMsg)) {
				restP.setMessage(errorMsg);
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("缺少必要的查询参数！" + errorMsg);
				return restP;
			}
			registertoken = mobileMessage.getUsername() + "_" + getDateString();
			//校验图片验证码
			if(!validateRandCode(request,mobileMessage.getVercode(),restP))
			{
				return restP;
			}
			//校验用户
			/*if ("0".equals(mobileMessage.getType())
					&& !checkLoginUserName(mobileMessage.getUsername(), restP,
							session, mobileMessage.getType())) {
				return restP;
			} else if ("1".equals(mobileMessage.getType())
					&& !checkLoginUserName(mobileMessage.getUsername(), restP,
							session, mobileMessage.getType())) {
				return restP;
			}*/
			
			
			//风控检查
			if("0".equals(mobileMessage.getType())){
				if(!sendRiskCtrl(request,restP,env,mobileMessage.getUsername(),mobileMessage.getType())){
					return restP;
				}
			}

			/*
			 * int sendCount = 1; if
			 * (StringUtils.isNotBlank(payPassWordCacheService
			 * .load(registertoken))) { sendCount =
			 * Integer.valueOf(payPassWordCacheService .load(registertoken)); }
			 * if (StringUtils.isNotBlank(sendMessageNumber) &&
			 * Long.valueOf(sendMessageNumber)>0 && sendCount >
			 * Long.valueOf(sendMessageNumber)) {
			 * log.error("此手机发送校验码已经达到上限，请在24小时后重试");
			 * restP.setMessage("此手机发送校验码已经达到上限，请在24小时后重试");
			 * payPassWordCacheService.clear(request
			 * .getParameter("captcha_value") + env.getClientIp());
			 * restP.setSuccess(true); return restP; } else {
			 */
			// 封装发送短信请求
			AuthCodeRequest req = new AuthCodeRequest();
			req.setMobile(mobileMessage.getUsername());
			req.setBizId(mobileMessage.getUsername());
			if (isNeedActiveMember(mobileMessage.getUsername(), env)) {
				// 激活模板
				req.setBizType(BizType.ACTIVE_MOBILE.getCode());
			} else {
				// 注册模板
				req.setBizType(BizType.REGISTER_MOBILE.getCode());
			}
			String ticket = defaultUesService.encryptData(mobileMessage
					.getUsername());
			req.setMobileTicket(ticket);
			req.setValidity(CommonConstant.VALIDITY);
			// 发送短信
			if (defaultSmsService.sendMessage(req, env)) {
				restP.setSuccess(true);
				/*
				 * payPassWordCacheService.saveOrUpdate(registertoken,
				 * String.valueOf(++sendCount));
				 */
			} else {
				log.error("发送短信失败,手机号码：" + mobileMessage.getUsername());
				restP.setMessage("");
				restP.setSuccess(false);
			}
			// }
			restP.setMessage("发送短信成功");
		} catch (Exception e) {
			log.error("发送短信失败:" + e.getMessage(), e.getCause());
			restP.setSuccess(false);
		}
		return restP;
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	@ResponseBody
	public RestResponse loginTest(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,
			String memberId) throws Exception {
		RestResponse restP = new RestResponse();

		BaseMember baseMember = memberService.queryMemberById(memberId, env);
		PersonMember member = new PersonMember();
		member.setMemberId(baseMember.getMemberId());
		member.setMemberName(baseMember.getMemberName());
		member.setLoginName(baseMember.getLoginName());
		member.setLockStatus(baseMember.getLockStatus());

		this.saveToken(member, request, response, restP,
				CommonConstant.USERTYPE_PERSON);
		if (logger.isInfoEnabled()) {
			logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(), member,
					env));
		}
		return restP;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	@ResponseBody
	public RestResponse logOutTest(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env)
			throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		this.clearLoginInfo(request, response);
		return restP;
	}

	// silentBack
	@RequestMapping(value = "/silentBack")
	@ResponseBody
	public RestResponse silentBack(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env)
			throws IOException, ServiceException {
		String code = request.getParameter("code");
		RestResponse restP = new RestResponse();
		// 传递code参数去获取我们的openid和token*************//*
		try {
			JSONObject userinfo = WeixinUtil.getAccreditAccesstoken(code,
					wxappid, wxappsecret);
			/** 查询是否绑定 *************/
			List<UserAccreditInfo> list = personalFindBindThirdAccount
					.getUserInfo(null, "weixin", userinfo.getString("openid"),
							2);
			if (CollectionUtils.isNotEmpty(list)) {
				log.info("微信已绑定silentBack");
				String memberid = list.get(0).getMemberId();
				BaseMember person = new BaseMember();
				person = memberService.queryMemberById(memberid, env);
				PersonMember member = new PersonMember();
				member.setMemberId(person.getMemberId());
				member.setMemberName(person.getMemberName());
				member.setLoginName(person.getLoginName());
				member.setLockStatus(person.getLockStatus());
				if (MemberLockStatus.LOCKED.name().equals(
						member.getLockStatus().name())) {
					if (logger.isInfoEnabled()) {
						logger.info(LogUtil.appLog(
								OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(),
								member, env));
					}
					restP.setMessage("会员已被锁定，请联系客服！");
					restP.setSuccess(true);
					return restP;
				}
				this.saveToken(member, request, response, restP,
						CommonConstant.USERTYPE_PERSON);
				if (logger.isInfoEnabled()) {
					logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
							member, env));
				}
			}
			// ** 已经绑定成功 跳转到公众号h5页面 *****//*
			restP.setSuccess(true);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (BizException e) {
			e.printStackTrace();
		} catch (MemberNotExistException e) {
			e.printStackTrace();
		}
		return restP;
	}

	/**
	 * 
	 * @param username
	 * @param env
	 * @param restP
	 * @param session
	 * @param type
	 *            0登陆，1注册
	 * @return
	 */
	public Boolean checkLoginUserName(String username, RestResponse restP,
			HttpSession session, String type) {
		OperationEnvironment env = new OperationEnvironment();
		PersonMember person = new PersonMember();
		person.setLoginName(username);
		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			if ("1".equals(type) && !StringUtils.isBlank(person.getMemberId())) {
				restP.setMessage("该手机号已注册");
				restP.setCode(CommonDefinedException.ACCOUNT_EXIST_ERROR
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("该手机号已注册");
				return false;
			}
			if (person.getLockStatus() == MemberLockStatus.LOCKED) {
				restP.setMessage("用户名被锁定");// 用户名被锁定
				restP.setSuccess(false);
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				return false;
			}
		} catch (Exception e) {
			if ("0".equals(type)) {
				restP.setMessage("用户不存在，请先注册");
				logger.error("用户不存在，请先注册！");
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
			} else {
				return true;
			}
			return false;
		}
		return true;
	}

	public static String getDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 是否需要激活
	 * 
	 * @param loginName
	 * @param env
	 * @return
	 */
	public boolean isNeedActiveMember(String loginName, OperationEnvironment env) {
		// 账户是否激活
		boolean isNeedActiveMember = false;
		try {
			PersonMember person = new PersonMember();
			person.setLoginName(loginName);
			person = memberService.queryMemberIntegratedInfo(person, env);
			if (null != person && null != person.getStatus()) {
				if (person.getMemberType() == MemberType.PERSONAL) {
					isNeedActiveMember = person.getStatus() == MemberStatus.NOT_ACTIVE ? true
							: false;
				}
			}
		} catch (Exception e) {
			logger.info("{}未注册，无需激活", loginName);
		}
		return isNeedActiveMember;
	}

	/**
	 * 
	 * @param memberIdentity
	 *            会员身份
	 * @param loginName
	 *            登录名
	 * @param otpValue
	 *            验证码
	 * @param request
	 * @param env
	 * @return
	 */
	private boolean validateOtpValue(String memberIdentity, String loginName,
			String otpValue, HttpServletRequest request,
			OperationEnvironment env) {
		try {
			if ("email".equals(memberIdentity)
					|| "enterprise".equals(memberIdentity)) {
				String keyStr = loginName + "_" + otpValue.toLowerCase() + "_"
						+ env.getClientIp();
				String info = payPassWordCacheService.load(keyStr);
				if ((null == info) || "".equals(info)) {
					return false;
				}
			} else {
				AuthCodeRequest req = new AuthCodeRequest();
				req.setAuthCode(otpValue);
				req.setMobile(loginName);
				req.setMobileTicket(uesServiceClient.encryptData(loginName));
				req.setBizId(loginName);
				if (isNeedActiveMember(loginName, env)) {
					req.setBizType(BizType.ACTIVE_MOBILE.getCode());
				} else {
					req.setBizType(BizType.REGISTER_MOBILE.getCode());
				}
				boolean result = defaultSmsService.verifyMobileAuthCode(req,
						env);
				return result;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	/**
	 * 风险控制
	 * @param member
	 * @param request
	 * @param env
	 * @throws Exception
	 */
	private Boolean riskCtrl(String remoteAddr,String loginName,
			String type,String deviceID,String traceId,RestResponse restP,OperationEnvironment env){
		Map<String,String> params = new HashMap<String, String>();
		params.put("user_ip_address", remoteAddr);
		params.put("device_id", deviceID);
		params.put("is_send_smscode","1");
		params.put("mobile", loginName);
		Boolean flag = "0".equals(type);
		String saltPasswd = "";
		String memberId = "";
		
		PersonMember person = new PersonMember();
		person.setLoginName(loginName);
		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			memberId = person.getMemberId();
			saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(person.getLoginPasswd())
					+ DEFAULT_SALT);
		} catch (Exception e1) {
			logger.info("查询用户信息失败");
		}
		
		params.put("user_id",StringUtils.isBlank(memberId)?StringUtils.EMPTY:memberId);
		params.put("account_password", flag?saltPasswd:StringUtils.EMPTY);//账户密码摘要  登录的时候需要添加  注册不传
		params.put("trace_id", traceId);
		String strategy = flag?UdcreditType.LOGIN.getStrategyCode():UdcreditType.REGISTER.getStrategyCode();
		String scene    = flag?UdcreditType.LOGIN.getScenarioCode():UdcreditType.REGISTER.getScenarioCode();
		logger.info("风控请求参数:{},攻略ID.strategy:{},场景ID.scene:{}",params.toString(),strategy,scene);
		SarsResponse resp = null;
		try {
			resp = defaultSarsService.riskControl(strategy, scene, params, env);
		} catch (BizException e) {
			logger.error("风控调用异常",e);
		}
		if(resp != null && !StringUtils.equalsIgnoreCase(resp.getResult(),"0000")){
			logger.info("风控拦截");
			restP.setMessage(resp.getMsg());
			restP.setSuccess(false);			
			return false;
		}
		return true;
	}
	
	//风控
	private Boolean sendRiskCtrl(HttpServletRequest request,RestResponse restP,OperationEnvironment env,
			String mobile,String type){
		String deviceId = "";
		String traceid = "";
		for(Cookie cookie:request.getCookies()){
			if(StringUtils.equalsIgnoreCase(cookie.getName(), "UDCREDIT_DEVICEID")){
				deviceId = cookie.getValue();
			}
			if(StringUtils.equalsIgnoreCase(cookie.getName(), "UDCREDIT_TRACEID")){
				traceid = cookie.getValue();
			}
		}
		return riskCtrl(env.getClientIp(),mobile,type,deviceId,traceid,restP,env);
	}
	private Boolean validateRandCode(HttpServletRequest request,String Vercode
			,RestResponse restP){
		/** 校验图形验证码 **************************/
		try {
			if (!ImageAction.validateRandCode(request,
					Vercode)) {
				restP.setMessage("图片验证码有误");
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("图片验证码有误");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/*
	 * 保存SsoUser并将其对应的token保存至cookie(单点登录用)
	 */
	private void saveToken(PersonMember member, HttpServletRequest request,
			HttpServletResponse response, RestResponse restP, String userType) {
		try {
			if ((null != member) && (null != member.getMemberId())) {
				clearLoginInfo(request, response);
				SsoUser ssoUser = new SsoUser();
				ssoUser.setId(member.getMemberId());
				logger.info("LoginName: " + member.getLoginName());
				ssoUser.setLoginName(member.getLoginName());
				ssoUser.setName(member.getMemberName());
				ssoUser.setUserType(userType);
				String token = userSessionService.create(ssoUser);
				logger.info("token: " + token);
				if (token != null) {
					VfSsoCookie.setCookie(token, response);
					// 为了使cookie能在多个系统中共享，需要给cookie设置多个系统共通的域名
					logger.info("vfssoDomain: " + webResource.getVfssoDomain());
					if (StringUtils.isNotBlank(webResource.getVfssoDomain())) {
						for (String domain : webResource.getVfssoDomain()
								.split(",")) {
							VfSsoCookie.setCookie(token, response, domain, "/");
						}
					}
					if (SessionStatusKind.pending.equals(ssoUser
							.getSessionStatus())) {
						// 强制登录，踢掉上一个登录的人
						userSessionService.forceIn(token, ssoUser);
					}
					restP.setSuccess(true);
				} else {
					logger.error("登录失败！");
					restP.setMessage("登录失败");
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage(getErrMsg(e.getMessage()));
		}
	}

	private void clearLoginInfo(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession();
		session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
		session.invalidate();
		try {
			VfSsoCookie.removeCookie(resp);
			VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
			if (StringUtils.isNotBlank(webResource.getVfssoDomain())) {
				for (String domain : webResource.getVfssoDomain().split(",")) {
					VfSsoCookie.removeCookie(resp, domain);
				}
			}
			if (VfSsoUser.getCurrentToken() != null) {
				userSessionService.remove(VfSsoUser.getCurrentToken());
			}
		} catch (SessionException e) {
			logger.error("单点登录注销用户信息失败", e);
		}
	}

}
