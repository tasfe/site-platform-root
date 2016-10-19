package com.yongda.site.ext.service.facade.personal.impl;


import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.fos.service.facade.FundoutFacade;
import com.netfinworks.fos.service.facade.request.FundoutRequest;
import com.netfinworks.fos.service.facade.response.FundoutResponse;
import com.netfinworks.ma.service.base.model.Account;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.ma.service.base.model.Operator;
import com.netfinworks.ma.service.facade.IAccountFacade;
import com.netfinworks.ma.service.facade.IBankAccountFacade;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.request.AccountRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.response.AccountResponse;
import com.netfinworks.ma.service.response.BankAccountInfoResponse;
import com.netfinworks.ma.service.response.BaseMemberInfo;
import com.netfinworks.ma.service.response.MemberIntegratedResponse;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.ext.integration.fundout.convert.FundoutConvertor;
import com.netfinworks.site.ext.integration.member.BankAccountService;
import com.netfinworks.site.ext.integration.member.convert.MemberConvert;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.netfinworks.voucher.common.utils.JsonUtils;
import com.yongda.site.service.personal.facade.request.PersonalTradeRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;
import com.yongda.site.service.personal.facade.response.PersonalTradeResponse;
/**
 * 提现申请提交
 * @author yp
 *
 */
public class PersonalSubmitWithdrawService extends AbstractRoutService<PersonalTradeRequest, BaseResponse> {

	@Resource
    private VoucherService voucherService;
	
	@Resource
    private FundoutFacade fundoutFacade;
	
	@Resource(name = "accountFacade")
    private IAccountFacade accountFacade;
	
	@Resource(name = "bankAccountFacade")
    private IBankAccountFacade bankAccountFacade;
	
	@Resource(name = "memberFacade")
	private IMemberFacade memberFacade;
	
	
	/**日志**/
	private Logger logger = LoggerFactory
			.getLogger(PersonalSubmitWithdrawService.class);
	
	@Override
	public String getRoutName() {
		
		return "submit_withdraw";
	}

	@Override
	public PersonalTradeRequest buildRequest(Map<String, String> paramMap) {
		PersonalTradeRequest req = HttpRequestConvert
				.convert2Request(paramMap,PersonalTradeRequest.class);

		return req;
	}

	@Override
	public BaseResponse doProcess(PersonalTradeRequest req) {
		PersonalTradeResponse pResp = new PersonalTradeResponse();
		BaseResponse bresp = new BaseResponse();
		/**校验参数**/
        if(req.getAmount()==null){
        	bresp.setErrorMessage("amount不能为空");
        	bresp.setErrorCode("ILLEGAL_ARGUMENT");
        	return bresp;
        }else if(req.getBankcardId()==null){
        	bresp.setErrorMessage("bankcardId不能为空");
        	bresp.setErrorCode("ILLEGAL_ARGUMENT");
        	return bresp;
        }else if(req.getClientIp()==null){
        	bresp.setErrorMessage("clientIp不能为空");
        	bresp.setErrorCode("ILLEGAL_ARGUMENT");
        	return bresp;
        }else if(req.getType()==null){
        	bresp.setErrorMessage("type不能为空");
        	bresp.setErrorCode("ILLEGAL_ARGUMENT");
        	return bresp;
        }else if(!validate(req.getAmount())){//金额格式校验
        	bresp.setErrorCode("ILLEGAL_ARGUMENT");
        	bresp.setErrorMessage("金额格式不正确");
        	return bresp;
        }
		
		try {
            long beginTime = 0L;
            //请求类型转换
            TradeRequestInfo treq = service(req);
            //查询账户的可提现余额
            AccountRequest qreq = new AccountRequest();
            qreq.setMemberId(req.getMemberId());
            Long aType = 101L;
            qreq.setAccountType(aType);
            AccountResponse response = accountFacade.queryAccountByMemberId(null,qreq);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询会员余额， 耗时:{} (ms); 响应结果:{} ",
                        new Object[] { consumeTime, response });
            }
            if(!ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())){
            	bresp.setErrorCode(response.getResponseCode());
            	bresp.setErrorMessage(response.getResponseMessage());
            	return bresp;
            }
            Money balance = null;
            String accountId = null;
            for(Account accounts:response.getAccounts()){
               if(accounts.getAccountType().equals(aType)){
                   balance = accounts.getWithdrawBalance();
                   accountId = accounts.getAccountId();
                   treq.getPayer().setAccountId(accountId);
               }
            }
