package com.yongda.site.wallet.app.common.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>日志拦截器</p>
 * @author eric
 * @version $Id: LoggerInterceptor.java, v 0.1 2013-7-30 下午5:25:25 Exp $
 */
public class LoggerInterceptor implements MethodInterceptor {

    private String loggerName;

    /*
     * (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } catch (Throwable throwable) {
            getLogger().error("执行异常", throwable);

            throw throwable;
        } finally {
            if (getLogger().isDebugEnabled()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                getLogger().debug(
                    invocation.getMethod().getDeclaringClass().getName() + "."
                            + invocation.getMethod().getName() + "执行[" + elapsedTime + "]毫秒");
            }
        }
    }

    /**
     * 获取日志对象
     * @return
     */
    private Logger getLogger() {
        return LoggerFactory.getLogger(loggerName);
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }
}
