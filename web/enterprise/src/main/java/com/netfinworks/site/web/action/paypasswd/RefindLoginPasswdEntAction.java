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
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.LoginEmailAddress;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.KaptchaImageAction;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;

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

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    @Resource(name = "webResource")
    private WebDynamicResource      webResource;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

    @Resource
    private OperatorService operatorService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
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
	public ModelAndView goMain(HttpServletRequest request, String loginName, String captcha_value,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
        //检验参数
		if (StringUtils.isEmpty(loginName) || StringUtils.isEmpty(captcha_value)) {
			restP.setMessage("参数错误！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/index", "response", restP);
        }
		
		String rs = KaptchaImageAction.validateRandCode(request, captcha_value);
		if (!"success".equals(rs)) {
			if("expire".equals(rs))
				restP.setMessage("验证码失效");
			else
				restP.setMessage("验证码错误");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/index", "response", restP);
		}
		
       // 1、验证图片验证码
       // 2.验证用户名是否存在
        EnterpriseMember enterpriseMember = null;
        try {
            EnterpriseMember queryCondition=new EnterpriseMember();
            queryCondition.setLoginName(loginName);
            enterpriseMember = memberService.queryCompanyMember(queryCondition, env);
            enterpriseMember.setLoginName(loginName);
            super.setJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, enterpriseMember);

        } catch (MemberNotExistException e) {
			restP.setMessage("账户名未注册！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/index", "response", restP);
        }

		AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
		authVerifyInfo.setMemberId(enterpriseMember.getMemberId());
		List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
		boolean isBindPhone = false;
		boolean isBindEmail = false;
		for (int i = 0; i < infos.size(); i++) {
			authVerifyInfo = infos.get(i);
			if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
				isBindPhone = true;
			} else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
				isBindEmail = true;
			}
		}

		boolean isNameVerify = false;
		AuthInfoRequest info = new AuthInfoRequest();
		info.setMemberId(enterpriseMember.getMemberId());
		info.setAuthType(AuthType.REAL_NAME);
		info.setOperator(enterpriseMember.getOperatorId());
		info.setMessage("merchant");
		AuthResultStatus state = defaultCertService.queryAuthType(info);
		if (state.getCode().equals("PASS")) {
			isNameVerify = true;
		}
		if ((isNameVerify != isBindPhone) && !(isNameVerify && isBindPhone)) {
			if (!isBindEmail) {
				restP.setMessage("请先绑定邮箱！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
		}

		if (!isNameVerify && !isBindPhone) {
			if (!isBindEmail) {
				restP.setMessage("请先绑定邮箱！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
			request.getSession().setAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN,
					CommonConstant.REFIND_LOGINPWD_TOKEN);
			request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
			// 记录找回密码
			logger.info(LogUtil.generateMsg(OperateTypeEnum.FIND_LOGIN_PWD, enterpriseMember, env, "通过邮箱找回登录密码"));
			return new ModelAndView("redirect:/my/refind/loginPwd/ent/goRefindEmail.htm");
		}
		data.put("isNameVerify", isNameVerify);
		data.put("isBindPhone", isBindPhone);
        // 记录找回密码
        logger.info(LogUtil.generateMsg(OperateTypeEnum.FIND_LOGIN_PWD, enterpriseMember, env, "通过手机找回登录密码"));
		return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/phoneFirst", "response", restP);

    }

    /**
     * 跳转手机-验证手机短信码
     * @param request
     * @param resp
     * @param model
     * @return
     * @throws BizException
     * @throws ServiceException
     */
    @RequestMapping(value = "/checkTelMsgCode.htm")
    public ModelAndView checkTelMsgCode(String phoneVerifyCode,HttpServletRequest request, OperationEnvironment env) throws BizException, ServiceException{
        if(request.getSession().getAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT)==null){
            return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
        }
        EnterpriseMember enterpriseMember=super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class);
        RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		String isNameVerify = request.getParameter("isNameVerify");
		String isBindPhone = request.getParameter("isBindPhone");

		if ("true".equals(isBindPhone)) {
			// 1、验证短信验证码是否正确
			AuthCodeRequest req = new AuthCodeRequest();
			EncryptData encryptData = memberService.decipherMember(enterpriseMember.getMemberId(),
					DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
			String mobile = encryptData.getPlaintext(); // 获取解密手机号码进行验证码校验
			req.setMemberId(enterpriseMember.getMemberId());
			req.setMobile(mobile);
			String ticket = defaultUesService.encryptData(mobile);
			req.setMobileTicket(ticket);
			req.setAuthCode(phoneVerifyCode);
			req.setBizId(enterpriseMember.getMemberId());
			req.setBizType(BizType.REFIND_LOGIN_SMS.getCode());
			// 校验短信验证码
			boolean messageResult = defaultSmsService.verifyMobileAuthCode(req, env);
			if (!messageResult) {
				data.put("isBindPhone", isBindPhone);
				data.put("isNameVerify", isNameVerify);
				restP.setMessage("您输入的短信验证码不正确，请重新输入！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/phoneFirst", "response",
						restP);
			}
		}
		if ("true".equals(isNameVerify)) {
			String licenseNo = request.getParameter("licenseNo");
			CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(enterpriseMember, env);
			if (!licenseNo.equals(compInfo.getLicenseNo())) {
				data.put("isBindPhone", isBindPhone);
				data.put("isNameVerify", isNameVerify);
				restP.setMessage("营业执照号错误！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/phoneFirst", "response",
						restP);
			}
		}
		if ("true".equals(isNameVerify) && "true".equals(isBindPhone)) {
			request.getSession().setAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN,
					CommonConstant.REFIND_LOGINPWD_TOKEN);
	        return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd","response",restP);
		}
		request.getSession().setAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN, CommonConstant.REFIND_LOGINPWD_TOKEN);
		request.getSession().setAttribute(CommonConstant.REFRESH_TOKEN, CommonConstant.REFRESH_TOKEN);
		return new ModelAndView("redirect:/my/refind/loginPwd/ent/goRefindEmail.htm");
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
	public ModelAndView reSetLoginPwd(String login_pw, String reLoginPwd, HttpServletRequest request,
			OperationEnvironment env) throws ServiceException, BizException, MemberNotExistException {

		String token = (String) request.getSession().getAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN);
		if (!token.equals(CommonConstant.REFIND_LOGINPWD_TOKEN)) {
			return new ModelAndView("redirect:/my/refind/loginPwd/ent/index.htm");
		}

		// 解密
		login_pw = decrpPassword(request, login_pw);
		reLoginPwd = decrpPassword(request, reLoginPwd);

		if (StringUtils.isEmpty(login_pw) || StringUtils.isEmpty(reLoginPwd)) {
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd", "errorMsg",
					"登录密码和重复登录密码均不能为空");
            }
            //登陆密码和确认密码不一致
		if (!login_pw.equals(reLoginPwd)) {
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd", "errorMsg",
					"登录密码和确认密码不一致");
            }

            //清空随机因子
            super.deleteMcrypt(request);
            EnterpriseMember enterpriseMember=super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class);
            OperatorLoginPasswdRequest req = new OperatorLoginPasswdRequest();
            req.setValidateType(2);
		req.setPassword(login_pw);
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
			return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd", "errorMsg",
					"登录密码不能和支付密码相同！");
            }
		request.getSession().removeAttribute(CommonConstant.REFIND_LOGINPWD_TOKEN);
		request.getSession().removeAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/reSetLoginPwdResult");
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
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/my/refind/loginPwd/ent/checkRefindEmail.htm?token=" + token;
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

    /**
     * 异步发送短信验证码
     *
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/ajaxSendSms.htm")
    public @ResponseBody
    RestResponse sendMessageByMobile(HttpServletRequest request, OperationEnvironment env)
                                                                                          throws Exception {
        RestResponse restP = new RestResponse();
        try {
            if(request.getSession().getAttribute(CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT)==null){
                restP.setSuccess(false);
                return restP;
            }
            EnterpriseMember enterpriseMember=super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_RESET_LOGIN_PWD_MEMBER_ENT, EnterpriseMember.class);
            String mobile="";
            if (StringUtils.isNotEmpty(enterpriseMember.getMemberId())) {
                EncryptData data = memberService.decipherMember(enterpriseMember.getMemberId(),
                    DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
                mobile = data.getPlaintext();
            }
            if(StringUtils.isNotEmpty(mobile)){
                //封装发送短信请求
                AuthCodeRequest req = new AuthCodeRequest();
                req.setMemberId(enterpriseMember.getMemberId());
                req.setMobile(mobile);
                req.setBizId(enterpriseMember.getMemberId());
                req.setBizType(BizType.REFIND_LOGIN_SMS.getCode());
                String ticket = defaultUesService.encryptData(mobile);
                req.setMobileTicket(ticket);
                req.setValidity(CommonConstant.VALIDITY);
                //发送短信
                if (defaultSmsService.sendMessage(req, env)) {
                    restP.setSuccess(true);
                } else {
                    restP.setSuccess(false);
                }
            }else{
                restP.setSuccess(false);
            }
            return restP;
        } catch (Exception e) {
            log.error("发送短信失败:" + e.getMessage(), e.getCause());
            restP.setSuccess(false);
            return restP;
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

		if (!CommonConstant.REFRESH_TOKEN.equals(request.getSession().getAttribute(CommonConstant.REFRESH_TOKEN))) {
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
		String activeUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/my/refind/loginPwd/ent/checkRefindEmail.htm?token=" + token;
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
		request.getSession().removeAttribute(CommonConstant.REFRESH_TOKEN);
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
        payPassWordCacheService.clear(token);
        RestResponse restP = new RestResponse();
        restP.setData(initOcx());
        return new ModelAndView(CommonConstant.URL_PREFIX + "/loginPwd/refind/ent/goReSetLoginPwd","response",restP);
    }

}
