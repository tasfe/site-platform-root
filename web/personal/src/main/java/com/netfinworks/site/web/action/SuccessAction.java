package com.netfinworks.site.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 *
 * <p>回调成功页面</p>
 * @author qinde
 * @version $Id: SuccessAction.java, v 0.1 2013-12-9 下午2:15:58 qinde Exp $
 */
@Controller
public class SuccessAction extends BaseAction {

    /**
     * 进入首页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/success.htm", method = RequestMethod.GET)
    public ModelAndView enter(HttpServletRequest req, HttpServletResponse resp,
                              OperationEnvironment env) throws Exception {
        String success = req.getParameter("success");
        return new ModelAndView("success", "common/success/success", success);
    }

}
