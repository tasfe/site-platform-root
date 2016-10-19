/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月7日
 */
package com.netfinworks.site.web.action.money;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * @author xuwei
 * @date 2014年7月7日
 */
@Controller
@RequestMapping("/overview")
public class OverViewAction extends BaseAction {
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
	/**
	 * 进入资金管理总览页面
	 * @return
	 */
	@RequestMapping("/toOverview")
	public ModelAndView toOverview(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		
		// 查询可用余额、冻结资金
		try {
			// 先获取用户信息
			EnterpriseMember user = getUser(request);
			
			// 根据用户ID查询会员账户
			MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			
			mav.addObject("account", account);
		} catch (Exception e) {
			logger.error("查询可用余额、冻结资金失败", e);
		}
		
		mav.setViewName(CommonConstant.URL_PREFIX + "/overview/overview");
		return mav;
	}
	
	/**
	 * 查询一年内的收支情况，按月份统计
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryIncomingAndOutgoing")
	public RestResponse queryIncomingAndOutgoing() {
		// 后台服务层缺少提供该查询服务的接口，将该接口规格考虑好整理到文档中
		RestResponse restResp = new RestResponse();
		restResp.setSuccess(true);
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("incoming", new double[] {8000.05, 8000.05, 8009.5, 8014.5, 18.4, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6});
		dataMap.put("outgoing", new double[] {3.9, 4.2, 5.7, 5000.5, 8001.9, 800.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8});
		restResp.setData(dataMap);
		return restResp;
	}
	
	/**
	 * 查询上月支出，按类型统计
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryLastMonthOutgoing")
	public RestResponse queryLastMonthOutgoing() {
		// 后台服务层缺少提供该查询服务的接口，将该接口规格考虑好整理到文档中
		RestResponse restResp = new RestResponse();
		restResp.setSuccess(true);
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("transfer", 36.0);
		dataMap.put("salary", 24.0);
		dataMap.put("order", 10.0);
		dataMap.put("cash", 20.0);
		dataMap.put("refund", 10.0);
		restResp.setData(dataMap);
		return restResp;
	}
}
