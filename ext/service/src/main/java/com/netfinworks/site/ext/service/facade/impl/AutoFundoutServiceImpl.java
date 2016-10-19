package com.netfinworks.site.ext.service.facade.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.netfinworks.site.core.dal.daointerface.AutoFundoutOrderDAO;
import com.netfinworks.site.core.dal.dataobject.AutoFundoutOrderDO;
import com.netfinworks.site.service.facade.api.AutoFundoutFacade;
import com.netfinworks.site.service.facade.model.AutoFundout;
import com.netfinworks.site.service.facade.response.AutoFundoutResponse;

public class AutoFundoutServiceImpl implements AutoFundoutFacade {

	private Logger logger = LoggerFactory
			.getLogger(AutoFundoutServiceImpl.class);

	@Resource(name = "autoFundoutOrderDAO")
	private AutoFundoutOrderDAO autoFundoutOrderDAO;

	public AutoFundoutOrderDAO getAutoFundoutOrderDAO() {
		return autoFundoutOrderDAO;
	}

	public void setAutoFundoutOrderDAO(AutoFundoutOrderDAO autoFundoutOrderDAO) {
		this.autoFundoutOrderDAO = autoFundoutOrderDAO;
	}

	@Override
	public AutoFundoutResponse queryByMemberId(String memberId) {

		long beginTime = 0L;
		if (logger.isInfoEnabled()) {
			logger.info("查询出款信息，请求参数：{}", memberId);
			beginTime = System.currentTimeMillis();
		}

		AutoFundoutResponse response = new AutoFundoutResponse();
		List<AutoFundout> orders = new ArrayList<AutoFundout>();

		if (StringUtils.isBlank(memberId)) {
			response.setResultMessage("请求参数：memberId不能为空");
			response.setSuccess(false);
			logger.info("查询出款信息,memberId不能为空");
			return response;
		}
		List<AutoFundoutOrderDO> orderDO = autoFundoutOrderDAO
				.queryByMemberId(memberId);
		if (orderDO != null) {
			for (AutoFundoutOrderDO autoFundoutOrderDO : orderDO) {
				AutoFundout order = new AutoFundout();
				BeanUtils.copyProperties(autoFundoutOrderDO, order);
				orders.add(order);
			}
			response.setAutoFundOutlist(orders);
		}

		if (logger.isInfoEnabled()) {
			long consumeTime = System.currentTimeMillis() - beginTime;
			logger.info("查询出款信息， 耗时:{} (ms); 响应结果:{} ", consumeTime, response);
		}
		response.setResultMessage("查询出款信息成功");
		response.setSuccess(true);
		return response;
	}

}
