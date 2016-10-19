package com.netfinworks.site.core.common.constants;

/**
 * <p>通用常量持有器 </p>
 * @author eric
 * @version $Id: CommonConstant.java, v 0.1 2013-7-16 下午1:52:57  Exp $
 */
public interface CommonConstant {
    /** 编码 */
    public static final String ENCODE         = "UTF-8";
    /** 无操作地方*/
    public static final String NO_URL         = "#";
    /** 本系统标示*/
    public static final String SELF_MARK      = "site";
    /** 真:标记 */
    public static final String TRUE_STRING    = "Y";
    /** 假:标记 */
    public static final String FALSE_STRING   = "N";

    /** 请求方法 */
    public static final String REQUEST_METHOD = "method";
    /** 角色前缀 */
    public static final String ROLE_PREFIX    = "ROLE_";
    
    /** 支付编码 */
    public static final String DEFAULT_PAYMENT_CODE = "1001";
    
    /** 系统编码 */
    public static final String SYSTEM_CODE = "1";
    
    //soap-success
    public static final String RETURN_CODE_SUCCESS = "0";

    public static final long MEMBER_LOCKED = 1;

    public static final long ACTIVE_STATUS = 1;

    public static final String  IMAGE_TYPE = "image";

    /**
     * personal/enterpries url地址前缀
     */
    public static final String URL_PREFIX  = "main";

    final public static String SERVLET_ATTR_NAME_GLOBAL_RESOURCE = "GLOBAL_RESOURCE";
    /**当前普通会员用户的session*/
    final public static String SESSION_ATTR_NAME_CURRENT_PERSONAL_USER = "CURRENT_PERSONAL_USER";
    /**当前特约商户用户的session*/
    final public static String SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER = "CURRENT_ENTERPRISE_USER";
    /**当前个人用户的MemerId*/
    final public static String SESSION_ATTR_NAME_CURRENT_PERSONAL_USER_ID = "CURRENT_PERSONAL_USER_ID";
    /**当前企业用户的MemerId*/
    final public static String SESSION_ATTR_NAME_CURRENT_ENTERPRISE_USER_ID = "CURRENT_ENTERPRICE_USER_ID";
    /**当前用户的LoginName*/
    final public static String SESSION_ATTR_NAME_CURRENT_USER_LOGINNAME = "CURRENT_USER_LOGINNAME";
    /**当前普通用户的MemberName*/
    final public static String KJT_PERSON_USER_NAME = "KJT_PERSON_USER_NAME";
    /**当前特约商户的MemberName*/
    final public static String KJT_ENTERPRISE_USER_NAME = "KJT_ENTERPRISE_USER_NAME";

    /**当前收款人信息*/
    final public static String SESSION_ATTR_NAME_CURRENT_PAYER = "CURRENT_PAYER";
    /**当前转账交易信息*/
    final public static String SESSION_ATTR_NAME_CURRENT_TRANSFER = "CURRENT_TRANSFER";
    /**当前批量转账交易信息*/
    final public static String SESSION_ATTR_NAME_CURRENT_BATCH_TRANSFER = "CURRENT_BATCH_TRANSFER";
    /**当前代发工资交易信息*/
    final public static String SESSION_ATTR_NAME_CURRENT_PAYOFF = "CURRENT_BATCH_PAYOFF";
    /**批量代发工资交易信息*/
    final public static String SESSION_ATTR_NAME_CURRENT_BATCH_PAYOFF = "CURRENT_BATCH_PAYOFF";
    /** 当前提现信息 */
    final public static String SESSION_ATTR_NAME_CURRENT_CASHING = "CURRENT_CASHING";
    /**支付密码状态*/
    final public static String SESSION_PAY_PASSWD_STATUS = "SESSION_PAY_PASSWD_STATUS";

    final public static String SESSION_ATTR_NAME_KAPTCHA_KEY = "KAPTCHA";
    final public static String SESSION_ATTR_NAME_SOURCE = "SOURCE";

