package com.netfinworks.site.domain.domain.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.info.PlatformInfo;
/**
 * 主要集成一些请求的公共字段
 * @author zhaozq
 * @date 2015年6月25日
 */

public class UnionmaRequestParam implements UnionmaRequest {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3274668892902727558L;
	
	private PlatformInfo platformInfo 		= new PlatformInfo(); // 平台信息
	private OperationEnvironment enviroment = new OperationEnvironment(); // 操作环境
	private String extention; // 扩展字段(JSON)
	
	public OperationEnvironment getEnviroment() {
		return enviroment;
	}
	public void setEnviroment(OperationEnvironment enviroment) {
		
		this.enviroment = enviroment;
	}

	public PlatformInfo getPlatformInfo() {
		return platformInfo;
	}

	public void setPlatformInfo(PlatformInfo platformInfo) {
		this.platformInfo = platformInfo;
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
