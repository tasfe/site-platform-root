package com.netfinworks.site.ext.integration.member;

import java.util.List;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.enums.ResponseCode;

/**
 * <p>手机号、邮箱等认证验证类接口</p>
 * @author liangzhizhuang.m
 * @version $Id: VerifyService.java, v 0.1 2014年5月23日 上午9:56:06 liangzhizhuang.m Exp $
 */
public interface AuthVerifyService {

    /**
     * 创建认证信息
     * @param info
     * @param env
     * @return
     */
    ResponseCode createVerify(AuthVerifyInfo info, OperationEnvironment env);

    /**
     * 修改认证信息
     * @param info
     * @param env
     * @return
     */
    ResponseCode updateVerify(AuthVerifyInfo info, OperationEnvironment env);

    /**
     * 移除认证信息
     * @param verifyId
     * @param env
     * @return
     */
    ResponseCode deleteVerify(long verifyId, OperationEnvironment env);

    /**
     * 查询认证信息
     * @param info
     * @param env
     * @return
     */
    List<AuthVerifyInfo> queryVerify(AuthVerifyInfo info, OperationEnvironment env);

}
