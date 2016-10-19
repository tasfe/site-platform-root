/**
 * file name    RegisterAction.java
 * description  
 * @author      tiantao
 * @version     1.0
 * create at    2014年5月21日 下午4:32:56
 * copyright 2013 weihui 
 */
package com.netfinworks.site.web.action;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import com.kjt.unionma.api.common.model.PlatformInfo;
import com.kjt.unionma.api.register.request.RegisterRequestParam;
import com.kjt.unionma.api.register.response.RegisterResponse;
import com.kjt.unionma.api.register.service.RegisterFacadeWS;
import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.SampleExceptionUtils;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.UnionmaPerfectionIdentityReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CardType;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.sars.DefaultSarsService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.unionma.UnionmaRegisterService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.util.BankCardUtil;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.vfsso.cas.VfSsoConstants;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.vfsso.session.enums.SessionStatusKind;
import com.netfinworks.vfsso.session.exceptions.SessionException;

/**
 * className		RegisterAction    
 * description	
 * @author		Tiantao
 * @version 	2014年5月21日 下午4:32:56
 */
@Controller
public class RegisterAction extends BaseAction {
	
	private static final Logger logger = LoggerFactory.getLogger(RegisterAction.class);
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "defaultSmsService")
    private DefaultSmsService defaultSmsService;
	
	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
	@Resource(name = "webResource")
    private WebDynamicResource      webResource;
	
	@Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 
	
    @Resource(name="accountService")
    private AccountService accountService;
    
    @Resource(name = "authVerifyService")
    private AuthVerifyService       authVerifyService;
    
    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "cachedSessionService")
	private IVfSsoSession<SsoUser> userSessionService;
	
	@Resource(name = "loginPasswdService")
	private LoginPasswdService loginPasswdService;
	
	@Resource(name = "defaultCertService")
    private DefaultCertService defaultCertService;
	
	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;
	
	@Value("${sendMessageNumber}")
	private String             sendMessageNumber;
	
	@Resource(name = "defaultSarsService")
	private DefaultSarsService defaultSarsService;
	@Autowired
	private RegisterFacadeWS registerFacadeWS;
	
	@Resource(name = "unionmaRegisterService")
	private UnionmaRegisterService unionmaRegisterService;
	
	@RequestMapping(value="/register/main.htm")
	public ModelAndView main(HttpServletRequest request,HttpServletResponse response) {
		RestResponse restP = new RestResponse();
		RegisterPageTitleOper.setRegisterPageTitleUser(request);
		return new ModelAndView(CommonConstant.URL_PREFIX +  ResourceInfo.REGISTER.getUrl(), "response", restP);
	}
	@RequestMapping(value="/register/accountActive.htm")
	public ModelAndView accountActive(HttpServletRequest request,HttpServletResponse response) {
		RestResponse restP = new RestResponse();
		RegisterPageTitleOper.setRegisterPageTitleUserActive(request);
		return new ModelAndView(CommonConstant.URL_PREFIX +  ResourceInfo.REGISTER.getUrl(), "response", restP);	
	}
	
	@RequestMapping(value = "/register/{regType}/{memberIdentity}/active.htm",method=RequestMethod.GET)
	public ModelAndView showRegisterActive(@PathVariable String regType,@PathVariable String memberIdentity,HttpServletRequest request,HttpServletResponse response, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();	
		data.put("regType", regType);
		data.put("memberIdentity", memberIdentity);
		String returnHTML="register-"+regType+"-"+memberIdentity+"-new";
		return new ModelAndView(CommonConstant.URL_PREFIX +"/register/"+returnHTML, "response", restP);
	}
	
	@RequestMapping(value = "/register/{regType}/{memberIdentity}/active.htm",method=RequestMethod.POST)
	public ModelAndView registerActive(@PathVariable String regType,@PathVariable String memberIdentity,HttpServletRequest request,HttpServletResponse response, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();		
		String returnHTML = "register-"+regType+"-"+memberIdentity+"-new";
		String rstUrl 	  = CommonConstant.URL_PREFIX + "/register/"+returnHTML;
		String memberId   = null;
		try {			
			String username1 = request.getParameter("username1"); //手机会员	
			String username2 = request.getParameter("username2"); //邮箱会员
			String username3 = request.getParameter("username3"); //企业会员
			String otpValue=request.getParameter("otpValue");	
			String username = "";
			String personIdentiy = ""; // 人员身份 1.个人会员  2.企业会员
			if("person".equals(regType)){
				if("mobile".equals(memberIdentity)){
					username=username1;
					personIdentiy = MemberType.PERSONAL.getCode();
				}else{
					username=username2;
					personIdentiy = MemberType.PERSONAL.getCode();
				}				
			}else{
				username = username3;
				personIdentiy = MemberType.ENTERPRISE.getCode();
			}
			data.put("username", username);
			data.put("regType", regType);
			data.put("memberIdentity", memberIdentity);
			
			restP.setData(data);
			if(!validateOtpValue(memberIdentity,username,otpValue,request,env)){
				restP.setMessage("校验码输入错误 ！");
				return new ModelAndView(rstUrl, "response", restP);
			}
			
			PlatformInfo platformInfo = UnionmaConvert.createKjtPlatformInfo();
			
			RegisterRequestParam requestParam = new RegisterRequestParam();
			String registerType  = memberIdentity.toUpperCase(); // 注册类别
			String loginName	 = username;// 登录名
			String spreading=request.getParameter("spreading");
			/**
			 * 设置注册参数
			 */
			requestParam.setPlatformInfo(platformInfo);

			requestParam.setRegisterType(registerType);
			requestParam.setLoginName(loginName);
			requestParam.setPersonIdentiy(personIdentiy);
			requestParam.setExtention(spreading);
			logger.info("requestParam : {},loginName: {}", requestParam,loginName);
			RegisterResponse registerResponse = registerFacadeWS.register(requestParam);
			logger.info("registerResponse : {}", registerResponse);
			memberId = registerResponse.getMemberId();
			String rstMsg = "";
			/**
			 * 注册失败
			 */
			if (!registerResponse.getIsSuccess()) {
				String code = registerResponse.getResponseCode();
				rstMsg      = registerResponse.getResponseMessage();
				rstUrl		= CommonConstant.URL_PREFIX + "/register/"+returnHTML;
				
				if (SysResponseCode.ILLEGAL_ARGUMENT.getCode().equals(code)) {
					rstMsg = "注册参数验证失败";
				} else if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(code)) {
				}
			}
			restP.setMessage(rstMsg);
			
			
			if (StringUtils.isNotBlank(memberId)) {
				String token   = BankCardUtil.getUuid();
				String keyStr  = "register_"+token+"_"+ env.getClientIp();
				rstUrl         = "redirect:/register/register-input-person.htm?token=" + token;
				logger.info("注册registerActive放入缓存->key={}, memberId={}", keyStr, memberId);
				payPassWordCacheService.put(keyStr, memberId);
			}
			logger.info("rstUrl : {}", rstUrl);
			return new ModelAndView(rstUrl, "response", restP);	
		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage("操作失败");
		}
		return new ModelAndView(rstUrl);
	}	
	
	/**
	 * 
	 * @param memberIdentity  会员身份
	 * @param loginName  登录名
	 * @param otpValue   验证码	
	 * @param request
	 * @param env
	 * @return
	 */
	private boolean validateOtpValue(String memberIdentity,String loginName,String otpValue, HttpServletRequest request, OperationEnvironment env) {
		try {
			    if("email".equals(memberIdentity)||"enterprise".equals(memberIdentity)){
			    	String keyStr =loginName+"_"+otpValue.toLowerCase()+"_"+ env.getClientIp();
					String info = payPassWordCacheService.load(keyStr);	
					if ((null == info) || "".equals(info)) {						
						return false;
					}			    
				}else{				
					AuthCodeRequest req = new AuthCodeRequest();
					req.setAuthCode(otpValue);
					req.setMobile(loginName);
					req.setMobileTicket(uesServiceClient.encryptData(loginName));
					req.setBizId(request.getSession().getId());
					if(isNeedActiveMember(loginName,env)){
						req.setBizType(BizType.ACTIVE_MOBILE.getCode());
					}else{
						req.setBizType(BizType.REGISTER_MOBILE.getCode());
					}
					boolean result = defaultSmsService.verifyMobileAuthCode(req, env);
					return result;
				}
			 }
			catch (Exception e) {			
				return false;
			}		
		return true;
	}	
	
	/**
	 * 验证图片验证码
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/register/checkCaptcha.htm")
	@ResponseBody
	public RestResponse checkCaptcha(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) {		
		RestResponse restP = new RestResponse();		
		String token = BankCardUtil.getUuid();
		restP.setSuccess(false);	
	    try {
	    	if(StringUtils.isNotBlank(request.getParameter("token"))){	    		
	    		payPassWordCacheService.clear(request.getParameter("token")+env.getClientIp());
	    	}
			if (!KaptchaImageAction.validateRandCode(request, request.getParameter("code"))) {
				restP.setMessage("验证码错误");				
			}else{
				String keyStr = token + env.getClientIp();
				payPassWordCacheService.put(keyStr, request.getParameter("code"));
				restP.setCode(token);
				restP.setSuccess(true);	
				return restP;
			}
		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage("系统内部错误！");
			return restP;
		}
		return restP;		
	}	
	

	/**
	 * 异步发送邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/register/send-mail.htm", method = RequestMethod.POST)
	@ResponseBody
	private RestResponse ajaxSendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String registertoken=request.getParameter("username")+"_"+DateUtils.getDateString();
		//发送邮件前图片验证码校验
		String key = request.getParameter("captcha_value") + env.getClientIp();
    	String info = payPassWordCacheService.load(key);	
    	if ((null == info) || "".equals(info)) {
    		restP.setMessage("验证码错误");        		
			restP.setSuccess(false);
			return restP;
		}else{
			payPassWordCacheService.clear(key);
		}
    	int sendCount=1;
    	if(StringUtils.isNotBlank(payPassWordCacheService.load(registertoken))){
    		sendCount=Integer.valueOf(payPassWordCacheService.load(registertoken));
    	}
    	if(sendCount>Long.valueOf(sendMessageNumber)){
    		restP.setMessage("此邮箱发送校验码已经达到上限，请在24小时后重试");
    		payPassWordCacheService.clear(request.getParameter("captcha_value") + env.getClientIp());        		
			restP.setSuccess(false);
			return restP;
    	}else{
    		String email = request.getParameter("username");
    		String bizType;    		
    		 if(isNeedActiveMember(email,env)){
            	 //激活模板              	 
              	bizType = BizType.ACTIVE_EMAIL.getCode();	
            }else{
            	 //注册模板            	
            	bizType = BizType.REGISTER_EMAIL_PER.getCode();	
            }     		
    		String randomCode=RandomStringUtils.randomAlphanumeric(4);
    		String activeUrl = randomCode;	
    		logger.info("激活邮箱的地址：{},发送邮件验证码：{}", email, randomCode);
    		Map<String, Object> objParams = new HashMap<String, Object>();
    		objParams.put("userName", email);
    		objParams.put("activeUrl", activeUrl);		
    		boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType, objParams);
    		logger.info("发送至{}邮箱的结果：{}", email, emailResult);
    		if (emailResult) {
    			// 调用统一缓存
    			String keyStr =email+"_"+randomCode.toLowerCase()+"_"+ env.getClientIp();
    			payPassWordCacheService.put(keyStr, email);
    			payPassWordCacheService.saveOrUpdate(registertoken, String.valueOf(++sendCount)); 
    		}
    		restP.setSuccess(true);
    	}		
		return restP;
	}
    
	 /**
     * 注册-2  完善账号信息(企业)
     * @param request
     * @param response
     * @param env
     * @return
     */
	@RequestMapping(value = "/register/register-input-enterprise.htm")
	public ModelAndView registerEnt(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		String key = request.getParameter("token");
		String info = payPassWordCacheService.load(key + env.getClientIp());
		if ((null == info) || "".equals(info)) {
			restP.setMessage("注册链接已经失效");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-result", "response", restP);
		}
		data.put("username", info);
		Pattern pattern = Pattern.compile(CommonConstant.PATTERN_MOBILE);
		Matcher matcher = pattern.matcher(info);
		if (!matcher.matches()) {
			data.put("isEmail", "true");
		}

		return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input-enterprise", "response", restP);
	}
    
	 /**
     * 注册-2  完善账号信息(个人)
     * @param request
     * @param response
     * @param env
     * @return
     */
	@RequestMapping(value = "/register/register-input-person.htm")
	public ModelAndView registerPer(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		String token = request.getParameter("token");
		String keyStr ="register_"+token+"_"+ env.getClientIp();
	    String memberId = payPassWordCacheService.load(keyStr);
	    logger.info("注册registerPer->key={}, memberId={}", keyStr, memberId);
	    
	    if(StringUtils.isBlank(token)){
	    	return new ModelAndView(CommonConstant.URL_PREFIX +  ResourceInfo.REGISTER.getUrl(), "response", restP);
	    }
		if (StringUtils.isEmpty(memberId)) {
			restP.setMessage("注册链接已经失效");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-result", "response", restP);
		}
		try {
			BaseMember member=memberService.queryMemberById(memberId, env);			
			data.put("username", member.getLoginName());
			data.put("token", token);
			//2 企业会员
			if("2".equals(member.getMemberType().getCode())){
				return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input-enterprise", "response", restP);
			}else{
				AuthInfoRequest info=new AuthInfoRequest();
				info.setAuthType(AuthType.IDENTITY);
				info.setMemberId(member.getMemberId());
				//查看激活会员是否通过实名认证
				AuthInfo authInfo=defaultCertService.queryRealName(info);
				if(StringUtils.isNotBlank(authInfo.getMemberId())){	
					data.put("authCode", authInfo.getCertType().getCode());
					data.put("authName", authInfo.getAuthName());
					data.put("authNo", StarUtil.mockCommon(authInfo.getAuthNo()));
					return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input-person-auth", "response", restP);
				}else{
					return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input-person", "response", restP);
				}				
			}
		} catch (BizException e) {
			logger.info("系统内部错误",e);	
		} catch (MemberNotExistException e) {
			logger.info("系统内部错误",e);	
		} catch (ServiceException e) {
			logger.info("系统内部错误",e);	
		}	
		return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-result", "response", restP);
	}
    /**
     * 注册-2  完善账号信息
     * @param request
     * @param response
     * @param env
     * @return
     */
	@RequestMapping(value = "/register/do-regiseter.htm")
	@ResponseBody
	public RestResponse doRegister(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		
		String login_pw  = decrpPassword(request, request.getParameter("login_pw"));
		String login_pw2 = decrpPassword(request, request.getParameter("re_login_pw"));
		
		String payPw    = decrpPassword(request, request.getParameter("pay_pw")); // 支付密码
		String payPw2    = decrpPassword(request, request.getParameter("pay_pw2")); // 确认支付密码
		
		String realname  = request.getParameter("realname");
		String cardtype	 = request.getParameter("cardtype");
		String idcard 	 = request.getParameter("idcard");
		String token 	 = request.getParameter("token");		
		String keyStr 	 = "register_"+token+"_"+ env.getClientIp();
		String memberId = payPassWordCacheService.load(keyStr);
		
		if ((null == memberId) || "".equals(memberId)) {
			restP.setMessage("注册链接已经失效");
			restP.setRedirect(request.getContextPath() + "/register/register-input-person.htm?token=" + token);
			return restP;
		}
		
		if (!payPw.equals(payPw2)) {
			restP.setMessage("支付确认密码与原支付密码不同");
			restP.setRedirect(request.getContextPath() + "/register/register-input-person.htm?token=" + token);
			return restP;
		}
		
		BaseMember member=null;
		PersonMember user=new PersonMember();
		PersonMember personMember;
		boolean passVerified=true;
		try {			
			member	= memberService.queryMemberById(memberId, env);			 
			//注册会员是否已激活
			boolean isActiveMember=member.getStatus()==MemberStatus.NOT_ACTIVE?false:true;		
			if(!isActiveMember){
				//查询会员是否实名认证
				AuthInfoRequest authInfoReq = new AuthInfoRequest();
				authInfoReq.setMemberId(member.getMemberId());
				authInfoReq.setAuthType(AuthType.IDENTITY);
				authInfoReq.setOperator(member.getOperatorId());
				AuthInfo info = defaultCertService.queryRealName(authInfoReq);
			    if(null!=info&&StringUtils.isNotBlank(info.getAuthNo())&&AuthResultStatus.CHECK_PASS.getCode().equals(info.getResult().getCode())&&!info.getAuthNo().equals(idcard)){
			    	restP.setMessage("您输入的证件号和您的实名认证信息不一致,请您重新输入！");
					restP.setRedirect(request.getContextPath() + "/register/register-input-person-auth.htm?token=" + token);
					return restP;
			    }
				
				//进行会员激活	
				PersonMember person=new PersonMember();
				person.setMemberId(memberId);
				memberService.activatePersonalMemberInfo(person, env);				
			}			
			 UnionmaPerfectionIdentityReq req=new UnionmaPerfectionIdentityReq();
			 req.setMemberId(memberId);
			 req.setLoginPassword(login_pw);
			 req.setEnsureLoginPassword(login_pw2);
			 req.setPayPassword(payPw);
			 req.setEnsurePayPassword(payPw2);
			 req.setRealName(realname);
			 req.setIdCardNum(idcard);
			 req.setEnsureIdCardNum(idcard);
			 req.setIdCardType(cardtype);
			 Map<String, Object> map = new HashMap<String, Object>(); 
			 map.put("isEncrypt", "true");//扩展字段
			 req.setExtention(JSONObject.toJSONString(map));
			 UnionmaBaseResponse Response=unionmaRegisterService.perfectionIdentity(req);
			 restP.setRedirect(request.getContextPath() + "/my/result.htm");	
			 if (Response.getResponseCode().equals(SysResponseCode.NEWLOGINPWD_EQUALS_OLDPWD.getCode())) {
				 restP.setMessage("新旧密码不能一致");				
				 return restP;
			}else if(Response.getResponseCode().equals(SysResponseCode.LOGIN_PWD_HAS_SET.getCode())){
				restP.setMessage("您的帐号已设置密码，请登录后修改密码");				
				 return restP;
			}else if(Response.getResponseCode().equals(SysResponseCode.MEMBER_NOT_EXIST.getCode())){
				restP.setMessage("会员不存在");				
				 return restP;
			}else if(Response.getResponseCode().equals(SysResponseCode.IDENTITY_NOT_EQUALS_REALNAME.getCode())){
				passVerified=false;
			}else if(Response.getResponseCode().equals(SysResponseCode.AUTH_REAL_NAME_FAIL.getCode())){
				passVerified=false;
			}		 
			 user.setLoginName(member.getLoginName());	
			 user.setStrRealName(realname);
			 user.setMemberId(memberId);
			 personMember = memberService.queryMemberIntegratedInfo(user, env);			 	 
			 
			 this.saveToken(personMember, request, response, restP, CommonConstant.USERTYPE_PERSON);
			 //登录成功后，清除注册时用的TOKEN
			 payPassWordCacheService.clear(keyStr);
			 
		} catch (ServiceException e) {
			restP.setMessage("系统内部错误");
			return restP;			
		} catch (BizException e) {
			restP.setMessage("系统内部错误");
			return restP;			
		} catch (MemberNotExistException e) {
			restP.setMessage("未找到会员信息");		
			return restP;	
		}
		restP.setSuccess(true);
		restP.setRedirect(request.getContextPath() + "/my/result.htm?username="+member.getLoginName());	
		if("PERSONAL".equals(member.getMemberType().name())
				&&passVerified){
			if("idCard".equals(cardtype)){
				restP.setSuccess(true);
				restP.setRedirect(request.getContextPath() + "/my/register-input-payment.htm");
			}
		}
		return restP;		
	}
	
	/**
	 * 注册-3  设置支付方式
	 * @param req
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/register-input-payment.htm", method = RequestMethod.GET)
	public ModelAndView paymentPer(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(req);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("companyName", "" + user.getRealName().getPlaintext());
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/register/register-input-payment", "response",
				restP);
	}
	
	
	/**
	 * 注册-3  设置支付方式
	 * @param req
	 * @param env
	 * @return
	 * @throws Exception
	 */
	
	/**
	 * TODO 暂时使用以前代码
	 * @param req
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/do-register-input-payment.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse doPayment(HttpServletRequest req, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			PersonMember user = getUser(req);
			if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.CERTIFICATION_PERSONAL.getMsg(), user, env));
			}
			
			String bankaccount1 = req.getParameter("bankaccount1");
			String bankaccount2 = req.getParameter("bankaccount2");
			if (!bankaccount1.equals(bankaccount2)) {
				restP.setMessage("银行账户两次输入不一致");
				return restP;
			}
			BankAccRequest accReq = new BankAccRequest();
			accReq.setMemberId(super.getMemberId(req));
			accReq.setClientIp(req.getRemoteAddr());
			accReq.setBankAccountNum(bankaccount1);
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
			if ((list != null) && (list.size() > 0)) {
				// restP.setMessage("对不起，此银行卡已绑定，请重新选择另一张!");
				accReq.setBankcardId(list.get(0).getBankcardId());
				defaultBankAccountService.removeBankAccount(accReq);
			}

			String bank_account_name = user.getRealName().getPlaintext();
			String bankName = req.getParameter("bankName");
			String bankCode = req.getParameter("bankCode");
			String branchname = req.getParameter("branchname");
			String branchNo = req.getParameter("branchNo");

			try {
				CardBin cardBin = defaultPfsBaseService.queryByCardNo(bankaccount1, CommonConstant.ENTERPRISE_APP_ID);
				if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
					restP.setMessage("绑定银行卡不能是信用卡!");
					return restP;
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", bankaccount1);
				if (!defaultPfsBaseService.cardValidate(CommonConstant.ENTERPRISE_APP_ID, bankCode, bankaccount1)) {
					restP.setMessage("绑定银行卡卡号不正确!");
					return restP;
				}
			}
			BankAccRequest bankReq = new BankAccRequest();
			bankReq.setMemberId(super.getMemberId(req));
			bankReq.setOperatorId(user.getOperatorId());
			bankReq.setBankName(bankName);
			bankReq.setBankCode(bankCode);
			bankReq.setBranchName(branchname);
			bankReq.setBranchNo(branchNo);
			bankReq.setCardType(Integer.valueOf(CardType.JJK.getCode()));
			bankReq.setCardAttribute(1);
			bankReq.setPayAttribute("normal");
			bankReq.setProvName(req.getParameter("province"));
			bankReq.setCityName(req.getParameter("city"));
			bankReq.setBankAccountNum(bankaccount1);
			bankReq.setRealName(bank_account_name);
			bankReq.setIsVerified(0);// 0未认证 1已认证 2认证中 3认证失败
			bankReq.setMemberIdentity(user.getMemberIdentity());// 会员标识
			defaultCertService.verifyBankCard(bankReq,env);
			
//			UnionmaSetPaymentReq unionmaReq=new UnionmaSetPaymentReq();
//			unionmaReq.setMemberId(super.getMemberId(req));
//			unionmaReq.setRealName(bank_account_name);
//			unionmaReq.setPersonName(bank_account_name);
//			unionmaReq.setBankCode(bankCode);
//			unionmaReq.setBankName(bankName);
//			unionmaReq.setBankBranch(branchname);
//			unionmaReq.setBankBranchNo(branchNo);
//			unionmaReq.setBankAccountNo(bankaccount1);
//			unionmaReq.setProvince(req.getParameter("province"));
//			unionmaReq.setCity(req.getParameter("city"));
//			unionmaReq.setMemberIdentity(user.getMemberIdentity());
//			unionmaReq.setOperatorId(user.getOperatorId());
//			UnionmaBaseResponse response = unionmaRegisterService.setPayment(unionmaReq);
			restP.setMessage("个人会员实名认证成功");
			restP.setSuccess(true);	
		} catch (Exception e) {
			restP.setSuccess(false);
			logger.error("", e);
			// 检查异常是否提示给客户
			if (SampleExceptionUtils.isTip(e)) {
				String msg = e.getMessage();
				restP.setMessage(SampleExceptionUtils.restMsg(msg));
			} else {
				restP.setMessage("个人会员实名认证失败");
			}
		}
		return restP;
	}
	
	
	
    /**
     * 注册成功结果页
     * @param request
     * @param response
     * @return
     */
	@RequestMapping(value = "/my/result.htm")
	public ModelAndView result(HttpServletRequest request, HttpServletResponse response) {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
		if("ENTERPRISE".equals(user.getMemberType().name())){
			data.put("memberType", "ENTERPRISE");
			return new ModelAndView("redirect:/my/certification-guide.htm");
		}
		restP.setData(data);	
		
		String loginName= user.getLoginName();
		Pattern pattern = Pattern.compile(CommonConstant.PATTERN_MOBILE);
		Matcher matcher = pattern.matcher(loginName);
		if (matcher.matches()) {
			data.put("loginName", StarUtil.mockMobile(loginName));
		} else {
			data.put("loginName", StarUtil.mockEmail(loginName));
		}		
		restP.setSuccess(true);
		return new ModelAndView("main/register/register-result", "response", restP);
	}

	
	
	
	
	
	 /*
     * 保存SsoUser并将其对应的token保存至cookie(单点登录用)
     */
    private void saveToken(PersonMember member, HttpServletRequest request,
                           HttpServletResponse response, RestResponse restP, String userType) {
        String registerTitleName = RegisterPageTitleOper.getPageTitle(request);
        		
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
                    //为了使cookie能在多个系统中共享，需要给cookie设置多个系统共通的域名
                    logger.info("vfssoDomain: " + webResource.getVfssoDomain());
                    if(StringUtils.isNotBlank(webResource.getVfssoDomain())){
                    	for (String domain : webResource.getVfssoDomain().split(",")) {
                            VfSsoCookie.setCookie(token, response, domain, null);
                        }
                    }
                    String redirect_url = request.getParameter("returnUrl");
                    if (StringUtils.isBlank(redirect_url)) {
						redirect_url = request.getParameter("redirect_url");
					}
                    if (SessionStatusKind.pending.equals(ssoUser.getSessionStatus())) {
                    	//强制登录，踢掉上一个登录的人
                        userSessionService.forceIn(token, ssoUser);
                        if (StringUtils.isNotEmpty(redirect_url) && !"".equals(redirect_url)) {
                            String redirectUrl = URLDecoder.decode(redirect_url,request.getCharacterEncoding());
                            StringBuffer url = request.getRequestURL();  
                            String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/").toString();
                            //如果跨域，则需要把token传过去
                            if(!redirectUrl.contains(tempContextUrl)){
                                if(redirectUrl.contains("?")){
                                    redirectUrl = redirectUrl + "&" + VfSsoConstants.CAS_TOKEN_COOKIE_NAME +"=" + token;
                                }else{
                                    redirectUrl = redirectUrl + "?" + VfSsoConstants.CAS_TOKEN_COOKIE_NAME +"=" + token;
                                }
                                
                            }
                            restP.setRedirect(redirectUrl);
                        } else {
                            restP.setRedirect(request.getContextPath() + "/my/home.htm");
                        }
                    } else if (StringUtils.isNotEmpty(redirect_url) && !"".equals(redirect_url)) {
                        String redirectUrl = URLDecoder.decode(redirect_url,request.getCharacterEncoding());
                        StringBuffer url = request.getRequestURL();  
                        String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/").toString();
                        //如果跨域，则需要把token传过去
                        if(!redirectUrl.contains(tempContextUrl)){
                            if(redirectUrl.contains("?")){
                                redirectUrl = redirectUrl + "&" + VfSsoConstants.CAS_TOKEN_COOKIE_NAME +"=" + token;
                            }else{
                                redirectUrl = redirectUrl + "?" + VfSsoConstants.CAS_TOKEN_COOKIE_NAME +"=" + token;
                            }
                            
                        }
                        restP.setRedirect(redirectUrl);
                    } else {
                        restP.setRedirect(request.getContextPath() + "/my/home.htm");
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
        } finally {
        	RegisterPageTitleOper.setRegisterPageTitle(request, registerTitleName);
        }
    }
    
    private void clearLoginInfo(HttpServletRequest req,HttpServletResponse resp){
        HttpSession session = req.getSession();
        session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
        session.invalidate();
        try {
            VfSsoCookie.removeCookie(resp);
            VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
            if(VfSsoUser.getCurrentToken()!=null) {
				userSessionService.remove(VfSsoUser.getCurrentToken());
			}
        } catch (SessionException e) {
            logger.error("单点登录注销用户信息失败",e);
        }
    }
    /**
     * 
     * @author tangL
     *
     */
    public static class RegisterPageTitleOper {
    	public static final String REGISTER_PAGE_TITLE 				= "registPageTitle";
    	public static final String REGISTER_PAGE_TITLE_USER 		= "用户注册";
    	public static final String REGISTER_PAGE_TITLE_USER_ACTIVE 	= "账户激活";
    	public static final String REGISTER_PAGE_TITLE_USER_FIND    = "找回密码";
    	// 用户注册
    	public static void setRegisterPageTitleUser(HttpServletRequest request) {
    		setRegisterPageTitle(request, RegisterPageTitleOper.REGISTER_PAGE_TITLE_USER );
    	}
    	// 账号激活
    	public static void setRegisterPageTitleUserActive(HttpServletRequest request) {
    		setRegisterPageTitle(request, RegisterPageTitleOper.REGISTER_PAGE_TITLE_USER_ACTIVE);
    	}
    	// 找回密码
    	public static void setRegisterPageTitleFind(HttpServletRequest request) {
    		setRegisterPageTitle(request, RegisterPageTitleOper.REGISTER_PAGE_TITLE_USER_FIND);
    	}
    	public static String getPageTitle(HttpServletRequest request) {
    		Object o = WebUtils.getSessionAttribute(request, RegisterPageTitleOper.REGISTER_PAGE_TITLE);
    		return o == null ? "" : o.toString();
    	}
    	public static void setRegisterPageTitle(HttpServletRequest request, String title) {
    		WebUtils.setSessionAttribute(request, RegisterPageTitleOper.REGISTER_PAGE_TITLE, title);
    	}
    	
    }
    
    
    /**
     *  是否需要激活
     * @param loginName
     * @param env
     * @return
     */
    public boolean isNeedActiveMember(String loginName, OperationEnvironment env){
    	//账户是否激活
		boolean isNeedActiveMember=false;        		
		try {
			PersonMember person = new PersonMember();
    		person.setLoginName(loginName);
			person = memberService.queryMemberIntegratedInfo(person,env);
			if(null!=person&&null!=person.getStatus()){
				//个人会员
				if(person.getMemberType()==MemberType.PERSONAL){
					isNeedActiveMember=person.getStatus()==MemberStatus.NOT_ACTIVE?true:false;	
				}
			}
		} catch (Exception e) {
			logger.info("{}未注册，无需激活",loginName);
		} 
    	return isNeedActiveMember;
    }
	
}
