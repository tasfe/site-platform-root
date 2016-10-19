package com.netfinworks.site.core.common;

import java.io.Serializable;

public class WxRestResponse implements Serializable{
	    private static final long   serialVersionUID = -802482953469560837L;
	    private boolean             success;
	    protected String            message;
	    protected String            code;
	    protected String            redirect;
	    private Object data;;
	    
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public Object getData() {
			return data;
		}
		public void setData(Object data) {
			this.data = data;
		}
		public String getRedirect() {
			return redirect;
		}
		public void setRedirect(String redirect) {
			this.redirect = redirect;
		}
	    
}
