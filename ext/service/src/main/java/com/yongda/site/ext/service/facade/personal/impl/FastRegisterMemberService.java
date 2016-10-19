package com.yongda.site.ext.service.facade.personal.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.kjt.unionma.api.common.model.PlatformInfo;
import com.kjt.unionma.api.register.request.RegisterRequestParam;
import com.kjt.unionma.api.register.response.RegisterResponse;
import com.kjt.unionma.api.register.service.RegisterFacadeWS;
import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.site.core.common.constants.ValidationConstants;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.service.personal.facade.request.FastRegisterRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public class FastRegisterMemberService extends  AbstractRoutService<FastRegisterRequest,BaseResponse>{
	public  Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RegisterFacadeWS registerFacadeWS;
	
	@Override
	public String getRoutName() {
		return "personal_fast_register_member";
	}
	@Resource(name = "memberService")
	private MemberService memberService;

	@Override
	public FastRegisterRequest buildRequest(Map<String, String> paramMap) {
		return HttpRequestConvert.convert2Request(paramMap, FastRegisterRequest.class);
	}

	@Override
	public BaseResponse doProcess(FastRegisterRequest fastRegisterRequest) {
		BaseResponse fastRegisterResponse=new BaseResponse();
		Map<String,String> map=new HashMap<String,String>();
		try {
			fastRegisterValidatPara(fastRegisterRequest);
			/************注册********************************/
			PlatformInfo platformInfo = UnionmaConvert.createKjtPlatformInfo();
			RegisterRequestParam requestParam = new RegisterRequestParam();
			String registerType  = "mobile";
			String loginName	 = fastRegisterRequest.getAccount();// 登录名
			requestParam.setPlatformInfo(platformInfo);
			requestParam.setRegisterType(registerType.toUpperCase());
			requestParam.setLoginName(loginName);
			requestParam.setPersonIdentiy("1");
			
				/**判断是否已经注册过*********************************/
			
			try {
				BaseMember baseMember=memberService.queryMemberByName(fastRegisterRequest.getAccount(), null);
				if(baseMember!=null){
					map.put("memberId", baseMember.getMemberId());
					fastRegisterResponse.setErrorMessage(CommonDefinedException.ACCOUNT_EXIST_ERROR.getErrorMsg());
					fastRegisterResponse.setErrorCode(CommonDefinedException.ACCOUNT_EXIST_ERROR.getErrorCode());
					fastRegisterResponse.setSuccess(false);
					fastRegisterResponse.setData(map);
					return fastRegisterResponse;
				}
			} catch (MemberNotExistException e) {
			}
			
			
			
			RegisterResponse registerResponse = registerFacadeWS.register(requestParam);
			System.out.println("registerResponse:"+registerResponse);
			String rstMsg = "";
			if (!registerResponse.getIsSuccess()) {
				String code = registerResponse.getResponseCode();
				rstMsg      = registerResponse.getResponseMessage();
				
				if (SysResponseCode.ILLEGAL_ARGUMENT.getCode().equals(code)) {
					rstMsg = "注册参数验证失败";
					logger.info("错误:"+rstMsg);
				} 
				if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(code)) {
					rstMsg = "账号已存在";
					logger.info("错误:"+rstMsg+": memberid:"+registerResponse.getMemberId());
				} else{
					
					logger.info("错误:"+rstMsg);
					rstMsg = "其他错误";
				}
				fastRegisterResponse.setErrorCode(code);
				fastRegisterResponse.setErrorMessage(rstMsg);
				fastRegisterResponse.setSuccess(false);
			}else{
				logger.info("快速注册成功");
				fastRegisterResponse.setSuccess(true);
				map.put("memberId", registerResponse.getMemberId());
				fastRegisterResponse.setData(map);
			}
			
		} catch (BizException e) {
			if(e.getCode().equals("ILLEGAL_ARGUMENT")){
				fastRegisterResponse.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				fastRegisterResponse.setErrorMessage(e.getMessage());
				fastRegisterResponse.setSuccess(false);
			}else{
				fastRegisterResponse.setErrorCode(e.getCode().getCode());
				fastRegisterResponse.setErrorMessage(e.getMessage());
				fastRegisterResponse.setSuccess(false);
			}
			logger.error("error:"+e.getMessage(),e);
		}//验证请求信息格式
		return fastRegisterResponse;
	}

	/**
	 * 快速注册参数验证
	 */
	private void fastRegisterValidatPara(FastRegisterRequest fastRegisterRequest)throws BizException{
		
		//验证批次号长度
		if(StringUtil.isBlank(fastRegisterRequest.getAccount()) || fastRegisterRequest.getAccount().getBytes().length>128)
		{
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT,"快速注册账号输入不正确！");
		}
		if (!fastRegisterRequest.getAccount().matches(ValidationConstants.MOBILE_PATTERN)||fastRegisterRequest.getAccount()==null) {
			logger.error("账号格式不正确！");
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "账号格式不正确！");
		}
	}

}