//            balance = treq.getAmount();//测试数据
            Money amount = treq.getAmount();
            //判断提现金额是否大于可提现余额
            if(amount.compareTo(balance)==1){
            	logger.info("提现金额{}大于提现余额{}",amount,balance);
            	bresp.setErrorCode(CommonDefinedException.INSUFFICIENT_BALANCE.getErrorCode());
            	bresp.setErrorMessage(CommonDefinedException.INSUFFICIENT_BALANCE.getErrorMsg());
                return bresp;
            }
            BankAcctDetailInfo bankCard = new BankAcctDetailInfo();
            
            BankAccountInfoResponse bankResponse = bankAccountFacade.queryBankAccountDetail(null, req.getBankcardId());
                
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询银行卡信息， 耗时:{} (ms); 响应结果:{} ",
                new Object[] { consumeTime, response });
            }
            if (ResponseCode.SUCCESS.getCode().equals(bankResponse.getResponseCode())) {
                bankCard = bankResponse.getBankAcctInfo();
            }else if (ResponseCode.NO_QUERY_RESULT.getCode().equals(bankResponse.getResponseCode())) {
            	bresp.setErrorCode(CommonDefinedException.USER_BANKCARD_NOT_EXIST.getErrorCode());
            	bresp.setErrorMessage(CommonDefinedException.USER_BANKCARD_NOT_EXIST.getErrorMsg());
                return bresp;
            } else{
            	bresp.setErrorCode(bankResponse.getResponseCode());
            	bresp.setErrorMessage(bankResponse.getResponseMessage());
                return bresp;
            }
            //生成收款方
            PartyInfo payee = new PartyInfo();
            payee.setAccountName(bankCard.getRealName());
            payee.setIdentityNo(bankCard.getBankAccountNumMask());
            treq.setPayee(payee);
            //提现方会员号与银行卡绑定的会员必须一致
            if(!bizValidate(treq, bankCard)){
            	bresp.setErrorCode(CommonDefinedException.MEMBERID_NOTEQUALS_BANKCARDMEMBERID.getErrorCode());
            	bresp.setErrorMessage(CommonDefinedException.MEMBERID_NOTEQUALS_BANKCARDMEMBERID.getErrorMsg());
                return bresp;	
            }
                	
            //落地交易凭证
            String voucherNo = voucherService.recordTradeVoucher(treq);
            treq.setTradeVoucherNo(voucherNo);
            treq.setClientId("site");
            //出款服务转换
            FundoutRequest request = FundoutConvertor.convert(treq, bankCard);
            //生成支付凭证号开始
            treq.setTradeExtension(JsonUtils.toJson(request));
            String paymentVoucherNo = voucherService.recordPaymentVoucher(treq);
            request.setPaymentOrderNo(paymentVoucherNo);
            //生成支付凭证号结束
            if (logger.isInfoEnabled()) {
                logger.info("提交申请提现，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            FundoutResponse resp = fundoutFacade.submit(request, new OperationEnvironment());
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程提交申请提现， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
            if (ResponseCode.APPLY_SUCCESS.getCode().equals(resp.getReturnCode())
                || resp.getReturnCode().startsWith("S")) {
            	bresp.setSuccess(true);
            	pResp.setAmount(req.getAmount().toString());
            	pResp.setFundout_order_no(paymentVoucherNo);
            	pResp.setType(req.getType());
            	
            } else {
                logger.info("远程提交申请提现，响应结果为失败:{},{}", resp.getReturnCode(), resp.getReturnMessage());
                bresp.setErrorCode(resp.getReturnCode());
                bresp.setErrorMessage(resp.getReturnMessage());
                pResp.setFundout_order_no(paymentVoucherNo);
            	pResp.setType(req.getType());
            }
            bresp.setData(pResp);
        } catch (Exception e) {
            logger.error("提交申请提现异常:请求信息{},异常信息:{}", req, e);
            bresp.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
        	bresp.setErrorMessage(CommonDefinedException.SYSTEM_ERROR.getErrorMsg());
        }
		return bresp;
	}
	private TradeRequestInfo service(PersonalTradeRequest req) {
		TradeRequestInfo tReq = new TradeRequestInfo();
		
		MemberIntegratedIdRequest request = MemberConvert
					.createMemberIntegratedIdRequest(req.getMemberId());
		MemberIntegratedResponse response = memberFacade
					.queryMemberIntegratedInfoById(null, request);
		
		BaseMemberInfo member = response.getBaseMemberInfo();
		Operator operator = response.getDefaultOperator();
		//生成付款方
		PartyInfo payer = new PartyInfo();
		payer.setMemberId(req.getMemberId());
		payer.setMemberType(MemberType.getByCode(member.getMemberType()));
		payer.setOperatorId(operator.getOperatorId());
		tReq.setPayer(payer);
		tReq.setClientIp(req.getClientIp());
        //提现类型
        if(req.getType().equals("1")){
        	tReq.setTradeType(TradeType.WITHDRAW);
        }else if(req.getType().equals("0")){
        	tReq.setTradeType(TradeType.WITHDRAW_INSTANT);
        }
        //提现金额
        Money amount = new Money();
		amount.setAmount(req.getAmount());
        tReq.setAmount(amount);
        
		return tReq;
	}

	private boolean validate(String money) {
        //提现金额验证
		String reg="^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$";
		return money.matches(reg);
    }

    private boolean bizValidate(TradeRequestInfo req, BankAcctDetailInfo card) {
        Assert.notNull(card);
        String memberId = req.getPayer().getMemberId();
        String cardMemberId = card.getMemberId();
        return memberId.equals(cardMemberId);
    }
}
