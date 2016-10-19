package com.netfinworks.site.ext.service.facade.converter;


import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.core.dal.dataobject.AuditDO;
import com.netfinworks.site.service.facade.model.Audit;

/**
 * <p>审核转换</p>
 * @author zhangyun.m
 * @version $Id: AuditConvert.java, v 0.1 2014年7月16日 下午7:26:27 zhangyun.m Exp $
 */
public class AuditConvert {

    public static Audit from(AuditDO source) {
        Audit target = new Audit();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static AuditDO to(Audit source) {
        AuditDO target = new AuditDO();
		BeanUtils.copyProperties(source, target);
        target.setGmtCreated(new Date());
		// target.setGmtModified(new Date());
        return target;
    }
}
