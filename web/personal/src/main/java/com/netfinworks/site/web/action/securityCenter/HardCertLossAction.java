package com.netfinworks.site.web.action.securityCenter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.action.common.BaseAction;

@Controller
@RequestMapping("/hardcertActivate")
public class HardCertLossAction extends BaseAction {

	@Resource(name = "memberService")
	private MemberService memberService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;
	
	@Resource(name = "certificationService")
	private CertificationService certificationService;

	/*
	 * 盾挂失
	 * 
	 * @param req
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/hardCertLoss.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse hardCertLoss(HttpServletRequest req,
			OperationEnvironment env) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();

		RestResponse restP = new RestResponse();
		String certSn = req.getParameter("certSn");
		// 先获取用户信息
		PersonMember user = this.getUser(req);
		String mobile = user.getMobile();

		String password = req.getParameter("password");
		String busCode = req.getParameter("idcard");
		MemberType memberType = user.getMemberType();
		String memberId = user.getMemberId();
		String realName = null;
		// 获取用户输入的手机验证码
		String authCode = req.getParameter("authCode");

		try {
			if ("1".equals(memberType.getCode())) {// 个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
			} else if ("2".equals(memberType.getCode())) {// 企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
			}

		} catch (Exception e) {
			logger.error("获取真实姓名失败!");
			e.printStackTrace();
		}
		// 校验支付密码
		String payPassword = decrpPassword(req, password);
		deleteMcrypt(req);
		// 校验加密支付密码
		PayPasswdRequest payPasswsReq = new PayPasswdRequest();
		payPasswsReq.setOperator(user.getOperatorId());
		payPasswsReq.setAccountId(user.getDefaultAccountId());
		payPasswsReq.setOldPassword(payPassword);
		payPasswsReq.setValidateType(1);
		// 检查支付密码
		PayPasswdCheck pcheck = null;
		String id_card = null;
		try {
			pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			// 获取身份证号
			id_card = getEncryptInfo(req, DeciphedType.ID_CARD, env);
			data.put("certSn", certSn);
			if (pcheck == null) {
				logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
				restP.setMessage("支付密码校验异常");
				return restP;
			}
			if (id_card == null || "".equals(id_card)) {
				logger.error("用户身份证号为空或未实名认证，用户ID[{}]", user.getMemberId());
				restP.setMessage("身份证号信息未实名认证");
				return restP;
			}
			if (pcheck.isSuccess() && busCode.toLowerCase().equals(id_card.toLowerCase())) {
				
			} else {
				if (!pcheck.isSuccess()) {

					if (pcheck.isLocked()) {
						logger.info("用户{}账户被锁定", user.getLoginName());
						restP.setMessage("您的账户被锁定，请联系客服处理");
					} else {
						restP.setMessage("您输入的支付密码不正确，请重新输入");
					}
				}
				if (!busCode.equals(id_card)) {
					restP.setMessage("您输入的身份证号不正确，请重新输入");
				}

				return restP;
			}
		} catch (ServiceException e) {
			logger.error("支付密码校验异常", e);
			restP.setMessage("支付密码校验异常");
			return restP;
		}
		
		EncryptData edata = memberService.decipherMember(memberId,
				DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
		String mobiledata = edata.getPlaintext();
		logger.info("mobile:{},authCode:{}", mobiledata, authCode);
		// 封装校验短信验证码请求
		AuthCodeRequest anthCodereq = new AuthCodeRequest();
		anthCodereq.setMemberId(memberId);
		anthCodereq.setMobile(mobiledata);
		String ticket = defaultUesService.encryptData(mobiledata);
		anthCodereq.setMobileTicket(ticket);
		anthCodereq.setAuthCode(authCode);
		anthCodereq.setBizId(memberId);
		anthCodereq.setBizType(BizType.KJTSHILED_ACTIVE.getCode());

		// 校验短信验证码
		boolean verifyResult = defaultSmsService.verifyMobileAuthCode(
				anthCodereq, env);

		if (!verifyResult) {

			restP.setMessage("短信校验码错误");
			return restP;
		}

	
		String operatorId = user.getOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		
	    restP = certificationService.certReportLoss(request, env);
	    if(!restP.isSuccess()){
	    	restP.setMessage("挂失失败，您已挂失或盾已失效");
	    }
		return restP;
	}
	
	/*
	 * 盾解挂
	 * 
	 * @param req
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/hardCertRlvLoss.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse hardCertRelieveLoss(HttpServletRequest req,
			OperationEnvironment env) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();

		RestResponse restP = new RestResponse();
		String certSn = req.getParameter("certSn");
		// 先获取用户信息
		PersonMember user = this.getUser(req);
		String mobile = user.getMobile();

		String password = req.getParameter("password");
		String busCode = req.getParameter("idcard");
		MemberType memberType = user.getMemberType();
		String memberId = user.getMemberId();
		String realName = null;
		// 获取用户输入的手机验证码
		String authCode = req.getParameter("authCode");

		try {
			if ("1".equals(memberType.getCode())) {// 个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
			} else if ("2".equals(memberType.getCode())) {// 企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
			}

		} catch (Exception e) {
			logger.error("获取真实姓名失败!");
			e.printStackTrace();
		}
		// 校验支付密码
		String payPassword = decrpPassword(req, password);
		deleteMcrypt(req);
		// 校验加密支付密码
		PayPasswdRequest payPasswsReq = new PayPasswdRequest();
		payPasswsReq.setOperator(user.getOperatorId());
		payPasswsReq.setAccountId(user.getDefaultAccountId());
		payPasswsReq.setOldPassword(payPassword);
		payPasswsReq.setValidateType(1);
		// 检查支付密码
		PayPasswdCheck pcheck = null;
		String id_card = null;
		try {
			pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			// 获取身份证号
			id_card = getEncryptInfo(req, DeciphedType.ID_CARD, env);
			data.put("certSn", certSn);
			if (pcheck == null) {
				logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
				restP.setMessage("支付密码校验异常");
				return restP;
			}
			if (id_card == null || "".equals(id_card)) {
				logger.error("用户身份证号为空或未实名认证，用户ID[{}]", user.getMemberId());
				restP.setMessage("身份证号信息未实名认证");
				return restP;
			}
			if (pcheck.isSuccess() && busCode.toLowerCase().equals(id_card.toLowerCase())) {
				
			} else {
				if (!pcheck.isSuccess()) {

					if (pcheck.isLocked()) {
						logger.info("用户{}账户被锁定", user.getLoginName());
						restP.setMessage("您的账户被锁定，请联系客服处理");
					} else {
						restP.setMessage("您输入的支付密码不正确，请重新输入");
					}
				}
				if (!busCode.equals(id_card)) {
					restP.setMessage("您输入的身份证号不正确，请重新输入");
				}

				return restP;
			}
		} catch (ServiceException e) {
			logger.error("支付密码校验异常", e);
			restP.setMessage("支付密码校验异常");
			return restP;
		}
		
		EncryptData edata = memberService.decipherMember(memberId,
				DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
		String mobiledata = edata.getPlaintext();
		logger.info("mobile:{},authCode:{}", mobiledata, authCode);
		// 封装校验短信验证码请求
		AuthCodeRequest anthCodereq = new AuthCodeRequest();
		anthCodereq.setMemberId(memberId);
		anthCodereq.setMobile(mobiledata);
		String ticket = defaultUesService.encryptData(mobiledata);
		anthCodereq.setMobileTicket(ticket);
		anthCodereq.setAuthCode(authCode);
		anthCodereq.setBizId(memberId);
		anthCodereq.setBizType(BizType.KJTSHILED_ACTIVE.getCode());

		// 校验短信验证码
		boolean verifyResult = defaultSmsService.verifyMobileAuthCode(
				anthCodereq, env);

		if (!verifyResult) {

			restP.setMessage("短信校验码错误");
			return restP;
		}

	
		String operatorId = user.getOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		
	    restP = certificationService.certRelieveLoss(request, env);
	    if(!restP.isSuccess()){
	    	restP.setMessage("解挂失败，您已解挂或盾已失效");
	    }
		return restP;
	}
	
	
	/*
	 * 盾注销
	 * 
	 * @param req
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/hardCertRevoke.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse hardCertRevoke(HttpServletRequest req,
			OperationEnvironment env) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();

		RestResponse restP = new RestResponse();
		String certSn = req.getParameter("certSn");
		// 先获取用户信息
		PersonMember user = this.getUser(req);
		String mobile = user.getMobile();

		String password = req.getParameter("password");
		String busCode = req.getParameter("idcard");
		MemberType memberType = user.getMemberType();
		String memberId = user.getMemberId();
		String realName = null;
		// 获取用户输入的手机验证码
		String authCode = req.getParameter("authCode");

		try {
			if ("1".equals(memberType.getCode())) {// 个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
			} else if ("2".equals(memberType.getCode())) {// 企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
			}

		} catch (Exception e) {
			logger.error("获取真实姓名失败!");
			e.printStackTrace();
		}
		// 校验支付密码
		String payPassword = decrpPassword(req, password);
		deleteMcrypt(req);
		// 校验加密支付密码
		PayPasswdRequest payPasswsReq = new PayPasswdRequest();
		payPasswsReq.setOperator(user.getOperatorId());
		payPasswsReq.setAccountId(user.getDefaultAccountId());
		payPasswsReq.setOldPassword(payPassword);
		payPasswsReq.setValidateType(1);
		// 检查支付密码
		PayPasswdCheck pcheck = null;
		String id_card = null;
		try {
			pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			// 获取身份证号
			id_card = getEncryptInfo(req, DeciphedType.ID_CARD, env);
			data.put("certSn", certSn);
			if (pcheck == null) {
				logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
				restP.setMessage("支付密码校验异常");
				return restP;
			}
			if (id_card == null || "".equals(id_card)) {
				logger.error("用户身份证号为空或未实名认证，用户ID[{}]", user.getMemberId());
				restP.setMessage("身份证号信息未实名认证");
				return restP;
			}
			if (pcheck.isSuccess() && busCode.toLowerCase().equals(id_card.toLowerCase())) {
				
			} else {
				if (!pcheck.isSuccess()) {

					if (pcheck.isLocked()) {
						logger.info("用户{}账户被锁定", user.getLoginName());
						restP.setMessage("您的账户被锁定，请联系客服处理");
					} else {
						restP.setMessage("您输入的支付密码不正确，请重新输入");
					}
				}
				if (!busCode.equals(id_card)) {
					restP.setMessage("您输入的身份证号不正确，请重新输入");
				}

				return restP;
			}
		} catch (ServiceException e) {
			logger.error("支付密码校验异常", e);
			restP.setMessage("支付密码校验异常");
			return restP;
		}
		
		EncryptData edata = memberService.decipherMember(memberId,
				DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
		String mobiledata = edata.getPlaintext();
		logger.info("mobile:{},authCode:{}", mobiledata, authCode);
		// 封装校验短信验证码请求
		AuthCodeRequest anthCodereq = new AuthCodeRequest();
		anthCodereq.setMemberId(memberId);
		anthCodereq.setMobile(mobiledata);
		String ticket = defaultUesService.encryptData(mobiledata);
		anthCodereq.setMobileTicket(ticket);
		anthCodereq.setAuthCode(authCode);
		anthCodereq.setBizId(memberId);
		anthCodereq.setBizType(BizType.KJTSHILED_ACTIVE.getCode());

		// 校验短信验证码
		boolean verifyResult = defaultSmsService.verifyMobileAuthCode(
				anthCodereq, env);

		if (!verifyResult) {

			restP.setMessage("短信校验码错误");
			return restP;
		}

	
		String operatorId = user.getOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		request.setReason("会员主动注销");
	    restP = certificationService.certRevoke(request, env);
	    if(!restP.isSuccess()){
	    	restP.setMessage("注销失败，您已注销或盾已失效");
	    }
		return restP;
	}
	
	
	
}
