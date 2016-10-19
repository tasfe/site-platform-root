package com.netfinworks.site.domainservice.account.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.response.AccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.enums.AccountTypeKind;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;

@Service("defaultAccountService")
public class DefaultAccountServiceImpl implements DefaultAccountService {
    protected Logger       log = LoggerFactory.getLogger(getClass());

    @Resource(name = "accountService")
    private AccountService accountService;
    
    @Resource(name = "memberService")
    private MemberService memberService;
    
    @Resource(name = "maQueryService")
    private MaQueryService maQueryService;

    /**
     * 查询账户余额
     *
     * @param req
     * @return
     * @throws BizException
     */
    @Override
    public MemberAccount queryAccountById(String accoutId, OperationEnvironment env) throws ServiceException {
        try {
            return accountService.queryAccountById(accoutId, env);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String queryAccountByMemberId(String memberId, OperationEnvironment env)
            throws Exception {
            BaseMember accountInfo=new BaseMember();
            accountInfo=memberService.queryMemberById(memberId,env);
            log.info("根据memberId查询账户信息:"+accountInfo);
            MemberInfo memberInfo = maQueryService.queryMemberIntegratedInfo(accountInfo.getLoginName(), "" + 1);
            List<AccountInfo> accounts = memberInfo.getAccounts();
            log.info("根据登录帐户查询帐户信息:"+accounts);
            String accountType = null;
            if(accounts.size()>1){
                for(AccountInfo account:accounts){
                    if(account.getAccountType().equals(AccountTypeKind.BAOLIHU_BASE_ACCOUNT.getCode())){
                        accountType = "保理账户";
                    }
                }
            }else{
                accountType = "基础账户";
            }
            return accountType;
       
    }
    
	@Override
	public MemberAccount queryAccountByMemberId(String memberId, Long accountType, OperationEnvironment env)  {
		return accountService.queryAccountByMemberId(memberId, accountType, env);
	}
}
