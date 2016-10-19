package com.yongda.site.ext.integration.message.convert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.netfinworks.mns.client.Response;
import com.netfinworks.mns.client.domain.ClassicPage;
import com.netfinworks.mns.client.domain.NotifyQueryCondition;
import com.netfinworks.mns.client.domain.PageInfo;
import com.netfinworks.mns.client.domain.QueryResult;
import com.netfinworks.site.core.common.util.PageQuery;
import com.yongda.site.domain.domain.message.PersonalMessage;
import com.yongda.site.domain.domain.message.PersonalMsgQuery;
import com.yongda.site.domain.domain.message.PersonalMsgQueryResult;

public class MessageConvert {
	public static NotifyQueryCondition createQueryRequest(PersonalMsgQuery req){
		NotifyQueryCondition request = new NotifyQueryCondition();
		if(req.getQueryBase() != null){
			ClassicPage page = new ClassicPage();
			page.setCurrentPage(req.getQueryBase().getCurrentPage());
			page.setPageSize(req.getQueryBase().getPageSize());
			request.setPage(page);
		}
		request.setAddress(req.getMemberId());
		request.setAppId(req.getAppId());
		request.setIsRead(req.getIsRead());
		request.setMsgType(req.getMsgType());
		request.setStatus(request.getStatus());
		request.setTemplateId(request.getTemplateId());
		request.setTitle(request.getTitle());
		return request;
	}
	
	public static PersonalMsgQueryResult buildResponse(Response resp){
		QueryResult<Map> msgData = (QueryResult<Map>) resp.getData();
		PersonalMsgQueryResult response = new PersonalMsgQueryResult();
		List<PersonalMessage> msgList = new ArrayList<PersonalMessage>();
		if(CollectionUtils.isNotEmpty(msgData.getItems())){
			for(Map dataMap:msgData.getItems()){
				PersonalMessage msg = new PersonalMessage();
				msg.setAppId(MapUtils.getString(dataMap, "appId"));
				msg.setContent(MapUtils.getString(dataMap, "content"));
				msg.setGmtCreate(new Date(MapUtils.getLong(dataMap, "gmtCreate")));
				msg.setMsgId(MapUtils.getString(dataMap, "msgId"));
				msg.setMsgOrderNo(MapUtils.getString(dataMap, "msgOrderNo"));
				msg.setRead(MapUtils.getBoolean(dataMap, "read",false));
				msg.setSubject(MapUtils.getString(dataMap, "subject"));
				msgList.add(msg);
			}
		}
		PageInfo remotePage = msgData.getPageInfo();
		response.setQueryBase(new PageQuery());
		response.getQueryBase().setCurrentPage(remotePage.getCurrentPage());
		response.getQueryBase().setPageSize(remotePage.getPageSize());
		response.getQueryBase().setTotalItem(remotePage.getTotalCount());
		response.setMessages(msgList);
		return response;
	}
}
