package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>省信息</p>
 * @author 覃德
 * @version $Id: Province.java, v 0.1 2013-11-28 上午10:39:41 qinde Exp $
 */
public class Province implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1056045143054995661L;
    /**省ID   */
    long                      provId;
    /**省名称    */
    String                    provName;
    /**省简称    */
    String                    provShortName;
    /**城市信息    */
    List<City>                cityInfos;

    public long getProvId() {
        return provId;
    }

    public void setProvId(long provId) {
        this.provId = provId;
    }

    public String getProvName() {
        return provName;
    }

    public void setProvName(String provName) {
        this.provName = provName;
    }

    public String getProvShortName() {
        return provShortName;
    }

    public void setProvShortName(String provShortName) {
        this.provShortName = provShortName;
    }

    public List<City> getCityInfos() {
        return cityInfos;
    }

    public void setCityInfos(List<City> cityInfos) {
        this.cityInfos = cityInfos;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
