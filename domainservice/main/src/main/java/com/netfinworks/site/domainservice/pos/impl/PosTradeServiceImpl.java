package com.netfinworks.site.domainservice.pos.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.dal.daointerface.PosTradeDOMapper;
import com.netfinworks.site.core.dal.dataobject.PosRequest;
import com.netfinworks.site.core.dal.dataobject.PosTradeDO;
import com.netfinworks.site.domain.domain.info.SummaryInfo;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.pos.PosTradeRequest;
import com.netfinworks.site.domainservice.pos.PosTradeResponse;
import com.netfinworks.site.domainservice.pos.PosTradeService;

public class PosTradeServiceImpl implements PosTradeService {
	
	private Logger logger = LoggerFactory.getLogger(PosTradeServiceImpl.class);

	@Resource(name = "posTradeDOMapper")
	private PosTradeDOMapper posTradeDOMapper;

	@Override
	public PosTradeResponse selectPosList(PosTradeRequest req) {
		PosTradeResponse resp = new PosTradeResponse();
		PosRequest request = converter(req);
		logger.info("请求参数："+request);
		try {
			String tradeNo = req.getTradeNo();
			String tradeSrcNo = req.getTradeSrcNo();
			String tradeType = req.getTradeType();
			String status = req.getStatus();
			List<PosTradeDO> posTotalList = new ArrayList<PosTradeDO>();//所有记录
			List<PosTradeDO> posList = new ArrayList<PosTradeDO>();//分页记录
			if(tradeNo!=null){//交易订单号
				posTotalList = posTradeDOMapper.selectByTradeNo(request);
				resp.setPosList(posTotalList);
			}else if(tradeSrcNo!=null){//原始订单号
				posTotalList = posTradeDOMapper.selectByTradeSrcNo(request);
				resp.setPosList(posTotalList);
			}else if(status==null&&tradeType==null&&req.getStartTime()!=null){//按时间查询
				posList = posTradeDOMapper.selectByTradeTime(request);
				posTotalList = posTradeDOMapper.selectTotalByTradeTime(request);
				resp.setPosList(posList);
			}else if(status==null&&req.getStartTime()!=null){//按交易类型查询
				posList = posTradeDOMapper.selectByTradeType(request);
				posTotalList = posTradeDOMapper.selectTotalByTradeType(request);
				resp.setPosList(posList);
			}else if(tradeType==null&&req.getStartTime()!=null){//按交易状态查询
				posList = posTradeDOMapper.selectByTradeStatus(request);
				posTotalList = posTradeDOMapper.selectTotalByTradeStatus(request);
				resp.setPosList(posList);
			}else if(req.getStartTime()!=null){//按交易类型，状态查询
				posList = posTradeDOMapper.selectByTradeAll(request);
				posTotalList = posTradeDOMapper.selectTotalByTradeAll(request);
				resp.setPosList(posList);
			}
			
			List<String> type = posTradeDOMapper.selectTradeType(request);
			
			SummaryInfo sum = new SummaryInfo();
			Money amount = new Money();
			Money feeAmount = new Money();
			Money refundAmount = new Money();
			Money settleAmount = new Money();
			
			int num = 0;
			for(PosTradeDO pt : posTotalList){
				if(pt.getStatus().equals("处理失败")){
					refundAmount.addTo(pt.getTradeSum());
					num++;
				}else{
					//总交易额
					amount.addTo(pt.getTradeAmount());
					//手续费
					feeAmount.addTo(pt.getTradeFee());
					//结算金额
					settleAmount.addTo(pt.getTradeSum());
				}
			}
			if(req.getQueryBase()!=null){
				req.getQueryBase().setTotalItem(posTotalList.size());
				resp.setQueryBase(req.getQueryBase());
			}
			//交易总笔数（去掉失败）
			sum.setTotalTradeCount(posTotalList.size()-num);
			sum.setTotalRefundAmount(refundAmount);
			sum.setTotalFeeAmount(feeAmount);
			sum.setTotalTradeAmount(amount);
		//	Money settle = feeAmount.add(amount);
			sum.setTotalSettledAmount(settleAmount);
			resp.setTradeType(type);
			resp.setSummaryInfo(sum);
		} catch (Exception e) {
			logger.error(e.getMessage(), e.getCause());
		}
		return resp;
		
	}
	/**请求类型转换 **/
   public PosRequest converter(PosTradeRequest req){
	   PosRequest request = new PosRequest();
	   request.setEndTime(req.getEndTime());
	   request.setStartTime(req.getStartTime());
	   request.setStatus(req.getStatus());
	   request.setTradeType(req.getTradeType());
	   request.setMemberNo(req.getMemberId());
	   request.setTradeNo(req.getTradeNo());
	   request.setTradeSrcNo(req.getTradeSrcNo());
	   int page1;
	   int page2;
	   if(req.getQueryBase()!=null){
		   int pageSize = req.getQueryBase().getPageSize();
		   int currentPage = req.getQueryBase().getCurrentPage();
		   page1 = (currentPage-1)*pageSize+1;
		   page2 = currentPage*pageSize;
		   request.setPage1(page1);
		   request.setPage2(page2);
	   }
	   return request;
   }
}
