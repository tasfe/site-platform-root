package com.yongda.site.app.form.wx;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

public class WxMessage {
	@NotBlank(message = "openId不能为空")
	private String openId;
	@NotBlank(message = "product不能为空")
	private String product;//商品名称
	@NotBlank(message = "price不能为空")
	private String price;//商品价格
	@NotBlank(message = "time不能为空")
	private String time;//消费时间
	//@NotBlank(message = "remark不能为空")
	private String remark;
	//@NotBlank(message = "token不能为空")
	private String token;
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
