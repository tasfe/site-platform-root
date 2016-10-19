package com.yongda.site.app.action.paypwdset;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.form.PayPwdRequest;
import com.yongda.site.app.form.checkIdentityRequest;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.app.validator.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
/**
 * 支付密码
 * @author admin
 *
 */

@Controller
public class PayPwdsetAction extends BaseAction{
	protected Logger log = LoggerFactory.getLogger(getClass());
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;
	 
	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;
	
	@Resource(name = "payPasswordService")
	private PayPasswordService payPasswordService;
	
	private static final String MEMBER_ID_ENTITY = "mobile";
	
	@Value("${sendMessageNumber}")
	private String sendMessageNumber;
	
	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;
	
	@Resource(name = "xuCache")
	private XUCache<String> loginCache;
	
	/**
	 * 查询身份是否认证   返回true/false
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/identity", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse Identity(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) throws IOException
			{
			RestResponse restP = new RestResponse();
			response.setCharacterEncoding("utf8");
		try{
			PersonMember user = getUser(request);
			
			PersonMember person = new PersonMember();
			person.setLoginName(user.getLoginName());
			person = memberService.queryMemberIntegratedInfo(person, env);
			String token   =  "identity"+UUID.randomUUID().toString().replace("-", "");
			loginCache.set(token, user.getMemberId(), 3600);
			logger.info("根据认证类型查询认证结果");
			//根据认证类型查询认证结果
			String identitySts = PersonalCommonQueryRealName.queryRealName(person.getMemberId(),
					StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
			
			if (ResultStatus.CHECK_PASS.getCode().equals(identitySts)) {
				Map<String,Object> identityMap = new HashMap<String,Object>();
				AuthInfo info = PersonalCommonQueryRealName.queryUserInfo(person.getMemberId(),
						StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
				String authname = info.getAuthName();//认证名
				identityMap.put("name", authname);
				identityMap.put("loginname", user.getLoginName());
				identityMap.put("token", token);
				restP.setData(identityMap);
				restP.setMessage("您的身份认证已经审核通过！");
				restP.setSuccess(true);
				return restP;
			}
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("loginname", user.getLoginName());
			map.put("token", token);
			restP.setData(map);
			restP.setMessage("您未通过身份认证！");
			restP.setSuccess(false);
		} catch (ServiceException e) {
			logger.error("个人身份认证：" + e.getMessage());
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage(e.getMessage());
			restP.setSuccess(false);
		}catch (Exception e) {
			logger.error("个人身份认证：" + e.getMessage());
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
		}
		return restP;
	}
	
	
	/**
	 * 身份认证  请求
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/checkIdentity", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse checkIdentity(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env)
			{
			RestResponse restP = new RestResponse();
			response.setCharacterEncoding("utf8");
			response.setHeader("content-type", "application/json;charset=UTF-8");
			Map<String,Object> result = new HashMap<String,Object>();
		try{
			Map<String,Object> map = request.getParameterMap();
			logger.info("未审核通过  提交身份认证申请：");
			checkIdentityRequest pay = new checkIdentityRequest();
			BeanUtils.populate(pay, map);
			// 校验提交参数
			String errorMsg = commonValidator.validate(pay);

			if (StringUtils.isNotEmpty(errorMsg)) {
				restP.setMessage(errorMsg);
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("缺少必要的查询参数！" + errorMsg);
				return restP;
			}
			
			CacheRespone<String> cacherespone = loginCache.get(pay.getToken());
			if(StringUtils.isBlank(cacherespone.get())){
				return restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode(), "请求超时");
			}
			
			PersonMember person = new PersonMember();
			person.setLoginName(pay.getUsername());
			person = memberService.queryMemberIntegratedInfo(person, env);
			
			String identitySts = PersonalCommonQueryRealName.queryRealName(person.getMemberId(),
					StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
			String token   =  "checkIdentity"+UUID.randomUUID().toString().replace("-", "");
			loginCache.set(token, person.getMemberId(), 3600);
			logger.info("个人身份认证狀態：" + identitySts);
			result.put("token",token);
			if (ResultStatus.CHECK_PASS.getCode().equals(identitySts)) {
				
				AuthInfo info = PersonalCommonQueryRealName.queryUserInfo(person.getMemberId(),
						StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
				String authname = info.getAuthName();//认证名
				String authNo = info.getAuthNo();//认证号
				if(!authname.equals(pay.getRealname())){
					throw new ServiceException("用户名与身份证不一致!");
				}
				
				if(!authNo.equals(pay.getIdcard())){
					throw new ServiceException("用户名与身份证不一致!");
				}
				restP.setMessage("身份认证成功");
				restP.setSuccess(true);
				restP.setData(result);
				return restP;
				
			}
			Boolean status = defaultCertService.verifyRealName(
					person.getMemberId(), StringUtils.EMPTY,
					StringUtils.trim(pay.getRealname()),
					StringUtils.trim(pay.getIdcard()), "idCard", env);
			restP.setSuccess(status);
			restP.setMessage("身份认证成功！");
			restP.setData(result);
		} catch (ServiceException e) {
			logger.error("个人身份认证：" + e.getMessage());
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage(e.getMessage());
			restP.setData(result);
			restP.setSuccess(false);
		}catch (Exception e) {
			logger.error("个人身份认证：" + e.getMessage());
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("身份认证失败");
			restP.setSuccess(false);
			restP.setData(result);
		}
		return restP;
	}
	
	
	/**
	 * 设置支付密码
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/setPayPwd", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse setPayPwd(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) 
			{
			RestResponse restP = new RestResponse();
			response.setCharacterEncoding("utf8");
			response.setHeader("content-type", "application/json;charset=UTF-8");
		try{
			Map<String,Object> map = request.getParameterMap();
			PersonMember user = getUser(request);
			PayPwdRequest paypwd = new PayPwdRequest();
			BeanUtils.populate(paypwd, map);
			String token   =  "setPayPwd"+UUID.randomUUID().toString().replace("-", "");
			loginCache.set(token, user.getMemberId(), 3600);
			String newPasswd = paypwd.getPassword().replaceAll(" ","");// 去除可能會出現的空格  新支付密码
			if(StringUtils.isBlank(newPasswd) || !newPasswd.equals(paypwd.getPassword())){
				 restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode(), "密码不能包含空格");
				 restP.getData().put("token",token);
				 return restP;
			}
			// 校验提交参数
			String errorMsg = commonValidator.validate(paypwd);

			if (StringUtils.isNotEmpty(errorMsg)) {
				logger.error("缺少必要的查询参数！" + errorMsg);
				restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode(), errorMsg);
				restP.getData().put("token",token);
				return restP;
			}
			
			CacheRespone<String> cacherespone = loginCache.get(paypwd.getToken());
			if(StringUtils.isBlank(cacherespone.get())){
				restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode(), "请求超时");
				restP.getData().put("token",token);
				return restP;
				
			}
			
			PayPasswordSetReq req = new PayPasswordSetReq();
			req.setPlatformInfo(createDefaultPlatform(user.getMemberType()));
			req.setAccountId(user.getDefaultAccountId());
			req.setEnviroment(env);
			req.setOperatorId(user.getOperatorId());
			req.setPayPassword(newPasswd);
			
			UnionmaBaseResponse reomotResp = payPasswordService.payPasswordSet(req);
			if(!reomotResp.getIsSuccess()){
				restP = ResponseUtil.buildExpResponse(reomotResp.getResponseCode(), reomotResp.getResponseMessage());
			}
			restP.setSuccess(true);
			user.setPaypasswdstatus(MemberPaypasswdStatus.DEFAULT);
			loginCache.delete(token);
		} catch (Exception e) {
			logger.error("支付密码：" + e.getMessage());
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
		}
		return restP;
	}
	
	
	/**
	 * 检查手机验证码是否正确
	 * @param request
	 * @param response
	 * @param env
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/checkdx", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse checkdx(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) throws IOException
			{
			RestResponse restP = new RestResponse();
			response.setCharacterEncoding("utf8");
			response.setHeader("content-type", "application/json;charset=UTF-8");
		try{
			String mobilecode = request.getParameter("mobilecode");
			PersonMember user = getUser(request);
			
			Boolean flag = validateOtpValue(MEMBER_ID_ENTITY,
					user.getLoginName(), mobilecode, request, env);
			if (!flag) {
				restP.setMessage("短信验证码有误");
				restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
				restP.setSuccess(false);
				logger.error("短信验证码有误");
				return restP;
			}
			restP.setSuccess(true);
			restP.setMessage("短信验证码正确");
		}catch (Exception e) {
			logger.error("短信验证码：" + e.getMessage());
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
		}
		return restP;
	}
	
	/**
	 * 发送短信验证码
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws BindException
	 */
	@RequestMapping(value = "/sendMobileMessagezf", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendMobileMessage(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env)
			throws IOException {
		RestResponse restP = new RestResponse();
		String registertoken = null;
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		try {
			HttpSession session = request.getSession();
			PersonMember user = getUser(request);
			registertoken = user.getLoginName() + "_" + getDateString();
			
			/*int sendCount = 1;
			if (StringUtils.isNotBlank(payPassWordCacheService
					.load(registertoken))) {
				sendCount = Integer.valueOf(payPassWordCacheService
						.load(registertoken));
			}
			if (sendCount > Long.valueOf(sendMessageNumber)) {
				restP.setMessage("此手机发送校验码已经达到上限，请在24小时后重试");
				payPassWordCacheService.clear(request
						.getParameter("captcha_value") + env.getClientIp());
				restP.setSuccess(true);
				return restP;
			} else {*/
				// 封装发送短信请求
				AuthCodeRequest req = new AuthCodeRequest();
				req.setMobile(user.getLoginName());
				req.setBizId(session.getId());
				req.setBizType(BizType.SET_PAYPASSWD.getCode());
				String ticket = defaultUesService.encryptData(user.getLoginName());
				req.setMobileTicket(ticket);
				req.setValidity(CommonConstant.VALIDITY);
				// 发送短信
				if (defaultSmsService.sendMessage(req, env)) {
					restP.setSuccess(true);
					/*payPassWordCacheService.saveOrUpdate(registertoken,
							String.valueOf(++sendCount));*/
				} else {
					log.error("发送短信失败,手机号码：" +user.getLoginName());
					restP.setMessage("");
					restP.setSuccess(false);
				}
			/*}*/
			log.error("发送短信成功,手机号码：" +user.getLoginName());
			restP.setMessage("发送短信成功");
		} catch (Exception e) {
			log.error("发送短信失败:" + e.getMessage(), e.getCause());
			restP.setSuccess(false);
		}
		return restP;
	}
	
	public static String getDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 
	 * @param memberIdentity
	 *            会员身份
	 * @param loginName
	 *            登录名
	 * @param otpValue
	 *            验证码
	 * @param request
	 * @param env
	 * @return
	 */
	private boolean validateOtpValue(String memberIdentity, String loginName,
			String otpValue, HttpServletRequest request,
			OperationEnvironment env) {
		try {
			if ("email".equals(memberIdentity)
					|| "enterprise".equals(memberIdentity)) {
				String keyStr = loginName + "_" + otpValue.toLowerCase() + "_"
						+ env.getClientIp();
				String info = payPassWordCacheService.load(keyStr);
				if ((null == info) || "".equals(info)) {
					return false;
				}
			} else {
				AuthCodeRequest req = new AuthCodeRequest();
				req.setAuthCode(otpValue);
				req.setMobile(loginName);
				req.setMobileTicket(uesServiceClient.encryptData(loginName));
				req.setBizId(request.getSession().getId());
				req.setBizType(BizType.SET_PAYPASSWD.getCode());
				boolean result = defaultSmsService.verifyMobileAuthCode(req,
						env);
				return result;
			}
		} catch (Exception e) {
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
	
}
