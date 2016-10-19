package com.yongda.site.service.personal.facade.request;

import java.io.Serializable;

import javax.validation.constraints.Size;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.yongda.site.core.common.annotation.HttpField;

public class BaseRequest implements Serializable{

	private static final long serialVersionUID = -600297848998283701L;
	
	private String service;
	@NotBlank(message="version不能为空！")
	private String version;
	private String memberId;
	@HttpField(name="_input_charset")
	private String inputCharset;
	@HttpField(name="partner_id")
	private String partnerId;
	@NotBlank(message="plat_usr_id 不能为空")
	@Size(min=1,max=32,message="plat_usr_id 应为1-32位")
	@HttpField(name="plat_usr_id")
	private String platUserId;
	private String deviceid;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getInputCharset() {
		return inputCharset;
	}

	public void setInputCharset(String inputCharset) {
		this.inputCharset = inputCharset;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getPlatUserId() {
		return platUserId;
	}

	public void setPlatUserId(String platUserId) {
		this.platUserId = platUserId;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

	@Override
	public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
