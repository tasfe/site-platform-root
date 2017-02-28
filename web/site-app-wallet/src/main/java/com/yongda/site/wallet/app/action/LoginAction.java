package com.yongda.site.wallet.app.action;

import com.kjt.unionma.api.common.model.PlatformInfo;
import com.kjt.unionma.api.register.request.RegisterRequestParam;
import com.kjt.unionma.api.register.response.RegisterResponse;
import com.kjt.unionma.api.register.service.RegisterFacadeWS;
import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.enums.*;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.*;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.site.ext.integration.unionma.convert.UnionmaConvert;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.vfsso.session.enums.SessionStatusKind;
import com.netfinworks.vfsso.session.exceptions.SessionException;
import com.yongda.site.wallet.app.WebDynamicResource;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.common.util.LogUtil;
import com.yongda.site.wallet.app.util.PayPasswordUtil;
import com.yongda.site.wallet.app.util.ResponseUtil;
import com.yongda.site.wallet.app.util.VerifyRequestParamsUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * @author zhangweijie
 *         <p>
 *         2016年10月27日下午12:26:26
 *         <p>
 *         永达钱包 登录
 */
@Controller
@RequestMapping(value = "/wallet")
public class LoginAction extends BaseAction {

    @Resource(name = "memberService")
    private MemberService memberService;

    @Resource(name = "memberAppService")
    private MemberAppService memberAppService;

    @Autowired
    private RegisterFacadeWS registerFacadeWS;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    @Resource(name = "webResource")
    private WebDynamicResource webResource;

    @Resource(name = "defaultLoginPasswdService")
    private DefaultLoginPasswdService defaultLoginPasswdService;

    @Resource(name = "cachedSessionService")
    private IVfSsoSession<SsoUser> userSessionService;

    @Resource(name = "memberService")
    private MemberService commMemberService;

    @Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;

    @Resource(name = "defaultCertService")
    private DefaultCertService defaultCertService;

    @Resource(name = "loginFacadeService")
    private LoginFacadeService loginFacadeService;

    @Resource(name = "authVerifyService")
    private AuthVerifyService authVerifyService;

    @Resource(name = "loginPasswdService")
    private LoginPasswdService loginPasswdService;

    @Resource(name = "defaultUesService")
    private DefaultUesService defaultUesService;

    @Resource(name = "transferService")
    private TransferService transferService;

    @Resource(name = "PayMPasswordService")
    private PayMPasswordService payPasswordService;

    /*@Resource(name = "payPasswordService")
    private PayPasswordService payPasswdService;*/

    protected Logger log = LoggerFactory.getLogger(getClass());

    private static final String MEMBER_ID_ENTITY = "MOBILE";

    private static final String LOGIN_TYPE = "0";//登录标志

    private static final String REGISTER_TYPE = "1";//注册

    /**
     * 注册
     *
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "register", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse registerActive(HttpServletRequest request,
                                       HttpServletResponse response, OperationEnvironment env) {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            String login_Name = request.getParameter("login_Name");//登录账户  手机号
            String verifyCode = request.getParameter("verify_Code");//短信验证码
            logger.info("请求参数：登录账号：{},验证码：{}", login_Name, verifyCode);
            restP = VerifyRequestParamsUtil.verifyRegisterParams(login_Name,verifyCode,restP);
            if (!restP.isSuccess())
                return restP;

            /** 校验用户是否存在 *********************/
            if (!checkLoginUserName(login_Name, restP, REGISTER_TYPE)) {
                log.info("用户已存在....");
                return restP;
            }

            /**
             * 校验手机验证码 memberIdentity 会员身份 mobile loginName 登录名 otpValue 验证码
             */
            Boolean flag = transferService.validateOtpValue(MEMBER_ID_ENTITY, login_Name,
                    login_Name, login_Name, BizType.REGISTER_MOBILE, verifyCode.trim(), env);

