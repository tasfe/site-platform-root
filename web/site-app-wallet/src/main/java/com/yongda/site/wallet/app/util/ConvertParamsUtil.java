package com.yongda.site.wallet.app.util;

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <br>  参数组装，类型转换,工具类
 * 作者： zhangweijie <br>
 * 日期： 2016/10/28-16:10<br>
 */
public class ConvertParamsUtil {

    public static Map<String, Object> getUserInfo(PersonMember person, AuthInfo info,
                                                  String mobile, String level) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("member_id", person.getMemberId());
        resultMap.put("login_name", person.getLoginName());
        resultMap.put("verify_mobile", mobile);
        resultMap.put("name", person.getMemberName());
        resultMap.put("real_name_level", CertifyLevel.getByCode(level));
        resultMap.put("idcard_no", info.getAuthNo());
        //resultMap.put("faceImageUrl", person.getFaceImageUrl());//头像路径
        resultMap.put("bankCardCount", person.getBankCardCount());//银行卡数量
        return resultMap;
    }

    /**JavaBean转换成Map****/
    public static Map<String, Object> transBeanToMap(Object obj) {
        if(obj == null){
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    map.put(key, value);
                }

            }
        } catch (Exception e) {
            System.out.println("transBean2Map Error " + e);
        }

        return map;
    }

    /**转换并输出****/
    public static String transBeanToMapToString(Object obj) {
        if(obj == null){
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    map.put(key, value);
                }

            }
        } catch (Exception e) {
            System.out.println("transBean2Map Error " + e);
        }

        return map.toString();
    }

    /**Map转换javaBean*****/
    public static void transMapToBean(Map<String, Object> map, Object obj) {
        if (map == null || obj == null) {
            return;
        }
        try {
            BeanUtils.populate(obj, map);
        } catch (Exception e) {
            System.out.println("transMap2Bean2 Error " + e);
        }
    }
}
