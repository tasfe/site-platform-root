/**
 *
 */
package com.netfinworks.site.ext.integration.cashier.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.cashier.facade.api.CashierFacade;
import com.netfinworks.cashier.facade.api.PayLimitFacade;
import com.netfinworks.cashier.facade.domain.QueryPayLimitRequest;
import com.netfinworks.cashier.facade.domain.QueryPayLimitResponse;
import com.netfinworks.cashier.facade.domain.SiteUrlRequest;
import com.netfinworks.cashier.facade.domain.UrlResponse;
import com.netfinworks.cashier.facade.enums.AccessChannel;
import com.netfinworks.ma.service.facade.IMerchantFacade;
import com.netfinworks.ma.service.request.MerchantQueryRequest;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.trade.BatchTradeRequestInfo;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.cashier.CashierService;
import com.netfinworks.site.ext.integration.cashier.convert.CashierConvertor;
import com.yongda.site.domain.domain.cashier.PayLimit;

/**
 * <p>收银台服务</p>
 * @author fjl
 * @version $Id: CashierServiceImpl.java, v 0.1 2013-11-28 下午5:59:42 fjl Exp $
 */
@Service("cashierService")
public class CashierServiceImpl implements CashierService {
    private Logger        logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private CashierFacade cashierFacade;
    
    @Resource
    private PayLimitFacade cashierPayLimitFacade;
    
    @Resource(name = "merchantFacade")
    private IMerchantFacade merchantFacade;
    
    @Override
    public String applyCashierUrl(TradeRequestInfo req) throws BizException {
        try {
            return applyCashierUrl(req,"WEB");
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("请求收银台地址异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
    
    @Override
	public String applyCashierUrl(TradeRequestInfo req, String accessChannel)
			throws BizException {
    	try {
            PartyInfo partyInfo=req.getPayer();
            MerchantQueryRequest merchantQueryRequest = new MerchantQueryRequest();
            merchantQueryRequest.setMemberId(partyInfo.getMemberId());
            validate(req);
            SiteUrlRequest request = CashierConvertor.convert(req,accessChannel);
            return applyRemotCashierUrl(request);
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("请求收银台地址异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
	}



	@Override
    public String applyCashierUrl(BatchTradeRequestInfo req) throws BizException {
        try {
            PartyInfo partyInfo=req.getPayer();
            MerchantQueryRequest merchantQueryRequest = new MerchantQueryRequest();
            merchantQueryRequest.setMemberId(partyInfo.getMemberId());
            validate(req);
            SiteUrlRequest request = CashierConvertor.convert(req);
            return applyRemotCashierUrl(request);
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("钱包付款请求收银台地址异常:请求信息" + req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, "钱包付款请求收银台地址异常", e);
            }
        }
    }

    private String applyRemotCashierUrl(SiteUrlRequest request) throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("请求收银台地址，请求参数：{}", request);
            beginTime = System.currentTimeMillis();
        }
        UrlResponse resp = cashierFacade.getCashierUrlFromSite(request);
        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("钱包付款远程请求收银台地址， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
        }
        if (resp.isSuccess()) {
            return resp.getUrl();
        } else {
            throw new BizException(ErrorCode.SYSTEM_ERROR, resp.getResultMessage());
        }
    }
    

    /** 
    * @Title: queryInstPayLimit 
    * @Description: TODO(这里用一句话描述这个方法的作用) 
    * @param @param instCode
    * @param @param payChannel 51 借记 ，52 贷记
    * @param @return
    * @param @throws BizException    设定文件 
    * @return 返回类型 
    * @throws 
    */
    @Override
	public List<PayLimit> queryInstPayLimit(String instCode, String payChannel)
			throws BizException {
    	 long beginTime = 0L;
    	 
    	 QueryPayLimitRequest req = new QueryPayLimitRequest();
         req.setBankCode(instCode);
         req.setPayChannel(payChannel);
         if (logger.isInfoEnabled()) {
             logger.info("查询机构限额,请求参数：{}", req);
             beginTime = System.currentTimeMillis();
         }
         QueryPayLimitResponse resp = cashierPayLimitFacade.queryPayLimitList(req);
         if (logger.isInfoEnabled()) {
             long consumeTime = System.currentTimeMillis() - beginTime;
             logger.info("查询机构限额， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
         }
         if (resp.isSuccess()) {
             return CashierConvertor.convertResponse(resp.getInfos());
         } else {
             throw new BizException(ErrorCode.SYSTEM_ERROR, resp.getResultMessage());
         }
	}

	private void validate(TradeRequestInfo req) {
        // 参数验证
        Assert.notNull(req.getTradeVoucherNo());
        Assert.notNull(req.getTradeType());
    }

    private void validate(BatchTradeRequestInfo req) {
        // 参数验证
        Assert.notEmpty(req.getTradeVoucherNo());
        Assert.notNull(req.getTradeType());
    }
}
