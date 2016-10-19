package com.netfinworks.site.core.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.fastjson.JSON;

public class HttpClientUtil {
	
	private static Logger log = LoggerFactory.getLogger(HttpClientUtil.class);  
  
    // 读取超时  
    private final static int SOCKET_TIMEOUT = 10000;
    // 连接超时  
    private final static int CONNECTION_TIMEOUT = 10000;  
    // 每个HOST的最大连接数量  
    private final static int MAX_CONN_PRE_HOST = 20;  
    // 连接池的最大连接数  
    private final static int MAX_CONN = 60;
    // 连接池  
    private final static HttpConnectionManager httpConnectionManager;  
  
    static {  
        httpConnectionManager = new MultiThreadedHttpConnectionManager();  
        HttpConnectionManagerParams params = httpConnectionManager.getParams();  
        params.setConnectionTimeout(CONNECTION_TIMEOUT);
        params.setSoTimeout(SOCKET_TIMEOUT);  
        params.setDefaultMaxConnectionsPerHost(MAX_CONN_PRE_HOST);  
        params.setMaxTotalConnections(MAX_CONN);  
    }
    
   static public interface ResponseParse<T>{
	   public T getEntry(InputStream in);
   }
   
    public static <T> T doRequest(HttpMethodBase method,ResponseParse<T> parse) throws HttpException, IOException {  
        HttpClient httpClient = new HttpClient(httpConnectionManager);  
        httpClient.executeMethod(method);
        try{
        	return parse.getEntry(method.getResponseBodyAsStream());
        }finally{
        	method.releaseConnection();
        }
    }
    
    public static NameValuePair[] buildNameValuePairs(Map<String, String> params) {
        Object[] keys = params.keySet().toArray();  
        NameValuePair[] pairs = new NameValuePair[keys.length];  
        for (int i = 0; i < keys.length; i++) {  
            String key = (String) keys[i];  
            pairs[i] = new NameValuePair(key, params.get(key));  
        }  
        return pairs;
    }
    
    public static PostMethod buildPostFormMethod(String url,Map<String, String> params,String charset){
    	NameValuePair[] pairs = buildNameValuePairs(params);//组装参数  
        PostMethod postMethod = new PostMethod(url);//放地址  
        postMethod.setRequestHeader("Content-type",  
                "application/x-www-form-urlencoded; charset=" + charset);  
        postMethod.setRequestBody(pairs);  
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler()); 
        return postMethod;
    }
    
    public static PostMethod buildPostJsonMethod(String url,Object params){
    	String jsonBody = "";
    	if(params instanceof String){
    		jsonBody = (String)params;
    	}else if(params instanceof Map){
    		jsonBody = JSON.toJSONString(params);
    	}
    	StringRequestEntity entity;
		try {
			entity = new StringRequestEntity(jsonBody, "application/json", null);
		} catch (UnsupportedEncodingException e) {
			log.error("构建请求失败", e);
			return null;
		}
        PostMethod postMethod = new PostMethod(url);//放地址 
        postMethod.setRequestEntity(entity);
        //postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler()); 
        return postMethod;
    }
    
    public static PostMethod buildPostTextMethod(String url,String content,String charset){
    	StringRequestEntity entity;
		try {
			entity = new StringRequestEntity(content, "text/html", charset);
		} catch (UnsupportedEncodingException e) {
			log.error("构建请求失败", e);
			return null;
		}
        PostMethod postMethod = new PostMethod(url);//放地址  
        postMethod.setRequestEntity(entity);
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler()); 
        return postMethod;
    }
    
    public static String doGet(GetMethod method,final String charset){
    	String respose = "";
		try {
			respose = doRequest(method,new  ResponseParse<String>() {
				@Override
				public String getEntry(InputStream in){
					String entry = "";
					BufferedReader sin = null;
					try{
						sin = new BufferedReader(new InputStreamReader(in, charset));
			            StringBuffer buffer = new StringBuffer();  
			            String line = "";  
			            while ((line = sin.readLine()) != null) {  
			                buffer.append(line);  
			            }
			            entry = buffer.toString();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}finally {  
			            if (sin != null) {  
			                try {  
			                	sin.close();  
			                } catch (IOException e) {  
			                    e.printStackTrace();  
			                }  
			            }
					}
					return entry;
				}
			});
		} catch (SocketTimeoutException e) {  
            log.error("连接超时",e);  
            respose = "连接超时";  
        } catch (HttpException e) {  
            log.error("读取外部服务器数据失败",e);
            respose = "读取外部服务器数据失败";  
        } catch (UnknownHostException e) {
            log.error("请求的主机地址无效",e);  
            respose = "请求的主机地址无效";
        } catch (IOException e) {
        	log.error("向外部接口发送数据失败",e);  
        	respose = "向外部接口发送数据失败";  
		}
    	return respose;
    }
    
    public static String doPostAsString(PostMethod method,final String charset){
    	String respose = "";
		try {
			respose = doRequest(method,new  ResponseParse<String>() {
				@Override
				public String getEntry(InputStream in){
					String entry = "";
					BufferedReader sin = null;
					try{
						sin = new BufferedReader(new InputStreamReader(in, charset));
			            StringBuffer buffer = new StringBuffer();  
			            String line = "";  
			            while ((line = sin.readLine()) != null) {  
			                buffer.append(line);  
			            }
			            entry = buffer.toString();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}finally {  
			            if (sin != null) {  
			                try {  
			                	sin.close();  
			                } catch (IOException e) {  
			                    e.printStackTrace();  
			                }  
			            }
					}
					return entry;
				}
			});
		} catch (SocketTimeoutException e) {  
            log.error("连接超时",e);  
            respose = "连接超时";  
        } catch (HttpException e) {  
            log.error("读取外部服务器数据失败",e);
            respose = "读取外部服务器数据失败";  
        } catch (UnknownHostException e) {
            log.error("请求的主机地址无效",e);  
            respose = "请求的主机地址无效";
        } catch (IOException e) {
        	log.error("向外部接口发送数据失败",e);  
        	respose = "向外部接口发送数据失败";  
		}
    	return respose;
    }
}
