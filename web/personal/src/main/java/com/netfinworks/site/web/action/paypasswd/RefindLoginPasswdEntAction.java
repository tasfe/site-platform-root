package com.netfinworks.site.web.action.paypasswd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.KaptchaImageAction;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.processor.ValidationProcessor;

/**
 *
 * <p>找回登陆密码（短信方式、邮件方式）--企业用户</p>
 * @author luoxun
 * @version $Id: RefindLoginPasswdAction.java, v 0.1 2014-5-31 下午12:51:49 luoxun Exp $
 */
@Controller
@RequestMapping(value = "/my/refind/loginPwd/ent")
public class RefindLoginPasswdEntAction extends BaseAction {

    protected Logger                log = LoggerFactory.getLogger(getClass());

    @Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;
    
    @Resource
    private DefaultLoginPasswdService defaultLoginPasswdService;

    @Resource(name = "memberService")
    private MemberService memberService;

    @Resource(name = "defaultCertService")
    private DefaultCertService      defaultCertService;

    @Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;

    @Resource
    private ValidationProcessor     validationProcessor;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    @Resource(name = "webResource")
    private WebDynamicResource      webResource;

    @Resource
    private OperatorService operatorService;
    /**
     * 找回登陆密码首页
     * @param request
     * @param resp
     * @param model
     * @return
     */
    @RequestMapping(value = "/index.htm")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse resp,
                                             ModelMap model){
        return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/index");
    }
    
    
    @RequestMapping(value = "/checkpicture.htm")
	@ResponseBody
	// 1、验证登录账号图片验证码
	public RestResponse checkpicture(HttpServletRequest request, String code,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		if (KaptchaImageAction.validateRandCode(request, code)) {
			restP.setSuccess(true);
		} else {
			restP.setSuccess(false);
		}
		return restP;
	}


    /**
     * 跳转找回密码主页
     * @param request
     * @param resp
     * @param model
     * @return
     * @throws Exception
     * @throws MemberNotExistException
     */
    @RequestMapping(value = "/goMain.htm")
    public ModelAndView goMain(HttpServletRequest request,String selectlogin,String loginName,String imageVerifyCode, OperationEnvironment env) throws Exception{
        //检验参数
        if(StringUtils.isEmpty(loginName)||StringUtils.isEmpty(imageVerifyCode)){
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT);
        }
        if (!KaptchaImageAction.validateRandCode(request, imageVerifyCode)) {
            ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/index","errorMsg","验证码输入有误！");
            modelAndView.addObject("loginName", loginName);
            return modelAndView;
        }
       // 1、验证图片验证码
       // 2.验证用户名是否存在
        EnterpriseMember enterpriseMember = null;
        try {
            EnterpriseMember queryCondition=new EnterpriseMember();
            queryCondition.setLoginName(loginName);
            enterpriseMember = memberService.queryCompanyMember(queryCondition, env);
            super.setJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, enterpriseMember);
        } catch (MemberNotExistException e) {
            ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/index","accountNoExistMsg","账户名未注册！");
            modelAndView.addObject("loginName", loginName);
            return modelAndView;
        }
        
        AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(enterpriseMember.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(
				authVerifyInfo, env);
		boolean isBindEmail = false;
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				isBindEmail = true;
			}
		}

		boolean isNameVerify = false;
		AuthInfoRequest info = new AuthInfoRequest();
		info.setMemberId(enterpriseMember.getMemberId());
		info.setAuthType(AuthType.REAL_NAME);
		info.setOperator(enterpriseMember.getOperatorId());
		AuthResultStatus state = defaultCertService.queryAuthType(info);
		if (state.getCode().equals("PASS")) {
			isNameVerify = true;
		}
		 
		if (selectlogin.equals("1") && !isNameVerify && isBindEmail) {

			ModelAndView modelAndView = new ModelAndView(
					"redirect:/my/refind/loginPwd/goRefindEmail.htm");
			modelAndView.addObject("email",enterpriseMember.getEmail());
			return modelAndView;
		}  else if (!isNameVerify) {
			// 安全问题找回登陆密码
			ModelAndView modelAndView = new ModelAndView(
					"redirect:/my/refind/loginPwd/");

		} else if ((isNameVerify && isBindEmail)) {
			ModelAndView modelAndView = new ModelAndView(
					CommonConstant.URL_PREFIX
							+ "/loginPwd/refind/certificateno", "personMember",
							enterpriseMember);
			modelAndView.addObject("loginName", loginName);
			modelAndView.addObject("selectlogin", selectlogin);
			return modelAndView;
		}

       return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/main","enterpriseMember",enterpriseMember);
    }

   
    /**
     * 重置登陆密码
     * @param request
     * @param resp
     * @param model
     * @return
     * @throws ServiceException
     * @throws MemberNotExistException
     * @throws BizException
     */
    @RequestMapping(value = "/reSetLoginPwd.htm")
    public ModelAndView reSetLoginPwd(String loginPwd, String reLoginPwd,HttpServletRequest request , OperationEnvironment env) throws ServiceException, BizException, MemberNotExistException{


        if(request.getSession().getAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT)==null||
                !(Boolean) request.getSession().getAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT)){
                return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
            }
            if(request.getSession().getAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT)==null){
                return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
            }
            if(StringUtils.isEmpty(loginPwd)||StringUtils.isEmpty(reLoginPwd)){
                return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd","errorMsg","登陆密码和重复登陆密码均不能为空");
            }
            //登陆密码和确认密码不一致
            if(!loginPwd.equals(reLoginPwd)){
                return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd","errorMsg","登陆密码和确认密码不一致");
            }
            //解密
            loginPwd= decrpPassword(request,loginPwd);
            //清空随机因子
            super.deleteMcrypt(request);
            EnterpriseMember enterpriseMember=super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class);
            OperatorLoginPasswdRequest req = new OperatorLoginPasswdRequest();
            req.setValidateType(2);
            req.setPassword(loginPwd);
            req.setClientIp(request.getRemoteAddr());
            req.setOperatorId(enterpriseMember.getOperatorId());
            //会员标识
            req.setMemberIdentity(enterpriseMember.getMemberId());
            //会员标识平台类型  1.UID
            req.setPlatformType("1");
            //重置登陆密码锁
            operatorService.resetLoginPwdLock(enterpriseMember.getOperatorId(), env);
            //重置登陆密码
            CommResponse commResponse=defaultLoginPasswdService.setLoginPasswordEnt(req);
            String responseCode=commResponse.getResponseCode();
            if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(responseCode)){
                return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd","errorMsg","登陆密码不能和支付密码相同！");
            }
            request.getSession().removeAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT);
            request.getSession().removeAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/reSetLoginPwdResult");
    }

    /**
     * ajax 重发邮件
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/ajaxResendMail.htm")
    @ResponseBody
    public RestResponse ajaxResendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        if(super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class)==null){
            restP.setSuccess(false);
            return restP;
        }
        EnterpriseMember enterpriseMember=super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class);
        String email = "";
        EncryptData data = memberService.decipherMember(enterpriseMember.getMemberId(),
            DeciphedType.EMAIL, DeciphedQueryFlag.ALL, env);
        email = data.getPlaintext();
        if(StringUtils.isEmpty(email)){
            restP.setSuccess(false);
            return restP;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        String activeUrl =  request.getScheme()+"://"+request.getServerName() + ":" + request.getServerPort()+"/my/refind/loginPwd/ent/checkRefindEmail.htm?token="+token;
        logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName", StringUtils.isEmpty(enterpriseMember.getMemberName()) ? email : enterpriseMember.getMemberName());
        objParams.put("activeUrl", activeUrl);
        boolean emailResult = defaultPayPasswdService.sendEmail(email, BizType.REFIND_LOGIN_EMAIL.getCode(), objParams);
        logger.info("发送至{}邮箱的结果：{}", email, emailResult);
        if (emailResult) {
            // 调用统一缓存
            payPassWordCacheService.put(token, JSONArray.toJSONString(enterpriseMember));
        }
        restP.setSuccess(true);
        return restP;
    }

    @RequestMapping(value="/checkLoginNameIsExist.htm" , method=RequestMethod.POST)
    @ResponseBody
    public boolean checkLoginNameIsExist(String loginName,HttpServletResponse response,
            OperationEnvironment env) throws BizException {
        try {
            EnterpriseMember queryCondition=new EnterpriseMember();
            queryCondition.setLoginName(loginName);
            EnterpriseMember enterpriseMember = memberService.queryCompanyMember(queryCondition, env);
            return enterpriseMember!=null;
        } catch (MemberNotExistException e) {
            return false;
        }
    }

    

    /**
     * 通过邮件找回密码->发送激活邮件
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/goRefindEmail.htm")
    public ModelAndView sendMail(HttpServletRequest request, OperationEnvironment env) throws Exception {
        if(request.getSession().getAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT)==null){
            return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
        }
        EnterpriseMember enterpriseMember=super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class);
        String email = "";
        EncryptData data = memberService.decipherMember(enterpriseMember.getMemberId(),
            DeciphedType.EMAIL, DeciphedQueryFlag.ALL, env);
        email = data.getPlaintext();
        if(StringUtils.isEmpty(email)){
            return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        String activeUrl =  request.getScheme()+"://"+request.getServerName() + ":" + request.getServerPort()+"/my/refind/loginPwd/ent/checkRefindEmail.htm?token="+token;
        logger.info("激活邮箱的地址：{},发送邮件激活链接地址：{}", email, activeUrl);
        Map<String, Object> objParams = new HashMap<String, Object>();
        objParams.put("userName", StringUtils.isEmpty(enterpriseMember.getMemberName()) ? email : enterpriseMember.getMemberName());
        objParams.put("activeUrl", activeUrl);
        boolean emailResult = defaultPayPasswdService.sendEmail(email, BizType.REFIND_LOGIN_EMAIL.getCode(), objParams);
        logger.info("发送至{}邮箱的结果：{}", email, emailResult);
        if (emailResult) {
            // 调用统一缓存
            payPassWordCacheService.put(token, JSONArray.toJSONString(enterpriseMember));
        }
        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX +"/loginPwd/refind/ent/sendMailResult","emailResult",emailResult);
        modelAndView.addObject("email", enterpriseMember.getEmail());
        String loginMailUrl=LoginEmailAddress.getEmailLoginUrl(email);
        modelAndView.addObject("loginMailUrl", loginMailUrl);
        return modelAndView;
    }

    /**
     * 通过邮件找回密码->邮件校验
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/checkRefindEmail.htm")
    public ModelAndView checkRefindEmail(HttpServletRequest request, OperationEnvironment env) throws Exception {
        String token=request.getParameter("token");
        if(StringUtils.isEmpty(token)){
            return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
        }
        EnterpriseMember enterpriseMember=null;
        String jsonStr=payPassWordCacheService.load(token);
        if(StringUtils.isNotBlank(jsonStr)){
            enterpriseMember=JSONArray.parseObject(jsonStr, EnterpriseMember.class);
        }
        if(enterpriseMember==null){
            return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
        }
        super.setJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, enterpriseMember);
        request.getSession().setAttribute(CommonConstant.SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT, true);
        payPassWordCacheService.clear(token);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd");
    }

}
