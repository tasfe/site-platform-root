package com.netfinworks.site.ext.service.facade.converter;


import com.netfinworks.deposit.api.domain.DepositDetailRequest;
import com.netfinworks.deposit.api.domain.DepositDetailResponse;
import com.netfinworks.fos.service.facade.domain.FundoutInfo;
import com.netfinworks.fos.service.facade.response.FundoutGetResponse;
import com.netfinworks.site.core.common.util.DateUtils;
import com.netfinworks.site.service.facade.model.Receipt;
import com.netfinworks.site.service.facade.request.ReceiptRequest;
import com.netfinworks.site.service.facade.response.ReceiptRespose;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;
import com.netfinworks.tradeservice.facade.response.TradeDetailQueryResponse;


/**
 * <p>电子对账单转换器</p>
 * @author zhangyun.m
 * @version $Id: RecepitConverter.java, v 0.1 2014年7月3日 上午11:23:58 zhangyun.m Exp $
 */
public class ReceiptConverter {
    
    public static TradeBasicQueryRequest convertReceiptRequset(ReceiptRequest req) {
        TradeBasicQueryRequest request = new TradeBasicQueryRequest();
       // request.setTradeVoucherNo(req.);
        
        return request;
        
    }
    
    public static ReceiptRespose convertTradeReceiptReponseObj(TradeDetailQueryResponse  rep){
        ReceiptRespose reponse = new ReceiptRespose();
        
        return reponse;
    }
    
    
    /**充值电子对账单请求转换
     * @param paymentVoucherNo
     * @return
     */
    public static DepositDetailRequest convertDepositReceiptRequset(String  paymentVoucherNo) {
        DepositDetailRequest request = new DepositDetailRequest();
        request.setPaymentVoucherNo(paymentVoucherNo);
        return request;
        
    }
    
    /**充值电子对账单响应转换
     * @param rep
     * @return
     */
    public static ReceiptRespose convertDepositReceiptReponseObj(DepositDetailResponse  rep){
        ReceiptRespose reponse = new ReceiptRespose();
        
        return reponse;
    }
    
    /**出款电子对账单响应转换
     * @param rep
     * @return
     */
    public static ReceiptRespose convertDepositReceiptFundoutObj(FundoutGetResponse  rep){
        
        FundoutInfo fInfo = rep.getFundoutInfo();
        
        ReceiptRespose reponse = new ReceiptRespose();
        
        Receipt receipt = new Receipt();
        //交易凭证号
        receipt.setTradeVoucherNo(fInfo.getFundoutOrderNo());
        //收款方信息
        receipt.setSellerBranchName(fInfo.getBankName()+fInfo.getBranchName());
        receipt.setSellerName(fInfo.getName());
        
        receipt.setSellerAccountNo(fInfo.getCardNo());
        receipt.setTradeAmount(fInfo.getAmount());
        //付款方账户
        receipt.setBuyerAccountNo(fInfo.getAccountNo());
        receipt.setBuyerId(fInfo.getMemberId());
       
        receipt.setAccountType(fInfo.getAccountType());
        
        receipt.setFeeAmount(fInfo.getFee());
        //交易时间
        String orderTime =  DateUtils.dateToString(fInfo.getOrderTime(), "MEDIUM");
        receipt.setGmtPaid(orderTime);
        
        receipt.setTradeMemo(fInfo.getPurpose());
        
        reponse.setReceipt(receipt);
        
        return reponse;
    }
    
    


}
