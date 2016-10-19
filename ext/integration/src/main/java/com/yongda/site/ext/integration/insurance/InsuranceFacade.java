package com.yongda.site.ext.integration.insurance;

import com.netfinworks.site.domain.exception.ErrorCodeException;
import com.yongda.site.domain.domain.insurance.InsuranceAuthResult;
import com.yongda.site.domain.domain.insurance.InsuranceRemoteQuery;
import com.yongda.site.domain.domain.insurance.InsuranceRemoteQueryResult;

/** 
 * 宝贝科技接口相关
* @ClassName: InsuranceService 
* @Description: TODO(保险远程服务) 
* @author slong
* @date 2016年5月20日 上午10:14:40 
*  
*/
public interface InsuranceFacade {
    
    public InsuranceRemoteQueryResult queryInsurance(InsuranceRemoteQuery query)throws ErrorCodeException.CommonException;
    
    public InsuranceAuthResult queryAuthToken(String memberId)throws ErrorCodeException.CommonException;
}
