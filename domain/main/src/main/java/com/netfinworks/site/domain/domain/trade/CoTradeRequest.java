package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.enums.TradeTypeRequest;

public class CoTradeRequest extends OperationEnvironment implements
		Serializable {
	private static final long serialVersionUID = 1L;

	/** 会员Id */
	private String memberId;
	/** 卖家Id */
	private String sellerId;
	/** 查询开始时间 */
	private Date beginTime;
	/** 查询结束时间 */
	private Date endTime;
	/** 交易状态 */
	private List<String> status;
	/** 交易类别 */
	private List<TradeTypeRequest> tradeType;
	/** 产品编码 */
    private List<String> productCodes;
    /** 过滤的产品编码 */
    private List<String> ignoreProductCodes;
	/** 分页 */
	private QueryBase queryBase;
	/** 是否汇总 */
	private boolean needSummary = false;
	/** 是否发生过结算 */
	private boolean hasSettled = false;
	/** 关键字对应的--交易凭证号*/
	private String tradeVoucherNo;
	/** 商户订单号*/
	private String tradeSourceVoucherNo;
	/** 外部批次号 */
	private String sourceBatchNo;
	
	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getTradeSourceVoucherNo() {
		return tradeSourceVoucherNo;
	}

	public void setTradeSourceVoucherNo(String tradeSourceVoucherNo) {
		this.tradeSourceVoucherNo = tradeSourceVoucherNo;
	}

	public String getTradeVoucherNo() {
		return tradeVoucherNo;
	}

	public void setTradeVoucherNo(String tradeVoucherNo) {
		this.tradeVoucherNo = tradeVoucherNo;
	}

	public String getMemberId() {
		return memberId;
	}

	public QueryBase getQueryBase() {
		return queryBase;
	}

	public void setQueryBase(QueryBase queryBase) {
		this.queryBase = queryBase;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public List<String> getStatus() {
		return status;
	}

	public void setStatus(List<String> status) {
		this.status = status;
	}

	public boolean isNeedSummary() {
		return needSummary;
	}

	public void setNeedSummary(boolean needSummary) {
		this.needSummary = needSummary;
	}

	public List<TradeTypeRequest> getTradeType() {
		return tradeType;
	}

	public void setTradeType(List<TradeTypeRequest> tradeType) {
		this.tradeType = tradeType;
	}

	public boolean isHasSettled() {
		return hasSettled;
	}

	public void setHasSettled(boolean hasSettled) {
		this.hasSettled = hasSettled;
	}

	public List<String> getProductCodes() {
		return productCodes;
	}

	public void setProductCodes(List<String> productCodes) {
		this.productCodes = productCodes;
	}

	public List<String> getIgnoreProductCodes() {
		return ignoreProductCodes;
	}

	public void setIgnoreProductCodes(List<String> ignoreProductCodes) {
		this.ignoreProductCodes = ignoreProductCodes;
	}
	public String getSourceBatchNo() {
		return sourceBatchNo;
	}

	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
