package com.netfinworks.site.web.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.recharge.facade.api.RechargeQueryServiceFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.enums.AccountTypeKind;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.FormatForDate;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.client.authapi.domain.User;

/**
 * <p>首页</p>
 * @author eric
 * @version $Id: IndexAction.java, v 0.1 2013-7-5 下午1:29:44  Exp $
 */
@Controller
public class IndexAction extends BaseAction {

    @Resource(name = "defaultTradeQueryService")
    private DefaultTradeQueryService defaultTradeQueryService;

    @Resource(name = "webResource")
    private WebDynamicResource webResource;

    @Resource(name="accountService")
    private AccountService accountService;

    @Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "certificationService")
	private CertificationService certificationService;

	@Resource(name = "defaultFundoutService")
	private DefaultFundoutService defaultFundoutService;

	@Resource(name = "defaultDepositInfoService")
	private DefaultDepositInfoService defaultDepositInfoService;
	
	@Resource(name = "rechargeQueryServiceFacade")
    private RechargeQueryServiceFacade rechargeQueryServiceFacade;
	
	@Resource(name = "maQueryService")
	private MaQueryService maQueryService;
	
    /**
     * 进入首页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/index.htm", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest req, HttpServletResponse resp, OperationEnvironment env) throws Exception {
         //1.获取user
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        User vfSsoUser = null;
        try {
            vfSsoUser = VfSsoUser.get();
        } catch (Exception e) {
            //获取单点登录用户信息失败，直接跳转至登录页面
            data.put("salt", DEFAULT_SALT);
            String redirect_url = req.getParameter("returnUrl");
            if(StringUtils.isBlank(redirect_url)) {
				redirect_url = req.getParameter("redirect_url");
			}
            String cashier_memberType = req.getParameter("cashier_memberType");
            data.put("returnUrl", redirect_url);
            data.put("cashier_memberType", cashier_memberType+"");
            restP.setData(data);
            return new ModelAndView(CommonConstant.URL_PREFIX +  ResourceInfo.INDEX.getUrl(), "response", restP);
        }
        PersonMember user = getUser(req);//获取本地session的用户信息
        if(logger.isInfoEnabled()) {
            logger.info("Member go into the index page!{}", user);
        }
        data.put("salt", DEFAULT_SALT);
        restP.setData(data);
        if(user != null) {
        	if((vfSsoUser != null) &&"login".equals(vfSsoUser.getSessionStatus())){
                data.put("mobile", user.getMobileStar());
                data.put("member", user);
				return new ModelAndView("redirect:/my/home.htm");
            }else{
                //用户被踢出，清掉用户本地session
                req.getSession().removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER_ID);
                req.getSession().removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_USER_LOGINNAME);
                req.getSession().removeAttribute(CommonConstant.KJT_PERSON_USER_NAME);
                req.getSession().removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
            }
        }
        
        String redirect_url = req.getParameter("returnUrl");
		if(StringUtils.isBlank(redirect_url)) {
			redirect_url = req.getParameter("redirect_url");
		}
        String cashier_memberType = req.getParameter("cashier_memberType");
        data.put("returnUrl", redirect_url);
        data.put("cashier_memberType", cashier_memberType+"");
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX +  ResourceInfo.INDEX.getUrl(), "response", restP);
    }
    /**
     * 进入主页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/home.htm", method = RequestMethod.GET)
    public ModelAndView home(HttpServletRequest req, HttpServletResponse resp, OperationEnvironment env,HttpSession session) throws Exception {
        //1.获取user
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        PersonMember user = getUser(req);
        
        String userType = null;
        String accountType = null;
        try {
//            userType = accountService.queryUserType(user.getMemberId());//根据memberId查询企业账户是0:供应商  1:核心企业  -1:普通企业
            accountType = accountService.queryAccountType(user.getMemberId(), env);
//            session.setAttribute("userType", userType);
            session.setAttribute("accountType", accountType);
        }
        catch (Exception e) {
            logger.info("根据memberId查询企业账户类型失败,memberId="+user.getMemberId());
            logger.error("错误信息",e);
        }
        data.put("userType", userType);   
        data.put("accountType", accountType); 
        if(logger.isInfoEnabled()) {
            logger.info("Member go into the home page!{}", user);
        }
		try {
			user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
		} catch (BizException e) {
			logger.error(e.getMessage());
		}
		
		MemberInfo memberInfo = maQueryService.queryMemberIntegratedInfo(user.getLoginName(), "" + 1);//根据登录名查出所有帐号信息
        List<com.netfinworks.ma.service.response.AccountInfo> accounts = memberInfo.getAccounts();
        String baoLiAccountId = null;
        for(com.netfinworks.ma.service.response.AccountInfo account:accounts){
            if(account.getAccountType().equals(AccountTypeKind.BAOLIHU_BASE_ACCOUNT.getCode())){
                baoLiAccountId=account.getAccountId();
            }
        }
        MemberAccount baoliaccount = new MemberAccount();
        if(baoLiAccountId != null){
            baoliaccount = accountService.queryAccountById(baoLiAccountId, env);
            data.put("baoliaccount", baoliaccount);
        }
        
		// 查询个人会员实名认证等级
		if (user.getMemberId().startsWith("1")) {
			data.put("membertype", "personal");
			data.put("certifylevel", user.getCertifyLevel().getCode());
		}

        // 查询绑定银行卡
 		BankAccRequest reqAcc = new BankAccRequest();
 		reqAcc.setMemberId(user.getMemberId());
 		reqAcc.setClientIp(req.getRemoteAddr());
		List<BankAccountInfo> list = null;
		try {
			list = defaultBankAccountService.queryBankAccount(reqAcc);
			user.setBankCardCount(list.size());
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
		data.put("list", list);
		data.put("member", user);
		data.put("authRemind", req.getSession().getAttribute("authRemind"));
		try {
			restP = defaultMember(restP, data, user, env);
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
        return new ModelAndView(CommonConstant.URL_PREFIX + "/home", "response", restP);
    }

    /**
     * 查询最近交易记录
     *
     * @param restP
     * @param user
     * @param data
     * @param member
     * @param env
     * @return
     * @throws ServiceException
     */
	private RestResponse defaultMember(RestResponse restP, Map<String, Object> data,
			PersonMember member, OperationEnvironment env) throws ServiceException {

		List<TradeInfo> list = new ArrayList<TradeInfo>();

		// 分页信息
		QueryBase queryBase = new QueryBase();
		queryBase.setCurrentPage(1);

		FormatForDate formatForDate = new FormatForDate();

        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setMemberId(member.getMemberId());
		tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-30));
		tradeRequest.setGmtEnd(new Date());
		tradeRequest.setQueryBase(queryBase);

        PageResultList page = defaultTradeQueryService.queryTradeList(tradeRequest, env);

		if (null != page.getInfos()) {
			list.addAll(page.getInfos());
		}

		// 查询出款记录
		FundoutQuery fundoutQuery = new FundoutQuery();
		fundoutQuery.setMemberId(member.getMemberId());
		fundoutQuery.setAccountNo(member.getDefaultAccountId());
		fundoutQuery.setCurrentPage(1);
		fundoutQuery.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));
		fundoutQuery.setOrderTimeEnd(new Date());
		PageResultList page2 = null;
		List<Fundout> fundoutList = null;
		try {
			page2 = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
			fundoutList = page2.getInfos();
		} catch (ServiceException e) {
			logger.error("首页出款记录查询异常", e);
			return restP;
		}
		if (null != fundoutList) {
			for (Fundout fundout : fundoutList) {
				TradeInfo info = new TradeInfo();
				info.setGmtSubmit(fundout.getOrderTime());
				info.setTradeVoucherNo(fundout.getFundoutOrderNo());
				info.setPayAmount(fundout.getAmount());
				info.setPayeeFee(fundout.getFee());
				info.setStatus(fundout.getStatus());
				info.setBuyerId(member.getMemberId());
				if (fundout.getProductCode().equals(TradeType.WITHDRAW.getBizProductCode())) {
					info.setTradeMemo("普通提现");
				} else if (fundout.getProductCode().equals(TradeType.WITHDRAW_INSTANT.getBizProductCode())) {
					info.setTradeMemo("快速提现");
				} else if (fundout.getProductCode().equals(TradeType.PAY_TO_BANK.getBizProductCode())) {
					info.setTradeMemo("转账到卡(T+N)");
				} else if (fundout.getProductCode().equals(TradeType.PAY_TO_BANK_INSTANT.getBizProductCode())) {
					info.setTradeMemo("转账到卡(实时)");
				} else if (fundout.getProductCode().equals(TradeType.PAYOFF_TO_BANK.getBizProductCode())) {
					info.setTradeMemo("代发工资到银行卡");
				}
				list.add(info);
			}
		}

		// 查询充值记录
		DepositListRequest dRequest = new DepositListRequest();
		dRequest.setMemberId(member.getMemberId());
		dRequest.setAccountNo(member.getDefaultAccountId());
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(1);
		dRequest.setPageInfo(pageInfo);
		dRequest.setRequestId(System.currentTimeMillis() + member.getMemberId());
		dRequest.setTimeBegin(DateUtil.getDateNearCurrent(-30));
		dRequest.setTimeEnd(new Date());
		PageResultList page3 = null;
		List<DepositBasicInfo> depositInfoList = null;
		try {
			page3 = defaultDepositInfoService.queryList(dRequest, env);
			depositInfoList = page3.getInfos();
		} catch (ServiceException e) {
			logger.error("首页充值记录查询异常", e);
			return restP;
		}
		if (null != depositInfoList) {
			for (DepositBasicInfo depositInfo : depositInfoList) {
				TradeInfo info = new TradeInfo();
				info.setGmtSubmit(depositInfo.getGmtPaySubmit());
				info.setTradeVoucherNo(depositInfo.getPaymentVoucherNo());
				info.setTradeMemo("充值");
				info.setPayAmount(depositInfo.getAmount());
				info.setStatus(depositInfo.getPaymentStatus());
				info.setSellerId(member.getMemberId());
				list.add(info);
			}
		}
		
