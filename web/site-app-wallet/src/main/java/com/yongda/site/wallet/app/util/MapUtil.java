package com.yongda.site.wallet.app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import com.meidusa.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.TypeReference;
import com.netfinworks.common.lang.StringUtil;

/**
 * <p>JSON字符串转换工具</p>
 * @author wangbin.ben
 * @version $Id: MapUtil.java, v 0.1 2013-2-22 下午1:52:28 User Exp $
 */
public class MapUtil {
    /**
     * JSON字符串转成MAP
     * @param ext
     * @return
     */
    public static Map<String, String> jsonToMap(String ext) {
        Map<String, String> extMap = StringUtil.isBlank(ext) ? new HashMap<String, String>() : JSON
            .parseObject(ext, new TypeReference<Map<String, String>>() {
            });
        return extMap;
    }

    /**
     * MAP转换成JSON
     * @param map
     * @return
     */
    public static String mapToJson(Map map) {
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        return JSON.toJSONString(map, SerializerFeature.UseISO8601DateFormat);
    }


    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params
     *            需要排序并参与字符拼接的参数组
     * @param encode 是否需要urlEncode
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, Object> params, boolean encode) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        String charset = (String) params.get("_input_charset");
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = (String)params.get(key);
            if (StringUtils.isNotBlank(value)){
                if (encode) {
                    try {
                        value = URLEncoder.encode(value, charset);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                    prestr = prestr + key + "=" + value;
                } else {
                    prestr = prestr + key + "=" + value + "&";
                }
            }
        }

        return prestr;
    }

    /**
     * 类似的方法  主要区分request.getParameterMap()取出来的map类型问题  map的get(key)返回的是String[]
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params
     *            需要排序并参与字符拼接的参数组
     * @param encode 是否需要urlEncode
     * @return 拼接后字符串
     */
    public static String createLinkString2(Map<String, Object> params, boolean encode) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        String charset = null;
        try {
            charset = ((String[]) params.get("_input_charset"))[0];
        } catch (Exception e) {
            charset = (String) params.get("_input_charset");
        }
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
           // String value = (String)params.get(key);//出错。map的get(key)返回的是String[]
            String value = ((String[])params.get(key))[0];//主要是针对request.getParameterMap()取出来的map的get(key)返回的是String[]
            if (value!=null){
                if (encode) {
                    try {
                        value = URLEncoder.encode(value, charset);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                    prestr = prestr + key + "=" + value;
                } else {
                    prestr = prestr + key + "=" + value + "&";
                }
            }
        }

        return prestr;
    }

    /**
     * MAP新增VALUE
     * @param source
     * @param key
     * @param value
     * @return
     */
    public static String addValue(String source, String key, String value) {
        Map<String, String> map = MapUtil.jsonToMap(source);
        map.put(key, value);
        return mapToJson(map);
    }

    public static String addValue(String source, Map<String, String> map) {
        Map<String, String> sourceMap = MapUtil.jsonToMap(source);
        if (!CollectionUtils.isEmpty(map)) {
            sourceMap.putAll(map);
        }
        return mapToJson(sourceMap);
    }
}
