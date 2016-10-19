package com.netfinworks.site.core.dal.daointerface;

import com.netfinworks.site.core.dal.dataobject.InsuranceOrderDO;
import com.netfinworks.site.core.dal.dataobject.InsuranceOrderDOExample;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface InsuranceOrderDAO {
    int countByExample(InsuranceOrderDOExample example);

    int deleteByExample(InsuranceOrderDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(InsuranceOrderDO record);

    int insertSelective(InsuranceOrderDO record);

    List<InsuranceOrderDO> selectByExample(InsuranceOrderDOExample example);

    List<InsuranceOrderDO> query(Map parameters);

    int count(Map parameters);

    InsuranceOrderDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") InsuranceOrderDO record, @Param("example") InsuranceOrderDOExample example);

    int updateByExample(@Param("record") InsuranceOrderDO record, @Param("example") InsuranceOrderDOExample example);

    int updateByPrimaryKeySelective(InsuranceOrderDO record);

    int updateByPrimaryKey(InsuranceOrderDO record);
}