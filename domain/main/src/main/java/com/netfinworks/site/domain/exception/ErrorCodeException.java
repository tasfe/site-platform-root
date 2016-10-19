package com.netfinworks.site.domain.exception;

/**
 *
 * <p>错误码异常类</p>
 * @author Your name
 * @version $Id: ErrorCodeException.java, v 0.1 2013-11-18 下午4:20:12 lilun Exp $
 */
public class ErrorCodeException extends Exception {

    private static final long serialVersionUID = 4005358808809902012L;

    private String            errorCode;
    private String            errorMsg;
    private String            memo;

    public ErrorCodeException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public static class CommonException extends ErrorCodeException {

        private static final long serialVersionUID = -3004891288021568474L;

        public CommonException(String errorCode, String errorMsg) {
            super(errorCode, errorMsg);
        }

    }

}
