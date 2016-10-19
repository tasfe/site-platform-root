package com.netfinworks.site.web.action.paymerge;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.web.action.common.BaseAction;
@Controller
public class PaymergeAction extends BaseAction{
    @RequestMapping(value="/my/paymerge.htm", method = RequestMethod.GET)
    public String paymerge(){
        return  CommonConstant.URL_PREFIX+"/paymerge/paymerge";
    }

}
