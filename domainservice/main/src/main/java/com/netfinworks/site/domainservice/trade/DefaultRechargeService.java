/**
 * 
 */
package com.netfinworks.site.domainservice.trade;

import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>钱包充值服务</p>
 * 
 * @author fjl
 * @version $Id: RechargeService.java, v 0.1 2013-11-28 上午10:47:11 fjl Exp $
 */
public interface DefaultRechargeService {
    
    /**
     * 申请充值
     * <li>落地凭证</li>
     * <li>获取收银台地址</li>
     * <li>调用者获得地址后跳转<li>
     * @param currUser 系统当前用户
     * @return 返回收银台地址
     */
    public String applyRecharge(BaseMember currUser,TradeEnvironment env) throws BizException;
    
    public String applyRecharge(BaseMember currUser,TradeEnvironment env,String accessChannel) throws BizException;
}
