/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domain-main
 * @version 1.0.0
 * @date 2015年2月3日 江海龙/2015年2月3日/首次创建
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>
 * 商户平台操作类型枚举
 * </p>
 * 
 * @author 江海龙
 * @since jdk6
 * @date 2015年2月3日
 */
public enum OperateTypeEnum {

    REGISTER("1", "注册"), LOGIN("2", "登录"), FIND_LOGIN_PWD("3", "找回登录密码"), ACCOUNT_ACTIVE("4",
            "账户激活"), ADD_ROLE("5", "添加角色"), MOD_ROLE("6", "修改角色"), DEL_ROLE("7", "删除角色"), ADD_OPERATOR(
            "8", "添加操作员"), MOD_OPERATOR("9", "修改操作员"), DEL_OPERATOR("10", "删除操作员"), RECHARGE("11",
            "充值"), WITHDRAW_APPLY("12", "提现申请"), WITHDRAW_EXAMINE("13", "提现审核"), TRANSFER_APPLY_FILE(
            "14", "文件转账申请"), TRANSFER_APPLY("15", "转账申请"), TRANSFER_EXAMINE("16", "转账审核"), REFUND_APPLY_FILE(
            "17", "文件退款申请"), SINGLE_REFUND("18", "单笔退款"), REFUND_EXAMINE("19", "退款审核"), BIND_ACCOUNT(
            "20", "绑定银行账户"), UNBIND_ACCOUNT("21", "解绑银行账户"), GONGZI_APPLY("22", "代发工资申请"), GONGZI_EXAMINE(
            "23", "代发工资审核"), BIND_PHONE("24", "绑定手机"), UNBIND_PHONE("25", "解绑手机"), MOD_PHONE("26",
            "修改手机"), BIND_MAIL("27", "绑定邮箱"), UNBIND_MAIL("28", "解绑邮箱"), MOD_MAIL("29", "修改邮箱"), MOD_LOGIN_PWD(
            "30", "修改登录密码"), MOD_PAY_PWD("31", "修改支付密码"), FIND_PAY_PWD("32", "找回支付密码"), HARDCERT_ACTIVE(
            "33", "硬证书激活"), HARDCERT_UNBIND("34", "硬证书解绑"), HADRCERT_UNDATE("35", "硬证书更新"), LOCK_LOGIN_PWD(
            "36", "锁定登录密码"), LOCK_PAY_PWD("37", "锁定支付密码"), ID_CERT("38", "实名认证身份认证"), MONEY_CERT(
            "39", "实名认证金额认证");

    private String code;

    private String name;

    private OperateTypeEnum(String code, String name) {
        this.setCode(code);
        this.setName(name);
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
