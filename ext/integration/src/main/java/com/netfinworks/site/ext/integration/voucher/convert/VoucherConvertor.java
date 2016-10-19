/**
 *
 */
package com.netfinworks.site.ext.integration.voucher.convert;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.voucher.common.utils.JsonUtils;
import com.netfinworks.voucher.service.facade.domain.access.WebAccessInfo;
import com.netfinworks.voucher.service.facade.domain.enums.AccessInfoType;
import com.netfinworks.voucher.service.facade.domain.voucher.PaymentVoucherInfo;
import com.netfinworks.voucher.service.facade.domain.voucher.SimpleOrderVoucherInfo;
import com.netfinworks.voucher.service.facade.request.RecordingRequest;

/**
 * <p>凭证转换器</p>
 * @author fjl
 * @version $Id: VoucherConverter.java, v 0.1 2013-11-28 下午3:52:00 fjl Exp $
 */
public class VoucherConvertor {

    /**
     * 交易请求转换成凭证请求对象
     * @param req
     * @return
     */
    public static RecordingRequest convertTradeReq(TradeRequestInfo req) {
        RecordingRequest request = new RecordingRequest();

        SimpleOrderVoucherInfo info = convertVocher(req);
        request.setVoucherType(SimpleOrderVoucherInfo.voucherType());
        request.setVoucher(JsonUtils.toJson(info));
        return request;
    }

    private static SimpleOrderVoucherInfo convertVocher(TradeRequestInfo req) {
        SimpleOrderVoucherInfo info = new SimpleOrderVoucherInfo();
        info.setAccessType(AccessInfoType.WEB.getCode());
        info.setAccess(convertAccessToJson(req));
        info.setAmount(req.getAmount());
        if(info.getAmount() == null){
            info.setAmount(new Money());
        }
        PartyInfo payer = req.getPayer();
        if (payer != null) {
            info.setPayerId(payer.getMemberId());
        }
        PartyInfo peyee = req.getPayee();
        if (peyee != null) {
            info.setPayeeId(peyee.getMemberId());
        }
        info.setProductCode(req.getTradeType().getBizProductCode());

        info.setRequestTime(new Date());
        info.setSource(CommonConstant.SOURCE_CODE);
        if(StringUtils.isBlank(req.getTradeSourceVoucherNo())){
            req.setTradeSourceVoucherNo(genRequestNo());
        }
        info.setSourceVoucherNo(req.getTradeSourceVoucherNo());
        info.setDescription(req.getMemo());
        return info;
    }
    
    private static String convertAccessToJson(TradeRequestInfo req){
        WebAccessInfo info = new WebAccessInfo();
        info.setBrowser(req.getBrowser());
        info.setBrowserVersion(req.getBrowserVersion());
        info.setMac(req.getClientMac());
        info.setRequestIp(req.getClientIp());
        //if (!TradeType.RECHARGE.getBizProductCode().equals(req.getTradeType().getBizProductCode())) {
        	// 充值服务不传入returnURL，采用收银台成功页面
        	info.setReturnUrl(req.getSuccessDispalyUrl());
        //}
        
        return JsonUtils.toJson(info);
    }
    
    //TODO 从统一凭证中获取一批
    private static String genRequestNo(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    
    public static RecordingRequest convertPaymentReq(TradeRequestInfo req) {
        RecordingRequest request = new RecordingRequest();

        PaymentVoucherInfo info = convertPaymentVocher(req);
        request.setVoucherType(PaymentVoucherInfo.voucherType());
        request.setVoucher(JsonUtils.toJson(info));
        return request;
    }
    
    private static PaymentVoucherInfo convertPaymentVocher(TradeRequestInfo req) {
        PaymentVoucherInfo info = new PaymentVoucherInfo();
        info.setAccessType(AccessInfoType.WEB.getCode());
        info.setAccess(convertAccessToJson(req));
        info.setProductCode(req.getTradeType().getBizProductCode());
        info.setSource(req.getClientId());
        info.setSourceVoucherNo(req.getTradeSourceVoucherNo());
        info.setRelatedSourceVoucherNo(req.getTradeSourceVoucherNo());
        info.setPaymentDetail(req.getTradeExtension());
        return info;
    }
    
}