//		//查询话费充值记录
//		RechargeQueryRequest query = new RechargeQueryRequest();
//		query.setMemberId(member.getMemberId());
//		query.setStartDate(formatForDate.getFirstDayOfMonth());
//		query.setEndDate(new Date());
//		query.setCurrentPage(1);
//		query.setPageSize(20);
//		RechargeQueryResponse rechargeResponse = rechargeQueryServiceFacade.query(query);
//		List<RechargeOrder> rechargeList=rechargeResponse.getRechargeOrderList();
//		if(null !=rechargeList){
//		    for(RechargeOrder recharge : rechargeList){
//		        TradeInfo info = new TradeInfo();
//		        info.setGmtSubmit(recharge.getGmtCreate());
//		        info.setTradeVoucherNo(recharge.getTradeOrderNo());
//		        info.setTradeMemo("话费充值");
//		        info.setPayAmount(recharge.getPayAmount());
//		        if(("F001").equals(recharge.getPayStatus())){
//		            info.setStatus("1");
//		        }else if(("F002").equals(recharge.getPayStatus()) && !("F002").equals(recharge.getRechargeStatus())){
//		            info.setStatus("2");
//		        }else if(("F002").equals(recharge.getPayStatus()) && ("F002").equals(recharge.getRechargeStatus())){
//		            info.setStatus("3");
//		        }else if(("F003").equals(recharge.getPayStatus())){
//		            info.setStatus("4");
//		        }
//		        info.setBuyerId(member.getMemberId());
//		        list.add(info);
//		    }
//		}
		Collections.sort(list, new Comparator<TradeInfo>() {
			@Override
			public int compare(TradeInfo b1, TradeInfo b2) {
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					Date d1 = sf.parse(sf.format(b1.getGmtSubmit()));
					Date d2 = sf.parse(sf.format(b2.getGmtSubmit()));
					return d2.compareTo(d1);
				} catch (ParseException e) {
					logger.error(e.getMessage());
				}
				return -1;
			}
		});

		if (list.size() > 20) {
			list = list.subList(0, 20);
		}

        data.put("mobile", member.getMobileStar());
        data.put("member", member);
		data.put("pageList", list);
        
		data.put("verify_name", member.getNameVerifyStatus().getCode());

		String loginName = member.getLoginName();
		Pattern pattern = Pattern.compile(CommonConstant.PATTERN_MOBILE);
		Matcher matcher = pattern.matcher(loginName);
		if (matcher.matches()) {
			data.put("loginName", StarUtil.mockMobile(loginName));
		} else {
			data.put("loginName", StarUtil.mockEmail(loginName));
		}


		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(member.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				data.put("verify_mobile", authVerifyInfo.getVerifyEntity());
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				data.put("verify_email", authVerifyInfo.getVerifyEntity());
			}
		}

		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		certRequest.setMemberId(member.getMemberId());
		certRequest.setOperatorId(member.getOperatorId());
		certRequest.setCertificationType(CertificationType.SOFT_CERTIFICATION.getCode());
		certRequest.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
		// 判读用户证书是否激活
		RestResponse certResponse;
		try {
			certResponse = certificationService.getCertByCertStatus(certRequest, env);
			if (certResponse.isSuccess()) {
				data.put("cert", "true");// 已激活
			} else {
				data.put("cert", "false");// 其他
			}
		} catch (BizException e) {
			data.put("cert", "false");// 其他
		}

        restP.setData(data);
        return restP;
    }
	
	@RequestMapping(value = "/static/{name}.htm", method = RequestMethod.GET)
	public ModelAndView staticUrl(@PathVariable String name, HttpServletResponse resp, ModelMap model)
			throws Exception {
		return new ModelAndView(CommonConstant.URL_PREFIX + "/static/" + name);
	}
	
	@RequestMapping(value = "/help/{name}.htm", method = RequestMethod.GET)
	public ModelAndView helpUrl(@PathVariable String name, HttpServletResponse resp, ModelMap model) throws Exception {

		return new ModelAndView(CommonConstant.URL_PREFIX + "/help/" + name);
	}
	
	/**
	 * 实名认证提醒
	 * 
	 * @param req
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/my/authRemind.htm", method = RequestMethod.GET)
	@ResponseBody
	public RestResponse authRemind(HttpServletRequest req) throws BizException {
		RestResponse restP = new RestResponse();

		req.getSession().setAttribute("authRemind", "false");

		restP.setSuccess(true);

		return restP;
	}

}
