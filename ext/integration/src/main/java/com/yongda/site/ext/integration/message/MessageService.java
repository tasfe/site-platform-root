package com.yongda.site.ext.integration.message;

import com.netfinworks.mns.client.domain.WxNotifyParam;
import com.netfinworks.site.domain.exception.ErrorCodeException;
import com.yongda.site.domain.domain.message.PersonalMsgQuery;
import com.yongda.site.domain.domain.message.PersonalMsgQueryResult;

public interface MessageService {
	
	public PersonalMsgQueryResult queryPersonalMsg(PersonalMsgQuery query)throws ErrorCodeException.CommonException;
	
	/**
	 * 更新已读
	 * @param memberId 会员ID
	 * @param msgOrderNo 消息单号
	 * @param msgType 消息类型
	 * @throws ErrorCodeException.CommonException
	 */
	public void readPersonalMsg(String memberId,String msgOrderNo,String msgType)throws ErrorCodeException.CommonException;
	
	/**
	 * 发送微信通知
	 * @param memberId
	 * @param templateId
	 * @param param
	 * @return
	 */
	public void sendWxMsg(String memberId,String templateId,WxNotifyParam param)throws ErrorCodeException.CommonException;
}
