package com.netfinworks.site.service.facade.api;

import javax.jws.WebService;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.service.facade.request.AuditRequest;
import com.netfinworks.site.service.facade.response.AuditResponse;

/**
 * <p>审核接口</p>
 * @author zhangyun.m
 * @version $Id: AuditFacade.java, v 0.1 2014年7月16日 下午6:05:22 zhangyun.m Exp $
 */
@WebService(targetNamespace = "http://facade.site.netfinworks.com")
public interface AuditFacade {
    
    /** 创建申请
     * @param request
     * @param oe
     * @return
     */
    public AuditResponse createApply(AuditRequest request,OperationEnvironment oe);
    
    
    /**根据申请ID获得申请对象
     * @param auditId
     * @param oe
     * @return
     */
    public AuditResponse queryApplyObj(String auditId,OperationEnvironment oe);
    
    
    
    
    
    

}
