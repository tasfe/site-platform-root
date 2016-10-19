/*
 * Copyright 2014 netfinworks.com, Inc. All rights reserved.
 */
package com.netfinworks.site.service.facade.model;

import java.util.HashMap;
import java.util.Map;

/**
 * <p></p>
 *
 * @author zhangjiewen
 * @version $Id: AppNotifyParam.java, v 0.1 14-2-26 下午3:02 zhangjiewen Exp $
 */
public class WxNotifyData {
    private String url;
    private Map<String,TemplateDataAttr> data = new HashMap<String, TemplateDataAttr>();
    
	public void addDataParam(String dataName,String dataValue,String dataColor){
    	data.put(dataName, new TemplateDataAttr(dataValue, dataColor));
    }
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
    public Map<String, TemplateDataAttr> getData() {
		return data;
	}

	public void setData(Map<String, TemplateDataAttr> data) {
		this.data = data;
	}



	static public class TemplateDataAttr{
    	private String dataValue;
    	private String dataColor;
    	
		public TemplateDataAttr() {
			super();
		}
		public TemplateDataAttr(String dataValue, String dataColor) {
			super();
			this.dataColor = dataColor;
		}
		public String getDataValue() {
			return dataValue;
		}
		public void setDataValue(String dataValue) {
			this.dataValue = dataValue;
		}
		public String getDataColor() {
			return dataColor;
		}
		public void setDataColor(String dataColor) {
			this.dataColor = dataColor;
		}
    }
}
