package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

/**
 * <p>
 * 企业钱包结算汇总信息
 * </p>
 * 
 * @author zhangyun.m
 * @version $Id: SummaryInfo.java, v 0.1 2013-12-6 下午5:02:01 HP Exp $
 */
public class SummaryInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 公司名称 */
	private String companyName;
	/** 订单笔数 */
	private long totalTradeCount;
	/** 订单金额 */
	private Money totalTradeAmount;
	/** 结算金额 */
	private Money totalSettledAmount;
	/** 退单笔数 */
	private long totalRefundCount;
	/** 退单金额 */
	private Money totalRefundAmount;
	/** 退单结算金额 */
	private Money totalRefundSettledAmount;
	/** 实收金额 */
	private Money realAmount;
	/** 总计手续费金额 */
	private Money totalFeeAmount;
	/** 总计手续费笔数 */
	private long totalFeeCount;
    /** 总计退还手续费金额 */
	private Money totalRfdFeeAmount;
	/** 总计退还手续费笔数 */
	private long totalRfdFeeCount;	
	/** 交易查询开始时间 */
	private Date gmtStart;
	/** 交易查询结束时间 */
	private Date gmtEnd;
	/** 用json 包装的Map<String,Money>,key=splitTag，value=金额 */
	private String splitTagAmoutMapJson;
	/** 金币 */
	private Money coin;
	/** 佣金 */
	private Money rebate;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public long getTotalTradeCount() {
		return totalTradeCount;
	}

	public void setTotalTradeCount(long totalTradeCount) {
		this.totalTradeCount = totalTradeCount;
	}

	public Money getTotalTradeAmount() {
		return totalTradeAmount;
	}

	public void setTotalTradeAmount(Money totalTradeAmount) {
		this.totalTradeAmount = totalTradeAmount;
	}

	public Money getTotalSettledAmount() {
		return totalSettledAmount;
	}

	public void setTotalSettledAmount(Money totalSettledAmount) {
		this.totalSettledAmount = totalSettledAmount;
	}

	public long getTotalRefundCount() {
		return totalRefundCount;
	}

	public void setTotalRefundCount(long totalRefundCount) {
		this.totalRefundCount = totalRefundCount;
	}

	public Money getTotalRefundAmount() {
		return totalRefundAmount;
	}

	public void setTotalRefundAmount(Money totalRefundAmount) {
		this.totalRefundAmount = totalRefundAmount;
	}

	public Money getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(Money realAmount) {
		this.realAmount = realAmount;
	}

	public Date getGmtStart() {
		return gmtStart;
	}

	public void setGmtStart(Date gmtStart) {
		this.gmtStart = gmtStart;
	}

	public Date getGmtEnd() {
		return gmtEnd;
	}

	public void setGmtEnd(Date gmtEnd) {
		this.gmtEnd = gmtEnd;
	}

	public String getSplitTagAmoutMapJson() {
		return splitTagAmoutMapJson;
	}

	public void setSplitTagAmoutMapJson(String splitTagAmoutMapJson) {
		this.splitTagAmoutMapJson = splitTagAmoutMapJson;
	}

	public Money getTotalRefundSettledAmount() {
		return totalRefundSettledAmount;
	}

	public void setTotalRefundSettledAmount(Money totalRefundSettledAmount) {
		this.totalRefundSettledAmount = totalRefundSettledAmount;
	}

	public Money getCoin() {
		return coin;
	}

	public void setCoin(Money coin) {
		this.coin = coin;
	}

	public Money getRebate() {
		return rebate;
	}

	public void setRebate(Money rebate) {
		this.rebate = rebate;
	}	

	public Money getTotalFeeAmount() {
		return totalFeeAmount;
	}

	public void setTotalFeeAmount(Money totalFeeAmount) {
		this.totalFeeAmount = totalFeeAmount;
	}

	public long getTotalFeeCount() {
		return totalFeeCount;
	}

	public void setTotalFeeCount(long totalFeeCount) {
		this.totalFeeCount = totalFeeCount;
	}

	public Money getTotalRfdFeeAmount() {
		return totalRfdFeeAmount;
	}

	public void setTotalRfdFeeAmount(Money totalRfdFeeAmount) {
		this.totalRfdFeeAmount = totalRfdFeeAmount;
	}

	public long getTotalRfdFeeCount() {
		return totalRfdFeeCount;
	}

	public void setTotalRfdFeeCount(long totalRfdFeeCount) {
		this.totalRfdFeeCount = totalRfdFeeCount;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
