package com.yongda.site.service.personal.facade.request;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.enums.TradeType;

public class PersonalTradeRequest extends BaseRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 992079451647289909L;

    //提现金额
	private String            amount;
    
	//提现类型
    private String            type;
  
    //银行卡ID
    private String            bankcardId;
    
    //客户端IP
    private String            clientIp;
    

    public String getBankcardId() {
		return bankcardId;
	}

	public void setBankcardId(String bankcardId) {
		this.bankcardId = bankcardId;
	}
    

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

}
