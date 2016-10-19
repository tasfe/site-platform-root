package com.netfinworks.site.ext.integration.auth.inner;

import java.util.List;

import com.netfinworks.authorize.ws.dataobject.OperatorRole;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 
 * <p>权限管理-会员服务-对内</p>
 * @author luoxun
 * @version $Id: MemberAuthInnerService.java, v 0.1 2014-5-28 下午12:42:13 luoxun Exp $
 */
public interface MemberAuthInnerService {
    
    /**
     * 获取某个会员的所有功能列表
     * @param memberId
     * @return
     * @throws BizException
     */
    public List<FunctionVO> getFunctionListFromMember(AuthVO authVO)throws BizException;

	/**
	 * 获取会员角色列表
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public List<OperatorRole> getOperatorRoleListFromMember(AuthVO authVO) throws BizException;

	/**
	 * 添加会员角色
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public boolean addRoleToMember(AuthVO authVO) throws BizException;
}
