package com.netfinworks.site.web.action.securityCenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
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
import com.netfinworks.ma.service.response.OperatorInfoResponse;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.info.CertificationDetail;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
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
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.WebDynamicResource;
//import com.netfinworks.site.web.action.KaptchaImageAction;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * 数值软证书action
 * @author chengwen
 *
 */
@Controller
@RequestMapping("/softcertActivate")
public class SoftCertActivateAction extends BaseAction {
	
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
    
    @Resource(name = "operatorFacade")
    private IOperatorFacade operatorFacade;
    
    @RequestMapping("/appCenterTemp.htm")
    public ModelAndView appCenterTemp() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/temp");
        return mv;
    }
    
    /**
	 * 根据用户证书的激活状态，判断跳转到已激活页面、还是未激活页面
	 * @param req
	 * @param env
	 * @return
	 */
    @RequestMapping(value = "/showSoftDetails.htm", method = RequestMethod.GET)
	public ModelAndView safetyDetails(HttpServletRequest req, OperationEnvironment env){
		RestResponse restP = new RestResponse();
		ModelAndView mv = new ModelAndView();
		//获取用户信息
		EnterpriseMember user = getUser(req);
		try {
			String memberId = user.getMemberId();
			String operatorId = user.getCurrentOperatorId();
			CertificationInfoRequest request = new CertificationInfoRequest();
			request.setMemberId(memberId);
			request.setOperatorId(operatorId);
			request.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
			request.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
			//判读用户证书是否激活
			restP = certificationService.getCertByCertStatus(request, env);//CertificationStatus.ACTIVATED.getCode()
			if(restP.isSuccess()) {
				List<CertificationDetail> certificationDetails = (List<CertificationDetail>)restP.getData().get("certificationDetails");
				List<String> certSns = (List<String>)restP.getData().get("certSns");
				if((certSns != null) && (certSns.size() > 0)) {
					mv.addObject("certificationDetails", certificationDetails);
					mv.addObject("certSns", certSns);
					mv.addObject("now",new Date());
					mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activate");
				} else {
					mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activateNo");
				}
			} else {
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activateNo");
			}
		} catch(Exception e) {
			logger.error("获取证书列表失败！", e);
		}
		return mv;
	}
	
	@ResponseBody
    @RequestMapping(value = "/checkSoftCert.htm", method = RequestMethod.GET)
    public RestResponse checkSoftCert(HttpServletRequest req, OperationEnvironment env) {
    	RestResponse result = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
    	//获取用户信息
    	EnterpriseMember user = getUser(req);
		try {
			String memberId = user.getMemberId();
			String operatorId = user.getCurrentOperatorId();
			
			OperatorInfoResponse response = operatorFacade.getOperatorByOperatorId(env, operatorId);
			Operator operator = response.getOperatorInfo();
			Map<String, String> maps = JsonMapUtil.jsonToMapStr(operator.getExtention());
			String securityFlag = maps.get("securityFlag");
			if("1".equals(securityFlag)) {//1表示软证书用户,10表示硬证书用户
				CertificationInfoRequest request = new CertificationInfoRequest();
				request.setMemberId(memberId);
				request.setOperatorId(operatorId);
				request.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
				request.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
				List<String> certSns = new ArrayList<String>();
				// 判读用户证书是否激活
				RestResponse restP = certificationService.getCertByCertStatus(request, env);
				if (restP.isSuccess()) {
					certSns = (List<String>)restP.getData().get("certSns");
				}
				result.setSuccess(true);
				data.put("certSns", certSns);
				result.setData(data);
			} else { //非证书用户
				result.setSuccess(false);
			}
		} catch (Exception e) {
			logger.error("获取证书列表失败！", e);
		}
    	return result;
    }
	
	/**
	 * 是证书用户，并且当前机器安装了数字证书，跳转到“管理数字证书页面”页面
	 * 是证书用户，但当前机器没有安装数字证书，跳转到“你是数字证书用户，但本台电脑尚未安装证书，安装数字证书”页面
	 * 非数字证书用户
	 * @param req
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/gotoview.htm",method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView gotoview(HttpServletRequest req, OperationEnvironment env) {
		ModelAndView mv = new ModelAndView();
    	RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		mv.addObject("response", restP);
		String btype = req.getParameter("btype");
		EnterpriseMember user = getUser(req);
		MemberType memberType = user.getMemberType();
		String membercode = memberType.getCode();
		String mobile = user.getMobile();
		String email = user.getEmail();
	    String realName = null;
	    String enterpriseName = user.getEnterpriseName();
		try {
		    realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
        } catch (Exception e) {
            logger.error("软证书激活第一步获取真实姓名失败： " + e.getMessage(), e.getCause());
        }
		if("isCertUser".equals(btype)) {//是证书用户，本机安装证书，跳转证书详情页面
			return new ModelAndView("redirect:" + "/softcertActivate/showSoftDetails.htm");
		} else if("NotInstallCertUser".equals(btype)) {//是证书用户，但本机未安装证书，跳转安装证书页面
		    mv.addObject("email",email);
			mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_revokeOk");
		}else if("isNotCertUser".equals(btype)){
			//非证书用户，跳转申请证书页面
			//是否是硬证书
			boolean hard = isCertActive(req, "hard", env);
			mv.addObject("email",email);
			mv.addObject("hard", hard);
			mv.addObject("mobile", mobile);
			mv.addObject("realName", realName);
			mv.addObject("enterpriseName", enterpriseName);
			mv.addObject("membercode", membercode);
			mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activateNo");
		} else {
			mv.setViewName("redirect:/securityCenter/safetyIndex.htm");
		}
		return mv;
	}
	
    @RequestMapping(value = "/step1.htm", method = RequestMethod.GET)
    public ModelAndView gotoCheck(HttpServletRequest req, OperationEnvironment env) {
    	ModelAndView mv = new ModelAndView();
    	RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		mv.addObject("response", restP);
    	EnterpriseMember user = getUser(req);
    	String mobile = user.getMobile();
    	String email = user.getEmail();
    	MemberType memberType = user.getMemberType();
    	String realName = null;
    	String enterpriseName = user.getEnterpriseName();
    	try {
            realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
        } catch (Exception e) {
            logger.error("软证书激活第一步获取真实姓名失败： " + e.getMessage(), e.getCause());
        }
		if (user.getMemberId().startsWith("2")) {
			if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
				mv.addObject("message", "请先进行实名认证！");
				mv.addObject("mobile", mobile);
				mv.addObject("realName", realName);
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activateNo");
				return mv;
			}
		}
		mv.addObject("email",email);
    	mv.addObject("mobile", mobile);
    	mv.addObject("realName", realName);
    	mv.addObject("enterpriseName", enterpriseName);
    	mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activate_step1");
    	return mv;
    }
    
    /**
     * 身份证校验（异步）
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/idcard.htm", method = RequestMethod.POST)
    public RestResponse idCard(HttpServletRequest req, OperationEnvironment env){
    	RestResponse restP = new RestResponse();
    	String idcard = req.getParameter("idcard");
		String id_card = null;
		try {
			id_card = getEncryptInfo(req, DeciphedType.ID_CARD, env);
			if((id_card == null) || "".equals(id_card)) {
				restP.setSuccess(false);
				restP.setMessage("身份校验失败！");
			}if(idcard.equals(id_card)){
				restP.setSuccess(true);
			}
		} catch (Exception e) {
			logger.error("身份校验失败！", e);
		}
		return restP;
    }
    
    /**
     * 校验支付密码和手机验证码
     * @param req
     * @param env
     * @return
     * 
     * */
    @RequestMapping(value = "/step2.htm", method = RequestMethod.POST)
    public ModelAndView checkIdentity(HttpServletRequest req, OperationEnvironment env) throws Exception {
    	ModelAndView mv = new ModelAndView();
    	RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		mv.addObject("response", restP);
    	EnterpriseMember user = getUser(req);
    	
    	//真实姓名
    	MemberType memberType = user.getMemberType();
    	String realName = null;
    	String enterpriseName = user.getEnterpriseName();
		try {
		    realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
			mv.addObject("realName", realName);
			mv.addObject("enterpriseName", enterpriseName);
		} catch (Exception e) {
			logger.error("软证书激活第一步获取真实姓名失败： " + e.getMessage(), e.getCause());
		}
		
		//使用地点
		String position = req.getParameter("position");
		HttpSession session = req.getSession();
    	session.setAttribute("position", position);
		if("-1".equals(position)) {
    		//如果自定义字段为null，默认为1（办公室）
    		position = StringUtils.isBlank(req.getParameter("address"))  ? "" : req.getParameter("address");
    		mv.addObject("address", position);
    	}
		
		//手机验证码和支付密码校验
		String memberId = user.getMemberId();
    	//获取手机校验码
    	String authCode = req.getParameter("authCode");
    	//获取支付密码
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
		
		EncryptData  edata = memberService.decipherMember(memberId,
	            DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
	    String mobile = edata.getPlaintext();
		//封装校验短信验证码请求
        AuthCodeRequest anthCodereq = new AuthCodeRequest();
        anthCodereq.setMemberId(memberId);
        anthCodereq.setMobile(mobile);
        String ticket = defaultUesService.encryptData(mobile);
        anthCodereq.setMobileTicket(ticket);
        anthCodereq.setAuthCode(authCode);
        anthCodereq.setBizId(memberId);
        anthCodereq.setBizType(BizType.CERT_APPLY.getCode());
        
        //校验短信验证码
        //boolean verifyResult = false;
		// 检查支付密码
		PayPasswdCheck pcheck = null;
		try{
			//verifyResult = defaultSmsService.verifyMobileAuthCode(anthCodereq, env);
			pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (pcheck == null) {
				logger.error("软证书激活_支付密码校验异常，用户ID[{}]", user.getMemberId());
				mv.addObject("mobile", mobile);
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activate_step1");
				return mv;
			}
			if (pcheck.isSuccess() ) {//&& verifyResult
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activate_step3");
				return mv;
			} else {
//				if(!verifyResult) {//校验码验证失败
//					logger.error("软证书激活_校验码验证失败[{}]", user.getMemberId());
//					mv.addObject("authCodeMessage", "校验码验证失败！");
//					mv.addObject("mobile", mobile);
//				}
				if (!pcheck.isSuccess()) {
					mv.addObject("success", false);
					if (pcheck.isLocked()) {
						logger.info("用户{}账户被锁定", user.getLoginName());
						mv.addObject("passwordmessage", "您的账户被锁定，请联系客服处理");
						
					} else {
						mv.addObject("passwordmessage", "您输入的支付密码不正确，请重新输入！");
					}
				} 
				mv.addObject("mobile", mobile);
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activate_step1");
				return mv;
			}
		} catch (Exception e) {
			logger.error("支付密码或手机验证码校验异常", e);
		}
		return mv;
    }
    
    @ResponseBody
    @RequestMapping(value = "/softcertApply.htm", method = RequestMethod.POST)
    public RestResponse certInstall(HttpServletRequest req, OperationEnvironment env) {
    	RestResponse result = new RestResponse();
    	String csr = req.getParameter("csr");
    	String position = (String)req.getSession().getAttribute("position");
    	String address = req.getParameter("address");
    	//如果产生csr异常，拒绝申请
		if((csr == null) || "".equals(csr)) {
			logger.error("申请软证书失败:产生csr异常！");
			result.setMessage("申请证书失败");
			result.setSuccess(false);
			return result;
		}
		EnterpriseMember user = getUser(req);
    	String memberId = user.getMemberId();
    	String operatorId = user.getCurrentOperatorId();
    	String userIp = env.getClientIp();
    	String userMac = env.getClientMac();
    	String accountHash = null;
    	MemberType memberType = user.getMemberType();
		if("1".equals(memberType.getCode())) {//个人会员
			accountHash = webResource.getPersonalPAccountHash();
		} else if ("2".equals(memberType.getCode())) {//企业会员
			accountHash = webResource.getPersonalEAccountHash();
		} else if ("3".equals(memberType.getCode())) {//商户
		    accountHash = webResource.getEnterpriseAccountHash();
		}
    	CertificationInfoRequest request = new CertificationInfoRequest();
    	request.setMemberId(memberId);
    	request.setOperatorId(operatorId);
    	request.setCsr(csr);
    	if(!StringUtils.isBlank(address)){
    		request.setPosition(address);
    	}else{
        	request.setPosition(position);	
    	}
    	request.setUserIp(userIp);
    	request.setUserMac(userMac);
    	request.setRequestTime(new Date());
    	request.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
    	
    	request.setAccountHash(accountHash);
    	
    	//TODO
    	RestResponse restP = new RestResponse();
    	CertificationInfoRequest request1 = new CertificationInfoRequest();
        request1.setMemberId(memberId);
        request1.setOperatorId(operatorId);
        request1.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
        request1.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
        //判读用户证书是否激活
        try {
            restP = certificationService.getCertByCertStatus(request1, env);
        }catch (BizException e1) {
            logger.error("获取证书列表失败！", e1);
        }
        if(restP.isSuccess()){
            List<CertificationDetail> certificationDetails = (List<CertificationDetail>)restP.getData().get("certificationDetails");
            if(certificationDetails.size()>=5){
                logger.info("一个操作员ID最多可以绑定5个软证书");
                result.setMessage("1");
                result.setSuccess(false);
                return result;
            }
        }
    	try {
			result = certificationService.certApply(request, env);
			if(!result.isSuccess()) {
				result.setMessage(result.getMessage());
			}
		} catch (BizException e) {
			logger.error("数字软证书安装失败:" + e.getMessage(), e.getCause());
			e.printStackTrace();
		}
    	return result;
    }
    
    @RequestMapping(value = "/softcertActive.htm", method = RequestMethod.POST)
    public ModelAndView softcertActive(HttpServletRequest req, OperationEnvironment env) {
    	ModelAndView mv = new ModelAndView();
    	RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		mv.addObject("response", restP);
		String certSn = req.getParameter("certSn");
		//获取
		EnterpriseMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getCurrentOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		RestResponse result = null;
		try {
			result = certificationService.certActivate(request, env);
			if(result.isSuccess()) {
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activate_step4");
			} else {
				mv.addObject("message", "数字软证书激活失败！");
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_activate_step3");
			}
		} catch (BizException e) {
			logger.error("数字软证书激活失败: " + e.getMessage(), e.getCause());
			e.printStackTrace();
		}
    	return mv;
    }
    
    @ResponseBody
    @RequestMapping(value = "/checkCode.htm", method = RequestMethod.POST)
    public RestResponse checkCode(HttpServletRequest req, OperationEnvironment env) throws Exception {
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = new HashMap<String, Object>();
		//获取
    	EnterpriseMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getCurrentOperatorId();
    	//获取手机校验码
    	String authCode = req.getParameter("authCode");
    	//获取支付密码
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
		
		EncryptData  edata = memberService.decipherMember(memberId,
	            DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
	    String mobile = edata.getPlaintext();
		//封装校验短信验证码请求
        AuthCodeRequest anthCodereq = new AuthCodeRequest();
        anthCodereq.setMemberId(memberId);
        anthCodereq.setMobile(mobile);
        String ticket = defaultUesService.encryptData(mobile);
        anthCodereq.setMobileTicket(ticket);
        anthCodereq.setAuthCode(authCode);
        anthCodereq.setBizId(memberId);
        anthCodereq.setBizType(BizType.CERT_UNBIND.getCode());
        
        //校验短信验证码
        boolean verifyResult = false;
		// 检查支付密码
		PayPasswdCheck pcheck = null;
		
		try{
			verifyResult = defaultSmsService.verifyMobileAuthCode(anthCodereq, env);
			pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
			if (pcheck == null) {
				restP.setSuccess(false);
				return restP;
			}
			if (pcheck.isSuccess() && verifyResult) {
				restP.setSuccess(true);
				return restP;
			} else {
				if(!verifyResult) {//校验码验证失败
					logger.error("软证书激活第三步校验码验证失败[{}]", user.getMemberId());
					restP.setSuccess(false);
					data.put("message", "校验码验证失败！");
					restP.setData(data);
					return restP;
				}
				if (!pcheck.isSuccess()) {
					restP.setSuccess(false);
					if (pcheck.isLocked()) {
						logger.info("用户{}账户被锁定", user.getLoginName());
						data.put("message", "您的账户被锁定，请联系客服处理");
					} else {
						data.put("message", "您输入的支付密码不正确，请重新输入！");
					}
					restP.setData(data);
					return restP;
				} 
			}
		} catch (Exception e) {
			logger.error("支付密码或手机验证码校验异常", e);
		}
		return restP;
    }
    
    /**
     * 取消证书：
     * 		取消数字证书调用解绑接口，certSn证书序列号不能传值，即解绑所有用户所用证书，取消成功后，该用户为非证书用户
     * @param req
     * @param env
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/cancelCert.htm", method = RequestMethod.POST)
    public ModelAndView cancelCert(HttpServletRequest req, OperationEnvironment env) {
    	ModelAndView mv = new ModelAndView();
    	//获取
    	EnterpriseMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getCurrentOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
		RestResponse result = null;
		try {
			result = certificationService.certUnbind(request, env);
			if(result.isSuccess()) {
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/softcert_cancel");
			} else {
				logger.error("取消数字证书失败！");
    			mv.addObject("message", "取消数字证书失败");
    			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			}
		} catch (BizException e) {
			logger.error("取消数字软证书失败: " + e.getMessage(), e.getCause());
			e.printStackTrace();
		}
		return mv;
    }
    
    /**
     * 删除数字证书：
     * 		删除用户选中的单个证书，调用吊销接口，即使所用证书都被删除后，该用户认为证书用户。
     * @param req
     * @param env
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delCert.htm", method = RequestMethod.POST)
    public RestResponse delCert(HttpServletRequest req, OperationEnvironment env) {
    	RestResponse restP = new RestResponse();
    	String certSn = req.getParameter("certSn");
    	
    	EnterpriseMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getCurrentOperatorId();
		
    	CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setReason("reason");
		request.setCertSn(certSn);
		RestResponse result = null;
		try {
			result = certificationService.certRevoke(request, env);
			if(result.isSuccess()) {
				restP.setSuccess(true);
			} else {
				restP.setSuccess(false);
			}
		} catch (BizException e) {
			logger.error("数字软证书删除失败: " + e.getMessage(), e.getCause());
			e.printStackTrace();
		}
    	return restP;
    }
    
    
    /**
     * 更新数字证书：
     * 		删除用户选中的单个证书，调用吊销接口，再重新申请一个新证书，但是无需进行验证
     * @param req
     * @param env
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/update.htm",method=RequestMethod.POST)
    public RestResponse updateCert(HttpServletRequest req,OperationEnvironment env){
    	
    	RestResponse restP = new RestResponse();
    	//删除证书所需值
    	String certSn = req.getParameter("certSn");
    	//重新申请证书所需值
    	String csr = req.getParameter("csr");
    	String position = req.getParameter("position");
    	//Csr为空直接提示失败
    	if((csr == null) || "".equals(csr)) {
			logger.error("更新软证书失败:产生csr异常！");
			restP.setMessage("更新证书失败");
			restP.setSuccess(false);
			return restP;
		}
    	//登陆用户信息
    	EnterpriseMember user = getUser(req);
    	String memberId = user.getMemberId();
    	String operatorId = user.getCurrentOperatorId();
    	String userIp = env.getClientIp();
    	String userMac = env.getClientMac();
    	String accountHash = null;
    	MemberType memberType = user.getMemberType();
    	if("1".equals(memberType.getCode())) {//个人会员
			accountHash = webResource.getPersonalPAccountHash();
		} else if ("2".equals(memberType.getCode())) {//企业会员
			accountHash = webResource.getPersonalEAccountHash();
		} else if ("3".equals(memberType.getCode())) {//商户
            accountHash = webResource.getEnterpriseAccountHash();
        }
    	//删除证书的request
    	CertificationInfoRequest request = new CertificationInfoRequest();
    	request.setMemberId(memberId);
    	request.setOperatorId(operatorId);
    	request.setReason("reason");
    	request.setCertSn(certSn);
    	//重新安装新证书的request
    	CertificationInfoRequest request2 = new CertificationInfoRequest();
    	request2.setMemberId(memberId);
    	request2.setOperatorId(operatorId);
    	request2.setCsr(csr);
    	request2.setPosition(position);
    	request2.setUserIp(userIp);
    	request2.setUserMac(userMac);
    	request2.setRequestTime(new Date());
    	request2.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
    	request2.setAccountHash(accountHash);
    	
    	//删除和安装的返回值,只有先删除再进行安装。
    	RestResponse result = null;
    	RestResponse result2 = null;
    	try{
    		result=certificationService.certRevoke(request, env);
    		if(result.isSuccess()){
    			try{
    			result2 = certificationService.certApply(request2, env);
    			if(result2.isSuccess()){
    				restP.setSuccess(true);
    				restP.setData(result2.getData());
    			}else{
    				restP.setMessage("数字证书更新失败");
    				restP.setSuccess(false);
    			}
    			}catch(BizException e){
    				logger.error("数字证书更新失败");
    				e.printStackTrace();
    			}
    		}else{
    			restP.setMessage("数字证书更新失败");
    			restP.setSuccess(false);
    		}
    	}catch(BizException e){
    		logger.error("数字证书更新失败");
    		e.printStackTrace();
    	}
    	return restP ;
    }
    
}
