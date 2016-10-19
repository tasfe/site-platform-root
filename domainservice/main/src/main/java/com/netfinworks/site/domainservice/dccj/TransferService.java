package com.netfinworks.site.domainservice.dccj;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.BizType;

public interface TransferService {
	/**
	 * 检查用户是否存在
	 * @param username
	 * @param restP
	 * @param type
	 * @return
	 * @throws ServiceException
	 */
	public  RestResponse checkLoginUserName(String username, RestResponse restP,String type) throws ServiceException;
	/**
	 * 校验短信验证码是否正确
	 * @param memberIdentity  会员身份
	 * @param mobile 绑定手机号
	 * @param bizId 
	 * @param loginName 登录名
	 * @param bizType   业务类型
	 * @param otpValue  验证码
	 * @param env
	 * @return
	 * @throws ServiceException  验证处理失败
	 */
	public boolean validateOtpValue(String memberIdentity,String loginName, String mobile,String bizId,BizType bizType,
			String otpValue, OperationEnvironment env);

	
	/**
	 * 发送短信验证码
	 * @param bizId 
	 * @param mobile 绑定的手机号
	 * @param env
	 * @param bizType 约为类型
	 * @return
	 * @throws ServiceException   发送短信失败
	 */
	public boolean sendMobileMessage(String bizId,String mobile, OperationEnvironment env,BizType bizType) throws ServiceException;
	
	
	/**
	 * 是否需要激活
	 * 
	 * @param loginName
	 * @param env
	 * @return
	 */
	public  boolean isNeedActiveMember(String loginName, OperationEnvironment env)throws ServiceException;
	
}