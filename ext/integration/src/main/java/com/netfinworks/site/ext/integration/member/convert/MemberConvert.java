package com.netfinworks.site.ext.integration.member.convert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.netfinworks.ma.service.base.model.CompanyInfo;
import com.netfinworks.ma.service.base.model.VerifyInfo;
import com.netfinworks.ma.service.request.AccountQueryRequest;
import com.netfinworks.ma.service.request.ActivateCompanyRequest;
import com.netfinworks.ma.service.request.ActivatePersonalRequest;
import com.netfinworks.ma.service.request.CompanyMemberQueryRequest;
import com.netfinworks.ma.service.request.CreateMemberInfoRequest;
import com.netfinworks.ma.service.request.IntegratedPersonalRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.request.MemberIntegratedRequest;
import com.netfinworks.ma.service.request.PersonalMemberInfoRequest;
import com.netfinworks.ma.service.request.PersonalMemberQueryRequest;
import com.netfinworks.ma.service.request.PersonalMemberRequest;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.IdentityType;
import com.netfinworks.site.domain.enums.LoginNamePlatformTypeEnum;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.NewPlatformTypeEnum;
import com.netfinworks.site.domain.enums.VerifyStatus;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.ues.util.UesUtils;

/**
 *
 * <p>会员转换</p>
 * @author qinde
 * @version $Id: MemberConvert.java, v 0.1 2013-12-13 下午1:15:34 qinde Exp $
 */
public class MemberConvert {

    /**
     * 创建集成个人用户的request
     * @return
     */
    public static IntegratedPersonalRequest createIntegratedPersonalRequest(PersonMember user) {
        IntegratedPersonalRequest request = new IntegratedPersonalRequest();

//        request.setPlatformUserId("uid_"+user.getPlateFormId());//外部平台用户标识
        PersonalMemberRequest personRequest = new PersonalMemberRequest();
        personRequest.setLoginName(user.getLoginName());
        personRequest.setMemberName(user.getMemberName());
		personRequest.setRealName(user.getStrRealName());
       // personRequest.setLoginPassword(UesUtils.hashSignContent(user.getLoginPasswd()));
        List<VerifyInfo> verifys = new ArrayList<VerifyInfo>();
        VerifyInfo verifyInfo = new VerifyInfo();
        request.setPlatformType(NewPlatformTypeEnum.KJT.getCode().toString());
        personRequest.setLoginNamePlatformType(LoginNamePlatformTypeEnum.KJT.getCode().toString());
        
        if (IdentityType.EMAIL.getCode().equals(user.getMemberIdentity())) {
        	personRequest.setLoginNameType(IdentityType.EMAIL.getInsCode());
        	verifyInfo.setVerifyType(VerifyType.EMAIL.getCode());
        	verifyInfo.setVerifyEntity(user.getEmail());
        	verifyInfo.setStatus(VerifyStatus.YES.getCode());
        	verifyInfo.setVerifiedTime(new Date());
        	verifys.add(verifyInfo);
        	request.setVerifys(verifys);
        } else if (IdentityType.MOBILE.getCode().equals(user.getMemberIdentity())) {
        	personRequest.setLoginNameType(IdentityType.MOBILE.getInsCode());
        	verifyInfo.setVerifyType(VerifyType.CELL_PHONE.getCode());
        	verifyInfo.setVerifyEntity(user.getMobile());
        	verifyInfo.setStatus(VerifyStatus.YES.getCode());
        	verifyInfo.setVerifiedTime(new Date());
        	verifys.add(verifyInfo);
        	request.setVerifys(verifys);
        }
        
        request.setPersonalRequest(personRequest);
        return request;
    }

    /**
     * 内部请求：创建企业会员
     * @param info
     * @return
     */
    public static CreateMemberInfoRequest createMemberInfoRequest(EnterpriseMember info){
    	CreateMemberInfoRequest req = new CreateMemberInfoRequest();
    	req.setLoginName(info.getLoginName());
    	req.setLoginNameType(IdentityType.EMAIL.getInsCode());  // 为email
    	req.setLoginNamePlatformType(LoginNamePlatformTypeEnum.KJT.getCode().toString());  // 1
		//req.setLoginPassword(UesUtils.hashSignContent(info.getLoginPasswd()));
    	//plateFormId为companyId
//    	req.setPlatformUserId(info.getPlateFormId()+"1");
    	//企业用户使用companyId与ma 会员关联
    	req.setPlatformType(NewPlatformTypeEnum.KJT.getCode().toString());
    	req.setMemberType(new Integer(MemberType.ENTERPRISE.getCode()));
    	req.setMemberName(info.getMemberName());
    	return req;
    }
    /**
     * 创建个人会员查询的request
     *
     * @return
     */
    public static PersonalMemberQueryRequest createPersonalMemberQueryRequest(String memberId) {
        PersonalMemberQueryRequest request = new PersonalMemberQueryRequest();
        request.setMemberId(memberId);
        return request;
    }

    /**
     * 创建会员ID查询的request
     *
     * @return
     */
    public static MemberIntegratedIdRequest createMemberIntegratedIdRequest(String memberId) {
        MemberIntegratedIdRequest request = new MemberIntegratedIdRequest();
        request.setMemberId(memberId);
        request.setRequireDefaultOperator(true); //查默认操作员
        request.setRequireVerifyInfos(false);//认证
        return request;
    }
    /**
     * 创建会员综合查询的request
     *
     * @return
     */
    public static MemberIntegratedRequest createMemberIntegratedRequest(String memberIdentity,IdentityType IdenType,MemberType memberType) {
        MemberIntegratedRequest request = new MemberIntegratedRequest();
        request.setMemberIdentity(memberIdentity);
        request.setRequireDefaultOperator(true); //查默认操作员
        request.setPlatformType(IdenType.getPlatformType());
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(true);
        List<Long> accountTypes = new ArrayList<Long>(1);
        Long accountTypeL = null;
        try {
        	accountTypeL = Long.valueOf(memberType.getCode());
        	accountTypes.add(accountTypeL);
		} catch (Exception e) {
			accountTypeL = null;
		}
        request.setAccountRequest(accountRequest);
        request.setRequireVerifyInfos(true);//认证
        return request;
    }
    
