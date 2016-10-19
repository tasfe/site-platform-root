/**
 * 
 */
package com.netfinworks.site.ext.integration.voucher.impl;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.meidusa.fastjson.JSON;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.ext.integration.voucher.convert.VoucherConvertor;
import com.netfinworks.voucher.service.facade.VoucherFacade;
import com.netfinworks.voucher.service.facade.domain.access.WebAccessInfo;
import com.netfinworks.voucher.service.facade.domain.enums.ControlType;
import com.netfinworks.voucher.service.facade.domain.voucher.ControlVoucherInfo;
import com.netfinworks.voucher.service.facade.request.RecordingRequest;
import com.netfinworks.voucher.service.facade.request.RegisterRequest;
import com.netfinworks.voucher.service.facade.response.RecordingResponse;
import com.netfinworks.voucher.service.facade.response.RegisterResult;

/**
 * <p>凭证服务</p>
 * @author fjl
 * @version $Id: VoucherService.java, v 0.1 2013-11-28 上午11:14:17 fjl Exp $
 */
@Service("voucherService")
public class VoucherServiceImpl implements VoucherService {
    private Logger         logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private VoucherFacade voucherFacade;
    
    @Override
    public String recordTradeVoucher(TradeRequestInfo req) throws BizException{
        try {
            validate(req);
            
            long beginTime = 0L;
            RecordingRequest request = VoucherConvertor.convertTradeReq(req);
            if (logger.isInfoEnabled()) {
                logger.info("落地交易统一凭证，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            RecordingResponse resp = voucherFacade.record(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程落地交易统一凭证， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, resp });
            }
            if(ResponseCode.INVOK_SUCCESS.getCode().equals(resp.getReturnCode())){
                return resp.getVoucherNo();
            }else{
                throw new BizException(ErrorCode.SYSTEM_ERROR,resp.getReturnMessage());
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("落地交易统一凭证异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
    
    @Override
    public String recordPaymentVoucher(TradeRequestInfo req) throws BizException {
        try {
            validate(req);
            
            long beginTime = 0L;
            RecordingRequest request = VoucherConvertor.convertPaymentReq(req);
            if (logger.isInfoEnabled()) {
                logger.info("落地支付统一凭证，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            RecordingResponse resp = voucherFacade.record(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程落地支付统一凭证， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, resp });
            }
            if(ResponseCode.INVOK_SUCCESS.getCode().equals(resp.getReturnCode())){
                return resp.getVoucherNo();
            }else{
                throw new BizException(ErrorCode.SYSTEM_ERROR,resp.getReturnMessage());
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("落地支付统一凭证异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    private void validate(TradeRequestInfo req){
        //参数验证
        Assert.notNull(req.getPayer());
        Assert.hasText(req.getPayer().getMemberId());
        Assert.notNull(req.getTradeType());
    }

    public String regist(String voucherType) throws BizException {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setVoucherType(voucherType);
        regReq.setNumber(1);
        try {
            RegisterResult res = voucherFacade.regist(regReq);
            if (res != null && ResponseCode.INVOK_SUCCESS.getCode().equals(res.getReturnCode())
                    && res.getVoucherNos() != null && res.getVoucherNos().size() > 0) {
                return res.getVoucherNos().get(0);
            }
        }
        catch (Exception e) {
            logger.error("注册交易凭证号失败", e);
        }
        
        return null;
    }
    
    public void record(String voucherNo, String voucherType, String productCode, String partnerId, 
            TradeEnvironment env) throws BizException {
        RecordingRequest recordingrequest = new RecordingRequest();
        recordingrequest.setVoucherType(voucherType);
        ControlVoucherInfo voucher = buildControlVoucher(voucherNo, productCode, partnerId, env);
        String voucherJson = JSON.toJSONString(voucher);
        recordingrequest.setVoucher(voucherJson);
        
        voucherFacade.record(recordingrequest);
    }
    
    private ControlVoucherInfo buildControlVoucher(String voucherNo, String productCode, String partnerId,
            TradeEnvironment env) {
        ControlVoucherInfo ctrlInfo = new ControlVoucherInfo();
        
        String uuid = UUID.randomUUID().toString().replace("-", "");
        ctrlInfo.setVoucherNo(voucherNo);
        ctrlInfo.setSourceVoucherNo(uuid);
        ctrlInfo.setRelatedSourceVoucherNo(uuid);
        ctrlInfo.setAccess(JSON.toJSONString(buildWebAccessInfo(voucherNo, env)));
        ctrlInfo.setAccessType(WebAccessInfo.accessType());
        ctrlInfo.setMemo(ControlType.BATCH.getCode());
        ctrlInfo.setProductCode(productCode);
        ctrlInfo.setSource(partnerId);
        ctrlInfo.setRequestTime(new Date());
        ctrlInfo.setControlType(ControlType.BATCH.getCode());
        
        return ctrlInfo;
    }
    
    private WebAccessInfo buildWebAccessInfo(String voucherNo, TradeEnvironment env) {
        WebAccessInfo webAccess = new WebAccessInfo();
        webAccess.setBrowser(env.getBrowser());
        webAccess.setBrowserVersion(env.getBrowserVersion());
        webAccess.setMac(env.getClientMac());
        webAccess.setMemo(env.getDomainName());
        webAccess.setRequestIp(env.getClientIp());
        webAccess.setReturnUrl(env.getSuccessDispalyUrl());
        return webAccess;
    }
}
