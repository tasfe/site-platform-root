package com.netfinworks.site.domain.domain.response;

import com.netfinworks.biz.common.util.BaseResult;

/**
 * <p>
 * 打款验证响应
 * </p>
 * 
 * @author liuchen
 * @version 创建时间：2015-2-10 下午8:10:48
 */
public class VerifyAmountResponse extends BaseResult {

	/** 剩余验证次数 */
	private int remainCount = 0;

	public int getRemainCount() {
		return remainCount;
	}

	public void setRemainCount(int remainCount) {
		this.remainCount = remainCount;
	}

}
