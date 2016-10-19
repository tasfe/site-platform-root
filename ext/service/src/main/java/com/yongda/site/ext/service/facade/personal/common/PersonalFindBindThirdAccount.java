package com.yongda.site.ext.service.facade.personal.common;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.urm.domain.info.UserAccreditInfo;
import com.yongda.site.ext.integration.urm.UserAccreditService;

/**
 * 
 * @author csl 
 *	查找有关第三方账号绑定的信息
 */
@Service("personalFindBindThirdAccount")
public class PersonalFindBindThirdAccount{
	
	@Resource(name = "userAccreditService")
	private UserAccreditService userAccreditService;
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	/**
	 * 通过memberId、partnerId、third_account来判断third_account与memberId的绑定信息是否想匹配
	 */
	public boolean match(String memberId,String plat_usr_id,String partnerId) throws ServiceException{
		try{
			List<UserAccreditInfo>	list=userAccreditService.queryAccreditInfo(memberId, partnerId, plat_usr_id, 2);
			if(list!=null){
				for(UserAccreditInfo uai:list){
					if(uai.getMemberId().equals(memberId)){
						logger.info("第三方账号与永达用户号匹配方法执行成功");
						return true;
					}
				}
			}
			logger.info("第三方账号与永达用户号匹配方法执行成功,plat_usr_id与memberId不匹配");
			return false;
		}catch(Exception e){
			throw new ServiceException("SYSTEM_ERROR");
		}
	}
	
	//查询
	public List<UserAccreditInfo> getUserInfo(String memberId,String partnerId,String plat_usr_id,int type) throws ServiceException{
		try{
			List<UserAccreditInfo>	list=userAccreditService.queryAccreditInfo(memberId, partnerId, plat_usr_id, type);
			return list;
		}catch(Exception e){
			throw new ServiceException("SYSTEM_ERROR");
		}
	}
	//添加
	public boolean addAccreditInfo(String memberId,String partner_id,String open_id,String union_id) throws ServiceException{
		try{
			return userAccreditService.addAccreditInfo(memberId,partner_id,open_id,union_id);
		}catch(Exception e){
			throw new ServiceException("SYSTEM_ERROR");
		}
	}
	
	

}
