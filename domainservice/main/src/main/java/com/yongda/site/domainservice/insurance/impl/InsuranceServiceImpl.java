package com.yongda.site.domainservice.insurance.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.netfinworks.site.core.dal.daointerface.InsuranceOrderDAO;
import com.netfinworks.site.core.dal.dataobject.InsuranceOrderDO;
import com.netfinworks.site.core.dal.dataobject.InsuranceOrderDOExample;
import com.netfinworks.site.core.dal.dataobject.InsuranceOrderDOExample.Criteria;
import com.netfinworks.site.domainservice.convert.InsuranceOrderConvert;
import com.yongda.site.domain.domain.insurance.InsuranceOrder;
import com.yongda.site.domain.domain.insurance.InsuranceQuery;
import com.yongda.site.domain.domain.insurance.InsuranceQueryResult;
import com.yongda.site.domainservice.insurance.InsuranceService;

public class InsuranceServiceImpl implements InsuranceService{

	@Resource(name = "insuranceOrderDAO")
    private InsuranceOrderDAO insuranceOrderDAO;
	
	@Override
	public boolean insert(InsuranceOrder record) {
		InsuranceOrderDO order = InsuranceOrderConvert.to(record);
		return insuranceOrderDAO.insert(order)>0?true:false;
	}
	
	@Override
	public InsuranceOrder queryDetail(Long id,String memberId) {
		InsuranceOrderDOExample example = new InsuranceOrderDOExample();
		example.createCriteria().andMemberIdEqualTo(memberId);
		example.createCriteria().andIdEqualTo(id);
		List<InsuranceOrderDO> details = insuranceOrderDAO.selectByExample(example);
		if(CollectionUtils.isNotEmpty(details))
			return InsuranceOrderConvert.from(details.get(0));
		return null;
	}

	@Override
	public boolean deleteByPrimaryKey(Long id,String memberId) {
		InsuranceOrderDOExample example = new InsuranceOrderDOExample();
		example.createCriteria().andMemberIdEqualTo(memberId);
		example.createCriteria().andIdEqualTo(id);
		insuranceOrderDAO.deleteByExample(example);
		return insuranceOrderDAO.deleteByExample(example)>0?true:false;
	}

	@Override
	public InsuranceOrder queryDetail(String memberId, String bxgsId,
			String company) {
		InsuranceOrderDOExample example = new InsuranceOrderDOExample();
		Criteria criteria = example.createCriteria();
		criteria.andMemberIdEqualTo(memberId);
		criteria.andBxgsidEqualTo(bxgsId);
		criteria.andCompanyEqualTo(company);
		List<InsuranceOrderDO> details = insuranceOrderDAO.selectByExample(example);
		if(CollectionUtils.isNotEmpty(details))
			return InsuranceOrderConvert.from(details.get(0));
		return null;
	}
	
	@Override
	public boolean existInsuranceOrder(String memberId, String bxgsId,
			String company) {
		InsuranceOrder order = this.queryDetail(memberId, bxgsId, company);
		return order!=null?true:false;
	}

	@Override
	public InsuranceQueryResult queryByPage(InsuranceQuery query) {
		InsuranceQueryResult result = new InsuranceQueryResult();
		List<InsuranceOrder> datas = new ArrayList<InsuranceOrder>();
		Map<String, Object> params = new HashMap<String, Object>();
		if(StringUtils.isBlank(query.getBxgsId())){
			params.put("bxgsid", query.getBxgsId());
		}
		if(StringUtils.isBlank(query.getCompany())){
			params.put("company", query.getCompany());
		}
		if(StringUtils.isBlank(query.getMemberId())){
			params.put("memberId", query.getMemberId());
		}
		if(StringUtils.isBlank(query.getStatus())){
			params.put("status", query.getStatus());
		}
		if(query.getQueryBase() != null){
			int count = insuranceOrderDAO.count(params);
			query.getQueryBase().setTotalItem(count);
	        if (query.getQueryBase().getEndRow() == 0) {
	        	query.getQueryBase().setStartRow(query.getQueryBase().getPageFristItem());
	        	query.getQueryBase().setEndRow(query.getQueryBase().getPageLastItem());
	        }
	        params.put("queryBase", query.getQueryBase());
		}
		List<InsuranceOrderDO> list = insuranceOrderDAO.query(params);
		for(InsuranceOrderDO data:list){
			datas.add(InsuranceOrderConvert.from(data));
		}
		result.setOrders(datas);
		result.setQueryBase(query.getQueryBase());
		return result;
	}

}
