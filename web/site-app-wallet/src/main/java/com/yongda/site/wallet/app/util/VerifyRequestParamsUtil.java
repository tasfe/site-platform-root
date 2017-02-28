package com.yongda.site.wallet.app.util;

import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <br>  校验请求参数工具类
 * 作者： zhangweijie <br>
 * 日期： 2016/10/28-17:42<br>
 */
public class VerifyRequestParamsUtil {
    private static Logger logger = LoggerFactory.getLogger("VerifyRequestParamsUtil");
    private static String[] TYPE = {"fees","flow"};
    public static RestResponse verifySetPayPwdLogin(RestResponse restP, String pay_Password,
                                                    String affirm_Pay_Password, String login_Name) {
        if (StringUtils.isBlank(pay_Password) || !pay_Password.matches(RegexRule.WALLET_PAY_PWD)) {
            String str = StringUtils.isBlank(pay_Password) == true ? "支付密码不能为空" : "请输入6位数字支付密码！";
            logger.error("请求参数pay_Password有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(affirm_Pay_Password) || !affirm_Pay_Password.matches(RegexRule.WALLET_PAY_PWD)) {
            String str = StringUtils.isBlank(affirm_Pay_Password) == true ? "确认支付密码不能为空" : "请输入6位数字确认支付密码！";
            logger.error("请求参数affirm_Pay_Password有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(login_Name) || !login_Name.matches(RegexRule.MOBLIE)) {
            String str = StringUtils.isBlank(login_Name) == true ? "手机号不能为空" : "没有这样的手机号哦！";
            logger.error("请求参数login_Password有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (!pay_Password.equals(affirm_Pay_Password)) {
            logger.error("请求参数affirm_Pay_Password有误：{}", "两次支付密码输入不一致");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "两次支付密码输入不一致");
            return restP;
        }

        return restP;
    }


    public static RestResponse verifyRegisterParams(String login_Name, String verifyCode, RestResponse restP) {
        if (StringUtils.isBlank(login_Name) || !login_Name.matches(RegexRule.MOBLIE)) {
            String str = StringUtils.isBlank(login_Name) == true ? "手机号不能为空" : "没有这样的手机号哦！";
            logger.error("请求参数login_Name有误：{}", str);
            return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
        }

        if (StringUtils.isBlank(verifyCode)) {
            logger.error("缺少必要的输入参数！验证码为空！");
            return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "短信验证码不能为空");
        }
        return restP;
    }


    public static RestResponse verifyResetLoginPwdParams(String login_Name, String new_Login_Pwd, String affirm_Login_Pwd,
                                                         String token, RestResponse restP) {
        if (StringUtils.isBlank(login_Name) || !login_Name.matches(RegexRule.MOBLIE)) {
            String str = StringUtils.isBlank(login_Name) == true ? "手机号不能为空" : "没有这样的手机号哦！";
            logger.error("请求参数login_Name有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(new_Login_Pwd)/* || !new_Login_Pwd.matches(RegexRule.WALLET_LOGIN_PWD)*/) {
            String str = StringUtils.isBlank(new_Login_Pwd) == true ? "登录密码不能为空" : "登录密码格式错误 ";
            logger.error("请求参数new_Login_Pwd有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(affirm_Login_Pwd)/* || !affirm_Login_Pwd.matches(RegexRule.WALLET_LOGIN_PWD)*/) {
            String str = StringUtils.isBlank(affirm_Login_Pwd) == true ? "确认登录密码不能为空" : "确认登录密码格式错误";
            logger.error("请求参数affirm_Login_Pwd有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (!new_Login_Pwd.equals(affirm_Login_Pwd)) {
            logger.error("请求参数有误：{}", "两次密码输入不一致");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "两次登录密码输入不一致");
            return restP;
        }

        if (StringUtils.isBlank(token)) {
            logger.error("请求参数token有误：{}","token不能为空");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.UNAUTHORIZED_ERROR, "token不能为空");
            return restP;
        }
        return restP;
    }

    /**
     * 找回支付密码
     * @param login_Name     登录账号
     * @param type           类型
     * @param restP
     * @return
     */
    public static RestResponse verifyResetPayPwdParams(String login_Name, String type, RestResponse restP) {
        if (StringUtils.isBlank(login_Name) || !login_Name.matches(RegexRule.MOBLIE)) {
            String str = StringUtils.isBlank(login_Name) == true ? "手机号不能为空" : "没有这样的手机号哦！";
            logger.error("请求参数login_Name有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(type)) {
             logger.error("请求参数有误：{}","短信类型不能为空");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "短信类型不能为空");
            return restP;
        }
        return restP;
    }

    /**
     * 设置支付密码和登录密码请求
     * @param restP
     * @param login_Password
     * @param affirm_Login_Password
     * @param pay_Password
     * @param affirm_Pay_Password
     * @param login_Name
     * @return
     */
    public static RestResponse verify_Pay_Login_PwdLogin(RestResponse restP, String login_Password,String affirm_Login_Password,
                                                         String pay_Password,String affirm_Pay_Password,String login_Name) {

        if (StringUtils.isBlank(login_Password)/* || !login_Password.matches(RegexRule.WALLET_LOGIN_PWD)*/) {
            String str = StringUtils.isBlank(login_Password) == true ? "登录密码不能为空" : "登录密码格式错误";
            logger.error("请求参数new_Login_Pwd有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(affirm_Login_Password)/* || !affirm_Login_Password.matches(RegexRule.WALLET_LOGIN_PWD)*/) {
            String str = StringUtils.isBlank(affirm_Login_Password) == true ? "确认登录密码不能为空" : "确认登录密码格式错误 ";
            logger.error("请求参数affirm_Login_Pwd有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(pay_Password) || !pay_Password.matches(RegexRule.WALLET_PAY_PWD)) {
            String str = StringUtils.isBlank(pay_Password) == true ? "支付密码不能为空" : "请输入6位数字支付密码！";
            logger.error("请求参数pay_Password有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(affirm_Pay_Password) || !affirm_Pay_Password.matches(RegexRule.WALLET_PAY_PWD)) {
            String str = StringUtils.isBlank(affirm_Pay_Password) == true ? "确认支付密码不能为空" : "请输入6位数字确认支付密码！";
            logger.error("请求参数affirm_Pay_Password有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(login_Name) || !login_Name.matches(RegexRule.MOBLIE)) {
            String str = StringUtils.isBlank(login_Name) == true ? "手机号不能为空" : "没有这样的手机号哦！";
            logger.error("请求参数login_Password有误：{}", str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (!login_Password.equals(affirm_Login_Password)) {
            logger.error("请求参数affirm_Login_Password有误：{}", "两次登录密码输入不一致");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "两次登录密码输入不一致");
            return restP;
        }

        if (!pay_Password.equals(affirm_Pay_Password)) {
            logger.error("请求参数affirm_Pay_Password有误：{}", "两次支付密码输入不一致");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "两次支付密码输入不一致");
            return restP;
        }

        return restP;
    }

    public static RestResponse verifyOcrParams(RestResponse restP, String idCardFrontData,String idCardBackData){
        if (StringUtils.isBlank(idCardFrontData)) {
            logger.error("请求参数idCardFrontData不能为空");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "身份证正面照不能为空");
            return restP;
        }

        if (StringUtils.isBlank(idCardBackData)) {
            logger.error("请求参数idCardBackData不能为空");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "身份证反面照不能为空");
            return restP;
        }
        return restP;
    }

    //话费充值
    public static RestResponse phrechage(String type,String mobile,String rechargeMoney,String productId,RestResponse restP){
        if (StringUtils.isBlank(type)) {
            logger.error("请求参数type不能为空");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "充值类型不能为空");
            return restP;
        }

        if (StringUtils.isBlank(mobile) || !mobile.matches(RegexRule.MOBLIE)) {
            String str = StringUtils.isBlank(mobile) == true ? "手机号不能为空" : "没有这样的手机号哦！";
            logger.error("请求参数mobile错误：{}",str);
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, str);
            return restP;
        }

        if (StringUtils.isBlank(rechargeMoney)) {
            logger.error("请求参数rechargeMoney不能为空");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "充值金额不能为空");
            return restP;
        }

        if (type.equals("flow") && StringUtils.isBlank(productId)) {
            logger.error("请求参数productId不能为空");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "产品id不能为空");
            return restP;
        }

        if (ArrayUtils.indexOf(TYPE,type)<0) {
            logger.error("请求参数有误：{}", "充值类型非法");
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "充值类型非法");
            return restP;
        }

        return restP;
    }

}
