/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月6日
 */
package com.netfinworks.site.web.action.app;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * 应用中心
 * @author xuwei
 * @date 2014年8月6日
 */
@Controller
@RequestMapping("/appCenter")
public class AppCenterAction extends BaseAction {
	
	/**资管开通的商户号*/
	@Value("${fmsMemchantCode}")
 	private String fmsMemchantCode;
	
	@RequestMapping("/toAppCenter.htm")
	public ModelAndView toAppCenter(HttpServletRequest request, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		EnterpriseMember user = getUser(request);
		String flag="";
		//判断此商户是否开通资管应用
		if(fmsMemchantCode.indexOf(user.getMemberId())!=-1){
			flag="yes";
		}else{
			flag="no";
		}
		data.put("flag", flag);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/appCenter/app-center","response", restP);
	}
	
	@RequestMapping(value = "/downloadGatewayCertificate.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse toModifySummary(HttpServletRequest request, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		restP.setSuccess(true);
		return restP;
	}

}
