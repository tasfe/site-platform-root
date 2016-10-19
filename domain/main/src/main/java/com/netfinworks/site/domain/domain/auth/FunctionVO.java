package com.netfinworks.site.domain.domain.auth;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class FunctionVO implements Serializable {

    private static final long serialVersionUID = -6768780201679541729L;

    private String            functionId;
    private String            functionName;
    private String            appId;
    private int               canAssign;
    private String            functionAlias;
    private int               status;
    private int               sort;
    private int               visible;
    private String            parentId;
    private String            memo;
    private String            functionGroup;
    private int               functionType;
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getCanAssign() {
        return canAssign;
    }

    public void setCanAssign(int canAssign) {
        this.canAssign = canAssign;
    }

    public String getFunctionAlias() {
        return functionAlias;
    }

    public void setFunctionAlias(String functionAlias) {
        this.functionAlias = functionAlias;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getFunctionGroup() {
        return functionGroup;
    }

    public void setFunctionGroup(String functionGroup) {
        this.functionGroup = functionGroup;
    }

    public int getFunctionType() {
        return functionType;
    }

    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }

    public FunctionVO() {

    }

    public FunctionVO(String functionId, String functionName, String appId, int canAssign,
                      String functionAlias, int status, int sort, int visible, String parentId,
                      String memo, String functionGroup, int functionType) {
        super();
        this.functionId = functionId;
        this.functionName = functionName;
        this.appId = appId;
        this.canAssign = canAssign;
        this.functionAlias = functionAlias;
        this.status = status;
        this.sort = sort;
        this.visible = visible;
        this.parentId = parentId;
        this.memo = memo;
        this.functionGroup = functionGroup;
        this.functionType = functionType;
    }

}
