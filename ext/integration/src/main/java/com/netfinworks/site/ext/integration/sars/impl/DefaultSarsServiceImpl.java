package com.netfinworks.site.ext.integration.sars.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.cert.service.facade.ICertUdcreditFacade;
import com.netfinworks.cert.service.request.UdcreditRequest;
import com.netfinworks.cert.service.response.UdcreditResponseV3;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.sars.client.api.SarsClientService;
import com.netfinworks.sars.client.api.VerifyResult;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.sars.SarsResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.sars.DefaultSarsService;
import com.netfinworks.site.ext.integration.sars.convert.SarsConvert;

@Service("defaultSarsService")
public class DefaultSarsServiceImpl implements DefaultSarsService{	

	private Logger logger = LoggerFactory.getLogger(DefaultSarsServiceImpl.class);	
	
	@Resource(name = "sarsService")
    private SarsClientService sarsService;
	
	@Resource(name = "certUdcreditFacade")
    private ICertUdcreditFacade certUdcreditFacade;
	
	@Override
	public SarsResponse verify(String type, BaseMember member, OperationEnvironment env)
			throws BizException {
     
		try {
			long beginTime = 0L;		
			if (logger.isInfoEnabled()) {
				logger.info("风控验证，请求参数类型 ：{}", type);
				beginTime = System.currentTimeMillis();
			}
			
			Map<String, Object> map=SarsConvert.createRequest(type, member, env);
			VerifyResult verifyResult=sarsService.verify(map);		
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用风控验证， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, verifyResult.getResult() });
			}
			SarsResponse response=new SarsResponse();
			
			if(null!=verifyResult){
				response.setResult(verifyResult.getResult());
				response.setMsg(verifyResult.getMsg());
			}
			return response;
		} catch (Exception e) {
			  logger.error("请求风控验证异常:请求信息", e);
              throw new BizException(ErrorCode.SYSTEM_ERROR);
		}
		
	}

	@Override
	public SarsResponse riskControl(String strategy, String scene,
			Map<String, String> params,OperationEnvironment env) throws BizException {
		try {
			long beginTime = 0L;		
			if (logger.isInfoEnabled()) {
				logger.info("风控验证，请求参数类型 ：strategy={},scene={},params={}", strategy,scene,params);
				beginTime = System.currentTimeMillis();
			}
			UdcreditRequest request = new UdcreditRequest();
			request.setScenarioCode(scene);
			request.setStrategyCode(strategy);
			request.setPackageMap(params);
			UdcreditResponseV3 resp = certUdcreditFacade.udcreditDo(request, env);		
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用风控验证， 耗时:{} (ms); 响应结果:{} ",consumeTime, resp);
			}
			SarsResponse response=new SarsResponse();
			if (resp==null) {//resp为null的异常情况都放行
				response.setResult("0000");
				response.setMsg("风控调用异常");
	        } else if(StringUtils.equalsIgnoreCase(resp.getRetCode(),"0000")) {//放行
	        	response.setResult("0000");
	        	response.setMsg("建议放行");
	        }else{
	        	response.setResult(resp.getRetCode());
	        	response.setMsg(resp.getRetMsg());
	        }
			return response;
		} catch (Exception e) {//UdcreditException
			  logger.error("请求风控验证异常:请求信息", e);
              throw new BizException(ErrorCode.SYSTEM_ERROR);
		}
	}
}
