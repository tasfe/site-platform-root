package com.netfinworks.site.ext.integration.member.convert;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.netfinworks.ma.service.response.LoginNameInfo;
import com.netfinworks.site.domain.domain.member.LoginMethodInfoVO;

public class LoginMethodInfoVOConvert {
    
    public static LoginMethodInfoVO convert(LoginNameInfo loginNameInfo){
        if(loginNameInfo==null){
            return null;
        }
        LoginMethodInfoVO loginMethodInfoVO=new LoginMethodInfoVO();
        BeanUtils.copyProperties(loginNameInfo, loginMethodInfoVO);
        loginMethodInfoVO.setId(loginNameInfo.getLoginId());
        return loginMethodInfoVO;
    }
    
    public static List<LoginMethodInfoVO> convertList(List<LoginNameInfo> loginNameInfoList){
        if(loginNameInfoList==null){
            return null;
        }
        List<LoginMethodInfoVO> loginMethodInfoVOList=new ArrayList<LoginMethodInfoVO>();
        for(LoginNameInfo loginNameInfo:loginNameInfoList){
            LoginMethodInfoVO loginMethodInfoVO=LoginMethodInfoVOConvert.convert(loginNameInfo);
            if(loginMethodInfoVO!=null){
                loginMethodInfoVOList.add(loginMethodInfoVO);
            }
        }
        return loginMethodInfoVOList;
    }
    
    
}
