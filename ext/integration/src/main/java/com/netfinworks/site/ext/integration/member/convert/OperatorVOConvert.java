package com.netfinworks.site.ext.integration.member.convert;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.netfinworks.ma.service.base.model.Operator;
import com.netfinworks.ma.service.request.OperatorInputRequest;
import com.netfinworks.ma.service.request.OperatorLoginRequest;
import com.netfinworks.ma.service.response.OperatorLoginInfo;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.enums.OperatorLockStatusEnum;
import com.netfinworks.site.domain.enums.OperatorStatusEnum;
import com.netfinworks.site.domain.enums.OperatorTypeEnum;
import com.netfinworks.site.domain.enums.PlatformTypeEnum;
import com.netfinworks.ues.util.UesUtils;

/**
 * 
 * <p>操作员数据转换类</p>
 * @author luoxun
 * @version $Id: OperatorConvert.java, v 0.1 2014-5-21 上午9:57:24 luoxun Exp $
 */
public class OperatorVOConvert {
    
    public static OperatorVO convertOperatorVO(Operator operator){
        if(operator==null){
            return null;
        }
        OperatorVO operatorVO=new OperatorVO();
        BeanUtils.copyProperties(operator, operatorVO);
        operatorVO.setOperatorTypeEnum(OperatorTypeEnum.getByCode(operator.getOperatorType().intValue()));
        operatorVO.setStatusEnum(OperatorStatusEnum.getByCode(operator.getStatus().intValue()));
        operatorVO.setLockStatusEnum(OperatorLockStatusEnum.getByCode(operator.getLockStatus().intValue()));
        return operatorVO;
    }
    
    public static List<OperatorVO> convertList(List<Operator> operatorList){
        if(operatorList==null){
            return null;
        }
        List<OperatorVO> operatorVOList=new ArrayList<OperatorVO>();
        for(Operator operator:operatorList){
            OperatorVO operatorVO=OperatorVOConvert.convertOperatorVO(operator);
            if(operatorVO!=null){
                operatorVOList.add(operatorVO);
            }
        }
        return operatorVOList;
    }
    
    public static OperatorVO convertOperatorVO(OperatorLoginInfo operatorLoginInfo){
        if(operatorLoginInfo==null){
            return null;
        }
        OperatorVO operatorVO=new OperatorVO();
        BeanUtils.copyProperties(operatorLoginInfo, operatorVO);
        operatorVO.setOperatorTypeEnum(OperatorTypeEnum.getByCode(operatorLoginInfo.getOperatorType().intValue()));
        operatorVO.setStatusEnum(OperatorStatusEnum.getByCode(operatorLoginInfo.getStatus().intValue()));
        operatorVO.setLockStatusEnum(OperatorLockStatusEnum.getByCode(operatorLoginInfo.getLockStatus().intValue()));
        return operatorVO;
    }
    
    public static List<OperatorVO> convertOperatorLoginInfoList(List<OperatorLoginInfo> operatorLoginInfoList){
        if(operatorLoginInfoList==null){
            return null;
        }
        List<OperatorVO> operatorVOList=new ArrayList<OperatorVO>();
        for(OperatorLoginInfo operatorLoginInfo:operatorLoginInfoList){
            OperatorVO operatorVO=OperatorVOConvert.convertOperatorVO(operatorLoginInfo);
            if(operatorVO!=null){
                operatorVOList.add(operatorVO);
            }
        }
        return operatorVOList;
    }
    
    public static OperatorInputRequest createAddOperRequest(OperatorVO operatorVO){
        OperatorInputRequest request=new OperatorInputRequest();
        if(operatorVO!=null){
            BeanUtils.copyProperties(operatorVO, request);
            request.setLoginPwd(UesUtils.hashSignContent(operatorVO.getLoginPwd()));
            request.setLoginNameType(operatorVO.getLoginNameTypeEnum().getCode());
            request.setPlatformType(String.valueOf(operatorVO.getPlatformTypeEnum().getCode()));
        }
        return request;
    }
    
    
    public static OperatorLoginRequest createOperatorLoginQueryRequest(OperatorVO operatorVO){
        OperatorLoginRequest request=new OperatorLoginRequest();
        if(operatorVO!=null){
            BeanUtils.copyProperties(operatorVO, request);
            if(operatorVO.getStatusEnum()!=null){
                request.setOperatorStatus(Long.valueOf(operatorVO.getStatusEnum().getCode()));
            }
            if(operatorVO.getLockStatusEnum()!=null){
                request.setLockStatus(Long.valueOf(operatorVO.getLockStatusEnum().getCode()));
            }
            //TODO -- 需要新增根据 source_id查询条件
        }
        return request;
    }
}
