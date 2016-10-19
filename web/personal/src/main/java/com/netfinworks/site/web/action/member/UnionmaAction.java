/**
 * @company 永达互联网金融信息服务有限公司
 * @group 支付产品开发组
 * @project site-web-personal
 * @version 1.0.0
 * @date 2015年6月27日
 * 徐威/2015年6月27日/首次创建
 */
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.LoginNameEditRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
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

/**
 * <p>统一账户相关Action类</p>
 * @author 徐威
 * @date 2015年6月27日
 */
public class UnionmaAction extends BaseAction{
	protected Logger                  log = LoggerFactory.getLogger(getClass());
	
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
    
    @Resource(name = "memberService")
    private MemberService             memberService;
    
    @Resource(name = "loginFacadeService")
    private LoginFacadeService loginFacadeService;
    
    @Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
//	/**
//     * 统一账户登录，由kjt账户账户创建统一账户，显示统一后的信息
//     * @param model
//     * @param request
//     * @return
//     * @throws Exception 
//     */
//    @RequestMapping(value = "/my/kjtlogin-confirm-info.htm" )
//    public ModelAndView kjtLogin(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {
//    	String Mid = request.getParameter("Mid");
//    	RestResponse restP = new RestResponse();
//    	Map<String, Object> data = new HashMap<String, Object>();
//    	data.put("Mid", Mid);
//    	BaseMember kjtMember=new BaseMember();
//		kjtMember=memberService.queryMemberById(Mid,env);
//		data.put("loginName", kjtMember.getLoginName());
//		data.put("mobile", getEncryptInfo(request, DeciphedType.CELL_PHONE,Mid, env));
//		data.put("email", getEncryptInfo(request, DeciphedType.EMAIL,Mid, env));
//        restP.setData(data);
//        return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/kjtlogin-confirm-accountname","response",restP);
//    }
//    
//    /**
//     * 统一账户登录，由hry账户账户创建统一账户，有可用的手机和邮箱，供选择；有可用唯一的手机号/邮箱，供展示；没有可用的，让用户输入
//     * @param model
//     * @param request
//     * @return
//     * @throws Exception 
//     */
//    @RequestMapping(value = "/my/hrylogin-confirm-info.htm")
//    public ModelAndView hryLogin(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {
//
//    	String Mid = request.getParameter("Mid");
//    	RestResponse restP = new RestResponse();
//    	Map<String, Object> data = new HashMap<String, Object>();
//    	BaseMember kjtMember=new BaseMember();
//		kjtMember=memberService.queryMemberById(Mid,env);
//		data.put("loginName", kjtMember.getLoginName());
////		String mobile=getEncryptInfo(request, DeciphedType.CELL_PHONE,Mid, env);
//		String mobile="18258803234";
//		String email=getEncryptInfo(request, DeciphedType.EMAIL,Mid, env);
//		data.put("mobile", mobile==null?"false":mobile);
//		data.put("email", email==null?"false":email);
//    	data.put("Mid", Mid);
//        restP.setData(data);
//        restP.setSuccess(true);
//        if(mobile==null && email==null){
//        	return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-confirm-accountname1","response",restP);
//        }
//        return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-confirm-accountname1","response",restP);
//    }
//    
//    public String getEncryptInfo(HttpServletRequest request, DeciphedType type,String mid,
//            OperationEnvironment env) throws Exception {
//		EncryptData encryptData = commMemberService.decipherMember(mid,
//		type, DeciphedQueryFlag.ALL, env);
//		return encryptData.getPlaintext();
//		}
//   
//    /**
//     * hry账户设置登录名
//     * @param model
//     * @param request
//     * @return
//     * @throws Exception 
//     */
//    @RequestMapping(value = "/my/hrylogin-set-name.htm",method = RequestMethod.POST)
//    public ModelAndView hryloginSetName(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) throws Exception {
//
//    	String Mid = request.getParameter("Mid");
//    	RestResponse restP = new RestResponse();
//    	Map<String, Object> data = new HashMap<String, Object>();
//    	BaseMember kjtMember=new BaseMember();
//		kjtMember=memberService.queryMemberById(Mid,env);
//		data.put("loginName", kjtMember.getLoginName());
////	
//		String sendObject = request.getParameter("memberIdentity");//发送对象。手机还是邮箱、
//		String sjyzm = request.getParameter("mobileCaptcha");//校验码
//		String accountname = request.getParameter("accountname");//新的账户名（手机或邮箱）
//
//		//验证校验码
//		if(sendObject.equals("MOBILE")){
//			if (!validateOtpValue(sendObject,accountname,Mid,sjyzm,request,env)) {
//				restP.setMessage("验证码输入错误！");
//				data.put("type", "captcha");
//				data.put("msg", "验证码输入错误！");
//				restP.setData(data);
//				return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify-result", "response", restP);
//			}
//		}else if(sendObject.equals("EMAIL")) {
//			if (!validateOtpValue(sendObject,accountname,Mid,sjyzm,request,env)) {
//				restP.setMessage("验证码输入错误！");
//				data.put("type", "captcha");
//				data.put("msg", "验证码输入错误！");
//				restP.setData(data);
//				return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify-result", "response", restP);
//			}
//		}
//		LoginNameEditRequest req=new LoginNameEditRequest();
//		req.setMemberId(kjtMember.getMemberId());
//		req.setOperatorId(kjtMember.getOperatorId());
//		req.setLoginName(accountname);
//		req.setOldName(kjtMember.getLoginName());
//		req.setLoginNameType(sendObject);
//
//		try {
//			loginFacadeService.loginNameEdit(req);
//			restP.setData(data);
//			return new ModelAndView(CommonConstant.URL_PREFIX + "/unionma/hrylogin-update-login-pwd", "response", restP);
//		} catch (Exception e) {
//			data.put("msg", "账户名修改失败");
//			return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-name-modify-result", "response", restP);
//		}
//	}
//    
//    
//    
//    
//    /**
//	 * 异步发送邮件
//	 * 
//	 * @param request
//	 * @param env
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping(value = "/my/login-send-mail.htm", method = RequestMethod.POST)
//	@ResponseBody
//	private RestResponse ajaxSendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
//		RestResponse restP = new RestResponse();
//		String email = request.getParameter("email");
//		String bizType = request.getParameter("bizType"); 		
//		String randomCode=RandomStringUtils.randomAlphanumeric(4);
//		String activeUrl = randomCode;	
//		logger.info("激活邮箱的地址：{},发送邮件验证码：{}", email, randomCode);
//		Map<String, Object> objParams = new HashMap<String, Object>();
//		objParams.put("userName", email);
//		objParams.put("activeUrl", activeUrl);		
//		boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType, objParams);
//		logger.info("发送至{}邮箱的结果：{}", email, emailResult);
//		if (emailResult) {
//			// 调用统一缓存
//			String keyStr =email+"_"+randomCode.toLowerCase()+"_"+ env.getClientIp();
//			payPassWordCacheService.put(keyStr, email);
//		}
//		restP.setSuccess(true);	
//		return restP;
//	}
//    
//	@ResponseBody
//	@RequestMapping(value = "/my/login-send-mobile.htm")
//    public RestResponse sendMobileCertCode(HttpServletRequest request, HttpSession session, OperationEnvironment env) {
//        RestResponse restP = new RestResponse();
//        String Mid = request.getParameter("Mid");
//        String mobile = request.getParameter("mobile");
//        try {
//        	if("".equals(mobile) ||mobile ==null){
//        		mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
//        	}
//        	if (StringUtils.isEmpty(mobile)) {
//        		restP.setSuccess(false);
//        		restP.setMessage("您的账户尚未绑定手机号码!");
//        		return restP;
//        	}
//        	if (!Pattern.matches(ValidationConstants.MOBILE_PATTERN, mobile)) {
//				restP.setMessage("手机号码格式错误");
//				return restP;
//			}
//            // 验证码类型
//        	String bizType = request.getParameter("bizType");
//            AuthCodeRequest req = new AuthCodeRequest();
//            req.setMemberId(Mid);
//            req.setMobile(mobile);
//            req.setBizId(Mid);
//            req.setBizType(bizType);
//            String ticket = defaultUesService.encryptData(mobile);
//            req.setMobileTicket(ticket);
//            req.setValidity(CommonConstant.VALIDITY);
//            
//            if (defaultSmsService.sendMessage(req, env)) {
//                restP.setSuccess(true);
//                restP.setMessage("发送短信验证码成功");
//            }
//        } catch (Exception e) {
//        	restP.setSuccess(false);
//        	restP.setMessage("发送短信失败");
//            logger.error("发送短信失败", e);
//            return restP;
//        }
//        
//        return restP;
//    }
//	
//	/**
//	 * 
//	 * @param memberIdentity  会员身份
//	 * @param loginName  登录名
//	 * @param otpValue   验证码	
//	 * @param request
//	 * @param env
//	 * @return
//	 */
//	
//	
//	private boolean validateOtpValue(String memberIdentity,String loginName,String Mid,String otpValue, HttpServletRequest request, OperationEnvironment env) {
//		try {
//			    if("email".equals(memberIdentity)){
//			    	String keyStr =loginName+"_"+otpValue.toLowerCase()+"_"+ env.getClientIp();
//					String info = payPassWordCacheService.load(keyStr);	
//					if ((null == info) || "".equals(info)) {						
//						return false;
//					}			    
//				}else if("mobile".equals(memberIdentity)){				
//					AuthCodeRequest req = new AuthCodeRequest();
//					req.setMemberId(Mid);
//					req.setAuthCode(otpValue);
//					req.setMobile(loginName);
//					req.setMobileTicket(uesServiceClient.encryptData(loginName));
//					req.setBizId(Mid);
//					req.setBizType(BizType.ACC_UPD_TOPHONE.getCode());
//					boolean result = defaultSmsService.verifyMobileAuthCode(req, env);
//					return result;
//				}
//			 }
//			catch (Exception e) {			
//				return false;
//			}		
//		return true;
//	}
}
