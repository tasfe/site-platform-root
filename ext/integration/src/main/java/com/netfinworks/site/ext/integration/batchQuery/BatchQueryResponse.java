package com.netfinworks.site.ext.integration.batchQuery;

import java.util.List;

public class BatchQueryResponse {
    private String resultCode;
    private String resultMessage;
    private Integer totalSize;
    private List resultDetails;
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultMessage() {
		return resultMessage;
	}
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	public Integer getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(Integer totalSize) {
		this.totalSize = totalSize;
	}
	public List getResultDetails() {
		return resultDetails;
	}
	public void setResultDetails(List resultDetails) {
		this.resultDetails = resultDetails;
	}
    
    
    

}
