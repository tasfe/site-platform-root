package com.netfinworks.site.ext.integration.member.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.ma.service.base.model.CompanyInfo;
import com.netfinworks.ma.service.base.model.DecipherItem;
import com.netfinworks.ma.service.base.model.DecipherResult;
import com.netfinworks.ma.service.base.model.MerchantInfo;
import com.netfinworks.ma.service.base.model.Operator;
import com.netfinworks.ma.service.base.model.VerifyInfo;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.facade.IMerchantFacade;
import com.netfinworks.ma.service.facade.IPayPwdFacade;
import com.netfinworks.ma.service.request.AccountQueryRequest;
import com.netfinworks.ma.service.request.ActivateCompanyRequest;
import com.netfinworks.ma.service.request.ActivatePersonalRequest;
import com.netfinworks.ma.service.request.CompanyMemberQueryRequest;
import com.netfinworks.ma.service.request.CreateMemberInfoRequest;
import com.netfinworks.ma.service.request.DecipherInfoRequest;
import com.netfinworks.ma.service.request.IntegratedCompanyRequest;
import com.netfinworks.ma.service.request.IntegratedPersonalRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.request.MemberIntegratedRequest;
import com.netfinworks.ma.service.request.MerchantAddRequest;
import com.netfinworks.ma.service.request.MerchantQueryRequest;
import com.netfinworks.ma.service.request.PersonalMemberInfoRequest;
import com.netfinworks.ma.service.request.PersonalMemberQueryRequest;
import com.netfinworks.ma.service.request.UpdateMemberVerifyLevelRequest;
import com.netfinworks.ma.service.request.ValidatePayPwdRequest;
import com.netfinworks.ma.service.response.AccountInfo;
import com.netfinworks.ma.service.response.ActivateCompanyResponse;
import com.netfinworks.ma.service.response.ActivatePersonalResponse;
import com.netfinworks.ma.service.response.BaseMemberInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.ma.service.response.CompanyMemberResponse;
import com.netfinworks.ma.service.response.CreateMemberInfoResponse;
import com.netfinworks.ma.service.response.DecipherInfoResponse;
import com.netfinworks.ma.service.response.IdentityInfo;
import com.netfinworks.ma.service.response.IntegratedCompanyResponse;
import com.netfinworks.ma.service.response.IntegratedPersonalResponse;
import com.netfinworks.ma.service.response.MemberIntegratedResponse;
import com.netfinworks.ma.service.response.MemberVerifyLevelResponse;
import com.netfinworks.ma.service.response.MerchantAddResponse;
import com.netfinworks.ma.service.response.MerchantListResponse;
import com.netfinworks.ma.service.response.PersonalMemberResponse;
import com.netfinworks.ma.service.response.ValidatePayPwdResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.AccountType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.IdentityType;
import com.netfinworks.site.domain.enums.LoginNamePlatformTypeEnum;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.NewPlatformTypeEnum;
import com.netfinworks.site.domain.enums.PayPwdSet;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.member.CertService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.convert.MemberConvert;
import com.netfinworks.site.ext.integration.member.convert.PaypasswdConvert;
import com.netfinworks.ues.util.UesUtils;

/**
 *
 * <p>
 * 会员查询接口
 * </p>
 *
 * @author qinde
 * @version $Id: MemberServiceImpl.java, v 0.1 2013-12-6 上午10:37:03 qinde Exp $
 */
