package com.yongda.site.wallet.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

import com.meidusa.fastjson.JSON;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domainservice.ocx.PasswordOcxService;

/**
 *
 * <p>获取web信息</p>
 * @author qinde
 * @version $Id: WebProcessInfoResolver.java, v 0.1 2013-12-3 上午10:28:55 qinde Exp $
 */
public class WebProcessInfoResolver implements WebArgumentResolver {

	@Resource(name = "passwordOcxService")
	private PasswordOcxService passwordOcxService;
	
    @Override
    public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
        if (methodParameter.getParameterType().equals(TradeEnvironment.class)) {
            return extractPayEnv(webRequest.getNativeRequest(HttpServletRequest.class));
        } else if (methodParameter.getParameterType().equals(OperationEnvironment.class)) {
            return extractEnv(webRequest.getNativeRequest(HttpServletRequest.class));
        }

        return UNRESOLVED;
    }

    private TradeEnvironment extractPayEnv(HttpServletRequest request) {
        TradeEnvironment channelInfo = new TradeEnvironment();
        String agent = request.getHeader("User-Agent");
        channelInfo.setBrowser(agent);
        channelInfo.setBrowserVersion(null);
        String referer = request.getHeader("Referer");
        channelInfo.setReferUrl(referer);
        try {
            URL url = new URL(referer);
            channelInfo.setDomainName(url.getHost());
        } catch (MalformedURLException e) {
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            HashMap cookiesMap = new HashMap(cookies.length);
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName() != null) {
                    cookiesMap.put(cookies[i].getName(), cookies[i].getValue());
                }
            }
            channelInfo.setCookie(JSON.toJSONString(cookiesMap));
        }
        setChannelInfos(channelInfo,request);
        return channelInfo;
    }

    private OperationEnvironment extractEnv(HttpServletRequest request) {
        OperationEnvironment channelInfo = new OperationEnvironment();
        setChannelInfos(channelInfo,request);
        return channelInfo;
    }
    
    
    private void setChannelInfos(OperationEnvironment channelInfo,HttpServletRequest request) {
    	 String machineNetwork = decrpMc(request,request.getParameter("machineNetwork"));
         String ip = request.getHeader("x-forwarded-for");
         if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
             ip = request.getRemoteAddr();
         }
         channelInfo.setClientIp(ip);
         if(!"".equals(machineNetwork)||StringUtils.isNotEmpty(machineNetwork)){
        	String[] arrs = machineNetwork.split(";");
 			StringBuffer ips = new StringBuffer();
 			for(String str:arrs){
 				if(!str.contains("0.0.0.0")){
 					ips.append(str+";");
 				}
 			}
 			machineNetwork = ips.toString().substring(0, ips.toString().length()-1);
 			channelInfo.setClientMac(machineNetwork);
         }else{
        	 channelInfo.setClientMac("x12345678x");
         }
         channelInfo.setClientId(CommonConstant.PERSONAL_APP_ID);
	}
    
    protected String decrpMc(HttpServletRequest request, String mc) {
        HttpSession session = request.getSession();
        if(null!=session&&null!=session.getAttribute("mcrypt_key")){
        	if ((null != mc) && !"".equals(mc)) {
        			String mcrypt_key = (String)session.getAttribute("mcrypt_key");//随机因子
        			return passwordOcxService.decrpPassword(mcrypt_key, mc);//调用解密接口
                }
        }
        return "";
    }
}
