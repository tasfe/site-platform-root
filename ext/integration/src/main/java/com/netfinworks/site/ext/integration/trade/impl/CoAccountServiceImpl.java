package com.netfinworks.site.ext.integration.trade.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.dpm.accounting.api.AccountService;
import com.netfinworks.dpm.accounting.api.requests.BalanceQueryRequest;
import com.netfinworks.dpm.accounting.api.responses.BalanceQueryResponse;
import com.netfinworks.ma.service.facade.IAccountFacade;
import com.netfinworks.ma.service.request.AccBalanceListRequest;
import com.netfinworks.ma.service.response.AccBalanceListResponse;
import com.netfinworks.ma.service.response.AccountBalanceListResp;
import com.netfinworks.site.domain.domain.response.WalletCheckResponse;
import com.netfinworks.site.domain.domain.trade.WalletCheckRequest;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.trade.CoAccountService;
import com.netfinworks.site.ext.integration.trade.convert.WalletCheckConvert;

/**
 * <p>
 * 企业钱包对账单
 * </p>
 * 
 * @author zhangyun.m
 * @version $Id: CoAccountServiceImpl.java, v 0.1 2013-12-12 下午4:23:59 HP Exp $
 */
@Service("coAccountService")
public class CoAccountServiceImpl extends ClientEnvironment implements
		CoAccountService {
	private Logger logger = LoggerFactory.getLogger(CoAccountServiceImpl.class);

	@Resource(name = "accountFacade")
	private IAccountFacade accountFacade;

	@Resource(name = "accountServiceApi")
	private AccountService accountServiceApi;

	/**
	 * zhangyun.m 查询企业钱包对账单
	 * 
	 * @param req
	 * @return WalletCheckInfo
	 */
	@Override
	public WalletCheckResponse queryWalletCheckList(WalletCheckRequest req)
			throws BizException {
		WalletCheckResponse rep = new WalletCheckResponse();
		Money currentMonthBalance = null;
		// 上月余额
		
		Money lMonthBalance = null;
		// 查询结束日余额
		Money eDayBalance = null;
		QueryBase queryBase = new QueryBase();
		queryBase.setNeedQueryAll(true);
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询钱包对账单，请求参数：{}", req);
				beginTime = System.currentTimeMillis();
			}
			// 请求转换
			AccBalanceListRequest request = WalletCheckConvert
					.convertWalletCheckRequset(req);
			// 对账单流水返回结果
			OperationEnvironment oe = getEnv(req.getClientIp());
			AccBalanceListResponse response = accountFacade
					.queryAccountBalanceList(oe, request);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询钱包对账单， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if (ResponseCode.SUCCESS.getCode().equals(
					response.getResponseCode())) {

				// 查询单日余额请求转换
				BalanceQueryRequest balanceReq = WalletCheckConvert
						.convertBalanceRequset(req);
				// 查询上月最后一天余额
				balanceReq.setAccountDate(req.getLmlday());
				BalanceQueryResponse lastMonthBalance = accountServiceApi
						.queryAccountBalance(balanceReq, oe);
				lMonthBalance = lastMonthBalance.getBalance();
				rep.setLmldBalance(lMonthBalance);
				// 查询结束日余额
				balanceReq.setAccountDate(req.getEndTime());
				BalanceQueryResponse endDayBalance = accountServiceApi
						.queryAccountBalance(balanceReq, oe);
				eDayBalance = endDayBalance.getBalance();
				// 本月余额= 查询结束日余额 -上月最后一天余额
				if (lMonthBalance == null) {
					lMonthBalance = new Money();
				}
				if (eDayBalance == null){
					eDayBalance = new Money();
				}
				currentMonthBalance = eDayBalance.subtract(lMonthBalance);
				rep.setCurrentMonthBalance(currentMonthBalance);

				// 总收入
				rep.setTotalIncome(response.getTotalIncome());
				// 总支出
				rep.setTotalPayout(response.getTotalPayout());
				// 分页(总记录数)
				queryBase.setTotalItem(response.getTotalCount());
				// 分页(页大小)
				queryBase.setPageSize(req.getQueryBase().getPageSize());
				queryBase.setCurrentPage(req.getQueryBase().getCurrentPage());
				rep.setQueryBase(queryBase);

				List<AccountBalanceListResp> result = response
						.getBalanceListResp();
				if (result != null && result.size() > 0) {
					// 对结果转换
					rep.setList(WalletCheckConvert
							.convertWalletCheckReponseList(result));
					return rep;
				}
				return null;
			} else {
				queryBase.setPageSize(req.getQueryBase().getPageSize());
				queryBase.setCurrentPage(req.getQueryBase().getCurrentPage());
				rep.setQueryBase(queryBase);
				// 无查询结果
				return rep;
			}

		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(
						"查询钱包对账单 " + req.getMemberId() + "信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
	}
}
