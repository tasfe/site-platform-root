package com.netfinworks.site.web.common.util;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.voucher.common.utils.JsonUtils;

public class JSONUtil {
	
	public EnterpriseMember getMemberName(String userString) {
		if(StringUtils.isNotBlank(userString)) {
            try {
                return  JsonUtils.parse(userString, EnterpriseMember.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
	}

}
