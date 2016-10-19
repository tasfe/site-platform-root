package com.yongda.site.ext.service.facade.personal.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.netfinworks.ma.service.base.model.Account;
import com.netfinworks.ma.service.facade.IAccountFacade;
import com.netfinworks.ma.service.request.AccountRequest;
import com.netfinworks.ma.service.response.AccountResponse;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.netfinworks.site.service.facade.model.PersonalAccount;
import com.yongda.site.service.personal.facade.request.QueryMemberRequest;
import com.yongda.site.service.personal.facade.response.BalancesResponse;
import com.yongda.site.service.personal.facade.response.BaseResponse;
/**
 * 查询账户余额
 * @author yp
 *
 */
public class PersonalBalanceService extends AbstractRoutService<QueryMemberRequest, BaseResponse>{

	/**日志**/
	private Logger logger = LoggerFactory
			.getLogger(PersonalBalanceService.class);
	
	@Resource(name = "accountFacade")
    private IAccountFacade accountFacade;
	
	@Override
	public String getRoutName() {
		return "account_info";
	}

	@Override
	public QueryMemberRequest buildRequest(Map<String, String> paramMap) {
		QueryMemberRequest qReq = HttpRequestConvert
				.convert2Request(paramMap,QueryMemberRequest.class);
		Long l = new Long(101);
		qReq.setAccountType(l);//账户类型
		return qReq;
	}

	@Override
	public BaseResponse doProcess(QueryMemberRequest qReq) {
		BalancesResponse resp = new BalancesResponse();
		BaseResponse bresp = new BaseResponse();
		try {
			    AccountRequest req = new AccountRequest();
			    BeanUtils.copyProperties(qReq,req);
			    long beginTime = 0L;
	            if (logger.isInfoEnabled()) {
	                logger.info("查询会员账户，请求参数：{}", qReq.getMemberId());
	                beginTime = System.currentTimeMillis();
	            }
	            AccountResponse response = accountFacade.queryAccountByMemberId(null,req);
	            if (logger.isInfoEnabled()) {
	                long consumeTime = System.currentTimeMillis() - beginTime;
	                logger.info("远程查询会员账户信息， 耗时:{} (ms); 响应结果:{} ",
	                        new Object[] { consumeTime, response });
	            }
	    	    if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
	    	    	PersonalAccount pa = new PersonalAccount();
	    	    	List<PersonalAccount> account = new ArrayList<PersonalAccount>();
	    	    	for(Account a : response.getAccounts()){
	    	    		if(a.getAccountType().toString().equals("101")){
	    	    		   pa.setAccount_id(a.getAccountId());
	    	    		   pa.setAccount_type(a.getAccountType().toString());
	    	    		   pa.setBalance(a.getBalance().toString());
	    	    		   pa.setFrozenBalance(a.getFrozenBalance().toString());
	    	    		   account.add(pa);
	    	    		}
	    	    	}
	    	    	resp.setAccounts(account);
	    	    	bresp.setSuccess(true);
	    	    	bresp.setData(resp);
	            }else {
	            	bresp.setErrorCode(response.getResponseCode());
	            	bresp.setErrorMessage(response.getResponseMessage());
	            }
	    	    return bresp;  
		}catch(Exception e){
			BizException e1 = null;
            if (e instanceof BizException) {
            	e1 = (BizException) e;
            	bresp.setErrorCode(e1.getCode().getCode());
            	bresp.setErrorMessage(e1.getMessage());
            } else {
                logger.error("查询会员ID: {}信息异常:，异常信息：{},{}",qReq.getMemberId(),e.getMessage(),e);
                bresp.setErrorCode("SYSTEM_ERROR");
                bresp.setErrorMessage(e.getMessage());
            }
            return bresp;
		}
	}
}
