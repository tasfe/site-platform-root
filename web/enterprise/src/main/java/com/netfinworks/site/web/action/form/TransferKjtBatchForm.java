/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月15日
 */
package com.netfinworks.site.web.action.form;
import java.io.Serializable;
import javax.validation.constraints.Pattern;
import com.netfinworks.site.core.common.constants.RegexRule;
/**
 * 批量永达互联网金融转账收款信息Form
 * @author xuwei
 * @date 2014年7月15日
 */
public class TransferKjtBatchForm implements Serializable {
	private static final long serialVersionUID = 1468850584685697567L;

	// 永达互联网金融账户
	private String kjtAccount;
	
	// 账户名称
	private String kjtAccountName;
	
	// 转账金额（元）
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "转账金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String transferAmount;
	
	// 服务费
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "服务费格式不正确（小数点前最多19位，小数点后最多2位）")
	private String serviceFee;
	
	// 收款人联系手机
	@Pattern(regexp = RegexRule.MOBLIE, message = "手机号码格式不正确")
	private String mobile;
	
	// 转账备注
	private String remark;
	
	//商户订单号
	private String sourceDetailNo;

	public String getKjtAccount() {
		return kjtAccount;
	}

	public void setKjtAccount(String kjtAccount) {
		this.kjtAccount = kjtAccount;
	}

	public String getTransferAmount() {
		return transferAmount;
	}

	public void setTransferAmount(String transferAmount) {
		this.transferAmount = transferAmount;
	}

	public String getKjtAccountName() {
		return kjtAccountName;
	}

	public void setKjtAccountName(String kjtAccountName) {
		this.kjtAccountName = kjtAccountName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getServiceFee() {
		return serviceFee;
	}

	public void setServiceFee(String serviceFee) {
		this.serviceFee = serviceFee;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSourceDetailNo() {
		return sourceDetailNo;
	}

	public void setSourceDetailNo(String sourceDetailNo) {
		this.sourceDetailNo = sourceDetailNo;
	}
	
	
	
}
