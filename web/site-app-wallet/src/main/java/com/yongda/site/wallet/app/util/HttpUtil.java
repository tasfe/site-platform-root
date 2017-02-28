package com.yongda.site.wallet.app.util;

import com.netfinworks.site.core.common.RestResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.http.ParseException;
import org.nutz.http.Http;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/11/16-16:48<br>
 */
public class HttpUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     *
     * @param gatewayUrl  网关请求地址
     * @param map         请求数据
     * @param key         MD5加签key
     * @param timeout     请求超时时间
     * @param restP        数据响应对象
     * @return
     */
    public static RestResponse doPost(String gatewayUrl, Map<String, Object> map,
                                             String key,String timeout,RestResponse restP){
        try {
            String Str = doPostStr(gatewayUrl,map,key,Integer.parseInt(timeout));
            logger.info("查询网关地址响应信息：{}",Str);
            if (StringUtils.isBlank(Str)){
                restP.setSuccess(false);
                restP.setMessage("网关请求失败");
                return restP;
            }
            Map<String, Object> resultMap = new HashMap<String, Object>();
            String[] resultStr = Str.split("&");
            /**
             * 返回String格式的字符串无法转换成map..json..
             */
            String is_success = resultStr[0].replace("is_success=","");
            if(!is_success.equalsIgnoreCase("T")){
                restP.setSuccess(false);
                restP.setCode(resultStr[2].replace("error_code=",""));
                restP = ResponseUtil.buildExpResponse(resultStr[2].replace("error_code=",""), resultStr[3].replace("error_message=",""));
                return restP;
            }
            String cashierUrl  = resultStr[4].replace("cashierUrl=","");
            resultMap.put("cashierUrl",cashierUrl);
            resultMap.put("token",cashierUrl.replace(cashierUrl.substring(0,cashierUrl.indexOf("token=")+6),""));
            resultMap.put("memberType",resultStr[5].replace("memberType=",""));
            restP.setSuccess(true);
            restP.setData(resultMap);
            restP.setMessage("网关请求成功");
            return restP;
        } catch (Exception e) {
            logger.error("查询网关地址响应出错：{}",e);
            restP.setSuccess(false);
            restP.setMessage("网关请求失败"+e.getMessage());
        }
        return restP;
    }


    /**
     * nutz http表单提交Post请求
     * POST请求
     * @param url
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static String doPostStr(String url, Map<String, Object> map,String key,int timeout){
        String prestr = MapUtil.createLinkString(map, false); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String sign = null;
        try {
            sign = MD5Util.sign(prestr,key,"UTF-8");
            logger.debug("排序后的字符串：{},请求url:{},MD5签名key:{},加签结果",prestr,url,key,sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String   result  = null;
        try {
            String trade_list = URLEncoder.encode((String) map.get("trade_list"),"utf-8");
            String memo = URLEncoder.encode((String) map.get("memo"),"utf-8");
            map.put("sign",sign);
            map.put("pay_method",StringUtils.EMPTY);
            map.put("sign_type","MD5");
            map.put("trade_list",trade_list);
            map.put("memo",memo);
            logger.info("网关请求参数：{}",map.toString());
            result = Http.post(url,map,timeout);
        } catch (Exception e) {
            logger.info(""+e);
        }
        return result;
    }

}
