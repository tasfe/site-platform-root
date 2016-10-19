package com.netfinworks.site.service.facade.response;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.BaseResult;
import com.netfinworks.site.service.facade.model.Receipt;

/**
 * <p>电子对账单响应</p>
 * @author zhangyun.m
 * @version $Id: ElectronicDocumentRespose.java, v 0.1 2014年7月1日 下午5:20:24 zhangyun.m Exp $
 */
@XmlType(namespace="http://response.site.netfinworks.com")
public class ReceiptRespose extends BaseResult{

    /**
     * 
     */
    private static final long serialVersionUID = -1L;

    /** 电子对账单  */
    private Receipt receipt;

    /** 买家账户 解密 */
    private String  encryptBuyerAccountNo;

    /** 卖家账户 解密 */
    private String  encryptSellerAccountNo;
    

    public String getEncryptBuyerAccountNo() {
        return encryptBuyerAccountNo;
    }

    public void setEncryptBuyerAccountNo(String encryptBuyerAccountNo) {
        this.encryptBuyerAccountNo = encryptBuyerAccountNo;
    }

    public String getEncryptSellerAccountNo() {
        return encryptSellerAccountNo;
    }

    public void setEncryptSellerAccountNo(String encryptSellerAccountNo) {
        this.encryptSellerAccountNo = encryptSellerAccountNo;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
