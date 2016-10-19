package com.yongda.site.app.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.wx.CheckUtil;
import com.yongda.site.app.util.wx.MessageUtil;
import com.yongda.site.app.util.wx.WeixinUtil;

/**
 * 微信公众号接入
 * @author zhangweijie
 *
 */
@Controller
public class WeixinAction extends BaseAction{
	private Logger logger = LoggerFactory.getLogger(WeixinAction.class);
	
	@Resource(name = "xuCache")
	private XUCache<String> loginCache;
	
	/**
	 * 接入验证
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 接入验证
	 */
	@RequestMapping(value = "/wechat",method=RequestMethod.GET)
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String signature = req.getParameter("signature");
		String timestamp = req.getParameter("timestamp");
		String nonce = req.getParameter("nonce");
		String echostr = req.getParameter("echostr");
		
		PrintWriter out = resp.getWriter();
		if(CheckUtil.checkSignature(signature, timestamp, nonce)){
			out.print(echostr);
		}
	}
	
	
	/**
	 * 消息的接收与响应
	 */
	@RequestMapping(value = "/wechat",method=RequestMethod.POST)
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		try {
			Map<String, String> map = MessageUtil.xmlToMap(req);
			String fromUserName = map.get("FromUserName");
			String toUserName = map.get("ToUserName");
			String msgType = map.get("MsgType");
			String content = map.get("Content");
			
			String message = "";
			if(MessageUtil.MESSAGE_EVNET.equals(msgType)){
				String eventType = map.get("Event");
				if(MessageUtil.WX_LOCATION.equals(eventType))
				{
					String latitude = map.get("Latitude");//纬度
					String Longitude = map.get("Longitude");//经度
					JSONObject jsonobject = WeixinUtil.getAddress(latitude,Longitude);//獲取地理位置
					logger.info("用户的地理位置信息：{}"+jsonobject.toString());
					JSONObject result = (JSONObject) jsonobject.get("result");
					JSONObject addressComponent = (JSONObject) result.get("addressComponent");
					String city = addressComponent.getString("city");//用户所在城市名称
					String Data = WeixinUtil.getCityWeather(city);//查询城市天气
					loginCache.set(fromUserName+CommonConstant.CITY_WEATHER, Data,3600);
					logger.info("用户的所在城市city：{},城市天气{}"+city+","+loginCache.get(fromUserName+CommonConstant.CITY_WEATHER));
				}else if(MessageUtil.MESSAGE_SUBSCRIBE.equals(eventType))
				{	
					logger.info("user..subscribe");
					message = MessageUtil.initText(toUserName, fromUserName, MessageUtil.subscribeText());
				}/*else if(MessageUtil.MESSAGE_UNSUBSCRIBE.equals(eventType))
				{
					message = MessageUtil.initText(toUserName, fromUserName, MessageUtil.menuText());
				}*/
			}else{
				message = MessageUtil.initText(toUserName, fromUserName, MessageUtil.otherReply());
			}
			logger.info("永达服务短信响应微信消息："+message);
			out.print(message);
		} catch (DocumentException e) {
			e.printStackTrace();
		}finally{
			out.close();
		}
	}

}
