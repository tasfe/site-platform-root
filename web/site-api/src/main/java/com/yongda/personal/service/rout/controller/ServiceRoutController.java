package com.yongda.personal.service.rout.controller;

import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSON;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;
import com.yongda.site.service.personal.facade.api.IRoutService;
import com.yongda.site.service.personal.facade.api.RoutServiceManager;
import com.yongda.site.service.personal.facade.response.BaseResponse;

@Controller
public class ServiceRoutController{
	
	public static final String INPUT_CHARSET 	= "_input_charset";
	public static final String SERVICE 			= "service";
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceRoutController.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/rout")
	protected ModelAndView handleRequest(HttpServletRequest request,HttpServletResponse response) throws Exception {
		BaseResponse resp = null;
		try{
			Map<?, ?> parameters = request.getParameterMap();
			logger.info("method:{},request={}",request.getMethod(),JSON.toJSONString(parameters));
			if (parameters == null || parameters.isEmpty() == true) {
				// 什么参数都没有
				throw CommonDefinedException.REQUIRED_FIELD_NOT_EXIST;
			}
			Charset charset = getEncoding(request);
	
			Map<String, String> formattedParameters = new HashMap<String, String>(
					parameters.size());
			for (Map.Entry<?, ?> entry : parameters.entrySet()) {
				if (entry.getValue() == null
						|| Array.getLength(entry.getValue()) == 0) {
					formattedParameters.put((String) entry.getKey(), null);
				} else {
					if (HttpMethod.GET.name().equals(request.getMethod())) {
						formattedParameters.put((String) entry.getKey(),
								(String) Array.get(entry.getValue(), 0));
					} else {
						formattedParameters
								.put((String) entry.getKey(), URLDecoder
										.decode((String) Array.get(
												entry.getValue(), 0),
												charset.name()));
					}
				}
			}
			// 防XSS攻击，去掉“<”和“>”
			//formattedParameters = auniXSS(formattedParameters);
			String serviceName = getServiceName(formattedParameters);
			//路由
			IRoutService service = RoutServiceManager.routByName(serviceName);
			if(service == null)//服务不存在
				throw CommonDefinedException.ILLEGAL_SERVICE;
			//调用服务
			resp = service.process(formattedParameters);
		}catch(Exception e){
			logger.error("服务错误 ",e);
			if(e instanceof CommonException){
				resp = buildRespFromExcp((CommonException)e);
			}else{
				resp = buildRespFromExcp(CommonDefinedException.SYSTEM_ERROR);
			}
		}finally{
			fillJsonResp(response, resp);
		}
		return null;
	}

	private Charset getEncoding(HttpServletRequest request) {
		Charset utf8Charset = Charset.forName("UTF-8");
		Charset gbkCharset = Charset.forName("GBK");
		Charset gb2312Charset = Charset.forName("GB2312");
		String inputCharset = request.getParameter(INPUT_CHARSET);
		if ("utf-8".equalsIgnoreCase(inputCharset)) {
			return utf8Charset;
		} else if ("gbk".equalsIgnoreCase(inputCharset)) {
			return gbkCharset;
		} else if ("gb2312".equalsIgnoreCase(inputCharset)) {
			return gb2312Charset;
		}
		return utf8Charset;
	}

	private String getServiceName(Map<String, String> parameters)
			throws Exception {
		String serviceName = parameters.get(SERVICE);
		if (StringUtil.isNotBlank(serviceName)) {
			serviceName = serviceName.toLowerCase();
		} else {
			CommonException exp = CommonDefinedException.REQUIRED_FIELD_NOT_EXIST;
			exp.setMemo("接口名称");
			throw exp;
		}
		return serviceName;
	}
	
	private void fillJsonResp(HttpServletResponse response,BaseResponse resp) throws Exception {
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		response.getWriter().write(JSON.toJSONString(resp));
	}

	private Map<String, String> auniXSS(Map<String, String> reqMap) {
		Map<String, String> result = new HashMap<String, String>(reqMap.size());
		for (Map.Entry<String, String> entry : reqMap.entrySet()) {
			if (entry.getValue() == null) {
				result.put((String) entry.getKey(), null);
			} else {
				String value = entry.getValue().replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
						.replaceAll("\"", "&quot;").replaceAll("'", "&acute;");
				result.put((String) entry.getKey(), value);
			}
		}
		return result;
	}
	
	private BaseResponse buildRespFromExcp(CommonException e){
		BaseResponse resp = new BaseResponse();
		resp.setErrorCode(e.getErrorCode());
		resp.setErrorMessage(e.getErrorMsg());
		resp.setSuccess(false);
		return resp;
	}
}
