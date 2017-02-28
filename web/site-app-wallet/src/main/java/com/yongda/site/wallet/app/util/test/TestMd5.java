package com.yongda.site.wallet.app.util.test;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/11/30-13:23<br>
 */
public class TestMd5 {
    public static void main(String[] args) {
        Login();
        /*public static void main(String[] args) {
		try {
			String KEY = "1234567890walletappyongda";
			Map map = new HashMap();
			map.put("login_Name","13136480229");
			map.put("login_Password","123456a");
            String signStr = MapUtil.createLinkString(map,false);
			System.out.println("排序后字符串："+signStr);
			String md5Sign = MD5Util.sign(signStr,KEY,"utf-8");
			System.out.println("加签结果："+md5Sign);
             Boolean flag = MD5Util.verify(signStr,"F33F8AACD11503DC1F5220CFA688B981",KEY,"utf-8");
			System.out.println(flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

    }


    public static void Login() {
        String url = "http://kmag.yongdapay.cn/mag/gateway/receiveOrder.do";
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        String memo = "话费-30-13770539272";
        try {
            memo = URLEncoder.encode(memo,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 填入各个表单域的值
        NameValuePair[] data = {
                new NameValuePair("service", "create_mobile_recharge_trade"),//接口名称-v1.0,
                new NameValuePair("version", "1.0"),//接口版本
                new NameValuePair("partner_id", "200000910016"),//合作者身份ID/商户号--200000010039
                new NameValuePair("_input_charset", "UTF-8"),//字符集/参数编码字符集
                new NameValuePair("sign", ""), //签名--签约合作方的钱包唯一用户号 不可空（就是下面的map）
                new NameValuePair("sign_type", "MD5"),//签名方式--只支持ITRUSSRV不可空
                //new NameValuePair("return_url", webResource.getWalletrechargeNotifyUrl());// 返回页面路径/页面跳转同步返回页面路径
                new NameValuePair("memo",memo),//备注
                new NameValuePair("risk_item", "{\"recharge_mobile\":13770539272\"}"),//充值手机号
                new NameValuePair("request_no", "13770539272"),//商户网站请求号
                new NameValuePair("trade_list", "asdas"),//交易列表
                new NameValuePair("operator_id", "asdas"),//操作员ID
                new NameValuePair("buyer_id", "13770539272"),//nimi买家"anonymous"
                new NameValuePair("buyer_id_type", "1"),//买家ID类型--用户ID平台默认值为1 不可空
                new NameValuePair("buyer_ip", "192.168.10.105"),//买家IP地址--可空
                new NameValuePair("pay_method", ""),//支付方式^金额^备注  可空(支付方式为空，默认跳转收银台)
                new NameValuePair("go_cashier", "N"),//是否转收银台标识 Y使用(默认值)N不使用-
        };
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
       /* postMethod.addRequestHeader("Content-Type","text/html;charset=UTF-8");*/
        //postMethod.setRequestHeader("Content-Type", "text/html;charset=UTF-8");
        // 将表单的值放入postMethod中
        postMethod.setRequestBody(data);
        // 执行postMethod
        int statusCode = 0;
        try {
            statusCode = httpClient.executeMethod(postMethod);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HttpClient对于要求接受后继服务的请求，象POST和PUT等不能自动处理转发
        // 301或者302
        if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            // 从头中取出转向的地址
            Header locationHeader = postMethod.getResponseHeader("location");
            String location = null;
            if (locationHeader != null) {
                location = locationHeader.getValue();
                System.out.println("diandianLogin:" + location);
            } else {
                System.err.println("Location field value is null.");
            }
            return;
        } else {
            System.out.println(postMethod.getStatusLine());
            String str = "";
            try {
                str = postMethod.getResponseBodyAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(str);
        }
        postMethod.releaseConnection();
        return;
    }
}
