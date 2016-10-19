package com.netfinworks.site.domain.domain.bank;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>分行信息</p>
 * @author qinde
 * @version $Id: BankBranch.java, v 0.1 2013-11-29 下午4:25:43 qinde Exp $
 */
public class BankBranchShort implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -7056422307315254159L;
    /**id*/
    private long              id;
    /**联行号    */
    private String            no;
    /**分行名称    */
    private String            name;
    /**分行简称    */
    private String            sName;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
