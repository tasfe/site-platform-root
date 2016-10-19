package com.netfinworks.site.core.common;

public class FlowValue {
	
	private String cardNo;//产品ID,流量充值时候需要传
	private String denomination;//流量面值：10M、50M、1G等
    private String price;//全国流量原价
    private String sellPrice;//全国流量实际售价
    private String localPrice;//本地流量原价
    private String localSellPrice;//本地流量实际售价
    public String getDenomination() {
        return denomination;
    }
    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
	public String getLocalPrice() {
		return localPrice;
	}
	public void setLocalPrice(String localPrice) {
		this.localPrice = localPrice;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getSellPrice() {
		return sellPrice;
	}
	public void setSellPrice(String sellPrice) {
		this.sellPrice = sellPrice;
	}
	public String getLocalSellPrice() {
		return localSellPrice;
	}
	public void setLocalSellPrice(String localSellPrice) {
		this.localSellPrice = localSellPrice;
	}
    
}
