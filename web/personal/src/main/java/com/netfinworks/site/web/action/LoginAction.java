package com.netfinworks.site.web.action;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.response.MerchantListResponse;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.LoginNameEditRequest;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.request.loginPasswordEditReq;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.sars.SarsResponse;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
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
import com.netfinworks.site.web.ValidationConstants;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.form.LoginForm;
import com.netfinworks.site.web.util.ConvertObject;
import com.netfinworks.vfsso.cas.VfSsoConstants;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.vfsso.session.enums.SessionStatusKind;
import com.netfinworks.vfsso.session.exceptions.SessionException;

/**
 * <p>登录</p>
 * @author eric
 * @version $Id: LoginAction.java, v 0.1 2013-7-18 下午6:07:43  Exp $
 */
@Controller
public class LoginAction extends BaseAction {

    protected Logger                  log = LoggerFactory.getLogger(getClass());

    @Resource(name = "memberService")
    private MemberService             memberService;

    @Resource(name = "loginPasswdService")
    private LoginPasswdService        loginPasswdService;

    @Resource(name = "webResource")
    private WebDynamicResource        webResource;

    @Resource(name = "accountService")
    private AccountService            accountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name = "cachedSessionService")
    private IVfSsoSession<SsoUser>    userSessionService;
    
    @Resource(name = "defaultSarsService")
    private DefaultSarsService defaultSarsService;

    @Resource(name = "loginFacadeService")
    private LoginFacadeService loginFacadeService;
    
    @Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
    @Resource(name = "authVerifyService")
    private AuthVerifyService    authVerifyService;
    
    @Resource(name = "memberService")
	private MemberService commMemberService;
    
    @Resource(name = "defaultSmsService")
    private DefaultSmsService defaultSmsService;
    
    @Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 
    
    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
    
    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;
    
    @Resource(name = "loginPasswordService")
	private LoginPasswordService loginPasswordService; 
    
	@Resource(name = "payPasswordService")
	private PayPasswordService payPasswordService; 
	
	@Resource(name = "passwordOcxService")
    private PasswordOcxService passwordOcxService;
    
    /**
	 * 进入登录页面
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login/page.htm", method = RequestMethod.GET)
	public ModelAndView enter(ModelMap model) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		data.put("salt", DEFAULT_SALT);
		restP.setData(data);
		return new ModelAndView("login", "response", restP);
	}

    /**
     * 进入登录页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/login.htm", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse login(HttpServletRequest request, HttpServletResponse response,
                              @Validated LoginForm form, OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        try {

			if (!KaptchaImageAction.validateRandCode(request, form.getVercode())) {
				restP.setSuccess(false);
				restP.setMessage("验证码输入有误！");
				return restP;
			}

			// 登录密码解密
			String password = request.getParameter("unSafePwInput");
			
			if(password == null || password.equals("")){
				String mcrypt_key = request.getSession().getAttribute("mcrypt_key").toString();
				password=passwordOcxService.decrpPassword(mcrypt_key, request.getParameter("password"));
				
			}
			
			if (null == password) {
				restP.setSuccess(false);
				restP.setMessage("登录密码输入有误！");
				return restP;
			}
			form.setPassword(password);
			PersonMember person = new PersonMember();
			EnterpriseMember enterprise = new EnterpriseMember();
			enterprise.setLoginName(form.getUsername());
			LoginRequest loginReq=new LoginRequest();
			loginReq.setLoginName(form.getUsername());

			loginReq.setLoginPassowrd(form.getPassword());
			loginReq.setLoginType(form.getMemberIdentity());
			LoginNameEditResp loginResp=new LoginNameEditResp();
			boolean isPerson = false;
			BaseMember kjtMember=new BaseMember();
			try {
				loginResp=loginFacadeService.login(loginReq);
				if(!SysResponseCode.SUCCESS.getCode().equals(loginResp.getResponseCode())){
					restP.setMessage(loginResp.getResponseMessage());
					if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(loginResp.getResponseCode())) {
						restP.setMessage("账户未设置登录密码,请进行账户激活操作");
					}
					return restP;
				}
				kjtMember=memberService.queryMemberById(loginResp.getMemberId(),env);
				person.setLoginName(kjtMember.getLoginName());
				if("1".equals(loginResp.getMemberType())){
					isPerson = true;
					/*if(!loginResp.isAccountUpgrading()){
						logger.info("非首次登陆：统一登陆"+loginResp.getMemberId());
						person = memberService.queryMemberIntegratedInfo(person, env);
						if (person.getMemberType() != MemberType.PERSONAL) {
							throw new MemberNotExistException(ErrorCode.MEMBER_NOT_EXIST);
						}
						isPerson = true;
						if (loginResp.getAccountStatus()!=4) {
							if(loginResp.getAccountStatus()==1||loginResp.getAccountStatus()==2){
								payPassWordCacheService.clear(loginResp.getMemberId()+"_pwd");
								payPassWordCacheService.put(loginResp.getMemberId()+"_pwd",form.getPassword());
								payPassWordCacheService.clear(loginResp.getMemberId()+"_email");
								payPassWordCacheService.put(loginResp.getMemberId()+"_email",loginResp.getEmail());
								payPassWordCacheService.clear(loginResp.getMemberId()+"_mobile");
								payPassWordCacheService.put(loginResp.getMemberId()+"_mobile",loginResp.getMobile());
							}
							logger.info("首次登陆：跳到海融易"+loginResp.getMemberId());
							restP.setRedirect(request.getContextPath() + "/my/hrylogin-confirm-info.htm?Mid="+loginResp.getMemberId()
									+"&status="+loginResp.getAccountStatus());
							restP.setSuccess(true);
							return restP;
						}
					}else{//1未设置登录名2.未设置登录密码3.未设置支付密码4.确认完毕
						payPassWordCacheService.put(loginResp.getMemberId()+"_accountType",loginResp.getAccountType().toString());
						if(loginResp.getAccountType()==2){//来自海融易账户未绑定//不会有4的状态
							payPassWordCacheService.clear(loginResp.getMemberId()+"_pwd");
							payPassWordCacheService.put(loginResp.getMemberId()+"_pwd",form.getPassword());
							payPassWordCacheService.clear(loginResp.getMemberId()+"_email");
							payPassWordCacheService.put(loginResp.getMemberId()+"_email",loginResp.getEmail());
							payPassWordCacheService.clear(loginResp.getMemberId()+"_mobile");
							payPassWordCacheService.put(loginResp.getMemberId()+"_mobile",loginResp.getMobile());
							logger.info("首次登陆：跳到海融易"+loginResp.getMemberId());
							restP.setRedirect(request.getContextPath() + "/my/hrylogin-confirm-info.htm?Mid="+loginResp.getMemberId()
									+"&status="+loginResp.getAccountStatus());
						}else {
							logger.info("首次登陆：跳到永达互联网金融"+loginResp.getMemberId());
							restP.setRedirect(request.getContextPath() + "/my/kjtlogin-confirm-info.htm?Mid="
							+loginResp.getMemberId()+"&status="+loginResp.getAccountStatus());
						}
						restP.setSuccess(true);
						return restP;
					}*/
				}else {
					try {
						enterprise = memberService.queryCompanyMember(enterprise, env);
					} catch (MemberNotExistException e2) {
						restP.setMessage("会员不存在！");
						return restP;
					}
				}
			} catch (MemberNotExistException e1) {
				try {
					enterprise = memberService.queryCompanyMember(enterprise, env);
				} catch (MemberNotExistException e2) {
					restP.setMessage("会员不存在！");
					return restP;
				}
			}
			if (isPerson) {
				PersonMember member=new PersonMember();
				member.setMemberId(kjtMember.getMemberId());
				member.setMemberName(kjtMember.getMemberName());
				member.setLoginName(kjtMember.getLoginName());
				member.setLockStatus(kjtMember.getLockStatus());
				if (MemberLockStatus.LOCKED.name().equals(member.getLockStatus().name())) {
					if (logger.isInfoEnabled()) {
		                logger.info(LogUtil.appLog(OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(), member, env));
					}
					restP.setMessage("会员已被锁定，请联系客服！");
					return restP;
				}
				
				this.saveToken(member, request, response, restP, CommonConstant.USERTYPE_PERSON);
				if (logger.isInfoEnabled()) {
	                logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(), member, env));
				}
			} else {
				EnterpriseMember member=new EnterpriseMember();
				member.setMemberId(kjtMember.getMemberId());
				member.setMemberName(kjtMember.getMemberName());
				member.setLoginName(kjtMember.getLoginName());
				member.setLockStatus(kjtMember.getLockStatus());
				if (MemberLockStatus.LOCKED.name().equals(member.getLockStatus().name())) {
					if (logger.isInfoEnabled()) {
		                logger.info(LogUtil.appLog(OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(), member, env));
					}
					restP.setMessage("会员已被锁定，请联系客服！");
					return restP;
				}

				MerchantListResponse merchantListResponse = memberService.queryMerchantByMemberId(member.getMemberId());
				if ((null != merchantListResponse.getMerchantInfos())
						&& (merchantListResponse.getMerchantInfos().size() > 0)) {
					restP.setMessage("商户用户不能登录会员平台！");
					restP.setRedirect(webResource.getEnterWalletAddr() + "/index.htm");
					return restP;
				}

				PersonMember personMember = new PersonMember();
				ConvertObject.convert(member, personMember);
				this.saveToken(personMember, request, response, restP, CommonConstant.USERTYPE_ENTERPRISE);
				if (logger.isInfoEnabled()) {
	                logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(), personMember, env));
				}
			}
			//风控验证
			BaseMember baseMember = new BaseMember();
			baseMember.setLoginName(form.getUsername());
			restP.setSuccess(true);
			SarsResponse result=defaultSarsService.verify(OperateeTypeEnum.LOGIN.getCode(), baseMember, env);
			if(null!=result && !"000".equals(result.getResult())){				
				restP.setMessage(result.getMsg());
				restP.setSuccess(false);			
				return restP;
			}
        } catch (Exception e) {
            logger.error("", e);
            restP.setMessage(getErrMsg(e.getMessage()));
        }
        return restP;
    }
    
    /**
     * 统一账户登录，由kjt账户账户创建统一账户，显示统一后的信息
     * @param model
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/my/kjtlogin-confirm-info.htm" )
    public ModelAndView kjtLogin(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {
    	String Mid = request.getParameter("Mid");
    	String status = request.getParameter("status");
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("Mid", Mid);
    	logger.info("首次登陆：永达互联网金融"+Mid+"/my/kjtlogin-confirm-info.htm");
    	BaseMember kjtMember=new BaseMember();
		kjtMember=memberService.queryMemberById(Mid,env);
		data.put("loginName", kjtMember.getLoginName());
		String mobile=getEncryptInfo(request, DeciphedType.CELL_PHONE,Mid, env);
		String email=getEncryptInfo(request, DeciphedType.EMAIL,Mid, env);
		data.put("mobile", mobile==null?"false":mobile);
		data.put("email", email==null?"false":email);
		data.put("gotype", "kjt");
		data.put("status", status);
        restP.setData(data);
//        if(status.equals("3")){
//        	return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd","response",restP);
//        }
        PersonMember member=new PersonMember();
		member.setMemberId(kjtMember.getMemberId());
		member.setMemberName(kjtMember.getMemberName());
		member.setLoginName(kjtMember.getLoginName());
		member.setLockStatus(kjtMember.getLockStatus());
        payPassWordCacheService.clear(Mid+"_pwd");
		this.saveToken(member, request, response, restP, CommonConstant.USERTYPE_PERSON);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/kjtlogin-confirm-accountname","response",restP);
    }
    /**
     * kjt跳转成功页
     * @param model
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/my/kjtlogin-success.htm" )
    public ModelAndView kjtLoginSuccess(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {
    	RestResponse restP = new RestResponse();
    	String Mid = request.getParameter("Mid");
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("Mid", Mid);
    	restP.setData(data);
    	String status = request.getParameter("status");
    	if(status.equals("3")){
        	return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd","response",restP);
        }
    	logger.info("首次登陆：永达互联网金融"+Mid+"/my/kjtlogin-success.htm");
    	String gotype=payPassWordCacheService.load(Mid+"_accountType");
    	data.put("gotype", gotype);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/unionma-login-success","response",restP);
    }
    /**
     * 统一账户登录，由hry账户账户创建统一账户，有可用的手机和邮箱，供选择；有可用唯一的手机号/邮箱，供展示；没有可用的，让用户输入
     * @param model
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/my/hrylogin-confirm-info.htm")
    public ModelAndView hryLogin(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {

    	String Mid = request.getParameter("Mid");
    	String status = request.getParameter("status");
    	logger.info("首次登陆：海融易"+Mid+"/my/hrylogin-confirm-info.htm");
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
    	BaseMember kjtMember=new BaseMember();
		kjtMember=memberService.queryMemberById(Mid,env);
		data.put("loginName", kjtMember.getLoginName());
		
		String mobile=payPassWordCacheService.load(Mid+"_email");
		String email=payPassWordCacheService.load(Mid+"_mobile");
		if(("").equals(mobile)
				||kjtMember.getLoginName().equals(mobile) 
				||checkLoginNameIsExist(mobile,env)){
			mobile=null;
		}
		if(("").equals(email)
				||kjtMember.getLoginName().equals(email)
				||checkLoginNameIsExist(email,env)){
			email=null;
		}
		data.put("mobile", mobile==null?"false":mobile);
		data.put("email", email==null?"false":email);
    	data.put("Mid", Mid);
        restP.setData(data);
        restP.setSuccess(true);
        if (status.equals("3")) {
        	return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd","response",restP);
		}
        if(mobile==null && email==null){
        	return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-confirm-accountname1","response",restP);
        }
        return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-confirm-accountname","response",restP);
    }
    
    public String getEncryptInfo(HttpServletRequest request, DeciphedType type,String mid,
            OperationEnvironment env) throws Exception {
		EncryptData encryptData = commMemberService.decipherMember(mid,
		type, DeciphedQueryFlag.ALL, env);
		return encryptData.getPlaintext();
		}
   
    public boolean checkLoginNameIsExist(String loginName,OperationEnvironment env)  {
    	boolean isExist=false;
    	if(loginName==null){
    		return true;
    	}
    	PersonMember person = new PersonMember();
    	person.setLoginName(loginName);
    	EnterpriseMember enterprise = new EnterpriseMember();
		enterprise.setLoginName(loginName);
    	LoginPasswdRequest loginPasswdRequest=new LoginPasswdRequest();
		loginPasswdRequest.setValidateMode(1);	
		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			if (person.getMemberType() != MemberType.PERSONAL) {
				throw new MemberNotExistException(ErrorCode.MEMBER_NOT_EXIST);
			}
			
			if (person != null && StringUtils.isNotEmpty(person.getMemberId())) {
				isExist=true;
			}
		} catch (MemberNotExistException e1) {
			try {
				enterprise = memberService.queryCompanyMember(enterprise, env);
				if (enterprise != null && StringUtils.isNotEmpty(enterprise.getMemberId())) {
	                isExist=true;
	            }
			} catch (MemberNotExistException e2) {
				return isExist;
			}catch (Exception e) {
				return isExist;
			}
		}catch (Exception e) {
			return isExist;
		}
		return isExist;
	}
    
    
    /**
     * hry账户设置登录名
     * @param model
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/my/hrylogin-set-name.htm",method = RequestMethod.POST)
    public ModelAndView hryloginSetName(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {

    	String Mid = request.getParameter("Mid");
    	logger.info("首次登陆：海融易"+Mid+"/my/hrylogin-set-name.htm");
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
    	BaseMember kjtMember=new BaseMember();
		kjtMember=memberService.queryMemberById(Mid,env);
		data.put("loginName", kjtMember.getLoginName());
		String mobile=payPassWordCacheService.load(Mid+"_email");
		String email=payPassWordCacheService.load(Mid+"_mobile");
		if(("").equals(mobile)
				||kjtMember.getLoginName().equals(mobile)
				||checkLoginNameIsExist(mobile,env)){
			mobile=null;
		} if(("").equals(email)
				||kjtMember.getLoginName().equals(email)
				||checkLoginNameIsExist(email,env)){
			email=null;
		}
		data.put("Mid", Mid);
		data.put("mobile", mobile==null?"false":mobile);
		data.put("email", email==null?"false":email);
		String sendObject = request.getParameter("memberIdentity");//发送对象。手机还是邮箱、
		String sjyzm = request.getParameter("mobileCaptcha");//校验码
		String accountname = request.getParameter("accountname");//新的账户名（手机或邮箱）
		String url;
		if(mobile==null && email==null){
			url="/unionma/hrylogin-confirm-accountname1";
			//验证校验码
			if(sendObject.equals("MOBILE")){
				if (!validateOtpValue(sendObject,accountname,Mid,sjyzm,request,env)) {
					restP.setMessage("验证码输入错误！");
					restP.setData(data);
					return new ModelAndView(CommonConstant.URL_PREFIX + url, "response", restP);
				}
			}else if(sendObject.equals("EMAIL")) {
				if (!validateOtpValue(sendObject,accountname,Mid,sjyzm,request,env)) {
					restP.setMessage("验证码输入错误！");
					restP.setData(data);
					return new ModelAndView(CommonConstant.URL_PREFIX + url, "response", restP);
				}
			}
    	}else{
    		url="/unionma/hrylogin-confirm-accountname";
    	}
		LoginNameEditRequest req=new LoginNameEditRequest();
		req.setMemberId(kjtMember.getMemberId());
		req.setOperatorId(kjtMember.getOperatorId());
		req.setLoginName(accountname);
		req.setOldName(kjtMember.getLoginName());
		req.setLoginNameType(sendObject);
		
		
		restP.setData(data);
		try {
			UnionmaBaseResponse resp = loginFacadeService.loginNameEdit(req);
			if(resp.getIsSuccess()){
				payPassWordCacheService.clear(Mid+"_email");
				payPassWordCacheService.clear(Mid+"_mobile");
				restP.setSuccess(true);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
			}else {
				restP.setMessage(resp.getResponseMessage());
				if(SysResponseCode.DUPLICATE_LOGIN_NAME.getCode().equals(resp.getResponseCode())){
					restP.setMessage("该登录名已被占用！");
				}
				return new ModelAndView(CommonConstant.URL_PREFIX + url, "response", restP);
			}
		} catch (Exception e) {
			restP.setMessage("设置登录名失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + url, "response", restP);
		}
	}
    
    /**
     * hry账户设置登录密码
     * @param model
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/my/hry-set-loginpwd.htm",method = RequestMethod.POST)
    public ModelAndView hrySetloginpwd(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {

    	String Mid = request.getParameter("Mid");
    	String newPasswd = request.getParameter("loginpwd");
		String renewPasswd = request.getParameter("loginpwd2");
		String oldPasswd=payPassWordCacheService.load(Mid+"_pwd");
		logger.info("首次登陆：海融易"+Mid+"/my/hry-set-loginpwd.htm");
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
    	if (newPasswd == null) {
		  restP.setMessage("新登陆密码不能为空！");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
        } else if (renewPasswd == null) {
        	restP.setMessage("旧登陆密码不能为空！");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
        }else if (oldPasswd == null) {
        	restP.setMessage("确认登陆密码不能为空！");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
		} else if ((newPasswd.length() < 6) || (newPasswd.length() > 32)) {
			restP.setMessage("登陆密码位数不能低于6位或不能大于32位！");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
        } else if (!newPasswd.equals(renewPasswd)) {
            restP.setMessage("您输入的两次登陆密码不一致，请重新输入！");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
        }
    	
    	BaseMember kjtMember=new BaseMember();
		kjtMember=memberService.queryMemberById(Mid,env);
		data.put("loginName", kjtMember.getLoginName());
		loginPasswordEditReq req=new loginPasswordEditReq();
		req.setOperatorId(kjtMember.getOperatorId());
		req.setOldPassword(oldPasswd);
		req.setNewPassword(newPasswd);
		req.setEnsureNewPassword(renewPasswd);
		req.setMemberType(kjtMember.getMemberType().getCode());
		req.setLoginName(kjtMember.getLoginName());
		data.put("Mid", Mid);
		restP.setData(data);
		try {
			UnionmaBaseResponse resp=loginPasswordService.loginPasswordEdit(req);
			if(resp.getIsSuccess()){
				payPassWordCacheService.clear(Mid+"_pwd");
				payPassWordCacheService.put(Mid+"_pwd",newPasswd);
				restP.setSuccess(true);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd", "response", restP);
			}else {
				if(newPasswd !=null){
					restP.setMessage(resp.getResponseMessage());
				}
				return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
			}
		} catch (Exception e) {
			restP.setMessage("修改登录密码失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
		}
	}
    
    /**
     * hry账户设置支付密码
     * @param model
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/my/hry-set-paypwd.htm",method = RequestMethod.POST)
    public ModelAndView hrySetpaypwd(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {

    	String Mid = request.getParameter("Mid");
    	String newPasswd = request.getParameter("paypwd1");
		String renewPasswd = request.getParameter("paypwd2");
		logger.info("首次登陆："+Mid+"/my/hry-set-paypwd.htm");
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
    	BaseMember kjtMember=new BaseMember();
		kjtMember=memberService.queryMemberById(Mid,env);
		data.put("loginName", kjtMember.getLoginName());
		  if (newPasswd == null) {
			  restP.setMessage("新支付密码不能为空！");
	            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd", "response", restP);
	        } else if (renewPasswd == null) {
	        	restP.setMessage("确认支付密码不能为空！");
	            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd", "response", restP);
			} else if ((newPasswd.length() < 6) || (newPasswd.length() > 32)) {
				restP.setMessage("支付密码位数不能低于6位或不能大于32位！");
	            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd", "response", restP);
	        } else if (!newPasswd.equals(renewPasswd)) {
	            restP.setMessage("您输入的两次支付密码不一致，请重新输入！");
	            return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd", "response", restP);
	        }
	
		PersonMember member=new PersonMember();
		member.setMemberId(kjtMember.getMemberId());
		member.setMemberName(kjtMember.getMemberName());
		member.setLoginName(kjtMember.getLoginName());
		member.setLockStatus(kjtMember.getLockStatus());
		member=memberService.queryMemberIntegratedInfo(member, env);
		PayPasswordSetReq req=new PayPasswordSetReq();
		req.setAccountId(member.getDefaultAccountId());
		req.setOperatorId(member.getOperatorId());
		req.setPayPassword(newPasswd);
		data.put("Mid", Mid);
		String gotype=payPassWordCacheService.load(Mid+"_accountType");
		data.put("gotype", gotype);
		restP.setData(data);
		try {
			UnionmaBaseResponse resp=payPasswordService.payPasswordSet(req);
			if(resp.getIsSuccess()){
				restP.setSuccess(true);
				payPassWordCacheService.clear(Mid+"_pwd");
				this.saveToken(member, request, response, restP, CommonConstant.USERTYPE_PERSON);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/unionma-login-success", "response", restP);
			}else {
				if(newPasswd !=null){
					restP.setMessage(resp.getResponseMessage());
				}
				return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd", "response", restP);
			}
		} catch (Exception e) {
			restP.setMessage("设置支付密码失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-pay-pwd", "response", restP);
		}
	}
    /**
	 * 异步发送邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/login-send-mail.htm", method = RequestMethod.POST)
	@ResponseBody
	private RestResponse ajaxSendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String email = request.getParameter("email");
		String bizType = request.getParameter("bizType"); 		
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
		}
		restP.setSuccess(true);	
		return restP;
	}
    
	@ResponseBody
	@RequestMapping(value = "/my/login-send-mobile.htm")
    public RestResponse sendMobileCertCode(HttpServletRequest request, HttpSession session, OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        String Mid = request.getParameter("Mid");
        String mobile = request.getParameter("mobile");
        try {
        	if("".equals(mobile) ||mobile ==null){
        		mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
        	}
        	if (StringUtils.isEmpty(mobile)) {
        		restP.setSuccess(false);
        		restP.setMessage("您的账户尚未绑定手机号码!");
        		return restP;
        	}
        	if (!Pattern.matches(ValidationConstants.MOBILE_PATTERN, mobile)) {
				restP.setMessage("手机号码格式错误");
				return restP;
			}
            // 验证码类型
        	String bizType = request.getParameter("bizType");
            AuthCodeRequest req = new AuthCodeRequest();
            req.setMemberId(Mid);
            req.setMobile(mobile);
            req.setBizId(Mid);
            req.setBizType(bizType);
            String ticket = defaultUesService.encryptData(mobile);
            req.setMobileTicket(ticket);
            req.setValidity(CommonConstant.VALIDITY);
            
            if (defaultSmsService.sendMessage(req, env)) {
                restP.setSuccess(true);
                restP.setMessage("发送短信验证码成功");
            }
        } catch (Exception e) {
        	restP.setSuccess(false);
        	restP.setMessage("发送短信失败");
            logger.error("发送短信失败", e);
            return restP;
        }
        
        return restP;
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
	
	
	private boolean validateOtpValue(String memberIdentity,String loginName,String Mid,String otpValue, HttpServletRequest request, OperationEnvironment env) {
		try {
			    if("EMAIL".equals(memberIdentity)){
			    	String keyStr =loginName+"_"+otpValue.toLowerCase()+"_"+ env.getClientIp();
					String info = payPassWordCacheService.load(keyStr);	
					if ((null == info) || "".equals(info)) {						
						return false;
					}			    
				}else if("MOBILE".equals(memberIdentity)){				
					AuthCodeRequest req = new AuthCodeRequest();
					req.setMemberId(Mid);
					req.setAuthCode(otpValue);
					req.setMobile(loginName);
					req.setMobileTicket(uesServiceClient.encryptData(loginName));
					req.setBizId(Mid);
					req.setBizType(BizType.ACC_UPD_TOPHONE.getCode());
					boolean result = defaultSmsService.verifyMobileAuthCode(req, env);
					return result;
				}
			 }
			catch (Exception e) {			
				return false;
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
                    //为了使cookie能在多个系统中共享，需要给cookie设置多个系统共通的域名
                    logger.info("vfssoDomain: " + webResource.getVfssoDomain());
                    if(StringUtils.isNotBlank(webResource.getVfssoDomain())){
                    	for (String domain : webResource.getVfssoDomain().split(",")) {
                            VfSsoCookie.setCookie(token, response, domain, "/");
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
        }
    }

    /**
     * 进入退出页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/logout.htm")
    public String logout(HttpServletRequest req,HttpServletResponse resp) {
        log.info("cas个人钱包退出，退出地址：/logout.htm");
        this.clearLoginInfo(req, resp);
//        HttpSession session = req.getSession();
//        session.invalidate();
//        try {
//        	VfSsoCookie.removeCookie(resp);
//            VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
//            if(VfSsoUser.getCurrentToken()!=null)userSessionService.remove(VfSsoUser.getCurrentToken());
//        } catch (SessionException e) {
//        	logger.error("单点登录注销用户信息失败",e);
//            return "redirect:" + req.getContextPath() + "/index.htm";
//        }
        return "redirect:" + req.getContextPath() + "/index.htm";
    }

    /**
     * 进入退出页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/logoutAndRedirect.htm")
    public String logoutAndRedirect(HttpServletRequest req,HttpServletResponse resp) {
        log.info("cas个人钱包退出，退出地址：/logoutAndRedirect.htm");
        this.clearLoginInfo(req, resp);
//        HttpSession session = req.getSession();
//        session.invalidate();
//        try {
//        	 VfSsoCookie.removeCookie(resp);
//             VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
//             if(VfSsoUser.getCurrentToken()!=null)userSessionService.remove(VfSsoUser.getCurrentToken());
//        } catch (SessionException e) {
//        	logger.error("单点登录注销用户信息失败",e);
//            return "redirect:" + webResource.getEnterWalletAddr() + "/index.htm";
//        }
        return "redirect:" + webResource.getEnterWalletAddr() + "/index.htm";
    }
    
    private void clearLoginInfo(HttpServletRequest req,HttpServletResponse resp){
        HttpSession session = req.getSession();
        session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
        session.invalidate();
        try {
            VfSsoCookie.removeCookie(resp);
            VfSsoCookie.removeCookie(resp, webResource.getVfssoDomain());
            if(StringUtils.isNotBlank(webResource.getVfssoDomain())){
                for (String domain : webResource.getVfssoDomain().split(",")) {
                    VfSsoCookie.removeCookie(resp, domain);
                }
            }
            if(VfSsoUser.getCurrentToken()!=null) {
				userSessionService.remove(VfSsoUser.getCurrentToken());
			}
        } catch (SessionException e) {
            logger.error("单点登录注销用户信息失败",e);
        }
    }

	/**
	 * 异步校验登陆名
	 * 修改登录名时无登录密码的账号也视为账号已存在
	 * @param req
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/login/checkNewLoginName.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse checkNewLoginNameIsExist(HttpServletRequest req, OperationEnvironment env) throws BizException {
		RestResponse restP = new RestResponse();

		PersonMember person = new PersonMember();
		person.setLoginName(req.getParameter("username"));

		EnterpriseMember enterprise = new EnterpriseMember();
		enterprise.setLoginName(req.getParameter("username"));
		
		LoginPasswdRequest loginPasswdRequest=new LoginPasswdRequest();
		loginPasswdRequest.setValidateMode(1);	

		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			if (person.getMemberType() != MemberType.PERSONAL) {
				throw new MemberNotExistException(ErrorCode.MEMBER_NOT_EXIST);
			}
			if ("login".equals(req.getParameter("cmd"))) {
				if (person.getLockStatus() == MemberLockStatus.LOCKED) {
					restP.setMessage("2");// 用户名被锁定
				} else {
					restP.setSuccess(true);
				}
			} else {
			    if (person != null && StringUtils.isNotEmpty(person.getMemberId())) {
			        restP.setMessage("1");// 用户名已存在
			    }
			}
		} catch (MemberNotExistException e1) {
			try {
				enterprise = memberService.queryCompanyMember(enterprise, env);
				if ("login".equals(req.getParameter("cmd"))) {
					if (enterprise.getLockStatus() == MemberLockStatus.LOCKED) {
						restP.setMessage("2");// 用户名被锁定
					} else {
						restP.setSuccess(true);
					}
				} else {
				    if (enterprise != null && StringUtils.isNotEmpty(enterprise.getMemberId())) {
				        restP.setMessage("1");// 用户名已存在
				    }
				}
			} catch (MemberNotExistException e2) {
				if ("register".equals(req.getParameter("cmd"))) {
					restP.setSuccess(true);
				} else {
					restP.setMessage("1");// 用户名不存在
				}
			}
		}

		return restP;
	}

	
	/**
	 * 异步校验登陆名
	 * 注册和激活时有验证登录密码
	 * @param req
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/login/checkLoginName.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse checkLoginNameIsExist(HttpServletRequest req, OperationEnvironment env) throws BizException {
		RestResponse restP = new RestResponse();

		PersonMember person = new PersonMember();
		person.setLoginName(req.getParameter("username"));

		EnterpriseMember enterprise = new EnterpriseMember();
		enterprise.setLoginName(req.getParameter("username"));
		
		LoginPasswdRequest loginPasswdRequest=new LoginPasswdRequest();
		loginPasswdRequest.setValidateMode(1);	

		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			if (person.getMemberType() != MemberType.PERSONAL) {
				throw new MemberNotExistException(ErrorCode.MEMBER_NOT_EXIST);
			}
			if ("login".equals(req.getParameter("cmd"))) {
				if (person.getLockStatus() == MemberLockStatus.LOCKED) {
					restP.setMessage("2");// 用户名被锁定
				} else {
					restP.setSuccess(true);
				}
			} else {				
				loginPasswdRequest.setOperatorId(person.getOperatorId());							
				if(!loginPasswdService.validateLoginPwdIsNull(loginPasswdRequest)){
					restP.setMessage("1");// 用户名已存在
				}else{
					restP.setSuccess(true);
				}
				
			}
		} catch (MemberNotExistException e1) {
			try {
				enterprise = memberService.queryCompanyMember(enterprise, env);
				if ("login".equals(req.getParameter("cmd"))) {
					if (enterprise.getLockStatus() == MemberLockStatus.LOCKED) {
						restP.setMessage("2");// 用户名被锁定
					} else {
						restP.setSuccess(true);
					}
				} else {					
					loginPasswdRequest.setOperatorId(enterprise.getOperatorId());					
					if(!loginPasswdService.validateLoginPwdIsNull(loginPasswdRequest)){
						restP.setMessage("1");// 用户名已存在
					}else{
						restP.setSuccess(true);
					}
				}
			} catch (MemberNotExistException e2) {
				if ("register".equals(req.getParameter("cmd"))) {
					restP.setSuccess(true);
				} else {
					restP.setMessage("1");// 用户名不存在
				}
			}
		}

		return restP;
	}
	/**
	 * 异步校验验证码
	 * 
	 * @param req
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/login/checkVerifiCode.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse checkVerifiCode(HttpServletRequest req) throws BizException {
		RestResponse restP = new RestResponse();

		try {
			if (KaptchaImageAction.validateRandCode(req, req.getParameter("code"))) {
				restP.setSuccess(true);
			}
		} catch (Exception e) {
			logger.error("", e);
		}

		return restP;
	}
}
