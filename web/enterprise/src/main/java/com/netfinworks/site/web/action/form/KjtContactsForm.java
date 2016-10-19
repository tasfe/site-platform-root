/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月16日
 */
package com.netfinworks.site.web.action.form;

import java.util.List;

/**
 * 永达互联网金融账户联系人
 * @author xuwei
 * @date 2014年7月16日
 */
public class KjtContactsForm {
	/** 永达互联网金融账户列表 */
	private List<String> kjtAccountList;
	
	/** 备注列表 */
	private List<String> remarkList;
	
	public List<String> getKjtAccountList() {
		return kjtAccountList;
	}

	public void setKjtAccountList(List<String> kjtAccountList) {
		this.kjtAccountList = kjtAccountList;
	}

	public List<String> getRemarkList() {
		return remarkList;
	}

	public void setRemarkList(List<String> remarkList) {
		this.remarkList = remarkList;
	}
	
 }
