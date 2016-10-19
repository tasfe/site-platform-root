package com.netfinworks.site.web.action.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.compiler.ast.Pair;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;

import com.kinggrid.pdf.KGPdfHummer;
import com.kinggrid.pdf.executes.PdfSignature4KG;
import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
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
import com.netfinworks.ma.service.response.CompanyMemberInfo;
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
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.editsupport.DateES;
import com.netfinworks.site.domain.editsupport.LoginNameTypeEnumES;
import com.netfinworks.site.domain.editsupport.OperatorLockStatusEnumES;
import com.netfinworks.site.domain.editsupport.OperatorStatusEnumES;
import com.netfinworks.site.domain.editsupport.OperatorTypeEnumES;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.LoginNameTypeEnum;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.OperatorLockStatusEnum;
import com.netfinworks.site.domain.enums.OperatorStatusEnum;
import com.netfinworks.site.domain.enums.OperatorTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.lflt.LfltService;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.form.LfltForm;
import com.netfinworks.site.web.action.validator.CommonValidator;
import com.netfinworks.site.web.common.constant.PayChannel;
import com.netfinworks.site.web.common.constant.PaymentType;
import com.netfinworks.site.web.common.converter.StringTrimCustomConverter;
import com.netfinworks.site.web.common.util.FormValidatorHolder;
import com.netfinworks.site.web.common.util.ObjectUtils;
import com.netfinworks.site.web.common.vo.SessionPage;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * <p>action操作基类</p>
 * @author eric
 * @version $Id: BaseAction.java, v 0.1 2013-7-18 下午6:15:00  Exp $
 */
public class BaseAction implements CommonConstant {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected final String POS_APP = "POS_APP";
	
	protected final String POS_CASHIER_APP = "POS_CASHIER_APP";

    @Resource
    private WebDynamicResource webDynamicResource;
    @Resource(name = "memberService")
	private MemberService commMemberService;

    @Resource(name = "passwordOcxService")
    private PasswordOcxService passwordOcxService;

    protected OperationEnvironment env;

    @Resource
    private OperatorAuthOuterService operatorAuthOuterService;

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
    
    @Resource(name = "defaultCertService")
    private DefaultCertService       defaultCertService;
    
    @Resource(name = "webResource")
    private WebDynamicResource        webResource;
    
    @Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade payPartyFeeFacade;
    
    @Resource
    private LfltService lfltService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	
	@Resource(name = "commonValidator")
    protected CommonValidator validator;
	
	@Resource(name = "xuCache")
	private XUCache<String> loginCache;
	
	public Map<String,Object> initOcx(){
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("ocx_skey", passwordOcxService.getSkey());
        data.put("ocx_enstr", passwordOcxService.getCipher(data.get("ocx_skey").toString()));
        return data;
    }
	
	protected ModelAndView initOcxView(){
    	ModelAndView mv = new ModelAndView();
    	RestResponse restP = new RestResponse();
    	Map<String, Object> data = initOcx();
        restP.setData(data);
        mv.addObject("response", restP);
        return mv;
    }
	
	public String[] getLastLoginInfo(HttpServletRequest request,String memId,String operId){
		String[] result = new String[2];
		String key = memId+"_"+operId;
		if(StringUtils.isBlank(loginCache.get("show_last_login_ip_"+key).get()) 
				|| StringUtils.isBlank(loginCache.get("show_last_login_time_"+key).get())){
			updateLastLoginInfo(request, memId, operId);
		}
		result[0] = loginCache.get("show_last_login_ip_"+key).get();
		result[1] = loginCache.get("show_last_login_time_"+key).get();
    	return result;
	}
	
