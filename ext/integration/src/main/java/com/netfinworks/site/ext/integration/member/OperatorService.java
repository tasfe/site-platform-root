package com.netfinworks.site.ext.integration.member;

import java.util.List;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.member.LoginMethodInfoVO;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 操作员远程调用接口
 * <p>注释</p>
 * @author luoxun
 * @version $Id: OperatorService.java, v 0.1 2014-5-20 下午6:35:18 luoxun Exp $
 */
public interface OperatorService {

    /***
     * 增加操作员
     * @param operator
     * @param environment
     * @return
     */
    public MemberAndAccount addOperator(OperatorVO operatorVO, OperationEnvironment env)
                                                                                        throws BizException;

    /**
     * 激活操作员
     * @param operatorId
     * @param env
     * @return
     * @throws BizException
     */
    public void activateOperator(String operatorId, OperationEnvironment env) throws BizException;

    /**
     * 关闭操作员
     * @param operatorId
     * @param env
     * @return
     * @throws BizException
     */
    public void closeOperator(String operatorId, OperationEnvironment env) throws BizException;

    /**
     * 锁定操作员
     * @param operatorId
     * @param env
     * @return
     * @throws BizException
     */
    public void lockOperator(String operatorId, OperationEnvironment env) throws BizException;

    /**
     * 解锁操作员
     * @param operatorId
     * @param env
     * @return
     * @throws BizException
     */
    public void unlockOperator(String operatorId, OperationEnvironment env) throws BizException;

    /***
     * 修改昵称
     * @param operatorId
     * @param nickname
     * @param env
     * @return
     * @throws BizException
     */
    public void updateNickname(String operatorId, String nickName, OperationEnvironment env)
                                                                                            throws BizException;

    /***
     * 根据会员ID查询该会员所有的操作员信息
     * @param memberId
     * @param environment
     * @return
     * @throws BizException
     */
    public List<OperatorVO> queryOperatorsByMemberId(String memberId, Boolean isFilterClose,
                                                     Boolean isFilterDefault,
                                                     OperationEnvironment env) throws BizException;

    /***
     * 根据操作员ID查询操作员信息
     * @param operatorId
     * @param environment
     * @return
     */
    public OperatorVO getOperatorById(String operatorId, OperationEnvironment env)
                                                                                  throws BizException;
    
    public OperatorVO getDefaultOperator(String memberId, OperationEnvironment env)
                                                                                  throws BizException;

    /**
     * 根据操作员登陆名查询操作员信息
     * @param memberId
     * @param loginName
     * @param env
     * @return
     * @throws BizException
     */
    public OperatorVO getOperByLoginName(String memberId, String loginName,String platformType,OperationEnvironment env)
                                                                                                     throws BizException;

    /**
     * 检查操作员登陆名是否已经使用
     * @param memberId
     * @param loginName
     * @param env
     * @return
     * @throws BizException
     */
    public boolean checkLoginNameExist(String memberId, String loginName, String platformType,OperationEnvironment env)
                                                                                                   throws BizException;

    /***
     * 根据操作员ID查询登陆方式列表
     * @param operatorId
     * @param environment
     * @return
     * @throws BizException
     */
    public List<LoginMethodInfoVO> queryLoginMethods(String operatorId, OperationEnvironment env)
                                                                                                 throws BizException;
    
    /**
	 * 删除操作员
	 * 
	 * @param loginId
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public boolean removeLoginName(Long loginId, OperationEnvironment env) throws BizException;

    /***
     * 重置操作员的登陆密码
     * @param operatorId
     * @param loginPwd
     * @param env
     * @return
     * @throws BizException
     */
    public boolean resetLoginPwd(String operatorId, String loginPwd, OperationEnvironment env)
                                                                                              throws BizException;

    /**
     * 重置操作员登陆密码锁定状态
     * @param operatorId
     * @param env
     * @return
     * @throws BizException
     */
    public boolean resetLoginPwdLock(String operatorId, OperationEnvironment env)
                                                                                 throws BizException;

    /**
     * 按条件查询操作员
     * @param operatorVO
     * @param env
     * @return
     * @throws BizException
     */
    public List<OperatorVO> queryOperatorLoginInfos(OperatorVO operatorVO, OperationEnvironment env)
                                                                                                    throws BizException;

	/***
	 * 修改操作员
	 * @param operatorId
	 * @param nickname
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public void updateOperator(OperatorVO operatorVO, OperationEnvironment env) throws BizException;
}
