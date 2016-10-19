package com.netfinworks.site.web.action.mobilephone;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * <p>绑定手机号码</p>
 * @author liangzhizhuang.m
 * @version $Id: MobilePhoneSetAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m Exp $
 */
@Controller
public class MobilePhoneSetAction extends BaseAction {

    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;

    @Resource(name = "authVerifyService")
    private AuthVerifyService       authVerifyService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "loginPasswdService")
	private LoginPasswdService loginPasswdService;
	
    protected static final Logger   logger = LoggerFactory.getLogger(MobilePhoneSetAction.class);

    /**
     * 跳转绑定手机页面(支付密码)
     */
    @RequestMapping(value = "/my/go-set-mobilephoneByPayPwd.htm")
    public ModelAndView goSetMobleByPayPwd(HttpServletRequest request) throws Exception{
    	RestResponse restP = new RestResponse();
    	restP.setData(initOcx());
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind2","response",restP);
    }
    
    /**
     * 手机绑定-验证支付密码
     */
    @RequestMapping(value = "/my/go-set-mobilephoneByValidPayPwd.htm",method = RequestMethod.POST)
    public ModelAndView goSetMobleByValidPayPwd(HttpServletRequest request) throws Exception{
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = initOcx();
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
		     restP.setData(data);
		     return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind","response",restP);
		 }
		 data.put("bizType",BizType.SET_MOBILE.getCode());
		 restP.setData(data);
		request.getSession().setAttribute(CommonConstant.SET_MOBILE_TOKEN, CommonConstant.SET_MOBILE_TOKEN);
		 return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind2","response",restP);
    }
    
    
    /**
     * 绑定手机号码
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/set-mobilephone.htm", method = RequestMethod.POST)
    public ModelAndView setMobile(HttpServletRequest request, OperationEnvironment env)throws Exception {
    	RestResponse restP = new RestResponse();
    	Map<String,Object>data = initOcx();
        String mobile = request.getParameter("mobile");
        String mobileCaptcha = request.getParameter("mobileCaptcha");
        String bizType = request.getParameter("bizType");
        
        try {
            PersonMember user = getUser(request);
            //封装校验短信验证码请求
            AuthCodeRequest req = new AuthCodeRequest();
            req.setMemberId(user.getMemberId());
            req.setMobile(mobile);
            String ticket = defaultUesService.encryptData(mobile);
            req.setMobileTicket(ticket);
            req.setAuthCode(mobileCaptcha);
            req.setBizId(user.getMemberId());
            req.setBizType(bizType);
            //校验短信验证码
            boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
            if (!messageResult) {
                logger.info("手机短信验证码验证失败！");
                data.put("validCode_error", "验证码错误");
                data.put("bizType", bizType);
                restP.setData(data);
                return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind2","response",restP);
            }

			String loginPasswd = decrpPassword(request, request.getParameter("loginPasswd"));// 登录密码
			if (StringUtils.isEmpty(loginPasswd)) {
				restP.setMessage("登录密码不能为空！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
			// 验证登录密码
			try {
				loginPasswdService.checkMemberLoginPwd(user.getLoginName(), loginPasswd, user.getMemberIdentity(),
						DEFAULT_SALT, env.getClientIp());
			} catch (BizException e) {
				restP.setMessage(e.getMessage());
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}

            AuthVerifyInfo info = new AuthVerifyInfo();
            info.setMemberId(user.getMemberId());
            info.setVerifyType(VerifyType.CELL_PHONE.getCode());
            info.setVerifyEntity(mobile);
            ResponseCode verifyResult = authVerifyService.createVerify(info, env);
            if (ResponseCode.SUCCESS == verifyResult) {
            	restP.setSuccess(true);
                updateSessionObject(request);
            } else if (ResponseCode.DUPLICATE_VERIFY == verifyResult) {
            	logger.error("认证信息重复");
            	restP.setSuccess(false);
				restP.setMessage(ResponseCode.DUPLICATE_VERIFY.getMessage());
            } else {
            	logger.error("绑定手机失败");
                restP.setSuccess(false);
				restP.setMessage("绑定手机失败");
            }
        } catch (Exception e) {
            logger.error("绑定手机错误：{}", e);
            e.printStackTrace();
            restP.setSuccess(false);
			restP.setMessage("绑定手机错误");
        }
        String former3 = mobile.substring(0, 3);
        String later4 = mobile.substring(7);
        data.put("mobile", former3+"****"+later4);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind3","response",restP);
    }

}
