package com.netfinworks.site.ext.integration.util;

import com.netfinworks.common.util.money.Money;

public class MoneyFormat {
	
	public static Money convertMoney(Money money){
		if(money == null){
			money = new Money();
		}
		return money;
	}

}
