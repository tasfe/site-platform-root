package com.netfinworks.site.web.action.securityCenter;

import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * 
 * @author chengwen
 *
 */
@Controller
@RequestMapping("/securityCenter")
public class PersonSecurityCenterAction extends BaseAction {
	
    @Resource(name = "certificationService")
	private CertificationService certificationService;
    
    @RequestMapping(value = "/safetyIndex.htm", method = RequestMethod.GET)
    public ModelAndView safetyIndex() {
    	ModelAndView mv = new ModelAndView();
    	mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/safetyIndex");
    	return mv;
    }
    
    
    @RequestMapping(value = "/error.htm", method = RequestMethod.POST)
	public ModelAndView errorPage() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("message", "下载证书失败！");
		mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
		return mv;
		
	}
    
	
	
}
