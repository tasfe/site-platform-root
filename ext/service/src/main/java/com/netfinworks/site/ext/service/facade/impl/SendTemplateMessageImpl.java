package com.netfinworks.site.ext.service.facade.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.ext.service.facade.converter.AccessToken;
import com.netfinworks.site.service.facade.api.SendTemplateMessageFacade;
import com.netfinworks.urm.domain.info.UserAccreditInfo;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.ext.service.facade.personal.common.WeixinUtil;
import com.yongda.site.service.personal.facade.request.WxMessageRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public class SendTemplateMessageImpl implements SendTemplateMessageFacade{
	private static Logger logger = LoggerFactory
			.getLogger(SendTemplateMessageImpl.class);
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name = "xuCache")
	private XUCache<String> loginCache;
	
	@Resource(name = "personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Value("${wx_appid}")
	private String appid;

	@Value("${wx_appsecret}")
	private String appsecret;
	
	@Override
	public BaseResponse sendMessage(WxMessageRequest request) {
		BaseResponse response = new BaseResponse();
		try{	
			// 校验提交参数
			String errorMsg = commonValidator.validate(request);
			if (StringUtils.isNotEmpty(errorMsg)) {
				response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				response.setErrorMessage(errorMsg);
				response.setSuccess(false);
				logger.error("缺少必要的查询参数！"+errorMsg);
				return response;
			}
			CacheRespone cityWeatherData  = loginCache.get("token");
			String token = "";
			if(StringUtils.isBlank((String) cityWeatherData.get())){ 
				AccessToken accesstoken = WeixinUtil.getAccessToken(appid,appsecret);
				token = accesstoken.getToken();
				loginCache.set("token", token, 7200);
			} else{
				token = (String)cityWeatherData.get();
			}
			List<UserAccreditInfo> list = personalFindBindThirdAccount
					.getUserInfo(request.getMemberId(),"weixin",null,1);
			String openid = "";
			if(list!=null){
				for(UserAccreditInfo uai:list){
					if(uai.getMemberId().equals(request.getMemberId())){
						openid = uai.getPlatUsrId();
						break;
					}
				}
			}
			if(StringUtils.isBlank(openid)){
				response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				response.setErrorMessage("未查询到对应的openid");
				response.setSuccess(false);
				logger.error("缺少必要的查询参数！openid为空");
				return response;
			}
			request.setOpenid(openid);
			logger.info("模板号template_id-----"+request.getTemplateId());
			WeixinUtil.sendTemplateData(request,token);
			response.setSuccess(true);
			response.setErrorMessage("发送短信模板成功");
		} catch (Exception e) {
			logger.error("系统错误",e);;
			response.setSuccess(false);
			response.setErrorMessage("发送短信模板失败"+e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
		}
			return response;
		}
	
	}


