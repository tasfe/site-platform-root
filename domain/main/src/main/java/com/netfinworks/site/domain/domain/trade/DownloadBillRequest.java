package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;

public class DownloadBillRequest extends OperationEnvironment implements
		Serializable {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	/** 会员ID */
	private String memberId;
	/** 查询开始时间 */
	private Date beginTime;

	/** 查询结束时间 */
	private Date endTime;
	/** 分页 */
	private QueryBase queryBase;
	/** 卖家ID */
	private String sellerId;
	/** 交易状态 */
	private List<String> tradeStatus;

	private String refundStatus;
	/**交易订单号*/
	private String tradeVoucherNo;
	/** 产品编码 */
    private List<String> productCodes;

	/** 批次号 */
	private String batchNo;
	
	/** 商户外部批次号 */
	private String sourceBatchNo;
	
	/** 商户外部订单号 */
	private String tradeSourceVoucherNo;

	public List<String> getProductCodes() {
		return productCodes;
	}

	public void setProductCodes(List<String> productCodes) {
		this.productCodes = productCodes;
	}

	public String getTradeVoucherNo() {
		return tradeVoucherNo;
	}

	public void setTradeVoucherNo(String tradeVoucherNo) {
		this.tradeVoucherNo = tradeVoucherNo;
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

	public QueryBase getQueryBase() {
		return queryBase;
	}

	public void setQueryBase(QueryBase queryBase) {
		this.queryBase = queryBase;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public List<String> getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(List<String> tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	
	

	public String getSourceBatchNo() {
		return sourceBatchNo;
	}

	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}
	
	

	public String getTradeSourceVoucherNo() {
		return tradeSourceVoucherNo;
	}

	public void setTradeSourceVoucherNo(String tradeSourceVoucherNo) {
		this.tradeSourceVoucherNo = tradeSourceVoucherNo;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
