package com.netfinworks.site.ext.integration.auth.inner;

import java.util.List;

import com.netfinworks.authorize.ws.dataobject.OperatorRole;
import com.netfinworks.authorize.ws.response.impl.ResponseHeader;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 
 * <p>权限管理服务-操作员-对内</p>
 * @author luoxun
 * @version $Id: OperatorAuthInnerService.java, v 0.1 2014-5-28 下午12:41:39 luoxun Exp $
 */
public interface OperatorAuthInnerService {
    
    /**
     * 给操作员添加一个function权限
     * @param authVO
     * @return
     * @throws BizException
     */
//    public boolean addFunctionToOperator(AuthVO authVO)throws BizException;

    /**
     * 给操作员删除一个function权限
     * @param authVO
     * @return
     * @throws BizException
     */
//    public boolean removeFunctionFromOperator(AuthVO authVO)throws BizException;

    /**
     * 获取某个操作员所有的功能列表
     * @param authVO
     * @return
     * @throws BizException
     */
    public List<FunctionVO> getFunctionListFromOperator(AuthVO authVO)throws BizException;
    
	/**
	 * 创建操作员角色
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public OperatorRole createOperatorRole(AuthVO authVO) throws BizException;

	/**
	 * 为操作员角色添加功能
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public ResponseHeader addFunctionListToOperatorRole(AuthVO authVO) throws BizException;

	/**
	 * 更新操作员角色名称
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public ResponseHeader updateOperatorRoleName(AuthVO authVO) throws BizException;

	/**
	 * 删除操作员角色
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public ResponseHeader removeOperatorRole(AuthVO authVO) throws BizException;

	/**
	 * 获取操作员角色功能列表
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public List<FunctionVO> getFunctionListFromOperatorRole(AuthVO authVO) throws BizException;

	/**
	 * 从操作员角色删除功能
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public ResponseHeader removeFunctionListFromOperatorRole(AuthVO authVO) throws BizException;

	/**
	 * 从操作员删除角色
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public ResponseHeader removeRoleFromOperator(AuthVO authVO) throws BizException;

	/**
	 * 获取操作员角色列表
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public List<OperatorRole> getRoleListFromOperator(AuthVO authVO) throws BizException;

	/**
	 * 验证操作员是否拥有某个角色
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public boolean checkRoleFromOperator(AuthVO authVO) throws BizException;

	/**
	 * 为操作员添加角色
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public ResponseHeader addRoleToOperator(AuthVO authVO) throws BizException;

}
