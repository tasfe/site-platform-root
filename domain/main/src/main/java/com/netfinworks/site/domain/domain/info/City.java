/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 通用说明： 城市
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-28 上午10:31:37
 *
 */
public class City implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -5552063986488149255L;
    /**城市ID    */
    long                      cityId;
    /**省ID    */
    long                      provId;
    /**城市名称   */
    String                    cityName;
    /**城市简称    */
    String                    cityShortName;
    /**地区信息    */
    List<Area>                areaInfos;

    public long getCityId() {
        return cityId;
    }

    public void setCityId(long cityId) {
        this.cityId = cityId;
    }

    public long getProvId() {
        return provId;
    }

    public void setProvId(long provId) {
        this.provId = provId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityShortName() {
        return cityShortName;
    }

    public void setCityShortName(String cityShortName) {
        this.cityShortName = cityShortName;
    }

    public List<Area> getAreaInfos() {
        return areaInfos;
    }

    public void setAreaInfos(List<Area> areaInfos) {
        this.areaInfos = areaInfos;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