    final public static String SESSION_CERTIFICATION = "CERTIFICATION";
    
    final public static String USERTYPE_PERSON = "person";
    final public static String USERTYPE_ENTERPRISE = "enterprise";
	final public static String USERTYPE_MERCHANT = "merchant";

	final public static String SESSION_CSRF_TOKEN = "SESSION_CSRF_TOKEN";
	final public static String RESET_MOBILE_TOKEN = "RESET_MOBILE_TOKEN";
	final public static String SET_MOBILE_TOKEN = "SET_MOBILE_TOKEN";
	final public static String REFIND_PAYPWD_TOKEN = "REFIND_PAYPWD_TOKEN";
	final public static String REFIND_PAYPWD_TOKEN_TWO = "REFIND_PAYPWD_TOKEN_TWO";
	final public static String REFIND_LOGINPWD_TOKEN = "REFIND_LOGINPWD_TOKEN";
	final public static String RESET_EMAIL_TOKEN = "RESET_EMAIL_TOKEN";
	final public static String SET_EMAIL_TOKEN = "SET_EMAIL_TOKEN";
	final public static String OPERATOR_TOKEN = "OPERATOR_TOKEN";

	final public static String REFRESH_TOKEN = "REFRESH_TOKEN";

	final public static String CUSTOM_EMAIL = "service@yongdapay.com";

    /**用户不存在*/
    final public static String USER_NOT_EXIST = "103";

    /**操作员不存在*/
    final public static String OPERATOR_NOT_EXIST="201";

    /** 个人钱包应用ID */
    final public static String PERSONAL_APP_ID = "fj050";
    /**
     * 商户钱包
     */
    final public static String ENTERPRISE_APP_ID= "fj059";
    /**
     * reset支付密码url
     */
    public static final String RESET_PAYPASSWORD_URL="resetPayPassWordUrl";
    /**
     * 邮箱
     */
    public static final String MEMBER_EMAIL="email";
    /**
     * 曾用提现账号
     */
    public static final String WITHDRAWAL_CARDID="withdrawalCardId";
    /**
     * 曾用充值账号
     */
    public static final String RECHARGE_CARDID="rechargeCardId";
    /**
     * 钱包缓存命名空间
     */
    public static final String WALLET_NAMESPACE_CACHE="walletcache";
    /**
     *
     */
    public static final String NOTIFY_EMAIL="notifyEmail";
    public static final String NOTIFY_MOBILE="notifyMobile";
    /**
     *
     */
    public static final String NOTIFY_MESSAGE="notifyMessage";

    /**
     * 手机验证码有效时间
     */
	public static final long VALIDITY = 180L;

    /**
     * 钱包转账落地交易订单的partnerId
     */
    public static final String TRADE_PARTNER_ID="innerMember";

    public static final String  WAIT_BUYER_PAY = "100";

    public static final String  WAIT_BUYER_PAY_BANK = "110";

    public static final String  REFUND_ING = "900";
    public static final String  REFUND_SUCCESS = "951";
    public static final String  REFUND_FAIL = "952";

    /**证书*/
    public static final String _base64_cert = "base64_cert";

    public static final String CACHE_NAMESPACE_PAY_PASSWORD_SALT = "PAY_PASSWORD_SALT";

    /**账户开户处理*/
    public static final String MEMBER_SET_STATUS = "model";

    public static final String MEMBER_ACTIVE_MOBILE = "MEMBER_ACTIVE_MOBILE";

    /**转账通知的信息扩展字段key */
    public static final String TRADE_NOTIFY_KEY = "notify_info";
    /**转账通知的手机号扩展字段key */
    public static final String TRADE_NOTIFY_MOBILE_KEY = "notify_mobile";
    /**转账通知的备注扩展字段key */
    public static final String TRADE_NOTIFY_MEMO_KEY = "notify_memo";
    /**转账通知的模板扩展字段key */
    public static final String TRADE_NOTIFY_TEMPLATE_KEY = "notify_template";
    /**转账通知app的模板扩展字段key */
    public static final String TRADE_NOTIFY_APP_TEMPLATE_KEY = "notify_template_app";
    /**转账通知app的模板扩展字段key */
    public static final String TRADE_NOTIFY_MEMBER_KEY = "notify_member";
    /**转账通知的信息扩展字段 参数key */
	public static final String TRADE_NOTIFY_PARAM = "notify_param";

