package com.netfinworks.site.web.action.securityCenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.Operator;
import com.netfinworks.ma.service.facade.IOperatorFacade;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.ma.service.response.OperatorInfoResponse;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.ITrustUtil;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;
/**
 * 数字证书申请激活的Action：
 * step1：验证身份（营业执照和支付密码校验）
 * step2：获取手机校验码、校验手机校验码
 * step3：下载数字证书、将数字证书写入快捷盾
 * @author chengwen
 *
 */

@Controller
@RequestMapping("/certActive")
public class CertificationActiveAction extends BaseAction {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String O = "永达互联网金融";
	
	private static final String OU = "RA";
	
	@Resource(name = "memberService")
    private MemberService memberService;
	
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	
	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;
	
	@Resource(name = "defaultSmsService")
    private DefaultSmsService	defaultSmsService;
	
	@Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
	
	@Resource(name = "webResource")
    private WebDynamicResource        webResource;
	
	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Resource(name = "operatorFacade")
    private IOperatorFacade operatorFacade;
	
	/**
	 * 跳转到身份验证页面
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/step1.htm", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView activateCertStep1(HttpServletRequest req) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		ModelAndView mv = new ModelAndView();
		mv.addObject("response", restP);
		EnterpriseMember user = getUser(req);

		if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
			restP.setMessage("实名认证后才可以激活快捷盾！");
			mv.addObject("response", restP);
			mv.setViewName(CommonConstant.URL_PREFIX + "/error");
			return mv;
		}

		String mobile = user.getMobile();
		String enterpriseName = user.getEnterpriseName();
		CompanyMemberInfo compInfo = null;
		try {
			compInfo = defaultMemberService.queryCompanyInfo(user, env);
			Integer companyType = compInfo.getCompanyType();
			if("0".equals(companyType.toString())) {//是企业
				mv.addObject("company", "营业执照注册号");
			} else {
				mv.addObject("company", "组织机构代码证号");
			}
		} catch (ServiceException e) {
			logger.info("获取企业信息失败");
            logger.error("获取企业信息失败", e);
		}
		mv.addObject("mobile", mobile);
		mv.addObject("enterpriseName", enterpriseName);
		mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_active_step1");
		return mv;
	}
	
	/**
	 * 支付密码和营业执照号校验
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/step2.htm", method = RequestMethod.POST)
	public ModelAndView activateCertStep2(HttpServletRequest req, OperationEnvironment env) throws Exception {
		String certSn = req.getParameter("certSn");
		ModelAndView mv = new ModelAndView();
		// 先获取用户信息
		EnterpriseMember user = this.getUser(req);
		String mobile = user.getMobile();
		String enterpriseName = user.getEnterpriseName();
		String licenseNo = user.getLicenseNo();
		mv.addObject("mobile", mobile);
		mv.addObject("enterpriseName", enterpriseName);
		String password = req.getParameter("password");
		String busCode = req.getParameter("busCode");
		CompanyMemberInfo compInfo = null;
		try {
			compInfo = defaultMemberService.queryCompanyInfo(user, env);
			Integer companyType = compInfo.getCompanyType();
			if("0".equals(companyType.toString())) {//是企业
				mv.addObject("company", "企业营业执照号");
			} else {
				mv.addObject("company", "组织机构代码");
			}
		} catch (ServiceException e) {
			logger.info("获取企业信息失败");
            logger.error("获取企业信息失败", e);
		}
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
		try {
			mv.addObject("certSn", certSn);
			pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (pcheck == null) {
				logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_active_step1");
				return mv;
			}
			if (pcheck.isSuccess() && busCode.equals(licenseNo)) {
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_active_step2");
			} else {
				if((licenseNo == null) || "".equals(licenseNo)) {
					mv.addObject("licenseNoMessage", "没有注册营业执照号");
				} else {
					if(!busCode.equals(licenseNo)) {//user.getLicenseNo()
						logger.error("营业执照号验证失败，用户ID[{}]", user.getMemberId());
						mv.addObject("licenseNoMessage", "请输入正确的营业执照号码！");
					} else {
						mv.addObject("licenseNoMessage", "");
					}
				}
				if (!pcheck.isSuccess()) {
					mv.addObject("success", false);
					if (pcheck.isLocked()) {
	                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
	                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
						logger.info("用户{}账户被锁定", user.getLoginName());
						mv.addObject("payPasswordmessage", "您的账户被锁定，请联系客服处理");
					} else {
						mv.addObject("payPasswordmessage", "您输入的支付密码不正确，还有" + pcheck.getRemainNum() + "次机会！");
					}
					mv.addObject("remainNum", pcheck.getRemainNum());
				}else {
					mv.addObject("payPasswordmessage", "");
				}
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_active_step1");
				return mv;
			}
		} catch (ServiceException e) {
			logger.error("支付密码校验异常", e);
			mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_active_step1");
			return mv;
		}
		return mv;
	}
	
	/**
	 * 验证用户输入的手机验证码是否正确
	 * @param authCode		用户输入的手机验证码
	 * @param req
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws ServiceException
	 */
	@ResponseBody
	@RequestMapping(value = "/checkAuthCode.htm", method = RequestMethod.POST)
	public RestResponse checkAuthCode(HttpServletRequest req, OperationEnvironment env) throws BizException, ServiceException {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		ModelAndView mv = new ModelAndView();
		//获取用户输入的手机验证码
		String authCode = req.getParameter("authCode");
		//获取用户信息
	    EnterpriseMember user = getUser(req);
	    String memberId = user.getMemberId();
	    String operatorId = user.getCurrentOperatorId();
	    EncryptData  edata = memberService.decipherMember(memberId,
	            DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
	    String mobile = edata.getPlaintext();
        logger.info("mobile:{},authCode:{}", mobile, authCode);
        //封装校验短信验证码请求
        AuthCodeRequest anthCodereq = new AuthCodeRequest();
        anthCodereq.setMemberId(memberId);
        anthCodereq.setMobile(mobile);
        String ticket = defaultUesService.encryptData(mobile);
        anthCodereq.setMobileTicket(ticket);
        anthCodereq.setAuthCode(authCode);
        anthCodereq.setBizId(memberId);
        anthCodereq.setBizType(BizType.KJTSHILED_ACTIVE.getCode());
        
        //校验短信验证码
        boolean verifyResult = defaultSmsService.verifyMobileAuthCode(anthCodereq, env);
        
        if (verifyResult) {//如果验证码正确，到第三步下载激活证书
        	restP.setSuccess(true);
        } else {
        	restP.setMessage("校验码验证失败！");
        	restP.setSuccess(false);
        }
        return restP;
	}
		
	@RequestMapping(value = "/step3.htm", method = RequestMethod.POST)
	public ModelAndView tostep3(HttpServletRequest req, OperationEnvironment env) throws BizException {
		ModelAndView mv = new ModelAndView();
		mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_active_step3");
		return mv;
	}
	/**
	 * 申请数字证书
	 * @param req
	 * @param env
	 * @throws BizException
	 */
	@ResponseBody
	@RequestMapping(value = "/certApply.htm", method = RequestMethod.POST)
	public RestResponse downloadCert(HttpServletRequest req, OperationEnvironment env) throws BizException {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		//获取账号hash 测试环境：A14C4EADE161828C018634E51C6B6740
		//String accountHash = "2AA59FEB6DCEBECA3D6F5E6D78B3910E";//MD5Util.MD5Encrpytion(O + OU); 
		String accountHash = webResource.getEnterpriseAccountHash();
		String csr = req.getParameter("csr");
		String serialNo = req.getParameter("serialNo");//TODO 获取到的快捷盾编号
		//如果快捷盾编号超出范围，拒绝申请
		if(!ITrustUtil.isInRange(serialNo)){
		    logger.error("申请硬证书失败:快捷盾编号不在范围内！");;
            restP.setMessage("申请证书失败");
            restP.setSuccess(false);
            return restP;
        }
		//如果产生csr异常，拒绝申请
		if((csr == null) || "".equals(csr)) {
			logger.error("申请硬证书失败:产生csr异常！");;
			restP.setMessage("申请证书失败");
			restP.setSuccess(false);
			return restP;
		}
		EnterpriseMember user = getUser(req);
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(user.getMemberId());
		request.setOperatorId(user.getCurrentOperatorId());
		request.setCsr(csr);
		request.setUserIp(env.getClientIp());
		request.setUserMac(env.getClientMac());
		request.setRequestTime(new Date());
		request.setMobileNo(user.getMobile());
		request.setCertificationType(CertificationType.HARD_CERTIFICATION.getCode());
		request.setAccountHash(accountHash);
		request.setSerialNo(serialNo);
		RestResponse result = certificationService.certApply(request, env);
		if(result.isSuccess()) {
			String certSerialNumber = (String) result.getData().get("certSerialNumber");			//证书序列号
			String certSignBufP7 = (String) result.getData().get("certSignBufP7");					//写入u盾的证书内容
			data.put("certSerialNumber", certSerialNumber);
			data.put("certSignBufP7", certSignBufP7);
			restP.setSuccess(true);
			restP.setData(data);
		} else {
			logger.error("申请硬证书失败:" + result.getMessage());;
			restP.setMessage("申请证书失败");
			restP.setSuccess(false);
		}
		return restP;
	}
	
	/**
	 * 申请证书成功后，激活证书
	 * @param req
	 * @param env
	 * @return
	 * @throws BizException 
	 */
	@RequestMapping(value = "/certActive.htm", method = RequestMethod.POST)
	public ModelAndView certActive(HttpServletRequest req, OperationEnvironment env) throws BizException {
		ModelAndView mv = new ModelAndView();
		String certSn = req.getParameter("certSn");
		//获取
		EnterpriseMember user = getUser(req);
		
		logger.info(LogUtil.generateMsg(OperateTypeEnum.HARDCERT_ACTIVE, user, env, StringUtils.EMPTY));
		String memberId = user.getMemberId();
		String operatorId = user.getCurrentOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		RestResponse result = certificationService.certActivate(request, env);
		
		if(result.isSuccess()) {
			//取消软证书
			CertificationInfoRequest request1 = new CertificationInfoRequest();
			request1.setMemberId(memberId);
			request1.setOperatorId(operatorId);
			request1.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
			RestResponse cancelResult = null;
			try {
				cancelResult = certificationService.certUnbind(request1, env);
				if(cancelResult.isSuccess()) {
					mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_active_step4");
				} else {
					logger.error("激活快捷盾时取消数字软证书失败！");
	    			mv.addObject("message", "激活快捷盾失败");
	    			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
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
	 * 获取证书列表
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getCertSns.htm")
	public RestResponse getCertSns(HttpServletRequest request) {
		String type = request.getParameter("type");
		String status = request.getParameter("status");
		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		EnterpriseMember user = this.getUser(request);
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getCurrentOperatorId());
		certRequest.setCertificationType(type);
		certRequest.setCertificationStatus(status);
		RestResponse result = null;
		OperatorInfoResponse response = operatorFacade.getOperatorByOperatorId(env, user.getCurrentOperatorId());
		Operator operator = response.getOperatorInfo();
		Map<String, String> maps = JsonMapUtil.jsonToMapStr(operator.getExtention());
		String securityFlag = maps.get("securityFlag");
		try {
			 result = certificationService.getCertByCertStatus(certRequest, env);
			 if(!result.isSuccess() && (("1".equals(securityFlag) && type.equals("soft"))||("10".equals(securityFlag) && type.equals("hard")))){
				result.setSuccess(true);
				result.setData(new HashMap<String, Object>());
				result.getData().put("certSns",new ArrayList<String>());
			 }
		} catch (BizException e) {
			logger.error("获取证书列表失败");
		}
		return result;
	}
	
	/**
	 * 异步方式验证签名
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@ResponseBody
	@RequestMapping("/validateSignature.htm")
	public RestResponse validateSignature(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		String plain = request.getParameter("plain");
		String signData = request.getParameter("signData");
		
		try {
			if (!validateSignature(request, plain, signData, response, null, env)) {
				logger.error("验证硬证书签名失败");
				response.setMessage("您的快捷盾已经过期！");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("验证硬证书签名异常", e);
			response.setMessage("您的快捷盾已经过期！");
		}
		
		return response;
	}
	
}
