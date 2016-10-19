package com.netfinworks.site.ext.service.facade.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.fos.service.facade.FundoutFacade;
import com.netfinworks.fos.service.facade.response.FundoutGetResponse;
import com.netfinworks.ma.service.base.model.Account;
import com.netfinworks.ma.service.base.model.MerchantInfo;
import com.netfinworks.ma.service.facade.IAccountFacade;
import com.netfinworks.ma.service.facade.IMerchantFacade;
import com.netfinworks.ma.service.request.MerchantQueryRequest;
import com.netfinworks.ma.service.response.AccountInfoResponse;
import com.netfinworks.ma.service.response.MerchantListResponse;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.service.facade.converter.ReceiptConverter;
import com.netfinworks.site.service.facade.api.ReceiptFacade;
import com.netfinworks.site.service.facade.model.Receipt;
import com.netfinworks.site.service.facade.request.ReceiptRequest;
import com.netfinworks.site.service.facade.response.ReceiptRespose;
import com.netfinworks.ues.UesClient;
import com.netfinworks.ues.ctx.EncryptContext;

/**
 * <p>电子对账单实现</p>
 * @author zhangyun.m
 * @version $Id: ReceiptServiceImpl.java, v 0.1 2014年7月3日 上午10:47:16 zhangyun.m Exp $
 */
public class ReceiptServiceImpl implements ReceiptFacade {

    private Logger              logger = LoggerFactory.getLogger(ReceiptServiceImpl.class);

    //出款服务
    private FundoutFacade       fundoutFacade;

    private Map<String, String> fundoutProducts;

    private Map<String, String> tradeProducts;

    @Resource
    private UesClient           uesClient;

    @Resource
    private IAccountFacade      accountFacade;

    @Resource
    private IMerchantFacade     merchantFacade;

    @Override
    public ReceiptRespose queryReceipt(ReceiptRequest request, OperationEnvironment oe) {
        
        logger.info("查询资金对账单数据，请求参数：{}", request);

        ReceiptRespose rep =  new ReceiptRespose();
        String tradeType = null;

        try {
            //出款
            
            if(StringUtil.isBlank(request.getTradeType())){
                rep.setResultMessage("请求参数：交易类型不能为空");
                rep.setSuccess(false);
                logger.info("查询资金对账单数据，交易类型不能为空");
                return rep; 
            }
            
            if(StringUtil.isBlank(request.getTradeVoucherNo())){
                rep.setResultMessage("请求参数：交易凭证号不能为空");
                rep.setSuccess(false);
                logger.info("查询资金对账单数据，交易凭证号不能为空");
                return rep; 
            }
            
            if (!request.getTradeType().isEmpty() && fundoutProducts != null) {
                for (String fundoutP : fundoutProducts.keySet()) {
                    if (request.getTradeType().equals(fundoutP)) {
                        rep = queryReceiptByFundout(request, oe);
                        tradeType = fundoutProducts.get(fundoutP);
                        break;
                    }
                }
            }
            
            if(tradeType==null){
                rep.setResultMessage("请求参数：交易类型有误");
                rep.setSuccess(false);
                logger.info("查询资金对账单数据，交易类型有误");
                return rep;
            }
            
            if(rep.getReceipt() == null){
                logger.info("查询资金对账单数据 {}",rep.getResultMessage());
                return rep;
            }
            //账户信息
            AccountInfoResponse accountInfoResponse = null;
            if (rep != null) {
                accountInfoResponse = accountFacade.queryAccountById(oe, rep.getReceipt()
                    .getBuyerAccountNo());
                Account account = accountInfoResponse.getAccount();
                Receipt receipt = rep.getReceipt();
                receipt.setBuyerAccountNo(account.getAccountId());
                receipt.setBuyerName(account.getAccountName());

                //入账账户
                receipt.setEnterAccount(account.getAccountId());
                receipt.setEnterAccountName(account.getAccountName());
                receipt.setEnterAmount(receipt.getTradeAmount().getAmount().toString());
                //交易类型
                receipt.setTradeType(tradeType);

                //账户类型：一般商户 、 特约商户
                MerchantQueryRequest merchantQueryRequest = new MerchantQueryRequest();
                merchantQueryRequest.setMemberId(rep.getReceipt().getBuyerId());
                MerchantListResponse merListRep = merchantFacade.queryMerchantInfos(oe,
                    merchantQueryRequest);
                List<MerchantInfo> merList = merListRep.getMerchantInfos();
                if (merList != null && merList.size() > 0) {
                    receipt.setAccountType("特约商户");
                } else {
                    receipt.setAccountType("一般商户");
                }
            }

        } catch (BizException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return rep;
    }

    @SuppressWarnings("null")
    public ReceiptRespose queryReceiptByFundout(ReceiptRequest request, OperationEnvironment oe)
                                                                                                throws BizException {

        //for test
        oe = new OperationEnvironment();

        if (request == null)
            return null;

        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("查询电子对账单（出款），请求参数：{}", request);
            beginTime = System.currentTimeMillis();
        }

        FundoutGetResponse response = null;
        if (request.getTradeVoucherNo() != null) {
            // 请求返回
            response = fundoutFacade.getFundoutInfo(request.getTradeVoucherNo(), oe);
        }

        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("远程查询查询电子对账单（出款）， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                    response });
        }

        ReceiptRespose rep = null;

        if (ResponseCode.DEPOSITY_SUCCESS.getCode().equals(response.getReturnCode())) {
            if (response.getFundoutInfo() != null) {
                rep = ReceiptConverter.convertDepositReceiptFundoutObj(response);

                //收款方 银行卡解密
                rep.setEncryptSellerAccountNo(getDataBxyTicket(rep.getReceipt()
                    .getSellerAccountNo()));
                //付款方 银行卡解密
                rep.setEncryptBuyerAccountNo(getDataBxyTicket(rep.getReceipt().getBuyerAccountNo()));
            }else{
                rep = new ReceiptRespose();
            }

            rep.setResultMessage(response.getReturnMessage());
            rep.setSuccess(true);

        } else {
            rep.setErrorCode(response.getReturnCode());
        }

        return rep;
    }

