package com.yongda.site.wallet.app.action.password;


import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.enums.*;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberAppService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.PayMPasswordService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.yongda.site.wallet.app.WebDynamicResource;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.util.PayPasswordUtil;
import com.yongda.site.wallet.app.util.ResponseUtil;
import com.yongda.site.wallet.app.util.UnifyLoginUtil;
import com.yongda.site.wallet.app.util.VerifyRequestParamsUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 密码相关处理控制器
 *
 * @author yp
 */
@RequestMapping(value = "/loginpwd/find")
@Controller
public class RefindLoginPwdAction extends BaseAction {

    private static Logger log = LoggerFactory.getLogger(RefindLoginPwdAction.class);

    @Resource(name = "defaultUesService")
    private DefaultUesService defaultUesService;

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

    private static final String MEMBER_ID_ENTITY = "mobile";

    @Resource(name = "memberAppService")
    private MemberAppService memberAppService;

    @Resource(name = "PayMPasswordService")
    private PayMPasswordService payPasswordService;

    @Resource(name = "xuCache")
    private XUCache<String> loginCache;

    @Resource(name = "cachedSessionService")
    private IVfSsoSession<SsoUser> userSessionService;

    @Resource(name = "webResource")
    private WebDynamicResource webResource;

    @Resource(name = "loginFacadeService")
    private LoginFacadeService loginFacadeService;

    private static String[] types_str = {"login_Passwd_Sms","pay_Passwd_Sms","RESET_LOGIN_PWD","RESET_PAY_PWD"};

    private static final String  LOGIN_TOKEN = "login_token";

    private static final String  RESET_LOGIN_TOKEN = "reset_login_token";

    private static final String  PAY_TOKEN = "pay_token";
    /**
     * 发送短信验证码
     *
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "sendcode", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse sendMessage(HttpServletRequest request,
                                    HttpServletResponse response, OperationEnvironment env, String login_Name) {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        /**
         *  短信类型
         *  1.login_Passwd_Sms    短信找回登录密码
         *  2.pay_Passwd_Sms      短信找回支付密码
         *  3.RESET_LOGIN_PWD     重设登录密码
         *  4.RESET_PAY_PWD       重设支付密码
         *
         */
        String sms_Type = request.getParameter("sms_Type");
        log.info("找回密码发送短信请求参数：login_Name:{},sms_type:{}", login_Name, sms_Type);
        restP = VerifyRequestParamsUtil.verifyResetPayPwdParams(login_Name, sms_Type, restP);
        if (!restP.isSuccess())
            return restP;


