package com.netfinworks.site.web.action.trade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.deposit.api.domain.DepositInfo;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.pfs.service.manager.basis.baseinfo.BankInfoFacade;
import com.netfinworks.pfs.service.manager.basis.domain.BankInfoVO;
import com.netfinworks.pfs.service.manager.common.QueryResult;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;

/**
 * 查询个人钱包用户的充值记录
 *
 * @param model
 * @param request
 * @return
 */
@Controller
public class PrepaidrecordsAction extends BaseAction{
	
    @Resource(name = "defaultDepositInfoService")
    private DefaultDepositInfoService defaultDepositInfoService;
    
    @Resource(name="accountService")
    private AccountService accountService;
    
	@Resource(name="bankInfoFacade")
	private BankInfoFacade bankInfoFacade;
	
    private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    @RequestMapping(value = "/my/all-recharge1.htm")
    public ModelAndView searchAllRecharge(HttpServletRequest request, HttpServletResponse resp,
                                          boolean error, ModelMap model, OperationEnvironment env)
                                                                                                  throws Exception {
        String refresh=request.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(request);
        }
        RestResponse restP = new RestResponse();
        String currentPage = request.getParameter("currentPage");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        PersonMember user =  getUser(request);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        DepositListRequest dRequest = new DepositListRequest();
        dRequest.setMemberId(user.getMemberId());
        dRequest.setAccountNo(user.getDefaultAccountId());
        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrentPage(Integer.valueOf(currentPage));
        dRequest.setPageInfo(pageInfo);
        dRequest.setRequestId(System.currentTimeMillis() + user.getMemberId());
        if(StringUtils.isNotBlank(startDate)) {
            dRequest.setTimeBegin(DateUtils.parseDate(startDate+ ":00"));
        } else {
        	startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
        	dRequest.setTimeBegin(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
        	startDate = DateUtils.getDateString()+" 00:00";
        }
        if(StringUtils.isNotBlank(endDate))  {
            dRequest.setTimeEnd(DateUtils.parseDate(endDate+ ":59"));
        } else {
        	dRequest.setTimeEnd(new Date());
        	endDate = sdf.format(new Date())+" 23:59";
        }
        PageResultList<DepositBasicInfo> page = defaultDepositInfoService.queryList(dRequest, env);
        if(page.getInfos()!=null){
	        for (int i = 0; i < page.getInfos().size(); i++) {
				Map<String, Object> data = JSONObject.parseObject(page.getInfos().get(i).getExtension());//获取待审核交易数据
				QueryResult<BankInfoVO> bankInfoVO=bankInfoFacade.queryByBankCode((String)data.get("BANK_CODE"));
				if(bankInfoVO.isSuccess() &&bankInfoVO.getResults()!=null)
				{
					page.getInfos().get(i).setBank(bankInfoVO.getFirstResult().getShortName());
				}
				else {
					page.getInfos().get(i).setBank((String)data.get("BANK_CODE"));
				}
			}
        }
        map.put("page", page);
        restP.setData(map);
        map.put("mobile", user.getMobileStar());
        map.put("member", user);
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/trade/prepaidrecords", "response",
            restP);

    }
    
    @RequestMapping(value = "/my/all-recharge-download.htm")
    public void download(HttpServletRequest request, HttpServletResponse resp,
                                          boolean error, ModelMap model, OperationEnvironment env)
                                                                                                  throws Exception {
        String refresh=request.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(request);
        }
        RestResponse restP = new RestResponse();
        String currentPage = "1";
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        PersonMember user =  getUser(request);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        DepositListRequest dRequest = new DepositListRequest();
        dRequest.setMemberId(user.getMemberId());
        dRequest.setAccountNo(user.getDefaultAccountId());
        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrentPage(Integer.valueOf(currentPage));
        pageInfo.setPageSize(50000);
        dRequest.setPageInfo(pageInfo);
        dRequest.setRequestId(System.currentTimeMillis() + user.getMemberId());
        if(StringUtils.isNotBlank(startDate)) {
            dRequest.setTimeBegin(DateUtils.parseDate(startDate+ ":00"));
        } else {
        	startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
        	dRequest.setTimeBegin(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
        	startDate = DateUtils.getDateString()+" 00:00";
        }
        if(StringUtils.isNotBlank(endDate))  {
            dRequest.setTimeEnd(DateUtils.parseDate(endDate+ ":59"));
        } else {
        	dRequest.setTimeEnd(new Date());
        	endDate = sdf.format(new Date())+" 23:59";
        }
        PageResultList<DepositBasicInfo> page = defaultDepositInfoService.queryList(dRequest, env);
        if(page.getInfos()!=null){
	        for (int i = 0; i < page.getInfos().size(); i++) {
				Map<String, Object> data = JSONObject.parseObject(page.getInfos().get(i).getExtension());//获取待审核交易数据
				QueryResult<BankInfoVO> bankInfoVO=bankInfoFacade.queryByBankCode((String)data.get("BANK_CODE"));
				if(bankInfoVO.isSuccess() &&bankInfoVO.getResults()!=null)
				{
					page.getInfos().get(i).setBank(bankInfoVO.getFirstResult().getShortName());
				}
				else {
					page.getInfos().get(i).setBank((String)data.get("BANK_CODE"));
				}
			}
        }
        
        map.put("page", page);
        restP.setData(map);
        map.put("mobile", user.getMobileStar());
        map.put("member", user);
        restP.setSuccess(true);
        if(page.getInfos()==null)
		{
			List<DepositBasicInfo> depositInfos=new ArrayList<DepositBasicInfo>();
			page.setInfos(depositInfos);
		}
        ExcelUtil excelUtil = new ExcelUtil();
		excelUtil.toExcel(request, resp,page.getInfos(), 2, startDate, endDate);
    }
}
