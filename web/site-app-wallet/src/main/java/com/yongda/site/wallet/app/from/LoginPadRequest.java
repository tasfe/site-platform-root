package com.yongda.site.wallet.app.from;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.constants.RegexRule;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * <br>  设置登录密码，支付密码请求
 * 作者： zhangweijie <br>
 * 日期： 2016/10/27-15:42<br>
 */
public class LoginPadRequest extends OperationEnvironment implements Serializable {

    private static final long serialVersionUID = -1L;

    @NotBlank(message = "登录密码不能为空")
    @Pattern(regexp = RegexRule.WALLET_LOGIN_PWD, message = "请设置正确的登录密码格式")
    private String login_Password;

    @NotBlank(message = "确认登录密码不能为空")
    @Pattern(regexp = RegexRule.WALLET_LOGIN_PWD, message = "请设置正确的确认登录密码格式")
    private String affirm_Login_Password;

    @NotBlank(message = "支付密码不能为空")
    @Pattern(regexp = RegexRule.WALLET_PAY_PWD, message = "请设置正确的支付密码格式")
    private String pay_Password;

    @NotBlank(message = "确认支付密码不能为空")
    @Pattern(regexp = RegexRule.WALLET_PAY_PWD, message = "请设置正确的确认支付密码格式")
    private String affirm_Pay_Password;

    @NotBlank(message = "手机号不能为空")
    private String login_Name;

    public String getLogin_Password() {
        return login_Password;
    }

    public void setLogin_Password(String login_Password) {
        this.login_Password = login_Password;
    }

    public String getAffirm_Login_Password() {
        return affirm_Login_Password;
    }

    public void setAffirm_Login_Password(String affirm_Login_Password) {
        this.affirm_Login_Password = affirm_Login_Password;
    }

    public String getPay_Password() {
        return pay_Password;
    }

    public void setPay_Password(String pay_Password) {
        this.pay_Password = pay_Password;
    }

    public String getAffirm_Pay_Password() {
        return affirm_Pay_Password;
    }

    public void setAffirm_Pay_Password(String affirm_Pay_Password) {
        this.affirm_Pay_Password = affirm_Pay_Password;
    }

    public String getLogin_Name() {
        return login_Name;
    }

    public void setLogin_Name(String login_Name) {
        this.login_Name = login_Name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
