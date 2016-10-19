package com.netfinworks.site.domainservice.pos;

import com.netfinworks.site.core.dal.dataobject.PosTradeDO;
import com.netfinworks.site.domain.exception.BizException;

/**传统pos交易查询接口**/
public interface PosTradeService {
	
   public PosTradeResponse selectPosList(PosTradeRequest req);
   
}
