/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domain-main
 * @version 1.0.0
 * @date 2014年11月12日 徐威/2014年11月12日/首次创建
 */
package com.netfinworks.site.domain.exception;

import com.netfinworks.common.lang.exception.ChainedException;

/**
 * <p>
 * 业务服务层异常类
 * </p>
 * 
 * @author 徐威
 * @since jdk6
 * @date 2014年11月12日
 */
public class ServiceException extends ChainedException {
    private static final long serialVersionUID = 3427328890517025634L;

    /**
     * 创建一个异常。
     */
    public ServiceException() {
        super();
    }

    /**
     * 创建一个异常。
     * 
     * @param message 异常信息
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * 创建一个异常。
     * 
     * @param message 异常信息
     * @param cause 异常原因
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个异常。
     * 
     * @param cause 异常原因
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
}
