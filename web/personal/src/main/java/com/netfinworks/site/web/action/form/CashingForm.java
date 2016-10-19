/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.netfinworks.site.web.action.form;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 提现输入Form
 * 
 * @author xuwei
 * @date 2014年7月11日
 */
public class CashingForm implements Serializable {
    private static final long serialVersionUID = 7657443973519179835L;

    // 提现金额
    @Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "提现金额格式不正确（小数点前最多19位，小数点后最多2位）")
    private String            money;

    // 银行卡绑定ID
    private String            bankCardId;

    // 提现银行编号
    private String            bankCode;

    // 提现银行
    private String            bankName;

    // 提现账户号
    private String            accountNo;

    // 提现类型
    private int               type;

    // 提现类型描述
    private String            typeDesc;

    // 服务费
    private String            serviceCharge;

    // 支付密码
    private String            payPassword;

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getBankCardId() {
        return bankCardId;
    }

    public void setBankCardId(String bankCardId) {
        this.bankCardId = bankCardId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public String getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(String serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

}
