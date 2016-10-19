package com.netfinworks.site.service.facade.request;

import com.netfinworks.site.service.facade.model.Audit;

/**
 * <p>审核请求</p>
 * @author zhangyun.m
 * @version $Id: AuditRequest.java, v 0.1 2014年7月16日 下午5:30:50 zhangyun.m Exp $
 */
public class AuditRequest {
    
    /** 审核信息 */
    private Audit audit;

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }
    
    

}
