package com.netfinworks.site.domain.domain.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>交易信息</p>
 * @author qinde
 * @version $Id: CreateTradeRequest.java, v 0.1 2013-11-29 下午9:46:18 qinde Exp $
 */
public class CreateTradeRequest {
	private String memberId;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
