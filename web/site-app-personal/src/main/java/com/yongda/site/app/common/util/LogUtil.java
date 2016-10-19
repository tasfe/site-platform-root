package com.yongda.site.app.common.util;

import java.text.MessageFormat;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.member.BaseMember;

public class LogUtil {
	 
	/**
	 * 
	 * @param type  操作类型
	 * @param member  会员信息
	 * @param env
	 * @return
	 */
	 public static String appLog(String type,BaseMember member,OperationEnvironment env) {  
		 String context="操作类型-{0},用户ID-{1}，账号-{2},名称-{3},操作员-{4},IP-{5},MAC-{6}";
		 return MessageFormat.format(context,type,member.getMemberId(),member.getLoginName(),member.getMemberName(),"",env.getClientIp(),env.getClientMac());
	 }
	 
	
}
