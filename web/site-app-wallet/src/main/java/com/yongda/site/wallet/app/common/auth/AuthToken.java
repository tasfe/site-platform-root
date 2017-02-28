package com.yongda.site.wallet.app.common.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.netfinworks.site.domain.domain.info.Member;

/**
 * <p>为登录用户创建的放在session中的对象，保存了会员、权限、菜单等信息</p>
 * @author eric
 * @version $Id: AuthToken.java, v 0.1 2013-7-16 下午4:50:30  Exp $
 */
public class AuthToken implements UserDetails {
    private static final long      serialVersionUID = -5216741304014703904L;

    /** 会员 */
    private Member                 member;
    /** security认识的权限列�?*/
    private List<GrantedAuthority> authList         = new ArrayList<GrantedAuthority>();

    /**
     * 默认构�?
     */
    public AuthToken() {
    }

    /**
     * 根据会员角色构�?
     * @param member
     * @param authList
     */
    public AuthToken(Member member, List<GrantedAuthority> authList) {
        this.member = member;
        this.authList = authList;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authList;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.userdetails.UserDetails#getPassword()
     */
    @Override
    public String getPassword() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.userdetails.UserDetails#getUsername()
     */
    @Override
    public String getUsername() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.userdetails.UserDetails#isAccountNonExpired()
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.userdetails.UserDetails#isAccountNonLocked()
     */
    @Override
    public boolean isAccountNonLocked() {
    	// 根据会员具体修改
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.userdetails.UserDetails#isCredentialsNonExpired()
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.userdetails.UserDetails#isEnabled()
     */
    @Override
    public boolean isEnabled() {
    	// 根据会员状态修改
        return  false;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
