package com.netfinworks.site.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.pfs.service.basis.branchinfo.BranchQueryByNoRequest;
import com.netfinworks.pfs.service.basis.branchinfo.BranchQueryFacade;
import com.netfinworks.pfs.service.basis.common.QueryResult;
import com.netfinworks.pfs.service.basis.domain.BranchInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.bank.BankBranchShort;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.CardType;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.pfs.convert.PfsConvert;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.BankAccountForm;
import com.netfinworks.site.web.util.LogUtil;

/**
 * 
 * <p>
 * 完善银行卡信息
 * </p>
 * 
 * @author Guan Xiaoxu
 * @version $Id: BankAccountAction.java, v 0.1 2014-1-13 上午9:31:48 Guanxiaoxu
 *          Exp $
 */
@Controller
public class BankAccountAction extends BaseAction {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	 @Resource(name = "branchQueryFacade")
	   private BranchQueryFacade     branchQueryFacade;
	 

	/**
	 * 添加银行卡信息
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/finishBankAccount.htm", method = RequestMethod.POST)
	public ModelAndView update(HttpServletRequest request,
			@Valid BankAccountForm form, BindingResult result) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> errorMap = null;
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);
		if (result.hasErrors()) {
			List<ObjectError> list = result.getAllErrors();
			Map<String, String> errors = initError(list);
			restP.setErrors(errors);
			logger.error("表单校验失败！");
			restP.setData(data);
			return new ModelAndView("redirect:/my/withdraw.htm", "response",
					restP);
		}
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setCardAttribute(Integer.parseInt(form.getCardAttribute()));
		req.setPayAttribute("normal");

		Map<String, String> map = defaultPfsBaseService.queryCityInfos(true, false, form.getProvName());
		req.setProvName(retProCityMap(form.getProvName()));
		req.setCityName(map.get(form.getCityName()));
		req.setCardType(form.getCardType());
		req.setBankcardId(request.getParameter("showBankCardId"));
		String branch = form.getBranchCode();
		if (branch.contains("@")) {
			String[] info = branch.split("@");
			req.setBranchNo(info[0]);
			req.setBranchName(info[1]);
		}
		defaultBankAccountService.updateBankAccount(req);
		// System.out.println("访问参数：" + req.toString());
		return new ModelAndView("redirect:/my/withdraw.htm");
	}

	/**
	 * 添加银行卡信息
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/improveBankAccount.htm", method = RequestMethod.POST)
	public ModelAndView updateCardInfo(HttpServletRequest request,
			@Valid BankAccountForm form, BindingResult result) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> errorMap = null;
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);
		if (result.hasErrors()) {
			List<ObjectError> list = result.getAllErrors();
			Map<String, String> errors = initError(list);
			restP.setErrors(errors);
			logger.error("表单校验失败！");
			restP.setData(data);
			return new ModelAndView("redirect:/my/withdraw.htm", "response",
					restP);
		}
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setCardAttribute(Integer.parseInt(form.getCardAttribute()));
		req.setPayAttribute("normal");

		Map<String, String> map = defaultPfsBaseService.queryCityInfos(true, false, form.getProvName());
		req.setProvName(retProCityMap(form.getProvName()));
		req.setCityName(map.get(form.getCityName()));
		req.setCardType(form.getCardType());
		req.setBankcardId(request.getParameter("bankCardId"));
		String branch = form.getBranchCode();
		if (branch.contains("@")) {
			String[] info = branch.split("@");
			req.setBranchNo(info[0]);
			req.setBranchName(info[1]);
		}
		defaultBankAccountService.updateBankAccount(req);
		// System.out.println("完善卡信息入口访问参数：" + req.toString());
		return new ModelAndView("redirect:/my/accountManage.htm");
	}

	/**
	 * 添加银行卡主页
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/add-bank-index.htm")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env, ModelMap model)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();

		EnterpriseMember user = getUser(request);
		if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
			restP.setMessage("实名认证后才可以添加银行卡！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}

		BankAccRequest reqAcc = new BankAccRequest();
		reqAcc.setMemberId(user.getMemberId());
		reqAcc.setClientIp(request.getRemoteAddr());
		List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(reqAcc);
		if (list.size() >= 8) {
			restP.setMessage("最多可以添加8张银行卡！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}

		CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(super.getUser(request), env);
		data.put("username", compInfo.getCompanyName());
		restP.setData(data);
		if (user.getNameVerifyStatus().equals("PASS")) {
			return new ModelAndView(CommonConstant.URL_PREFIX + "/member/addBankCard", "response", restP);
		} else {
			// return new ModelAndView("redirect:/my/card-manage-index.htm");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/member/addBankCard", "response", restP);
		}

	}

	/**
	 * 添加银行卡信息
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/addBankAccount.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse add(HttpServletRequest request, @Valid BankAccountForm form, BindingResult result, OperationEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		try {
			String bankAccountNum = form.getBankAccountNum();

			EnterpriseMember user = getUser(request);
			
            logger.info(LogUtil.generateMsg(OperateTypeEnum.BIND_ACCOUNT, user, env,
                    StringUtils.EMPTY));

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
						CommonConstant.ENTERPRISE_APP_ID);
				if (cardBin != null) {
					if(!Dbcr.DC.getCode().equals(cardBin.getCardType())){
						restP.setMessage("提现银行卡不能是信用卡!");
						return restP;
					}
					if(!StringUtils.equals(form.getBankCode(), cardBin.getBankCode())){
						restP.setMessage("绑定银行卡卡号不正确!");
						return restP;
					}
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", form.getBankAccountNum());
				if (!defaultPfsBaseService.cardValidate(CommonConstant.ENTERPRISE_APP_ID, form.getBankCode(),
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
			req.setProvName(form.getProvName());
			req.setCityName(form.getCityName());
			req.setCardType(Integer.valueOf(CardType.JJK.getCode()));
			req.setCardAttribute(0);
			req.setPayAttribute("normal");
			req.setRealName(form.getRealName());
			req.setBankAccountNum(form.getBankAccountNum());

			String operType = request.getParameter("operType");
			String oldBankCardId = request.getParameter("oldBankCardId");

			if ((operType != null) && operType.equals("update")) {
				req.setBankcardId(oldBankCardId);
				defaultBankAccountService.updateBankAccount(req);
			} else {
				defaultBankAccountService.addBankAccount(req);
				user.setBankCardCount(user.getBankCardCount() + 1);
			}

			restP.setSuccess(true);
			return restP;

		} catch (Exception e) {
			log.error(e.getMessage(), e.getCause());
			restP.setMessage(e.getMessage());
			return restP;
		}
	}

	/**
	 * 查询银行名字(吴永飞)
	 * 
	 * @param request
	 * @return
	 * @throws BindException
	 */
	@RequestMapping(value = "/my/querybankname.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody RestResponse queryBankName(HttpServletRequest request, OperationEnvironment env)
			throws Exception {
		Map<String,Object> data = new HashMap<String,Object>();
		RestResponse restP = new RestResponse();
		String branchNo = request.getParameter("branchCode");
		
		try {

			BranchQueryByNoRequest bqbnr = new BranchQueryByNoRequest();
			bqbnr.setBranchNo(branchNo);
			QueryResult<BranchInfo> qresult = branchQueryFacade.queryByNo(bqbnr);
			List<BankBranch> branchs = PfsConvert.convertBranch(qresult.getResults());
		
			if (logger.isInfoEnabled()) {
				logger.info("查询分行的结果：{},{}", branchs != null ? branchs.size()
						: 0, branchs);
			}
			String BranchshortName = null;
			if((branchs != null) && (branchs.size() > 0)){
				 BranchshortName = branchs.get(0).getBranchShortName();
			}
			data.put("branchshortname",BranchshortName);
			restP.setSuccess(true);
			restP.setData(data);
			return restP;
		} catch (Exception e) {
			log.error(e.getMessage(), e.getCause());
			restP.setMessage(e.getMessage());
			restP.setSuccess(false);
			restP.setData(data);
			return restP;
		}
	}

	/**
	 * 查询省市县、银行、实名信息
	 * 
	 * @param request
	 * @return
	 * @throws BindException
	 */
	@RequestMapping(value = "/my/queryProv.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody
	RestResponse queryProc(HttpServletRequest request, OperationEnvironment env)
			throws Exception {

		RestResponse restP = new RestResponse();
		String brankId = request.getParameter("brankId");
		Map<String, Object> data = new HashMap<String, Object>();
		EnterpriseMember user = getUser(request);
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());

		try {
			// 查询城市信息
			List<Province> list = defaultPfsBaseService.queryProvInfos(true,
					false);
			data.put("province", list);
			List<BankCard> banks = defaultPfsBaseService
					.queryBank(CropOrPersonal.PERSONAL);
			List<BankAccountInfo> brankList = defaultBankAccountService
					.queryBankAccount(req);
			for (BankAccountInfo bankAccountInfo : brankList) {
				if (bankAccountInfo.getBankcardId().equals(brankId)) {
					data.put("userName", bankAccountInfo.getRealName());
					data.put("brandAmountNum",
							bankAccountInfo.getBankAccountNumMask());
					data.put("bankName", bankAccountInfo.getBankName());
					data.put("bankCode", bankAccountInfo.getBankCode());
					data.put("bankCardId", bankAccountInfo.getBankcardId());

				}
			}

			// 查询银行信息
			data.put("bank", banks);
			restP.setSuccess(true);
			restP.setData(data);
			return restP;
		} catch (ServiceException e) {
			log.error(e.getMessage(), e.getCause());
			restP.setSuccess(false);
			restP.setMessage(e.getMessage());
			return restP;
		}
	}

	/**
	 * 查询根据省查询市信息
	 * 
	 * @param request
	 * @return
	 * @throws BindException
	 */
	@RequestMapping(value = "/my/queryCity.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody
	RestResponse queryCity(HttpServletRequest request) throws Exception {

		String provId = request.getParameter("provId");
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			// 查询城市信息
			List<Province> list = defaultPfsBaseService.queryProvInfos(true,
					false);
			for (Province p : list) {
				if (p.getProvId() == Long.valueOf(provId)) {
					data.put("city", p.getCityInfos());
				}
			}
			restP.setData(data);
			return restP;
		} catch (ServiceException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e.getCause());
			restP.setSuccess(false);
			restP.setMessage(e.getMessage());
			return restP;
		}
	}

