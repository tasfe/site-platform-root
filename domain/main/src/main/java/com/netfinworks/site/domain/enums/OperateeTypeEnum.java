package com.netfinworks.site.domain.enums;

/**
 * 日志操作类型
 * @author KJTPAY
 *
 */
public enum OperateeTypeEnum {
	REGISETER("regiseter", "注册"), 
	LOGIN("login", "登录"),
	RECHARGE("recharge","充值"),
	TOAUDITCASHINFO("toAuditCashInfo","提现"),
	RANSFERKJT("transferKjt","单笔转账"),
	ADDBANKCARD("addBankCard","绑定银行账户"),
	REMOVEBANKCARD("removeBankCard","解绑银行账户"),
	PHONERECHARGE("phoneRecharge","话费充值"),
	RESETMOBILEPHONE("resetMobilephone","修改手机"),
	SETEMAIL("setEmail","绑定邮箱"),
	UNSETEMAIL("unsetEmail","解绑邮箱"),
	RESETEMAIL("resetEmail","修改邮箱"),
	RESET_LOGIN_PASSWD("reset_login_passwd","修改登录密码"),
	RESET_PAY_PASSWD("reset_pay_passwd","修改支付密码"),
	SET_BACK_PAYPASSWD("set_back_paypasswd","找回支付密码"),
	RESET_LOGIN_PWD("reset_login_pwd","找回登录密码"),
	LOCKEDLOGINPASS("lockedLoginPass","锁定登录密码"),
	LOCKEDPAYPASS("lockedPPass","锁定支付密码"),
	CERTIFICATION_PERSONAL("certification_personal","实名认证身份认证"),
	ACCOUNT_CERTIFICATION_PERSONAL("account_certification_personal","实名认证金额认证"),
	
	;
    private String code;
    private String  msg;

    

    public String getCode() {
		return code;
	}



	public void setCode(String code) {
		this.code = code;
	}



	public String getMsg() {
		return msg;
	}



	public void setMsg(String msg) {
		this.msg = msg;
	}


	private OperateeTypeEnum(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}



	public static String getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (OperateeTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }
        return null;
    }
}
