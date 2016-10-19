package com.netfinworks.site.core.dal.dataobject;

import java.util.Date;

/**
 * <p>角色资源关系</p>
 * @author eric
 * @version $Id: RoleResourceDO.java, v 0.1 2013-7-17 上午11:04:12  Exp $
 */
public class RoleResourceDO {
    /** 角色代码 */
    private String roleCode;
    /** 资源代码 */
    private String resourceCode;
    /** 创建时间 */
    private Date   gmtCreate;

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

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

}
