package com.yongda.site.app.action.loginpwd;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.LoginPasswdCheck;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.yongda.site.app.action.common.BaseAction;
import com.netfinworks.site.core.common.util.RSASignUtil;

/**
 * 修改登录密码
 * @author yp
 *
 */
@Controller
public class ModifyLoginPwdAction extends BaseAction {
	
	private static Logger logger = LoggerFactory.getLogger(ModifyLoginPwdAction.class);
	
	@Resource(name = "defaultLoginPasswdService")
    private DefaultLoginPasswdService defaultLoginPasswdService;
	
	private RSASignUtil rs = new RSASignUtil();
	// 私钥参数
	@Value(value="${dc.priKey}")
	private String priKey;
	
	/**
	 * 设置登录密码
	 * @param request
	 * @param response
	 * @param env
	 * @param old_login_pwd 旧登录密码
	 * @param new_login_pwd 新登录密码
	 * @param renew_login_pwd 确认新登录密码
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/loginpwd/modify/do", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendMessage(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,
			String old_login_pwd,String new_login_pwd,String renew_login_pwd)throws Exception{
		RestResponse restP = new RestResponse();
		PersonMember user = getUser(request);
		//验证参数是否为空
		if(old_login_pwd==null||old_login_pwd.equals("")){
			restP.setMessage("旧登录密码不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
			logger.error("缺少旧登录密码参数！");
			restP.setSuccess(false);
			return restP;
		}else if(new_login_pwd==null||new_login_pwd.equals("")){
			restP.setMessage("新登录密码不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
			logger.error("缺少新登录密码参数！");
			restP.setSuccess(false);
			return restP;
		}else if(renew_login_pwd==null||renew_login_pwd.equals("")){
			restP.setMessage("确认新登录密码不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
			logger.error("缺少确认新登录密码参数！");
			restP.setSuccess(false);
			return restP;
		}
		//解密
		old_login_pwd = rs.decode(old_login_pwd,priKey);
		new_login_pwd = rs.decode(new_login_pwd,priKey);
		renew_login_pwd = rs.decode(renew_login_pwd,priKey);
		if(old_login_pwd==null||new_login_pwd==null||renew_login_pwd==null){
			restP.setMessage("密码解密失败");
			restP.setCode(CommonDefinedException.PWD_DECRYPTION_ERROR
					.getErrorCode());
			logger.error("密码解密失败");
			restP.setSuccess(false);
			return restP;
		}
		//校验参数格式
		if(new_login_pwd.equals(old_login_pwd)){
			restP.setMessage("新登录密码与旧登录密码不能一样");
			restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
					.getErrorCode());
			logger.error("新登录密码与旧登录密码不能一样");
			restP.setSuccess(false);
			return restP;
		}else if(!judgmentPWD(old_login_pwd)){
			restP.setMessage("旧登录密码强度校验未通过");
			restP.setCode(CommonDefinedException.PWD_STRENGTH_VERIFICATION_ERROR
					.getErrorCode());
			logger.error("旧登录密码强度校验未通过");
			restP.setSuccess(false);
			return restP;
		}else if(!judgmentPWD(new_login_pwd)){
			restP.setMessage("新登录密码强度校验未通过");
			restP.setCode(CommonDefinedException.PWD_STRENGTH_VERIFICATION_ERROR
					.getErrorCode());
			logger.error("新登录密码强度校验未通过");
			restP.setSuccess(false);
			return restP;
		}else if(!new_login_pwd.equals(renew_login_pwd)){
			restP.setMessage("新登录密码与确认新登录密码不一致");
			restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
					.getErrorCode());
			logger.error("新登录密码与确认新登录密码不一致");
			restP.setSuccess(false);
			return restP;
		}
		// 验证旧登录密码
		LoginPasswdCheck checkResult = new LoginPasswdCheck();
		// 加密
		old_login_pwd = MD5Util.MD5(old_login_pwd);
		new_login_pwd = MD5Util.MD5(new_login_pwd);
		LoginPasswdRequest req = new LoginPasswdRequest();
		req.setOperatorId(user.getOperatorId());
		req.setOldPassword(old_login_pwd);
		req.setValidateType(1);
		req.setPassword(new_login_pwd);
		// 会员标识
		req.setMemberIdentity(user.getMemberIdentity());
		// 会员标识平台类型 
		req.setPlatformType(user.getPlatformType());
		checkResult = defaultLoginPasswdService.checkPersonalLoginPasswd(req);
		if (checkResult.isSuccess() && !checkResult.isLocked()) {
				//没有锁住 ，则设置登录密码。
				CommResponse commRep = defaultLoginPasswdService.setLoginPassword(req);
				if (commRep.isSuccess()) {
					restP.setSuccess(true);
					restP.setMessage("登录密码修改成功");
				} else {
					restP.setSuccess(false);
					logger.error("用户修改登录密码失败:{}", user);
					if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(commRep.getResponseCode())) {
						restP.setMessage("新登录密码不能和支付密码一样");
						restP.setCode(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR
								.getErrorCode());
						logger.error("新登录密码不能和支付密码一样");
						restP.setSuccess(false);
					} else {
						restP.setMessage("设置新登录密码失败");
						logger.error("设置新登录密码失败");
						restP.setSuccess(false);
					}
				}
		}else if(checkResult.isLocked()){
			restP.setMessage("登录密码被锁定了");
			logger.error("登录密码被锁定了");
			restP.setCode(CommonDefinedException.LOGIN_PASSWORD_LOCKED_ERROR
					.getErrorCode());
			restP.setSuccess(false);
		}else{
			restP.setMessage("旧登录密码验证不通过，您还有"+checkResult.getRemainNum()+"次机会");
			logger.error("旧登录密码验证不通过，您还有"+checkResult.getRemainNum()+"次机会");
			restP.setCode(CommonDefinedException.OLD_LOGIN_PASSWORD_ERROR
					.getErrorCode());
			restP.setSuccess(false);
		}
		return restP;
	}
	
	//验证密码格式
	private boolean judgmentPWD(String str){
		String zz1="^([a-zA-z0-9~!@#$%^&*\\;',./_+|{}\\[\\]:\"<>?]{7,23})?$";
		String zz2="^(((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])[\\x20-\\x7E]{7,23}))$";
		System.out.println("TF:"+(str.matches(zz1)&&str.matches(zz2)));
		return str.matches(zz1)&&str.matches(zz2);
	}
	
}
