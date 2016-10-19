package com.yongda.site.app.common.util;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.voucher.common.utils.JsonUtils;

public class JSONUtil {

	public PersonMember getMemberName(String userString) {
	    if(StringUtils.isNotBlank(userString)) {
            try {
                return  JsonUtils.parse(userString, PersonMember.class);
            } catch (Exception e) {
                return null;
            }
	    }
	    return null;
	}

}
