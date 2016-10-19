/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
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
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AccountTypeKind;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CardType;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberSetStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.form.BankAccountForm;

/**
 * 通用说明：会员信息control
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午10:04:25
 *
 */
@Controller
public class MemberAction extends BaseAction {

    @Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;

    @Resource(name = "webResource")
    private WebDynamicResource   webResource;

    @Resource(name = "authVerifyService")
    private AuthVerifyService    authVerifyService;

	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;

	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;

	@Resource(name = "certificationService")
	private CertificationService certificationService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "memberService")
	private MemberService commMemberService;

	@Resource(name = "maQueryService")
    private MaQueryService maQueryService;
	
	@Resource(name = "accountService")
    private AccountService accountService;

	
	/**
	 * 账户资产
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/accountInfo.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView accountInfo(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env,HttpSession session) throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
		String userType = (String) session.getAttribute("userType");
		String accountType = (String) session.getAttribute("accountType");
//        try {
//            userType = accountService.queryUserType(user.getMemberId());//根据memberId查询企业账户是0:供应商  1:核心企业  -1:普通企业
//        }
//        catch (Exception e) {
//            logger.info("根据memberId查询企业账户类型失败,memberId="+user.getMemberId());
//            logger.error("错误信息",e);
//        }
		if(userType!=null){
		    data.put("userType", userType);
		}
		if(accountType!=null){
            data.put("accountType", accountType);
        }
		MemberInfo memberInfo = maQueryService.queryMemberIntegratedInfo(user.getLoginName(), "" + 1);//根据登录名查出所有帐号信息
		List<com.netfinworks.ma.service.response.AccountInfo> accounts = memberInfo.getAccounts();
		String baoLiAccountId = null;
		for(com.netfinworks.ma.service.response.AccountInfo account:accounts){
		    if(account.getAccountType().equals(AccountTypeKind.BAOLIHU_BASE_ACCOUNT.getCode())){
		        baoLiAccountId=account.getAccountId();
		    }
		}
		MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
		MemberAccount baoliaccount = new MemberAccount();
		if(baoLiAccountId != null){
		    baoliaccount = defaultAccountService.queryAccountById(baoLiAccountId, env);
		    data.put("baoliaccount", baoliaccount);
		}
		data.put("account", account);
		
		// 查询绑定银行卡
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());
		List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
		String defaultCard = this.getDefaultBankCard(list);
		
		data.put("defaultCard", defaultCard);
		data.put("list", list);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-info", "response", restP);
	}

	/**
	 * 银行账户
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/bankCardInfo.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView bankCardInfo(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
		MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
		data.put("account", account);


		// 查询绑定银行卡
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());
		List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
		String defaultCard = this.getDefaultBankCard(list);
		
		data.put("defaultCard", defaultCard);
		data.put("list", list);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/bank-card-info", "response", restP);
	}

	/**
	 * 账户设置
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/accountSetting.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView accountSetting(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();

		data.put("membertType", user.getMemberType().getCode());
		data.put("verify_name", user.getNameVerifyStatus().getCode());

		if (user.getMemberType().getCode().equals("1")) {
			data.put("dealedRealName", StarUtil.hideLongRealName(user.getRealName().getPlaintext()));
			data.put("realName", user.getRealName().getPlaintext());
		}

		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(user.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);

		if(infos.size()>=1){
			data.put("phone_email", "true");//有绑定手机或邮箱
		}else {
			data.put("phone_email", "false");//都没绑定
		}
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				data.put("verify_mobile", authVerifyInfo.getVerifyEntity());
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				data.put("verify_email", authVerifyInfo.getVerifyEntity());
			}
		}

		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getOperatorId());
		certRequest.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
		certRequest.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
		// 判读用户软证书是否激活
		RestResponse certResponse;
		try {
			certResponse = certificationService.getCertByCertStatus(certRequest, env);
			if (certResponse.isSuccess()) {
				data.put("soft", "true");// 已激活
			} else {
				data.put("soft", "false");// 其他
			}
		} catch (BizException e) {
			data.put("soft", "false");// 其他
		}

		certRequest.setCertificationType(CertificationType.HARD_CERTIFICATION.getCode());
		// 判读用户硬证书是否激活
		try {
			certResponse = certificationService.getCertByCertStatus(certRequest, env);
			if (certResponse.isSuccess()) {
				data.put("hard", "true");// 已激活
			} else {
				data.put("hard", "false");// 其他
			}
		} catch (BizException e) {
			data.put("hard", "false");// 其他
		}

		AuthInfoRequest authInfoReq = new AuthInfoRequest();
		authInfoReq.setMemberId(user.getMemberId());
		authInfoReq.setAuthType(AuthType.IDENTITY);
		authInfoReq.setOperator(user.getOperatorId());
		AuthInfo info = defaultCertService.queryRealName(authInfoReq);
		data.put("info", info);

		if (StringUtils.isEmpty(info.getAuthNo())) {
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.REAL_NAME);
			authInfoReq.setOperator(user.getOperatorId());
			info = defaultCertService.queryRealName(authInfoReq);
		}

		// 屏蔽身份证号码
		if (info.getAuthNo() != null) {
			int len=info.getAuthNo().length();
			if(len>=8){
				data.put("authNo", StarUtil.hideStrBySym(info.getAuthNo(), 4, 4, 6));
			}else if(len>=4){
				data.put("authNo", StarUtil.hideStrBySym(info.getAuthNo(), 2, 2, 6));
			}else {
				data.put("authNo", info.getAuthNo());
			}
		} else {
			data.put("authNo", "");
		}

		data.put("user", user);
		if(user.getMemberName()!=null){
			if(user.getMemberName().length()>4){
				data.put("dealedMemberName", StarUtil.hideLongRealName(user.getMemberName()));
			}else{
				data.put("dealedMemberName", user.getMemberName());
			}
		}
		data.put("memberName", user.getMemberName());

		String loginName = user.getLoginName();
		Pattern pattern = Pattern.compile(CommonConstant.PATTERN_MOBILE);
		Matcher matcher = pattern.matcher(loginName);
		if (matcher.matches()) {
			data.put("loginName", StarUtil.mockMobile(loginName));
		} else {
			int index = loginName.indexOf("@");
			if(-1==index){
				data.put("loginName", loginName);
			}
			data.put("loginName", StarUtil.hideStrBySym(loginName, 1, loginName.length()-index, 3));
		}

		// 实名认证级别
		if (user.getMemberId().startsWith("1")) {
			data.put("memberType", "personal");
			data.put("certifyLevel", user.getCertifyLevel().getCode());
		}

		// 是否设置支付密码
		if (user.getPaypasswdstatus() == MemberPaypasswdStatus.NOT_SET_PAYPASSWD) {
			data.put("isSetPayPwd", "false");
		}

		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-setting-info", "response", restP);
	}

	/**
	 * 安全设置
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/securityInfo.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView securityInfo(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		PersonMember user = getUser(request);
		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getOperatorId());
		certRequest.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
		certRequest.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
		// 判读用户软证书是否激活
		RestResponse certResponse;
		try {
			certResponse = certificationService.getCertByCertStatus(certRequest, env);
			if (certResponse.isSuccess()) {
				data.put("soft", "true");// 已激活
			} else {
				data.put("soft", "false");// 其他
			}
		} catch (BizException e) {
			data.put("soft", "false");// 其他
		}

		// 是否设置支付密码
		if (user.getPaypasswdstatus() == MemberPaypasswdStatus.NOT_SET_PAYPASSWD) {
			data.put("isSetPayPwd", "false");
		}
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/security-info", "response", restP);
	}

	/**
	 * 添加银行卡主页
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/add-bankcard-index.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView addBankCardIndex(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.ADDBANKCARD.getMsg(), user, env));
		}		
		Map<String, Object> data = new HashMap<String, Object>();

		if (user.getMemberId().startsWith("2")) {
			if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
				restP.setMessage("实名认证后才可以添加银行卡！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
		}

		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());
		List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);

		if (!(list.size() < 20)) {
			restP.setMessage("最多添加20张银行卡！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}

		AuthInfoRequest authInfoReq = new AuthInfoRequest();
		authInfoReq.setMemberId(user.getMemberId());
		authInfoReq.setAuthType(AuthType.IDENTITY);
		authInfoReq.setOperator(user.getOperatorId());
		AuthInfo info = defaultCertService.queryRealName(authInfoReq);

		if (StringUtils.isEmpty(info.getAuthNo())) {
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.REAL_NAME);
			authInfoReq.setOperator(user.getOperatorId());
			info = defaultCertService.queryRealName(authInfoReq);
		}

		data.put("membertType", user.getMemberType().getCode());
		if (user.getMemberType().getCode().equals("1")) {
			data.put("realName", user.getMemberName());
		} else {
			if (user.getNameVerifyStatus() == AuthResultStatus.PASS) {
				data.put("isVerify", "true");
				String legalPerson = super.getEncryptInfo(request, DeciphedType.LEGAL_NAME, env);
				data.put("legalPerson", legalPerson);
			}
		}
		if (null != info) {
			data.put("authNo", StarUtil.mockCommon(info.getAuthNo()));
		}
		data.put("user", user);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/add-bankcard-index", "response", restP);
	}

	/**
	 * 添加银行卡信息
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/addBankCard.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse add(HttpServletRequest request, @Valid BankAccountForm form, BindingResult result)
			throws Exception {
		RestResponse restP = new RestResponse();
		try {
			String bankAccountNum = form.getBankAccountNum();

			PersonMember user = getUser(request);

			// // 查询绑定银行卡
			BankAccRequest accReq = new BankAccRequest();
			accReq.setMemberId(user.getMemberId());
			accReq.setClientIp(request.getRemoteAddr());
			accReq.setBankAccountNum(bankAccountNum);
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
			if ((list != null) && (list.size() > 0)) {
				restP.setMessage("对不起，此银行卡已绑定，请重新选择另一张!");
				restP.setSuccess(false);
				return restP;
			}

			try {
				CardBin cardBin = defaultPfsBaseService.queryByCardNo(form.getBankAccountNum(),
						CommonConstant.PERSONAL_APP_ID);
				if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
					restP.setMessage("提现银行卡不能是信用卡!");
					return restP;
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", form.getBankAccountNum());
				if (!defaultPfsBaseService.cardValidate(CommonConstant.PERSONAL_APP_ID, form.getBankCode(),
						form.getBankAccountNum())) {
					restP.setMessage("绑定银行卡卡号不正确!");
					return restP;
				}
			}

			BankAccRequest req = new BankAccRequest();
			req.setMemberId(user.getMemberId());
			req.setBankName(form.getBankName());
			req.setBankCode(form.getBankCode());
			req.setBranchName(form.getBranchName());
			req.setBranchShortName(form.getBranchShortName());
			req.setBranchNo(form.getBranchNo());
			req.setProvName(form.getProvince());
			req.setCityName(form.getCity());
			req.setCardType(Integer.valueOf(CardType.JJK.getCode()));
			req.setCardAttribute(Integer.valueOf(form.getCardAttribute()));

			req.setPayAttribute("normal");
			req.setRealName(form.getRealName());
			req.setBankAccountNum(form.getBankAccountNum());

			defaultBankAccountService.addBankAccount(req);
			user.setBankCardCount(user.getBankCardCount() + 1);

			restP.setSuccess(true);
			return restP;

		} catch (Exception e) {
			restP.setMessage(e.getMessage());
			return restP;
		}
	}

	/**
	 * 设置默认银行卡
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/setDefaultCard.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse setDefaultCard(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env) {
		RestResponse restP = new RestResponse();
		String bankCardId = request.getParameter("bankCardId");
		String defaultCardId = request.getParameter("defaultCardId");

		try {
			BankAccRequest req = new BankAccRequest();
			req.setMemberId(super.getMemberId(request));
			if ((defaultCardId != null) && !"".equals(defaultCardId)) {
				if (defaultCardId.equals(bankCardId)) {
					restP.setSuccess(true);
					return restP;
				} else {
                    Map<String, String> map = new HashMap<String, String>();
                    BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(defaultCardId);
                    if (StringUtil.isNotBlank(info.getExtention())) {
                        Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                                new TypeReference<Map<String, String>>() {
                                });
                        for (String key : extMap.keySet()) {
                            map.put(key, extMap.get(key));
                        }
                    }
                    map.put("isDefaultcard", "0");
	                
					req.setBankcardId(defaultCardId);
					req.setExtInfo(JSONObject.toJSONString(map));
					if (!defaultBankAccountService.updateDefaultAccount(req)) {
						throw new Exception("移除默认银行卡失败!");
					}
				}
			}
			
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
			
			req.setBankcardId(bankCardId);
			req.setExtInfo(JSONObject.toJSONString(map));
			if (!defaultBankAccountService.updateDefaultAccount(req)) {
				throw new Exception("添加默认银行卡失败!");
			}
			restP.setSuccess(true);

		} catch (Exception e) {
			restP.setMessage(e.getMessage());
			logger.error("", e);
		}

		return restP;

	}

	/**
	 * 银行卡管理
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/cardManage.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView cardManage(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();

		String bankCardId = request.getParameter("bankCardId");
		
		if(StringUtils.isBlank(bankCardId)){				
			return new ModelAndView("redirect:/my/bankCardInfo.htm");
		}		

		BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(bankCardId);

		data.put("info", info);
		
		String extention = info.getExtention();
		if (StringUtils.isNotBlank(extention)) {
			JSONObject obj = (JSONObject) JSONObject.parse(extention);
			if ((null != obj) && "1".equals(obj.get("isDefaultcard"))) {
				data.put("defaultCard", "true");
			}
		}

		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/card-manage", "response", restP);
	}

	/**
	 * 解除会员银行卡绑定
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/removeBankCard.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse removeBankCard(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env) {
		RestResponse restP = new RestResponse();

		PersonMember user = getUser(request);		
		if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.REMOVEBANKCARD.getMsg(), user, env));
		}
		if (user.getMemberId().startsWith("2")) {
			if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
				restP.setMessage("请先进行实名认证！");
				return restP;
			}
		}

		BankAccRequest req = new BankAccRequest();
		req.setBankcardId(request.getParameter("bankCardId"));
		req.setMemberId(super.getMemberId(request));
		try {
			defaultBankAccountService.removeBankAccount(req);
			restP.setSuccess(true);
		} catch (ServiceException e) {
			restP.setMessage("解除会员银行卡绑定失败!");
			logger.error("解除会员银行卡绑定，调用接口异常！");
		}
		return restP;

	}

    /**
     * 个人会员管理
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/accountManage.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView accountManage(HttpServletRequest request, HttpServletResponse resp,ModelMap model, OperationEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        setResponseValues(request, resp, model);
        PersonMember user = getUser(request);
        AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
        authVerifyInfo.setMemberId(user.getMemberId());
        List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
        Map<String, Object> data = new HashMap<String, Object>();
        for (int i = 0; i < infos.size(); i++) {
            authVerifyInfo = infos.get(i);
            if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("mobile", authVerifyInfo.getVerifyEntity());
            } else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
            	String email = authVerifyInfo.getVerifyEntity();
            	int index = email.indexOf("@");
    			data.put("email", StarUtil.hideStrBySym(email, 1, email.length()-index, 3));
            }
        }
        data.put("member", user);
		data.put("verify_name", user.getNameVerifyStatus().getCode());

		// 实名认证级别
		if (user.getMemberId().startsWith("1")) {
			data.put("memberType", "personal");
			data.put("certifyLevel", user.getCertifyLevel().getCode());
		}
		// 是否设置支付密码
		if (user.getPaypasswdstatus() == MemberPaypasswdStatus.NOT_SET_PAYPASSWD) {
			data.put("isSetPayPwd", "false");
		}
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/member/safetyIndex", "response",
            restP);
    }
    
    /**
     * 创建个人会员
     *
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/createMember.htm", method = { RequestMethod.POST, RequestMethod.GET })
    public ModelAndView ceateMember(HttpServletRequest req, HttpServletResponse resp,
                                    OperationEnvironment env) throws Exception {
        PersonMember user = getUser(req);
        try {
            RestResponse restP = new RestResponse();
            Map<String, Object> data = new HashMap<String, Object>();
            if ((user == null) || (user.getMemberId() != null)) {//用户已存在，不能再创建
                return new ModelAndView(ResourceInfo.ERROR.getUrl(), "response", restP);
            }
            //创建用户
            MemberAndAccount ma = defaultMemberService.createIntegratedPersonalMember(user, env);
            user.setDefaultAccountId(ma.getAccountId());
            user.setMemberId(ma.getMemberId());
            user.setOperatorId(ma.getOperatorId());
            data.put("userName", user.getPlateName());
            user.setMemberName(user.getPlateName());
            data.put("member", user);
//            session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
            String mobile = String.valueOf(req.getAttribute(CommonConstant.MEMBER_ACTIVE_MOBILE));
            data.put(CommonConstant.MEMBER_SET_STATUS, MemberSetStatus.ACTIVE.getCode());
            if ("Y".equals(mobile)) { //进入手机激活页面
                data.put("mobile", user.getMobile());
                restP.setData(data);
                return new ModelAndView(CommonConstant.URL_PREFIX + "/mobile/set-pay-passwd",
                    "response", restP);
            } else {
                //进入PC激活页面
                data.put("mobile", user.getMobileStar());
                data.put("salt", DEFAULT_SALT);
                restP.setData(data);
                return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember",
                    "response", restP);
            }
        } catch (Exception e) {
//            session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
            logger.error("创建会员失败,失败原因:{},用户信息：{}，{}", e.getMessage(), user, e);
            throw new ServiceException("创建会员失败,失败原因:" + e.getMessage());
        }
    }

    /**
     * PC激活个人会员
     *
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/active/avtiveMember.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView activeMember(HttpServletRequest req, HttpServletResponse resp,
                                     ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        PersonMember user = getUser(req);
        String backUrl = req.getParameter("backUrl");
        String message = req.getParameter("message");
        if (user == null) {
            return new ModelAndView(ResourceInfo.ERROR.getUrl(), "response", restP);
        } else if ((user.getStatus().getCode() == MemberStatus.DEFAULT.getCode())
                   && (user.getPaypasswdstatus().getCode() == MemberPaypasswdStatus.DEFAULT
                       .getCode())) {
            //已经激活，直接调整到首页
            return new ModelAndView("redirect:/my/home.htm");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Member: [" + user.getMemberId()
                        + " ] is not actived, go into active page!");
        }
        data.put(CommonConstant.MEMBER_SET_STATUS,
            req.getParameter(CommonConstant.MEMBER_SET_STATUS));
        data.put("mobile", user.getMobileStar());
        data.put("userName", user.getPlateName());
        data.put("email", user.getEmail());

        data.put("nologin", "Y");
        data.put("member", user);
        data.put("backUrl", backUrl);
        data.put("message", message);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember", "response",
            restP);
    }

    /**
     * 移动设备设置支付密码激活用户
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/active/mobile-setpaypasswd.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView setPayPasswdMobileIndex(HttpServletRequest request, ModelMap model)
                                                                                           throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        PersonMember user = getUser(request);
        if (user.getStatus().getCode() == MemberStatus.NOT_ACTIVE.getCode()) {
            data.put(CommonConstant.MEMBER_SET_STATUS, MemberSetStatus.ACTIVE.getCode());
        } else {
            data.put(CommonConstant.MEMBER_SET_STATUS, MemberSetStatus.SET_PASSWD.getCode());
        }
        data.put("mobile", user.getMobile());
        data.put("userName", user.getMemberName());
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/mobile/set-pay-passwd", "response",
            restP);
    }
    
    @RequestMapping("/appCenterTemp.htm")
	public ModelAndView appCenterTemp() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/temp");
		return mv;
	}

	@RequestMapping("/my/active/check-member-index.htm")
	public ModelAndView checkMemberInfoIndex(HttpServletRequest request) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		String model = request.getParameter("model");
		data.put("model", model);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/check-member-index", "response", restP);
	}

}
