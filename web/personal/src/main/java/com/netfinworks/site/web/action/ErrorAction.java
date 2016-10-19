package com.netfinworks.site.web.action;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * <p>错误处理</p>
 * @author dexter.qin
 * @version $Id: LoginAction.java, v 0.1 2013-7-18 下午6:07:43  Exp $
 */
@Controller
public class ErrorAction extends BaseAction {

    @Resource(name = "webResource")
    private WebDynamicResource      webResource;

    /**
     * 进入错误页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/error.htm", method = RequestMethod.GET)
    public ModelAndView error(HttpServletRequest req, HttpServletResponse resp, ModelMap model) {
        HttpSession session = req.getSession();
        PersonMember user = getUser(req);
        String msg = req.getParameter("error");
        String memberName = req.getParameter("memberName");
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        if(user != null) {
			data.put("member", user);
		}
        data.put("memberName", memberName);
        restP.setData(data);
        model.put("response", user);
        model.put("error", msg);
        return new ModelAndView(ResourceInfo.ERROR.getUrl(), "response", restP);
    }
    
    /**
	 * 进入错误页面
	 *
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loginTypeError.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public ModelAndView loginError(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		 HttpSession session = req.getSession();
	        PersonMember user = getUser(req);
	        String msg = req.getParameter("error");
	        String memberName = req.getParameter("memberName");
	        RestResponse restP = new RestResponse();
	        Map<String, Object> data = new HashMap<String, Object>();
	        if(user != null) {
				data.put("member", user);
			}
	        data.put("memberName", memberName);
	        restP.setData(data);
	        model.put("response", user);
	        model.put("error", msg);
	        return new ModelAndView("/common/exception/loginTypeError", "response", restP);
	}

    /**
     * 404处理
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/error/404.htm", method = RequestMethod.GET)
    public ModelAndView error404(HttpServletRequest req,HttpServletResponse resp, ModelMap model) {
        HttpSession session = req.getSession();
        PersonMember user = getUser(req);
        String msg = (String) session.getAttribute(CommonConstant.ERROR);
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("member", user);
        restP.setData(data);
        model.put("response", user);
        model.put("error", msg);
        return new ModelAndView("common/error/404", "response", restP);
    }

    /**
     * 500处理
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/error/500.htm", method = RequestMethod.GET)
    public ModelAndView error500(HttpServletRequest req,HttpServletResponse resp, ModelMap model) {
        HttpSession session = req.getSession();
        PersonMember user = getUser(req);
        String msg = (String) session.getAttribute(CommonConstant.ERROR);
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("member", user);
        restP.setData(data);
        model.put("response", user);
        model.put("error", msg);
        return new ModelAndView("common/error/500", "response", restP);
    }

	/**
	 * 进入无权限页面
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/authReject.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView authReject(HttpServletRequest req, HttpServletResponse resp, ModelMap model) {
		HttpSession session = req.getSession();
		PersonMember user = getUser(req);
		String msg = (String) session.getAttribute(CommonConstant.ERROR);
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("member", user);
		restP.setData(data);
		model.put("response", user);
		model.put("error", msg);
		return new ModelAndView("/common/exception/authReject", "response", restP);
	}
}
