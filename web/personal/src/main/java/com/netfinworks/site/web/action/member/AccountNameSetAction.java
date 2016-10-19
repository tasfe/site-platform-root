package com.netfinworks.site.web.action.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.LoginNameEditRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.site.web.ValidationConstants;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.voucher.common.utils.JsonUtils;
/**
 * 修改登录名control
 *
 * @author 赵贞强
 *
 * @version   2015-06-24 下午19:47:25
 *
 */
@Controller
public class AccountNameSetAction extends BaseAction{

    @Resource(name = "authVerifyService")
    private AuthVerifyService    authVerifyService;

	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 

	@Resource(name = "defaultSmsService")
    private DefaultSmsService defaultSmsService;
	
	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
	@Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;
	
	@Value("${sendMessageNumber}")
	private String             sendMessageNumber;

	@Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
	
	@Resource(name = "loginFacadeService")
    private LoginFacadeService       loginFacadeService;
	
	@Resource(name = "memberService")
    private MemberService             memberService;
	
	
	/**
	 * 修改用户名-身份验证
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/accountNameSetting.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView accountNameSetting(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		String verify = request.getParameter("verify");//是否有实名认证
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
		
		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(user.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		String mobile="";
		String email="";
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				mobile=authVerifyInfo.getVerifyEntity();
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				email=authVerifyInfo.getVerifyEntity();
			}
		}
		String sendGoals="";
		String sendObject="";
		if(!mobile.equals("")){
			sendGoals="手机："+mobile;
			sendObject="mobile";
		}else {
			sendGoals="邮箱："+email;
			sendObject="email";
		}
		data.put("sendGoals", sendGoals);
		data.put("sendObject", sendObject);
		data.put("verify", verify);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-setting-verification", "response", restP);
	}
	
	/**
	 * 异步发送邮件
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/send-mail.htm", method = RequestMethod.POST)
	@ResponseBody
	private RestResponse ajaxSendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String email = request.getParameter("email");
		if("".equals(email) ||email ==null){
			email  = getEncryptInfo(request, DeciphedType.EMAIL, env);
		}
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
	@RequestMapping(value = "/my/send-mobile.htm")
    public RestResponse sendMobileCertCode(HttpServletRequest request, HttpSession session, OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        PersonMember user = this.getUser(request);
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
            req.setMemberId(user.getMemberId());
            req.setMobile(mobile);
            req.setBizId(user.getMemberId());
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
	 * 修改用户名-修改
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/accountNamemodify.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView accountNamemodify(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		String sendObject = request.getParameter("sendObject");//发送对象。手机还是邮箱、
		String verify = request.getParameter("verify");//是否有实名认证
		String IDcardvalue = request.getParameter("IDcardvalue");//身份证号
		
		String sjyzm = request.getParameter("mobileCaptcha");//校验码
		
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("sendObject", sendObject);
		data.put("verify", verify);
		//验证校验码
		String sendGoals="";
		if(sendObject.equals("mobile")){
			String mobile  = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
			sendGoals="手机："+mobile;
			data.put("sendGoals", sendGoals);
			if (!validateOtpValue(sendObject,mobile,sjyzm,request,env)) {
				restP.setMessage("验证码输入错误！");
				restP.setData(data);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-setting-verification", "response", restP);
			}
		}else if(sendObject.equals("email")) {
			String email  = getEncryptInfo(request, DeciphedType.EMAIL, env);
			sendGoals="邮箱："+email;
			data.put("sendGoals", sendGoals);
			if (!validateOtpValue(sendObject,email,sjyzm,request,env)) {
				restP.setMessage("验证码输入错误！");
				restP.setData(data);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-setting-verification", "response", restP);
			}
		}
		//对实名认证用户验证身份证
		
		if(verify.equals("true")){
			String IDcard  = getEncryptInfo(request, DeciphedType.ID_CARD, env);
			if(!IDcard.toLowerCase().equals(IDcardvalue.trim().toLowerCase())){
				restP.setMessage("身份证号有误！");
				restP.setData(data);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-setting-verification", "response", restP);
				
			}
		}
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify", "response", restP);
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
			PersonMember user = getUser(request);
			    if("email".equals(memberIdentity)){
			    	String keyStr =loginName+"_"+otpValue.toLowerCase()+"_"+ env.getClientIp();
					String info = payPassWordCacheService.load(keyStr);	
					if ((null == info) || "".equals(info)) {						
						return false;
					}			    
				}else if("mobile".equals(memberIdentity)){				
					AuthCodeRequest req = new AuthCodeRequest();
					req.setMemberId(user.getMemberId());
					req.setAuthCode(otpValue);
					req.setMobile(loginName);
					req.setMobileTicket(uesServiceClient.encryptData(loginName));
					req.setBizId(user.getMemberId());
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
	
	
	
	/**
	 * 修改用户名-提交确认
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/accountModifySubmit.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView accountModifySubmit(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env)  {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String password = decrpPassword(request, request.getParameter("password"));//支付密码
		String sendObject = request.getParameter("sendObject");//发送对象。手机还是邮箱、
		String sjyzm = request.getParameter("mobileCaptcha");//校验码
		String accountname = request.getParameter("accountname");//新的账户名（手机或邮箱）
		String verify = request.getParameter("verify");
		data.put("verify", verify);
		// 校验支付密码
		PersonMember user = getUser(request);
		PayPasswdRequest payPasswsReq = new PayPasswdRequest();
		payPasswsReq.setOperator(user.getOperatorId());
		payPasswsReq.setAccountId(user.getDefaultAccountId());
		payPasswsReq.setOldPassword(password);
		payPasswsReq.setValidateType(1);
		PayPasswdCheck checkResult= new PayPasswdCheck();
		try {
			checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
			restP.setData(data);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify", "response", restP);
		}
		//验证校验码
		if(sendObject.equals("mobile")){
			if (!validateOtpValue(sendObject,accountname,sjyzm,request,env)) {
				restP.setMessage("验证码输入错误！");
				restP.setData(data);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify", "response", restP);
			}
		}else if(sendObject.equals("email")) {
			if (!validateOtpValue(sendObject,accountname,sjyzm,request,env)) {
				restP.setMessage("验证码输入错误！");
				restP.setData(data);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify", "response", restP);
			}
		}
		try {
			LoginNameEditRequest req=new LoginNameEditRequest();
			req.setMemberId(user.getMemberId());
			req.setOperatorId(user.getOperatorId());
			req.setLoginName(accountname);
			req.setOldName(user.getLoginName());
			if(sendObject.equals("email")){
				req.setLoginNameType("EMAIL");
				int index = accountname.indexOf("@");
				user.setEmail(StarUtil.hideStrBySym(accountname, 1, accountname.length()-index, 3));
				user.setEmailStar(StarUtil.hideStrBySym(accountname, 1, accountname.length()-index, 3));
			}else if("mobile".equals(sendObject)){
				req.setLoginNameType("MOBILE");
				user.setMobile(StarUtil.mockMobile(accountname));
				user.setMobileStar(StarUtil.mockMobile(accountname));
			}
		
			UnionmaBaseResponse baseResp=loginFacadeService.loginNameEdit(req);
			if(baseResp.getIsSuccess()){
				user.setLoginName(accountname);
				request.getSession().setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER,
						JsonUtils.toJson(user));
				data.put("type", "success");
				data.put("msg", "您的账户名修改成功");
			}else {
				data.put("msg", "该登录名已被占用！");
				if(SysResponseCode.DUPLICATE_LOGIN_NAME.getCode().equals(baseResp.getResponseCode())){
					data.put("msg", "该登录名已被占用！");
				}
			}
			restP.setData(data);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify-result", "response", restP);
		} catch (Exception e) {
			data.put("msg", "账户名修改失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify-result", "response", restP);
		}
	}
	
}
