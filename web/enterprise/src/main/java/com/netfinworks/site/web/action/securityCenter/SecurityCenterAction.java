package com.netfinworks.site.web.action.securityCenter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;

@Controller
@RequestMapping("/securityCenter")
public class SecurityCenterAction extends BaseAction {
	
	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;
	
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	
	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;
	
	@Resource(name = "webResource")
    private WebDynamicResource        webResource;
	
	@RequestMapping(value = "/safetyIndex.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView safetyIndex(HttpServletRequest request) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		EnterpriseMember user = getUser(request);
		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(user.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				data.put("verify_mobile", authVerifyInfo.getVerifyEntity());
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				String email = authVerifyInfo.getVerifyEntity();
            	int index = email.indexOf("@");
    			data.put("verify_email", StarUtil.hideStrBySym(email, 1, email.length()-index, 3));
			}
		}
		data.put("verify_name", user.getNameVerifyStatus().getCode());
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/securityCenter/safetyIndex", "response", restP);

	}
	
	/**
	 * 根据用户证书的激活状态，判断跳转到已激活页面、还是未激活页面
	 * @param req
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/safetyDetails.htm", method = RequestMethod.GET)
	public ModelAndView safetyDetails(HttpServletRequest req, OperationEnvironment env){
		RestResponse restP = new RestResponse();
		ModelAndView mv = new ModelAndView();
		//获取用户信息
		EnterpriseMember user = getUser(req);
		try {
			String memberId = user.getMemberId();
			String operatorId = user.getCurrentOperatorId();
			String mobile = user.getMobile();
			String enterpriseName = user.getEnterpriseName();
			CertificationInfoRequest request = new CertificationInfoRequest();
			request.setMemberId(memberId);
			request.setOperatorId(operatorId);
			request.setCertificationType(CertificationType.HARD_CERTIFICATION.getCode());
			request.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
			//判读用户证书是否激活
			restP = certificationService.getCertByCertStatus(request, env);//CertificationStatus.ACTIVATED.getCode()
			if(restP.isSuccess()) {
				List<String> certSns = (List<String>) restP.getData().get("certSns");
				if(certSns != null && certSns.size() > 0) {
					mv.addObject("certSn", certSns.get(0));
					mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/safety_usbKey_activate");
				} else {
					mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/safety_usbKey_activateNo");
				}
			} else {
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/safety_usbKey_activateNo");
			}
		} catch(Exception e) {
			logger.error("获取证书列表失败！", e);
		}
		return mv;
	}
	
	/**
	 * 申请网关证书
	 * @param req
	 * @param env
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/applyGatewayCert.htm", method = RequestMethod.POST)
	public RestResponse applyGatewayCert(HttpServletRequest req, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		//获取当前用户
		EnterpriseMember user = getUser(req);
		try {
			String password = req.getParameter("password");
			// 校验支付密码
			String payPassword = decrpPassword(req, password);
			deleteMcrypt(req);
			// 校验加密支付密码
			PayPasswdRequest payPasswsReq = new PayPasswdRequest();
			payPasswsReq.setOperator(user.getCurrentOperatorId());
			payPasswsReq.setAccountId(user.getDefaultAccountId());
			payPasswsReq.setOldPassword(payPassword);
			payPasswsReq.setValidateType(1);
			// 检查支付密码
			PayPasswdCheck pcheck = null;
			try{
				pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
				if (pcheck == null) {
					logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
					restP.setSuccess(false);
					restP.setMessage("支付密码校验异常！");
					return restP;
				}
				if (pcheck.isSuccess()) {
					String csr = req.getParameter("csr");
					//如果产生csr异常，拒绝申请
					if(csr == null || "".equals(csr)) {
						logger.error("申请网关证书失败:产生csr异常！");;
						restP.setMessage("申请网关证书失败！");
						restP.setSuccess(false);
						return restP;
					}
					String memberId = user.getMemberId();
					String operatorId = user.getCurrentOperatorId();
					//测试环境accountHash：A14C4EADE161828C018634E51C6B6740
					String accountHash = webResource.getGatewayAccountHash();
					CertificationInfoRequest request = new CertificationInfoRequest();
					request.setMemberId(memberId);
					request.setOperatorId(operatorId);
					request.setCsr(csr);
					request.setRequestTime(new Date());
					request.setCertificationType(CertificationType.GATEWAY_CERTIFICATION.getCode());
					request.setAccountHash(accountHash);
					restP = certificationService.certApply(request, env);
					if(restP.isSuccess()) {
						restP.setMessage("申请网关证书成功,可以从浏览器导出网关证书！");;
					} else {
						restP.setMessage("申请网关证书失败！");
					}
				} else {
					restP.setSuccess(false);
					if (!pcheck.isSuccess()) {
						if (pcheck.isLocked()) {
		                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
		                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
							logger.info("用户{}账户被锁定", user.getLoginName());
							restP.setMessage("您的账户被锁定，请联系客服处理");
						} else {
							restP.setMessage("您输入的支付密码不正确，请重新输入！");
						}
					}
					return restP;
				}
			} catch (ServiceException e) {
				logger.error("申请网关证书失败: " + e.getMessage(), e.getCause());
				restP.setSuccess(false);
				restP.setMessage("申请网关证书失败！");
				return restP;
			}
		} catch (BizException e) {
			logger.error("申请网关证书失败: " + e.getMessage(), e.getCause());
			restP.setSuccess(false);
			restP.setMessage("申请网关证书失败！");
			return restP;
		}
		return restP;
	}
	
	@RequestMapping(value = "/error.htm", method = RequestMethod.POST)
	public ModelAndView errorPage(HttpServletRequest req) {
		ModelAndView mv = new ModelAndView();
		String errMessage = req.getParameter("errMessage");
		if("1".equals(errMessage)){
		    mv.addObject("message", "下载证书不能超过5个！");
		}else{
		    mv.addObject("message", "下载证书失败！");
		}
		mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
		return mv;
		
	}
}
