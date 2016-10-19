package com.netfinworks.site.core.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PhoneRechageResponse implements Serializable{
    private boolean             success;
    protected String            mobile;
    protected String            message;
    protected String            address;
    private String              province;
    private List<CardValue> types;
    private List<FlowValue> flows;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public List<CardValue> getTypes() {
		return types;
	}
	public void setTypes(List<CardValue> types) {
		this.types = types;
	}
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
	public List<FlowValue> getFlows() {
		return flows;
	}
	public void setFlows(List<FlowValue> flows) {
		this.flows = flows;
	}
    
}
