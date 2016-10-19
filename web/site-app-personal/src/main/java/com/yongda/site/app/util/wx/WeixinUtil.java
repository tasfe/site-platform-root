package com.yongda.site.app.util.wx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nutz.http.Http;
import org.nutz.http.Response;

import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.yongda.site.app.form.wx.AccessToken;

/**
 * 微信工具类
 * @author Stephen
 *
 */
public class WeixinUtil {
	//获得token
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	//強授权
	private static final String WEB_PAGE_ACCREDIT_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
	//
	private static final String SILENT_ACCREDIT_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
	
	private static final String ACCREDIT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	//获取用户信息  ACCESS_TOKEN  不是基础支持的token而是  网页授权接口调用凭证,
	private static final String GET_USER_INFO_URL = " https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
	//百度秘钥
	private static final String BAIDU_API_AK = "lDkGl5U3toyTZ7QouwNxF3H06c2YjskE";
	//百度地图url
	private static final String BAIDU_API_URL = "http://api.map.baidu.com/geocoder?location=LOCATION&output=json&key=AK";
	//中国天气网api
	private static final String WEATHER_API_URL = "http://wthrcdn.etouch.cn/weather_mini?citykey=cityCode";
	//编码
	private static final String ENCODING = "UTF-8";
	
	private static final String WX_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	
	private static final String flag = "今日不限行";
	
	private static final String flagNO = "今日限行";
	
	private static final String specialflagNO = "请注意限行尾号";
	
	private static final String singularControlCount = "尾号单数";
	
	private static final String evenlarControlCount = "尾号双数";
	
	private static final String[] hzArray = {"1,9","2,8","3,7","4,6","5,0"};
	
	private static final String[] gyArray = {"1,6","2,7","3,8","4,9","5,0"};
	
	private static final String NOT_CAR_WASH = "两天内可能有雨,不适宜洗车";
	
	private static final String YES_CAR_WASH = "适宜洗车";
	
	private static  List<Object> list_row = new ArrayList<Object>();  
	
