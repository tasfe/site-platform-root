package com.netfinworks.site.web.action.audit;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.batchservice.facade.model.query.PayResultDetail;
import com.netfinworks.batchservice.facade.model.query.ResultDetail;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.audit.AuditLog;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.info.WalletCheckInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditLogServiceImpl;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;
import com.netfinworks.site.ext.integration.batchQuery.BatchQuery;
import com.netfinworks.site.ext.integration.batchQuery.BatchQueryResponse;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.QueryDetailService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.constant.AuditSubType;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.ExcelUtil;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.site.web.util.TradeReqestUtil;
import com.netfinworks.tradeservice.facade.response.PaymentResponse;

/**
 * <p>
 * 转账到永达互联网金融账户
 * </p>
 * 
 * @author yangshihong
 * @version $Id: AuditAction.java, v 0.1 2014年5月21日 下午2:21:49 hdfs Exp $
 */
@Controller
public class TransferBatchMoreAction extends BaseAction {
	protected Logger                log = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "queryDetailService")
	private QueryDetailService queryDetailService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 
	
	@Resource(name = "auditService")
    private AuditServiceImpl auditService;
	
	private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@RequestMapping(value = "/my/transferKjtBatch.htm", method = {
        RequestMethod.POST, RequestMethod.GET })
    public ModelAndView transferKjtBatch(HttpServletRequest request,
            HttpServletResponse resp, OperationEnvironment env) throws Exception {
	    BatchQuery req = new BatchQuery();
	    RestResponse restP = new RestResponse();
	    Map<String, Object> data = initOcx();
        String batchNo = request.getParameter("batchNo");// 批次号
        String id = request.getParameter("id");
        String currentPage = request.getParameter("currentPage");
        if (StringUtils.isBlank(currentPage) || currentPage.equals("0")) {
            currentPage = "1";
        }
        
        req.setBatchNo(batchNo);
        req.setPageNum(Integer.parseInt(currentPage));
        req.setPageSize(50000);
        
        List<Map<String,String>> payResultList = null;
        
        //获得批次明细
        BatchQueryResponse queryResponse = queryDetailService.batchQueryDetail(req);
        
        if (queryResponse.getResultCode().equals("S")) {// 成功
            data.put("resultMessage", queryResponse.getResultMessage());
            data.put("totalSize", queryResponse.getTotalSize());
            
            PageResultList resultList = new PageResultList();
            
            List<ResultDetail> list = queryResponse.getResultDetails();
            
            BigDecimal allFee = new BigDecimal("0");
            if ((list != null) && (list.size() > 0)) {
                payResultList = new ArrayList<Map<String,String>>();
                for (int i = 0; i < list.size(); i++) {// 目前只能强转了,稍后和维金沟通如何获取两个批量明细的子类
                    Map<String,String> detail = new HashMap<String,String>();
                    PayResultDetail pay = (PayResultDetail) list.get(i);
                    detail.put("tradeVoucherNo", pay.getTradeVoucherNo());
                    if((pay.getPayeeBankCode() == null) || pay.getPayeeBankCode().isEmpty()){
                        detail.put("payeeAccountNo", pay.getPayeeLoginId());
                        detail.put("payeeAccountInfo", getTargetAccountName(null, pay.getPayeeLoginId()));
                    }else{
                        detail.put("payeeAccountNo", CommonUtils.getMaskData(uesServiceClient.getDataByTicket(pay
                                .getPayeeBankAccountNo())));
                        detail.put("payeeAccountInfo", pay.getPayeeBankName() + "\n" + pay.getPayeeBranchName());
                    }
                    detail.put("amount", pay.getAmount().toString());
                    Map<String, Object> fee = JSONObject.parseObject(pay.getExtension());
                    if(fee.get("fee") != null){
                        detail.put("fee", (String)fee.get("fee"));
                        allFee = allFee.add(new BigDecimal((String)fee.get("fee")));
                    }
                    
                    detail.put("memo", pay.getMemo());
                    detail.put("status", pay.getStatus());
                    detail.put("sourceDetailNo", pay.getSourceDetailNo());//商户订单号
                    payResultList.add(detail);
                }
                resultList.setInfos(payResultList);// list
            }
            data.put("page", resultList);
            data.put("batchNo", batchNo);
            data.put("id", id);
        }
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX
                + "/tradeUnstyled/transfer_BatchDetailMore", "response", restP);
	}
	
	/**
	 * 转账审核查看详情查看更多记录导出
	 * */
	@RequestMapping(value = "/my/transferBatchMore.htm", method = {
        RequestMethod.POST, RequestMethod.GET })
    public void transferBatchMore(HttpServletRequest request,
            HttpServletResponse resp, OperationEnvironment env) throws Exception {
        BatchQuery req = new BatchQuery();
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        String batchNo = request.getParameter("batchNo");// 批次号
        String id = request.getParameter("id");
        String currentPage = request.getParameter("currentPage");
        if (StringUtils.isBlank(currentPage) || currentPage.equals("0")) {
            currentPage = "1";
        }
        
        req.setBatchNo(batchNo);
        req.setPageNum(Integer.parseInt(currentPage));
        req.setPageSize(50000);
        
        List<Map<String,String>> payResultList = null;
        
        Audit audit = auditService.queryAudit(id);
        String createTime = sf.format(audit.getGmtCreated());
        //获得批次明细
        BatchQueryResponse queryResponse = queryDetailService.batchQueryDetail(req);
        PageResultList resultList = new PageResultList();
        if (queryResponse.getResultCode().equals("S")) {// 成功
            data.put("resultMessage", queryResponse.getResultMessage());
            data.put("totalSize", queryResponse.getTotalSize());
            
            List<ResultDetail> list = queryResponse.getResultDetails();
            
            BigDecimal allFee = new BigDecimal("0");
            if ((list != null) && (list.size() > 0)) {
                payResultList = new ArrayList<Map<String,String>>();
                for (int i = 0; i < list.size(); i++) {// 目前只能强转了,稍后和维金沟通如何获取两个批量明细的子类
                    Map<String,String> detail = new HashMap<String,String>();
                    PayResultDetail pay = (PayResultDetail) list.get(i);
                    detail.put("tradeVoucherNo", pay.getTradeVoucherNo());
                    if((pay.getPayeeBankCode() == null) || pay.getPayeeBankCode().isEmpty()){
                        detail.put("payeeAccountNo", pay.getPayeeLoginId());
                        detail.put("payeeAccountInfo", getTargetAccountName(null, pay.getPayeeLoginId()));
                    }else{
                        detail.put("payeeAccountNo", CommonUtils.getMaskData(uesServiceClient.getDataByTicket(pay
                                .getPayeeBankAccountNo())));
                        detail.put("payeeAccountInfo", pay.getPayeeBankName() + "\n" + pay.getPayeeBranchName());
                    }
                    detail.put("amount", pay.getAmount().toString());
                    Map<String, Object> fee = JSONObject.parseObject(pay.getExtension());
                    if(fee.get("fee") != null){
                        detail.put("fee", (String)fee.get("fee"));
                        allFee = allFee.add(new BigDecimal((String)fee.get("fee")));
                    }
                    detail.put("batchNo", pay.getBatchNo());//批次号
                    detail.put("sourceDetailNo", pay.getSourceDetailNo());//商户订单号
                    detail.put("sourceBatchNo", pay.getSourceBatchNo());//商户批次号
                    detail.put("gmtCreated", createTime);//创建时间
                    detail.put("operatorName", audit.getOperatorName());//申请操作员
                    detail.put("auditorName", audit.getAuditorName());//审核操作员
                    detail.put("memo", pay.getMemo());
                    detail.put("status", pay.getStatus());
                    
                    payResultList.add(detail);
                }
                resultList.setInfos(payResultList);// list
            }
        }
        ExcelUtil excelUtil = new ExcelUtil();
        excelUtil.toExcel4(request, resp,payResultList, 1, createTime, sf.format(new Date()));
    }
}