	public void updateLastLoginInfo(HttpServletRequest request,String memId,String operId){
    	String key = memId+"_"+operId;
    	CacheRespone<String> last_login_ip = loginCache.get("last_login_ip_"+key);
    	CacheRespone<String> last_login_time = loginCache.get("last_login_time_"+key);
    	if(!last_login_ip.isSuccess() || StringUtils.isBlank(last_login_ip.get())){
    		loginCache.set("show_last_login_ip_"+key, getRemortIP(request),Integer.MAX_VALUE);
    		loginCache.set("last_login_ip_"+key, getRemortIP(request),Integer.MAX_VALUE);
    	}else{
    		loginCache.set("show_last_login_ip_"+key, last_login_ip.get(),Integer.MAX_VALUE);
    		loginCache.set("last_login_ip_"+key, getRemortIP(request),Integer.MAX_VALUE);
    	}
    	
    	if(!last_login_time.isSuccess() || StringUtils.isBlank(last_login_time.get())){
    		loginCache.set("show_last_login_time_"+key, new Date().getTime()+"",Integer.MAX_VALUE);
    		loginCache.set("last_login_time_"+key, new Date().getTime()+"",Integer.MAX_VALUE);
    	}else{
    		loginCache.set("show_last_login_time_"+key, last_login_time.get(),Integer.MAX_VALUE);
    		loginCache.set("last_login_time_"+key, new Date().getTime()+"",Integer.MAX_VALUE);
    	}
    }
    
    public String getRemortIP(HttpServletRequest request) {
    	String ip = request.getHeader("X-Forwarded-For");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
	}
    
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
    
    /**
     * UES加密
     * @param data 原文
     * @return 密文
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
     * 鉴权-判定当前登陆操作员是否有某项权限 -luoxun
     * @param request
     * @param functionId
     * @return
     */
    public boolean auth(HttpServletRequest request,String functionId){
        EnterpriseMember member = getUser(request);
        AuthVO authVO=new AuthVO();
        authVO.setSourceId(getSourceId());
        authVO.setFunctionId(functionId);
        authVO.setMemberId(member.getMemberId());
        authVO.setOperatorId(member.getCurrentOperatorId());
        try {
            return operatorAuthOuterService.checkFunctionFromOperator(authVO);
        } catch (BizException e) {
            logger.error("鉴权异常：{} 参数：functionId:{}",e,functionId);
        }
        return false;

    }

    /**
     * 返回当前登陆操作员所有已经分配的权限列表
     * @param request
     * @return
     * @throws BizException
     */
    public Map<String,FunctionVO> auth(HttpServletRequest request) throws BizException{
        EnterpriseMember member = getUser(request);
        AuthVO authVO=new AuthVO();
        authVO.setSourceId(getSourceId());
        authVO.setMemberId(member.getMemberId());
        authVO.setOperatorId(member.getCurrentOperatorId());
        List<FunctionVO> assignFunctions=operatorAuthOuterService.getFunctionListFromOperator(authVO);
        Map<String,FunctionVO> assignFunctionMap=new HashMap<String,FunctionVO>();
        for(FunctionVO functionVO:assignFunctions){
            if(functionVO!=null){
                assignFunctionMap.put(functionVO.getFunctionId(), functionVO);
            }
        }
        return assignFunctionMap;
    }

