package com.netfinworks.site.core.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.common.util.MD5Builder;

public class HttpUtil {
	public static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
    /**
     * 获取公司ID签名
     * @return
     */
    public static String getCorpSignature(String method, String uid, String key) {
        StringBuilder sb = new StringBuilder();
        //md5算法
        sb.append(method);
        //uid
        sb.append(uid);
        //md5key
        sb.append(key);
        String str = MD5Builder.getMD5(sb.toString());
        //截取前16个字符
        return str.substring(0, 16);
    }

    /**
     * 获取永达互联网金融用户信息
     * @param map
     * @param path
     * @return
     * @throws Exception
     */
    public static String getUserInfo(Map<String, String> map, String path) throws Exception {
        /*String str = JSONObject.toJSONString(map);
        return submitDataByDoPost(str, path);*/
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        String str = sb.toString();
        return submitDataByDoPost(str, path);
    }

    /**
     * 普通方式的DoPost请求提交数据
     * @param map 传递进来的数据，以map的形式进行了封装
     * @param path 要求服务器servlet的地址
     * <a href="http://my.oschina.net/u/556800" class="referer" target="_blank">@return</a>  返回的boolean类型的参数
     * @throws Exception
     */
    public static String getCorpId(Map<String, String> map, String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        String str = sb.toString();
        return submitDataByDoPost(str, path);
    }
    
    /**
     * 判断请求是否为Ajax请求
     * @param request
     * @return
     */
    public static boolean isAjaxRequest(HttpServletRequest request){
    	String header = request.getHeader("X-Requested-With");
    	boolean isAjax = "XMLHttpRequest".equals(header) ? true:false;
    	return isAjax;
    }

    /**
     * 普通方式的DoPost请求提交数据
     * @param map 传递进来的数据，以map的形式进行了封装
     * @param path 要求服务器servlet的地址
     * <a href="http://my.oschina.net/u/556800" class="referer" target="_blank">@return</a>  返回的boolean类型的参数
     * @throws Exception
     */
    public static String submitDataByDoPost(String params, String path) throws Exception {
        // 注意Post地址中是不带参数的，所以newURL的时候要注意不能加上后面的参数
        URL Url = new URL(path);
        // Post方式提交的时候参数和URL是分开提交的，参数形式是这样子的：name=y&age=6
        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) Url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setReadTimeout(5000);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("accept", "text/xml");
            httpConn.setRequestProperty("Content-Length", String.valueOf(params.getBytes("UTF-8").length));
            OutputStream os = httpConn.getOutputStream();
            os.write(params.getBytes("UTF-8"));
            os.flush();
            os.close();
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuffer result = new StringBuffer();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                in.close();
                return result.toString();
            }
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }
        }
        return null;
    }

    /**
     * 获取网络图片
     * @param path
     * @return
     * @throws Exception
     */
    public static byte[] getWebImg(String path) throws Exception {
        HttpURLConnection conn = null;
        InputStream inStream = null;
        byte[] buffer = null;
        try {
            //new一个URL对象
            URL url = new URL(path);
            //打开链接
            conn = (HttpURLConnection)url.openConnection();
            //设置请求方式为"GET"
            conn.setRequestMethod("GET");
            //超时响应时间为10秒
            conn.setConnectTimeout(10 * 1000);
            //通过输入流获取图片数据
            inStream = conn.getInputStream();
            //得到图片的二进制数据，以二进制封装得到数据，具有通用性
            buffer =   readInputStream(inStream);
            return buffer;
        } catch (Exception e) {
            throw new IOException("获取图片失败");
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
            if (inStream != null) {
                inStream.close();
                inStream = null;
            }
            return buffer;
        }
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while( (len=inStream.read(buffer)) != -1 ){
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }

    private static void getcorp() throws Exception {
        String sign_method = "md5";
        String uid = "100000757";
        String url = "760881697c81313b77c7784e0bb232cc";
        String signature = HttpUtil.getCorpSignature(sign_method, uid, url);
        Map<String, String> map = new HashMap<String, String>();
        map.put("signature", signature);
        map.put("sign_method", sign_method);
        map.put("uid", uid);
        String path = "";
        String cid = HttpUtil.getCorpId(map, path);
        System.out.println(cid);
    }

    private static void getUser() throws Exception {
        String sign_method = "md5";
        String mobile = "13621722085";
        String url = "";
        String key = "760881697c81313b77c7784e0bb232cc";

        StringBuffer sb = new StringBuffer();
        sb.append(mobile);
        sb.append(sign_method);
        sb.append(key);
        String result= MD5Builder.getMD5(sb.toString());

        Map<String, String> map = new HashMap<String, String>();
        map.put("mobile", mobile);
        map.put("sign_method", sign_method);
        map.put("signature", result.substring(0,16));
        String info = HttpUtil.getCorpId(map, url);
        System.out.println(info);
    }

    public static void main(String[] args) throws Exception {
        getWebImg("http://imgmall.tg.com.cn/group1/M00/30/FE/CgoUrFLCjpzFc6pPAAAXjd2QA2A316.jpg");
    }
}
