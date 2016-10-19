package com.netfinworks.site.web.action;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.DateUtils;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.FormatForDate;
import com.netfinworks.ufs.client.UFSClient;
import com.netfinworks.ufs.client.domain.FileNameInfo;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.client.authapi.domain.User;

/**
 * <p>
 * 首页
 * </p>
 *
 * @author eric
 * @version $Id: IndexAction.java, v 0.1 2013-7-5 下午1:29:44 Exp $
 */
@Controller
public class IndexAction extends BaseAction {

    @Resource(name = "webResource")
    private WebDynamicResource        webResource;

    @Resource(name = "tradeService")
    private TradeService              tradeService;

    @Resource(name = "accountService")
    private AccountService            accountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

	@Resource(name = "certificationService")
	private CertificationService certificationService;

	@Resource(name = "defaultTradeQueryService")//pos
	private DefaultTradeQueryService defaultTradeQueryService;
	
	@Resource(name = "defaultFundoutService")
	private DefaultFundoutService defaultFundoutService;

	@Resource(name = "defaultDepositInfoService")
	private DefaultDepositInfoService defaultDepositInfoService;
	
	@Resource(name = "ufsClient")
	private UFSClient   ufsClient;
	
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
    /**
     * 进入首页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/index.htm")
    public ModelAndView enter(HttpServletRequest req, HttpServletResponse resp,
                              OperationEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        EnterpriseMember user = getUser(req);
        User vfSsoUser = null;
        try {
            vfSsoUser = VfSsoUser.get();
        } catch (Exception e) {
            //logger.error("",e);
            String redirect_url = req.getParameter("returnUrl");
            if (StringUtils.isBlank(redirect_url)) {
				redirect_url = req.getParameter("redirect_url");
			}
            String cashier_memberType = req.getParameter("cashier_memberType");
            data.put("returnUrl", redirect_url);
            data.put("cashier_memberType", cashier_memberType + "");
            restP.setData(data);
			return new ModelAndView("index", "response", restP);
        }
        if ((null != user) && "login".equals(vfSsoUser.getSessionStatus())
				&& CommonConstant.USERTYPE_MERCHANT.equals(vfSsoUser.getUserType())) {
            return new ModelAndView("redirect:/my/home.htm");
        }
        String redirect_url = req.getParameter("returnUrl");
        if (StringUtils.isBlank(redirect_url)) {
			redirect_url = req.getParameter("redirect_url");
		}
        String cashier_memberType = req.getParameter("cashier_memberType");
        data.put("returnUrl", redirect_url);
        data.put("cashier_memberType", cashier_memberType + "");
        restP.setData(data);
		return new ModelAndView("index", "response", restP);
    }

	/**
	 * 进入首页
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/login.htm")
	public ModelAndView login(HttpServletRequest req, HttpServletResponse resp, OperationEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		EnterpriseMember user = getUser(req);
		User vfSsoUser = null;
		try {
			vfSsoUser = VfSsoUser.get();
		} catch (Exception e) {
			// logger.error("",e);
			String redirect_url = req.getParameter("returnUrl");
			if (StringUtils.isBlank(redirect_url)) {
				redirect_url = req.getParameter("redirect_url");
			}
			String cashier_memberType = req.getParameter("cashier_memberType");
			data.put("returnUrl", redirect_url);
			data.put("cashier_memberType", cashier_memberType + "");
			restP.setData(data);
			return new ModelAndView("login", "response", restP);
		}
		if ((null != user) && "login".equals(vfSsoUser.getSessionStatus())
				&& CommonConstant.USERTYPE_ENTERPRISE.equals(vfSsoUser.getUserType())) {
			return new ModelAndView("redirect:/my/home.htm");
		}
		String redirect_url = req.getParameter("returnUrl");
		if (StringUtils.isBlank(redirect_url)) {
			redirect_url = req.getParameter("redirect_url");
		}
		String cashier_memberType = req.getParameter("cashier_memberType");
		data.put("returnUrl", redirect_url);
		data.put("cashier_memberType", cashier_memberType + "");
		restP.setData(data);
		return new ModelAndView("login", "response", restP);
	}

    /**
     * 进入主页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/home.htm", method = { RequestMethod.POST, RequestMethod.GET })
    public ModelAndView home(HttpServletRequest req, HttpServletResponse resp,
                             OperationEnvironment env) throws Exception {
        // 1.获取user
        HttpSession session = req.getSession();
        RestResponse restP = new RestResponse();
        HashMap<String, Object>  data = new HashMap<String, Object>();
        restP.setData(data);
        EnterpriseMember user = getUser(req);
        String[] last_login_info= getLastLoginInfo(req,user.getMemberId(),user.getOperatorId());
        if(StringUtils.isNotBlank(last_login_info[1]))
        	data.put("lastLoginTime", new Date(Long.parseLong(last_login_info[1])));
        else
        	data.put("lastLoginTime", new Date());
        data.put("lastLoginIp", last_login_info[0]);
        logger.info("session object CURRENT_ENTERPRICE_USER_ID:"
                    + session
                        .getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER_ID));
        if (logger.isInfoEnabled()) {
            logger.info("Member goes into my home page, {}", user);
        }
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));

		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(user.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				restP.getData().put("verify_mobile", authVerifyInfo.getVerifyEntity());
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				restP.getData().put("verify_email", authVerifyInfo.getVerifyEntity());
			}
		}
		restP.getData().put("verify_name", user.getNameVerifyStatus().getCode());

		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getCurrentOperatorId());
		certRequest.setCertificationType(CertificationType.HARD_CERTIFICATION.getCode());
		certRequest.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
		// 判读用户证书是否激活
		RestResponse certResponse = certificationService.getCertByCertStatus(certRequest, env);
		if (certResponse.isSuccess()) {
			restP.getData().put("cert", "true");// 已激活
		} else {
			restP.getData().put("cert", "false");// 其他
		}

        // 查询绑定银行卡
        BankAccRequest reqAcc = new BankAccRequest();
        reqAcc.setMemberId(user.getMemberId());
        reqAcc.setClientIp(req.getRemoteAddr());
        List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(reqAcc);
        user.setBankCardCount(list.size());
        restP.getData().put("mobile", user.getMobileStar());
        restP.getData().put("member", user);
        
        try{
	        //查询昨天是否有对账单
	        String yestday = DateUtils.formatDateToString(DateUtils.getYesterday(),"yyyy.MM.dd");
	        String[] dataArr = yestday.split("\\.");
	        restP.getData().put("yesterday", yestday);
	        String dayFilePath = "batchTask"+ File.separator + user.getMemberId() + File.separator + dataArr[0] + ""
					+ dataArr[1] + File.separator+ "day" +File.separator + dataArr[0] + "" + dataArr[1] + ""
					+ dataArr[2];
			List<FileNameInfo> filelist = ufsClient.list(dayFilePath);
			if (filelist != null && !filelist.isEmpty())
				restP.getData().put("yesterday_settlefile", "/my/"+dataArr[0]+"/"+dataArr[1]+"/"+dataArr[2]+"/settlement-bill-download.htm");
        }catch(Exception e){
        	logger.error("查询昨天对账单失败", e);
        }
        
		try {
			// 根据用户ID查询会员账户
			MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			restP.getData().put("account", account);
		} catch (Exception e) {
			logger.error("查询可用余额、冻结资金失败", e);
		}
		
        // 页面请求Mapping
        restP.getData().put("pageReqMapping", "/my/home.htm");
        restP = defaultMember(req, restP, user, env);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/index", "response", restP);
    }

    /**
     * zhangyun.m 查询企业最近所有交易
     *
     * @param restP
     * @param user
     * @param data
     * @param member
     * @param env
     * @return
     * @throws ServiceException
     * @throws BizException
     */
    private RestResponse defaultMember(HttpServletRequest req, RestResponse restP,
                                       EnterpriseMember member, OperationEnvironment env)
                                                                                         throws ServiceException {
		List<BaseInfo> list = new ArrayList<BaseInfo>();
		// 分页
        QueryBase queryBase = new QueryBase();
        String currentPage = req.getParameter("currentPage");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        queryBase.setCurrentPage(Integer.valueOf(currentPage));
		FormatForDate formatForDate = new FormatForDate();
		// 查询交易记录
		CoTradeRequest request = new CoTradeRequest();
        request.setQueryBase(queryBase);
        request.setMemberId(member.getMemberId());
		request.setBeginTime(DateUtil.getDateNearCurrent(-30));
        request.setEndTime(new Date());
        request.setNeedSummary(false);
        List<String> productCodes = new ArrayList<String>();
        productCodes.add(TradeType.PAY_INSTANT.getBizProductCode());
        productCodes.add(TradeType.phone_recharge.getBizProductCode());
        productCodes.add(TradeType.PAY_ENSURE.getBizProductCode());
        productCodes.add(TradeType.bank_withholding.getBizProductCode());
        productCodes.add(TradeType.TRANSFER.getBizProductCode());
        productCodes.add(TradeType.COLLECT_TO_KJT.getBizProductCode());
        productCodes.add(TradeType.baoli_loan.getBizProductCode());
        productCodes.add(TradeType.baoli_withholding.getBizProductCode());
        productCodes.add(TradeType.POS.getBizProductCode());
        request.setProductCodes(productCodes);
		CoTradeQueryResponse rep = null;
        try {
            rep = tradeService.queryList(request);
        } catch (BizException e) {
			logger.error("首页交易记录查询异常", e);
			return restP;
		}
		if (null != rep.getBaseInfoList()) {
			for (BaseInfo baseInfo : rep.getBaseInfoList()) {
				if (baseInfo.getBizProductCode().equals(TradeType.PAYOFF_TO_KJT.getBizProductCode())) {
					baseInfo.setTradeType("代发工资到账户");
				}else if (baseInfo.getBizProductCode().equals(TradeType.COLLECT_TO_KJT.getBizProductCode())) {
					baseInfo.setTradeType("委托付款到永达账户");
				}else if (baseInfo.getBizProductCode().equals(TradeType.baoli_loan.getBizProductCode())) {
					baseInfo.setTradeType("保理放贷");
				}else if (baseInfo.getBizProductCode().equals(TradeType.baoli_withholding.getBizProductCode())) {
					baseInfo.setTradeType("保理代扣");
				}else if (baseInfo.getBizProductCode().equals(TradeType.POS.getBizProductCode())) {
					baseInfo.setTradeType("pos消费");
				}
				list.add(baseInfo);
			}
		}
		
		//查询云+撤销
		TradeRequest postradeRequest = new TradeRequest();
		postradeRequest.setMemberId(member.getMemberId());
		postradeRequest.setSellerId(member.getMemberId());
		QueryBase queryBase12 = new QueryBase();
		queryBase12.setCurrentPage(Integer.valueOf(currentPage));
		postradeRequest.setQueryBase(queryBase12);
		postradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-30));
		postradeRequest.setGmtEnd(new Date());
		
		List<String> pos=new ArrayList<String>();
		pos.add(TradeType.POS.getBizProductCode());
		postradeRequest.setProductCodes(pos);
		PageResultList pospage = null;
		List<TradeInfo> refundList = null;
		try {
			pospage = defaultTradeQueryService.queryRefundList(
					postradeRequest, env);
			refundList = pospage.getInfos();
		} catch (ServiceException e) {
			logger.error("首页pos撤销记录查询异常", e);
			return restP;
		}
		if (null != refundList) {
			for (TradeInfo refund : refundList) {
				BaseInfo info = new BaseInfo();
				info.setGmtSubmit(refund.getGmtSubmit());
				info.setTradeVoucherNo(refund.getTradeVoucherNo());
				info.setPayAmount(refund.getPayAmount());
				info.setPayeeFee(refund.getPayeeFee());
				info.setOrderState(refund.getStatus());
				info.setBuyerId(member.getMemberId());
				info.setSerialNumber(refund.getTradeSourceVoucherNo());
				if (refund.getBizProductCode().equals(TradeType.POS.getBizProductCode())) {
					info.setTradeType("pos撤销");
				}
				list.add(info);
			}
		}
	
		
		// 查询出款记录
		FundoutQuery fundoutQuery = new FundoutQuery();
		fundoutQuery.setMemberId(member.getMemberId());
		fundoutQuery.setAccountNo(member.getDefaultAccountId());
		fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
		fundoutQuery.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));
		fundoutQuery.setOrderTimeEnd(new Date());
		
		fundoutQuery.setProductCode(TradeType.PAY_TO_BANK.getBizProductCode()
                + ","
                + TradeType.PAY_TO_BANK_INSTANT.getBizProductCode()
                + ","
                + TradeType.auto_fundout.getBizProductCode()
                + ","
                + TradeType.WITHDRAW.getBizProductCode()
                + ","
                + TradeType.WITHDRAW_INSTANT.getBizProductCode()
                + ","
                + TradeType.COLLECT_TO_BANK.getBizProductCode()
		        );
		PageResultList page = null;
		List<Fundout> fundoutList = null;
		try {
			page = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
			fundoutList = page.getInfos();
		} catch (ServiceException e) {
			logger.error("首页出款记录查询异常", e);
			return restP;
		}
		if (null != fundoutList) {
			for (Fundout fundout : fundoutList) {
				BaseInfo info = new BaseInfo();
				info.setGmtSubmit(fundout.getOrderTime());
				info.setTradeVoucherNo(fundout.getFundoutOrderNo());
				info.setPayAmount(fundout.getAmount());
				info.setPayeeFee(fundout.getFee());
				info.setOrderState(fundout.getStatus());
				info.setBuyerId(member.getMemberId());
				info.setSerialNumber(fundout.getOutOrderNo());
				if (fundout.getProductCode().equals(TradeType.WITHDRAW.getBizProductCode())) {
					info.setTradeType("普通提现");
				} else if (fundout.getProductCode().equals(TradeType.WITHDRAW_INSTANT.getBizProductCode())) {
					info.setTradeType("快速提现");
				} else if (fundout.getProductCode().equals(TradeType.PAY_TO_BANK.getBizProductCode())) {
					info.setTradeType("转账到卡(T+N)");
				} else if (fundout.getProductCode().equals(TradeType.PAY_TO_BANK_INSTANT.getBizProductCode())) {
					info.setTradeType("转账到卡(实时)");
				} else if (fundout.getProductCode().equals(TradeType.PAYOFF_TO_BANK.getBizProductCode())) {
					info.setTradeType("代发工资到银行卡");
				} else if (fundout.getProductCode().equals(TradeType.COLLECT_TO_BANK.getBizProductCode())) {
					info.setTradeType("委托付款到银行卡");
				}
				list.add(info);
			}
		}
		// 查询充值记录
		DepositListRequest dRequest = new DepositListRequest();
		dRequest.setMemberId(member.getMemberId());
		dRequest.setAccountNo(member.getDefaultAccountId());
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.valueOf(currentPage));
		dRequest.setPageInfo(pageInfo);
		dRequest.setRequestId(System.currentTimeMillis() + member.getMemberId());
		dRequest.setTimeBegin(DateUtil.getDateNearCurrent(-30));
		dRequest.setTimeEnd(new Date());
		PageResultList page2 = null;
		List<DepositBasicInfo> depositInfoList = null;
		try {
			page2 = defaultDepositInfoService.queryList(dRequest, env);
			depositInfoList = page2.getInfos();
		} catch (ServiceException e) {
			logger.error("首页充值记录查询异常", e);
			return restP;
		}
		if (null != depositInfoList) {
			for (DepositBasicInfo depositInfo : depositInfoList) {
				BaseInfo info = new BaseInfo();
				info.setGmtSubmit(depositInfo.getGmtPaySubmit());
				info.setTradeVoucherNo(depositInfo.getPaymentVoucherNo());
				info.setTradeType("充值");
				info.setPayAmount(depositInfo.getAmount());
				info.setOrderState(depositInfo.getPaymentStatus());
				info.setSellerId(depositInfo.getMemberId());
				list.add(info);
			}
		}
		Collections.sort(list,new Comparator<BaseInfo>(){  
			@Override
			public int compare(BaseInfo b1, BaseInfo b2) {
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
		restP.getData().put("list", list);
		restP.getData().put("memberId", member.getMemberId());
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

}
