package com.netfinworks.site.web.action.money;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSON;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.ValidUtils;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AccountTypeKind;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.KjtTransferForm;
import com.netfinworks.site.web.action.form.TransferVo;
import com.netfinworks.site.web.common.vo.Transfer;

/**
 * 保理转账
 * @author tangl
 * @date 2015-02-04
 * 
 */
@Controller
@RequestMapping("/factoringtransfer")
public class FactoringTransferAction extends BaseAction {
	@Autowired
	private PersonalTransferManager personalTransferManager;
	@Autowired
	private DefaultMemberService defaultMemberService;
	@Autowired
	private DefaultAccountService defaultAccountService;
	/**
	 * 跳转到转账页面
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping("/toTransfer.htm")
	public ModelAndView toKjtTransfer(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView(CommonConstant.URL_PREFIX + "/factoringtransfer/transfer-to");
		try {
			// 获取用户信息
			PersonMember user = getUser(request);
			// 验证用户
			this.validMember(user);
			
		} catch (Exception e) {
			String message = ValidUtils.isValidException(e) ? ValidUtils.restMsg(e.getMessage()) : "操作失败";
			mv.addObject("message", message);
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
		}
		
		return mv;
	}

	/**
	 * 确认转账 并获取跳转地址
	 * @param request
	 * @param session
	 * @param env
	 * @return
	 */
	@RequestMapping(value="/confirmTransfer.htm", method = RequestMethod.POST)
	public @ResponseBody RestResponse confirmTransfer(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		RestResponse restResponse = new RestResponse();
		try {
			TransferVo transferVo = getTransferVoFromRequest(request, session);
			List<Transfer> transferList = transferVo.getTranserfers();
			KjtTransferForm transForm = transferVo.getTransForm();
			
			// 获取付款人信息
			PersonMember user = this.getUser(request);
			this.validMember(user);
			
			// 获取付款人支付账号 
			MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			user.setAccount(account);
			ValidUtils.valid(account == null, "无法获取当前用户账号信息");
						
			// 组装和设置 transfer
			this.wrapTransAndTotalTransfer(transForm, transferList, env);		
			
			// 提交组装后信息 进行转账并获取跳转地址
			String url = personalTransferManager.applyTransferUrl(transferVo, user, TradeType.baoli_repayment, env);
			Assert.hasText(url, "url is null");
			
			restResponse.setRedirect(url);
			restResponse.setSuccess(true);
		} catch (Exception e) {
			logger.error("转账失败", e);

			String message = ValidUtils.isValidException(e) ? ValidUtils.restMsg(e.getMessage()) : "转账失败";
			restResponse.setMessage(message);
			restResponse.setRedirect(CommonConstant.URL_PREFIX + "/business-error");
			restResponse.setSuccess(false);
		}
		return restResponse;
	}
	@RequestMapping(value="/getEnteriseName.htm", method = RequestMethod.GET)
	public @ResponseBody RestResponse getEnteriseName(String accountNo, TradeEnvironment env) {
		RestResponse resp = new RestResponse();
		try {
			ValidUtils.valid(!StringUtils.isNotBlank(accountNo), "账号不能为空");
			
			Long accountTpye = AccountTypeKind.BAOLIHU_BASE_ACCOUNT.getCode();
			BaseMember member = defaultMemberService.getMemberAndAccount(accountNo,  accountTpye, env);
			ValidUtils.valid(member == null, "帐号[%s]不存在，请输入正确的帐号", accountNo);
			this.validEnterpriseUser(member, accountNo);
			this.validFactoringUser(member, accountNo);
			
			String targetName = defaultMemberService.getTargetAccountName(member, env);
			 
			Map<String, Object> data = new HashMap<String, Object>();		
			if (StringUtils.isNotBlank(targetName)) {
				data.put("targetName", targetName);
				resp.setSuccess(true);
			} else {
				data.put("targetName", "");
				resp.setMessage("企业名称为空");
				resp.setSuccess(false);
			}
			
			resp.setData(data);
			
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error("获取企业名称失败", e);
			if(ValidUtils.isValidException(e)) {
				resp.setMessage(ValidUtils.restMsg(e.getMessage()));
			} else {
				resp.setMessage("获取企业名称失败");
			}
	
			resp.setSuccess(false);
			
		}
		
		return resp;
	}
	/**
	 * 验证用户
	 * @param user
	 */
	private void validMember(BaseMember user) {
		ValidUtils.valid(user == null, "获取会员账户失败");
		ValidUtils.valid(user.isPersonal(), "个人账户不允许操作");

		boolean isNotRealNameAuth = (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode()));
		ValidUtils.valid(isNotRealNameAuth, "登陆账户未进行实名认证");
	}
	
	/**
	 * 1. 组装和设置 transfer, 统计总转账金额
	 * 2. 查询和设置保理账号
	 * @param transferList
	 * @param env
	 */
	private void wrapTransAndTotalTransfer(KjtTransferForm form, List<Transfer> transferList, TradeEnvironment env) {
		Assert.notEmpty(transferList, "transferList is null");
		
		int orderNo = 1;
		Money transTotalMoney = new Money("0");
		for (int i=0, size = transferList.size(); i < size; i++) {
			Transfer transfer = transferList.get(i);
			
			String contact = transfer.getContact(),
				   money   = transfer.getMoney();
			
			Money transMoney = new Money(money);
			ValidUtils.valid(transMoney.getAmount().doubleValue() <= 0, "转账金额必须大于0");
			
			Assert.hasText(contact, "contact is null");
			// 查询和设置保理账号
			this.queryAndSetFactoringAccount(transfer, env);
			
			transfer.setOrderNo(orderNo);
			transfer.setRemark(TradeType.baoli_repayment.getMessage());
			transTotalMoney.addTo(transMoney);
			
			orderNo++;
		}
		form.setTotalTransMoney(transTotalMoney.getAmount().toString());
	}
	/**
	 * 1.查询和设置保理账号
	 * 2.查询和设置转账名称
	 * @param transferList
	 * @param env
	 */
	private void queryAndSetFactoringAccount(Transfer transfer, TradeEnvironment env) {
		String contact = transfer.getContact();
		//查询收款人
		Long accountTpye = AccountTypeKind.BAOLIHU_BASE_ACCOUNT.getCode();
		BaseMember member = defaultMemberService.getMemberAndAccount(contact,  accountTpye, env);
		ValidUtils.valid(member == null, "用户:[%s], 不存在", contact);
		
		validEnterpriseUser(member, contact);
		validFactoringUser(member,contact);
		
		String transferName = defaultMemberService.getTargetAccountName(member, env);
		
		/**
		 * 设置 会员和转账名称
		 */
		transfer.setMember(member);
		transfer.setName(transferName);
    }
	
	private void validEnterpriseUser(BaseMember member, String contact) {
		boolean isPersonal = MemberType.PERSONAL.getCode().equals(member.getMemberType().getCode());
		ValidUtils.valid(isPersonal, "用户:[%s] 不是企业会员",contact);
	}
	private void validFactoringUser(BaseMember member, String contact) {
		MemberAccount account = member.getAccount();
		ValidUtils.valid(account == null, "用户:[%s],没有开通保理账户", contact);
	}

	/**
	 * 转换request 请求
	 * @param request
	 * @param session
	 * @return
	 */
	private TransferVo getTransferVoFromRequest(HttpServletRequest request, HttpSession session) {
		TransferVo transferVo = new TransferVo();
		String transferListStr = request.getParameter("transferList");
		
		List<Transfer> transferList = JSON.parseArray(transferListStr, Transfer.class);
		KjtTransferForm form = new KjtTransferForm();
		transferVo.setTranserfers(transferList);
		transferVo.setTransForm(form);
		return transferVo;
	}
	
}
