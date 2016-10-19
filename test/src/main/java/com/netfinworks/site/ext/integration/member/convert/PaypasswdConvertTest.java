
package com.netfinworks.site.ext.integration.member.convert;

import org.junit.Test;

import com.netfinworks.site.domain.domain.request.PayPasswdRequest;


public class PaypasswdConvertTest {
    @Test
    public void createPayPwdCheckRequest() {
        PayPasswdRequest req = new PayPasswdRequest();
        req.setAccountId("200100200110000624859300001");
        req.setOperator("7000006277051");
        req.setPassword("weibopay");
        PaypasswdConvert.createPayPwdCheckRequest(req);
    }
}
