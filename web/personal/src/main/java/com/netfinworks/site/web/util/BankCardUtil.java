
package com.netfinworks.site.web.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.enums.BankType;


/**
 *
 * <p>银行卡</p>
 * @author qinde
 * @version $Id: BankCardUtil.java, v 0.1 2013-11-28 下午8:47:34 qinde Exp $
 */
public class BankCardUtil {

    /**
     * 生成银行信息
     */
    public static  List<BankCard>  initProvince (){
        List<BankCard> list = new ArrayList<BankCard>();
        for (BankType s : BankType.values()) {
            BankCard card = new BankCard();
            card.setBankId(s.getCode());
            card.setBankName(s.getMessage());
            list.add(card);
        }
        return list;
    }


    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void main(String[] args) {
        BankCardUtil.initProvince();
    }
}
