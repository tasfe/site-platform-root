package com.netfinworks.site.web.action.mobilephone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * <p>解绑手机号码</p>
 * @author liangzhizhuang.m
 * @version $Id: MobilePhoneUnsetAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m Exp $
 */
@Controller
public class MobilePhoneUnsetAction extends BaseAction {

    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;

    @Resource(name = "authVerifyService")
    private AuthVerifyService       authVerifyService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    protected static final Logger   logger = LoggerFactory.getLogger(MobilePhoneUnsetAction.class);

    /**
     * 跳至解绑手机号码页面
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/go-unset-mobilephone.htm", method = RequestMethod.GET)
    public ModelAndView goUnsetMobile(HttpServletRequest request, HttpServletResponse resP,OperationEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        PersonMember user = getUser(request);
        AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
        authVerifyInfo.setMemberId(user.getMemberId());
        List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
        for (int i = 0; i < infos.size(); i++) {
            authVerifyInfo = infos.get(i);
            if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("mobile", authVerifyInfo.getVerifyEntity());
                data.put("mobileType", BizType.UNSET_MOBILE_USEMOBILE.getCode());
            } else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("email", authVerifyInfo.getVerifyEntity());
                data.put("emailType", BizType.UNSET_MOBILE_USEEMAIL.getCode());
            }
        }
        data.put("useType", "useMobile");
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/unset-mobilephone","response", restP);
    }
    
    /**
     * HYJ
     * 跳至解绑手机号码页面
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/go-unset-mobilephon.htm", method = RequestMethod.GET)
    public ModelAndView goUnbindMobile(HttpServletRequest request,HttpServletResponse resP,OperationEnvironment env){
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        PersonMember user = getUser(request);
        AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
        authVerifyInfo.setMemberId(user.getMemberId());
        List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
        for (int i = 0; i < infos.size(); i++) {
            authVerifyInfo = infos.get(i);
            if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("mobile", authVerifyInfo.getVerifyEntity());
                data.put("mobileType", BizType.UNSET_MOBILE_USEMOBILE.getCode());
            } else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("email", authVerifyInfo.getVerifyEntity());
                data.put("emailType", BizType.UNSET_MOBILE_USEEMAIL.getCode());
            }
        }
        data.put("useType", "useMobile");
        restP.setData(data);
    	return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_unbind","response", restP);
    }
    

    /**
     * 通过短信验证码解绑手机号码
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/unset-mobilephone-usemobilephone.htm", method = RequestMethod.POST)
    public ModelAndView unsetMobileByMobile(HttpServletRequest request, OperationEnvironment env)
                                                                                                 throws Exception {
        String mobileCaptcha = request.getParameter("mobileCaptcha");//用户输入的校验码
        String bizType = request.getParameter("bizType");
        String useType = request.getParameter("useType");
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        restP.setData(data);
        try {
//        	String pwd = request.getParameter("password");
            String password = decrpPassword(request, request.getParameter("password"));
            deleteMcrypt(request);
            PersonMember user = getUser(request);
            String email = "";
            String mobile = "";
            long verifyId = -1L;
            AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
            authVerifyInfo.setMemberId(user.getMemberId());
            List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
            for (int i = 0; i < infos.size(); i++) {
                authVerifyInfo = infos.get(i);
                if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
                    mobile = authVerifyInfo.getVerifyEntity();
                    verifyId = authVerifyInfo.getVerifyId();
                } else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
                    email = authVerifyInfo.getVerifyEntity();
                }
            }
            data.put("email", email);
            data.put("emailType", BizType.UNSET_MOBILE_USEEMAIL.getCode());
            data.put("mobile", mobile);
            data.put("mobileType", bizType);
            data.put("useType", useType);
            logger.info("解绑手机号码，mobile:{}，mobileCaptcha:{}，password：{}", mobile, mobileCaptcha,password);
            //校验加盐支付密码
            PayPasswdRequest payPasswsReq = new PayPasswdRequest();
            payPasswsReq.setOperator(user.getOperatorId());
            payPasswsReq.setAccountId(user.getDefaultAccountId());
            payPasswsReq.setOldPassword(password);
            payPasswsReq.setValidateType(1);
            PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
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
                return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_unbind", "response", restP);
            }
            //封装校验短信验证码请求
            mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
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
                data.put("mobileCaptcha_error", "验证码错误");
                return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_unbind", "response", restP);
            }
            //移除认证信息
            ResponseCode verifyResult = authVerifyService.deleteVerify(verifyId, env);
            if (ResponseCode.SUCCESS == verifyResult) {
                restP.setSuccess(true);
                updateSessionObject(request);
            } else {
            	restP.setSuccess(false);
				restP.setMessage("解绑手机失败");
            }
        } catch (Exception e) {
            logger.error("通过短信验证码解绑手机错误：{}", e);
            restP.setSuccess(false);
			restP.setMessage("解绑手机失败");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_unbind2","response", restP);
        }
        return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_unbind2","response", restP);
    }

}
