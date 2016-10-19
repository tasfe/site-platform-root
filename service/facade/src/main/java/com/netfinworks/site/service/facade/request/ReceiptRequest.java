package com.netfinworks.site.service.facade.request;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * <p>注释</p>
 * @author zhangyun.m
 * @version $Id: ElectronicDocumentsRequest.java, v 0.1 2014年7月1日 下午1:17:28 zhangyun.m Exp $
 */
public class ReceiptRequest extends OperationEnvironment implements Serializable {
    private static final long serialVersionUID = -1L;

    /** 交易凭证号 */
    private String            tradeVoucherNo;
    /** 对账单类型 */
    private String            tradeType;


    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeVoucherNo() {
        return tradeVoucherNo;
    }

    public void setTradeVoucherNo(String tradeVoucherNo) {
        this.tradeVoucherNo = tradeVoucherNo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
