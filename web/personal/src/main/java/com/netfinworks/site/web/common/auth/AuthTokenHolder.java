package com.netfinworks.site.web.common.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.netfinworks.common.lang.StringUtil;

/**
 * <p>权限相关工具</p>
 * @author eric
 * @version $Id: AuthTokenHolder.java, v 0.1 2011-1-18 下午03:01:08  Exp $
 */
public class AuthTokenHolder {
    /**
     * 获取当前用户信息
     * @return
     */
    public static AuthToken getAuthToken() {
        return (AuthToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 判断当前用户是否登录
     * @return
     */
    public static boolean hasAuthToken() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            return false;
        } else {
            Authentication authentication = securityContext.getAuthentication();
            if (authentication == null) {
                return false;
            } else {
                Object principal = authentication.getPrincipal();
                if (principal instanceof String
                    && StringUtil.equals(principal.toString(), "anonymousUser")) {
                    return false;
                }
            }
        }
        return true;
    }
}
