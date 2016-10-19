package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.site.domain.enums.TradeTypeRequest;

/**
 * <p>交易信息查询</p>
 * @author qinde
 * @version $Id: MemberAllInfo.java, v 0.1 2013-11-19 下午3:41:32 qinde Exp $
 */
public class TradeRequest implements Serializable {
    /**
     *
     */
    private static final long      serialVersionUID = -6517955882241659184L;
    /** 分页信息 */
    private QueryBase              queryBase;

    /** 交易凭证号 */
    private String                 tradeVoucherNo;

    /** 交易原始凭证号 */
    private String                 tradeSourceVoucherNo;
    
    /** 原交易订单号 */
    private String                 origTradeVoucherNo;

    /** 买家ID */
    private String                 buyerId;

    /** 卖家ID*/
    private String                 sellerId;

    /** 会员ID*/
    private String                 memberId;

    /** 合作方ID */
    private String                 partnerId;

    /** 交易状态 */
    private List<String>           tradeStatus;

    /** 交易类别 */
    private List<TradeTypeRequest> tradeType;

    /** 是否汇总 */
    private boolean                needSummary      = false;

    /** 查询开始时间 */
    private Date                   gmtStart;

    /** 查询结束时间 */
    private Date                   gmtEnd;
    
    /** 查询创建开始时间 */
    private Date      gmtCreateStart;

    /** 查询创建结束时间 */
    private Date      gmtCreateEnd;

	/** 批次号 */
	private String batchNo;
	
	 /** 需要包含的产品码 */
    private List<String>    productCodes;
    
    /** 需要排除的产品码 */
    private List<String>    ignoreProductCodes;
//	
//	/** 产品编码 */
//    private List<String> productCodes;
//    
    /** 原交易原始凭证号 */
	private String origTradeSourceVoucherNo;
	
	/** 外部批次号 */
	private String sourceBatchNo;

    public String getOrigTradeVoucherNo() {
		return origTradeVoucherNo;
	}

	public void setOrigTradeVoucherNo(String origTradeVoucherNo) {
		this.origTradeVoucherNo = origTradeVoucherNo;
	}

	public TradeRequest() {
        super();
    }

    public QueryBase getQueryBase() {
        return queryBase;
    }

    public void setQueryBase(QueryBase queryBase) {
        this.queryBase = queryBase;
    }

    public String getTradeVoucherNo() {
        return tradeVoucherNo;
    }

    public void setTradeVoucherNo(String tradeVoucherNo) {
        this.tradeVoucherNo = tradeVoucherNo;
    }

    public String getTradeSourceVoucherNo() {
        return tradeSourceVoucherNo;
    }

    public void setTradeSourceVoucherNo(String tradeSourceVoucherNo) {
        this.tradeSourceVoucherNo = tradeSourceVoucherNo;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public List<String> getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(List<String> tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public List<TradeTypeRequest> getTradeType() {
        return tradeType;
    }

    public void setTradeType(List<TradeTypeRequest> tradeType) {
        this.tradeType = tradeType;
    }

    public boolean isNeedSummary() {
        return needSummary;
    }

    public void setNeedSummary(boolean needSummary) {
        this.needSummary = needSummary;
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

	public Date getGmtCreateStart() {
		return gmtCreateStart;
	}

	public void setGmtCreateStart(Date gmtCreateStart) {
		this.gmtCreateStart = gmtCreateStart;
	}

	public Date getGmtCreateEnd() {
		return gmtCreateEnd;
	}

	public void setGmtCreateEnd(Date gmtCreateEnd) {
		this.gmtCreateEnd = gmtCreateEnd;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
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

    public String getOrigTradeSourceVoucherNo() {
		return origTradeSourceVoucherNo;
	}

	public void setOrigTradeSourceVoucherNo(String origTradeSourceVoucherNo) {
		this.origTradeSourceVoucherNo = origTradeSourceVoucherNo;
	}

	public String getSourceBatchNo() {
		return sourceBatchNo;
	}

	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
