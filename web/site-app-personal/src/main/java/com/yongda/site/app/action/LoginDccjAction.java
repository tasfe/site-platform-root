package com.yongda.site.app.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
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
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.RSASignUtil;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyStatus;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.vfsso.session.enums.SessionStatusKind;
import com.netfinworks.vfsso.session.exceptions.SessionException;
import com.yongda.site.app.WebDynamicResource;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.form.dc.DcRegisterRequest;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.app.util.wx.LogUtil;
import com.yongda.site.app.validator.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;

@Controller
public class LoginDccjAction extends BaseAction{
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "transferService")
	private TransferService transferService;
	
	@Autowired
	private RegisterFacadeWS registerFacadeWS;
	
	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
	@Resource(name = "webResource")
	private WebDynamicResource webResource;
	
	@Resource(name = "defaultLoginPasswdService")
	private DefaultLoginPasswdService defaultLoginPasswdService;
	
	@Resource(name = "cachedSessionService")
	private IVfSsoSession<SsoUser> userSessionService;
	
    @Resource(name = "memberService")
	private MemberService commMemberService;
	  
    @Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
    
    @Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
    
    @Resource(name = "loginFacadeService")
    private LoginFacadeService loginFacadeService;
    
    @Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;
    
    @Resource(name = "loginPasswdService")
    private LoginPasswdService        loginPasswdService;
    
    @Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;
    
    @Resource(name = "xuCache")
	private XUCache<String> loginCache;
    
	private static final String LOGIN_TYPE = "0";//登录标志
	
	private static final String MEMBER_ID_ENTITY = "MOBILE";
	
	@Value("${dc.priKey}")
	private String priKey;
	
	@Value("${dc.pubKey}")
	private String pubKey;
	/**
	 * 登录
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 */
	@RequestMapping(value="/dclogin",method= RequestMethod.POST)
	@ResponseBody
	public RestResponse login(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		log.info("dclogin...start");
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		try {
			String loginName = request.getParameter("login_name");
			String loginPwd  = request.getParameter("login_pwd"); 
			
			restP = verifyReqParams(loginName,loginPwd,restP);
			if(!restP.isSuccess())
			{
				return restP;
			}
			
			//检查用户名是否存在
			restP = transferService.checkLoginUserName(loginName, restP, LOGIN_TYPE);
			if (!restP.isSuccess()) {
				return restP;
			}
			
			//RSA解密
			String deLoginPwd = RSASignUtil.decode(loginPwd, priKey);
			if(StringUtils.isBlank(deLoginPwd)){
				 logger.error("解密结果为空");
				 restP.setMessage("解密失败");
				 restP.setCode(CommonDefinedException.PWD_DECRYPTION_ERROR.getErrorCode());
		         restP.setSuccess(false);
		         return restP;
			}
			
			PersonMember person = new PersonMember();
			person.setLoginName(loginName);
			person = memberService.queryMemberIntegratedInfo(person, env);
			
			
			LoginRequest loginReq=new LoginRequest();
			loginReq.setLoginName(person.getLoginName());
			loginReq.setLoginPassowrd(deLoginPwd);
			loginReq.setLoginType(MEMBER_ID_ENTITY);
			LoginNameEditResp loginResp=new LoginNameEditResp();
			try {
				loginResp=loginFacadeService.login(loginReq);
				logger.info("统一账户登录：{}",loginResp.toString());
				if(!SysResponseCode.SUCCESS.getCode().equals(loginResp.getResponseCode())){
					restP.setMessage(loginResp.getResponseMessage());
					if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(loginResp.getResponseCode())) {
						restP.setMessage("账户未设置登录密码,请进行账户激活操作");
					}
					restP.setSuccess(false);
					restP.setCode(CommonDefinedException.PASSWORD_ERROR.getErrorCode());
					return restP;
				}
			}catch(BizException e){
				   logger.error("", e);
		           restP.setMessage(getErrMsg(e.getMessage()));
		           restP.setSuccess(false);
		           return restP;
			}	
				
			//用户成功,登录密码正确
			PersonMember member = new PersonMember();
			member.setMemberId(person.getMemberId());
			member.setMemberName(person.getMemberName());
			member.setLoginName(person.getLoginName());
			member.setLockStatus(person.getLockStatus());
			member.setOperatorId(person.getOperatorId());
			
			if (MemberLockStatus.LOCKED.name().equals(member.getLockStatus().name())) {
				if (logger.isInfoEnabled()) {
	                logger.info(LogUtil.appLog(OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(), member, env));
				}
				restP.setMessage("会员已被锁定，请联系客服！");
				return restP;
			}
			
			// 保存登录信息到session
			String token = this.saveToken(member, request, response, restP,
					CommonConstant.USERTYPE_PERSON,webResource.getVfssoDomain());
			if (logger.isInfoEnabled()) {
				logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
						member, env));
			}
			
			//获取个人用户绑定的号码
	    	String mobile = null;
	    	EncryptData encryptData = commMemberService.decipherMember(person.getMemberId(),
	    			DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
        	if (encryptData != null) {
        		mobile = encryptData.getPlaintext();
        	}
	        
        	
	        //查询实名认证等级	
        	String level = defaultMemberService.getMemberVerifyLevel(person.getMemberId(), env);
        	AuthInfo info = PersonalCommonQueryRealName.queryUserInfo(person.getMemberId(),
					StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
        	
	        Map<String,Object> resultMap = new HashMap<String,Object>();	
	        resultMap.put("token", token);
	        resultMap.put("member_id", person.getMemberId());
	        resultMap.put("login_name", person.getLoginName());
	        resultMap.put("verify_mobile", mobile);
	        resultMap.put("name", person.getMemberName());
	        resultMap.put("idcard_no", info.getAuthNo());
	        resultMap.put("real_name_level", CertifyLevel.getByCode(level));
	        resultMap.put("head_img", StringUtils.EMPTY);
	        restP.setData(resultMap);
	        
	        Cookie cookie = new Cookie("com.vfsso.cas.token",token);
	        cookie.setDomain(".yongdapay.com");
			cookie.setMaxAge(7200);
			response.addCookie(cookie);
			
		}catch(ServiceException e){
			logger.error("查询实名认证等级出错");
		} catch (Exception e) {
			logger.error("操作失败", e);
			restP.setMessage("操作失败");
		}
		return restP;
	}
	
	
	/**
	 * 注册
	 * 
	 * @throws IOException
	 *
	 */
	@RequestMapping(value = "/dcregister", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse registerActive(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) {
		log.info("dcregister...start");
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		try {
			Map<String,String> map = request.getParameterMap();
			DcRegisterRequest reqForm = new DcRegisterRequest();
			BeanUtils.populate(reqForm, map);
			logger.info("请求参数：{}", reqForm.toString());
			if(StringUtils.isBlank(reqForm.getLogin_pwd())){
				logger.error("缺少必要的输入参数！");
				return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"登录密码不能为空");
			}
			//RSA解密
			String deLoginPwd = RSASignUtil.decode(reqForm.getLogin_pwd(), priKey);
			if(StringUtils.isBlank(deLoginPwd)){
				 logger.error("解密结果为空");
				 restP.setMessage("解密失败");
				 restP.setCode(CommonDefinedException.PWD_DECRYPTION_ERROR.getErrorCode());
		         restP.setSuccess(false);
		         return restP;
			}
			reqForm.setLogin_pwd(deLoginPwd);
			// 校验提交参数
			String errorMsg = commonValidator.validate(reqForm);

			if (StringUtils.isNotEmpty(errorMsg)) {
				logger.error("缺少必要的查询参数！" + errorMsg);
				return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,errorMsg);
			}
			
			/**
			 * 校验手机验证码 memberIdentity 会员身份 mobile loginName 登录名 otpValue 验证码
			 */
			String loginName = reqForm.getLogin_name();
			Boolean flag = transferService.validateOtpValue(MEMBER_ID_ENTITY,loginName,
					loginName, loginName,BizType.REGISTER_MOBILE,reqForm.getCode().trim(), env);
			
			if (!flag) {
				logger.error("短信验证码有误");
				return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"短信验证码有误");
			}
			
			PersonMember person = new PersonMember();
			//云+
			restP = verifyYunOrWxData(reqForm.getLogin_name(),reqForm.getInvit_code(),env,restP);
			log.info("校验云+数据返回信息：{}",restP.toString());
			if(restP.isSuccess())
			{
				/**
				 * 校验登录名是否已存在
				 */
				log.info("未查询到注册信息....正常流程");
				/*restP = transferService.checkLoginUserName(reqForm.getLogin_name(), restP, REGISTER_TYPE);
				if (!restP.isSuccess()) {
					return restP;
				}*/
				if(!register(reqForm,restP,env)){
					return restP;  //註冊失敗
				}
			}else if(!StringUtils.isBlank(restP.getCode()) && restP.getCode().
					equals(CommonDefinedException.ACCOUNT_EXIST_ERROR.getErrorCode())){
				return restP;
			}
			
			person.setLoginName(reqForm.getLogin_name());
			person = memberService.queryMemberIntegratedInfo(person, env);
			
			
			//设置登录密码  并绑定手机号
			if(!setLoginPwd(deLoginPwd,person.getOperatorId(),restP))
			{
				logger.error("设置登录密码失败");
			}
			//注册完成暂不登录
			//saveToken(person,request,response,restP,env);
			restP.getData().put("member_id", person.getMemberId());
			restP.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage("操作失败");
		}
		return restP;
	}
	
		//免密登录  返回密文密码
		@RequestMapping(value = "/setavoidpwdlogin", method = RequestMethod.POST)
		@ResponseBody
		public RestResponse avoidPwdLogin(HttpServletRequest request,
				HttpServletResponse response, OperationEnvironment env){
			log.info("dcregister...start");
			RestResponse restP = ResponseUtil.buildSuccessResponse();
			
			String token = request.getParameter("token");
			//校验请求参数
			if(StringUtils.isBlank(token)){
				return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"token不能为空");
			}
			PersonMember user = getUser(request);
			String memberId  = user.getMemberId();
			String loginName = user.getLoginName();
			
			try {
				String ticket = defaultUesService.encryptData(memberId+","+loginName);
				restP.getData().put("loginToken", ticket);
				loginCache.set(memberId+"dcLogin", memberId,31536000);//365天
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return restP;
		}
		
		@RequestMapping(value = "/avoidpwdlogin", method = RequestMethod.POST)
		@ResponseBody
		public RestResponse setLogin(HttpServletRequest request,
				HttpServletResponse response, OperationEnvironment env){
			log.info("dcregister...start");
			RestResponse restP = ResponseUtil.buildSuccessResponse();
			
			String token = request.getParameter("loginToken");
			//校验请求参数
			if(StringUtils.isBlank(token)){
				return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"loginToken不能为空");
			}
			try {
				String ticket = defaultUesService.getDataByTicket(token);
				log.info("setLogin：{}"+ticket);
				
				String[] data = ticket.split(",");
				String memberId = data[0];
				CacheRespone<String> cacherData = loginCache.get(memberId+"dcLogin");
				if(StringUtils.isBlank((String) cacherData.get())){
					logger.error("免密登录失败！，未查询到绑定缓存中memberId：{}" + memberId);
					return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),"免密登录失败,请重新设置手势图形！");
				}
				
				PersonMember person = new PersonMember();
				person.setLoginName(data[1]);
				person = memberService.queryMemberIntegratedInfo(person, env);
				
				/*LoginRequest loginReq=new LoginRequest();
				loginReq.setLoginName(person.getLoginName());
				//loginReq.setLoginPassowrd(loginPwd);
				loginReq.setLoginType(MEMBER_ID_ENTITY);
				LoginNameEditResp loginResp=new LoginNameEditResp();
				try {
					loginResp=loginFacadeService.login(loginReq);
					logger.info("统一账户登录：{}",loginResp.toString());
					if(!SysResponseCode.SUCCESS.getCode().equals(loginResp.getResponseCode())){
						restP.setMessage(loginResp.getResponseMessage());
						if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(loginResp.getResponseCode())) {
							restP.setMessage("账户未设置登录密码,请进行账户激活操作");
						}
						restP.setSuccess(false);
						restP.setCode(CommonDefinedException.PASSWORD_ERROR.getErrorCode());
						return restP;
					}
				}catch(BizException e){
					   logger.error("", e);
			           restP.setMessage(getErrMsg(e.getMessage()));
			           restP.setSuccess(false);
			           return restP;
				}*/
				
				
				//用户成功,登录密码正确
				PersonMember member = new PersonMember();
				member.setMemberId(person.getMemberId());
				member.setMemberName(person.getMemberName());
				member.setLoginName(person.getLoginName());
				member.setLockStatus(person.getLockStatus());
				member.setOperatorId(person.getOperatorId());
				
				if (MemberLockStatus.LOCKED.name().equals(member.getLockStatus().name())) {
					if (logger.isInfoEnabled()) {
		                logger.info(LogUtil.appLog(OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(), member, env));
					}
					restP.setMessage("会员已被锁定，请联系客服！");
					return restP;
				}
				
				// 保存登录信息到session
				this.saveToken(member, request, response, restP,
						CommonConstant.USERTYPE_PERSON,webResource.getVfssoDomain());
				if (logger.isInfoEnabled()) {
					logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
							member, env));
				}
				
				restP.setSuccess(true);
				restP.setMessage("登录成功！");
			} catch (ServiceException e) {
				e.printStackTrace();
			}catch(Exception e){
				 logger.error("", e);
			}
			return restP;
		}
		
	//获取加密数据
	@RequestMapping(value = "/getrsadata", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse getRsaData(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		String loginPwd = request.getParameter("loginPwd");
		String dePwd = RSASignUtil.encode(loginPwd, pubKey);
		restP.getData().put("resultRsaData", dePwd);
		return restP;
	}
		
	private Boolean register(DcRegisterRequest reqForm,RestResponse restP,
			OperationEnvironment env){
		PlatformInfo platformInfo = UnionmaConvert.createKjtPlatformInfo();
		RegisterRequestParam requestParam = new RegisterRequestParam();
		String registerType = MEMBER_ID_ENTITY.toUpperCase(); // 注册类别
		String loginName = reqForm.getLogin_name();// 登录名
		String	personIdentiy = MemberType.PERSONAL.getCode();// 人员身份 1.个人会员 2.企业会员
		/**
		 * 设置注册参数
		 */
		requestParam.setPlatformInfo(platformInfo);
		requestParam.setRegisterType(registerType);
		requestParam.setLoginName(loginName);
		requestParam.setPersonIdentiy(personIdentiy);
		requestParam.setExtention(StringUtils.EMPTY);
		requestParam.setInvitCode(reqForm.getInvit_code());
		logger.info("requestParam : {},loginName: {}", requestParam,
				loginName);
		RegisterResponse registerResponse = registerFacadeWS
				.register(requestParam);
		logger.info("registerResponse : {}", registerResponse);
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
				restP.setMessage(rstMsg);
				restP.setSuccess(false);
				return false;
			} else if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(
					code)) {
				logger.error("未设置登录密码");
			}
			logger.error("注册失败：{}" + rstMsg);
			restP.setMessage(rstMsg);
			restP.setSuccess(false);
			return false;
		} else {
			logger.error("注册成功");
		}
		restP.setMessage(rstMsg);
		String memberId  = registerResponse.getMemberId();
		if (StringUtils.isNotBlank(memberId)) {
			String token = UUID.randomUUID().toString().replace("-", "");
			String keyStr = "register_" + token + "_" + env.getClientIp();
			logger.info("注册registerActive放入缓存->key={}, memberId={}",
					keyStr, memberId);
			payPassWordCacheService.put(keyStr, memberId);
		}
		return true;
	}
	
	//设置登录密码
	public Boolean setLoginPwd(String loginPassword,String operatorId,RestResponse restP) {
		LoginPasswdRequest req = new LoginPasswdRequest();
		req.setOperatorId(operatorId);
		req.setValidateType(1);
		req.setPassword(MD5Util.MD5(loginPassword));
		logger.info("设置登录密码请求参数：{}",req.toString());
		CommResponse commRep = null;
		try {
			commRep = defaultLoginPasswdService.setLoginPassword(req);
			if (commRep.isSuccess()) {
				logger.info("设置登录密码成功");
				restP.setSuccess(true);
				restP.setMessage("注册成功！");
			} else {
				restP.setSuccess(false);
				restP.setMessage("设置登录密码失败:"+commRep.getResponseMessage());
				return false;
			}
		} catch (ServiceException e) {
			restP.setSuccess(false);
			restP.setMessage("设置登录密码失败:"+commRep.getResponseMessage());
			restP.setCode(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorCode());
			return false;
		}
		return true;
	}
	
	
	private RestResponse verifyYunOrWxData(String loginName,String invitCode,OperationEnvironment env,RestResponse restP){
		PersonMember person = new PersonMember();
		person.setLoginName(loginName);
		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			if(person == null){
				restP.setSuccess(true);
				return restP;
			}
			//注册会员是否已激活    针对云+用户
			boolean isActiveMember = person.getStatus()==MemberStatus.NOT_ACTIVE?false:true;
			if(!isActiveMember)
			{
				//云+数据
				log.info("云+数据未激活");
				//进行会员激活	
				PersonMember per=new PersonMember();
				per.setMemberId(person.getMemberId());
				per.setInvitCode(invitCode);
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
	            restP.setSuccess(false);
				return restP;
			}
			
			LoginPasswdRequest loginPasswdRequest=new LoginPasswdRequest();
			loginPasswdRequest.setValidateMode(1);	
			loginPasswdRequest.setOperatorId(person.getOperatorId());	
			Boolean pwdFalg = false;
			try{
				pwdFalg = loginPasswdService.validateLoginPwdIsNull(loginPasswdRequest);
				log.info("验证操作员是否设置登录密码（个人会员）:{}",pwdFalg);
			}catch (BizException e) {
				log.info("验证操作员是否设置登录密码出错");
			} 
			
			if(pwdFalg){
				log.info("未设置登录密码");
				restP.setSuccess(false);
				return restP;
			}
			
			if(!pwdFalg && isActiveMember)//未设置登录密码
			{
				restP.setSuccess(false);
				restP.setMessage("用户已存在");
				restP.setCode(CommonDefinedException.ACCOUNT_EXIST_ERROR.getErrorCode());
				return restP;
			}
			
		}catch(BizException e){
			log.info("账号未注册");
			restP.setSuccess(true);
			return restP;
		}catch(MemberNotExistException e){
			log.info("账号未注册");
			restP.setSuccess(true);
			return restP;
		} catch (Exception e) {
			restP.setSuccess(false);
			restP.setMessage("系统错误");
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
		}
		return restP;
	}
	
	/*
	 * 保存SsoUser并将其对应的token保存至cookie(单点登录用)
	 */
	public String saveToken(PersonMember member, HttpServletRequest request,
			HttpServletResponse response, RestResponse restP, String userType,String vfssoDomain) {
		String token = StringUtils.EMPTY;
		try {
			if ((null != member) && (null != member.getMemberId())) {
				clearLoginInfo(request, response,vfssoDomain);
				SsoUser ssoUser = new SsoUser();
				ssoUser.setId(member.getMemberId());
				logger.info("LoginName: " + member.getLoginName());
				ssoUser.setLoginName(member.getLoginName());
				ssoUser.setName(member.getMemberName());
				ssoUser.setUserType(userType);
				token = userSessionService.create(ssoUser);
				logger.info("token: " + token);
				if (token != null) {
					VfSsoCookie.setCookie(token, response);
					// 为了使cookie能在多个系统中共享，需要给cookie设置多个系统共通的域名
					logger.info("vfssoDomain: " + vfssoDomain);
					if (StringUtils.isNotBlank(vfssoDomain)) {
						for (String domain : vfssoDomain
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
					restP.setMessage("登录成功");
				} else {
					logger.error("登录失败！");
					restP.setMessage("登录失败");
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			//restP.setMessage(getErrMsg(e.getMessage()));
		}
		return token;
		
	}

	private void clearLoginInfo(HttpServletRequest req, HttpServletResponse resp,String vfssoDomain) {
		HttpSession session = req.getSession();
		session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
		session.invalidate();
		try {
			VfSsoCookie.removeCookie(resp);
			VfSsoCookie.removeCookie(resp, vfssoDomain);
			if (StringUtils.isNotBlank(vfssoDomain)) {
				for (String domain : vfssoDomain.split(",")) {
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
	
	/**
	 * 校验请求参数
	 * @return
	 */
	private RestResponse verifyReqParams(String loginName,String loginPwd,
			RestResponse restP){
		if(StringUtils.isBlank(loginName))
		{
			log.info("登录账号不能为空");
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"登录账号不能为空");
			return restP;
		}
		
		if(StringUtils.isBlank(loginPwd))
		{
			log.info("登录密码不能为空");
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"登录密码不能为空");
			return restP;
		}
		restP = ResponseUtil.buildSuccessResponse();
		return restP;
	}
	
	
	
}
