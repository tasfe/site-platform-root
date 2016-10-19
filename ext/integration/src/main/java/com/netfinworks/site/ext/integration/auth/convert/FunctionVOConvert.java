package com.netfinworks.site.ext.integration.auth.convert;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.netfinworks.authorize.ws.dataobject.FunctionDO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;

public class FunctionVOConvert {
    public static FunctionVO covertFunctionVO(FunctionDO functionDO){
        if(functionDO==null){
            return null;
        }
        FunctionVO functionVO= new FunctionVO();
        BeanUtils.copyProperties(functionDO, functionVO);
        return functionVO;
    }
    
    public static List<FunctionVO> covertFunctionVOList(List<FunctionDO> functionDOList){
        if(functionDOList==null){
            return null;
        }
        List<FunctionVO> functionVOList= new ArrayList<FunctionVO>();
        for(FunctionDO functionDO:functionDOList){
            FunctionVO functionVO=covertFunctionVO(functionDO);
            if(functionVO!=null){
                functionVOList.add(functionVO);
            }
        }
        return functionVOList;
    }
}
