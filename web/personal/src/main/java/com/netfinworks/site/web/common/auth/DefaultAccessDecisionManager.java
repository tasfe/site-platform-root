package com.netfinworks.site.web.common.auth;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * <p>访问决策器</p>
 * @author eric
 * @version $Id: DefaultAccessDecisionManager.java, v 0.1 2013-7-16 下午7:05:14  Exp $
 */
public class DefaultAccessDecisionManager implements AccessDecisionManager {
    private static Logger logger = LoggerFactory.getLogger(DefaultAccessDecisionManager.class);

    /*
     * (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionManager#decide(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection)
     */
    @Override
    public void decide(Authentication authentication, Object object,
                       Collection<ConfigAttribute> configAttributes) throws AccessDeniedException,
                                                                    InsufficientAuthenticationException {
        if (configAttributes == null) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info(object.toString());
        }
        Iterator<ConfigAttribute> it = configAttributes.iterator();
        while (it.hasNext()) {
            ConfigAttribute ca = it.next();
            String needRole = ((SecurityConfig) ca).getAttribute();

            for (GrantedAuthority ga : authentication.getAuthorities()) {
                if (needRole.equals(ga.getAuthority())) {
                    return;
                }
            }
        }

        throw new AccessDeniedException("无访问权限");
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionManager#supports(org.springframework.security.access.ConfigAttribute)
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionManager#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

}
