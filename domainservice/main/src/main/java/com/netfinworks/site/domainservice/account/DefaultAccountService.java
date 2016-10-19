
package com.netfinworks.site.domainservice.account;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.exception.BizException;

/**
 *
 * <p>账户</p>
 * @author qinde
 * @version $Id: DefaultAccountService.java, v 0.1 2013-12-2 下午6:40:40 qinde Exp $
 */
public interface DefaultAccountService {
    /**
     * 查询账户
     *
     * @param req
     * @return
     * @throws BizException
     */
    public MemberAccount queryAccountById(String accountId, OperationEnvironment env) throws ServiceException;
    /**
     * 根据memberId, 账号类型获取账号
     * @param memberId 会员Id
     * @param accountType 会员类型
     * @param env
     * @return
     */
    public MemberAccount queryAccountByMemberId(String memberId, Long accountType, OperationEnvironment env);
    /**
     * 根据memberId查询账户类型(保理账户，基础账户)
     *
     * @param req
     * @return
     * @throws BizException
     */
    public String queryAccountByMemberId(String memberId, OperationEnvironment env) throws Exception;
}
