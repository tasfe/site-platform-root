package com.netfinworks.site.ext.integration.member;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.exception.BizException;
import com.yongda.supermag.core.common.utils.OperatEnvironment;
import com.yongda.supermag.facade.request.AgreementAdvanceRequest;
import com.yongda.supermag.facade.request.AgreementRequest;
import com.yongda.supermag.facade.response.AgreementAdvanceResponse;
import com.yongda.supermag.facade.response.AgreementResponse;

/**
 * 快捷签约 
 * @author admin
 *
 */
public interface ShortcutSigService {
	/**
	 * 签约接口
	 * @param req
	 * @param env
	 * @return
	 */
	public AgreementResponse sign(AgreementRequest req,OperatEnvironment env)throws BizException;
	
	/**
	 * 签约推进接口
	 * @param request
	 * @param env
	 * @return
	 */
	public AgreementAdvanceResponse signAdvance(AgreementAdvanceRequest req, OperatEnvironment env) throws BizException;
}
