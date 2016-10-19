/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.netfinworks.site.web.action.form;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Pattern;
import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 永达互联网金融账户转账页面输入Form
 * @author xuwei
 * @date 2014年7月11日
 */
public class KjtTransferForm implements Serializable {
	private static final long serialVersionUID = 1143145292411583072L;

	// 会员ID
	private String memberId;
	
	// 账户email地址列表
	private List<String> emailList;
	
	// 转账金额列表
	private List<String> moneyList;

	// 是否发送短信0-不发送，1-发送
	private int sendNoteMsg;
	
	// 转账总金额
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "转账金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String totalTransMoney;
	
	// 服务费
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "服务费格式不正确（小数点前最多19位，小数点后最多2位）")
	private String serviceCharge;
	
	// 总金额(转账总金额+服务费)
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "总金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String totalMoney;
	
	// 备注类型1-工资，2-还款，0-其他
	private int remarkType;
	
	// 备注
	private String remark;
	
	// 平台类型
	private String platformType;
	
	// 操作员登录名
	private String operLoginName;
	
	// 短信通知到的手机
	private String mobile;
	
	// 转账的目的账户总数
	private int transferCount;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public List<String> getEmailList() {
		return emailList;
	}

	public void setEmailList(List<String> emailList) {
		this.emailList = emailList;
	}

	public List<String> getMoneyList() {
		return moneyList;
	}

	public void setMoneyList(List<String> moneyList) {
		this.moneyList = moneyList;
	}

	public int getSendNoteMsg() {
		return sendNoteMsg;
	}

	public void setSendNoteMsg(int sendNoteMsg) {
		this.sendNoteMsg = sendNoteMsg;
	}

	public String getTotalTransMoney() {
		return totalTransMoney;
	}

	public void setTotalTransMoney(String totalTransMoney) {
		this.totalTransMoney = totalTransMoney;
	}

	public String getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(String totalMoney) {
		this.totalMoney = totalMoney;
	}

	public String getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(String serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public int getRemarkType() {
		return remarkType;
	}

	public void setRemarkType(int remarkType) {
		this.remarkType = remarkType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPlatformType() {
		return platformType;
	}

	public void setPlatformType(String platformType) {
		this.platformType = platformType;
	}

	public String getOperLoginName() {
		return operLoginName;
	}

	public void setOperLoginName(String operLoginName) {
		this.operLoginName = operLoginName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public int getTransferCount() {
		return transferCount;
	}

	public void setTransferCount(int transferCount) {
		this.transferCount = transferCount;
	}
	
}
