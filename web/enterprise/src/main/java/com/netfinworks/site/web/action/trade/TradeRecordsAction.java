package com.netfinworks.site.web.action.trade;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.batchservice.facade.api.BatchServiceFacade;
import com.netfinworks.batchservice.facade.enums.ProductType;
import com.netfinworks.batchservice.facade.enums.SubmitType;
import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.batchservice.facade.model.BatchRefundDetail;
import com.netfinworks.batchservice.facade.request.BatchRequest;
import com.netfinworks.batchservice.facade.response.BatchResponse;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.fos.service.facade.enums.FundoutStatus;
import com.netfinworks.payment.common.v2.enums.PartyRole;
import com.netfinworks.pbs.service.context.vo.PartyFeeInfo;
import com.netfinworks.pbs.service.context.vo.PayPricingReq;
import com.netfinworks.pbs.service.context.vo.PaymentPricingResponse;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.pfs.service.manager.basis.baseinfo.BankInfoFacade;
import com.netfinworks.pfs.service.manager.basis.domain.BankInfoVO;
import com.netfinworks.pfs.service.manager.common.QueryResult;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.dal.daointerface.AuditDAO;
import com.netfinworks.site.core.dal.daointerface.PosTradeDOMapper;
import com.netfinworks.site.core.dal.dataobject.AuditDO;
import com.netfinworks.site.core.dal.dataobject.PosRequest;
import com.netfinworks.site.core.dal.dataobject.PosTradeDO;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.SummaryInfo;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.AuditListQuery;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRefundRequset;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.domain.trade.TransferInfo;
import com.netfinworks.site.domain.enums.AccessChannel;
import com.netfinworks.site.domain.enums.BankType;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.enums.TradeTypeRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.convert.AuditConvert;
import com.netfinworks.site.domainservice.convert.RefundConvertor;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.pos.PosTradeRequest;
import com.netfinworks.site.domainservice.pos.PosTradeResponse;
import com.netfinworks.site.domainservice.pos.PosTradeService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.BatchRefundForm;
import com.netfinworks.site.web.action.form.QueryTradeForm;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.common.constant.PayChannel;
import com.netfinworks.site.web.common.constant.PaymentType;
import com.netfinworks.site.web.common.util.ObjectUtils;
import com.netfinworks.site.web.common.vo.SessionPage;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.tradeservice.facade.response.RefundResponse;
import com.netfinworks.voucher.common.utils.JsonUtils;
import com.netfinworks.voucher.service.facade.VoucherFacade;
import com.netfinworks.voucher.service.facade.domain.enums.VoucherInfoType;

@Controller
public class TradeRecordsAction extends BaseAction {
	@Resource(name = "defaultFundoutService")
	private DefaultFundoutService defaultFundoutService;

	@Resource(name = "defaultTradeQueryService")
	private DefaultTradeQueryService defaultTradeQueryService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	@Resource(name = "defaultDepositInfoService")
	private DefaultDepositInfoService defaultDepositInfoService;

	@Resource(name = "tradeService")
	private TradeService tradeService;

	@Resource(name = "defaultDownloadBillService")
	private DefaultDownloadBillService defaultDownloadBillService;

	@Resource(name = "auditService") 
	private AuditServiceImpl auditService;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private SimpleDateFormat sdfhms = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Resource
	private OperatorAuthOuterService operatorAuthOuterService;

	@Resource(name = "accountService")
	private AccountService accountService;

	@Resource(name = "voucherService")
	private VoucherService voucherService;   
	
	@Resource(name = "posTradeService")//pos**************
	private PosTradeService posTradeService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;

	@Resource(name = "defaultWithdrawService")
	private DefaultWithdrawService defaultWithdrawService;
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	@Resource(name = "payPartyFeeFacade")
	private PayPartyFeeFacade payPartyFeeFacade;
	private String txnType1= null;
	@Resource(name = "auditDAO")
    private AuditDAO auditDAO;
	@Resource(name="voucherFacade")
	private VoucherFacade voucherFacade;
	
	@Resource(name="bankInfoFacade")
	private BankInfoFacade bankInfoFacade;
	
	@Resource(name = "webResource")
	private WebDynamicResource webResource;
	
	@Resource(name = "fundsControlService")
    private FundsControlService fundsControlService;

	@Resource(name = "memberService")
    private MemberService memberService;
	
	private SimpleDateFormat YmdHmsNoSign = new SimpleDateFormat("yyyyMMddHHmmss");
	
