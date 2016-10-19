/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月8日
 */
package com.netfinworks.site.web.action.money;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AcqTradeType;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.DefaultPaymentService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.KjtTransferForm;
import com.netfinworks.site.web.common.constant.MemoType;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.common.vo.Transfer;
import com.netfinworks.site.web.util.ObjectUtils;
import com.netfinworks.site.web.util.TradeReqestUtil;
import com.netfinworks.ufs.client.UFSClient;

/**
 * 永达互联网金融账户转账
 * @author xuwei
 * @date 2014年7月8日
 */
@Controller
@RequestMapping("transfer")
public class KjtTransferAction extends BaseAction {
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
    @Resource(name = "auditService")
    private AuditServiceImpl auditService;
    
    @Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;
    
    @Resource(name="webResource")
    private WebDynamicResource webResource;
    
    @Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade payPartyFeeFacade;
    
    @Resource(name = "defaultTransferService")
    private DefaultTransferServiceImpl defaultTransferService;
    
    @Resource(name = "ufsClient")
    private UFSClient   ufsClient;
    
    @Resource(name = "fundsControlService")
    private FundsControlService fundsControlService;
    
    @Resource(name = "defaultPaymentService")
    private DefaultPaymentService defaultPaymentService;
    
    @Resource(name = "memberService")
    private MemberService commMemberService;
    
	/**
	 * 永达互联网金融转账
	 * @return
	 */
	@RequestMapping("/toTransferKjt.htm")
	public ModelAndView toKjtTransfer(HttpServletRequest request,
			HttpServletResponse resp, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 获取用户信息
		PersonMember user = getUser(request);
		
		// 实名认证
		if (user.getMemberId().startsWith("2")) {
			if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
				logger.error("登陆账户未进行实名认证");
				mv.addObject("message", "对不起，您的账户尚未实名认证，无法转账!");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
		}
		
		// 查询可用余额
		try {
			// 根据用户ID查询会员账户
			MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("account", account);
			
			String errorMsg = request.getParameter("errorMsg");
			if (StringUtils.isNotEmpty(errorMsg)) {
				mv.addObject("errorMsg", errorMsg);
			}
			
			mv.addObject("user", user);
		} catch (Exception e) {
			logger.error("查询可用余额、冻结资金失败", e);
		}
		
		mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-kjt");
		return mv;
	}
	
