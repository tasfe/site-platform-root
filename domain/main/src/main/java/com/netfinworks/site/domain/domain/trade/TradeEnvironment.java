/**
 *
 */
package com.netfinworks.site.domain.domain.trade;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * <p>交易环境</p>
 * @author fjl
 * @version $Id: TradeEnvironment.java, v 0.1 2013-11-28 下午1:26:18 fjl Exp $
 */
public class TradeEnvironment extends OperationEnvironment {

    /**
     *
     */
    private static final long   serialVersionUID = 1L;

    /** 请求浏览器 */
    private String              browser;
    /** 浏览器版本 */
    private String              browserVersion;
    /** sessionId*/
    private String              sessionId;
    /** 成功展示页面,充值与提现传给收银台*/
    private String              successDispalyUrl;

    private String              referUrl;

    private String              domainName;

    private String              cookie;

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSuccessDispalyUrl() {
        return successDispalyUrl;
    }

    public void setSuccessDispalyUrl(String successDispalyUrl) {
        this.successDispalyUrl = successDispalyUrl;
    }

    public String getReferUrl() {
        return referUrl;
    }

    public void setReferUrl(String referUrl) {
        this.referUrl = referUrl;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
