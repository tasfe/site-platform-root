package com.netfinworks.site.domain.domain.info;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.site.domain.enums.PlatformTypUnionma;
/**
 * 平台信息
 * @author zhaozq
 * @date 2015年6月25日
 */
public class PlatformInfo {
	private String platformType =PlatformTypUnionma.KJT.getCode(); // 平台类型 ( 永达互联网金融)
	private String platformSystemType="1" ; // 平台系统类型 个人企业平台//1-个人，2-企业
	private String terminal="PC"; // 终端 (PC, APP)
	private String terminalPlatformType; // 终端平台类型 (ANDROID, IOS)
	private String extention; // 扩展字段(只支持JSON格式）
	
	public PlatformInfo() {}
	
	public PlatformInfo(String platformType, String platformSystemType, String terminal, String terminalPlatformType, String extention) {
		super();
		this.platformType = platformType;
		this.platformSystemType = platformSystemType;
		this.terminal = terminal;
		this.terminalPlatformType = terminalPlatformType;
		this.extention = extention;
	}
	
	public String getPlatformType() {
		return platformType;
	}
	public void setPlatformType(String platformType) {
		this.platformType = platformType;
	}
	public String getPlatformSystemType() {
		return platformSystemType;
	}
	public void setPlatformSystemType(String platformSystemType) {
		this.platformSystemType = platformSystemType;
	}
	public String getTerminal() {
		return terminal;
	}
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
	public String getTerminalPlatformType() {
		return terminalPlatformType;
	}
	public void setTerminalPlatformType(String terminalPlatformType) {
		this.terminalPlatformType = terminalPlatformType;
	}
	public String getExtention() {
		return extention;
	}
	public void setExtention(String extention) {
		this.extention = extention;
	}
	
	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
