package com.netfinworks.site.domainservice.spi.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.CompanyInfo;
import com.netfinworks.ma.service.base.model.MerchantInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.ma.service.response.MemberVerifyLevelResponse;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.auth.inner.MemberAuthInnerService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.BankAccountService;
import com.netfinworks.site.ext.integration.member.MemberService;

/**
 *
 * <p>会员查询接口</p>
 * @author qinde
 * @version $Id: DefaultMemberServiceImpl.java, v 0.1 2013-12-5 下午8:21:28 qinde Exp $
 */
@Service("defaultMemberService")
public class DefaultMemberServiceImpl implements DefaultMemberService {

    protected Logger       log = LoggerFactory.getLogger(getClass());

    @Resource(name = "memberService")
    private MemberService  memberService;

    @Resource(name = "accountService")
    private AccountService accountService;

    @Resource(name = "bankAccountService")
    private BankAccountService bankAccountService;

	@Resource(name = "memberAuthInnerService")
	private MemberAuthInnerService memberAuthInnerService;

    /**
     * 查询集成会员
     * 1.查询
     * 2.无用户则创建一个用户
     * @param env
     * @param request
     * @return PersonalMemberResponse
     */
    @Override
    public PersonMember queryMemberIntegratedInfo(PersonMember user, OperationEnvironment env)
                                                                                              throws ServiceException,
                                                                                              MemberNotExistException {
        try {
            PersonMember info = memberService.queryMemberIntegratedInfo(user, env);
            //查询账户余额
            if (info.getDefaultAccountId() != null) {
                user.setDefaultAccountId(info.getDefaultAccountId());
                info.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
            } else {//没有账户ID，无法查询账户余额
                info.setAccount(null);
            }
            BankAccRequest req = new BankAccRequest();
            req.setMemberId(user.getMemberId());
            req.setClientIp(env.getClientIp());
            List<BankAccountInfo> list = bankAccountService.queryBankAccount(req);
            if((list != null) && (list.size() > 0)) {
                info.setBankCardCount(list.size());
            } else {
                info.setBankCardCount(0);
            }
            return info;
        } catch (MemberNotExistException e) {
            log.warn("查询会员：{},会员信息不存在", user);
            throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 查询账户余额
     * @param user
     * @param env
     * @return
     * @throws ServiceException
     */
    @Override
    public MemberAccount queryMemberAccount(PersonMember user, OperationEnvironment env) throws ServiceException {
        try {
            return accountService.queryAccountById(user.getDefaultAccountId(), env);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }
    /**
     * 查询企业会员
     * @param user
     * @param env
     * @return
     * @throws BizException
     * @throws MemberNotExistException
     */
    @Override
    public EnterpriseMember queryCompanyMember(EnterpriseMember user, 
    		OperationEnvironment env) throws ServiceException, MemberNotExistException {
        try {
            EnterpriseMember info = memberService.queryCompanyMember(user, env);
            //TODO 如果查询出来的会员类型不是企业抛错
            //查询账户余额
            if (info.getDefaultAccountId() != null) {
                user.setDefaultAccountId(info.getDefaultAccountId());
                info.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
            } else {//没有账户ID，无法查询账户余额
                info.setAccount(null);
            }
            return info;
        } catch (MemberNotExistException e) {
            log.warn("查询会员：{},会员信息不存在", user);
            throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 查询集成会员是否存在
     *
     * @param env
     * @param request
     * @return PersonalMemberResponse
     */
    @Override
    public PersonMember isMemberIntegratedExist(PersonMember user, OperationEnvironment env)
                                                                                     throws ServiceException, MemberNotExistException {

        try {
            //TODO 如果查询出来的会员类型不是个人抛错
            return memberService.queryPersonalMemberExist(user, env);
        } catch (MemberNotExistException e) {
            log.warn("查询会员：{},会员信息不存在", user);
            throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
        }  catch (BizException e) {
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 查询集成会员是否存在
     *
     * @param identityNo
     * @param platformType
     * @param request
     * @return BaseMember
     */
    @Override
    public BaseMember isMemberExists(String identityNo, String platformType, OperationEnvironment env)
    		throws ServiceException, MemberNotExistException {

        try {
            return memberService.queryPersonalMemberExist(identityNo, platformType, env);
        } catch (MemberNotExistException e) {
            log.warn("查询会员：{},会员信息不存在", identityNo);
            throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
        }  catch (BizException e) {
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 个人会员集成创建
     * @param env
     * @param request
     * @return Response
     * @throws ServiceException
     */
    @Override
    public MemberAndAccount createIntegratedPersonalMember(PersonMember user, OperationEnvironment env) throws ServiceException {
        try {
            return memberService.createIntegratedPersonalMember(user, env);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 创建企业会员
     * @param enterprise
     * @param env
     * @return
     * @throws BizException
     */
    @Override
    public MemberAndAccount createEnterpriseMember(EnterpriseMember enterprise,
                                                   OperationEnvironment env)
                                                                            throws ServiceException {
        try {
            return memberService.createEnterpriseMember(enterprise, env);
        } catch (BizException e) {
            throw new ServiceException(e.getMessage(), e.getCause());
        }

    }

    /**
     * 个人会员激活
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public boolean activatePersonalMemberInfo(PersonMember person, OperationEnvironment env) throws ServiceException {
        try {
            return memberService.activatePersonalMemberInfo(person, env);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 企业会员激活
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public boolean activateCompanyMember(EnterpriseMember enterprise, OperationEnvironment env)
                                                                                               throws ServiceException {
        try {
            return memberService.activateCompanyMember(enterprise, env);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void fillMemberRealName(PersonMember user, OperationEnvironment env)
                                                                               throws ServiceException {
        try {
            memberService.fillMemberRealName(user, env);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }

    }

    /**
     * 查询手机密文
     * @param memberId
     * @param base
     * @throws BizException
     */
    @Override
    public String getMobileTicket(String memberId, OperationEnvironment base)
                                                                             throws ServiceException {
        try {
            return memberService.getMobileTicket(memberId, base);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }

    }

	/**
	 * 企业会员信息设置
	 */
	@Override
	public boolean setCompanyMember(EnterpriseMember enterprise, OperationEnvironment env) throws ServiceException {
		try {
			return memberService.setCompanyMember(enterprise, env);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * 企业信息查询
	 */
	@Override
	public CompanyMemberInfo queryCompanyInfo(EnterpriseMember enterprise, OperationEnvironment env)
			throws ServiceException {
		try {
			return memberService.queryCompanyInfo(enterprise, env);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * 添加商户
	 */
	@Override
	public boolean createMerchant(EnterpriseMember enterprise, OperationEnvironment env) throws ServiceException {
		try {
			return memberService.createMerchant(enterprise, env);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * 添加会员角色
	 */
	@Override
	public boolean addRoleToMember(AuthVO authVO) throws ServiceException {
		try {
			return memberAuthInnerService.addRoleToMember(authVO);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * 创建企业会员
	 */
	@Override
	public MemberAndAccount createIntegratedCompanyMember(EnterpriseMember enterprise, CompanyInfo comInfo,
			MerchantInfo merInfo, OperationEnvironment env) throws ServiceException {
		try {
			return memberService.createIntegratedCompanyMember(enterprise, comInfo, merInfo, env);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public BaseMember getMemberAndAccount(String concat, Long accountType, TradeEnvironment env) {
		BaseMember member = null;
		try {
			member = this.isMemberExists(concat, CommonConstant.SYSTEM_CODE, env);
		} catch (Exception e) {
			log.error("查询会员信息失败", e);
		}
		
		if (member != null) {
			String memberId = member.getMemberId();
			MemberAccount account = accountService.queryAccountByMemberId(memberId, accountType, env);
			member.setAccount(account);
			
			if (account != null) {
				member.setDefaultAccountId(account.getAccountId());
			}
		}
		
		return member;
	}

	@Override
	public String getTargetAccountName(BaseMember member, OperationEnvironment env) {
	    if ((member == null) || StringUtils.isEmpty(member.getMemberId())) {
	        return StringUtils.EMPTY;
	    }
	    
	    if (MemberType.PERSONAL.equals(member.getMemberType())) {
	        // 个人会员直接取会员名称
	        return member.getMemberName();
	    } else {
	        // 商户和企业取企业名称
	        EnterpriseMember enterpriseMember = new EnterpriseMember();
	        enterpriseMember.setMemberId(member.getMemberId());
	        CompanyMemberInfo compInfo = null;
	        try {
	            compInfo = this.queryCompanyInfo(enterpriseMember, env);
	        } catch (ServiceException e) {
	            log.error("查询账户信息失败", e);
	        }
	        if (compInfo != null) {
	            return compInfo.getCompanyName();
	        }
	    }
	    
		return StringUtils.EMPTY;
	}

	/**
	 * 更新会员实名认证等级
	 */
	@Override
	public boolean updateMemberVerifyLevel(String memberId, CertifyLevel level, OperationEnvironment env)
			throws ServiceException {
		try{
			memberService.updateMemberVerifyLevel(memberId, level.getCode(), env);
		} catch (BizException e) {
			log.error("更新会员实名认证等级失败：", e.getMessage());
			throw new ServiceException("更新会员实名认证等级失败！");
		}
		return true;
	}

	/**
	 * 查询会员实名认证等级
	 */
	@Override
	public String getMemberVerifyLevel(String memberId, OperationEnvironment env) throws ServiceException {
		String level = "";
		try {
			MemberVerifyLevelResponse response = memberService.getMemberVerifyLevel(memberId, env);
			level = response.getVeriyLevel();
		} catch (BizException e) {
			log.error("查询会员实名认证等级失败：", e.getMessage());
			throw new ServiceException("更新会员实名认证等级失败！");
		}
		return level;
	}

}
