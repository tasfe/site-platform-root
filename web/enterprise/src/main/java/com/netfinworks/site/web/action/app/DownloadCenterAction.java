/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2015年4月23日
 */
package com.netfinworks.site.web.action.app;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.ersys.facade.request.TradeListQuery;
import com.netfinworks.ersys.facade.response.TDownloadBHDO;
import com.netfinworks.ersys.service.facade.api.ErServiceFacade;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.ErTradeForm;

/**
 * 下载
 * @author Jefferson
 * @date 2015年4月23日
 */
@Controller
@RequestMapping("/downloadCenter")
public class DownloadCenterAction extends BaseAction {
	@Resource(name="erServiceFacade")
	private ErServiceFacade erServiceFacade;
	@RequestMapping("/todownloadCenter.htm")
	public ModelAndView toAppCenter(HttpServletRequest request,
			HttpServletResponse resp,ErTradeForm form) {
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		EnterpriseMember user = getUser(request);
		String id = user.getMemberId();
		
		String currentPage = form.getCurrentPage();// 4 String currentPage =
		if (StringUtils.isBlank(currentPage)) {// 4
			currentPage = "1";// 4
		}
		
		
		TradeListQuery listQuery1=new TradeListQuery();
		
		listQuery1.setBuyerId(id);
		
		TradeListQuery listQuery=new TradeListQuery();
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.parseInt(currentPage));
		pageInfo.setPageSize(20);
		listQuery.setId(id);
		listQuery.setQueryBase(pageInfo);
		
		List<TDownloadBHDO> list2=erServiceFacade.selectByDownloadCount(listQuery);
		int i=0;
		if(null!=list2&&list2.size()>0){
			i=list2.size();
		}
		listQuery.setQueryBase(pageInfo);
		List<TDownloadBHDO> list=erServiceFacade.selectTDownloadBHDO(listQuery);
		
		// 分页信息及信息列表
		PageResultList resultList = new PageResultList();
		pageInfo.setTotalItem(i);
		resultList.setPageInfo(pageInfo);// 分页信息
		resultList.setInfos(list);// list
		restP.getData().put("page",resultList);
		restP.setSuccess(true);
		
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/ertrade/ertrade_recode_down", "response", restP);
	}
}