    public static MemberIntegratedIdRequest createMemberIntegratedIdRequest(String memberId,MemberType memberType) {
    	MemberIntegratedIdRequest request=new MemberIntegratedIdRequest();
        request.setMemberId(memberId);
        request.setRequireDefaultOperator(true); //查默认操作员
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(true);
        List<Long> accountTypes = new ArrayList<Long>(1);
        Long accountTypeL = null;
        try {
        	accountTypeL = Long.valueOf(memberType.getCode());
        	accountTypes.add(accountTypeL);
		} catch (Exception e) {
			accountTypeL = null;
		}
        request.setAccountRequest(accountRequest);
        request.setRequireVerifyInfos(true);//认证
        return request;
    }
    


    /**
     * 创建根据手机号查询会员是否存在的request
     *
     * @return
     */
    public static MemberIntegratedRequest mobileMemberIntegratedRequest(String mobile,String memberIdentity) {
    	MemberIntegratedRequest request = new MemberIntegratedRequest();
    	request.setMemberIdentity(mobile);
    	request.setRequireDefaultOperator(true); //不查默认操作员
    	if ("MOBILE".equals(memberIdentity)) {
    		request.setPlatformType(IdentityType.MOBILE.getPlatformType());
    	} else if ("EMAIL".equals(memberIdentity)) {
    		request.setPlatformType(IdentityType.EMAIL.getPlatformType());
    	}
    	AccountQueryRequest accountRequest = new AccountQueryRequest();
    	accountRequest.setRequireAccountInfos(true);
    	request.setAccountRequest(accountRequest);
    	request.setRequireVerifyInfos(true);//认证
        return request;
    }

    /**
     * 创建激活个人用户的request
     *
     * @return
     */
    public static ActivatePersonalRequest createActivatePersonalRequest(PersonMember person) {
        ActivatePersonalRequest request = new ActivatePersonalRequest();
        request.setActivateAccount(true);
        if (null != person.getPaypasswd()) {
        	request.setPayPassword(UesUtils.hashSignContent(person.getPaypasswd()));
        }
        PersonalMemberInfoRequest pRequest = new PersonalMemberInfoRequest();
        pRequest.setMemberId(person.getMemberId());
        pRequest.setInvitCode(person.getInvitCode());
        request.setPersonalMemberInfo(pRequest);
        return request;
    }
    /**
     * 创建激活企业用户的request
     *
     * @return
     */
    public static ActivateCompanyRequest activateEnterpriseRequest(EnterpriseMember req) {
        ActivateCompanyRequest request = new ActivateCompanyRequest();
        request.setActivateAccount(true);
        if (null != req.getPaypasswd()) {
			request.setPayPassword(UesUtils.hashSignContent(req.getPaypasswd()));
		}
        request.setMemberId(req.getMemberId());
        return request;
    }

	public static CompanyMemberQueryRequest createCompanyMemberQueryRequest(EnterpriseMember user) {
		if (user == null) {
			return null;
		}
		CompanyMemberQueryRequest request = new CompanyMemberQueryRequest();
		BeanUtils.copyProperties(user, request);
		return request;
	}

	/**
	 * 企业会员设置信息
	 * 
	 * @param info
	 * @return
	 */
	public static CompanyInfo createCompanyInfo(EnterpriseMember info) {
		CompanyInfo req = new CompanyInfo();
		req.setCompanyName(info.getCompanyName());
		req.setWebsite(info.getWebsite());
		req.setAddress(info.getAddress());
		req.setLicenseNo(info.getLicenseNo());
		req.setLicenseAddress(info.getLicenseAddress());
		req.setLicenseExpireDate(info.getLicenseExpireDate());
		req.setBusinessScope(info.getBusinessScope());
		req.setTelephone(info.getTelephone());
		req.setOrganizationNo(info.getOrganizationNo());
		req.setSummary(info.getSummary());
		req.setLegalPerson(info.getLegalPerson());
		req.setLegalPersonPhone(info.getLegalPersonPhone());
		req.setMemberId(info.getMemberId());
		req.setMemberName(info.getMemberName());
		req.setCompanyType(info.getCompanyType());
		req.setLicenseNoPath(info.getLicenseNoPath());
		req.setOrganizationNoPath(info.getOrganizationNoPath());
		req.setTaxNoPath(info.getTaxNoPath());
		req.setIcpLicensePath(info.getIcpLicensePath());
		req.setBusinessWebsite(info.getBusinessWebsite());
		req.setLegalPersonIdValidDate(info.getLegalPersonIdValidDate());
		req.setCompanyType(info.getCompanyType());
		req.setLicenseName(info.getLicenseName());
		req.setProxyPerson(info.getProxyPerson());
		return req;
	}

	/**
	 * 企业会员设置信息
	 * 
	 * @param info
	 * @return
	 */
	public static PersonalMemberInfoRequest createPersonalMemberInfoRequest(PersonMember info) {
		PersonalMemberInfoRequest req = new PersonalMemberInfoRequest();

		req.setMemberId(info.getMemberId());
		req.setRealName(info.getStrRealName());
		req.setMemberName(info.getMemberName());

		return req;
	}

}
