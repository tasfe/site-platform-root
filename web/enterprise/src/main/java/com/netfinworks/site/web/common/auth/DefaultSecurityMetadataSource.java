package com.netfinworks.site.web.common.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.RoleResourceRelation;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domainservice.repository.RoleResourceRepository;

/**
 * <p>初始化资源角色关系</p>
 * @author eric
 * @version $Id: DefaultSecurityMetadataSource.java, v 0.1 2013-7-16 下午6:06:47  Exp $
 */
public class DefaultSecurityMetadataSource implements FilterInvocationSecurityMetadataSource,
                                          CommonConstant {
    private static Logger                                   logger      = LoggerFactory
                                                                            .getLogger(DefaultSecurityMetadataSource.class);
    /** 资源映射 */
    private static Map<String, Collection<ConfigAttribute>> resourceMap = new HashMap<String, Collection<ConfigAttribute>>();

    @Resource(name = "roleRepository")
    private RoleResourceRepository                          roleRepository;

    /**
     * 初始�?
     */
    public void init() {
        List<RoleResourceRelation> relationList = roleRepository.loadAllResourceRelation();
        if (relationList != null && relationList.size() > 0) {
            for (RoleResourceRelation relation : relationList) {
                String url = ResourceInfo.getByCode(relation.getResourceCode()).getUrl();
                if (resourceMap.containsKey(url)) {
                    resourceMap.get(url).add(new SecurityConfig(relation.getRoleCode()));
                } else {
                    Collection<ConfigAttribute> attributeList = new ArrayList<ConfigAttribute>();
                    attributeList.add(new SecurityConfig(relation.getRoleCode()));

                    resourceMap.put(url, attributeList);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.access.SecurityMetadataSource#getAttributes(java.lang.Object)
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        String requestUrl = buildRequestUrl((FilterInvocation) object);
        if (logger.isInfoEnabled()) {
            logger.info("执行请求" + requestUrl);
        }

        return resourceMap.get(requestUrl);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.access.SecurityMetadataSource#getAllConfigAttributes()
     */
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.access.SecurityMetadataSource#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    /**
     * 获取请求URL
     * @param filterInvocation
     * @return
     */
    private String buildRequestUrl(FilterInvocation filterInvocation) {
        String requestURI = filterInvocation.getRequestUrl().split("\\u003F")[0];
        Map<?, ?> paraMeters = filterInvocation.getHttpRequest().getParameterMap();
        String[] methods = (String[]) paraMeters.get(REQUEST_METHOD);
        String method = null;
        if (methods != null) {
            method = methods[0].trim();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(requestURI);
        if (StringUtil.isNotBlank(method)) {
            sb.append("?method=").append(method);
        }

        return sb.toString();
    }
}
