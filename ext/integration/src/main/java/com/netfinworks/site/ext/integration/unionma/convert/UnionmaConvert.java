package com.netfinworks.site.ext.integration.unionma.convert;

import org.springframework.beans.BeanUtils;

import com.kjt.unionma.api.bind.request.BindEmailRequestParam;
import com.kjt.unionma.api.bind.request.BindMobileEditRequestParam;
import com.kjt.unionma.api.bind.request.CheckEmailTokenRequestParam;
import com.kjt.unionma.api.bind.request.UnBindEmailRequestParam;
import com.kjt.unionma.api.bind.request.VerifyAuthCodeRequestParam;
import com.kjt.unionma.api.common.enumes.PlatformSystemTypeEnum;
import com.kjt.unionma.api.common.enumes.PlatformTypeEnum;
import com.kjt.unionma.api.common.model.PlatformInfo;
import com.kjt.unionma.api.login.request.LoginNameEditRequestParam;
import com.kjt.unionma.api.login.request.LoginRequestParam;
import com.kjt.unionma.api.password.request.LoginPasswordEditRequestParam;
import com.kjt.unionma.api.password.request.PayPasswordEditRequestParam;
import com.kjt.unionma.api.password.request.PayPasswordSetRequestParam;
import com.kjt.unionma.api.password.request.PayPasswordVaildRequestParam;
import com.kjt.unionma.api.register.enumes.TerminalTypeEnum;
import com.kjt.unionma.api.register.request.RegisterPerfectionIdentityRequestParam;
import com.kjt.unionma.api.register.request.RegisterRequestParam;
import com.kjt.unionma.api.register.request.RegisterSetPaymentRequestParam;
import com.kjt.unionma.api.sms.enumes.SendTypeEnum;
import com.kjt.unionma.api.sms.request.SendEmailRquestParam;
import com.kjt.unionma.api.sms.request.SendMessageRquestParam;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.request.LoginNameEditRequest;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.request.PayPasswordCheckReq;
import com.netfinworks.site.domain.domain.request.PayPasswordEditReq;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.request.UnionmaBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaBindMobileEditRequest;
import com.netfinworks.site.domain.domain.request.UnionmaCheckEmailTokenRequest;
import com.netfinworks.site.domain.domain.request.UnionmaPerfectionIdentityReq;
import com.netfinworks.site.domain.domain.request.UnionmaRegisterRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSendEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSendMessageRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSetPaymentReq;
import com.netfinworks.site.domain.domain.request.UnionmaUnBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaVerifyAuthCodeRequest;
import com.netfinworks.site.domain.domain.request.loginPasswordEditReq;



