package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class LoginMethodInfoVO implements Serializable {
    private static final long serialVersionUID = -620216007148257208L;

    private Long              id;                                     //登录方式ID                            
    private String            memberId;                               //会员编号                              
    private String            operatorId;                             //操纵员编号                            
    private Integer           loginNameType;                          //登录名类型
    private String            loginName;                              //登录名                                
    private String            platFormType;                           //所属平台类型

    public LoginMethodInfoVO() {

    }

    public LoginMethodInfoVO(Long id, String memberId, String operatorId, Integer loginNameType,
                             String loginName, String platFormType) {
        super();
        this.id = id;
        this.memberId = memberId;
        this.operatorId = operatorId;
        this.loginNameType = loginNameType;
        this.loginName = loginName;
        this.platFormType = platFormType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getLoginNameType() {
        return loginNameType;
    }

    public void setLoginNameType(Integer loginNameType) {
        this.loginNameType = loginNameType;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPlatFormType() {
        return platFormType;
    }

    public void setPlatFormType(String platFormType) {
        this.platFormType = platFormType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
