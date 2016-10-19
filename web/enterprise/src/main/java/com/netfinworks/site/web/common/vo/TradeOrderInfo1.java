package com.netfinworks.site.web.common.vo;

import java.io.Serializable;
import java.util.Date;

import com.netfinworks.common.util.money.Money;

public class TradeOrderInfo1  implements Serializable{
	private static final long serialVersionUID = 1L;
	private String id;
	/**
	 * 产品编码
	 */
	private String bizProductCode;
	/**
	 * 交易凭证号
	 */
	private String tradeVoucherNo;
	/**
	 * 交易原始凭证号
	 */
	private String tradeSrcVoucherNo;
	/**
	 * 原交易凭证号
	 */
	private String origTradeVoucherNo;
	/**
	 * 商户订单号
	 */
	private String bizNo;
	/**
	 * 交易金额
	 */
	private Money tradeAmount1;
	/**
	 * 交易类型
	 */
	private String tradeType;
	/**
	 * 买家客户ID==memberId
	 */
	private String buyerId; 
	/**
	 * 买家客户名称 
	 */
	private String buyerName;
	/**
	 * 卖家客户ID
	 */
	private String sellerId;
	/**
	 * 卖家客户名称
	 */
	private String sellerName;
	/**
	 * 卖家账户
	 */
	private String sellerAccountNo;
	/**
	 * 平台方客户ID
	 */
	private String partnerId;
	/**
	 * 平台方客户名称
	 */
	private String partnerName;
	/**
	 * 终端类型
	 */
	private String accessChannel;
	/**
	 * 扩展信息
	 */
	private String extension;
	/**
	 * 前交易状态
	 */
	private String preStatus;
	/**
	 * 交易状态900退款初始901退款申请成功 例银行未返回结果951退款成功952退款失败
	 */
	private String status;
	/**
	 * 结果备注
	 */
	private String tradeMemo;
	private String resultMemo;
	/**
	 * 错误编码
	 */
	private String errorCode;
	/**
	 * 错误信息
	 */
	private String errorMsg;
	/**
	 * 创建时间
	 */
	private Date gmtCreate;
	/**
	 * 修改时间
	 */
	private Date gmtModified;
	/**
	 * 当前状态的通知情况Y-已通知N-未通知
	 */
	private String isNotified;
	
