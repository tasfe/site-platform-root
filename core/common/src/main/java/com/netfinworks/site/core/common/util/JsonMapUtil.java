/**
 * 
 */
package com.netfinworks.site.core.common.util;

import java.util.Map;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.TypeReference;
import com.meidusa.fastjson.serializer.SerializerFeature;
import com.netfinworks.common.lang.StringUtil;

/**
 * <p>Json字符串工具类</p>
 * @author fjl
 * @version $Id: JsonMapUtil.java 70869 2013-10-29 02:21:31Z fangjilue $
 */
public class JsonMapUtil {

    /**
     * 把JSON String 转换成 Map
     * @param jsonStr
     * @return
     */
    public static Map<String, Object> jsonToMap(String jsonStr) {
        if (StringUtil.isBlank(jsonStr)) {
            return null;
        }
        Map<String, Object> extMap = JSON.parseObject(jsonStr,
            new TypeReference<Map<String, Object>>() {
            });
        return extMap;
    }
    
    
    /**
     * 把JSON String 转换成 Map
     * @param jsonStr
     * @return
     */
    public static Map<String, String> jsonToMapStr(String jsonStr) {
        if (StringUtil.isBlank(jsonStr)) {
            return null;
        }
        Map<String, String> extMap = JSON.parseObject(jsonStr,
            new TypeReference<Map<String, String>>() {
            });
        return extMap;
    }

    /**
     * 把 Map 转换成 JSON String
     * @param map
     * @return
     */
    public static String mapToJson(Map<String, Object> map) {
        if(map == null || map.isEmpty()){
            return null;
        }
        return JSON.toJSONString(map, SerializerFeature.UseISO8601DateFormat);
    }
}
