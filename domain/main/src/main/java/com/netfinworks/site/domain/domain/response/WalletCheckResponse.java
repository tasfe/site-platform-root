package com.netfinworks.site.domain.domain.response;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.BaseResult;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.info.WalletCheckInfo;

/**
 * <p>返回查询钱包对账单页面</p>
 * @author zhangyun.m
 * @version $Id: WalletCheckResponse.java, v 0.1 2013-12-12 下午3:17:32 HP Exp $
 */
public class WalletCheckResponse extends BaseResult{

    private static final long serialVersionUID = 1L;
    
    /** 分页信息 */
    private  QueryBase              queryBase;
    /** 上月最后一天余额 */
    private  Money  lmldBalance;
    /** 查询月余额 */
    private  Money  currentMonthBalance;
    /** 对账单流水 */
    private  List<WalletCheckInfo>   list;
    /** 总收入 只有汇总时才有值*/
    private Money             totalIncome;
    /** 总支出 只有汇总时才有值*/
    private Money             totalPayout;    

    public QueryBase getQueryBase() {
        return queryBase;
    }

    public void setQueryBase(QueryBase queryBase) {
        this.queryBase = queryBase;
    }


    public List<WalletCheckInfo> getList() {
        return list;
    }

    public void setList(List<WalletCheckInfo> list) {
        this.list = list;
    }

    public Money getLmldBalance() {
        return lmldBalance;
    }

    public void setLmldBalance(Money lmldBalance) {
        this.lmldBalance = lmldBalance;
    }

    public Money getCurrentMonthBalance() {
        return currentMonthBalance;
    }

    public void setCurrentMonthBalance(Money currentMonthBalance) {
        this.currentMonthBalance = currentMonthBalance;
    }

    public Money getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Money totalIncome) {
        this.totalIncome = totalIncome;
    }

    public Money getTotalPayout() {
        return totalPayout;
    }

    public void setTotalPayout(Money totalPayout) {
        this.totalPayout = totalPayout;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
