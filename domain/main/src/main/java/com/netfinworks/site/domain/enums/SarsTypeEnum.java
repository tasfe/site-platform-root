package com.netfinworks.site.domain.enums;

public enum SarsTypeEnum {
	
	REGISETER("regiseter","CP201","注册"), 
	LOGIN("login","CP301", "登录");
	
	private String code;
	private String checkpoint;
	private String  msg;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCheckpoint() {
		return checkpoint;
	}

	public void setCheckpoint(String checkpoint) {
		this.checkpoint = checkpoint;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	private SarsTypeEnum(String code, String checkpoint, String msg) {
		this.code = code;
		this.checkpoint = checkpoint;
		this.msg = msg;
	}
	
	public static String getCheckPointByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SarsTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item.getCheckpoint();
            }
        }
        return null;
    }
	
	
	

}
