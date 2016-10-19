package com.netfinworks.site.core.dal.daointerface;

import java.util.List;
import java.util.Map;

import com.netfinworks.site.core.dal.dataobject.AuditDO;

public interface AuditDAO {
    int deleteByPrimaryKey(String id);

    int insertSelective(AuditDO record);

    List<AuditDO> query(Map parameters);
    
    int count(Map parameters);
    
    AuditDO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AuditDO record);

}