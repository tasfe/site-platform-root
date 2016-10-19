package com.yongda.site.app.action;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.meidusa.fastjson.JSON;
import com.netfinworks.basis.service.domain.ActivityVo;
import com.netfinworks.basis.service.domain.RowVo;
import com.netfinworks.basis.service.response.Response;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultSystemMessageService;
import com.yongda.site.app.WebDynamicResource;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.validator.CommonValidator;

/**
 * 微信消息
 * @author admin
 *
 */
@Controller
public class SendWxTemplateMessageAction extends BaseAction{
	private Logger logger = LoggerFactory.getLogger(SendWxTemplateMessageAction.class);
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name = "defaultSystemMessageService")
	protected DefaultSystemMessageService defaultSystemMessageService;
	
	private static final String PARAM_STRING = "weixin";
    
	@Resource(name="webResource")
	private WebDynamicResource webResource;
	
    /**
     * 获取消息列表
     * @param request
     * @param response
     * @param env
     * @return
     * @throws IOException 
     */
    @RequestMapping(value = "/getSystemMessageLimit",  method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public RestResponse getSystemMessageLimit(HttpServletRequest request,HttpServletResponse response,
                               OperationEnvironment env) throws IOException {
    	RestResponse restresponse = new RestResponse();
    	try{
    		response.setCharacterEncoding("utf8");
    		response.setHeader("content-type", "application/json;charset=UTF-8");
    		String currentPageStr = request.getParameter("currentPage");
    		String pageSizeStr = request.getParameter("pageSize");
    		if(!cheackParams(currentPageStr,pageSizeStr)){
    			restresponse.setMessage("缺少必要的查询参数");
    			restresponse.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
    			restresponse.setSuccess(false);
				logger.error("缺少必要的查询参数！");
				response.getWriter().write(JSON.toJSONString(restresponse));
				return null;
    		}
    		int currentPage = Integer.valueOf(currentPageStr);//当前页
    		int pageSize = Integer.valueOf(pageSizeStr);//每页数据量
    		restresponse = defaultSystemMessageService.getSystemMsgLimit(PARAM_STRING, currentPage, pageSize);
    		
    		restresponse.setSuccess(true);
    	}catch(Exception e){
    		restresponse.setSuccess(false);
    		restresponse.setMessage(e.getMessage());
    	}
		return restresponse;
    }
    
    /***
     * 获取消息详情
     * @param request
     * @param response
     * @param env
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getSystemMessageDetails", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse getSystemMessageDetails(HttpServletRequest request,HttpServletResponse response,
                               OperationEnvironment env) throws IOException {
    	RestResponse restresponse = new RestResponse();
    	try{
    		response.setCharacterEncoding("utf8");
    		response.setHeader("content-type", "application/json;charset=UTF-8");
    		String messageId = request.getParameter("messageId");
    		if(StringUtils.isBlank(messageId)){
    			restresponse.setMessage("缺少必要的查询参数");
    			restresponse.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
    			restresponse.setSuccess(false);
				logger.error("缺少必要的查询参数！");
				response.getWriter().write(JSON.toJSONString(restresponse));
				return null;
    		}
    		restresponse = defaultSystemMessageService.getSystemMessage(messageId);
    		restresponse.setSuccess(true);
    	}catch(Exception e){
    		restresponse.setSuccess(false);
    		restresponse.setMessage(e.getMessage());
    	}
		return restresponse;
    }
    
    /**
     * 获取活动列表
     * @param currentPageStr
     * @param pageSizeStr
     * @return
     */
    
    @RequestMapping(value = "/getActivityLimit", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse getActivityLimit(HttpServletRequest request,HttpServletResponse response,
                               OperationEnvironment env) throws IOException {
    	RestResponse restresponse = new RestResponse();
    	logger.info("-------getActivityLimit-------");
    	try{
    		response.setCharacterEncoding("utf8");
    		response.setHeader("content-type", "application/json;charset=UTF-8");
    		String currentPageStr = request.getParameter("currentPage");
    		String pageSizeStr = request.getParameter("pageSize");
    		if(!cheackParams(currentPageStr,pageSizeStr)){
    			restresponse.setMessage("缺少必要的查询参数");
    			restresponse.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
    			restresponse.setSuccess(false);
				logger.error("缺少必要的查询参数！");
				response.getWriter().write(JSON.toJSONString(restresponse));
				return null;
    		}
    		int currentPage = Integer.valueOf(currentPageStr);//当前页
    		int pageSize = Integer.valueOf(pageSizeStr);//每页数据量
    		Response<RowVo<ActivityVo>>  activityList= defaultSystemMessageService.getActivityLimit(PARAM_STRING, currentPage, pageSize);
    		try{
    			logger.info("活动列表展示",activityList.getBean().getRows());
    		}catch(Exception e){
    			logger.info("--------------");
    		}
    		
    		if(CollectionUtils.isEmpty(activityList.getBean().getRows())){
        		restresponse.setSuccess(false);
        		restresponse.setMessage("查无数据");
        		logger.info("查询无数剧");
        		return restresponse;
    		}
    		Map<String,Object> map = new HashMap<String,Object>();
			//遍历url
			if(CollectionUtils.isNotEmpty(activityList.getBean().getRows()))
				for(ActivityVo activity:activityList.getBean().getRows()){
					activity.setActivityContent(StringEscapeUtils.unescapeHtml(activity.getActivityContent()));
					activity.setImage(MessageFormat.format("{0}/image?fileName={1}",webResource.getWalletH5Address() ,activity.getImage()));
				}
			map.put("msgList",activityList.getBean().getRows());
			restresponse.setData(map);
    		restresponse.setSuccess(true);
    	}catch(Exception e){
    		restresponse.setSuccess(false);
    		restresponse.setMessage(e.getMessage());
    	}
		return restresponse;
    }
    
    
    /***
     * 获取活动详情
     * @param request
     * @param response
     * @param env
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getActivityDetails", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse getActivityById(HttpServletRequest request,HttpServletResponse response,
                               OperationEnvironment env) throws IOException {
    	RestResponse restresponse = new RestResponse();
    	try{
    		response.setCharacterEncoding("utf8");
    		response.setHeader("content-type", "application/json;charset=UTF-8");
    		String messageId = request.getParameter("messageId");
    		if(StringUtils.isBlank(messageId)){
    			restresponse.setMessage("缺少必要的查询参数");
    			restresponse.setCode(CommonDefinedException.ILLEGAL_ARGUMENT
						.getErrorCode());
    			restresponse.setSuccess(false);
				logger.error("缺少必要的查询参数！");
				response.getWriter().write(JSON.toJSONString(restresponse));
				return null;
    		}
    		restresponse = defaultSystemMessageService.getActivityById(messageId);
    		restresponse.setSuccess(true);
    	}catch(Exception e){
    		restresponse.setSuccess(false);
    		restresponse.setMessage(e.getMessage());
    	}
		return restresponse;
    }
    
    public Boolean cheackParams(String currentPageStr,String pageSizeStr){
    	if(StringUtils.isBlank(currentPageStr) || StringUtils.isBlank(pageSizeStr)){
    		return false;
    	}
		return true;
    }
}
