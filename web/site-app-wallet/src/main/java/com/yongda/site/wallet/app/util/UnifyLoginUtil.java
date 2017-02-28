package com.yongda.site.wallet.app.util;

import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.ext.integration.unionma.LoginFacadeService;
import com.netfinworks.vfsso.cas.sevlet.VfSsoCookie;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.netfinworks.vfsso.domain.SsoUser;
import com.netfinworks.vfsso.session.IVfSsoSession;
import com.netfinworks.vfsso.session.enums.SessionStatusKind;
import com.netfinworks.vfsso.session.exceptions.SessionException;
import com.yongda.site.wallet.app.common.util.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**统一登录
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/12/19-15:22<br>
 */
public class UnifyLoginUtil {

    private static Logger logger = LoggerFactory.getLogger(UnifyLoginUtil.class);

    private static final String MEMBER_ID_ENTITY = "MOBILE";
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
    public static RestResponse unifyLogin(PersonMember person, RestResponse restP, String login_Password,
                                   OperationEnvironment env, HttpServletRequest request,
                                   HttpServletResponse response,IVfSsoSession<SsoUser> userSessionService,String vfssoDomain,
                                   LoginFacadeService loginFacadeService){
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
                restP.setMessage(e.getMessage());
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
            String token = saveToken(person,request,response,restP,
                    CommonConstant.USERTYPE_PERSON,userSessionService,vfssoDomain);
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

    /*
    * 保存SsoUser并将其对应的token保存至cookie(单点登录用)
    */
    public static String saveToken(BaseMember member, HttpServletRequest request,HttpServletResponse response,
                            RestResponse restP, String userType,IVfSsoSession<SsoUser> userSessionService,String vfssoDomain) {
       // String vfssoDomain = webResource.getVfssoDomain();
        String token = StringUtils.EMPTY;
        try {
            if ((null != member) && (null != member.getMemberId())) {
                clearLoginInfo(request, response,vfssoDomain,userSessionService);
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

    private static void clearLoginInfo(HttpServletRequest req, HttpServletResponse resp,String vfssoDomain,
                                IVfSsoSession<SsoUser> userSessionService) {
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
