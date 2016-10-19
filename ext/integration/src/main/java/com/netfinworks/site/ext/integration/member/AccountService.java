/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 通用说明：会员远程调用接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:53:51
 *
 */
public interface AccountService {

    /**
     * 查询账户
     *
     * @param req
     * @return
     * @throws BizException
     */
    public MemberAccount queryAccountById(String accountId, OperationEnvironment env) throws BizException;
    /**
     * 根据memberId, 账号类型获取账号
     * @param memberId
     * @param accountType
     * @param env
     * @return
     */
    public MemberAccount queryAccountByMemberId(String memberId, Long accountType, OperationEnvironment env);
    
    /**
     * 根据memberId查询企业类型(0:供应商，1:核心企业, -1:普通企业)
     *
     * @param memberId
     * @return
     * @throws Exception
     */
    public String queryUserType(String memberId) throws Exception;
    
    /**
     * 根据memberId查询账户类型(205:保理账户)
     * 
     * @param memberId
     * @param env
     * @return
     * 
     */
    public String queryAccountType(String memberId,OperationEnvironment env);
    
    /**
     * 根据会员编号查询会员（供应商或核心企业）的组织机构代号
     *
     * @param req
     * @return
     * @throws BizException
     */
    public String queryMemberOrganization(String memberId) throws BizException;

}