public class UnionmaConvert {
	/**
     * 统一账户登陆
     *
     * @param req
     * @return
     */
    public static LoginRequestParam createLoginRequestParam(LoginRequest req) {
    	LoginRequestParam request = new LoginRequestParam();
    	request.setLoginName(req.getLoginName());
    	request.setLoginPassowrd(req.getLoginPassowrd());
    	request.setLoginType(req.getLoginType());
    	

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 修改登录名
     *
     * @param req
     * @return
     */
    public static LoginNameEditRequestParam createLoginNameEditRequestParam(LoginNameEditRequest req) {
    	LoginNameEditRequestParam request = new LoginNameEditRequestParam();
    	request.setLoginName(req.getLoginName());
    	request.setLoginNameType(req.getLoginNameType());
    	request.setMemberId(req.getMemberId());
    	request.setOldName(req.getOldName());
    	request.setOperatorId(req.getOperatorId());

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 修改登录密码
     *
     * @param req
     * @return
     */
    public static LoginPasswordEditRequestParam createLoginPasswordEditRequestParam(loginPasswordEditReq req) {
    	LoginPasswordEditRequestParam request = new LoginPasswordEditRequestParam();
    	request.setOperatorId(req.getOperatorId());
    	request.setOldPassword(req.getOldPassword());
    	request.setNewPassword(req.getNewPassword());
    	request.setEnsureNewPassword(req.getEnsureNewPassword());
    	request.setMemberType(req.getMemberType());
    	request.setLoginName(req.getLoginName());

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 设置支付密码
     *
     * @param req
     * @return
     */
    public static PayPasswordSetRequestParam createPayPasswordSetRequestParam(PayPasswordSetReq req) {
    	PayPasswordSetRequestParam request = new PayPasswordSetRequestParam();
    	request.setOperatorId(req.getOperatorId());
    	request.setAccountId(req.getAccountId());
    	request.setPayPassword(req.getPayPassword());

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    public static PayPasswordEditRequestParam createPayPasswordEditRequestParam(PayPasswordEditReq req) {
    	PayPasswordEditRequestParam request = new PayPasswordEditRequestParam();
    	request.setOperatorId(req.getOperatorId());
    	request.setAccountId(req.getAccountId());
    	request.setEnsureNewPayPassword(req.getEnsureNewPayPassword());
    	request.setNewPayPassword(req.getNewPayPassword());
    	request.setOldPayPassword(req.getOldPayPassword());
    	
    	if(req.getPlatformInfo() == null)
    		request.setPlatformInfo(createKjtPlatformInfo());
    	else{
    		PlatformInfo plt = new PlatformInfo();
    		BeanUtils.copyProperties(req.getPlatformInfo(), plt);
    		request.setPlatformInfo(plt);
    	}
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    public static PayPasswordVaildRequestParam createPayPasswordCheckRequestParam(PayPasswordCheckReq req) {
    	PayPasswordVaildRequestParam request = new PayPasswordVaildRequestParam();
    	request.setOperatorId(req.getOperatorId());
    	request.setAccountId(req.getAccountId());
    	request.setPayPassword(req.getPayPassword());

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    
    /**
     * 注册激活
     *
     * @param req
     * @return
     */
    public static RegisterRequestParam createRegisterRequestParam(UnionmaRegisterRequest req) {
    	RegisterRequestParam request = new RegisterRequestParam();
    	request.setMemberId(req.getMemberId());
    	request.setRegisterType(req.getRegisterType());
    	request.setLoginName(req.getLoginName());
    	request.setPersonIdentiy(req.getPersonIdentiy());

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 完善身份信息
     *
     * @param req
     * @return
     */
    public static RegisterPerfectionIdentityRequestParam createRegisterPerfectionIdentityRequestParam(UnionmaPerfectionIdentityReq req) {
    	RegisterPerfectionIdentityRequestParam request = new RegisterPerfectionIdentityRequestParam();
    	request.setMemberId(req.getMemberId());
    	request.setLoginPassword(req.getLoginPassword());
    	request.setEnsureLoginPassword(req.getEnsureLoginPassword());
    	request.setPayPassword(req.getPayPassword());
    	request.setEnsurePayPassword(req.getEnsurePayPassword());
    	request.setRealName(req.getRealName());
    	request.setIdCardNum(req.getIdCardNum());
    	request.setEnsureIdCardNum(req.getEnsureIdCardNum());
    	request.setIdCardType(req.getIdCardType());
    	request.setExtention(req.getExtention());
    	
    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
   
    
    /**
     * 设置支付方式
     *
     * @param req
     * @return
     */
    public static RegisterSetPaymentRequestParam createRegisterSetPaymentRequestParam(UnionmaSetPaymentReq req) {
    	RegisterSetPaymentRequestParam request = new RegisterSetPaymentRequestParam();
    	request.setMemberId(req.getMemberId());
    	request.setRealName(req.getRealName());
    	request.setPersonName(req.getPersonName());
    	request.setBankCode(req.getBankCode());
    	request.setBankName(req.getBankName());
    	request.setBankBranch(req.getBankBranch());
    	request.setBankBranchNo(req.getBankBranchNo());
    	request.setBankAccountNo(req.getBankAccountNo());
    	request.setProvince(req.getProvince());
    	request.setCity(req.getCity());
    	request.setMemberIdentity(req.getMemberIdentity());
    	request.setOperatorId(req.getOperatorId());
    	
    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 设置支付方式
     *
     * @param req
     * @return
     */
    public static BindMobileEditRequestParam createBindMobileEditRequestParam(UnionmaBindMobileEditRequest req) {
    	BindMobileEditRequestParam request = new BindMobileEditRequestParam();
    	request.setMemberId(req.getMemberId());
    	request.setNewPhoneNumber(req.getNewPhoneNumber());

    	
    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 发送邮箱请求转换
     *
     * @param req
     * @return
     */
    public static SendEmailRquestParam createSendEmailRquestParam(UnionmaSendEmailRequest req) {
    	SendEmailRquestParam request = new SendEmailRquestParam();
    	request.setMemberId(req.getMemberId());
    	request.setEmail(req.getEmail());
    	request.setUserName(req.getUserName());
    	request.setActiveUrl(req.getActiveUrl());
    	request.setToken(req.getToken());
    	request.setSendType(SendTypeEnum.getByCode(req.getSendType().getCode()));
    	request.setBizType(req.getBizType());


    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 发送手机请求转换
     *
     * @param req
     * @return
     */
    public static SendMessageRquestParam createSendMessageRquestParam(UnionmaSendMessageRequest req,OperationEnvironment env) {
    	SendMessageRquestParam request = new SendMessageRquestParam();
    	
    	request.setMemberId(req.getMemberId());
    	request.setBizType(req.getBizType());
    	request.setValidity(req.getValidity());
    	request.setMobile(req.getMobile());
    	

    	request.setPlatformInfo(createKjtPlatformInfo());
    	request.setEnviroment(env);
        return request;
    }
    
    /**
     * 校验验证码请求转换
     *
     * @param req
     * @return
     */
    public static VerifyAuthCodeRequestParam createVerifyAuthCodeRequestParam(UnionmaVerifyAuthCodeRequest req,OperationEnvironment env) {
    	VerifyAuthCodeRequestParam request = new VerifyAuthCodeRequestParam();
    	
    	request.setMemberId(req.getMemberId());
    	request.setVerifyValue(req.getVerifyValue());
    	request.setAuthCode(req.getAuthCode());
    	request.setBizType(req.getBizType());
    	

    	request.setPlatformInfo(createKjtPlatformInfo());
    	request.setEnviroment(env);
        return request;
    }
    
    /**
     * 绑定邮箱请求转换
     *
     * @param req
     * @return
     */
    public static BindEmailRequestParam createBindEmailRequestParam(UnionmaBindEmailRequest req) {
    	BindEmailRequestParam request = new BindEmailRequestParam();
    	
    	request.setMemberId(req.getMemberId());
    	request.setToken(req.getToken());
    	

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }
    
    /**
     * 绑定邮箱请求转换
     *
     * @param req
     * @return
     */
    public static CheckEmailTokenRequestParam createCheckEmailTokenRequestParam(UnionmaCheckEmailTokenRequest req) {
    	CheckEmailTokenRequestParam request = new CheckEmailTokenRequestParam();
    	
    	request.setMemberId(req.getMemberId());
    	request.setToken(req.getToken());
    	

    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }  
    /**
     * 解绑邮箱请求转换
     *
     * @param req
     * @return
     */
    public static UnBindEmailRequestParam createUnBindEmailRequestParam(UnionmaUnBindEmailRequest req) {
    	UnBindEmailRequestParam request = new UnBindEmailRequestParam();
    
    	request.setMemberId(req.getMemberId());
 
    	request.setPlatformInfo(createKjtPlatformInfo());
    	OperationEnvironment enviroment = new OperationEnvironment();
    	request.setEnviroment(enviroment);
        return request;
    }  
    
    public static PlatformInfo createKjtPlatformInfo() {
    	PlatformInfo platformInfo = new PlatformInfo();
    	
    	platformInfo.setPlatformType(PlatformTypeEnum.DEFAULT.getCode());
    	platformInfo.setPlatformSystemType(PlatformSystemTypeEnum.KJT_PERSONAL.getCode());
    	platformInfo.setTerminal(TerminalTypeEnum.COMPUTER.getCode());
    	return platformInfo;
    }
}
