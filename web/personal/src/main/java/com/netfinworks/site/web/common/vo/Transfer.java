/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.netfinworks.site.web.common.vo;

import com.netfinworks.site.domain.domain.member.BaseMember;

/**
 * 转账信息
 * @author xuwei
 * @date 2014年7月11日
 */
public class Transfer {
	// 序号
	private int orderNo;
	
	// 姓名
	private String name;

	// 联系人
	private String contact;
	
	// 金额
	private String money;
	
	// 备注
	private String remark;
	
	// 是否成功，false-失败，true-成功
	private boolean success;
	
	// 错误信息
	private String errorMsg;
	private BaseMember member;
	
	public BaseMember getMember() {
		return member;
	}

	public void setMember(BaseMember member) {
		this.member = member;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
