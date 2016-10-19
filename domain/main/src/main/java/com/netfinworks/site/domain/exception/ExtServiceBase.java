package com.netfinworks.site.domain.exception;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <p>外部系统接口服务基类</p>
 * @author leelun
 * @version $Id: ExtServiceBase.java, v 0.1 2013-12-19 上午9:55:15 lilun Exp $
 */
public class ExtServiceBase {

    private Logger extLogger = LoggerFactory.getLogger(this.getClass()+":Mag-Ext-LOGGER");

//    private StopWatch sw = new StopWatch();  // 计时器

    public Logger getExtLogger() {
        return extLogger;
    }

    public void setExtLogger(Logger extLogger) {
        this.extLogger = extLogger;
    }

//    public void startWatch() {
//        sw.reset();
//        sw.start();
//    }

//    public void stopWatch() {
//        sw.stop();
//    }

//    public long getEscTime() {
//        try {
//        sw.stop();
//        } catch(IllegalStateException e) {
//            // 已停止
//        }
//        long result = sw.getTime();
//        sw.reset();
//        return result;
//    }
}
