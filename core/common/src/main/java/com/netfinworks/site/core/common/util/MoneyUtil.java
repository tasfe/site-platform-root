/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.core.common.util;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.common.util.money.Money;

/**
 * 通用说明：
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-20 下午1:38:59
 *
 */
public class MoneyUtil {
    public static int SEED = 1000;

    public static String getAmount(Money money) {
        if (money == null) {
            return "";
        }
        return money.getAmount().toString();
    }
    /**
     * 比较金钱是否小于0
     * @param money
     * @return
     */
    public static boolean getAmountSize(Money money) {
        Money m = new Money();
        if(money.compareTo(m)==-1){
        	return true;
        }else{
        	return false;
        }
    }

    public static String getAmountNum(double money) {
        String retValue = null;
        retValue = new DecimalFormat("#0.00").format(money);
        return retValue;
    }

    public static double getNumDouble(Money money) {
        double dou = 0;
        if (money == null) {
            dou = 0;
        } else {
            dou = Double.parseDouble(money.getAmount().toString());
        }
        String retValue = null;
        retValue = new DecimalFormat("#0.00").format(dou);
        return Double.parseDouble(retValue);
    }

    public static Money getMaxMoney(List<String> amountList) {
        Money maxMoney = new Money();
        if (amountList == null || amountList.size() == 0) {
            return maxMoney;
        }
        
        for(String amount : amountList) {
            if (StringUtils.isEmpty(amount)) {
                continue;
            }
            
            Money money = new Money(amount);
            if (money.compareTo(maxMoney) > 0) {
                maxMoney = money;
            }
        }
        
        return maxMoney;
    }
    
}
