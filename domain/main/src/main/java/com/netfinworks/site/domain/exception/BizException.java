/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domain-main
 * @version 1.0.0
 * @date 2014年11月12日 徐威/2014年11月12日/首次创建
 */
package com.netfinworks.site.domain.exception;

import com.netfinworks.site.domain.enums.ErrorCode;

/**
 * <p>
 * 请编写类注释
 * </p>
 * 
 * @author 徐威
 * @since jdk6
 * @date 2014年11月12日
 */
public class BizException extends Exception {
    private static final long serialVersionUID = 4852957385720871802L;

    /**
     * 应答码
     */
    private ErrorCode         code;

    /**
     * 构造方法
     * 
     * @param code
     */
    public BizException(ErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

    /**
     * 构造方法
     * 
     * @param code
     * @param message
     */
    public BizException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     * 
     * @param code
     * @param message
     * @param cause
     */
    public BizException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getCodeStr() {
        return code.getCode();
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

}
