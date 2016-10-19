package com.yongda.site.web.action.yunplus;

import java.io.IOException;
import java.net.URLEncoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.site.core.common.util.HttpClientUtil;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.web.action.common.BaseAction;

@Controller
public class YunPlusAgentAction extends BaseAction {

	@Value("${yunPosUrl}") 
    private String yunPosUrl;
	
	@Resource
    private OperatorService operatorService;

    @RequestMapping("/agent.htm")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
    	String jsonReq = getRequestJsonString(request);
    	PostMethod method = HttpClientUtil.buildPostJsonMethod(yunPosUrl,jsonReq);
    	String resultJson = HttpClientUtil.doPostAsString(method, "UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(resultJson);
    }
    
    @RequestMapping("/orderAgent.htm")
    public void handleOrderRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
    	String jsonReq = getRequestJsonString(request);
    	PostMethod method = HttpClientUtil.buildPostJsonMethod(yunPosUrl,jsonReq);
    	String resultJson = HttpClientUtil.doPostAsString(method, "UTF-8");
    	JSONObject rs = JSON.parseObject(resultJson);
    	JSONObject errObj = rs.getJSONObject("err");
    	if(errObj.getIntValue("err_code") == 1){
    		//成功的情况下获取操作员账号
    		JSONObject orderObj = rs.getJSONObject("record");
    		OperatorVO operator = operatorService.getOperatorById(orderObj.getString("user_id"), env);
    		orderObj.put("operator_account", operator.getLoginName());
    		rs.put("record", orderObj);
    	}
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(rs.toJSONString());
    }
    
    
    /***
     * 获取 request 中 json 字符串的内容
     * 
     * @param request
     * @return : <code>byte[]</code>
     * @throws IOException
     */
    public static String getRequestJsonString(HttpServletRequest request)
            throws IOException {
        String submitMehtod = request.getMethod();
        // GET
        if (submitMehtod.equals("GET")) {
            return new String(request.getQueryString().getBytes("iso-8859-1"),"utf-8").replaceAll("%22", "\"");
        // POST
        } else {
            return getRequestPostStr(request);
        }
    }

    /**      
     * 描述:获取 post 请求的 byte[] 数组
     * <pre>
     * 举例：
     * </pre>
     * @param request
     * @return
     * @throws IOException      
     */
    public static byte[] getRequestPostBytes(HttpServletRequest request)
            throws IOException {
        int contentLength = request.getContentLength();
        if(contentLength<0){
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength;) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }

    /**      
     * 描述:获取 post 请求内容
     * <pre>
     * 举例：
     * </pre>
     * @param request
     * @return
     * @throws IOException      
     */
    public static String getRequestPostStr(HttpServletRequest request)
            throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        return new String(buffer, charEncoding);
    }
}
