package com.netfinworks.site.web.action.email;

import java.util.HashMap;
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
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.request.UnionmaBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSendEmailRequest;
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
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.unionma.BindFacadeService;
import com.netfinworks.site.ext.integration.unionma.SmsFacadeService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.util.BankCardUtil;

/**
 * <p>绑定邮箱</p>
 * @author liangzhizhuang.m
 * @version $Id: EmailSetAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m Exp $
 */
@Controller
public class EmailSetAction extends BaseAction {

    @Resource(name = "authVerifyService")
    private AuthVerifyService       authVerifyService;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

    @Resource(name = "smsFacadeService")
    private SmsFacadeService smsFacadeService;
    
    @Resource(name = "bindFacadeService")
	private BindFacadeService bindFacadeService;
    
    protected static final Logger   logger = LoggerFactory.getLogger(EmailSetAction.class);
    
    /**
     * 选择绑定邮箱的方式
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/check-set-email.htm")
    public ModelAndView checksetEmail(HttpServletRequest request,OperationEnvironment env) throws Exception{
    	
    	String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
    	RestResponse restP = new RestResponse();
    	
    	Map<String,Object>data = new HashMap<String, Object>();
    	restP.setData(data);
    	PersonMember user = getUser(request);
    	data.put("loginName", user.getLoginName());
    	// 是否设置支付密码
    	boolean hasPayPwd=false;
    	if(user.getPaypasswdstatus() != MemberPaypasswdStatus.NOT_SET_PAYPASSWD){
    		hasPayPwd=true;
    	}
		if (hasPayPwd && mobile==null ) {//只有支付密码
	        return new ModelAndView("redirect:/my/go-set-emailByPayPwd.htm?type=payPwd");
		}
		if (!hasPayPwd && mobile!=null) {//只有手机
	        return new ModelAndView("redirect:/my/go-set-emailByPayPwd.htm?type=mobile");
		}
        data.put("Secretmobile", user.getMobileStar());
    	return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/check-set-email","response",restP);
    }
    
    /**
     * 跳至绑定邮箱(输入支付密码页面)
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/go-set-emailByPayPwd.htm")
    public ModelAndView setEmailByPayPwd(HttpServletRequest request,OperationEnvironment env) throws Exception{
    	String type = request.getParameter("type");
    	String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
    	RestResponse restP = new RestResponse();
    	
    	Map<String,Object>data = initOcx();
    	restP.setData(data);
    	data.put("type", type);
    	PersonMember user = getUser(request);
    	data.put("loginName", user.getLoginName());
    	// 是否设置支付密码
    	boolean hasPayPwd=false;
    	boolean hasBind=false;
    	if("payPwd".equals(type)){//只有支付密码
    		hasPayPwd=true;
			data.put("hasPayPwd", hasPayPwd);
	        return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind","response",restP);
    	}else{
			hasBind=true;
			data.put("hasBind", hasBind);
			String former3 = mobile.substring(0, 3);
	        String later4 = mobile.substring(7);
	        data.put("Secretmobile", former3+"****"+later4);
	        data.put("bizType", BizType.SET_EMAIL_BY_M.getCode());
	        return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind","response",restP);
		}
    }
    
    
    /**
     * 绑定邮箱--跳到输入新邮箱页面("通过手机验证码")
     * @param requestk
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/go-set-EmialByMobileForm.htm", method = RequestMethod.POST)
  public ModelAndView goSetEmail(HttpServletRequest request, HttpServletResponse resP)
                                                                     throws Exception {
      RestResponse restP = new RestResponse(); 
      String email = request.getParameter("email");
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("bizType", BizType.SET_EMAIL.getCode());
      restP.setData(data);
		if (!"true".equals(request.getSession().getAttribute("authcode"))) {
			return new ModelAndView("redirect:/error.htm");
		}
		request.getSession().removeAttribute("authcode");
		request.getSession().setAttribute(CommonConstant.SET_MOBILE_TOKEN, CommonConstant.SET_MOBILE_TOKEN);
		request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
		return new ModelAndView("redirect:/my/go-set-email-active.htm?bizType="+BizType.SET_EMAIL.getCode()+"&email="+email.trim());
    }
    
    
    /**
     * 邮箱绑定-验证支付密码
     */
    @RequestMapping(value = "/my/go-set-emailByValidPayPwd.htm",method = RequestMethod.POST)
	public ModelAndView goSetMobleByValidPayPwd(HttpServletRequest request, OperationEnvironment env) throws Exception {
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
    	String email = request.getParameter("email");
    	String type = request.getParameter("type");
//		String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
//		if (mobile != null) {
//			data.put("hasBind", "true");
//			String former3 = mobile.substring(0, 3);
//			String later4 = mobile.substring(7);
//			data.put("Secretmobile", former3 + "****" + later4);
//			data.put("bizType", BizType.SET_EMAIL_BY_M.getCode());
//		}
    	String encryptPwd = request.getParameter("password");//页面传来的加密密码
    	String password = decrpPassword(request, encryptPwd);
		 deleteMcrypt(request);
		 PersonMember user = getUser(request);
		 //校验加盐支付密码
		 PayPasswdRequest payPasswsReq = new PayPasswdRequest();
		 payPasswsReq.setOperator(user.getOperatorId());
		 payPasswsReq.setAccountId(user.getDefaultAccountId());
		 payPasswsReq.setOldPassword(password);
		 payPasswsReq.setValidateType(1);
		 PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
		 if (!checkResult.isSuccess()) {
		     logger.info("支付密码验证失败！");
		     int remainNum = checkResult.getRemainNum();
		     data.put("remainNum", remainNum);
		     if (checkResult.isLocked()) {
		    	 data.put("error_passwd", "密码被锁定");
		     } else {
		    	 data.put("error_passwd", "密码错误");
		     }
			 data.put("hasPayPwd", "true");
			 data.put("type", type);
		     restP.setData(data);
		     return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind","response",restP);
		 }
		 request.getSession().setAttribute(CommonConstant.SET_MOBILE_TOKEN, CommonConstant.SET_MOBILE_TOKEN);
		 request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
		 data.put("bizType","SET_EMAIL");
		 restP.setData(data);
		 return new ModelAndView("redirect:/my/go-set-email-active.htm?bizType="+BizType.SET_EMAIL.getCode()+"&email="+email.trim());
    }
    
