package com.netfinworks.site.web.action.sms;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.ValidationConstants;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.BankCardUtil;

/**
 *
 * <p>短信服务</p>
 * @author qinde
 * @version $Id: MsnAction.java, v 0.1 2013-12-13 上午10:10:37 qinde Exp $
 */
@Controller
public class MsnAction extends BaseAction {

    protected Logger                log = LoggerFactory.getLogger(getClass());

    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;

    @Resource(name = "authVerifyService")
    private AuthVerifyService       authVerifyService;

    @Resource(name = "memberService")
    private MemberService           memberService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    /**
     * 根据页面获取的手机号码，发送短信验证码
     *
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/register/sendMobileMessage.htm", method = RequestMethod.POST)
    public @ResponseBody
    RestResponse sendMobileMessage(HttpServletRequest request, OperationEnvironment env)
                                                                                        throws Exception {

        RestResponse restP = new RestResponse();
        try {
            HttpSession session = request.getSession();
            String mobile = request.getParameter("mobile");

            //封装发送短信请求
            AuthCodeRequest req = new AuthCodeRequest();
            req.setMobile(mobile);
            req.setBizId(session.getId());
            req.setBizType(BizType.SET_MOBILE.getCode());
            String ticket = defaultUesService.encryptData(mobile);
            req.setMobileTicket(ticket);
            req.setValidity(CommonConstant.VALIDITY);
            //发送短信
            if (defaultSmsService.sendMessage(req, env)) {
                restP.setSuccess(true);
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

    /**
     * 异步发送短信验证码
     *
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/mobile/sendMessage.htm", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody RestResponse sendMessageByMobile(HttpServletRequest request, OperationEnvironment env)
                                                                                          throws Exception {
        RestResponse restP = new RestResponse();
        EnterpriseMember user = getUser(request);
        try {
            String mobile = "";
            String bizType = request.getParameter("bizType");
            if (BizType.SET_MOBILE.getCode().equals(bizType)) {
                mobile = request.getParameter("mobile");
            } else if (BizType.UNSET_MOBILE_USEMOBILE.getCode().equals(bizType)
                       || BizType.UNSET_EMAIL_USEMOBILE.getCode().equals(bizType)) {
                mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
            } else if (BizType.RESET_MOBILE.getCode().equals(bizType)) {
                mobile = request.getParameter("mobile");
                if (StringUtils.isEmpty(mobile)) {
                    mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
                }
            } else if (BizType.REFIND_PAYPASSWD.getCode().equals(bizType)) {//短信找回支付密码
                mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
            } else if(BizType.CERT_APPLY.getCode().equals(bizType)
            			|| BizType.CERT_INSTALL.getCode().equals(bizType)
            			|| BizType.CERT_UNBIND.getCode().equals(bizType)
            			|| BizType.KJTSHILED_ACTIVE.getCode().equals(bizType)
            			|| BizType.KJTSHILED_UNBIND.getCode().equals(bizType)) {	//证书相关操作校验码
            	mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
            }
            if (StringUtils.isEmpty(mobile)) {
                restP.setSuccess(false);
                restP.setMessage("您的账户尚未绑定手机!");
                return restP;
            }
			if (!Pattern.matches(ValidationConstants.MOBILE_PATTERN, mobile)) {
				restP.setMessage("手机号码格式错误");
				return restP;
			}

            //封装发送短信请求
            AuthCodeRequest req = new AuthCodeRequest();
            req.setMemberId(user.getMemberId());
            req.setMobile(mobile);
            req.setBizId(user.getMemberId());
            req.setBizType(bizType);
            String ticket = defaultUesService.encryptData(mobile);
            req.setMobileTicket(ticket);
            req.setValidity(CommonConstant.VALIDITY);
            //发送短信
            if (defaultSmsService.sendMessage(req, env)) {
                restP.setSuccess(true);
                restP.setMessage("短信验证码发送成功");
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

    /**
     * 异步发送邮件
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/email/sendMail.htm", method = RequestMethod.GET)
    public @ResponseBody
    RestResponse ajaxSendMail(HttpServletRequest request, OperationEnvironment env)
                                                                                   throws Exception {
        RestResponse restP = new RestResponse();
        EnterpriseMember user = getUser(request);
        String bizType = request.getParameter("bizType");
        String email = "";
        String token = BankCardUtil.getUuid();
        String activeUrl = "";
		String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
        if (BizType.SET_EMAIL.getCode().equals(bizType)) {
            email = request.getParameter("email");
            activeUrl = host + "/my/set-email.htm?token=" + token;
        } else if (BizType.UNSET_EMAIL_USEEMAIL.getCode().equals(bizType)) {
            email = getEncryptInfo(request, DeciphedType.EMAIL, env);
            activeUrl = host + "/my/unset-email-useemail.htm?token=" + token;
        } else if (BizType.RESET_EMAIL.getCode().equals(bizType)) {
            email = request.getParameter("email");
            activeUrl = host + "/my/reset-email.htm?token=" + token;
            if (StringUtils.isEmpty(email)) {
                email = getEncryptInfo(request, DeciphedType.EMAIL, env);
                activeUrl = host + "/my/go-reset-set-email.htm?token=" + token;
            }
        } else if (BizType.UNSET_MOBILE_USEEMAIL.getCode().equals(bizType)) {
            email = getEncryptInfo(request, DeciphedType.EMAIL, env);
            activeUrl = host + "/my/unset-mobilephone-useemail.htm?token=" + token;
        }
        logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName",
            StringUtils.isEmpty(user.getMemberName()) ? email : user.getMemberName());
        objParams.put("activeUrl", activeUrl);
        boolean emailResult = defaultPayPasswdService.sendEmail(email, bizType, objParams);
        logger.info("发送至{}邮箱的结果：{}", email, emailResult);
        restP.setSuccess(emailResult);
        if (emailResult) {
            // 调用统一缓存
            String keyStr = token + user.getMemberId();
            payPassWordCacheService.put(keyStr, email);
        }
        return restP;
    }
    
    @ResponseBody
	@RequestMapping(value = "/cert/sendMobileCertCode.htm")
    public RestResponse sendMobileCertCode(HttpServletRequest request, HttpSession session, OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        EnterpriseMember user = this.getUser(request);
        
        try {
        	String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
        	if (StringUtils.isEmpty(mobile)) {
        		restP.setSuccess(false);
        		restP.setMessage("您的账户尚未绑定手机号码!");
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
	

}
