/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.core.common.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.common.util.DateUtil;


/**
 * 通用说明：
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-20 下午1:38:59
 *
 */
public class RadomUtil {
    public static int SEED = 1000;
    
    public static long createRadom() {
        return Math.round(Math.random()*9999);
    }
    
    public static long createRandom(int n) {
        int num = (int) Math.pow(10, n);
        return new Random().nextInt((int)num) + num;
    }
    
    public static String getDatetimeId() {
        Date date = Calendar.getInstance().getTime();
        return DateUtil.format(date, DateUtil.shortFormat) 
                + date.getTime() + String.valueOf(createRandom(4));
    }
    
    public static String getUUId() {
        return StringUtils.replace(String.valueOf(UUID.randomUUID()), "-", "");
    }
    
    public static void main(String[] args) {
        System.out.println(getDatetimeId());
        System.out.println(getUUId());
    }
}
