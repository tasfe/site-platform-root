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
import com.netfinworks.site.core.common.util.RSASignUtil;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.yongda.site.app.action.common.BaseAction;
/**
 * 找回登录密码
 * @author yp
 *
 */
@Controller
public class RefindLoginPwdAction extends BaseAction {
	
	private static Logger log = LoggerFactory.getLogger(RefindLoginPwdAction.class);
	
	@Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	@Resource(name = "transferService")
	private TransferService transferService;
	
	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "defaultLoginPasswdService")
	private DefaultLoginPasswdService defaultLoginPasswdService;
	
	private RSASignUtil rs = new RSASignUtil();
	// 私钥参数
	@Value(value="${dc.priKey}")
	private String priKey;
	
	private static final String MEMBER_ID_ENTITY = "mobile";
	
	/**
	 * 发送短信验证码
	 * @param request
	 * @param response
	 * @param env
	 * @param login_name
	 * @return
	 */
	@RequestMapping(value = "/loginpwd/find/sendcode", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendMessage(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,String login_name){
		RestResponse restP = new RestResponse();
		//login_name不可为空
		if(login_name == null||login_name.equals("")){
			restP.setMessage("用户名不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
	 		restP.setSuccess(false);
			return restP;
		}
		// 验证用户名是否存在
		PersonMember personMember = new PersonMember();
		personMember.setLoginName(login_name);
		try{
			personMember = memberService.queryMemberIntegratedInfo(personMember, env);
			if(personMember.getMemberType() != MemberType.PERSONAL){
				restP.setMessage("用户名不存在");
				restP.setCode(CommonDefinedException.USER_ACCOUNT_NOT_EXIST
						.getErrorCode());
				restP.setSuccess(false);
				return restP;
			}
			String memberId = personMember.getMemberId();
			EncryptData encryptData = memberService.decipherMember(
					memberId, DeciphedType.CELL_PHONE,
					DeciphedQueryFlag.ALL, env);
            // 获取解密手机号码进行验证码校验
			String mobile = encryptData.getPlaintext();
			log.info("用户"+login_name+"的手机号："+mobile);
            //发送短信
            if (transferService.sendMobileMessage(memberId,
            		mobile, env, BizType.REFIND_LOGIN_SMS)) {
                restP.setSuccess(true);
                restP.setMessage("发送短信成功");
            } else {
            	log.error("发送短信失败");
    			restP.setSuccess(false);
    			restP.setMessage("发送短信失败");
            }
            return restP;
		} catch (MemberNotExistException e) {
			restP.setMessage("用户名不存在！");
			restP.setCode(CommonDefinedException.USER_ACCOUNT_NOT_EXIST
					.getErrorCode());
			restP.setSuccess(false);
		} catch (Exception e) {
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
		}
		return restP;
	}
	/**
	 * 重置登录密码
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/loginpwd/find/reset", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse refindloginpwd(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		RestResponse restP = new RestResponse();
		
		String loginname = request.getParameter("login_name");
		String code = request.getParameter("code");
		String newloginpwd = request.getParameter("new_login_pwd");
		
		//验证参数是否为空
		if(loginname == null||loginname.equals("")){
			restP.setMessage("用户名不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
			logger.error("缺少用户名参数！");
			restP.setSuccess(false);
			return restP;
		}else if(code == null||code.equals("")){
			restP.setMessage("短信验证码不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
			logger.error("缺少短信验证码参数！");
			restP.setSuccess(false);
			return restP;
		}else if(newloginpwd == null||newloginpwd.equals("")){
			restP.setMessage("新登录密码不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
			logger.error("缺少新登录密码参数！");
			restP.setSuccess(false);
			return restP;
		}
		//解密
		newloginpwd = rs.decode(newloginpwd,priKey);
		if(newloginpwd==null){
			restP.setMessage("密码解密失败");
			restP.setCode(CommonDefinedException.PWD_DECRYPTION_ERROR
					.getErrorCode());
			logger.error("密码解密失败");
			restP.setSuccess(false);
			return restP;
		}
		//验证密码格式
		if(!judgmentPWD(newloginpwd)){
			restP.setMessage("新登录密码强度校验未通过");
			restP.setCode(CommonDefinedException.PWD_STRENGTH_VERIFICATION_ERROR
					.getErrorCode());
			logger.error("新登录密码强度校验未通过");
			restP.setSuccess(false);
			return restP;
		}
		
		// 验证用户名是否存在
		PersonMember personMember = new PersonMember();
		personMember.setLoginName(loginname);
		try{
			personMember = memberService.queryMemberIntegratedInfo(personMember, env);
			if(personMember.getMemberType() != MemberType.PERSONAL){
				restP.setMessage("用户名不存在");
				restP.setCode(CommonDefinedException.USER_ACCOUNT_NOT_EXIST
						.getErrorCode());
				logger.error("用户名不存在");
				restP.setSuccess(false);
				return restP;
			}
			//验证短信验证码
			String memberId = personMember.getMemberId();
			EncryptData encryptData = memberService.decipherMember(
					memberId, DeciphedType.CELL_PHONE,
					DeciphedQueryFlag.ALL, env);
			//获取解密手机号码进行验证码校验
			String mobile = encryptData.getPlaintext(); 
			Boolean flag = transferService.validateOtpValue(MEMBER_ID_ENTITY,loginname,mobile,
					memberId, BizType.REFIND_LOGIN_SMS, code, env);
			if (!flag) {
				restP.setMessage("该手机号对应的短信验证码有误或失效");
				logger.error("该手机号对应的短信验证码有误或失效");
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
				return restP;
			}
			//设置登录密码
			LoginPasswdRequest req = new LoginPasswdRequest();
			// 加密
			newloginpwd = MD5Util.MD5(newloginpwd);
			req.setValidateType(2);
			req.setPassword(newloginpwd);
			req.setOperatorId(personMember.getOperatorId());
			// 会员标识
			req.setMemberIdentity(personMember.getMemberIdentity());
			// 会员标识平台类型 1.UID
			req.setPlatformType(personMember.getPlatformType());
			// 重置登陆密码锁
			defaultLoginPasswdService.resetLoginPasswordLock(req);
			// 重置登陆密码 
			CommResponse commResponse = defaultLoginPasswdService.setLoginPassword(req);
			String responseCode = commResponse.getResponseCode();
			if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(responseCode)) {				
				restP.setMessage("登陆密码不能和支付密码相同！");
				restP.setSuccess(false);
				return restP;
			}			
			restP.setSuccess(true);
			restP.setMessage("设置成功");
		} catch (MemberNotExistException e) {
			restP.setMessage("用户名不存在！");
			restP.setCode(CommonDefinedException.USER_ACCOUNT_NOT_EXIST
					.getErrorCode());
			restP.setSuccess(false);
		} catch (Exception e) {
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
		}
		return restP;
	}
	
	private boolean judgmentPWD(String str){
		String zz1="^([a-zA-z0-9~!@#$%^&*\\;',./_+|{}\\[\\]:\"<>?]{7,23})?$";
		String zz2="^(((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])[\\x20-\\x7E]{7,23}))$";
		System.out.println("TF:"+(str.matches(zz1)&&str.matches(zz2)));
		return str.matches(zz1)&&str.matches(zz2);
	}
}
