/**
 *
 */
package com.netfinworks.site.domainservice.ues;

import com.netfinworks.service.exception.ServiceException;

/**
 * <p>调用UES</p>
 * @author fjl
 * @version $Id: IRemoteUesServiceFacade.java 21711 2012-10-25 08:13:29Z fangjilue $
 */
public interface DefaultUesService {

    /**
     * 调用 ues 加密单个数据
     * @param src
     * @return
     */
    public String encryptData(String src) throws ServiceException;

    /**
     * 解密
     * @param ticket 密文
     * @return 明文
     * @throws ServiceException
     */
    public String getDataByTicket(String ticket) throws ServiceException;
}
