package com.yongda.site.ext.integration.insurance.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.site.core.common.util.DateUtils;
import com.netfinworks.site.core.common.util.HttpClientUtil;
import com.netfinworks.site.core.common.util.TripleDES;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException;
import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;
import com.netfinworks.site.ext.integration.voucher.convert.VoucherConvertor;
import com.netfinworks.voucher.service.facade.request.RecordingRequest;
import com.yongda.site.domain.domain.insurance.InsuranceAuthResult;
import com.yongda.site.domain.domain.insurance.InsuranceOrder;
import com.yongda.site.domain.domain.insurance.InsuranceRemoteQuery;
import com.yongda.site.domain.domain.insurance.InsuranceRemoteQueryResult;
import com.yongda.site.ext.integration.insurance.InsuranceFacade;

public class InsuranceFacadeImpl implements InsuranceFacade{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String queryUrl;
	private String token;
	private String sKey;
	private String key;
	private String vi;
	

	@Override
	public InsuranceRemoteQueryResult queryInsurance(InsuranceRemoteQuery query)
			throws ErrorCodeException.CommonException {
		InsuranceRemoteQueryResult response = new InsuranceRemoteQueryResult();
		try {
			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("token", token);
			Map<String,String> queryParams = new HashMap<String,String>();
			queryParams.put("sKey", sKey);
			queryParams.put("company", query.getCompany());
			queryParams.put("policyNo", query.getPolicyNo());
			queryParams.put("cardNo", query.getCardNo());
			queryParams.put("insuredName", query.getInsuredName());
			if(StringUtils.isNotBlank(query.getInsruedMobile()))
				queryParams.put("insuredMobile", query.getInsruedMobile());
			if(StringUtils.isNotBlank(query.getCarEngineId()))
				queryParams.put("carEngineId", query.getCarEngineId());
			String strJson = JSON.toJSONString(queryParams);
			logger.info("车险请求 req：{} ",strJson);
			postParams.put("arguJson",TripleDES.encryptString(strJson, key, vi));
			PostMethod method = HttpClientUtil.buildPostFormMethod(queryUrl+"/query", postParams, "UTF-8");
			String resultJson = HttpClientUtil.doPostAsString(method, "UTF-8");
			resultJson = TripleDES.decryptString(resultJson, key, vi);
			JSONObject resultObj = JSON.parseObject(resultJson);
			response.setSuccess(resultObj.getBooleanValue("status"));
			response.setReturnCode(resultObj.getString("code"));
			if(StringUtils.equalsIgnoreCase("0", response.getReturnCode())){
				response.setReturnCode("SUCCESS");
				response.setReturnMessage("查询成功");
				response.setOrders(new ArrayList<InsuranceOrder>());
				JSONObject dataObj = resultObj.getJSONObject("data");
				InsuranceOrder order = new InsuranceOrder();
				order.setBxgsid(dataObj.getString("BXGSid"));
				order.setCompany(query.getCompany());
				order.setCompanyName(dataObj.getString("Company"));
				order.setName(dataObj.getString("Name"));
				order.setPolicytype(dataObj.getString("PolicyType"));
				order.setSecuritymoney(dataObj.getString("SecurityMoney"));
				order.setCurrencytype(dataObj.getString("CurrencyType"));
				order.setApplicant(dataObj.getString("Applicant"));
				order.setAtype(dataObj.getString("AType"));
				order.setAidcard(dataObj.getString("AIDCard"));
				order.setAphone(dataObj.getString("APhone"));
				order.setBinsured(dataObj.getString("BInsured"));
				order.setBtype(dataObj.getString("BType"));
				order.setBidcard(dataObj.getString("BIDCard"));
				order.setBphone(dataObj.getString("BPhone"));
				order.setStartdate(DateUtils.formatStringToDate(dataObj.getString("StartDate"),"yyyy-MM-dd"));
				order.setEnddate(DateUtils.formatStringToDate(dataObj.getString("EndDate"),"yyyy-MM-dd"));
				order.setStatus(dataObj.getString("Status"));
				order.setSpeciltype(dataObj.getString("SpecilType"));
				order.setPid(dataObj.getString("Pid"));
				order.setLiabilitys(dataObj.getString("Liabilitys"));
				order.setMenucon(dataObj.getString("menuCon"));
				order.setCreateTime(new Date());
				response.getOrders().add(order);
			}else if(StringUtils.equalsIgnoreCase("10005", response.getReturnCode())){
				response.setReturnCode(CommonDefinedException.NOT_SUPPORT_COMPANY.getErrorCode());
				response.setReturnMessage("暂不支持此公司");
			}else if(StringUtils.equalsIgnoreCase("10003", response.getReturnCode())){
				response.setReturnCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				response.setReturnMessage("参数错误");
			}else if(StringUtils.equalsIgnoreCase("10000", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10000)sKey校验失败");
			}else if(StringUtils.equalsIgnoreCase("10001", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10001)json参数格式错误");
			}else if(StringUtils.equalsIgnoreCase("10002", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10002)解密数据失败");
			}else if(StringUtils.equalsIgnoreCase("10004", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10004)内部错误，稍后再试");
			}else if(StringUtils.equalsIgnoreCase("10006", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10006)token 不匹配");
			}
		} catch (Exception e) {
			logger.error("远程调用保险接口异常 ",e);
			throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"远程调用保险接口异常");
		}
		return response;
	}

	
	@Override
	public InsuranceAuthResult queryAuthToken(String memberId) throws CommonException {
		InsuranceAuthResult response = new InsuranceAuthResult();
		long beginTime = System.currentTimeMillis();
		try {
			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("token", token);
			Map<String,String> queryParams = new HashMap<String,String>();
			queryParams.put("sKey", sKey);
			queryParams.put("uniqId", memberId);
			String strJson = JSON.toJSONString(queryParams);
			logger.info("车险授权请求 req：{} ",strJson);
			postParams.put("arguJson",TripleDES.encryptString(strJson, key, vi));
			PostMethod method = HttpClientUtil.buildPostFormMethod(queryUrl+"/auth", postParams, "UTF-8");
			String resultJson = HttpClientUtil.doPostAsString(method, "UTF-8");
			resultJson = TripleDES.decryptString(resultJson, key, vi);
			JSONObject resultObj = JSON.parseObject(resultJson);
			response.setSuccess(resultObj.getBooleanValue("status"));
			response.setReturnCode(resultObj.getString("code"));
			if(StringUtils.equalsIgnoreCase("0", response.getReturnCode())){
				response.setReturnCode("SUCCESS");
				response.setReturnMessage("查询成功");
				JSONObject dataObj = resultObj.getJSONObject("data");
				response.setToken(dataObj.getString("token"));
			}else if(StringUtils.equalsIgnoreCase("10003", response.getReturnCode())){
				response.setReturnCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				response.setReturnMessage("参数错误");
			}else if(StringUtils.equalsIgnoreCase("10000", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10000)sKey校验失败");
			}else if(StringUtils.equalsIgnoreCase("10001", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10001)json参数格式错误");
			}else if(StringUtils.equalsIgnoreCase("10002", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10002)解密数据失败");
			}else if(StringUtils.equalsIgnoreCase("10006", response.getReturnCode())){
				throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"(10006)token 不匹配");
			}
		} catch (Exception e) {
			logger.error("远程调用保险授权接口异常 ",e);
			throw new ErrorCodeException.CommonException(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),"远程调用保险授权接口异常");
		}finally{
			long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("车险授权请求 耗时:{} (ms)",consumeTime);
		}
		return response;
	}



	public String getQueryUrl() {
		return queryUrl;
	}

	public void setQueryUrl(String queryUrl) {
		this.queryUrl = queryUrl;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getsKey() {
		return sKey;
	}

	public void setsKey(String sKey) {
		this.sKey = sKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getVi() {
		return vi;
	}

	public void setVi(String vi) {
		this.vi = vi;
	}
}