	static {
		InputStream in = WeixinUtil.class.getResourceAsStream("/cityCode.xml");
		SAXReader sr = new SAXReader();//获取读取方式
		Document doc;
		try {
			doc = sr.read(in);
			//读取xml文件，并且将数据全部存放到Document中
			Element root = doc.getRootElement();//获取根节点
			List xmlList = root.elements("province");//根据根节点，将根节点下 row中的所有数据放到list容器中。
			for(Object obj : xmlList ){
				   Element row = (Element)obj;
				   list_row = row.elements("county");//获取county节点下所有的内容，存入list_row容器中
			}
			in.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * get请求 1
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
	 * get请求 2
	 * 如果地址中涉及了特殊字符，如‘｜’‘&’等。所以不能直接用String代替URI来访问。必须采用%0xXX方式来替代特殊字符。但这种办法不直观。所以只能先把String转成URL，再能过URL生成URI的方法来解决问题
	 * @param url
	 * @return
	 */
	public static JSONObject doGetStrURL(URI url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		JSONObject jsonObject = null;
		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			if(entity != null){
				String result = EntityUtils.toString(entity,"UTF-8");
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
	 * POST请求
	 * @param url
	 * @param outStr
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static JSONObject doPostStr(String url,JSONObject outStr){
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(url);
		JSONObject jsonObject = null;
		httpost.setEntity(new StringEntity(outStr.toString(),"UTF-8"));
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
	 * POST2请求
	 * @param url
	 * @param outStr
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static JSONObject doPostStr2(String url,String outStr){
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
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static AccessToken getAccessToken(String wxappid,String wxappsecret) throws ParseException, IOException{
		AccessToken token = new AccessToken();
		String url = ACCESS_TOKEN_URL.replace("APPID", wxappid).replace("APPSECRET", wxappsecret);
		JSONObject jsonObject = doGetStr(url,ENCODING);
		if(jsonObject!=null){
			token.setToken(jsonObject.getString("access_token"));
			token.setExpiresIn(jsonObject.getInt("expires_in"));
		}
		return token;
	}
	/**
	 * 用户同意授权，获取code
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public  static String getAccreditCode(String wxappid,String domainUrl) throws ParseException, IOException{
		//String url = domainUrl+"/site-app-personal/wxredirect";
		String url = domainUrl+"/wxredirect"; 
		String strURL = java.net.URLEncoder.encode(url, "utf-8");
		return WEB_PAGE_ACCREDIT_URL.replace("APPID", wxappid).replace("REDIRECT_URI", strURL);
	}
	
	public  static String getSilentAccreditCode(String wxappid,String domainUrl) throws ParseException, IOException{
		//String url = domainUrl+"/site-app-personal/transfer";
		String url = domainUrl+"/transfer"; 
		String strURL = java.net.URLEncoder.encode(url, "utf-8");
		return SILENT_ACCREDIT_URL.replace("APPID", wxappid).replace("REDIRECT_URI", strURL);
	}
	
	/**
	 * 获取Access_token  通过code
	 * @param map
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws BizException 
	 */
	public  static JSONObject getAccreditAccesstoken(String code,String wxappid,String wxappsecret) throws ParseException, IOException, BizException{
		String url = ACCREDIT_ACCESS_TOKEN_URL.replace("APPID", wxappid).replace("SECRET", wxappsecret).replace("CODE", code);
		JSONObject jsonObject = null;
		try{
			jsonObject = doGetStr(url,ENCODING);
			if(!StringUtils.isBlank(jsonObject.getString("access_token"))){
				return jsonObject;
			}else{
				throw new BizException(ErrorCode.SYSTEM_ERROR,jsonObject.getString("errmsg"));
			}
		}catch(Exception e){
			throw new BizException(ErrorCode.SYSTEM_ERROR,jsonObject.getString("errmsg"));
		}
	}
	
	/**
	 * 获取用户信息
	 */
	public static JSONObject getUserInfo(String token,String openid){
		String url = GET_USER_INFO_URL.replace("ACCESS_TOKEN", token).replace("OPENID", openid);
		try{
			URL url1 = new URL(url);   
			URI uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), null); 
			JSONObject jsonObject = doGetStrURL(uri);
			if(!StringUtils.isBlank(jsonObject.getString("openid"))){
				return jsonObject;
			}else{
				throw new BizException(ErrorCode.SYSTEM_ERROR,jsonObject.getString("errmsg"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取用户的定位信息
	 * @return
	 */
	public static JSONObject getAddress(String Latitude,String Longitude){
		String url = BAIDU_API_URL.replace("AK", BAIDU_API_AK).replace("LOCATION", Latitude+","+Longitude);
		JSONObject jsonObject = doGetStr(url,ENCODING);
		return jsonObject;
	}
	
	/**获取城市的代码***/
	public  static String getCityWeather(String cityName) throws DocumentException{
		  String cityCode = null;
		  for(Object objempno : list_row ){
			    Element el_empno = (Element)objempno;
			    if(cityName.contains(el_empno.attributeValue("name"))){
			    	cityCode = el_empno.attributeValue("weatherCode");
			    	break;
			    }
		   }
		  Map<String,Object> map = new HashMap<String,Object>(); 
		  StringBuffer sbf = new StringBuffer();
		  if(!StringUtils.isBlank(cityCode)){
			  /**根据城市代码查询天气******/
			  String weatherUrl = WEATHER_API_URL.replace("cityCode", cityCode);
			  Response response = Http.get(weatherUrl);
			  JSONObject jsonObject = JSONObject.fromObject(response.getContent());
			  /***天气预报的详情****/
			  JSONObject weatherinfo = jsonObject.getJSONObject("data");
			  String city = weatherinfo.getString("city");
			  JSONArray forecastArray = weatherinfo.getJSONArray("forecast");
			  JSONObject result = (JSONObject) forecastArray.get(0);
			  result.put("ganmao", geWatherDescribe(forecastArray));//生活指导
			  result.put("city", city);
			  result.put("trafficControl", queryTrafficControl(cityCode));
			  sbf.append(result.toString());
		  }
		  return sbf.toString();
	}
	
	public static String geWatherDescribe(JSONArray forecastArray){
		 JSONObject result = (JSONObject) forecastArray.get(0);
		 JSONObject result1 = (JSONObject) forecastArray.get(1);
		 JSONObject result2 = (JSONObject) forecastArray.get(2);
		 StringBuffer sb = new StringBuffer();
		 sb.append(result.getString("type"));
		 sb.append(result1.getString("type"));
		 sb.append(result2.getString("type"));
		 if(sb.toString().contains("雨")){
			 return NOT_CAR_WASH;
		 }
		return YES_CAR_WASH;
	}
	
	public static String queryTrafficControl(String cityCode){
		 Date date= new Date(); 
		 Calendar cal = Calendar.getInstance();
	     cal.setTime(date);
	     int weekDay = cal.get(Calendar.DAY_OF_WEEK) -1;
	     SimpleDateFormat dateFormate = new SimpleDateFormat("dd");//输出的日
	     String dayStr = dateFormate.format(date);
	     int day = Integer.valueOf(dayStr.substring(1));
		 String VehicleNo = null;
		
		//单双号   哈尔滨  武汉
		if("101050101".equals(cityCode) || "101200101".equals(cityCode)){
			if(day%2==0){
				return flagNO+evenlarControlCount;
			}else{
				return flagNO+singularControlCount;
			}
		}
		//济南  单号日限双数
		if("101120101".equals(cityCode)){
			if(day%2==0){
				return flagNO+singularControlCount;
			}else{
				return flagNO+evenlarControlCount;
			}
		}
		//杭州  贵阳，南昌 兰州  北京,天津周六和周日不限行
		if(("101210101".equals(cityCode) || "101260101".equals(cityCode) || 
				"101240101".equals(cityCode) || "101160101".equals(cityCode) || 
				"101010100".equals(cityCode) || "101030100".equals(cityCode)) && 
				(weekDay == 0 || weekDay == 6) ){
			return flag;
		}
		
		//长春 31号不限行 
		if("101060101".equals(cityCode)){
			if("31".equals(dayStr)){
				return flag;
			}
			return flagNO+day;
		}
		//兰州
		if("101160101".equals(cityCode)){
			if(day == 1 || day == 6){
				VehicleNo = gyArray[0];
			}else if(day == 2 || day == 7){
				VehicleNo = gyArray[1];
			}else if(day == 3 || day == 8){
				VehicleNo = gyArray[2];
			}else if(day == 4 || day == 9){
				VehicleNo = gyArray[3];
			}else if(day == 5 || day == 0){
				VehicleNo = gyArray[4];
			}
			return flagNO+VehicleNo;
		}
		
		//杭州
		if("101210101".equals(cityCode)){
			VehicleNo = hzArray[weekDay-1];
			return flagNO+VehicleNo;
		}
		//贵阳，南昌
		if("101260101".equals(cityCode) || "101240101".equals(cityCode)){
			VehicleNo = gyArray[weekDay-1];
			return flagNO+VehicleNo;
		}
		if("101010100".equals(cityCode) || "101030100".equals(cityCode)){
			return specialflagNO;
		}
		return flag;
	}
}
