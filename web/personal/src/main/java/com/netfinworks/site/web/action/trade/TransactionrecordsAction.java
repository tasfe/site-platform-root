//package com.netfinworks.site.web.action.trade;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//import org.apache.commons.lang.StringUtils;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.ModelMap;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.servlet.ModelAndView;
//
//import com.netfinworks.biz.common.util.QueryBase;
//import com.netfinworks.common.domain.OperationEnvironment;
//import com.netfinworks.common.util.DateUtil;
//import com.netfinworks.fos.service.facade.domain.FundoutInfo;
//import com.netfinworks.recharge.facade.api.RechargeQueryServiceFacade;
//import com.netfinworks.recharge.facade.request.RechargeQueryRequest;
//import com.netfinworks.recharge.facade.response.RechargeQueryResponse;
//import com.netfinworks.service.exception.ServiceException;
//import com.netfinworks.site.core.common.RestResponse;
//import com.netfinworks.site.core.common.constants.CommonConstant;
//import com.netfinworks.site.domain.domain.fos.Fundout;
//import com.netfinworks.site.domain.domain.info.BaseInfo;
//import com.netfinworks.site.domain.domain.info.PageResultList;
//import com.netfinworks.site.domain.domain.info.TradeInfo;
//import com.netfinworks.site.domain.domain.info.WalletCheckInfo;
//import com.netfinworks.site.domain.domain.member.EnterpriseMember;
//import com.netfinworks.site.domain.domain.member.PersonMember;
//import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
//import com.netfinworks.site.domain.domain.trade.FundoutQuery;
//import com.netfinworks.site.domain.domain.trade.TradeRequest;
//import com.netfinworks.site.domain.domain.trade.TransferInfo;
//import com.netfinworks.site.domain.enums.PhoneRechargeStatus;
//import com.netfinworks.site.domain.enums.TradeType;
//import com.netfinworks.site.domain.enums.TradeTypeRequest;
//import com.netfinworks.site.domain.exception.BizException;
//import com.netfinworks.site.domainservice.account.DefaultAccountService;
//import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
//import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
//import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
//import com.netfinworks.site.ext.integration.member.AccountService;
//import com.netfinworks.site.web.action.common.BaseAction;
//import com.netfinworks.site.web.form.QueryTradeForm;
//import com.netfinworks.site.web.util.DateUtils;
//import com.netfinworks.site.web.util.ExcelUtil;
//import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
//
///**
// * 查询个人钱包用户的所有记录
// * 
// * @param model
// * @param request
// * @return
// */
//@Controller
//public class TransactionrecordsAction extends BaseAction {
//    @Resource(name = "rechargeQueryServiceFacade")
//    private RechargeQueryServiceFacade rechargeQueryServiceFacade;
//
//	@Resource(name = "defaultTradeQueryService")
//	private DefaultTradeQueryService defaultTradeQueryService;
//	
//    @Resource(name = "defaultFundoutService")
//    private DefaultFundoutService     defaultFundoutService;
//
//	@Resource(name = "accountService")
//	private AccountService accountService;
//	
//	@Resource(name = "defaultDownloadBillService")
//	private DefaultDownloadBillService defaultDownloadBillService;
//	
//	@Resource(name = "defaultAccountService")
//    private DefaultAccountService defaultAccountService;
//	
//	private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//	private String txnType1= null;
//	
//	@RequestMapping(value = "/my/all-trade1.htm")
//	public ModelAndView searchAll(HttpServletRequest request,
//			HttpServletResponse resp, boolean error, ModelMap model,
//			OperationEnvironment env,QueryTradeForm form,HttpSession session)  {
//		String refresh = request.getParameter("refresh");
//		if (StringUtils.isNotEmpty(refresh)
//				&& refresh.equals(CommonConstant.TRUE_STRING)) {
//			super.updateSessionObject(request);
//		}
//		RestResponse restP = new RestResponse();
//		String currentPage = request.getParameter("currentPage");
//		String startDate = request.getParameter("queryStartTime");
//		String endDate = request.getParameter("queryEndTime");
//		String txnType = request.getParameter("txnType");// 查询类型--页面获取
////		String txnState = request.getParameter("txnState");
//		String tradeState = form.getTradeStatus();
//		String[] TradeStatusCode = new String[7];
//		if (tradeState == null) {
//			TradeStatusCode[0] = "0";
//			TradeStatusCode[1] = "0";
//			TradeStatusCode[2] = "0";
//			TradeStatusCode[3] = "0";
//			TradeStatusCode[4] = "0";
//            TradeStatusCode[5] = "0";
//            TradeStatusCode[6] = "0";
//		} else {
//			TradeStatusCode = tradeState.split(",");
//		}
//		if (txnType == null) {
//			txnType = "1";// 默认值为1,<网关交易>
//		}
//		if(txnType1==null){
//			txnType1="1";
//		}
//		if(!txnType1.equals(txnType)){
//			startDate=null;
//			endDate=null;
//		}
//		txnType1=txnType;
//		
//		List<String> tradeStatus = new ArrayList<String>();
//		
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("queryStartTime", startDate);
//        map.put("queryEndTime", endDate);
//		PersonMember user = getUser(request);
//		if (user.getMemberId() == null){
//			logger.info("会员Id不能为null");
//		}
//		 
//        String userType = (String)session.getAttribute("userType");
//        String accountType = (String) session.getAttribute("accountType");
////        try {
////            userType = accountService.queryUserType(user.getMemberId());//根据memberId查询企业账户是0:供应商  1:核心企业  -1:普通企业
////        }
////        catch (Exception e) {
////            logger.info("根据memberId查询企业账户类型失败,memberId="+user.getMemberId());
////            e.printStackTrace();
////        }
//        if(userType!=null){
//            map.put("userType", userType);  
//        }
//        if(accountType!=null){
//            map.put("accountType", accountType);  
//        }
//		// 封装交易请求
//		TradeRequest tradeRequest = new TradeRequest();
//		tradeRequest.setMemberId(user.getMemberId());
//		tradeRequest.setTradeStatus(tradeStatus);
//		
//		// 分页信息
//		QueryBase queryBase = new QueryBase();
//		if (StringUtils.isBlank(currentPage) || ("0").equals(currentPage)) {
//			currentPage = "1";
//		}
//		queryBase.setCurrentPage(Integer.valueOf(currentPage));
//		tradeRequest.setQueryBase(queryBase);
//		try {
//			user.setAccount(accountService.queryAccountById(
//					user.getDefaultAccountId(), env));
//		} catch (BizException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		PageResultList<TradeInfo> page = new PageResultList<TradeInfo>();
//		PageResultList<Fundout> page1 = new PageResultList<Fundout>();
//		List<TradeInfo> baseInfo=new ArrayList<TradeInfo>();
//        BigDecimal income=new BigDecimal(Double.toString(0));
//        BigDecimal outpay=new BigDecimal(Double.toString(0));
//		switch (Integer.valueOf(txnType)) {
//		case 1:
//			if(TradeStatusCode[0].equals("1100")){
//				tradeStatus.add("100");
//			}else if(TradeStatusCode[0].equals("1201")){
//				tradeStatus.add("201");
//			}else if(TradeStatusCode[0].equals("1401")){
//				tradeStatus.add("401");
//			}else if(TradeStatusCode[0].equals("1998")){
//				tradeStatus.add("998");
//			}else{
//				tradeStatus.add("100");// 代付款
//				tradeStatus.add("301");// 交易成功
//				tradeStatus.add("401");// 交易结束
//				tradeStatus.add("998");// 交易失败
//				tradeStatus.add("999");// 交易关闭
//				tradeStatus.add("201");// 支付成功
//			}
//			// 设置查询时间
//			if (StringUtils.isNotBlank(startDate)) {
//				if (logger.isInfoEnabled())
//					logger.info("查询交易的起始时间为：" + startDate);
//				try {
//					tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ ":01"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//			} else {
//				startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//				try {
//					tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//	        	startDate = DateUtils.getDateString()+" 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				try {
//					tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//				} catch (ParseException e) {
//					logger.info("查询交易的终止时间错误");
//				}
//			} else {
//				tradeRequest.setGmtEnd(new Date());
//	        	endDate = sdf.format(new Date())+" 23:59";
//			}
//			List<TradeTypeRequest> tradeTypeList = new ArrayList<TradeTypeRequest>();
//			tradeTypeList.add(TradeTypeRequest.INSTANT_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.ENSURE_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.PREPAY_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.REFUND_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.MERGE);
//			tradeRequest.setTradeType(tradeTypeList);
//			// 查询交易记录
//		    try {
//				page = defaultTradeQueryService.queryTradeList(
//						tradeRequest, env);
//			} catch (ServiceException e) {
//				logger.info("查询交易记录失败");
//			}
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", TradeStatusCode[0]);
//			restP.setData(map);
//			restP.setSuccess(true);
//			baseInfo=new ArrayList<TradeInfo>();
//			if(page.getInfos()!=null){
//				baseInfo=page.getInfos();
//			}
//			income=new BigDecimal(Double.toString(0));
//			outpay=new BigDecimal(Double.toString(0));
//			for (TradeInfo tran : baseInfo) {
//				if (tran.getSellerId().equals(user.getMemberId())) {
//					income = (tran.getPayAmount().getAmount()).add(income);
//				}else if (tran.getBuyerId().equals(user.getMemberId())) {
//					outpay = (tran.getPayAmount().getAmount()).add(outpay);
//				}
//			}
//			if (restP.getData() == null) {
//				restP.setData(new HashMap<String, Object>());
//			}
//			restP.getData().put("income", income);
//			restP.getData().put("outpay", outpay);
//			break;
//		case 2:// 转账查询
//			if(TradeStatusCode[1].equals("2100")){
//				tradeStatus.add("100");
////			}else if(TradeStatusCode[1].equals("2201")){
////				tradeStatus.add("201");
//			}else if(TradeStatusCode[1].equals("2401")){
//				tradeStatus.add("401");
//			}else if(TradeStatusCode[1].equals("2998")){
//				tradeStatus.add("998");
//			}else{
//				tradeStatus.add("100");// 代付款
//				tradeStatus.add("301");// 交易成功
//				tradeStatus.add("401");// 交易结束
//				tradeStatus.add("998");// 交易失败
//				tradeStatus.add("999");// 交易关闭
////				tradeStatus.add("201");// 支付成功
//			}
//			// 设置查询时间
//			if (StringUtils.isNotBlank(startDate)) {
//				if (logger.isInfoEnabled())
//					logger.info("查询交易的起始时间为：" + startDate);
//				try {
//					tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ ":01"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//			} else {
//				startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//				try {
//					tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//	        	startDate = DateUtils.getDateString()+" 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				try {
//					tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//				} catch (ParseException e) {
//					logger.info("查询交易的终止时间错误");
//				}
//			} else {
//				tradeRequest.setGmtEnd(new Date());
//	        	endDate = sdf.format(new Date())+" 23:59";
//			}
//			
//			List<String> productCodes = new ArrayList<String>();
//			productCodes.add(TradeType.TRANSFER.getBizProductCode());
//			productCodes.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
//			tradeRequest.setProductCodes(productCodes);
//			tradeRequest.setMemberId(user.getMemberId());
//			queryBase.setCurrentPage(Integer.valueOf(currentPage));
//			tradeRequest.setQueryBase(queryBase);
//
//			try {
//				page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//			} catch (ServiceException e) {
//				logger.info("查询转账记录失败");
//			}
//
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", TradeStatusCode[1]);
//			restP.setData(map);
//			restP.setSuccess(true);
//			baseInfo=new ArrayList<TradeInfo>();
//			if(page.getInfos()!=null){
//				baseInfo=page.getInfos();
//			}
//			income=new BigDecimal(Double.toString(0));
//			outpay=new BigDecimal(Double.toString(0));
//			for (TradeInfo tran : baseInfo) {
//				if (tran.getSellerId().equals(user.getMemberId())) {
//					income = (tran.getPayAmount().getAmount()).add(income);
//				}else if (tran.getBuyerId().equals(user.getMemberId())) {
//					outpay = (tran.getPayAmount().getAmount()).add(outpay);
//				}
//			}
//			if (restP.getData() == null) {
//				restP.setData(new HashMap<String, Object>());
//			}
//			restP.getData().put("income", income);
//			restP.getData().put("outpay", outpay);
//			break;
//			
//		case 3://转账到卡
//			FundoutQuery fundoutQuery = new FundoutQuery();
//			fundoutQuery.setMemberId(user.getMemberId());
//			fundoutQuery.setAccountNo(user.getDefaultAccountId());
//			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
//			if (StringUtils.isNotBlank(startDate)) {
//				try {
//					fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate+ ":01"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//			} else {
//				fundoutQuery
//						.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));
//
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				try {
//					fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate+ ":59"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//			} else {
//				fundoutQuery.setOrderTimeEnd(DateUtil
//						.addMinutes(new Date(), 30));
//			}
//			fundoutQuery.setProductCode(TradeType.PAY_TO_BANK.getBizProductCode()+","+TradeType.PAY_TO_BANK_INSTANT.getBizProductCode());
//			if (TradeStatusCode[2].equals("3222")) {
//				fundoutQuery.setStatus("success");
//			} else if (TradeStatusCode[2].equals("3223")) {
//				fundoutQuery.setStatus("failed");// 失败
//			} else if (TradeStatusCode[2].equals("3221")) {
//				fundoutQuery.setStatus("submitted");//
//			}
//			try {
//				page1 = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
//			} catch (ServiceException e) {
//				logger.info("查询转账到卡记录失败");
//			}
//			map.put("page", page1);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", TradeStatusCode[2]);
//			restP.setData(map);
//			restP.setSuccess(true);
//			break;
//		case 4://代发工资到账户
//			DownloadBillRequest reqInfo = new DownloadBillRequest();
//			List<String> TraStatus = new ArrayList<String>();
//			if (TradeStatusCode[2].equals("662")) {
//				TraStatus.add("301");// 转账成功
//				TraStatus.add("401");// 交易完成
//				reqInfo.setTradeStatus(TraStatus);
//			} else if (TradeStatusCode[2].equals("663")) {
//				TraStatus.add("999");// 交易关闭
//				reqInfo.setTradeStatus(TraStatus);
//			} else if (TradeStatusCode[2].equals("661")) {
//				TraStatus.add("201");//付款成功=处理中
//				TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
//				reqInfo.setTradeStatus(TraStatus);
//			}
//			if (StringUtils.isNotBlank(startDate)) {
//				try {
//					reqInfo.setBeginTime(DateUtils.parseDate(startDate+ ":00"));
//				} catch (ParseException e) {
//					logger.info("查询代发工资的起始时间错误");
//				}
//			} else {
//				reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-30));
//				startDate = DateUtils.getDateString() + " 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				try {
//					reqInfo.setEndTime(DateUtils.parseDate(endDate + ":59"));
//				} catch (ParseException e) {
//					logger.info("查询代发工资的结束时间错误");
//				}
//			} else {
//				reqInfo.setEndTime(new Date());
//				endDate = sdf.format(new Date()) + " 23:59";
//			}
//			List<String> productCode=new ArrayList<String>();
//			productCode.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
//			reqInfo.setProductCodes(productCode);
//			reqInfo.setMemberId(user.getMemberId());
//			QueryBase queryBase4 = new QueryBase();
//			queryBase4.setCurrentPage(Integer.parseInt(currentPage));
//			reqInfo.setQueryBase(queryBase4);
//			page = defaultDownloadBillService.queryTransfer(reqInfo, env);
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", TradeStatusCode[2]);
//			restP.setData(map);
//			restP.setSuccess(true);
//			break;
//		case 5://代发工资
//			fundoutQuery = new FundoutQuery();
//			fundoutQuery.setMemberId(user.getMemberId());
//			fundoutQuery.setAccountNo(user.getDefaultAccountId());
//			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
//			if (StringUtils.isNotBlank(startDate)) {
//				try {
//					fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate+ ":01"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//			} else {
//				fundoutQuery
//						.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));
//
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				try {
//					fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate+ ":59"));
//				} catch (ParseException e) {
//					logger.info("查询交易的起始时间错误");
//				}
//			} else {
//				fundoutQuery.setOrderTimeEnd(DateUtil
//						.addMinutes(new Date(), 30));
//			}
//			fundoutQuery.setProductCode(TradeType.PAYOFF_TO_BANK.getBizProductCode());//代发工资
//			try {
//				page1 = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
//			} catch (ServiceException e) {
//				logger.info("查询代发工资记录失败");
//			}
//			map.put("page", page1);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", TradeStatusCode[2]);
//			restP.setData(map);
//			restP.setSuccess(true);
//			break;
//		case 6:
//		    RechargeQueryRequest query = new RechargeQueryRequest();
//		    
//		    query.setMemberId(user.getMemberId());
//		    
//		    if(PhoneRechargeStatus.RECHARGE_PAYING.getCode().equals(TradeStatusCode[3])){//生产交易订单，未进行支付的订单；支付失败的订单
//		        query.setPayStatus("F001");//F005(待支付,支付失败都指定为待支付)
//		    }else if(PhoneRechargeStatus.RECHARGE_SUCCESS.getCode().equals(TradeStatusCode[3])){//支付成功，充值成功的订单；支付成功，交易处理中的订单
//		        query.setPayStatus("F002");
//		        query.setRechargeStatus("F005");//F005支付成功，充值成功或者充值中的状态（交易成功）
//		    }else if(PhoneRechargeStatus.RECHARGE_FAILD.getCode().equals(TradeStatusCode[3])){//支付成功，充值明确充值失败的订单
//		        //query.setPayStatus("F002");//F002支付成功
//		        query.setRechargeStatus("F002");//F002充值失败
//		    }else if(PhoneRechargeStatus.RECHARGE_CLOSE.getCode().equals(TradeStatusCode[3])){
//		        query.setPayStatus("F003");
//		    }
//		    if (StringUtils.isNotBlank(startDate)) {
//                try {
//                    query.setStartDate(DateUtils.parseDate(startDate+ ":00"));
//                } catch (ParseException e) {
//                    logger.info("查询话费充值起始时间错误");
//                }
//            } else {
//                query.setStartDate(DateUtil.getDateNearCurrent(-30));
//                startDate = DateUtils.getDateString() + " 00:00";
//            }
//            if (StringUtils.isNotBlank(endDate)) {
//                try {
//                    query.setEndDate(DateUtils.parseDate(endDate + ":59"));
//                } catch (ParseException e) {
//                    logger.info("查询话费充值结束时间错误");
//                }
//            } else {
//                query.setEndDate(new Date());
//                endDate = sdf.format(new Date()) + " 23:59";
//            }
//            int totalItem = rechargeQueryServiceFacade.count(query);
//            query.setCurrentPage(Integer.valueOf(currentPage));
//            query.setPageSize(20);
//		    RechargeQueryResponse rechargeResponse = rechargeQueryServiceFacade.query(query);
//		    int totalPage = (totalItem+query.getPageSize()-1)/query.getPageSize();
//		    map.put("rechargeResponse", rechargeResponse);
//		    map.put("txnState", TradeStatusCode[3]);
//		    map.put("txnType", txnType);
//		    map.put("totalItem", totalItem);
//		    map.put("totalPage", totalPage);
//		    map.put("pageSize", query.getPageSize());
//		    map.put("currentPage", currentPage);
//		    restP.setData(map);
//		    break;
//		case 7:// 查询保理放贷
//		    if(TradeStatusCode[4].equals("7100")){
//                tradeStatus.add("100");
//            }else if(TradeStatusCode[4].equals("7401")){
//                tradeStatus.add("401");
//            }else if(TradeStatusCode[4].equals("7998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("301");// 交易成功
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//                tradeStatus.add("999");// 交易关闭
//            }
//            // 设置查询时间
//            if (StringUtils.isNotBlank(startDate)) {
//                if (logger.isInfoEnabled())
//                    logger.info("查询保理放贷的起始时间为：" + startDate);
//                try {
//                    tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ ":01"));
//                } catch (ParseException e) {
//                    logger.info("查询保理放贷的起始时间错误");
//                }
//            } else {
//                startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//                try {
//                    tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//                } catch (ParseException e) {
//                    logger.info("查询保理放贷的起始时间错误");
//                }
//                startDate = DateUtils.getDateString()+" 00:00";
//            }
//            if (StringUtils.isNotBlank(endDate)) {
//                try {
//                    tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//                } catch (ParseException e) {
//                    logger.info("查询保理放贷的终止时间错误");
//                }
//            } else {
//                tradeRequest.setGmtEnd(new Date());
//                endDate = sdf.format(new Date())+" 23:59";
//            }
//            
//            productCodes = new ArrayList<String>();
//            productCodes.add(TradeType.baoli_loan.getBizProductCode());
//            tradeRequest.setProductCodes(productCodes);
//            tradeRequest.setMemberId(user.getMemberId());
//            queryBase.setCurrentPage(Integer.valueOf(currentPage));
//            tradeRequest.setQueryBase(queryBase);
//
//            try {
//                page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//            } catch (ServiceException e) {
//                logger.info("查询保理放贷记录失败");
//            }
//
//            map.put("page", page);
//            map.put("mobile", user.getMobileStar());
//            map.put("member", user);
//            
//            map.put("txnType", txnType);
//            map.put("txnState", TradeStatusCode[4]);
//            restP.setData(map);
//            restP.setSuccess(true);
//            baseInfo=new ArrayList<TradeInfo>();
//            if(page.getInfos()!=null){
//                baseInfo=page.getInfos();
//            }
//            income=new BigDecimal(Double.toString(0));
//            outpay=new BigDecimal(Double.toString(0));
//            for (TradeInfo tran : baseInfo) {
//                if (tran.getSellerId().equals(user.getMemberId())) {
//                    income = (tran.getPayAmount().getAmount()).add(income);
//                }else if (tran.getBuyerId().equals(user.getMemberId())) {
//                    if(!"100".equals(tran.getStatus())){
//                        outpay = (tran.getPayAmount().getAmount()).add(outpay);
//                    }
//                }
//            }
//            if (restP.getData() == null) {
//                restP.setData(new HashMap<String, Object>());
//            }
//            restP.getData().put("income", income);
//            restP.getData().put("outpay", outpay);
//            break;
//		case 8:// 查询保理代扣
//		    if(TradeStatusCode[5].equals("8100")){
//                tradeStatus.add("100");
//            }else if(TradeStatusCode[5].equals("8401")){
//                tradeStatus.add("401");
//            }else if(TradeStatusCode[5].equals("8998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("301");// 交易成功
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//                tradeStatus.add("999");// 交易关闭
//            }
//            // 设置查询时间
//            if (StringUtils.isNotBlank(startDate)) {
//                if (logger.isInfoEnabled())
//                    logger.info("查询保理代扣的起始时间为：" + startDate);
//                try {
//                    tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ ":01"));
//                } catch (ParseException e) {
//                    logger.info("查询保理代扣的起始时间错误");
//                }
//            } else {
//                startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//                try {
//                    tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//                } catch (ParseException e) {
//                    logger.info("查询保理代扣的起始时间错误");
//                }
//                startDate = DateUtils.getDateString()+" 00:00";
//            }
//            if (StringUtils.isNotBlank(endDate)) {
//                try {
//                    tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//                } catch (ParseException e) {
//                    logger.info("查询保理代扣的终止时间错误");
//                }
//            } else {
//                tradeRequest.setGmtEnd(new Date());
//                endDate = sdf.format(new Date())+" 23:59";
//            }
//            
//            productCodes = new ArrayList<String>();
//            productCodes.add(TradeType.baoli_withholding.getBizProductCode());
//            tradeRequest.setProductCodes(productCodes);
//            tradeRequest.setMemberId(user.getMemberId());
//            queryBase.setCurrentPage(Integer.valueOf(currentPage));
//            tradeRequest.setQueryBase(queryBase);
//
//            try {
//                page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//            } catch (ServiceException e) {
//                logger.info("查询保理代扣记录失败");
//            }
//
//            map.put("page", page);
//            map.put("mobile", user.getMobileStar());
//            map.put("member", user);
//            
//            map.put("txnType", txnType);
//            map.put("txnState", TradeStatusCode[5]);
//            restP.setData(map);
//            restP.setSuccess(true);
//            baseInfo=new ArrayList<TradeInfo>();
//            if(page.getInfos()!=null){
//                baseInfo=page.getInfos();
//            }
//            income=new BigDecimal(Double.toString(0));
//            outpay=new BigDecimal(Double.toString(0));
//            for (TradeInfo tran : baseInfo) {
//                if (tran.getSellerId().equals(user.getMemberId())) {
//                    income = (tran.getPayAmount().getAmount()).add(income);
//                }else if (tran.getBuyerId().equals(user.getMemberId())) {
//                    if(!"100".equals(tran.getStatus())){
//                        outpay = (tran.getPayAmount().getAmount()).add(outpay);
//                    }
//                }
//            }
//            if (restP.getData() == null) {
//                restP.setData(new HashMap<String, Object>());
//            }
//            restP.getData().put("income", income);
//            restP.getData().put("outpay", outpay);
//            break;
//		case 9:// 查询保理还款
//		    if(TradeStatusCode[6].equals("9100")){
//                tradeStatus.add("100");
//            }else if(TradeStatusCode[6].equals("9401")){
//                tradeStatus.add("401");
//            }else if(TradeStatusCode[6].equals("9998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("301");// 交易成功
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//                tradeStatus.add("999");// 交易关闭
//            }
//            // 设置查询时间
//            if (StringUtils.isNotBlank(startDate)) {
//                if (logger.isInfoEnabled())
//                    logger.info("查询保理还款的起始时间为：" + startDate);
//                try {
//                    tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ ":01"));
//                } catch (ParseException e) {
//                    logger.info("查询保理还款的起始时间错误");
//                }
//            } else {
//                startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//                try {
//                    tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//                } catch (ParseException e) {
//                    logger.info("查询保理还款的起始时间错误");
//                }
//                startDate = DateUtils.getDateString()+" 00:00";
//            }
//            if (StringUtils.isNotBlank(endDate)) {
//                try {
//                    tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//                } catch (ParseException e) {
//                    logger.info("查询保理还款的终止时间错误");
//                }
//            } else {
//                tradeRequest.setGmtEnd(new Date());
//                endDate = sdf.format(new Date())+" 23:59";
//            }
//            
//            productCodes = new ArrayList<String>();
//            productCodes.add(TradeType.baoli_repayment.getBizProductCode());
//            tradeRequest.setProductCodes(productCodes);
//            tradeRequest.setMemberId(user.getMemberId());
//            queryBase.setCurrentPage(Integer.valueOf(currentPage));
//            tradeRequest.setQueryBase(queryBase);
//
//            try {
//                page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//            } catch (ServiceException e) {
//                logger.info("查询保理还款记录失败");
//            }
//
//            map.put("page", page);
//            map.put("mobile", user.getMobileStar());
//            map.put("member", user);
//            
//            map.put("txnType", txnType);
//            map.put("txnState", TradeStatusCode[6]);
//            restP.setData(map);
//            restP.setSuccess(true);
//            baseInfo=new ArrayList<TradeInfo>();
//            if(page.getInfos()!=null){
//                baseInfo=page.getInfos();
//            }
//            income=new BigDecimal(Double.toString(0));
//            outpay=new BigDecimal(Double.toString(0));
//            for (TradeInfo tran : baseInfo) {
//                if (tran.getSellerId().equals(user.getMemberId())) {
//                    income = (tran.getPayAmount().getAmount()).add(income);
//                }else if (tran.getBuyerId().equals(user.getMemberId())) {
//                    if(!"100".equals(tran.getStatus())){
//                        outpay = (tran.getPayAmount().getAmount()).add(outpay);
//                    }
//                }
//            }
//            if (restP.getData() == null) {
//                restP.setData(new HashMap<String, Object>());
//            }
//            restP.getData().put("income", income);
//            restP.getData().put("outpay", outpay);
//            break;
//		default:
//			break;
//		}
//		
//		return new ModelAndView(CommonConstant.URL_PREFIX
//				+ "/trade/transactionrecords", "response", restP);
//	}
//	
//	@RequestMapping(value = "/my/all-trade-download.htm")
//	public void download(HttpServletRequest request,
//			HttpServletResponse resp, boolean error, ModelMap model,
//			OperationEnvironment env) throws Exception {
//		String refresh = request.getParameter("refresh");
//		if (StringUtils.isNotEmpty(refresh)
//				&& refresh.equals(CommonConstant.TRUE_STRING)) {
//			super.updateSessionObject(request);
//		}
//		RestResponse restP = new RestResponse();
//		String currentPage = "1";
//		String startDate = request.getParameter("queryStartTime");
//		String endDate = request.getParameter("queryEndTime");
//
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("queryStartTime", startDate);
//        map.put("queryEndTime", endDate);
//		PersonMember user = getUser(request);
//		if (user.getMemberId() == null)
//			throw new IllegalAccessException("illegal user error!");
//		// 封装交易请求
//		TradeRequest tradeRequest = new TradeRequest();
//		tradeRequest.setMemberId(user.getMemberId());
//		
//
//		// 分页信息
//		QueryBase queryBase = new QueryBase();
//		if (StringUtils.isBlank(currentPage)) {
//			currentPage = "1";
//		}
//		queryBase.setCurrentPage(Integer.valueOf(currentPage));
//		queryBase.setPageSize(50000);
//		tradeRequest.setQueryBase(queryBase);
//		user.setAccount(accountService.queryAccountById(
//				user.getDefaultAccountId(), env));
//
//		String txnType = request.getParameter("txnType");// 查询类型--页面获取
//		if (txnType == null) {
//			txnType = "1";// 默认值为1,<网关交易>
//		}
//		String txnState = request.getParameter("txnState");
//		List<String> tradeStatus = new ArrayList<String>();
//		tradeRequest.setTradeStatus(tradeStatus);
//		ExcelUtil excelUtil = new ExcelUtil();
//		PageResultList page = new PageResultList();
//		switch (Integer.valueOf(txnType)) {
//		case 0:
//			// 设置查询时间
//			if (StringUtils.isNotBlank(startDate)) {
//				if (logger.isInfoEnabled())
//					logger.info("查询交易的起始时间为：" + startDate);
//				tradeRequest.setGmtStart(DateUtils.parseDate(startDate
//						+ ":01"));
//			} else {
//				startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//				tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//	        	startDate = DateUtils.getDateString()+" 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//			} else {
//				tradeRequest.setGmtEnd(new Date());
//	        	endDate = sdf.format(new Date())+" 23:59";
//			}
//			
//			// 查询交易记录
//			page = defaultTradeQueryService.queryTradeList(
//					tradeRequest, env);
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", txnState);
//			restP.setData(map);
//			restP.setSuccess(true);
//			if(page.getInfos()==null)
//			{
//				List<TradeInfo> baseInfo=new ArrayList<TradeInfo>();
//				page.setInfos(baseInfo);
//			}
//			excelUtil.toExcel1(request, resp,page.getInfos(),user.getMemberId(),0, startDate, endDate);
//			break;
//		case 1:
//		    if(txnState.equals("1100")){
//                tradeStatus.add("100");
//            }else if(txnState.equals("1201")){
//                tradeStatus.add("201");
//            }else if(txnState.equals("1401")){
//                tradeStatus.add("401");
//            }else if(txnState.equals("1998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("301");// 交易成功
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//                tradeStatus.add("999");// 交易关闭
//                tradeStatus.add("201");// 支付成功
//            }
//			// 设置查询时间
//			if (StringUtils.isNotBlank(startDate)) {
//				if (logger.isInfoEnabled())
//					logger.info("查询交易的起始时间为：" + startDate);
//				tradeRequest.setGmtStart(DateUtils.parseDate(startDate
//						+ ":01"));
//			} else {
//				startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//				tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//	        	startDate = DateUtils.getDateString()+" 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//			} else {
//				tradeRequest.setGmtEnd(new Date());
//	        	endDate = sdf.format(new Date())+" 23:59";
//			}
//			List<TradeTypeRequest> tradeTypeList = new ArrayList<TradeTypeRequest>();
//			tradeTypeList.add(TradeTypeRequest.INSTANT_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.ENSURE_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.PREPAY_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.REFUND_ACQUIRING);
//			tradeTypeList.add(TradeTypeRequest.MERGE);
//			tradeRequest.setTradeType(tradeTypeList);
//			// 查询交易记录
//			page = defaultTradeQueryService.queryTradeList(
//					tradeRequest, env);
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", txnState);
//			restP.setData(map);
//			restP.setSuccess(true);
//			if(page.getInfos()==null)
//			{
//				List<TradeInfo> baseInfo=new ArrayList<TradeInfo>();
//				page.setInfos(baseInfo);
//			}
//			excelUtil.toExcel1(request, resp,page.getInfos(), user.getMemberId(),0, startDate, endDate);
//			break;
//		case 2:// 转账查询
//		    if(txnState.equals("2100")){
//                tradeStatus.add("100");
//            }else if(txnState.equals("2401")){
//                tradeStatus.add("401");
//            }else if(txnState.equals("2998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//            }
//			// 设置查询时间
//			if (StringUtils.isNotBlank(startDate)) {
//				if (logger.isInfoEnabled())
//					logger.info("查询交易的起始时间为：" + startDate);
//				tradeRequest.setGmtStart(DateUtils.parseDate(startDate
//						+ ":01"));
//			} else {
//				startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//				tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//	        	startDate = DateUtils.getDateString()+" 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//			} else {
//				tradeRequest.setGmtEnd(new Date());
//	        	endDate = sdf.format(new Date())+" 23:59";
//			}
//			
//			List<String> productCodes = new ArrayList<String>();
//            productCodes.add(TradeType.TRANSFER.getBizProductCode());
//            productCodes.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
//            tradeRequest.setProductCodes(productCodes);
//			tradeRequest.setMemberId(user.getMemberId());
//			queryBase.setCurrentPage(Integer.valueOf(currentPage));
//			tradeRequest.setQueryBase(queryBase);
//
//			page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", txnState);
//			restP.setData(map);
//			restP.setSuccess(true);
//			if(page.getInfos()==null)
//			{
//				List<TradeInfo> baseInfo=new ArrayList<TradeInfo>();
//				page.setInfos(baseInfo);
//			}
//			excelUtil.toExcel1(request, resp,page.getInfos(),user.getMemberId(), 2, startDate, endDate);
//			break;
//			
//		case 3://转账到卡
//			FundoutQuery fundoutQuery = new FundoutQuery();
//			fundoutQuery.setMemberId(user.getMemberId());
//			fundoutQuery.setAccountNo(user.getDefaultAccountId());
//			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
//			if (StringUtils.isNotBlank(startDate)) {
//				fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate
//						+ ":01"));
//			} else {
//				fundoutQuery
//						.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));
//				startDate = DateUtils.getDateString()+" 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate
//						+ ":59"));
//			} else {
//				fundoutQuery.setOrderTimeEnd(DateUtil
//						.addMinutes(new Date(), 30));
//				endDate = sdf.format(new Date())+" 23:59";
//			}
//			if (txnState.equals("3222")) {
//                fundoutQuery.setStatus("success");
//            } else if (txnState.equals("3223")) {
//                fundoutQuery.setStatus("failed");// 失败
//            } else if (txnState.equals("3221")) {
//                fundoutQuery.setStatus("submitted");//
//            }
//			fundoutQuery.setPageSize(50000);
//			fundoutQuery.setProductCode(TradeType.PAY_TO_BANK.getBizProductCode()+","+TradeType.PAY_TO_BANK_INSTANT.getBizProductCode());//代发工资
//			PageResultList<Fundout> page1 = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
//			map = new HashMap<String, Object>();
//			map.put("page", page1);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", txnState);
//			restP.setData(map);
//			restP.setSuccess(true);
//			if(page1.getInfos()==null)
//			{
//				List<Fundout> baseInfo=new ArrayList<Fundout>();
//				page.setInfos(baseInfo);
//			}
//			excelUtil.toExcel1(request, resp,page1.getInfos(),user.getMemberId(), 3, startDate, endDate);
//			break;
//		case 4://代发工资到账户
//			DownloadBillRequest reqInfo = new DownloadBillRequest();
//			List<String> TraStatus = new ArrayList<String>();
//			if (txnState.equals("662")) {
//				TraStatus.add("301");// 转账成功
//				TraStatus.add("401");// 交易完成
//				reqInfo.setTradeStatus(TraStatus);
//			} else if (txnState.equals("663")) {
//				TraStatus.add("999");// 交易关闭
//				reqInfo.setTradeStatus(TraStatus);
//			} else if (txnState.equals("661")) {
//				TraStatus.add("201");//付款成功=处理中
//				TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
//				reqInfo.setTradeStatus(TraStatus);
//			}
//			if (StringUtils.isNotBlank(startDate)) {
//				try {
//					reqInfo.setBeginTime(DateUtils.parseDate(startDate+ ":00"));
//				} catch (ParseException e) {
//					logger.info("查询代发工资的起始时间错误");
//				}
//			} else {
//				reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-30));
//				startDate = DateUtils.getDateString() + " 00:00";
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				try {
//					reqInfo.setEndTime(DateUtils.parseDate(endDate + ":59"));
//				} catch (ParseException e) {
//					logger.info("查询代发工资的结束时间错误");
//				}
//			} else {
//				reqInfo.setEndTime(new Date());
//				endDate = sdf.format(new Date()) + " 23:59";
//			}
//			List<String> productCode=new ArrayList<String>();
//			productCode.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
//			reqInfo.setProductCodes(productCode);
//			reqInfo.setMemberId(user.getMemberId());
//			QueryBase queryBase4 = new QueryBase();
//			queryBase4.setCurrentPage(Integer.parseInt(currentPage));
//			reqInfo.setQueryBase(queryBase4);
//			page = defaultDownloadBillService.queryTransfer(reqInfo, env);
//			map = new HashMap<String, Object>();
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", txnState);
//			restP.setData(map);
//			restP.setSuccess(true);
//			if(page.getInfos()==null)
//			{
//				List<TransferInfo> baseInfo=new ArrayList<TransferInfo>();
//				page.setInfos(baseInfo);
//			}
//			excelUtil.toExcel1(request, resp,page.getInfos(),user.getMemberId(), 4, startDate, endDate);
//			break;
//		case 5://代发工资
//			fundoutQuery = new FundoutQuery();
//			fundoutQuery.setMemberId(user.getMemberId());
//			fundoutQuery.setAccountNo(user.getDefaultAccountId());
//			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
//			if (StringUtils.isNotBlank(startDate)) {
//				fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate
//						+ " 00:00:01"));
//			} else {
//				fundoutQuery
//						.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));
//
//			}
//			if (StringUtils.isNotBlank(endDate)) {
//				fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate
//						+ " 23:59:59"));
//			} else {
//				fundoutQuery.setOrderTimeEnd(DateUtil
//						.addMinutes(new Date(), 30));
//			}
//			fundoutQuery.setProductCode(TradeType.PAYOFF_TO_BANK.getBizProductCode());//代发工资
//			fundoutQuery.setPageSize(50000);
//			page = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
//			map = new HashMap<String, Object>();
//			map.put("page", page);
//			map.put("mobile", user.getMobileStar());
//			map.put("member", user);
//			
//			map.put("txnType", txnType);
//			map.put("txnState", txnState);
//			restP.setData(map);
//			restP.setSuccess(true);
//			excelUtil.toExcel1(request, resp,page.getInfos(),user.getMemberId(), 5, startDate, endDate);
//			break;
//		case 7:// 保理放贷导出
//            if(txnState.equals("7100")){
//                tradeStatus.add("100");
//            }else if(txnState.equals("7401")){
//                tradeStatus.add("401");
//            }else if(txnState.equals("7998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//            }
//            // 设置查询时间
//            if (StringUtils.isNotBlank(startDate)) {
//                if (logger.isInfoEnabled())
//                    logger.info("查询保理放贷的起始时间为：" + startDate);
//                tradeRequest.setGmtStart(DateUtils.parseDate(startDate
//                        + ":01"));
//            } else {
//                startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//                tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//                startDate = DateUtils.getDateString()+" 00:00";
//            }
//            if (StringUtils.isNotBlank(endDate)) {
//                tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//            } else {
//                tradeRequest.setGmtEnd(new Date());
//                endDate = sdf.format(new Date())+" 23:59";
//            }
//            
//            productCodes = new ArrayList<String>();
//            productCodes.add(TradeType.baoli_loan.getBizProductCode());
//            tradeRequest.setProductCodes(productCodes);
//            tradeRequest.setMemberId(user.getMemberId());
//            queryBase.setCurrentPage(Integer.valueOf(currentPage));
//            tradeRequest.setQueryBase(queryBase);
//
//            page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//
//            map.put("page", page);
//            map.put("mobile", user.getMobileStar());
//            map.put("member", user);
//            
//            map.put("txnType", txnType);
//            map.put("txnState", txnState);
//            restP.setData(map);
//            restP.setSuccess(true);
//            if(page.getInfos()==null)
//            {
//                List<TradeInfo> baseInfo=new ArrayList<TradeInfo>();
//                page.setInfos(baseInfo);
//            }
//            excelUtil.toExcel1(request, resp,page.getInfos(),user.getMemberId(), 7, startDate, endDate);
//            break;
//		case 8:// 保理代扣导出
//            if(txnState.equals("8100")){
//                tradeStatus.add("100");
//            }else if(txnState.equals("8401")){
//                tradeStatus.add("401");
//            }else if(txnState.equals("8998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//            }
//            // 设置查询时间
//            if (StringUtils.isNotBlank(startDate)) {
//                if (logger.isInfoEnabled())
//                    logger.info("查询保理代扣的起始时间为：" + startDate);
//                tradeRequest.setGmtStart(DateUtils.parseDate(startDate
//                        + ":01"));
//            } else {
//                startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//                tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//                startDate = DateUtils.getDateString()+" 00:00";
//            }
//            if (StringUtils.isNotBlank(endDate)) {
//                tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//            } else {
//                tradeRequest.setGmtEnd(new Date());
//                endDate = sdf.format(new Date())+" 23:59";
//            }
//            
//            productCodes = new ArrayList<String>();
//            productCodes.add(TradeType.baoli_withholding.getBizProductCode());
//            tradeRequest.setProductCodes(productCodes);
//            tradeRequest.setMemberId(user.getMemberId());
//            queryBase.setCurrentPage(Integer.valueOf(currentPage));
//            tradeRequest.setQueryBase(queryBase);
//
//            page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//
//            map.put("page", page);
//            map.put("mobile", user.getMobileStar());
//            map.put("member", user);
//            
//            map.put("txnType", txnType);
//            map.put("txnState", txnState);
//            restP.setData(map);
//            restP.setSuccess(true);
//            if(page.getInfos()==null)
//            {
//                List<TradeInfo> baseInfo=new ArrayList<TradeInfo>();
//                page.setInfos(baseInfo);
//            }
//            excelUtil.toExcel1(request, resp,page.getInfos(),user.getMemberId(), 8, startDate, endDate);
//            break;
//		case 9:// 保理还款导出
//            if(txnState.equals("9100")){
//                tradeStatus.add("100");
//            }else if(txnState.equals("9401")){
//                tradeStatus.add("401");
//            }else if(txnState.equals("9998")){
//                tradeStatus.add("998");
//            }else{
//                tradeStatus.add("100");// 代付款
//                tradeStatus.add("401");// 交易结束
//                tradeStatus.add("998");// 交易失败
//            }
//            // 设置查询时间
//            if (StringUtils.isNotBlank(startDate)) {
//                if (logger.isInfoEnabled())
//                    logger.info("查询交易的起始时间为：" + startDate);
//                tradeRequest.setGmtStart(DateUtils.parseDate(startDate
//                        + ":01"));
//            } else {
//                startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
//                tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
//                startDate = DateUtils.getDateString()+" 00:00";
//            }
//            if (StringUtils.isNotBlank(endDate)) {
//                tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + ":59"));
//            } else {
//                tradeRequest.setGmtEnd(new Date());
//                endDate = sdf.format(new Date())+" 23:59";
//            }
//            
//            productCodes = new ArrayList<String>();
//            productCodes.add(TradeType.baoli_repayment.getBizProductCode());
//            tradeRequest.setProductCodes(productCodes);
//            tradeRequest.setMemberId(user.getMemberId());
//            queryBase.setCurrentPage(Integer.valueOf(currentPage));
//            tradeRequest.setQueryBase(queryBase);
//
//            page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
//
//            map.put("page", page);
//            map.put("mobile", user.getMobileStar());
//            map.put("member", user);
//            
//            map.put("txnType", txnType);
//            map.put("txnState", txnState);
//            restP.setData(map);
//            restP.setSuccess(true);
//            if(page.getInfos()==null)
//            {
//                List<TradeInfo> baseInfo=new ArrayList<TradeInfo>();
//                page.setInfos(baseInfo);
//            }
//            excelUtil.toExcel1(request, resp,page.getInfos(),user.getMemberId(), 9, startDate, endDate);
//            break;
//		default:
//			break;
//		}
//	}
//}
