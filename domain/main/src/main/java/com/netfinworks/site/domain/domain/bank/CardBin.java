package com.netfinworks.site.domain.domain.bank;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>卡bin信息</p>
 * @author Dexter.qin
 * @version $Id: CardBin.java, v 0.1 2014-5-5 下午3:45:26 qinde Exp $
 */
public class CardBin implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -2348704339581911715L;
    /**银行编码*/
    private String            bankCode;
    /**银行名称*/
    private String            bankName;
    /**卡类型代码    */
    private String            cardType;
    /**卡类型名称    */
    private String            cardTypeName;
    /**卡种名称    */
    private String            cardName;
    /**卡bin号    */
    private String            binNo;
    /**卡长度    */
    private int               cardLength;
    /**银行代号    */
    private String            bankNo;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getBinNo() {
        return binNo;
    }

    public void setBinNo(String binNo) {
        this.binNo = binNo;
    }

    public int getCardLength() {
        return cardLength;
    }

    public void setCardLength(int cardLength) {
        this.cardLength = cardLength;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
