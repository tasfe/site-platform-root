/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月6日
 */
package com.netfinworks.site.web.action.app;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * 应用中心 商品管理
 * @author yp
 * @date 2016年4月11日
 */
@Controller
public class AppGoodsAction extends BaseAction {

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Resource(name = "accountService")
	private AccountService accountService;


    @RequestMapping(value = "/appCenter/{page}.htm")
    public ModelAndView goods(@PathVariable(value="page")String page,HttpServletRequest req, HttpServletResponse resp,
                              OperationEnvironment env) throws Exception {
    	String refresh = req.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(req);
		}
    	RestResponse restP = new RestResponse();
    	EnterpriseMember member = null;
    	Map<String, String> errorMap = new HashMap<String, String>();
		restP.setData(new HashMap<String, Object>());
		EnterpriseMember user = getUser(req);
		
		checkUser(user, errorMap, restP);
		// 查询会员所以需要的信息
		member = defaultMemberService.queryCompanyMember(user, env);
		restP.getData().put("member", member);
		
    	if(StringUtils.equalsIgnoreCase("goods", page)){
    		return new ModelAndView(CommonConstant.URL_PREFIX + "/goods/goods", "response", restP);
    	}else if(StringUtils.equalsIgnoreCase("import", page)){
    		return new ModelAndView(CommonConstant.URL_PREFIX + "/goods/importGoods", "response", restP);
    	}else if(StringUtils.equalsIgnoreCase("increase", page)){
    		return new ModelAndView(CommonConstant.URL_PREFIX + "/goods/increaseGoods", "response", restP);
    	}
    	return null;
    }

}