	/**
	 * 验证限额限次
	 * @param request
	 * @param cashingForm
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/validateLflt")
	public RestResponse validateLflt(HttpServletRequest request, KjtTransferForm form, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		
		// 获取用户信息
		PersonMember user = getUser(request);
		
		try {
			// 提现申请金额
			Money totalMoney = new Money(form.getTotalMoney());
			String payee = "";
			List<String> emailList = form.getEmailList();
			for(String email:emailList){
			    payee = commMemberService.queryMemberIntegratedInfo(email, env);
			    // 验证限额限次
	            if (!super.validateLflt(user.getMemberId(), payee, totalMoney, TradeType.TRANSFER, null, response, env)) {
	                logger.error("限额限次验证失败");
	                return response;
	            }
			}
		} catch (Exception e) {
			logger.error("检查限额限次异常", e);
			response.setMessage("您的余额不足！");
			return response;
		}
		
		response.setSuccess(true);
		return response;
	}
	
	/**
	 * 永达互联网金融转账确认
	 * @param form
	 * @param request
	 * @param session
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping("/toConfirmTransferKjt.htm")
	public ModelAndView toConfirmTransferKjt(KjtTransferForm form, HttpServletRequest request,
			HttpSession session, HttpServletResponse resp, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 校验提交参数
        String errorMsg = validator.validate(form);
        if (StringUtils.isNotEmpty(errorMsg)) {
            mv.addObject("message", errorMsg);
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
		
		// 获得用户信息
		PersonMember user = this.getUser(request);
		
		MemberAccount account = null;
		try {
			// 检查账户
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("user", user);
		} catch (Exception e) {
			logger.error("查询账户失败", e);
			mv.addObject("message", "您的账户未绑定银行卡");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		if (StringUtils.isEmpty(form.getTotalMoney())) {
			logger.error("转账金额输入有误");
			mv.addObject("message", "转账金额输入有误！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			// 检查余额是否足够
			Money avaBalance = account.getAvailableBalance();
			Money totalMoney = new Money(form.getTotalMoney());
			
			String payee = "";
            List<String> emailList = form.getEmailList();
            for(String email:emailList){
                payee = commMemberService.queryMemberIntegratedInfo(email, env);
                // 验证限额限次
                if (!super.validateLflt(user.getMemberId(), payee,totalMoney, TradeType.TRANSFER, mv, null, env)) {
                    mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                    return mv;
                }
            }
			
			mv.addObject("avaBalance", avaBalance);
		} catch (Exception e) {
			logger.error("转账金额输入有误");
			mv.addObject("message", "转账金额输入有误！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			// 生成转账信息
			List<Transfer> transferList = new ArrayList<Transfer>();
			int transferCount = generateTransferInfo(form, mv, transferList, env);
			
			// 把相关信息放到Session中
			this.setJsonAttribute(session, "kjtTransferForm", form);
			this.setJsonAttribute(session, "transferList", transferList);
			this.setJsonAttribute(session, "transferCount", transferCount);
		} catch (Exception e) {
			logger.error("生成转账信息失败", e);
			mv.addObject("message", "您的输入的转账信息不正确");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-confirm");
		return mv;
	}
	
	/**
	 * 生成转账信息
	 * @param form 表单对象
	 * @param mv 返回视图对象
	 * @param transferList 转账信息
	 * @return 生成转账记录条数
	 */
	private int generateTransferInfo(KjtTransferForm form, ModelAndView mv, List<Transfer> transferList,
			OperationEnvironment env) {
		List<String> emailList = form.getEmailList();
		List<String> moneyList = form.getMoneyList();
		int transferCount = 0;
		if (ObjectUtils.isListNotEmpty(emailList) && ObjectUtils.isListNotEmpty(moneyList)) {
			int size = emailList.size();
			int orderNo = 1;
			for (int i=0; i<size; i++) {
				String email = emailList.get(i);
				if (StringUtils.isEmpty(email)) {
					continue;
				}
				Transfer transfer = new Transfer();
				transfer.setOrderNo(orderNo);
				transfer.setContact(email);
				//查询收款人
		        BaseMember payeeUser = null;
				try {
					payeeUser = defaultMemberService.isMemberExists(email, SYSTEM_CODE, env);
					transfer.setName(getTargetAccountName(payeeUser, env));
				} catch (Exception e) {
					logger.error("查询收款人失败", e);
				}
				transfer.setMoney(moneyList.get(i));
				if (StringUtils.isEmpty(transfer.getRemark())) {
					if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
						form.setRemark(MemoType.getDesc(form.getRemarkType()));
						transfer.setRemark(MemoType.getDesc(form.getRemarkType()));
					} else {
						transfer.setRemark(form.getRemark());
					}
				}
				transferList.add(transfer);
				orderNo++;
			}
		}
		
