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

import com.kjt.unionma.api.bind.response.BindMobileEditResponse;
import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.request.UnionmaBindMobileEditRequest;
import com.netfinworks.site.domain.domain.request.UnionmaVerifyAuthCodeRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaBindMobileEditResponse;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.unionma.BindFacadeService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;

/**
 * <p>
 * 修改绑定手机号码
 * </p>
 * 
 * @author liangzhizhuang.m
 * @version $Id: MobilePhoneResetAction.java, v 0.1 2014年5月20日 下午4:45:48
 *          liangzhizhuang.m Exp $
 */
@Controller
public class MobilePhoneResetAction extends BaseAction {

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "memberService")
	private MemberService memberService;

	@Resource(name = "bindFacadeService")
	private BindFacadeService bindFacadeService;
	
	protected static final Logger logger = LoggerFactory.getLogger(MobilePhoneResetAction.class);

	/**
	 * 跳至修改绑定手机号码页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-reset-mobilephone.htm", method = RequestMethod.GET)
	public ModelAndView goResetMobile(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		Map<String, String> errors = new HashMap<String, String>();
		data.put("bizType", BizType.RESET_MOBILE.getCode());
		// AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.CELL_PHONE, env);
		// data.put("mobile", verifyInfo.getVerifyEntity());
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_amend", "response", restP);
	}

	/**
	 * 跳至绑定新手机号码页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-reset-set-mobilephone.htm", method = RequestMethod.POST)
	public ModelAndView goResetSetMobile(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String mobileCaptcha = request.getParameter("mobileCaptcha");// 短信验证码
		String bizType = request.getParameter("bizType");// 绑定类型
		Map<String, Object> data = new HashMap<String, Object>();
		String flag = request.getParameter("flag");
		try {
			data.put("bizType", bizType);
			String password = decrpPassword(request, request.getParameter("password"));
			deleteMcrypt(request);
			PersonMember user = getUser(request);
			// 校验加盐支付密码
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(password);
			payPasswsReq.setValidateType(1);
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (!checkResult.isSuccess()) {
				int remainNum = checkResult.getRemainNum();
				data.put("remainNum", remainNum + "");
				if (checkResult.isLocked()) {
					data.put("error_passwd", "密码被锁定");
				} else {
					data.put("error_passwd", "密码错误");
				}
				restP.setData(data);
				if ("origlPhoneCan".equals(flag)) {// 更换绑定手机
					return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_canUse1", "response", restP);
				} else {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_amend", "response", restP);
				}
			}
			// 封装校验短信验证码请求
			String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
//			AuthCodeRequest req = new AuthCodeRequest();
//			req.setMemberId(user.getMemberId());
//			req.setMobile(mobile);
//			String ticket = defaultUesService.encryptData(mobile);
//			req.setMobileTicket(ticket);
//			req.setAuthCode(mobileCaptcha);
//			req.setBizId(user.getMemberId());
//			req.setBizType(bizType);
			UnionmaVerifyAuthCodeRequest req=new UnionmaVerifyAuthCodeRequest();
			req.setAuthCode(mobileCaptcha);
			req.setBizType(bizType);
			req.setMemberId(user.getMemberId());
			req.setVerifyValue(mobile);
			UnionmaBaseResponse result=bindFacadeService.verifyAuthCode(req,env);
			// 校验短信验证码
//			boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
			if (!result.getIsSuccess()) {
				logger.info("手机短信验证码验证失败！");
				data.put("mobileCaptcha_error", "验证码错误");
				restP.setData(data);
				if ("origlPhoneCan".equals(flag)) {// 更换绑定手机
					return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_canUse1", "response", restP);
				} else {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_amend", "response", restP);
				}
			}
			restP.setData(data);
			request.getSession().setAttribute(CommonConstant.RESET_MOBILE_TOKEN, CommonConstant.RESET_MOBILE_TOKEN);
			if ("origlPhoneCan".equals(flag)) {// 更换绑定手机
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_canUse2", "response", restP);
			} else {
				return new ModelAndView("redirect:/my/reset-set-mobilephone.htm?bizType=" + bizType);
			}
		} catch (Exception e) {
			logger.error("修改绑定手机号码错误：{}", e);
			e.printStackTrace();
			restP.setSuccess(false);
			restP.setMessage("修改绑定手机号码错误");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response", restP);
		}
	}

	/**
	 * 绑定新手机号码
	 * 
	 * @param request
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/reset-mobilephone.htm", method = RequestMethod.POST)
	public ModelAndView resetMobile(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String mobile = request.getParameter("mobile");
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		String bizType = request.getParameter("bizType");
		String changeMobile = request.getParameter("changeMobile");
		String flag = request.getParameter("flag");
		try {
			String token = (String) request.getSession().getAttribute(CommonConstant.RESET_MOBILE_TOKEN);
			if (!token.equals(CommonConstant.RESET_MOBILE_TOKEN)) {
				data.put("bizType", bizType);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_amend",
						"response", restP);
			}

			data.put("bizType", bizType);
			PersonMember user = getUser(request);
			
			UnionmaVerifyAuthCodeRequest codeReq=new UnionmaVerifyAuthCodeRequest();
			codeReq.setAuthCode(mobileCaptcha);
			codeReq.setBizType(bizType);
			codeReq.setMemberId(user.getMemberId());
			codeReq.setVerifyValue(mobile);
			UnionmaBaseResponse result=bindFacadeService.verifyAuthCode(codeReq,env);
			if (!result.getIsSuccess()) {
				data.put("mobileCaptcha_error", "验证码错误");
				data.put("mobile", mobile);
				restP.setData(data);
				if ("changeMobileJSP".equals(changeMobile)) {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/ssafety_phoneBind_canUse2", "response", restP);
				}else if("1".equals(flag)){
					return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse3",
							"response", restP);
				} 
				else {
					return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_amend2", "response", restP);
				}
			}
			UnionmaBindMobileEditRequest req=new UnionmaBindMobileEditRequest();
			req.setMemberId(user.getMemberId());
			req.setNewPhoneNumber(mobile);
			UnionmaBindMobileEditResponse resp=bindFacadeService.bindMobileEdit(req);
			if(resp.getIsSuccess()){
				restP.setSuccess(true);
				updateSessionObject(request);
				data.put("mobile", resp.getMobile());
			}else {
				restP.setMessage("绑定手机失败");
			}
			request.getSession().removeAttribute(CommonConstant.RESET_MOBILE_TOKEN);
			if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.RESETMOBILEPHONE.getMsg(), user, env));
			}
		} catch (Exception e) {
			logger.error("修改绑定手机错误：{}", e);
			if("1".equals(flag)){
				restP.setMessage("绑定手机失败");
				request.getSession().removeAttribute(CommonConstant.RESET_MOBILE_TOKEN);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse4", "response", restP);
			}else{
				restP.setMessage("修改绑定手机错误");
				request.getSession().removeAttribute(CommonConstant.RESET_MOBILE_TOKEN);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response", restP);
			}
		}
		if("1".equals(flag)){
			return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse4", "response", restP);
		} else{
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_amend3", "response", restP);
		}
		
	}

	/**
	 * 跳至绑定新手机号码页面
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/reset-set-mobilephone.htm")
	public ModelAndView resetSetMobilephone(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("bizType", request.getParameter("bizType"));
		// data.put("mobile", request.getParameter("mobile"));
		// data.put("mobileCaptcha_error",
		// request.getParameter("mobileCaptcha_error"));
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneBind_amend2", "response", restP);
	}

	/**
	 * 验证实名认证证书+支付密码(原手机不可用)
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/validLisenceNo.htm")
	public ModelAndView validLisenceNo(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String useType = request.getParameter("useType");
		restP.setData(data);
		try {
			String password = decrpPassword(request, request.getParameter("password"));
			deleteMcrypt(request);
			PersonMember user = getUser(request);
			// 校验加盐支付密码
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(password);
			payPasswsReq.setValidateType(1);
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);

			if (!checkResult.isSuccess()) {
				int remainNum = checkResult.getRemainNum();
				data.put("remainNum", remainNum + "");
				if (checkResult.isLocked()) {
					data.put("error_passwd", "密码被锁定");
				} else {
					data.put("error_passwd", "密码错误");
				}
				restP.setData(data);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse1", "response", restP);
			}
			
			String licenseNo = request.getParameter("busCode");
			String certNo = "";
			if (user.getMemberType() == MemberType.PERSONAL) {
				AuthInfoRequest authInfoReq = new AuthInfoRequest();
				authInfoReq.setMemberId(user.getMemberId());
				authInfoReq.setAuthType(AuthType.IDENTITY);
				authInfoReq.setOperator(user.getOperatorId());
				AuthInfo info = defaultCertService.queryRealName(authInfoReq);
				certNo = info.getAuthNo();
				if (StringUtils.isEmpty(certNo)) {
					authInfoReq.setMemberId(user.getMemberId());
					authInfoReq.setAuthType(AuthType.REAL_NAME);
					authInfoReq.setOperator(user.getOperatorId());
					info = defaultCertService.queryRealName(authInfoReq);
					certNo = info.getAuthNo();
				}
			}else{
				EnterpriseMember enterpriseMember = new EnterpriseMember();
				enterpriseMember.setMemberId(user.getMemberId());
				CompanyMemberInfo compInfo = memberService.queryCompanyInfo(enterpriseMember, env);
				certNo = compInfo.getLicenseNo();
			}

			if (!licenseNo.equals(certNo)) {
				logger.info("实名认证证件号验证失败！");
				data.put("licenseNo_error", "licenseNo_error");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse1", "response", restP);
			}
			request.getSession().setAttribute(CommonConstant.RESET_MOBILE_TOKEN, CommonConstant.RESET_MOBILE_TOKEN);
			return new ModelAndView("redirect:/my/resetMobileByEmail.htm?useType=" + request.getParameter("useType")+"&bizType=RESET_MOBILE_E");
		} catch (Exception e) {
			logger.error("解绑手机错误：{}", e);
			e.printStackTrace();
			restP.setSuccess(false);
			restP.setMessage("解绑手机错误");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response", restP);
		}
	}
}
