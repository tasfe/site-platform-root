package com.yongda.site.ext.integration.message.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.mns.client.INotifyClient;
import com.netfinworks.mns.client.Response;
import com.netfinworks.mns.client.domain.NotifyUpdateCondition;
import com.netfinworks.mns.client.domain.WxNotifyParam;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException;
import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;
import com.yongda.site.domain.domain.message.PersonalMsgQuery;
import com.yongda.site.domain.domain.message.PersonalMsgQueryResult;
import com.yongda.site.ext.integration.message.MessageService;
import com.yongda.site.ext.integration.message.convert.MessageConvert;

@Service("messageService")
public class MessageServiceImpl implements MessageService{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource(name = "mnsClient")
    private INotifyClient mnsClient;
	
	@Override
	public PersonalMsgQueryResult queryPersonalMsg(PersonalMsgQuery query)
			throws CommonException {
		try {
			Response response = mnsClient.queryMsgNotify(query.getMemberId(),MessageConvert.createQueryRequest(query));
			if(response.isSuccess()){
				return MessageConvert.buildResponse(response);
			}else{
				throw response.getException();
			}
		} catch (Exception e) {
			logger.error("调用mns接口异常 ",e);
			throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"远程mns接口异常");
		}
	}

	@Override
	public void readPersonalMsg(String memberId, String msgOrderNo,
			String msgType) throws CommonException {
		try {
			NotifyUpdateCondition condition = new NotifyUpdateCondition();
			condition.setAddress(memberId);
			condition.setMsgOrderNo(msgOrderNo);
			condition.setMsgType(msgType);
			Response response = mnsClient.updateNotifyRead(condition);
			if(!response.isSuccess()){
				throw response.getException();
			}
		} catch (Exception e) {
			logger.error("调用mns接口异常 ",e);
			throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"远程mns接口异常");
		}
	}

	@Override
	public void sendWxMsg(String memberId, String templateId,
			WxNotifyParam param)throws CommonException {
		try {
			Response response = mnsClient.sendWxNotify(memberId, templateId, param);
			if(!response.isSuccess()){
				logger.error("调用mns接口发送信息错误 ",response);
				throw response.getException();
			}
		} catch (Exception e) {
			logger.error("调用mns接口发送信息异常 ",e);
			throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"远程mns接口异常");
		}
	}
}
