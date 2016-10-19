package com.yongda.site.ext.service.facade.personal.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.facade.IPayPwdFacade;
import com.netfinworks.ma.service.request.AccountQueryRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.response.MemberIntegratedResponse;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.member.CertService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.YDPayPasswdRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public class SetPayPWDService extends AbstractRoutService<YDPayPasswdRequest,BaseResponse>{

	@Resource(name = "maQueryService")
	private MaQueryService maQueryService;

	@Resource(name = "memberFacade")
	private IMemberFacade memberFacade;

	@Resource(name = "certService")
	private CertService certService;

	@Resource(name = "payPwdFacade")
    private IPayPwdFacade payPwdFacade;

	@Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;

	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	public  Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Override
	public String getRoutName() {
		// TODO Auto-generated method stub
		return "personal_set_pwd";
	}

	@Override
	public YDPayPasswdRequest buildRequest(Map<String, String> paramMap) {
		return HttpRequestConvert.convert2Request(paramMap, YDPayPasswdRequest.class);
	}

	@Override
	public BaseResponse doProcess(YDPayPasswdRequest yDPayPasswdRequest){
		BaseResponse resp=new BaseResponse();
		
		
		
		String errorMsg = commonValidator.validate(yDPayPasswdRequest);
		
		if (StringUtils.isNotEmpty(errorMsg)) {
			resp.setErrorMessage(errorMsg);
			resp.setSuccess(false);
			resp.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			logger.info("缺少必要的参数！"+errorMsg);
			return resp;
		}
		/**密码强度验证**/
		if(!judgmentPWD(yDPayPasswdRequest.getPassword())){
			resp.setErrorCode(ErrorCode.ILLEGAL_ARGUMENT.getCode());
			resp.setErrorMessage("密码格式错误");
			resp.setSuccess(false);
			logger.info("密码格式错误");
			return resp;			
		}
		/**设置支付密码**/
		Map<String,String> map=null;
		try {/**根据memberid获取账户id和操作员id/手机号**/
			map=getaccountId(yDPayPasswdRequest.getMemberId());
		} catch (Exception e) {
			resp.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			resp.setErrorMessage(e.getMessage());
			resp.setSuccess(false);
			logger.error("",e);
			return resp;
		}
		PayPasswdRequest req = new PayPasswdRequest();
		req.setMemberId(yDPayPasswdRequest.getMemberId());
		req.setPassword(MD5Util.MD5(yDPayPasswdRequest.getPassword()));
        req.setOperator(map.get("operatorId"));
        req.setAccountId(map.get("accountId"));
        req.setValidateType(2);
		
		/***设置密码**/
        try {
			CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
			if(commRep.isSuccess()){
				System.out.println("commRep:"+commRep.toString());
				resp.setErrorMessage(commRep.getResponseMessage());
				
				resp.setSuccess(true);
				logger.info(commRep.getResponseMessage());
	            
			}else{
				resp.setErrorMessage(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorCode());
				resp.setErrorCode(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorMsg());
				resp.setSuccess(false);		
				logger.info(commRep.getResponseMessage());
			}
			return resp;
		} catch (Exception e) {
			resp.setErrorMessage("设置支付密码失败：信息：" +e.getMessage());
			resp.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			resp.setSuccess(false);
			logger.error("设置支付密码失败：信息：" ,e);
            return resp;
		}
	}
	/**判断密码格式是否正确   正确=true**/
	private boolean judgmentPWD(String str){
		String zz1="^([a-zA-z0-9~!@#$%^&*\\;',./_+|{}\\[\\]:\"<>?]{7,23})?$";
		String zz2="^(((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])[\\x20-\\x7E]{7,23}))$";
		System.out.println("TF:"+(str.matches(zz1)&&str.matches(zz2)));
		return str.matches(zz1)&&str.matches(zz2);
	}
	
	/**获取操作员id、账户id、手机号码**/
	private Map<String,String> getaccountId(String memberId) throws Exception{
		Map<String,String> map=new HashMap<String,String>();
		MemberInfo memberInfo = maQueryService.queryMemberByMemberId(memberId);//根据id查出所有帐号信息
		
		/**获取操作员id**/
		MemberIntegratedIdRequest paramMemberIntegratedIdRequest = new MemberIntegratedIdRequest();
		paramMemberIntegratedIdRequest.setMemberId(memberId);
		paramMemberIntegratedIdRequest.setRequireDefaultOperator(true);
		paramMemberIntegratedIdRequest.setRequireVerifyInfos(true);
		//查询账号
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(true);
		MemberIntegratedResponse queryResult = memberFacade.queryMemberIntegratedInfoById(
			           null, paramMemberIntegratedIdRequest);
		map.put("operatorId",queryResult.getDefaultOperator().getOperatorId());
		map.put("accountId",memberInfo.getBaseAccountId());
		map.put("phone",memberInfo.getMemberIdentity());
		return map;
	}
	

	
	
	
	
	

}
