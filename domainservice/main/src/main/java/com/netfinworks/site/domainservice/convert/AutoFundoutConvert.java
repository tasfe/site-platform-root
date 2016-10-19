package com.netfinworks.site.domainservice.convert;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.core.dal.dataobject.AutoFundoutOrderDO;
import com.netfinworks.site.domain.domain.autoFundout.AutoFundoutOrder;

/**
 * <p>程序的简单说明</p>
 * @author liuchen
 * @version 创建时间：2015-4-14 下午2:03:55
 */
public class AutoFundoutConvert {

	public static AutoFundoutOrder from(AutoFundoutOrderDO orderDO) {
		AutoFundoutOrder order = new AutoFundoutOrder();

		BeanUtils.copyProperties(orderDO, order);

		return order;
	}

	public static AutoFundoutOrderDO to(AutoFundoutOrder order) {
		AutoFundoutOrderDO orderDO = new AutoFundoutOrderDO();

		BeanUtils.copyProperties(order, orderDO);

		return orderDO;
	}
	
	public static List<AutoFundoutOrder> change(List<AutoFundoutOrderDO> orderDO ){
		List<AutoFundoutOrder> orders = new ArrayList<AutoFundoutOrder>();
		for(AutoFundoutOrderDO autoFundoutOrderDO:orderDO){
			AutoFundoutOrder order = new AutoFundoutOrder();
			BeanUtils.copyProperties(autoFundoutOrderDO, order);
			orders.add(order);
		}
		return orders;
	}
}
