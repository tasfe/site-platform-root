/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domain.domain.request;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.info.FileInfo;
import com.netfinworks.site.domain.enums.AuthType;

/**
 * 通用说明： 认证信息
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-25 下午8:15:25
 *
 */
public class AuthInfoRequest extends OperationEnvironment implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 8262640773784682432L;
    /**会员ID*/
    private String            memberId;
    /**认证类型*/
    private AuthType          authType;
    /**证件类型*/
    private String            certType;
    /**证件号*/
    private String            authNo;
    /**认证名称*/
    private String            authName;
    /**认证文件*/
    private List<FileInfo>    authFiles;
    /**操作员*/
    private String            operator;

    private String            message;
    /**扩展字段*/
    private String            ext;
	/** 是否需要复核 */
	private Boolean isChecked;
	/** 认证结果 */
	private Boolean result = true;

	private List<String> orderNoList;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getAuthNo() {
        return authNo;
    }

    public void setAuthNo(String authNo) {
        this.authNo = authNo;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public List<FileInfo> getAuthFiles() {
        return authFiles;
    }

    public void setAuthFiles(List<FileInfo> authFiles) {
        this.authFiles = authFiles;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

	public List<String> getOrderNoList() {
		return orderNoList;
	}

	public void setOrderNoList(List<String> orderNoList) {
		this.orderNoList = orderNoList;
	}

	public Boolean isChecked() {
		return isChecked;
	}

	public void setChecked(Boolean isChecked) {
		this.isChecked = isChecked;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
