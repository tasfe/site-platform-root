package com.netfinworks.site.domainservice.autoFundout;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.site.core.dal.daointerface.AutoFundoutOrderDAO;
import com.netfinworks.site.core.dal.dataobject.AutoFundoutOrderDO;
import com.netfinworks.site.domain.domain.autoFundout.AutoFundoutOrder;
import com.netfinworks.site.domainservice.convert.AutoFundoutConvert;

/**
 * <p>程序的简单说明</p>
 * @author liuchen
 * @version 创建时间：2015-4-14 下午1:50:50
 */
public class AutoFundoutServiceImpl {
	private static Logger logger = LoggerFactory.getLogger(AutoFundoutServiceImpl.class);

	@Resource(name = "autoFundoutOrderDAO")
	private AutoFundoutOrderDAO autoFundoutOrderDAO;

	public AutoFundoutOrder queryOrderById(String outerTradeNo) {
		AutoFundoutOrderDO autoFundoutOrderDO = autoFundoutOrderDAO.selectByPrimaryKey(outerTradeNo);

		if (null != autoFundoutOrderDO) {
			return AutoFundoutConvert.from(autoFundoutOrderDO);
		}

		return null;
	}

	public boolean insertOrder(AutoFundoutOrder order) {

		AutoFundoutOrderDO orderDO = AutoFundoutConvert.to(order);

		int count = autoFundoutOrderDAO.insertSelective(orderDO);

		return count > 0 ? true : false;
	}

	public boolean updateOrder(AutoFundoutOrder order) {

		AutoFundoutOrderDO orderDO = AutoFundoutConvert.to(order);

		int count = autoFundoutOrderDAO.updateByPrimaryKeySelective(orderDO);

		return count > 0 ? true : false;
	}

	public List<AutoFundoutOrder> queryByMemberId(String memberId){
		List<AutoFundoutOrderDO> orderDO = autoFundoutOrderDAO.queryByMemberId(memberId);
		List<AutoFundoutOrder> order = AutoFundoutConvert.change(orderDO);
		return order;
	}
}