            if (!flag) {
                logger.error("您输入的验证码有误，请重新输入");
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "您输入的验证码有误，请重新输入");
            }

            restP = registerActive(login_Name, env);
            if (!restP.isSuccess()) {
                return restP;
            }
        } catch (Exception e) {
            logger.error("", e);
            restP.setMessage("操作失败" + e.getMessage());
        }
        return restP;
    }

    /**
     * 登录方式一  账号，密码登录（未设置支付密码的先设置支付密码）
     *
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse login(HttpServletRequest request,
                              HttpServletResponse response, OperationEnvironment env) {
        RestResponse restP    = ResponseUtil.buildSuccessResponse();
        try {
            String login_Name     = request.getParameter("login_Name");//登录名
            String login_Password = request.getParameter("login_Password");//登录密码
            log.info("登录请求参数  login_Name-{},login_Password-{}", login_Name, login_Password);
            if (StringUtils.isBlank(login_Name) || !login_Name.matches(RegexRule.MOBLIE)) {
                String str = StringUtils.isBlank(login_Name) == true ? "用户名不能为空" : "没有这样的手机号哦！";
                logger.error("请求参数login_Name有误：{}", str);
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            }

            if (StringUtils.isBlank(login_Password)/* || !login_Password.matches(RegexRule.WALLET_LOGIN_PWD)*/) {
                String str = StringUtils.isBlank(login_Password) == true ? "登录密码不能为空" : "登录密码格式错误";
                logger.error("请求参数login_Password有误：{}", str);
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            }

            //检查用户名是否存在
            restP = transferService.checkLoginUserName(login_Name, restP, LOGIN_TYPE);
            if (!restP.isSuccess()) {
                logger.info("登录有误：{}", restP.getMessage());
                return restP;
            }

            /**云+数据检测*****/
            restP = verifyYunOrWxData(login_Name,env,restP);
            if (!restP.isSuccess())
                return restP;

            PersonMember person = new PersonMember();
            person.setLoginName(login_Name);
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            /**判断是否设置支付密码**/
            restP = PayPasswordUtil.check_Pwd_Status(person,restP,env,loginPasswdService);
            if(!restP.isSuccess()){
                if(restP.getCode().equals(CommonDefinedException.NOT_SET_PAY_LOGIN_PASSWORD.getErrorCode())){//未设置登录密码和支付密码直接返回
                    restP.setSuccess(true);
                    return restP;
                }

                if (restP.getCode().equals(CommonDefinedException.NOT_SET_LOGIN_PASSWORD.getErrorCode())){//未设置登录密码
                    restP.setMessage("未设置登录密码,请通过短信方式登录！");
                    restP.setSuccess(true);
                    return restP;
                }

                //支付密码为设置的  先登录再调支付密码页面
                if(person.getPaypasswdstatus().equals(MemberPaypasswdStatus.NOT_SET_PAYPASSWD)) {
                    restP = unifyLogin(person,restP,login_Password,env,request,response);
                    if (!restP.isSuccess()) {
                        return restP;
                    }
                    restP.setSuccess(true);
                    restP.setCode(CommonDefinedException.NOT_SET_PAYPASSWORD.getErrorCode());
                    restP.setMessage(CommonDefinedException.NOT_SET_PAYPASSWORD.getMessage());
                    return restP;
                }
            }

            //统一登录，并设置session
            restP = unifyLogin(person,restP,login_Password,env,request,response);
            if (!restP.isSuccess()) {
                return restP;
            }
            restP.setMessage("永达钱包登录成功");
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (MemberNotExistException e) {
            e.printStackTrace();
        } catch (BizException e) {
            e.printStackTrace();
        }
        return restP;
    }

    /**登录方式一  设置完支付密码后登录****/
    @RequestMapping(value="setPayPwd",method=RequestMethod.POST)
    @ResponseBody
    public RestResponse setPayPassword(HttpServletRequest request,
                                       HttpServletResponse response, OperationEnvironment env){
        RestResponse restP          = ResponseUtil.buildSuccessResponse();
        try {
            String pay_Password         = request.getParameter("pay_Password");//支付密码
            String affirm_Pay_Password  = request.getParameter("affirm_Pay_Password");//确认支付密码
            String login_Name           = request.getParameter("login_Name");
            log.info("设置app端支付密码requst参数：支付密码:{},确认支付密码：{},登录账号：{}",pay_Password,affirm_Pay_Password,login_Name);
            restP = VerifyRequestParamsUtil.verifySetPayPwdLogin(restP,pay_Password,affirm_Pay_Password,login_Name);
            if(!restP.isSuccess()){
                return restP;
            }

            PersonMember person = new PersonMember();
            person.setLoginName(login_Name);
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            //设置支付密码成功
            restP  = PayPasswordUtil.setPayPwd(pay_Password, person, payPasswordService, env);
            if(!restP.isSuccess()){//设置支付密码失败
                return restP;
            }
            restP.setMessage("设置支付密码成功");
        } catch (Exception e) {
            log.error("操作失败",e);
            restP.setMessage("操作失败:" + e.getMessage());
        }
        return restP;
    }

    /**
     * 登录方式二 通过账户，短信进行登录
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value="sms_login",method = RequestMethod.POST)
    @ResponseBody
    public  RestResponse smsLogin(HttpServletRequest request,
                                  HttpServletResponse response, OperationEnvironment env){
        RestResponse restP          = ResponseUtil.buildSuccessResponse();
        try {
            String login_Name = request.getParameter("login_Name");
            String verifyCode = request.getParameter("verify_Code");//短信验证码
            log.info("账号，短信登录请求参数：login_Name{},verifyCode:{}",login_Name,verifyCode);
            restP = VerifyRequestParamsUtil.verifyRegisterParams(login_Name,verifyCode,restP);
            if (!restP.isSuccess())
                return restP;

            /** 校验用户是否存在 *********************/
            if (!checkLoginUserName(login_Name, restP, LOGIN_TYPE)) {
                log.info("用户不存在....");
                return restP;
            }

            /**
             * 校验手机验证码 memberIdentity 会员身份 mobile loginName 登录名 otpValue 验证码
             */
            Boolean flag = transferService.validateOtpValue(MEMBER_ID_ENTITY, login_Name,
                    login_Name, login_Name, BizType.LOGIN_SMS, verifyCode.trim(), env);

            if (!flag) {
                logger.error("您输入的验证码有误，请重新输入");
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "您输入的验证码有误，请重新输入");
            }

            /**云+数据检测*****/
            restP = verifyYunOrWxData(login_Name,env,restP);
            if (!restP.isSuccess())
                return restP;


            PersonMember person = new PersonMember();
            person.setLoginName(login_Name);
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            logger.info("当前用户信息：{},是否设置支付密码:{}",person.toString(),person.getPaypasswdstatus());
            /**判断是否设置支付密码**/
            restP = PayPasswordUtil.check_Pwd_Status(person,restP,env,loginPasswdService);
            if(!restP.isSuccess()){
                if(restP.getCode().equals(CommonDefinedException.NOT_SET_PAY_LOGIN_PASSWORD.getErrorCode())){//未设置登录密码和支付密码直接返回
                    restP.setSuccess(true);
                    return restP;
                }

                if (restP.getCode().equals(CommonDefinedException.NOT_SET_LOGIN_PASSWORD.getErrorCode())){//未设置登录密码
                    restP.setSuccess(true);
                    restP.setMessage("未设置登录密码,请重新设置登录密码和支付密码");
                    return restP;
                }

                //支付密码为设置的  先登录再调支付密码页面
                if(person.getPaypasswdstatus().equals(MemberPaypasswdStatus.NOT_SET_PAYPASSWD)) {
                    String token = this.saveToken(person,request,response,restP,
                            CommonConstant.USERTYPE_PERSON);
                    if (logger.isInfoEnabled()) {
                        logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
                                person, env));
                    }
                    Map result = new HashMap();
                    result.put("token",token);
                    restP.setData(result);
                    restP.setSuccess(true);
                    restP.setCode(CommonDefinedException.NOT_SET_PAYPASSWORD.getErrorCode());
                    restP.setMessage(CommonDefinedException.NOT_SET_PAYPASSWORD.getMessage());
                    return restP;
                }
            }

            // 保存登录信息到session
            String token = this.saveToken(person,request,response,restP,
                    CommonConstant.USERTYPE_PERSON);
            if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
                        person, env));
            }
            restP.getData().put("token",token);
        } catch (Exception e) {
           if(e instanceof MemberNotExistException){
               log.error("",e);
               restP.setMessage("操作失败:" + e.getMessage());
           }else {
               log.error("", e);
               restP.setMessage("操作失败:" + e.getMessage());
           }
        }
        return restP;
    }

    /**
     * 刷新token
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "/refresh_token", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public RestResponse refreshToken(HttpServletRequest request,
                                HttpServletResponse response, OperationEnvironment env){
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            String memberId = request.getParameter("memberId");
            log.info("刷新token...start,请求参数memberId-{}",memberId);
            if (StringUtils.isBlank(memberId)){
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.NOT_LOGIN,CommonDefinedException.NOT_LOGIN.getMessage());
                return restP;
            }
            BaseMember person = new BaseMember();
            person = memberService.queryMemberById(memberId,env);
            if (person==null){
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"会员号不存在");
                return restP;
            }
            // 保存登录信息到session
            String token = this.saveToken(person,request,response,restP,
                    CommonConstant.USERTYPE_PERSON);
            if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
                        person, env));
            }
            restP.getData().put("token",token);
        } catch (BizException e) {
            log.error("根据会员Id查询用户信息失败",e);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR,e.getMessage());
        } catch (MemberNotExistException e) {
            log.error("",e);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR,e.getMessage());
        }
        return restP;
    }
    /**
     * 登出
     * @param request
     * @param response
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/logout", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public RestResponse logOut(HttpServletRequest request,
                                   HttpServletResponse response, OperationEnvironment env)
            throws Exception {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        this.clearLoginInfo(request, response,webResource.getVfssoDomain());
        return restP;
    }


    /**
     * 登出
     * @param request
     * @param response
     * @param env
     * @return
     * @throws Exception
     */
        @RequestMapping(value = "/getDataByTicket", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public RestResponse getDataByTicket(HttpServletRequest request,
                               HttpServletResponse response, OperationEnvironment env,String requestStr)
            throws Exception {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        restP.getData().put("data",super.getDataByTicket(requestStr));
        return restP;
    }


    /**
     * 统一登录，保存seesion
     * @param person   用户信息
     * @param restP    响应实体类
     * @param login_Password 登录密码
     * @param env
     * @param request
     * @param response
     * @return
     */
    public  RestResponse unifyLogin(PersonMember person,RestResponse restP,String login_Password,
                                    OperationEnvironment env,HttpServletRequest request,
                                    HttpServletResponse response){
        try {
            /**统一登录*****/
            LoginRequest loginReq=new LoginRequest();
            loginReq.setLoginName(person.getLoginName());
            loginReq.setLoginPassowrd(login_Password);
            loginReq.setLoginType(MEMBER_ID_ENTITY);
            LoginNameEditResp loginResp=new LoginNameEditResp();
            try {
                loginResp=loginFacadeService.login(loginReq);
                logger.info("统一账户登录响应信息：{}",loginResp.toString());
                if(!SysResponseCode.SUCCESS.getCode().equals(loginResp.getResponseCode())){
                    restP.setMessage(loginResp.getResponseMessage());
                    if (SysResponseCode.LOGIN_PWD_NOT_SET.getCode().equals(loginResp.getResponseCode())) {
                        restP.setMessage("账户未设置登录密码,请通过短信验证码登录");
                    }
                    restP.setSuccess(false);
                    restP.setCode(CommonDefinedException.PASSWORD_ERROR.getErrorCode());
                    return restP;
                }
            }catch(BizException e){
                logger.error("", e);
                restP.setMessage(getErrMsg(e.getMessage()));
                restP.setSuccess(false);
                return restP;
            }

            if (MemberLockStatus.LOCKED.name().equals(person.getLockStatus().name())) {
                if (logger.isInfoEnabled()) {
                    logger.info(LogUtil.appLog(OperateeTypeEnum.LOCKEDLOGINPASS.getMsg(), person, env));
                }
                restP.setMessage("会员已被锁定，请联系客服！");
                restP.setSuccess(false);
                return restP;
            }

            // 保存登录信息到session
           String token = this.saveToken(person,request,response,restP,
                    CommonConstant.USERTYPE_PERSON);
            if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.LOGIN.getMsg(),
                        person, env));
            }
            Map resultMap = new HashMap();
            resultMap.put("token",token);
            restP.setData(resultMap);
        }catch (Exception e){
            if(e instanceof BizException){
                logger.error("", e);
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, e.getMessage());
            }else
                logger.error("", e);
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, e.getMessage());
        }
        return restP;
    }



    public RestResponse registerActive(String loginName, OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        String memberId = null;
        try {
            String personIdentiy      = MemberType.PERSONAL.getCode(); // 人员身份 1.个人会员 2.企业会员
            PlatformInfo platformInfo = UnionmaConvert.createKjtPlatformInfo();
            RegisterRequestParam requestParam = new RegisterRequestParam();
            String registerType = MEMBER_ID_ENTITY.toUpperCase(); // 注册类别
            /**
             * 设置注册参数
             */
            requestParam.setPlatformInfo(platformInfo);

            requestParam.setRegisterType(registerType);
            requestParam.setLoginName(loginName);
            requestParam.setPersonIdentiy(personIdentiy);
            requestParam.setExtention(StringUtils.EMPTY);
            logger.info("requestParam : {},loginName: {}", requestParam,
                    loginName);
            RegisterResponse registerResponse = registerFacadeWS
                    .register(requestParam);
            logger.info("registerResponse : {}", registerResponse);
            memberId = registerResponse.getMemberId();
            String rstMsg = "注册成功";
            /**
             * 注册失败
             */
            if (!registerResponse.getIsSuccess()) {
                String code = registerResponse.getResponseCode();
                rstMsg = registerResponse.getResponseMessage();

                if (SysResponseCode.ILLEGAL_ARGUMENT.getCode().equals(code)) {
                    logger.error("注册失败：{}" + rstMsg);
                    return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, rstMsg);
                } else if (SysResponseCode.SYSTEM_ERROR.getCode().equals(
                        code)) {
                    logger.error("注册失败：{}" + rstMsg);
                    return ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR, rstMsg);
                }
            }
            if (StringUtils.isNotBlank(memberId)) {
                String token = UUID.randomUUID().toString().replace("-", "");
                String keyStr = "register_" + token + "_" + env.getClientIp();
                logger.info("注册成功registerActive放入缓存->key={}, memberId={}",
                        keyStr, memberId);
                payPassWordCacheService.put(keyStr, memberId);
            }
            restP.setMessage(rstMsg);
            restP.setSuccess(true);
        } catch (Exception e) {
            logger.error("", e);
            restP.setMessage("操作失败:"+e.getMessage());
        }
        return restP;
    }

    /**
     * @param username
     * @param restP
     * @param type     0登陆，1注册
     * @return
     */
    public Boolean checkLoginUserName(String username, RestResponse restP,
                                      String type) {
        OperationEnvironment env = new OperationEnvironment();
        PersonMember person = new PersonMember();
        person.setLoginName(username);
        try {
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            if ("1".equals(type) && !StringUtils.isBlank(person.getMemberId())) {
                restP.setMessage("该手机号已注册");
                restP.setCode(CommonDefinedException.ACCOUNT_EXIST_ERROR
                        .getErrorCode());
                restP.setSuccess(false);
                logger.info("该手机号已注册，用户信息：{}" + person.toString());
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
            if ("0".equals(type)) {
                restP.setMessage("该账号不存在，请重新输入");
                logger.info("该账号不存在，请重新输入");
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
     * 云+数据处理
     *
     * @param loginName
     * @param env
     * @param restP
     * @return
     */
    private RestResponse verifyYunOrWxData(String loginName/*, String invitCode*/, OperationEnvironment env, RestResponse restP) {
        PersonMember person = new PersonMember();
        person.setLoginName(loginName);
        try {
            log.info("检测是否为云+数据");
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            if (person == null) {
                restP.setSuccess(true);
                return restP;
            }
            //注册会员是否已激活    针对云+用户
            boolean isActiveMember = person.getStatus() == MemberStatus.NOT_ACTIVE ? false : true;
            if (!isActiveMember) {
                //云+数据
                log.info("云+数据激活");
                //进行会员激活
                PersonMember per = new PersonMember();
                per.setMemberId(person.getMemberId());
               // per.setInvitCode(invitCode);
                try {
                    memberService.activatePersonalMemberInfo(per, env);
                } catch (BizException e) {
                    log.error("激活会员失败：{}");
                }

                //新增一条认证信息
                AuthVerifyInfo info = new AuthVerifyInfo();
                info.setMemberId(person.getMemberId());
                info.setVerifyType(VerifyType.CELL_PHONE.getCode());
                info.setVerifyEntity(person.getLoginName());
                info.setStatus(VerifyStatus.YES.getCode());
                logger.error("认证请求：{}", info.toString());
                ResponseCode verifyResult = authVerifyService.createVerify(info, env);

                if (ResponseCode.SUCCESS == verifyResult) {
                    logger.error("绑定手机成功");
                } else if (ResponseCode.DUPLICATE_VERIFY == verifyResult) {
                    logger.error("认证信息重复");
                } else {
                    logger.error("绑定手机失败");
                }
                restP.setSuccess(true);
                return restP;
            }
            restP.setSuccess(true);
        }catch (Exception e) {
            restP.setSuccess(false);
            restP.setMessage("系统错误"+e);
            restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
        }
        return restP;
    }



    /*
      * 保存SsoUser并将其对应的token保存至cookie(单点登录用)
      */
    public String saveToken(BaseMember member, HttpServletRequest request,
                            HttpServletResponse response, RestResponse restP, String userType) {
        String vfssoDomain = webResource.getVfssoDomain();
        String token = StringUtils.EMPTY;
        try {
            if ((null != member) && (null != member.getMemberId())) {
                clearLoginInfo(request, response,vfssoDomain);
                SsoUser ssoUser = new SsoUser();
                ssoUser.setId(member.getMemberId());
                logger.info("LoginName: " + member.getLoginName());
                ssoUser.setLoginName(member.getLoginName());
                ssoUser.setName(member.getMemberName());
                ssoUser.setOpId(member.getOperatorId());
                ssoUser.setUserType(userType);
                token = userSessionService.create(ssoUser);
                logger.info("token: " + token);
                if (token != null) {
                    VfSsoCookie.setCookie(token, response);
                    // 为了使cookie能在多个系统中共享，需要给cookie设置多个系统共通的域名
                    logger.info("vfssoDomain: " + vfssoDomain);
                    if (StringUtils.isNotBlank(vfssoDomain)) {
                        for (String domain : vfssoDomain
                                .split(",")) {
                            VfSsoCookie.setCookie(token, response, domain, "/");
                        }
                    }
                    if (SessionStatusKind.pending.equals(ssoUser
                            .getSessionStatus())) {
                        // 强制登录，踢掉上一个登录的人
                        userSessionService.forceIn(token, ssoUser);
                    }
                    restP.setSuccess(true);
                    restP.setMessage("登录成功");
                } else {
                    logger.error("登录失败！");
                    restP.setMessage("登录失败");
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            //restP.setMessage(getErrMsg(e.getMessage()));
        }
        return token;

    }

    private void clearLoginInfo(HttpServletRequest req, HttpServletResponse resp,String vfssoDomain) {
        HttpSession session = req.getSession();
        session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER);
        session.invalidate();
        try {
            VfSsoCookie.removeCookie(resp);
            VfSsoCookie.removeCookie(resp, vfssoDomain);
            if (StringUtils.isNotBlank(vfssoDomain)) {
                for (String domain : vfssoDomain.split(",")) {
                    VfSsoCookie.removeCookie(resp, domain);
                }
            }
            if (VfSsoUser.getCurrentToken() != null) {
                userSessionService.remove(VfSsoUser.getCurrentToken());
            }
        } catch (SessionException e) {
            logger.error("单点登录注销用户信息失败", e);
        }
    }
}
