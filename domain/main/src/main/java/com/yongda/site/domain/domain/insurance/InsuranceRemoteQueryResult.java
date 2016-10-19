package com.yongda.site.domain.domain.insurance;

import java.io.Serializable;
import java.util.List;

public class InsuranceRemoteQueryResult implements Serializable {

	
    /** 描述 */
	private static final long serialVersionUID = 4295423726023938658L;
	
	boolean isSuccess;
	
	String returnCode;
	
	String returnMessage;
	
	List<InsuranceOrder> orders;

	public List<InsuranceOrder> getOrders() {
		return orders;
	}

	public void setOrders(List<InsuranceOrder> orders) {
		this.orders = orders;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}
}
