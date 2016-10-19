package com.netfinworks.site.domain.domain.info;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>角色资源关系</p>
 * @author eric
 * @version $Id: RoleResourceRelation.java, v 0.1 2013-7-16 下午6:14:16  Exp $
 */
public class RoleResourceRelation {
    /** 角色代码 */
    private String roleCode;
    /** 资源代码 */
    private String resourceCode;

    public RoleResourceRelation() {
    }

    /**
     * 角色资源关系构�?
     * @param roleCode
     * @param resourceCode
     */
    public RoleResourceRelation(String roleCode, String resourceCode) {
        this.roleCode = roleCode;
        this.resourceCode = resourceCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
