package com.yongda.site.ext.service.facade.personal.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.DecipherItem;
import com.netfinworks.ma.service.base.model.DecipherResult;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.request.DecipherInfoRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.response.BaseMemberInfo;
import com.netfinworks.ma.service.response.DecipherInfoResponse;
import com.netfinworks.ma.service.response.MemberIntegratedResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.convert.MemberConvert;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.service.personal.facade.request.BaseRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;
import com.yongda.site.service.personal.facade.response.PersonalMemberInfoResponse;
/**
 * 会员信息查询(会员名，手机号,会员等级)
 * @author yp
 *
 */
public class PersonalMemberInfoService extends AbstractRoutService<BaseRequest, BaseResponse> {

	/**日志**/
	private Logger logger = LoggerFactory
			.getLogger(PersonalMemberInfoService.class);
	
	@Resource(name = "memberFacade")
	private IMemberFacade memberFacade;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Override
	public String getRoutName() {
		return "member_info";
	}

	@Override
	public BaseRequest buildRequest(Map<String, String> paramMap) {
		BaseRequest req = HttpRequestConvert
				.convert2Request(paramMap,BaseRequest.class);
		return req;
	}

	@Override
	public BaseResponse doProcess(BaseRequest req) {
		BaseResponse  resp = new BaseResponse();
		PersonalMemberInfoResponse pResp = new PersonalMemberInfoResponse();
		
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员信息，请求参数：{}", req.getMemberId());
				beginTime = System.currentTimeMillis();
			}
			//查询会员手机号
			DecipherInfoRequest dreq = new DecipherInfoRequest();
			dreq.setMemberId(req.getMemberId());
			List<DecipherItem> items = new ArrayList<DecipherItem>(1);
			DecipherItem item = new DecipherItem();
			item.setDecipheredType(DeciphedType.CELL_PHONE.getCode());
			item.setQueryFlag(DeciphedQueryFlag.ALL.getCode());
			items.add(item);
			dreq.setColumnList(items);
			DecipherInfoResponse dresponse = memberFacade.querytMemberDecipherInfo(
					new OperationEnvironment(), dreq);
			if (ResponseCode.SUCCESS.getCode().equals(dresponse.getResponseCode())) {
				List<DecipherResult> restult = dresponse.getDecipheredResult();
				if (restult!=null) {
					DecipherResult rest = restult.get(0);
					pResp.setMobile(rest.getPrimitiveValue());
					if(pResp.getMobile()==null){
						pResp.setMobile("未绑定手机");
					}
				}
			}else{
				resp.setErrorCode(dresponse.getResponseCode());
				resp.setErrorMessage(dresponse.getResponseMessage());
				return resp;
			}
			//查询会员所有基本信息
			MemberIntegratedIdRequest request = MemberConvert
					.createMemberIntegratedIdRequest(req.getMemberId());
			MemberIntegratedResponse response = memberFacade
					.queryMemberIntegratedInfoById(null, request);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("根据ID远程查询会员， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
			    BaseMemberInfo member = response.getBaseMemberInfo();
				if(member.getMemberName()!=null){
					pResp.setMemberName(member.getMemberName());
				}else{
					pResp.setMemberName("空");;
				}
				resp.setData(pResp);
				resp.setSuccess(true);
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
//				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
				resp.setErrorCode(CommonDefinedException.USER_ACCOUNT_NOT_EXIST.getErrorCode());
				resp.setErrorMessage(CommonDefinedException.USER_ACCOUNT_NOT_EXIST.getErrorMsg());
			} else {
				logger.error("根据ID查询会员 {}信息异常:返回信息:{},{}", req.getMemberId(),
						response.getResponseCode(),
						response.getResponseMessage());
//				throw new BizException(ErrorCode.SYSTEM_ERROR);
				resp.setErrorCode(response.getResponseCode());
				resp.setErrorMessage(response.getResponseMessage());
			}
			String level = defaultMemberService.getMemberVerifyLevel(req.getMemberId(),null);
			pResp.setLevel(level);
			return resp;
		} catch (Exception e) {
			if (e instanceof BizException) {
				BizException e1 = (BizException) e;
				resp.setErrorCode(e1.getCode().getCode());
				resp.setErrorMessage(e1.getCode().getMessage());
			} else if (e instanceof MemberNotExistException) {
				MemberNotExistException e2 = (MemberNotExistException) e;
				resp.setErrorCode(e2.getCode().getCode());
				resp.setErrorMessage(e2.getCode().getMessage());
			} else {
				logger.error("根据ID查询会员 {}信息异常:异常信息{}", req.getMemberId(),
						e.getMessage(), e);
				resp.setErrorCode("SYSTEM_ERROR");
				resp.setErrorMessage(e.getMessage());
			}
			return resp;
		}
	}

}