	/**
	 * 商品信息
	 */
	private String prodDesc;
	/**
	 * 商品展示URL
	 */
	private String prodShowUrl;
	/**
	 * 关联交易凭证号（对应合并支付的交易）
	 */
	private String relatedTradeVoucherNo;
	/**
	 * 处理器ID
	 */
	private String processorId;
	private String batchNo;
	/**
	 * 储值账户
	 */
	private String accountNo;
	/**
	 * 算费所得
	 */
	private Money fee1;
	/**
	 * 认证卡号ID，会员提现时使用
	 */
	private String cardId;
	/**
	 * 银行卡卡号
	 */
	private String cardNo;
	/**
	 * 借记/贷记
	 */
	private String cardType;
	/**
	 * 银行卡户名
	 */
	private String name;
	/**
	 * 银行编码
	 */
	private String bankCode;
	/**
	 * 银行名称
	 */
	private String bankName;
	/**
	 *分支行信息 
	 */
	private String branchName;
	/**
	 * 联行号,如403874200010
	 */
	private String bankLineNo;
	/**
	 * 省份信息
	 */
	private String prov;
	/**
	 * 城市信息
	 */
	private String city;              
	private String companyPersonal;
	private String fundoutGrade ;
	private Date orderTime;
	/**
	 * 订单状态时间
	 */
	private Date resultTime;     
	/**
	 * 账户类型
	 */
	private String accountType;
	/**
	 * 结果描述
	 */
	private String resultDesc;
	/**
	 * 批次订单号
	 */
	private String batchOrderNo;
	/**
	 * 提交人ID
	 */
	private String submitId;
	/**
	 *  商户批次号
	 */
	private String sourceBatchNo;
	/**
	 * 是否下载，0未下载，1已下载
	 */
	private String hasDownload;
	/**
	 * 来源 1 到永达互联网金融 2 到卡
	 */
	private String source;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBizProductCode() {
		return bizProductCode;
	}
	public void setBizProductCode(String bizProductCode) {
		this.bizProductCode = bizProductCode;
	}
	public String getTradeVoucherNo() {
		return tradeVoucherNo;
	}
	public void setTradeVoucherNo(String tradeVoucherNo) {
		this.tradeVoucherNo = tradeVoucherNo;
	}
	public String getTradeSrcVoucherNo() {
		return tradeSrcVoucherNo;
	}
	public void setTradeSrcVoucherNo(String tradeSrcVoucherNo) {
		this.tradeSrcVoucherNo = tradeSrcVoucherNo;
	}
	public String getOrigTradeVoucherNo() {
		return origTradeVoucherNo;
	}
	public void setOrigTradeVoucherNo(String origTradeVoucherNo) {
		this.origTradeVoucherNo = origTradeVoucherNo;
	}
	public String getBizNo() {
		return bizNo;
	}
	public void setBizNo(String bizNo) {
		this.bizNo = bizNo;
	}
	public Money getTradeAmount1() {
		return tradeAmount1;
	}
	public void setTradeAmount1(Money tradeAmount1) {
		this.tradeAmount1 = tradeAmount1;
	}
	public String getTradeType() {
		return tradeType;
	}
	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}
	public String getBuyerId() {
		return buyerId;
	}
	public void setBuyerId(String buyerId) {
		this.buyerId = buyerId;
	}
	public String getBuyerName() {
		return buyerName;
	}
	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}
	public String getSellerId() {
		return sellerId;
	}
	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}
	public String getSellerName() {
		return sellerName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	public String getSellerAccountNo() {
		return sellerAccountNo;
	}
	public void setSellerAccountNo(String sellerAccountNo) {
		this.sellerAccountNo = sellerAccountNo;
	}
	public String getPartnerId() {
		return partnerId;
	}
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
	public String getPartnerName() {
		return partnerName;
	}
	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}
	public String getAccessChannel() {
		return accessChannel;
	}
	public void setAccessChannel(String accessChannel) {
		this.accessChannel = accessChannel;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getPreStatus() {
		return preStatus;
	}
	public void setPreStatus(String preStatus) {
		this.preStatus = preStatus;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTradeMemo() {
		return tradeMemo;
	}
	public void setTradeMemo(String tradeMemo) {
		this.tradeMemo = tradeMemo;
	}
	public String getResultMemo() {
		return resultMemo;
	}
	public void setResultMemo(String resultMemo) {
		this.resultMemo = resultMemo;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public Date getGmtCreate() {
		return gmtCreate;
	}
	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
	public Date getGmtModified() {
		return gmtModified;
	}
	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}
	public String getIsNotified() {
		return isNotified;
	}
	public void setIsNotified(String isNotified) {
		this.isNotified = isNotified;
	}
	public String getProdDesc() {
		return prodDesc;
	}
	public void setProdDesc(String prodDesc) {
		this.prodDesc = prodDesc;
	}
	public String getProdShowUrl() {
		return prodShowUrl;
	}
	public void setProdShow_url(String prodShowUrl) {
		this.prodShowUrl = prodShowUrl;
	}
	public String getRelatedTradeVoucherNo() {
		return relatedTradeVoucherNo;
	}
	public void setRelatedTradeVoucherNo(String relatedTradeVoucherNo) {
		this.relatedTradeVoucherNo = relatedTradeVoucherNo;
	}
	public String getProcessorId() {
		return processorId;
	}
	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public Money getFee1() {
		return fee1;
	}
	public void setFee1(Money fee1) {
		this.fee1 = fee1;
	}
	public String getCardId() {
		return cardId;
	}
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getBankLineNo() {
		return bankLineNo;
	}
	public void setBankLineNo(String bankLineNo) {
		this.bankLineNo = bankLineNo;
	}
	public String getProv() {
		return prov;
	}
	public void setProv(String prov) {
		this.prov = prov;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCompanyPersonal() {
		return companyPersonal;
	}
	public void setCompanyPersonal(String companyPersonal) {
		this.companyPersonal = companyPersonal;
	}
	public String getFundoutGrade() {
		return fundoutGrade;
	}
	public void setFundoutGrade(String fundoutGrade) {
		this.fundoutGrade = fundoutGrade;
	}
	public Date getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}
	public Date getResultTime() {
		return resultTime;
	}
	public void setResultTime(Date resultTime) {
		this.resultTime = resultTime;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getResultDesc() {
		return resultDesc;
	}
	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}
	public String getBatchOrderNo() {
		return batchOrderNo;
	}
	public void setBatchOrderNo(String batchOrderNo) {
		this.batchOrderNo = batchOrderNo;
	}
	public String getSubmitId() {
		return submitId;
	}
	public void setSubmitId(String submitId) {
		this.submitId = submitId;
	}
	public String getSourceBatchNo() {
		return sourceBatchNo;
	}
	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}
	public String getHasDownload() {
		return hasDownload;
	}
	public void setHasDownload(String hasDownload) {
		this.hasDownload = hasDownload;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
