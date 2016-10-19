package com.netfinworks.site.core.dal.daointerface;

import java.util.List;
import java.util.Map;

import com.netfinworks.site.core.dal.dataobject.AuditLogDO;

public interface AuditLogDAO {
    int deleteByPrimaryKey(String id);

    int insertSelective(AuditLogDO record);

    List<AuditLogDO> query(Map parameters);

    AuditLogDO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AuditLogDO record);

}