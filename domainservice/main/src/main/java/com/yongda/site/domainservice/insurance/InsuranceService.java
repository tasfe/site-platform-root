package com.yongda.site.domainservice.insurance;

import com.yongda.site.domain.domain.insurance.InsuranceOrder;
import com.yongda.site.domain.domain.insurance.InsuranceQuery;
import com.yongda.site.domain.domain.insurance.InsuranceQueryResult;

public interface InsuranceService {
	
	public boolean insert(InsuranceOrder record);

	public boolean deleteByPrimaryKey(Long id,String memberId);
	
	public InsuranceOrder queryDetail(String memberId,String bxgsId,String company);
	
	public InsuranceOrder queryDetail(Long id,String memberId);
    
    public InsuranceQueryResult queryByPage(InsuranceQuery query);
    
    public boolean existInsuranceOrder(String memberId,String bxgsId,String company);
}
