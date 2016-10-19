package com.netfinworks.site.domain.domain.info;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>角色</p>
 * @author eric
 * @version $Id: Role.java, v 0.1 2013-7-16 下午7:14:35  Exp $
 */
public class Role {
    /** 代码 */
    private String  code;
    /** 名称 */
    private String  name;
    /** 描述 */
    private String  description;
    /** 是否可用 */
    private boolean enable;
    /** 备注 */
    private String  memo;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
