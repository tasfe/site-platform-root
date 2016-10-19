package com.netfinworks.site.web.action.ocx;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;
import com.netfinworks.site.web.action.common.BaseAction;

@Controller
public class PasswordOcxAction extends BaseAction {

    @Resource
    private PasswordOcxService passwordOcxService;
    
    @RequestMapping(value = "/ocx/generateRandomKey.htm")
    @ResponseBody
    public String index(HttpServletRequest request, HttpServletResponse resp) throws Exception {
        String mcrypt_key = passwordOcxService.generateRandomKey(32);
        request.getSession().setAttribute("mcrypt_key", mcrypt_key);
        return mcrypt_key;
    }
                             
}
