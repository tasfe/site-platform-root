package com.netfinworks.site.domain.domain.auth;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AuthVO implements Serializable {
    private static final long serialVersionUID = -7899499587665035019L;

    /**
     * 会员编号
     */
    private String            memberId;

    /**
     * 操作员编号 
     */
    private String            operatorId;

    /**
     * 功能编号 
     */
    private String            functionId;

    /**
     * 系统编号
     */
    private String              sourceId;
    
    /**
	 * 角色名称
	 */
	private String operatorRoleName;

	/**
	 * 角色ID
	 */
	private String operatorRoleId;

	/**
	 * 会员角色ID
	 */
	private String memberRoleId;

	/**
	 * 操作员类型
	 */
	private String operatorType;

	/**
	 * 请求操作员编号
	 */
	private String requestOperator;

	/**
	 * 功能列表
	 */
	private List<String> functionList;

	/**
	 * 备注
	 */
    private String memo;
    
    public AuthVO() {

    }

	public AuthVO(String memberId, String operatorId, String functionId, String sourceId, String memo) {
        super();
        this.memberId = memberId;
        this.operatorId = operatorId;
        this.functionId = functionId;
        this.sourceId=sourceId;
        this.memo=memo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

	public String getOperatorRoleName() {
		return operatorRoleName;
	}

	public void setOperatorRoleName(String operatorRoleName) {
		this.operatorRoleName = operatorRoleName;
	}

	public String getOperatorRoleId() {
		return operatorRoleId;
	}

	public void setOperatorRoleId(String operatorRoleId) {
		this.operatorRoleId = operatorRoleId;
	}

	public List<String> getFunctionList() {
		return functionList;
	}

	public void setFunctionList(List<String> functionList) {
		this.functionList = functionList;
	}

	public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

	public String getOperatorType() {
		return operatorType;
	}

	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}

	public String getRequestOperator() {
		return requestOperator;
	}

	public void setRequestOperator(String requestOperator) {
		this.requestOperator = requestOperator;
	}

	public String getMemberRoleId() {
		return memberRoleId;
	}

	public void setMemberRoleId(String memberRoleId) {
		this.memberRoleId = memberRoleId;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
