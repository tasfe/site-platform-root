package com.yongda.site.wallet.app.common.auth;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;

import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.Member;
import com.netfinworks.site.domain.domain.info.Role;
import com.netfinworks.site.domainservice.repository.RoleResourceRepository;

/**
 * <p>用户详情服务默认实现</p>
 * @author eric
 * @version $Id: DefaultUserDetailsService.java, v 0.1 2013-7-16 下午1:49:24  Exp $
 */
public class DefaultUserDetailsService implements UserDetailsService, CommonConstant {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUserDetailsService.class);

    /** 角色仓储 */
    @Resource(name = "roleRepository")
    private RoleResourceRepository      roleResourceRepository;

    /*
     * (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
        	// TODO 获取会员
            Member member = null;
			// if (member == null) {
			// throw new UsernameNotFoundException("不存在此用户");
			// }

            List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
            
            // TODO  获取角色列表
            List<Role> roleList = null;
            if (!CollectionUtils.isEmpty(roleList)) {
                for (Role role : roleList) {
                    authList.add(new SimpleGrantedAuthority(role.getCode()));
                }
            }

            return new AuthToken(member, authList);
        } catch (Exception e) {
            logger.error("获取用户登录信息异常", e);
            return null;
        }
    }
}
