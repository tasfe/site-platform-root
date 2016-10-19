package com.netfinworks.site.web.action.securityCenter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.request.PersonalMemberQueryRequest;
import com.netfinworks.ma.service.response.PersonalMemberResponse;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.ITrustUtil;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.info.CertificationDetail;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.convert.MemberConvert;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;

@Controller
@RequestMapping("/hardcertActivate")
public class HardCertActivateAction extends BaseAction {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(name = "certificationService")
	private CertificationService certificationService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "memberService")
	private MemberService memberService;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "webResource")
	private WebDynamicResource webResource;

	@Resource(name = "memberFacade")
	private IMemberFacade memberFacade;

	@Resource(name = "memberService")
	private MemberService commMemberService;

	private static final String SOURCEPOSITION = "永达互联网金融信息服务有限公司";

	/**
	 * 根据用户证书的激活状态，判断跳转到已激活页面、还是未激活页面
	 * 
	 * @param req
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/showHardDetails.htm", method = RequestMethod.GET)
	public ModelAndView showHardDetails(HttpServletRequest req,
			OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		ModelAndView mv = initOcxView();
		// 获取用户信息
		PersonMember user = getUser(req);
		
		mv.addObject("mobile",user.getMobile());
		try {
			String memberId = user.getMemberId();
			String operatorId = user.getOperatorId();
			CertificationInfoRequest request = new CertificationInfoRequest();
			request.setMemberId(memberId);
			request.setOperatorId(operatorId);
			request.setCertificationType(CertificationType.HARD_CERTIFICATION
					.getCode());
			request.setCertificationStatus(CertificationStatus.ACTIVATED
					.getCode());
			// 判读用户证书是否激活
			restP = certificationService.getCertByCertStatus(request, env);// CertificationStatus.ACTIVATED.getCode()
			
			MemberType memberType = user.getMemberType();
			String realName = null;
		
			if("1".equals(memberType.getCode())) {//个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
				mv.addObject("company","身份证");
			} else if ("2".equals(memberType.getCode())) {//企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
				mv.addObject("company","法人身份证");
			}

			mv.addObject("realName",realName);
			mv.addObject("memberType",memberType.getCode());
			if (restP.isSuccess()) {
				List<String> certSns = (List<String>) restP.getData().get(
						"certSns");
				if (certSns != null && certSns.size() > 0) {
					mv.addObject("certSn", certSns.get(0));
					mv.setViewName(CommonConstant.URL_PREFIX
							+ "/securityCenter/hardcert_activate");
				} else {
					mv.setViewName(CommonConstant.URL_PREFIX
							+ "/securityCenter/hardcert_activateNo");
				}
			} else {
				mv.setViewName(CommonConstant.URL_PREFIX
						+ "/securityCenter/hardcert_activateNo");
			}
		} catch (Exception e) {
			logger.error("获取证书列表失败！", e);
		}
		return mv;
	}

	/**
	 * 获取证书列表
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCertSns.htm", method = RequestMethod.GET)
	public RestResponse getCertSns(HttpServletRequest request,
			OperationEnvironment env) {
		String type = request.getParameter("type");
		String status = request.getParameter("status");
		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		PersonMember user = this.getUser(request);
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getOperatorId());
		certRequest.setCertificationType(type);
		certRequest.setCertificationStatus(status);
		RestResponse result = null;
		try {
			result = certificationService.getCertByCertStatus(certRequest, env);
		} catch (BizException e) {
			logger.error("获取证书列表失败");
		}
		return result;
	}

	/**
	 * 跳转到身份验证页面
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/step1.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public ModelAndView activateCertStep1(HttpServletRequest req,
			OperationEnvironment env) {
		ModelAndView mv = new ModelAndView();
		PersonMember user = getUser(req);
		MemberType memberType = user.getMemberType();
		String mobile = user.getMobile();
		String memberId = user.getMemberId();
		String realName = null;
		try {
			if ("1".equals(memberType.getCode())) {// 个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
				mv.addObject("company", "身份证号");
			} else if ("2".equals(memberType.getCode())) {// 企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
				try {
					/*EnterpriseMember enterpriseMember = new EnterpriseMember();
					enterpriseMember.setMemberId(memberId);
					enterpriseMember = commMemberService.queryCompanyMember(
							enterpriseMember, env);
					CompanyMemberInfo compInfo = memberService
							.queryCompanyInfo(enterpriseMember, env);
					Integer companyType = compInfo.getCompanyType();
					if ("0".equals(companyType.toString())) {// 是企业
						mv.addObject("company", "企业营业执照号");
					} else {
						mv.addObject("company", "组织机构代码");
					}*/
					
					mv.addObject("company", "法人身份证号");
					
				} catch (Exception e) {
					logger.error("获取企业信息失败!");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error("获取真实姓名失败!");
			e.printStackTrace();
		}
		mv.addObject("mobile", mobile);
		mv.addObject("realName", realName);
		mv.setViewName(CommonConstant.URL_PREFIX
				+ "/securityCenter/hardcert_activate_step1");
		return mv;
	}

	/**
	 * 支付密码和营业执照号校验
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/step2.htm", method = RequestMethod.POST)
	public ModelAndView activateCertStep2(HttpServletRequest req,
			OperationEnvironment env) throws Exception {
		ModelAndView mv = new ModelAndView();

		String certSn = req.getParameter("certSn");
		// 先获取用户信息
		PersonMember user = this.getUser(req);
		String mobile = user.getMobile();
		mv.addObject("mobile", mobile);
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
				mv.addObject("company", "身份证号");
			} else if ("2".equals(memberType.getCode())) {// 企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
				/*
				EnterpriseMember enterpriseMember = new EnterpriseMember();
				enterpriseMember.setMemberId(memberId);
				enterpriseMember = commMemberService.queryCompanyMember(
						enterpriseMember, env);
				CompanyMemberInfo compInfo = memberService.queryCompanyInfo(
						enterpriseMember, env);
				Integer companyType = compInfo.getCompanyType();
				if ("0".equals(companyType.toString())) {// 是企业
					mv.addObject("company", "企业营业执照号");
				} else {
					mv.addObject("company", "组织机构代码");
				}
				*/
				mv.addObject("company", "法人身份证号");
			}
			mv.addObject("realName", realName);
		} catch (Exception e) {
			logger.error("获取真实姓名失败!");
			e.printStackTrace();
		}
		 
		if(req.getSession().getAttribute("mcrypt_key") == null){
			//mv.addObject("passwordmessage", "页面失效，请重新填写申请信息。");
			mv.setViewName(CommonConstant.URL_PREFIX
					+ "/securityCenter/hardcert_activate_step1");
			return mv;
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
			mv.addObject("certSn", certSn);
			if (pcheck == null) {
				logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
				mv.addObject("passwordmessage", "支付密码错误");
				mv.setViewName(CommonConstant.URL_PREFIX
						+ "/securityCenter/hardcert_activate_step1");
				return mv;
			}
			if (id_card == null || "".equals(id_card)) {
				logger.error("用户身份证号为空或未实名认证，用户ID[{}]", user.getMemberId());
				mv.addObject("buscodeMessage", "身份证号信息未实名认证！");
				mv.setViewName(CommonConstant.URL_PREFIX
						+ "/securityCenter/hardcert_activate_step1");
				return mv;
			}
			if (pcheck.isSuccess() && busCode.toLowerCase().equals(id_card.toLowerCase())) {
				mv.setViewName(CommonConstant.URL_PREFIX
						+ "/securityCenter/hardcert_activate_step3");
			} else {
				if (!pcheck.isSuccess()) {
					mv.addObject("success", false);
					if (pcheck.isLocked()) {
						logger.info("用户{}账户被锁定", user.getLoginName());
						mv.addObject("passwordmessage", "您的账户被锁定，请联系客服处理");
					} else {
						mv.addObject("passwordmessage", "您输入的支付密码不正确，请重新输入");
					}
					mv.addObject("remainNum", pcheck.getRemainNum());
				}
				if (!busCode.equals(id_card)) {
					mv.addObject("buscodeMessage", "您输入的身份证号不正确，请重新输入");
				}
				mv.setViewName(CommonConstant.URL_PREFIX
						+ "/securityCenter/hardcert_activate_step1");
				return mv;
			}
		} catch (ServiceException e) {
			mv.addObject("passwordmessage", "支付密码校验异常");
			logger.error("支付密码校验异常", e);
			mv.setViewName(CommonConstant.URL_PREFIX
					+ "/securityCenter/hardcert_activate_step1");
			return mv;
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
		
		if(!verifyResult){
			mv.setViewName(CommonConstant.URL_PREFIX
					+ "/securityCenter/hardcert_activate_step1");
		}

		return mv;
	}

	/**
	 * 验证用户输入的手机验证码是否正确
	 * 
	 * @param authCode
	 *            用户输入的手机验证码
	 * @param req
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws ServiceException
	 */
	@ResponseBody
	@RequestMapping(value = "/checkAuthCode.htm", method = RequestMethod.POST)
	public RestResponse checkAuthCode(HttpServletRequest req,
			OperationEnvironment env) throws BizException, ServiceException {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		ModelAndView mv = new ModelAndView();
		// 获取用户输入的手机验证码
		String authCode = req.getParameter("authCode");
		// 获取用户信息
		PersonMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getOperatorId();
		EncryptData edata = memberService.decipherMember(memberId,
				DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
		String mobile = edata.getPlaintext();
		logger.info("mobile:{},authCode:{}", mobile, authCode);
		// 封装校验短信验证码请求
		AuthCodeRequest anthCodereq = new AuthCodeRequest();
		anthCodereq.setMemberId(memberId);
		anthCodereq.setMobile(mobile);
		String ticket = defaultUesService.encryptData(mobile);
		anthCodereq.setMobileTicket(ticket);
		anthCodereq.setAuthCode(authCode);
		anthCodereq.setBizId(memberId);
		anthCodereq.setBizType(BizType.KJTSHILED_ACTIVE.getCode());

		// 校验短信验证码
		boolean verifyResult = defaultSmsService.verifyMobileAuthCode(
				anthCodereq, env);

		if (verifyResult) {// 如果验证码正确，到第三步下载激活证书
			restP.setSuccess(true);
		} else {
			restP.setMessage("校验码验证失败！");
			restP.setSuccess(false);
		}
		return restP;
	}
	
	/*
	@RequestMapping(value = "/step3.htm", method = RequestMethod.POST)
	public ModelAndView tostep3(HttpServletRequest req, OperationEnvironment env)
			throws BizException {
		ModelAndView mv = new ModelAndView();
		mv.setViewName(CommonConstant.URL_PREFIX
				+ "/securityCenter/hardcert_activate_step3");
		return mv;
	}
	*/

	/**
	 * 激活快捷盾
	 * 
	 * @param req
	 * @param env
	 * @throws BizException
	 */
	@ResponseBody
	@RequestMapping(value = "/certApply.htm", method = RequestMethod.POST)
	public RestResponse downloadCert(HttpServletRequest req,
			OperationEnvironment env) throws BizException {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String accountHash = null;
		String csr = req.getParameter("csr");
		String serialNo = req.getParameter("serialNo");//TODO 获取到的快捷盾编号
		//如果快捷盾编号超出范围，拒绝申请
        if(!ITrustUtil.isInRange(serialNo)){
            logger.error("申请硬证书失败:快捷盾编号不在范围内！");;
            restP.setMessage("申请证书失败");
            restP.setSuccess(false);
            return restP;
        }
		// 如果产生csr异常，拒绝申请
		if (csr == null || "".equals(csr)) {
			logger.error("申请硬证书失败:产生csr异常！");
			restP.setMessage("申请证书失败");
			restP.setSuccess(false);
			return restP;
		}
		PersonMember user = getUser(req);
		CertificationInfoRequest request = new CertificationInfoRequest();
		String memberId = user.getMemberId();
		String operatorId = user.getOperatorId();

		MemberType memberType = user.getMemberType();
		if ("1".equals(memberType.getCode())) {// 个人会员
			accountHash = webResource.getPersonalPAccountHash();
		} else if ("2".equals(memberType.getCode())) {// 企业会员
			accountHash = webResource.getPersonalEAccountHash();
		}

		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCsr(csr);
		request.setUserIp(env.getClientIp());
		request.setUserMac(env.getClientMac());
		request.setRequestTime(new Date());
		request.setMobileNo(user.getMobile());
		request.setCertificationType(CertificationType.HARD_CERTIFICATION
				.getCode());
		request.setAccountHash(accountHash);
		request.setSerialNo(serialNo);
		
		
		RestResponse result = certificationService.certApply(request, env);
		if (result.isSuccess()) {
			String certSerialNumber = (String) result.getData().get(
					"certSerialNumber"); // 证书序列号
			String certSignBufP7 = (String) result.getData().get(
					"certSignBufP7"); // 写入u盾的证书内容
			data.put("certSerialNumber", certSerialNumber);
			data.put("certSignBufP7", certSignBufP7);
			restP.setSuccess(true);
			restP.setData(data);
		} else {
			logger.error("申请证书失败：" + result.getMessage());
			restP.setMessage(result.getMessage());
			restP.setSuccess(false);
		}
		return restP;
	}

	/**
	 * 申请证书成功后，激活证书,并取消软证书
	 * 
	 * @param req
	 * @param env
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/certActive.htm", method = RequestMethod.POST)
	public ModelAndView certActive(HttpServletRequest req,
			OperationEnvironment env) throws BizException {
		ModelAndView mv = new ModelAndView();
		String certSn = req.getParameter("certSn");
		// 获取
		PersonMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		RestResponse result = certificationService.certActivate(request, env);
		if (result.isSuccess()) {
			// 取消软证书
			CertificationInfoRequest request1 = new CertificationInfoRequest();
			request1.setMemberId(memberId);
			request1.setOperatorId(operatorId);
			request1.setCertificationType(CertificationType.SOFT_CERTIFICATION
					.getCode());
			RestResponse cancelResult = null;
			try {
				cancelResult = certificationService.certUnbind(request1, env);
				if (cancelResult.isSuccess()) {
					mv.setViewName(CommonConstant.URL_PREFIX
							+ "/securityCenter/hardcert_activate_step4");
				} else {
					logger.error("激活快捷盾时取消数字软证书失败！");
					mv.addObject("message", "激活快捷盾失败");
					mv.setViewName(CommonConstant.URL_PREFIX
							+ "/business-error");
				}
			} catch (BizException e) {
				logger.error("激活快捷盾时取消数字软证书失败: " + e.getMessage(), e.getCause());
				mv.addObject("message", "激活快捷盾失败");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			}
		} else {
			logger.error("激活快捷盾失败！");
			mv.addObject("message", "激活快捷盾失败");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
		}
		return mv;
	}

	/**
	 * 查看证书详情
	 * 
	 * @param req
	 * @param resp
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws Exception
	 */
	@RequestMapping(value = "/showHardCertDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView showCertDetail(HttpServletRequest req,
			HttpServletResponse resp, OperationEnvironment env)
			throws BizException, Exception {
		ModelAndView mv = new ModelAndView();

		String certSn = req.getParameter("certSn");
		CertificationInfoRequest request = new CertificationInfoRequest();
		CertificationDetail certificationDetail = null;
		// 获取用户信息
		PersonMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getOperatorId();
		/*
		PersonalMemberQueryRequest personalMemberQueryRequest = MemberConvert
				.createPersonalMemberQueryRequest(memberId);
		PersonalMemberResponse personalMemberResponse = memberFacade
				.queryPersonalMember(env, personalMemberQueryRequest)
		String realName = personalMemberResponse.getPersonalMemberInfo()
				.getRealName();
		*/
		String realName = null;
		MemberType memberType = user.getMemberType();
		try {
			if("1".equals(memberType.getCode())) {//个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
			} else if ("2".equals(memberType.getCode())) {//企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
			}
		} catch (Exception e) {
			logger.error("硬书激活第一步获取真实姓名失败： " + e.getMessage(), e.getCause());
		}
		
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		certificationDetail = certificationService.certDetail(request, env);
		Map<String, String> map = JsonMapUtil.jsonToMapStr(certificationDetail
				.getCertData());
		String mid = certificationDetail.getMemberId();
		String certIssuerDn = map.get("certIssuerDn");
		String date = map.get("certApproveDate");
		DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		dateFormat.setLenient(false);
		String dateStr = dateFormat.format(DateUtil.parseDateLongFormat(date));
		String certSerialNumber = map.get("certSerialNumber");
		mv.addObject("sourcePosition", SOURCEPOSITION);
		mv.addObject("mid", mid);
		mv.addObject("realName", realName);
		mv.addObject("certIssuerDn", certIssuerDn);
		mv.addObject("certSerialNumber", certSerialNumber);
		mv.addObject("dateStr", dateStr);
		mv.setViewName(CommonConstant.URL_PREFIX
				+ "/securityCenter/hardcert_details");

		return mv;
	}
	
	
    
}
