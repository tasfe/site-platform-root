package com.yongda.site.wallet.app.util.test;


import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.nutz.http.Http;
import org.nutz.http.Response;


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

import java.io.IOException;
import java.util.Map;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/11/18-14:51<br>
 */
public class TestJob{
   /* *
     * POST请求
     *
     * @param url
     * @param outStr
     * @return
     * @throws ParseException
     * @throws IOException
     * */
   /* public static JSONObject doPostStr(String url, Map map) {
        HttpClient client = new DefaultHttpClient();
        HttpPost httpost = new HttpPost(url);
        JSONObject jsonObject = null;
        httpost.setEntity(new StringEntity(map, "UTF-8"));
        HttpResponse response;
        String result;
        try {
            response = client.execute(httpost);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");
            jsonObject = JSONObject.fromObject(result);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }*/

    public static void main(String[] args) {
        Map sParaTemp = new HashedMap();
        sParaTemp.put("service", "create_mobile_recharge_trade");//接口名称-v1.0,
        sParaTemp.put("version", "1.0");//接口版本
        sParaTemp.put("partner_id", "200000010039");//合作者身份ID/商户号--200000010039
        sParaTemp.put("_input_charset", "UTF-8");//字符集/参数编码字符集
        sParaTemp.put("sign", ""); //签名--签约合作方的钱包唯一用户号 不可空（就是下面的map）
        sParaTemp.put("sign_type", "");//签名方式--只支持ITRUSSRV不可空
        //sParaTemp.put("return_url", webResource.getWalletrechargeNotifyUrl());// 返回页面路径/页面跳转同步返回页面路径
        sParaTemp.put("memo", "话费-"+30+"-13770539272");//备注
        sParaTemp.put("risk_item", "{\"recharge_mobile\":13770539272\"}");//充值手机号
        sParaTemp.put("request_no", "13770539272");//商户网站请求号
        sParaTemp.put("trade_list", "asdas");//交易列表
        sParaTemp.put("operator_id", "asdas");//操作员ID
        sParaTemp.put("buyer_id", "13770539272");//nimi买家"anonymous"
        sParaTemp.put("buyer_id_type", "1");//买家ID类型--用户ID平台默认值为1 不可空
        sParaTemp.put("buyer_ip", "192.168.10.105");//买家IP地址--可空
        sParaTemp.put("pay_method", "");//支付方式^金额^备注  可空(支付方式为空，默认跳转收银台)
        sParaTemp.put("go_cashier", "N");//是否转收银台标识 Y使用(默认值)N不使用-
        Response response = Http.post2("http://kmag.yongdapay.cn/mag/gateway/receiveOrder.do",sParaTemp,2000);
        String content = response.getContent();
        System.out.println(content);
    }
}
