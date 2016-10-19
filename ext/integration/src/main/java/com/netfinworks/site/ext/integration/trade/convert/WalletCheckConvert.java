package com.netfinworks.site.ext.integration.trade.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.netfinworks.dpm.accounting.api.requests.BalanceQueryRequest;
import com.netfinworks.ma.service.request.AccBalanceListRequest;
import com.netfinworks.ma.service.request.GetBalanceListRequestParam;
import com.netfinworks.ma.service.response.AccountBalanceListResp;
import com.netfinworks.site.domain.domain.info.WalletCheckInfo;
import com.netfinworks.site.domain.domain.trade.WalletCheckRequest;
import com.netfinworks.site.domain.enums.DealType;

/**
 * <p>企业查询转换</p>
 * @author zhangyun.m
 * @version $Id: BusinessQueryConvert.java, v 0.1 2013-12-4 下午2:45:09 HP Exp $
 */
public class WalletCheckConvert {

    /**
     * zhangyun.m
     * 钱包对账单：请求转换
    * @param req
    * @return
    */
    public static AccBalanceListRequest convertWalletCheckRequset(WalletCheckRequest req) {

        AccBalanceListRequest request = new AccBalanceListRequest();
        GetBalanceListRequestParam reqParam = new GetBalanceListRequestParam();
        //企业Id
        if (req.getMemberId() != null) {
            request.setMemberId(req.getMemberId());
        }
        //分页信息
        reqParam.setPageId(req.getQueryBase().getCurrentPage());
        reqParam.setPageSize(req.getQueryBase().getPageSize());
        //查询开始时间
        if (req.getBeginTime() != null) {
            reqParam.setStartTime(req.getBeginTime());

        }
        //查询结束时间
        if (req.getEndTime() != null) {
            reqParam.setEndTime(req.getEndTime());
        }

        //交易类型
        if (req.getTxnType() != null) {
            reqParam.setTxnType(req.getTxnType().getCode());
        }
        //是否需要汇总
        reqParam.setNeedSummary(req.isNeedSummary());
        reqParam.setOrderMode(2);//设置排序方式
        request.setBalanceRequest(reqParam);
        //账户类型
        if (req.getAccountType() != null) {
            request.setAccountType(Long.parseLong(req.getAccountType().toString()));
        }

        return request;
    }

    /**
     * zhangyun.m
     * 钱包对账单： 结果转换
     * @param list
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static List<WalletCheckInfo> convertWalletCheckReponseList(List<AccountBalanceListResp> list)
                                                                                                        throws IllegalAccessException,
                                                                                                        InvocationTargetException {

        List<WalletCheckInfo> result = null;
        if ((list != null) && (list.size() > 0)) {
            result = new ArrayList<WalletCheckInfo>();
            WalletCheckInfo wcInfo = null;
            for (AccountBalanceListResp info : list) {
                wcInfo = new WalletCheckInfo();

                wcInfo.setTxnTime(info.getTxnTime());
                //支付编码（支付类型）
                wcInfo.setPayCode(info.getPayCode());
				// 产品编码
				wcInfo.setProductCode(info.getProductCode());
                //订单号 
                wcInfo.setSysTraceNo(info.getSysTraceNo());
                //摘要
                wcInfo.setSummary(info.getSummary());
                //交易类型
                wcInfo.setTxnType(DealType.getByCode(info.getTxnType()));
                //金额
                wcInfo.setTxnAmt(info.getTxnAmt());
                //余额
                wcInfo.setAfterAmt(info.getAfterAmt());
                result.add(wcInfo);
            }
            return result;
        }
        return null;
    }

    /**
     * zhangyun.m
     * 查询单日余额请求转换
     * @param req
     * @return
     */
    public static BalanceQueryRequest convertBalanceRequset(WalletCheckRequest wreq) {
        BalanceQueryRequest breq = new BalanceQueryRequest();
        //会员ID
        breq.setMemberId(wreq.getMemberId());
        //账户类型
        breq.setAccountType(wreq.getAccountType());
        //账户号
        breq.setAccountNo(wreq.getAccountNo());
        return breq;

    }

}
