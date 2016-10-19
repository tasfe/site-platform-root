package com.yongda.site.app.action.trade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.PageQuery;
import com.netfinworks.site.domain.domain.info.WalletCheckInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.response.WalletCheckResponse;
import com.netfinworks.site.domain.domain.trade.WalletCheckRequest;
import com.netfinworks.site.domain.enums.DealType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.ext.integration.trade.CoAccountService;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.ResponseUtil;
/**
 * 消息
 * @author yp
 *
 */
@Controller
@RequestMapping("/trade/query")
public class TradeQueryAction extends BaseAction{
	
	@Resource(name = "coAccountService")
	private CoAccountService coAccountService;
	
	/**
	 * 收支明细  需要memberId、当前页currentPage、AccountId
	 * 交易类型txnType：1（全部），2（收入），3（支出）
	 * 最多加载当前时间前90天内的数据
	 * 可以根据订单号查询
	 * @param req
	 * @param resp   
	 * @param model
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "tradeflow", method = {RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public RestResponse tradeflow(HttpServletRequest req, HttpServletResponse resp,OperationEnvironment env,
			String paySeq,
			@RequestParam(defaultValue="1") Integer txnType,
			@RequestParam(defaultValue="10") Integer pageSize,
			@RequestParam(defaultValue="1")Integer currentPage) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		try{
			PersonMember user =  getUser(req);
			// 分页
			QueryBase queryBase = new QueryBase();
			queryBase.setNeedQueryAll(true);
			queryBase.setPageSize(pageSize);
			queryBase.setCurrentPage(currentPage);
			// 查询会员所以需要的信息
			WalletCheckRequest wcRequest = new WalletCheckRequest();
			// 设置分页信息
			wcRequest.setQueryBase(queryBase);
			wcRequest.setMemberId(user.getMemberId());
			wcRequest.setAccountNo(user.getDefaultAccountId());
			wcRequest.setClientIp(req.getRemoteAddr());
			// 账户类型
			wcRequest.setAccountType(Integer.parseInt(user.getMemberType().getBaseAccount().toString()));
			// 交易类型(枚举)
			DealType dealType = DealType.getByCode(txnType);
			if(dealType == null)
				return ResponseUtil.buildExpResponse(CommonDefinedException.FIELD_TYPE_ERROR,"查询交易类型错误");
			wcRequest.setTxnType(dealType);
	        wcRequest.setBeginTime(DateUtil.getDayBegin(DateUtil.getDateNearCurrent(-90)));
	        wcRequest.setEndTime(new Date());
			// 是否需要汇总
			wcRequest.setNeedSummary(true);
			//订单号
			wcRequest.setSysTraceNo(paySeq);
			WalletCheckResponse rep = coAccountService.queryWalletCheckList(wcRequest);
			List<WalletCheckInfo> list = new ArrayList<WalletCheckInfo>();
			if(rep.getList()!=null){
				list=rep.getList();
			}
			PageQuery pageInfo = new PageQuery();
			pageInfo.copyPropertiesFrom(rep.getQueryBase());
			restP.getData().put("pageData",list);
			restP.getData().put("pageInfo", pageInfo);
		}catch(Exception e){
			logger.error("查询支付流水异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
}
