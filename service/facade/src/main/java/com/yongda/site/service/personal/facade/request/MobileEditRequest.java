package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.yongda.site.core.common.annotation.HttpField;

/**
 * 修改手机请求参数
 * @author yp
 *
 */
public class MobileEditRequest extends BaseRequest {

	private static final long serialVersionUID = -6917286978870563050L;
	
	/**
	 * 新的绑定手机号码
	 */
	private String newPhoneNumber;

	public String getNewPhoneNumber() {
		return newPhoneNumber;
	}

	public void setNewPhoneNumber(String newPhoneNumber) {
		this.newPhoneNumber = newPhoneNumber;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
