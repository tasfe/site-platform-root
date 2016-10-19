/**
 *
 */
package com.netfinworks.site.domain.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 *
 * <p>银行类型</p>
 * @author qinde
 * @version $Id: BankType.java, v 0.1 2013-11-28 下午12:28:51 qinde Exp $
 */
public enum BankType {
    ICBC("ICBC", "中国工商银行|工行|工商银行"),

    ABC("ABC", "中国农业银行|农行|农业银行"),

    BCOM("COMM", "交通银行|交行"),

    CCB("CCB", "中国建设银行|建行|建设银行"),

    BOC("BOC", "中国银行|中行"),

    CMB("CMB", "招商银行|招行"),

    PAB("SZPAB", "平安银行"),

    CEB("CEB", "中国光大银行|光大银行"),

    SPDB("SPDB", "上海浦东发展银行|浦发银行|浦东发展银行"),

    CIB("CIB", "兴业银行"),

    CITIC("CITIC", "中信银行"),

    CMBC("CMBC", "中国民生银行|民生银行"),

	PSBC("PSBC", "中国邮政储蓄|邮政|邮储|中国邮储银行"),

    BEA("HKBEA", "东亚银行"),

    HXB("HXB", "华夏银行"),

    HZB("HCCB", "杭州银行"),

    NBCB("NBCB", "宁波银行"),

    NJCB("NJCB", "南京银行"),

    SHB("BOS", "上海银行"),

    BOB("BCCB", "北京银行"),

    CBHB("CBHB", "渤海银行"),

    GZCB("GZCB", "广州银行|广州市商业银行"),

    GDB("GDB", "广东发展银行|广发银行"),

    CZB("CZB", "浙商银行"),

    HSB("HSBANK", "徽商银行"),

    SRCB("SHRCB", "上海农村商业银行"),

    BJRCB("BJRCB", "北京农村商业银行|北京农商行"),

    GZRCC("GZRCC", "广州市农村信用合作社"),
    
    RCC("RCC", "农村信用合作社"),
    
    SDE("SDE","顺德农信社"),
    
    EGBANK("EGBANK","恒丰银行"),
    
    SDB("SDB","深圳发展银行"),
    
    WZCB("WZCB","温州市商业银行"),
    
    HKBCHINA("HKBCHINA","汉口银行"),
    
    YDXH("YDXH","尧都信用合作联社"),
    
    RCB("RCB","农村商业银行"),
    
    CZCB("CZCB","浙江稠州商业银行"),
    
    UCC("UCC","城市信用合作社"),
    
    SXJS("SXJS","晋城市商业银行"),   
    
    CITYBANK("CITYBANK","城市商业银行"),
    
    COUNTYBANK("COUNTYBANK","村镇银行"),
    
    SCB("SCB","渣打银行"),
    
    ZHNX("ZHNX","珠海市农村信用合作社"),  
    
    CCQTGB("CCQTGB","重庆三峡银行"),
    
    CYB("CYB","集友银行"),
    
    HSBANK("HSBANK","徽商银行"),
    
    UPOP("UPOP","银联在线支行|银联卡"),
    
    URCB("URCB","农村合作银行"),
    
    HSBC("HSBC","香港上海汇丰银行"),
    
    DBBJ("DBBJ","德意志银行"),
    
    CITIB("CITIB","花旗银行"),
    
    JPM("JPM","摩根大通银行"),
    
    HNNXS("HNNXS","湖南农信社"),
    
    SHFG("SHFG","新韩银行"),
    
    TESTBANK("SHFG","测试银行");

    private String code;
    private String message;

    private BankType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static BankType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (BankType item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
    
    public static BankType getByMsg(String msg) {
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        for (BankType item : values()) {
            String[] msgs = item.getMessage().split("\\|");
            Set<String> msgSet = new HashSet<String>();
            msgSet.addAll(Arrays.asList(msgs));
            if (msgSet.contains(msg)) {
                return item;
            }
        }
        return null;
    }

}
