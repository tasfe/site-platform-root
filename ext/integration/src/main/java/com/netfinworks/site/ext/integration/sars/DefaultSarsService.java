package com.netfinworks.site.ext.integration.sars;

import java.util.Map;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.sars.SarsResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface DefaultSarsService {
	
	/**
	 * 风控验证
	 * @param type
	 * @param member
	 * @param env
	 * @throws BizException
	 */
    public SarsResponse verify(String type,BaseMember member,OperationEnvironment env)  throws BizException;
    
    public SarsResponse riskControl(String strategy,String scene,Map<String,String> params,OperationEnvironment env)throws BizException;
}
