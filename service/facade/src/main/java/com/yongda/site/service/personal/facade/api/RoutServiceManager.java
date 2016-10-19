package com.yongda.site.service.personal.facade.api;

import java.util.HashMap;
import java.util.Map;

public class RoutServiceManager {
	private static Map<String,IRoutService> routMap = new HashMap<String, IRoutService>();
	
	static public void registService(String routName,IRoutService service){
		routMap.put(routName, service);
	}
	
	static public IRoutService routByName(String routName){
		return routMap.get(routName);
	}
}
