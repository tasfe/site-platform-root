package com.netfinworks.site.service.facade.response;

import com.netfinworks.biz.common.util.BaseResult;

/**
 * <p>审核响应</p>
 * @author zhangyun.m
 * @version $Id: AuditResponse.java, v 0.1 2014年7月16日 下午5:30:29 zhangyun.m Exp $
 */
public class AuditResponse extends BaseResult{

    /**
     * 
     */
    private static final long serialVersionUID = -1L;
    
    
    private String auditId;


    public String getAuditId() {
        return auditId;
    }


    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }
    
    

}
