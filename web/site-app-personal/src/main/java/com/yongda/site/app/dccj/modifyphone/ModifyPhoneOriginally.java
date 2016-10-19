package com.yongda.site.app.dccj.modifyphone;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.UnionmaBindMobileEditRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBindMobileEditResponse;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.unionma.BindFacadeService;
import com.yongda.site.app.action.common.BaseAction;

/**
 * 修改手机号
 * @author yp csl
 *
 */
@Controller
public class ModifyPhoneOriginally extends BaseAction {
	
	private static Logger logger = LoggerFactory.getLogger(ModifyPhoneOriginally.class);

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;
	
	@Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "transferService")
	private TransferService transferService;
	
	@Resource(name = "xuCache")
	private XUCache<String> loginCache;
	
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	
	@Resource(name = "bindFacadeService")
	private BindFacadeService bindFacadeService;
	
	private static final String MEMBER_ID_ENTITY = "mobile";
	
	/**
	 * 短信校验码发送接口
	 * @param request
	 * @param response
	 * @param env
	 * @param phone
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/phone/modify/sendcode", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse sendMessage(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,String verify_mobile)throws Exception{
		RestResponse restP = new RestResponse();
		//验证参数
		if (StringUtils.isBlank(verify_mobile) || !Pattern.matches(RegexRule.MOBLIE, verify_mobile)) {
			restP.setMessage(StringUtils.isBlank(verify_mobile)?"手机号码不能为空":"手机号格式错误");
			restP.setCode(StringUtils.isBlank(verify_mobile)?
					CommonDefinedException.REQUIRED_FIELD_NOT_EXIST.getErrorCode():
					CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			restP.setSuccess(false);
			logger.error("手机号参数错误");
			return restP;
		}
		PersonMember user = getUser(request);
		try{
              String memberId = user.getMemberId();
            //发送短信
            if (transferService.sendMobileMessage(memberId,
            		verify_mobile, env, BizType.RESET_MOBILE)) {
                restP.setSuccess(true);
                logger.error("发送短信成功");
                restP.setMessage("发送短信成功");
            } else {
            	logger.error("发送短信失败");
    			restP.setSuccess(false);
    			restP.setMessage("发送短信失败");
            }
		} catch (Exception e) {
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
		}
		
		return restP;
	}
	/**
	 * 验证旧手机短信验证码（原手机可用）
	 * @param request
	 * @param response
	 * @param env
	 * @param verify_mobile 原手机号
	 * @param code 短信验证码
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/phone/modify/validoldphone", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse veridOldPhone(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,
			String code)throws Exception{
		RestResponse restP = new RestResponse();
		//验证参数
		if(code == null||code.equals("")){
			restP.setMessage("短信验证码不可为空");
			restP.setCode(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST
					.getErrorCode());
			restP.setSuccess(false);
			logger.error("缺少短信验证码参数！");
			return restP;
		}
		PersonMember user = getUser(request);
		try{
		    //验证短信验证码
		    String memberId = user.getMemberId();
		    EncryptData encryptData = memberService.decipherMember(
					memberId, DeciphedType.CELL_PHONE,
					DeciphedQueryFlag.ALL, env);
            // 获取解密手机号码进行验证码校验
			String mobile = encryptData.getPlaintext();
			logger.info("用户"+user.getLoginName()+"的手机号："+mobile);
		    Boolean flag = transferService.validateOtpValue(MEMBER_ID_ENTITY,user.getLoginName(),mobile,
					memberId, BizType.RESET_MOBILE, code, env);
		    if (!flag) {
			       restP.setMessage("该手机号对应的短信验证码有误或失效");
			       logger.error("该手机号对应的短信验证码有误或失效");
			       restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
					     .getErrorCode());
			       restP.setSuccess(false);
			       return restP;
		    }
		    restP.setSuccess(true);
		    restP.setMessage("短信验证码校验通过");
		    //创建token
		    String token = user.getMemberId() + "_" +"dccjVeridOldPhone"+ new Date().getTime();
		    loginCache.set(token, user.getMemberId(), 3600);
		    Map<String,Object> map=new HashMap<String,Object>();
		    map.put("token", token);
		    logger.info("参数token："+token);
		    restP.setData(map);
		} catch (Exception e) {
			restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
		}
		return restP;
	}
	
	/**
	 * 身份证验证接口（原手机号不可用）
	 * @param request
	 * @param response
	 * @param env
	 * @param idcard_no 身份证
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/phone/modify/valididcard", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse veridcard(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,
			String idcard_no){
		RestResponse restP = new RestResponse();
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		PersonMember user = getUser(request);
		Map<String,String> map=new HashMap<String, String>();
		/*参数校验*/
		if(idcard_no==null||idcard_no.equals("")){
			restP.setSuccess(false);
			restP.setMessage("参数校验失败，身份证格式不正确。");
			map.put(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(), "参数校验失败，身份证格式不正确。");
			restP.setErrors(map);
			logger.info("会员："+user.getMemberId()+":校验参数未通过");
			return restP;
		}
		if(user.getCertifyLevel().getCode().equals("0")){
			restP.setSuccess(false);
			restP.setMessage("该用户未进行实名认证");
			map.put(CommonDefinedException.GET_MEMBER_INFO_ERROR.getErrorCode(), "该用户未进行实名认证");
			restP.setErrors(map);
			logger.info("会员："+user.getMemberId()+":该用户未进行实名认证");
			return restP;
		}
		
		/*查询身份证号码*/
		try {
			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.IDENTITY);
			authInfoReq.setOperator(user.getOperatorId());
			AuthInfo info = defaultCertService.queryRealName(authInfoReq);
			if(!info.getAuthNo().equals(idcard_no)){
				restP.setSuccess(false);
				restP.setMessage("身份证信息比对结果不成功");
				map.put(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),
						CommonDefinedException.ILLEGAL_ARGUMENT.getErrorMsg());
				restP.setErrors(map);
				logger.info("会员："+user.getMemberId()+":身份证信息比对结果不成功");
				return restP;
			}
		} catch (ServiceException e) {
			restP.setSuccess(false);
			restP.setMessage("身份证校验失败");
			map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),
				CommonDefinedException.SYSTEM_ERROR.getErrorMsg());
			restP.setErrors(map);
			logger.error("查询实名认证信息失败:" + e.getMessage(), e.getCause());
			return restP;
		}
		//创建token
	    String token = user.getMemberId() + "_" +"dccjPhoneVeridcard"+ new Date().getTime();
	    loginCache.set(token, user.getMemberId(), 3600);
	    Map<String,Object> map2=new HashMap<String,Object>();
	    map2.put("token", token);
	    restP.setSuccess(true);
	    restP.setMessage("身份证校验成功");
	    restP.setData(map2);
		return restP;
	}
	/**
	 * 设置新的手机号
	 * @param request
	 * @param response
	 * @param env
	 * @param new_verify_mobile 需要绑定的新手机号
	 * @param code   			新手机的短信验证码
	 * @param token				
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/phone/modify/do", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse setNewPhone(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,
			String new_verify_mobile,String code,String token){
		RestResponse restP = new RestResponse();
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		PersonMember user = getUser(request);
		Map<String,String> map=new HashMap<String,String>();
		/*校验参数*/
		if(!vail(new_verify_mobile,code,token)){
			restP.setSuccess(false);
			restP.setMessage("参数校验失败，请确认业务参数是否正确");
			map.put(CommonDefinedException.REQUIRED_FIELD_NOT_EXIST.getErrorCode(), "参数校验失败，请确认业务参数是否正确");
			restP.setErrors(map);
			logger.info("会员："+user.getMemberId()+":校验参数未通过");
			return restP;
		}
		if (!Pattern.matches(RegexRule.MOBLIE, new_verify_mobile)) {
			restP.setMessage("手机号格式错误");
			restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			restP.setSuccess(false);
			logger.error("手机号参数错误");
			return restP;
		}
		//比较缓存中的token
		CacheRespone<String> cacherespone = loginCache.get(token);
		if(cacherespone.isSuccess()){
			if(!user.getMemberId().equals(cacherespone.get())){
				restP.setSuccess(false);
				restP.setMessage("token参数校验失败");
				map.put(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(), "token参数校验失败");
				restP.setErrors(map);
				logger.info("token参数："+token+":校验参数未通过");
				return restP;
			}
		}
		try{
		    //验证短信验证码
	        String memberId = user.getMemberId();
	        Boolean flag = transferService.validateOtpValue(MEMBER_ID_ENTITY,user.getLoginName(),new_verify_mobile,
				memberId, BizType.RESET_MOBILE, code, env);
	        if (!flag) {

		       restP.setMessage("该手机对应的短信验证码有误或失效");
		       logger.error("该手机对应的短信验证码有误或失效");
		       map.put(CommonDefinedException.ILLEGAL_ARGUMENT
				     .getErrorCode(), "该手机对应的短信验证码有误或失效");
		       restP.setErrors(map);
		       restP.setSuccess(false);
		       return restP;
	        }
	        //封装修改手机请求
	        UnionmaBindMobileEditRequest req=new UnionmaBindMobileEditRequest();
		    req.setMemberId(memberId);
		    req.setNewPhoneNumber(new_verify_mobile);
		    //设置新手机号
		    UnionmaBindMobileEditResponse resp=bindFacadeService.bindMobileEdit(req);
		    if(resp.getIsSuccess()){
			   restP.setSuccess(true);
			   restP.setMessage("绑定新手机成功");
		    }else {
			   restP.setMessage("绑定手机失败"+resp.getResponseMessage());
			   restP.setSuccess(false);
		    }
		}catch (Exception e) {
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
			map.put(CommonDefinedException.SYSTEM_ERROR.getErrorCode(), "对不起，服务器繁忙，请稍后再试！");
			restP.setErrors(map);
		}
		
		return restP;
	}
	
	
	private boolean vail(String mobile,String code,String token){
		if(mobile==null||code==null||token==null){
			return false;
		}
		if(mobile.equals("")||code.equals("")||token.equals("")){
			return false;
		}
		return true;
	}
	
}