	@Resource(name = "batchServiceFacade")
    private BatchServiceFacade batchServiceFacade;
	/**
	 * 下载操作 导出Excel
	 * 
	 * @param request
	 * @param response
	 * @param env
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/TradeToExcel.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public void downloadToExcel(HttpServletRequest request,
			HttpServletResponse response, boolean error, ModelMap model,
			OperationEnvironment env, QueryTradeForm form) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String txnType = form.getTxnType();// 查询类型--页面获取
		if (txnType == null) {
			txnType = "1";// 默认值为1,<网关交易>
		}
		SummaryInfo sum = new SummaryInfo();
		String tradeType = form.getTradeType();// 交易类型
		String tradeStatus = form.getTradeStatus();// 交易状态--页面获取；页面有七个hidden的name为tradeStatus；当都为空时，返回值为(,,,,,,)
		String currentPage = form.getCurrentPage();// 4 String currentPage =
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3

		String keyword = form.getKeyword();
		if ((keyword == null) || keyword.equals("")) {
			keyword = "-1";
		}
		String keyvalue = form.getKeyvalue();

		String queryStartTime = form.getQueryStartTime();// 查询时间//5、
		String queryEndTime = form.getQueryEndTime();// 查询时间 //5、
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		TradeExcelUtil excelUtil = new TradeExcelUtil();
		switch (Integer.valueOf(txnType)) {
		case 1:// 网关交易 有值0721
		case 11:
			QueryBase queryBase = new QueryBase();// 分页信息
			queryBase.setNeedQueryAll(true);
			queryBase.setPageSize(50000);
			queryBase.setCurrentPage(Integer.valueOf(currentPage));// currentPage

			CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装	
//			// 交易类型 -条件封装
			List<TradeTypeRequest> tradeTypes = new ArrayList<TradeTypeRequest>();
			List<String> productCodes=new ArrayList<String>();
			if (txnType.equals("11")) {//银行卡代扣
				productCodes.add(TradeType.bank_withholding.getBizProductCode());
				tradeTypes.add(TradeTypeRequest.getByCode("11"));
				tradeTypes.add(TradeTypeRequest.getByCode("17"));
			}else if (tradeType.equals("11")) {
				productCodes.add(TradeType.PAY_INSTANT.getBizProductCode());
				productCodes.add(TradeType.phone_recharge.getBizProductCode());
				tradeTypes.add(TradeTypeRequest.getByCode("11"));
			} else if (tradeType.equals("12")) {
				tradeTypes.add(TradeTypeRequest.getByCode("12"));
				productCodes.add(TradeType.PAY_ENSURE.getBizProductCode());
			}else {
				// 不需要的数据，要删掉
				productCodes.add(TradeType.PAY_INSTANT.getBizProductCode());
				productCodes.add(TradeType.phone_recharge.getBizProductCode());
				productCodes.add(TradeType.PAY_ENSURE.getBizProductCode());
				tradeTypes.add(TradeTypeRequest.getByCode("11"));
				tradeTypes.add(TradeTypeRequest.getByCode("12"));
			}
			breq.setProductCodes(productCodes);
			breq.setTradeType(tradeTypes);
			// 交易状态 -条件封装
			List<String> status = new ArrayList<String>();
			if (tradeStatus.equals("100")) {
				status.add("100");// 待支付
			} else if (tradeStatus.equals("301")) {
				status.add("301");// 交易成功
			} else if (tradeStatus.equals("401")) {
				status.add("401");// 交易结束
			} else if (tradeStatus.equals("998")) {
				status.add("998");// 交易失败
				status.add("999");// 交易关闭
			} else if (tradeStatus.equals("201")) {
				status.add("201");// 支付成功
			} else {
				status.add("100");// 代付款
				status.add("301");// 交易成功
				status.add("401");// 交易结束
				status.add("998");// 交易失败
				status.add("999");// 交易关闭
				status.add("201");// 支付成功
			}
			breq.setStatus(status);
			// 判断关键字具体赋给具体的查询条件项
			if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
				breq.setTradeSourceVoucherNo(keyvalue);
			}else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
				breq.setSourceBatchNo(keyvalue);
			}
			breq.setQueryBase(queryBase);
			breq.setNeedSummary(true);// 需要汇总
			//breq.setMemberId(user.getMemberId());
			breq.setSellerId(user.getMemberId());
			if (StringUtils.isNotBlank(queryStartTime)) {
				breq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
			    breq.setBeginTime(sdfhms.parse(queryStartTime + ":00"));

			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				breq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));

			} else {
				breq.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}

			CoTradeQueryResponse rep = tradeService.queryList(breq);// totalTradeCount
			if(rep.getBaseInfoList()==null)
			{
				List<BaseInfo> baseInfo=new ArrayList<BaseInfo>();
				rep.setBaseInfoList(baseInfo);
			}
			excelUtil.toExcel(request, response, rep.getBaseInfoList(),sum, "", Integer.valueOf(txnType),
					queryStartTime, queryEndTime);
			break;
		
		case 2:// 充值 没值0721
			DepositListRequest dRequest = new DepositListRequest();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				dRequest.setTradeVoucherNo(keyvalue);
			}
			dRequest.setMemberId(user.getMemberId());
			dRequest.setAccountNo(user.getDefaultAccountId());
			PageInfo pageInfo = new PageInfo();
			pageInfo.setCurrentPage(Integer.valueOf(currentPage));
			pageInfo.setPageSize(50000);
			dRequest.setPageInfo(pageInfo);
			dRequest.setRequestId(System.currentTimeMillis()
					+ user.getMemberId());

			if (StringUtils.isNotBlank(queryStartTime)) {
				dRequest.setTimeBegin(DateUtils.parseDate(queryStartTime
						+ ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				dRequest.setTimeBegin(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				dRequest.setTimeEnd(DateUtils.parseDate(queryEndTime + ":59"));
			} else {
				dRequest.setTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 交易状态
			if (tradeStatus.equals("111")) {
				dRequest.setTradeStatus("S");// 成功
			} else if (tradeStatus.equals("112")) {
				dRequest.setTradeStatus("F");// 失败
			}// TradeStatusCode[2]即不等于111也不等于112，就不设置状态，默认查询全部，包括成功，失败，处理中

			PageResultList<DepositBasicInfo> page2 = defaultDepositInfoService.queryList(
					dRequest, env);
			if(page2.getInfos()==null)
			{
				List<DepositBasicInfo> depositInfos=new ArrayList<DepositBasicInfo>();
				page2.setInfos(depositInfos);
			}else 
			{
				for (int i = 0; i < page2.getInfos().size(); i++) {
					Map<String, Object> data = JSONObject.parseObject(page2.getInfos().get(i).getExtension());//获取待审核交易数据
					QueryResult<BankInfoVO> bankInfoVO=bankInfoFacade.queryByBankCode((String)data.get("BANK_CODE"));
					if(bankInfoVO.isSuccess() &&(bankInfoVO.getResults()!=null))
					{
						page2.getInfos().get(i).setBank(bankInfoVO.getFirstResult().getShortName());
					}
					else {
						page2.getInfos().get(i).setBank((String)data.get("BANK_CODE"));
					}
				}
			}
			excelUtil.toExcel(request, response, page2.getInfos(),sum, "", Integer.valueOf(txnType),
					queryStartTime, queryEndTime);
			break;
		case 3:// 提现-有值0721
			FundoutQuery fundoutQuery = new FundoutQuery();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				fundoutQuery.setFundoutOrderNo(keyvalue);
			}
			fundoutQuery.setProductCode(TradeType.WITHDRAW.getBizProductCode()
					+ "," + TradeType.WITHDRAW_INSTANT.getBizProductCode());// 字符码--即使提现，T+n提现
			fundoutQuery.setMemberId(user.getMemberId());
			// fundoutQuery.setAccountNo(user.getDefaultAccountId());
			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
			fundoutQuery.setPageSize(50000);
			if (StringUtils.isNotBlank(queryStartTime)) {
				fundoutQuery.setOrderTimeStart(DateUtils
						.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				fundoutQuery
						.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(queryEndTime
						+ ":59"));
			} else {
				fundoutQuery.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 交易状态
			if (tradeStatus.equals("332")) {
				fundoutQuery.setStatus("success");
			} else if (tradeStatus.equals("333")) {
				fundoutQuery.setStatus("failed");// 失败
			} else if (tradeStatus.equals("331")) {
				fundoutQuery.setStatus("submitted");// 失败
			}
			PageResultList page3 = defaultFundoutService.queryFundoutInfo(
					fundoutQuery, env);
			if(page3.getInfos()==null)
			{
				List<Fundout> fundout = new ArrayList<Fundout>();
				page3.setInfos(fundout);
			}
			excelUtil.toExcel(request, response, page3.getInfos(),sum, "", Integer.valueOf(txnType),
					queryStartTime, queryEndTime);
			break;
		case 4:// 转账-分为转账到永达互联网金融和转账到银行卡，现有转账接口获取的都是转到永达互联网金融账户的数据，转账到银行卡的数据等于提现，需要调提现接口，通过设置字符码，获取数据
			PageResultList page4 = new PageResultList();
			if (tradeType.equals("2")) {
				FundoutQuery fundoutQuery4 = new FundoutQuery();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					//fundoutQuery4.setFundoutOrderNo(keyvalue);
					fundoutQuery4.setOutOrderNo(keyvalue);
				}// 通过交易订单号查
				else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
					//fundoutQuery4.setBatchOrderNo(keyvalue);
					fundoutQuery4.setSourceBatchNo(keyvalue);
				}
				fundoutQuery4.setProductCode(TradeType.PAY_TO_BANK
						.getBizProductCode()
						+ ","
						+ TradeType.PAY_TO_BANK_INSTANT.getBizProductCode()
						+ ","
						+ TradeType.auto_fundout.getBizProductCode());// 设置字符码
				fundoutQuery4.setPageSize(50000);
				fundoutQuery4.setMemberId(user.getMemberId());
				fundoutQuery4.setAccountNo(user.getDefaultAccountId());
				fundoutQuery4.setCurrentPage(Integer.valueOf(currentPage));
				if (StringUtils.isNotBlank(queryStartTime)) {
					fundoutQuery4.setOrderTimeStart(DateUtils
							.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					fundoutQuery4.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					fundoutQuery4.setOrderTimeEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					fundoutQuery4.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				if (tradeStatus.equals("222")) {
					fundoutQuery4.setStatus("success");
				} else if (tradeStatus.equals("223")) {
					fundoutQuery4.setStatus("failed");// 失败
				} else if (tradeStatus.equals("221")) {
					fundoutQuery4.setStatus("submitted");//
				}
				page4 = defaultFundoutService.queryFundoutInfo(fundoutQuery4,
						env);
				if(page4.getInfos()==null){
					List<Fundout> fundout = new ArrayList<Fundout>();
					page4.setInfos(fundout);
				}
				List<Fundout> fundoutlist=page4.getInfos();
				for (int i = 0; i < fundoutlist.size(); i++) {
					String tradeVoucherNo=fundoutlist.get(i).getFundoutOrderNo();
					List<Audit> auditList = new ArrayList<Audit>();
					Audit audit = new Audit();
					/* 查询审核列表 */
					AuditListQuery query = new AuditListQuery();
					query.setTransId(tradeVoucherNo);
					auditList = auditService.queryAuditList(query);
			        if((auditList!=null) && (auditList.size()>0)){
			            fundoutlist.get(i).setSuccessTime(auditList.get(0).getGmtModified());
			        }
				}
			} else {//
				DownloadBillRequest reqInfo = new DownloadBillRequest();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {// 交易订单号
					//reqInfo.setTradeVoucherNo(keyvalue);
					reqInfo.setTradeSourceVoucherNo(keyvalue);
				} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {// 批次号
					//reqInfo.setBatchNo(keyvalue);
					reqInfo.setSourceBatchNo(keyvalue);
				}
				List<String> TraStatus = new ArrayList<String>();
				if (tradeStatus.equals("222")) {
					TraStatus.add("301");// 转账成功
					TraStatus.add("401");// 交易完成
				} else if (tradeStatus.equals("223")) {
					TraStatus.add("999");// 交易关闭
					TraStatus.add("998");// 转账失败
				} else if (tradeStatus.equals("221")) {
					TraStatus.add("201");//付款成功=处理中
					TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
				}
				reqInfo.setTradeStatus(TraStatus);
				if (StringUtils.isNotBlank(queryStartTime)) {
					reqInfo.setBeginTime(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					reqInfo.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					reqInfo.setEndTime(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					reqInfo.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				List<String> productCode=new ArrayList<String>();
				productCode.add(TradeType.TRANSFER.getBizProductCode());
				reqInfo.setProductCodes(productCode);
				reqInfo.setMemberId(user.getMemberId());
				QueryBase queryBase4 = new QueryBase();
				queryBase4.setCurrentPage(Integer.parseInt(currentPage));
				queryBase4.setPageSize(50000);
				reqInfo.setQueryBase(queryBase4);
				page4 = defaultDownloadBillService.queryTransfer(reqInfo, env);
				if(page4.getInfos()==null)
				{
					List<TransferInfo> transferInfo = new ArrayList<TransferInfo>();
					page4.setInfos(transferInfo);
				}
				List<TransferInfo> transferInfolist=page4.getInfos();
				BaseMember buyMember=new BaseMember();
				BaseMember sellMember=new BaseMember();
				for (int i = 0; i < transferInfolist.size(); i++) {
					buyMember=memberService.queryMemberById(transferInfolist.get(i).getBuyId(),env);
					sellMember=memberService.queryMemberById(transferInfolist.get(i).getSellId(),env);
					transferInfolist.get(i).setBuyId(buyMember.getLoginName());
					transferInfolist.get(i).setSellId(sellMember.getLoginName());
				}
			}
			
			excelUtil.toExcel(request, response, page4.getInfos(),sum, tradeType,
					Integer.valueOf(txnType), queryStartTime, queryEndTime);
			break;
		case 5:// 退款
			if (user.getMemberId() == null) {
				throw new IllegalAccessException("illegal user error!");
			}
			TradeRequest tradeRequest = new TradeRequest();
			if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
                tradeRequest.setTradeSourceVoucherNo(keyvalue);//商户订单号
            } else if (keyword.equals("5") && !StringUtils.isEmpty(keyvalue)) {
                tradeRequest.setOrigTradeSourceVoucherNo(keyvalue);//原商户订单号
            } else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
                tradeRequest.setSourceBatchNo(keyvalue);//商户批次号
            }
			tradeRequest.setMemberId(user.getMemberId());
			tradeRequest.setSellerId(user.getMemberId());
			if (StringUtils.isNotBlank(queryStartTime)) {
				tradeRequest.setGmtCreateStart(DateUtils.parseDate(queryStartTime
						+ ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				tradeRequest.setGmtCreateStart(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				tradeRequest.setGmtCreateEnd(DateUtils
						.parseDate(queryEndTime + ":59"));
			} else {
				tradeRequest.setGmtCreateEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 分页信息
			QueryBase queryBase5 = new QueryBase();
			queryBase5.setPageSize(50000);
			queryBase5.setCurrentPage(Integer.valueOf(currentPage));
			tradeRequest.setQueryBase(queryBase5);

			// 交易状态
			List<String> refundstatus = new ArrayList<String>();
			if (tradeStatus.equals("950")) {
				refundstatus.add("900");// 退款创建成功
				refundstatus.add("901");// 退结算成功
			} else if (tradeStatus.equals("951")) {// 退款成功
				refundstatus.add("951");// 成功
			} else if (tradeStatus.equals("952")) {// 退款失败
				refundstatus.add("952");// 失败
			}
			List<String> productCodes5=new ArrayList<String>();
			productCodes5.add(TradeType.POS.getBizProductCode());
			tradeRequest.setIgnoreProductCodes(productCodes5);
			
			tradeRequest.setTradeStatus(refundstatus);
			PageResultList page5 = defaultTradeQueryService.queryRefundList(
					tradeRequest, env);
			if(page5.getInfos()==null)
			{
				List<TradeInfo>  tradeInfo=new ArrayList<TradeInfo>();
				page5.setInfos(tradeInfo);
			}
			excelUtil.toExcel(request, response, page5.getInfos(),sum,
					user.getMemberId(), Integer.valueOf(txnType), queryStartTime, queryEndTime);
			break;
		case 6:// 代发工资，调用提现接口，通过不同的字符码区别
			PageResultList page6 = new PageResultList();
			if (tradeType.equals("03")) {
				FundoutQuery fundoutQuery4 = new FundoutQuery();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					fundoutQuery4.setFundoutOrderNo(keyvalue);
				}// 通过交易订单号查
				else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
					fundoutQuery4.setBatchOrderNo(keyvalue);
				}
				fundoutQuery4.setProductCode(TradeType.PAYOFF_TO_BANK.getBizProductCode());// 设置字符码
				fundoutQuery4.setPageSize(50000);
				fundoutQuery4.setMemberId(user.getMemberId());
				fundoutQuery4.setAccountNo(user.getDefaultAccountId());
				fundoutQuery4.setCurrentPage(Integer.valueOf(currentPage));
				if (StringUtils.isNotBlank(queryStartTime)) {
					fundoutQuery4.setOrderTimeStart(DateUtils
							.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					fundoutQuery4.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					fundoutQuery4.setOrderTimeEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					fundoutQuery4.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				// 交易状态
				// 提现-交易状态
				if (tradeStatus.equals("662")) {
					fundoutQuery4.setStatus("success");
				} else if (tradeStatus.equals("663")) {
					fundoutQuery4.setStatus("failed");// 失败
				} else if (tradeStatus.equals("661")) {
					fundoutQuery4.setStatus("submitted");//
				}
				page6 = defaultFundoutService.queryFundoutInfo(fundoutQuery4,
						env);
				if(page6.getInfos()==null)
				{
					List<Fundout> fundout = new ArrayList<Fundout>();
					page6.setInfos(fundout);
				}
				List<Fundout> fundoutlist=page6.getInfos();
				for (int i = 0; i < fundoutlist.size(); i++) {
					String tradeVoucherNo=fundoutlist.get(i).getFundoutOrderNo();
					List<Audit> auditList = new ArrayList<Audit>();
					Audit audit = new Audit();
					/* 查询审核列表 */
					AuditListQuery query = new AuditListQuery();
					query.setTransId(tradeVoucherNo);
					auditList = auditService.queryAuditList(query);
			        if((auditList!=null) && (auditList.size()>0)){
			            fundoutlist.get(i).setSuccessTime(auditList.get(0).getGmtModified());
			        }
				}
			} else {//
				DownloadBillRequest reqInfo = new DownloadBillRequest();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					reqInfo.setTradeVoucherNo(keyvalue);
				} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
					reqInfo.setBatchNo(keyvalue);
				}
				List<String> TraStatus = new ArrayList<String>();
				if (tradeStatus.equals("662")) {
					TraStatus.add("301");// 转账成功
					TraStatus.add("401");// 交易完成
				} else if (tradeStatus.equals("663")) {
					TraStatus.add("999");// 交易关闭
				} else if (tradeStatus.equals("661")) {
					TraStatus.add("201");//付款成功=处理中
					TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
				}// TradeStatusCode[2]即不等于111也不等于112，就不设置状态，默认查询全部，包括成功，失败，处理中
				reqInfo.setTradeStatus(TraStatus);
				if (StringUtils.isNotBlank(queryStartTime)) {
					reqInfo.setBeginTime(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					reqInfo.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					reqInfo.setEndTime(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					reqInfo.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				List<String> productCode=new ArrayList<String>();
				productCode.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
				reqInfo.setProductCodes(productCode);
			
				reqInfo.setMemberId(user.getMemberId());
				QueryBase queryBase4 = new QueryBase();
				queryBase4.setCurrentPage(Integer.parseInt(currentPage));
				queryBase4.setPageSize(50000);
				reqInfo.setQueryBase(queryBase4);
				page6 = defaultDownloadBillService.queryTransfer(reqInfo, env);
				if(page6.getInfos()==null)
				{
					List<TransferInfo> transferInfo = new ArrayList<TransferInfo>();
					page6.setInfos(transferInfo);
				}
				List<TransferInfo> transferInfolist=page6.getInfos();
				BaseMember buyMember=new BaseMember();
				BaseMember sellMember=new BaseMember();
				for (int i = 0; i < transferInfolist.size(); i++) {
					buyMember=memberService.queryMemberById(transferInfolist.get(i).getBuyId(),env);
					sellMember=memberService.queryMemberById(transferInfolist.get(i).getSellId(),env);
					transferInfolist.get(i).setBuyId(buyMember.getLoginName());
					transferInfolist.get(i).setSellId(sellMember.getLoginName());
				}
			}
			excelUtil.toExcel(request, response, page6.getInfos(),sum, tradeType, Integer.valueOf(txnType),
					queryStartTime, queryEndTime);
			break;
		case 7:// 委托付款-
			PageResultList page7 = new PageResultList();
			if (tradeType.equals("13")) {//交易类型-//委托付款到卡
				FundoutQuery fundoutQuery7 = new FundoutQuery();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					fundoutQuery7.setFundoutOrderNo(keyvalue);
				}// 通过交易订单号查
				fundoutQuery7.setProductCode(TradeType.COLLECT_TO_BANK
						.getBizProductCode());// 设置字符码-归集到银行
				fundoutQuery7.setMemberId(user.getMemberId());
				fundoutQuery7.setAccountNo(user.getDefaultAccountId());
				fundoutQuery7.setCurrentPage(Integer.valueOf(currentPage));
				if (StringUtils.isNotBlank(queryStartTime)) {
					fundoutQuery7.setOrderTimeStart(DateUtils
							.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					fundoutQuery7.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					fundoutQuery7.setOrderTimeEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					fundoutQuery7.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				// 交易状态
				if (tradeStatus.equals("222")) {// 交易状态
					fundoutQuery7.setStatus("success");//成功
				} else if (tradeStatus.equals("223")) {
					fundoutQuery7.setStatus("failed");// 失败
				} else if (tradeStatus.equals("221")) {
					fundoutQuery7.setStatus("submitted");//处理中
				}
				page7 = defaultFundoutService.queryFundoutInfo(fundoutQuery7,
						env);
				if(page7.getInfos()==null)
				{
					List<Fundout> fundout = new ArrayList<Fundout>();
					page7.setInfos(fundout);
				}
				List<Fundout> fundoutlist=page7.getInfos();
				for (int i = 0; i < fundoutlist.size(); i++) {
					String tradeVoucherNo=fundoutlist.get(i).getFundoutOrderNo();
					List<Audit> auditList = new ArrayList<Audit>();
					Audit audit = new Audit();
					/* 查询审核列表 */
					AuditListQuery query = new AuditListQuery();
					query.setTransId(tradeVoucherNo);
					auditList = auditService.queryAuditList(query);
			        if((auditList!=null) && (auditList.size()>0)){
			            fundoutlist.get(i).setSuccessTime(auditList.get(0).getGmtModified());
			        }
				}
			} else {//交易类型-//归集到账户
				DownloadBillRequest reqInfo7 = new DownloadBillRequest();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {// 通过交易订单号查
					reqInfo7.setTradeVoucherNo(keyvalue);
				}
				List<String> TraStatus = new ArrayList<String>();
				if (tradeStatus.equals("222")) {//交易状态
					TraStatus.add("301");// 转账成功
					TraStatus.add("401");// 交易完成
				} else if (tradeStatus.equals("223")) {
					TraStatus.add("999");// 交易关闭
					TraStatus.add("998");// 转账失败
				} 
//				else if (TradeStatusCode[3].equals("221")) {
//					TraStatus.add("201");//付款成功=处理中
//					TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
//				}// TradeStatusCode[2]即不等于111也不等于112，就不设置状态，默认查询全部，包括成功，失败，处理中
				reqInfo7.setTradeStatus(TraStatus);
				if (StringUtils.isNotBlank(queryStartTime)) {
					reqInfo7.setBeginTime(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					reqInfo7.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					reqInfo7.setEndTime(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					reqInfo7.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				List<String> productCode=new ArrayList<String>();
				productCode.add(TradeType.COLLECT_TO_KJT.getBizProductCode());
				reqInfo7.setProductCodes(productCode);
				reqInfo7.setMemberId(user.getMemberId());
				QueryBase queryBase7 = new QueryBase();
				queryBase7.setCurrentPage(Integer.parseInt(currentPage));
				reqInfo7.setQueryBase(queryBase7);
				page7 = defaultDownloadBillService.queryTransfer(reqInfo7, env);
				if(page7.getInfos()==null)
				{
					List<TransferInfo> transferInfo = new ArrayList<TransferInfo>();
					page7.setInfos(transferInfo);
				}
				List<TransferInfo> transferInfolist=page7.getInfos();
				BaseMember buyMember=new BaseMember();
				BaseMember sellMember=new BaseMember();
				for (int i = 0; i < transferInfolist.size(); i++) {
					buyMember=memberService.queryMemberById(transferInfolist.get(i).getBuyId(),env);
					sellMember=memberService.queryMemberById(transferInfolist.get(i).getSellId(),env);
					transferInfolist.get(i).setBuyId(buyMember.getLoginName());
					transferInfolist.get(i).setSellId(sellMember.getLoginName());
				}
			}
			excelUtil.toExcel(request, response, page7.getInfos(),sum, tradeType, Integer.valueOf(txnType),
					queryStartTime, queryEndTime);
			break;
		case 8:// 退票记录
			PageResultList page8 = new PageResultList();
			FundoutQuery fundoutQuery8 = new FundoutQuery();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				fundoutQuery8.setFundoutOrderNo(keyvalue);
			}// 通过交易订单号查
			fundoutQuery8.setMemberId(user.getMemberId());
			fundoutQuery8.setStatus(FundoutStatus.SUCCESS.getCode());
			// fundoutQuery8.setHasOutOrderNo(true);
			fundoutQuery8.setCurrentPage(Integer.valueOf(currentPage));
			if (StringUtils.isNotBlank(queryStartTime)) {
				fundoutQuery8.setRefundTimeStart(DateUtils.parseDate(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				fundoutQuery8.setRefundTimeEnd(DateUtils.parseDate(queryEndTime + ":00"));
			}

			page8 = defaultFundoutService.queryFundoutOrderInfo(fundoutQuery8, env);
			
			if(page8.getInfos()==null){
				List<Fundout> fundout = new ArrayList<Fundout>();
				page8.setInfos(fundout);
			}
			List<Fundout> fundoutlist=page8.getInfos();
			for (int i = 0; i < fundoutlist.size(); i++) {
				String tradeVoucherNo=fundoutlist.get(i).getFundoutOrderNo();
				List<Audit> auditList = new ArrayList<Audit>();
				Audit audit = new Audit();
				/* 查询审核列表 */
				AuditListQuery query = new AuditListQuery();
				query.setTransId(tradeVoucherNo);
				auditList = auditService.queryAuditList(query);
		        if((auditList!=null) && (auditList.size()>0)){
		            fundoutlist.get(i).setSuccessTime(auditList.get(0).getGmtModified());
		        }
			}
			
			excelUtil.toExcel(request, response, page8.getInfos(),sum, tradeType, Integer.valueOf(txnType),
					queryStartTime, queryEndTime);
			break;
		case 9://保理放贷
		case 10:// 保理代扣
			PageResultList page9 = new PageResultList();
			DownloadBillRequest reqInfo = new DownloadBillRequest();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {// 交易订单号
				reqInfo.setTradeVoucherNo(keyvalue);
			} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {// 批次号
				reqInfo.setBatchNo(keyvalue);
			}
			List<String> TraStatus = new ArrayList<String>();
			if (tradeStatus.equals("101")) {
				TraStatus.add("301");// 转账成功
				TraStatus.add("401");// 交易完成
			} else if (tradeStatus.equals("102")) {
				TraStatus.add("999");// 交易关闭
				TraStatus.add("998");// 转账失败
			} 
			reqInfo.setTradeStatus(TraStatus);
			if (StringUtils.isNotBlank(queryStartTime)) {
				reqInfo.setBeginTime(DateUtils.parseDate(queryStartTime
						+ ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				reqInfo.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				reqInfo.setEndTime(DateUtils
						.parseDate(queryEndTime + ":59"));
			} else {
				reqInfo.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			List<String> productCode=new ArrayList<String>();
			if(txnType.equals("9"))
			{
				productCode.add(TradeType.baoli_loan.getBizProductCode());
			}else if(txnType.equals("10")){
				productCode.add(TradeType.baoli_withholding.getBizProductCode());
			}			reqInfo.setProductCodes(productCode);
			reqInfo.setMemberId(user.getMemberId());
			QueryBase queryBase9 = new QueryBase();
			queryBase9.setCurrentPage(Integer.parseInt(currentPage));
			queryBase9.setPageSize(50000);
			reqInfo.setQueryBase(queryBase9);
			page9 = defaultDownloadBillService.queryTransfer(reqInfo, env);
			if(page9.getInfos()==null)
			{
				List<TransferInfo> transferInfo = new ArrayList<TransferInfo>();
				page9.setInfos(transferInfo);
			}
			List<TransferInfo> transferInfolist=page9.getInfos();
			BaseMember buyMember=new BaseMember();
			BaseMember sellMember=new BaseMember();
			for (int i = 0; i < transferInfolist.size(); i++) {
				buyMember=memberService.queryMemberById(transferInfolist.get(i).getBuyId(),env);
				sellMember=memberService.queryMemberById(transferInfolist.get(i).getSellId(),env);
				transferInfolist.get(i).setBuyId(buyMember.getLoginName());
				transferInfolist.get(i).setSellId(sellMember.getLoginName());
			}
			
			excelUtil.toExcel(request, response, page9.getInfos(),sum, tradeType,
					Integer.valueOf(txnType), queryStartTime, queryEndTime);
			break;
		case 12:
			if(tradeType.equals("12")){//pos消费撤销
				if (user.getMemberId() == null) {
					throw new IllegalAccessException("illegal user error!");
				}
				TradeRequest postradeRequest = new TradeRequest();
				if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
	                postradeRequest.setTradeSourceVoucherNo(keyvalue);//商户订单号
	            } else if (keyword.equals("5") && !StringUtils.isEmpty(keyvalue)) {
	                postradeRequest.setOrigTradeSourceVoucherNo(keyvalue);//原商户订单号
	            } else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
	                postradeRequest.setSourceBatchNo(keyvalue);//商户批次号
	            }
				postradeRequest.setMemberId(user.getMemberId());
				postradeRequest.setSellerId(user.getMemberId());
				if (StringUtils.isNotBlank(queryStartTime)) {
					postradeRequest.setGmtCreateStart(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					postradeRequest.setGmtCreateStart(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					postradeRequest.setGmtCreateEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					postradeRequest.setGmtCreateEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				// 分页信息
				QueryBase queryBase12 = new QueryBase();
				queryBase12.setPageSize(10000);
				queryBase12.setCurrentPage(Integer.valueOf(currentPage));
				postradeRequest.setQueryBase(queryBase12);

				// 交易状态
				List<String> refundstatus12 = new ArrayList<String>();
				if (tradeStatus.equals("950")) {
					refundstatus12.add("900");// 退货创建成功
					refundstatus12.add("901");// 退结算成功
				} else if (tradeStatus.equals("951")) {// 退货成功
					refundstatus12.add("951");// 成功
				} else if (tradeStatus.equals("952")) {// 退货失败
					refundstatus12.add("952");// 失败
				}else{
					refundstatus12.add("900");// 退货创建成功
					refundstatus12.add("901");// 退结算成功
					refundstatus12.add("951");// 成功
					refundstatus12.add("952");// 失败
				}
				List<String> productCodes1=new ArrayList<String>();
				productCodes1.add(TradeType.POS.getBizProductCode());
				postradeRequest.setProductCodes(productCodes1);
				
				postradeRequest.setTradeStatus(refundstatus12);
				PageResultList page12 = defaultTradeQueryService.queryRefundList(
						postradeRequest, env);
				if(page12.getInfos()==null)
				{
					List<TradeInfo>  tradeInfo=new ArrayList<TradeInfo>();
					page12.setInfos(tradeInfo);
				}
				excelUtil.toExcel(request, response, page12.getInfos(),sum,
						tradeType, Integer.valueOf(txnType), queryStartTime, queryEndTime);
				
			}else{//pos消费
				QueryBase queryBasepos = new QueryBase();// 分页信息
				queryBasepos.setNeedQueryAll(true);
				queryBasepos.setPageSize(10000);
				queryBasepos.setCurrentPage(Integer.valueOf(currentPage));// currentPage

				CoTradeRequest pos = new CoTradeRequest();// zzq查询条件封装	
//				// 交易类型 -条件封装
				List<TradeTypeRequest> postradeTypes = new ArrayList<TradeTypeRequest>();
				List<String> posproductCodes=new ArrayList<String>();
				 //POS消费产品编码
				posproductCodes.add(TradeType.POS.getBizProductCode());
				postradeTypes.add(TradeTypeRequest.getByCode("12"));
				
				pos.setProductCodes(posproductCodes);
				pos.setTradeType(postradeTypes);
				// 交易状态 -条件封装
				List<String> posstatus = new ArrayList<String>();
				if (tradeStatus.equals("100")) {
					posstatus.add("100");// 待支付
				} else if (tradeStatus.equals("301")) {
					posstatus.add("301");// 交易成功
				} else if (tradeStatus.equals("401")) {
					posstatus.add("401");// 交易结束
				} else if (tradeStatus.equals("998")) {
					posstatus.add("998");// 交易失败
					posstatus.add("999");// 交易关闭
				} else if (tradeStatus.equals("201")) {
					posstatus.add("201");// 支付成功
				} else {
					posstatus.add("100");// 代付款
					posstatus.add("301");// 交易成功
					posstatus.add("401");// 交易结束
					posstatus.add("998");// 交易失败
					posstatus.add("999");// 交易关闭
					posstatus.add("201");// 支付成功
				}
				pos.setStatus(posstatus);
				// 判断关键字具体赋给具体的查询条件项
				if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
					pos.setTradeSourceVoucherNo(keyvalue);
				}
				pos.setQueryBase(queryBasepos);
				pos.setNeedSummary(true);// 需要汇总
				pos.setMemberId(user.getMemberId());
				if (StringUtils.isNotBlank(queryStartTime)) {
					pos.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				    pos.setBeginTime(sdfhms.parse(queryStartTime + ":00"));

				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					pos.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));

				} else {
					pos.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}

				CoTradeQueryResponse posRep = tradeService.queryList(pos);// totalTradeCount
				List<BaseInfo> posbaseInfoList= posRep.getBaseInfoList();
				if(posbaseInfoList!=null){
					for(BaseInfo info:posbaseInfoList){
						String payChannel = tradeService.queryPayMethods(info.getTradeVoucherNo(), env);
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
				if(posRep.getBaseInfoList()==null)
				{
					List<BaseInfo> baseInfo=new ArrayList<BaseInfo>();
					posRep.setBaseInfoList(baseInfo);
				}
				excelUtil.toExcel(request, response, posRep.getBaseInfoList(),sum,"",  Integer.valueOf(txnType),
						queryStartTime, queryEndTime);
				}
			break;
		case 13://云+pos交易
			if (user.getMemberId() == null) {
				throw new IllegalAccessException("illegal user error!");
			}
			PosTradeRequest pos = new PosTradeRequest();
			QueryBase queryB = new QueryBase();// 分页信息
			queryB.setNeedQueryAll(true);
			queryB.setPageSize(10000);
			queryB.setCurrentPage(Integer.valueOf(currentPage));
			pos.setQueryBase(queryB);
			if (StringUtils.isNotBlank(queryStartTime)) {
				pos.setStartTime(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				pos.setStartTime(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				pos.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));

			} else {
				pos.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 判断关键字具体赋给具体的查询条件项
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				pos.setTradeNo(keyvalue);
			}else if(keyword.equals("5") && !StringUtils.isEmpty(keyvalue)) {
				pos.setTradeSrcNo(keyvalue);
			}
			//交易类型
			String posType = tradeType;
			if(posType.equals("0")){
				pos.setTradeType(null);
			}else{
				pos.setTradeType(posType);
			}
			
			// 交易状态
			String posStatus = new String();
			if (tradeStatus.equals("1")) {
				posStatus = "交易成功";
			} else if (tradeStatus.equals("2")) {
				posStatus = "处理失败";
			} 
			if(tradeStatus.equals("0")){
				pos.setStatus(null);
			}else{
				pos.setStatus(posStatus);
			}
			
//			pos.setMemberId("990290055116257");
			pos.setMemberId(user.getMemberId());
			
			PosTradeResponse pospage = posTradeService.selectPosList(pos);
			sum.setTotalFeeAmount(pospage.getSummaryInfo().getTotalFeeAmount());
			sum.setTotalRefundAmount(pospage.getSummaryInfo().getTotalRefundAmount());
			sum.setTotalTradeAmount(pospage.getSummaryInfo().getTotalTradeAmount());
			sum.setTotalSettledAmount(pospage.getSummaryInfo().getTotalSettledAmount());
			if(pospage.getPosList()==null)
			{
				List<PosTradeDO> baseInfo=new ArrayList<PosTradeDO>();
				pospage.setPosList(baseInfo);
			}
			excelUtil.toExcel(request, response, pospage.getPosList(),sum,"",  Integer.valueOf(txnType),
					queryStartTime, queryEndTime);
			break;
		default:
			break;
		}
	}
   
	/**
	 * 交易查询
	 * */
	@RequestMapping(value = "/my/TransactionRecords.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchTradeRecords(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env, QueryTradeForm form) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String txnType = form.getTxnType();// 查询类型--页面获取
		if (txnType == null) {
			txnType = "1";// 默认值为1,<网关交易>
		}
		
		String tradeType = form.getTradeType();// 交易类型
		String[] TradeTypeCode = new String[5];
		if (tradeType == null) {
			TradeTypeCode[0] = "0";// 网关
			TradeTypeCode[1] = "1";
			TradeTypeCode[2] = "1";
			TradeTypeCode[3] = "1";
			TradeTypeCode[4] = "1";
		} else {
			TradeTypeCode = tradeType.split(",");
		}
		String tradeStatus = form.getTradeStatus();// 交易状态--页面获取；页面有七个hidden的name为tradeStatus；当都为空时，返回值为(,,,,,,)
		String[] TradeStatusCode = new String[10];
		if (tradeStatus == null) {
			TradeStatusCode[0] = "0";
			TradeStatusCode[1] = "0";
			TradeStatusCode[2] = "0";
			TradeStatusCode[3] = "0";
			TradeStatusCode[4] = "0";
			TradeStatusCode[5] = "0";
			TradeStatusCode[6] = "0";
			TradeStatusCode[7] = "0";
			TradeStatusCode[8] = "0";
			TradeStatusCode[9] = "0";
		} else {
			TradeStatusCode = tradeStatus.split(",");
		}

		String currentPage = form.getCurrentPage();// 4 String currentPage =
													// request.getParameter("currentPage");
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3

		String keyword = form.getKeyword();
		if ((keyword == null) || keyword.equals("")) {
			keyword = "-1";
		}
		String keyvalue = form.getKeyvalue();
		
		String queryStartTime = form.getQueryStartTime();// 查询时间//5、String-startDate=request.getParameter("startDate");
		String queryEndTime = form.getQueryEndTime();// 查询时间
		
//		if(txnType1==null){
//			txnType1="1";
//		}
//		if(!txnType1.equals(txnType)){
//			queryStartTime=null;
//			queryEndTime=null;
//		}
//		txnType1=txnType;
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		restP.getData().put("txnType", txnType);
		//获取云+pos交易类型
		PosTradeRequest posT = new PosTradeRequest();
		posT.setMemberId(user.getMemberId());
//		posT.setMemberId("990290055116257");
		PosTradeResponse pResp = posTradeService.selectPosList(posT);
		restP.getData().put("posType", pResp.getTradeType());
		switch (Integer.valueOf(txnType)) {
		case 1:// 网关交易
		case 11:
			QueryBase queryBase = new QueryBase();// 分页信息
			queryBase.setNeedQueryAll(true);
			queryBase.setPageSize(20);
			queryBase.setCurrentPage(Integer.valueOf(currentPage));// currentPage
			CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
			String TradeReqStatus=null;
			if(txnType.equals("1")){
				TradeReqStatus=TradeStatusCode[0];
			}else if (txnType.equals("11")) {
				TradeReqStatus=TradeStatusCode[8];
			}
			// 交易类型 -条件封装
			List<TradeTypeRequest> tradeTypes = new ArrayList<TradeTypeRequest>();
			List<String> productCodes=new ArrayList<String>();
			if (txnType.equals("11")) {//银行卡代扣
				productCodes.add(TradeType.bank_withholding.getBizProductCode());
				tradeTypes.add(TradeTypeRequest.getByCode("11"));
				tradeTypes.add(TradeTypeRequest.getByCode("17"));
			}else if (TradeTypeCode[0].equals("11")) {//交易类型
				productCodes.add(TradeType.PAY_INSTANT.getBizProductCode());
				productCodes.add(TradeType.phone_recharge.getBizProductCode());
				tradeTypes.add(TradeTypeRequest.getByCode("11"));
			} else if (TradeTypeCode[0].equals("12")) {
				tradeTypes.add(TradeTypeRequest.getByCode("12"));
				productCodes.add(TradeType.PAY_ENSURE.getBizProductCode());
			}else {
				// 不需要的数据，要删掉
				productCodes.add(TradeType.PAY_INSTANT.getBizProductCode());
				productCodes.add(TradeType.phone_recharge.getBizProductCode());
//				productCodes.add(TradeType.POS.getBizProductCode());
				productCodes.add(TradeType.PAY_ENSURE.getBizProductCode());
				tradeTypes.add(TradeTypeRequest.getByCode("11"));
				tradeTypes.add(TradeTypeRequest.getByCode("12"));
			}
			breq.setProductCodes(productCodes);
			breq.setTradeType(tradeTypes);
//			 交易状态 -条件封装
			List<String> status = new ArrayList<String>();
			if (TradeReqStatus.equals("100")) {//待支付
				status.add("100");// 待支付
			} else if (TradeReqStatus.equals("301")) {//交易成功
				status.add("301");// 交易成功
			} else if (TradeReqStatus.equals("401")) {//交易结束
					status.add("401");// 交易结束
			}else if (TradeReqStatus.equals("998")) {//交易失败
				status.add("998");// 交易失败
				status.add("999");// 交易关闭
			} else if (TradeReqStatus.equals("201")) {//支付成功
				status.add("201");// 支付成功
			} else {
				status.add("100");// 代付款
				status.add("301");// 交易成功
				status.add("401");// 交易结束
				status.add("998");// 交易失败
				status.add("999");// 交易关闭
				status.add("201");// 支付成功
			}
			breq.setStatus(status);
			// 判断关键字具体赋给具体的查询条件项
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				breq.setTradeVoucherNo(keyvalue);
			} else if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
				breq.setTradeSourceVoucherNo(keyvalue);
			}else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
				breq.setSourceBatchNo(keyvalue);
			}
			breq.setQueryBase(queryBase);
			breq.setNeedSummary(true);// 需要汇总
			//breq.setMemberId(user.getMemberId());
			breq.setSellerId(user.getMemberId());
			if (StringUtils.isNotBlank(queryStartTime)) {
				breq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				breq.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				breq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));

			} else {
				breq.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("mobile", member.getMobileStar());
			restP.getData().put("member", member);
			
			restP.getData().put("tradeStatus", TradeReqStatus);
			restP.getData().put("tradeType", TradeTypeCode[0]);

//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			restP.getData().put("pageReqMapping", "/my/all-trade.htm");// 页面请求Mapping
			// 查询所有交易信息

			CoTradeQueryResponse rep = tradeService.queryList(breq);// totalTradeCount
			List<BaseInfo> baseInfoList= rep.getBaseInfoList();
			if(txnType.equals("1")){
				// 统计已退款金额
				TradeRequest tradeRequestSum = new TradeRequest();
				// 分页信息
				QueryBase queryBase5Sum = new QueryBase();
				queryBase5Sum.setPageSize(50000);//
				queryBase5Sum.setCurrentPage(1);
				tradeRequestSum.setQueryBase(queryBase5Sum);
				tradeRequestSum.setGmtStart(DateUtil.getDateNearCurrent(-366));
				tradeRequestSum.setGmtEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				if(baseInfoList!= null && baseInfoList.size()>0){
					for (int j = 0; j < baseInfoList.size(); j++) {
						// 网关交易订单号==退款记录的交易原始凭证号
						tradeRequestSum.setOrigTradeVoucherNo(baseInfoList.get(j).getTradeVoucherNo());
						// 交易对应的所有退款记录
						PageResultList refundPage = defaultTradeQueryService.queryRefundList(tradeRequestSum, env);// 通过所有数据获取已退款金额
						//不可退金额包括审核中金额，处理中金额和退款成功金额
						Money refundAmount=new Money();
						List<TradeInfo> refundTradeList = refundPage.getInfos();
						if((refundTradeList!=null) && (refundTradeList.size()>0))
						{
							for (int i = 0; i < refundTradeList.size(); i++) 
							{
								if(refundTradeList.get(i).getStatus().equals("951"))
								{
									refundAmount.addTo(refundTradeList.get(i).getPayAmount());
								}
							}
						}
						baseInfoList.get(j).setRefundAmount(refundAmount);
					}
				}
			}else if(txnType.equals("11")){
				if(baseInfoList!= null && baseInfoList.size()>0){
					for (int j = 0; j < baseInfoList.size(); j++) {
						Map<String, Object> data = JSONObject.parseObject(baseInfoList.get(j).getExtention());
						String cardNo= data.get("card_no").toString();
						String userName= data.get("user_name").toString();
						baseInfoList.get(j).setExtention(userName+" ******"+cardNo.substring(cardNo.length()-4, cardNo.length()));
					}
				}
			}
			restP.getData().put("pageSize",
					rep.getSummaryInfo().getTotalTradeCount());
			restP.getData().put("summary", rep.getSummaryInfo());
			restP.getData().put("list", rep.getBaseInfoList());
			restP.getData().put("page", rep.getQueryBase());
			restP.getData().put("totalItem", rep.getQueryBase().getTotalItem());
			break;
		case 2:// 充值
			DepositListRequest dRequest = new DepositListRequest();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				dRequest.setTradeVoucherNo(keyvalue);
			}
			dRequest.setMemberId(user.getMemberId());
			dRequest.setAccountNo(user.getDefaultAccountId());
			PageInfo pageInfo = new PageInfo();
			pageInfo.setCurrentPage(Integer.valueOf(currentPage));
			dRequest.setPageInfo(pageInfo);
			dRequest.setRequestId(System.currentTimeMillis()
					+ user.getMemberId());

			if (StringUtils.isNotBlank(queryStartTime)) {
				dRequest.setTimeBegin(DateUtils.parseDate(queryStartTime
						+ ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				dRequest.setTimeBegin(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				dRequest.setTimeEnd(DateUtils.parseDate(queryEndTime + ":59"));
			} else {
				dRequest.setTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 交易状态
			if (TradeStatusCode[1].equals("111")) {
				dRequest.setTradeStatus("S");// 成功
			} else if (TradeStatusCode[1].equals("112")) {
				dRequest.setTradeStatus("F");// 失败
			}// TradeStatusCode[2]即不等于111也不等于112，就不设置状态，默认查询全部，包括成功，失败，处理中

			PageResultList<DepositBasicInfo>  page2 = defaultDepositInfoService.queryList(
					dRequest, env);
			if(page2.getInfos()!=null)
			{
				for (int i = 0; i < page2.getInfos().size(); i++) {
					Map<String, Object> data = JSONObject.parseObject(page2.getInfos().get(i).getExtension());//获取待审核交易数据
					QueryResult<BankInfoVO> bankInfoVO=bankInfoFacade.queryByBankCode((String)data.get("BANK_CODE"));
					if(bankInfoVO.isSuccess() &&(bankInfoVO.getResults()!=null))
					{
						page2.getInfos().get(i).setBank(bankInfoVO.getFirstResult().getShortName());
					}
					else {
						page2.getInfos().get(i).setBank((String)data.get("BANK_CODE"));
					}
				}
			}
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page2);
			restP.getData().put("totalItem", page2.getPageInfo().getTotalItem());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("tradeStatus", TradeStatusCode[1]);
			restP.getData().put("tradeType", TradeTypeCode[0]);
			restP.getData().put("pageReqMapping", "/my/all-recharge.htm");
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		case 3:// 提现
			FundoutQuery fundoutQuery = new FundoutQuery();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				fundoutQuery.setFundoutOrderNo(keyvalue);
			}
			fundoutQuery.setMemberId(user.getMemberId());
			fundoutQuery.setProductCode(TradeType.WITHDRAW.getBizProductCode()
					+ "," + TradeType.WITHDRAW_INSTANT.getBizProductCode());// 字符码--即使提现，T+n提现
			// fundoutQuery.setAccountNo(user.getDefaultAccountId());
			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
			if (StringUtils.isNotBlank(queryStartTime)) {
				fundoutQuery.setOrderTimeStart(DateUtils
						.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				fundoutQuery.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(queryEndTime
						+ ":59"));
			} else {
				fundoutQuery.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 提现-交易状态
			if (TradeStatusCode[2].equals("332")) {
				fundoutQuery.setStatus("success");
			} else if (TradeStatusCode[2].equals("333")) {
				fundoutQuery.setStatus("failed");// 失败
			} else if (TradeStatusCode[2].equals("331")) {
				fundoutQuery.setStatus("submitted");//
			}
			PageResultList page3 = defaultFundoutService.queryFundoutInfo(
					fundoutQuery, env);
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page3);
			restP.getData().put("totalItem", page3.getPageInfo().getTotalItem());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("tradeStatus", TradeStatusCode[2]);
			restP.getData().put("tradeType", TradeTypeCode[0]);
			restP.getData().put("pageReqMapping", "/my/all-cach.htm");
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		case 4:// 转账-分为转账到永达互联网金融和转账到银行卡，现有转账接口获取的都是转到永达互联网金融账户的数据，转账到银行卡的数据等于提现，需要调提现接口，通过设置字符码，获取数据
			PageResultList page1 = new PageResultList();
			if (TradeTypeCode[1].equals("2")) {
				FundoutQuery fundoutQuery4 = new FundoutQuery();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					//fundoutQuery4.setFundoutOrderNo(keyvalue);
					fundoutQuery4.setOutOrderNo(keyvalue);
				}// 通过交易订单号查
				else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
					//fundoutQuery4.setBatchOrderNo(keyvalue);
					fundoutQuery4.setSourceBatchNo(keyvalue);
				}
				fundoutQuery4.setProductCode(TradeType.PAY_TO_BANK
						.getBizProductCode()
						+ ","
						+ TradeType.PAY_TO_BANK_INSTANT.getBizProductCode()
						+ ","
						+ TradeType.auto_fundout.getBizProductCode());// 设置字符码
				fundoutQuery4.setMemberId(user.getMemberId());
				fundoutQuery4.setAccountNo(user.getDefaultAccountId());
				fundoutQuery4.setCurrentPage(Integer.valueOf(currentPage));
				if (StringUtils.isNotBlank(queryStartTime)) {
					fundoutQuery4.setOrderTimeStart(DateUtils
							.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
	                fundoutQuery4.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));

				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					fundoutQuery4.setOrderTimeEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					fundoutQuery4.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				// 交易状态
				// 提现-交易状态
				if (TradeStatusCode[3].equals("222")) {
					fundoutQuery4.setStatus("success");
				} else if (TradeStatusCode[3].equals("223")) {
					fundoutQuery4.setStatus("failed");// 失败
				} else if (TradeStatusCode[3].equals("221")) {
					fundoutQuery4.setStatus("submitted");//
				}
				page1 = defaultFundoutService.queryFundoutInfo(fundoutQuery4,
						env);
			} else {//
				DownloadBillRequest reqInfo = new DownloadBillRequest();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {// 交易订单号
					//reqInfo.setTradeVoucherNo(keyvalue);  
					reqInfo.setTradeSourceVoucherNo(keyvalue);
				} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {// 批次号
					//reqInfo.setBatchNo(keyvalue);
					reqInfo.setSourceBatchNo(keyvalue);
				}
				List<String> TraStatus = new ArrayList<String>();
				if (TradeStatusCode[3].equals("222")) {
					TraStatus.add("301");// 转账成功
					TraStatus.add("401");// 交易完成
				} else if (TradeStatusCode[3].equals("223")) {
					TraStatus.add("999");// 交易关闭
					TraStatus.add("998");// 转账失败
				} 
//					else if (TradeStatusCode[3].equals("221")) {
//					TraStatus.add("201");//付款成功=处理中
//					TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
//				}// TradeStatusCode[2]即不等于111也不等于112，就不设置状态，默认查询全部，包括成功，失败，处理中
				reqInfo.setTradeStatus(TraStatus);
				if (StringUtils.isNotBlank(queryStartTime)) {
					reqInfo.setBeginTime(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					reqInfo.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					reqInfo.setEndTime(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					reqInfo.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				List<String> productCode=new ArrayList<String>();
				productCode.add(TradeType.TRANSFER.getBizProductCode());
				reqInfo.setProductCodes(productCode);
				reqInfo.setMemberId(user.getMemberId());
				QueryBase queryBase4 = new QueryBase();
				queryBase4.setCurrentPage(Integer.parseInt(currentPage));
				reqInfo.setQueryBase(queryBase4);
				page1 = defaultDownloadBillService.queryTransfer(reqInfo, env);
			}
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page1);
			restP.getData().put("totalItem", page1.getPageInfo().getTotalItem());
			restP.getData().put("summary", page1.getSummaryInfo());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("tradeStatus", TradeStatusCode[3]);
			restP.getData().put("tradeType", TradeTypeCode[1]);
			restP.getData().put("pageReqMapping", "/my/all-transfer.htm");
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		case 5:// 退款
			  if (user.getMemberId() == null) {
				throw new IllegalAccessException("illegal user error!");
			}
			TradeRequest tradeRequest = new TradeRequest();
			
			if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
				tradeRequest.setTradeSourceVoucherNo(keyvalue);//商户订单号
			} else if (keyword.equals("5") && !StringUtils.isEmpty(keyvalue)) {
				tradeRequest.setOrigTradeSourceVoucherNo(keyvalue);//原商户订单号
			} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
				tradeRequest.setSourceBatchNo(keyvalue);//商户批次号
			}
			tradeRequest.setMemberId(user.getMemberId());
			tradeRequest.setSellerId(user.getMemberId());
			if (StringUtils.isNotBlank(queryStartTime)) {
				tradeRequest.setGmtCreateStart(DateUtils.parseDate(queryStartTime
						+ ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				tradeRequest.setGmtCreateStart(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				tradeRequest.setGmtCreateEnd(DateUtils
						.parseDate(queryEndTime + ":59"));
			} else {
				tradeRequest.setGmtCreateEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 分页信息
			QueryBase queryBase5 = new QueryBase();
			queryBase5.setCurrentPage(Integer.valueOf(currentPage));
			tradeRequest.setQueryBase(queryBase5);
			// 交易状态
			List<String> refundstatus = new ArrayList<String>();
			if (TradeStatusCode[4].equals("950")) {
				refundstatus.add("900");// 退款创建成功
				refundstatus.add("901");// 退结算成功
			} else if (TradeStatusCode[4].equals("951")) {// 退款成功
				refundstatus.add("951");// 成功
			} else if (TradeStatusCode[4].equals("952")) {// 退款失败
				refundstatus.add("952");// 失败
			}
			
			List<String> productCodes5=new ArrayList<String>();
			productCodes5.add(TradeType.POS.getBizProductCode());
			tradeRequest.setIgnoreProductCodes(productCodes5);
			
			tradeRequest.setTradeStatus(refundstatus);
			PageResultList page = defaultTradeQueryService.queryRefundList(
					tradeRequest, env);
			
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page);
			restP.getData().put("totalItem", page.getPageInfo().getTotalItem());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", user);
			restP.getData().put("tradeStatus", TradeStatusCode[4]);
			restP.getData().put("tradeType", TradeTypeCode[0]);
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
					
		case 6:// 代发工资，调用提现接口，通过不同的字符码区别
			PageResultList page6 = new PageResultList();
			if (TradeTypeCode[2].equals("03")) {
				FundoutQuery fundoutQuery4 = new FundoutQuery();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					fundoutQuery4.setFundoutOrderNo(keyvalue);
				}// 通过交易订单号查
				else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
					fundoutQuery4.setBatchOrderNo(keyvalue);
				}
				fundoutQuery4.setProductCode(TradeType.PAYOFF_TO_BANK.getBizProductCode());// 设置字符码
				fundoutQuery4.setMemberId(user.getMemberId());
				fundoutQuery4.setAccountNo(user.getDefaultAccountId());
				fundoutQuery4.setCurrentPage(Integer.valueOf(currentPage));
				if (StringUtils.isNotBlank(queryStartTime)) {
					fundoutQuery4.setOrderTimeStart(DateUtils
							.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					fundoutQuery4.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					fundoutQuery4.setOrderTimeEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					fundoutQuery4.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				// 交易状态
				// 提现-交易状态
				if (TradeStatusCode[5].equals("662")) {
					fundoutQuery4.setStatus("success");
				} else if (TradeStatusCode[5].equals("663")) {
					fundoutQuery4.setStatus("failed");// 失败
				} else if (TradeStatusCode[5].equals("661")) {
					fundoutQuery4.setStatus("submitted");//
				}
				page6 = defaultFundoutService.queryFundoutInfo(fundoutQuery4,
						env);
			} else {//
				DownloadBillRequest reqInfo = new DownloadBillRequest();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					reqInfo.setTradeVoucherNo(keyvalue);
				} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
					reqInfo.setBatchNo(keyvalue);
				}
				List<String> TraStatus = new ArrayList<String>();
				if (TradeStatusCode[5].equals("662")) {
					TraStatus.add("301");// 转账成功
					TraStatus.add("401");// 交易完成
					reqInfo.setTradeStatus(TraStatus);
				} else if (TradeStatusCode[5].equals("663")) {
					TraStatus.add("999");// 交易关闭
					reqInfo.setTradeStatus(TraStatus);
				} else if (TradeStatusCode[5].equals("661")) {
					TraStatus.add("201");//付款成功=处理中
					TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
					reqInfo.setTradeStatus(TraStatus);
				}// TradeStatusCode[2]即不等于111也不等于112，就不设置状态，默认查询全部，包括成功，失败，处理中
				
				if (StringUtils.isNotBlank(queryStartTime)) {
					reqInfo.setBeginTime(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					reqInfo.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					reqInfo.setEndTime(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					reqInfo.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				List<String> productCode=new ArrayList<String>();
				productCode.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
				reqInfo.setProductCodes(productCode);
				reqInfo.setMemberId(user.getMemberId());
				QueryBase queryBase4 = new QueryBase();
				queryBase4.setCurrentPage(Integer.parseInt(currentPage));
				reqInfo.setQueryBase(queryBase4);
				page6 = defaultDownloadBillService.queryTransfer(reqInfo, env);
			}
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page6);
			restP.getData().put("totalItem", page6.getPageInfo().getTotalItem());
			restP.getData().put("summary", page6.getSummaryInfo());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("tradeStatus", TradeStatusCode[5]);
			restP.getData().put("tradeType", TradeTypeCode[2]);
			restP.getData().put("pageReqMapping", "/my/all-transfer.htm");
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		case 7:// 委托付款-
			PageResultList page7 = new PageResultList();
			if (TradeTypeCode[3].equals("13")) {//交易类型-//委托付款到卡
				FundoutQuery fundoutQuery7 = new FundoutQuery();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
					fundoutQuery7.setFundoutOrderNo(keyvalue);
				}// 通过交易订单号查
				fundoutQuery7.setProductCode(TradeType.COLLECT_TO_BANK
						.getBizProductCode());// 设置字符码-归集到银行
				fundoutQuery7.setMemberId(user.getMemberId());
				fundoutQuery7.setAccountNo(user.getDefaultAccountId());
				fundoutQuery7.setCurrentPage(Integer.valueOf(currentPage));
				if (StringUtils.isNotBlank(queryStartTime)) {
					fundoutQuery7.setOrderTimeStart(DateUtils
							.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					fundoutQuery7.setOrderTimeStart(sdfhms.parse(queryStartTime + ":00"));

				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					fundoutQuery7.setOrderTimeEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					fundoutQuery7.setOrderTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				// 交易状态
				if (TradeStatusCode[7].equals("222")) {// 交易状态
					fundoutQuery7.setStatus("success");//成功
				} else if (TradeStatusCode[7].equals("223")) {
					fundoutQuery7.setStatus("failed");// 失败
				} else if (TradeStatusCode[7].equals("221")) {
					fundoutQuery7.setStatus("submitted");//处理中
				}
				page7 = defaultFundoutService.queryFundoutInfo(fundoutQuery7,
						env);
			} else {//交易类型-//归集到账户
				DownloadBillRequest reqInfo7 = new DownloadBillRequest();
				if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {// 通过交易订单号查
					reqInfo7.setTradeVoucherNo(keyvalue);
				}
				List<String> TraStatus = new ArrayList<String>();
				if (TradeStatusCode[7].equals("222")) {//交易状态
					TraStatus.add("301");// 转账成功
					TraStatus.add("401");// 交易完成
				} else if (TradeStatusCode[7].equals("223")) {
					TraStatus.add("999");// 交易关闭
					TraStatus.add("998");// 转账失败
				} 
//				else if (TradeStatusCode[3].equals("221")) {
//					TraStatus.add("201");//付款成功=处理中
//					TraStatus.add("100");//待支付=审核中，维金有提供，但实际业务没这个状态，审核成功后是处理中
//				}// TradeStatusCode[2]即不等于111也不等于112，就不设置状态，默认查询全部，包括成功，失败，处理中
				reqInfo7.setTradeStatus(TraStatus);
				if (StringUtils.isNotBlank(queryStartTime)) {
					reqInfo7.setBeginTime(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					reqInfo7.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					reqInfo7.setEndTime(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					reqInfo7.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				List<String> productCode=new ArrayList<String>();
				productCode.add(TradeType.COLLECT_TO_KJT.getBizProductCode());
				reqInfo7.setProductCodes(productCode);
				reqInfo7.setMemberId(user.getMemberId());
				QueryBase queryBase7 = new QueryBase();
				queryBase7.setCurrentPage(Integer.parseInt(currentPage));
				reqInfo7.setQueryBase(queryBase7);
				page7 = defaultDownloadBillService.queryTransfer(reqInfo7, env);
			}
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page7);
			restP.getData().put("totalItem", page7.getPageInfo().getTotalItem());
			restP.getData().put("summary", page7.getSummaryInfo());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("tradeStatus", TradeStatusCode[7]);
			restP.getData().put("tradeType", TradeTypeCode[3]);
			restP.getData().put("pageReqMapping", "/my/all-transfer.htm");
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		case 8:// 退票记录
			PageResultList page8 = new PageResultList();
			FundoutQuery fundoutQuery8 = new FundoutQuery();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				fundoutQuery8.setFundoutOrderNo(keyvalue);
			}// 通过交易订单号查
			fundoutQuery8.setMemberId(user.getMemberId());
			fundoutQuery8.setStatus(FundoutStatus.SUCCESS.getCode());
			// fundoutQuery8.setHasOutOrderNo(true);
			fundoutQuery8.setCurrentPage(Integer.valueOf(currentPage));
			if (StringUtils.isNotBlank(queryStartTime)) {
				fundoutQuery8.setRefundTimeStart(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				fundoutQuery8.setRefundTimeStart(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				fundoutQuery8.setRefundTimeEnd(DateUtils.parseDate(queryEndTime + ":00"));
			} else {
				fundoutQuery8.setRefundTimeEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}

			page8 = defaultFundoutService.queryFundoutOrderInfo(fundoutQuery8, env);
			restP.getData().put("queryStartTime", queryStartTime);
			restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page8);
			restP.getData().put("totalItem", page8.getPageInfo().getTotalItem());
			restP.getData().put("summary", page8.getSummaryInfo());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("tradeStatus", TradeStatusCode[3]);
			restP.getData().put("tradeType", TradeTypeCode[1]);
			restP.getData().put("pageReqMapping", "/my/all-transfer.htm");
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		case 9:// 保理放贷
		case 10://保理代扣
			PageResultList page9 = new PageResultList();
			DownloadBillRequest reqInfo = new DownloadBillRequest();
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {// 交易订单号
				reqInfo.setTradeVoucherNo(keyvalue);
			} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {// 批次号
				reqInfo.setBatchNo(keyvalue);
			}
			List<String> TraStatus = new ArrayList<String>();
			String reqStatus=null;
			if(txnType.equals("9")){
				reqStatus=TradeStatusCode[6];
			}else if (txnType.equals("10")) {
				reqStatus=TradeStatusCode[7];
			}
			
			if (reqStatus.equals("101")) {
				TraStatus.add("301");// 转账成功
				TraStatus.add("401");// 交易完成
			} else if (reqStatus.equals("102")) {
				TraStatus.add("999");// 交易关闭
				TraStatus.add("998");// 转账失败
			} 
			reqInfo.setTradeStatus(TraStatus);
			if (StringUtils.isNotBlank(queryStartTime)) {
				reqInfo.setBeginTime(DateUtils.parseDate(queryStartTime
						+ ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				reqInfo.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				reqInfo.setEndTime(DateUtils
						.parseDate(queryEndTime + ":59"));
			} else {
				reqInfo.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			List<String> productCode=new ArrayList<String>();
			if(txnType.equals("9"))
			{
				productCode.add(TradeType.baoli_loan.getBizProductCode());
			}else if(txnType.equals("10")){
				productCode.add(TradeType.baoli_withholding.getBizProductCode());
			}
			reqInfo.setProductCodes(productCode);
			reqInfo.setMemberId(user.getMemberId());
			QueryBase queryBase4 = new QueryBase();
			queryBase4.setCurrentPage(Integer.parseInt(currentPage));
			reqInfo.setQueryBase(queryBase4);
			page9 = defaultDownloadBillService.queryTransfer(reqInfo, env);
				
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("page", page9);
			restP.getData().put("totalItem", page9.getPageInfo().getTotalItem());
			restP.getData().put("summary", page9.getSummaryInfo());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("tradeStatus", reqStatus);
			restP.getData().put("tradeType", TradeTypeCode[0]);
//			restP.getData().put("txnType", txnType);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		case 12:
			if(TradeTypeCode[2].equals("12")){//pos撤销
				if (user.getMemberId() == null) {
					throw new IllegalAccessException("illegal user error!");
				}
				TradeRequest postradeRequest = new TradeRequest();
				
				if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
					postradeRequest.setTradeSourceVoucherNo(keyvalue);//商户订单号
				} else if (keyword.equals("5") && !StringUtils.isEmpty(keyvalue)) {
					postradeRequest.setOrigTradeSourceVoucherNo(keyvalue);//原商户订单号
				} else if (keyword.equals("6") && !StringUtils.isEmpty(keyvalue)) {
					postradeRequest.setSourceBatchNo(keyvalue);//商户批次号
				}
				postradeRequest.setMemberId(user.getMemberId());
				postradeRequest.setSellerId(user.getMemberId());
				if (StringUtils.isNotBlank(queryStartTime)) {
					postradeRequest.setGmtCreateStart(DateUtils.parseDate(queryStartTime
							+ ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					postradeRequest.setGmtCreateStart(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
				
					postradeRequest.setGmtCreateEnd(DateUtils
							.parseDate(queryEndTime + ":59"));
				} else {
					postradeRequest.setGmtCreateEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				// 分页信息
				QueryBase queryBase12 = new QueryBase();
				queryBase12.setCurrentPage(Integer.valueOf(currentPage));
				postradeRequest.setQueryBase(queryBase12);
				// 交易状态
				List<String> posrefundstatus = new ArrayList<String>();
				if (TradeStatusCode[6].equals("950")) {
					posrefundstatus.add("900");// 撤销创建成功
					posrefundstatus.add("901");// 撤销结算成功
				} else if (TradeStatusCode[6].equals("951")) {// 撤销成功
					posrefundstatus.add("951");// 成功
				} else if (TradeStatusCode[6].equals("952")) {// 撤销失败
					posrefundstatus.add("952");// 失败
				} else{
					posrefundstatus.add("900");// 撤销创建成功
					posrefundstatus.add("901");// 撤销结算成功
					posrefundstatus.add("951");// 成功
					posrefundstatus.add("952");// 失败
				}
				List<String> productCodes1=new ArrayList<String>();
				productCodes1.add(TradeType.POS.getBizProductCode());
				postradeRequest.setProductCodes(productCodes1);
				
				postradeRequest.setTradeStatus(posrefundstatus);
				PageResultList pospage = defaultTradeQueryService.queryRefundList(
						postradeRequest, env);
				restP.getData().put("queryStartTime", queryStartTime);
		        restP.getData().put("queryEndTime", queryEndTime);
				restP.getData().put("page", pospage);
				restP.getData().put("totalItem", pospage.getPageInfo().getTotalItem());
				restP.getData().put("mobile", user.getMobileStar());
				restP.getData().put("member", user);
				restP.getData().put("tradeStatus", TradeStatusCode[6]);
				restP.getData().put("tradeType", TradeTypeCode[2]);
//				restP.getData().put("txnType", txnType);
				restP.getData().put("keyword", keyword);
				restP.getData().put("keyvalue", keyvalue);
			}else{//pos消费
				QueryBase queryBasepos = new QueryBase();// 分页信息
				queryBasepos.setNeedQueryAll(true);
				queryBasepos.setPageSize(20);
				queryBasepos.setCurrentPage(Integer.valueOf(currentPage));// currentPage
				CoTradeRequest pos = new CoTradeRequest();// zzq查询条件封装
				String posTradeReqStatus=TradeStatusCode[5];
				
				// 交易类型 -条件封装
				List<TradeTypeRequest> postradeTypes = new ArrayList<TradeTypeRequest>();
				List<String> posproductCodes=new ArrayList<String>();
			    //POS消费产品编码
				posproductCodes.add(TradeType.POS.getBizProductCode());
				postradeTypes.add(TradeTypeRequest.getByCode("12"));
				
				pos.setProductCodes(posproductCodes);
				pos.setTradeType(postradeTypes);
//				 交易状态 -条件封装
				List<String> posstatus = new ArrayList<String>();
				if (posTradeReqStatus.equals("100")) {//待支付
					posstatus.add("100");// 待支付
				} else if (posTradeReqStatus.equals("201")) {//支付成功
					posstatus.add("301");// 交易成功
					posstatus.add("201");// 支付成功
				} else if (posTradeReqStatus.equals("401")) {//交易结束
						posstatus.add("401");// 交易结束
				}else if (posTradeReqStatus.equals("998")) {//交易失败
					posstatus.add("998");// 交易失败
					posstatus.add("999");// 交易关闭
				} 
//				else if (posTradeReqStatus.equals("301")) {//交易成功
//					posstatus.add("301");// 成功
//				} 
				else {
					posstatus.add("100");// 代付款
					posstatus.add("301");// 交易成功
					posstatus.add("401");// 交易结束
					posstatus.add("998");// 交易失败
					posstatus.add("999");// 交易关闭
					posstatus.add("201");// 支付成功
				}
				pos.setStatus(posstatus);
				// 判断关键字具体赋给具体的查询条件项
				if (keyword.equals("4") && !StringUtils.isEmpty(keyvalue)) {
					pos.setTradeSourceVoucherNo(keyvalue);
				}
				pos.setQueryBase(queryBasepos);
				pos.setNeedSummary(true);// 需要汇总
				pos.setMemberId(user.getMemberId());
				if (StringUtils.isNotBlank(queryStartTime)) {
					pos.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
				} else {
				    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
					pos.setBeginTime(sdfhms.parse(queryStartTime + ":00"));
				}
				if (StringUtils.isNotBlank(queryEndTime)) {
					pos.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));

				} else {
					pos.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
					queryEndTime = sdf.format(new Date()) + " 23:59";
				}
				restP.getData().put("queryStartTime", queryStartTime);
		        restP.getData().put("queryEndTime", queryEndTime);
				restP.getData().put("mobile", member.getMobileStar());
				restP.getData().put("member", member);
				
				restP.getData().put("tradeStatus", posTradeReqStatus);
				restP.getData().put("tradeType", TradeTypeCode[2]);

//				restP.getData().put("txnType", txnType);
				restP.getData().put("keyword", keyword);
				restP.getData().put("keyvalue", keyvalue);
				restP.getData().put("pageReqMapping", "/my/all-trade.htm");// 页面请求Mapping
				// 查询所有交易信息

				CoTradeQueryResponse posRep = tradeService.queryList(pos);// totalTradeCount
				List<BaseInfo> posbaseInfoList= posRep.getBaseInfoList();
				if(posbaseInfoList!=null){
					for(BaseInfo info:posbaseInfoList){
						String payChannel = tradeService.queryPayMethods(info.getTradeVoucherNo(), env);
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
				// 统计已退款金额
				TradeRequest tradeRequestSum = new TradeRequest();
				// 分页信息
				QueryBase queryBase5Sum = new QueryBase();
				queryBase5Sum.setPageSize(50000);//
				queryBase5Sum.setCurrentPage(1);
				tradeRequestSum.setQueryBase(queryBase5Sum);
				tradeRequestSum.setGmtStart(DateUtil.getDateNearCurrent(-366));
				tradeRequestSum.setGmtEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				if(posbaseInfoList!= null && posbaseInfoList.size()>0){
					for (int j = 0; j < posbaseInfoList.size(); j++) {
						// 网关交易订单号==退款记录的交易原始凭证号
						tradeRequestSum.setOrigTradeVoucherNo(posbaseInfoList.get(j).getTradeVoucherNo());
						// 交易对应的所有退款记录
						PageResultList refundPage = defaultTradeQueryService.queryRefundList(tradeRequestSum, env);// 通过所有数据获取已退款金额
						//不可退金额包括审核中金额，处理中金额和退款成功金额
						Money refundAmount=new Money();
						List<TradeInfo> refundTradeList = refundPage.getInfos();
						if((refundTradeList!=null) && (refundTradeList.size()>0))
						{
							for (int i = 0; i < refundTradeList.size(); i++) 
							{
								if(refundTradeList.get(i).getStatus().equals("951"))
								{
									refundAmount.addTo(refundTradeList.get(i).getPayAmount());
								}
							}
						}
						posbaseInfoList.get(j).setRefundAmount(refundAmount);
					}
				}
				
				restP.getData().put("pageSize",
						posRep.getSummaryInfo().getTotalTradeCount());
				restP.getData().put("summary", posRep.getSummaryInfo());
				restP.getData().put("list", posRep.getBaseInfoList());
				restP.getData().put("page", posRep.getQueryBase());
				restP.getData().put("totalItem", posRep.getQueryBase().getTotalItem());
				
			}
			break;
		case 13://云+pos交易
			if (user.getMemberId() == null) {
				throw new IllegalAccessException("illegal user error!");
			}
			QueryBase queryB = new QueryBase();// 分页信息
			queryB.setNeedQueryAll(true);
			queryB.setPageSize(20);
			queryB.setCurrentPage(Integer.valueOf(currentPage));
			
			PosTradeRequest pos = new PosTradeRequest();
			if (StringUtils.isNotBlank(queryStartTime)) {
				pos.setStartTime(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				pos.setStartTime(sdfhms.parse(queryStartTime + ":00"));
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				pos.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));

			} else {
				pos.setEndTime(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				queryEndTime = sdf.format(new Date()) + " 23:59";
			}
			// 判断关键字具体赋给具体的查询条件项
			if (keyword.equals("1") && !StringUtils.isEmpty(keyvalue)) {
				pos.setTradeNo(keyvalue);
			}else if(keyword.equals("5") && !StringUtils.isEmpty(keyvalue)) {
				pos.setTradeSrcNo(keyvalue);
			}
			//交易类型
			String posType = TradeTypeCode[4];
			if(posType.equals("0")){
				pos.setTradeType(null);
			}else{
				pos.setTradeType(posType);
			}
			
			// 交易状态
			String posStatus = new String();
			if (TradeStatusCode[8].equals("1")) {
				posStatus = "交易成功";
			} else if (TradeStatusCode[8].equals("2")) {
				posStatus = "处理失败";
			} 
			if(TradeStatusCode[8].equals("0")){
				pos.setStatus(null);
			}else{
				pos.setStatus(posStatus);
			}
			
//			pos.setMemberId("990290055116257");
			pos.setMemberId(user.getMemberId());
			pos.setQueryBase(queryB);
			//查询交易记录
			PosTradeResponse pospage = posTradeService.selectPosList(pos);
			Money sum = new Money();//结算金额
			Money fee = new Money();//手续费
			Money money = new Money();
			sum = pospage.getSummaryInfo().getTotalSettledAmount();
			fee = pospage.getSummaryInfo().getTotalFeeAmount();
			//结算金额判断正负
			if(sum.compareTo(money)==-1){
				restP.getData().put("minusAmount", sum);//负数
			}else{
				restP.getData().put("plusAmount", sum);//正数
			}
			//手续费判断正负
			if(fee.compareTo(money)==-1){
				restP.getData().put("minusFee", fee);//负数
			}else{
				restP.getData().put("plusFee", fee);//正数
			}
			restP.getData().put("queryStartTime", queryStartTime);
			restP.getData().put("txnType", txnType);
	        restP.getData().put("queryEndTime", queryEndTime);
			restP.getData().put("pospage", pospage);
			restP.getData().put("pageSize",
					pospage.getSummaryInfo().getTotalTradeCount());
			restP.getData().put("summary", pospage.getSummaryInfo());
			
			restP.getData().put("totalItem", pospage.getQueryBase().getTotalItem());
			restP.getData().put("page", pospage.getQueryBase());
			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", user);
			restP.getData().put("tradeStatus", TradeStatusCode[8]);
			restP.getData().put("tradeType", TradeTypeCode[4]);
			restP.getData().put("keyword", keyword);
			restP.getData().put("keyvalue", keyvalue);
			break;
		default:
			break;
		}
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/TransactionRecords", "response", restP);///trade/TransactionRecords`tradeUnstyled/gatewayTable
	}
    
	/**
	 * (查看)云+POS交易--查看详情+退款记录
	 * */
	@RequestMapping(value = "/my/PosDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchPosDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String tradeVoucherNo = request.getParameter("tradeVoucherNo");
		String currentPage = request.getParameter("currentPage");
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		QueryBase queryBase = new QueryBase();// 分页信息
		queryBase.setNeedQueryAll(true);
		queryBase.setCurrentPage(Integer.valueOf(currentPage));// currentPage

		CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
		breq.setQueryBase(queryBase);
		breq.setNeedSummary(true);// 需要汇总
		// breq.setMemberId(user.getMemberId());
		breq.setTradeVoucherNo(tradeVoucherNo);
		breq.setBeginTime(DateUtil.getDateNearCurrent(-366));
		breq.setEndTime(new Date());

		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		if (user.getMemberId() == null) {
			throw new IllegalAccessException("illegal user error!");
		}
		TradeRequest tradeRequest = new TradeRequest();
		tradeRequest.setMemberId(user.getMemberId());
		tradeRequest.setSellerId(user.getMemberId());
		// 网关交易订单号==退款记录的交易原始凭证号
		tradeRequest.setOrigTradeVoucherNo(tradeVoucherNo);
		
		// 分页信息
		QueryBase queryBase5 = new QueryBase();
		queryBase5.setPageSize(5);// 每页五条
		queryBase5.setCurrentPage(Integer.valueOf(currentPage));
		tradeRequest.setQueryBase(queryBase5);
		tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));
		Date date=new Date();
		tradeRequest.setGmtEnd(sdfhms.parse(sdf.format(date) + " 23:59:59"));
		// 交易记录对应的所有退款记录（每页五条记录）
		PageResultList page = defaultTradeQueryService.queryRefundList(
				tradeRequest, env);
		
		// 统计已退款笔数和金额
		TradeRequest tradeRequestSum = new TradeRequest();
		tradeRequestSum.setMemberId(user.getMemberId());
		tradeRequestSum.setSellerId(user.getMemberId());
		// 网关交易订单号==退款记录的交易原始凭证号
		tradeRequestSum.setOrigTradeVoucherNo(tradeVoucherNo);
		// 分页信息
		QueryBase queryBase5Sum = new QueryBase();
		queryBase5Sum.setPageSize(50000);//
		queryBase5Sum.setCurrentPage(Integer.valueOf(currentPage));
		tradeRequestSum.setQueryBase(queryBase5Sum);
		tradeRequestSum.setGmtStart(DateUtil.getDateNearCurrent(-366));
		tradeRequestSum.setGmtEnd(sdfhms.parse(sdf.format(date) + " 23:59:59"));
		// 交易对应的所有退款记录
		PageResultList<TradeInfo> page5 = defaultTradeQueryService.queryRefundList(
				tradeRequestSum, env);// 通过所有数据获取退款笔数和已退款金额
		int count=0;
		double am=0;
		if(page5.getInfos()!=null){
			for (int i = 0; i < page5.getInfos().size(); i++) {
				if(page5.getInfos().get(i).getStatus().equals("951"))//退款成功
				{
					am=am+page5.getInfos().get(i).getPayAmount().getAmount().doubleValue();
				}
				count++;
			}
		}
		restP.getData().put("info", page.getInfos());
		restP.getData().put("page", page.getPageInfo());
//		restP.getData().put("page5", page5);
		restP.getData().put("count", count);
		restP.getData().put("am", am);
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", user);
		// 网关交易详情
		CoTradeQueryResponse rep = tradeService.queryList(breq);
		if (rep != null) {
			//查询支付方式
			String payChannel = tradeService.queryPayMethods(tradeVoucherNo, env);
			if(payChannel.equals("05")){
				rep.getBaseInfoList().get(0).setPayChannel("纯贷（银行卡）");
			}else if(payChannel.equals("71")){
				rep.getBaseInfoList().get(0).setPayChannel("纯借（银行卡）");
			}else if(payChannel.equals("72")){
				rep.getBaseInfoList().get(0).setPayChannel("微信");
			}else if(payChannel.equals("73")){
				rep.getBaseInfoList().get(0).setPayChannel("支付宝");
			}else{
				rep.getBaseInfoList().get(0).setPayChannel(null);
			}
			restP.getData().put("list", rep);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/PosDetail", "response", restP);
	}
	
	/**
	 * (查看)网关交易--查看详情+退款记录
	 * */
	@RequestMapping(value = "/my/GatewayDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchGatewayDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String tradeVoucherNo = request.getParameter("tradeVoucherNo");
		String currentPage = request.getParameter("currentPage");
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		QueryBase queryBase = new QueryBase();// 分页信息
		queryBase.setNeedQueryAll(true);
		queryBase.setCurrentPage(Integer.valueOf(currentPage));// currentPage

		CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
		breq.setQueryBase(queryBase);
		breq.setNeedSummary(true);// 需要汇总
		// breq.setMemberId(user.getMemberId());
		breq.setTradeVoucherNo(tradeVoucherNo);
		breq.setBeginTime(DateUtil.getDateNearCurrent(-366));
		breq.setEndTime(new Date());

		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		if (user.getMemberId() == null) {
			throw new IllegalAccessException("illegal user error!");
		}
		TradeRequest tradeRequest = new TradeRequest();
		tradeRequest.setMemberId(user.getMemberId());
		tradeRequest.setSellerId(user.getMemberId());
		// 网关交易订单号==退款记录的交易原始凭证号
		tradeRequest.setOrigTradeVoucherNo(tradeVoucherNo);
		
		// 分页信息
		QueryBase queryBase5 = new QueryBase();
		queryBase5.setPageSize(5);// 每页五条
		queryBase5.setCurrentPage(Integer.valueOf(currentPage));
		tradeRequest.setQueryBase(queryBase5);
		tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));
		Date date=new Date();
		tradeRequest.setGmtEnd(sdfhms.parse(sdf.format(date) + " 23:59:59"));
		// 交易记录对应的所有退款记录（每页五条记录）
		PageResultList page = defaultTradeQueryService.queryRefundList(
				tradeRequest, env);
		
		// 统计已退款笔数和金额
		TradeRequest tradeRequestSum = new TradeRequest();
		tradeRequestSum.setMemberId(user.getMemberId());
		tradeRequestSum.setSellerId(user.getMemberId());
		// 网关交易订单号==退款记录的交易原始凭证号
		tradeRequestSum.setOrigTradeVoucherNo(tradeVoucherNo);
		// 分页信息
		QueryBase queryBase5Sum = new QueryBase();
		queryBase5Sum.setPageSize(50000);//
		queryBase5Sum.setCurrentPage(Integer.valueOf(currentPage));
		tradeRequestSum.setQueryBase(queryBase5Sum);
		tradeRequestSum.setGmtStart(DateUtil.getDateNearCurrent(-366));
		tradeRequestSum.setGmtEnd(sdfhms.parse(sdf.format(date) + " 23:59:59"));
		// 交易对应的所有退款记录
		PageResultList<TradeInfo> page5 = defaultTradeQueryService.queryRefundList(
				tradeRequestSum, env);// 通过所有数据获取退款笔数和已退款金额
		int count=0;
		double am=0;
		if(page5.getInfos()!=null){
			for (int i = 0; i < page5.getInfos().size(); i++) {
				if(page5.getInfos().get(i).getStatus().equals("951"))//退款成功
				{
					am=am+page5.getInfos().get(i).getPayAmount().getAmount().doubleValue();
				}
				count++;
			}
		}
		restP.getData().put("info", page.getInfos());
		restP.getData().put("page", page.getPageInfo());
		restP.getData().put("page5", page5);
		restP.getData().put("count", count);
		restP.getData().put("am", am);
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", user);
		// 网关交易详情
		CoTradeQueryResponse rep = tradeService.queryList(breq);
		if (rep != null) {
			restP.getData().put("list", rep);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/GatewayDetail", "response", restP);
	}
	
	/**
	 * (查看)云+POS交易--查看详情
	 * */
	@RequestMapping(value = "/my/NewPosDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchNewPosDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String tradeNo = request.getParameter("tradeNo");
		EnterpriseMember user = getUser(request);
		PosTradeRequest req = new PosTradeRequest();// 云+pos查询条件封装
		req.setMemberId(user.getMemberId());
//		req.setMemberId("990290055116257");
		req.setTradeNo(tradeNo);

		RestResponse restP = new RestResponse();// zzq返回数据封装 
		restP.setData(new HashMap<String, Object>());
		Map<String, String> errorMap = new HashMap<String, String>();
		checkUser(user, errorMap, restP);// 验证用户是否存在
		if (user.getMemberId() == null) {
			throw new IllegalAccessException("illegal user error!");
		}
		PosTradeResponse page = posTradeService.selectPosList(req);
		restP.getData().put("page", page.getPosList());
		restP.getData().put("member", user);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/NewPosDetail", "response", restP);
	}

	/**
	 * 退款异步查询，确认是否可退款
	 * 
	 * */
	@ResponseBody
	@RequestMapping(value = "/my/getIsRefund.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public RestResponse getIsRefund(HttpServletRequest request,HttpSession session) throws Exception  {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
//		
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		Date date=new Date();
		EnterpriseMember user = getUser(request);// 8、
		String tradeVoucherNo = request.getParameter("tradeVoucherNo");
		// 统计已退款笔数和金额
		TradeRequest tradeRequestSum = new TradeRequest();
//		tradeRequestSum.setBuyerId(user.getMemberId());
		// 网关交易订单号==退款记录的交易原始凭证号
		tradeRequestSum.setOrigTradeVoucherNo(tradeVoucherNo);
		// 分页信息
		QueryBase queryBase5Sum = new QueryBase();
		queryBase5Sum.setPageSize(50000);//
		queryBase5Sum.setCurrentPage(1);
		tradeRequestSum.setQueryBase(queryBase5Sum);
		tradeRequestSum.setGmtStart(DateUtil.getDateNearCurrent(-366));
		tradeRequestSum.setGmtEnd(sdfhms.parse(sdf.format(date) + " 23:59:59"));
		// 交易对应的所有退款记录
		PageResultList page5=new PageResultList();
		try {
			page5 = defaultTradeQueryService.queryRefundList(tradeRequestSum, env);
		} catch (ServiceException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}// 通过所有数据获取退款笔数和已退款金额
		//不可退金额包括审核中金额，处理中金额和退款成功金额
		Money noRefundMoney=new Money();
		List<TradeInfo> refundTradeList = page5.getInfos();
		if((refundTradeList!=null) && (refundTradeList.size()>0))
		{
			for (int i = 0; i < refundTradeList.size(); i++) 
			{
				if(refundTradeList.get(i).getStatus().equals("900") || refundTradeList.get(i).getStatus().equals("901") 
						|| refundTradeList.get(i).getStatus().equals("951"))
				{
//					noRefundMoney.add(refundTradeList.get(i).getPayAmount());
					noRefundMoney.addTo(refundTradeList.get(i).getPayAmount());
				}
			}
		}
		//查询退款审核中的金额
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("origTranVoucherNo",tradeVoucherNo);
        List<Audit> AuditList = new ArrayList<Audit>();
        List<AuditDO> list = auditDAO.query(params);
        if((list!=null) && (list.size()>0)){
            Audit audit;
            for(int i=0;i<list.size();i++){
            	audit = AuditConvert.from(list.get(i));
                AuditList.add(audit);
            }
        }
		if (AuditList.size() > 0)
        {
        	for (int i = 0; i < AuditList.size(); i++) {
        		if(AuditList.get(i).getStatus().equals("1"))
        		{
        			noRefundMoney.addTo(AuditList.get(i).getAmount());
        		}
			}
        }
		QueryBase queryBase = new QueryBase();// 分页信息
		queryBase.setNeedQueryAll(true);
		queryBase.setCurrentPage(1);// currentPage
		CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
		breq.setQueryBase(queryBase);
		breq.setTradeVoucherNo(tradeVoucherNo);
		breq.setBeginTime(DateUtil.getDateNearCurrent(-366));// 只能查询一年内的
		breq.setEndTime(sdfhms.parse(sdf.format(date) + " 23:59:59"));// 只能查询一年内的
		EnterpriseMember member=new EnterpriseMember();
		try {
			member = defaultMemberService.queryCompanyMember(user,
					env);
		} catch (ServiceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (MemberNotExistException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}// 查询会员所以需要的信息//10、
		try {
			member.setAccount(accountService.queryAccountById(
					user.getDefaultAccountId(), env));
		} catch (BizException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}// 查询会员所以需要的信息//11、
		if (user.getMemberId() == null) {
			try {
				throw new IllegalAccessException("illegal user error!");
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 网关交易详情
		CoTradeQueryResponse tradeQueryResponse=new CoTradeQueryResponse();
		try {
			tradeQueryResponse = tradeService.queryList(breq);
		} catch (BizException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BaseInfo baseInfo = tradeQueryResponse.getBaseInfoList().get(0);

		Money maxRefundMoney = baseInfo.getPayAmount().subtract(noRefundMoney);
		if ((maxRefundMoney == null) || (maxRefundMoney.compareTo(new Money("0")) == -1)) {
			maxRefundMoney.setAmount("0");
		}
		boolean isrefund=false;
		if(maxRefundMoney.equals(new Money("0")))
		{
			isrefund=true;
		}
		super.setJsonAttribute(session, "baseInfoList", tradeQueryResponse
				.getBaseInfoList().get(0));
//		String fee = getServiceFee(request, new Money(amount));
		restP.setSuccess(true);
		data.put("maxRefundMoney", maxRefundMoney.getAmount());
		data.put("noRefundMoney", noRefundMoney.getAmount());
		data.put("isrefund", isrefund);
		restP.setData(data);
		return restP;
	}
	
	/**
	 * 第一步：网关交易-退款--获取交易订单号，(代码实现的就是将网关交易的详情通过交易订单号查出来)+显示的退款服务费需要单独接口的计算！！！
	 * */
	@RequestMapping(value = "/my/RefundApply.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView RefundApply(HttpServletRequest request,
			HttpServletResponse resp, HttpSession session, boolean error,
			ModelMap model, OperationEnvironment env) throws Exception {
		ModelAndView mv = new ModelAndView();
		BaseInfo baseInfoList = this.getJsonAttribute(session, "baseInfoList",
				BaseInfo.class);
		String maxRefundMoney = request.getParameter("maxRefundMoney");
		String noRefundMoney = request.getParameter("noRefundMoney");
		mv.addObject("maxRefundMoney", maxRefundMoney);
		mv.addObject("baseInfoList", baseInfoList);
		mv.addObject("noRefundMoney", noRefundMoney);
		mv.setViewName(CommonConstant.URL_PREFIX + "/trade/RefundApply");
		return mv;
	}

	// new Money(getServiceFee(request, amountMoney))
	@ResponseBody
	@RequestMapping(value = "/my/getFee.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public RestResponse getFee(HttpServletRequest request) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String amount = request.getParameter("feeAmount");
		String fee = getServiceFee(request, new Money(amount));
		restP.setSuccess(true);
		data.put("fee", fee);
		restP.setData(data);
		return restP;
	}

	/**
	 * 第二步：网关交易退款--获取交易订单号，上一步退款金额和备注+显示的退款服务费需要单独接口的计算！！！
	 * */
	@RequestMapping(value = "/my/RefundApplyPay.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView RefundApplyPay(HttpServletRequest request,
			HttpSession session, HttpServletResponse resp, boolean error,
			ModelMap model, TradeEnvironment env, QueryTradeForm form)
			throws Exception {
		ModelAndView mv = new ModelAndView();
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		// private List<BaseInfo> baseInfoList;
		String refundAmount = request.getParameter("refundAmount");// 退款金额form.getRefundAmount();
		String remarks = request.getParameter("remarks");// 备注
		String refundFee = request.getParameter("refundFee");// 费率
		if(refundFee==""){
		    refundFee="0.00";
		}
		String maxmoney = request.getParameter("maxmoney");// 可退金额
		BaseInfo baseInfoList = this.getJsonAttribute(session, "baseInfoList",
				BaseInfo.class);
		mv.addObject("baseInfoList", baseInfoList);
		mv.addObject("refundAmount", refundAmount);
		mv.addObject("remarks", remarks);
		mv.addObject("refundFee", refundFee);
		mv.addObject("maxmoney", maxmoney);
		// this.setJsonAttribute(session, "tradeQueryResponse",
		// tradeQueryResponse);
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			mv.addObject("isCertActive", "yes");
		} else {
			mv.addObject("isCertActive", "no");
		}
		if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {//EW_MY_APPROVE-EW_REFUND_AUDIT
			// 有退款审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/RefundApplyPay");
		} else {
			// 无退款审核权限RefundPay-Apply.vm
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/RefundPay-Apply");
		}

		return mv;
	}

	/**
	 * 有权限--最终实现退款
	 * */
	@RequestMapping(value = "/my/TradeRefund.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView tradeRefund(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			HttpSession session, TradeEnvironment env, QueryTradeForm form) throws Exception {
		ModelAndView mv = new ModelAndView();
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String refundAmount = form.getRefundAmount();
		String remarks = form.getRemarks();
		EnterpriseMember currUser = this.getUser(request);
		
		// 记录退款申请
		logger.info(LogUtil.generateMsg(OperateTypeEnum.SINGLE_REFUND, currUser, env, StringUtils.EMPTY));
		
		TradeRequestInfo req = RefundConvertor
				.convertRefundApply(currUser, env);
		BaseInfo baseInfoList = this.getJsonAttribute(session, "baseInfoList",
				BaseInfo.class);
		EnterpriseMember user = getUser(request);
		String type = "";// 返回页面显示
		String mobileCaptcha = request.getParameter("mobileCaptcha");// 有验证码说明没有硬证书，反之
		if (StringUtils.isNotEmpty(mobileCaptcha)) {// 验证码是否为空
			if (!validateCaptcha(request, user, null, mv, env)) {// 验证码是否正确
				logger.error("验证码错误！");
				mv.setViewName(CommonConstant.URL_PREFIX
						+ "/trade/refund-result");
				return mv;
			}
		} else {
			// 验证硬证书签名
			String payPassword = request.getParameter("payPassword");
			String signedData = request.getParameter("signedData");
			try {
				if (!validateSignature(request, payPassword, signedData, null,
						mv, env)) {
					logger.error("硬证书签名错误！");
					mv.setViewName(CommonConstant.URL_PREFIX
							+ "/trade/refund-result");
					return mv;
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("验证证书时编码错误", e);
				mv.addObject("message", "您未插入快捷盾或证书已经过期！");
				mv.setViewName(CommonConstant.URL_PREFIX
						+ "/trade/refund-result");
				return mv;
			}			
		}
		// 检查支付密码
		if (!validatePayPassword(request, user, null, mv)) {
			logger.error("支付密码错误");
			mv.setViewName(CommonConstant.URL_PREFIX
					+ "/trade/refund-result");
			return mv;
		}
		MemberAccount account = null;
		try {
			// 检查账户
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			
			// 检查余额是否足够
			Money avaBalance = account.getAvailableBalance();
			Money totalMoney = new Money(refundAmount);
			
			if (avaBalance.compareTo(totalMoney) == -1) {
				logger.error("账户[{}]余额不足", account.getAccountId());
				mv.addObject("message", "账户余额不足");
				mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
				return mv;
			}
			mv.addObject("avaBalance", avaBalance);
		} catch (Exception e) {
			logger.error("查询账户失败", e);
			mv.addObject("message", "无法获取到您的账户信息!");
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
			return mv;
		}
		
		// 生成退款交易凭证号
		String voucherNo = voucherService.recordTradeVoucher(req);
		// 页面获取到的退款金额
		TradeRefundRequset tradeRefundRequset = new TradeRefundRequset();
		tradeRefundRequset.setRefundAmount(new Money(refundAmount));
//		
		tradeRefundRequset.setTradeSourceVoucherNo(req.getTradeSourceVoucherNo());
		tradeRefundRequset.setTradeVoucherNo(voucherNo);
		tradeRefundRequset.setRemarks(remarks);
		tradeRefundRequset.setAccessChannel(AccessChannel.WEB.getCode());
		RefundResponse refundResponse = tradeService.tradeRefund(
				baseInfoList, tradeRefundRequset, env);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("rep", baseInfoList);
		data.put("memo", remarks);
		
	    // 记录退款审核
        logger.info(LogUtil.generateMsg(OperateTypeEnum.REFUND_EXAMINE, currUser, env, "通过"));
		
		if(refundResponse.isSuccess())
		{
			Audit audit = new Audit();
			audit.setTranSourceVoucherNo(req.getTradeSourceVoucherNo());
			audit.setOrigTranVoucherNo(baseInfoList.getTradeVoucherNo());//原交易订单号
			audit.setExt(baseInfoList.getSerialNumber());//商户订单号
			audit.setTranVoucherNo(voucherNo);// 退款凭证
			audit.setAuditType(TradeType.REFUND.getCode());// 2.退款-refund
			audit.setAmount(new Money(form.getRefundAmount()));// 退款金额
			audit.setMemberId(user.getMemberId());// 会员id
			audit.setOperatorName(user.getOperator_login_name());// 登陆名
			audit.setOperatorId(user.getCurrentOperatorId());
			audit.setStatus("2");// 1.待审核
			audit.setGmtCreated(new Date());
			audit.setAuditData(JSONObject.toJSONString(data));
			audit.setAuditSubType("single");//单笔batch
			audit.setAuditorName(user.getOperator_login_name());//审核人
			audit.setGmtModified(new Date());
			auditService.addAudit(audit);
			type = "tradeSuccess";
		}else {
			mv.addObject("message", refundResponse.getResultMessage());
		}
		mv.addObject("type", type);
		mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
		return mv;
	}

	/**
	 * 无权限 -最终实现退款申请
	 * */
	@RequestMapping(value = "/my/TradeRefundApply.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView tradeRefundApply(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			HttpSession session, TradeEnvironment env, QueryTradeForm form) throws Exception {
		ModelAndView mv = new ModelAndView();
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String refundFee = request.getParameter("refundFee");// 1
		if(refundFee==""){
		    refundFee="0.00";
		}
		String refundAmount = form.getRefundAmount();
		String remarks = form.getRemarks();
		BaseInfo baseInfoList = this.getJsonAttribute(session, "baseInfoList",
				BaseInfo.class);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("rep", baseInfoList);
		data.put("memo", remarks);
		String type = "";// 返回页面显示
		// 检查支付密码
		ModelAndView mvError = new ModelAndView();
		EnterpriseMember user = this.getUser(request);
		
	    // 记录退款申请
        logger.info(LogUtil.generateMsg(OperateTypeEnum.SINGLE_REFUND, user, env, StringUtils.EMPTY));
        
		if (!validatePayPassword(request, user, null, mvError)) {
			mvError.setViewName(CommonConstant.URL_PREFIX
					+ "/trade/refund-result");
			return mvError;
		}
		Money RefundAmount=new Money(form.getRefundAmount());
		Money RefundFee=new Money(refundFee);
		// 冻结金额
		String bizPaymentSeqNo = fundsControlService.freeze(user.getMemberId(), user.getDefaultAccountId(), 
				RefundAmount.add(RefundFee), env);
		if (StringUtils.isEmpty(bizPaymentSeqNo)) {
			logger.error("资金冻结失败");
			throw new Exception("您的账户余额不足！");
		}
		Audit audit = new Audit();
		TradeRequestInfo req = RefundConvertor
				.convertRefundApply(user, env);
		String voucherNo = voucherService.recordTradeVoucher(req);
		audit.setTranSourceVoucherNo(req.getTradeSourceVoucherNo());
		audit.setOrigTranVoucherNo(baseInfoList.getTradeVoucherNo());//原交易订单号
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("serialNumber", baseInfoList.getSerialNumber());//原商户订单号
		map.put("bizPaymentSeqNo", bizPaymentSeqNo);//冻结订单号
		audit.setExt(JSONObject.toJSONString(map));
//		audit.setExt(bizPaymentSeqNo);//冻结单号
		audit.setTranVoucherNo(voucherNo);// 退款凭证
		audit.setAuditType(TradeType.REFUND.getCode());// 2.退款-refund
		audit.setAmount(RefundAmount);// 退款金额
		audit.setMemberId(user.getMemberId());// 会员id
		audit.setOperatorName(user.getOperator_login_name());// 登陆名
		audit.setOperatorId(user.getCurrentOperatorId());
		audit.setStatus("1");// 1.待审核
		audit.setGmtCreated(new Date());
		audit.setAuditData(JSONObject.toJSONString(data));
		audit.setAuditSubType("single");//单笔
		audit.setRemark(remarks);//备注
		audit.setFee(RefundFee);//手续费
		auditService.addAudit(audit);
		type = "auditSuccess";
		mv.addObject("type", type);
		mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
		return mv;
	}

	/**
	 * 导入批量退款excel文件
	 * 
	 * @param request
	 * @param session
	 * @param batchFile
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/importBatchRefund.htm", method = RequestMethod.POST)
	public ModelAndView importBankBatchTransfer(HttpServletRequest request,
			HttpSession session,
			@RequestParam("batchFile") MultipartFile batchFile,
			TradeEnvironment env) {
		EnterpriseMember user = getUser(request);
		ModelAndView mv = new ModelAndView();

		logger.debug("导入Excel文件[{}]", batchFile.getOriginalFilename());

		Workbook xwb = null;
		// 批量退款账户信息 refundCount
		int refundCount = 0;// 退款笔数
		Money refundAmount = new Money();// 总金额//原交易服务费总计//退还服务费//额总计=退款总金额+退还服务费
		Money tradeFee = new Money();
		Money refundFee = new Money();
		Money sum = new Money();
		String isRefund = "true";
		List<BatchRefundForm> batchRefund = new ArrayList<BatchRefundForm>();// excel中经过处理后的数据
		//商户批次号里,退款商户订单号
		String[] sourceBatchNo=new String[2];
		try {
			xwb = WorkbookFactory.create(batchFile.getInputStream());
			// 循环工作表Sheet
			int numOfSheets = xwb.getNumberOfSheets();
			for (int i = 0; i < numOfSheets; i++) {
				parseOneBankSheet(batchRefund, xwb.getSheetAt(i), request,sourceBatchNo);
			}
		  //判断商户批次号是否为空			 
		    if("bare".equals(sourceBatchNo[0])){
				mv.addObject("message", "您提交的退款文件中，商户批次不能为空或格式不正确！");
				mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
				return mv;
		    }
			//判断退款商户订单号是否有重复			 
		    if("notonlyone".equals(sourceBatchNo[1])){
				mv.addObject("message", "您提交的退款文件中，有重复订单号，请确认重新提交");
				mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
				return mv;
		    }
			for (int i = 0; i < batchRefund.size(); i++) {				
				refundCount++;// 退款笔数
				if (batchRefund.get(i).getSuccess().equals("true")) {
					refundAmount=refundAmount.add(batchRefund.get(i).getRefundMoney());//退款金额
					tradeFee = tradeFee.add(batchRefund.get(i).getTradeFee());// 原交易服务费总计
					refundFee = refundFee.add(batchRefund.get(i).getRefundFee());// 退还服务费
				} else {
					isRefund = "false";
				}
			}
			MemberAccount account = null;
			try {
				// 检查账户
				account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
				
				// 检查余额是否足够
				Money avaBalance = account.getAvailableBalance();
				Money totalMoney = refundAmount;
				
				if (avaBalance.compareTo(totalMoney) == -1) {
					logger.error("账户[{}]余额不足", account.getAccountId());
					mv.addObject("message", "账户余额不足");
					mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
					return mv;
				}
				mv.addObject("avaBalance", avaBalance);
			} catch (Exception e) {
				logger.error("查询账户失败", e);
				mv.addObject("message", "无法获取到您的账户信息!");
				mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
				return mv;
			}
		} catch (Exception e) {
			logger.error("导入Excel文件失败", e);
			mv.addObject("message", "您导入的文件格式或内容不正确!");
			mv.setViewName(CommonConstant.URL_PREFIX
					+ "/trade/refund-result");
			return mv;
		}
		// 把相关信息放到Session中
		sum = refundFee.add(refundAmount);
		this.setJsonAttribute(session, "refundCount", refundCount);
		this.setJsonAttribute(session, "refundAmount", refundAmount);
		this.setJsonAttribute(session, "sum", sum);
		this.setJsonAttribute(session, "batchRefund", batchRefund);
		this.setJsonAttribute(session, "sourceBatchNo", sourceBatchNo[0]);
		mv.addObject("batchRefund", batchRefund);
		mv.addObject("refundCount", refundCount);
		mv.addObject("refundAmount", refundAmount);
		mv.addObject("tradeFee", tradeFee);
		mv.addObject("refundFee", refundFee);
		mv.addObject("sum", sum);
		mv.addObject("isRefund", isRefund);
		mv.addObject("sourceBatchNo", sourceBatchNo[0]);
		SessionPage<BatchRefundForm> sessionPage = new SessionPage<BatchRefundForm>();
		super.setSessionPage(batchRefund, sessionPage);
		mv.addObject("sessionPage", sessionPage);
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			mv.addObject("isCertActive", "yes");
		} else {
			mv.addObject("isCertActive", "no");
		}
		if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {
			// 有退款审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/BatchRefund");
		 } else {
			// 无退款审核权限
			mv.setViewName(CommonConstant.URL_PREFIX +"/trade/BatchRefund-apply");
		 }
		return mv;
	}

	/**
	 * 解析sheet中的退款信息
	 * 
	 * @param batchRefund
	 *            退款信息列表
	 * @param sheet
	 *            excel中的sheet对象
	 */
	private void parseOneBankSheet(List<BatchRefundForm> batchRefund,
			Sheet sheet, HttpServletRequest request,String sourceBatchNo[]) throws Exception {
		
		//获取商户批次号
		Row batchRow = sheet.getRow(1);
		sourceBatchNo[0]=getValue(batchRow.getCell(1));
		if (StringUtils.isEmpty(sourceBatchNo[0])||!sourceBatchNo[0].matches("^\\w{1,32}$")) {
			sourceBatchNo[0]="bare";//商户批次号为空
		}
		// 循环行Row
        Map<String, String> map = new HashMap<String, String>();
		for (int rowNum = 3; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				logger.debug(rowNum + "行为空");
				continue;
			}
			BatchRefundForm form = new BatchRefundForm();
            //原商户订单号
			String origSourceDetailNo = getValue(row.getCell(0));
			if (StringUtils.isEmpty(origSourceDetailNo)) {
				logger.debug(rowNum + "行1列为空");
				continue;// 判断-原商户订单号是否为空
			}
			//原商户订单号
			form.setSerialNumber(origSourceDetailNo.trim());
			
			//退款商户订单号
			String refundNumber = getValue(row.getCell(1));
			if (StringUtils.isEmpty(refundNumber)) {
				logger.debug(rowNum + "行2列为空");
				continue;// 判断-退款商户订单号是否为空
			}	
			if(!refundNumber.matches("^\\w{1,32}$"))
			{				
				form.setRemarks("退款商户订单号有误");				
				form.setSuccess("false");				
				batchRefund.add(form);
				logger.debug(rowNum + "行2列的退款商户订单号有误");
				continue;
			}
            if (map.get(refundNumber.trim()) != null) {
            	sourceBatchNo[1]="notonlyone";
            }else {
            	map.put(refundNumber.trim(), refundNumber.trim());
			}
            form.setRefundNumber(refundNumber);
			//退款金额
			String amount = getValue(row.getCell(2));// 要退款的金额
			if (StringUtils.isEmpty(amount)) {
				logger.debug(rowNum + "行3列为空");
				amount = "0";
			}
			Money amountMoney;
			if(amount.matches("^\\d+(\\.\\d{0,2})?$") ||amount.matches("[0-9]+"))
			{
				amountMoney = new Money(amount);
			}else {
				amountMoney = new Money();
				form.setRemarks("退款金额有误");
				form.setRefundMoney(amountMoney);// 退款金额
				form.setSuccess("false");
				form.setRefundFee(new Money());// 
				batchRefund.add(form);
				logger.debug(rowNum + "行3列的退款金额有误");
				continue;// 判断交易订单号对应的记录是否有效
			}
			
			Cell memo = row.getCell(3);
			form.setMemo(getValue(memo));// 备注
			logger.debug("原商户订单号订单号：{}", origSourceDetailNo);
			QueryBase queryBase = new QueryBase();// 分页信息
			queryBase.setNeedQueryAll(true);
			queryBase.setCurrentPage(1);// 
			CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
			breq.setQueryBase(queryBase);
			breq.setNeedSummary(true);// 需要汇总
			breq.setTradeSourceVoucherNo(origSourceDetailNo);
			breq.setBeginTime(DateUtil.getDateNearCurrent(-366));
			breq.setEndTime(new Date());
			CoTradeQueryResponse rep = tradeService.queryList(breq);
			if (rep.getBaseInfoList() == null) {
				form.setRemarks("原商户订单号有误");
				form.setRefundMoney(amountMoney);// 退款金额
				form.setSuccess("false");
				form.setRefundFee(new Money());// 
				batchRefund.add(form);
				logger.debug(rowNum + "行0列的原商户订单号不存在");
				continue;// 判断交易订单号对应的记录是否有效
			}
//			form.setSerialNumber(rep.getBaseInfoList().get(0).getSerialNumber());// 商户订单号
			form.setTradeMoney(rep.getBaseInfoList().get(0).getPayAmount());// 交易金额
			form.setTradeFee(rep.getBaseInfoList().get(0).getPayeeFee());// 交易服务费
			form.setBizNo(rep.getBaseInfoList().get(0).getBizNo());// 订单号			
			form.setTradeVoucherNo(rep.getBaseInfoList().get(0).getTradeVoucherNo());  //原交易凭证号 
			// 统计已退款金额
			TradeRequest tradeRequestSum = new TradeRequest();
			// 网关交易订单号==退款记录的交易原始凭证号
			tradeRequestSum.setOrigTradeVoucherNo(rep.getBaseInfoList().get(0).getTradeVoucherNo());
			// 分页信息
			QueryBase queryBase5Sum = new QueryBase();
			queryBase5Sum.setPageSize(50000);//
			queryBase5Sum.setCurrentPage(1);
			tradeRequestSum.setQueryBase(queryBase5Sum);
			tradeRequestSum.setGmtStart(DateUtil.getDateNearCurrent(-366));
			tradeRequestSum.setGmtEnd(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
			// 交易对应的所有退款记录
			PageResultList page5 = defaultTradeQueryService.queryRefundList(tradeRequestSum, env);// 通过所有数据获取已退款金额
			//不可退金额包括审核中金额，处理中金额和退款成功金额
			Money noRefundMoney=new Money();
			List<TradeInfo> refundTradeList = page5.getInfos();
			if((refundTradeList!=null) && (refundTradeList.size()>0))
			{
				for (int i = 0; i < refundTradeList.size(); i++) 
				{
					if(!refundTradeList.get(i).getStatus().equals("952"))
					{
						noRefundMoney.addTo(refundTradeList.get(i).getPayAmount());
					}
				}
			}
			//查询退款审核中的金额
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("origTranVoucherNo",origSourceDetailNo);
	        List<Audit> AuditList = new ArrayList<Audit>();
	        List<AuditDO> list = auditDAO.query(params);
	        if((list!=null) && (list.size()>0)){
	            Audit audit;
	            for(int i=0;i<list.size();i++){
	            	audit = AuditConvert.from(list.get(i));
	                AuditList.add(audit);
	            }
	        }
			if (AuditList.size() > 0)
	        {
	        	for (int i = 0; i < AuditList.size(); i++) {
	        		if(AuditList.get(i).getStatus().equals("1"))
	        		{
	        			noRefundMoney.addTo(AuditList.get(i).getAmount());
	        		}
				}
	        }
			
			if (rep.getBaseInfoList().get(0).getPayAmount().getAmount()
					.subtract(noRefundMoney.getAmount()).subtract(amountMoney.getAmount()).doubleValue() < 0) {
				form.setRemarks("超过可退金额");
				form.setRefundMoney(amountMoney);// 退款金额
				form.setSuccess("false");
				form.setRefundFee(new Money());// 
				batchRefund.add(form);
				logger.debug(rowNum + "行0列的退款金额不能大于交易金额");
				continue;// 退款金额不能大于可退金额
			}
			if(amountMoney.getAmount().doubleValue()==0)
			{
				form.setRemarks("退款金额不能为空或为0");
				form.setRefundMoney(amountMoney);// 退款金额
				form.setSuccess("false");
				form.setRefundFee(new Money());// 
				batchRefund.add(form);
				logger.debug(rowNum + "行0列的退款金额不能大于交易金额");
				continue;// 退款金额不能大于可退金额
			}
			form.setRefundMoney(amountMoney);// 退款金额
			form.setRefundFee(new Money(getServiceFee(request, amountMoney)));// 通过计算得到
			form.setRemarks("可退款");
			form.setSuccess("true");
			batchRefund.add(form);
		}
	}

	/**
	 * 得到Excel表中的值
	 * 
	 * @param cell
	 *            Excel中的单元格
	 * @return Excel中单元格的值
	 */
	@SuppressWarnings("static-access")
	private String getValue(Cell cell) {
		if (cell == null) {
			return StringUtils.EMPTY;
		}
		if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
			// 返回布尔类型的值
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
			// 返回数值类型的值
			return String.valueOf(cell.getNumericCellValue());
		} else {
			// 返回字符串类型的值
			return String.valueOf(cell.getStringCellValue());
		}
	}

	/**
	 * 计算服务费
	 * 
	 * @param request
	 *            HTTP请求对象
	 * @param money
	 *            退款金额
	 * @return 服务费用
	 */
	private String getServiceFee(HttpServletRequest request, Money money) {
		// 先获取用户信息
		String memberId = this.getMemberId(request);

		PayPricingReq req = new PayPricingReq();
		req.setAccessChannel("WEB");
		req.setSourceCode(SYSTEM_CODE);
		req.setRequestNo(String.valueOf(System.currentTimeMillis()));
		req.setProductCode(TradeType.REFUND.getBizProductCode());
		req.setPaymentCode(PaymentType.PAY_FUND.getCode());
		req.setPayChannel(PayChannel.FUNDOUT.getCode());
		req.setPaymentInitiate(new Date());
		req.setPayableAmount(money);
		req.setOrderAmount(money);

		// 付款方
		List<com.netfinworks.pbs.service.context.vo.PartyInfo> partyList = new ArrayList<com.netfinworks.pbs.service.context.vo.PartyInfo>();
		com.netfinworks.pbs.service.context.vo.PartyInfo party = new com.netfinworks.pbs.service.context.vo.PartyInfo();
		party.setMemberId(memberId);
		party.setPartyRole(PartyRole.PAYER);
		partyList.add(party);
		req.setPartyInfoList(partyList);

		// 调用服务费计算接口
		PaymentPricingResponse resp = payPartyFeeFacade.getFee(req);
		logger.info("计算服务费，响应结果：{}", resp); 
		if ((resp != null) && resp.isSuccess()) {
			List<PartyFeeInfo> pfiList = resp.getPartyFeeList();
			if (ObjectUtils.isListNotEmpty(pfiList)) {
				for (PartyFeeInfo pfi : pfiList) {
					if (memberId.equals(pfi.getMemberId())) {
						logger.info("账户ID：{};费用详情：{}",memberId, pfi); 
						return pfi.getFee().format();
					}
				}
			}

		}

		return StringUtils.EMPTY;
	}

	/**
	 * 确认提交批量退款申请
	 * 
	 * @param request
	 * @param session
	 * @param env
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/confirmRefundApply.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView confirmRefundApply(HttpServletRequest request,
			HttpSession session, TradeEnvironment env) throws Exception {
		ModelAndView mv = new ModelAndView();
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
		
        // 批量退款申请
        logger.info(LogUtil.generateMsg(OperateTypeEnum.REFUND_APPLY_FILE, user, env,
                StringUtils.EMPTY));
        
		// 检查支付密码
		if (!validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
			return mv;
		}
		// 从会话中获取到退款信息
		List<BatchRefundForm> batchRefundList = getBatchRefundForm(this
				.getJsonAttribute(session, "batchRefund", List.class));	
		String sourceBatchNo=this.getJsonAttribute(session, "sourceBatchNo", String.class);
		BatchRequest batchRequest = this.fillBatchRequest( request,user,batchRefundList,env,sourceBatchNo);
		String batchVourchNo = voucherService.regist(VoucherInfoType.REFUND.getCode());
		batchRequest.setBatchNo(batchVourchNo);
		String refundfee=  getServiceFee(request, new Money(batchRequest.getTotalAmount()));
		BatchResponse resp = batchServiceFacade.create(batchRequest);
		if("S002".equals(resp.getResultCode())){
			mv.addObject("message", "您提交的批次号已存在，不能重复提交");
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
			return mv;
		}else if("S001".equals(resp.getResultCode()))//提交成功
		{
			//生成审核记录
			Audit audit= this.fillAuditRequest(batchRequest,user,new Money(refundfee),"1");
			auditService.addAudit(audit);
		}else {
			mv.addObject("message", "退款失败");
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
			return mv;
		}
		mv.addObject("type", "auditSuccess");
		mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
		return mv;
	}

    
    /*
     * 拼装BatchRequest,批量服务用
     */
    private BatchRequest fillBatchRequest(HttpServletRequest request,EnterpriseMember memberInfo,
    		List<BatchRefundForm> batchRefundList,TradeEnvironment env,String sourceBatchNo) throws Exception {
        BatchRequest batchRequest = new BatchRequest();
        batchRequest.setSourceBatchNo(sourceBatchNo);//商户批次号
        batchRequest.setSubmitTime(new Date());
        batchRequest.setPartnerId(memberInfo.getMemberId());//
        batchRequest.setSubmitType(SubmitType.API.getCode());
        batchRequest.setProductType(ProductType.REFUND.getCode());
        batchRequest.setSubmitId(memberInfo.getCurrentOperatorId());
        batchRequest.setSubmitName(memberInfo.getOperator_login_name());
        this.analyzeTradeList(request,batchRefundList,batchRequest, memberInfo,env);
        return batchRequest;
    }
    
    /*
     * 解析退款列表
     */
    private void analyzeTradeList(HttpServletRequest request,List<BatchRefundForm> batchRefundList, BatchRequest batchRequest,
    		EnterpriseMember memberInfo,TradeEnvironment env)throws Exception {
        List<BatchDetail> batchRefundDetails = new ArrayList<BatchDetail>();
        BatchRefundDetail batchRefundDetail = null;
        BigDecimal totalAmount = new BigDecimal("0");
        int totalCount = 0;
        //迭代转账列表
        for (int i = 0; i < batchRefundList.size(); i++) {
            String memo = "";
            BatchRefundForm batchRefundForm=batchRefundList.get(i);
//            TradeRequestInfo req = RefundConvertor.convertRefundApply(memberInfo, env);
//            String voucherNo = voucherService.recordTradeVoucher(req);
            //生成付款信息
            batchRefundDetail = new BatchRefundDetail();
            batchRefundDetail.setSourceDetailNo(batchRefundForm.getRefundNumber());//外部明细号-即退款商户订单号
            batchRefundDetail.setOrigSourceDetailNo(batchRefundForm.getSerialNumber());//原交易外部明细号-即商户号
            String refundMoney=batchRefundForm.getRefundMoney().getAmount().toString();
            batchRefundDetail.setAmount(new BigDecimal(refundMoney));
            batchRefundDetail.setTradeType(TradeType.REFUND.getPayCode());
            memo=batchRefundForm.getMemo()==null?"":batchRefundForm.getMemo();
            batchRefundDetail.setMemo(memo);//描述
    		Map<String, Object> data = new HashMap<String, Object>();
    		data.put("fee", batchRefundForm.getTradeFee().getAmount().toString());//交易服务费
    		batchRefundDetail.setExtension(JSONObject.toJSONString(data));
            batchRefundDetails.add(batchRefundDetail);
            totalAmount = totalAmount.add(new BigDecimal(refundMoney));//计算总金额
            totalCount++;//计算总笔数
        }
        batchRequest.setBatchDetails(batchRefundDetails);
        batchRequest.setTotalCount(totalCount);
        batchRequest.setTotalAmount(totalAmount);
    }
    
    /*
     * 拼装AuditRequest，批量审核用
     */
    private Audit fillAuditRequest(BatchRequest batchRequest,EnterpriseMember memberInfo,Money refundFee,String status)throws Exception {
//        AuditRequest auditRequest = new AuditRequest();
//        com.netfinworks.site.service.facade.model.Audit audit = new com.netfinworks.site.service.facade.model.Audit();
    	Audit audit = new Audit();
    	audit.setGmtCreated(new Date());
        audit.setTranVoucherNo(batchRequest.getBatchNo());
        audit.setTranSourceVoucherNo(batchRequest.getSourceBatchNo());
        audit.setOrigTranVoucherNo("");
        audit.setAuditType(CommonConstant.AUDIT_TYPE_REFUND);
        audit.setAuditSubType(CommonConstant.AUDIT_SUB_TYPE_BATCH);
        audit.setAmount(new Money(batchRequest.getTotalAmount()));
        audit.setMemberId(memberInfo.getMemberId());
        audit.setOperatorName(memberInfo.getOperator_login_name());
        audit.setOperatorId(memberInfo.getCurrentOperatorId());
        audit.setStatus(status);//
        audit.setFee(refundFee);//手续费
        BatchRequest  auditData =  batchRequest;
		auditData.setBatchDetails(null);// 批量明细不再记录
        audit.setAuditData(JsonUtils.toJson(auditData));
        return audit;
    }
    
	/**
	 * (批量退款)提交
	 * */
	@RequestMapping(value = "/my/confirmRefund.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public ModelAndView confirmRefund(HttpServletRequest request,
			HttpSession session, TradeEnvironment env)
			throws Exception {
		ModelAndView mv = new ModelAndView();
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
		
        // 记录退款申请
        logger.info(LogUtil.generateMsg(OperateTypeEnum.REFUND_APPLY_FILE, user, env,
                StringUtils.EMPTY));
//		boolean isSuccess = true;
		String mobileCaptcha = request.getParameter("mobileCaptcha");// 有验证码说明没有硬证书，反之
		ModelAndView mvError = new ModelAndView();
		if (StringUtils.isNotEmpty(mobileCaptcha)) {// 且有硬证书
			// 检查手机验证码
			if (!validateCaptcha(request, user, null, mvError, env)) {
				mvError.setViewName(CommonConstant.URL_PREFIX
						+ "/trade/refund-result");
				return mvError;
			}
		}
		else {
			// 验证硬证书签名
			String payPassword = request.getParameter("payPassword");
			String signedData = request.getParameter("signedData");
			try {
				if (!validateSignature(request, payPassword, signedData,null, mvError, env)) {
					mvError.setViewName(CommonConstant.URL_PREFIX+ "/trade/refund-result");
					return mvError;
					}
				} catch (UnsupportedEncodingException e) {
					logger.error("验证证书时编码错误", e);
					mvError.addObject("message", "您未插入快捷盾或证书已经过期！");
					mvError.setViewName(CommonConstant.URL_PREFIX
							+ "/trade/refund-result");
					return mvError;
				}
			}
		// 检查支付密码
		if (!validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
			return mv;
		}
		// 从会话中获取到退款信息
		List<BatchRefundForm> batchRefundList = getBatchRefundForm(this
				.getJsonAttribute(session, "batchRefund", List.class));
		String sourceBatchNo=this.getJsonAttribute(session, "sourceBatchNo", String.class);
		BatchRequest batchRequest = this.fillBatchRequest(request,user,batchRefundList,env,sourceBatchNo);
		String batchVourchNo = voucherService.regist(VoucherInfoType.REFUND.getCode());
		batchRequest.setBatchNo(batchVourchNo);
		String refundfee=  getServiceFee(request, new Money(batchRequest.getTotalAmount()));
//		Money TotalAmount =new Money(batchRequest.getTotalAmount());
//		String bizPaymentSeqNo = fundsControlService.freeze(user.getMemberId(), user.getDefaultAccountId(), 
//				TotalAmount.add(new Money(refundfee)), env);
		BatchResponse resp = batchServiceFacade.createAndPay(batchRequest);
		if("S002".equals(resp.getResultCode())){
			mv.addObject("message", "您提交的批次号已存在，不能重复提交");
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
			return mv;
		}else if("S001".equals(resp.getResultCode()))
		{
		    // 记录审核日志
		    logger.info(LogUtil.generateMsg(OperateTypeEnum.REFUND_EXAMINE, user, env, "通过"));
			//生成审核记录
			Audit audit =this.fillAuditRequest(batchRequest,user,new Money(refundfee),"2");
			audit.setAuditorId(user.getCurrentOperatorId());
			audit.setAuditorName(user.getOperator_login_name());
			audit.setGmtModified(new Date());
			audit.setRemark("审核通过");
			auditService.addAudit(audit);
		}else {
			mv.addObject("message", resp.getResultMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
			return mv;
		}


		mv.addObject("type", "tradeSuccess");
		mv.setViewName(CommonConstant.URL_PREFIX + "/trade/refund-result");
		return mv;
	}
	/**
	 * 从会话中分页查询记录
	 * 给批量退款退款结束后的结果分页
	 * @param sessionPage
	 * @param session
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getEndSessionPage")
	public RestResponse getSessionPages(
			com.netfinworks.mns.client.domain.PageInfo page, HttpSession session) {
		RestResponse response = new RestResponse();

		try {
			List<BatchRefundForm> batchRefundList = getBatchRefundForm(this
					.getJsonAttribute(session, "batchRefundEnd", List.class));
			SessionPage<BatchRefundForm> sessionPage = new SessionPage<BatchRefundForm>(
					page, new ArrayList<BatchRefundForm>());
			super.setSessionPage(batchRefundList, sessionPage);
			response.setSuccess(true);
			response.setMessageObj(sessionPage);
		} catch (Exception e) {
			logger.error("查询分页信息出错");
			response.setMessage("查询分页信息出错");
		}
		return response;
	}
	/**
	 * 从会话中分页查询记录
	 * 给批量退款导入数据分页
	 * @param sessionPage
	 * @param session
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getSessionPage")
	public RestResponse getSessionPage(
			com.netfinworks.mns.client.domain.PageInfo page, HttpSession session) {
		RestResponse response = new RestResponse();

		try {
			List<BatchRefundForm> batchRefundList = getBatchRefundForm(this
					.getJsonAttribute(session, "batchRefund", List.class));
			SessionPage<BatchRefundForm> sessionPage = new SessionPage<BatchRefundForm>(
					page, new ArrayList<BatchRefundForm>());
			super.setSessionPage(batchRefundList, sessionPage);
			response.setSuccess(true);
			response.setMessageObj(sessionPage);
		} catch (Exception e) {
			logger.error("查询分页信息出错");
			response.setMessage("查询分页信息出错");
		}
		return response;
	}

	/**
	 * 解析Transfer的JSON字符串列表
	 * 
	 * @param list
	 * @return
	 */
	private List<BatchRefundForm> getBatchRefundForm(List<?> list) {
		if (ObjectUtils.isListEmpty(list)) {
			return null;
		}

		List<BatchRefundForm> batchRefundList = new ArrayList<BatchRefundForm>();
		for (Object object : list) {
			BatchRefundForm transfer = JSONArray.parseObject(
					String.valueOf(object), BatchRefundForm.class);
			batchRefundList.add(transfer);
		}

		return batchRefundList;
	}
	
	/**
	 * 云+POS撤销详情--
	 * */
	@RequestMapping(value = "/my/PosRefundDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchPosRefundDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env, QueryTradeForm form) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String currentPage = form.getCurrentPage();// 4
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		String tradeVoucherNo = request.getParameter("tradeVoucherNo");// 1
		String origTradeSourceVoucherNo = request
				.getParameter("origTradeSourceVoucherNo");
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、

		if (user.getMemberId() == null) {
			throw new IllegalAccessException("illegal user error!");
		}
		TradeRequest tradeRequest = new TradeRequest();
		tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));// 参照查询退款接口默认时间
		tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));//sdfhms.parse(DateUtils.getDateString() + " 00:00:00")sdfhms.parse(sdf.format(new Date()) + " 23:59:59")
		// 分页信息
		QueryBase queryBase5 = new QueryBase();
		queryBase5.setCurrentPage(Integer.valueOf(currentPage));
		tradeRequest.setQueryBase(queryBase5);
		tradeRequest.setTradeVoucherNo(tradeVoucherNo);
		PageResultList page = defaultTradeQueryService.queryRefundList(
				tradeRequest, env);
		List<TradeInfo> list = page.getInfos();
		restP.getData().put("page", page);
		if(list.get(0).getBatchNo()!=null){
		    restP.getData().put("batchNo", list.get(0).getBatchNo());
		}
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", user);
		restP.setSuccess(true);

		// 查询订单详情
		CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
		breq.setQueryBase(queryBase5);
		breq.setNeedSummary(true);// 需要汇总
		// breq.setMemberId(user.getMemberId());
		breq.setTradeSourceVoucherNo(origTradeSourceVoucherNo);
		
		breq.setBeginTime(DateUtil.getDateNearCurrent(-366));
		breq.setEndTime(DateUtil.addMinutes(new Date(), 30));
		CoTradeQueryResponse rep = tradeService.queryList(breq);
		if (rep != null) {
			restP.getData().put("list", rep);
		}
		List<Audit> auditList = new ArrayList<Audit>();
		Audit audit = new Audit();
		/* 查询审核列表 */
		AuditListQuery query = new AuditListQuery();
		
		if(list.get(0).getBatchNo()==null){
		    query.setTransId(tradeVoucherNo);
        }else{
            query.setTransId(list.get(0).getBatchNo());
        }
		auditList = auditService.queryAuditList(query);
		if((auditList!=null) && (auditList.size()!=0)){
    		for (int i = 0; i < auditList.size(); i++) {
    			audit = auditList.get(i);
    			audit = auditService.queryAudit(audit.getId());
    			restP.getData().put(
    					"applyTime",
    					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit
    							.getGmtCreated()));// 申请时间
    			restP.getData().put("operatorName", audit.getOperatorName());// 申请操作员名称
    			if(audit.getGmtModified() != null) {
    				restP.getData().put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtModified()));// 更新时间
    				restP.getData().put("auditorName", audit.getAuditorName());// 审核员名称
    			}else{
    				restP.getData().put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//更新时间
    				restP.getData().put("auditorName", audit.getOperatorName());// 申请操作员名称
    			}
    			restP.getData().put("remark", audit.getRemark());// 审核说明
    			restP.getData().put("status", audit.getStatus());// 审核结果
    		}
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/PosRefundDetail", "response", restP);
	}

	/**
	 * 退款详情--
	 * */
	@RequestMapping(value = "/my/RefundDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchRefundDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env, QueryTradeForm form) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String currentPage = form.getCurrentPage();// 4
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		String tradeVoucherNo = request.getParameter("tradeVoucherNo");// 1
		String origTradeSourceVoucherNo = request
				.getParameter("origTradeSourceVoucherNo");
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、

		if (user.getMemberId() == null) {
			throw new IllegalAccessException("illegal user error!");
		}
		TradeRequest tradeRequest = new TradeRequest();
		tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));// 参照查询退款接口默认时间
		tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));//sdfhms.parse(DateUtils.getDateString() + " 00:00:00")sdfhms.parse(sdf.format(new Date()) + " 23:59:59")
		// 分页信息
		QueryBase queryBase5 = new QueryBase();
		queryBase5.setCurrentPage(Integer.valueOf(currentPage));
		tradeRequest.setQueryBase(queryBase5);
		tradeRequest.setTradeVoucherNo(tradeVoucherNo);
		PageResultList page = defaultTradeQueryService.queryRefundList(
				tradeRequest, env);
		List<TradeInfo> list = page.getInfos();
		restP.getData().put("page", page);
		if(list.get(0).getBatchNo()!=null){
		    restP.getData().put("batchNo", list.get(0).getBatchNo());
		}
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", user);
		restP.setSuccess(true);

		// 查询订单详情
		CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
		breq.setQueryBase(queryBase5);
		breq.setNeedSummary(true);// 需要汇总
		// breq.setMemberId(user.getMemberId());
		breq.setTradeSourceVoucherNo(origTradeSourceVoucherNo);
		
		breq.setBeginTime(DateUtil.getDateNearCurrent(-366));
		breq.setEndTime(DateUtil.addMinutes(new Date(), 30));
		CoTradeQueryResponse rep = tradeService.queryList(breq);
		if (rep != null) {
			restP.getData().put("list", rep);
		}
		List<Audit> auditList = new ArrayList<Audit>();
		Audit audit = new Audit();
		/* 查询审核列表 */
		AuditListQuery query = new AuditListQuery();
		
		if(list.get(0).getBatchNo()==null){
		    query.setTransId(tradeVoucherNo);
        }else{
            query.setTransId(list.get(0).getBatchNo());
        }
		auditList = auditService.queryAuditList(query);
		if((auditList!=null) && (auditList.size()!=0)){
    		for (int i = 0; i < auditList.size(); i++) {
    			audit = auditList.get(i);
    			audit = auditService.queryAudit(audit.getId());
    			restP.getData().put(
    					"applyTime",
    					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit
    							.getGmtCreated()));// 申请时间
    			restP.getData().put("operatorName", audit.getOperatorName());// 申请操作员名称
    			if(audit.getGmtModified() != null) {
    				restP.getData().put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtModified()));// 更新时间
    				restP.getData().put("auditorName", audit.getAuditorName());// 审核员名称
    			}else{
    				restP.getData().put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//更新时间
    				restP.getData().put("auditorName", audit.getOperatorName());// 申请操作员名称
    			}
    			restP.getData().put("remark", audit.getRemark());// 审核说明
    			restP.getData().put("status", audit.getStatus());// 审核结果
    		}
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/RefundDetail", "response", restP);
	}
	
	/**
	 * 代发工资详情
	 */
	@RequestMapping(value = "/my/PayrollDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchPayrollDetail(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = req.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(req);
		}
		RestResponse restP = new RestResponse();
		String currentPage = req.getParameter("currentPage");
		String fundoutOrderNo = req.getParameter("fundoutOrderNo");
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> errorMap = new HashMap<String, String>();

		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));
		
		List<Audit> auditList = new ArrayList<Audit>();
		PageResultList page = new PageResultList();
		Audit audit = new Audit();
		String tradeType = req.getParameter("tradeType");
		if(tradeType.equals("1")){
			DownloadBillRequest reqInfo = new DownloadBillRequest();
			List<String> productCode=new ArrayList<String>();
			productCode.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
			reqInfo.setProductCodes(productCode);
			reqInfo.setMemberId(user.getMemberId());
			QueryBase queryBase4 = new QueryBase();
			queryBase4.setCurrentPage(Integer.parseInt(currentPage));
			reqInfo.setQueryBase(queryBase4);
			reqInfo.setTradeVoucherNo(fundoutOrderNo);
			reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-366));
			reqInfo.setEndTime(DateUtil.addMinutes(new Date(), 30));
			page = defaultDownloadBillService.queryTransfer(reqInfo, env);
			List<TransferInfo> list = page.getInfos();
			double sum = 0;
			for (TransferInfo s : list) {
				sum = s.getTransferAmount().getAmount().doubleValue()
						+ s.getPayeeFee().getAmount().doubleValue();
			}
			BaseMember baseMember=new BaseMember();
            if(list.get(0).getPlamId().equals("1")){
                
                baseMember=memberService.queryMemberById(list.get(0).getBuyId(),env);
            }else {
                baseMember=memberService.queryMemberById(list.get(0).getSellId(),env);
            }
            data.put("loginName", baseMember.getLoginName());
			DecimalFormat df = new DecimalFormat("##0.00");
			data.put("sum", df.format(sum));
			/* 查询审核列表 */
			AuditListQuery query = new AuditListQuery();
			if(list.get(0).getBatchNo()==null){
                query.setTransId(fundoutOrderNo);
            }else{
                query.setTransId(list.get(0).getBatchNo());
            }
			auditList = auditService.queryAuditList(query);
			for (int i = 0; i < auditList.size(); i++) {
				audit = auditList.get(i);
				audit = auditService.queryAudit(audit.getId());
				Map<String, Object> auditData = JSONObject.parseObject(audit
						.getAuditData());
				if(auditData!=null){
    				Map<String, Object> tradeReqestMap = (Map<String, Object>) auditData
    						.get("tradeReqest");
    				data.put("sendMessage", tradeReqestMap.get("sendMessage"));//是否短信通知
				}
				data.put("applyTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//申请时间
				data.put("operatorName", audit.getOperatorName());// 申请操作员名称
				if(audit.getGmtModified() != null) {
					data.put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtModified()));// 更新时间
					data.put("auditorName", audit.getAuditorName());// 审核员名称
				}else{
					data.put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//更新时间
					data.put("auditorName", audit.getOperatorName());// 申请操作员名称
				}
				data.put("remark", audit.getRemark());// 审核说明
				data.put("status", audit.getStatus());// 审核结果
			}
		}else if(tradeType.equals("03")){
			FundoutQuery fundoutQuery = new FundoutQuery();
			fundoutQuery.setMemberId(user.getMemberId());
			fundoutQuery.setAccountNo(user.getDefaultAccountId());
			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));

			fundoutQuery.setFundoutOrderNo(fundoutOrderNo);// 页面传来的交易订单号
			fundoutQuery.setProductCode(TradeType.PAYOFF_TO_BANK.getBizProductCode());// 代发工资字符码
			fundoutQuery.setOrderTimeStart(DateUtil.getDateNearCurrent(-366));

			fundoutQuery.setOrderTimeEnd(DateUtil.addMinutes(new Date(), 30));
		    page = defaultFundoutService.queryFundoutInfo(
					fundoutQuery, env);
			List<Fundout> list = page.getInfos();
			data.put("payeeNo", list.get(0).getBranchName()+""+list.get(0).getCardNo());// 设置收款账号
			data.put("payeeAccountName",list.get(0).getName() );// 设置收款账户名称
			double sum = 0;
			for (Fundout s : list) {
				sum = s.getAmount().getAmount().doubleValue()
						+ s.getFee().getAmount().doubleValue();
			}
			DecimalFormat df = new DecimalFormat("##0.00");
			data.put("sum", df.format(sum));
			/* 查询审核列表 */
			AuditListQuery query = new AuditListQuery();
			if(list.get(0).getBatchOrderNo()==null){
	            query.setTransId(fundoutOrderNo);
	        }else{
	            query.setTransId(list.get(0).getBatchOrderNo());
	        }
			auditList = auditService.queryAuditList(query);
			for (int i = 0; i < auditList.size(); i++) {
				audit = auditList.get(i);
				audit = auditService.queryAudit(audit.getId());
				Map<String, Object> auditData = JSONObject.parseObject(audit
						.getAuditData());
				if(auditData!=null){
    				Map<String, Object> tradeReqestMap = (Map<String, Object>) auditData
    						.get("tradeReqest");
    				data.put("sendMessage", tradeReqestMap.get("sendMessage"));//是否短信通知
				}
				data.put("applyTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//申请时间
				data.put("operatorName", audit.getOperatorName());// 申请操作员名称
				if(audit.getGmtModified() != null) {
					data.put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtModified()));// 更新时间
					data.put("auditorName", audit.getAuditorName());// 审核员名称
				}else{
					data.put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//更新时间
					data.put("auditorName", audit.getOperatorName());// 申请操作员名称
				}
				data.put("remark", audit.getRemark());// 审核说明
				data.put("status", audit.getStatus());// 审核结果
			}
		}
		data.put("auditList", auditList);
		data.put("page", page);
		data.put("mobile", user.getMobileStar());
		data.put("member", member);
		data.put("tradeType", tradeType);
		restP.setData(data);
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/PayrollDetail", "response", restP);
	}

	/**
	 * 转账到永达互联网金融账户详情
	 */
	@RequestMapping(value = "/my/TransferKJTDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchTransferKJTDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env,HttpSession session) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		String tradeType = request.getParameter("tradeType");
		String flag = request.getParameter("flag");
		String orderId = request.getParameter("orderId");
		PageResultList page1 = new PageResultList();
		List<Audit> auditList = new ArrayList<Audit>();
		Audit audit = new Audit();
	
		DownloadBillRequest reqInfo = new DownloadBillRequest();
		reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-366));
		reqInfo.setEndTime(DateUtil.addMinutes(new Date(), 30));

		reqInfo.setMemberId(user.getMemberId());
		QueryBase queryBase4 = new QueryBase();
		queryBase4.setCurrentPage(1);
		reqInfo.setQueryBase(queryBase4);
		reqInfo.setTradeVoucherNo(orderId);// 通过转账订单号查询
		page1 = defaultDownloadBillService.queryTransfer(reqInfo, env);
		List<TransferInfo> list=page1.getInfos();
		this.setJsonAttribute(session, "TransferInfoList", list.get(0));
		double sum=0;
		for(TransferInfo s:list){
			sum=s.getTransferAmount().getAmount().doubleValue()+s.getPayerFee().getAmount().doubleValue();
		}
		DecimalFormat df = new DecimalFormat("##0.00");
		restP.getData().put("sum", df.format(sum));
		String buyAccountType="";
		String selAccountType="";
		String[] accountType=new String[2];
		if(list.get(0).getExtention() !=null)
		{
			Map<String, Object> data = JSONObject.parseObject(list.get(0).getExtention());//获取
			String extention=(String)data.get("account_type");
			if(extention !=null){
				accountType=extention.split(",");
				buyAccountType=MemberType.getMessage(new Long(accountType[0]));
	            selAccountType=MemberType.getMessage(new Long(accountType[1]));
			}
		}
		BaseMember baseMember=new BaseMember();
		if(list.get(0).getPlamId().equals("1")){
            baseMember=memberService.queryMemberById(list.get(0).getBuyId(),env);
        }else {
            baseMember=memberService.queryMemberById(list.get(0).getSellId(),env);
        }
        restP.getData().put("selAccountType", selAccountType);
        restP.getData().put("buyAccountType", buyAccountType);
        restP.getData().put("loginName", baseMember.getLoginName());
        restP.getData().put("loginName2", user.getLoginName());
		/* 查询审核列表 */
		AuditListQuery query = new AuditListQuery();
		if(list.get(0).getBatchNo()==null){
		    query.setTransId(orderId);
        }else{
            query.setTransId(list.get(0).getBatchNo());
        }
		auditList = auditService.queryAuditList(query);
		if((auditList!=null) && (auditList.size()!=0)){
			this.setJsonAttribute(session, "auditList", auditList.get(0));
			for (int i = 0; i < auditList.size(); i++) {
				audit = auditList.get(i);
				audit = auditService.queryAudit(audit.getId());
				Map<String, Object> auditData = JSONObject.parseObject(audit
						.getAuditData());
				if(auditData!=null){
				    Map<String, Object> tradeReqestMap = (Map<String, Object>) auditData
	                        .get("tradeReqest");
					// 增加空值判断，批量和单笔审核扩展信息结构并不一致
					if (tradeReqestMap != null) {
						restP.getData().put("sendMessage", tradeReqestMap.get("sendMessage"));// 是否短信通知
					}
				}
				restP.getData().put("applyTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//申请时间
				restP.getData().put("operatorName", audit.getOperatorName());// 申请操作员名称
				if(audit.getGmtModified() != null) {
					restP.getData().put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtModified()));// 更新时间
					restP.getData().put("auditorName", audit.getAuditorName());// 审核员名称
				}else{
					restP.getData().put("gmtModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));//更新时间
					restP.getData().put("auditorName", audit.getOperatorName());// 申请操作员名称
				}
				restP.getData().put("remark", audit.getRemark());// 审核说明
				restP.getData().put("status", audit.getStatus());// 审核结果
			}
		}
		restP.getData().put("flag", flag);
		restP.getData().put("auditList", auditList);
		restP.getData().put("page", page1);
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("tradeType", tradeType);
		restP.getData().put("pageReqMapping", "/my/all-transfer.htm");
		// restP.getData().put("txnType", txnType);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/TransferDetail_kjt", "response", restP);
	}
	
	/**
	 * 转账到银行账户详情
	 */
	@RequestMapping(value = "/my/TransferBankDetail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchTransferBankDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env,HttpSession session) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		String tradeType = request.getParameter("tradeType");
		String orderId = request.getParameter("orderId");
		PageResultList page1 = new PageResultList();
		List<Audit> auditList = new ArrayList<Audit>();
		Audit audit = new Audit();
		FundoutQuery fundoutQuery4 = new FundoutQuery();
		// fundoutQuery4.setProductCode(TradeType.PAY_TO_BANK
		// .getBizProductCode()
		// + ","
		// + TradeType.PAY_TO_BANK_INSTANT.getBizProductCode());// 设置字符码
		fundoutQuery4.setMemberId(user.getMemberId());
		fundoutQuery4.setAccountNo(user.getDefaultAccountId());
		fundoutQuery4.setCurrentPage(1);
		fundoutQuery4.setOrderTimeStart(DateUtil.getDateNearCurrent(-366));

		fundoutQuery4.setOrderTimeEnd(DateUtil.addMinutes(new Date(), 30));
		fundoutQuery4.setFundoutOrderNo(orderId);// 通过转账订单号查询
		page1 = defaultFundoutService.queryFundoutInfo(fundoutQuery4, env);
		List<Fundout> list = page1.getInfos();
		this.setJsonAttribute(session, "FundoutList", list.get(0));//
		restP.getData().put("payeeNo", list.get(0).getBranchName()+""+list.get(0).getCardNo());// 设置收款账号
		restP.getData().put("payeeAccountName",list.get(0).getName() );// 设置收款账户名称
		double sum = 0;
		for (Fundout s : list) {
			sum = s.getAmount().getAmount().doubleValue()
					+ s.getFee().getAmount().doubleValue();
			restP.getData().put("cardNo", s.getCardNo());
		}
		DecimalFormat df = new DecimalFormat("##0.00");
		restP.getData().put("sum", df.format(sum));
		
		if (tradeType.equals("bankRefund")) {
			// 查询退票信息
			PageResultList page8 = new PageResultList();
			FundoutQuery fundoutQuery8 = new FundoutQuery();
			fundoutQuery8.setFundoutOrderNo(orderId);
			fundoutQuery8.setMemberId(user.getMemberId());
			fundoutQuery8.setStatus(FundoutStatus.SUCCESS.getCode());
			// fundoutQuery8.setHasOutOrderNo(true);
			fundoutQuery8.setCurrentPage(1);

			page8 = defaultFundoutService.queryFundoutOrderInfo(fundoutQuery8, env);
			Fundout out = (Fundout) page8.getInfos().get(0);

			restP.getData().put("refundTime", out.getRefundTime());

		}

		/* 查询审核列表 */
		AuditListQuery query = new AuditListQuery();
		if(list.get(0).getBatchOrderNo()==null){
            query.setTransId(orderId);
        }else{
            query.setTransId(list.get(0).getBatchOrderNo());
        }
		auditList = auditService.queryAuditList(query);
		if ((auditList != null) && (auditList.size() != 0)) {
			this.setJsonAttribute(session, "auditList", auditList.get(0));
			for (int i = 0; i < auditList.size(); i++) {
				audit = auditList.get(i);
				audit = auditService.queryAudit(audit.getId());
				Map<String, Object> auditData = JSONObject.parseObject(audit.getAuditData());
				if(auditData!=null){
    				Map<String, Object> tradeReqestMap = (Map<String, Object>) auditData.get("tradeReqest");
					// 增加空值判断，批量和单笔审核扩展信息结构并不一致
					if (tradeReqestMap != null) {
						restP.getData().put("sendMessage", tradeReqestMap.get("sendMessage"));// 是否短信通知
					}
				}
				restP.getData().put("applyTime",
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));// 申请时间
				restP.getData().put("operatorName", audit.getOperatorName());// 申请操作员名称
				if (audit.getGmtModified() != null) {
					restP.getData().put("gmtModified",
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtModified()));// 更新时间
					restP.getData().put("auditorName", audit.getAuditorName());// 审核员名称
				} else {
					restP.getData().put("gmtModified",
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));// 更新时间
					restP.getData().put("auditorName", audit.getOperatorName());// 申请操作员名称
				}
				restP.getData().put("remark", audit.getRemark());// 审核说明
				restP.getData().put("status", audit.getStatus());// 审核结果
			}
		}
		
		restP.getData().put("auditList", auditList);
		restP.getData().put("page", page1);
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("tradeType", tradeType);
		restP.getData().put("pageReqMapping", "/my/all-transfer.htm");
		// restP.getData().put("txnType", txnType);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/TransferDetail_bank", "response", restP);
	}
	
	/**
	 * 委托付款到永达互联网金融账户-详情
	 */
	@RequestMapping(value = "/my/fundscollectYD.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchFundscollectKjtDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env,HttpSession session) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		String orderId = request.getParameter("orderId");
		String tradeType = request.getParameter("tradeType");
		String flag = request.getParameter("flag");
		PageResultList page1 = new PageResultList();

		DownloadBillRequest reqInfo = new DownloadBillRequest();
		reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-366));
		reqInfo.setEndTime(DateUtil.addMinutes(new Date(), 30));

		reqInfo.setMemberId(user.getMemberId());
		QueryBase queryBase4 = new QueryBase();
		queryBase4.setCurrentPage(1);
		reqInfo.setQueryBase(queryBase4);
		reqInfo.setTradeVoucherNo(orderId);// 通过转账订单号查询
		page1 = defaultDownloadBillService.queryTransfer(reqInfo, env);
		List<TransferInfo> list=page1.getInfos();
		restP.getData().put("loginName2", user.getLoginName());
		BaseMember baseMember=new BaseMember();
		if(list.get(0).getPlamId().equals("1")){
			
			baseMember=memberService.queryMemberById(list.get(0).getBuyId(),env);
		}else {
			baseMember=memberService.queryMemberById(list.get(0).getSellId(),env);
		}
		this.setJsonAttribute(session, "TransferInfoList", list.get(0));//给电子回单提供数据
		Money sum=new Money();
		for(TransferInfo s:list){
			sum=s.getTransferAmount().add(s.getPayerFee());
		}
		DecimalFormat df = new DecimalFormat("##0.00");
		restP.getData().put("flag", flag);
		restP.getData().put("tradeType", tradeType);
		restP.getData().put("sum", df.format(sum.getAmount().doubleValue()));
		restP.getData().put("page", page1);
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("loginName", baseMember.getLoginName());
		
		restP.getData().put("pageReqMapping", "/my/fundscollectYD.htm");
		// restP.getData().put("txnType", txnType);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/fundscollect_ydzh", "response", restP);
	}
	/**
	 * 测试委托付款到永达互联网金融账户-详情
	 * @param request
	 * @param resp
	 * @param error
	 * @param model
	 * @param env
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/yptest.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchtest(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env,HttpSession session) throws Exception {
		
		   RestResponse restP = new RestResponse();
		 return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/fundscollect_ydzh", "response", restP);
	}
	/**
	 *   委托付款到银行卡-详情
	 */
	@RequestMapping(value = "/my/fundscollectbank.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchFundscollectBankDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env,HttpSession session) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);// 查询会员所以需要的信息//10、
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));// 查询会员所以需要的信息//11、
		String orderId = request.getParameter("fundoutOrderNo");//交易订单号
		String tradeType = request.getParameter("tradeType");
		String flag = request.getParameter("flag");
		PageResultList page1 = new PageResultList();
		FundoutQuery fundoutQuery4 = new FundoutQuery();
		fundoutQuery4.setProductCode(TradeType.COLLECT_TO_BANK.getBizProductCode());// 设置字符码
		fundoutQuery4.setMemberId(user.getMemberId());
		fundoutQuery4.setAccountNo(user.getDefaultAccountId());
		fundoutQuery4.setCurrentPage(1);
		fundoutQuery4.setOrderTimeStart(DateUtil.getDateNearCurrent(-366));
		fundoutQuery4.setOrderTimeEnd(DateUtil.addMinutes(new Date(), 30));
		fundoutQuery4.setFundoutOrderNo(orderId);// 通过转账订单号查询
		page1 = defaultFundoutService.queryFundoutInfo(fundoutQuery4, env);
		List<Fundout> list = page1.getInfos();
       	this.setJsonAttribute(session, "FundoutList", list.get(0));//给电子回单提供数据
		double sum = 0;
		for (Fundout s : list) {
			sum = s.getAmount().getAmount().doubleValue()
					+ s.getFee().getAmount().doubleValue();
			restP.getData().put("cardNo", s.getCardNo());
		}
		DecimalFormat df = new DecimalFormat("##0.00");
		restP.getData().put("flag", flag);
		restP.getData().put("tradeType", tradeType);
		restP.getData().put("sum", df.format(sum));
		restP.getData().put("page", page1);
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("loginName2", user.getLoginName());
		restP.getData().put("pageReqMapping", "/my/all-transfer.htm");
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/fundscollect_bank", "response", restP);
	}
	/*
     * 打印电子回单
     * */
	@RequestMapping("/print.htm")
	public ModelAndView Print(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
		ModelAndView mv = new ModelAndView();
		String tradeType = request.getParameter("tradeType");// 1
		mv.addObject("tradeType", tradeType);
		String flag = request.getParameter("flag");// 1
		mv.addObject("flag", flag);
		mv.setViewName(CommonConstant.URL_PREFIX + "/tradeUnstyled/TransferInformationPrint");
		return mv;
	}
	
	/*
	 * 下载电子回单
	 * */
	@RequestMapping("/downImage.htm")
	public void downImage(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
		response.setContentType("application/x-download");
		response.setHeader("content-disposition", "attachment;filename=" +"kjt" +YmdHmsNoSign.format(new Date())+".jpg");
		OutputStream fOut = null;
		try {  
			fOut = response.getOutputStream();
			String tradeType = request.getParameter("tradeType");
			BufferedImage buffImg=null;
			if(tradeType.equals("1")){
				buffImg=createImg_kjt(request);//到账户
			}else if(tradeType.equals("2")){
				buffImg=createImg_bank(request);//到银行卡
			}
			ImageIO.write(buffImg, "jpg", fOut);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				fOut.flush();
				fOut.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	/**
	 * 查看电子回单
	 * 
	 * */
	@RequestMapping("/SingleReceipt.htm")
	public void handleRequest(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
		try {
			String tradeType = request.getParameter("tradeType");
			ServletOutputStream out = response.getOutputStream();
			BufferedImage buffImg=null;
			if(tradeType.equals("1")){
				buffImg=createImg_kjt(request);//到账户
			}else if(tradeType.equals("2")){
				buffImg=createImg_bank(request);//到银行卡
			}
			// 创键编码器，用于编码内存中的图象数据。
			ImageIO.write(buffImg, "jpg", out);
			try {
				out.flush();
			} finally {
				out.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	/**
	 * 处理一行太长，分成多行显示
	 * str:待处理的字符
	 * x：对应图片的横坐标
	 * y:对应图片的纵坐标
	 * g：画笔对象
	 * row：每行显示多少字
	 * n:行距
	 * */
	public void newlines(String str,int x,int y,Graphics g,int row,int n)
	{
		int lenString=str.length();
        for (int i = 0; i <= (lenString/row); i++) {
        	if(((i+1)*row)<lenString)
        	{
        		g.drawString(str.substring(0+(i*row), (i+1)*row),x, y+(n*i));
        	}else {
        		g.drawString(str.substring(0+(i*row), lenString),x, y+(n*i));
			}
		}
	}
	public BufferedImage createImg_bank(HttpServletRequest request) {
		EnterpriseMember user = getUser(request);// 8
		String sign = request.getParameter("sign");
		String flagNew = request.getParameter("flag");
		BufferedImage buffImg = null;
		try {
			HttpSession session = request.getSession();
			String filePath = request.getSession().getServletContext().getRealPath("/") + "/view/main/static";
			FileInputStream is = new FileInputStream(filePath+"/dzd.jpg");//v1219注释
			buffImg = ImageIO.read(is);
			is.close();
			// 得到画笔对象
			Graphics g = buffImg.getGraphics();
			// 设置颜色。
			g.setColor(Color.BLACK);
			// 最后一个参数用来设置字体的大小
			Font textFont = new Font("宋体", Font.PLAIN, 12);
			g.setFont(textFont);
			//v1219注释
			Fundout FundoutList = this.getJsonAttribute(session, "FundoutList", Fundout.class);
			BaseMember BuyAccount=new BaseMember();
			BuyAccount=memberService.queryMemberById(user.getMemberId(),env);
			String fundoutOrderNo= FundoutList.getFundoutOrderNo()== null ? "" :FundoutList.getFundoutOrderNo();
			g.drawString(fundoutOrderNo, 125, 77);// 交易流水号
			g.drawString(DateUtil.getNewFormatDateString(FundoutList.getOrderTime()), 458, 77);//支付时间
			String memberName=  BuyAccount.getMemberName()== null ? "" : BuyAccount.getMemberName();
			newlines(memberName,142,108,g,14,20);//付款方名称
			g.drawString(BuyAccount.getLoginName()== null ? "" :BuyAccount.getLoginName(), 142, 150);//付款方账号
			if(sign.equals("trade"))
			{
				String name=FundoutList.getName()== null ? "" :FundoutList.getName();
				newlines(name,447,108,g,14,20);// 收款人姓名
				g.drawString(FundoutList.getCardNo()== null ? "" :FundoutList.getCardNo(), 447, 150);// 收款人账号
				String branchName=FundoutList.getBranchName()== null ? "" :FundoutList.getBranchName();
				newlines(branchName,447,200,g,14,18);// 收款人开户行
				g.drawString(digitUppercase(FundoutList.getAmount().getAmount().doubleValue()), 185, 271);// 金额（大写）
				g.drawString("￥" + FundoutList.getAmount(), 485, 271);// 金额（小写）
				if(flagNew.equals("1")){// 交易类型
					g.drawString("转账到银行账户", 144, 305);
				}else if(flagNew.equals("2")){
					g.drawString("资金归集到银行", 144, 305);
				}
				g.drawString(fundoutOrderNo+"-1", 478, 461);//电子回单号
				String memo=FundoutList.getPurpose()== null ? "" :FundoutList.getPurpose();
				newlines(memo,80,348,g,21,20);//备注
			}else if(sign.equals("fee")) {
				//fee
				g.drawString("永达互联网金融信息服务有限公司", 447, 108);// 收款人姓名
				g.drawString(digitUppercase(FundoutList.getFee().getAmount().doubleValue()), 185, 271);// 转账服务费（大写）
				g.drawString("￥" + FundoutList.getFee(), 485, 271);// 转账服务费（小写）
				if(flagNew.equals("1")){// 交易类型
					g.drawString("转账服务费", 144, 305);
				}else if(flagNew.equals("2")){
					g.drawString("资金归集服务费", 144, 305);
				}
				g.drawString(fundoutOrderNo+"-2", 478, 461);//电子回单号
				g.drawString("服务费", 80, 348);//备注
			}
			// 2.jpg是你的小图片的路径
//			ImageIcon imgIcon = new ImageIcon(filePath+"/zhang.png");//A1219取消注释
			// 得到Image对象。
//			Image img = imgIcon.getImage();//A1219取消注释
			// 将小图片绘到大图片上。
			// 5,300 .表示你的小图片在大图片上的位置。
//			g.drawImage(img, 365, 205, null);//A1219取消注释
			g.dispose();
			return buffImg;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return buffImg;
	}
	
	 
	public BufferedImage createImg_kjt(HttpServletRequest request) {
		BufferedImage buffImg = null;
		try {
			String sign = request.getParameter("sign");
			String flagNew = request.getParameter("flag");
			HttpSession session = request.getSession();
			String filePath = request.getSession().getServletContext().getRealPath("/") + "/view/main/static";
			FileInputStream is = new FileInputStream(filePath+"/dzd.jpg");
			buffImg = ImageIO.read(is);
			is.close();
			// 得到画笔对象
			Graphics g = buffImg.getGraphics();
			// 设置颜色。
			g.setColor(Color.BLACK);
			// 最后一个参数用来设置字体的大小
			Font textFont = new Font("宋体", Font.PLAIN, 12);
			g.setFont(textFont);
			// 生成回执单号
			TransferInfo TransferInfoList = this.getJsonAttribute(session, "TransferInfoList", TransferInfo.class);
			BaseMember BuyAccount=new BaseMember();
			BaseMember SellAccount=new BaseMember();
			BuyAccount=memberService.queryMemberById(TransferInfoList.getBuyId(),env);
			SellAccount=memberService.queryMemberById(TransferInfoList.getSellId(),env);
			String buyAccountType="";
			String selAccountType="";
			if(flagNew.equals("3") ||flagNew.equals("4"))//保理放贷/保理代扣
			{
				String[] accountType=new String[2];
				if(TransferInfoList.getExtention() !=null)
				{
					Map<String, Object> data = JSONObject.parseObject(TransferInfoList.getExtention());//获取
					String extention=(String)data.get("account_type");
					if(extention !=null){
						accountType=extention.split(",");
						buyAccountType=MemberType.getMessage(new Long(accountType[0]));
	                    selAccountType=MemberType.getMessage(new Long(accountType[1]));
					}
				}
			}
			String orderId=TransferInfoList.getOrderId()== null ? "" :TransferInfoList.getOrderId();
			g.drawString(orderId, 125, 77);// 交易流水号
			g.drawString(DateUtil.getNewFormatDateString(TransferInfoList.getTransferTime()), 458, 77);// 创建时间
			if(sign.equals("trade")){
				String buyerName=TransferInfoList.getBuyerName() == null ? "" : TransferInfoList.getBuyerName();
				newlines(buyerName,142,108,g,14,20);// 付款方名称
				g.drawString(BuyAccount.getLoginName()== null ? "" :BuyAccount.getLoginName() , 142, 150);//付款方账号
				if(flagNew.equals("3") ||flagNew.equals("4"))//保理放贷/保理代扣
				{
					g.drawString(buyAccountType , 142, 170);//付款方账号类型
					g.drawString(selAccountType , 447, 170);//付款方账号类型
				}
				String sellerName=TransferInfoList.getSellerName() == null ? "" : TransferInfoList.getSellerName();
				newlines(sellerName,447,108,g,14,20);// 收款人名称
				g.drawString(SellAccount.getLoginName()== null ? "" :SellAccount.getLoginName(), 447, 150);// 收款人账号
				g.drawString(digitUppercase(TransferInfoList.getTransferAmount().getAmount().doubleValue()), 185, 271);// 金额大写
				g.drawString("￥" + TransferInfoList.getTransferAmount(), 485, 271);// 金额小写
				if(flagNew.equals("1")){// 交易类型
					g.drawString("转账到永达互联网金融账户", 144, 305);
				}else if(flagNew.equals("2")){
					g.drawString("资金归集到账户", 144, 305);
				}else if(flagNew.equals("3")){
					g.drawString("保理放贷", 144, 305);
				}else if(flagNew.equals("4")){
					g.drawString("保理代扣", 144, 305);
				}
				g.drawString(orderId+"-1", 478, 461);//电子回单号
				String tradeMemo= TransferInfoList.getTradeMemo()== null ? "" :TransferInfoList.getTradeMemo();
				newlines(tradeMemo,80,348,g,21,20);// 备注
			}else if(sign.equals("fee")) {
				g.drawString("永达互联网金融信息服务有限公司", 447, 108);// 收款人姓名
				if(TransferInfoList.getPlamId().equals("1"))//是卖家
				{
			        String sellerName=TransferInfoList.getSellerName() == null ? "" : TransferInfoList.getSellerName();
			        newlines(sellerName,142,108,g,14,20);// 付款方名称
					g.drawString(SellAccount.getLoginName()== null ? "" :SellAccount.getLoginName(), 142, 150);// 收款人账号
					g.drawString(selAccountType , 142, 170);//付款方账号类型
					g.drawString(digitUppercase(TransferInfoList.getPayeeFee().getAmount().doubleValue()), 185, 271);// 服务费（大写）
					g.drawString("￥" + TransferInfoList.getPayeeFee(), 485, 271);//
				}else {//是买家
			        String buyerName=TransferInfoList.getBuyerName() == null ? "" : TransferInfoList.getBuyerName();
			        newlines(buyerName,142,108,g,14,20);// 付款方名称
					g.drawString(BuyAccount.getLoginName()== null ? "" :BuyAccount.getLoginName() , 142, 150);//付款方账号
					g.drawString(buyAccountType , 142, 170);//付款方账号类型
					g.drawString(digitUppercase(TransferInfoList.getPayerFee().getAmount().doubleValue()), 185, 271);// 服务费（大写）
					g.drawString("￥" + TransferInfoList.getPayerFee(), 485, 271);//
				}
				if(flagNew.equals("1")){// 交易类型
					g.drawString("转账服务费", 144, 305);
				}else if(flagNew.equals("2")){
					g.drawString("资金归集服务费", 144, 305);
				}else if(flagNew.equals("3")){
					g.drawString("保理放贷服务费", 144, 305);
				}else if(flagNew.equals("4")){
					g.drawString("保理代扣服务费", 144, 305);
				}
				g.drawString(orderId+"-2", 478, 461);//电子回单号
				g.drawString("服务费", 80, 348);//备注
			}
			// this.setJsonAttribute(session, "tradeType", String.class);
			// 2.jpg是你的小图片的路径
//			ImageIcon imgIcon = new ImageIcon(filePath+"/zhang.png");
			// 得到Image对象。
//			Image img = imgIcon.getImage();
			// 将小图片绘到大图片上。
			// 5,300 .表示你的小图片在大图片上的位置。
//			g.drawImage(img, 365, 205, null);
			g.dispose();

			return buffImg;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return buffImg;
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
	 * (查看)银行卡代扣
	 * */
	@RequestMapping(value = "/my/Withholding.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchWithholdingDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		String tradeVoucherNo = request.getParameter("tradeVoucherNo");
		String currentPage = request.getParameter("currentPage");
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		QueryBase queryBase = new QueryBase();// 分页信息
		queryBase.setNeedQueryAll(true);
		queryBase.setCurrentPage(Integer.valueOf(currentPage));// currentPage

		CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
		breq.setQueryBase(queryBase);
		breq.setNeedSummary(true);// 需要汇总
		breq.setTradeVoucherNo(tradeVoucherNo);
		breq.setBeginTime(DateUtil.getDateNearCurrent(-366));
		breq.setEndTime(new Date());
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		EnterpriseMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、
		if (user.getMemberId() == null) {
			throw new IllegalAccessException("illegal user error!");
		}
		// 银行卡代扣详情
		CoTradeQueryResponse rep = tradeService.queryList(breq);
		if (rep != null) {
			List<BaseInfo> baseInfoList= rep.getBaseInfoList();
			restP.getData().put("list", baseInfoList);
			if(baseInfoList!= null && baseInfoList.size()>0){
				for (int j = 0; j < baseInfoList.size(); j++) {
					Map<String, Object> data = JSONObject.parseObject(baseInfoList.get(j).getExtention());
					String cardNo= data.get("card_no").toString();
					String userName= data.get("user_name").toString();
					String bankCode= data.get("bank_code").toString();
					String bankName="";
					BankType bankType=BankType.getByCode(bankCode);
					if (bankType !=null) {
						bankName=bankType.getMessage().split("\\|")[0];
					}else {
						bankName=bankCode;
					}
					restP.getData().put("userName", userName);
					restP.getData().put("cardNo", "******"+cardNo.substring(cardNo.length()-4, cardNo.length()));
					restP.getData().put("bankName", bankName);
				}
			}
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/withholding", "response", restP);
	}
}
