package com.yongda.site.wallet.app.util.test;

import org.apache.commons.httpclient.HttpClient;
import org.nutz.http.Http;
import org.nutz.http.Response;

import java.io.IOException;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/12/21-15:36<br>
 */
public class NutzHttpClient {

    //百度秘钥
    private static final String BAIDU_API_AK = "lDkGl5U3toyTZ7QouwNxF3H06c2YjskE";
    //百度地图url
    private static final String BAIDU_API_URL = "http://api.map.baidu.com/geocoder?location=LOCATION&output=json&key=AK";

    public static void main(String[] args) {
        //for (int i =0;i<10;i++) {
            String Latitude = "30.329189";
            String Longitude = "120.176170";
            TestHttpClient(Latitude, Longitude);
        //}
    }

    public  static void TestHttpClient(String Latitude,String Longitude){
            String url = BAIDU_API_URL.replace("AK", BAIDU_API_AK).replace("LOCATION", Latitude + "," + Longitude);
            Response response = Http.get(url);
            //处理内容
            System.out.println(new String(response.getContent()));
            System.out.println();
    }
}
