package com.netfinworks.site.web.action.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.domain.domain.member.OperatorVO;

/**
 * 
 * <p>操作员表单</p>
 * @author luoxun
 * @version $Id: OperatorForm.java, v 0.1 2014-5-23 上午11:09:31 luoxun Exp $
 */
public class OperatorForm implements Serializable {

    private static final long serialVersionUID = -309964115692939169L;

    private String                 memberId;                               //会员编号                                                     
    private String                 operatorId;                             //操纵员编号        
    
	@NotNull(message = "op_nick_name_is_not_null")
    private String            nickName;                               //昵称           
    
	@NotNull(message = "op_login_name_is_not_null")
    private String            loginName;                              //登陆名
    
    private String            loginPwd;                               //登陆密码
    private String            loginPwdConfirm;                        //登陆密码确认

	private String payPwd; // 支付密码
	private String payPwdConfirm; // 支付密码确认

	private String status; // 操作员状态

	private String remark; // 操作员备注
	
	/**
	 * 联系方式
	 */
	private String contact;

    public static OperatorVO convertVO(OperatorForm operatorForm) {
        OperatorVO operatorVO = new OperatorVO();
        BeanUtils.copyProperties(operatorForm, operatorVO);
        return operatorVO;

    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPwd() {
        return loginPwd;
    }

    public void setLoginPwd(String loginPwd) {
        this.loginPwd = loginPwd;
    }

    public String getLoginPwdConfirm() {
        return loginPwdConfirm;
    }

    public void setLoginPwdConfirm(String loginPwdConfirm) {
        this.loginPwdConfirm = loginPwdConfirm;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

	public String getPayPwd() {
		return payPwd;
	}

	public void setPayPwd(String payPwd) {
		this.payPwd = payPwd;
	}

	public String getPayPwdConfirm() {
		return payPwdConfirm;
	}

	public void setPayPwdConfirm(String payPwdConfirm) {
		this.payPwdConfirm = payPwdConfirm;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public OperatorForm() {

    }

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public OperatorForm(String memberId, String operatorId, String nickName, String loginName, String loginPwd,
			String loginPwdConfirm, String payPwd, String payPwdConfirm, String status, String remark) {
		super();
		this.memberId = memberId;
		this.operatorId = operatorId;
		this.nickName = nickName;
		this.loginName = loginName;
		this.loginPwd = loginPwd;
		this.loginPwdConfirm = loginPwdConfirm;
		this.payPwd = payPwd;
		this.payPwdConfirm = payPwdConfirm;
		this.status = status;
		this.remark = remark;
	}

    
}
