package com.netfinworks.site.web.action.securityCenter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.request.PersonalMemberQueryRequest;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.ma.service.response.PersonalMemberResponse;
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
@RequestMapping("/hardcertRebind")
public class HardCertRebindAction extends BaseAction {
	
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
    /**
	 * 跳转到身份验证页面
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/step1.htm", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView rebindCertStep1(HttpServletRequest req, OperationEnvironment env) {
		
		ModelAndView mv = initOcxView();
		String certSn = req.getParameter("certSn");
		PersonMember user = getUser(req);
		String mobile = user.getMobile();
		String memberId = user.getMemberId();
		MemberType memberType = user.getMemberType();
		String realName = null;
		try {
			if("1".equals(memberType.getCode())) {//个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
				mv.addObject("company", "身份证号");
			} else if ("2".equals(memberType.getCode())) {//企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
				try{
					EnterpriseMember enterpriseMember = new EnterpriseMember();
	                enterpriseMember.setMemberId(memberId);
	                enterpriseMember = commMemberService.queryCompanyMember(enterpriseMember, env);
					CompanyMemberInfo compInfo = memberService.queryCompanyInfo(enterpriseMember, env);
					Integer companyType = compInfo.getCompanyType();
					if("0".equals(companyType.toString())) {//是企业
						mv.addObject("company", "企业营业执照号");
					} else {
						mv.addObject("company", "组织机构代码");
					}
				} catch(Exception e) {
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
		mv.addObject("certSn", certSn);
		mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step1");
		return mv;
	}
	
	/**
	 * 支付密码和营业执照号校验
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/step2.htm", method = RequestMethod.POST)
	public ModelAndView rebindCertStep2(HttpServletRequest req, OperationEnvironment env) throws Exception {
		String certSn = req.getParameter("certSn");
		ModelAndView mv = initOcxView();
		// 先获取用户信息
		PersonMember user = this.getUser(req);
		String mobile = user.getMobile();
		String memberId = user.getMemberId();
		MemberType memberType = user.getMemberType();
		String realName = null;
		try {
			if("1".equals(memberType.getCode())) {//个人会员
				realName = getEncryptInfo(req, DeciphedType.TRUE_NAME, env);
				mv.addObject("company", "身份证号");
			} else if ("2".equals(memberType.getCode())) {//企业会员
				realName = getEncryptInfo(req, DeciphedType.LEGAL_NAME, env);
				try{
					EnterpriseMember enterpriseMember = new EnterpriseMember();
	                enterpriseMember.setMemberId(memberId);
	                enterpriseMember = commMemberService.queryCompanyMember(enterpriseMember, env);
					CompanyMemberInfo compInfo = memberService.queryCompanyInfo(enterpriseMember, env);
					Integer companyType = compInfo.getCompanyType();
					if("0".equals(companyType.toString())) {//是企业
						mv.addObject("company", "企业营业执照号");
					} else {
						mv.addObject("company", "组织机构代码");
					}
				} catch(Exception e) {
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
		String password = req.getParameter("password");
		String busCode = req.getParameter("busCode");
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
			//获取身份证号
			id_card = getEncryptInfo(req, DeciphedType.ID_CARD, env);
			mv.addObject("certSn", certSn);
			if (pcheck == null) {
				logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step1");
				return mv;
			}
			if(id_card == null || "".equals(id_card)) {
				logger.error("用户身份证号为空或未实名认证，用户ID[{}]", user.getMemberId());
				mv.addObject("buscodeMessage", "身份证号信息未实名认证！");
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step1");
				return mv;
			}
			if (pcheck.isSuccess()) {//user.getLicenseNo()
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step2");
			} else {
				if (!pcheck.isSuccess()) {
					mv.addObject("success", false);
					if (pcheck.isLocked()) {
						logger.info("用户{}账户被锁定", user.getLoginName());
						mv.addObject("payPasswordmessage", "您的账户被锁定，请联系客服处理");
					} else {
						mv.addObject("payPasswordmessage", "您输入的支付密码不正确，请重新输入！");
					}
					mv.addObject("remainNum", pcheck.getRemainNum());
				}
				if(!busCode.toLowerCase().equals(id_card.toLowerCase())) {
					mv.addObject("buscodeMessage", "您输入的身份证号不正确，请重新输入");
				}
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step1");
				return mv;
			}
		} catch (ServiceException e) {
			logger.error("支付密码校验异常", e);
			mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step1");
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
		String certSn = req.getParameter("certSn");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		ModelAndView mv = new ModelAndView();
		//获取用户输入的手机验证码
		String authCode = req.getParameter("authCode");
		//获取用户信息
	    PersonMember user = getUser(req);
	    String memberId = user.getMemberId();
	    String operatorId = user.getOperatorId();
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
        	data.put("certSn", certSn);
            restP.setData(data);
        } else {
        	restP.setMessage("校验码验证失败！");
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
	@RequestMapping(value = "/certRebind.htm", method = RequestMethod.POST)
	public ModelAndView certRebind(HttpServletRequest req, OperationEnvironment env) throws BizException {
		ModelAndView mv = initOcxView();
		String certSn = req.getParameter("certSn");
		//获取
		PersonMember user = getUser(req);
		String memberId = user.getMemberId();
		String operatorId = user.getOperatorId();
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		RestResponse result = certificationService.certActivate(request, env);
		if(result.isSuccess()) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step3");
		} else {
			mv.addObject("message", "系统错误，重绑失败！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/hardcert_rebind_step2");
		}
		return mv;
	}
}
