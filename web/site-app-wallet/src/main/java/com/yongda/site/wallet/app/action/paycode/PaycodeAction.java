package com.yongda.site.wallet.app.action.paycode;

import java.util.HashMap;



import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.yongda.site.ext.service.facade.personal.common.OneTimePasswordAlgorithm;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.util.ResponseUtil;

@Controller
@RequestMapping(value = "/paycode")
public class PaycodeAction extends BaseAction {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private OneTimePasswordAlgorithm oneTimePasswordAlgorithm;
	
	@RequestMapping("/getPaycode")
	@ResponseBody
	public RestResponse getPaycode(HttpServletRequest request, HttpServletResponse response) {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		Map map = new HashMap();
//		String cookieToken = null;
//		Cookie[] cookies = request.getCookies();
//		for(Cookie c :cookies ){
//			if (c.getName().equals("com.vfsso.cas.token")){
//				cookieToken = c.getValue();
//			}
//		}
		PersonMember user = getUser(request);
		String memberId = user.getMemberId();
		logger.info("获取paycode请求参数，memberId：{}", memberId);
		String paycode = oneTimePasswordAlgorithm.generatePaycode(memberId);
		logger.info("获取paycode响应结果，result：{}", paycode);
		if(StringUtils.isBlank(paycode)) {
			restP.setSuccess(false);
			return restP;
		}
		restP.setSuccess(true);
		map.put("paycode", paycode);
		restP.setData(map);
		return restP;
		
	}
}
