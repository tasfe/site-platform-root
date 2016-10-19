package com.yongda.site.ext.integration.urm.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netfinworks.urm.domain.info.UserAccreditInfo;
import com.netfinworks.urm.domain.query.UserAccreditInfoQuery;
import com.netfinworks.urm.domainservice.accredit.UserAccreditInfoInter;
import com.yongda.site.ext.integration.urm.UserAccreditService;
@Service("userAccreditService")
public class UserAccreditServiceImpl implements UserAccreditService {

	@Resource(name="userAccreditFacade")
	UserAccreditInfoInter userAccreditInfoInter;
	/**
	 * 用 memberId 、 第三方id 查询 账号绑定信息,type:1 memberId查找，2 第三方id查找
	 * */
	@Override
	public List<UserAccreditInfo> queryAccreditInfo(String memberId,
			String partnerId, String plat_usr_id,int type) {
		UserAccreditInfoQuery query = new UserAccreditInfoQuery();
		if(type==1){
			query.setPartner_id(partnerId);
			query.setMember_id(memberId);
		}else{
			query.setPartner_id(partnerId);
			query.setPlat_usr_id(plat_usr_id);
		}
		List<UserAccreditInfo> list=userAccreditInfoInter.queryAccreditInfo(query);
		
		return list;
	}

	@Override
	public boolean addAccreditInfo(String memberId, String partnerId,
			String plat_usr_id) {
		UserAccreditInfo  userAccreditInfo =new UserAccreditInfo();
		userAccreditInfo.setPartnerId(partnerId);
		userAccreditInfo.setMemberId(memberId);
		userAccreditInfo.setPlatUsrId(plat_usr_id);
		return userAccreditInfoInter.addUserAccreditInfo(userAccreditInfo);
	}

	@Override
	public boolean addAccreditInfo(String memberId, String partnerId,
			String plat_usr_id, String plat_usr) {
		UserAccreditInfo  userAccreditInfo =new UserAccreditInfo();
		userAccreditInfo.setPartnerId(partnerId);
		userAccreditInfo.setMemberId(memberId);
		userAccreditInfo.setPlatUsrId(plat_usr_id);
		userAccreditInfo.setPlatUsr(plat_usr);
		return userAccreditInfoInter.addUserAccreditInfo(userAccreditInfo);
	}
	
}
