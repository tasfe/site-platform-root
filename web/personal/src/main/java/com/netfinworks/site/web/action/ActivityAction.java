package com.netfinworks.site.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.site.core.common.constants.CommonConstant;

/**
 * 活动
 * @author tangL
 * @date 2014-01-06
 */
@Controller
@RequestMapping("activity")
public class ActivityAction {
	/**
	 * 话费充值
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/phonerecharge.htm", method = RequestMethod.GET)
	public ModelAndView phonerecharge(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView(CommonConstant.URL_PREFIX+ "/activity/phonerecharge");

	}
}
