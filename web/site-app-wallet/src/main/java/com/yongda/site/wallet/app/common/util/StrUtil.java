package com.yongda.site.wallet.app.common.util;

import org.apache.commons.lang.StringUtils;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/12/7-19:06<br>
 */
public class StrUtil {
    /**
     * 银行卡截取最后的4位
     * @param mobile
     * @return
     */
    public static String captureBankCard(String bankCardNo) {
        if (StringUtils.isBlank(bankCardNo)) {
            return null;
        }
        return bankCardNo.substring(bankCardNo.length()-4,bankCardNo.length());
    }

}
