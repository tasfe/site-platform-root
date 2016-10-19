package com.netfinworks.site.ext.integration.auth.outer;

import java.util.List;

import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 
 * <p>权限鉴定服务-操作员-对外服务接口</p>
 * @author luoxun
 * @version $Id: OperatorAuthOuterService.java, v 0.1 2014-5-28 上午11:25:59 luoxun Exp $
 */
public interface OperatorAuthOuterService {
    
    
     /***
     * 验证操作员是否拥有某个功能
     * @param authVO
     * @return
     * @throws BizException
     */
    public boolean checkFunctionFromOperator(AuthVO  authVO) throws BizException;
    

    /**
     * 给操作员添加一个function权限
     * @param authVO
     * @return
     * @throws BizException
     */
    public boolean addFunctionToOperator(AuthVO authVO)throws BizException;

    /**
     * 给操作员删除一个function权限
     * @param authVO
     * @return
     * @throws BizException
     */
    public boolean removeFunctionFromOperator(AuthVO authVO)throws BizException;

	
	/**
	 * 获取某个操作员所有的功能列表
	 * 
	 * @param authVO
	 * @return
	 * @throws BizException
	 */
	public List<FunctionVO> getFunctionListFromOperator(AuthVO authVO) throws BizException;





}
