package com.yongda.site.domain.domain.insurance;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/** 
* @ClassName: InsuranceQuery 
* @Description: TODO() 
* @author slong
* @date 2016年5月20日 上午10:38:39 
*  
*/
public class InsuranceRemoteQuery implements Serializable {

	/** 描述 */
	private static final long serialVersionUID = 7814634869739672447L;
	
	private String sKey;
	
    /** 保单号 */
    private String policyNo;
    
    /** 保险公司 */
    private String company;
    
    /** 被保人身份证号 */
    private String cardNo;
    
    /** 被保人姓名 */
    private String insuredName;
    
    /** 被保人手机号 */
    private String insruedMobile;
    
    /** 发动机编号 */
    private String carEngineId;

	public String getsKey() {
		return sKey;
	}

	public void setsKey(String sKey) {
		this.sKey = sKey;
	}

	public String getPolicyNo() {
		return policyNo;
	}

	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getInsuredName() {
		return insuredName;
	}

	public void setInsuredName(String insuredName) {
		this.insuredName = insuredName;
	}

	public String getInsruedMobile() {
		return insruedMobile;
	}

	public void setInsruedMobile(String insruedMobile) {
		this.insruedMobile = insruedMobile;
	}

	public String getCarEngineId() {
		return carEngineId;
	}

	public void setCarEngineId(String carEngineId) {
		this.carEngineId = carEngineId;
	}
	
	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
