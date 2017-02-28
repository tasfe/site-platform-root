/**
 * file name    ConvertObject.java
 * description  
 * @author      tiantao
 * @version     1.0
 * create at    2014年5月29日 下午9:01:11
 * copyright 2013 weihui 
 */
package com.yongda.site.wallet.app.util;

import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;

/**
 * className		ConvertObject    
 * description	
 * @author		Tiantao
 * @version 	2014年5月29日 下午9:01:11
 */
public class ConvertObject {
	
	public static void convert(EnterpriseMember enterpriseMember, PersonMember member) {
		member.setAccount(enterpriseMember.getAccount());
		member.setBankCardCount(enterpriseMember.getBankCardCount());
		member.setCardCount(enterpriseMember.getCardCount());
		member.setDefaultAccountId(enterpriseMember.getDefaultAccountId());
		member.setEmail(enterpriseMember.getEmail());
		member.setEmailStar(enterpriseMember.getEmail());
		member.setFaceImageUrl(enterpriseMember.getFaceImageUrl());
		member.setLastLoginTime(enterpriseMember.getLastLoginTime());
		member.setLockStatus(enterpriseMember.getLockStatus());
		member.setLoginName(enterpriseMember.getLoginName());
		member.setLogoutUrl(enterpriseMember.getLogoutUrl());
		member.setMemberId(enterpriseMember.getMemberId());
		member.setMemberName(enterpriseMember.getMemberName());
		member.setMemberType(enterpriseMember.getMemberType());
		member.setMobile(enterpriseMember.getMobile());
		member.setMobileStar(enterpriseMember.getMobileStar());
		member.setOperatorId(enterpriseMember.getOperatorId());
		member.setPaypasswd(enterpriseMember.getPaypasswd());
		member.setPaypasswdstatus(enterpriseMember.getPaypasswdstatus());
		member.setPlateFormId(enterpriseMember.getPlateFormId());
		member.setPlateName(enterpriseMember.getPlateName());
		member.setRegistUrl(enterpriseMember.getRegistUrl());
		member.setStatus(enterpriseMember.getStatus());
		member.setNameVerifyStatus(enterpriseMember.getNameVerifyStatus());
	}
}
