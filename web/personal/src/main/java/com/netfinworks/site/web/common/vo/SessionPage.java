/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月13日
 */
package com.netfinworks.site.web.common.vo;

import java.util.ArrayList;
import java.util.List;

import com.netfinworks.mns.client.domain.PageInfo;

/**
 * 会话数据分页
 * @author xuwei
 * @date 2014年8月13日
 */
public class SessionPage<T> {
	private PageInfo page;
	
	private List<T> dataList = null;
	
	public SessionPage() {
		page = new PageInfo();
		dataList = new ArrayList<T>();
	}
	
	public SessionPage(PageInfo page, List<T> dataList) {
		this.page = page;
		this.dataList = dataList;
	}

	public PageInfo getPage() {
		return page;
	}

	public void setPage(PageInfo page) {
		this.page = page;
	}

	public List<T> getDataList() {
		return dataList;
	}

	public void setDataList(List<T> dataList) {
		this.dataList = dataList;
	}
	
}
