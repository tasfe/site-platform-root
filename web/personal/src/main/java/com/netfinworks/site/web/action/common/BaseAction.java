package com.netfinworks.site.web.action.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.lflt.api.enums.LimitedTypeEnum;
import com.netfinworks.lflt.api.enums.PayRoleEnum;
import com.netfinworks.lflt.api.enums.RangeTypeEnum;
import com.netfinworks.lflt.api.enums.RangeValueEnum;
import com.netfinworks.lflt.api.request.check.GetQuatoTimesRequest;
import com.netfinworks.lflt.api.request.check.VerifyRequest;
import com.netfinworks.lflt.api.response.AvailableResponse;
import com.netfinworks.lflt.api.response.AvailableResult;
import com.netfinworks.lflt.api.response.CheckLfltResponse;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.mns.client.domain.PageInfo;
import com.netfinworks.payment.common.v2.enums.PartyRole;
import com.netfinworks.pbs.service.context.vo.PartyFeeInfo;
import com.netfinworks.pbs.service.context.vo.PayPricingReq;
import com.netfinworks.pbs.service.context.vo.PaymentPricingResponse;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.HttpUtil;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.lflt.LfltService;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.form.LfltForm;
import com.netfinworks.site.web.common.auth.AuthToken;
import com.netfinworks.site.web.common.constant.PayChannel;
import com.netfinworks.site.web.common.constant.PaymentType;
import com.netfinworks.site.web.common.converter.StringTrimCustomConverter;
import com.netfinworks.site.web.common.util.FormValidatorHolder;
import com.netfinworks.site.web.common.util.ObjectUtils;
import com.netfinworks.site.web.common.vo.SessionPage;
import com.netfinworks.site.web.util.ConvertObject;
import com.netfinworks.site.web.validator.CommonValidator;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * <p>action操作基类</p>
 * @author eric
 * @version $Id: BaseAction.java, v 0.1 2013-7-18 下午6:15:00  Exp $
 */
public class BaseAction implements CommonConstant {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "memberService")
	private MemberService commMemberService;

    @Resource(name = "passwordOcxService")
    private PasswordOcxService passwordOcxService;

    @Resource(name="accountService")
    private AccountService accountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;
    
    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;
    
    @Resource(name = "memberService")
    private MemberService     memberService;
    
    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;
    
    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
    
    @Resource(name = "webResource")
    private WebDynamicResource        webResource;
    
    @Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade payPartyFeeFacade;
    
    @Resource
    private LfltService lfltService;
    
    @Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 
    
    @Resource(name = "certificationService")
	private CertificationService certificationService;
    
    @Resource(name = "commonValidator")
    protected CommonValidator validator;
    
    /**
     * UES解密
     * @param ticket 密文
     * @return
     */
    public String getDataByTicket(String ticket) {
    	try {
			return uesServiceClient.getDataByTicket(ticket);
		} catch (BizException e) {
			logger.error("解密失败，密文：{}", ticket);
		}
    	return StringUtils.EMPTY;
    }
    
    public Map<String,Object> initOcx(){
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("ocx_skey", passwordOcxService.getSkey());
        data.put("ocx_enstr", passwordOcxService.getCipher(data.get("ocx_skey").toString()));
        return data;
    }
    
    protected void initOcx(ModelMap model){
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = initOcx();
        restP.setData(data);
    	model.addAttribute("response", restP);
    }
    
    protected ModelAndView initOcxView(){
    	ModelAndView mv = new ModelAndView();
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = initOcx();
        restP.setData(data);
        mv.addObject("response", restP);
        return mv;
    }
    
    /**
     * UES加密
     * @param data 原文
     * @return
     */
    public String getTicketFromData(String data) {
    	try {
			return uesServiceClient.encryptData(data);
		} catch (BizException e) {
			logger.error("解密失败，原文：{}", data);
		}
    	return StringUtils.EMPTY;
    }
    
    /**
     * 获取session中的值
     * @param session
     * @param key
     * @param clazz
     * @return
     */
    public <T> T getJsonAttribute(HttpSession session, String key,Class<T> clazz){
        if(session.getAttribute(key)==null){
            return null;
        }
        return JSONArray.parseObject((String) session.getAttribute(key), clazz);
    }

    /**
     * 往session中设值
     * @param session
     * @param key
     * @param value
     */
    public void setJsonAttribute(HttpSession session, String key,Object value){
        String jsonStr=JSONArray.toJSONString(value);
        session.setAttribute(key, jsonStr);
    }


    /**
     * 获取用户信息
     * @return
     */
    protected static AuthToken getAuth() {
        return (AuthToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 获取用户信息
     * @return
     */
    protected PersonMember getUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        PersonMember user = null;
        String userString = session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER) != null
        		? session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER).toString() : null;
        if (StringUtils.isNotBlank(userString)) {
        	user = JsonUtils.parse(userString, PersonMember.class);
        }
        return user;
    }
    protected EnterpriseMember getEntUser(HttpServletRequest request){
    	HttpSession session = request.getSession();
    	EnterpriseMember user = null;
    	String userString = session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER) != null
    			? session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER).toString() : null;
    	if(StringUtils.isNotBlank(userString)){
    		user = JsonUtils.parse(userString, EnterpriseMember.class);
    	}
    	return user;
    }

    /**
     * 设置全局信息
     * @return
     */
    protected void setResponseValues(HttpServletRequest request, HttpServletResponse resp,
                                            ModelMap model) {
        PersonMember user = getUser(request);
        model.put("member", user);
    }

    /**
     * 获取会员ID
     * @return
     */
