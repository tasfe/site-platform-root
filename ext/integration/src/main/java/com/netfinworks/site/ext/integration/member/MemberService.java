/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.CompanyInfo;
import com.netfinworks.ma.service.base.model.MerchantInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.ma.service.response.MemberVerifyLevelResponse;
import com.netfinworks.ma.service.response.MerchantListResponse;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;

/**
 * 通用说明：会员远程调用接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:53:51
 *
 */
public interface MemberService {

    /**
     * 根据ID会员信息
     *
     * @param user
     * @param env
     * @return
     * @throws BizException
     * @throws MemberNotExistException
     */
    public BaseMember queryMemberById(String memberId, OperationEnvironment env)
                                                                                  throws BizException,
                                                                                  MemberNotExistException;

    /**
     * 查询个人会员信息
     *
     * @param user
     * @param env
     * @return
     * @throws BizException
     * @throws MemberNotExistException
     */
    public PersonMember queryMemberIntegratedInfo(PersonMember user, OperationEnvironment env)
                                                                                              throws BizException,
                                                                                              MemberNotExistException;

    /**
     * 根据手机号查询个人会员是否存在
     * @param env
     * @param request
     * @return PersonalMemberResponse
     */
    public PersonMember queryPersonalMemberExist(PersonMember user, OperationEnvironment env)
                                                                                             throws BizException,
                                                                                             MemberNotExistException;


    /**
     * 根据登录标识查询个人会员是否存在
     * @param identityNo
     * @param platformType
     * @param env
     * @return
     * @throws BizException
     * @throws MemberNotExistException
     */
    public BaseMember queryPersonalMemberExist(String identityNo, String platformType, OperationEnvironment env)
                                                                                             throws BizException,
                                                                                             MemberNotExistException;

    /**
     * 根据登录标识查询个人会员是否存在
     * @param identityNo
     * @param platformType
     * @param request
     * @return String
     */
    public String existsMember(String identityNo, String platformType, OperationEnvironment env)
                                                                             	throws BizException,
                                                                                MemberNotExistException;

    /**
     * 查询企业会员
     * @param user
     * @param env
     * @return
     * @throws BizException
     * @throws MemberNotExistException
     */
    public EnterpriseMember queryCompanyMember(EnterpriseMember user, OperationEnvironment env)
                                                                                               throws BizException,
                                                                                               MemberNotExistException;
    /**
     * 填充会员真实姓名(明文和ticket),掩码不填充
     * @param user
     * @param env
     */
    public void fillMemberRealName(PersonMember user, OperationEnvironment env) throws BizException;

    /**
     * 查询手机密文
     * @param memberId
     * @param base
     * @throws BizException
     */
    public String getMobileTicket(String memberId, OperationEnvironment env) throws BizException;

    /**
     * 查询银行卡密文
     * @param memberId
     * @param env
     * @throws BizException
     */
    public String getBankCardPlaintext(String memberId, OperationEnvironment env)
                                                                                 throws BizException;

    /**
     * 个人会员集成创建
     * @param env
     * @param request
     * @return Response
     */
    public MemberAndAccount createIntegratedPersonalMember(PersonMember user,
                                                           OperationEnvironment env)
                                                                                    throws BizException;
    /**
     * 创建企业会员
     * @param enterprise
     * @param env
     * @return
     * @throws BizException
     */
    public MemberAndAccount createEnterpriseMember(EnterpriseMember enterprise,
                                                   OperationEnvironment env) throws BizException;

    /**
     * 个人会员激活
     * @param req
     * @return BankAccountResponse
     */
    public boolean activatePersonalMemberInfo(PersonMember person, OperationEnvironment env)
                                                                                            throws BizException;

    /**
     * 企业会员激活
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public boolean activateCompanyMember(EnterpriseMember enterprise, OperationEnvironment env)
                                                                                               throws BizException;

    /**
     * 解密会员信息
     * @param memberId
     * @param t
     * @param f
     * @param base
     * @return
     */
    public EncryptData decipherMember(String memberId, DeciphedType t, DeciphedQueryFlag f,
                                      OperationEnvironment base) throws BizException;


    /**
     * 根据memberId查询商户
    * @Title: queryMerchantByMemberId
    * @param memberId
    * @throws BizException
    * @return MerchantListResponse
    * @throws
     */
    public MerchantListResponse queryMerchantByMemberId(String memberId) throws BizException;


    /**
     * 根据memberIdentity 查询用户
    * @Title: queryMemberIntegratedInfo
    * @param  memberIdentity
    * @throws BizException
    * @return String
    * @throws
     */
    public String queryMemberIntegratedInfo(String memberIdentity, OperationEnvironment environment) throws BizException;

	/**
	 * 企业会员信息设置
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public boolean setCompanyMember(EnterpriseMember enterprise, OperationEnvironment env) throws BizException;

	/**
	 * 企业信息查询
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public CompanyMemberInfo queryCompanyInfo(EnterpriseMember enterprise, OperationEnvironment env)
			throws BizException;

	/**
	 * 添加商户
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public boolean createMerchant(EnterpriseMember enterprise, OperationEnvironment env) throws BizException;

	/**
	 * 创建企业会员
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public MemberAndAccount createIntegratedCompanyMember(EnterpriseMember enterprise, CompanyInfo comInfo,
			MerchantInfo merInfo, OperationEnvironment env) throws BizException;

	/**
	 * 根据账户名查询会员信息
	 * 
	 * @param loginName
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public BaseMember queryMemberByName(String loginName, OperationEnvironment env) throws BizException,
			MemberNotExistException;

	/**
	 * 个人会员信息设置
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public boolean setPersonalMemberInfo(PersonMember person, OperationEnvironment env) throws BizException;
	
	/**
	 * 创建会员
	 * @param personMember
	 * @param env
	 * @return
	 */
	public boolean createMemberInfo(PersonMember personMember, OperationEnvironment env);
	
	/**
	 * 更新会员实名认证等级
	 * 
	 * @param memberId
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public boolean updateMemberVerifyLevel(String memberId, String level, OperationEnvironment env) throws BizException;

	/**
	 * 查询会员实名认证等级
	 * 
	 * @param memberId
	 * @param env
	 * @return
	 * @throws BizException
	 */
	MemberVerifyLevelResponse getMemberVerifyLevel(String memberId, OperationEnvironment env) throws BizException;
	
	 /**
     * 查询所有认证商户
    * @Title: queryMerchantByMemberId
    * @param memberId
    * @throws BizException
    * @return MerchantListResponse
    * @throws
     */
    public MerchantListResponse queryVerfiedMerchants() throws BizException;


}
