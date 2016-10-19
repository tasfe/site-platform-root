package com.netfinworks.site.ext.integration.sars.convert;

import java.util.HashMap;
import java.util.Map;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.enums.SarsTypeEnum;
import com.netfinworks.site.ext.integration.util.DateUtils;

public class SarsConvert {
	
	public static Map<String,Object> createRequest(String type, BaseMember member, OperationEnvironment env) {
		Map<String, Object> map=new HashMap<String,Object>();
		map.put("rms_ip_addr", env.getClientIp());
		map.put("Rms_mac", env.getClientMac());
		map.put("rms_login_name", member.getLoginName());
		map.put("version","1" );
		map.put("rms_biz_category","web");
		map.put("rms_biz_code",type);
		map.put("checkPoint", SarsTypeEnum.getCheckPointByCode(type));
		map.put("rms_dt_request", DateUtils.getStringDate(DateUtils.PATTERN_FULL));
		return map;
	}

}