	/**
	 * 查询分行信息
	 * 
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/queryBranch.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody
	RestResponse queryBranch(HttpServletRequest request) {

		RestResponse restP = new RestResponse();
		try {
			String bankCode = request.getParameter("bankCode");
			// String provId = request.getParameter("provId");
			String cityId = request.getParameter("cityId");
			Map<String, Object> data = new HashMap<String, Object>();
			if (logger.isInfoEnabled()) {
				logger.info("查询分行的处理后的信息,银行：{}，城市：{}", bankCode, cityId);
			}
			List<BankBranch> branchs = defaultPfsBaseService.queryBranch(CommonConstant.ENTERPRISE_APP_ID, bankCode,
					Long.valueOf(cityId));
			
			if (logger.isInfoEnabled()) {
				logger.info("查询分行的结果：{},{}", branchs != null ? branchs.size()
						: 0, branchs);
			}
			List<BankBranchShort> list = new ArrayList<BankBranchShort>();
			if ((branchs != null) && (branchs.size() > 0)) {
				for (BankBranch branch : branchs) {
					BankBranchShort bshort = new BankBranchShort();
					bshort.setId(branch.getBranchId());
					bshort.setName(branch.getBranchName());
					bshort.setNo(branch.getBranchNo());
					bshort.setsName(branch.getBranchShortName());
					list.add(bshort);
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("查询分行的结果：{},{}", list.size(), list);
			}
			data.put("branch", list);
			data.put("total", branchs != null ? branchs.size() : 0);
			restP.setData(data);
			return restP;
		} catch (NumberFormatException e) {
			restP.setSuccess(false);
			return restP;
		} catch (ServiceException e) {
			restP.setSuccess(false);
			return restP;
		}
	}

	public String retProCityMap(String id) {
		Map<String, String> map = new HashMap<String, String>();
		List<Province> list = null;
		try {
			list = defaultPfsBaseService.queryProvInfos(true, false);
			for (Province pr : list) {
				map.put(String.valueOf(pr.getProvId()), pr.getProvName());
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map.get(id);
	}

}
