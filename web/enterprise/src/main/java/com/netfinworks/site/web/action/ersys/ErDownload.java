package com.netfinworks.site.web.action.ersys;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.ersys.facade.request.TradeListQuery;
import com.netfinworks.ersys.facade.response.TDownloadBHDO;
import com.netfinworks.ersys.facade.response.TradeOrderInfo;
import com.netfinworks.ersys.service.facade.api.ErServiceFacade;
import com.netfinworks.fos.service.facade.FundoutFacade;
import com.netfinworks.fos.service.facade.domain.FundoutInfo;
import com.netfinworks.fos.service.facade.request.FundoutQueryRequest;
import com.netfinworks.fos.service.facade.response.FundoutQueryResponse;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.ErTradeForm;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.tradeservice.facade.api.TradeQueryFacade;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;
import com.netfinworks.tradeservice.facade.response.TradeBasicQueryResponse;
@Controller
public class ErDownload extends BaseAction{
	@Resource(name="erServiceFacade")
	private ErServiceFacade erServiceFacade;
	@Resource(name = "memberService")
    private MemberService memberService;
	
    @Resource(name = "tradeQueryFacade")
    private TradeQueryFacade tradeQueryFacade;
	
    
    @Resource(name="fundoutFacade")
    private FundoutFacade  fundoutFacade;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdfhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
    @Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
    
	/**本地文件目录*/
	@Value("${erFile}")
 	private String erFile;
	
	@Resource
	private XUCache<String> xuCache;
	
	public  boolean loadCache(String methodName){		
		String cacheKey=methodName+"_"+DateUtils.getStringDate("yyyy-MM-dd");
		logger.info("loadCache key={}",cacheKey);
		boolean result=xuCache.add(cacheKey, "true", 1*24*60*60);
		return result;
	}
	@RequestMapping(value="/data/delete.htm")
	public void test3(){
		erServiceFacade.deteleAll();
	}
	@RequestMapping(value="/transfer/accountOnebyOne.htm")
	public void test(HttpServletRequest request){
		logger.info("dataMigrationAccount start account");
		if(this.loadCache("dataMigrationAccount")){
		    logger.info("dataMigrationAccount begin");
			String startTime=request.getParameter("start");
			String endTime=request.getParameter("end");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d = null;
			try {
				d = sdf.parse(startTime);
			} catch (Exception e) {
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
				int num=0;
				for(String queryDate:dateList){
					OperationEnvironment env=new OperationEnvironment();
					PageResultList page= new PageResultList();
					TradeBasicQueryRequest req=new TradeBasicQueryRequest();
					List<String> productCodes=new ArrayList<String>();
					productCodes.add("10310");
					req.setProductCodes(productCodes);
					req.setGmtStart(sdfhms.parse(queryDate+" 00:00:00"));
					req.setGmtEnd(sdfhms.parse(queryDate+" 23:59:59"));
					QueryBase queryBase=new QueryBase();
					queryBase.setPageSize(50000);
					req.setQueryBase(queryBase);
					List<String>  tradeStatus=new ArrayList<String>();
					tradeStatus.add("301");
					tradeStatus.add("401");
					req.setTradeStatus(tradeStatus);
					TradeBasicQueryResponse response=tradeQueryFacade.queryList(req, env);
					List<TradeBasicInfo> tradeBasicInfoList=response.getTradeBasicInfoList();
					List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
					TradeOrderInfo tradeOrderInfo=null;
					if(tradeBasicInfoList!=null && !tradeBasicInfoList.isEmpty()){
						num=num+tradeBasicInfoList.size();
						for(TradeBasicInfo tbi:tradeBasicInfoList){
							tradeOrderInfo=new TradeOrderInfo();
							tradeOrderInfo.setBizProductCode(tbi.getBizProductCode());
							tradeOrderInfo.setTradeVoucherNo(tbi.getTradeVoucherNo());
							tradeOrderInfo.setTradeSrcVoucherNo(tbi.getTradeSourceVoucherNo());
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
						logger.info("erServiceFacade account start account");
						erServiceFacade.batchInsertTradeOrder(newList);
						logger.info("account end account");
						Thread.sleep(2000);
					}
					}
				logger.info("list size:"+num);
			} catch (Exception e) {
				logger.error("dataMigrationAccount Exception account",e);
			}
			logger.info("dataMigrationAccount account end");
	   }
	}
	
	@RequestMapping(value="/transfer/cardOnebyOne.htm")
	public void test1(HttpServletRequest request){
		if(this.loadCache("cardOneByeOne")){
		    logger.info("cardOneByeOne begin");
			String startTime=request.getParameter("start");
			String endTime=request.getParameter("end");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d = null;
			try {
				d = sdf.parse(startTime);
			} catch (Exception e) {
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
				int num=0;
				for(String queryDate:dateList){
					OperationEnvironment env=new OperationEnvironment();
					PageResultList page= new PageResultList();
					
					FundoutQueryRequest queryRequest=new FundoutQueryRequest();
					queryRequest.setProductCode("10220,10221");
					queryRequest.setResultTimeStart(sdfhms.parse(queryDate+" 00:00:00"));
					queryRequest.setResultTimeEnd(sdfhms.parse(queryDate+" 23:59:59"));
					queryRequest.setPageSize(50000);
					queryRequest.setStatus("success");
		            FundoutQueryResponse response = fundoutFacade.queryFundoutOrderInfo(queryRequest, env);
		            List<FundoutInfo> list = response.getFundoutInfoList();
					List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
					TradeOrderInfo tradeOrderInfo=null;
					if(list!=null && !list.isEmpty()){
						num=num+list.size();
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
							tradeOrderInfo.setResultTime(fif.getResultTime());
							tradeOrderInfo.setExtension(fif.getExtension());
							tradeOrderInfo.setGmtCreate(fif.getOrderTime());
							tradeOrderInfo.setResultTime(fif.getResultTime());
							tradeOrderInfo.setAccountType(fif.getAccountType());
							tradeOrderInfo.setResultDesc(fif.getResultDesc());
							tradeOrderInfo.setSourceBatchNo(fif.getSourceBatchNo());
							tradeOrderInfo.setBatchOrderNo(fif.getBatchOrderNo());
							tradeOrderInfo.setSource("2");
							tradeOrderInfo.setHasDownload("0");
							newList.add(tradeOrderInfo);
						}
						logger.info("erServiceFacade card start card");
						erServiceFacade.batchInsertTradeOrder(newList);
						logger.info("card end card");
						Thread.sleep(2000);
					}
				}
				logger.info("list size:"+num);
			} catch (Exception e) {
				logger.error("error",e);
			}
			logger.info("cardOneByeOne end");
	   }
	}
	
