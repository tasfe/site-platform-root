/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.certification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.meidusa.fastjson.TypeReference;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.SampleExceptionUtils;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.auth.ICertValidateVO;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.FileInfo;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CardType;
import com.netfinworks.site.domain.enums.CertType;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultICertValidateService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.email.EmailResetAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.form.EnterpriseCertificationForm;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * <p>企业会员实名认证</p>
 * @author liangzhizhuang.m
 * @version $Id: EnterpriseCertificationAction.java, v 0.1 2014年5月30日 下午7:37:03 liangzhizhuang.m Exp $
 */
@Controller
public class EnterpriseCertificationAction extends BaseAction {

    @Resource(name = "defaultCertService")
    private DefaultCertService defaultCertService;
    
	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;

	@Resource(name = "webResource")
	private WebDynamicResource webResource;

	@Resource(name = "memberService")
	private MemberService commMemberService;
	
	@Resource(name = "defaultICertValidateService")
    private DefaultICertValidateService defaultICertValidateService;
	
    @Resource(name="accountService")
    private AccountService accountService;
    
    @Resource(name = "uesService")
	private UesServiceClient uesService;

    protected static final Logger   logger = LoggerFactory.getLogger(EmailResetAction.class);

    
    /**
     * 企业会员实名认证引导页面
     * @param req
     * @param resp
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/certification-guide.htm", method = RequestMethod.GET)
	public ModelAndView certificationGuide(HttpServletRequest req, HttpServletResponse resp,
			OperationEnvironment env) throws Exception {
    	RestResponse restP = new RestResponse();
		return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-enterprise-guide", "response",
				restP);
    }
    
    
    /**
     * 进入企业会员实名认证页面
     * @param req
     * @param resp
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/go-enterprise-certification.htm", method = RequestMethod.GET)
	public ModelAndView goEnterpriseCertification(HttpServletRequest req, HttpServletResponse resp,
			OperationEnvironment env) throws Exception {
		PersonMember user = getUser(req);

		if ((user.getNameVerifyStatus() != AuthResultStatus.NOT_FOUND)
				&& (user.getNameVerifyStatus() != AuthResultStatus.CHECK_REJECT)) {
			return new ModelAndView("redirect:/error.htm");
		}

		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		if (user.getMemberType() == MemberType.PERSONAL) {
			data.put("realName", "" + user.getRealName().getPlaintext());
			data.put("loginName", "" + user.getLoginName());
			return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-personal", "response",
					restP);
		}

		EnterpriseMember enterpriseMember = new EnterpriseMember();
		enterpriseMember.setMemberId(user.getMemberId());
		enterpriseMember = commMemberService.queryCompanyMember(enterpriseMember, env);

		CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(enterpriseMember, env);

		enterpriseMember.setCompanyType(compInfo.getCompanyType());
		enterpriseMember.setEnterpriseName(compInfo.getCompanyName());

		data.put("companyType", "" + enterpriseMember.getCompanyType());
		// data.put("companyName", "" + enterpriseMember.getEnterpriseName());
		
		String userType = getJsonAttribute(req.getSession(), "userType", String.class);
		if("0".equals(userType) || "1".equals(userType)) {// 核心企业和供应商的组织机构代号不能修改
		    String organizationId = accountService.queryMemberOrganization(user.getMemberId());
		    if(StringUtils.isEmpty(organizationId)) {
		       return new ModelAndView("redirect:/error.htm");
		    }
		    data.put("organizationId", organizationId);
		}

		super.setJsonAttribute(req.getSession(), "enterpriseMember", enterpriseMember);



		return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/enterprise-certification", "response",
				restP);
    }

	/**
	 * 保存实名认证信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/enterprise-certification.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse enterpriseCertification(HttpServletRequest req, EnterpriseCertificationForm form,
			BindingResult result, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		if (result.hasErrors()) {
			restP.setMessage("表单验证错误");
			return restP;
		}
		PersonMember user = super.getUser(req);
		try {
			String filePath = StringUtils.trim(webResource.getUploadFilePath());
			EnterpriseMember infoMember = new EnterpriseMember();
			infoMember.setCompanyName(form.getCorpLicenceName());
			infoMember.setMemberName(form.getCorpLicenceName());
			infoMember.setMemberId(super.getMemberId(req));
			infoMember.setLicenseNo(form.getCorpLicenceNo());
			if (form.getEndDate().equals("长期")) {
				infoMember.setLicenseExpireDate(DateUtil.parseDateNoTime("2050-11-11", "yyyy-mm-dd"));
			} else {
				infoMember.setLicenseExpireDate(DateUtil.parseDateNoTime(form.getEndDate(), "yyyy-mm-dd"));
			}
			
    	    String userType = getJsonAttribute(req.getSession(), "userType", String.class);
	        if("0".equals(userType) || "1".equals(userType)) {// 核心企业和供应商的组织机构代号不能修改
	            String organizationId = accountService.queryMemberOrganization(user.getMemberId());
	            if(!form.getCorpOrganizeNo1().equals(organizationId)) {
	                restP.setMessage("您的组织机构代号不能修改");
	                restP.setSuccess(false);
	                return restP;
	            }
	        }
			infoMember.setOrganizationNo(form.getCorpOrganizeNo1());
			infoMember.setIcpLicensePath(form.getIcpNo());
			infoMember.setAddress(form.getProvince() + form.getCity() + form.getTown() + form.getCorpAddr1());
			infoMember.setBusinessWebsite(form.getSite());
			infoMember.setBusinessScope(form.getScope());

			/*
			 * if (form.getCompType().equals("0")) {
			 * infoMember.setLicenseNoPath(filePath + form.getImageUrl1());
			 * } else {
			 * infoMember.setLicenseName(form.getRegistration());
			 * infoMember.setLicenseNoPath(filePath + form.getImageUrl5());
			 * }
			 */
			infoMember.setLicenseNoPath(filePath + form.getImageUrl1());
			if (!defaultMemberService.setCompanyMember(infoMember, env)) {
				throw new Exception("企业会员信息设置失败");
			}
			req.getSession().setAttribute(CommonConstant.KJT_PERSON_USER_NAME, infoMember.getMemberName());
			restP.setSuccess(true);

		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage(e.getMessage());
		}
		return restP;
	}

	/**
	 * 进入资金账户页面
	 * 
	 * @param req
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/go-account-certification.htm", method = RequestMethod.GET)
	public ModelAndView goAccountCertification(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws Exception {
		RestResponse restP = new RestResponse();
		EnterpriseMember user = super.getJsonAttribute(req.getSession(), "enterpriseMember", EnterpriseMember.class);
		Map<String, Object> data = new HashMap<String, Object>();
		String memberName = (String) req.getSession().getAttribute(CommonConstant.KJT_PERSON_USER_NAME);
		data.put("companyName", "" + memberName);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/account-certification", "response", restP);
	}

	/**
	 * 保存资金账户信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/account-certification.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse accountCertification(HttpServletRequest req, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			String bankaccount1 = req.getParameter("bankaccount1");
			String bankaccount2 = req.getParameter("bankaccount2");
			if (!bankaccount1.equals(bankaccount2)) {
				restP.setMessage("银行账户两次输入不一致");
				return restP;
			}
			BankAccRequest accReq = new BankAccRequest();
			accReq.setMemberId(super.getMemberId(req));
			accReq.setClientIp(req.getRemoteAddr());
			accReq.setBankAccountNum(bankaccount1);
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
			if ((list != null) && (list.size() > 0)) {
				// restP.setMessage("对不起，此银行卡已绑定，请重新选择另一张!");
				accReq.setBankcardId(list.get(0).getBankcardId());
				defaultBankAccountService.removeBankAccount(accReq);
			}

			String bank_account_name = req.getParameter("bank_account_name");
			String bankName = req.getParameter("bankName");
			String bankCode = req.getParameter("bankCode");
			String branchname = req.getParameter("branchname");

			try {
				CardBin cardBin = defaultPfsBaseService.queryByCardNo(bankaccount1, CommonConstant.ENTERPRISE_APP_ID);
				if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
					restP.setMessage("绑定银行卡不能是信用卡!");
					return restP;
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", bankaccount1);
				if (!defaultPfsBaseService.cardValidate(CommonConstant.ENTERPRISE_APP_ID, bankCode, bankaccount1)) {
					restP.setMessage("绑定银行卡卡号不正确!");
					return restP;
				}
			}

			EnterpriseMember infoMember = new EnterpriseMember();
			infoMember.setCompanyName(bank_account_name);
			infoMember.setMemberId(super.getMemberId(req));

			if (!defaultMemberService.setCompanyMember(infoMember, env)) {
				restP.setMessage("企业会员信息设置失败");
				return restP;
			}


			BankAccRequest bankReq = new BankAccRequest();
			bankReq.setMemberId(super.getMemberId(req));
			bankReq.setBankName(bankName);
			bankReq.setBankCode(bankCode);
			bankReq.setBranchName(branchname);
			bankReq.setCardType(Integer.valueOf(CardType.JJK.getCode()));
			bankReq.setCardAttribute(0);
			bankReq.setPayAttribute("normal");
			bankReq.setProvName(req.getParameter("province"));
			bankReq.setCityName(req.getParameter("city"));
			bankReq.setBankAccountNum(bankaccount1);
			bankReq.setRealName(bank_account_name);
			bankReq.setIsVerified(0);// 0未认证 1已认证 2认证中 3认证失败

			String bankCardId = defaultBankAccountService.addBankAccount(bankReq);

			if ((bankCardId != null) && !"".equals(bankCardId)) {
				BankAccRequest bankReq2 = new BankAccRequest();
				bankReq2.setMemberId(super.getMemberId(req));
				
                Map<String, String> map = new HashMap<String, String>();
                BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(bankCardId);
                if (StringUtil.isNotBlank(info.getExtention())) {
                    Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                            new TypeReference<Map<String, String>>() {
                            });
                    for (String key : extMap.keySet()) {
                        map.put(key, extMap.get(key));
                    }
                }
                map.put("isDefaultcard", "1");
                
				bankReq2.setBankcardId(bankCardId);
				bankReq2.setExtInfo(JSONObject.toJSONString(map));

				defaultBankAccountService.updateDefaultAccount(bankReq2);
			}

			restP.setSuccess(true);

		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage(e.getMessage());
		}
		return restP;
	}

	/**
	 * 进入法人信息页面
	 * 
	 * @param req
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/go-person-certification.htm", method = RequestMethod.GET)
	public ModelAndView goPersonCertification(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		List<CertType> certTypeList = CertType.toFilterList(Arrays.asList(new CertType[]{CertType.BANK_CARD_NO}));
		req.setAttribute("certTypeList", certTypeList);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/certification/person-certification", "response", restP);
	}

	/**
	 * 保存法人信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/person-certification.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse personCertification(HttpServletRequest req, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			String dbrflag = req.getParameter("dbrflag");
			boolean isDbrFlag = "true".equals(dbrflag);
			// 个人信息
			String corporate    = req.getParameter("corporate"),
				   idCard       = req.getParameter("idcard"),
				   cardType1    = req.getParameter("cardType1");
			// 代理人信息
			String proxyPersonId   = req.getParameter("dbrnameid"),
				   proxyPersonName = req.getParameter("dbrname"),
				   cardType2       = req.getParameter("cardType2");
			
			EnterpriseMember infoMember = new EnterpriseMember();
			infoMember.setMemberId(super.getMemberId(req));
			infoMember.setLegalPerson(corporate);
			infoMember.setLegalPersonPhone(req.getParameter("mobile"));

			if (isDbrFlag) {
				infoMember.setProxyPerson(proxyPersonName);
			}

			if (!defaultMemberService.setCompanyMember(infoMember, env)) {
				throw new Exception("企业会员信息设置失败");
			}
			EnterpriseMember user = super
					.getJsonAttribute(req.getSession(), "enterpriseMember", EnterpriseMember.class);
			CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(user, env);
			
			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(user.getMemberId());
			info.setOperator(user.getOperatorId());
			info.setAuthType(AuthType.REAL_NAME);
			req.getSession().removeAttribute(CommonConstant.SESSION_CERTIFICATION);

			String filePath = StringUtils.trim(webResource.getUploadFilePath());

			List<FileInfo> authFiles = new ArrayList<FileInfo>();
			FileInfo file1 = createAuthFile(filePath + req.getParameter("imageUrl1"));
			FileInfo file2 = createAuthFile(filePath + req.getParameter("imageUrl2"));
			FileInfo file3 = createAuthFile(compInfo.getLicenseNoPath());
			// FileInfo file4 = createAuthFile(compInfo.getOrganizationNoPath());

			authFiles.add(file1);
			authFiles.add(file2);
			authFiles.add(file3);
			// authFiles.add(file4);

			// if (compInfo.getTaxNoPath() != null) {
			// FileInfo file5 = createAuthFile(compInfo.getTaxNoPath());
			// authFiles.add(file5);
			// }


			if (isDbrFlag) {
				FileInfo file6 = createAuthFile(filePath + req.getParameter("imageUrl3"));
				FileInfo file7 = createAuthFile(filePath + req.getParameter("imageUrl4"));
				FileInfo file8 = createAuthFile(filePath + req.getParameter("imageUrl5"));
				authFiles.add(file6);
				authFiles.add(file7);
				authFiles.add(file8);
				Map map = new HashMap();
				map.put("proxyPersonId", proxyPersonId);
				info.setExt(JSONObject.toJSONString(map));
			}

			
			/** 国政通实名认证 */
			// 验证参数
			ICertValidateVO iCertVO = new ICertValidateVO();
			iCertVO.setIsDbrFlag(isDbrFlag);
			// 设置企业信息
			iCertVO.setName(corporate);
			iCertVO.setIdCard(idCard);
			iCertVO.setCardType1(cardType1);
			// 设置代理人信息
			iCertVO.setProxyPersonName(proxyPersonName);
			iCertVO.setProxyPersonIdCard(proxyPersonId);
			iCertVO.setCardType2(cardType2);
			
			// 国政通验证  这里不需要验证是否成功 不抛出异常就是成功
			defaultICertValidateService.validPersonAndProxyPerson(env, iCertVO);
			/****************************/

			info.setAuthFiles(authFiles);
			info.setClientIp(env.getClientId());
			info.setAuthName(compInfo.getCompanyName());
			info.setCertType(cardType1);
			info.setAuthNo(idCard);
			
			if (defaultCertService.certification(info)) {
				restP.setSuccess(true);
				user.setNameVerifyStatus(AuthResultStatus.INIT);
			} else {
				throw new Exception("会员实名认证失败");
			}

			// 更新session中的user信息
			user.setMemberName(compInfo.getCompanyName());
			req.getSession().setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER,
					JsonUtils.toJson(user));

			restP.setSuccess(true);
			restP.setMessage("会员实名认证成功");
		} catch (Exception e) {
			restP.setSuccess(false);
			logger.error("", e);
			// 检查异常是否提示给客户
			if (SampleExceptionUtils.isTip(e)) {
				String msg = e.getMessage();
				restP.setMessage(SampleExceptionUtils.restMsg(msg));
			} else {
				restP.setMessage("会员实名认证失败");
			}
		}
		return restP;
	}

	/**
	 * 保存法人信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/certification-personal.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse certificationPersonal(HttpServletRequest req, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			PersonMember user = getUser(req);

			String realName = req.getParameter("realName");

			if ((null == realName) || "".equals(realName)) {
				throw new Exception("真实姓名不能为空");
			}

			user.setStrRealName(realName);
			user.setMemberName(realName);
			if (!commMemberService.setPersonalMemberInfo(user, env)) {
				throw new Exception("个人会员信息设置失败");
			}

			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(user.getMemberId());
			info.setOperator(user.getOperatorId());
			info.setAuthType(AuthType.REAL_NAME);
			req.getSession().removeAttribute(CommonConstant.SESSION_CERTIFICATION);

			String filePath = StringUtils.trim(webResource.getUploadFilePath());

			List<FileInfo> authFiles = new ArrayList<FileInfo>();
			FileInfo file1 = createAuthFile(filePath + req.getParameter("imageUrl1"));
			FileInfo file2 = createAuthFile(filePath + req.getParameter("imageUrl2"));

			authFiles.add(file1);
			authFiles.add(file2);

			info.setAuthFiles(authFiles);
			info.setClientIp(env.getClientId());
			info.setAuthName(realName);
			info.setCertType(CertType.ID_CARD.getCode());
			info.setAuthNo(req.getParameter("idcard"));

			super.setJsonAttribute(req.getSession(), "authInfoRequest", info);
			restP.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			restP.setMessage(e.getMessage());
		}
		return restP;
	}

	/**
	 * 进入资金账户页面
	 * 
	 * @param req
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/go-account-certification-per.htm", method = RequestMethod.GET)
	public ModelAndView goAccountCertificationPer(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(req);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("companyName", "" + user.getRealName().getPlaintext());
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/account-certification-per", "response",
				restP);
	}

	/**
	 * 保存资金账户信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/account-certification-per.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse accountCertificationPer(HttpServletRequest req, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			PersonMember user = getUser(req);
			if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.CERTIFICATION_PERSONAL.getMsg(), user, env));
			}
			
			String bankaccount1 = req.getParameter("bankaccount1");
			String bankaccount2 = req.getParameter("bankaccount2");
			if (!bankaccount1.equals(bankaccount2)) {
				restP.setMessage("银行账户两次输入不一致");
				return restP;
			}
			BankAccRequest accReq = new BankAccRequest();
			accReq.setMemberId(super.getMemberId(req));
			accReq.setClientIp(req.getRemoteAddr());
			accReq.setBankAccountNum(bankaccount1);
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
			if ((list != null) && (list.size() > 0)) {
				// restP.setMessage("对不起，此银行卡已绑定，请重新选择另一张!");
				accReq.setBankcardId(list.get(0).getBankcardId());
				defaultBankAccountService.removeBankAccount(accReq);
			}

			String bank_account_name = req.getParameter("bank_account_name");
			String bankName = req.getParameter("bankName");
			String bankCode = req.getParameter("bankCode");
			String branchname = req.getParameter("branchname");

			try {
				CardBin cardBin = defaultPfsBaseService.queryByCardNo(bankaccount1, CommonConstant.ENTERPRISE_APP_ID);
				if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
					restP.setMessage("绑定银行卡不能是信用卡!");
					return restP;
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", bankaccount1);
				if (!defaultPfsBaseService.cardValidate(CommonConstant.ENTERPRISE_APP_ID, bankCode, bankaccount1)) {
					restP.setMessage("绑定银行卡卡号不正确!");
					return restP;
				}
			}
			BankAccRequest bankReq = new BankAccRequest();
			bankReq.setMemberId(super.getMemberId(req));
			bankReq.setBankName(bankName);
			bankReq.setBankCode(bankCode);
			bankReq.setBranchName(branchname);
			bankReq.setCardType(Integer.valueOf(CardType.JJK.getCode()));
			bankReq.setCardAttribute(1);
			bankReq.setPayAttribute("normal");
			bankReq.setProvName(req.getParameter("province"));
			bankReq.setCityName(req.getParameter("city"));
			bankReq.setBankAccountNum(bankaccount1);
			bankReq.setRealName(bank_account_name);
			bankReq.setIsVerified(0);// 0未认证 1已认证 2认证中 3认证失败

			String bankCardId = defaultBankAccountService.addBankAccount(bankReq);

			if ((bankCardId != null) && !"".equals(bankCardId)) {
				BankAccRequest bankReq2 = new BankAccRequest();
				bankReq2.setMemberId(super.getMemberId(req));
				
				Map<String, String> map = new HashMap<String, String>();
                BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(bankCardId);
                if (StringUtil.isNotBlank(info.getExtention())) {
                    Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                            new TypeReference<Map<String, String>>() {
                            });
                    for (String key : extMap.keySet()) {
                        map.put(key, extMap.get(key));
                    }
                    map.put("isDefaultcard", "1");
                }
                
				bankReq2.setBankcardId(bankCardId);
				bankReq2.setExtInfo(JSONObject.toJSONString(map));

				defaultBankAccountService.updateDefaultAccount(bankReq2);
			}
			
			AuthInfoRequest info = super.getJsonAttribute(req.getSession(), "authInfoRequest", AuthInfoRequest.class);
			
			/** 国政通验证 */
			String realName = info.getAuthName(),
				   idCardNo = info.getAuthNo();
			
			defaultICertValidateService.IdCardValidate(env, realName, idCardNo);
			/****************/

			if (defaultCertService.certification(info)) {
				user.setNameVerifyStatus(AuthResultStatus.INIT);
			} else {
				throw new Exception("个人会员实名认证失败");
			}

			restP.setMessage("个人会员实名认证成功");
			restP.setSuccess(true);		

		} catch (Exception e) {
			restP.setSuccess(false);
			logger.error("", e);
			// 检查异常是否提示给客户
			if (SampleExceptionUtils.isTip(e)) {
				String msg = e.getMessage();
				restP.setMessage(SampleExceptionUtils.restMsg(msg));
			} else {
				restP.setMessage("个人会员实名认证失败");
			}
		}
		return restP;
	}

	/**
	 * 结果页
	 * 
	 * @param req
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/certification-result.htm", method = RequestMethod.GET)
	public ModelAndView goResult(HttpServletRequest req, HttpServletResponse resp, ModelMap model) throws Exception {
		return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-result");
	}

	/**
	 * 金额验证
	 * 
	 * @param req
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/verifyAmount.htm", method = RequestMethod.GET)
	public ModelAndView verifyAmount(HttpServletRequest req, HttpServletResponse resp, ModelMap model) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();

		BankAccRequest bankReq = new BankAccRequest();
		bankReq.setMemberId(super.getMemberId(req));
		bankReq.setClientIp(req.getRemoteAddr());
		List<BankAccountInfo> list = null;
		try {
			list = defaultBankAccountService.queryBankAccount(bankReq);
			data.put("info", list.get(0));
		} catch (ServiceException e) {
			logger.error("查询会员银行卡信息，调用接口异常！");
			restP.setMessage(e.getMessage());
		}

		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/verify-amount", "response", restP);
	}

	/**
	 * 金额验证
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/doVerifyAmount.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse doVerifyAmount(HttpServletRequest req, EnterpriseCertificationForm form,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			PersonMember user = getUser(req);
			if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.ACCOUNT_CERTIFICATION_PERSONAL.getMsg(), user, env));
			}
			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.BANK_CARD);
			authInfoReq.setOperator(user.getOperatorId());
			AuthInfo info = defaultCertService.queryRealName(authInfoReq);
			if ((info == null) || "".equals(info.getExtension())) {
				restP.setMessage("未查询到实名认证信息");
				return restP;
			}
			Map map = JSONObject.parseObject(info.getExtension());
			String realAmount = (String) map.get("amount");
			String amount = req.getParameter("money");
			if (!amount.equals(realAmount)) {
				restP.setMessage("打款金额和实际不符");
				return restP;
			}
			List<String> orderNoList = new ArrayList<String>();
			orderNoList.add(info.getOrderNo());
			authInfoReq.setOrderNoList(orderNoList);
			authInfoReq.setChecked(true);
			data.put("authNo", getDataByTicket(info.getAuthNo()));
			data.put("verifyDate", DateUtil.format(new Date(), DateUtil.webFormat));

			restP.setSuccess(defaultCertService.verify(authInfoReq));
			restP.setSuccess(true);

		} catch (Exception e) {
			restP.setMessage("金额验证失败");
			logger.error("", e);
		}
		return restP;
	}

	/**
	 * 详情页
	 * 
	 * @param req
	 * @param resp
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/certification-detail.htm", method = RequestMethod.GET)
	public ModelAndView queryDetail(HttpServletRequest req, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		PersonMember user = getUser(req);
		data.put("memberType", user.getMemberType().getCode());
		if (user.getMemberType() == MemberType.PERSONAL) {
			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.REAL_NAME);
			authInfoReq.setOperator(user.getOperatorId());
			AuthInfo info = defaultCertService.queryRealName(authInfoReq);
			data.put("authNo", StarUtil.mockCommon(info.getAuthNo()));
			data.put("realName", user.getRealName().getPlaintext());
		} else {
			EnterpriseMember enterpriseMember = new EnterpriseMember();
			enterpriseMember.setMemberId(user.getMemberId());
			CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(enterpriseMember, env);

			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.REAL_NAME);
			authInfoReq.setOperator(user.getOperatorId());
			AuthInfo info = defaultCertService.queryRealName(authInfoReq);
			Map map = JSONObject.parseObject(info.getExtension());
			String proxyPersonId = (String) map.get("proxyPersonId");

			data.put("user", compInfo);
			String legalPerson = this.uesService.getDataByTicket(compInfo.getLegalPerson());
			data.put("legalPerson", legalPerson);
			data.put("authNo", StarUtil.hideStrBySym(info.getAuthNo(), 5, 3, 6));
			data.put("licenseNo", compInfo.getLicenseNo());
			data.put("organizationNo", compInfo.getOrganizationNo());
			data.put("proxyPersonId", StarUtil.hideStrBySym(proxyPersonId,5,3,6));
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-detail", "response", restP);
	}

	/**
	 * 查询所有省市县
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/my/queryAllProvCities")
	public RestResponse queryAllProvinces(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		List<Province> provlist = null;
		try {
			// 查询省市县信息
			provlist = defaultPfsBaseService.queryProvInfos(true, true);
		} catch (ServiceException e) {
			logger.error("查询所有所有省市县失败", e);
		}

		response.setSuccess(true);
		response.setMessageObj(provlist);
		return response;
	}


    private FileInfo createAuthFile(String imageUrl) {
		if ((imageUrl == null) || "".equals(imageUrl)) {
			return null;
		}
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String fileType = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
        FileInfo file = new FileInfo();
        file.setFileName(fileName);
        file.setFilePath(imageUrl);
        file.setFileType(fileType);
        return file;
    }

}
