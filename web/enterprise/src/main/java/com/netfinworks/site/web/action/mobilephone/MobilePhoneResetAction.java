package com.netfinworks.site.web.action.mobilephone;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;

/**
 * <p>修改绑定手机号码</p>
 * @author liangzhizhuang.m
 * @version $Id: MobilePhoneResetAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m Exp $
 */
@Controller
public class MobilePhoneResetAction extends BaseAction {

    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;

    @Resource(name = "authVerifyService")
    private AuthVerifyService       authVerifyService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;
    
    @Resource(name="operatorService")
    private OperatorService operatorService;

    protected static final Logger   logger = LoggerFactory.getLogger(MobilePhoneResetAction.class);

    /**
     * 跳至修改绑定手机号码页面
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/go-reset-mobilephone.htm", method = RequestMethod.GET)
    public ModelAndView goResetMobile(HttpServletRequest request, OperationEnvironment env)
                                                                                           throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errors = new HashMap<String, String>();
        data.put("bizType", BizType.RESET_MOBILE.getCode());
		// AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);
		// data.put("mobile", verifyInfo.getVerifyEntity());
		// data.put("mobileCaptcha_error", request.getParameter("mobileCaptcha_error"));
		// if (request.getParameter("remainNum") != null) {
		// errors.put("remainNum", request.getParameter("remainNum").toString());
		// }
		// if (request.getParameter("error_passwd_is_locked") != null) {
		// errors.put("error_passwd_is_locked", request.getParameter("error_passwd_is_locked")
		// .toString());
		// }
		// if (request.getParameter("error_passwd_not_right") != null) {
		// errors.put("error_passwd_not_right", request.getParameter("error_passwd_not_right")
		// .toString());
		// }

		AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);

		if ((null == verifyInfo.getVerifyEntity()) || "".equals(verifyInfo.getVerifyEntity())) {
			restP.setMessage("您尚未绑定手机！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}

		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}

        restP.setData(data);
        restP.setErrors(errors);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-mobilephone",
            "response", restP);
    }

	/**
	 * 解绑-手机号码不可用
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/reset-useLicenseNo.htm", method = RequestMethod.GET)
	public ModelAndView useLicenseNo(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		EnterpriseMember user = getUser(request);
		
		// 硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            data.put("isCertActive", "yes");
        } else {
            data.put("isCertActive", "no");
        }
		restP.setData(data);
		if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
			restP.setMessage("请先进行实名认证！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-result", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-mobilephone-licenseNo", "response",
				restP);
	}

    /**
     * 跳至绑定新手机号码页面
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/go-reset-set-mobilephone.htm", method = RequestMethod.POST)
    public ModelAndView goResetSetMobile(HttpServletRequest request, OperationEnvironment env,
                                         ModelMap map) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		data.put("isCertActive", request.getParameter("isCertActive"));
		restP.setData(data);
		try {
			// 校验支付密码
			String password = decrpPassword(request, request.getParameter("password"));
			deleteMcrypt(request);
			EnterpriseMember user = getUser(request);
			String useType = request.getParameter("useType");

			data.put("useType", useType);
			data.put("bizType", BizType.RESET_MOBILE.getCode());

			// 校验加盐支付密码
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getCurrentOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(password);
			payPasswsReq.setValidateType(1);
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (!checkResult.isSuccess()) {
				int remainNum = checkResult.getRemainNum();
				Map<String, String> errors = new HashMap<String, String>();
				errors.put("remainNum", remainNum + "");
				if (checkResult.isLocked()) {
					errors.put("error_passwd_is_locked", "error_passwd_is_locked");
                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
				} else {
					errors.put("error_passwd_not_right", "error_passwd_not_right");
				}
				restP.setErrors(errors);
				if (useType.equals("usePhone")) {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-mobilephone", "response",
							restP);
				} else {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-mobilephone-licenseNo",
							"response",
							restP);
				}

			}
			AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
			authVerifyInfo.setMemberId(user.getMemberId());
			List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
			String mobile = "";
			long verifyId = -1L;
			for (int i = 0; i < infos.size(); i++) {
				authVerifyInfo = infos.get(i);
				if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
					mobile = authVerifyInfo.getVerifyEntity();
					verifyId = authVerifyInfo.getVerifyId();
				}
			}

			if (useType.equals("usePhone")) {
				String mobileCaptcha = request.getParameter("mobileCaptcha");
				logger.info("解绑手机号码，mobile:{}，mobileCaptcha:{}，password：{}", mobile, mobileCaptcha, password);
				// 封装校验短信验证码请求
				mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
				AuthCodeRequest req = new AuthCodeRequest();
				req.setMemberId(user.getMemberId());
				req.setMobile(mobile);
				String ticket = defaultUesService.encryptData(mobile);
				req.setMobileTicket(ticket);
				req.setAuthCode(mobileCaptcha);
				req.setBizId(user.getMemberId());
				req.setBizType(BizType.RESET_MOBILE.getCode());
				// 校验短信验证码
				boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
				if (!messageResult) {
					logger.info("手机短信验证码验证失败！");
					data.put("mobileCaptcha_error", "您输入的验证码不正确，请重新输入");
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-mobilephone", "response",
							restP);
				}
			} else if (useType.equals("useLicenseNo")) {
				String licenseNo = request.getParameter("busCode");
				if (!licenseNo.equals(user.getLicenseNo())) {
					logger.info("营业执照号验证失败！");
					data.put("licenseNo_error", "营业执照号验证失败！");
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-mobilephone-licenseNo",
							"response", restP);
				}
			}

			request.getSession().setAttribute(CommonConstant.RESET_MOBILE_TOKEN, CommonConstant.RESET_MOBILE_TOKEN);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-set-mobilephone", "response", restP);
		} catch (Exception e) {
			logger.error("解绑手机错误：{}", e);
		}
		return new ModelAndView("redirect:/my/reset-mobilephone-result.htm?useType=" + request.getParameter("useType"));
    }

    /**
     * 绑定新手机号码
     * @param request
     * @param env
     * @return
     */
    @RequestMapping(value = "/my/reset-mobilephone.htm", method = RequestMethod.POST)
    public ModelAndView resetMobile(HttpServletRequest request, OperationEnvironment env, ModelMap map)
                                                                                         throws Exception {
        String mobile = request.getParameter("mobile");
        String mobileCaptcha = request.getParameter("mobileCaptcha");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
        try {
			String token = (String) request.getSession().getAttribute(CommonConstant.RESET_MOBILE_TOKEN);
			if (!token.equals(CommonConstant.RESET_MOBILE_TOKEN)) {
				return new ModelAndView("redirect:/my/go-reset-mobilephone.htm");
			}
            EnterpriseMember user = getUser(request);
            //封装校验短信验证码请求
            AuthCodeRequest req = new AuthCodeRequest();
            req.setMemberId(user.getMemberId());
            req.setMobile(mobile);
            String ticket = defaultUesService.encryptData(mobile);
            req.setMobileTicket(ticket);
            req.setAuthCode(mobileCaptcha);
            req.setBizId(user.getMemberId());
            req.setBizType(BizType.RESET_MOBILE.getCode());
            //校验短信验证码
            boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
            logger.info("手机短信验证码校验结果：{}", messageResult);
            if (!messageResult) {
            	String useType = request.getParameter("useType");
    			data.put("useType", useType);
				data.put("mobileCaptcha_error", "您输入的验证码不正确，请重新输入");
				data.put("mobile", mobile);
				data.put("bizType", BizType.RESET_MOBILE.getCode());
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-set-mobilephone", "response",
						restP);
            }
            AuthVerifyInfo info = new AuthVerifyInfo();
            info.setMemberId(user.getMemberId());
            info.setVerifyType(VerifyType.CELL_PHONE.getCode());
            info.setVerifyEntity(mobile);
            AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);
            info.setVerifyId(verifyInfo.getVerifyId());
            ResponseCode verifyResult = authVerifyService.updateVerify(info, env);
            if (ResponseCode.SUCCESS == verifyResult) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.MOD_PHONE, user, env, mobile));
                map.put("success", true);
                map.put("message", "reset_mobile_sucess");
                updateSessionObject(request);
                verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);
                map.put("mobile", verifyInfo.getVerifyEntity());
                //修改默认操作员联系方式
				OperatorVO adminOperator = operatorService.getDefaultOperator(this.getUser(request).getMemberId(), env);
				adminOperator.setContact(mobile);
				operatorService.updateOperator(adminOperator, env);
            } else if (ResponseCode.DUPLICATE_VERIFY == verifyResult) {
                map.put("success", false);
				map.put("message", ResponseCode.DUPLICATE_VERIFY.getMessage());
                map.put("mobile", mobile);
            } else {
                map.put("success", false);
				map.put("message", "修改手机绑定失败");
            }
			request.getSession().removeAttribute(CommonConstant.RESET_MOBILE_TOKEN);
        } catch (Exception e) {
            logger.error("修改绑定手机错误：{}", e);
            e.printStackTrace();
            map.put("success", false);
            map.put("message", "reset_mobile_fail");
        }
		return new ModelAndView("redirect:/my/reset-mobilephone-result.htm?useType=" + request.getParameter("useType"));
    }
    
    /**
     * 跳至修改绑定结果页面
     * @param request
     * @param resp
     * @return
     */
    @RequestMapping(value = "/my/reset-mobilephone-result.htm")
    public ModelAndView resetResult(HttpServletRequest request, OperationEnvironment env)
                                                                                         throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("mobile", request.getParameter("mobile"));
		data.put("useType", request.getParameter("useType"));
        restP.setMessage(request.getParameter("message"));
        restP.setSuccess(Boolean.valueOf(request.getParameter("success")));
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephone/reset-result", "response",
            restP);
    }

}
