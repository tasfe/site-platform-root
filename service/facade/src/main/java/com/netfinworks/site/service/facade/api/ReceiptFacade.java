package com.netfinworks.site.service.facade.api;

import javax.jws.WebService;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.service.facade.request.ReceiptRequest;
import com.netfinworks.site.service.facade.response.ReceiptRespose;


/**
 * <p>电子对账单接口</p>
 * @author zhangyun.m
 * @version $Id: ElectronicDocumentsFacade.java, v 0.1 2014年7月1日 下午1:11:14 zhangyun.m Exp $
 */
@WebService(targetNamespace = "http://facade.site.netfinworks.com")
public interface ReceiptFacade {
    
    
    /**获得电子对账单信息 
     * @param request
     * @param oe
     * @return
     */
    public ReceiptRespose queryReceipt(ReceiptRequest request,OperationEnvironment oe);
    
    
    
    
    

    
}
