package com.netfinworks.site.domain.enums;

/**
 * 速卡充值類型
 * @author admin
 *
 */
public enum SkRechargeType {
	//话费
	TRADE_FEES("FEES", "客官，您的银子我们已经收到，这就挽起袖子帮您充值。","重要提醒：话费预计在10分钟左右内到账，请注意查收。月初月末优惠客官太多，加上运营商大哥时常维护，所以到账偶尔会比较慢，请海涵。 "),
	//流量
	TRADE_FLOW("FLOW", "客官，您的银子我们已经收到，这就挽起袖子帮您充值。","重要提醒：流量预计在10分钟左右内到账，请注意查收。月初月末优惠客官太多，加上运营商大哥时常维护，所以到账偶尔会比较慢，请海涵。");
	
	// 油卡  备注： 重要提醒：流量预计在10-30分钟左右内到账，请注意查收。在油卡使用前，请务必到油站服务区进行【圈存】。
	private final String code;

    private final String message;
    
    private final String remark;
    
    private SkRechargeType(String code,  String message, String remark) {
        this.code = code;
        this.message = message;
        this.remark = remark;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

	public String getRemark() {
		return remark;
	}
    
}