    /**
     * 跳至绑定邮箱激活页面
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/go-set-email-active.htm",  method = {
			RequestMethod.POST, RequestMethod.GET })
    public ModelAndView goSetEmailActive(HttpServletRequest request, OperationEnvironment env) throws Exception {
        String email = request.getParameter("email");
        String bizType = request.getParameter("bizType");
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
		if (!CommonConstant.REFRESH_TOKEN.equals(request.getSession().getAttribute(CommonConstant.REFRESH_TOKEN))) {
			return new ModelAndView("redirect:/my/check-set-email.htm");
		}
        try {
            deleteMcrypt(request);
            data.put("url", LoginEmailAddress.getEmailLoginUrl(email));
            data.put("bizType", bizType);
            if (newSendMail(request, env)) {
                data.put("email", email);
                restP.setData(data);
				request.getSession().removeAttribute(CommonConstant.REFRESH_TOKEN);
                return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailBind3","response", restP);
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
     * 绑定邮箱
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/set-email.htm", method = RequestMethod.GET)
    public ModelAndView setEmail(HttpServletRequest request, OperationEnvironment env) throws Exception {
        String token = request.getParameter("token");
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        restP.setData(data);
        try {
			String emailToken = (String) request.getSession().getAttribute(CommonConstant.SET_MOBILE_TOKEN);
			if (!(CommonConstant.SET_MOBILE_TOKEN).equals(emailToken)) {
				return new ModelAndView("redirect:/my/check-set-email.htm");
			}
            PersonMember user = getUser(request);
            String email = payPassWordCacheService.load(token + user.getMemberId());
            if (!StringUtils.isEmpty(email)) {
            	UnionmaBindEmailRequest bindReq=new UnionmaBindEmailRequest();
				bindReq.setMemberId(user.getMemberId());
				bindReq.setToken(token);
				UnionmaBindEmailResponse result=bindFacadeService.bindEmail(bindReq);
                if (result.getIsSuccess()) {                	
                    payPassWordCacheService.clear(token + user.getMemberId());
                    restP.setSuccess(true);
                    restP.setMessage("set_email_sucess");
                    updateSessionObject(request);
                    AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.EMAIL, env);
                    data.put("email", verifyInfo.getVerifyEntity());
                    if (logger.isInfoEnabled()) {
    	                logger.info(LogUtil.appLog(OperateeTypeEnum.SETEMAIL.getMsg(), user, env));
    				}
                } else if ("EMAIL_ALREADY_SET".equals(result.getResponseCode())) {
					restP.setMessage("认证信息重复");
                    restP.setSuccess(false);
                } else {
                	restP.setSuccess(false);
					restP.setMessage("绑定邮箱失败");
                }
            } else {
            	restP.setSuccess(false);
				restP.setMessage("验证邮箱超时");
            }
        } catch (Exception e) {
            logger.error("绑定邮箱错误：{}", e);
            e.printStackTrace();
			restP.setMessage("绑定邮箱失败");
            restP.setSuccess(false);
    		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
        }
		request.getSession().removeAttribute(CommonConstant.SET_MOBILE_TOKEN);
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_mailResult", "response", restP);
    }
    
    /**
     * 发送邮件
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
        String activeUrl =  request.getScheme()+"://"+ request.getServerName() + ":" + request.getServerPort()
        	    + request.getContextPath() + "/my/set-email.htm?token=" + token;
        logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName",
            StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
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
        String activeUrl =  request.getScheme()+"://"+ request.getServerName() + ":" + request.getServerPort()
        	    + request.getContextPath() + "/my/set-email.htm?token=" + token;
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
        logger.info("发送至{}邮箱的结果：{}", email, result.getIsSuccess());
        if (result.getIsSuccess()) {
            // 调用统一缓存
            String keyStr = token + user.getMemberId();
            payPassWordCacheService.put(keyStr, email);
        }
        return result.getIsSuccess();
    }
}
