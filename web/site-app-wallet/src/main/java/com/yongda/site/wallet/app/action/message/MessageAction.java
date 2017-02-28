package com.yongda.site.wallet.app.action.message;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yongda.site.wallet.app.action.common.BaseAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.basis.service.api.SystemMsgFacade;
import com.netfinworks.basis.service.domain.RowVo;
import com.netfinworks.basis.service.domain.SystemMsgVo;
import com.netfinworks.basis.service.response.Response;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.PageQuery;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.yongda.site.wallet.app.util.ResponseUtil;
import com.yongda.site.domain.domain.message.PersonalMsgQuery;
import com.yongda.site.domain.domain.message.PersonalMsgQueryResult;
import com.yongda.site.ext.integration.message.MessageService;

import java.util.Date;

/**
 * 消息
 * @author yp
 *
 */
@Controller
@RequestMapping("/message")
public class MessageAction extends BaseAction {
	private Logger logger = LoggerFactory.getLogger(MessageAction.class);

	@Resource(name="systemMsgFacadeService")
	private SystemMsgFacade systemMsgFacade;
	
	@Resource(name = "messageService")
    private MessageService messageService;
	
	@RequestMapping(value = "my/{platform}", method =RequestMethod.GET)
	@ResponseBody
	public RestResponse my(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathVariable(value="platform") String plateformType
			,@RequestParam(defaultValue="1") Integer currentPage
			,@RequestParam(defaultValue="10") Integer pageSize
			,String isRead) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		try {
			logger.info("app端查询我的消息request参数,plateformType-{},currentPage-{},pageSize-{}"
					,plateformType,currentPage,pageSize);
			PersonMember user = getUser(request);
			PageQuery queryBase = new PageQuery();
			queryBase.setNeedQueryAll(true);
			queryBase.setPageSize(pageSize);
			queryBase.setCurrentPage(currentPage);
			PersonalMsgQuery query = new PersonalMsgQuery();
			if(StringUtils.equalsIgnoreCase(isRead, "yes")){
				query.setIsRead(true);
			}else if(StringUtils.equalsIgnoreCase(isRead, "no")){
				query.setIsRead(false);
			}
			query.setMemberId(user.getMemberId());
			query.setQueryBase(queryBase);
			if("weixin".equalsIgnoreCase(plateformType)){
				query.setMsgType("W");
				PersonalMsgQueryResult result = messageService.queryPersonalMsg(query);
				if (result!=null && result.getQueryBase()!=null){
					int totalPage = result.getQueryBase().getTotalPage();
					if(currentPage>totalPage){
						restP.getData().put("pageData", null);
					}else
						restP.getData().put("pageData", result.getMessages());
				}
				restP.getData().put("pageInfo", result.getQueryBase());
			}
			restP.setMessage("查询完成");
		}catch (Exception e) {
			logger.error("查询我的消息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	
	@RequestMapping(value = "my/{platform}/read", method ={RequestMethod.POST,RequestMethod.PUT})
	@ResponseBody
	public RestResponse read(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathVariable(value="platform") String plateformType) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		try {
			String msgNo = request.getParameter("msgOrderNo");
			logger.info("app端更新我的消息request参数,msgOrderNo-{}",msgNo);
			if (StringUtils.isBlank(msgNo)){
				restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"消息单号不能为空");
				return restP;
			}

			PersonMember user = getUser(request);
			if("weixin".equalsIgnoreCase(plateformType)){
				if(!"all".equalsIgnoreCase(msgNo))
					messageService.readPersonalMsg(user.getMemberId(), msgNo, "W");
				else
					messageService.readPersonalMsg(user.getMemberId(), null, "W");
			}
		}catch (Exception e) {
			logger.error("更新我的消息已读异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	
	@RequestMapping(value = "system/{platform}", method = {RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public RestResponse sysmsg(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathVariable(value="platform") String plateformType
			,@RequestParam(defaultValue="1") Integer currentPage
			,@RequestParam(defaultValue="10") Integer pageSize) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		try {
			logger.info("app端查询系统消息request参数,plateformType-{},currentPage-{},pageSize-{}"
					,plateformType,currentPage,pageSize);
			Response<RowVo<SystemMsgVo>> response = systemMsgFacade.getSystemMsgLimit(plateformType, currentPage, pageSize);
			if(CollectionUtils.isEmpty(response.getBean().getRows())){
				restP.setMessage("查无数据");
        		return restP;
    		}
			PageQuery queryBase = new PageQuery();
			queryBase.setNeedQueryAll(true);
			queryBase.setTotalItem(response.getBean().getTotal());
			queryBase.setPageSize(response.getBean().getPageSize());
			queryBase.setCurrentPage(response.getBean().getCurrentPage());
			if(CollectionUtils.isNotEmpty(response.getBean().getRows())){
				for(SystemMsgVo msg:response.getBean().getRows()){
					msg.setNewsContent(StringEscapeUtils.unescapeHtml(msg.getNewsContent()));
				}
			}
			restP.getData().put("pageData",response.getBean().getRows());
			restP.getData().put("pageInfo",queryBase);
			restP.setMessage("查询完成");
		} catch (Exception e) {
			logger.error("查询系统消息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}

}
