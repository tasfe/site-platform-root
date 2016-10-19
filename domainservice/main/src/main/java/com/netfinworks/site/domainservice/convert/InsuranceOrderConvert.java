package com.netfinworks.site.domainservice.convert;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.core.dal.dataobject.InsuranceOrderDO;
import com.yongda.site.domain.domain.insurance.InsuranceOrder;

public class InsuranceOrderConvert {

    public static InsuranceOrder from(InsuranceOrderDO source) {
    	InsuranceOrder target = new InsuranceOrder();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static InsuranceOrderDO to(InsuranceOrder source) {
    	InsuranceOrderDO target = new InsuranceOrderDO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
