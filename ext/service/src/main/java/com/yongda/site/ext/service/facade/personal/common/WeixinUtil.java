package com.yongda.site.ext.service.facade.personal.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.service.facade.converter.AccessToken;
import com.netfinworks.site.service.facade.model.WxNotifyData.TemplateDataAttr;
import com.yongda.site.service.personal.facade.request.TemplateData;
import com.yongda.site.service.personal.facade.request.WxMessageRequest;
import com.yongda.site.service.personal.facade.request.WxTemplate;

/**
 * 微信工具类
 * @author Stephen
 *
 */
public class WeixinUtil {
	//公众号模板消息所属行业编号
	private static final String ENCODING = "UTF-8";
	//获得token
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	//send 模板消息
	private static final String SEND_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";	
	
	private static final String WX_TEMPLATE_URL ="http://weixin.qq.com/download";
	
	/**
	 * get请求 
	 * @param url  直接用String代替URI来访问
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static JSONObject doGetStr(String url,String encoding) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		JSONObject jsonObject = null;
		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			if(entity != null){
				String result = EntityUtils.toString(entity,encoding);
				jsonObject = JSONObject.fromObject(result);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * POST2请求
	 * @param url
	 * @param outStr
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static JSONObject doPostStr(String url,String outStr){
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(url);
		JSONObject jsonObject = null;
		httpost.setEntity(new StringEntity(outStr,"UTF-8"));
		HttpResponse response;
		String result;
		try {
			response = client.execute(httpost);
			result = EntityUtils.toString(response.getEntity(),"UTF-8");
			jsonObject = JSONObject.fromObject(result);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * 获取accessToken
	 * @return  appid  应用Id
	 *      appsecret  应用秘钥
	 * @throws ParseException
	 * @throws IOException
	 */
	public static AccessToken getAccessToken(String appid,String appsecret) throws ParseException, IOException{
		AccessToken token = new AccessToken();
		String url = ACCESS_TOKEN_URL.replace("APPID", appid).replace("APPSECRET", appsecret);
		JSONObject jsonObject = doGetStr(url,ENCODING);
		if(jsonObject!=null){
			token.setToken(jsonObject.getString("access_token"));
			token.setExpiresIn(jsonObject.getInt("expires_in"));
		}
		return token;
	}
	
	
	
	
	
	
	/**
	 * 模板消息接口 发送模板
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws BizException 
	 */
	public static JSONObject sendTemplateData(WxMessageRequest wxm,String token) throws  BizException{
		String url = SEND_TEMPLATE_URL.replace("ACCESS_TOKEN", token);
		WxTemplate temp = new WxTemplate();
		Map<String,TemplateData> notifyDatas = new HashMap<String,TemplateData>();
		if(wxm.getTemplateData() != null){
			temp.setUrl(wxm.getTemplateData().getUrl());
			temp.setTemplate_id(wxm.getTemplateId());
			temp.setTouser(wxm.getOpenid());
			if(wxm.getTemplateData().getData() != null){
				Set<Entry<String, TemplateDataAttr>> entrys = wxm.getTemplateData().getData().entrySet();
				for(Entry<String, TemplateDataAttr> entry:entrys){
					TemplateData data = new TemplateData();
					if(StringUtils.isNotEmpty(entry.getValue().getDataColor())){
						data.setColor(entry.getValue().getDataColor());
					}else{
						data.setColor("#000000");
					}
					data.setValue(entry.getValue().getDataValue());
					notifyDatas.put(entry.getKey(), data);
				}
			}
		}
        temp.setData(notifyDatas);
        String jsonString = JSONObject.fromObject(temp).toString();
		JSONObject objectSend = doPostStr(url,jsonString);
		if("0".equals(objectSend.getString("errcode"))){
			return objectSend;
		}else{
			throw new BizException(ErrorCode.SYSTEM_ERROR,objectSend.getString("errmsg"));
		}
	}
	
}
