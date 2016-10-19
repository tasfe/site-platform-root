package com.netfinworks.site.ext.integration.trade.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.trade.SettlementService;
import com.netfinworks.site.ext.integration.trade.convert.SettlementConvert;
import com.netfinworks.tradeservice.facade.api.TradeQueryFacade;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.model.query.TradeInfoSummary;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;
import com.netfinworks.tradeservice.facade.response.TradeBasicQueryResponse;
import com.netfinworks.tradeservice.facade.response.TradeSummaryQueryResponse;

@Service("enterpriseTradeService")
public class SettlementServiceImpl extends ClientEnvironment implements
		SettlementService {
	private Logger logger = LoggerFactory
			.getLogger(SettlementServiceImpl.class);

	@Resource(name = "tradeQueryFacade")
	private TradeQueryFacade tradeQueryFacade;

	@Resource(name = "memberFacade")
	private IMemberFacade iMemberFacade;

	@Override
	public CoTradeQueryResponse queryList(CoTradeRequest lReq,
			CoTradeRequest sReq) throws BizException {
		try {

			OperationEnvironment oe = getEnv(lReq.getClientIp());
			// -----------1.请求
			// 查询结算流水开始------------------------------------------------
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询钱包结算对账单，结算流水明细请求参数：{}", lReq);
				beginTime = System.currentTimeMillis();
			}
			// 请求查询结算流水明细转换
			TradeBasicQueryRequest listReq = SettlementConvert
					.convertSettlementRequset(lReq);
			// 请求返回list(流水)
			TradeBasicQueryResponse response = tradeQueryFacade.querySettledList(
					listReq, oe);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询钱包结算对账单，结算流水明细  耗时:{} (ms); 响应结果:{} ",
						new Object[] { consumeTime, response });
			}
			// ---------- 1.请求
			// 查询结算流水结束----------------------------------------------------
            
			TradeSummaryQueryResponse sumRep=null;
			if(null!=response&&response.isSuccess()&&null!=response.getTradeBasicInfoList()&&response.getTradeBasicInfoList().size()>0){
            	// ------------2.请求
    			// 查询汇总开始------------------------------------------------------
    			long sumBeginTime = 0L;
    			if (logger.isInfoEnabled()) {
    				logger.info("查询钱包结算对账单，汇总请求参数：{}", sReq);
    				sumBeginTime = System.currentTimeMillis();
    			}
    			// 汇总查询请求转换
    			TradeBasicQueryRequest summaryReq = SettlementConvert
    					.convertSettlementRequset(sReq);
    			// 返回汇总信息
    			 sumRep = tradeQueryFacade.querySummary(
    					summaryReq, oe);

    			if (logger.isInfoEnabled()) {
    				long sumConsumeTime = System.currentTimeMillis() - sumBeginTime;
    				logger.info("远程查询钱包结算对账单，汇总  耗时:{} (ms); 响应结果:{} ",
    						new Object[] { sumConsumeTime, sumRep });
    			}
    			// ------------- 2.请求
    			// 查询汇总结束---------------------------------------------------
            }
			

			String companyName = null;
			if (response.isSuccess()) {

				List<TradeBasicInfo> result = response.getTradeBasicInfoList();
				CoTradeQueryResponse rep = new CoTradeQueryResponse();
				if (result != null && result.size() > 0) {
					// 对结算流水list进行转换
					rep.setBaseInfoList(SettlementConvert
							.convertSettlementReponseList(result,
									lReq.getMemberId()));
				}

				/*// 获得公司信息
				CompanyMemberQueryRequest comRequest = new CompanyMemberQueryRequest();
				comRequest.setMemberId(lReq.getMemberId());
				CompanyMemberResponse comResponse = iMemberFacade
						.queryCompanyMember(oe, comRequest);

				// 获得公司名称
				if (comResponse.getCompanyMemberInfo() != null) {
					companyName = comResponse.getCompanyMemberInfo()
							.getCompanyName();
				}*/

				// 对汇总信息进行转换
				TradeInfoSummary infoSum=null;
				if(sumRep==null){
					infoSum=new TradeInfoSummary();
					SettlementConvert.initSummaryInfo(infoSum);
				}else if(null!=sumRep&&sumRep.isSuccess()){					
					 infoSum = sumRep.getTradeInfoSummary();	
				}else if(!sumRep.isSuccess()){					
					throw new BizException(ErrorCode.SYSTEM_ERROR);	
				}
				rep.setSummaryInfo(SettlementConvert.convertSummaryReponse(infoSum, companyName));
				rep.setQueryBase(response.getQueryBase());
				return rep;

			} else {
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}

		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("查询钱包结算流水明细 " + lReq.getMemberId()
						+ "信息异常:[列表请求信息]" + lReq + "。 [汇总请求信息]" + sReq + "。", e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
	}

}
