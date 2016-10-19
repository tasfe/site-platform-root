package com.yongda.site.app.action.paypwdset;

import java.util.HashMap;
import java.util.Map;

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

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.IPayPwdFacade;
import com.netfinworks.ma.service.request.PayPwdLockRequest;
import com.netfinworks.ma.service.request.ValidatePayPwdRequest;
import com.netfinworks.ma.service.response.ValidatePayPwdResponse;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.core.common.util.RSASignUtil;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.request.PayPasswordCheckReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.convert.PaypasswdConvert;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.yongda.site.app.action.common.BaseAction;

@Controller
public class DccjPaypwd extends BaseAction{
	protected Logger log = LoggerFactory.getLogger(getClass());
	@Resource(name = "payPasswordService")
	private PayPasswordService payPasswordService;
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;
	@Resource(name="transferService")
	private TransferService transferService;
	@Resource(name="defaultCertService")
	private DefaultCertService defaultCertService;
	@Resource(name="defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;
	@Resource(name = "memberService")
	private MemberService memberService;
	@Resource(name = "payPwdFacade")
    private IPayPwdFacade payPwdFacade;
	//解密类
	private RSASignUtil rsa=new RSASignUtil();
	//解密私钥
	@Value(value="${dc.priKey}")
	private String priKey;
	
	
	
	/**
	 * 找回支付密码
	 * @param request
	 * @param response
	 * @param code  短信验证码
	 * @param idcard_no  身份证号
	 * @param pay_pwd  新密码
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/paypwd/find/reset", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse findPayPwd(HttpServletRequest request,HttpServletResponse response,
			String code, String idcard_no,String pay_pwd,OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		PersonMember user = getUser(request);
		String mobile=null;
		Map<String,String> map=new HashMap<String, String>();
		try {
			mobile = getPhoneNum(user.getMemberId(), env);
		} catch (BizException e) {
			restP.setSuccess(false);
			restP.setMessage("系统错误");
			map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),
					CommonDefinedException.SYSTEM_ERROR.getErrorMsg());
			restP.setErrors(map);
			log.error("系统错误" + e.getMessage(), e.getCause());
			return restP;
		}
		
		if(code==null||pay_pwd==null){
			restP.setSuccess(false);
			restP.setMessage("必填字段未填");
			map.put(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST.getErrorCode(),
					CommonDefinedException.REQUIRED_FIELD_NOT_EXIST.getErrorMsg());
			restP.setErrors(map);
			log.info("会员："+user.getMemberId()+":必填字段未填");
			return restP;
		}
		//对加密的密码进行解密
		pay_pwd=rsa.decode(pay_pwd, priKey);
		if(pay_pwd==null){
			restP.setSuccess(false);
			restP.setMessage("密码解密错误");
			map.put(CommonDefinedException.PWD_DECRYPTION_ERROR.getErrorCode(),
					CommonDefinedException.PWD_DECRYPTION_ERROR.getErrorMsg());
			restP.setErrors(map);
			log.info("会员："+user.getMemberId()+":密码解密失败");
			return restP;
		}
		//密码强度校验
		if(!judgmentPWD(pay_pwd)){
			restP.setSuccess(false);
			restP.setMessage("密码强度校验未通过");
			map.put(CommonDefinedException.PWD_STRENGTH_VERIFICATION_ERROR.getErrorCode(),
					CommonDefinedException.PWD_STRENGTH_VERIFICATION_ERROR.getErrorMsg());
			restP.setErrors(map);
			log.info("会员："+user.getMemberId()+":密码强度校验未通过");
			return restP;
		}
		/*判断是否实名认证*/
		if(!user.getCertifyLevel().equals(CertifyLevel.NOT_CERTIFY)){
			/*查询身份证号码*/
			try {
				AuthInfoRequest authInfoReq = new AuthInfoRequest();
				authInfoReq.setMemberId(user.getMemberId());
				authInfoReq.setAuthType(AuthType.IDENTITY);
				authInfoReq.setOperator(user.getOperatorId());
				AuthInfo info = defaultCertService.queryRealName(authInfoReq);
				if(idcard_no==null||!info.getAuthNo().equals(idcard_no)){
					restP.setSuccess(false);
					restP.setMessage("身份证信息比对结果不成功");
					map.put(CommonDefinedException.ID_CARD_VERIFICATION_ERROR.getErrorCode(),
							CommonDefinedException.ID_CARD_VERIFICATION_ERROR.getErrorMsg());
					restP.setErrors(map);
					log.info("会员："+user.getMemberId()+":身份证信息比对结果不成功");
					return restP;
				}
			} catch (ServiceException e) {
				restP.setSuccess(false);
				restP.setMessage("实名认证信息校验失败");
				map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),
					"系统繁忙，请稍后再试");
				restP.setErrors(map);
				log.error("查询实名认证信息失败:" + e.getMessage(), e.getCause());
				return restP;
			}
		}
		//验证短信验证码
		boolean result=transferService.validateOtpValue("mobile", user.getLoginName(),
				mobile, user.getMemberId(),  BizType.REFIND_PAYPASSWD, code, env);
		if(!result){
			restP.setSuccess(false);
			restP.setMessage("手机验证码未通过");
			map.put(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),
					"手机验证码未通过");
			restP.setErrors(map);
			log.info("会员："+user.getMemberId()+":手机验证码未通过");
		}else{
			PayPasswdRequest req= new PayPasswdRequest();
			req.setMemberId(user.getMemberId());
			req.setAccountId(user.getDefaultAccountId());
			req.setValidateMode(0);
			req.setValidateType(2);
			req.setOperator(user.getOperatorId());
			try {
				if(!resetPayPasswordLock(req,env)){
					map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(), 
							"密码解锁失败，请稍后再试");
					restP.setErrors(map);
					restP.setSuccess(false);
					restP.setMessage("重置支付密码失败");
					logger.info("设置支付密码失败：信息：" ,"密码解锁失败");
				}
			} catch (BizException e) {
				map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(), 
						"系统繁忙，请稍后再试");
				restP.setErrors(map);
				restP.setSuccess(false);
				restP.setMessage("重置支付密码失败");
				logger.error("设置支付密码失败：信息：" ,e);
			}
	        try {
	        	/*进行设置密码*/
	        	//PayPasswdRequest req = new PayPasswdRequest();
	        	//req.setMemberId(user.getMemberId());
	        	req.setPassword(MD5Util.MD5(pay_pwd));
	        	//req.setOperator(user.getOperatorId());
	        	//req.setAccountId(user.getDefaultAccountId());
	        	//req.setValidateType(2);
	        	req.setValidateMode(1);
				CommResponse commRep = defaultPayPasswdService.setPayPassword(req);
				
				if(commRep.isSuccess()){
					restP.setSuccess(true);
					restP.setMessage("重置支付密码成功");
					logger.info(commRep.getResponseMessage());
		            
				}else{
					map.put(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorCode(),
							CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorMsg());
					restP.setErrors(map);
					restP.setSuccess(false);
					restP.setMessage("重置支付密码失败");
					logger.info(commRep.getResponseMessage());
				}
			} catch (ServiceException e) {
				map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(), 
						"系统繁忙，请稍后再试");
				restP.setErrors(map);
				restP.setSuccess(false);
				restP.setMessage("重置支付密码失败");
				logger.error("设置支付密码失败：信息：" ,e);
			}
		}
		return restP;
	}
	
	/**
	 * 修改支付密码
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 */
	@SuppressWarnings("finally")
	@RequestMapping(value = "/paypwd/modify/do", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse resetPayPwd(HttpServletRequest request,HttpServletResponse response,
			String old_pay_pwd, String new_pay_pwd,String renew_pay_pwd,OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		PersonMember user = getUser(request);
		Map<String,String> map=new HashMap<String, String>();
		/**校验参数**/
		if(old_pay_pwd==null||new_pay_pwd==null||renew_pay_pwd==null){
			restP.setSuccess(false);
			restP.setMessage("校验参数未通过");
			map.put(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST.getErrorCode(),
					CommonDefinedException.REQUIRED_FIELD_NOT_EXIST.getErrorMsg());
			restP.setErrors(map);
			log.info("会员："+user.getMemberId()+":校验参数未通过");
			return restP;
		}
		//对加密的密码解密
		new_pay_pwd=rsa.decode(new_pay_pwd, priKey);
		old_pay_pwd=rsa.decode(old_pay_pwd, priKey);
		renew_pay_pwd=rsa.decode(renew_pay_pwd, priKey);
		if(new_pay_pwd==null||old_pay_pwd==null||renew_pay_pwd==null){
			restP.setSuccess(false);
			restP.setMessage("密码解密错误");
			map.put(CommonDefinedException.PWD_DECRYPTION_ERROR.getErrorCode(),
					CommonDefinedException.PWD_DECRYPTION_ERROR.getErrorMsg());
			restP.setErrors(map);
			log.info("会员："+user.getMemberId()+":密码解密失败");
			return restP;
		}
		if(!(new_pay_pwd.equals(renew_pay_pwd)&&!new_pay_pwd.equals(old_pay_pwd))){
			map.put(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(), "新密码与确认密码不符||新密码与旧密码不能相同！");
			restP.setErrors(map);
			restP.setSuccess(false);
			restP.setMessage("新密码与确认密码不符||新密码与旧密码不能相同！");
			logger.info("会员："+user.getMemberId()+":新密码与确认密码不符||新密码与旧密码不能相同！");
			return restP;
		}
		if(!judgmentPWD(new_pay_pwd)){
			map.put(CommonDefinedException.PWD_STRENGTH_VERIFICATION_ERROR.getErrorCode(),"新密码强度不够，应该为包含字母、数字、特殊符号中的两种或以上组合，并且长度在7-23位！");
			restP.setErrors(map);
			restP.setSuccess(false);
			restP.setMessage("新密码强度不够，应该为包含字母、数字、特殊符号中的两种或以上组合，并且长度在7-23位！");
			logger.info("会员："+user.getMemberId()+":新密码强度不够，应该为包含字母、数字、特殊符号中的两种或以上组合，并且长度在7-23位！");
			return restP;
		}
		/**unionma校验支付密码*/
		PayPasswordCheckReq req=new PayPasswordCheckReq();
		req.setAccountId(user.getDefaultAccountId());
		req.setEnviroment(env);
		req.setOperatorId(user.getOperatorId());
		req.setPayPassword(old_pay_pwd);
		req.setPlatformInfo(createDefaultPlatform(user.getMemberType()));
		try {
			UnionmaBaseResponse resp=payPasswordService.payPasswordCheck(req);
			if(!resp.getIsSuccess()){
				restP.setSuccess(false);
				restP.setMessage(resp.getResponseMessage());
				map.put(resp.getResponseCode(), resp.getResponseMessage());
				restP.setErrors(map);
				log.info("会员："+user.getMemberId()+":原密码验证失败："+resp.getResponseMessage());
				return restP;
			}
		} catch (BizException e) {
			restP.setSuccess(false);
			restP.setMessage("系统错误");
			map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),e.getMessage());
			restP.setErrors(map);
			log.error("会员："+user.getMemberId()+":原密码验证失败："+e.getMessage());
			return restP;
		}

		/**设置支付密码**/
		try {
			PayPasswdRequest pwdreq = new PayPasswdRequest();
			pwdreq.setOperator(user.getOperatorId());
			pwdreq.setAccountId(user.getDefaultAccountId());
			pwdreq.setOldPassword( MD5Util.MD5(old_pay_pwd));
			pwdreq.setValidateType(1);
			pwdreq.setPassword(MD5Util.MD5(new_pay_pwd));
			CommResponse commRep = defaultPayPasswdService.setPayPassword(pwdreq);
			if (commRep.isSuccess()) {
				restP.setSuccess(true);
				restP.setMessage("修改密码成功");
				log.error("会员："+user.getMemberId()+":修改支付密码成功！");
			}else{
				restP.setSuccess(false);
				if(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(commRep.getResponseCode())) {
					restP.setMessage("支付密码不能和登录密码相同！");
					map.put(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorCode(),
							"支付密码不能和登录密码相同！");
					restP.setErrors(map);
					log.info("会员："+user.getMemberId()+":修改支付密码失败："+"支付密码不能和登录密码相同！");
				}else{
					restP.setMessage("修改支付密码失败");
					map.put(CommonDefinedException.PASSWORD_ERROR.getErrorCode(),
							"修改支付密码失败");
					restP.setErrors(map);
					log.info("会员："+user.getMemberId()+":修改支付密码失败："+"修改支付密码失败");
				}
			}
		} catch (ServiceException e) {
			restP.setSuccess(false);
			restP.setMessage("修改支付密码失败");
			map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(), "系统繁忙，请稍后再试");
			restP.setErrors(map);
			log.error("会员："+user.getMemberId()+":修改支付密码失败："+e);
		}finally{
			return restP;
		}
	}
	
	/**
	 * 发送“设置支付密码”短信验证码
	 * @return
	 */
	@RequestMapping(value = "/paypwd/find/sendcode", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendMobileMessage(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		RestResponse restP = new RestResponse();
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		PersonMember user = getUser(request);
		Map<String,String> map=new HashMap<String, String>();
		String mobile=null;
		try {
			mobile = getPhoneNum(user.getMemberId(), env);
		} catch (BizException e) {
			restP.setSuccess(false);
			restP.setMessage("系统繁忙，请稍后再试");
			map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),
					"系统繁忙，请稍后再试");
			restP.setErrors(map);
			log.error("获取绑定手机号异常:" + e.getMessage(), e.getCause());
			return restP;
		}
		try {
			boolean result=transferService.sendMobileMessage(user.getMemberId(), mobile, env, BizType.REFIND_PAYPASSWD);
			if (result) {
				restP.setSuccess(true);
				log.info("发送短信成功,手机号码：" +mobile);
				restP.setMessage("发送短信成功");
			} else {
				restP.setMessage("短信验证码发送失败，请手机号是否正确");
				restP.setSuccess(false);
				map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),
						"系统繁忙，请稍后再试");//错误码有待更新
				restP.setErrors(map);
			}
		} catch (ServiceException e) {
			log.error("发送短信失败:" + e.getMessage(), e.getCause());
			restP.setSuccess(false);
			map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),
					"系统繁忙，请稍后再试");
			restP.setErrors(map);
		}
		return restP;
	}

	
	
	/**判断密码格式是否正确   正确=true**/
	private boolean judgmentPWD(String str){
		String zz1="^([a-zA-z0-9~!@#$%^&*\\;',./_+|{}\\[\\]:\"<>?]{7,23})?$";
		String zz2="^(((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])[\\x20-\\x7E]{7,23}))$";
		System.out.println("TF:"+(str.matches(zz1)&&str.matches(zz2)));
		return str.matches(zz1)&&str.matches(zz2);
	}
	
	/**通过memberID获取绑定手机号
	 * @throws BizException */
	private String getPhoneNum(String memberId,OperationEnvironment env) throws BizException{
		EncryptData encryptData = memberService.decipherMember(
				memberId, DeciphedType.CELL_PHONE,
				DeciphedQueryFlag.ALL, env);
        // 获取解密手机号码进行验证码校验
		String mobile = encryptData.getPlaintext();
		return mobile;
	}
	
	/**解锁支付密码*/
	public boolean resetPayPasswordLock(PayPasswdRequest req,OperationEnvironment env) throws BizException {
        PayPwdLockRequest request = PaypasswdConvert.createPayPwdLockRequest(req);
        Response response = payPwdFacade.resetPayPasswordLock(getEnv(env.getClientIp()), request);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return true;
        } else {
            return false;
        }
    }
	
	public static OperationEnvironment getEnv(String clientIp) {
        OperationEnvironment env = new OperationEnvironment();
        env.setClientId("site");
        env.setClientIp(clientIp);
        return env;
    }

}
