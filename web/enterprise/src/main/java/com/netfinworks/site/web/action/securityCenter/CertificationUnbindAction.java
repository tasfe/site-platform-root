package com.netfinworks.site.web.action.securityCenter;

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
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.CertificationDetail;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertificationStatus;
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
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;

/**
 * 数字证书解绑的action：
 * step1：验证解绑信息（企业营业执照号、支付密码）
 * @author chengwen
 *
 */
@Controller
@RequestMapping("/certUnbind")
public class CertificationUnbindAction extends BaseAction {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "memberService")
    private MemberService memberService;
	
	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;
	
	@Resource(name = "defaultSmsService")
    private DefaultSmsService	defaultSmsService;
	
	@Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
	
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	
	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@RequestMapping(value = "/step1.htm", method = RequestMethod.GET)
	public ModelAndView UnbindCertStep1(HttpServletRequest req) {
		ModelAndView mv = new ModelAndView();
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		mv.addObject("response", restP);
		String certSn = req.getParameter("certSn");
		EnterpriseMember user = getUser(req);
		String mobile = user.getMobile();
		String enterpriseName = user.getEnterpriseName();
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
		mv.addObject("mobile", mobile);
		mv.addObject("enterpriseName", enterpriseName);
		mv.addObject("certSn", certSn);
		mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_unbind_step1");
		return mv;
	}
	
	/**
	 * 营业执照和支付密码验证
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/step2.htm", method = RequestMethod.POST)
	public ModelAndView UnbindCertStep2(HttpServletRequest req, OperationEnvironment env) throws Exception {
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
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_unbind_step1");
				return mv;
			}
			if (pcheck.isSuccess() && busCode.equals(licenseNo)) {
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_unbind_step2");
			} else {
				if(licenseNo == null || "".equals(licenseNo)) {
					mv.addObject("licenseNoMessage", "注册营业执照号为空！");
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
						mv.addObject("payPasswordmessage", "您输入的支付密码不正确，请重新输入！");
					}
					mv.addObject("remainNum", pcheck.getRemainNum());
				}else {
					mv.addObject("payPasswordmessage", "");
				}
				mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_unbind_step1");
				return mv;
			}
		} catch (ServiceException e) {
			logger.error("支付密码校验异常", e);
			mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_unbind_step1");
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
        anthCodereq.setBizType(BizType.KJTSHILED_UNBIND.getCode());
        
        //校验短信验证码
        boolean verifyResult = defaultSmsService.verifyMobileAuthCode(anthCodereq, env);
        
        if (verifyResult) {//如果验证码正确，到第三步解绑证书
        	restP.setSuccess(true);
        } else {
        	restP.setMessage("校验码验证失败！");
        	restP.setSuccess(false);
        }
        return restP;
	}
	
	@RequestMapping(value = "/certUnbind.htm", method = RequestMethod.POST)
	public ModelAndView certUnbind(HttpServletRequest req, OperationEnvironment env) throws BizException {
		ModelAndView mv = new ModelAndView();
		String certSn = req.getParameter("certSn");
		//获取用户信息
	    EnterpriseMember user = getUser(req);
	    
	    logger.info(LogUtil.generateMsg(OperateTypeEnum.HARDCERT_UNBIND, user, env, StringUtils.EMPTY));
	    String memberId = user.getMemberId();
	    String operatorId = user.getCurrentOperatorId();
	    CertificationInfoRequest request = new CertificationInfoRequest();
	    request.setMemberId(memberId);
	    request.setOperatorId(operatorId);
	    request.setCertSn(certSn);
	    RestResponse result = certificationService.certUnbind(request, env);
	    if(result.isSuccess()) {
	    	mv.addObject("certSn", certSn);
	    	mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_unbind_step3");
	    } else {
	    	mv.addObject("message", "解绑证书失败！");
	    	mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_unbind_step2");
	    }
		return mv;
	}
}
