package com.netfinworks.site.ext.integration.member.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.ILoginPwdFacade;
import com.netfinworks.ma.service.facade.IOperatorFacade;
import com.netfinworks.ma.service.request.LoginPwdRequest;
import com.netfinworks.ma.service.request.OperatorChangeRequest;
import com.netfinworks.ma.service.request.OperatorInputRequest;
import com.netfinworks.ma.service.request.OperatorLoginRequest;
import com.netfinworks.ma.service.request.OperatorQueryRequest;
import com.netfinworks.ma.service.response.LoginNameResponse;
import com.netfinworks.ma.service.response.NewOperatorResponse;
import com.netfinworks.ma.service.response.OperatorInfoMultiResponse;
import com.netfinworks.ma.service.response.OperatorInfoResponse;
import com.netfinworks.ma.service.response.OperatorLoginResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.LoginMethodInfoVO;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.OperatorLockStatusEnum;
import com.netfinworks.site.domain.enums.OperatorStatusEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.ext.integration.member.convert.LoginMethodInfoVOConvert;
import com.netfinworks.site.ext.integration.member.convert.OperatorVOConvert;
import com.netfinworks.ues.util.UesUtils;

/**
 * 
 * <p>操作员远程调用接口-实现类</p>
 * @author luoxun
 * @version $Id: OperatorServiceImpl.java, v 0.1 2014-5-21 下午3:44:33 luoxun Exp $
 */
@Service("operatorService")
public class OperatorServiceImpl implements OperatorService {

    private Logger          logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private IOperatorFacade operatorFacade;
    @Resource
    private ILoginPwdFacade loginPwdFacade;
    
