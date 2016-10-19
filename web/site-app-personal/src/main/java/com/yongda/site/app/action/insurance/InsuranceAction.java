package com.yongda.site.app.action.insurance;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.PageQuery;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.aop.advice.FormValid;
import com.yongda.site.app.form.InsuranceForm;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.domain.domain.insurance.InsuranceAuthResult;
import com.yongda.site.domain.domain.insurance.InsuranceOrder;
import com.yongda.site.domain.domain.insurance.InsuranceQuery;
import com.yongda.site.domain.domain.insurance.InsuranceQueryResult;
import com.yongda.site.domain.domain.insurance.InsuranceRemoteQuery;
import com.yongda.site.domain.domain.insurance.InsuranceRemoteQueryResult;
import com.yongda.site.domainservice.insurance.InsuranceService;
import com.yongda.site.ext.integration.insurance.InsuranceFacade;

/** 
* @ClassName: InsuranceAction 
* @Description: 保单操作ACTION 
* @author slong
* @date 2016年5月20日 下午2:34:16 
*  
*/
@Controller
@RequestMapping("/insurance")
public class InsuranceAction extends BaseAction {
	
	@Autowired
	private InsuranceService insuranceService;
	
	@Resource
	private InsuranceFacade insuranceFacade;
	
	private String remoteAddUrl = "http://tp.baobeikeji.cn/car/index";
	private String remoteManageUrl = "http://tp.baobeikeji.cn/car/manage";

	@RequestMapping(value = "my", method ={RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public RestResponse my(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env,
			@RequestParam(defaultValue="10") Integer pageSize,
			@RequestParam(defaultValue="1")Integer currentPage) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		InsuranceQuery query = new InsuranceQuery();
		query.setQueryBase(new QueryBase());
		query.getQueryBase().setCurrentPage(currentPage);
		query.getQueryBase().setPageSize(pageSize);
		query.setMemberId(user.getMemberId());
		InsuranceQueryResult result = insuranceService.queryByPage(query);
		PageQuery pageInfo = new PageQuery();
		pageInfo.copyPropertiesFrom(result.getQueryBase());
		restP.getData().put("pageData",result.getOrders());
		restP.getData().put("pageInfo", pageInfo);
		return restP;
	}
	
	@RequestMapping(value = "add", method ={RequestMethod.POST})
	@ResponseBody
	public RestResponse add(HttpServletRequest request, HttpServletResponse resp,@FormValid InsuranceForm form,OperationEnvironment env) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try {
			if(!insuranceService.existInsuranceOrder(user.getMemberId(), form.getPolicyNo(), form.getCompany())){
				InsuranceRemoteQuery remoteQuery = new InsuranceRemoteQuery();
				BeanUtils.copyProperties(form, remoteQuery);
				InsuranceRemoteQueryResult response = insuranceFacade.queryInsurance(remoteQuery);
				if(response.isSuccess() && StringUtils.equalsIgnoreCase(response.getReturnCode(), "success")){
					InsuranceOrder newOrder = response.getOrders().get(0);
					newOrder.setMemberId(user.getMemberId());
					insuranceService.insert(newOrder);
					restP.getData().put("order", newOrder);
				}else{
					restP = ResponseUtil.buildExpResponse(response.getReturnCode(),response.getReturnMessage());
				}
			}
		} catch (Exception e) {
			logger.error("添加保单信息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	@RequestMapping(value = "detail/{id}", method ={RequestMethod.GET})
	@ResponseBody
	public RestResponse detail(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathParam(value="id") Long id) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try {
			InsuranceOrder order = insuranceService.queryDetail(id,user.getMemberId());
			restP.getData().put("order", order);
		} catch (Exception e) {
			logger.error("查询保单信息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	@RequestMapping(value = "delete/{id}", method ={RequestMethod.GET,RequestMethod.POST,RequestMethod.DELETE})
	@ResponseBody
	public RestResponse delete(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathParam(value="id") Long id) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try {
			insuranceService.deleteByPrimaryKey(id,user.getMemberId());
		} catch (Exception e) {
			logger.error("删除保单信息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	@RequestMapping(value = "remote/token", method ={RequestMethod.GET})
	@ResponseBody
	public RestResponse authToken(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try {
			InsuranceAuthResult remotResp = insuranceFacade.queryAuthToken(user.getMemberId());
			if(remotResp.isSuccess() && StringUtils.equalsIgnoreCase(remotResp.getReturnCode(), "success")){
				restP.getData().put("token", remotResp.getToken());
			}
		} catch (Exception e) {
			logger.error("删除保单信息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	@RequestMapping(value = "remote/add", method ={RequestMethod.GET})
	public void remoteAdd(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env) throws Exception {
		PersonMember user = getUser(request);
		InsuranceAuthResult remotResp = insuranceFacade.queryAuthToken(user.getMemberId());
		resp.sendRedirect(remoteAddUrl+"?t="+remotResp.getToken());
	}
	
	@RequestMapping(value = "remote/manage", method ={RequestMethod.GET})
	public void remoteManage(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env) throws Exception {
		PersonMember user = getUser(request);
		InsuranceAuthResult remotResp = insuranceFacade.queryAuthToken(user.getMemberId());
		resp.sendRedirect(remoteManageUrl+"?t="+remotResp.getToken());
	}
}