    /** 根据ticket获得解密信息
     * @param ticket
     * @return
     * @throws BizException
     */
    public String getDataBxyTicket(String ticket) throws BizException {
        if (StringUtil.isEmpty(ticket)) {
            return null;
        }
        EncryptContext context = new EncryptContext().useTicket(ticket.trim());
        //远程调用计时开始
        long beginTime = System.currentTimeMillis();
        try {
            boolean rest = uesClient.getDataByTicket(context);
            if (!rest) {
                logger.error("ues invoke error code : {} , message : {}",
                    new Object[] { context.getResultCode(), context.getResultMessage() });
            }

            if (logger.isInfoEnabled()) {
                //远程调用计时结束
                long consumeTime = System.currentTimeMillis() - beginTime;
                //log远程调用耗时和响应
                logger.info("远程调用：{} 耗时: {} (ms)", new Object[] {
                        "uesServiceClient#getDataByTicket", consumeTime });
            }
            return context.getPlainData();
        } catch (Exception e) {
            logger.error("调用ues 解密出错", e);
            throw new RuntimeException(e);
        }
    }

    public void setTradeProducts(Map<String, String> tradeProducts) {
        this.tradeProducts = tradeProducts;
    }

    public void setFundoutProducts(Map<String, String> fundoutProducts) {
        this.fundoutProducts = fundoutProducts;
    }

    public void setFundoutFacade(FundoutFacade fundoutFacade) {
        this.fundoutFacade = fundoutFacade;
    }

    public void setUesClient(UesClient uesClient) {
        this.uesClient = uesClient;
    }

    public void setAccountFacade(IAccountFacade accountFacade) {
        this.accountFacade = accountFacade;
    }

    public void setMerchantFacade(IMerchantFacade merchantFacade) {
        this.merchantFacade = merchantFacade;
    }

}
