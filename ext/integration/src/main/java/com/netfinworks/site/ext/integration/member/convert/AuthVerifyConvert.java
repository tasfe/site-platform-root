package com.netfinworks.site.ext.integration.member.convert;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.netfinworks.ma.service.base.model.VerifyInfo;
import com.netfinworks.ma.service.request.VerifyInputRequest;
import com.netfinworks.ma.service.request.VerifyQueryRequest;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;

/**
 * <p>认证验证转换类</p>
 * @author liangzhizhuang.m
 * @version $Id: VerifyInputConvert.java, v 0.1 2014年5月23日 上午10:39:50 liangzhizhuang.m Exp $
 */
public class AuthVerifyConvert {

    /**
     * 创建、修改认证请求参数转换
     * @param info
     * @return
     */
    public VerifyInputRequest toVefifyInputConvert(AuthVerifyInfo info) throws Exception{
        VerifyInputRequest request = new VerifyInputRequest();
        VerifyInfo vInfo = new VerifyInfo();
        BeanUtils.copyProperties(info, vInfo);
        request.setVerifyInfo(vInfo);
        return request;
    }
    
    /**
     * 查询认证请求参数转换
     * @param info
     * @return
     */
    public VerifyQueryRequest toVefifyQueryConvert(AuthVerifyInfo info) throws Exception{
        VerifyQueryRequest request = new VerifyQueryRequest();
        BeanUtils.copyProperties(info, request);
        return request;
    }
    
    public List<AuthVerifyInfo> toAuthVerifyInfoConvert(List<VerifyInfo> verifyInfos) throws Exception {
        List<AuthVerifyInfo> authVerifyInfos = new ArrayList<AuthVerifyInfo>();
        for (int i = 0; i < verifyInfos.size(); i++) {
            AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
            VerifyInfo verifyInfo = verifyInfos.get(i);
            BeanUtils.copyProperties(verifyInfo, authVerifyInfo);
            authVerifyInfos.add(authVerifyInfo);
        }
        return authVerifyInfos;
    }
    
}
