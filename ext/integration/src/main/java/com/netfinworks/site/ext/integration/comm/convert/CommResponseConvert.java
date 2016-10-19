package com.netfinworks.site.ext.integration.comm.convert;

import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.site.domain.domain.comm.CommResponse;

/**
 * <p>返回结果转换</p>
 * @author zhangyun.m
 * @version $Id: CommResponseConvert.java, v 0.1 2014年5月29日 下午6:43:28 zhangyun.m Exp $
 */
public class CommResponseConvert { 
    
    /**返回结果转换
     * @param rep
     * @return
     */
    public static  CommResponse  commResponseConvert(Response rep){
        CommResponse commRep = new CommResponse();
        commRep.setResponseCode(rep.getResponseCode());
        commRep.setResponseMessage(rep.getResponseMessage());
        commRep.setExtention(rep.getExtention());
        return commRep;
    }
    

}