    /**
     * 检测用户是否已经实名认证
     * @param member
     * @return
     */
    protected boolean isNameVerify(EnterpriseMember member) {
    	boolean isNameVerify = false;
		AuthInfoRequest info = new AuthInfoRequest();
		info.setMemberId(member.getMemberId());
		info.setAuthType(AuthType.REAL_NAME);
		info.setOperator(member.getOperatorId());
		info.setMessage("merchant");
		AuthResultStatus state;
		try {
			state = defaultCertService.queryAuthType(info);
		} catch (ServiceException e) {
			logger.error("检测实名时出现异常", e);
			return false;
		}
		if (state.getCode().equals("PASS")) {
			isNameVerify = true;
		}
		
		return isNameVerify;
    }
    
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
        binder.registerCustomEditor(Date.class, new DateES());
        binder.registerCustomEditor(LoginNameTypeEnum.class, new LoginNameTypeEnumES());
        binder.registerCustomEditor(OperatorLockStatusEnum.class, new OperatorLockStatusEnumES());
        binder.registerCustomEditor(OperatorStatusEnum.class, new OperatorStatusEnumES());
        binder.registerCustomEditor(OperatorTypeEnum.class, new OperatorTypeEnumES());
    }

    /**
     * 验证用户是否存在
     *
     * @param user
     * @param error
     * @param restP
     * @return
     */
    protected ModelAndView checkUser(PersonMember user, Map<String, String> error, RestResponse restP) {
        if (user == null) {
            logger.error("用户没有登录,请先登录");
            error = new HashMap<String, String>();
            error.put("noLogin", "用户没有登录,请先登录");
            restP.setErrors(error);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
        }
        return null;
    }
    /**
     * 验证用户是否存在
     *
     * @param user
     * @param error
     * @param restP
     * @return
     */
    protected ModelAndView checkUser(EnterpriseMember user, Map<String, String> error, RestResponse restP) {
        if (user == null) {
            logger.error("用户没有登录,请先登录");
            error = new HashMap<String, String>();
            error.put("noLogin", "用户没有登录,请先登录");
            restP.setErrors(error);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
        }
        return null;
    }

    public Map<String, String> initError(List<ObjectError>  list) {
        Map<String, String> errorInfo = new HashMap<String, String>();
        for(ObjectError error :list) {
            String code = error.getDefaultMessage();
            errorInfo.put(code, code);
        }
        return errorInfo;
    }


    public String getSourceId(){
        return webDynamicResource.getSourceId();
    }


    /**
     * 获取用户信息
     * @return
     */
    protected EnterpriseMember getUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        EnterpriseMember user = null;
        String userString = session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER) != null
        		? session.getAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER).toString() : null;
        if (StringUtils.isNotBlank(userString)) {
        	user = JsonUtils.parse(userString, EnterpriseMember.class);
        }
        return user;
    }

    public boolean updateSessionObject(HttpServletRequest request) {
    	try {
    		String memberId = null;
    		if (null != getUser(request)) {
    			memberId = getUser(request).getMemberId();
    		}
			OperationEnvironment env = new OperationEnvironment();
			EnterpriseMember user = getUser(request);
			user.setMemberId(memberId);
			user = commMemberService.queryCompanyMember(user, env);
			HttpSession session = request.getSession();
			session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER_ID, user.getMemberId());
			session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_USER_LOGINNAME, user.getLoginName());

			CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(user, env);

			user.setLicenseNo(compInfo.getLicenseNo());
			user.setEnterpriseName(compInfo.getCompanyName());
			user.setSummary(compInfo.getSummary());

			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(user.getMemberId());
			info.setAuthType(AuthType.REAL_NAME);
			info.setOperator(user.getOperatorId());
			info.setMessage("merchant");
			AuthResultStatus state = defaultCertService.queryAuthType(info);
			user.setNameVerifyStatus(state);

			/*查询账户信息*/
			/*user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));*/
            // 查询绑定银行卡
            BankAccRequest reqAcc = new BankAccRequest();
            reqAcc.setMemberId(user.getMemberId());
            reqAcc.setClientIp(request.getRemoteAddr());
            List<BankAccountInfo> list = defaultBankAccountService
                    .queryBankAccount(reqAcc);
            user.setBankCardCount(list.size());

            session.setAttribute(CommonConstant.KJT_ENTERPRISE_USER_NAME, user.getMemberName());
            session.setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER, JsonUtils.toJson(user));
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
    		return "无法显示全部的错误信息";
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
        if(session==null){
            logger.info("decrpPassword session is null");
        }else{
            if(session.getAttribute("mcrypt_key")==null){
                logger.info("decrpPassword mcrypt_key is null");
            }
        }

        //随机因子
        String mcrypt_key = (String)session.getAttribute("mcrypt_key");
        //调用解密接口
		if ((null != password) && !"".equals(password)) {
			return MD5Util.MD5(passwordOcxService.decrpPassword(mcrypt_key, password));
		} else {
			return "";
		}
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
   	 * 检查支付密码
   	 * @param request HTTP请求对象
   	 * @param response 验证结果
   	 * @return true-成功，false-失败
   	 */
   	public boolean validatePayPassword(HttpServletRequest request, EnterpriseMember user, 
   			RestResponse response, ModelAndView mv) {
   		// 支付密码解密
   		String password = decrpPassword(request, request.getParameter("payPassword"));
		PayPasswdRequest payPasswdRequest = new PayPasswdRequest();
		payPasswdRequest.setAccountId(user.getDefaultAccountId());
		payPasswdRequest.setOperator(user.getCurrentOperatorId());
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
		if(pcheck.isLocked()) {
		    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
		            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
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
   	 * 检验手机校验码是否正确
   	 * @param request Http请求
   	 * @param response 返回异步请求结果
   	 * @param mv 返回到视图
   	 * @return true-成功，false-失败
   	 */
   	public boolean validateCaptcha(HttpServletRequest request, EnterpriseMember user, 
   			RestResponse response, ModelAndView mv, OperationEnvironment env) {
   		boolean result = false;
   		String memberId = user.getMemberId();
		try {
			EncryptData edata = memberService.decipherMember(memberId, DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
			String mobile = edata.getPlaintext();
			String mobileCaptcha = request.getParameter("mobileCaptcha");
			String bizType = request.getParameter("bizType");
			logger.info("手机号码:{}, 校验码:{}", mobile, mobileCaptcha);

			if (StringUtils.isEmpty(mobile)) {
				throw new Exception("该用户未绑定手机!");
			}
			
			// 封装校验短信校验码请求
			AuthCodeRequest req = new AuthCodeRequest();
			req.setMemberId(memberId);
			req.setMobile(mobile);
			String ticket = defaultUesService.encryptData(mobile);
			req.setMobileTicket(ticket);
			req.setAuthCode(mobileCaptcha);
			req.setBizId(memberId);
			req.setBizType(bizType);

			// 校验短信校验码
			result = defaultSmsService.verifyMobileAuthCode(req, env);
		} catch (Exception e) {
			logger.error("手机校验码校验异常，用户ID[{}]", memberId);
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
				response.setMessage("您输入的校验码不正确!");
			}
			if (mv != null) {
				mv.addObject("success", false);
				mv.addObject("message", "您输入的校验码不正确!");
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
					response.setMessage("您未插入快捷盾或证书已经过期！");
				}
				if (mv != null) {
					mv.addObject("success", false);
					mv.addObject("message", "您未插入快捷盾或证书已经过期！");
				}
			}
		} catch (Exception e) {
			logger.error("验证硬证书签名失败", e);
			if (response != null) {
				response.setMessage("您未插入快捷盾或证书已经过期！");
			}
			if (mv != null) {
				mv.addObject("success", false);
				mv.addObject("message", "您未插入快捷盾或证书已经过期！");
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
   	 * 实名认证
   	 * @param user
   	 * @param mv
   	 * @return
   	 */
   	public boolean validateRealnameCert(EnterpriseMember user) {
   		AuthInfoRequest info = new AuthInfoRequest();
		info.setMemberId(user.getMemberId());
		info.setAuthType(AuthType.REAL_NAME);
		info.setOperator(user.getOperatorId());
		info.setMessage("merchant");
		AuthResultStatus state;
		try {
			state = defaultCertService.queryAuthType(info);
			if (state.getCode().equals("PASS")) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
   	}
   	
   	/**
   	 * 获取默认银行卡
   	 * @param baList
   	 * @return
   	 */
   	public String getDefaultBankCard(List<BankAccountInfo> baList) {
   		if (baList != null){
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
	 * 查询每月的限额限次
	 * @param user
	 * @param mv
	 * @param response
	 * @param env
	 */
	public void queryLfltOfMonth(HttpServletRequest request, EnterpriseMember user, TradeType tradeType,
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
		logger.info("{}计算服务费：{}", productCode, req);
		PaymentPricingResponse resp = payPartyFeeFacade.getFee(req);
		logger.info("{}计算服务费返回：{}", productCode, resp);
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
		EnterpriseMember user = this.getUser(request);
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getCurrentOperatorId());
		certRequest.setCertificationType(type);
		certRequest.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
		RestResponse result = null;
		try {
			logger.info("查询{}证书是否被激活：{}", type, certRequest);
			result = certificationService.getCertByCertStatus(certRequest, env);
			logger.info("查询证书返回：{}", result.getData());
			if ((result != null) && result.isSuccess() && (result.getData() != null) 
					&& (result.getData().get("certSns") != null)) {
				return true;
			}
		} catch (BizException e) {
			logger.error("获取证书列表失败");
		}
		
		return false;
	}
	
	/**
     * 获取目标账户名称
     * @param baseMember 会员对象
     * @return 会员名称
	 * @throws Exception 
     */
    public String getTargetAccountName(BaseMember member, String accountNo) throws Exception {
        if (member == null) {
            member = defaultMemberService.isMemberExists(accountNo, SYSTEM_CODE, env);
            if (member == null || StringUtils.isEmpty(member.getMemberId())) {
                throw new Exception("收款人不存在");
            }
        }
        
        if (MemberType.PERSONAL.equals(member.getMemberType())) {
            // 个人会员直接取会员名称
            return member.getMemberName();
        } else {
            // 商户和企业取企业名称
            EnterpriseMember enterpriseMember = new EnterpriseMember();
            enterpriseMember.setMemberId(member.getMemberId());
            CompanyMemberInfo compInfo = null;
            
            
            try {
                compInfo = defaultMemberService.queryCompanyInfo(enterpriseMember, new OperationEnvironment());
            } catch (ServiceException e) {
                logger.error("查询账户信息失败", e);
            }
            if (compInfo != null) {
                return compInfo.getCompanyName();
            }
        }
        
        return StringUtils.EMPTY;
    }
	
    /**
     * 单笔格式电子回单
     * @param savepathnew 增加签章后的pdf
     * @param savepath 原pdf
     * 
     */
    public  void signature(String savepathnew,String savepath) throws IOException{
        KGPdfHummer hummer = null;
        FileInputStream cert = null;
        FileOutputStream  fileOutputStream = null;      
        try {
            logger.info("电子签章请求参数{}{}", webResource.getSignaturePath(), webResource.getKeystoreUrl());
            cert = new FileInputStream(webResource.getSignaturePath());
            fileOutputStream = new FileOutputStream(savepathnew);           
            
            hummer = KGPdfHummer.createSignature(savepath, null, 
                    true, fileOutputStream, null,true);
            hummer.setCertificate(cert, webResource.getKeystorePwd1(), webResource.getKeystorePwd2());
            PdfSignature4KG pdfSignature4KG = new PdfSignature4KG(
                    webResource.getKeystoreUrl(),0,"123456");
            //PdfSignature4KG pdfSignature4KG = new PdfSignature4KG(webResource.getKeystoreUrl(),KGServerTypeEnum.AUTO,"test","123456","永达互联网金融信息服务有限公司");
            pdfSignature4KG.setXY(520, 580);
            hummer.setPdfSignature(pdfSignature4KG);            
            hummer.doSignature();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("生成单笔格式电子签章失败:{}"+e);
        }  finally {
            cert.close();
            fileOutputStream.close();
            if(hummer != null) hummer.close();
        }
    }
    
    /**
     * 批量格式电子回单
     * @param savepathnew 增加签章后的pdf
     * @param savepath 原pdf
     * 
     */
    public  void batchSignature(String savepathnew,String savepath) throws IOException{
        KGPdfHummer hummer = null;
        FileInputStream cert = null;
        FileOutputStream  fileOutputStream = null;      
        try {
            logger.info("批量电子签章请求参数{}{}", webResource.getSignaturePath(), webResource.getKeystoreUrl());
            cert = new FileInputStream(webResource.getSignaturePath());
            fileOutputStream = new FileOutputStream(savepathnew);           
            
            hummer = KGPdfHummer.createSignature(savepath, null, 
                    true, fileOutputStream, null,true);
            hummer.setCertificate(cert, webResource.getKeystorePwd1(), webResource.getKeystorePwd2());
            PdfSignature4KG pdfSignature4KG = new PdfSignature4KG(
                    webResource.getKeystoreUrl(),0,"123456");
            //PdfSignature4KG pdfSignature4KG = new PdfSignature4KG(webResource.getKeystoreUrl(),KGServerTypeEnum.AUTO,"test","123456","永达互联网金融信息服务有限公司");
            pdfSignature4KG.setXY(700, 1000);
            hummer.setPdfSignature(pdfSignature4KG);            
            hummer.doSignature();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("生成批量格式电子签章失败:{}"+e);
        }  finally {
            cert.close();
            fileOutputStream.close();
            if(hummer != null) hummer.close();
        }
    }
}