	public static final String NOTIFY_PARAM_IDENTITY_TYPE = "identityType";

	public static final String NOTIFY_PARAM_IDENTITY_TYPE_MEMBER = "MEMBER_ID";

    public static final String ERROR = "ERROR";
    /**判断是否为剩余次数*/
    public static final String REMAINNUM = "256";
    /**支付密码被锁*/
    public static final String PAYPASSWD_LOCKED = "264";
    /**登录密码被锁*/
    public static final String LOGINPASSWD_LOCKED = "274";

	/** 登录密码和支付密码相同 */
	public static final String LOGINPWD_EQUALS_PAYPWD = "276";

    public static String ERROR_NOTRIGHT = "noright";
    public static String ERROR_LOCKED = "locked";
    public static String ERROR_PARTEN = "parten";
    public static String ERROR_SAME = "same";
    public static String ERROR_NOT_SAME = "notsame";

    public static String DEFAULT_SALT = "ac5e940b94a51609788f38c6db70f39b";

    public static final String SOURCE_CODE                       = "fj054";


    /**用户能重置登陆密码的登陆会员信息*/
    public static String SESSION_RESET_LOGIN_PWD_MEMBER="SESSION_RESET_LOGIN_PWD_MEMBER";
    /**用户能否重置登陆密码的标志位*/
    public static String SESSION_CAN_RESET_LOGIN_PWD_FLAG="SESSION_CAN_RESET_LOGIN_PWD_FLAG";

    /**用户能重置登陆密码的登陆会员信息-企业会员*/
    public static String SESSION_RESET_LOGIN_PWD_MEMBER_ENT="SESSION_RESET_LOGIN_PWD_MEMBER_ENT";
    /**用户能否重置登陆密码的标志位-企业会员*/
    public static String SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT="SESSION_CAN_RESET_LOGIN_PWD_FLAG_ENT";
    
    /**登录密码和支付密码相同*/
    public static String ERROR_PAYPWD_EQUAL_LOGINPWD = "PAYPWD_EQUAL_LOGINPWD";
    
    public static final int MAX_IMPORT_COUNT = 999;

	public static final String PATTERN_MOBILE = "^(13[0-9]{9}|15[012356789][0-9]{8}|18[02356789][0-9]{8}|147[0-9]{8}|17[0-9]{9})$";

	
	public static final String AUDIT_TYPE_REFUND                            = "refund";
    public static final String AUDIT_SUB_TYPE_BATCH                         = "batch";
	/** 线下充值产品码 */
	public static final String OFFLINE_RECHARGE_PRODUCTCODE = "10010002";
	/** 打款验证次数 缓存key前半部分 */
	public static final String VERIFY_AMOUNT = "verify_amount_";
	
	public static final String ACCOUNT_TYPE = "account_type";

	/** 出款状态 */
	public static final String SUBMITTED_FAIL = "SUBMITTED_FAIL";// 提交银行失败
	public static final String WITHDRAWAL_SUBMITTED = "WITHDRAWAL_SUBMITTED";// 已提交银行
	public static final String WITHDRAWAL_SUCCESS = "WITHDRAWAL_SUCCESS";// 出款成功
	public static final String WITHDRAWAL_FAIL = "WITHDRAWAL_FAIL";// 出款失败
	public static final String RETURN_TICKET = "RETURN_TICKET";// 已退票
	
	public static final String CITY_WEATHER = "city_weather";//城市天气
	public static final String USER_NAME = "user_name";//城市天气
	public static final String OPENID = "openid";
}
