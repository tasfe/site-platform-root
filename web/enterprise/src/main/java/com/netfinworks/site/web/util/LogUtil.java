/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @version 1.0.0
 * @date 2015年2月3日 江海龙/2015年2月3日/首次创建
 */
package com.netfinworks.site.web.util;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.enums.OperateTypeEnum;

/**
 * <p>
 * 风控日志工具
 * </p>
 * 
 * @author 江海龙
 * @since jdk6
 * @date 2015年2月3日
 */
public class LogUtil {

    private static final String[] keyName       = { "操作类型", "用户ID", "账号", "名称", "操作员", "IP", "MAC",
        "内容"                                   };

    private static final String   split_arr     = ",";
    private static final String   split_name    = "-";

    public static String generateMsg(OperateTypeEnum operateType, EnterpriseMember member,
            OperationEnvironment environment, String content) {

        StringBuffer sb = new StringBuffer();
        sb.append(keyName[0]).append(split_name).append(operateType.getName()).append(split_arr);
        sb.append(keyName[1]).append(split_name).append(member.getMemberId()).append(split_arr);
        sb.append(keyName[2]).append(split_name).append(member.getLoginName()).append(split_arr);
        sb.append(keyName[3]).append(split_name).append(member.getMemberName()).append(
                split_arr);
        sb.append(keyName[4]).append(split_name).append(member.getOperator_login_name()).append(split_arr);
        sb.append(keyName[5]).append(split_name).append(
                environment != null ? environment.getClientIp() : "null").append(split_arr);
        sb.append(keyName[6]).append(split_name).append(
                environment != null ? environment.getClientMac() : "null").append(split_arr);
        sb.append(keyName[7]).append(split_name).append(content);
        return sb.toString();
    }

}
