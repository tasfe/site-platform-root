package com.netfinworks.site.domainservice.convert;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.core.dal.dataobject.AuditLogDO;
import com.netfinworks.site.domain.domain.audit.AuditLog;

public class AuditLogConvert {

    public static AuditLog from(AuditLogDO source) {
        AuditLog target = new AuditLog();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static AuditLogDO to(AuditLog source) {
        AuditLogDO target = new AuditLogDO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
