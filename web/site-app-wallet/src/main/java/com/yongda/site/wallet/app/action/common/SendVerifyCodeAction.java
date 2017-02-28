package com.yongda.site.wallet.app.action.common;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.mns.client.INotifyClient;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberAppService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.regex.Pattern;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/10/27-14:07<br>
 */
@Controller
@RequestMapping(value="/send")
public class SendVerifyCodeAction {
    private static Logger log = LoggerFactory.getLogger(SendVerifyCodeAction.class);

    @Resource(name = "defaultUesService")
    private DefaultUesService defaultUesService;

    @Resource(name = "defaultSmsService")
    private DefaultSmsService defaultSmsService;

    @Resource(name = "memberService")
    private MemberService memberService;

    @Resource(name = "memberAppService")
    private MemberAppService memberAppService;

    @Resource(name = "payPassWordCacheService")
    private PayPassWordCacheService payPassWordCacheService;

    @Resource(name = "uesService")
    private UesServiceClient uesServiceClient;

    @Resource(name = "transferService")
    private TransferService transferService;

    @Resource(name = "loginPasswdService")
    private LoginPasswdService loginPasswdService;

    private static final String REGISTER_UIL = "register";

    private static final String LOGIN_UIL = "LOGIN";

    @Resource(name = "memberService")
    private MemberService  commMemberService;

    private static  String[] types_str = {"register","login"};
    /**
     * 发送短信验证码
     * @param request
     * @return
     * @throws
     * @throws BindException
     */
    @RequestMapping(value = "/mobile_code", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse sendMobileMessage(HttpServletRequest request,
                                          HttpServletResponse response, OperationEnvironment env){
        RestResponse restP = new RestResponse();
        try {
            String mobile = request.getParameter("login_Name");
            String types   = request.getParameter("type");
            log.info("发送短信验证码:mobile:{},types:{}",mobile,types);
            /**类型   登陆（login） 注册（register）*****/
            String request_type = null; /*request.getParameter("type");*/
            // 校验提交参数
            if(!verifyReqParams(mobile,types,restP))
            {
                return restP;
            }

            if (ArrayUtils.indexOf(types_str,types)<0) {
                restP.setSuccess(false);
                restP.setMessage("短信类型错误");
                return restP;
            }

            if (types.equalsIgnoreCase("register")){
                request_type = REGISTER_UIL;
            }else if(types.equalsIgnoreCase("login")){
                //登录模板
                request_type = LOGIN_UIL;
            }

            if(!checkLoginUserName(mobile,request_type,restP))
            {
                return restP;
            }

            // 封装发送短信请求
            AuthCodeRequest req = new AuthCodeRequest();
            req.setMobile(mobile);
            req.setBizId(mobile);
            if(types.equalsIgnoreCase("login")){
                //登录模板
                req.setBizType(BizType.LOGIN_SMS.getCode());
            }else{
                if (isNeedActiveMember(mobile, env)) {
                    // 激活模板
                    req.setBizType(BizType.ACTIVE_MOBILE.getCode());
                } else {
                    // 注册模板
                    req.setBizType(BizType.REGISTER_MOBILE.getCode());
                }
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
                return restP;
            }
            // }
            restP.setMessage("发送短信成功");
        }catch(ServiceException e){
            log.error("发送短信失败:" + e.getMessage(), e.getCause());
            restP.setSuccess(false);
        } catch (Exception e) {
            log.error("发送短信失败:" + e.getMessage(), e.getCause());
            restP.setSuccess(false);
        }
        return restP;
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
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            if (null != person && null != person.getStatus()) {
                if (person.getMemberType() == MemberType.PERSONAL) {
                    isNeedActiveMember = person.getStatus() == MemberStatus.NOT_ACTIVE ? true
                            : false;
                }
            }
        } catch (Exception e) {
            log.info("{}未注册，无需激活", loginName);
        }
        return isNeedActiveMember;
    }


    //校验请求参数
    private Boolean verifyReqParams(String mobile,String types,RestResponse restP){
        if (StringUtils.isBlank(mobile) || !Pattern.matches(RegexRule.MOBLIE, mobile)) {
            restP.setMessage(StringUtils.isBlank(mobile)?"手机号不能为空":"没有这样的手机号哦！");
            restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
                    .getErrorCode());
            restP.setSuccess(false);
            log.error("缺少必要的查询参数！");
            return false;
        }

        if(StringUtils.isBlank(types)) {
            restP.setMessage("短信类型不能为空");
            restP.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
                    .getErrorCode());
            restP.setSuccess(false);
            log.error("参数不能为空！");
            return false;
        }
        return true;
    }

    /**
     *
     * @param restP
     * @param type
     *             登陆（login） 注册（register）
     * @return
     */
    public Boolean checkLoginUserName(String mobile, String type,RestResponse restP) {
        OperationEnvironment env = new OperationEnvironment();
        PersonMember person = new PersonMember();
        person.setLoginName(mobile);
        try {
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            //注册会员是否已激活    针对云+用户
            boolean isActiveMember=person.getStatus()==MemberStatus.NOT_ACTIVE?false:true;

            LoginPasswdRequest loginPasswdRequest=new LoginPasswdRequest();
            loginPasswdRequest.setValidateMode(1);
            loginPasswdRequest.setOperatorId(person.getOperatorId());
            // Boolean falg = loginPasswdService.validateLoginPwdIsNull(loginPasswdRequest);//未设置登录密码
            if ("register".equalsIgnoreCase(type) && !StringUtils.isBlank(person.getMemberId())
                    && isActiveMember) {
                restP.setMessage("该手机号已注册，可以直接登录哦");
                restP.setCode(CommonDefinedException.ACCOUNT_EXIST_ERROR
                        .getErrorCode());
                restP.setSuccess(false);
                log.error("该手机号已注册，可以直接登录哦");
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
                restP.setMessage("该账号不存在，请重新输入");
                log.error("该账号不存在，请重新输入");
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
}
