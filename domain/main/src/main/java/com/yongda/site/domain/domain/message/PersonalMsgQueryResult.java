package com.yongda.site.domain.domain.message;

import java.util.List;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.site.core.common.util.PageQuery;

public class PersonalMsgQueryResult {
	private PageQuery queryBase;
	
	private List<PersonalMessage> messages;


	public PageQuery getQueryBase() {
		return queryBase;
	}

	public void setQueryBase(PageQuery queryBase) {
		this.queryBase = queryBase;
	}

	public List<PersonalMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<PersonalMessage> messages) {
		this.messages = messages;
	}
}
