/**
 *
 */
package com.netfinworks.site.domain.enums;

/**
 *
 * <p>业务类型</p>
 * @author qinde
 * @version $Id: BizType.java, v 0.1 2013-12-4 下午4:01:57 qinde Exp $
 */
public enum BizType {
    SET_PAYPASSWD("SET_PAYPASSWD", "设置支付密码"),
    REGISTER_MOBILE("REGISTER_MOBILE", "手机号码注册"),
    REGISTER_EMAIL("REGISTER_EMAIL", "邮箱号码注册"),
    REGISTER_EMAIL_PER("REGISTER_EMAIL_PER", "会员邮箱号码注册"),
    REFIND_PAYPASSWD("REFIND_PAYPASSWD","短信找回支付密码"),
    REFIND_SENDEMAIL("REFIND_SENDEMAIL","邮件找回支付密码"),
    
    SET_MOBILE("SET_MOBILE", "绑定手机号码"),
    RESET_MOBILE("RESET_MOBILE", "修改手机号码"),
    UNSET_MOBILE_USEMOBILE("UNSET_MOBILE_M", "通过短信验证码解绑手机号码"),
    UNSET_MOBILE_USEEMAIL("UNSET_MOBILE_E", "通过邮箱验证解绑手机号码"),
    SET_EMAIL("SET_EMAIL", "绑定邮箱"),
    RESET_EMAIL("RESET_EMAIL", "修改邮箱"),
    UNSET_EMAIL_USEEMAIL("UNSET_EMAIL_E", "通过邮箱验证解绑邮箱"),
    UNSET_EMAIL_USEMOBILE("UNSET_EMAIL_M", "通过手机验证码解绑邮箱"),
    REFIND_LOGIN_SMS("REFI_LOGIN_SMS","短信找回登陆密码"),
    REFIND_LOGIN_EMAIL("REFI_LOGIN_EMA","邮件找回登陆密码"),
    
	CUSTOM_EMAIL("CUSTOM_EMAIL", "客服邮件发送"),	
	
	ACTIVE_MOBILE("ACTIVE_MOBILE", "手机号码激活"),
    ACTIVE_EMAIL("ACTIVE_EMAIL", "邮箱号码激活"),

    /**
     * 通过手机修改邮箱
     */
    RESET_EMAIL_BY_M("RESET_EMAIL_BY_M","通过手机修改邮箱"),
    
    /**
     * 通过手机绑定邮箱 
     */
    SET_EMAIL_BY_M("SET_EMAIL_BY_M","通过手机验证码绑定邮箱"),
    
   
    /**
     * 通过邮箱修改绑定手机
     */
    RESET_MOBILE_E("RESET_MOBILE_E","通过邮箱修改绑定手机"),
    
    /**
     * 审核
     */
    BIZ_TYPE_AUDIT("BIZ_TYPE_AUDIT","审核"),
    
    AUDIT_TRANSFER("AUDIT_TRANSFER","转账审核"),
   
    AUDIT_WITHDRAW("AUDIT_WITHDRAW","提现审核"),
    
    AUDIT_REFUND("AUDIT_REFUND","退款审核"),
    
    AUDIT_PAYOFF("AUDIT_PAYOFF","代发工资审核"),
    
    /** 提现 */
    BIZ_TYPE_WITHDRAW("PAY_WITHDRAW","提现"),
    
    /** 转账 */
    BIZ_TYPE_TRANSFER("PAY_TRANSFER","转账"),
    
    /** 代发工资 */
    BIZ_TYPE_PAYOFF("PAY_PAYOFF","代发工资"),
    
    /** 退款 */
    BIZ_TYPE_REFUND("PAY_REFUND","退款"),
    
    CERT_APPLY("CERT_APPLY", "申请数字证书"),
    
    CERT_INSTALL("CERT_INSTALL", "安装数字证书"),
    
    CERT_UNBIND("CERT_UNBIND", "取消数字证书"),
    
    KJTSHILED_ACTIVE("KJTSHILED_ACTIVE", "激活快捷盾"),
    
	KJTSHILED_UNBIND("KJTSHILED_UNBIND", "解绑快捷盾"),
    
	AUTOFUNDOUT_NOTIFY_EMAIL("AUTOFUNDOUT_NOTIFY_EMAIL", "打款认证失败邮件通知"),
    AUTOFUNDOUT_NOTIFY_MOBILE("AUTOFUNDOUT_NOTIFY_MOBILE", "打款认证失败短信通知"),
    
    ACC_UPD_TOPHONE("ACC_UPD_TOPHONE", "修改登录名发送手机验证码"),
    ACC_UPD_TOEMAIL("ACC_UPD_TOEMAIL", "修改登录名发送邮箱验证码")
    ;

    /** 代码 */
    private final String code;
    /** 信息 */
    private final String message;

    BizType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static BizType getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (BizType at : BizType.values()) {
            if (at.getCode().equals(code)) {
                return at;
            }
        }

        return null;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