    @Override
    public MemberAndAccount addOperator(OperatorVO operatorVO, OperationEnvironment environment)
                                                                                                throws BizException {
        String opMsg="远程新增操作员";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", operatorVO);
            }
            OperatorInputRequest request=OperatorVOConvert.createAddOperRequest(operatorVO);
            NewOperatorResponse response = operatorFacade.addOperator(environment, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            String responseCode = response.getResponseCode();
            if(ResponseCode.SUCCESS.getCode().equals(responseCode)){
                MemberAndAccount memberAndAccount=new MemberAndAccount();
                memberAndAccount.setMemberId(response.getMemberId());
                memberAndAccount.setOperatorId(response.getOperatorId());
                return memberAndAccount;
            }else{
                logger.error(opMsg+"{}信息异常:返回信息:{},{}", operatorVO, response.getResponseCode(),
                    response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorVO, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
        
    }

    @Override
    public void activateOperator(String operatorId, OperationEnvironment env) throws BizException {
        String opMsg="远程激活操作员";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", operatorId);
            }
            OperatorChangeRequest request = new OperatorChangeRequest();
            request.setOperatorId(operatorId);
            request.setStatus(OperatorStatusEnum.NORMAL.getCode().longValue());
            Response response = operatorFacade.updateOperatorInfo(env, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            String responseCode = response.getResponseCode();
            if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else if (!ResponseCode.SUCCESS.getCode().equals(responseCode)) {
                logger.error(opMsg+" {}信息异常:返回信息:{},{}", operatorId, response.getResponseCode(),
                    response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public void closeOperator(String operatorId, OperationEnvironment env) throws BizException {
        String opMsg="远程注销操作员";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", operatorId);
            }
            OperatorChangeRequest request = new OperatorChangeRequest();
            request.setOperatorId(operatorId);
            request.setStatus(OperatorStatusEnum.CLOSE.getCode().longValue());
            Response response = operatorFacade.updateOperatorInfo(env, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            String responseCode = response.getResponseCode();
            if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else if (!ResponseCode.SUCCESS.getCode().equals(responseCode)) {
                logger.error(opMsg+"{}信息异常:返回信息:{},{}", operatorId, response.getResponseCode(),
                    response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public void lockOperator(String operatorId, OperationEnvironment env) throws BizException {
        String opMsg="远程锁定操作员";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", operatorId);
            }
            OperatorChangeRequest request = new OperatorChangeRequest();
            request.setOperatorId(operatorId);
            request.setLockStatus(OperatorLockStatusEnum.LOCK.getCode().longValue());
            Response response = operatorFacade.updateOperatorInfo(env, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            String responseCode = response.getResponseCode();
            if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else if (!ResponseCode.SUCCESS.getCode().equals(responseCode)) {
                logger.error(opMsg+"{}信息异常:返回信息:{},{}", operatorId, response.getResponseCode(),
                    response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public void unlockOperator(String operatorId, OperationEnvironment env) throws BizException {
        String opMsg="远程解锁操作员";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", operatorId);
            }
            OperatorChangeRequest request = new OperatorChangeRequest();
            request.setOperatorId(operatorId);
            request.setLockStatus(OperatorLockStatusEnum.UNLOCK.getCode().longValue());
            Response response = operatorFacade.updateOperatorInfo(env, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            String responseCode = response.getResponseCode();
            if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else if (!ResponseCode.SUCCESS.getCode().equals(responseCode)) {
                logger.error(opMsg+"{}信息异常:返回信息:{},{}", operatorId, response.getResponseCode(),
                    response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public void updateNickname(String operatorId, String nickName, OperationEnvironment env)
                                                                                            throws BizException {
        String opMsg="远程修改操作员昵称";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", operatorId);
            }
            OperatorChangeRequest request = new OperatorChangeRequest();
            request.setOperatorId(operatorId);
            request.setNickName(nickName);
            Response response = operatorFacade.updateOperatorInfo(env, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            String responseCode = response.getResponseCode();
            if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else if (!ResponseCode.SUCCESS.getCode().equals(responseCode)) {
                logger.error(opMsg+"{}信息异常:返回信息:{},{}", operatorId, response.getResponseCode(),
                    response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public List<OperatorVO> queryOperatorsByMemberId(String memberId,Boolean isFilterClose,Boolean isFilterDefault,
                                                     OperationEnvironment env)
                                                                                      throws BizException {
        String opMsg="远程根据会员ID查询该会员下所有的操作员";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", memberId);
            }
            OperatorInfoMultiResponse response = operatorFacade.queryOperators(env, memberId);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            String responseCode = response.getResponseCode();
            if(ResponseCode.SUCCESS.getCode().equals(responseCode)){
                List<OperatorVO> operatorVOList=OperatorVOConvert.convertList(response.getOperatorInfos());
                //查询登陆名，同时根据isFilterClose的值，选择是否过滤掉已经注销掉的操作员信息
                List<OperatorVO> resultList=new ArrayList<OperatorVO>();
                for(OperatorVO operatorVO:operatorVOList){
                    //过滤已经关闭的操作员
                    if(isFilterClose){
                        if(operatorVO.getStatusEnum().equals(OperatorStatusEnum.CLOSE)){
                            continue;
                        }
                    }
                    //过滤默认操作员
                    if(isFilterDefault){
                        if(operatorVO.getIsDefaultOperator()){
                            continue;
                        }
                    }
                    List<LoginMethodInfoVO> loginMethodInfoVOList=this.queryLoginMethods(operatorVO.getOperatorId(), env);
                    if((loginMethodInfoVOList!=null)&&(loginMethodInfoVOList.size()>0)){
                        String loginName=loginMethodInfoVOList.get(0).getLoginName();
                        operatorVO.setLoginName(loginName);
                    }
                    resultList.add(operatorVO);
                }
                return resultList;
            }else if (CommonConstant.USER_NOT_EXIST.equals(responseCode)) {
                throw new BizException(ErrorCode.MEMBER_NOT_EXIST);
            }else{
                logger.error(opMsg+"{}信息异常:返回信息:{},{}", memberId, response.getResponseCode(),
                    response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", memberId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public OperatorVO getOperatorById(String operatorId, OperationEnvironment env)
                                                                                        throws BizException {
        String opMsg="根据ID远程查询操作员信息"; 
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：{}", operatorId);
            }
            OperatorInfoResponse response = operatorFacade.getOperatorByOperatorId(env,
                operatorId);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            String responseCode = response.getResponseCode();
            if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { //成功
                OperatorVO operatorVO = OperatorVOConvert.convertOperatorVO(response.getOperatorInfo());
                List<LoginMethodInfoVO> loginMethodInfoVOList=this.queryLoginMethods(operatorVO.getOperatorId(), env);
                if((loginMethodInfoVOList!=null)&&(loginMethodInfoVOList.size()>0)){
                    String loginName=loginMethodInfoVOList.get(0).getLoginName();
                    operatorVO.setLoginName(loginName);
                }
                return operatorVO;
            } else if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else {
                logger.error(opMsg+" {}信息异常:返回信息:{},{}", operatorId,
                    response.getResponseCode(), response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public boolean checkLoginNameExist(String memberId, String loginName, String platformType,OperationEnvironment env) throws BizException{
        try {
            OperatorVO operatorVO = this.getOperByLoginName(memberId, loginName, platformType,env);
            return operatorVO!=null;
        } catch (BizException e) {
            if(e.getCode().equals(ErrorCode.OPERATOR_NOT_EXIST)){
                return false;
            }else{
               throw e; 
            }
        }
    }

    @Override
    public OperatorVO getOperByLoginName(String memberId, String loginName, String platformType,OperationEnvironment env)
                                                                                                     throws BizException {
        String opMsg="根据操作员登陆名查询操作员信息"; 
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：memberId:{},loginName:{}", memberId,loginName);
            }
            OperatorQueryRequest request=new OperatorQueryRequest();
            request.setLoginName(loginName);
            request.setMemberId(memberId);
            request.setPlatformType(platformType);
            OperatorInfoResponse response = operatorFacade.queryOperator(env, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            String responseCode = response.getResponseCode();
            if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { //成功
                return OperatorVOConvert.convertOperatorVO(response.getOperatorInfo());
            } else if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else {
                logger.error(opMsg+" {}信息异常:返回信息:{},{}", memberId,
                    response.getResponseCode(), response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", memberId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public List<LoginMethodInfoVO> queryLoginMethods(String operatorId,
                                                     OperationEnvironment env)
                                                                                      throws BizException {
        String opMsg="根据操作员ID查询操作员登陆方式列表"; 
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：memberId:{}", operatorId);
            }
            LoginNameResponse response = operatorFacade.queryLoginNames(env, operatorId);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            String responseCode = response.getResponseCode();
            if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { //成功
                return LoginMethodInfoVOConvert.convertList(response.getLoginNames());
            } else if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else {
                logger.error(opMsg+" {}信息异常:返回信息:{},{}", operatorId,
                    response.getResponseCode(), response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

	@Override
	public boolean removeLoginName(Long loginId, OperationEnvironment env) throws BizException {
		String opMsg = "删除操作员";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：loginId:{}", loginId);
			}
			Response response = operatorFacade.removeLoginName(env, loginId);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { // 成功
				return true;
			} else {
				logger.error(opMsg + " {}信息异常:返回信息:{},{}", loginId, response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "{}信息异常:异常信息{}", loginId, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

    @Override
    public boolean resetLoginPwd(String operatorId, String loginPwd, OperationEnvironment env)
                                                                                              throws BizException {
        String opMsg="根据操作员ID重置操作员登陆密码"; 
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：operatorId:{},loginPwd", operatorId,loginPwd);
            }
            //先重置锁
            if(this.resetLoginPwdLock(operatorId, env)){
                LoginPwdRequest request=new LoginPwdRequest();
                request.setOperatorId(operatorId);
                request.setPassword(UesUtils.hashSignContent(loginPwd));
                Response response=loginPwdFacade.setLoginPwd(env, request);
                long consumeTime = System.currentTimeMillis() - beginTime;
                if (logger.isInfoEnabled()) {
                    logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                            response });
                }
                String responseCode = response.getResponseCode();
                if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { //成功
                    return true;
                } else if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                    throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
				} else if (CommonConstant.LOGINPWD_EQUALS_PAYPWD.equals(responseCode)) {
					throw new BizException(ErrorCode.LOGINPWD_EQUALS_PAYPWD);
				}else {
                    logger.error(opMsg+" {}信息异常:返回信息:{},{}", operatorId,
                        response.getResponseCode(), response.getResponseMessage());
                    throw new BizException(ErrorCode.SYSTEM_ERROR);
                }
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
        
        return false;
    }

    @Override
    public boolean resetLoginPwdLock(String operatorId, OperationEnvironment env)
                                                                                 throws BizException {
        String opMsg="根据操作员ID重置操作员登陆密码的锁定状态"; 
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：operatorId:{}", operatorId);
            }
            Response response = loginPwdFacade.resetLoginPwdLockByOperatorId(env, operatorId);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            String responseCode = response.getResponseCode();
            if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { //成功
                return true;
            } else if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
            } else {
                logger.error(opMsg+" {}信息异常:返回信息:{},{}", operatorId,
                    response.getResponseCode(), response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorId, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public List<OperatorVO> queryOperatorLoginInfos(OperatorVO operatorVO, OperationEnvironment env)throws BizException {
        String opMsg="根据条件查询操作员信息"; 
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：operatorVO:{}", operatorVO);
            }
            
            OperatorLoginRequest request=OperatorVOConvert.createOperatorLoginQueryRequest(operatorVO);
            OperatorLoginResponse response = operatorFacade.queryOperatorLoginInfos(env, request);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            String responseCode = response.getResponseCode();
            if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { //成功
                return OperatorVOConvert.convertOperatorLoginInfoList(response.getOperatorInfos());
            } else if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                return new ArrayList<OperatorVO>();
            } else {
                logger.error(opMsg+" {}信息异常:返回信息:operatorVO{},{}", operatorVO,
                    response.getResponseCode(), response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"{}信息异常:异常信息{}", operatorVO, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }                                                                                            
    }
    

	@Override
	public OperatorVO getDefaultOperator(String memberId,
			OperationEnvironment env) throws BizException {
		String opMsg="查询默认操作员信息"; 
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：memberID:{}", memberId);
            }
            
            OperatorInfoMultiResponse response = operatorFacade.queryOperators(env, memberId);
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            String responseCode = response.getResponseCode();
            if (ResponseCode.SUCCESS.getCode().equals(responseCode)) { //成功
                return OperatorVOConvert.convertOperatorVO(response.getOperatorInfos().get(0));
            } else if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {//操作员不存在
                return null;
            } else {
                logger.error(opMsg+" 信息异常:返回信息:{},{}",
                    response.getResponseCode(), response.getResponseMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"信息异常:异常信息{}", e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }  
	}

	@Override
	public void updateOperator(OperatorVO operatorVO, OperationEnvironment env) throws BizException {
		String opMsg = "修改操作员";
		try {
			long beginTime = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "，请求参数：{}", operatorVO);
			}
			OperatorChangeRequest request = new OperatorChangeRequest();
			request.setOperatorId(operatorVO.getOperatorId());
			request.setLockStatus(operatorVO.getLockStatusEnum().getCode().longValue());
			request.setNickName(operatorVO.getNickName());
			request.setExtention(operatorVO.getExtention());
			request.setContact(operatorVO.getContact());

			Response response = operatorFacade.updateOperatorInfo(env, request);
			long consumeTime = System.currentTimeMillis() - beginTime;
			if (logger.isInfoEnabled()) {
				logger.info(opMsg + "， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			String responseCode = response.getResponseCode();
			if (CommonConstant.OPERATOR_NOT_EXIST.equals(responseCode)) {// 操作员不存在
				throw new BizException(ErrorCode.OPERATOR_NOT_EXIST);
			} else if (!ResponseCode.SUCCESS.getCode().equals(responseCode)) {
				logger.error(opMsg + "{}信息异常:返回信息:{},{}", operatorVO, response.getResponseCode(),
						response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error(opMsg + "{}信息异常:异常信息{}", operatorVO, e.getMessage(), e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}

}
