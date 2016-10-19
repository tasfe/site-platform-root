package com.netfinworks.site.domainservice.convert;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.core.dal.dataobject.AuditDO;
import com.netfinworks.site.domain.domain.audit.Audit;

public class AuditConvert {

    public static Audit from(AuditDO source) {
        Audit target = new Audit();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static AuditDO to(Audit source) {
        AuditDO target = new AuditDO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