@Service("memberService")
public class MemberServiceImpl extends ClientEnvironment implements
		MemberService {

	private Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);
	@Resource(name = "memberFacade")
	private IMemberFacade memberFacade;

	@Resource(name = "payPwdFacade")
	private IPayPwdFacade payPwdFacade;

	@Resource(name = "certService")
	private CertService certService;

	@Resource(name = "merchantFacade")
	private IMerchantFacade merchantFacade;

	/**
	 * 根据ID会员信息
	 *
	 * @param user
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws MemberNotExistException
	 */
	@Override
	public BaseMember queryMemberById(String memberId, OperationEnvironment env)
			throws BizException, MemberNotExistException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员信息，请求参数：{}", memberId);
				beginTime = System.currentTimeMillis();
			}
		
			MemberIntegratedIdRequest request = MemberConvert
					.createMemberIntegratedIdRequest(memberId);
			MemberIntegratedResponse response = memberFacade
					.queryMemberIntegratedInfoById(env, request);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("根据ID远程查询会员， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				BaseMember baseMember = new BaseMember();
				BaseMemberInfo member = response.getBaseMemberInfo();
				Operator operator=response.getDefaultOperator();
				baseMember.setMemberId(member.getMemberId());
				baseMember.setMemberName(member.getMemberName());
				baseMember.setMemberType(MemberType.getByCode(member
						.getMemberType()));
				baseMember.setOperatorId(operator.getOperatorId());
				baseMember
						.setStatus(MemberStatus.getByCode(member.getStatus()));
				baseMember.setLockStatus(MemberLockStatus.getByCode(member
						.getLockStatus()));
				if (member.getIdentitys().size() > 0) {
					baseMember.setLoginName(member.getIdentitys().get(0)
							.getIdentity());
				}
				return baseMember;
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else {
				logger.error("根据ID查询会员 {}信息异常:返回信息:{},{}", memberId,
						response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else if (e instanceof MemberNotExistException) {
				throw (MemberNotExistException) e;
			} else {
				logger.error("根据ID查询会员 {}信息异常:异常信息{}", memberId,
						e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	/**
	 * 查询集成会员
	 *
	 * @param env
	 * @param request
	 * @return PersonalMemberResponse
	 */
	@Override
	public PersonMember queryMemberIntegratedInfo(PersonMember user,
			OperationEnvironment env) throws BizException,
			MemberNotExistException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员信息，请求参数：{}", user);
				beginTime = System.currentTimeMillis();
			}
			MemberIntegratedRequest memberIntegratedRequest = null;
			memberIntegratedRequest = MemberConvert
					.createMemberIntegratedRequest(user.getLoginName(),
							IdentityType.EMAIL, MemberType.PERSONAL);
			MemberIntegratedResponse response = memberFacade
					.queryMemberIntegratedInfo(env, memberIntegratedRequest);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询会员余额， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			PersonMember personMember = new PersonMember();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				BaseMemberInfo member = response.getBaseMemberInfo();
				personMember.setOperatorId(response.getDefaultOperator()
						.getOperatorId());
				personMember.setMemberId(member.getMemberId());
				personMember.setMemberName(member.getMemberName());
				personMember.setMemberType(MemberType.getByCode(member
						.getMemberType()));
				if ((null != member.getIdentitys())
						&& (null != member.getIdentitys().get(0))) {
					personMember.setPlateFormId(member.getIdentitys().get(0)
							.getPlatformType());
					personMember.setPlatformType(member.getIdentitys().get(0)
							.getPlatformType());
				}
				List<VerifyInfo> verifyList = response.getVerifyInfos();
				if ((verifyList != null) && (verifyList.size() > 0)) {
					for (VerifyInfo info : verifyList) {
						// if (info.getVerifyType().equals(
						// VerifyType.ID_CARD.getCode())) {
						// if (info.getStatus() == VerifyStatus.YES.getCode()) {
						// personMember
						// .setNameVerifyStatus(AuthResultStatus.PASS);
						// if (logger.isInfoEnabled()) {
						// logger.info("会员  {} 实名认证已通过",
						// member.getMemberId());
						// }
						//
						// continue;
						//
						// }
						// }
						if (info.getVerifyType().equals(
								VerifyType.CELL_PHONE.getCode())) {
							// personMember.setMobile(decipherMember(member.getMemberId(),
							// DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL,
							// env).getPlaintext());
							personMember.setMobile(info.getVerifyEntity());
							personMember.setMobileStar(info.getVerifyEntity());
						}
						if (info.getVerifyType().equals(
								VerifyType.EMAIL.getCode())) {
							// personMember.setEmail(decipherMember(member.getMemberId(),
							// DeciphedType.EMAIL, DeciphedQueryFlag.ALL,
							// env).getPlaintext());
							personMember.setEmail(info.getVerifyEntity());
							personMember.setEmailStar(info.getVerifyEntity());
						}
					}

				}
				// 查询实名认证状态
				// if (personMember.getNameVerifyStatus() == null) {
				// AuthInfoRequest info = new AuthInfoRequest();
				// info.setMemberId(personMember.getMemberId());
				// info.setAuthType(AuthType.REAL_NAME);
				// info.setOperator(personMember.getOperatorId());
				// AuthInfo status = certService.queryRealName(info);
				// if (status == null) { // 未认证
				// personMember
				// .setNameVerifyStatus(AuthResultStatus.NOT_FOUND);
				// } else {
				// logger.info("会员  {} 实名认证审核:", status.getResult()
				// .getCode());
				// personMember.setNameVerifyStatus(AuthResultStatus
				// .getByCode(status.getResult().getCode()));
				// }
				// }

				// 获取账户激活状态
				personMember.setStatus(MemberStatus.getByCode(member
						.getStatus()));
				// 获取账户锁定状态
				personMember.setLockStatus(MemberLockStatus.getByCode(member
						.getLockStatus()));

				// 查询缺省账户
				List<AccountInfo> accounts = response.getAccountInfos();
				if ((accounts != null) && (accounts.size() > 0)) {
					personMember.setDefaultAccountId(accounts.get(0)
							.getAccountId());
					// 验证支付密码设置
					personMember.setPaypasswdstatus(checkPasswd(personMember));
				} else {
					personMember
							.setPaypasswdstatus(MemberPaypasswdStatus.NOT_SET_PAYPASSWD);
				}
				PersonalMemberQueryRequest personalMemberQueryRequest = MemberConvert
						.createPersonalMemberQueryRequest(member.getMemberId());
				PersonalMemberResponse personalMemberResponse = memberFacade
						.queryPersonalMember(env, personalMemberQueryRequest);
				if(personalMemberResponse
                        .getPersonalMemberInfo()!=null){
    				personMember.setLoginName(personalMemberResponse
    						.getPersonalMemberInfo().getDefaultLoginName());
    				personMember.setMemberIdentity(personalMemberResponse
    						.getPersonalMemberInfo().getDefaultLoginName());
    				EncryptData realName = new EncryptData();
    				if (ResponseCode.SUCCESS.getCode().equals(responseCode)) {
    					// 直接查询的是掩码
    					realName.setMask(personalMemberResponse
    							.getPersonalMemberInfo().getRealName());
    				}
    				personMember.setRealName(realName);
    				fillMemberRealName(personMember, env);
				}
				// 2014-05-31 update by zhangyun.m( begin)
				List<IdentityInfo> identityList = member.getIdentitys();
				String pformId = user.getPlateFormId();
				if (pformId != null) {
					for (IdentityInfo info : identityList) {
						if (pformId.equals(info.getPlatformType())) {
							personMember.setMemberIdentity(info.getIdentity());
							personMember.setPlateFormId(pformId);
							personMember.setPlatformType(pformId);
							break;
						}
					}
				}
				// 2014-05-31 update by zhangyun.m( end )
				return personMember;
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else {
				logger.error("查询会员 {}信息异常:返回信息:{},{}", user.getMobile(),
						response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else if (e instanceof MemberNotExistException) {
				throw (MemberNotExistException) e;
			} else {
				logger.error("查询会员 {}信息异常:异常信息{}", user.getMobile(),
						e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}

	}

	/**
	 * 根据手机号查询个人会员是否存在
	 *
	 * @param env
	 * @param request
	 * @return PersonalMemberResponse
	 */
	@Override
	public PersonMember queryPersonalMemberExist(PersonMember user,
			OperationEnvironment env) throws BizException,
			MemberNotExistException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员是否信息，请求参数：{}", user.getMobile());
				beginTime = System.currentTimeMillis();
			}
			MemberIntegratedRequest memberIntegratedRequest = null;
			if ("MOBILE".equals(user.getMemberIdentity())) {

				memberIntegratedRequest = MemberConvert
						.mobileMemberIntegratedRequest(user.getMobile(),
								user.getMemberIdentity());
			} else if ("EMAIL".equals(user.getMemberIdentity())) {
				memberIntegratedRequest = MemberConvert
						.mobileMemberIntegratedRequest(user.getEmail(),
								user.getMemberIdentity());
			}
			MemberIntegratedResponse response = memberFacade
					.queryMemberIntegratedInfo(env, memberIntegratedRequest);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询会员信息， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				BaseMemberInfo member = response.getBaseMemberInfo();
				user.setOperatorId(response.getDefaultOperator()
						.getOperatorId());
				user.setMemberId(member.getMemberId());
				user.setMemberName(member.getMemberName());
				user.setMemberType(MemberType.getByCode(member.getMemberType()));
				user.setStatus(MemberStatus.getByCode(member.getStatus()));
				user.setLockStatus(MemberLockStatus.getByCode(member
						.getLockStatus()));
//				List<VerifyInfo> verifyList = response.getVerifyInfos();
//
//				// 查询是否实名认证
//				if ((verifyList != null) && (verifyList.size() > 0)) {
//					for (VerifyInfo info : verifyList) {
						// if (info.getVerifyType().equals(
						// VerifyType.ID_CARD.getCode())) {
						// if (info.getStatus() == VerifyStatus.YES.getCode()) {
						// user.setNameVerifyStatus(AuthResultStatus.PASS);
						// if (logger.isInfoEnabled()) {
						// logger.info("会员  {} 实名认证已通过",
						// member.getMemberId());
						// }
						// break;
						// } /*
						// * //ma中无会员认证状态时，去认证服务查询 else {
						// * user.setNameVerifyStatus(AuthResultStatus.INIT);
						// * if (logger.isInfoEnabled()) {
						// * logger.info("会员  {} 实名认证审核中",
						// * member.getMemberId()); } }
						// */
						// }
//					}
//
//				}
				// if (user.getNameVerifyStatus() == null) {
				// user.setNameVerifyStatus(AuthResultStatus.NOT_FOUND);
				// }
				List<AccountInfo> accounts = response.getAccountInfos();
				if ((accounts != null) && (accounts.size() > 0)) {
					user.setDefaultAccountId(accounts.get(0).getAccountId());
				}
				return user;
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else {
				logger.error("根据手机查询会员 {}是否存在异常:返回信息:{},{}", user.getMobile(),
						response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else if (e instanceof MemberNotExistException) {
				throw (MemberNotExistException) e;
			} else {
				logger.error("查询会员 {}信息异常:请求信息", user.getMobile(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}

	}

	/**
	 * 查询企业会员
	 *
	 * @param env
	 * @param request
	 * @return PersonalMemberResponse
	 */
	@Override
	public EnterpriseMember queryCompanyMember(EnterpriseMember user,
			OperationEnvironment env) throws BizException,
			MemberNotExistException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员信息，请求参数：{}", user);
				beginTime = System.currentTimeMillis();
			}

			MemberIntegratedResponse response = null;
			if ((null != user) && StringUtil.isNotEmpty(user.getMemberId())) {
				MemberIntegratedIdRequest request = MemberConvert.createMemberIntegratedIdRequest(user.getMemberId(),
						user.getMemberType());
				response = memberFacade.queryMemberIntegratedInfoById(env,
						request);

			} else {
				MemberIntegratedRequest memberIntegratedRequest = MemberConvert.createMemberIntegratedRequest(
						user.getLoginName(), IdentityType.EMAIL, user.getMemberType());
				response = memberFacade.queryMemberIntegratedInfo(env,
						memberIntegratedRequest);
			}

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询会员余额， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			EnterpriseMember enterpriseMember = new EnterpriseMember();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				enterpriseMember.setOperator_login_name(user.getOperator_login_name());
				enterpriseMember.setCurrentOperatorId(user.getCurrentOperatorId());
				BaseMemberInfo member = response.getBaseMemberInfo();
				enterpriseMember.setOperatorId(response.getDefaultOperator()
						.getOperatorId());
				enterpriseMember.setMemberId(member.getMemberId());
				enterpriseMember.setMemberName(member.getMemberName());
				List<VerifyInfo> verifyList = response.getVerifyInfos();
				if ((verifyList != null) && (verifyList.size() > 0)) {
					for (VerifyInfo info : verifyList) {
						// if (info.getVerifyType().equals(
						// VerifyType.ID_CARD.getCode())) {
						// if (info.getStatus() == VerifyStatus.YES.getCode()) {
						// enterpriseMember
						// .setNameVerifyStatus(AuthResultStatus.PASS);
						// if (logger.isInfoEnabled()) {
						// logger.info("会员  {} 实名认证已通过",
						// member.getMemberId());
						// }
						// continue;
						// }
						// }
						if (info.getVerifyType().equals(
								VerifyType.CELL_PHONE.getCode())) {
							enterpriseMember.setMobile(info.getVerifyEntity());
							enterpriseMember.setMobileStar(info.getVerifyEntity());
						}
						if (info.getVerifyType().equals(
								VerifyType.EMAIL.getCode())) {
							enterpriseMember.setEmail(info.getVerifyEntity());
							enterpriseMember.setEmailStar(info.getVerifyEntity());
						}
					}

				}
				/*
				 * // 查询实名认证状态
				 * if (enterpriseMember.getNameVerifyStatus() == null) {
				 * AuthInfoRequest info = new AuthInfoRequest();
				 * info.setMemberId(enterpriseMember.getMemberId());
				 * info.setAuthType(AuthType.REAL_NAME);
				 * info.setOperator(enterpriseMember.getOperatorId());
				 * AuthInfo status = certService.queryRealName(info);
				 * if (status == null) { // 未认证
				 * enterpriseMember
				 * .setNameVerifyStatus(AuthResultStatus.NOT_FOUND);
				 * } else {
				 * logger.info("会员  {} 实名认证审核:", status.getResult()
				 * .getCode());
				 * enterpriseMember.setNameVerifyStatus(AuthResultStatus
				 * .getByCode(status.getResult().getCode()));
				 * }
				 * }
				 */
				if ((null != member.getIdentitys())
						&& (null != member.getIdentitys().get(0))) {
					if (StringUtils.isNotEmpty(user.getLoginName())) {
						enterpriseMember.setLoginName(user.getLoginName());
					} else {
						enterpriseMember.setLoginName(member.getIdentitys().get(0)
								.getIdentity());
					}
					enterpriseMember.setPlateFormId(member.getIdentitys()
							.get(0).getPlatformType());
				}
				enterpriseMember.setMemberType(MemberType.getByCode(member
						.getMemberType()));
				// 获取账户激活状态
				enterpriseMember.setStatus(MemberStatus.getByCode(member
						.getStatus()));
				// 获取账户锁定状态
				enterpriseMember.setLockStatus(MemberLockStatus
						.getByCode(member.getLockStatus()));

				// 查询缺省账户
				List<AccountInfo> accounts = response.getAccountInfos();
				if ((accounts != null) && (accounts.size() > 0)) {
					enterpriseMember.setDefaultAccountId(accounts.get(0)
							.getAccountId());
					// 验证支付密码设置
					enterpriseMember
							.setPaypasswdstatus(checkPasswd(enterpriseMember));
				} else {
					enterpriseMember
							.setPaypasswdstatus(MemberPaypasswdStatus.NOT_SET_PAYPASSWD);
				}
				return enterpriseMember;
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else {
				logger.error("查询企业会员 {}信息异常，返回代码{},返回信息:{}", user.getMobile(),
						responseCode, response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else if (e instanceof MemberNotExistException) {
				throw (MemberNotExistException) e;
			} else {
				logger.error("查询企业会员 {}信息异常:异常信息", user.getMobile(),
						e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	private MemberPaypasswdStatus checkPasswd(BaseMember user) throws Exception {
		PayPasswdRequest req = new PayPasswdRequest();
		req.setAccountId(user.getDefaultAccountId());
		req.setOperator(user.getOperatorId());
		req.setValidateMode(1);
		ValidatePayPwdRequest request = PaypasswdConvert
				.createValidatePayPwdRequest(req);
		ValidatePayPwdResponse validatePayPwdResponse = payPwdFacade
				.validatePayPwd(getEnv(req.getClientIp()), request);
		if (ResponseCode.SUCCESS.getCode().equals(
				validatePayPwdResponse.getResponseCode())) {
			if (PayPwdSet.SET.equalsByCode(validatePayPwdResponse.getIsSet())) {
				return MemberPaypasswdStatus.DEFAULT;
			} else {
				return MemberPaypasswdStatus.NOT_SET_PAYPASSWD;
			}
		} else {
			logger.error("查询会员是否设置支付密码出错：{}",
					validatePayPwdResponse.getResponseMessage());
			throw new BizException(ErrorCode.SYSTEM_ERROR);
		}
	}

	@Override
	public void fillMemberRealName(PersonMember user, OperationEnvironment base)
			throws BizException {

		EncryptData data = decipherMember(user.getMemberId(),
				DeciphedType.TRUE_NAME, DeciphedQueryFlag.ALL, base);
		if (data != null) {
			EncryptData realName = user.getRealName();
			realName.setCiphertext(data.getCiphertext());
			realName.setPlaintext(data.getPlaintext());
		}
	}

	/**
	 * 获取手机
	 *
	 * @param user
	 * @param base
	 */
	public void fillMemberMobile(PersonMember user, OperationEnvironment base)
			throws BizException {
		EncryptData data = decipherMember(user.getMemberId(),
				DeciphedType.CELL_PHONE, DeciphedQueryFlag.TICKET, base);
		if (data != null) {
			user.setMobileTicket(data.getCiphertext());
		}
	}

	/**
	 * 查询手机密文
	 *
	 * @param memberId
	 * @param base
	 * @throws BizException
	 */
	@Override
	public String getMobileTicket(String memberId, OperationEnvironment base)
			throws BizException {
		EncryptData data = decipherMember(memberId, DeciphedType.CELL_PHONE,
				DeciphedQueryFlag.TICKET, base);
		return data.getCiphertext();
	}

	/**
	 * 查询银行卡密文
	 *
	 * @param memberId
	 * @param base
	 * @throws BizException
	 */
	@Override
	public String getBankCardPlaintext(String memberId,
			OperationEnvironment base) throws BizException {
		EncryptData data = decipherMember(memberId, DeciphedType.BANK_CARD,
				DeciphedQueryFlag.PRIMITIVE, base);
		return data.getPlaintext();
	}

	/**
	 * 解密会员信息
	 *
	 * @param memberId
	 * @param t
	 * @param f
	 * @param base
	 * @return
	 */
	@Override
	public EncryptData decipherMember(String memberId, DeciphedType t,
			DeciphedQueryFlag f, OperationEnvironment base) throws BizException {
		DecipherInfoRequest req = new DecipherInfoRequest();
		req.setMemberId(memberId);
		List<DecipherItem> items = new ArrayList<DecipherItem>(1);
		DecipherItem item = new DecipherItem();
		item.setDecipheredType(t.getCode());
		item.setQueryFlag(f.getCode());
		items.add(item);
		req.setColumnList(items);
		DecipherInfoResponse response = memberFacade.querytMemberDecipherInfo(
				base, req);
		if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
			EncryptData data = new EncryptData();
			List<DecipherResult> restult = response.getDecipheredResult();
			if (CollectionUtils.isNotEmpty(restult)) {
				DecipherResult rest = restult.get(0);
				data.setCiphertext(rest.getTicket());
				data.setPlaintext(rest.getPrimitiveValue());
				return data;
			}
		}
		return null;
	}

	/**
	 * 个人会员集成创建
	 *
	 * @param env
	 * @param request
	 * @return Response
	 */
	/*
	 * @Override public MemberAndAccount
	 * createIntegratedPersonalMember(PersonMember user, OperationEnvironment
	 * env) throws BizException { try { long beginTime = 0L; if
	 * (logger.isInfoEnabled()) { logger.info("创建个人会员，请求参数：{}", user); beginTime
	 * = System.currentTimeMillis(); } MemberAndAccount ma = new
	 * MemberAndAccount(); IntegratedPersonalRequest request =
	 * MemberConvert.createIntegratedPersonalRequest(user);
	 * request.setMemberAccountFlag(1); IntegratedPersonalResponse response =
	 * memberFacade.createIntegratedPersonalMember(env, request); if
	 * (logger.isInfoEnabled()) { long consumeTime = System.currentTimeMillis()
	 * - beginTime; logger.info("远程调用创建个人会员， 耗时:{} (ms); 响应结果:{} ", new Object[]
	 * { consumeTime, response }); }
	 *
	 * if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
	 * ma.setMemberId(response.getMemberId());
	 * ma.setOperatorId(response.getOperatorId());
	 * ma.setAccountId(response.getAccountId()); if (logger.isInfoEnabled()) {
	 * logger.info("创建个人会员成功，请求参数：{}", user); } return ma; } else {
	 * logger.error("创建个人会员异常:请求信息:{},返回信息:{}", user,
	 * response.getResponseMessage()); throw new
	 * BizException(ErrorCode.SYSTEM_ERROR, response.getResponseMessage()); } }
	 * catch (Exception e) { if (e instanceof BizException) { throw
	 * (BizException) e; } else { logger.error("创建个人会员异常:请求信息{},错误信息:{}", user,
	 * e); throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
	 * e.getCause()); } } }
	 */

	/**
	 * 个人会员集成创建
	 *
	 * @param env
	 * @param request
	 * @return Response
	 */
	@Override
	public MemberAndAccount createIntegratedPersonalMember(PersonMember user,
			OperationEnvironment env) throws BizException {

		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("创建个人会员，请求参数：{}", user);
				beginTime = System.currentTimeMillis();
			}
			MemberAndAccount ma = new MemberAndAccount();
			IntegratedPersonalRequest request = MemberConvert
					.createIntegratedPersonalRequest(user);
			request.setMemberAccountFlag(user.getMemberAccountFlag());
			IntegratedPersonalResponse response = memberFacade
					.createIntegratedPersonalMember(env, request);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用创建个人会员， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}

			if (ResponseCode.SUCCESS.getCode().equals(
					response.getResponseCode())) {
				ma.setMemberId(response.getMemberId());
				ma.setOperatorId(response.getOperatorId());
				ma.setAccountId(response.getAccountId());
				if (logger.isInfoEnabled()) {
					logger.info("创建个人会员成功，请求参数：{}", user);
				}
				return ma;
			} else {
				logger.error("创建个人会员异常:请求信息:{},返回信息:{}", user,
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR,
						response.getResponseMessage());
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("创建个人会员异常:请求信息{},错误信息:{}", user, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	/**
	 * 创建企业会员
	 *
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	@Override
	public MemberAndAccount createEnterpriseMember(EnterpriseMember enterprise,
			OperationEnvironment env) throws BizException {
		try {
			long beginTime = 0L;
			MemberAndAccount ma = new MemberAndAccount();
			CreateMemberInfoRequest req = MemberConvert
					.createMemberInfoRequest(enterprise);
			if (logger.isInfoEnabled()) {
				logger.info("创建企业会员，请求参数：{}", req);
				beginTime = System.currentTimeMillis();
			}
			CreateMemberInfoResponse response = memberFacade.createMemberInfo(
					env, req);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用创建企业会员， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if (ResponseCode.SUCCESS.getCode().equals(
					response.getResponseCode())) {
				ma.setMemberId(response.getMemberId());
				ma.setOperatorId(response.getOperatorId());
				enterprise.setMemberId(response.getMemberId());
				if (logger.isInfoEnabled()) {
					logger.info("创建企业会员成功，请求参数：{}", enterprise);
				}
				return ma;
			} else if (ResponseCode.MEMBER_IDENTITY_EXIST.getCode().equals(
					response.getResponseCode())) {
				logger.error("创建企业会员失败：{}，会员标示已存在", enterprise);
				throw new BizException(ErrorCode.IDENTITY_EXIST_ERROR);
			} else {
				logger.error("创建企业会员异常:请求信息:{},返回信息:{}", enterprise,
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("开企业户异常:请求信息{},异常:{}", enterprise, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	/**
	 * 个人会员激活
	 *
	 * @param env
	 * @param request
	 * @return BankAccountResponse
	 */
	@Override
	public boolean activatePersonalMemberInfo(PersonMember person,
			OperationEnvironment env) throws BizException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("激活个人会员，请求参数：{}", person);
				beginTime = System.currentTimeMillis();
			}
			ActivatePersonalRequest request = MemberConvert
					.createActivatePersonalRequest(person);
			logger.info("激活云+注册会员账号，请求参数：{}", request.toString());
			ActivatePersonalResponse response = memberFacade
					.activatePersonalMemberInfo(env, request);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用激活个人会员， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if (ResponseCode.SUCCESS.getCode().equals(
					response.getResponseCode())) {
				if (logger.isInfoEnabled()) {
					logger.info("激活个人会员成功，请求参数：{}", person);
				}
				return true;
			} else {
				logger.error("激活个人会员异常:请求信息:{},返回信息:{}", person,
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR,response.getResponseMessage());
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("激活个人户异常:请求信息{},异常：{}", person, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	/**
	 * 企业会员激活
	 *
	 * @param env
	 * @param request
	 * @return BankAccountResponse
	 */
	@Override
	public boolean activateCompanyMember(EnterpriseMember enterprise,
			OperationEnvironment env) throws BizException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("激活企业会员，请求参数：{}", enterprise);
				beginTime = System.currentTimeMillis();
			}
			ActivateCompanyRequest request = MemberConvert
					.activateEnterpriseRequest(enterprise);
			ActivateCompanyResponse response = memberFacade
					.activateCompanyMember(env, request);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用激活企业会员， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if (ResponseCode.SUCCESS.getCode().equals(
					response.getResponseCode())) {
				if (logger.isInfoEnabled()) {
					logger.info("激活企业会员成功，请求参数：{}", enterprise);
				}
				return true;
			} else {
				logger.error("激活企业户异常:请求信息{},返回信息：{},{}", enterprise,
						response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR,response.getResponseMessage());
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("激活企业户异常:请求信息{},异常：{}", enterprise, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	@Override
	public MerchantListResponse queryMerchantByMemberId(String memberId)
			throws BizException {
		long beginTime = 0L;
		if (logger.isInfoEnabled()) {
			logger.info("远程调用查询企业会员商户，请求参数：{}", memberId);
			beginTime = System.currentTimeMillis();
		}
		OperationEnvironment environment = new OperationEnvironment();
		MerchantQueryRequest request = new MerchantQueryRequest();
		request.setMemberId(memberId);
		MerchantListResponse response = merchantFacade.queryMerchantInfos(
				environment, request);
		if (logger.isInfoEnabled()) {
			long consumeTime = System.currentTimeMillis() - beginTime;
			logger.info("远程调用查询企业会员商户， 耗时:{} (ms); 响应结果:{} ", new Object[] {
					consumeTime, response });
		}
		return response;
	}

	@Override
	public String queryMemberIntegratedInfo(String memberIdentity,
			OperationEnvironment environment) throws BizException {
		long beginTime = 0L;
		if (logger.isInfoEnabled()) {
			logger.info("根据会员类型远程调用查询企业会员商户，请求参数：{}", memberIdentity);
			beginTime = System.currentTimeMillis();
		}
		MemberIntegratedRequest request = new MemberIntegratedRequest();
		request.setMemberIdentity(memberIdentity);
		request.setPlatformType("1");
		request.setRequireVerifyInfos(false);
		AccountQueryRequest accountRequest = new AccountQueryRequest();
		accountRequest.setRequireAccountInfos(false);
		request.setAccountRequest(accountRequest);
		MemberIntegratedResponse response = memberFacade
				.queryMemberIntegratedInfo(environment, request);
		if (logger.isInfoEnabled()) {
			long consumeTime = System.currentTimeMillis() - beginTime;
			logger.info("根据会员类型远程调用查询企业会员商户， 耗时:{} (ms); 响应结果:{} ",
					new Object[] { consumeTime, response });
		}
		if (!ResponseCode.SUCCESS.getCode().equals(
				response.getResponseCode())) {
			if (logger.isInfoEnabled()) {
				logger.info("根据会员类型远程调用查询企业会员商户，请求参数：{}", memberIdentity);
			}
			throw new BizException(ErrorCode.MEMBER_NOT_EXIST, response.getResponseMessage());
		} 
		BaseMemberInfo baseMemberInfo = response.getBaseMemberInfo();
		if (null == baseMemberInfo) {
			logger.error("根据会员类型远程调用查询企业会员商户异常:请求信息{},返回信息：{},{}",
					memberIdentity, response.getResponseCode(),
					response.getResponseMessage());
			throw new BizException(ErrorCode.SYSTEM_ERROR);
		}
		return baseMemberInfo.getMemberId();
	}

	@Override
	public String existsMember(String identityNo, String platformType, OperationEnvironment env)
			throws BizException, MemberNotExistException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员是否信息，请求参数：{}", identityNo);
				beginTime = System.currentTimeMillis();
			}
			MemberIntegratedRequest memberIntegratedRequest = new MemberIntegratedRequest();
			memberIntegratedRequest.setMemberIdentity(identityNo);
			memberIntegratedRequest.setPlatformType(platformType);
			AccountQueryRequest accountRequest = new AccountQueryRequest();
	    	accountRequest.setRequireAccountInfos(true);
	    	memberIntegratedRequest.setAccountRequest(accountRequest);
			memberIntegratedRequest.setRequireDefaultOperator(false);
			memberIntegratedRequest.setRequireVerifyInfos(false);

			MemberIntegratedResponse response = memberFacade
					.queryMemberIntegratedInfo(env, memberIntegratedRequest);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询会员信息， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				BaseMemberInfo member = response.getBaseMemberInfo();

				return member.getMemberId();
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else {
				logger.error("根据手机查询会员 {}是否存在异常:返回信息:{},{}", identityNo,
						response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else if (e instanceof MemberNotExistException) {
				throw (MemberNotExistException) e;
			} else {
				logger.error("查询会员 {}信息异常:请求信息", identityNo, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	@Override
	public BaseMember queryPersonalMemberExist(String identityNo,
			String platformType, OperationEnvironment env) throws BizException,
			MemberNotExistException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员是否信息，请求参数：{}", identityNo);
				beginTime = System.currentTimeMillis();
			}
			MemberIntegratedRequest memberIntegratedRequest = new MemberIntegratedRequest();
			memberIntegratedRequest.setMemberIdentity(identityNo);
			memberIntegratedRequest.setPlatformType(platformType);
			AccountQueryRequest accountRequest = new AccountQueryRequest();
	    	accountRequest.setRequireAccountInfos(true);
	    	memberIntegratedRequest.setAccountRequest(accountRequest);
			memberIntegratedRequest.setRequireDefaultOperator(true);
			memberIntegratedRequest.setRequireVerifyInfos(true);

			MemberIntegratedResponse response = memberFacade
					.queryMemberIntegratedInfo(env, memberIntegratedRequest);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询会员信息， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			
			String responseCode = response.getResponseCode();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				BaseMember user = new BaseMember();
				BaseMemberInfo member = response.getBaseMemberInfo();
				String memberId = member.getMemberId();
				user.setOperatorId(response.getDefaultOperator()
						.getOperatorId());
				user.setMemberId(member.getMemberId());
				user.setMemberName(member.getMemberName());
				user.setMemberType(MemberType.getByCode(member.getMemberType()));
				user.setStatus(MemberStatus.getByCode(member.getStatus()));
				user.setLockStatus(MemberLockStatus.getByCode(member.getLockStatus()));
				
				if (StringUtils.isNotEmpty(memberId) && memberId.startsWith("2")) {
					EnterpriseMember enterpriseMember = new EnterpriseMember();
	                enterpriseMember.setMemberId(memberId);
					CompanyMemberInfo compInfo = this.queryCompanyInfo(enterpriseMember, env);
					user.setMemberName(compInfo.getCompanyName());
				}
				
				
				
				
//				List<VerifyInfo> verifyList = response.getVerifyInfos();
				// 查询是否实名认证
				// if ((verifyList != null) && (verifyList.size() > 0)) {
				// for (VerifyInfo info : verifyList) {
				// if (info.getVerifyType().equals(
				// VerifyType.ID_CARD.getCode())) {
				// if (info.getStatus() == VerifyStatus.YES.getCode()) {
				// user.setNameVerifyStatus(AuthResultStatus.PASS);
				// if (logger.isInfoEnabled()) {
				// logger.info("会员  {} 实名认证已通过", member.getMemberId());
				// }
				// break;
				// }
				// }
				// }
				//
				// }
				// if (user.getNameVerifyStatus() == null) {
				// user.setNameVerifyStatus(AuthResultStatus.NOT_FOUND);
				// }
				List<AccountInfo> accounts = response.getAccountInfos();
				if ((accounts != null) && (accounts.size() > 0)) {
					user.setDefaultAccountId(accounts.get(0).getAccountId());
				}
				return user;
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else {
				logger.error("根据手机查询会员 {}是否存在异常:返回信息:{},{}", identityNo,
						response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else if (e instanceof MemberNotExistException) {
				throw (MemberNotExistException) e;
			} else {
				logger.error("查询会员 {}信息异常:请求信息", identityNo, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),
						e.getCause());
			}
		}
	}

	/**
	 * 企业会员信息设置
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	@Override
	public boolean setCompanyMember(EnterpriseMember enterprise, OperationEnvironment env) throws BizException {
		boolean result = false;
		try {
			CompanyInfo req = MemberConvert.createCompanyInfo(enterprise);

			if (logger.isInfoEnabled()) {
				logger.info("企业会员信息设置，请求参数：{}", req);
			}
			Response response = memberFacade.setCompanyMember(env, req);

			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				if (logger.isInfoEnabled()) {
					logger.info("企业会员信息设置成功，请求参数：{}", enterprise);
				}
				result = true;
			} else {
				logger.error("企业会员信息设置异常:请求信息:{},返回信息:{}", enterprise, response.getResponseMessage());
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("开企业户异常:请求信息{},异常:{}", enterprise, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
		return result;
	}

	/**
	 * 企业信息查询
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	@Override
	public CompanyMemberInfo queryCompanyInfo(EnterpriseMember enterprise, OperationEnvironment env)
			throws BizException {
		CompanyMemberInfo info = new CompanyMemberInfo();
		try {
			CompanyMemberQueryRequest req = MemberConvert.createCompanyMemberQueryRequest(enterprise);

			if (logger.isInfoEnabled()) {
				logger.info("企业会员信息设置，请求参数：{}", req);
			}
			CompanyMemberResponse response = memberFacade.queryCompanyMember(env, req);

			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				return response.getCompanyMemberInfo();
			} else {
				logger.error("企业会员信息设置异常:请求信息:{},返回信息:{}", enterprise, response.getResponseMessage());
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("开企业户异常:请求信息{},异常:{}", enterprise, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
		return info;
		
	}

	/**
	 * 添加商户
	 */
	@Override
	public boolean createMerchant(EnterpriseMember enterprise, OperationEnvironment env)
			throws BizException {
		long beginTime = 0L;
		boolean result = false;
		if (logger.isInfoEnabled()) {
			logger.info("添加商户，请求参数：{}", enterprise);
			beginTime = System.currentTimeMillis();
		}
		MerchantAddRequest request = new MerchantAddRequest();
		request.setMemberId(enterprise.getMemberId());
		request.setMerchantType(0L);// 0：外部商户，1：内部商户，2：关联商户
		request.setMerchantName(enterprise.getEnterpriseName());
		MerchantAddResponse response = merchantFacade.createMerchant(env, request);
		if (logger.isInfoEnabled()) {
			long consumeTime = System.currentTimeMillis() - beginTime;
			logger.info("远程调用查询企业会员商户， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
		}
		if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
			result = true;
		}
		return result;
	}

	/**
	 * 集成创建企业会员
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	@Override
	public MemberAndAccount createIntegratedCompanyMember(EnterpriseMember enterprise, CompanyInfo comInfo,
			MerchantInfo merInfo, OperationEnvironment env) throws BizException {
		try {
			long beginTime = 0L;
			MemberAndAccount ma = new MemberAndAccount();
			IntegratedCompanyRequest req = new IntegratedCompanyRequest();
			req.setMemberType(Integer.parseInt(enterprise.getMemberType().getCode()));
			req.setMemberName(enterprise.getMemberName());
			req.setLoginName(enterprise.getLoginName());
			req.setLoginNameType(IdentityType.EMAIL.getInsCode());
			req.setLoginNamePlatformType(LoginNamePlatformTypeEnum.KJT.getCode().toString());
			req.setLoginPassword(UesUtils.hashSignContent(enterprise.getLoginPasswd()));
			req.setPlatformType(NewPlatformTypeEnum.KJT.getCode().toString());
			req.setCompanyInfo(comInfo);
			req.setMerchantInfo(merInfo);
			req.setMemberAccountFlag(2);// 2-会员账户都激活
			if (logger.isInfoEnabled()) {
				logger.info("集成创建企业会员，请求参数：{}", req);
				beginTime = System.currentTimeMillis();
			}
			IntegratedCompanyResponse response = memberFacade.createIntegratedCompanyMember(env, req);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用创建企业会员， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				ma.setMemberId(response.getMemberId());
				ma.setOperatorId(response.getOperatorId());
				ma.setAccountId(response.getAccountId());
				ma.setMerchantId(response.getMerchantId());
				enterprise.setMemberId(response.getMemberId());
				if (logger.isInfoEnabled()) {
					logger.info("创建企业会员成功，请求参数：{}", enterprise);
				}
				return ma;
			} else if (ResponseCode.MEMBER_IDENTITY_EXIST.getCode().equals(response.getResponseCode())) {
				logger.error("创建企业会员失败：{}，会员标示已存在", enterprise);
				throw new BizException(ErrorCode.IDENTITY_EXIST_ERROR);
			} else {
				logger.error("创建企业会员异常:请求信息:{},返回信息:{}", enterprise, response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("开企业户异常:请求信息{},异常:{}", enterprise, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	/**
	 * 根据账户名查询会员信息
	 * 
	 * @param user
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws MemberNotExistException
	 */
	@Override
	public BaseMember queryMemberByName(String loginName, OperationEnvironment env) throws BizException,
			MemberNotExistException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询会员信息，请求参数：{}", loginName);
				beginTime = System.currentTimeMillis();
			}

			MemberIntegratedRequest request = new MemberIntegratedRequest();
			request.setMemberIdentity(loginName);
			request.setRequireDefaultOperator(true); // 查默认操作员
			request.setRequireVerifyInfos(true);// 认证

			AccountQueryRequest accountRequest = new AccountQueryRequest();
			accountRequest.setRequireAccountInfos(true);
			List<Long> accountTypes = new ArrayList<Long>();
			accountTypes.add(AccountType.PERSONAL_BASE.getCode());
			accountTypes.add(AccountType.ENTERPRISE_BASE.getCode());
			accountRequest.setAccountTypes(accountTypes);

			request.setAccountRequest(accountRequest);
			request.setPlatformType(IdentityType.MOBILE.getPlatformType());

			MemberIntegratedResponse response = memberFacade.queryMemberIntegratedInfo(env, request);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("根据ID远程查询会员， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				BaseMember baseMember = new BaseMember();
				BaseMemberInfo member = response.getBaseMemberInfo();
				baseMember.setMemberId(member.getMemberId());
				baseMember.setMemberName(member.getMemberName());
				baseMember.setMemberType(MemberType.getByCode(member.getMemberType()));
				baseMember.setStatus(MemberStatus.getByCode(member.getStatus()));
				baseMember.setLockStatus(MemberLockStatus.getByCode(member.getLockStatus()));
				if (member.getIdentitys().size() > 0) {
					baseMember.setLoginName(member.getIdentitys().get(0).getIdentity());
				}
				return baseMember;
			} else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {// 用户不存在
				throw new MemberNotExistException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else {
				logger.error("根据ID查询会员 {}信息异常:返回信息:{},{}", loginName, response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else if (e instanceof MemberNotExistException) {
				throw (MemberNotExistException) e;
			} else {
				logger.error("根据ID查询会员 {}信息异常:异常信息{}", loginName, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	/**
	 * 个人会员信息设置
	 * 
	 * @param enterprise
	 * @param env
	 * @return
	 * @throws BizException
	 */
	@Override
	public boolean setPersonalMemberInfo(PersonMember person, OperationEnvironment env) throws BizException {
		boolean result = false;
		try {
			PersonalMemberInfoRequest req = MemberConvert.createPersonalMemberInfoRequest(person);

			if (logger.isInfoEnabled()) {
				logger.info("个人会员信息设置，请求参数：{}", req);
			}
			Response response = memberFacade.setPersonalMemberInfo(env, req);

			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				if (logger.isInfoEnabled()) {
					logger.info("个人会员信息设置成功，请求参数：{}", person);
				}
				result = true;
			} else {
				logger.error("个人会员信息设置异常:请求信息:{},返回信息:{}", person, response.getResponseMessage());
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("个人会员信息设置异常:请求信息{},异常:{}", person, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
		return result;
	}
    
	/**
	 * 创建会员
	 */
	@Override
	public boolean createMemberInfo(PersonMember personMember,
			OperationEnvironment env) {
		long beginTime = 0L;
		boolean result = false;
		if (logger.isInfoEnabled()) {
			logger.info("添加会员，请求参数：{}", personMember);
			beginTime = System.currentTimeMillis();
		}
		CreateMemberInfoRequest request = new CreateMemberInfoRequest();
		request.setMemberType(new Integer(MemberType.PERSONAL.getCode()));
		request.setLoginName(personMember.getLoginName());		
		request.setLoginPassword(UesUtils.hashSignContent(personMember.getLoginPasswd()));
		request.setLoginNamePlatformType(LoginNamePlatformTypeEnum.KJT.getCode().toString());	
		if (IdentityType.EMAIL.getCode().equals(personMember.getMemberIdentity())) {
			request.setLoginNameType(IdentityType.EMAIL.getInsCode());
		} else if (IdentityType.MOBILE.getCode().equals(personMember.getMemberIdentity())) {
			request.setLoginNameType(IdentityType.MOBILE.getInsCode());
		}		
		CreateMemberInfoResponse response=memberFacade.createMemberInfo(env, request);
		
		if (logger.isInfoEnabled()) {
			long consumeTime = System.currentTimeMillis() - beginTime;
			logger.info("远程调用创建会员， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
		}
		if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
			result = true;
		}
		return result;
	}

	/**
	 * 更新会员实名认证等级
	 */
	@Override
	public boolean updateMemberVerifyLevel(String memberId, String level, OperationEnvironment env) throws BizException {
		
		boolean result = false;
		try {
			UpdateMemberVerifyLevelRequest request = new UpdateMemberVerifyLevelRequest();
			request.setMemberId(memberId);
			request.setVefifyLevel(Integer.parseInt(level));
			if (logger.isInfoEnabled()) {
				logger.info("更新会员实名认证等级，请求参数：{}", request);
			}
			Response response = memberFacade.updateMemberVerifyLevel(env, request);

			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				if (logger.isInfoEnabled()) {
					logger.info("更新会员实名认证等级，返回参数：{}", response);
				}
				result = true;
			} else {
				logger.info("更新会员实名认证等级，返回参数：{}", response);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.info("更新会员实名认证等级，返回参数：{}", e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
		return result;
	}

	/**
	 * 查询会员实名认证等级
	 */
	@Override
	public MemberVerifyLevelResponse getMemberVerifyLevel(String memberId, OperationEnvironment env)
			throws BizException {

		MemberVerifyLevelResponse response = new MemberVerifyLevelResponse();
		try {
			if (logger.isInfoEnabled()) {
				logger.info("查询会员实名认证等级，请求参数：{}", memberId);
			}
			response = memberFacade.getMemberVerifyLevel(env, memberId);

			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				if (logger.isInfoEnabled()) {
					logger.info("查询会员实名认证等级，返回参数：{}", response);
				}
				return response;
			} else {
				logger.info("查询会员实名认证等级，返回参数：{}", response);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.info("查询会员实名认证等级，返回参数：{}", e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public MerchantListResponse queryVerfiedMerchants() throws BizException {
		long beginTime = 0L;
		if (logger.isInfoEnabled()) {
			logger.info("远程调用查询企业会员商户");
			beginTime = System.currentTimeMillis();
		}
		OperationEnvironment environment = new OperationEnvironment();
		MerchantQueryRequest request = new MerchantQueryRequest();
		
		MerchantListResponse response = merchantFacade.queryVerfiedMerchants(environment, request);
		if (logger.isInfoEnabled()) {
			long consumeTime = System.currentTimeMillis() - beginTime;
			logger.info("远程调用查询企业会员商户， 耗时:{} (ms); 响应结果:{} ", new Object[] {
					consumeTime, response });
		}
		return response;
	}
   
	
	
}
