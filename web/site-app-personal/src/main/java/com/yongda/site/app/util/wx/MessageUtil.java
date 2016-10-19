package com.yongda.site.app.util.wx;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.thoughtworks.xstream.XStream;
import com.yongda.site.app.form.wx.TextMessage;
/**
 * 消息封装类
 * @author Stephen
 *
 */
public class MessageUtil {	
	
	public static final String MESSAGE_TEXT = "text";
	public static final String MESSAGE_NEWS = "news";
	public static final String MESSAGE_IMAGE = "image";
	public static final String MESSAGE_VOICE = "voice";
	public static final String MESSAGE_MUSIC = "music";
	public static final String MESSAGE_VIDEO = "video";
	public static final String MESSAGE_LINK = "link";
	public static final String MESSAGE_LOCATION = "location";
	public static final String MESSAGE_EVNET = "event";
	public static final String MESSAGE_SUBSCRIBE = "subscribe";
	public static final String MESSAGE_UNSUBSCRIBE = "unsubscribe";
	public static final String MESSAGE_CLICK = "CLICK";
	public static final String MESSAGE_VIEW = "VIEW";
	public static final String MESSAGE_SCANCODE= "scancode_push";
	public static final String WX_LOCATION = "LOCATION";
	//调用客户消息处理
	public static final String CUSTOMER_SERVICE = "transfer_customer_service";
	public static final String SUBSCRIBE_MESSAGE="哈喽，艾瑞芭迪，小雷达在此恭候很久咯~~欢迎来到永达互金服务号！【行·者】为您提供各种行车便捷服务，记得点进去看看哦！";
	public static final String UNSUBSCRIBE_MESSAGE="谢谢您的支持.";
	public static final String OTHER_MESSAGE="别急哦，小雷达马上就来咯~";
	/**
	 * xml转为map集合
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static Map<String, String> xmlToMap(HttpServletRequest request) throws IOException, DocumentException{
		Map<String, String> map = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		
		InputStream ins = request.getInputStream();
		Document doc = reader.read(ins);
		
		Element root = doc.getRootElement();
		
		List<Element> list = root.elements();
		
		for(Element e : list){
			map.put(e.getName(), e.getText());
		}
		ins.close();
		return map;
	}
	
	/**
	 * 将文本消息对象转为xml
	 * @param textMessage
	 * @return
	 */
	public static String textMessageToXml(TextMessage textMessage){
		XStream xstream = new XStream();
		xstream.alias("xml", textMessage.getClass());
		return xstream.toXML(textMessage);
	}
	
	/**
	 * 组装文本消息
	 * @param toUserName
	 * @param fromUserName
	 * @param content
	 * @return
	 */
	public static String initText(String toUserName,String fromUserName,String content){
		TextMessage text = new TextMessage();
		text.setFromUserName(toUserName);
		text.setToUserName(fromUserName);
		text.setMsgType(MessageUtil.MESSAGE_TEXT);
		text.setCreateTime(new Date().getTime());
		text.setContent(content);
		return textMessageToXml(text);
	}
	
	public static String initCustomer(String toUserName,String fromUserName){
		TextMessage text = new TextMessage();
		text.setFromUserName(("<![CDATA["+toUserName+"]]>").replace("&lt;",'<'+""));
		text.setToUserName(("<![CDATA["+fromUserName+"]]>").replace("&lt;",'<'+""));
		text.setMsgType(("<![CDATA["+MessageUtil.CUSTOMER_SERVICE+"]]>").replace("&lt;",'<'+""));
		text.setCreateTime(new Date().getTime());
		//text.setContent(content);
		return textMessageToXml(text);
	}
	
	/**
	 * 关注
	 * @return
	 */
	public static String subscribeText(){
		StringBuffer sb = new StringBuffer();
		sb.append(SUBSCRIBE_MESSAGE);
		return sb.toString();
	}
	/**
	 * 取消关注
	 */
	public static String unsubscribeText(){
		StringBuffer sb = new StringBuffer();
		sb.append(UNSUBSCRIBE_MESSAGE);
		return sb.toString();
	}
	
	public static String otherReply(){
		StringBuffer sb = new StringBuffer();
		sb.append(OTHER_MESSAGE);
		return sb.toString();
	}
	/*public static String firstMenu(){
		StringBuffer sb = new StringBuffer();
		sb.append("永达微信公众号开发，主要涉及公众号消息、定位,授权");
		return sb.toString();
	}
	
	public static String secondMenu(){
		StringBuffer sb = new StringBuffer();
		sb.append("永达互联网金融");
		return sb.toString();
	}
	
	public static String threeMenu(){
		StringBuffer sb = new StringBuffer();
		sb.append("词组翻译使用指南\n\n");
		return sb.toString();
	}*/
	
	
	
}
	
	
