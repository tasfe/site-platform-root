/**
 *
 */
package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>会员信息</p>
 * @author fjl
 * @version $Id: MemberAndAccount.java, v 0.1 2013-11-13 上午10:26:41 fjl Exp $
 */
public class MemberAndAccount implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 6347219847044332395L;

    private String            memberId;

    private String            operatorId;

    private String            accountId;

	private String merchantId;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
