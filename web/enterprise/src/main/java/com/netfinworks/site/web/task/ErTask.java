package com.netfinworks.site.web.task;


import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ersys.facade.response.TradeOrderInfo;
import com.netfinworks.ersys.service.facade.api.ErServiceFacade;
import com.netfinworks.fos.service.facade.FundoutFacade;
import com.netfinworks.fos.service.facade.domain.FundoutInfo;
import com.netfinworks.fos.service.facade.request.FundoutQueryRequest;
import com.netfinworks.fos.service.facade.response.FundoutQueryResponse;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.tradeservice.facade.api.TradeQueryFacade;
import com.netfinworks.tradeservice.facade.enums.TimeQueryType;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;
import com.netfinworks.tradeservice.facade.response.TradeBasicQueryResponse;

public class ErTask{
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource(name="erServiceFacade")
	private ErServiceFacade erServiceFacade;
	
    @Resource(name = "tradeQueryFacade")
    private TradeQueryFacade tradeQueryFacade;
	
    
    @Resource(name="fundoutFacade")
    private FundoutFacade  fundoutFacade;
    
    @Resource(name = "defaultFundoutService")
	private DefaultFundoutService defaultFundoutService;
    
	@Resource
	private XUCache<String> xuCache;
	
	/**本地文件目录*/
	@Value("${erIP}")
 	private String erIP;
    
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private SimpleDateFormat sdfhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public void insertData(){
		erServiceFacade.insertData("1022");
	}
	public  boolean loadCache(String methodName){
		String cacheKey=methodName+"_"+DateUtils.getStringDate("yyyy-MM-dd");
		logger.info("loadCache key={}",cacheKey);
		boolean result=xuCache.add(cacheKey, "true", 1*24*60*60);
		return result;
	}
	
