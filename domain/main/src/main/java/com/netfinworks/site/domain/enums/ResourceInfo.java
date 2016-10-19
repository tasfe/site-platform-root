package com.netfinworks.site.domain.enums;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>资源枚举</p>
 * @author eric
 * @version $Id: FunctionEnum.java, v 0.1 2013-7-16 下午1:59:23  Exp $
 */
public enum ResourceInfo {
    INDEX("首页", ResourceType.PAGE, "/index", null),
    
	REGISTER("注册页", ResourceType.PAGE, "/register/register-person-mobile-new", null),

    HOME("主页", ResourceType.PAGE, "/home", null),

    LONGIN("登录页面", ResourceType.PAGE, "/login", null),

    ERROR("错误页面", ResourceType.PAGE, "/common/exception/error", null),

    RESET_PAYPASSWD("重新设置支付密码页面", ResourceType.PAGE, "/paypasswd/reset/reset-paypasswd", null),
    
    RESET_PAYPASSWD_C("重新设置支付密码页面(C账户)", ResourceType.PAGE, "/paypasswd/reset/safetyPay_amend", null),
    
    NAME_VERIFY("实名认证首页面", ResourceType.PAGE, "/certification/name-verify", null),

    NAME_VERIFY_INFO("实名认证上传身份证页面", ResourceType.PAGE, "/certification/certification", null),

    NAME_VERIFY_SUCCESS("实名认证提交成功页面", ResourceType.PAGE, "/certification/certification-success", null),

    MEGER_PAY("合并付款页面", ResourceType.PAGE, "/list/meger_pay", null),

    RECHARGE_SUCCESS("付款成功页面", ResourceType.PAGE, "/recharge/recharge-success", null),

    TRANSFER_INDEX("转账首页面", ResourceType.PAGE, "/transfer/transfer", null),

    TRANSFER_CONFIRM("转账确认页面", ResourceType.PAGE, "/transfer/transfer-confirm", null),

    TRANSFER_SUCCESS("转账成功页面", ResourceType.PAGE, "/transfer/transfer-result", null),
    
    RESET_LOGINPASSWD("重新设置登录密码页面", ResourceType.PAGE, "/paypasswd/reset/reset-loginpasswd", null),

    ;

    /** 资源名称 */
    private final String                     name;
    /** 资源类型 */
    private final ResourceType               type;
    /** 请求地址 */
    private final String                     url;
    /** 父资源 */
    private final ResourceInfo               parent;

    /** 资源MAP，供查找提高效率 */
    private static Map<String, ResourceInfo> resourceMap = new HashMap<String, ResourceInfo>();

    /**
     * 初始化
     */
    static {
        for (ResourceInfo resource : ResourceInfo.values()) {
            resourceMap.put(resource.name(), resource);
        }
    }

    /**
     * 构造
     * @param name
     * @param type
     * @param method
     * @param url
     * @param parent
     */
    ResourceInfo(String name, ResourceType type, String url, ResourceInfo parent) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.parent = parent;
    }

    /**
     * 根据CODE获取枚举
     * @param code
     * @return
     */
    public static ResourceInfo getByCode(String code) {
        return resourceMap.get(code);
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public ResourceInfo getParent() {
        return parent;
    }

}
