package com.netfinworks.site.domain.exception;

import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;



/**
 *
 * <p>错误码常量类</p>
 * @author leelun
 * @version $Id: CommonDefinedException.java, v 0.1 2013-11-18 下午4:20:37 lilun Exp $
 */
public class CommonDefinedException {
    public static CommonException SYSTEM_ERROR                      = new ErrorCodeException.CommonException(
                                                                        "SYSTEM_ERROR", "系统内部错误");
    
    public static CommonException NOT_LOGIN                      	= new ErrorCodeException.CommonException(
            															"NOT_LOGIN", "未登录");
    public static CommonException NOT_BIND_PHONE                    = new ErrorCodeException.CommonException(
																		"NOT_BIND_PHONE", "未绑定手机");
    public static CommonException NOT_MEMBER_V0                    = new ErrorCodeException.CommonException(
																		"NOT_MEMBER_V0", "未实名认证V0");
    public static CommonException NOT_MEMBER_V1                    = new ErrorCodeException.CommonException(
																		"NOT_MEMBER_V1", "未实名认证V1");
    public static CommonException NOT_MEMBER_V2                    = new ErrorCodeException.CommonException(
																		"NOT_MEMBER_V2", "未实名认证V2");
    public static CommonException NOT_SET_PAYPASSWORD              = new ErrorCodeException.CommonException(
																		"NOT_SET_PAYPASSWORD", "未设置支付密码");
    
    public static CommonException NOT_SUPPORT_COMPANY              = new ErrorCodeException.CommonException(
																		"NOT_SUPPORT_COMPANY", "不支持此公司");
    
    public static CommonException REQUIRED_FIELD_NOT_EXIST          = new ErrorCodeException.CommonException(
                                                                        "REQUIRED_FIELD_NOT_EXIST",
                                                                        "必填字段未填写");
    public static CommonException FIELD_LENGTH_EXCEEDS_LIMIT        = new ErrorCodeException.CommonException(
                                                                        "FIELD_LENGTH_EXCEEDS_LIMIT",
                                                                        "字段长度超过限制");
    public static CommonException FIELD_TYPE_ERROR                  = new ErrorCodeException.CommonException(
                                                                        "FIELD_TYPE_ERROR",
                                                                        "字段类型错误");