	//转账到永达互联网金融前一天的
	public void dataMigrationAccountYesterday() throws ParseException{
		logger.info("dataMigrationAccountYesterday start");
		if(this.loadCache("dataMigrationAccountYesterday")){
			try {
				logger.info("dataMigrationAccountYesterday loadCache");
				OperationEnvironment env=new OperationEnvironment();
				TradeBasicQueryRequest req=new TradeBasicQueryRequest();
				List<String> productCodes=new ArrayList<String>();
				productCodes.add("10310");
				req.setProductCodes(productCodes);
				req.setGmtStart(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 00:00:00"));
				req.setGmtEnd(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 23:59:59"));
				QueryBase queryBase=new QueryBase();
				queryBase.setPageSize(20000);
				req.setQueryBase(queryBase);
				List<String>  tradeStatus=new ArrayList<String>();
				tradeStatus.add("301");
				tradeStatus.add("401");
				req.setTradeStatus(tradeStatus);
				req.setTimeQueryType(TimeQueryType.MODIFIED);
				TradeBasicQueryResponse response=tradeQueryFacade.queryList(req, env);
				logger.info("dataMigrationAccountYesterday response");
				List<TradeBasicInfo> tradeBasicInfoList=response.getTradeBasicInfoList();
				
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(tradeBasicInfoList!=null && !tradeBasicInfoList.isEmpty()){
					logger.info("dataMigrationAccountYesterday size:",tradeBasicInfoList.size());
					for(TradeBasicInfo tbi:tradeBasicInfoList){
						tradeOrderInfo=new TradeOrderInfo();
						tradeOrderInfo.setBizProductCode(tbi.getBizProductCode());
						tradeOrderInfo.setTradeVoucherNo(tbi.getTradeVoucherNo());
						tradeOrderInfo.setTradeSrcVoucherNo(tbi.getTradeSourceVoucherNo());
						tradeOrderInfo.setSourceBatchNo(tbi.getSourceBatchNo());
						tradeOrderInfo.setBatchOrderNo(tbi.getBatchNo());
						tradeOrderInfo.setBizNo(tbi.getBizNo());
						tradeOrderInfo.setTradeAmount(tbi.getTradeAmount().getAmount());
						tradeOrderInfo.setTradeType(tbi.getTradeType().getCode());
						tradeOrderInfo.setBuyerId(tbi.getBuyerId());
						tradeOrderInfo.setBuyerName(tbi.getBuyerName());
						tradeOrderInfo.setFee(tbi.getPayerFee().getAmount());
						tradeOrderInfo.setSellerId(tbi.getSellerId());
						tradeOrderInfo.setSellerName(tbi.getSellerName());
						tradeOrderInfo.setExtension(tbi.getExtention());
						tradeOrderInfo.setStatus(tbi.getStatus());
						tradeOrderInfo.setTradeMemo(tbi.getTradeMemo());
						tradeOrderInfo.setGmtCreate(tbi.getGmtSubmit());
						tradeOrderInfo.setGmtModified(tbi.getGmtPaid());
						tradeOrderInfo.setProdDesc(tbi.getProdDesc());
						tradeOrderInfo.setHasDownload("0");
						tradeOrderInfo.setSource("1");
						newList.add(tradeOrderInfo);
					}
				}
				logger.info("dataMigrationAccountYesterday er start");
				erServiceFacade.batchInsertTradeOrder(newList);
				logger.info("dataMigrationAccountYesterday er end");
			} catch (Exception e) {
				logger.error("dataMigrationAccountYesterday Exception",e);
			}
		}
		logger.info("dataMigrationAccountYesterday end");
	}
	
	/**
	 * // 10241  ,"资金归集到账户" 10231  ,"代发工资到账户"  (针对于10241、10231的业务品种)对前一天的数据的迁移
	 * @throws ParseException
	 */
	public void capitalAndSalaryToAccountYesterday() throws ParseException{
		logger.info("capitalAndSalaryToAccountYesterday start");
		if(this.loadCache("capitalAndSalaryToAccountYesterday")){
			try {
				logger.info("capitalAndSalaryToAccountYesterday loadCache");
				OperationEnvironment env=new OperationEnvironment();
				TradeBasicQueryRequest req=new TradeBasicQueryRequest();
				List<String> productCodes=new ArrayList<String>();
				productCodes.add("10231");
				productCodes.add("10241");
				req.setProductCodes(productCodes);
				req.setGmtStart(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 00:00:00"));
				req.setGmtEnd(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 23:59:59"));
				QueryBase queryBase=new QueryBase();
				queryBase.setPageSize(20000);
				req.setQueryBase(queryBase);
				List<String>  tradeStatus=new ArrayList<String>();
				tradeStatus.add("301");
				tradeStatus.add("401");
				req.setTradeStatus(tradeStatus);
				req.setTimeQueryType(TimeQueryType.MODIFIED);
				TradeBasicQueryResponse response=tradeQueryFacade.queryList(req, env);
				logger.info("capitalAndSalaryToAccountYesterday response");
				List<TradeBasicInfo> tradeBasicInfoList=response.getTradeBasicInfoList();
				
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(tradeBasicInfoList!=null && !tradeBasicInfoList.isEmpty()){
					logger.info("capitalAndSalaryToAccountYesterday size:",tradeBasicInfoList.size());
					for(TradeBasicInfo tbi:tradeBasicInfoList){
						tradeOrderInfo=new TradeOrderInfo();
						tradeOrderInfo.setBizProductCode(tbi.getBizProductCode());
						tradeOrderInfo.setTradeVoucherNo(tbi.getTradeVoucherNo());
						tradeOrderInfo.setTradeSrcVoucherNo(tbi.getTradeSourceVoucherNo());
						tradeOrderInfo.setSourceBatchNo(tbi.getSourceBatchNo());
						tradeOrderInfo.setBatchOrderNo(tbi.getBatchNo());
						tradeOrderInfo.setBizNo(tbi.getBizNo());
						tradeOrderInfo.setTradeAmount(tbi.getTradeAmount().getAmount());
						tradeOrderInfo.setTradeType(tbi.getTradeType().getCode());
						tradeOrderInfo.setBuyerId(tbi.getBuyerId());
						tradeOrderInfo.setBuyerName(tbi.getBuyerName());
						tradeOrderInfo.setFee(tbi.getPayerFee().getAmount());
						tradeOrderInfo.setSellerId(tbi.getSellerId());
						tradeOrderInfo.setSellerName(tbi.getSellerName());
						tradeOrderInfo.setExtension(tbi.getExtention());
						tradeOrderInfo.setStatus(tbi.getStatus());
						tradeOrderInfo.setTradeMemo(tbi.getTradeMemo());
						tradeOrderInfo.setGmtCreate(tbi.getGmtSubmit());
						tradeOrderInfo.setGmtModified(tbi.getGmtPaid());
						tradeOrderInfo.setProdDesc(tbi.getProdDesc());
						tradeOrderInfo.setHasDownload("0");
						tradeOrderInfo.setSource("1");
						newList.add(tradeOrderInfo);
					}
				}
				logger.info("capitalAndSalaryToAccountYesterday er start");
				erServiceFacade.batchInsertTradeOrder(newList);
				logger.info("capitalAndSalaryToAccountYesterday er end");
			} catch (Exception e) {
				logger.error("capitalAndSalaryToAccountYesterday Exception",e);
			}
		}
		logger.info("capitalAndSalaryToAccountYesterday end");
	}
	
	//转账到卡前一天
	public void dataMigrationBankCardYesterday() throws ParseException{
		logger.info("dataMigrationBankCardYesterday start");
		if(this.loadCache("dataMigrationBankCardYesterday")){
			try {
				logger.info("dataMigrationBankCardYesterday loadCache");
				OperationEnvironment env=new OperationEnvironment();
				
				FundoutQueryRequest queryRequest=new FundoutQueryRequest();
				queryRequest.setProductCode("10220,10221");
				queryRequest.setResultTimeStart(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 00:00:00"));
				queryRequest.setResultTimeEnd(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 23:59:59"));
				queryRequest.setPageSize(20000);
				queryRequest.setStatus("success");
	            FundoutQueryResponse response = fundoutFacade.queryFundoutOrderInfo(queryRequest, env);
	            logger.info("dataMigrationBankCardYesterday response");
	            List<FundoutInfo> list = response.getFundoutInfoList();
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(list!=null && !list.isEmpty()){
					logger.info("dataMigrationBankCardYesterday size:",list.size());
					for(FundoutInfo fif:list){
						tradeOrderInfo=new TradeOrderInfo();
						tradeOrderInfo.setTradeVoucherNo(fif.getFundoutOrderNo());
						tradeOrderInfo.setBizProductCode(fif.getProductCode());
						tradeOrderInfo.setBuyerId(fif.getMemberId());
						tradeOrderInfo.setAccountNo(fif.getAccountNo());
						tradeOrderInfo.setTradeAmount(fif.getAmount().getAmount());
						tradeOrderInfo.setFee(fif.getFee().getAmount());
						tradeOrderInfo.setCardNo(fif.getCardNo());
						tradeOrderInfo.setCardType(fif.getCardType());
						tradeOrderInfo.setName(fif.getName());
						tradeOrderInfo.setBankCode(fif.getBankCode());
						tradeOrderInfo.setBankName(fif.getBankName());
						tradeOrderInfo.setBranchName(fif.getBranchName());
						tradeOrderInfo.setBankLineNo(fif.getBankLineNo());
						tradeOrderInfo.setProv(fif.getProv());
						tradeOrderInfo.setCity(fif.getCity());
						tradeOrderInfo.setTradeMemo(fif.getPurpose());
						tradeOrderInfo.setCompanyPersonal(fif.getCompanyOrPersonal());
						tradeOrderInfo.setFundoutGrade(fif.getFundoutGrade());
						tradeOrderInfo.setStatus(fif.getStatus());
						tradeOrderInfo.setOrderTime(fif.getOrderTime());
						tradeOrderInfo.setResultTime(fif.getResultTime());
						tradeOrderInfo.setExtension(fif.getExtension());
						tradeOrderInfo.setGmtCreate(fif.getOrderTime());
						tradeOrderInfo.setResultTime(fif.getResultTime());
						tradeOrderInfo.setAccountType(fif.getAccountType());
						tradeOrderInfo.setResultDesc(fif.getResultDesc());
						tradeOrderInfo.setSourceBatchNo(fif.getSourceBatchNo());
						tradeOrderInfo.setBatchOrderNo(fif.getBatchOrderNo());
						tradeOrderInfo.setTradeSrcVoucherNo(fif.getOutOrderNo());
						tradeOrderInfo.setSource("2");
						tradeOrderInfo.setHasDownload("0");
						newList.add(tradeOrderInfo);
					}
					logger.info("dataMigrationBankCardYesterday er start");
					erServiceFacade.batchInsertTradeOrder(newList);
					logger.info("dataMigrationBankCardYesterday er end");
				}
			} catch (Exception e) {
				logger.error("dataMigrationBankCardYesterday Exception",e);
			}
		}
		logger.info("dataMigrationBankCardYesterday end");
	}
	/*
	 *电子回单 退票 	迁移前一天的数据
	 */
	public void refundTicketYesterday() throws ParseException{
		logger.info("refundTicketYesterday start");
		if(this.loadCache("refundTicketYesterday")){
			try {
				logger.info("refundTicketYesterday loadCache");
				OperationEnvironment env=new OperationEnvironment();
				PageResultList page8 = new PageResultList();
				FundoutQuery fundoutQuery8 = new FundoutQuery();
				fundoutQuery8.setStatus("success");
				fundoutQuery8.setCurrentPage(1);
				fundoutQuery8.setPageSize(20000);
				fundoutQuery8.setRefundTimeStart(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 00:00:00"));
				
				fundoutQuery8.setRefundTimeEnd(sdf.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 23:59:59"));
				page8 = defaultFundoutService.queryFundoutOrderInfo(fundoutQuery8, env);
				logger.info("refundTicketYesterday page8");
				List<Fundout> fundoutlist=page8.getInfos();
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(fundoutlist!=null && !fundoutlist.isEmpty()){
					logger.info("refundTicketYesterday size:",fundoutlist.size());
					for(Fundout fif:fundoutlist){
						tradeOrderInfo=new TradeOrderInfo();
						tradeOrderInfo.setTradeVoucherNo(fif.getFundoutOrderNo());
						tradeOrderInfo.setBizProductCode(fif.getProductCode());
						tradeOrderInfo.setBuyerId(fif.getMemberId());
						tradeOrderInfo.setAccountNo(fif.getAccountNo());
						tradeOrderInfo.setTradeAmount(fif.getAmount().getAmount());
						tradeOrderInfo.setFee(fif.getFee().getAmount());
						tradeOrderInfo.setCardNo(fif.getCardNo());
						tradeOrderInfo.setCardType(fif.getCardType());
						tradeOrderInfo.setName(fif.getName());
						tradeOrderInfo.setBankCode(fif.getBankCode());
						tradeOrderInfo.setBankName(fif.getBankName());
						tradeOrderInfo.setBranchName(fif.getBranchName());
						tradeOrderInfo.setBankLineNo(fif.getBankLineNo());
						tradeOrderInfo.setProv(fif.getProv());
						tradeOrderInfo.setCity(fif.getCity());
						tradeOrderInfo.setCompanyPersonal(fif.getCompanyOrPersonal());
						tradeOrderInfo.setFundoutGrade(fif.getFundoutGrade());
						if("success".equals(fif.getStatus())){
							tradeOrderInfo.setStatus("refundTicket");
						}
						tradeOrderInfo.setTradeMemo(fif.getPurpose());
						tradeOrderInfo.setOrderTime(fif.getOrderTime());
						tradeOrderInfo.setExtension(fif.getExtension());
						tradeOrderInfo.setGmtCreate(fif.getRefundTime());
						tradeOrderInfo.setSourceBatchNo(fif.getSourceBatchNo());
						tradeOrderInfo.setBatchOrderNo(fif.getBatchOrderNo());
						tradeOrderInfo.setTradeSrcVoucherNo(fif.getOutOrderNo());
						tradeOrderInfo.setSource("2");
						tradeOrderInfo.setHasDownload("0");
						newList.add(tradeOrderInfo);
					}
					logger.info("refundTicketYesterday er card");
					erServiceFacade.batchInsertTradeOrder(newList);
					logger.info("refundTicketYesterday er card");
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				logger.error("refundTicketYesterday Exception",e);
			}
		}
		logger.info("refundTicketYesterday end");
	}
	/**
	 * 	// 10240  ,"资金归集到银行卡" 10230  ,"代发工资到银行卡"  (针对于10240、10230的业务品种)对前一天的数据的迁移
	 * @throws ParseException
	 */
	public void capitalAndSalaryToBankCardYesterday() throws ParseException{
		logger.info("capitalAndSalaryToBankCardYesterday start");
		if(this.loadCache("capitalAndSalaryToBankCardYesterday")){
			try {
				logger.info("capitalAndSalaryToBankCardYesterday loadCache");
				OperationEnvironment env=new OperationEnvironment();
				FundoutQueryRequest queryRequest=new FundoutQueryRequest();
				queryRequest.setProductCode("10240,10230");
				queryRequest.setResultTimeStart(sdfhms.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 00:00:00"));
				queryRequest.setResultTimeEnd(sdfhms.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 23:59:59"));
				queryRequest.setPageSize(20000);
				queryRequest.setStatus("success");
	            FundoutQueryResponse response = fundoutFacade.queryFundoutOrderInfo(queryRequest, env);
	            logger.info("capitalAndSalaryToBankCardYesterday response");
	            List<FundoutInfo> list = response.getFundoutInfoList();
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(list!=null && !list.isEmpty()){
					logger.info("capitalAndSalaryToBankCardYesterday size:",list.size());
					for(FundoutInfo fif:list){
						tradeOrderInfo=new TradeOrderInfo();
						tradeOrderInfo.setTradeVoucherNo(fif.getFundoutOrderNo());
						tradeOrderInfo.setBizProductCode(fif.getProductCode());
						tradeOrderInfo.setBuyerId(fif.getMemberId());
						tradeOrderInfo.setAccountNo(fif.getAccountNo());
						tradeOrderInfo.setTradeAmount(fif.getAmount().getAmount());
						tradeOrderInfo.setFee(fif.getFee().getAmount());
						tradeOrderInfo.setCardNo(fif.getCardNo());
						tradeOrderInfo.setCardType(fif.getCardType());
						tradeOrderInfo.setName(fif.getName());
						tradeOrderInfo.setBankCode(fif.getBankCode());
						tradeOrderInfo.setBankName(fif.getBankName());
						tradeOrderInfo.setBranchName(fif.getBranchName());
						tradeOrderInfo.setBankLineNo(fif.getBankLineNo());
						tradeOrderInfo.setProv(fif.getProv());
						tradeOrderInfo.setCity(fif.getCity());
						tradeOrderInfo.setTradeMemo(fif.getPurpose());
						tradeOrderInfo.setCompanyPersonal(fif.getCompanyOrPersonal());
						tradeOrderInfo.setFundoutGrade(fif.getFundoutGrade());
						tradeOrderInfo.setStatus(fif.getStatus());
						tradeOrderInfo.setOrderTime(fif.getOrderTime());
						tradeOrderInfo.setResultTime(fif.getResultTime());
						tradeOrderInfo.setExtension(fif.getExtension());
						tradeOrderInfo.setGmtCreate(fif.getOrderTime());
						tradeOrderInfo.setResultTime(fif.getResultTime());
						tradeOrderInfo.setAccountType(fif.getAccountType());
						tradeOrderInfo.setResultDesc(fif.getResultDesc());
						tradeOrderInfo.setSourceBatchNo(fif.getSourceBatchNo());
						tradeOrderInfo.setBatchOrderNo(fif.getBatchOrderNo());
						tradeOrderInfo.setTradeSrcVoucherNo(fif.getOutOrderNo());
						tradeOrderInfo.setSource("2");
						tradeOrderInfo.setHasDownload("0");
						newList.add(tradeOrderInfo);
					}
					logger.info("capitalAndSalaryToBankCardYesterday er start");
					erServiceFacade.batchInsertTradeOrder(newList);
					logger.info("capitalAndSalaryToBankCardYesterday er end");
				}
			} catch (Exception e) {
				logger.error("capitalAndSalaryToBankCardYesterday Exception",e);
			}
		}
		logger.info("capitalAndSalaryToBankCardYesterday end");
	}
	
	//转账到永达互联网金融-自动日期区间的数据
	public void dataMigrationAccount() throws ParseException{
		logger.info("dataMigrationAccount start");
		if(this.loadCache("dataMigrationAccount")){
			try {
				logger.info("dataMigrationAccount loadCache");
				String startTime="2015-05-17";
				String endTime="2015-05-30";
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf10 = new SimpleDateFormat("yyyy-MM-dd");//日期10位
				Date d = null;
				try {
					d = sdf10.parse(startTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				cal.setTime(d);
				List<String> dateList = new ArrayList<String>();
				dateList.add("2015-06-04");
				dateList.add("2015-06-11");
				dateList.add("2015-06-10");
				if (startTime.equals(endTime)) {
					dateList.add(startTime);
				} else {
					String date = null;
					dateList.add(startTime);
					do {
						cal.add(Calendar.DAY_OF_MONTH, 1);
						date = sdf10.format(cal.getTime());
						dateList.add(date);
					} while (date.compareTo(endTime) < 0);
				}	
				for(String queryDate:dateList){
				OperationEnvironment env=new OperationEnvironment();
				TradeBasicQueryRequest req=new TradeBasicQueryRequest();
				List<String> productCodes=new ArrayList<String>();
				productCodes.add("10310");
				req.setProductCodes(productCodes);
				req.setGmtStart(sdf.parse(queryDate+" 00:00:00"));
				req.setGmtEnd(sdf.parse(queryDate+" 23:59:59"));
				QueryBase queryBase=new QueryBase();
				queryBase.setPageSize(20000);
				req.setQueryBase(queryBase);
				List<String>  tradeStatus=new ArrayList<String>();
				tradeStatus.add("301");
				tradeStatus.add("401");
				req.setTradeStatus(tradeStatus);
				req.setTimeQueryType(TimeQueryType.MODIFIED);
				TradeBasicQueryResponse response=tradeQueryFacade.queryList(req, env);
				logger.info("dataMigrationAccount response");
				List<TradeBasicInfo> tradeBasicInfoList=response.getTradeBasicInfoList();
				
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(tradeBasicInfoList!=null && !tradeBasicInfoList.isEmpty()){
					logger.info("dataMigrationAccount size:",tradeBasicInfoList.size());
					for(TradeBasicInfo tbi:tradeBasicInfoList){
						tradeOrderInfo=new TradeOrderInfo();
						tradeOrderInfo.setBizProductCode(tbi.getBizProductCode());
						tradeOrderInfo.setTradeVoucherNo(tbi.getTradeVoucherNo());
						tradeOrderInfo.setTradeSrcVoucherNo(tbi.getTradeSourceVoucherNo());
						tradeOrderInfo.setSourceBatchNo(tbi.getSourceBatchNo());
						tradeOrderInfo.setBatchOrderNo(tbi.getBatchNo());
						tradeOrderInfo.setBizNo(tbi.getBizNo());
						tradeOrderInfo.setTradeAmount(tbi.getTradeAmount().getAmount());
						tradeOrderInfo.setTradeType(tbi.getTradeType().getCode());
						tradeOrderInfo.setBuyerId(tbi.getBuyerId());
						tradeOrderInfo.setBuyerName(tbi.getBuyerName());
						tradeOrderInfo.setFee(tbi.getPayerFee().getAmount());
						tradeOrderInfo.setSellerId(tbi.getSellerId());
						tradeOrderInfo.setSellerName(tbi.getSellerName());
						tradeOrderInfo.setExtension(tbi.getExtention());
						tradeOrderInfo.setStatus(tbi.getStatus());
						tradeOrderInfo.setTradeMemo(tbi.getTradeMemo());
						tradeOrderInfo.setGmtCreate(tbi.getGmtSubmit());
						tradeOrderInfo.setGmtModified(tbi.getGmtPaid());
						tradeOrderInfo.setProdDesc(tbi.getProdDesc());
						tradeOrderInfo.setHasDownload("0");
						tradeOrderInfo.setSource("1");
						newList.add(tradeOrderInfo);
					}
				}
				logger.info("erServiceFacade account start");
				erServiceFacade.batchInsertTradeOrder(newList);
				logger.info("account end");
				System.out.println("account end");
			}
			} catch (Exception e) {
				logger.error("dataMigrationAccount Exception",e);
			}
		}
		logger.info("dataMigrationAccount end");
	}
	/**
	 * // 10241  ,"资金归集到账户" 10231  ,"代发工资到账户"  (针对于10241、10231的业务品种)对（2015年1-5月）的数据的迁移
	 * @throws ParseException
	 */
	public void capitalAndSalaryToAccount() throws ParseException{
		logger.info("capitalAndSalaryToAccount begin");
		if(this.loadCache("capitalAndSalaryToAccount")){
			try {
				logger.info("capitalAndSalaryToAccount loadCache");
				String startTime="2015-05-17";
				String endTime="2015-05-30";
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf10 = new SimpleDateFormat("yyyy-MM-dd");//日期10位
				Date d = null;
				try {
					d = sdf10.parse(startTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				cal.setTime(d);
				List<String> dateList = new ArrayList<String>();
				dateList.add("2015-06-04");
				dateList.add("2015-06-11");
				dateList.add("2015-06-10");
				if (startTime.equals(endTime)) {
					dateList.add(startTime);
				} else {
					String date = null;
					dateList.add(startTime);
					do {
						cal.add(Calendar.DAY_OF_MONTH, 1);
						date = sdf10.format(cal.getTime());
						dateList.add(date);
					} while (date.compareTo(endTime) < 0);
				}	
					for(String queryDate:dateList){
						OperationEnvironment env=new OperationEnvironment();
						TradeBasicQueryRequest req=new TradeBasicQueryRequest();
						List<String> productCodes=new ArrayList<String>();
						productCodes.add("10241");
						productCodes.add("10231");
						req.setProductCodes(productCodes);
						req.setGmtStart(sdf.parse(queryDate+" 00:00:00"));
						req.setGmtEnd(sdf.parse(queryDate+" 23:59:59"));
						QueryBase queryBase=new QueryBase();
						queryBase.setPageSize(20000);
						req.setQueryBase(queryBase);
						List<String>  tradeStatus=new ArrayList<String>();
						tradeStatus.add("301");
						tradeStatus.add("401");
						req.setTradeStatus(tradeStatus);
						req.setTimeQueryType(TimeQueryType.MODIFIED);
						TradeBasicQueryResponse response=tradeQueryFacade.queryList(req, env);
						logger.info("capitalAndSalaryToAccount response");
						List<TradeBasicInfo> tradeBasicInfoList=response.getTradeBasicInfoList();
						
						List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
						TradeOrderInfo tradeOrderInfo=null;
						if(tradeBasicInfoList!=null && !tradeBasicInfoList.isEmpty()){
							logger.info("capitalAndSalaryToAccount size:",tradeBasicInfoList.size());
							for(TradeBasicInfo tbi:tradeBasicInfoList){
								tradeOrderInfo=new TradeOrderInfo();
								tradeOrderInfo.setBizProductCode(tbi.getBizProductCode());
								tradeOrderInfo.setTradeVoucherNo(tbi.getTradeVoucherNo());
								tradeOrderInfo.setTradeSrcVoucherNo(tbi.getTradeSourceVoucherNo());
								tradeOrderInfo.setSourceBatchNo(tbi.getSourceBatchNo());
								tradeOrderInfo.setBatchOrderNo(tbi.getBatchNo());
								tradeOrderInfo.setBizNo(tbi.getBizNo());
								tradeOrderInfo.setTradeAmount(tbi.getTradeAmount().getAmount());
								tradeOrderInfo.setTradeType(tbi.getTradeType().getCode());
								tradeOrderInfo.setBuyerId(tbi.getBuyerId());
								tradeOrderInfo.setBuyerName(tbi.getBuyerName());
								tradeOrderInfo.setFee(tbi.getPayerFee().getAmount());
								tradeOrderInfo.setSellerId(tbi.getSellerId());
								tradeOrderInfo.setSellerName(tbi.getSellerName());
								tradeOrderInfo.setExtension(tbi.getExtention());
								tradeOrderInfo.setStatus(tbi.getStatus());
								tradeOrderInfo.setTradeMemo(tbi.getTradeMemo());;
								tradeOrderInfo.setGmtCreate(tbi.getGmtSubmit());
								tradeOrderInfo.setGmtModified(tbi.getGmtPaid());
								tradeOrderInfo.setProdDesc(tbi.getProdDesc());
								tradeOrderInfo.setHasDownload("0");
								tradeOrderInfo.setSource("1");
								newList.add(tradeOrderInfo);
							}
						}
						logger.info("capitalAndSalaryToAccount er start");
						erServiceFacade.batchInsertTradeOrder(newList);
						logger.info("capitalAndSalaryToAccount er end");
						System.out.println("account end");
					
					}
					logger.info("capitalAndSalaryToAccount begin");
				} catch (Exception e) {
					logger.error("error",e);
				}
		}
		logger.info("capitalAndSalaryToAccount end");
	}
	//转账到银行卡10220,10221
	public void dataMigrationBankCard() throws ParseException{
		logger.info("dataMigrationBankCard start");
		if(this.loadCache("dataMigrationBankCard")){
			logger.info("dataMigrationBankCard loadCache");
			String startTime="2015-05-17";
			String endTime="2015-05-30";
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d = null;
			try {
				d = sdf.parse(startTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			cal.setTime(d);
			List<String> dateList = new ArrayList<String>();
			dateList.add("2015-06-11");
			if (startTime.equals(endTime)) {
				dateList.add(startTime);
			} else {
				String date = null;
				dateList.add(startTime);
				do {
					cal.add(Calendar.DAY_OF_MONTH, 1);
					date = sdf.format(cal.getTime());
					dateList.add(date);
				} while (date.compareTo(endTime) < 0);
			}	
			
			try {
				for(String queryDate:dateList){
					OperationEnvironment env=new OperationEnvironment();
					FundoutQueryRequest queryRequest=new FundoutQueryRequest();
					queryRequest.setProductCode("10220,10221");
					queryRequest.setResultTimeStart(sdfhms.parse(queryDate+" 00:00:00"));
					queryRequest.setResultTimeEnd(sdfhms.parse(queryDate+" 23:59:59"));
					queryRequest.setPageSize(20000);
					queryRequest.setStatus("success");
		            FundoutQueryResponse response = fundoutFacade.queryFundoutOrderInfo(queryRequest, env);
		            logger.info("dataMigrationBankCard response");
		            List<FundoutInfo> list = response.getFundoutInfoList();
					List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
					TradeOrderInfo tradeOrderInfo=null;
					if(list!=null && !list.isEmpty()){
						logger.info("dataMigrationBankCard size:",list.size());
						for(FundoutInfo fif:list){
							tradeOrderInfo=new TradeOrderInfo();
							tradeOrderInfo.setTradeVoucherNo(fif.getFundoutOrderNo());
							tradeOrderInfo.setBizProductCode(fif.getProductCode());
							tradeOrderInfo.setBuyerId(fif.getMemberId());
							tradeOrderInfo.setAccountNo(fif.getAccountNo());
							tradeOrderInfo.setTradeAmount(fif.getAmount().getAmount());
							tradeOrderInfo.setFee(fif.getFee().getAmount());
							tradeOrderInfo.setCardNo(fif.getCardNo());
							tradeOrderInfo.setCardType(fif.getCardType());
							tradeOrderInfo.setName(fif.getName());
							tradeOrderInfo.setBankCode(fif.getBankCode());
							tradeOrderInfo.setBankName(fif.getBankName());
							tradeOrderInfo.setBranchName(fif.getBranchName());
							tradeOrderInfo.setBankLineNo(fif.getBankLineNo());
							tradeOrderInfo.setProv(fif.getProv());
							tradeOrderInfo.setCity(fif.getCity());
							tradeOrderInfo.setCompanyPersonal(fif.getCompanyOrPersonal());
							tradeOrderInfo.setFundoutGrade(fif.getFundoutGrade());
							tradeOrderInfo.setStatus(fif.getStatus());
							tradeOrderInfo.setTradeMemo(fif.getPurpose());
							tradeOrderInfo.setOrderTime(fif.getOrderTime());
							tradeOrderInfo.setExtension(fif.getExtension());
							tradeOrderInfo.setGmtCreate(fif.getOrderTime());
							tradeOrderInfo.setResultTime(fif.getResultTime());//++
							tradeOrderInfo.setAccountType(fif.getAccountType());//++
							tradeOrderInfo.setResultDesc(fif.getResultDesc());//++
							tradeOrderInfo.setSourceBatchNo(fif.getSourceBatchNo());
							tradeOrderInfo.setBatchOrderNo(fif.getBatchOrderNo());
							tradeOrderInfo.setTradeSrcVoucherNo(fif.getOutOrderNo());
							tradeOrderInfo.setSource("2");
							tradeOrderInfo.setHasDownload("0");
							newList.add(tradeOrderInfo);
						}
						logger.info("dataMigrationBankCard er start");
						erServiceFacade.batchInsertTradeOrder(newList);
						logger.info("dataMigrationBankCard er end");
						Thread.sleep(20000);
					}
				}
			} catch (Exception e) {
				logger.error("dataMigrationBankCard Exception",e);
			}
		}
		logger.info("dataMigrationBankCard end");
	}
	/**
	 * 10240  ,"资金归集到银行卡" 10230  ,"代发工资到银行卡"  (针对于10240、10230的业务品种)对指定日期区间的数据的迁移
	 * @throws ParseException
	 */
	public void capitalAndSalaryToBankCard() throws ParseException{
		logger.info("capitalAndSalaryToBankCard begin");
		if(this.loadCache("capitalAndSalaryToBankCard")){
			logger.info("capitalAndSalaryToBankCard loadCache");
			String startTime="2015-05-17";
			String endTime="2015-05-30";
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d = null;
			try {
				d = sdf.parse(startTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			cal.setTime(d);
			List<String> dateList = new ArrayList<String>();
			dateList.add("2015-06-11");
			if (startTime.equals(endTime)) {
				dateList.add(startTime);
			} else {
				String date = null;
				dateList.add(startTime);
				do {
					cal.add(Calendar.DAY_OF_MONTH, 1);
					date = sdf.format(cal.getTime());
					dateList.add(date);
				} while (date.compareTo(endTime) < 0);
			}	
			
			try {
				for(String queryDate:dateList){
					OperationEnvironment env=new OperationEnvironment();
					FundoutQueryRequest queryRequest=new FundoutQueryRequest();
					queryRequest.setProductCode("10240,10230");
					queryRequest.setResultTimeStart(sdfhms.parse(queryDate+" 00:00:00"));
					queryRequest.setResultTimeEnd(sdfhms.parse(queryDate+" 23:59:59"));
					queryRequest.setPageSize(20000);
					queryRequest.setStatus("success");
		            FundoutQueryResponse response = fundoutFacade.queryFundoutOrderInfo(queryRequest, env);
		            logger.info("capitalAndSalaryToBankCard response");
		            List<FundoutInfo> list = response.getFundoutInfoList();
					List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
					TradeOrderInfo tradeOrderInfo=null;
					if(list!=null && !list.isEmpty()){
						logger.info("capitalAndSalaryToBankCard size:",list.size());
						for(FundoutInfo fif:list){
							tradeOrderInfo=new TradeOrderInfo();
							tradeOrderInfo.setTradeVoucherNo(fif.getFundoutOrderNo());
							tradeOrderInfo.setBizProductCode(fif.getProductCode());
							tradeOrderInfo.setBuyerId(fif.getMemberId());
							tradeOrderInfo.setAccountNo(fif.getAccountNo());
							tradeOrderInfo.setTradeAmount(fif.getAmount().getAmount());
							tradeOrderInfo.setFee(fif.getFee().getAmount());
							tradeOrderInfo.setCardNo(fif.getCardNo());
							tradeOrderInfo.setCardType(fif.getCardType());
							tradeOrderInfo.setName(fif.getName());
							tradeOrderInfo.setBankCode(fif.getBankCode());
							tradeOrderInfo.setBankName(fif.getBankName());
							tradeOrderInfo.setBranchName(fif.getBranchName());
							tradeOrderInfo.setBankLineNo(fif.getBankLineNo());
							tradeOrderInfo.setProv(fif.getProv());
							tradeOrderInfo.setCity(fif.getCity());
							tradeOrderInfo.setCompanyPersonal(fif.getCompanyOrPersonal());
							tradeOrderInfo.setFundoutGrade(fif.getFundoutGrade());
							tradeOrderInfo.setStatus(fif.getStatus());
							tradeOrderInfo.setTradeMemo(fif.getPurpose());
							tradeOrderInfo.setOrderTime(fif.getOrderTime());
							tradeOrderInfo.setExtension(fif.getExtension());
							tradeOrderInfo.setGmtCreate(fif.getOrderTime());
							tradeOrderInfo.setResultTime(fif.getResultTime());//++
							tradeOrderInfo.setAccountType(fif.getAccountType());//++
							tradeOrderInfo.setResultDesc(fif.getResultDesc());//++
							tradeOrderInfo.setSourceBatchNo(fif.getSourceBatchNo());
							tradeOrderInfo.setBatchOrderNo(fif.getBatchOrderNo());
							tradeOrderInfo.setTradeSrcVoucherNo(fif.getOutOrderNo());
							tradeOrderInfo.setSource("2");
							tradeOrderInfo.setHasDownload("0");
							newList.add(tradeOrderInfo);
						}
						logger.info("capitalAndSalaryToBankCard er start");
						erServiceFacade.batchInsertTradeOrder(newList);
						logger.info("capitalAndSalaryToBankCard er end");
						Thread.sleep(20000);
					}
				}
			} catch (Exception e) {
				logger.error("error",e);
			}
	   }
		logger.info("capitalAndSalaryToBankCard end");
	}
	/*
	 *电子回单 退票 --迁移指定日期区间的数据	
	 */
	public void refundTicket(){
		logger.info("refundTicket begin");
		if(this.loadCache("refundTicket")){
			logger.info("refundTicket loadCache");
			String startTime="2015-05-17";
			String endTime="2015-05-30";
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d = null;
			try {
				d = sdf.parse(startTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			cal.setTime(d);
			List<String> dateList = new ArrayList<String>();
			if (startTime.equals(endTime)) {
				dateList.add(startTime);
			} else {
				String date = null;
				dateList.add(startTime);
				do {
					cal.add(Calendar.DAY_OF_MONTH, 1);
					date = sdf.format(cal.getTime());
					dateList.add(date);
				} while (date.compareTo(endTime) < 0);
			}	
			dateList.add("2015-06-11");
			try {
				for(String queryDate:dateList){
					OperationEnvironment env=new OperationEnvironment();
					PageResultList page8 = new PageResultList();
					FundoutQuery fundoutQuery8 = new FundoutQuery();
					fundoutQuery8.setStatus("success");
					fundoutQuery8.setCurrentPage(1);
					fundoutQuery8.setPageSize(20000);
					fundoutQuery8.setRefundTimeStart(sdfhms.parse(queryDate+" 00:00:00"));
					fundoutQuery8.setRefundTimeEnd(sdfhms.parse(queryDate+" 23:59:59"));
					page8 = defaultFundoutService.queryFundoutOrderInfo(fundoutQuery8, env);
					logger.info("refundTicket page8");
					List<Fundout> fundoutlist=page8.getInfos();
					List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
					TradeOrderInfo tradeOrderInfo=null;
					if(fundoutlist!=null && !fundoutlist.isEmpty()){
						logger.info("refundTicket size:",fundoutlist.size());
						for(Fundout fif:fundoutlist){
							tradeOrderInfo=new TradeOrderInfo();
							tradeOrderInfo.setTradeVoucherNo(fif.getFundoutOrderNo());
							tradeOrderInfo.setBizProductCode(fif.getProductCode());
							tradeOrderInfo.setBuyerId(fif.getMemberId());
							tradeOrderInfo.setAccountNo(fif.getAccountNo());
							tradeOrderInfo.setTradeAmount(fif.getAmount().getAmount());
							tradeOrderInfo.setFee(fif.getFee().getAmount());
							tradeOrderInfo.setCardNo(fif.getCardNo());
							tradeOrderInfo.setCardType(fif.getCardType());
							tradeOrderInfo.setName(fif.getName());
							tradeOrderInfo.setBankCode(fif.getBankCode());
							tradeOrderInfo.setBankName(fif.getBankName());
							tradeOrderInfo.setBranchName(fif.getBranchName());
							tradeOrderInfo.setBankLineNo(fif.getBankLineNo());
							tradeOrderInfo.setProv(fif.getProv());
							tradeOrderInfo.setCity(fif.getCity());
							tradeOrderInfo.setCompanyPersonal(fif.getCompanyOrPersonal());
							tradeOrderInfo.setFundoutGrade(fif.getFundoutGrade());
							if("success".equals(fif.getStatus())){
								tradeOrderInfo.setStatus("refundTicket");
							}
							tradeOrderInfo.setTradeMemo(fif.getPurpose());
							tradeOrderInfo.setOrderTime(fif.getOrderTime());
							tradeOrderInfo.setExtension(fif.getExtension());
							tradeOrderInfo.setGmtCreate(fif.getRefundTime());
							tradeOrderInfo.setSourceBatchNo(fif.getSourceBatchNo());
							tradeOrderInfo.setBatchOrderNo(fif.getBatchOrderNo());
							tradeOrderInfo.setTradeSrcVoucherNo(fif.getOutOrderNo());
							tradeOrderInfo.setSource("2");
							tradeOrderInfo.setHasDownload("0");
							newList.add(tradeOrderInfo);
						}
						logger.info("refundTicket er start");
						erServiceFacade.batchInsertTradeOrder(newList);
						logger.info("refundTicket er end");
						Thread.sleep(1000);
					}
				}
			} catch (Exception e) {
				logger.error("error",e);
			}
			
	   }
		logger.info("refundTicket end");
	
	}
}
