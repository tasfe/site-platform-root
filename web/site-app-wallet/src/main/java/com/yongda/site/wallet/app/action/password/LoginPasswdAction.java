package com.yongda.site.wallet.app.action.password;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.MemberAppService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.PayMPasswordService;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.yongda.site.wallet.app.WebDynamicResource;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.util.PayPasswordUtil;
import com.yongda.site.wallet.app.util.ResponseUtil;
import com.yongda.site.wallet.app.util.UnifyLoginUtil;
import com.yongda.site.wallet.app.util.VerifyRequestParamsUtil;
import com.yongda.site.wallet.app.validator.CommonValidator;
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

/**
 * <br>  设置登录密码和支付密码
 * 作者： zhangweijie <br>
 * 日期： 2016/10/27-15:32<br>
 */
@Controller
public class LoginPasswdAction extends BaseAction {
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Resource(name = "defaultLoginPasswdService")
    private DefaultLoginPasswdService defaultLoginPasswdService;

    @Resource(name = "commonValidator")
    protected CommonValidator commonValidator;

    @Resource(name = "memberService")
    private MemberService memberService;

    @Resource(name = "memberAppService")
    private MemberAppService memberAppService;

    @Resource(name = "PayMPasswordService")
    private PayMPasswordService payPasswordService;

    @Resource(name = "cachedSessionService")
    private IVfSsoSession<SsoUser> userSessionService;

    @Resource(name = "webResource")
    private WebDynamicResource webResource;

    @Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;

    @Resource(name = "defaultCertService")
    private DefaultCertService defaultCertService;

    @Resource(name = "memberService")
    private MemberService commMemberService;

    private static final String MEMBER_ID_ENTITY = "MOBILE";

    @Resource(name = "loginFacadeService")
    private LoginFacadeService loginFacadeService;

    /**
     * 注册   设置支付密码和登录密码
     *
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "/set/loginPwdAndPayPwd")
    @ResponseBody
    public RestResponse setPwd(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            String login_Password          = request.getParameter("login_Password");
            String affirm_Login_Password   = request.getParameter("affirm_Login_Password");
            String pay_Password            = request.getParameter("pay_Password");
            String affirm_Pay_Password     = request.getParameter("affirm_Pay_Password");
            String login_Name              = request.getParameter("login_Name");
            restP = VerifyRequestParamsUtil.verify_Pay_Login_PwdLogin(restP,login_Password,affirm_Login_Password,pay_Password
            ,affirm_Pay_Password,login_Name);
            if (!restP.isSuccess()) {
                return restP;
            }
            PersonMember person = new PersonMember();
            person.setLoginName(login_Name);
           // person = memberService.queryMemberIntegratedInfo(person, env);
            person = memberAppService.queryAPPMemberIntegratedInfo(person, env);
            //@1  设置登录密码
            restP = PayPasswordUtil.setLoginPwd(login_Password, defaultLoginPasswdService, person.getOperatorId());
            if (!restP.isSuccess()) {//设置登录密码失败
                return restP;
            }


            //@2 设置支付密码
            restP = PayPasswordUtil.setPayPwd(pay_Password, person, payPasswordService, env);

            if (!restP.isSuccess()) {//设置支付密码失败
                return restP;
            }

            //统一登录，并设置session
            //restP = unifyLogin(person,restP,login_Password,env,request,response);
            restP = UnifyLoginUtil.unifyLogin(person,restP,login_Password,env,request,response,
                    userSessionService,webResource.getVfssoDomain(),loginFacadeService);
            if (!restP.isSuccess()) {
                return restP;
            }
            restP.setMessage("设置密码,并登陆成功");
        } catch (Exception e) {
            logger.error("", e);
            restP.setMessage("操作失败:" + e.getMessage());
            restP.setSuccess(false);
        }
        return restP;
    }

    /**
     * 校验支付密码
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value="check/paypwd",method= RequestMethod.POST)
    @ResponseBody
    public RestResponse checkPayPassword(HttpServletRequest request,
                                         HttpServletResponse response, OperationEnvironment env) {
        RestResponse restP          = ResponseUtil.buildSuccessResponse();
        logger.info("app端验证支付密码...strat");
        try {
            String pay_Password = request.getParameter("pay_Password");//支付密码
            if (StringUtils.isBlank(pay_Password) || !pay_Password.matches(RegexRule.WALLET_PAY_PWD)) {
                String str = StringUtils.isBlank(pay_Password) == true ? "支付密码不能为空":
                        "请输入6位数字支付密码！";
                logger.error("请求参数pay_Password有误：{}", str);
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
                return restP;
            }
            PersonMember user = getUser(request);
            PayPasswdRequest req = new PayPasswdRequest();
            req.setMemberId(user.getMemberId());
            req.setAccountId(user.getDefaultAccountId());
            req.setPassword(pay_Password);
            req.setOperator(user.getOperatorId());
            PayPasswdCheck payPasswdCheck = payPasswordService.checkPayMPwdToSalt(req);
            logger.info("校验支付密码响应数据：{}", payPasswdCheck.toString());
            if (payPasswdCheck.isSuccess()){
                restP.setMessage("校验支付密码成功");
                return restP;
            }
            restP.setSuccess(false);
            if (payPasswdCheck.isLocked()== true) {
                restP.setMessage("支付密码已被锁定,锁定时间：" + String.valueOf(payPasswdCheck.getRemainNum()) + "分钟。");
                return restP;
            }
            restP.setMessage("支付密码错误");
        } catch (Exception e) {
            logger.error("校验支付密码出错：{}",e);
            restP.setSuccess(false);
            restP.setMessage("密码校验失败:{}"+e.getMessage());
        }
        return restP;
    }
}
