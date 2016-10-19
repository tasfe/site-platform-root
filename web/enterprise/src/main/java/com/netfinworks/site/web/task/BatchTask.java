package com.netfinworks.site.web.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.fos.service.facade.domain.FundoutInfoSummary;
import com.netfinworks.fos.service.facade.enums.FundoutStatus;
import com.netfinworks.ma.service.base.model.MerchantInfo;
import com.netfinworks.ma.service.response.MerchantListResponse;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.enums.TradeTypeRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.ServiceException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.trade.impl.SettlementServiceImpl;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.common.util.FormatForDate;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;
import com.netfinworks.ufs.client.UFSClient;
import com.netfinworks.ufs.client.ctx.FileFileContext;
@Controller
public class BatchTask {
	@Resource(name = "enterpriseTradeService")
	private SettlementServiceImpl settlementServiceImpl;
	
	@Resource(name = "defaultFundoutService")
	private DefaultFundoutService defaultFundoutService;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "tradeService")
	private TradeService tradeService;
	
	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
	@Resource(name = "ufsClient")
	private UFSClient   ufsClient;
	
	/**本地文件目录*/
	@Value("${merchantTradeFilePath}")
 	private String merchantTradeFilePath;
	
	@Resource
	private XUCache<String> xuCache;
	
	/**
	 * 文件路径分隔符
	 */
	private static final String fileSeparator=File.separator;	 
	 
	private static final Logger logger = LoggerFactory.getLogger(BatchTask.class);
	
	/***/
	@Resource(name = "webResource")
    private WebDynamicResource        webResource;
	
	/**
	 * 结算对账单
	 * @param startTime
	 * @param endTime
	 * @param memberId
	 * @param tradeFlag  1.网关交易对账单  2.退款对账单 3.付款到账户对账单 6.银行卡代扣
	 * @param fileType  区分是日对账和月对账  (例  MM：201503 DD:20150323)
	 * @throws Exception
	 */
	public  HSSFWorkbook trade(Date startTime,Date endTime,String memberId,String tradeFlag,String fileType) throws Exception{
		QueryBase queryBase = new QueryBase();
		// 1.查询请求：结算对账单流水明细
		CoTradeRequest listSreq = new CoTradeRequest();
		// 2.查询请求：汇总
		CoTradeRequest sumSreq = new CoTradeRequest();		
		// 查询时间			
		listSreq.setBeginTime(startTime);
		sumSreq.setBeginTime(startTime);		
		sumSreq.setEndTime(endTime);		
		listSreq.setEndTime(endTime);	
		//商户Id
		listSreq.setMemberId(memberId);
		sumSreq.setMemberId(memberId);
		// 分页
		queryBase.setCurrentPage(1);
		queryBase.setPageSize(50000);
		listSreq.setQueryBase(queryBase);
		sumSreq.setQueryBase(queryBase);
		// 需要汇总 (汇总请求 querySummary ，不在用 queryList里的汇总，所以这里值为fasle)
		listSreq.setNeedSummary(false);
		sumSreq.setNeedSummary(true);
		// 是否发生过结算
		listSreq.setHasSettled(true);
		//sumSreq.setHasSettled(true);
		// 交易状态
		List<String> tradeStatus = new ArrayList<String>();
		String[] sumTradeStatus = null;
		List<String> productCode = new ArrayList<String>();
		List<TradeTypeRequest> tradeType=new ArrayList<TradeTypeRequest>();		
		if("1".equals(tradeFlag)){
			sumTradeStatus = new String[] {"401","951"};//201：付款成功  301：交易成功  401：交易结束  901：处理中(结算成功) 951：退款成功 998：交易失败
			tradeType.add(TradeTypeRequest.INSTANT_ACQUIRING);
			productCode.add(TradeType.PAY_INSTANT.getBizProductCode());
			productCode.add(TradeType.phone_recharge.getBizProductCode());
			listSreq.setProductCodes(productCode);
			sumSreq.setProductCodes(productCode);
		}else if("2".equals(tradeFlag)){
			sumTradeStatus = new String[] { "401","951","998" };//201：付款成功  301：交易成功  401：交易结束  901：处理中(结算成功) 951：退款成功 998：交易失败
			tradeType.add(TradeTypeRequest.REFUND_ACQUIRING);
			productCode.add(TradeType.POS.getBizProductCode());
			listSreq.setIgnoreProductCodes(productCode);
			sumSreq.setIgnoreProductCodes(productCode);
		}else if("3".equals(tradeFlag)){
			sumTradeStatus = new String[] { "401","951","998" };//201：付款成功  301：交易成功  401：交易结束  901：处理中(结算成功) 951：退款成功 998：交易失败
			tradeType.add(TradeTypeRequest.INSTANT_TRASFER);
		}else if("6".equals(tradeFlag)){
		    sumTradeStatus = new String[] {"401"};
		    tradeType.add(TradeTypeRequest.INSTANT_ACQUIRING);//原数据tradeType为11
		    tradeType.add(TradeTypeRequest.BANK_WITHHOLDING);//新数据tradeType为17
		    productCode.add(TradeType.bank_withholding.getBizProductCode());
		    listSreq.setProductCodes(productCode);
			sumSreq.setProductCodes(productCode);
		}else if("7".equals(tradeFlag)){
			sumTradeStatus = new String[] {"401"};//{"401","951"};//201：付款成功  301：交易成功  401：交易结束  901：处理中(结算成功) 951：退款成功 998：交易失败
			tradeType.add(TradeTypeRequest.ENSURE_ACQUIRING);
			productCode.add(TradeType.POS.getBizProductCode());
			listSreq.setProductCodes(productCode);
			sumSreq.setProductCodes(productCode);
		}else if("8".equals(tradeFlag)){
			sumTradeStatus = new String[] { "401","951","998" };//201：付款成功  301：交易成功  401：交易结束  901：处理中(结算成功) 951：退款成功 998：交易失败
			tradeType.add(TradeTypeRequest.REFUND_ACQUIRING);
			productCode.add(TradeType.POS.getBizProductCode());//pos
			listSreq.setProductCodes(productCode);
			sumSreq.setProductCodes(productCode);
		}
		tradeStatus = Arrays.asList(sumTradeStatus);
		listSreq.setStatus(tradeStatus);
		sumSreq.setStatus(tradeStatus);			
		
		listSreq.setTradeType(tradeType);
		sumSreq.setTradeType(tradeType);
		
//		listSreq.setProductCodes(productCode);
//		sumSreq.setProductCodes(productCode);
		// 查询结算流水、汇总信息
		CoTradeQueryResponse rep = null;
		HSSFWorkbook ws=null; 
		try {
			rep = settlementServiceImpl.queryList(listSreq, sumSreq);
			ExcelUtil excelUtil = new ExcelUtil();			
			if(null!=rep && null!=rep.getBaseInfoList() && rep.getBaseInfoList().size()>0){
			   if("7".equals(tradeFlag)){
				   List<BaseInfo> posbaseInfoList= rep.getBaseInfoList();
				   if(posbaseInfoList!=null){
					   for(BaseInfo info:posbaseInfoList){
							String payChannel = tradeService.queryPayMethods(info.getTradeVoucherNo(), null);
							if(payChannel.equals("05")){
								info.setPayChannel("纯贷（银行卡）");
							}else if(payChannel.equals("71")){
								info.setPayChannel("纯借（银行卡）");
							}else if(payChannel.equals("72")){
								info.setPayChannel("微信");
							}else if(payChannel.equals("73")){
								info.setPayChannel("支付宝");
							}else{
								info.setPayChannel(null);
							}
						}
				   }
				   rep.setBaseInfoList(posbaseInfoList);
			   }
		       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	           SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM");
		       String tradeTime; 			
			   if("MM".equals(fileType)){
		    	  tradeTime=sdFormat.format(endTime);
		        }else{
		          tradeTime=sdf.format(endTime);
		       }		
			    if("1".equals(tradeFlag)){			
					 ws=excelUtil.toExcel5(rep.getBaseInfoList(),rep.getSummaryInfo(), tradeTime);	
				}else if("2".equals(tradeFlag)){
					 ws=excelUtil.toExcel6(rep.getBaseInfoList(),rep.getSummaryInfo(), tradeTime);	
				}else if("3".equals(tradeFlag)){
					 ws=excelUtil.toExcel7(rep.getBaseInfoList(),rep.getSummaryInfo(), tradeTime);	
			    }else if("6".equals(tradeFlag)){
			         ws=excelUtil.toExcel10(rep.getBaseInfoList(),rep.getSummaryInfo(), tradeTime);  
			    }else if("7".equals(tradeFlag)){
			         ws=excelUtil.toExcel11(rep.getBaseInfoList(),rep.getSummaryInfo(), tradeTime);  
			    }else if("8".equals(tradeFlag)){
			         ws=excelUtil.toExcel12(rep.getBaseInfoList(),rep.getSummaryInfo(), tradeTime);  
			    }
			    logger.info("商户:{} 结算对账单：{} ,生成Excel文件成功",memberId,startTime);
			}
		}catch (BizException e) {	
			logger.error("系统内部错误", e);
			logger.error("商户:{} 结算对账单：{} ,生成Excel文件出错",memberId,startTime);	
		}		
		return ws;
	}
	
	/**
	 * 结算对账单
	 * @param startTime
	 * @param endTime
	 * @param memberId
	 * @param tradeFlag    4.付款到卡对账单  5.退票对账单
	 * @param fileType     区分是日对账和月对账  (例  MM：201503 DD:20150323)
	 * @return
	 * @throws Exception
	 */	
	private HSSFWorkbook queryFundoutOrderInfo (Date startTime,Date endTime,String memberId,String tradeFlag,String fileType) throws Exception{
		OperationEnvironment env=new OperationEnvironment();
		FundoutQuery fundoutQuery = new FundoutQuery();
		fundoutQuery.setMemberId(memberId);
		fundoutQuery.setProductCode(StringUtils.join(new String[]{TradeType.PAY_TO_BANK.getBizProductCode(),TradeType.PAY_TO_BANK_INSTANT.getBizProductCode()},","));
		fundoutQuery.setCurrentPage(1);
		fundoutQuery.setPageSize(50000);
		String status;
		if("4".equals(tradeFlag)){
			status = StringUtils.join(new String[]{FundoutStatus.SUCCESS.getCode(), FundoutStatus.FAILED.getCode()}, ",");
			fundoutQuery.setStatus(status);		
			fundoutQuery.setResultTimeStart(startTime);
			fundoutQuery.setResultTimeEnd(endTime);
		}else if("5".equals(tradeFlag)){
			fundoutQuery.setStatus(FundoutStatus.SUCCESS.getCode());
			fundoutQuery.setHasOutOrderNo(true);
			fundoutQuery.setRefundTimeStart(startTime);
			fundoutQuery.setRefundTimeEnd(endTime);
		}		
		
		PageResultList page=null;
		FundoutInfoSummary fundoutInfoSummary=null;
		HSSFWorkbook ws=null; 
		ExcelUtil excelUtil = new ExcelUtil();		
		try {
			page = defaultFundoutService.queryFundoutOrderInfo(fundoutQuery, env);			
			if(null!=page&&null!=page.getInfos()&& page.getInfos().size()>0){
				fundoutInfoSummary = defaultFundoutService.queryFundoutOrderInfoSummary(fundoutQuery, env);
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM");
		        String tradeTime; 			
			    if("MM".equals(fileType)){
		    	   tradeTime=sdFormat.format(endTime);
		        }else{
		          tradeTime=sdf.format(endTime);
		        }
			    if("4".equals(tradeFlag)){
			    	 ws=excelUtil.toExcel8(page.getInfos(),fundoutInfoSummary, tradeTime);	
			    }else if("5".equals(tradeFlag)){
			    	 ws=excelUtil.toExcel9(page.getInfos(),fundoutInfoSummary, tradeTime);	
				}	
			}
			
		}catch (ServiceException e) {
			logger.error("系统内部错误", e);
			logger.error("商户:{} 结算对账单：{} ,生成Excel文件出错",memberId,startTime);	
		}
		return ws;
	}
	
	/**
	 * 查询所有认证后的商户
	 * @return
	 */
	public List<String> queryVerfiedMerchantIds(){
		List<String> list=new ArrayList<String>();		
		try {
			MerchantListResponse response=memberService.queryVerfiedMerchants();
			if(null!=response && ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())){
				List<MerchantInfo> merchantInfos=response.getMerchantInfos();
				for(MerchantInfo info:merchantInfos){
					list.add(info.getMemberId());
				}
			}
		} catch (BizException e) {
			logger.error("系统内部错误",e);
		}
		return list;
	}
	
	public  boolean loadCache(String methodName){		
		String cacheKey=methodName+"_"+DateUtils.getStringDate("yyyy-MM-dd");
		logger.info("loadCache key={}",cacheKey);
		boolean result=xuCache.add(cacheKey, "true", 1*24*60*60);
		return result;
	}
	
	
	
	/**
	 * 月对账单上传
	 */
	public void upLoadTradeMonth(){			
		if(this.loadCache("upLoadTradeMonth")){
			logger.info("upLoadTradeMonth begin");
			FormatForDate formatForDate = new FormatForDate();
			String PreviousMonth=formatForDate.getPreviousMonthEnd();
			String queryDate=PreviousMonth.substring(0,7);
			try {
				List<String> merchantIds=this.queryVerfiedMerchantIds();
				for(String memberId:merchantIds){
					this.uploadTrade(memberId, queryDate, "1", "MM");
					this.uploadTrade(memberId, queryDate, "2", "MM");
					this.uploadTrade(memberId, queryDate, "3", "MM");
					this.uploadTrade(memberId, queryDate, "4", "MM");
					this.uploadTrade(memberId, queryDate, "5", "MM");
					this.uploadTrade(memberId, queryDate, "6", "MM");
//					this.uploadTradePos(memberId, queryDate, "7", "MM");//pos消费
//					this.uploadTradePos(memberId, queryDate, "8", "MM");//pos撤销
					
				}			
			} catch (Exception e) {
				logger.error("error",e);
			}
			logger.info("upLoadTradeMonth end");
		}
	}
	
	/**
	 * 日对账单上传
	 */
	public void upLoadTradeDay(){		
	   if(this.loadCache("upLoadTradeDay")){
		   logger.info("upLoadTradeDay begin");
			String queryDate=DateUtils.getBeforeDate("yyyy-MM-dd");
			try {
				List<String> merchantIds=this.queryVerfiedMerchantIds();
				for(String memberId:merchantIds){
					this.uploadTrade(memberId, queryDate, "1", "DD");
					this.uploadTrade(memberId, queryDate, "2", "DD");
					this.uploadTrade(memberId, queryDate, "3", "DD");
					this.uploadTrade(memberId, queryDate, "4", "DD");
					this.uploadTrade(memberId, queryDate, "5", "DD");
					this.uploadTrade(memberId, queryDate, "6", "DD");
				}
			} catch (Exception e) {
				logger.error("error",e);
			}
			logger.info("upLoadTradeDay end");
	   }
	}
	
	/**
	 * 传统pos月对账单上传
	 */
	public void upLoadPosTradeMonth(){			
		if(this.loadCache("PosMonth")){
			logger.info("upLoadPosTradeMonth begin");
			FormatForDate formatForDate = new FormatForDate();
			String PreviousMonth=formatForDate.getPreviousMonthEnd();
			String queryDate=PreviousMonth.substring(0,7);
			try {
				List<String> merchantIds=this.queryVerfiedMerchantIds();
				for(String memberId:merchantIds){
					this.uploadTradePos(memberId, queryDate, "7", "MM");//pos消费
//					this.uploadTradePos(memberId, queryDate, "8", "MM");//pos撤销
				}			
			} catch (Exception e) {
				logger.error("error",e);
			}
			logger.info("upLoadPosTradeMonth end");
		}
	}
	
	/**
	 * 传统pos日对账单上传
	 */
	public void upLoadPosTradeDay(){		
	   if(this.loadCache("PosDay")){
		    logger.info("upLoadPosTradeDay begin");
		    //获取连连POS会员id
		    String[] otherMemberId = webResource.getLlPOSMemberId().split(",");
			String queryDate=DateUtils.getStringDate("yyyy-MM-dd");//今天
//			String queryDate2=DateUtils.getBeforeDate("yyyy-MM-dd");//昨天
			try {
				List<String> merchantIds=this.queryVerfiedMerchantIds();
				List<String> memberIds=new ArrayList<String>();
				memberIds.addAll(merchantIds);
				//去除连连POS会员
				for(String memberId:merchantIds){
					for(int i=0;i<otherMemberId.length;i++){
						if(memberId.equals(otherMemberId[i])){
							memberIds.remove(memberId);
							break;
						}
					}
				}
				for(String memberId:memberIds){
					this.uploadTradePos(memberId, queryDate, "7", "DD");//pos消费
				}
			} catch (Exception e) {
				logger.error("error",e);
			}
			logger.info("upLoadPosTradeDay end");
	   }
	}
	/**
	 * 连连传统pos日对账单上传
	 */
	public void upLoadOtherPosTradeDay(){		
	   if(this.loadCache("OtherPosDay")){
		    logger.info("upLoadOtherPosTradeDay begin");
		    //获取连连POS会员id
		    String[] otherMemberId = webResource.getLlPOSMemberId().split(",");
			String queryDate=DateUtils.getStringDate("yyyy-MM-dd");//今天
			try {
				for(int i=0;i<otherMemberId.length;i++){
					this.uploadTradePos(otherMemberId[i], queryDate, "7", "DD");//pos消费
				}
			} catch (Exception e) {
				logger.error("error",e);
			}
			logger.info("upLoadPosTradeDay end");
	   }
	}
	@RequestMapping(value="/setteleBill/test2.htm")
	public ModelAndView test2(HttpServletRequest request) {		
		logger.info("upLoadOtherPosTradeDay begin");
	    //获取连连POS会员id
	    String[] otherMemberId = webResource.getLlPOSMemberId().split(",");
		String queryDate=DateUtils.getStringDate("yyyy-MM-dd");//今天
		System.out.println(otherMemberId.length);
		try {
			for(int i=0;i<otherMemberId.length;i++){
				this.uploadTradePos(otherMemberId[i], queryDate, "7", "DD");//pos消费
			}
		} catch (Exception e) {
			logger.error("error",e);
		}
		logger.info("upLoadPosTradeDay end");
		return new ModelAndView(CommonConstant.URL_PREFIX
                + "/trade/Success");
	}
	
	/**
	 * 补充日对账单上传
	 */
	@RequestMapping(value="/setteleBill/upLoadTradeDayFill.htm")
	public ModelAndView upLoadTradeDayFill(HttpServletRequest request){		
//	   if(this.loadCache("upLoadTradeDayFill")){
		    logger.info("upLoadTradeDayFill begin");
			String startTime=request.getParameter("startTime");		
			String endTime=request.getParameter("endTime");    
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
			
			try {
				List<String> merchantIds=this.queryVerfiedMerchantIds();				
				for(String memberId:merchantIds){
					for(String queryDate:dateList){
						this.uploadTrade(memberId, queryDate, "1", "DD");
						this.uploadTrade(memberId, queryDate, "2", "DD");
						this.uploadTrade(memberId, queryDate, "3", "DD");
						this.uploadTrade(memberId, queryDate, "4", "DD");
						this.uploadTrade(memberId, queryDate, "5", "DD");
						this.uploadTrade(memberId, queryDate, "6", "DD");
//						this.uploadTradePos(memberId, queryDate, "7", "DD");//pos消费
//						this.uploadTradePos(memberId, queryDate, "8", "DD");//pos撤销
					}
				}
			} catch (Exception e) {
				logger.error("error",e);
			}

			logger.info("upLoadTradeDayFill end");
			return new ModelAndView(CommonConstant.URL_PREFIX
                    + "/trade/Success");
//	   }
	}
	
	
	/**
	 * 补充月对账单上传
	 */
	@RequestMapping(value="/setteleBill/upLoadTradeMonthFill.htm")
	public ModelAndView upLoadTradeMonthFill(HttpServletRequest request){		
//		if(this.loadCache("upLoadTradeMonthFill")){
			logger.info("upLoadTradeMonthFill begin");
			String settleDate=request.getParameter("settleDate");
			List<String> dateList = new ArrayList<String>();				
			dateList.add(settleDate);			
			try {
				List<String> merchantIds=this.queryVerfiedMerchantIds();								
				for(String memberId:merchantIds){
					for(String queryDate:dateList){
						this.uploadTrade(memberId, queryDate, "1", "MM");
						this.uploadTrade(memberId, queryDate, "2", "MM");
						this.uploadTrade(memberId, queryDate, "3", "MM");
						this.uploadTrade(memberId, queryDate, "4", "MM");
						this.uploadTrade(memberId, queryDate, "5", "MM");
						this.uploadTrade(memberId, queryDate, "6", "MM");
//						this.uploadTradePos(memberId, queryDate, "7", "MM");//pos消费
//						this.uploadTradePos(memberId, queryDate, "8", "MM");//pos撤销
					}
				}
			} catch (Exception e) {
				logger.error("error",e);
			}
			logger.info("upLoadTradeMonthFill end");
			return new ModelAndView(CommonConstant.URL_PREFIX
                    + "/trade/Success");
//		}
	}
	
	@RequestMapping(value="/setteleBill/index.htm", method = {RequestMethod.POST,RequestMethod.GET} )
	public ModelAndView setteleBillIndex(HttpServletRequest request){
		return new ModelAndView(CommonConstant.URL_PREFIX
                + "/trade/upLoadPos");
	}
	/**
	 * 补充云+pos日对账单上传
	 */
	@RequestMapping(value="/setteleBill/upLoadPosDayFill.htm", method = RequestMethod.POST )
	public ModelAndView upLoadPosTradeDayFill(HttpServletRequest request,
			String date,String memberId){		
        logger.info("upLoadTradeMonthFill begin");
		try {
			this.uploadTradePos(memberId, date, "7", "DD");
		} catch (Exception e) {
			logger.error("error",e);
		}
		logger.info("upLoadTradeMonthFill end");
		return new ModelAndView(CommonConstant.URL_PREFIX
                + "/trade/Success");
	}
	
	
	
	@RequestMapping(value="/setteleBill/test1.htm")
	public ModelAndView test(HttpServletRequest request) {		
		logger.info("test begin");	
		try {
//			if(this.loadCache("upLoadTradeMonthFill")){
		    String memberId = request.getParameter("memberId");
		    String queryTime = request.getParameter("queryTime");
		    String tradeType = request.getParameter("tradeType");
		    String dateType = request.getParameter("dateType");
				    //this.uploadTrade("200000030006", "2015-01", "3", "MM");
				    this.uploadTrade("200000905051", "2016-04-08", "1", "DD");
					this.uploadTradePos("200000905051", "2016-04-08", "7", "DD");
					this.uploadTradePos("200000905051", "2016-04-08", "8", "DD");
//					this.uploadTrade("200000905051", "2016-04-08", "2", "DD");
//					this.uploadTradePos("200000905051", "2016-04", "8", "MM");
//					this.uploadTrade("200000905051", "2016-04", "2", "MM");
			//this.uploadTrade(memberId, queryTime, tradeType, dateType);	
//			}
		} catch (Exception e) {			
			logger.error("error",e);
		}	
		logger.info("test end");    
		return new ModelAndView(CommonConstant.URL_PREFIX
                + "/trade/Success");
	}
	
	/**
	 * 获取文件名
	 * @param fileName
	 * @param fileType：区分是日对账和月对账  (例  MM：201503 DD:20150323)
	 * @return
	 */
	private String getFileName(String fileName,String fileType){
		String tempFileName=fileName.replaceAll("-", "");
		if("MM".equals(fileType)){
			return tempFileName.substring(0,6);
		}
		return tempFileName;
	}
	/**
	 * 
	 * @param memberId
	 * @param startTime
	 * @param endTime
	 * @param tradeType  
	 * @param fileType ：区分是日对账和月对账  (例  MM：201503 DD:20150323)
	 * @throws Exception 
	 */
	public void uploadTradePos(String memberId,String tradeDate,String tradeType,String fileType){//pos	
		FormatForDate formatForDate = new FormatForDate();
		String secondFormat = "yyyy-MM-dd HH:mm:ss";		
		DateFormat dateFormat = new SimpleDateFormat(secondFormat);	
		OperationEnvironment env = new OperationEnvironment();
		String loginName = null;
		//生成的文件名
		String fileName="";
		if("7".equals(tradeType)){
			fileName="pos"+getFileName(tradeDate,fileType)+".xls";
		}else if("8".equals(tradeType)){
			fileName="refundPos"+getFileName(tradeDate,fileType)+".xls";
		}
		
		Date queryStartTime=null;
		Date queryEndTime=null;
		//生成文件的路径
		String fileDirectory;	  
		OutputStream fOut=null; 
	  	boolean uploadFlag=false;
	  	File file=null;	
	  	HSSFWorkbook ws=null;
	  	try {	  		
	  		if("MM".equals(fileType)){
				String[] yearAndMonth = tradeDate.split("-");			
				int gYear = Integer.parseInt(yearAndMonth[0]);
				int gMonth = Integer.parseInt(yearAndMonth[1]);
				// 获取指定年月的第一天（零点零分零秒开始）
				queryStartTime = formatForDate.getFirstDayOfMonthD(gYear, gMonth);
				// 获取指定年月的最后一天（23点59分59秒结束）
				queryEndTime = formatForDate.getLastDayOfMonthD(gYear, gMonth);	
				fileDirectory=fileSeparator+"batchTask"+fileSeparator+memberId+fileSeparator+"pos"+fileSeparator+getFileName(tradeDate,"MM")+fileSeparator+"month"+fileSeparator;
			}else{
				 queryStartTime=dateFormat.parse(tradeDate+" 00:00:00");		
				 queryEndTime=dateFormat.parse(tradeDate+" 23:59:59");
				 fileDirectory=fileSeparator+"batchTask"+fileSeparator+memberId+fileSeparator+"pos"+fileSeparator+getFileName(tradeDate,"MM")+fileSeparator+"day"+fileSeparator+getFileName(tradeDate,"DD")+fileSeparator;
			}
	  	   	
	  			ws=this.trade(queryStartTime, queryEndTime, memberId,tradeType,fileType);	
	  		
			
			if(null!=ws){				
				logger.info("merchant trade fileDirectory:"+merchantTradeFilePath+fileDirectory);	
				 //判断目录是否存在
		  		file=new File(merchantTradeFilePath+fileDirectory);
			    if(!file.isDirectory()){
			    	file.mkdirs();
			    }
			    //判断文件是否存在
			    file=new File(merchantTradeFilePath+fileDirectory+fileName);
			  	if(file.exists()){
			  	  file.delete();
			  	}
			  	//生成本地Excel
				fOut= new FileOutputStream(merchantTradeFilePath+fileDirectory+fileName);
				ws.write(fOut);		
				fOut.close();				
				uploadFlag=true;				
				logger.info("商户:{} 结算对账单类型：{} 结算对账单日期：{} ,本地生成EXCEL成功",memberId,tradeType,tradeDate);				
				//本地生成成功后，上传到NFS
			  	if(uploadFlag){
			  		FileFileContext ctx=new FileFileContext(fileDirectory+fileName,file);		  			  		
			  		uploadFlag=ufsClient.putFile(ctx);
			  		if(uploadFlag){
			  			logger.info("商户:{} 结算对账单类型：{} 结算对账单：{} ,上传UFS成功",memberId,tradeType,tradeDate);		  		
			  		}else{
			  			logger.info("商户:{} 结算对账单类型：{} 结算对账单：{} ,上传UFS失败",memberId,tradeType,tradeDate);
			  		}
			  	}	
			}else{
				logger.info("商户:{} 结算对账单类型：{} 结算对账单：{},类型：{} 未找到数据",memberId,tradeType,tradeDate,fileType);
			}
		} catch (Exception e) {			
			logger.error("系统错误",e);
			logger.error("商户:{} 结算对账单类型：{} 结算对账单：{} ,生成对账文件出错",memberId,tradeType,tradeDate);			    
		}finally{
			if(null!=fOut){
				try {
					fOut.close();
				} catch (IOException e) {					
					logger.error("error",e);
				}				
			}				
		}
	}
	
	/**
	 * 
	 * @param memberId
	 * @param startTime
	 * @param endTime
	 * @param tradeType  
	 * @param fileType ：区分是日对账和月对账  (例  MM：201503 DD:20150323)
	 * @throws Exception 
	 */
	public void uploadTrade(String memberId,String tradeDate,String tradeType,String fileType){		
		FormatForDate formatForDate = new FormatForDate();
		String secondFormat = "yyyy-MM-dd HH:mm:ss";		
		DateFormat dateFormat = new SimpleDateFormat(secondFormat);	
		OperationEnvironment env = new OperationEnvironment();
		String loginName = null;
		//生成的文件名
		String fileName="";
		if("1".equals(tradeType)){
			fileName="gateway"+getFileName(tradeDate,fileType)+".xls";
		}else if("2".equals(tradeType)){
			fileName="refund"+getFileName(tradeDate,fileType)+".xls";
		}else if("3".equals(tradeType)){
			fileName="account"+getFileName(tradeDate,fileType)+".xls";
		}else if("4".equals(tradeType)){
			fileName="bank"+getFileName(tradeDate,fileType)+".xls";
		}else if("5".equals(tradeType)){
			fileName="ticket"+getFileName(tradeDate,fileType)+".xls";
		}else if("6".equals(tradeType)){
		    try {
	            BaseMember member = memberService.queryMemberById(memberId, env);
	            loginName = member.getLoginName();
	        }
	        catch (Exception e) {
	            logger.error("查询账户信息失败");
	        }
		    fileName=loginName+"bank_withholding"+getFileName(tradeDate,fileType)+".xls";
		}
		Date queryStartTime=null;
		Date queryEndTime=null;
		//生成文件的路径
		String fileDirectory;	  
		OutputStream fOut=null; 
	  	boolean uploadFlag=false;
	  	File file=null;	
	  	HSSFWorkbook ws=null;
	  	try {	  		
	  		if("MM".equals(fileType)){
				String[] yearAndMonth = tradeDate.split("-");			
				int gYear = Integer.parseInt(yearAndMonth[0]);
				int gMonth = Integer.parseInt(yearAndMonth[1]);
				// 获取指定年月的第一天（零点零分零秒开始）
				queryStartTime = formatForDate.getFirstDayOfMonthD(gYear, gMonth);
				// 获取指定年月的最后一天（23点59分59秒结束）
				queryEndTime = formatForDate.getLastDayOfMonthD(gYear, gMonth);	
				fileDirectory=fileSeparator+"batchTask"+fileSeparator+memberId+fileSeparator+getFileName(tradeDate,"MM")+fileSeparator+"month"+fileSeparator;
			}else{
				 queryStartTime=dateFormat.parse(tradeDate+" 00:00:00");		
				 queryEndTime=dateFormat.parse(tradeDate+" 23:59:59");
				 fileDirectory=fileSeparator+"batchTask"+fileSeparator+memberId+fileSeparator+getFileName(tradeDate,"MM")+fileSeparator+"day"+fileSeparator+getFileName(tradeDate,"DD")+fileSeparator;
			}
	  	   	
	  		
	  		if("4".equals(tradeType)||"5".equals(tradeType)){
	  			ws=this.queryFundoutOrderInfo(queryStartTime, queryEndTime, memberId,tradeType,fileType);	
	  		}else{
	  			ws=this.trade(queryStartTime, queryEndTime, memberId,tradeType,fileType);	
	  		}
			
			if(null!=ws){				
				logger.info("merchant trade fileDirectory:"+merchantTradeFilePath+fileDirectory);	
				 //判断目录是否存在
		  		file=new File(merchantTradeFilePath+fileDirectory);
			    if(!file.isDirectory()){
			    	file.mkdirs();
			    }
			    //判断文件是否存在
			    file=new File(merchantTradeFilePath+fileDirectory+fileName);
			  	if(file.exists()){
			  	  file.delete();
			  	}
			  	//生成本地Excel
				fOut= new FileOutputStream(merchantTradeFilePath+fileDirectory+fileName);
				ws.write(fOut);		
				fOut.close();				
				uploadFlag=true;				
				logger.info("商户:{} 结算对账单类型：{} 结算对账单日期：{} ,本地生成EXCEL成功",memberId,tradeType,tradeDate);				
				//本地生成成功后，上传到NFS
			  	if(uploadFlag){
			  		FileFileContext ctx=new FileFileContext(fileDirectory+fileName,file);		  			  		
			  		uploadFlag=ufsClient.putFile(ctx);
			  		if(uploadFlag){
			  			logger.info("商户:{} 结算对账单类型：{} 结算对账单：{} ,上传UFS成功",memberId,tradeType,tradeDate);		  		
			  		}
			  	}	
			}else{
				logger.info("商户:{} 结算对账单类型：{} 结算对账单：{},类型：{} 未找到数据",memberId,tradeType,tradeDate,fileType);
			}
		} catch (Exception e) {			
			logger.error("系统错误",e);
			logger.error("商户:{} 结算对账单类型：{} 结算对账单：{} ,生成对账文件出错",memberId,tradeType,tradeDate);			    
		}finally{
			if(null!=fOut){
				try {
					fOut.close();
				} catch (IOException e) {					
					logger.error("error",e);
				}				
			}				
		}
	}
	
	

}
