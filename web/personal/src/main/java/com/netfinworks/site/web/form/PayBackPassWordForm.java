package com.netfinworks.site.web.form;

import java.io.Serializable;
import java.util.Date;

public class PayBackPassWordForm implements Serializable{

    private static final long serialVersionUID = 1L;
    //证件类型
    private String cardType;
    //身份证类型
    private String idCard;
    //身份证号码正面
    private String certificationFront;
    //身份证号码反面
    private String certificationBack;
    //身份证号码到期时间
    private Date   certificationExpireDate;
    //身份证到期类型 1:有期限;2长期
    private String timeType;
    //使用过的提现银行卡
    private String withdrawalCardId;
    //使用过的充值银行卡
    private String rechargeCardId;
    //实名认证的姓名
    private String authName;
    //邮箱
    private String email;

    public PayBackPassWordForm() {
        super();
    }

    public PayBackPassWordForm(String cardType, String idCard, String certificationFront,
                               String certificationBack, Date certificationExpireDate, String timeType,
                               String withdrawalCardId, String rechargeCardId, String authName,String email) {
        super();
        this.cardType = cardType;
        this.idCard = idCard;
        this.certificationFront = certificationFront;
        this.certificationBack = certificationBack;
        this.certificationExpireDate = certificationExpireDate;
        this.timeType = timeType;
        this.withdrawalCardId = withdrawalCardId;
        this.rechargeCardId = rechargeCardId;
        this.authName = authName;
        this.email=email;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getCertificationFront() {
        return certificationFront;
    }

    public void setCertificationFront(String certificationFront) {
        this.certificationFront = certificationFront;
    }

    public String getCertificationBack() {
        return certificationBack;
    }

    public void setCertificationBack(String certificationBack) {
        this.certificationBack = certificationBack;
    }

    public Date getCertificationExpireDate() {
        return certificationExpireDate;
    }

    public void setCertificationExpireDate(Date certificationExpireDate) {
        this.certificationExpireDate = certificationExpireDate;
    }
    

    public String getTimeType() {
        return timeType;
    }

    public void setTimeType(String timeType) {
        this.timeType = timeType;
    }

    public String getWithdrawalCardId() {
        return withdrawalCardId;
    }

    public void setWithdrawalCardId(String withdrawalCardId) {
        this.withdrawalCardId = withdrawalCardId;
    }

    public String getRechargeCardId() {
        return rechargeCardId;
    }

    public void setRechargeCardId(String rechargeCardId) {
        this.rechargeCardId = rechargeCardId;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
