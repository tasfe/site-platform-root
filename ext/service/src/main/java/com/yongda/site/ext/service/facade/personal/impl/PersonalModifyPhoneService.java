package com.yongda.site.ext.service.facade.personal.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.kjt.unionma.api.bind.request.BindMobileEditRequestParam;
import com.kjt.unionma.api.bind.response.BindMobileEditResponse;
import com.kjt.unionma.api.bind.service.BindFacadeWS;
import com.netfinworks.site.domain.domain.request.UnionmaBindMobileEditRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.service.personal.facade.request.MobileEditRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;
import com.yongda.site.service.personal.facade.response.MobileEditResponse;
/**
 * 修改手机号码
 * @author yp
 *
 */
public class PersonalModifyPhoneService extends AbstractRoutService<MobileEditRequest, BaseResponse> {

	/**日志**/
	private Logger logger = LoggerFactory
			.getLogger(PersonalModifyPhoneService.class);
	
	@Resource(name = "bindFacadeWSC")
    private BindFacadeWS bindFacadeWSC;
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Override
	public String getRoutName() {
		return "modify_phone";
	}

	@Override
	public MobileEditRequest buildRequest(Map<String, String> paramMap) {
		MobileEditRequest req = HttpRequestConvert
				.convert2Request(paramMap,MobileEditRequest.class);
		return req;
	}

	@Override
	public BaseResponse doProcess(MobileEditRequest req) {
         MobileEditResponse resp = new MobileEditResponse();
         BaseResponse bresp = new BaseResponse();
         /**校验参数**/
        if(req.getNewPhoneNumber()==null){
        	bresp.setErrorMessage("newPhoneNumber不能为空");
        	bresp.setErrorCode("ILLEGAL_ARGUMENT");
        	return bresp;
        }else if(!digit(req.getNewPhoneNumber())){//校验手机输入
        	bresp.setErrorMessage("手机号格式输入不正确");
        	bresp.setErrorCode("ILLEGAL_ARGUMENT");
        	return bresp;
        }
		
		try {
            UnionmaBindMobileEditRequest ureq = new UnionmaBindMobileEditRequest();
            ureq.setMemberId(req.getMemberId());
            ureq.setNewPhoneNumber(req.getNewPhoneNumber());
            long beginTime = 0L;
            BindMobileEditRequestParam request = UnionmaConvert.createBindMobileEditRequestParam(ureq);

            if (logger.isInfoEnabled()) {
            	logger.info("修改绑定手机，请求参数：{}", request); 
                beginTime = System.currentTimeMillis();
            }
            BindMobileEditResponse result = bindFacadeWSC.bindMobileEdit(request);
            if(result.getIsSuccess()==true){
            	bresp.setSuccess(result.getIsSuccess());
            	resp.setMobile(result.getMobile());
            	bresp.setData(resp);
            }else{
            	bresp.setErrorCode(result.getResponseCode());
            	bresp.setErrorMessage(result.getResponseMessage());
            }
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("修改绑定手机， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
        }catch (Exception e) {
        	BizException e1 = null;
            if (e instanceof BizException) {
            	e1 = (BizException) e;
                bresp.setErrorCode(e1.getCode().getCode());
                bresp.setErrorMessage(e1.getCode().getMessage());
            }else {
                logger.error("修改绑定手机异常:请求信息" + req, e);
                bresp.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
                bresp.setErrorMessage(CommonDefinedException.SYSTEM_ERROR.getErrorMsg());
            }
           
        }
		return bresp;
	}

	private boolean digit(String phone){
		String str = "^[1-9]\\d{10}$";
		boolean bl = phone.matches(str);
		return bl;
	}
}
