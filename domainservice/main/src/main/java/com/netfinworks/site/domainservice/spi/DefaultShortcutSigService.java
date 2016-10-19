package com.netfinworks.site.domainservice.spi;

import com.netfinworks.service.exception.ServiceException;
import com.yongda.site.service.personal.facade.request.PersonalShortcutSigRequest;
import com.yongda.site.service.personal.facade.request.PersonalSignAdvanceRequest;
import com.yongda.supermag.core.common.utils.OperatEnvironment;
import com.yongda.supermag.facade.response.AgreementAdvanceResponse;
import com.yongda.supermag.facade.response.AgreementResponse;

public interface DefaultShortcutSigService {
	/**
	 * 快捷签约
	 * 
	 * @param env
	 * @param request
	 * @return boolean
	 */
	public AgreementResponse ShortcutSig(PersonalShortcutSigRequest req, OperatEnvironment env) throws ServiceException;
	/**
	 * 签约推进接口
	 * @param request
	 * @param env
	 * @return
	 */
	public AgreementAdvanceResponse signAdvance(PersonalSignAdvanceRequest req, OperatEnvironment env) throws ServiceException;
}
