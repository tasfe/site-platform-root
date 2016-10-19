package com.netfinworks.site.web.action;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.web.action.common.BaseAction;

@Controller
public class CustomAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(CustomAction.class);

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@RequestMapping(value = "/customservice/index.htm")
	public ModelAndView main(HttpServletRequest request, HttpServletResponse response) {
		RestResponse restP = new RestResponse();
		return new ModelAndView(CommonConstant.URL_PREFIX + "/customservice/customservice", "response", restP);
	}

	@RequestMapping(value = "/customservice/sendEmail.htm")
	public ModelAndView sendEmail(HttpServletRequest request, HttpServletResponse response) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try{
			String rs = KaptchaImageAction.validateRandCode(request, request.getParameter("captcha_value"));
			if (!"success".equals(rs)) {
				if("expire".equals(rs))
					restP.setMessage("验证码失效");
				else
					restP.setMessage("验证码错误");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/customservice/customresult", "response", restP);
			}
			String ms_email = request.getParameter("ms_email");
			String ms_advice = request.getParameter("ms_advice");
			String ms_words = request.getParameter("ms_words");

			String bizType = BizType.CUSTOM_EMAIL.getCode();

			Map<String, Object> objParams = new HashMap<String, Object>();
			objParams.put("ms_email", ms_email);
			objParams.put("ms_advice", ms_advice);
			objParams.put("ms_words", ms_words);
			boolean emailResult = defaultPayPasswdService.sendEmail(CommonConstant.CUSTOM_EMAIL, bizType, objParams);
			logger.info("发送至{}邮箱的结果：{}", CommonConstant.CUSTOM_EMAIL, emailResult);
			if (emailResult) {
				restP.setSuccess(true);
				data.put("email", ms_email);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/customservice/customresult", "response", restP);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			restP.setMessage("邮件发送失败，请重试！");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/customservice/customresult", "response", restP);
	}
}
