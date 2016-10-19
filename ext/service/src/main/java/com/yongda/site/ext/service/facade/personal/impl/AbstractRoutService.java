package com.yongda.site.ext.service.facade.personal.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.api.IRoutService;
import com.yongda.site.service.personal.facade.api.RoutServiceManager;
import com.yongda.site.service.personal.facade.request.BaseRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public abstract class AbstractRoutService<Req extends BaseRequest, Resp extends BaseResponse> implements IRoutService{
	private static final String PERSONAL_FAST_REGISTER_MEMBER = "personal_fast_register_member";
	private static final String PERSONAL_BIND_ACCOUNT = "personal_bind_account";
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	abstract public String getRoutName();
	
	abstract public Req buildRequest(Map<String,String> paramMap) throws Exception;
	
	abstract public Resp doProcess(Req req) throws Exception;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(StringUtils.isNotBlank(getRoutName()))
			RoutServiceManager.registService(getRoutName(), this);
	}

	@Override
	public BaseResponse process(Map<String, String> req) throws Exception {
		Req r = this.buildRequest(req);
		BaseRequest base = convertBaseReq(req);
		BeanUtils.copyProperties(r, base);
		BaseResponse resp = validateRequestParam(r);
		if(!resp.isSuccess()){
			return resp;
		}
		resp = doProcess(r);
		resp.setInputCharset("UTF-8");
		resp.setPartnerId(base.getPartnerId());
		return resp;
	}
	
	static public BaseRequest convertBaseReq(Map<String,String> paramMap){
		BaseRequest base = new BaseRequest();
		base.setDeviceid(paramMap.get("deviceid"));
		base.setInputCharset(paramMap.get("_input_charset"));
		base.setPartnerId(paramMap.get("partner_id"));
		base.setPlatUserId(paramMap.get("plat_usr_id"));
		base.setService(paramMap.get("service"));
		base.setVersion(paramMap.get("version"));
		base.setMemberId(paramMap.get("memberId"));
		return base;
	}
	
	public BaseResponse validateRequestParam(Req req){
		BaseResponse restP = new BaseResponse();
		String serviceName = req.getService();
		if(!serviceName.equalsIgnoreCase(PERSONAL_FAST_REGISTER_MEMBER) && !serviceName.equalsIgnoreCase(PERSONAL_BIND_ACCOUNT)){
			/**验证memberId与plat_usr_id是否匹配**/
			try {
				if(StringUtils.isNotEmpty(req.getMemberId()) && StringUtils.isNotEmpty(req.getPlatUserId())){
					if(!personalFindBindThirdAccount.match(req.getMemberId(),req.getPlatUserId(), req.getPartnerId())){
						throw new Exception("memberId与plat_usr_id不匹配");
					}
				}else{
					throw new Exception("MemberId与plat_usr_id不能为空,请检查请求参数！");
				}
			}catch (Exception e) {
				restP.setErrorMessage(e.getMessage());
				restP.setSuccess(false);
				restP.setInputCharset("UTF-8");
				restP.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				return restP;
			}
		}
		restP.setSuccess(true);
		return restP;
	}
	
}
