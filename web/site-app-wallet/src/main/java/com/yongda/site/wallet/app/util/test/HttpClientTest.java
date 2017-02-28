package com.yongda.site.wallet.app.util.test;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/12/21-14:21<br>
 */
public class HttpClientTest {
    //百度秘钥
    private static final String BAIDU_API_AK = "lDkGl5U3toyTZ7QouwNxF3H06c2YjskE";

    //private static final String BAIDU_API_AK = "8G2Hb18ok56bQyb9l4FRRQCIQ01AHOxU";
    //百度地图url
    private static final String BAIDU_API_URL = "http://api.map.baidu.com/geocoder/v2/?location=LOCATION&output=json&ak=AK&pois=0";

    public static void main(String[] args) {
            String Latitude = "30.329189";
            String Longitude = "120.176170";
            TestHttpClientGet(Latitude, Longitude);
    }

    /**
     * HttpClient 的get请求
     * @param Latitude
     * @param Longitude
     */
    public  static void TestHttpClientGet(String Latitude,String Longitude){
        HttpClient httpClient   = new HttpClient();
        String url = BAIDU_API_URL.replace("AK", BAIDU_API_AK).replace("LOCATION", Latitude + "," + Longitude);
        GetMethod getMethod = new GetMethod(url);
        try {
            //设置成了默认的恢复策略，在发生异常时候将自动重试3次，在这里你也可以设置成自定义的恢复策略
            getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler());
            int statusCode  = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + getMethod.getStatusLine());
            }

            //读取内容
            byte[] responseBody = getMethod.getResponseBody();
            //处理内容
            System.out.println(new String(responseBody));
            System.out.println();
        } catch (IOException e) {
            //发生网络异常
            e.printStackTrace();
        }finally{
            //释放连接
            getMethod.releaseConnection();
        }
    }
}
