package com.netfinworks.site.core.dal.daointerface;

import java.util.List;

import com.netfinworks.site.core.dal.dataobject.AutoFundoutOrderDO;

/**
 * <p>程序的简单说明</p>
 * @author liuchen
 * @version 创建时间：2015-4-14 下午1:43:56
 */
public interface AutoFundoutOrderDAO {

	AutoFundoutOrderDO selectByPrimaryKey(String outerTradeNo);

	int deleteByPrimaryKey(String outerTradeNo);

	int insertSelective(AutoFundoutOrderDO autoFundoutOrderDO);

	int updateByPrimaryKeySelective(AutoFundoutOrderDO autoFundoutOrderDO);
	
	List<AutoFundoutOrderDO> queryByMemberId(String memberId);
}
