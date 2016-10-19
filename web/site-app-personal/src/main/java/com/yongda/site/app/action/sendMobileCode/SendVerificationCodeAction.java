package com.yongda.site.app.action.sendMobileCode;

import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.mns.client.INotifyClient;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.core.common.util.RSASignUtil;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.MD5Util;
import com.yongda.site.app.util.ResponseUtil;

/**
 * 點車成金 發送手機短信接口
 * @author admin
 *
 */
@Controller
public class SendVerificationCodeAction extends BaseAction{
	
	private static Logger log = LoggerFactory.getLogger(SendVerificationCodeAction.class);
	
	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;
	
	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	@Resource(name = "transferService")
	private TransferService transferService;
	
	@Resource(name = "loginPasswdService")
	private LoginPasswdService        loginPasswdService;
	 
	private static final String REGISTER_UIL = "register";
	
	@Resource(name = "memberService")
    private MemberService             commMemberService;
	
	@Resource(name = "mnsClient")
    private INotifyClient mnsClient;
	
	@Value("${dc.priKey}")
	private String priKey;
	
	@Value("${dc.pubKey}")
	private String pubKey;
	
	@Value("${md5Signkey}")
	private String md5Key;
	/**
	 * 发送短信验证码
	 * @param request
	 * @return
	 * @throws  
	 * @throws IOException
	 * @throws BindException
	 */
	@RequestMapping(value = "/sendMobileVerifyCode", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendMobileMessage(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		RestResponse restP = new RestResponse();
		//String registertoken = null;
		try {
			HttpSession session = request.getSession();
			String mobile = request.getParameter("login_name");
			/**类型   登陆（login） 注册（register）*****/
			String type = REGISTER_UIL; /*request.getParameter("type");*/
			// 校验提交参数
			if(!verifyReqParams(mobile,type,restP))
			{
				return restP;
			}
			// 封装发送短信请求
			AuthCodeRequest req = new AuthCodeRequest();
			req.setMobile(mobile);
			req.setBizId(mobile);
			if (isNeedActiveMember(mobile, env)) {
				// 激活模板
				req.setBizType(BizType.ACTIVE_MOBILE.getCode());
			} else {
				// 注册模板
				req.setBizType(BizType.REGISTER_MOBILE.getCode());
			}
			String ticket = defaultUesService.encryptData(mobile);
			req.setMobileTicket(ticket);
			req.setValidity(CommonConstant.VALIDITY);
			
			//transferService.sendMobileMessage(session.getId(), mobile, env, BizType.REGISTER_MOBILE);
			
			log.info("发送手机短信请求参数：{}",req.toString());
			// 发送短信
			if (defaultSmsService.sendMessage(req, env)) {
				restP.setSuccess(true);
				/*
				 * payPassWordCacheService.saveOrUpdate(registertoken,
				 * String.valueOf(++sendCount));
				 */
			} else {
				log.error("发送短信失败,手机号码：" + mobile);
				restP.setMessage("");
				restP.setSuccess(false);
			}
			// }
			restP.setMessage("发送短信成功");
		}catch(ServiceException e){
			log.error("发送短信失败");
		} catch (Exception e) {
			log.error("发送短信失败:" + e.getMessage(), e.getCause());
			restP.setSuccess(false);
		}
		return restP;
	}
	
	//点车发送短信信息
	@RequestMapping(value = "/send/dcsms", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendsms(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		String phoneNum = request.getParameter("phoneNum");
		String memberid = request.getParameter("memberid");
		String content  = request.getParameter("content");
		
		String decodePhoneNum = RSASignUtil.decode(phoneNum, priKey);
		String decodeMemberid = RSASignUtil.decode(memberid, priKey);
		logger.info("短信发送请求参数：手机号码{},会员号{}",decodePhoneNum,decodeMemberid);
		
		RestResponse restP = verifyReqParams(decodePhoneNum,decodeMemberid,content);
		if(!restP.isSuccess()){
			return restP;
		}
		
		com.netfinworks.mns.client.Response responseResult = mnsClient.sendMobileMsg(decodePhoneNum,content);
		logger.info("发送短信响应信息：{}",responseResult.toString());
		if (!responseResult.isSuccess()) {
			logger.info("短信发送失败！：{}",responseResult.getMsg());
			return ResponseUtil.buildExpResponse(responseResult.getCode(),responseResult.getMsg());
		} 
		restP.setMessage("发送成功");
		return restP;
	}
	
	@RequestMapping(value = "/send/dcphpsms", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse phpsendsms(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		String phoneNum = request.getParameter("phoneNum");
		String memberid = request.getParameter("memberid");
		String content  = request.getParameter("content");
		String sign = request.getParameter("sign");//md5签名结果
		logger.info("请求参数手机号：{},会员号：{},短信内容：{},签名结果：{}",phoneNum,memberid,content,sign);
		RestResponse restP = verifyReqParams(phoneNum,memberid,content);
		if(!restP.isSuccess()){
			return restP;
		}
		try {
			if(StringUtils.isBlank(sign) || !MD5Util.verify(phoneNum+memberid+content, sign, md5Key, "UTF-8")){
				logger.error("缺少必要的输入参数！"+ StringUtils.isBlank(sign) == null?"签名结果不能为空":"验签失败");
				return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,StringUtils.isBlank(sign)?"签名结果不能为空":"验签失败");
			}
		} catch (Exception e) {
			logger.error("",e);
		}
		
		com.netfinworks.mns.client.Response responseResult = mnsClient.sendMobileMsg(phoneNum,content);
		logger.info("发送短信响应信息：{}",responseResult.toString());
		if (!responseResult.isSuccess()) {
			logger.info("短信发送失败！：{}",responseResult.getMsg());
			return ResponseUtil.buildExpResponse(responseResult.getCode(),responseResult.getMsg());
		} 
		restP.setMessage("发送成功");
		return restP;
	}
	
	@RequestMapping(value = "/getsign", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse getsign(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env){
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		String phoneNum = request.getParameter("phoneNum");
		String memberid = request.getParameter("memberid");
		String content  = request.getParameter("content");
		
		try {
			restP.getData().put("sign", MD5Util.sign(phoneNum+memberid+content, md5Key, "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		restP.setMessage("md5签名处理成功");
		return restP;
	}
	
	/**
	 * 
	 * @param username
	 * @param env
	 * @param restP
	 * @param session
	 * @param type
	 *             登陆（login） 注册（register）
	 * @return
	 */
	public Boolean checkLoginUserName(String mobile, String type,RestResponse restP) {
		OperationEnvironment env = new OperationEnvironment();
		PersonMember person = new PersonMember();
		person.setLoginName(mobile);
		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			//注册会员是否已激活    针对云+用户
			boolean isActiveMember=person.getStatus()==MemberStatus.NOT_ACTIVE?false:true;		
			
			LoginPasswdRequest loginPasswdRequest=new LoginPasswdRequest();
			loginPasswdRequest.setValidateMode(1);	
			loginPasswdRequest.setOperatorId(person.getOperatorId());					
			Boolean falg = loginPasswdService.validateLoginPwdIsNull(loginPasswdRequest);//未设置登录密码
			if ("register".equalsIgnoreCase(type) && !StringUtils.isBlank(person.getMemberId()) 
					&& isActiveMember && !falg) {
				restP.setMessage("该手机号已注册");
				restP.setCode(CommonDefinedException.ACCOUNT_EXIST_ERROR
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("该手机号已注册");
				return false;
			}
			if (person.getLockStatus() == MemberLockStatus.LOCKED) {
				restP.setMessage("用户名被锁定");// 用户名被锁定
				restP.setSuccess(false);
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				return false;
			}
		} catch (Exception e) {
			if ("login".equalsIgnoreCase(type)) {
				restP.setMessage("用户不存在，请先注册");
				logger.error("用户不存在，请先注册！");
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
			} else {
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 是否需要激活
	 * 
	 * @param loginName
	 * @param env
	 * @return
	 */
	public boolean isNeedActiveMember(String loginName, OperationEnvironment env) {
		// 账户是否激活
		boolean isNeedActiveMember = false;
		try {
			PersonMember person = new PersonMember();
			person.setLoginName(loginName);
			person = memberService.queryMemberIntegratedInfo(person, env);
			if (null != person && null != person.getStatus()) {
				if (person.getMemberType() == MemberType.PERSONAL) {
					isNeedActiveMember = person.getStatus() == MemberStatus.NOT_ACTIVE ? true
							: false;
				}
			}
		} catch (Exception e) {
			logger.info("{}未注册，无需激活", loginName);
		}
		return isNeedActiveMember;
	}

	//校验请求参数
	private Boolean verifyReqParams(String mobile,String type,RestResponse restP){
		if (StringUtils.isBlank(mobile) || !Pattern.matches(RegexRule.MOBLIE, mobile)) {
			restP.setMessage(StringUtils.isBlank(mobile)?"手机号码不能为空":"没有这样的手机号哦");
			restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
					.getErrorCode());
			restP.setSuccess(false);
			logger.error("缺少必要的查询参数！");
			return false;
		}
		
		/*if(StringUtils.isBlank(type)){
			restP.setMessage("类型不能为空");
			restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
					.getErrorCode());
			restP.setSuccess(false);
			logger.error("缺少必要的查询参数！");
			return false;
		}*/
		
		if(!checkLoginUserName(mobile,type,restP))
		{
			return false;
		}
		return true;
	}
		
	private RestResponse verifyReqParams(String decodePhoneNum,String decodeMemberid,String content){
		
		if(StringUtils.isBlank(decodePhoneNum) || !Pattern.matches(RegexRule.MOBLIE, decodePhoneNum)){
			logger.error("缺少必要的输入参数！"+StringUtils.isBlank(decodePhoneNum) == null?"手机号码不能为空":"没有这样的手机号哦");
			return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,StringUtils.isBlank(decodePhoneNum)?"手机号码不能为空":"没有这样的手机号哦");
		}
		
		if(StringUtils.isBlank(decodeMemberid)){
			logger.error("缺少必要的输入参数！会员号不能为空");
			return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"会员号不能为空");
		}
		
		if(StringUtils.isBlank(content)){
			logger.error("缺少必要的输入参数！短信内容为空");
			return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"短信内容不能为空");
		}
		
		try {
			commMemberService.queryMemberById(decodeMemberid, new OperationEnvironment());
		} catch (BizException e) {
			logger.error("",e);
			return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,e.getMessage());
		} catch (MemberNotExistException e) {
			logger.error("",e);
			return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,e.getMessage());
		}
		return ResponseUtil.buildSuccessResponse();
	}
}
