/**
 *
 */
package com.netfinworks.site.ext.integration.ues;

import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>调用UES</p>
 * @author fjl
 * @version $Id: IRemoteUesServiceFacade.java 21711 2012-10-25 08:13:29Z fangjilue $
 */
public interface UesServiceClient {

    /**
     * 调用 ues 加密单个数据
     * @param src
     * @return
     */
    public String encryptData(String src) throws BizException;

    /**
     * 调用 ues 加密单个数据，并设置摘要信息
     * @param srcData
     * @return
     */
    public String encryptData(EncryptData srcData) throws BizException;

    /**
     * @param ticket
     * @return
     */
    public String getDataByTicket(String ticket) throws BizException;

}
