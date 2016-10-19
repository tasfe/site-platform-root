package com.netfinworks.site.domain.domain.info;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * <p>银行卡信息</p>
 * @author qinde
 * @version $Id: BankCode2Name.java, v 0.1 2013-11-29 下午12:42:38 qinde Exp $
 */
public class BankInfo {
    private static Map<String, String> map = new HashMap<String, String>();

    static {
        map.put("ICBC", "工商银行");
        map.put("ABC", "农业银行");
        map.put("CMB", "招商银行");
        map.put("COMM", "交通银行");
        map.put("CCB", "建设银行");
        map.put("BOC", "中国银行");
        map.put("CEB", "光大银行");
        map.put("PSBC", "中国邮政储蓄银行");
        map.put("SPDB", "上海浦东发展银行");
        map.put("CIB", "兴业银行");
        map.put("CMBC", "民生银行");
        map.put("CITIC", "中信银行");
        map.put("BOS", "上海银行");
        map.put("SZPAB", "平安银行");
        map.put("GDB", "广东发展银行");
        map.put("SCB", "渣打银行");
        map.put("CYB", "集友银行");
    }


    public static Map<String, String> getMap() {
        return map;
    }

    public static String getBankName(String bankCode) {
        return map.get(bankCode);
    }

}
