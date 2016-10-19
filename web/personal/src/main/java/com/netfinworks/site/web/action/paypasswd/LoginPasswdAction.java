package com.netfinworks.site.web.action.paypasswd;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.LoginPasswdCheck;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.site.domain.enums.LoginNamePlatformTypeEnum;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.form.ResetLoginPasswordForm;

/**
 * <p>登录密码control</p>
 * @author zhangyun.m
 * @version $Id: LoginPasswdAction.java, v 0.1 2014年5月20日 下午2:17:30 zhangyun.m Exp $
 */
@Controller
public class LoginPasswdAction extends BaseAction {

    protected Logger                  log = LoggerFactory.getLogger(getClass());

    @Resource(name = "webResource")
    private WebDynamicResource        webResource;

    @Resource(name = "defaultLoginPasswdService")
    private DefaultLoginPasswdService defaultLoginPasswdService;

    /**
     * 重置登录密码页面
     *
     * @param model  
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/reset-login-passwd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView resetPayPasswd(HttpServletRequest request, HttpServletResponse resp,
                                       ModelMap model) throws Exception {
        PersonMember user = getUser(request);
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();

        data.put("member", user);
        restP.setErrors(errorMap);
        restP.setData(data);
        return new ModelAndView(
            CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response", restP);
    }

    /** 重置登录密码（个人或者企业会员）
     * @param request
     * @param resp
     * @param form
     * @param model  
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/do-reset-loginpasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setNewLoginPasswd(HttpServletRequest request, HttpServletResponse resp,
                                          @Validated ResetLoginPasswordForm form, ModelMap model,OperationEnvironment env)
                                                                                                 throws Exception {
        PersonMember user = getUser(request);
        if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.RESET_LOGIN_PASSWD.getMsg(), user, env));
		}
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>(); 
		String oldPasswd = decrpPassword(request, form.getOldPasswd());
		String newPasswd = decrpPassword(request, form.getNewPasswd());
		String renewPasswd = decrpPassword(request, form.getRenewPasswd());
        //清空随机因子
		deleteMcrypt(request);
        String resultMapping = "/my/reset-login-passwd.htm";
        Map<String, String> errorMap = new HashMap<String, String>();
        data.put("member", user);
        restP.setData(data);
        if (oldPasswd == null) {
            errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response", restP);
        } else if (newPasswd == null) {
            errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response", restP);
        } else if (renewPasswd == null) {
            errorMap.put("paypasswd_not_exist", "paypasswd_not_exist");
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response", restP);
		} else if ((newPasswd.length() < 6) || (newPasswd.length() > 32)) {
            errorMap.put("error_passwd_parten", "error_passwd_parten");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response", restP);
        } else if (!newPasswd.equals(renewPasswd)) {
            errorMap.put("repassword_password_not_same", "repassword_password_not_same");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response", restP);
        }

		// 1.验证旧登录密码
		LoginPasswdCheck checkResult = new LoginPasswdCheck();
		if (user.getMemberType() == MemberType.ENTERPRISE) {
			OperatorLoginPasswdRequest req = new OperatorLoginPasswdRequest();
			req.setOperatorId(user.getOperatorId());
			req.setOldPassword(oldPasswd);
			req.setValidateType(1);
			req.setPassword(newPasswd);
			req.setClientIp(request.getRemoteAddr());

			req.setLoginName(user.getLoginName());
			req.setLoginNamePlatformType("" + LoginNamePlatformTypeEnum.KJT.getCode());
			checkResult = defaultLoginPasswdService.checkEntLoginPasswd(req);

			if (checkResult.isSuccess() && !checkResult.isLocked()) {
				if (newPasswd.equals(oldPasswd)) {// 新登录密码不能和旧登录密码相同
					errorMap.put("newloginpwd_equal_oldloginpwd", "newloginpwd_equal_oldloginpwd");
					restP.setErrors(errorMap);
					return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(),
							"response", restP);
				}
				// 2.验证旧密码是否锁住
				if (!defaultLoginPasswdService.isLoginPwdClockedEnt(req)) {
					// 3.没有锁住 ，则设置登录密码。
					CommResponse commRep = defaultLoginPasswdService.setLoginPasswordEnt(req);
					if (commRep.isSuccess()) {
						restP.setSuccess(true);
					} else {
						restP.setSuccess(false);
						log.error("用户修改登录密码失败:{}", user);
						if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(commRep.getResponseCode())) {
							errorMap.put("login_password_equal_pay", "login_password_equal_pay");
							restP.setErrors(errorMap);
							return new ModelAndView(
									CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response",
									restP);
						} else {
							restP.setMessage("设置登录密码失败");
						}
					}

				}

				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/reset/reset-loginpasswd-success",
						"response", restP);
			}
		} else {
			LoginPasswdRequest req = new LoginPasswdRequest();
			req.setOperatorId(user.getOperatorId());
			req.setOldPassword(oldPasswd);
			req.setValidateType(1);
			req.setPassword(newPasswd);
			req.setClientIp(request.getRemoteAddr());
			// 会员标识
			req.setMemberIdentity(user.getMemberIdentity());
			// 会员标识平台类型 1.UID
			req.setPlatformType(user.getPlatformType());
			checkResult = defaultLoginPasswdService.checkPersonalLoginPasswd(req);
			
			if (checkResult.isSuccess() && !checkResult.isLocked()) {
				if (newPasswd.equals(oldPasswd)) {// 新登录密码不能和旧登录密码相同
					errorMap.put("newloginpwd_equal_oldloginpwd", "newloginpwd_equal_oldloginpwd");
					restP.setErrors(errorMap);
					return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(),
							"response", restP);
				}
				// 2.验证旧密码是否锁住
				if (!defaultLoginPasswdService.isLoginPwdClocked(req)) {
					// 3.没有锁住 ，则设置登录密码。
					CommResponse commRep = defaultLoginPasswdService.setLoginPassword(req);
					if (commRep.isSuccess()) {
						restP.setSuccess(true);
					} else {
						restP.setSuccess(false);
						log.error("用户修改登录密码失败:{}", user);
						if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(commRep.getResponseCode())) {
							errorMap.put("login_password_equal_pay", "login_password_equal_pay");
							restP.setErrors(errorMap);
							return new ModelAndView(
									CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response",
									restP);
						} else {
							restP.setMessage("设置登录密码失败");
						}
					}

				}

				return new ModelAndView(CommonConstant.URL_PREFIX + "/paypasswd/reset/reset-loginpasswd-success",
						"response", restP);
			}
		}

		if (!checkResult.isSuccess()) {

			if (checkResult.isLocked()) {
				errorMap.put("login_password_is_locked", "login_password_is_locked");
				restP.setErrors(errorMap);
				return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(),
						"response", restP);
			} else {
				errorMap.put("login_password_is_error", "login_password_is_error");
				restP.setErrors(errorMap);
				data.put("remainNum", String.valueOf(checkResult.getRemainNum()));
				return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(),
						"response", restP);
			}

		}
		return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response",
				restP);


    }
    
    
    

}