		transferCount = transferList.size();
		mv.addObject("transferPerCount", transferCount);
		mv.addObject("transferList", transferList);
		mv.addObject("form", form);
		if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
			// 选择其他时，获取用户输入的备注
			form.setRemark(MemoType.getDesc(form.getRemarkType()));
		}
		
		return transferCount;
	}
	
	/**
	 * 确认永达互联网金融转账
	 * @param request
	 * @param env
	 * @return
	 */
	@RequestMapping("/confirmTransferKjt")
	public ModelAndView confirmTransferKjt(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 获得用户信息
		PersonMember user = this.getUser(request);
		if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.RANSFERKJT.getMsg(), user, env));
		}
		
 		// 从会话中获取转账信息
 		KjtTransferForm form = this.getJsonAttribute(session, "kjtTransferForm", KjtTransferForm.class);
		List<Transfer> transferList = getTransferList(this.getJsonAttribute(session, "transferList", List.class));
		Integer transferCount = this.getJsonAttribute(session, "transferCount", Integer.class);
        
		String totalMoney = form.getTotalMoney();
		if (StringUtils.isEmpty(totalMoney)) {
			logger.error("转账总金额为0");
			mv.addObject("message", "转账金额不能为0！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
 		// 跳转收银台
		try {
			String url = this.applyTransferUrl(transferList, form, user, transferCount, env);
			if (StringUtils.isEmpty(url)) {
				throw new Exception("您输入的转账信息有误！");
			}
			mv.setViewName("redirect:" + url);
		} catch (Exception e) {
			logger.error("转账失败", e);
			mv.addObject("message", e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		return mv;
	}
	
	/**
	 * 提交所有的转账
	 * @param transferList
	 * @param form
	 * @param user
	 * @param transferCount
	 * @param env
	 * @return
	 */
	private String applyTransferUrl(List<Transfer> transferList, KjtTransferForm form, 
			PersonMember user, int transferCount, TradeEnvironment env) throws Exception {
		if (ObjectUtils.isListEmpty(transferList)) {
			logger.debug("转账的目标账户不存在");
			throw new Exception("转账的目标账户不存在");
		}
		
		List<String> voucherNoList = new ArrayList<String>();
		int size = transferList.size();
		for (int i=0; i<size; i++) {
			Transfer transfer = transferList.get(i);
			//查询收款人
	        BaseMember payeeUser = null;
			try {
				payeeUser = defaultMemberService.isMemberExists(transfer.getContact(), SYSTEM_CODE, env);
			} catch (Exception e) {
				logger.error("收款人不存在", e);
				throw new Exception("收款人不存在");
			}
			
	        //生成收款方
            logger.info("转账,生成收款方");	
            PartyInfo payee = new PartyInfo();
            payee.setAccountId(payeeUser.getDefaultAccountId());
            payee.setMemberId(payeeUser.getMemberId());
            payee.setOperatorId(payeeUser.getCurrentOperatorId());
            payee.setIdentityNo(transfer.getContact());
            payee.setMemberName(payeeUser.getMemberName());
            payee.setMemberType(payeeUser.getMemberType());

            //生成付款方
            PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
                user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
                user.getMemberName(),user.getMemberType());
            payer.setAccountName(user.getLoginName());
//            payer.setEnterpriseName(user.getEnterpriseName());
            logger.info("转账,生成付款方");

            //生成交易请求
            TradeRequestInfo tradeReqest = TradeReqestUtil.createTransferTradeRequest(payer, payee,
        		new Money(transfer.getMoney()), transfer.getRemark(), new Money(), 
        		form.getSendNoteMsg() == 0 ? FALSE_STRING : TRUE_STRING, TradeType.TRANSFER);
            tradeReqest.setMemo(transfer.getRemark());
            env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm?link=0101");
            logger.info("转账,生成交易请求");
            
            //封装客户端信息
            try {
				BeanUtils.copyProperties(tradeReqest, env);
			} catch (Exception e) {
				logger.error("获取交易环境信息失败", e);
				throw new Exception("获取交易环境信息失败");
			}
            
            // 生成转账记录
            try {
            	defaultTransferService.etransfer(tradeReqest, TradeType.TRANSFER);
            	voucherNoList.add(tradeReqest.getTradeVoucherNo());
			} catch (BizException e) {
				logger.error("转账到账户{}的操作失败", transfer.getContact(), e);
				throw new Exception("转账失败");
			}
		}
		
		return defaultPaymentService.applyPayment(user, voucherNoList, AcqTradeType.INSTANT_TRASFER, env);
	}
	
	/**
	 * 查询银行转账的服务费用
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/queryServiceCharge", method = RequestMethod.POST)
	public RestResponse queryServiceCharge(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		String money = request.getParameter("money");
		logger.debug("money={}", money);
		
		getServiceFee(request, money, TradeType.TRANSFER.getBizProductCode(), response);
		return response;
	}
	
	/**
	 * 解析Transfer的JSON字符串列表
	 * @param list
	 * @return
	 */
	private List<Transfer> getTransferList(List<?> list) {
		if (ObjectUtils.isListEmpty(list)) {
			return null;
		}
		
		List<Transfer> transferList = new ArrayList<Transfer>();
		for(Object object : list) {
			String jsonStr = String.valueOf(object);
			Transfer transfer = JSONArray.parseObject(jsonStr, Transfer.class);
			transferList.add(transfer);
		}
		
		return transferList;
	}
	
	/**
	 * 获取目标账户名称
	 * @param baseMember 会员对象
	 * @return 会员名称
	 */
	private String getTargetAccountName(BaseMember member, OperationEnvironment env) {
	    if ((member == null) || StringUtils.isEmpty(member.getMemberId())) {
	        return StringUtils.EMPTY;
	    }
	    
	    if (MemberType.PERSONAL.equals(member.getMemberType())) {
	        // 个人会员直接取会员名称
	        return member.getMemberName();
	    } else {
	        // 商户和企业取企业名称
	        EnterpriseMember enterpriseMember = new EnterpriseMember();
	        enterpriseMember.setMemberId(member.getMemberId());
	        CompanyMemberInfo compInfo = null;
	        try {
	            compInfo = defaultMemberService.queryCompanyInfo(enterpriseMember, env);
	        } catch (ServiceException e) {
	            logger.error("查询账户信息失败", e);
	        }
	        if (compInfo != null) {
	            return compInfo.getCompanyName();
	        }
	    }
	    
		return StringUtils.EMPTY;
	}
	
}