	@RequestMapping(value="/transfer/accountYesterday.htm")
	public void test4(){
		if(this.loadCache("dataMigrationAccountYesterday")){
			try {
				logger.info("dataMigrationAccount start");
				OperationEnvironment env=new OperationEnvironment();
				PageResultList page= new PageResultList();
				TradeBasicQueryRequest req=new TradeBasicQueryRequest();
				List<String> productCodes=new ArrayList<String>();
				productCodes.add("10310");
				req.setProductCodes(productCodes);
				req.setGmtStart(sdfhms.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 00:00:00"));
				req.setGmtEnd(sdfhms.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 23:59:59"));
				QueryBase queryBase=new QueryBase();
				queryBase.setPageSize(20000);
				req.setQueryBase(queryBase);
				List<String>  tradeStatus=new ArrayList<String>();
				tradeStatus.add("301");
				tradeStatus.add("401");
				req.setTradeStatus(tradeStatus);
				TradeBasicQueryResponse response=tradeQueryFacade.queryList(req, env);
				List<TradeBasicInfo> tradeBasicInfoList=response.getTradeBasicInfoList();
				
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(tradeBasicInfoList!=null && !tradeBasicInfoList.isEmpty()){
					for(TradeBasicInfo tbi:tradeBasicInfoList){
						tradeOrderInfo=new TradeOrderInfo();
						tradeOrderInfo.setBizProductCode(tbi.getBizProductCode());
						tradeOrderInfo.setTradeVoucherNo(tbi.getTradeVoucherNo());
						tradeOrderInfo.setTradeSrcVoucherNo(tbi.getTradeSourceVoucherNo());
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
			} catch (Exception e) {
				logger.error("dataMigrationAccount Exception",e);
			}
		}
	}
	
	@RequestMapping(value="/transfer/cardYesterday.htm")
	public void test5(){
		if(this.loadCache("dataMigrationBankCardYesterday")){
			try {
				logger.info("dataMigrationBankCard start");
				OperationEnvironment env=new OperationEnvironment();
				PageResultList page= new PageResultList();
				
				FundoutQueryRequest queryRequest=new FundoutQueryRequest();
				queryRequest.setProductCode("10220,10221");
				queryRequest.setResultTimeStart(sdfhms.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 00:00:00"));
				queryRequest.setResultTimeEnd(sdfhms.parse(DateUtils.getBeforeDate("yyyy-MM-dd")+" 23:59:59"));
				queryRequest.setPageSize(20000);
				queryRequest.setStatus("success");
	            FundoutQueryResponse response = fundoutFacade.queryFundoutOrderInfo(queryRequest, env);
	            List<FundoutInfo> list = response.getFundoutInfoList();
				List<TradeOrderInfo> newList=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo tradeOrderInfo=null;
				if(list!=null && !list.isEmpty()){
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
						tradeOrderInfo.setSource("2");
						tradeOrderInfo.setHasDownload("0");
						newList.add(tradeOrderInfo);
					}
					logger.info("erServiceFacade card start");
					erServiceFacade.batchInsertTradeOrder(newList);
					logger.info("card end");
				}
			} catch (Exception e) {
				logger.error("dataMigrationBankCard Exception",e);
			}
		}
	}
	
	@RequestMapping(value="/my/download.htm")
	public ModelAndView searchErAll(HttpServletRequest request,
			HttpServletResponse resp,ErTradeForm form) throws Exception {
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		EnterpriseMember user = getUser(request);
		String id = user.getMemberId();
		
		String txnType=request.getParameter("txnType");
		String tradeType=request.getParameter("tradeType");
		String downStatus=request.getParameter("downStatus");
		String queryStartTime=request.getParameter("queryStartTime");
		String queryEndTime=request.getParameter("queryEndTime");
		String flag=request.getParameter("flag");
		String isNeed=request.getParameter("isNeed");//true为 需要银行卡信息 false或者""为不需要银行卡信息
		String keyword=request.getParameter("keyword");
		String keyvalue=request.getParameter("keyvalue");
		
		String taskName="";
		String BHId="";
		//查询是否存在于表中 
//		String taskDate=request.getParameter("taskDate");
		Date taskDate=new Date();
//		if(!StringUtils.isBlank(taskDate)){
//			date=sdfhms.parse(taskDate);
//		}

		Boolean bo=false;//默认第一次插入
		String isFirst=(String)request.getSession().getAttribute(user.getMemberId()+"_"+taskDate+"_"+flag);
		if(isFirst !=null){
			bo=true;//只做查询
		}else {
			request.getSession().setAttribute(user.getMemberId()+"_"+taskDate+"_"+flag, user.getMemberId()+"_"+taskDate+"_"+flag);
		}

		String txnType1="";
		String status="";
		TradeListQuery listQuery1=new TradeListQuery();
		if(StringUtils.isNotBlank(txnType)&&StringUtils.isNotBlank(tradeType)){
			if(txnType.equals("2") &&tradeType.equals("3")){//转账入款
				taskName="电子回单  转账入款 "+queryStartTime+"~"+queryEndTime;
				txnType1="10310";
			}else if("2".equals(txnType)&&"1".equals(tradeType)){//转账到永达互联网金融 
				txnType1="10310";
				taskName="电子回单 付款到永达互联网金融 "+queryStartTime+"~"+queryEndTime;
			}else if("2".equals(txnType)&&"2".equals(tradeType)){//转账到卡
				txnType1="10220,10221";
				taskName="电子回单 付款到银行卡 "+queryStartTime+"~"+queryEndTime; 
			}else if("3".equals(txnType)&&"1".equals(tradeType)){//代发工资到账户
				txnType1="10231";
				taskName="电子回单 代发工资到永达互联网金融 "+queryStartTime+"~"+queryEndTime;
			}else if("3".equals(txnType)&&"2".equals(tradeType)){//代发工资到银行卡
				txnType1="10230";
				taskName="电子回单 代发工资到银行卡 "+queryStartTime+"~"+queryEndTime; 
			}else if("4".equals(txnType)&&"1".equals(tradeType)){//资金归集到账户
				txnType1="10241";
				taskName="电子回单 资金归集到永达互联网金融 "+queryStartTime+"~"+queryEndTime;
			}else if("4".equals(txnType)&&"2".equals(tradeType)){//资金归集到银行卡
				txnType1="10240";
				taskName="电子回单 资金归集到银行卡 "+queryStartTime+"~"+queryEndTime; 
			}else if("5".equals(txnType)){//退票
				status="refundTicket";
				taskName="电子回单 退票  "+queryStartTime+"~"+queryEndTime;
			}
			if ("3".equals(tradeType)) {
				listQuery1.setSellerId(id);//入款
			}else {
				listQuery1.setBuyerId(id);
			}
		}else{
			txnType1="10310";
			listQuery1.setSellerId(id);//入款
		}
		if(false==bo &&txnType!=null){
			TDownloadBHDO record=new TDownloadBHDO();
			record.setTaskName(taskName);
			record.setMemo(id);
			record.setTaskStatus("1");

			record.setTasksubmitTime(taskDate);
			try {
				BHId=erServiceFacade.insertTDownloadBHDO(record);
			} catch (Exception e) {
				logger.info("插入下载回单的记录",e);
			}
		}
		String currentPage = form.getCurrentPage();// 4 String currentPage =
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		
		if (StringUtils.isNotBlank(queryStartTime)) {
			listQuery1.setStartDate(DateUtils.parseDate(queryStartTime + ":00"));
		} else {
		    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
		    listQuery1.setStartDate(sdfhms.parse(queryStartTime + ":00"));
		}
		if (StringUtils.isNotBlank(queryEndTime)) {
			listQuery1.setEndDate(DateUtils.parseDate(queryEndTime + ":59"));
		} else {
			listQuery1.setEndDate(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
			queryEndTime = sdf.format(new Date()) + " 23:59";
		}
		if (StringUtils.isNotBlank(downStatus)) {
			listQuery1.setHasDownLoad(downStatus);
		}
		if("1".equals(keyword)&&StringUtils.isNotBlank(keyvalue)){
			listQuery1.setSourceBatchNo(keyvalue);
		}else if("2".equals(keyword)&&StringUtils.isNotBlank(keyvalue)){
			listQuery1.setTradeSrcVoucherNo(keyvalue);
		}	
		listQuery1.setTradeType(txnType1);
		
		if(tradeType==null){
			tradeType="";
		}
		if(false==bo &&txnType!=null){
			final EnterpriseMember user1=user;
			final String BHId1=BHId;
			final TradeListQuery listQuery4=listQuery1;
			if("refundTicket".equals(status)){
				listQuery4.setStatus(status);
			}else{
				listQuery4.setStatus("success");//批量只查询成功数据
			}
			final String flag1=flag;
			final String isNeed1=isNeed;
			final String tradeType1=tradeType;
			Thread thread = new Thread(){  
				public void run(){  
					try {
						if("2".equals(flag1)){
							batchDownload(user1,BHId1,listQuery4,isNeed1);
						}else if("1".equals(flag1)){
							moreSingle(user1,BHId1,listQuery4,isNeed1,tradeType1);
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (DocumentException e) {
						e.printStackTrace();
					} catch (BizException e) {
						e.printStackTrace();
					} catch (MemberNotExistException e) {
						e.printStackTrace();
					}
				}  
			};  
			 thread.start(); 
		}

		return new ModelAndView("redirect:/my/downList.htm?taskDate="+taskDate+"&flag="+flag);
	}
	
	@RequestMapping(value="/my/downList.htm")
	public ModelAndView searchErDown(HttpServletRequest request,
			HttpServletResponse resp,ErTradeForm form) throws Exception {
		String taskDate=request.getParameter("taskDate");
		String flag=request.getParameter("flag");
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		EnterpriseMember user = getUser(request);
		request.getSession().removeAttribute(user.getMemberId()+"_"+taskDate+"_"+flag);
		String id = user.getMemberId();
		String currentPage = form.getCurrentPage();// 4 String currentPage =
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		TradeListQuery listQuery=new TradeListQuery();
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.parseInt(currentPage));
		pageInfo.setPageSize(20);
		listQuery.setId(id);
		listQuery.setQueryBase(pageInfo);
		List<TDownloadBHDO> list2=new ArrayList<TDownloadBHDO>();
		try {
			list2=erServiceFacade.selectByDownloadCount(listQuery);
		} catch (Exception e) {
			logger.info("查询电子回单记录条数异常", e);
		}
		int i=0;
		if(null!=list2&&list2.size()>0){
			i=list2.size();
		}
		listQuery.setQueryBase(pageInfo);
		List<TDownloadBHDO> list=new ArrayList<TDownloadBHDO>();
		try {
			list=erServiceFacade.selectTDownloadBHDO(listQuery);
		} catch (Exception e) {
			logger.info("查询批量下载记录:",e);
		}
		// 分页信息及信息列表
		PageResultList resultList = new PageResultList();
		pageInfo.setTotalItem(i);
		resultList.setPageInfo(pageInfo);// 分页信息
		resultList.setInfos(list);// list
		restP.getData().put("page",resultList);
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/ertrade/ertrade_recode_down", "response", restP);
	}
	
	/**
	 * 
	 * @param user
	 * @param BHId1
	 * @param listQuery
	 * @param isNeed1 //true为 需要银行卡信息 false或者""为不需要银行卡信息
	 * @throws IOException
	 * @throws DocumentException
	 * @throws BizException
	 * @throws MemberNotExistException
	 */
	public void batchDownload(EnterpriseMember user,String BHId1,TradeListQuery listQuery,String isNeed1) throws IOException, DocumentException, BizException, MemberNotExistException{
		DecimalFormat df = new DecimalFormat("0.00");
		try{
			logger.info("batchDownload start");
			String id = user.getMemberId();
			BaseMember BuyAccount=new BaseMember();
			BaseMember SellAccount=new BaseMember();
			try {
				BuyAccount=memberService.queryMemberById(id,env);
			} catch (Exception e) {
				logger.info("查询买方信息报错：",e);
			}
			
			List<TradeOrderInfo> list=new ArrayList<TradeOrderInfo>();
			try {
				list=erServiceFacade.queryByTradeCount(listQuery);
			} catch (Exception e) {
				logger.info("查询电子回单记录条数异常", e);
			}
			int pageSize=10;
			String fileName ="";
			String filePath=erFile+File.separator+"batch"+File.separator+id+File.separator+"er"+File.separator+BHId1;
			String savepath="";
			StringBuffer sb=new StringBuffer();
			String tradeType="";
			String tradeTypeName="";
			if(listQuery.getTradeType().equals("10310")){//转账到永达互联网金融 
				tradeType="1";
				tradeTypeName="付款到永达互联网金融";
			}else if(listQuery.getTradeType().equals("10220,10221")){//转账到卡
				tradeType="2";
				tradeTypeName="付款到卡";
			}else if(listQuery.getTradeType().equals("10231")){//代发工资到账户
				tradeType="1";
				tradeTypeName="代发工资到账户";
			}else if(listQuery.getTradeType().equals("10230")){//代发工资到银行卡
				tradeType="2";
				tradeTypeName="代发工资到银行卡";
			}else if(listQuery.getTradeType().equals("10241")){//资金归集到账户
				tradeType="1";
				tradeTypeName="资金归集到账户";
			}else if(listQuery.getTradeType().equals("10240")){//资金归集到银行卡
				tradeType="2";
				tradeTypeName="资金归集到银行卡";
			}else if(listQuery.getStatus().equals("refundTicket")){//退票
				tradeType="0";
				tradeTypeName="退票";
			}
			if(list!=null && !list.isEmpty()){
				logger.info("create batchDownload trade <10");
				//小于10笔交易
				if(list.size()<=pageSize){
			    	   fileName = erFile+File.separator+"batchF.pdf"; 
			    	   //生成批量交易回单
			    	   PdfReader reader = new PdfReader(fileName);
			    	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    	   PdfStamper ps = new PdfStamper(reader, bos);
			    	   BaseFont bf = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
			    	   Font FontChinese = new Font(bf, 5, Font.NORMAL); 
			    	   AcroFields s = ps.getAcroFields();
			    	   s.addSubstitutionFont(bf);
			    	   s.setField("create_time", sdfhms.format(new Date()));
			    	   s.setField("f_full_name",BuyAccount.getMemberName());
					   s.setField("f_acct_no",BuyAccount.getLoginName());
					   s.setField("f_company","永达互联网金融信息服务有限公司");
			    	   s.setField("total",String.valueOf(list.size()));
			    	   int i=1;
			    	   BigDecimal totalAmt=BigDecimal.ZERO;
			    	   List<TradeOrderInfo> listFee=new ArrayList<TradeOrderInfo>();
			    	   for(TradeOrderInfo tof:list){
			    		   if (tradeType.equals("0")) {// 退票
								BigDecimal amount=new BigDecimal(tof.getTradeAmount()==null?0:tof.getTradeAmount().doubleValue());
								BigDecimal tradeFee=new BigDecimal(tof.getFee()==null?0:tof.getFee().doubleValue());
								tof.setTradeAmount(amount.add(tradeFee));
							}
			    		   if(tof.getTradeAmount()!=null){
			    			   totalAmt=totalAmt.add(tof.getTradeAmount());
			    		   }
			    		   if(tof.getFee()!=null && tof.getFee().compareTo(BigDecimal.ZERO)>0){
			    			   listFee.add(tof);
			    		   }
			    		   if(tradeType.equals("1")){
			    			   SellAccount=memberService.queryMemberById(tof.getSellerId(),env);
			    			   s.setField("t_company"+i,"永达互联网金融信息服务有限公司");
			    			   s.setField("num"+i,i+"");
				    		   s.setField("t_full_name"+i,tof.getSellerName());
				    		   s.setField("trade_no"+i,tof.getTradeVoucherNo());
				    		   s.setField("pay_time"+i,tof.getGmtModified()==null?null:sdfhms.format(tof.getGmtModified()));
				    		   s.setField("trans_type"+i,tradeTypeName);
				    		   s.setField("t_acct_no"+i,SellAccount.getLoginName());
			    		   }else{
			    			   s.setField("t_company"+i,tof.getBranchName());
			    			   s.setField("num"+i,i+"");
			    			   s.setField("t_full_name"+i,tof.getName());
				    		   s.setField("trade_no"+i,tof.getTradeVoucherNo());
				    		   s.setField("pay_time"+i,tof.getOrderTime()==null?null:sdfhms.format(tof.getOrderTime()));
				    		   s.setField("trans_type"+i,tradeTypeName);
				    		   String cardNo="";
							   if(tof.getCardNo()!=null && !tof.getCardNo().equals("")){
								   cardNo = uesServiceClient.getDataByTicket(tof.getCardNo());
							   }
							   if(StringUtils.isNotBlank(cardNo)){
								   if(isNeed1.equals("true")){
									   s.setField("t_acct_no"+i,cardNo);
								   }else{
									   s.setField("t_acct_no"+i,"****"+cardNo.substring(cardNo.length()-4));
								   }
							   }
			    		   }
			    		   s.setField("amt"+i,"¥ "+df.format(tof.getTradeAmount())+"  "+digitUppercase(Double.parseDouble(tof.getTradeAmount().toString())));
			    		   s.setField("remark"+i,tof.getTradeMemo());
			    		   i++;
			    	   }
			    	   s.setField("total_amt",df.format(totalAmt));
			    	   
			    	   ps.setFormFlattening(true);
			    	   ps.close();
			    	   File file=new File(filePath);
			    	   if(!file.isDirectory()){
			    		   file.mkdirs();
			    	   }
			    	   String str=sdfhms.format(new Date()).replaceAll(" ","").replaceAll(":","").replaceAll("-","");
			    	   FileOutputStream fos = new FileOutputStream(filePath
                               + File.separator + System.currentTimeMillis()
                               + ".pdf");
			    	  
			    	   fos.write(bos.toByteArray());
			    	   List<File> listFile = getFileSort(filePath);
    	               String[] files = new String[listFile.size()];
    	               for (int n = 0; n < listFile.size(); n++) {
    	                   files[n] = listFile.get(n).getPath();
    	               }
    	               
    	               savepath = filePath + File.separator + "电子回单_"
    	                       + tradeTypeName + "_"+str+"_old" + "_00001.pdf";
    	               String savepathnew = filePath + File.separator + "电子回单_"
    	                       + tradeTypeName + "_"+str + "_00001.pdf";
    	               mergePdfFiles(files, savepath);
    
    	               batchSignature(savepathnew, savepath);//增加电子签章
    	               sb.append(savepathnew);
			    	   if (!tradeType.equals("0")) {//退票没有服务费回单
			    		 //生成费用回单
				    	   if(listFee!=null && !listFee.isEmpty()){
				    		   filePath=erFile+File.separator+"batch"+File.separator+id+File.separator+"fee"+File.separator+BHId1;
				    		   logger.info("create batchDownload fee <10");
				    		   PdfReader reader1 = new PdfReader(fileName);
					    	   ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
					    	   PdfStamper ps1 = new PdfStamper(reader1, bos1);
					    	   
					    	   BaseFont bf1 = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
					    	   Font FontChinese1 = new Font(bf1, 5, Font.NORMAL); 
					    	   AcroFields s1 = ps1.getAcroFields();
					    	   s1.addSubstitutionFont(bf1);
					    	   s1.setField("create_time", sdfhms.format(new Date()));
					    	   s1.setField("f_full_name",BuyAccount.getMemberName());
							   s1.setField("f_acct_no",BuyAccount.getLoginName());
							   s1.setField("f_company","永达互联网金融信息服务有限公司");
					    	   s1.setField("total",String.valueOf(listFee.size()));
					    	   int i1=1;
					    	   BigDecimal fee=BigDecimal.ZERO;
					    	   for(TradeOrderInfo tof:listFee){
					    		   if(tof.getFee()!=null){
					    			   fee=fee.add(tof.getFee());
					    		   }
					    		   if(tradeType.equals("1")){
					    			   SellAccount=memberService.queryMemberById(tof.getSellerId(),env);
					    			   s1.setField("t_company"+i1,"永达互联网金融信息服务有限公司");
					    			   s1.setField("num"+i1,i1+"");
						    		   s1.setField("t_full_name"+i1,tof.getSellerName());
						    		   s1.setField("trade_no"+i1,tof.getTradeVoucherNo());
						    		   s1.setField("pay_time"+i1,tof.getGmtModified()==null?null:sdfhms.format(tof.getGmtModified()));
						    		   s1.setField("trans_type"+i1,tradeTypeName+"服务费");
						    		   s1.setField("t_acct_no"+i1,SellAccount.getLoginName());
					    		   }else{
					    			   s1.setField("num"+i1,i1+"");
					    			   s1.setField("t_full_name"+i1,tof.getName());
					    			   s1.setField("t_company"+i1,tof.getBranchName());
						    		   s1.setField("trade_no"+i1,tof.getTradeVoucherNo());
						    		   s1.setField("pay_time"+i1,tof.getOrderTime()==null?null:sdfhms.format(tof.getOrderTime()));
						    		   s1.setField("trans_type"+i1,tradeTypeName+"服务费");
						    		   String cardNo="";
									   if(tof.getCardNo()!=null && !tof.getCardNo().equals("")){
										   cardNo = uesServiceClient.getDataByTicket(tof.getCardNo());
									   }
									   if(StringUtils.isNotBlank(cardNo)){
										   if(isNeed1.equals("true")){
											   s1.setField("t_acct_no"+i1,cardNo);
										   }else{
											   s1.setField("t_acct_no"+i1,"****"+cardNo.substring(cardNo.length()-4));
										   }
									   }
					    		   }
					    		   s1.setField("amt"+i1,"¥ "+df.format(tof.getFee())+"  "+digitUppercase(Double.parseDouble(tof.getFee().toString())));
					    		   s1.setField("remark"+i1,tof.getTradeMemo());
					    		   i1++;
					    	   }
					    	   s1.setField("total_amt",df.format(fee));
					    	   
					    	   ps1.setFormFlattening(true);
					    	   ps1.close();
					    	   File file1=new File(filePath);
					    	   if(!file1.isDirectory()){
					    		   file1.mkdirs();
					    	   }
					    	   String strFee=sdfhms.format(new Date()).replaceAll(" ","").replaceAll(":","").replaceAll("-","");
					    	   FileOutputStream fos1 = new FileOutputStream(filePath
		                                + File.separator + System.currentTimeMillis()
		                                + ".pdf");
					    	   fos1.write(bos1.toByteArray()); 
					    	   
					    	   listFile = getFileSort(filePath);
                               files = new String[listFile.size()];
                               for (int n = 0; n < listFile.size(); n++) {
                                   files[n] = listFile.get(n).getPath();
                               }
                               
                               String savepath1 = filePath+File.separator+"电子回单_"+tradeTypeName+"_服务费_"+strFee+"_old_00002.pdf";
                               String savepathnew1 = filePath+File.separator+"电子回单_"+tradeTypeName+"_服务费_"+strFee+"_00002.pdf";
                               mergePdfFiles(files, savepath1);
            
                               batchSignature(savepathnew1, savepath1);//增加电子签章
                               sb.append(",").append(savepathnew1);
				    	   }
					   }
			    	   
			    	   savepath=sb.toString();
			        }else{
			           logger.info("create batchDownload trade >10");
			           int page=0;
			           if(list.size()%pageSize==0){
			        	   page=list.size()/pageSize;
			           }else{
			        	   page=list.size()/pageSize+1;
			           }
			    	   fileName = erFile+File.separator+"batchF.pdf";   
			    	   filePath=erFile+File.separator+"batch"+File.separator+id+File.separator+"er"+File.separator+BHId1;
			    	   PdfReader reader = new PdfReader(fileName);
			    	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    	   PdfStamper ps = new PdfStamper(reader, bos);
			    	   
			    	   BaseFont bf = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
			    	   Font FontChinese = new Font(bf, 5, Font.NORMAL); 
			    	   AcroFields s = ps.getAcroFields();
			    	   s.addSubstitutionFont(bf);
			    	   s.setField("create_time", sdfhms.format(new Date()));
			    	   s.setField("f_full_name",BuyAccount.getMemberName());
					   s.setField("f_acct_no",BuyAccount.getLoginName());
					   s.setField("f_company","永达互联网金融信息服务有限公司");
			    	   s.setField("total",String.valueOf(list.size()));
			    	   int i=1;
			    	   BigDecimal totalAmt=BigDecimal.ZERO;
			    	   BigDecimal fee=BigDecimal.ZERO;
			    	   List<TradeOrderInfo> listFee=new ArrayList<TradeOrderInfo>();
			    	   for(TradeOrderInfo tof:list){
			    		   if (tradeType.equals("0")) {// 退票
								BigDecimal amount=new BigDecimal(tof.getTradeAmount()==null?0:tof.getTradeAmount().doubleValue());
								BigDecimal tradeFee=new BigDecimal(tof.getFee()==null?0:tof.getFee().doubleValue());
								tof.setTradeAmount(amount.add(tradeFee));
							}
			    		   if(tof.getTradeAmount()!=null){
			    			   totalAmt=totalAmt.add(tof.getTradeAmount()); 
			    		   }
			    		   if(tof.getFee()!=null && tof.getFee().compareTo(BigDecimal.ZERO)>0){
			    			   fee=fee.add(tof.getFee());
			    			   listFee.add(tof);
			    		   }
			    	   }
			    	   s.setField("total_amt",df.format(totalAmt));
			    	   for(TradeOrderInfo tof:list){
			    		   if(i>pageSize){
			    			   break;
			    		   }
		    			   if(tradeType.equals("1")){
		    				   SellAccount=memberService.queryMemberById(tof.getSellerId(),env);
		    				   s.setField("t_company"+i,"永达互联网金融信息服务有限公司");
		    				   s.setField("num"+i,i+"");
		    				   s.setField("t_full_name"+i,tof.getSellerName());
		    				   s.setField("trade_no"+i,tof.getTradeVoucherNo());
		    				   s.setField("pay_time"+i,tof.getGmtModified()==null?null:sdfhms.format(tof.getGmtModified()));
		    				   s.setField("trans_type"+i,tradeTypeName);
		    				   s.setField("t_acct_no"+i,SellAccount.getLoginName());
		    			   }else{
		    				   s.setField("num"+i,i+"");
		    				   s.setField("t_full_name"+i,tof.getName());
		    				   s.setField("t_company"+i,tof.getBranchName());
		    				   s.setField("trade_no"+i,tof.getTradeVoucherNo());
		    				   s.setField("pay_time"+i,tof.getOrderTime()==null?null:sdfhms.format(tof.getOrderTime()));
		    				   s.setField("trans_type"+i,tradeTypeName);
				    		   String cardNo="";
							   if(tof.getCardNo()!=null && !tof.getCardNo().equals("")){
								   cardNo = uesServiceClient.getDataByTicket(tof.getCardNo());
							   }
							   if(StringUtils.isNotBlank(cardNo)){
								   if(isNeed1.equals("true")){
									   s.setField("t_acct_no"+i,cardNo);
								   }else{
									   s.setField("t_acct_no"+i,"****"+cardNo.substring(cardNo.length()-4));
								   }
							   }
				    		   
		    			   }
		    			   s.setField("amt"+i,"¥ "+df.format(tof.getTradeAmount())+"  "+digitUppercase(Double.parseDouble(tof.getTradeAmount().toString())));
		    			   s.setField("remark"+i,tof.getTradeMemo());
			    		   i++;
			    	   }
			    	   ps.setFormFlattening(true);
			    	   ps.close();
			    	   
			    	   File file=new File(filePath);
			    	   if(!file.isDirectory()){
			    		   file.mkdirs();
			    	   }
			    	   FileOutputStream fos = new FileOutputStream(filePath+File.separator+System.currentTimeMillis()+".pdf");
			    	   fos.write(bos.toByteArray());
			    	   
			    	   //生成后边所有页
			    	   for(int m=2;m<page+1;m++){
			    		   fileName = erFile+File.separator+"batchS.pdf";   
				    	   reader = new PdfReader(fileName);
				    	   bos = new ByteArrayOutputStream();
				    	   ps = new PdfStamper(reader, bos);
				    	   
				    	   bf = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
				    	   FontChinese = new Font(bf, 5, Font.NORMAL); 
				    	   s = ps.getAcroFields();
				    	   s.addSubstitutionFont(bf);
				    	   int k=1;
				    	   int limit=0;
				    	   if(list.size()<m*pageSize){
				    		   limit=list.size();
				    	   }else{
				    		   limit=m*pageSize;
				    	   }
				    	   for(int j=(m-1)*pageSize;j<limit;j++){
				    		   if (tradeType.equals("0")) {// 退票
									BigDecimal amount=new BigDecimal(list.get(j).getTradeAmount()==null?0:list.get(j).getTradeAmount().doubleValue());
									BigDecimal tradeFee=new BigDecimal(list.get(j).getFee()==null?0:list.get(j).getFee().doubleValue());
									list.get(j).setTradeAmount(amount.add(tradeFee));
								}
			    			   if(tradeType.equals("1")){
			    				   SellAccount=memberService.queryMemberById(list.get(j).getSellerId(),env);
			    				   s.setField("t_company"+k,"永达互联网金融信息服务有限公司");
			    				   s.setField("num"+k,(j+1)+"");
			    				   s.setField("t_full_name"+k,list.get(j).getSellerName());
			    				   s.setField("trade_no"+k,list.get(j).getTradeVoucherNo());
			    				   s.setField("pay_time"+k,list.get(j).getGmtModified()==null?null:sdfhms.format(list.get(j).getGmtModified()));
			    				   s.setField("trans_type"+k,tradeTypeName);
			    				   s.setField("t_acct_no"+k,SellAccount.getLoginName());
			    			   }else{
			    				   s.setField("num"+k,(j+1)+"");
			    				   s.setField("t_full_name"+k,list.get(j).getName());
			    				   s.setField("t_company"+k,list.get(j).getBranchName());
			    				   s.setField("trade_no"+k,list.get(j).getTradeVoucherNo());
			    				   s.setField("pay_time"+k,list.get(j).getOrderTime()==null?null:sdfhms.format(list.get(j).getOrderTime()));
			    				   s.setField("trans_type"+k,tradeTypeName);
					    		   String cardNo="";
								   if(list.get(j).getCardNo()!=null && !list.get(j).getCardNo().equals("")){
									   cardNo = uesServiceClient.getDataByTicket(list.get(j).getCardNo());
								   }
								   if(StringUtils.isNotBlank(cardNo)){
									   if(isNeed1.equals("true")){
										   s.setField("t_acct_no"+k,cardNo);
									   }else{
										   s.setField("t_acct_no"+k,"****"+cardNo.substring(cardNo.length()-4));
									   }
								   }
			    			   }
			    			   s.setField("amt"+k,"¥ "+df.format(list.get(j).getTradeAmount())+"  "+digitUppercase(Double.parseDouble(list.get(j).getTradeAmount().toString())));
			    			   s.setField("remark"+k,list.get(j).getTradeMemo());
			    		   k++;
				    	   }
				    	   ps.setFormFlattening(true);
				    	   ps.close();
				    	   
				    	   fos = new FileOutputStream(filePath+File.separator+System.currentTimeMillis()+".pdf");
				    	   fos.write(bos.toByteArray());
			    	   }
			    	   
			    	   //合并
//			    	   file=new File(filePath);
//			    	   File[] tempList=file.listFiles();
//			    	   String[] files=new String[tempList.length];
//			    	   for(int n=0;n<tempList.length;n++){
//			    		   files[n]=tempList[n].getPath();
//			    	   }
			    	   List<File> listFile=getFileSort(filePath);
					   String[] files=new String[listFile.size()];
					   for(int n=0;n<listFile.size();n++){
						   files[n]=listFile.get(n).getPath();
					   }
			    	   String str=sdfhms.format(new Date()).replaceAll(" ","").replaceAll(":","").replaceAll("-","");
			    	   savepath =filePath+File.separator+"电子回单_"+tradeTypeName+"_"+str+"_old"+"_00001.pdf";  
			    	   String savepathnew = filePath+File.separator+"电子回单_"+tradeTypeName+"_"+str+"_00001.pdf";
			    	   mergePdfFiles(files, savepath); 
			           batchSignature(savepathnew, savepath);//增加电子签章
			           deleteFiles(savepath);
			           sb.append(savepathnew);
			           
			           if (!tradeType.equals("0")) {
			        	 //生成费用回单
				           if(listFee!=null && !listFee.isEmpty()){
				        	   filePath=erFile+File.separator+"batch"+File.separator+id+File.separator+"fee"+File.separator+BHId1;
				        	   //小于10个费用
				        	   if(listFee.size()<=pageSize){
				        		   fileName = erFile+File.separator+"batchF.pdf"; 
				        		   PdfReader reader1 = new PdfReader(fileName);
						    	   ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
						    	   PdfStamper ps1 = new PdfStamper(reader1, bos1);
						    	   
						    	   BaseFont bf1 = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
						    	   Font FontChinese1 = new Font(bf1, 5, Font.NORMAL); 
						    	   AcroFields s1 = ps1.getAcroFields();
						    	   s1.addSubstitutionFont(bf1);
						    	   s1.setField("create_time", sdfhms.format(new Date()));
						    	   s1.setField("f_full_name",BuyAccount.getMemberName());
								   s1.setField("f_acct_no",BuyAccount.getLoginName());
								   s1.setField("f_company","永达互联网金融信息服务有限公司");
						    	   s1.setField("total",String.valueOf(listFee.size()));
						    	   i=1;
						    	   for(TradeOrderInfo tof:listFee){
						    		   if(tradeType.equals("1")){
						    			   SellAccount=memberService.queryMemberById(tof.getSellerId(),env);
						    			   s1.setField("t_company"+i,"永达互联网金融信息服务有限公司");
						    			   s1.setField("num"+i,i+"");
							    		   s1.setField("t_full_name"+i,tof.getSellerName());
							    		   s1.setField("trade_no"+i,tof.getTradeVoucherNo());
							    		   s1.setField("pay_time"+i,tof.getGmtModified()==null?null:sdfhms.format(tof.getGmtModified()));
							    		   s1.setField("trans_type"+i,tradeTypeName);
							    		   s1.setField("t_acct_no"+i,SellAccount.getLoginName());
						    		   }else{
						    			   s1.setField("num"+i,i+"");
						    			   s1.setField("t_full_name"+i,tof.getName());
						    			   s1.setField("t_company"+i,tof.getBranchName());
							    		   s1.setField("trade_no"+i,tof.getTradeVoucherNo());
							    		   s1.setField("pay_time"+i,tof.getOrderTime()==null?null:sdfhms.format(tof.getOrderTime()));
							    		   s1.setField("trans_type"+i,tradeTypeName);
							    		   String cardNo="";
										   if(tof.getCardNo()!=null && !tof.getCardNo().equals("")){
											   cardNo = uesServiceClient.getDataByTicket(tof.getCardNo());
										   }
										   if(StringUtils.isNotBlank(cardNo)){
											   if(isNeed1.equals("true")){
												   s.setField("t_acct_no"+i,cardNo);
											   }else{
												   s.setField("t_acct_no"+i,"****"+cardNo.substring(cardNo.length()-4));
											   }
										   }
						    		   }
						    		   s1.setField("amt"+i,"¥ "+df.format(tof.getFee())+"  "+digitUppercase(Double.parseDouble(tof.getFee().toString())));
						    		   s1.setField("remark"+i,tof.getTradeMemo());
						    		   i++;
						    	   }
						    	   s1.setField("total_amt",df.format(fee));
						    	   
						    	   ps1.setFormFlattening(true);
						    	   ps1.close();
						    	   File file1=new File(filePath);
						    	   if(!file1.isDirectory()){
						    		   file1.mkdirs();
						    	   }
						    	   String strFee=sdfhms.format(new Date()).replaceAll(" ","").replaceAll(":","").replaceAll("-","");
						    	   FileOutputStream fos1 = new FileOutputStream(filePath+File.separator+"电子回单_"+tradeTypeName+"_服务费_"+strFee+"00002.pdf");
						    	   fos1.write(bos1.toByteArray());
						    	   
						    	   listFile = getFileSort(filePath);
	                               files = new String[listFile.size()];
	                               for (int n = 0; n < listFile.size(); n++) {
	                                   files[n] = listFile.get(n).getPath();
	                               }
	                               
	                               String savepath1 = filePath+File.separator+"电子回单_"+tradeTypeName+"_服务费_"+strFee+"_old_00002.pdf";
	                               String savepathnew1 = filePath+File.separator+"电子回单_"+tradeTypeName+"_服务费_"+strFee+"_00002.pdf";
	                               mergePdfFiles(files, savepath1);
	            
	                               batchSignature(savepathnew1, savepath1);//增加电子签章
	                               sb.append(",").append(savepathnew1);
						    	   savepath=sb.toString();
				        	   }else{//费用条数大于10
				        		   fileName = erFile+File.separator+"batchF.pdf"; 
					        	   reader = new PdfReader(fileName);
						    	   bos = new ByteArrayOutputStream();
						    	   ps = new PdfStamper(reader, bos);
						    	   
						    	   bf = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
						    	   FontChinese = new Font(bf, 5, Font.NORMAL); 
						    	   s = ps.getAcroFields();
						    	   s.addSubstitutionFont(bf);
						    	   s.setField("create_time", sdfhms.format(new Date()));
						    	   s.setField("f_full_name",BuyAccount.getMemberName());
								   s.setField("f_acct_no",BuyAccount.getLoginName());
								   s.setField("f_company","永达互联网金融信息服务有限公司");
						    	   s.setField("total",String.valueOf(listFee.size()));
						    	   i=1;
						    	   for(TradeOrderInfo tof:listFee){
						    		   if(i>pageSize){
						    			   break;
						    		   }
					    			   if(tradeType.equals("1")){
					    				   SellAccount=memberService.queryMemberById(tof.getSellerId(),env);
					    				   s.setField("t_company"+i,"永达互联网金融信息服务有限公司");
					    				   s.setField("num"+i,i+"");
					    				   s.setField("t_full_name"+i,tof.getSellerName());
					    				   s.setField("trade_no"+i,tof.getTradeVoucherNo());
					    				   s.setField("pay_time"+i,tof.getGmtModified()==null?null:sdfhms.format(tof.getGmtModified()));
					    				   s.setField("trans_type"+i,tradeTypeName+"服务费");
					    				   s.setField("t_acct_no"+i,SellAccount.getLoginName());
					    			   }else{
					    				   s.setField("num"+i,i+"");
					    				   s.setField("t_full_name"+i,tof.getName());
					    				   s.setField("t_company"+i,tof.getBranchName());
					    				   s.setField("trade_no"+i,tof.getTradeVoucherNo());
					    				   s.setField("pay_time"+i,tof.getOrderTime()==null?null:sdfhms.format(tof.getOrderTime()));
					    				   s.setField("trans_type"+i,tradeTypeName+"服务费");
							    		   String cardNo="";
										   if(tof.getCardNo()!=null && !tof.getCardNo().equals("")){
											   cardNo = uesServiceClient.getDataByTicket(tof.getCardNo());
										   }
										   if(StringUtils.isNotBlank(cardNo)){
											   if(isNeed1.equals("true")){
												   s.setField("t_acct_no"+i,cardNo);
											   }else{
												   s.setField("t_acct_no"+i,"****"+cardNo.substring(cardNo.length()-4));
											   }
										   }
					    			   }
				    			   s.setField("amt"+i,"¥ "+df.format(tof.getFee())+"  "+digitUppercase(Double.parseDouble(tof.getFee().toString())));
				    			   s.setField("remark"+i,tof.getTradeMemo());
						    	   s.setField("total_amt",df.format(fee));
						    	   i++;
				        	   }
					    	   ps.setFormFlattening(true);
					    	   ps.close();
					    	   file=new File(filePath);
					    	   if(!file.isDirectory()){
					    		   file.mkdirs();
					    	   }
					    	   fos = new FileOutputStream(filePath+File.separator+System.currentTimeMillis()+".pdf");
					    	   fos.write(bos.toByteArray());
						    	   
					    	   //生成后边所有页
					    	   for(int m=2;m<page+1;m++){
					    		   fileName = erFile+File.separator+"batchS.pdf";   
						    	   reader = new PdfReader(fileName);
						    	   bos = new ByteArrayOutputStream();
						    	   ps = new PdfStamper(reader, bos);
						    	   
						    	   bf = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
						    	   FontChinese = new Font(bf, 5, Font.NORMAL); 
						    	   s = ps.getAcroFields();
						    	   s.addSubstitutionFont(bf);
						    	   int k=1;
						    	   int limit=0;
						    	   if(listFee.size()<m*pageSize){
						    		   limit=listFee.size();
						    	   }else{
						    		   limit=m*pageSize;
						    	   }
						    	   for(int j=(m-1)*pageSize;j<limit;j++){
					    			   if(tradeType.equals("1")){
					    				   SellAccount=memberService.queryMemberById(listFee.get(j).getSellerId(),env);
					    				   s.setField("t_company"+k,"永达互联网金融信息服务有限公司");
					    				   s.setField("num"+k,(j+1)+"");
					    				   s.setField("t_full_name"+k,listFee.get(j).getSellerName());
					    				   s.setField("trade_no"+k,listFee.get(j).getTradeVoucherNo());
					    				   s.setField("pay_time"+k,listFee.get(j).getGmtModified()==null?null:sdfhms.format(listFee.get(j).getGmtModified()));
					    				   s.setField("trans_type"+k,tradeTypeName+"服务费");
					    				   s.setField("t_acct_no"+k,SellAccount.getLoginName());
					    			   }else{
					    				   s.setField("num"+k,(j+1)+"");
					    				   s.setField("t_full_name"+k,j+1+" "+listFee.get(j).getName());
					    				   s.setField("t_company"+k,listFee.get(j).getBranchName());
					    				   s.setField("trade_no"+k,listFee.get(j).getTradeVoucherNo());
					    				   s.setField("pay_time"+k,listFee.get(j).getOrderTime()==null?null:sdfhms.format(listFee.get(j).getOrderTime()));
					    				   s.setField("trans_type"+k,tradeTypeName+"服务费");
							    		   String cardNo="";
										   if(listFee.get(j).getCardNo()!=null && !listFee.get(j).getCardNo().equals("")){
											   cardNo = uesServiceClient.getDataByTicket(listFee.get(j).getCardNo());
										   }
										   if(StringUtils.isNotBlank(cardNo)){
											   if(isNeed1.equals("true")){
												   s.setField("t_acct_no"+k,cardNo);
											   }else{
												   s.setField("t_acct_no"+k,"****"+cardNo.substring(cardNo.length()-4));
											   }
										   }
					    			   }
				    			   s.setField("amt"+k,df.format(listFee.get(j).getFee())+"  "+digitUppercase(listFee.get(j).getFee().doubleValue()));
				    			   s.setField("remark"+k,listFee.get(j).getTradeMemo());
					    		   k++;
						    	   }
						    	   ps.setFormFlattening(true);
						    	   ps.close();
						    	   fos = new FileOutputStream(filePath+File.separator+System.currentTimeMillis()+".pdf");
						    	   fos.write(bos.toByteArray());
					    	   }
					    	   
					    	   //合并
					    	   file=new File(filePath);
//					    	   tempList=file.listFiles();
//					    	   files=new String[tempList.length];
//					    	   for(int n=0;n<tempList.length;n++){
//					    		   files[n]=tempList[n].getPath();
//					    	   }
					    	   listFile=getFileSort(filePath);
							   files=new String[listFile.size()];
							   for(int n=0;n<listFile.size();n++){
									files[n]=listFile.get(n).getPath();
							   }
					    	   String strFee=sdfhms.format(new Date()).replaceAll(" ","").replaceAll(":","").replaceAll("-","");
					    	   savepath =filePath+File.separator+"电子回单_"+tradeTypeName+"_服务费_"+strFee+"_old_00002.pdf"; 
					    	  
					    	   savepathnew = filePath+File.separator+"电子回单_"+tradeTypeName+"_服务费_"+strFee+"_00002.pdf"; 
					    	   mergePdfFiles(files, savepath); 
					    	   batchSignature(savepathnew, savepath);//增加电子签章
					    	   deleteFiles(savepath);
					    	   sb.append(",").append(savepathnew);
					    	   savepath=sb.toString();
				           }
			           }
			           }
			           
		        }
				//更新数据状态
				TDownloadBHDO record=new TDownloadBHDO();
				record.setTaskStatus("2");
				record.setUrl(sb.toString());
				record.setId(BHId1);
				try {
					erServiceFacade.updateTDownloadBHDO(record);
				} catch (Exception e) {
					logger.info("更新批量下载状态:",e);
				}
				
				List<TradeOrderInfo> list1=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo t=null;
				for(TradeOrderInfo tif:list){
					t=new TradeOrderInfo();
					t.setId(tif.getId());
					t.setHasDownload("1");
					list1.add(t);
				}
				try {
					erServiceFacade.updateTradeOrder(list1);
				} catch (Exception e) {
					logger.info("更新回单下载状态",e);
				}
				
			}
		}catch(Exception e){
			logger.error("batchDownload Exception",e);
		}
		
    }
	
	/**
	 * 
	 * @param user
	 * @param BHId1
	 * @param listQuery
	 * @param isNeed1 //true为 需要银行卡信息 false或者""为不需要银行卡信息
	 * @throws IOException
	 * @throws DocumentException
	 * @throws BizException
	 * @throws MemberNotExistException
	 */
	
	public void moreSingle(EnterpriseMember user,String BHId1,TradeListQuery listQuery,String isNeed1,String tradeType1) throws IOException, DocumentException, BizException, MemberNotExistException {
		DecimalFormat df = new DecimalFormat("0.00");
		try{
			logger.info("moreSingle start");
			String tradeType="";
			String tradeTypeName="";
			if("3".equals(tradeType1)){
				tradeType="1";
				tradeTypeName="转账入款";
			}else if(listQuery.getTradeType().equals("10310") && !"3".equals(tradeType1)){//转账到永达互联网金融 ------
				tradeType="1";
				tradeTypeName="付款到永达互联网金融";
			}else if(listQuery.getTradeType().equals("10220,10221")){//转账到卡---
				tradeType="2";
				tradeTypeName="付款到卡";
			}else if(listQuery.getTradeType().equals("10231")){//代发工资到账户-----
				tradeType="1";
				tradeTypeName="代发工资到账户";
			}else if(listQuery.getTradeType().equals("10230")){//代发工资到银行卡------
				tradeType="2";
				tradeTypeName="代发工资到银行卡";
			}else if(listQuery.getTradeType().equals("10241")){//资金归集到账户----
				tradeType="1";
				tradeTypeName="资金归集到账户";
			}else if(listQuery.getTradeType().equals("10240")){//资金归集到银行卡
				tradeType="2";
				tradeTypeName="资金归集到银行卡";
			}else if(listQuery.getStatus().equals("refundTicket")){//退票
				tradeType="0";
				tradeTypeName="退票";
			}
			String id = user.getMemberId();
			deleteFiles(erFile+File.separator+"moreSingle"+File.separator+id+File.separator+"er");
			BaseMember BuyAccount=new BaseMember();
			BaseMember SellAccount=new BaseMember();
			
			List<TradeOrderInfo> list=new ArrayList<TradeOrderInfo>();
			try {
				list=erServiceFacade.queryByTradeCount(listQuery);
			} catch (Exception e) {
				logger.info("查询电子回单记录条数异常", e);
			}
			String savepath="";
			String fileName =erFile+File.separator+"single.pdf";
			String filePath="";
			StringBuffer sb=new StringBuffer();
			boolean flag=false;
			if(list!=null && !list.isEmpty()){
				logger.info("create moreSingle trade");
				//生成交易回单
				String strName1="";//临时文件1
				String strName2="";//临时文件2
				int fl=0;
				for(TradeOrderInfo tif:list){
					fl=fl+1;
					if (tradeType.equals("0")) {// 退票
						BigDecimal amount=new BigDecimal(tif.getTradeAmount()==null?0:tif.getTradeAmount().doubleValue());
						BigDecimal tradeFee=new BigDecimal(tif.getFee()==null?0:tif.getFee().doubleValue());
						tif.setTradeAmount(amount.add(tradeFee));
					}
					filePath=erFile+File.separator+"moreSingle"+File.separator+id+File.separator+"er"+File.separator+BHId1+File.separator+tif.getId();
					if(tif.getFee()!=null && tif.getFee().compareTo(BigDecimal.ZERO)>0){
						flag=true;
					}else{
						flag=false;
					}
					PdfReader reader = new PdfReader(fileName);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					PdfStamper ps = new PdfStamper(reader, bos);
					
					BaseFont bf = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
					Font FontChinese = new Font(bf, 5, Font.NORMAL); 
					AcroFields s = ps.getAcroFields();
					s.addSubstitutionFont(bf);
					s.setField("create_time",sdfhms.format(new Date()));
					s.setField("trade_no",tif.getTradeVoucherNo());
					BuyAccount=memberService.queryMemberById(tif.getBuyerId(),env);
					s.setField("f_full_name",BuyAccount.getMemberName());
					s.setField("f_acct_no",BuyAccount.getLoginName());
					s.setField("up_amt"," "+digitUppercase(tif.getTradeAmount()==null?0:tif.getTradeAmount().doubleValue()));
					s.setField("low_amt"," "+df.format(tif.getTradeAmount()==null?null:tif.getTradeAmount()));
					s.setField("remark",tif.getTradeMemo());
					s.setField("f_company","永达互联网金融信息服务有限公司");
					if(tradeType.equals("1")){
						SellAccount=memberService.queryMemberById(tif.getSellerId(),env);
						s.setField("trans_type",tradeTypeName);
						s.setField("pay_time",tif.getGmtModified()==null?null:sdfhms.format(tif.getGmtModified()));
						s.setField("t_full_name",tif.getSellerName());
						s.setField("t_acct_no",SellAccount.getLoginName());
						s.setField("t_company","永达互联网金融信息服务有限公司");
					}else{
						s.setField("trans_type",tradeTypeName);
						s.setField("pay_time",tif.getOrderTime()==null?null:sdfhms.format(tif.getOrderTime()));
						s.setField("t_full_name",tif.getName());
						String cardNo="";
						if(tif.getCardNo()!=null && !tif.getCardNo().equals("")){
							cardNo = uesServiceClient.getDataByTicket(tif.getCardNo());
						}
						if(isNeed1.equals("true")){
							s.setField("t_acct_no",cardNo);
						}else{
							s.setField("t_acct_no","****"+cardNo.substring(cardNo.length()-4));
						}
						s.setField("t_company",tif.getBranchName());
					}
					ps.setFormFlattening(true);
					ps.close();
					File file=new File(filePath);
					if(!file.isDirectory()){
						file.mkdirs();
					}
					strName1=String.valueOf(System.currentTimeMillis());
					FileOutputStream fos = new FileOutputStream(filePath+File.separator+strName1+".pdf");
					fos.write(bos.toByteArray());
					
					//生成服务回单
					if(flag){
						logger.info("create moreSingle fee");
						reader = new PdfReader(fileName);
						bos = new ByteArrayOutputStream();
						ps = new PdfStamper(reader, bos);
						
						bf = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
						FontChinese = new Font(bf, 5, Font.NORMAL); 
						s = ps.getAcroFields();
						s.addSubstitutionFont(bf);
						s.setField("create_time",sdfhms.format(new Date()));
						s.setField("trade_no",tif.getTradeVoucherNo());
						s.setField("f_full_name",BuyAccount.getMemberName());
						s.setField("f_acct_no",BuyAccount.getLoginName());
						s.setField("t_company","永达互联网金融信息服务有限公司");
						s.setField("up_amt"," "+digitUppercase(Double.parseDouble(tif.getFee().toString())));
						s.setField("low_amt"," "+df.format(tif.getFee()));
						s.setField("f_company","永达互联网金融信息服务有限公司");
						s.setField("remark",tif.getTradeMemo());
						if(tradeType.equals("0")){//退票
							
						}if(tradeType.equals("1")){
							s.setField("trans_type",tradeTypeName+"付款服务费");
							SellAccount=memberService.queryMemberById(tif.getSellerId(),env);
							s.setField("pay_time",tif.getGmtModified()==null?null:sdfhms.format(tif.getGmtModified()));
							s.setField("t_full_name",tif.getSellerName());
							s.setField("t_acct_no",SellAccount.getLoginName());
							s.setField("t_company","永达互联网金融信息服务有限公司");
						}else{
							s.setField("trans_type",tradeTypeName+"服务费");
							s.setField("pay_time",tif.getOrderTime()==null?null:sdfhms.format(tif.getOrderTime()));
							s.setField("t_full_name",tif.getName());
							String cardNo="";
							if(tif.getCardNo()!=null && !tif.getCardNo().equals("")){
								cardNo = uesServiceClient.getDataByTicket(tif.getCardNo());
							}
							if(StringUtils.isNotBlank(cardNo)){
								if(isNeed1.equals("true")){
									s.setField("t_acct_no",cardNo);
								}else{
									s.setField("t_acct_no","****"+cardNo.substring(cardNo.length()-4));
								}
							}
							s.setField("t_company",tif.getBranchName());
						}
						ps.setFormFlattening(true);
						ps.close();
						file=new File(filePath);
						if(!file.isDirectory()){
							file.mkdirs();
						}
						strName2=String.valueOf(System.currentTimeMillis());
						fos = new FileOutputStream(filePath+File.separator+strName2+".pdf");
						fos.write(bos.toByteArray());
					}
					//合并一次循环的交易和费用回单
//					file=new File(filePath);
//					File[] tempList=file.listFiles();
					List<File> listFile=getFileSort(filePath);
					String[] files=new String[listFile.size()];
					for(int n=0;n<listFile.size();n++){
						files[n]=listFile.get(n).getPath();
					}
					String str=sdfhms.format(new Date()).replaceAll(" ","").replaceAll(":","").replaceAll("-","");
					int num=String.valueOf(fl).length();
					String fla="";
					if (num<5) {
						for (int i = 1; i < 5-num; i++) {
							fla+="0";
						}
					}
					savepath =erFile+File.separator+"moreSingle"+File.separator+id+File.separator+"final"+File.separator+BHId1+File.separator+"电子回单_"+tradeTypeName+"_"+str+"_old.pdf";  
					String savepathnew = erFile+File.separator+"moreSingle"+File.separator+id+File.separator+"final"+File.separator+BHId1+File.separator+"电子回单_"+tradeTypeName+"_"+str+"_"+fla+fl+".pdf";  
					File filef=new File(erFile+File.separator+"moreSingle"+File.separator+id+File.separator+"final"+File.separator+BHId1);
					if(!filef.isDirectory()){
					    filef.mkdirs();
					}
					mergePdfFiles(files, savepath); 
					signature(savepathnew, savepath);//增加电子签章
					deleteFiles(savepath);
					Thread.sleep(600);
				}
				
				sb.append(erFile+File.separator+"moreSingle"+File.separator+id+File.separator+"final"+File.separator+BHId1);

				TDownloadBHDO record=new TDownloadBHDO();
				record.setTaskStatus("2");
				record.setUrl(sb.toString());
				record.setId(BHId1);
				erServiceFacade.updateTDownloadBHDO(record);
				
				List<TradeOrderInfo> list1=new ArrayList<TradeOrderInfo>();
				TradeOrderInfo t=null;
				for(TradeOrderInfo f:list){
					t=new TradeOrderInfo();
					t.setId(f.getId());
					t.setHasDownload("1");
					list1.add(t);
				}
				try {
					erServiceFacade.updateTradeOrder(list1);
				} catch (Exception e) {
					logger.info("更新回单下载状态",e);
				}
			}
		}catch(Exception e){
			logger.error("moreSingle Exception",e);
		}
	}
	
	
	
	
	
	/**
     * 获取目录下所有文件(按时间排序)
     * 
     * @param path
     * @return
     */
    public static List<File> getFileSort(String path) {
 
        List<File> list = getFiles(path, new ArrayList<File>());
 
        if (list != null && list.size() > 0) {
 
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                	String fileName=file.getName().substring(0, file.getName().length()-4);
                	String newFileName=newFile.getName().substring(0, newFile.getName().length()-4);
                	Long fileDate=Long.valueOf(fileName).longValue();
                	Long newFileDate=Long.valueOf(newFileName).longValue();
                    if (fileDate > newFileDate) {
                        return 1;
                    } else if (fileDate == newFileDate) {
                        return 0;
                    } else {
                        return -1;
                    }
 
                }
            });
 
        }
 
        return list;
    }
    /**
     * 
     * 获取目录下所有文件
     * 
     * @param realpath
     * @param files
     * @return
     */
    public static List<File> getFiles(String realpath, List<File> files) {
 
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }
	
	//删除文件以及文件夹
		public void deleteFiles(String path) {
			try {
				File file = new File(path);
				if (!file.isDirectory()) {
					file.delete();
				} else {
					String[] fileList = file.list();
					for (int j = 0; j < fileList.length; j++) {
						File filessFile = new File(path + File.separator
								+ fileList[j]);
						if (!filessFile.isDirectory()) {
							filessFile.delete();
						} else {
							deleteFiles(path + File.separator + fileList[j]);
						}
					}
					file.delete();
				}
			} catch (Exception e) {
				logger.error("deleteFiles Exception", e);
			}
		}
	
	/**
     * 数字金额大写转换，思想先写个完整的然后将如零拾替换成零
     * 要用到正则表达式
     */
    public static String digitUppercase(double n){
        String fraction[] = {"角", "分"};
        String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
        String unit[][] = {{"元", "万", "亿"},
                     {"", "拾", "佰", "仟"}};
        String head = n < 0? "负": "";
        n = Math.abs(n);
        String s = "";
        for (int i = 0; i < fraction.length; i++) {
            s += (digit[(int)(Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
        }
        if(s.length()<1){
            s = "整";   
        }
        int integerPart = (int)Math.floor(n);
        for (int i = 0; (i < unit[0].length) && (integerPart > 0); i++) {
            String p ="";
            for (int j = 0; (j < unit[1].length) && (n > 0); j++) {
                p = digit[integerPart%10]+unit[1][j] + p;
                integerPart = integerPart/10;
            }
            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }
        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }
	/**
	 * 合并pdf
	 * @param files
	 * @param newfile
	 * @return
	 */
    public static boolean mergePdfFiles(String[] files, String newfile) {  
        boolean retValue = false;  
        Document document = null;  
        try {  
            document = new Document(new PdfReader(files[0]).getPageSize(1));  
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(newfile));  
            document.open();  
            for (int i = 0; i < files.length; i++) {  
                PdfReader reader = new PdfReader(files[i]);  
                int n = reader.getNumberOfPages();  
                for (int j = 1; j <= n; j++) {  
                    document.newPage();  
                    PdfImportedPage page = copy.getImportedPage(reader, j);  
                    copy.addPage(page);  
                }  
            }  
            retValue = true; 
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            document.close();  
        }  
        return retValue;  
    }
    //下载
    @RequestMapping(value="/download.htm")
    public void download(HttpServletRequest req,HttpServletResponse resp) throws IOException{
    	try{
    		String id=req.getParameter("id")==null?null:req.getParameter("id");
    		if(id!=null){
        		TradeListQuery tradeListQuery=new TradeListQuery();
        		tradeListQuery.setId(getUser(req).getMemberId());//商户id
        		tradeListQuery.setBuyerId(id);//主键
        		List<TDownloadBHDO> list=erServiceFacade.selectByDownloadCount(tradeListQuery);
        		if(list!=null && !list.isEmpty()){
        			String url=list.get(0).getUrl();
        			if(url.contains(".pdf")){
    		    		String[] urls=url.split(",");
    		    		OutputStream os = resp.getOutputStream();
    		    		ZipOutputStream zos = new ZipOutputStream(os);
    		    		for(String s:urls){
    		    			File file=new File(s);
    		    			String name=file.getName();
    		    			resp.setContentType("APPLICATION/x-download");
    		    			resp.setHeader("Content-Disposition", "attachment;fileName="
    		    					+ new String("电子回单.rar".getBytes("gb2312"), "ISO8859-1"));
    		    			zos.setEncoding("gb2312");// 设置文件名编码方式
    		    			byte[] buf = new byte[1024];
    		    			InputStream in = new FileInputStream(file);
    		    			zos.putNextEntry(new ZipEntry(name));
    		    			int len = 0;
    		    			while ((len = in.read(buf)) != -1) {
    		    				zos.write(buf, 0, len);
    		    			}
    		    			in.close();
    		    			zos.closeEntry();
    		    		}
    					zos.close();
    					os.close();
        			}else{
        				String[] urls=url.split(",");
            			OutputStream os = resp.getOutputStream();
            			ZipOutputStream zos = new ZipOutputStream(os);
            			resp.setContentType("APPLICATION/x-download");
            			resp.setHeader("Content-Disposition", "attachment;fileName="
            					+ new String("电子回单.rar".getBytes("gb2312"), "ISO8859-1"));
            			zos.setEncoding("gb2312");// 设置文件名编码方式
            			for(String s:urls){
            				File file=new File(s);
            				File[] tempList = file.listFiles();
            				for(int i=0;i<tempList.length;i++){
            					String name=tempList[i].getName();
            					byte[] buf = new byte[1024];
            					InputStream in = new FileInputStream(tempList[i]);
            					zos.putNextEntry(new ZipEntry(name));
            					int len = 0;
            					while ((len = in.read(buf)) != -1) {
            						zos.write(buf, 0, len);
            					}
            					in.close();
            					zos.closeEntry();
            				}
            			}
            			zos.close();
            			os.close();
        			}
        		}
        	}
    	}catch(Exception e){
    		logger.error("download huidan Exception",e);
    	}
    }
}
