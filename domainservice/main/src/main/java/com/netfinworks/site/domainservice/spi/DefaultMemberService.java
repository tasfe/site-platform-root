/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.spi;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.CompanyInfo;
import com.netfinworks.ma.service.base.model.MerchantInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;

/**
 * 通用说明：
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午9:17:08
 *
 */
public interface DefaultMemberService {

    /**
     * 查询集成会员
     * @param env
     * @param request
     * @return PersonalMemberResponse
     */
    public PersonMember queryMemberIntegratedInfo(PersonMember user, OperationEnvironment env)
                                                                                              throws ServiceException,
                                                                                              MemberNotExistException;

    /**
     * 查询账户余额
     * @param user
     * @param env
     * @return
     * @throws ServiceException
     */
    public MemberAccount queryMemberAccount(PersonMember user, OperationEnvironment env) throws ServiceException;

    /**
     * 查询企业会员
     * @param user
     * @param env
     * @return
     * @throws BizException
     * @throws MemberNotExistException
     */
    public EnterpriseMember queryCompanyMember(EnterpriseMember user, OperationEnvironment env)
                                                                                               throws ServiceException,
                                                                                               MemberNotExistException;
    /**
     * 企业会员激活
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public boolean activateCompanyMember(EnterpriseMember enterprise, OperationEnvironment env)
                                                                                               throws ServiceException;
    /**
     * 查询集成会员是否存在
     *
     * @param env
     * @param request
     * @return MemberAllInfo
     */
    public PersonMember isMemberIntegratedExist(PersonMember user, OperationEnvironment req)
                                                                                     throws ServiceException,
                                                                                     MemberNotExistException;

    /**
     * 个人会员集成创建
     * @param env
     * @param request
     * @return Response
     */
    public MemberAndAccount createIntegratedPersonalMember(PersonMember user, OperationEnvironment env) throws ServiceException;

    /**
     * 创建企业会员
     * @param enterprise
     * @param env
     * @return
     * @throws BizException
     */
    public MemberAndAccount createEnterpriseMember(EnterpriseMember enterprise,
                                                   OperationEnvironment env)
                                                                            throws ServiceException;

    /**
     * 个人会员激活
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public boolean activatePersonalMemberInfo(PersonMember person, OperationEnvironment env) throws ServiceException;

    /**
     * 填充会员真实姓名(明文和ticket),掩码不填充
     * @param user
     * @param base
     */
    public void fillMemberRealName(PersonMember user, OperationEnvironment env)
                                                                               throws ServiceException;

    /**
     * 查询手机密文
     * @param memberId
     * @param base
     * @throws BizException
     */
    public String getMobileTicket(String memberId, OperationEnvironment base)
                                                                             throws ServiceException;

    /**
     * 会员是否存在
     * @param identityNo
     * @param platformType
     * @param env
     * @return
     * @throws ServiceException
     * @throws MemberNotExistException
     */
	public BaseMember isMemberExists(String identityNo, String platformType, OperationEnvironment env)
			throws ServiceException, MemberNotExistException;

	/**
	 * 企业会员信息设置
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws ServiceException
	 */
	public boolean setCompanyMember(EnterpriseMember enterprise, OperationEnvironment env) throws ServiceException;

	/**
	 * 企业信息查询
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws ServiceException
	 */
	public CompanyMemberInfo queryCompanyInfo(EnterpriseMember enterprise, OperationEnvironment env)
			throws ServiceException;

	/**
	 * 添加商户
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws ServiceException
	 */
	public boolean createMerchant(EnterpriseMember enterprise, OperationEnvironment env) throws ServiceException;

	/**
	 * 添加会员角色
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws ServiceException
	 */
	public boolean addRoleToMember(AuthVO authVO) throws ServiceException;

	/**
	 * 创建企业会员
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public MemberAndAccount createIntegratedCompanyMember(EnterpriseMember enterprise, CompanyInfo comInfo,
			MerchantInfo merInfo, OperationEnvironment env) throws ServiceException;
	
	/**
	 * 获取会员和账号信息
	 * @param concat
	 * @param accountType
	 * @param env
	 * @return
	 */
	public BaseMember getMemberAndAccount(String concat, Long accountType, TradeEnvironment env);
	
	/**
	 * 获取目标账户名称
	 * @param baseMember 会员对象
	 * @return 会员名称
	 */
	public String getTargetAccountName(BaseMember member, OperationEnvironment env);

	/**
	 * 更新会员实名认证等级
	 * 
	 * @param baseMember 会员对象
	 * @return 会员名称
	 */
	public boolean updateMemberVerifyLevel(String memberId, CertifyLevel level, OperationEnvironment env)
			throws ServiceException;

	/**
	 * 查询会员实名认证等级
	 * 
	 * @param memberId
	 * @param env
	 * @return
	 * @throws ServiceException
	 */
	String getMemberVerifyLevel(String memberId, OperationEnvironment env) throws ServiceException;

}
