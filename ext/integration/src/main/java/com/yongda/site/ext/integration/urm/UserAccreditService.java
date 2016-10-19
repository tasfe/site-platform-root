package com.yongda.site.ext.integration.urm;

import java.util.List;

import com.netfinworks.urm.domain.info.UserAccreditInfo;

public interface UserAccreditService {
	/**
	 * 用 memberId 、 第三方id 查询 账号绑定信息,type:1 memberId查找，2 第三方id查找
	 * */
	public List<UserAccreditInfo> queryAccreditInfo(String memberId,String partnerId,String plat_usr_id,int type);
	
	/**
	 *  账号绑定
	 * */
	public boolean addAccreditInfo(String memberId,String partnerId,String plat_usr_id);
	
	public boolean addAccreditInfo(String memberId,String partnerId,String plat_usr_id,String plat_usr);
}
