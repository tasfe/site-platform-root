package com.netfinworks.site.web.action.trade;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.dal.daointerface.AuditDAO;
import com.netfinworks.site.core.dal.dataobject.AuditDO;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRefundRequset;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AccessChannel;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.convert.AuditConvert;
import com.netfinworks.site.domainservice.convert.RefundConvertor;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.QueryTradeForm;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.tradeservice.facade.response.RefundResponse;

/**
 * @ClassName:PosRefundAction
 * @Description:pos退款服务
 * @author yp
 * @date 2016年10月10日
 */
@Controller
public class PosRefundAction extends BaseAction {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private SimpleDateFormat sdfhms = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Resource(name = "defaultTradeQueryService")
	private DefaultTradeQueryService defaultTradeQueryService;
	
	@Resource(name = "auditDAO")
    private AuditDAO auditDAO;
	
	@Resource(name = "tradeService")
	private TradeService tradeService;
	
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
	@Resource(name = "voucherService")
	private VoucherService voucherService;
	
	@Resource(name = "auditService") 
	private AuditServiceImpl auditService;

	/**
	 * pos退款异步查询，确认是否可退款
	 * 
	 * */
	@ResponseBody
	@RequestMapping(value = "/my/pos/getIsRefund.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public RestResponse posGetIsRefund(HttpServletRequest request,HttpSession session,
			String tradeVoucherNo) throws Exception  {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);
		}
		Date date=new Date();
		EnterpriseMember user = getUser(request);
		if (user.getMemberId() == null) {
			try {
				throw new IllegalAccessException("illegal user error!");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		//查询渠道是否存在退款接口
		if(tradeService.queryIfExistsRefundApi(tradeVoucherNo, env)){
			restP.setSuccess(true);
			data.put("isrefundApi", false);
			restP.setData(data);
			return restP;
		}
		// 统计已退款笔数和金额
		TradeRequest tradeRequestSum = new TradeRequest();
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
			e3.printStackTrace();
		}
		// 通过所有数据获取退款笔数和已退款金额
		//不可退金额包括审核中金额，处理中金额和退款成功金额
		Money noRefundMoney=new Money();
		List<TradeInfo> refundTradeList = page5.getInfos();
		if((refundTradeList!=null) && (refundTradeList.size()>0)){
			for (int i = 0; i < refundTradeList.size(); i++){
				if(refundTradeList.get(i).getStatus().equals("900") || refundTradeList.get(i).getStatus().equals("901") 
						|| refundTradeList.get(i).getStatus().equals("951")){
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
		if (AuditList.size() > 0){
        	for (int i = 0; i < AuditList.size(); i++) {
        		if(AuditList.get(i).getStatus().equals("1")){
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
		// 网关交易详情
		CoTradeQueryResponse tradeQueryResponse=new CoTradeQueryResponse();
		try {
			tradeQueryResponse = tradeService.queryList(breq);
		} catch (BizException e) {
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
		restP.setSuccess(true);
		data.put("maxRefundMoney", maxRefundMoney);
		data.put("noRefundMoney", noRefundMoney);
		data.put("isrefund", isrefund);
		restP.setData(data);
		return restP;
	}
	/**
	 * 第一步：pos交易-退款--获取交易订单号
	 */
	@RequestMapping(value = "/my/pos/RefundApply.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView posRefundApply(HttpServletRequest request,
			HttpServletResponse resp, HttpSession session, boolean error,
			ModelMap model, OperationEnvironment env) throws Exception {
		ModelAndView mv = new ModelAndView();
		BaseInfo baseInfoList = this.getJsonAttribute(session, "baseInfoList",
				BaseInfo.class);
		String tradeVoucherNo = baseInfoList.getTradeVoucherNo();
		Date date = new Date();
		// 统计已退款笔数和金额
		TradeRequest tradeRequestSum = new TradeRequest();
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
			e3.printStackTrace();
		}
		// 通过所有数据获取退款笔数和已退款金额
		//不可退金额包括审核中金额，处理中金额和退款成功金额
		Money noRefundMoney=new Money();
		List<TradeInfo> refundTradeList = page5.getInfos();
		if((refundTradeList!=null) && (refundTradeList.size()>0)){
			for (int i = 0; i < refundTradeList.size(); i++){
				if(refundTradeList.get(i).getStatus().equals("900") || refundTradeList.get(i).getStatus().equals("901") 
						|| refundTradeList.get(i).getStatus().equals("951")){
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
		if (AuditList.size() > 0){
        	for (int i = 0; i < AuditList.size(); i++) {
        		if(AuditList.get(i).getStatus().equals("1")){
        			noRefundMoney.addTo(AuditList.get(i).getAmount());
        		}
			}
        }
		Money maxRefundMoney = baseInfoList.getPayAmount().subtract(noRefundMoney);
		if ((maxRefundMoney == null) || (maxRefundMoney.compareTo(new Money("0")) == -1)) {
			maxRefundMoney.setAmount("0");
		}
		mv.addObject("maxRefundMoney", maxRefundMoney);
		mv.addObject("baseInfoList", baseInfoList);
		mv.addObject("noRefundMoney", noRefundMoney);
		mv.setViewName(CommonConstant.URL_PREFIX + "/trade/posRefundApply");
		return mv;
	}
	
	/**
	 * 第二步：POS交易退款--获取交易订单号，上一步退款金额和备注+显示的退款服务费需要单独接口的计算！！！
	 * */
	@RequestMapping(value = "/my/pos/RefundApplyPay.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView posRefundApplyPay(HttpServletRequest request,
			HttpSession session, HttpServletResponse resp, boolean error,
			ModelMap model, TradeEnvironment env, QueryTradeForm form)
			throws Exception {
		ModelAndView mv = new ModelAndView();
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
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
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			mv.addObject("isCertActive", "yes");
		} else {
			mv.addObject("isCertActive", "no");
		}
		if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {//EW_MY_APPROVE-EW_REFUND_AUDIT
			// 有退款审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/posRefundApplyPay");
		} else {
			// 无退款审核权限RefundPay-Apply.vm
			mv.setViewName(CommonConstant.URL_PREFIX + "/trade/posRefundPay-Apply");
		}
		return mv;
	}
	
	
}
