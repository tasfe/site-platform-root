package com.yongda.site.ext.service.facade.personal.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;
import com.netfinworks.site.domainservice.trade.DefaultRechargeService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.service.personal.facade.request.QueryRechargeCashierRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public class QueryRechargeCashierUrl extends AbstractRoutService<QueryRechargeCashierRequest,BaseResponse>{

	@Resource(name="defaultRechargeService")
	private DefaultRechargeService defaultRechargeService;
	
	@Resource(name="memberService")
	private MemberService memberService;
	
	@Resource(name="accountService")
	private AccountService accountService;
	
	@Override
	public String getRoutName() {
		return "query_recharge_cashier_url";
	}

	@Override
	public QueryRechargeCashierRequest buildRequest(Map<String, String> paramMap)
			throws Exception {
		QueryRechargeCashierRequest req = HttpRequestConvert.convert2Request(paramMap, QueryRechargeCashierRequest.class);
		if(StringUtils.isBlank(req.getMemberId())){
			CommonException e = new CommonException(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),"会员ID不能为空");
			throw  e;
		}
		if(!StringUtils.equals("WAP", req.getChannalType()) && !StringUtils.equals("WEB", req.getChannalType())){
			CommonException e = new CommonException(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),"channal必须是WAP/WEB");
			throw  e;
		}
		return req;
	}

	@Override
	public BaseResponse doProcess(QueryRechargeCashierRequest req)
			throws Exception {
		BaseResponse resp = new BaseResponse();
		TradeEnvironment tradeEnv = new TradeEnvironment();
		tradeEnv.setClientIp("127.0.0.1");
		tradeEnv.setSuccessDispalyUrl(req.getReturnUrl());//充值成功返回首页
        BaseMember baseMember = memberService.queryMemberById(req.getMemberId(), new OperationEnvironment());
        Assert.isTrue(baseMember != null,"会员不存在");
        if(baseMember == null){
        	resp.setSuccess(false);
        	resp.setErrorCode(CommonDefinedException.MEMBER_ID_NOT_EXIST.getErrorCode());
        	resp.setErrorMessage(CommonDefinedException.MEMBER_ID_NOT_EXIST.getErrorMsg());
        	return resp;
        }
        MemberAccount  account = accountService.queryAccountByMemberId(baseMember.getMemberId(), baseMember.getMemberType().getBaseAccount(), new OperationEnvironment());
        baseMember.setDefaultAccountId(account.getAccountId());
        String url = defaultRechargeService.applyRecharge(baseMember, tradeEnv,req.getChannalType());
		Map<String,String> map=new HashMap<String,String>();
		map.put("url", url);
		resp.setData(map);
		return resp;
	}
}
