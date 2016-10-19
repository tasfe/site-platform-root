package com.netfinworks.site.web.action.recharge;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.trade.impl.DefaultRechargeServiceImpl;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.LogUtil;


/**
 *
 * <p>充值action</p>
 * @author Guan Xiaoxu
 * @version $Id: RechargeAction.java, v 0.1 2013-11-27 上午10:59:31 Guanxiaoxu Exp $
 */
@Controller
@RequestMapping("/recharge")
public class RechargeAction extends BaseAction {
    @Resource(name="defaultRechargeService")
    private DefaultRechargeServiceImpl defaultRechargeService;

    @Resource(name="webResource")
    private WebDynamicResource webResource;
    
    @Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;
    
	@Resource(name = "defaultFundoutService")
	private DefaultFundoutService defaultFundoutService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Resource(name = "defaultDepositInfoService")
	private DefaultDepositInfoService defaultDepositInfoService;
	
	@Resource(name="accountService")
    private AccountService accountService;
	
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
    
    /**
     * 充值第一步，进入输入金额页面
     * @param request
     * @return
     */
	@RequestMapping(value = "/toInputMoney")
	public ModelAndView toInputMoney(HttpServletRequest request, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		EnterpriseMember user = getUser(request);
		
		// 实名认证
		if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
			logger.error("登陆账户未进行实名认证");
			mv.addObject("message", "对不起，您的账户尚未实名认证，无法充值!");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		logger.info(LogUtil.generateMsg(OperateTypeEnum.RECHARGE, getUser(request), env, ""));
		
		env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");
		String url = "";
		try {
			url = defaultRechargeService.applyRecharge(user, env);
		} catch (BizException e) {
			logger.error("查询充值收银台URL失败", e);
		}
		
		mv.setViewName("redirect:" + url);
		return mv;
	};
	
	/**
	 * 充值第二步，进入选择银行页面
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/toChooseBank-del", method = RequestMethod.POST)
	public ModelAndView toChooseBank(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		
		// 传递输入金额
		Money money = new Money(request.getParameter("money"));
		logger.info("输入金额: " + money);
		mav.addObject("money", money);
		
		// 查询永达互联网金融合作银行
		try {
			List<BankCard> allBankList = defaultPfsBaseService.queryBank(CropOrPersonal.COMPANY);
			List<BankCard> bankList = allBankList.subList(0, 7);
			mav.addObject("bankList", bankList);
		} catch (ServiceException e) {
			logger.error("查询永达互联网金融合作银行失败" , e);
		}
		
		mav.setViewName("/money/recharge2");
		return mav;
	};
	
	@RequestMapping(value = "/queryMoreBanks")
	public ModelAndView queryMoreBanks() {
		ModelAndView mav = new ModelAndView();
		
		// 查询永达互联网金融合作银行
		try {
			List<BankCard> bankList = defaultPfsBaseService.queryBank(CropOrPersonal.COMPANY);
			mav.addObject("bankList", bankList);
		} catch (ServiceException e) {
			logger.error("查询永达互联网金融合作银行失败" , e);
		}
		
		mav.setViewName("/money/recharge-more-banks");
		
		return mav;
	}

    /**
     * 充值跳转收银台
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/toChooseBank", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request, boolean error, 
    		ModelMap model,TradeEnvironment env)  throws Exception{
        EnterpriseMember user = getUser(request);
        env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");
        String url = defaultRechargeService.applyRecharge(user, env);
        return new ModelAndView("redirect:" + url);
    }
    
	/**
	 * 查询钱包用户的充值记录
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/money/queryRechargeRecord.htm")
	public ModelAndView queryRechargeRecord(HttpServletRequest request, HttpServletResponse resp, 
			boolean error, ModelMap model, OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(request);
		}
		RestResponse restP = new RestResponse();
		String currentPage = request.getParameter("currentPage");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));
		DepositListRequest dRequest = new DepositListRequest();
		dRequest.setMemberId(user.getMemberId());
		dRequest.setAccountNo(user.getDefaultAccountId());
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.valueOf(currentPage));
		dRequest.setPageInfo(pageInfo);
		dRequest.setRequestId(System.currentTimeMillis() + user.getMemberId());
		if (StringUtils.isNotBlank(startDate)) {
			dRequest.setTimeBegin(DateUtils.parseDate(startDate + " 00:00:01"));
		} else {
			dRequest.setTimeBegin(DateUtil.getDateNearCurrent(-30));
			startDate = DateUtils.getDateString();

		}
		if (StringUtils.isNotBlank(endDate)) {
			dRequest.setTimeEnd(DateUtils.parseDate(endDate + " 23:59:59"));
		} else {
			dRequest.setTimeEnd(new Date());
			endDate = DateUtils.getDateString();
		}
		PageResultList<?> page = defaultDepositInfoService.queryList(dRequest, env);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("page", page);
		map.put("mobile", user.getMobileStar());
		map.put("member", member);
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("pageReqMapping", "/my/all-recharge.htm");
		restP.setData(map);
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/list/recharge-list", "response", restP);

	}

}
