/**
 * 
 */
package com.netfinworks.site.ext.integration.cashier.convert;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import com.netfinworks.cashier.facade.domain.PayLimitPO;
import com.netfinworks.cashier.facade.domain.SiteUrlRequest;
import com.netfinworks.cashier.facade.enums.AccessChannel;
import com.netfinworks.site.domain.domain.trade.BatchTradeRequestInfo;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.MemberType;
import com.yongda.site.domain.domain.cashier.PayLimit;

/**
 * <p>收银台转换</p>
 * @author fjl
 * @version $Id: CashierConverter.java, v 0.1 2013-11-28 下午6:07:39 fjl Exp $
 */
public class CashierConvertor {

    /**
     * 单笔充值，转账到收银台
     * @param req
     * @return
     */
    public static SiteUrlRequest convert(TradeRequestInfo req){
        return convert(req,AccessChannel.WEB.getCode());
    }
    
    public static SiteUrlRequest convert(TradeRequestInfo req,String accessChannel){
        SiteUrlRequest request = new SiteUrlRequest();
        MemberType type = req.getPayer().getMemberType();
        request.setMemberType(type.getCode());
        List<String> rest = new ArrayList<String>();
        rest.add(req.getTradeVoucherNo());
        request.setRequestNoList(rest);
        request.setBizIdentity(req.getTradeType().getBizProductIdentity());
        request.setAccessChannel(AccessChannel.getByCode(accessChannel));
        return request;
    }
    
    /**
     * 批量付款到收银台
     * @param req
     * @return
     */
    public static SiteUrlRequest convert(BatchTradeRequestInfo req){
        SiteUrlRequest request = new SiteUrlRequest();
        MemberType type = req.getPayer().getMemberType();
        request.setMemberType(type.getCode());
        request.setRequestNoList(req.getTradeVoucherNo());
        request.setBizIdentity(req.getTradeType().getBizProductIdentity());
        return request;
    }
    
    public static List<PayLimit> convertResponse(List<PayLimitPO> resp){
    	List<PayLimit> result = new ArrayList<PayLimit>();
    	if(CollectionUtils.isNotEmpty(resp)){
    		for(PayLimitPO po:resp){
    			PayLimit vo = new PayLimit();
        		BeanUtils.copyProperties(po, vo);
        		result.add(vo);
    		}
    	}
    	return result;
    }
}
