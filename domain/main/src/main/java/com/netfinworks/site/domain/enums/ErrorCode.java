/**
 *
 */
package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;


/**
 * <p>注释</p>
 * @author fjl
 * @version $Id: ErrorCode.java, v 0.1 2013-11-12 下午4:11:40 fjl Exp $
 */
public enum ErrorCode {
    SYSTEM_ERROR("SYSTEM_ERROR","系统内部错误"),

    SESSION_TIMEOUT("SESSION_TIMEOUT","session超时"),

    ILLEGAL_ACCESS_SWITCH_SYSTEM("ILLEGAL_ACCESS_SWITCH_SYSTEM","商户不允许访问该类型的接口"),

    PARTNER_ID_NOT_EXIST("PARTNER_ID_NOT_EXIST","合作方Id不存在"),

    TRADE_DATA_MATCH_ERROR("TRADE_DATA_MATCH_ERROR","交易信息有误"),

    ILLEGAL_REQUEST("ILLEGAL_REQUEST","无效请求"),

    EXTERFACE_INVOKE_CONTEXT_EXPIRED("EXTERFACE_INVOKE_CONTEXT_EXPIRED","接口调用上下文过期"),

    ILLEGAL_SIGN_TYPE("ILLEGAL_SIGN_TYPE","签名类型不正确"),

    ILLEGAL_SIGN("ILLEGAL_SIGN","验签未通过"),

    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT","参数错误"),

    ILLEGAL_SERVICE("ILLEGAL_SERVICE","服务接口不存在"),

    IDENTITY_EXIST_ERROR("IDENTITY_EXIST_ERROR","会员标识已经存在开户失败"),

    MEMBER_NOT_EXIST("MEMBER_NOT_EXIST","会员不存在"),

    ACCOUNT_NOT_EXIST("ACCOUNT_NOT_EXIST","账户不存在"),

	OPERATOR_NOT_EXIST("OPERATOR_NOT_EXIST", "您输入的操作员不存在"),
    
    SSO_NO_LOGIN("SSO_NO_LOGIN","会员未登录,在SSO未查询到会员信息"),
    
	PAYMENT_FAILURE("PAYMENT_FAILURE", "支付失败"),
    
    LOGINPWD_EQUALS_PAYPWD("LOGINPWD_EQUALS_PAYPWD","登录密码不能和支付密码相同");

    private String code;
    private String message;

    private ErrorCode(String code,String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode getByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        for(ErrorCode item : values()){
            if(item.getCode().equals(code)){
                return item;
            }
        }
        return null;
    }


}
