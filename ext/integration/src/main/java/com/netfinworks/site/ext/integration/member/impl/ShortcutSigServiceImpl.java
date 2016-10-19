package com.netfinworks.site.ext.integration.member.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.member.ShortcutSigService;
import com.yongda.supermag.core.common.utils.OperatEnvironment;
import com.yongda.supermag.facade.api.AgreementFacade;
import com.yongda.supermag.facade.request.AgreementAdvanceRequest;
import com.yongda.supermag.facade.request.AgreementRequest;
import com.yongda.supermag.facade.response.AgreementAdvanceResponse;
import com.yongda.supermag.facade.response.AgreementResponse;

/**
 * 快捷签约
 * @author admin
 *
 */
@Service("shortcutSigService")
public class ShortcutSigServiceImpl implements ShortcutSigService{
	private Logger             logger = LoggerFactory.getLogger(ShortcutSigServiceImpl.class);
	@Resource(name = "agreementFacade")
    private AgreementFacade agreementFacade;
	
	@Override
	public AgreementResponse sign(AgreementRequest req, OperatEnvironment env) throws BizException {
		try{
			long beginTime = 0L;
	        if (logger.isInfoEnabled()) {
	            logger.info("快速签约，请求参数：{}", req);
	            beginTime = System.currentTimeMillis();
	        }
	        AgreementResponse response = agreementFacade.sign(req, env);
	        if (logger.isInfoEnabled()) {
	            long consumeTime = System.currentTimeMillis() - beginTime;
	            logger.info("远程快速签约， 耗时:{} (ms); 响应结果:{} ",
	                    new Object[] { consumeTime, response });
	        }
	        
	        if (response.isSuccess()) {
                return response;
            }
            else {
                throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResultMessage());
            }
		}catch(Exception e){
			if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("快捷签约:异常信息：{},{}"+e.getMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
		}
	}

	/**
	 * 签约推荐接口
	 */
	@Override
	public AgreementAdvanceResponse signAdvance(AgreementAdvanceRequest req,
			OperatEnvironment env) throws BizException {
		
		try{
			long beginTime = 0L;
	        if (logger.isInfoEnabled()) {
	            logger.info("签约推进，请求参数：{}", req);
	            beginTime = System.currentTimeMillis();
	        }
	        AgreementAdvanceResponse response = agreementFacade.signAdvance(req, env);
	        if (logger.isInfoEnabled()) {
	            long consumeTime = System.currentTimeMillis() - beginTime;
	            logger.info("签约推进， 耗时:{} (ms); 响应结果:{} ",
	                    new Object[] { consumeTime, response });
	        }
	        
	        if (response.isSuccess()) {
                return response;
            }
            else {
                throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, response.getResultMessage());
            }
		}catch(Exception e){
			if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("签约推进:异常信息：{},{}"+e.getMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
		}
	}

}
