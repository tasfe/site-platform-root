package com.yongda.site.ext.service.facade.personal.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.netfinworks.urm.domain.info.UserAccreditInfo;
import com.yongda.site.ext.integration.urm.UserAccreditService;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.service.personal.facade.request.YDBindAccountRequest;
import com.yongda.site.service.personal.facade.request.YDPayPasswdRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public class YDBindAccountService extends AbstractRoutService<YDBindAccountRequest, BaseResponse>{
	@Resource(name = "userAccreditService")
	private UserAccreditService userAccreditService;
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	@Resource(name = "memberService")
	private MemberService memberService;

	public Logger logger = LoggerFactory.getLogger(this.getClass());
	@Override
	public String getRoutName() {
		return "personal_bind_account";
	}

	@Override
	public YDBindAccountRequest buildRequest(Map<String, String> paramMap) {
		return HttpRequestConvert.convert2Request(paramMap, YDBindAccountRequest.class);
	}

	@Override
	public BaseResponse doProcess(YDBindAccountRequest req) {
		System.out.println("req:"+req);
		
		BaseResponse resp=new BaseResponse();
		
		String errorMsg = commonValidator.validate(req);
		
		if (StringUtils.isNotEmpty(errorMsg)) {
			resp.setErrorMessage(errorMsg);
			resp.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			resp.setSuccess(false);
			logger.info("缺少必要的参数！"+errorMsg);
			return resp;
		}
		/**参数校验***/
		try {
			Vail(req);
		} catch (BizException e1) {
			resp.setErrorCode(e1.getCode().getCode());
			resp.setErrorMessage(e1.getMessage());
			resp.setSuccess(false);
			logger.info("缺少必要的参数！"+e1.getCode().getCode());
			return resp;
		}
		/**判断该memberId是否存在****/
			BaseMember baseMember;
			try {
				baseMember = memberService.queryMemberById(req.getMemberId(), null);
				System.out.println("memberInfo:"+baseMember);
			} catch (BizException e) {
				resp.setErrorCode(e.getCode().getCode());
				resp.setErrorMessage(e.getMessage());
				resp.setSuccess(false);
				logger.error("使用memberId查询会员信息出错！",e);
				return resp;
			} catch (MemberNotExistException e) {
				resp.setErrorCode(CommonDefinedException.MEMBER_ID_NOT_EXIST.getErrorCode());
				resp.setErrorMessage(CommonDefinedException.MEMBER_ID_NOT_EXIST.getMessage());
				resp.setSuccess(false);
				logger.info("memberId不存在！");
				return resp;
			}
		
		/**根据partnerId和Third_account查询是第三方账号是否已经绑定**/
		List<UserAccreditInfo> list=userAccreditService.queryAccreditInfo(req.getMemberId(), req.getPartnerId(), req.getPlatUserId(), 2);

		if(list!=null){
			resp.setErrorCode(CommonDefinedException.PLAT_USR_ID_BIND_EXIST_ERROR.getErrorCode());
			resp.setErrorMessage(CommonDefinedException.PLAT_USR_ID_BIND_EXIST_ERROR.getErrorMsg());
			resp.setSuccess(false);
			logger.info("该plat_usr_id已经绑定过了");
			return resp;
		}
		/**绑定账号*********************/
		if(userAccreditService.addAccreditInfo(req.getMemberId(), req.getPartnerId(), req.getPlatUserId())){
			resp.setSuccess(true);
			logger.info("绑定账号成功");
			return resp;
		}
		resp.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
		resp.setErrorMessage(CommonDefinedException.SYSTEM_ERROR.getErrorMsg());
		resp.setSuccess(false);
		logger.info("绑定账号失败");
		return resp;
	}
	
	public void Vail(YDBindAccountRequest req) throws BizException{
		if(req.getMemberId()==null||req.getMemberId().equals("")){
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "memberId不能为空");
		}
		if(req.getPlatUserId()==null||req.getPlatUserId().equals("")){
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "plat_usr_id不能为空");
		}
		if(req.getPartnerId()==null||req.getPartnerId().equals("")){
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "partner_id不能为空");
		}
		if(req.getMemberId().length()>32){
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "memberId 应为1-32位");
		}
		if(req.getPlatUserId().length()>32){
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "plat_usr_id 应为1-32位");
		}
		if(req.getPartnerId().length()>32){
			throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "partner_id 应为1-32位");
		}
	}

}