//    protected static String getMemberId() {
//        return getAuth().getMember().getMemberId();
//    }
    
    /**
     * 获取会员ID
     * @return
     */

    protected String getMemberId(HttpServletRequest request) {
        return getUser(request).getMemberId();
    }

    /**
     * 表单验证
     * @param formName
     * @param obj
     * @param bindingResult
     */
    protected boolean formValidate(String formName, Object obj, BindingResult bindingResult) {
        // 获取表单校验
        Validator validator = FormValidatorHolder.getValidator(formName);
        // 校验信息
        validator.validate(obj, bindingResult);

        return bindingResult.hasErrors();
    }

    /**
     * 信息banding
     * @param request
     * @param binder
     * @throws Exception
     */
    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
                                                                                       throws Exception {
        binder.registerCustomEditor(String.class, new StringTrimCustomConverter(true));
    }

    public Map<String, String> initError(List<ObjectError> list) {
        Map<String, String> errorInfo = new HashMap<String, String>();
        for (ObjectError error : list) {
            String code = error.getDefaultMessage();
            errorInfo.put(code, code);
        }
        return errorInfo;
    }

    /**
     * 验证用户是否存在
     *
     * @param user
     * @param error
     * @param restP
     * @return
     */
    protected ModelAndView checkUser(PersonMember user, Map<String, String> error,
                                     RestResponse restP) {
        if (user == null) {
            logger.error("用户没有登录,请先登录");
            error = new HashMap<String, String>();
            error.put("noLogin", "用户没有登录,请先登录");
            restP.setErrors(error);
            return new ModelAndView(ResourceInfo.ERROR.getUrl(), "response", restP);
        }
        return null;
    }

    public boolean updateSessionObject(HttpServletRequest request) {
    	try {
    		PersonMember member = getUser(request);
    		String memberId = null;
    		if (null != member) {
    			memberId = member.getMemberId();
    		}
			OperationEnvironment env = new OperationEnvironment();
			PersonMember personMember = new PersonMember();
			if (memberId.startsWith("1")) {
				BaseMember baseMember = commMemberService.queryMemberById(memberId, env);
				BeanUtils.copyProperties(baseMember, personMember);
				personMember = commMemberService.queryMemberIntegratedInfo(personMember, env);
			} else if (memberId.startsWith("2")) {
				EnterpriseMember user = new EnterpriseMember();
				user.setMemberId(memberId);
				user = commMemberService.queryCompanyMember(user, env);
				ConvertObject.convert(user,personMember);
			}

			/*查询账户信息*/
            /*personMember.setAccount(accountService.queryAccountById(personMember.getDefaultAccountId(), env));*/
            // 查询绑定银行卡
            BankAccRequest reqAcc = new BankAccRequest();
            reqAcc.setMemberId(personMember.getMemberId());
            reqAcc.setClientIp(request.getRemoteAddr());
            List<BankAccountInfo> list = defaultBankAccountService
                    .queryBankAccount(reqAcc);
            personMember.setBankCardCount(list.size());

			HttpSession session = request.getSession();
			session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER_ID, personMember.getMemberId());
			session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_USER_LOGINNAME, personMember.getLoginName());
			session.setAttribute(
            		CommonConstant.KJT_PERSON_USER_NAME,
            		personMember.getMemberName());
			session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER, JsonUtils.toJson(personMember));
			logger.info("Session 更新成功");
		} catch (Exception e) {
			logger.info("Session 更新失败");
			logger.error("",e);
			return false;
		}
    	return true;
    }

    public String getErrMsg(String message) {
    	if (message.length() > 40) {
    		return "系统内部错误";
    	}
    	if (message.contains("[")) {
    		int index = message.indexOf("[");
    		return message.substring(0,index);
		} else {
			return message;
		}
    }

    protected String decrpPassword(HttpServletRequest request, String password) {
        HttpSession session = request.getSession();
        //随机因子
        String mcrypt_key = session.getAttribute("mcrypt_key").toString();
        //调用解密接口
		return MD5Util.MD5(passwordOcxService.decrpPassword(mcrypt_key, password));
    }

    protected void deleteMcrypt(HttpServletRequest request) {
        request.getSession().removeAttribute("mcrypt_key");//清空session中随机因子
    }

    protected Map<String, String> passwordErrorInfo(HttpServletRequest request){
        Map<String, String> errorMap = new HashMap<String, String>();
        String error = request.getParameter("error");
        String remainNum = request.getParameter("remainNum");
        if (ERROR_LOCKED.equals(error)) {
            errorMap.put("remainNum", remainNum);
            errorMap.put("error_passwd_is_locked", "error_passwd_is_locked");
        }
        if (ERROR_NOTRIGHT.equals(error)) {
            errorMap.put("remainNum", remainNum);
            errorMap.put("error_passwd_not_right", "error_passwd_not_right");
        }
        return errorMap;
    }

    /**
     * 获取指定元素的解密信息
     * @param request　请求对象
     * @param type　解密类型
     * @param env   环境对象
     * @return
     * @throws Exception
     */
    public String getEncryptInfo(HttpServletRequest request, DeciphedType type,
                                 OperationEnvironment env) throws Exception {
    	EncryptData encryptData = commMemberService.decipherMember(getUser(request).getMemberId(),
            type, DeciphedQueryFlag.ALL, env);
    	if (encryptData != null) {
    	    return encryptData.getPlaintext();
    	}
        
    	return null;
    }
    
    /**
     * 根据Id获取指定元素的解密信息
     * @param memberId　Id
     * @param type　解密类型
     * @param env   环境对象
     * @return
     * @throws Exception
     */
    public String getEncryptInfo(String memberId, DeciphedType type,
    		OperationEnvironment env) throws Exception {
    	EncryptData encryptData = commMemberService.decipherMember(memberId,
    			type, DeciphedQueryFlag.ALL, env);
    	return encryptData.getPlaintext();
    }
    
    /**
     * 获取认证信息和认证ID
     * @param authVerifyService
     * @param request
     * @param verifyType
     * @param env
     * @return
     */
    public AuthVerifyInfo getVerifyInfo(AuthVerifyService authVerifyService, HttpServletRequest request, VerifyType verifyType, OperationEnvironment env) {
        //查询当前认证ID与加密认证信息
        AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
        authVerifyInfo.setMemberId(getUser(request).getMemberId());
        authVerifyInfo.setVerifyType(verifyType.getCode());
        List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
        if (infos.size() > 0) {
            authVerifyInfo = infos.get(0);
        }
        return authVerifyInfo;
    }
    
    /**
   	 * 获取默认银行卡
   	 * @param baList
   	 * @return
   	 */
   	public String getDefaultBankCard(List<BankAccountInfo> baList) {
   		if (baList != null) {
			for (BankAccountInfo info : baList) {
				String extention = info.getExtention();
				if (StringUtils.isNotBlank(extention)) {
					JSONObject obj = (JSONObject) JSONObject.parse(extention);
					if ((null != obj) && "1" . equals(obj.get("isDefaultcard"))) {
						return info.getBankcardId();
					}
				}
			}
   		}
		return StringUtils.EMPTY;
	}
   	
   	/**
   	 * 检查支付密码
   	 * @param request HTTP请求对象
   	 * @param response 验证结果
   	 * @return true-成功，false-失败
   	 */
   	public boolean validatePayPassword(HttpServletRequest request, PersonMember user, 
   			RestResponse response, ModelAndView mv) {
   		// 支付密码解密
   		String password = decrpPassword(request, request.getParameter("payPassword"));
		PayPasswdRequest payPasswdRequest = new PayPasswdRequest();
		payPasswdRequest.setAccountId(user.getDefaultAccountId());
		payPasswdRequest.setOperator(user.getOperatorId());
		payPasswdRequest.setPassword(password);
		payPasswdRequest.setValidateType(2);
   		
   		// 检查支付密码
		PayPasswdCheck pcheck = null;
		try {
			pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswdRequest);
		} catch (ServiceException e) {
			logger.error("支付密码校验异常", e);
			if (response != null) {
				response.setMessage("您输入的密码不正确!");
			}
			if (mv != null) {
				mv.addObject("success", false);
				mv.addObject("message", "您输入的密码不正确!");
			}
			
			return false;
		}
		if (pcheck == null) {
			logger.error("支付密码校验异常，用户ID[{}]", user.getMemberId());
			if (response != null) {
				response.setMessage("您输入的密码不正确!");
			}
			if (mv != null) {
				mv.addObject("success", false);
				mv.addObject("message", "您输入的密码不正确!");
			}
			return false;
		}
		if (!pcheck.isSuccess()) {
			if (pcheck.isLocked()) {
				logger.info("用户{}账户被锁定", user.getLoginName());
				if (response != null) {
    				response.setMessage("您的账户被锁定，请10分钟后再尝试!");
    			}
    			if (mv != null) {
    				mv.addObject("success", false);
    				mv.addObject("message", "您的账户被锁定，请10分钟后再尝试!");
    			}
			} else {
				String msg = "对不起，您输入的支付密码有误，剩余尝试次数" + pcheck.getRemainNum() + "次!";
				if (response != null) {
    				response.setMessage(msg);
    			}
    			if (mv != null) {
    				mv.addObject("success", false);
    				mv.addObject("message", msg);
    			}
			}
			return false;
		}
		
		if (response != null) {
			response.setSuccess(true);
		}
		if (mv != null) {
			mv.addObject("success", false);
		}
		
   		return true;
   	}
   	
   	/**
   	 * 检验手机验证码是否正确
   	 * @param request Http请求
   	 * @param response 返回异步请求结果
   	 * @param mv 返回到视图
   	 * @return true-成功，false-失败
   	 */
   	public boolean validateCaptcha(HttpServletRequest request, PersonMember user, 
   			RestResponse response, ModelAndView mv, OperationEnvironment env) {
   		boolean result = false;
   		String memberId = user.getMemberId();
		try {
			EncryptData edata = memberService.decipherMember(memberId, DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
			String mobile = edata.getPlaintext();
			String mobileCaptcha = request.getParameter("mobileCaptcha");
			String bizType = request.getParameter("bizType");
			logger.info("手机号码:{}, 验证码:{}", mobile, mobileCaptcha);

			if (StringUtils.isEmpty(mobile)) {
				throw new Exception("该用户未绑定手机!");
			}
			
			// 封装校验短信验证码请求
			AuthCodeRequest req = new AuthCodeRequest();
			req.setMemberId(memberId);
			req.setMobile(mobile);
			String ticket = defaultUesService.encryptData(mobile);
			req.setMobileTicket(ticket);
			req.setAuthCode(mobileCaptcha);
			req.setBizId(memberId);
			req.setBizType(bizType);

			// 校验短信验证码
			result = defaultSmsService.verifyMobileAuthCode(req, env);
		} catch (Exception e) {
			logger.error("手机验证码校验异常，用户ID[{}]", memberId);
			if (response != null) {
				response.setMessage("系统内部错误!");
			}
			if (mv != null) {
				mv.addObject("success", false);
				mv.addObject("message", "系统内部错误!");
			}
			return false;
		}
        
        if (result) {
        	if (response != null) {
    			response.setSuccess(true);
    		}
    		if (mv != null) {
    			mv.addObject("success", false);
    		}
        } else {
        	if (response != null) {
				response.setMessage("您输入的验证码不正确!");
			}
			if (mv != null) {
				mv.addObject("success", false);
				mv.addObject("message", "您输入的验证码不正确!");
			}
        }
        
        return result;
   	}
   	
   	/**
   	 * 验证硬证书签名
   	 * @param request HTTP请求
   	 * @param plain 明文
   	 * @param signData 签名
   	 * @param response 异步请求对象
   	 * @param mv 视图页面对象
   	 * @param env 环境变量对象
   	 * @return true-成功，false-失败
   	 * @throws UnsupportedEncodingException 
   	 */
   	protected boolean validateSignature(HttpServletRequest request, String plain, String signData, 
   			RestResponse response, ModelAndView mv, OperationEnvironment env) throws UnsupportedEncodingException {
   		boolean result = false;
   		request.setCharacterEncoding("UTF-8");
   		try {
   			StringBuilder sb = new StringBuilder();
   			
   			// Ajax请求需要在客户端编码否则此时已经出现编码丢失
   			if (!HttpUtil.isAjaxRequest(request)) {
   				// 发送HTTP请求需要将特殊符号（如“+”）编码，否则会出现编码丢失
   				signData = URLEncoder.encode(signData, "UTF-8");
   				plain = URLEncoder.encode(plain, "UTF-8");
   			}
			sb.append("oriToSign=").append(plain).append("&signedData=").append(signData);
			String httpRes = HttpUtil.submitDataByDoPost(sb.toString(), webResource.getSignatureAddress());
			logger.info("验证签名结果：" + httpRes);
			
			JSONObject jsonObj = JSONArray.parseObject(httpRes);
			String flag = jsonObj.getString("flag");
			if ("true".equalsIgnoreCase(flag)) {
				result = true;
			} else {
				if (response != null) {
					response.setMessage("您未插入快捷盾或未安装数字证书！");
				}
				if (mv != null) {
					mv.addObject("success", false);
					mv.addObject("message", "您未插入快捷盾或未安装数字证书！");
				}
			}
		} catch (Exception e) {
			logger.error("验证证书签名失败", e);
			if (response != null) {
				response.setMessage("您未插入快捷盾或未安装数字证书！");
			}
			if (mv != null) {
				mv.addObject("success", false);
				mv.addObject("message", "您未插入快捷盾或未安装数字证书！");
			}
		}
   		
   		return result;
	}
   	
   	/**
   	 * 获取分页数据
   	 * @param dataList
   	 * @param page
   	 * @return
   	 */
   	public <T> void setSessionPage(List<T> dataList, SessionPage<T> page) {
   		if ((page == null) || (page.getDataList() == null)
   				|| (page.getPage() == null) || ObjectUtils.isListEmpty(dataList)) {
   			return;
   		}
   		int size = dataList.size();
   		
   		// 小于每页条数，直接返回
   		if (size <= page.getPage().getPageSize()) {
   			page.setDataList(dataList);
   			page.getPage().setTotalCount(size);
   			return;
   		}
   		
   		// 去当前页数据
   		PageInfo pageInfo = page.getPage();
   		pageInfo.setTotalCount(size);
   		int fromIndex = (pageInfo.getCurrentPage() - 1) * pageInfo.getPageSize();
   		int toIndex = (fromIndex + pageInfo.getPageSize()) > size ? size : fromIndex + pageInfo.getPageSize();
   		page.getDataList().addAll(dataList.subList(fromIndex, toIndex));
   		
		return;
   	}
   	
   	/**
	 * 查询每月的限额限次
	 * @param user
	 * @param mv
	 * @param response
	 * @param env
	 */
	public void queryLfltOfMonth(HttpServletRequest request, PersonMember user, TradeType tradeType,
			ModelAndView mv, RestResponse response, OperationEnvironment env) {
		LfltForm lfltForm = new LfltForm();
		
		// 查询限额限次
		GetQuatoTimesRequest gqtReq = new GetQuatoTimesRequest();
		gqtReq.setMemberId(user.getMemberId());
		gqtReq.setPayCode(PaymentType.PAY_FUND.getCode());
		gqtReq.setPayRole(PayRoleEnum.PAYER);
		gqtReq.setProductCode(tradeType.getBizProductCode());
		if (TradeType.TRANSFER.getBizProductCode().equals(tradeType.getBizProductCode())) {
			gqtReq.setPaychannel(PayChannel.BALANCE.getCode());
		} else {
			gqtReq.setPaychannel(PayChannel.FUNDOUT.getCode());
		}
		AvailableResponse resp = lfltService.getAvailableLimit(gqtReq, env);
		
		if ((resp != null) && ObjectUtils.isListNotEmpty(resp.getAvailableResultList())) {
			List<AvailableResult> resList = resp.getAvailableResultList();
			for (AvailableResult res : resList) {
				// 统计月累计限制
				if ((res.getRangeValue() != null) && RangeValueEnum.MONTH.getCode().equals(res.getRangeValue().getCode())
						&& RangeTypeEnum.ACCUMULATED.getCode().equals(res.getRangeType().getCode())) {
					if (LimitedTypeEnum.QUOTA.getCode() == res.getAvailableType().getCode()) {
						// 累计每月限额
						lfltForm.setQuotaPerMonth(res.getAvailableValue());
					} else {
						// 累计每月限次
						lfltForm.setTimesPerMonth(res.getAvailableValue());
					}
				} else if (RangeTypeEnum.SINGLE.getCode().equals(res.getRangeType().getCode())) {
					// 单笔限额
					lfltForm.setQuota(res.getAvailableValue());
				}
			}
		}
	
		if (mv != null) {
			mv.addObject("lfltForm", lfltForm);
		} 
		if (response != null) {
			response.setMessageObj(lfltForm);
		}
	}
	
	/**
	 * 验证限额限次
	 * @param amount
	 * @param tradeType
	 * @param mv
	 * @param response
	 * @param env
	 * @return
	 */
	public boolean validateLflt(String memeberId, String payee,Money amount, TradeType tradeType,
			ModelAndView mv, RestResponse response, OperationEnvironment env) {
		try {
			VerifyRequest verReq = new VerifyRequest();
			
			verReq.setAmount(amount);
			verReq.setProductCode(tradeType.getBizProductCode());
			if (TradeType.TRANSFER.getBizProductCode().equals(tradeType.getBizProductCode())) {
				verReq.setPaychannel(PayChannel.BALANCE.getCode());
			} else {
				verReq.setPaychannel(PayChannel.FUNDOUT.getCode());
			}
			verReq.setPayer(memeberId);
			if(!"".equals(payee)){
			    verReq.setPayee(payee);
			}
			verReq.setPayCode(PaymentType.PAY_FUND.getCode());
			CheckLfltResponse lfltResp = lfltService.verifyLimit(verReq, env);
			
			if ((lfltResp != null) && lfltResp.isCheckResult()) {
				return true;
			} else {
				logger.info("限额限次验证结果：{},{}", lfltResp.getResponseCode(), lfltResp.getResponseMessage());
				if (mv != null) {
					mv.addObject("message", lfltResp.getResponseMessage());
				} 
				if (response != null) {
					response.setMessage(lfltResp.getResponseMessage());
				}
			}
		} catch (Exception e) {
			logger.error("验证限额限次异常", e);
		}
		
		return false;
	}
	
	/**
	 * 查询服务费用
	 * @param request
	 * @param money
	 * @param productCode
	 * @param response
	 */
	public String getServiceFee(HttpServletRequest request, String money, String productCode, RestResponse response) {
		if ((request == null) || (money == null) || StringUtils.isEmpty(productCode)) {
			throw new IllegalArgumentException("算费参数有误");
		}
		
		// 先获取用户信息
		String memberId = this.getMemberId(request);
		
		// 创建一个请求对象
		PayPricingReq req = new PayPricingReq();
		req.setAccessChannel("WEB");
		req.setSourceCode(SYSTEM_CODE);
		req.setRequestNo(String.valueOf(System.currentTimeMillis()));
		req.setProductCode(productCode);
		req.setPaymentCode(PaymentType.PAY_FUND.getCode());
		if (TradeType.TRANSFER.getBizProductCode().equalsIgnoreCase(productCode)) {
			req.setPayChannel(PayChannel.BALANCE.getCode());
		} else {
			req.setPayChannel(PayChannel.FUNDOUT.getCode());
		}
		req.setPaymentInitiate(new Date());
		req.setPayableAmount(new Money(money));
		req.setOrderAmount(new Money(money));
		
		// 付款方
		List<com.netfinworks.pbs.service.context.vo.PartyInfo> partyList 
			= new ArrayList<com.netfinworks.pbs.service.context.vo.PartyInfo>();
		com.netfinworks.pbs.service.context.vo.PartyInfo party 
			= new com.netfinworks.pbs.service.context.vo.PartyInfo();
		party.setMemberId(memberId);
		party.setPartyRole(PartyRole.PAYER);
		partyList.add(party);
		req.setPartyInfoList(partyList);
		
		// 调用服务费计算接口
		PaymentPricingResponse resp = payPartyFeeFacade.getFee(req);
		if ((resp != null) && resp.isSuccess()) {
			if (response != null) {
				response.setSuccess(true);
			}
			List<PartyFeeInfo> pfiList = resp.getPartyFeeList();
			if (ObjectUtils.isListNotEmpty(pfiList)) {
				for (PartyFeeInfo pfi : pfiList) {
					if (memberId.equals(pfi.getMemberId())) {
						String fee = pfi.getFee().toString();
						if (response != null) {
							response.setMessage(fee);
						}
						return fee;
					}
				}
			}
		}
		
		return StringUtils.EMPTY;
	}
	
	/**
	 * 检查证书是否激活
	 * @param request
	 * @param type
	 * @param env
	 * @return
	 */
	public boolean isCertActive(HttpServletRequest request, String type, OperationEnvironment env) {
		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		PersonMember user = this.getUser(request);
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getOperatorId());
		certRequest.setCertificationType(type);
		certRequest.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
		RestResponse result = null;
		try {
			logger.info("查询{}证书是否被激活：{}", type, certRequest);
			result = certificationService.getCertByCertStatus(certRequest, env);
			logger.info("查询证书返回：{}", result);
			 if (result != null && result.isSuccess() && result.getData() != null 
					 && result.getData().get("certSns") != null) {
				 return true;
			 }
		} catch (BizException e) {
			logger.error("获取证书列表失败");
		}
		
		return false;
	}
}
