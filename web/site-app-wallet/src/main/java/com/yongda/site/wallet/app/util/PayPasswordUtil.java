package com.yongda.site.wallet.app.util;

import com.kjt.unionma.api.common.enumes.PlatformSystemTypeEnum;
import com.kjt.unionma.api.common.enumes.PlatformTypeEnum;
import com.kjt.unionma.core.common.utils.MD5Util;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.PlatformInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberAppService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.PayMPasswordService;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/10/28-14:33<br>
 */
public class PayPasswordUtil {
    private static Logger logger = LoggerFactory.getLogger("PayPasswordUtil");
    /**
     * 设置登录密码
     * @param loginPassword
     * @param operatorId
     * @param restP
     * @return
     */
    public static RestResponse setLoginPwd(String loginPassword, DefaultLoginPasswdService defaultLoginPasswdService,String operatorId) {
        RestResponse restP = new RestResponse();
        LoginPasswdRequest req = new LoginPasswdRequest();
        req.setOperatorId(operatorId);
        req.setValidateType(1);
        req.setPassword(MD5Util.MD5(loginPassword));
        CommResponse commRep = null;
        try {
            logger.info("设置app端登录密码request参数：{}",req.toString());
            commRep = defaultLoginPasswdService.setLoginPassword(req);
            if (commRep.isSuccess()) {
                logger.info("设置登录密码成功");
                restP.setSuccess(true);
            } else {
                logger.info("设置登录密码失败:"+commRep.getResponseMessage());
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorCode(),
                        "设置登录密码失败:"+commRep.getResponseMessage());
                return restP;
            }
        } catch (ServiceException e) {
            logger.info("设置登录密码失败:"+e);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR.getErrorCode(),
                    "设置登录密码失败:"+e.getMessage());
            return restP;
        }
        return restP;
    }

    /**
     * 设置支付密码
     * @param payPassword  支付密码
     * @param env
     * @return
     */
    public static RestResponse setPayPwd(String payPassword, BaseMember user,
                                         PayMPasswordService payPasswordService, OperationEnvironment env) {
        RestResponse restP = new RestResponse();
        try{
            PayPasswdRequest req = new  PayPasswdRequest();
            req.setMemberId(user.getMemberId());
            req.setAccountId(user.getDefaultAccountId());
            req.setPassword(payPassword);
            req.setOperator(user.getOperatorId());
            logger.info("设置app端支付密码requst参数：{}",req.toString());

            CommResponse reomotResp = payPasswordService.setPayMPassword(req);
            if(!reomotResp.isSuccess()){
                restP = ResponseUtil.buildExpResponse(reomotResp.getResponseCode(), reomotResp.getResponseMessage());
                return restP;
            }

            restP.setSuccess(true);
            restP.setMessage("设置密码成功");
            logger.info("设置支付密码成功");
            user.setPaypasswdstatus(MemberPaypasswdStatus.DEFAULT);
        } catch (Exception e) {
            logger.error("支付密码：" + e.getMessage());
            restP.setCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
            restP.setMessage(e.getMessage());
            restP.setSuccess(false);
        }
        return restP;
    }

    /**
     * 检查是否设置了登录密码，支付密码
     * @param
     * @param restP
     * @param env
     * @param loginPasswdService
     * @return
     */
    public static RestResponse check_Pwd_Status(PersonMember person, RestResponse restP, OperationEnvironment env,
                                          LoginPasswdService loginPasswdService){
        try {
            LoginPasswdRequest loginPasswdRequest=new LoginPasswdRequest();
            loginPasswdRequest.setValidateMode(1);
            loginPasswdRequest.setOperatorId(person.getOperatorId());
            Boolean pwdFalg = false;
            pwdFalg = loginPasswdService.validateLoginPwdIsNull(loginPasswdRequest);

            /**判断是否设置支付密码**/
            if(person.getPaypasswdstatus().equals(MemberPaypasswdStatus.NOT_SET_PAYPASSWD) && pwdFalg){
                logger.error("未设置支付密码和登录密码");
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.NOT_SET_PAY_LOGIN_PASSWORD, CommonDefinedException.NOT_SET_PAY_LOGIN_PASSWORD.getErrorMsg());
                return restP;
            }else if(person.getPaypasswdstatus().equals(MemberPaypasswdStatus.NOT_SET_PAYPASSWD)) {
                logger.error("未设置支付密码");
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.NOT_SET_PAYPASSWORD, CommonDefinedException.NOT_SET_PAYPASSWORD.getErrorMsg());
                return restP;
            }else if(pwdFalg){
                logger.error("未设置登录密码");
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.NOT_SET_LOGIN_PASSWORD, CommonDefinedException.NOT_SET_LOGIN_PASSWORD.getErrorMsg());
                return restP;
            }
        } catch (Exception e) {
            logger.error("操作失败：" + e.getMessage());
        }
        return restP;
    }


    /*public static PlatformInfo createDefaultPlatform(MemberType type){
        PlatformInfo platformInfo = new PlatformInfo();
        if(type == MemberType.PERSONAL)
            platformInfo.setPlatformSystemType(PlatformSystemTypeEnum.KJT_PERSONAL.getCode());
        else
            platformInfo.setPlatformSystemType(PlatformSystemTypeEnum.KJT_ENTERPRISE.getCode());
        platformInfo.setTerminal("PC");
        platformInfo.setPlatformType(PlatformTypeEnum.DEFAULT.getCode());
        return platformInfo;
    }*/
}
