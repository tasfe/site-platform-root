package com.netfinworks.site.domainservice.audit;

import javax.annotation.Resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.site.core.dal.daointerface.AuditLogDAO;
import com.netfinworks.site.core.dal.dataobject.AuditLogDO;
import com.netfinworks.site.domain.domain.audit.AuditLog;
import com.netfinworks.site.domainservice.convert.AuditLogConvert;

public class AuditLogServiceImpl {

    private static Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Resource(name = "auditLogDAO")
    private AuditLogDAO auditLogDAO;

    public String addAuditLog(AuditLog log) {
        logger.debug("addAuditLog:",log);
        AuditLogDO record = AuditLogConvert.to(log);
        auditLogDAO.insertSelective(record);
        return record.getId();
    }

    public boolean updAuditLog(AuditLog log) {
        logger.debug("updAuditLog:",log);
        AuditLogDO record = AuditLogConvert.to(log);
        int count = auditLogDAO.updateByPrimaryKeySelective(record);
        return count>0 ? true:false;
    }

    public AuditLog queryAuditLog(String id) {
        logger.debug("queryAudit:",id);
        AuditLogDO logDO = auditLogDAO.selectByPrimaryKey(id);
        if(logDO!=null)
            return AuditLogConvert.from(logDO);
        else
            return null;
    }

}
