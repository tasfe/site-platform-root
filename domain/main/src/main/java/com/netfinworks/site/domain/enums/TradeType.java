/**
 * 
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>交易类型</p>
 * @author fjl
 * @version $Id: TradeType.java, v 0.1 2013-11-28 下午1:30:06 fjl Exp $
 */
public enum TradeType {
    RECHARGE("recharge","deposit","10101","10010001","充值", "11"),
    TRANSFER("transfer","transfer","10310","10020001","转账", "3"),
    PAYMENT("payment","trade",null,null,"付款", "10"),
    REFUND("refund","refund","10000",null,"退款", "4"),
    WITHDRAW("withdraw","fundout","10210","10030001","普通提现（T+N）", "5"),
    WITHDRAW_INSTANT("withdraw","fundout","10211","10030001","快速提现（实时）", "6"),
    COLLECT_TO_BANK("collecttobank","fundout","10240","30040004","资金归集到银行卡","14"),
    COLLECT_TO_KJT("collecttokjt","fundout","10241","10020001","资金归集到账户","15"),
    PAY_INSTANT("instantpay","trade","20201","20040001","及时到账交易", "12"),
    PAY_ENSURE("ensurepay","trade","20202","20040001","担保交易", "13"),
    PAY_TO_BANK("paytobank","fundout","10220","30040004","转账到银行卡", "1"),
    PAY_TO_BANK_INSTANT("paytobank","fundout","10221","30040004","转账到银行卡（实时）", "2"),
    PAYOFF_TO_BANK("payofftobank","fundout","10230","30040004","代发工资到银行卡", "7"),
    PAYOFF_TO_KJT("payofftokjt","fundout","10231","10020001","代发工资到账户", "8"),
    FREEZE("freeze","freeze","40101","30040004","冻结解冻", "9"),
    baoli_withholding("baoliwithholding","fundout","10250","10020001","保理代扣到账户","16"),
    baoli_repayment("baolirepayment","fundout","10251","10020001","保理还款到账户","17"),
    baoli_loan("baoliloan","fundout","10252","10020001","保理放贷","18"),
    phone_recharge("phoneRecharge","trade","20203","20040001","话费充值","19"),
    bank_withholding("bankwithholding","trade","20204","20040001","银行卡代扣", "20"),
    auto_fundout("autofundout","fundout","10222","30040004","自动打款", "21"),
    FUND_SUBSCRIBE("fundsubscribe","trade","90110","20040001","基金申购", "22"),
    FUND_REDEMPTION_INSTANT("fundredemptioninstant","trade","90200","10010001","基金赎回到账户（T+0）", "23"),
    FUND_REDEMPTION("fundredemption","trade","90201","10020001","基金赎回到账户（垫资）", "24"),
    FUND_REDEMPTION_TO_BANK("fundredemptiontobank","trade","90210","10030001","基金赎回到卡（T+N）", "25"),
    FUND_REDEMPTION_TO_BANK_INSTANT("fundredemptiontobankinstant","trade","90211","10030001","基金赎回到卡（实时）", "26"),
    POS("pos","trade","20205","20040001","银商POS","30")
    ;
    /** 代码 */
    private final String code;
    /** 业务产品标识*/
    private final String bizProductIdentity;
    /** 业务产品编码 */
    private final String bizProductCode;
    /** 支付产品编码*/
    private final String payProductCode;
    /** 信息 */
    private final String message;
    /** 付款类型 */
    private final String payCode;
    //1-付款到卡（T+N）, 2-付款到卡（快速）, 3-转账, 4-退款, 5-普通提现, 6-快速提现, 7-代发工资到卡, 8-代发工资到账户,
    //9-冻结解冻, 10-付款, 11-充值, 12-及时到账交易, 13-担保交易, 14-归集到卡, 15-归集到账户, 16-保理代扣到账户, 17-保理还款到账户, 18-保理放贷

    TradeType(String code,String bizProductIdentity, String bizProductCode,String payProductCode, String message, String payCode) {
        this.code = code;
        this.bizProductIdentity = bizProductIdentity;
        this.bizProductCode = bizProductCode;
        this.payProductCode = payProductCode;
        this.message = message;
        this.payCode = payCode;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static TradeType getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (TradeType item : TradeType.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * 通过业务产品编码获取枚举项
     * @param code
     * @return
     */
	public static TradeType getByBizProductCode(String bizProductCode) {
		if (bizProductCode == null) {
            return null;
        }

        for (TradeType item : TradeType.values()) {
			if (item.getBizProductCode() != null) {
				if (item.getBizProductCode().equals(bizProductCode)) {
					return item;
				}
			}
        }
        return null;
    }
    

    public String getBizProductIdentity() {
        return bizProductIdentity;
    }

    public String getBizProductCode() {
        return bizProductCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPayProductCode() {
        return payProductCode;
    }

    public String getPayCode() {
        return payCode;
    }
    
}
