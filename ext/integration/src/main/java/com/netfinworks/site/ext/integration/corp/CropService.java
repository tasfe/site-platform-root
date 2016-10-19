package com.netfinworks.site.ext.integration.corp;

import com.netfinworks.site.domain.domain.member.PlateUser;
import com.netfinworks.site.domain.exception.BizException;

/**
 *
 * <p>获取商户信息</p>
 * @author qinde
 * @version $Id: CropService.java, v 0.1 2013-12-11 下午2:54:32 qinde Exp $
 */
public interface CropService {
    /**
     * 获取公司ID
     * @param uid
     * @return
     */
    public String getCropId(String uid) throws BizException;

    /**
     * 获取平台用户
     * @param uid
     * @return
     * @throws BizException
     */
    public PlateUser queryQijiaMember(String uid) throws BizException;
}
