package com.netfinworks.site.ext.service.facade.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.dal.daointerface.AuditDAO;
import com.netfinworks.site.core.dal.dataobject.AuditDO;
import com.netfinworks.site.ext.service.facade.converter.AuditConvert;
import com.netfinworks.site.service.facade.api.AuditFacade;
import com.netfinworks.site.service.facade.model.Audit;
import com.netfinworks.site.service.facade.request.AuditRequest;
import com.netfinworks.site.service.facade.response.AuditResponse;

public class AuditServiceImpl implements AuditFacade{
    
    private static Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    
    private AuditDAO auditDAO;

    @Override
    public AuditResponse createApply(AuditRequest request, OperationEnvironment oe) {
        
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("创建申请，请求参数：{}", request);
            beginTime = System.currentTimeMillis();
        }
        Audit audit = request.getAudit();
        logger.debug("addAudit:",request);
        
        AuditDO record = AuditConvert.to(audit);
        int auditId = auditDAO.insertSelective(record);
        
        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("创建申请， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                     });
        }
       
        AuditResponse rep = null;
        if(auditId == 1){
            rep = new AuditResponse();
            rep.setAuditId(record.getId());
            rep.setSuccess(true);
        }
        return rep;
    }

    @Override
    public AuditResponse queryApplyObj(String auditId, OperationEnvironment oe) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAuditDAO(AuditDAO auditDAO) {
        this.auditDAO = auditDAO;
    }
    

}
