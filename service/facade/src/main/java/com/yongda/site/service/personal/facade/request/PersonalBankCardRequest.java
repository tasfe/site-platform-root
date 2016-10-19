package com.yongda.site.service.personal.facade.request;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

public class PersonalBankCardRequest extends BaseRequest{
		//@NotBlank(message = "银行名称不能为空")
		//@Pattern(regexp = RegexRule.CHAR_CHINESE,message="请输入中文的银行名称")
		private String bankName;// 银行名称
		
		@NotBlank(message = "银行编码不能为空")
		private String bankCode;// 银行编码
		
		@NotBlank(message = "支行名称不能为空")
		private String branchName;// 支行名称
		
		//@NotBlank(message = "联行号不能为空")
		private String branchNo;// 联行号
		
		@NotBlank(message = "银行卡类型不能为空")
		@Pattern(regexp = RegexRule.INTEGER,message="卡类型只能为数字")
		private String cardType;// 卡类型 1-借记卡 2-信用卡
		
		@NotBlank(message = "银行卡属性不能为空")
		@Pattern(regexp = RegexRule.INTEGER,message="卡类型只能为数字")
		private String cardAttribute;// 卡属性 0-对公 1-对私
		
		@NotBlank(message = "省份名称不能为空")
		private String provName; // 省份名称
		
		@NotBlank(message = "城市名称不能为空")
		private String cityName; // 城市名称
		
		@NotBlank(message = "卡号不能为空")
		@Pattern(regexp = RegexRule.BANKCARD_NO, message = "卡号格式不正确（8-32位数字）")
		private String bankAccountNum; // 卡号
		
		@NotBlank(message = "户名不能为空")
	    private String  realName;//钱包会员名称
		
		//@NotBlank(message = "操作员id不能为空")
	    private String  operatorId;//操作员id
		
		//@NotBlank(message = "会员标识不能为空")
	    private String  memberIdentity;//会员标识
		
		@NotBlank(message = "会员id不能为空")
		private String memberId;
		
		public String getBankName() {
			return bankName;
		}
		public void setBankName(String bankName) {
			this.bankName = bankName;
		}
		public String getBankCode() {
			return bankCode;
		}
		public void setBankCode(String bankCode) {
			this.bankCode = bankCode;
		}
		public String getBranchName() {
			return branchName;
		}
		public void setBranchName(String branchName) {
			this.branchName = branchName;
		}
		public String getBranchNo() {
			return branchNo;
		}
		public void setBranchNo(String branchNo) {
			this.branchNo = branchNo;
		}
		public String getCardType() {
			return cardType;
		}
		public void setCardType(String cardType) {
			this.cardType = cardType;
		}
		public String getCardAttribute() {
			return cardAttribute;
		}
		public void setCardAttribute(String cardAttribute) {
			this.cardAttribute = cardAttribute;
		}
		public String getProvName() {
			return provName;
		}
		public void setProvName(String provName) {
			this.provName = provName;
		}
		public String getCityName() {
			return cityName;
		}
		public void setCityName(String cityName) {
			this.cityName = cityName;
		}
		public String getBankAccountNum() {
			return bankAccountNum;
		}
		public void setBankAccountNum(String bankAccountNum) {
			this.bankAccountNum = bankAccountNum;
		}
		
		public String getRealName() {
			return realName;
		}
		public void setRealName(String realName) {
			this.realName = realName;
		}
		public String getOperatorId() {
			return operatorId;
		}
		public void setOperatorId(String operatorId) {
			this.operatorId = operatorId;
		}
		public String getMemberIdentity() {
			return memberIdentity;
		}
		public void setMemberIdentity(String memberIdentity) {
			this.memberIdentity = memberIdentity;
		}
		public String getMemberId() {
			return memberId;
		}
		public void setMemberId(String memberId) {
			this.memberId = memberId;
		}
		
}

