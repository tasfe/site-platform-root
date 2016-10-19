/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domainservice-main
 * @date 2014年8月11日
 */
package com.netfinworks.site.domainservice.payment;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;

/**
 * 资金冻结、解冻服务
 * @author xuwei
 * @date 2014年8月11日
 */
public interface FundsControlService {
	
	public String freeze(String memberId, String accountId, Money freezeAmount, OperationEnvironment env);

    public boolean unfreeze(String origVourceNo, Money unFreezeAmount, OperationEnvironment env);
}
