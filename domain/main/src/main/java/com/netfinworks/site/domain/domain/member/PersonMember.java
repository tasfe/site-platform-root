package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.enums.CertifyLevel;

/**
 * <p>永达互联网金融会员综合信息</p>
 * @author qinde
 * @version $Id: MemberAllInfo.java, v 0.1 2013-11-19 下午3:41:32 qinde Exp $
 */
public class PersonMember extends BaseMember implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2393418922180711911L;
    /**真实姓名*/
    private EncryptData       realName;

    /** 会员平台类型 */
    private String            platformType;

    /** 会员标识*/
    private String            memberIdentity;
    
    private Integer 		  memberAccountFlag;
    
	/** 真实姓名 */
	private String strRealName;

	/** 是否绑定手机 */
	private boolean isBindPhone;

	/** 实名认证级别 */
	private CertifyLevel certifyLevel;

    public EncryptData getRealName() {
        return realName;
    }

    public void setRealName(EncryptData realName) {
        this.realName = realName;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public String getMemberIdentity() {
        return memberIdentity;
    }

    public void setMemberIdentity(String memberIdentity) {
        this.memberIdentity = memberIdentity;
    }

	/**
	 * @return the memberAccountFlag
	 */
	public Integer getMemberAccountFlag() {
		return memberAccountFlag;
	}

	/**
	 * @param memberAccountFlag the memberAccountFlag to set
	 */
	public void setMemberAccountFlag(Integer memberAccountFlag) {
		this.memberAccountFlag = memberAccountFlag;
	}

	public String getStrRealName() {
		return strRealName;
	}

	public void setStrRealName(String strRealName) {
		this.strRealName = strRealName;
	}

	public boolean isBindPhone() {
		return isBindPhone;
	}

	public void setBindPhone(boolean isBindPhone) {
		this.isBindPhone = isBindPhone;
	}

	public CertifyLevel getCertifyLevel() {
		return certifyLevel;
	}

	public void setCertifyLevel(CertifyLevel certifyLevel) {
		this.certifyLevel = certifyLevel;
	}

	@Override
    public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
