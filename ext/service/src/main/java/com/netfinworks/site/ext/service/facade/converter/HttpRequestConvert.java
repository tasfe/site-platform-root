package com.netfinworks.site.ext.service.facade.converter;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import com.yongda.site.core.common.annotation.HttpField;
import com.yongda.site.service.personal.facade.request.FastRegisterRequest;

/**
 * http请求转换
 * @desc 
 * @author slong
 * @version: 2016年4月25日 下午5:40:46
 */
public class HttpRequestConvert {
	
	public static <T> T convert2Request(Map<String,String> paramsMap,Class<T> clz){
		T obj = null;
		try {
			obj = clz.newInstance();
			copyMapToBean(paramsMap, clz, obj);
		} catch (Exception e) {
		} 
		return obj;
	}

	@SuppressWarnings("unchecked")
	public static <T> T copyMapToBean(Map<String,String> paramsMap,Class<T> clz,T obj){
		if(obj == null){
			try {
				obj = clz.newInstance();
			} catch (Exception e1) {
				return null;
			}
		}
        Field[] declaredFields = clz.getDeclaredFields();
        for(Field declaredField:declaredFields){
        	if((declaredField.getModifiers() & java.lang.reflect.Modifier.STATIC) == java.lang.reflect.Modifier.STATIC){
        		continue;
        	}
        	String httpKey = declaredField.getName();
        	Object propValue = null;
        	HttpField field = declaredField.getAnnotation(HttpField.class);
        	Class<?> declaredFieldClass = declaredField.getType();
        	if(field != null){
        		if(StringUtils.isNotBlank(field.name())){
        			httpKey = field.name();
        		}
        	}
    		String value = paramsMap.get(httpKey);
			if(StringUtils.isNotBlank(value)){
				if(!declaredFieldClass.isEnum()){
					if(declaredFieldClass.getName().equals("java.util.Date")){
						if(field != null && StringUtils.isNotBlank(field.dateFormat()))
							try {
								propValue = new SimpleDateFormat(field.dateFormat()).parse(value);
							} catch (ParseException e) {
								propValue = null;
							}
					} else if (declaredFieldClass.getName().equals("java.lang.String")) {
						propValue = value;
					} else if (declaredFieldClass.getName().equals("java.lang.Integer") || declaredFieldClass.getName().equals("int")) {
						propValue = new java.lang.Integer(value.toString());
					} else if (declaredFieldClass.getName().equals("java.lang.Long")  || declaredFieldClass.getName().equals("long")) {
						propValue = new java.lang.Long(value.toString());
					} else if (declaredFieldClass.getName().equals("java.lang.Double") || declaredFieldClass.getName().equals("double")) {
						propValue = new java.lang.Double(value.toString());
					} else if (declaredFieldClass.getName().equals("java.lang.Float") || declaredFieldClass.getName().equals("float")) {
						propValue = new java.lang.Float(value.toString());
					} else if (declaredFieldClass.getName().equals("java.lang.Short")) {
						propValue = new java.lang.Short(value.toString());
					} else if (declaredFieldClass.getName().equals("java.lang.Byte")) {
						propValue = new java.lang.Byte(value.toString());
					} else if (declaredFieldClass.getName().equals("java.lang.Boolean") || declaredFieldClass.getName().equals("boolean")) {
						propValue = new java.lang.Boolean(value.toString());
					}else{
						continue;
					}
				}else{
					try {
						@SuppressWarnings("rawtypes")
						Class enumClazz = Class.forName(declaredFieldClass.getName());
						propValue = Enum.valueOf(enumClazz, value);
					} catch (Exception e) {
						continue;
					}
				}
				try {
					PropertyUtils.setProperty(obj, declaredField.getName(), propValue);
				} catch (Exception e) {
					
				}
			}
        }
        Class<? super T> superClass = clz.getSuperclass();
        if(superClass != null){
        	copyMapToBean(paramsMap, superClass, obj);
        }
        return obj;   
	}
	
	public static void main(String[] args) {
		Map<String,String> map = new HashMap<String, String>();
		map.put("account", "1234567");
		map.put("service", "create_member");
		map.put("deviceid", "app1234");
		map.put("type", "ID_CARD");
		map.put("id", "1");
		FastRegisterRequest req = convert2Request(map, FastRegisterRequest.class);
		System.out.println(req);
	}
}
