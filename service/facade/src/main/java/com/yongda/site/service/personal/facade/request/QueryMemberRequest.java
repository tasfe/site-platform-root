package com.yongda.site.service.personal.facade.request;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * 基本户查询条件
 * @author yp
 *
 */
public class QueryMemberRequest extends BaseRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4871670368429099552L;

    /**
    * 会员编号
    */
    private String            memberId;
    /**
    * 账户类型
    */
    private Long              accountType;

    public QueryMemberRequest() {

    }

    public QueryMemberRequest(String memberId, Long accountType) {
        this.memberId = memberId;
        this.accountType = accountType;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Long getAccountType() {
        return accountType;
    }

    public void setAccountType(Long accountType) {
        this.accountType = accountType;
    }

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
}