        if (ArrayUtils.indexOf(types_str,sms_Type)<0) {
            logger.error("请求参数有误：{}", "短信类型非法");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "短信类型非法");
            return restP;
        }


        // 验证用户名是否存在
        PersonMember personMember = new PersonMember();
        personMember.setLoginName(login_Name);
        try {
            personMember = memberService.queryMemberIntegratedInfo(personMember, env);
            if (personMember.getMemberType() != MemberType.PERSONAL) {
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.USER_ACCOUNT_NOT_EXIST, "用户名不存在");
                return restP;
            }
            String memberId = personMember.getMemberId();
            EncryptData encryptData = memberService.decipherMember(
                    memberId, DeciphedType.CELL_PHONE,
                    DeciphedQueryFlag.ALL, env);
            // 获取解密手机号码进行验证码校验
            String mobile = encryptData.getPlaintext();
            log.info("用户" + login_Name + "的手机号：" + mobile);
            Boolean flag = false;

            /**找回登录密码****/
            if (sms_Type.equalsIgnoreCase("login_Passwd_Sms")){
                log.info("找回登录密码");
                flag = transferService.sendMobileMessage(memberId,
                        mobile, env,BizType.REFIND_LOGIN_SMS);
            }

            /**找回支付密码****/
            if (sms_Type.equalsIgnoreCase("pay_Passwd_Sms")){
                log.info("找回支付密码");
                flag =transferService.sendMobileMessage(memberId,
                        mobile, env,BizType.REFIND_PAYPASSWD);
            }

            /**重设登录密码****/
            if (sms_Type.equalsIgnoreCase("RESET_LOGIN_PWD")){
                log.info("重设登录密码");
                flag = transferService.sendMobileMessage(memberId,
                        mobile, env,BizType.RESET_LOGIN_PWD);
            }

            /**重设支付密码****/
            if (sms_Type.equalsIgnoreCase("RESET_PAY_PWD")){
                log.info("重设支付密码");
                flag = transferService.sendMobileMessage(memberId,
                        mobile, env,BizType.RESET_PAY_PWD);
            }

            if (!flag){
                log.error("发送短信失败");
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR, "发送短信失败");
                return restP;
            }
            log.info("发送短信成功");
            restP.setMessage("发送短信成功");
            return restP;
        } catch (MemberNotExistException e) {
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.USER_ACCOUNT_NOT_EXIST, "用户名不存在");
        } catch (Exception e) {
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR, e.getMessage());
        }
        return restP;
    }

    /**
     * 校验找回登录密码短信验证码
     *
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "verifySmsCode")
    @ResponseBody
    public RestResponse verifySmsCode(HttpServletRequest request,
                                      HttpServletResponse response, OperationEnvironment env) {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            String login_Name = request.getParameter("login_Name");
            String verify_code = request.getParameter("code");
            /**
             *  短信类型
             *  1.login_Passwd_Sms    短信找回登录密码
             *  2.pay_Passwd_Sms      短信找回支付密码
             *  3.RESET_LOGIN_PWD     重设登录密码
             *  4.RESET_PAY_PWD       重设支付密码
             */
            String sms_Type = request.getParameter("sms_Type");
            logger.info("请求参数：login_Name:{},verify_code:{},sms_type:{}", login_Name, verify_code, sms_Type);
            restP = VerifyRequestParamsUtil.verifyRegisterParams(login_Name, verify_code, restP);
            if (!restP.isSuccess())
                return restP;


            if (StringUtils.isBlank(sms_Type) || ArrayUtils.indexOf(types_str,sms_Type)<0) {
                String str = StringUtils.isBlank(sms_Type) == true?"短信类型不能为空":"短信类型非法";
                logger.error("请求参数有误：{}", str);
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
                return restP;
            }

            // 验证用户名是否存在
            PersonMember personMember = new PersonMember();
            personMember.setLoginName(login_Name);
            personMember = memberService.queryMemberIntegratedInfo(personMember, env);
            if (personMember.getMemberType() != MemberType.PERSONAL) {
                logger.error("用户名不存在");
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.USER_ACCOUNT_NOT_EXIST,
                        CommonDefinedException.USER_ACCOUNT_NOT_EXIST.getErrorMsg());
                return restP;
            }
            //验证短信验证码
            String memberId = personMember.getMemberId();
            EncryptData encryptData = memberService.decipherMember(
                    memberId, DeciphedType.CELL_PHONE,
                    DeciphedQueryFlag.ALL, env);
            //获取解密手机号码进行验证码校验
            String mobile = encryptData.getPlaintext();
            Boolean flag = false;
            String flag_token = null;
            /**找回登录密码****/
            if (sms_Type.equalsIgnoreCase("login_Passwd_Sms")){
                flag_token = LOGIN_TOKEN;
                flag = transferService.validateOtpValue(MEMBER_ID_ENTITY, login_Name, mobile,
                        memberId,BizType.REFIND_LOGIN_SMS, verify_code, env);

            }

            /**找回支付密码****/
            if (sms_Type.equalsIgnoreCase("pay_Passwd_Sms")){
                flag_token = PAY_TOKEN;
                flag = transferService.validateOtpValue(MEMBER_ID_ENTITY, login_Name, mobile,
                        memberId,BizType.REFIND_PAYPASSWD, verify_code, env);
            }

            /**重设登录密码****/
            if (sms_Type.equalsIgnoreCase("RESET_LOGIN_PWD")){
                flag_token = RESET_LOGIN_TOKEN;
                flag = transferService.validateOtpValue(MEMBER_ID_ENTITY, login_Name, mobile,
                        memberId,BizType.RESET_LOGIN_PWD, verify_code, env);
            }

            /**重设支付密码****/
            if (sms_Type.equalsIgnoreCase("RESET_PAY_PWD")){
                flag_token = PAY_TOKEN;
                flag = transferService.validateOtpValue(MEMBER_ID_ENTITY, login_Name, mobile,
                        memberId,BizType.RESET_PAY_PWD, verify_code, env);
            }

            if (!flag) {
                logger.error("该手机号对应的短信验证码有误或失效");
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "您输入的验证码有误，请重新输入");
                return restP;
            }

            String token =flag_token+UUID.randomUUID().toString().replace("-", "");
            loginCache.set(token, memberId, 3600);
            restP.getData().put("token", token);
            restP.setMessage("校验成功");
            logger.info("找回密码--短信校验码验证,token：{}", token);
        } catch (Exception e) {
            logger.error("", e);
            restP.setSuccess(false);
            restP.setMessage("操作失败:" + e.getMessage());
        }
        return restP;
    }

    /**
     * 重置/找回 登录密码
     *
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "reset", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse refindloginpwd(HttpServletRequest request,
                                       HttpServletResponse response, OperationEnvironment env) {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        String login_Name = request.getParameter("login_Name");
        String new_Login_Pwd = request.getParameter("new_Login_Pwd");
        String affirm_Login_Pwd = request.getParameter("affirm_Login_Pwd");
        String token = request.getParameter("token");
        logger.error("重置/找回登录密码请求参数：login_Name：{},token:{}",
                login_Name, token);

        restP = VerifyRequestParamsUtil.verifyResetLoginPwdParams(login_Name, new_Login_Pwd, affirm_Login_Pwd, token, restP);
        if (!restP.isSuccess())
            return restP;

        if (!token.contains(LOGIN_TOKEN)) {
            logger.error("请求参数token有误：{}", "token错误或请求未带token");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.UNAUTHORIZED_ERROR, "token错误或请求未带token");
            return restP;
        }
        // 验证用户名是否存在
        PersonMember personMember = new PersonMember();
        personMember.setLoginName(login_Name);
        try {
            personMember = memberService.queryMemberIntegratedInfo(personMember, env);
            if (personMember.getMemberType() != MemberType.PERSONAL) {
                logger.error("用户名不存在");
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.USER_ACCOUNT_NOT_EXIST,
                        CommonDefinedException.USER_ACCOUNT_NOT_EXIST.getErrorMsg());
                return restP;
            }

            CacheRespone<String> cacherespone = loginCache.get(token);
            if (StringUtils.isBlank(cacherespone.get())) {
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT
                        .getErrorCode(), "token错误或请求未带token");
                return restP;
            }

            //设置登录密码
            LoginPasswdRequest req = new LoginPasswdRequest();
            // 加密
            new_Login_Pwd = MD5Util.MD5(new_Login_Pwd);
            req.setValidateType(2);
            req.setPassword(new_Login_Pwd);
            req.setOperatorId(personMember.getOperatorId());
            // 会员标识
            req.setMemberIdentity(personMember.getMemberIdentity());
            // 会员标识平台类型 1.UID
            req.setPlatformType(personMember.getPlatformType());
            // 重置登陆密码锁
            defaultLoginPasswdService.resetLoginPasswordLock(req);
            // 重置登陆密码
            CommResponse commResponse = defaultLoginPasswdService.setLoginPassword(req);
            logger.info("重设登录密码响应：{}",commResponse.toString());
            String responseCode = commResponse.getResponseCode();
            if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(responseCode)) {
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR,
                        CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getMessage());
                return restP;
            }
            loginCache.delete(token);
            if (!token.contains(RESET_LOGIN_TOKEN)){
                restP = UnifyLoginUtil.unifyLogin(personMember,restP,affirm_Login_Pwd,env,request,response,
                        userSessionService,webResource.getVfssoDomain(),loginFacadeService);
                return restP;
            }
            restP.setSuccess(true);
            restP.setMessage("设置成功");
        } catch (MemberNotExistException e) {
            logger.error("用户名不存在");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.USER_ACCOUNT_NOT_EXIST, CommonDefinedException.USER_ACCOUNT_NOT_EXIST.getErrorMsg());
        } catch (Exception e) {
            logger.error("对不起，服务器繁忙，请稍后再试！"+e);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR, e.getMessage());
        }
        return restP;
    }

    /**
     * 重置/找回 支付密码
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value="reset_pay_pwd",method=RequestMethod.POST)
    @ResponseBody
    public RestResponse setPayPassword(HttpServletRequest request,
                                       HttpServletResponse response, OperationEnvironment env){
        RestResponse restP          = ResponseUtil.buildSuccessResponse();
        try {
            String pay_Password         = request.getParameter("pay_Password");//支付密码
            String affirm_Pay_Password  = request.getParameter("affirm_Pay_Password");//确认支付密码
            String login_Name           = request.getParameter("login_Name");
            String token = request.getParameter("token");
            log.info("重置/找回支付密码登录请求参数：登录账号：{},token:{}",login_Name,token);
            restP = VerifyRequestParamsUtil.verifySetPayPwdLogin(restP,pay_Password,affirm_Pay_Password,login_Name);
            if(!restP.isSuccess()){
                return restP;
            }
            if (StringUtils.isBlank(token) || !token.contains(PAY_TOKEN)) {
                String errMag = StringUtils.isBlank(token)?"token不能为空":"token错误或请求未带token";
                logger.error("请求参数token有误：{}",errMag);
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.UNAUTHORIZED_ERROR, errMag);
                return restP;
            }

            CacheRespone<String> cacherespone = loginCache.get(token);
            if (StringUtils.isBlank(cacherespone.get())) {
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT
                        .getErrorCode(), "token错误或请求未带token");
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
            loginCache.delete(token);
            restP.setMessage("设置支付密码成功");
        } catch (Exception e) {
            log.error("",e);
            restP.setMessage("操作失败:" + e.getMessage());
        }
        return restP;
    }



}