    public static CommonException ILLEGAL_SIGN_TYPE                 = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_SIGN_TYPE",
                                                                        "签名类型不正确");
    public static CommonException ILLEGAL_SIGN                      = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_SIGN", "验签未通过");
    public static CommonException ILLEGAL_ARGUMENT                  = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_ARGUMENT",
                                                                        "参数校验未通过");
    public static CommonException ILLEGAL_SERVICE                   = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_SERVICE",
                                                                        "服务接口不存在");
    public static CommonException ILLEGAL_ID_TYPE                   = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_ID_TYPE",
                                                                        "ID类型不存在");
    public static CommonException USER_ACCOUNT_NOT_EXIST            = new ErrorCodeException.CommonException(
                                                                        "USER_ACCOUNT_NOT_EXIST",
                                                                        "用户账号不存在");
    public static CommonException MEMBER_ID_NOT_EXIST               = new ErrorCodeException.CommonException(
                                                                        "MEMBER_ID_NOT_EXIST",
                                                                        "用户MemberId不存在");
    public static CommonException PARTNER_ID_NOT_EXIST              = new ErrorCodeException.CommonException(
                                                                        "PARTNER_ID_NOT_EXIST",
                                                                        "合作方Id不存在");
    public static CommonException DUPLICATE_REQUEST_NO              = new ErrorCodeException.CommonException(
                                                                        "DUPLICATE_REQUEST_NO",
                                                                        "重复的请求号");
    public static CommonException ILLEGAL_OUTER_TRADE_NO            = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_OUTER_TRADE_NO",
                                                                        "交易订单号不存在");
    public static CommonException ILLEGAL_DATE_FORMAT               = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_DATE_FORMAT",
                                                                        "日期格式错误");
    public static CommonException PAY_METHOD_ERROR                  = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_PAY_ERROR",
                                                                        "支付方式错误");
    public static CommonException ILLEGAL_PAY_METHOD                = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_PAY_METHOD",
                                                                        "支付方式未授权");
    public static CommonException OPERATOR_ID_NOT_EXIST             = new ErrorCodeException.CommonException(
                                                                        "OPERATOR_ID_NOT_EXIST",
                                                                        "操作员Id不存在");
    public static CommonException TRADE_NO_MATCH_ERROR              = new ErrorCodeException.CommonException(
                                                                        "TRADE_NO_MATCH_ERROR",
                                                                        "交易号信息有误");
    public static CommonException TRADE_DATA_MATCH_ERROR            = new ErrorCodeException.CommonException(
                                                                        "TRADE_DATA_MATCH_ERROR",
                                                                        "交易信息有误");
    public static CommonException PREPAY_DATA_MATCH_ERROR           = new ErrorCodeException.CommonException(
                                                                        "PREPAY_DATA_MATCH_ERROR",
                                                                        "订金下订信息有误");

    public static CommonException ILLEGAL_ROYALTY_PARAMETERS        = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_ROYALTY_PARAMETERS",
                                                                        "分润账号集错误");
    public static CommonException MOBILE_NOT_EXIST                  = new ErrorCodeException.CommonException(
                                                                        "MOBILE_NOT_EXIST",
                                                                        "用户手机号不存在");
    public static CommonException TRADE_AMOUNT_MATCH_ERROR          = new ErrorCodeException.CommonException(
                                                                        "TRADE_AMOUNT_MATCH_ERROR",
                                                                        "交易内金额不匹配");
    public static CommonException TRADE_PAY_MATCH_ERROR             = new ErrorCodeException.CommonException(
                                                                        "TRADE_PAY_MATCH_ERROR",
                                                                        "交易与支付金额不匹配");
    public static CommonException ILLEGAL_PREPAY_NO                 = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_PREPAY_NO",
                                                                        "订金下订单号错误");
    public static CommonException ILLEGAL_ACCESS_SWITCH_SYSTEM      = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_ACCESS_SWITCH_SYSTEM",
                                                                        "商户不允许访问该类型的接口");
    public static CommonException ILLEGAL_REFUND_AMOUNT             = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_REFUND_AMOUNT",
                                                                        "退款金额信息错误");
    public static CommonException ILLEGAL_REQUEST                   = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_REQUEST", "风控未通过");
    public static CommonException ILLEGAL_AMOUNT_FORMAT             = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_AMOUNT_FORMAT",
                                                                        "金额格式错误");
    public static CommonException ILLEGAL_ENSURE_AMOUNT             = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_ENSURE_AMOUNT",
                                                                        "担保金额信息错误");
    public static CommonException ILLEGAL_TIME_INTERVAL             = new ErrorCodeException.CommonException(
                                                                        "ILLEGAL_TIME_INTERVAL",
                                                                        "时间区间错误");
    public static CommonException ACCOUNT_TYPE_NOT_SUPPORTED        = new ErrorCodeException.CommonException(
                                                                        "ACCOUNT_TYPE_NOT_SUPPORTED",
                                                                        "账户类型不支持");
    public static CommonException CARD_TYPE_NOT_SUPPORTED           = new ErrorCodeException.CommonException(
                                                                        "CARD_TYPE_NOT_SUPPORTED",
                                                                        "卡类型不支持");
    public static CommonException TRADE_NO_NOT_EXIST                = new ErrorCodeException.CommonException(
                                                                        "TRADE_NO_NOT_EXIST",
                                                                        "商户订单号和钱包交易号不能都为空");
    public static CommonException TRADE_LIST_ERROR                  = new ErrorCodeException.CommonException(
                                                                        "TRADE_LIST_ERROR",
                                                                        "转账列表参数错误");
    public static CommonException REFUND_LIST_ERROR                 = new ErrorCodeException.CommonException(
                                                                        "REFUND_LIST_ERROR",
                                                                        "退款列表参数错误");
    public static CommonException TOTAL_COUNT_ERROR                 = new ErrorCodeException.CommonException(
                                                                        "TOTAL_COUNT_ERROR",
                                                                        "总笔数与实际总笔数不符");
    public static CommonException TOTAL_AMOUNT_ERROR                = new ErrorCodeException.CommonException(
                                                                        "TOTAL_AMOUNT_ERROR",
                                                                        "总金额与实际总金额不符");
    public static CommonException USER_BANKCARD_NOT_EXIST           = new ErrorCodeException.CommonException(
                                                                        "USER_BANKCARD_NOT_EXIST",
                                                                        "用户不存在绑定银行卡");
    public static CommonException SEND_MESSAGE_NOT_SUPPORTED        = new ErrorCodeException.CommonException(
                                                                        "SEND_MESSAGE_NOT_SUPPORTED",
                                                                        "发送消息只支持 Y发送，N不发送");
    public static CommonException Fund_Out_Grade_NOT_SUPPORTED      = new ErrorCodeException.CommonException(
                                                                        "Fund_Out_Grade_NOT_SUPPORTED",
                                                                        "到账级别只支持 0普通，1快速");
    public static CommonException Company_Or_Personal_NOT_SUPPORTED = new ErrorCodeException.CommonException(
                                                                        "Company_Or_Personal_NOT_SUPPORTED",
                                                                        "对公对私只支持 B对公，C对私");
    public static CommonException IDENTITY_NO_ERROR                 = new ErrorCodeException.CommonException(
                                                                        "IDENTITY_NO_ERROR",
                                                                        "会员标识与证书持有者不匹配");
    public static CommonException ACCOUNT_NAME_DECRYPT_ERROR        = new ErrorCodeException.CommonException(
                                                                        "ACCOUNT_NAME_DECRYPT_ERROR",
                                                                        "银行卡账户名解密失败");
    public static CommonException CARD_NO_DECRYPT_ERROR             = new ErrorCodeException.CommonException(
                                                                        "CARD_NO_DECRYPT_ERROR",
                                                                        "银行卡号解密失败");
    public static CommonException TRANSFER_LIST_DECRYPT_ERROR       = new ErrorCodeException.CommonException(
                                                                        "TRANSFER_LIST_DECRYPT_ERROR",
                                                                        "转账列表解密失败");
    public static CommonException ANONYMOUS_PAY_ERROR         		= new ErrorCodeException.CommonException(
                                                                        "ANONYMOUS_PAY_ERROR",
                                                                        "匿名支付时支付方式不能为空");
    public static CommonException NO_VERIFY_ERROR         			= new ErrorCodeException.CommonException(
															            "NO_VERIFY_ERROR",
															            "未实名认证");
    public static CommonException PASSWORD_LOCKED_ERROR         	= new ErrorCodeException.CommonException(
															            "PASSWORD_LOCKED_ERROR",
															            "支付密码被锁定了");
    public static CommonException PASSWORD_ERROR         			= new ErrorCodeException.CommonException(
															            "PASSWORD_ERROR",
															            "支付密码错误");
    public static CommonException ID_LENGHT_ERROR         			= new ErrorCodeException.CommonException(
															            "ID_LENGHT_ERROR",
															            "请输入长度为15或18位身份证号");

    public static CommonException INSUFFICIENT_BALANCE         			= new ErrorCodeException.CommonException(
                                                                        "INSUFFICIENT_BALANCE",
                                                                        "余额不足");
    
    public static CommonException ACCOUNT_EXIST_ERROR      		    = new ErrorCodeException.CommonException(
															            "ACCOUNT_EXIST_ERROR",
															            "用户已存在");
    public static CommonException ACCOUNT_COUNT_ERROR      		    = new ErrorCodeException.CommonException(
            															"ACCOUNT_COUNT_ERROR",
            															"绑卡数量达到上限");
    public static CommonException ACCOUNT_BINDING_REPEAT_ERROR      = new ErrorCodeException.CommonException(
																		"ACCOUNT_BINDING_REPEAT_ERROR",
																		"银行卡已绑定");
    public static CommonException CARD_BIN_ERROR      		        = new ErrorCodeException.CommonException(
																		"CARD_BIN_ERROR",
																		"卡bIN校验失败");
    public static CommonException MEMBERID_NOTEQUALS_BANKCARDMEMBERID   = new ErrorCodeException.CommonException(
			                                                            "MEMBERID_NOTEQUALS_BANKCARDMEMBERID",
			                                                            "提现方会员号与银行卡绑定的会员必须一致");
    public static CommonException PLAT_USR_ID_BIND_EXIST_ERROR   	= new ErrorCodeException.CommonException(
            															"PLAT_USR_ID_BIND_EXIST_ERROR",
    																	"plat_usr_id已存在绑定");
    public static CommonException PASSWORD_EQUAL_LOGIN_PASSWORD_ERROR   = new ErrorCodeException.CommonException(
																		"PASSWORD_EQUAL_LOGIN_PASSWORD",
																		"登录密码与支付密码不能一致");
    public static CommonException GET_MEMBER_INFO_ERROR   			= new ErrorCodeException.CommonException(
																		"GET_MEMBER_INFO_ERROR",
																		"获取会员信息失败");
    public static CommonException SET_DEFAULT_BANK_CARD_ERROR   	= new ErrorCodeException.CommonException(
																		"SET_DEFAULT_BANK_CARD_ERROR",
																		"设置默认银行卡失败");
    public static CommonException REMOVE_BANK_CARD_ERROR   	= new ErrorCodeException.CommonException(
																		"REMOVE_BANK_CARD_ERROR",
																		"解除绑定银行卡失败");

    public static CommonException LOGIN_PASSWORD_LOCKED_ERROR   	= new ErrorCodeException.CommonException(
			                                                            "LOGIN_PASSWORD_LOCKED_ERROR",
			                                                            "登录密码被锁定了");
    public static CommonException OLD_LOGIN_PASSWORD_ERROR   	= new ErrorCodeException.CommonException(
                                                                        "OLD_LOGIN_PASSWORD_ERROR",
                                                                        "旧登录密码验证不通过");
  
    public static CommonException LOCKED_USER_ERROR               = new ErrorCodeException.CommonException(
																		"LOCKED_USER_ERROR",
																		"用户被锁定");
    public static CommonException ID_CARD_VERIFICATION_ERROR            = new ErrorCodeException.CommonException(
																		"ID_CARD_VERIFICATION_ERROR",
																		"身份证验证错误");
    public static CommonException PWD_STRENGTH_VERIFICATION_ERROR = new ErrorCodeException.CommonException(
																		"PWD_STRENGTH_VERIFICATION_ERROR",
																		"密码强度校验未通过");
    public static CommonException PWD_DECRYPTION_ERROR			  = new ErrorCodeException.CommonException(
																		"PWD_DECRYPTION_ERROR",
																		"密码解密失败");
}
