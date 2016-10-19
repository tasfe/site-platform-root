package com.netfinworks.site.domain.enums;

public enum SplitTag {
    REBAT("rebate", "佣金"), COIN("coin", "金币"), REFUND_REBAT("refund_rebate", "佣金退款"), REFUND_COIN(
                                                                                                  "refund_rebate",
                                                                                                  "金币退款");
    private String code;
    private String message;

    private SplitTag(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
