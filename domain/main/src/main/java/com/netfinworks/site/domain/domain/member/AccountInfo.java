/**
 *
 */
package com.netfinworks.site.domain.domain.member;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>账户信息</p>
 * @author guyihui
 * @version $Id: AccountInfo.java, v 0.1 2014-5-22 下午2:12:13 guyihui Exp $
 */
public class AccountInfo {
	private String accountType;

	private String accountId;

	public AccountInfo(){

	}

	public AccountInfo(String accountType, String accountId){
		this.accountType = accountType;
		this.accountId = accountId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	@Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }

	public boolean isValid() {
		return StringUtils.isNotBlank(this.accountType) && StringUtils.isNotBlank(this.accountId);
	}

}
