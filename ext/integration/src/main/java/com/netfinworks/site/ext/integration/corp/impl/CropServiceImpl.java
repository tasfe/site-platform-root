package com.netfinworks.site.ext.integration.corp.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.meidusa.fastjson.JSON;
import com.netfinworks.common.util.MD5Builder;
import com.netfinworks.site.core.common.util.HttpUtil;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.member.PlateUser;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.corp.CropService;

/**
 *
 * <p>获取平台用户信息</p>
 * @author qinde
 * @version $Id: CropServiceImpl.java, v 0.1 2013-12-20 上午9:27:11 qinde Exp $
 */
@Service("cropService")
public class CropServiceImpl implements CropService {

    private Logger logger = LoggerFactory.getLogger(CropServiceImpl.class);

    private String signMethod;

    private String signKey;

    private String url;

    private String userUrl;

    @Override
    public String getCropId(String uid) throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询公司ID，请求参数：会员uid:{}", uid);
                beginTime = System.currentTimeMillis();
            }

            String signature = HttpUtil.getCorpSignature(signMethod, uid, signKey);
            Map<String, String> map = new HashMap<String, String>();
            map.put("signature", signature);
            map.put("sign_method", signMethod);
            map.put("uid", uid);
            String result;
            result = HttpUtil.getCorpId(map, url);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询公司ID， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
            return getId(result);

        } catch (Exception e) {
            logger.error("查询公司ID： "+uid+"信息异常:请求信息", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),e.getCause());
        }
    }

    @Override
    public PlateUser queryQijiaMember(String mobile) throws BizException{
        String result = null;
        long beginTime = 0L;
        Map<String, String> map = new HashMap<String, String>();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("查询永达互联网金融平台会员信息，请求参数：会员mobile:{}", StarUtil.mockMobile(mobile));
                beginTime = System.currentTimeMillis();
            }
            map.put("mobile", mobile);
            map.put("sign_method", signMethod);
            map.put("signature", createMd5Sing(mobile));
            result = HttpUtil.getUserInfo(map, userUrl);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询永达互联网金融平台会员信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
            if(logger.isInfoEnabled()) {
                logger.info("查询会员头像返回结果：{}",result);
            }
            return getPlateUser(result);

        } catch (Exception e) {
            logger.error("查询永达互联网金融平台会员信息异常,会员手机号: {},返回结果：{}", StarUtil.mockMobile(mobile), result, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
        }

    }

    public String getSignMethod() {
        return signMethod;
    }

    public void setSignMethod(String signMethod) {
        this.signMethod = signMethod;
    }

    public String getSignKey() {
        return signKey;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    private String getId(String result) throws Exception {
        Map info = (Map) JSON.parse(result.replace("null", ""));
        boolean success = (Boolean) info.get("success");
        if (success) {
            return String.valueOf(info.get("company_id"));
        }
        return null;
    }

    private PlateUser getPlateUser(String msg) throws Exception {
        Map info = (Map) JSON.parse(msg.replace("null", ""));
        PlateUser user = new PlateUser();
        Object result = info.get("result");
        Map userInfo =  (Map) JSON.parse(String.valueOf(result));
        if (result != null) {
            user.setLast_login_time(String.valueOf(userInfo.get("last_login_time")));
            user.setFace_image_url(String.valueOf(userInfo.get("face_image_url")));
            return user;
        }
        return null;
    }

    private String createMd5Sing(String mobile) {
        StringBuffer sb = new StringBuffer();
        sb.append(mobile);
        sb.append(signMethod);
        sb.append(signKey);
        String result= MD5Builder.getMD5(sb.toString());
        return result.substring(0,16);
     }
}
