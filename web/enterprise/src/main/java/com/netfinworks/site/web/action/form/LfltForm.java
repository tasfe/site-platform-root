/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月7日
 */
package com.netfinworks.site.web.action.form;

/**
 * 限额限次
 * @author xuwei
 * @date 2014年8月7日
 */
public class LfltForm {
	/** 单笔限额 */
	private String quota = "-1";
	
	/** 每日累计限额 */
	private String quotaPerDay = "-1";
	
	/** 每日累计限次 */
	private String timesPerDay = "-1";
	
	/** 每月累计限额 */
	private String quotaPerMonth = "-1";
	
	/** 每月累计限次 */
	private String timesPerMonth = "-1";
	
	/** 每周累计限额 */
	private String quotaPerWeek = "-1";
	
	/** 每周累计限次 */
	private String timesPerWeek = "-1";
	
	/** 每年累计限额 */
	private String quotaPerYear = "-1";
	
	/** 每年累计限次 */
	private String timesPerYear = "-1";

	public String getQuotaPerDay() {
		return quotaPerDay;
	}

	public void setQuotaPerDay(String quotaPerDay) {
		this.quotaPerDay = quotaPerDay;
	}

	public String getTimesPerDay() {
		return timesPerDay;
	}

	public void setTimesPerDay(String timesPerDay) {
		this.timesPerDay = timesPerDay;
	}

	public String getQuotaPerMonth() {
		return quotaPerMonth;
	}

	public void setQuotaPerMonth(String quotaPerMonth) {
		this.quotaPerMonth = quotaPerMonth;
	}

	public String getTimesPerMonth() {
		return timesPerMonth;
	}

	public void setTimesPerMonth(String timesPerMonth) {
		this.timesPerMonth = timesPerMonth;
	}

	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}

	public String getQuotaPerWeek() {
		return quotaPerWeek;
	}

	public void setQuotaPerWeek(String quotaPerWeek) {
		this.quotaPerWeek = quotaPerWeek;
	}

	public String getTimesPerWeek() {
		return timesPerWeek;
	}

	public void setTimesPerWeek(String timesPerWeek) {
		this.timesPerWeek = timesPerWeek;
	}

	public String getQuotaPerYear() {
		return quotaPerYear;
	}

	public void setQuotaPerYear(String quotaPerYear) {
		this.quotaPerYear = quotaPerYear;
	}

	public String getTimesPerYear() {
		return timesPerYear;
	}

	public void setTimesPerYear(String timesPerYear) {
		this.timesPerYear = timesPerYear;
	}
	
}
