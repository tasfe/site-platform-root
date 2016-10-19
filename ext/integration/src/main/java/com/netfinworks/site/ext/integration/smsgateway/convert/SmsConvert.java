package com.netfinworks.site.ext.integration.smsgateway.convert;

import com.netfinworks.common.util.MD5Builder;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.smsgateway.service.facade.domain.enums.AuthTypeKind;
import com.netfinworks.smsgateway.service.facade.domain.enums.ServiceKind;
import com.netfinworks.smsgateway.service.facade.domain.request.SendAuthCodeRequest;
import com.netfinworks.smsgateway.service.facade.domain.request.VerifyMobileAuthCodeRequest;

public class SmsConvert {
    /**
     * 生成发送短信request
     *
     * @param req
     * @return
     */
    public static SendAuthCodeRequest createSendAuthCodeRequest(AuthCodeRequest req) {
        SendAuthCodeRequest request = new SendAuthCodeRequest();
        String requestId = req.getMemberId() + System.currentTimeMillis();//请求代码
        request.setRequestId(requestId);
        request.setMobile(req.getMobile());
        request.setMobileTicket(req.getMobileTicket());
        request.setBizType(req.getBizType());
        request.setBizId(req.getBizId());
        request.setSign(MD5Builder.getMD5(req.getMobile() + req.getMemberId() + req.getBizType()));
        request.setSignType("NON");
        request.setAuthType(AuthTypeKind.REPEATEDLY);
        request.setValidity(req.getValidity());
        request.setService(ServiceKind.send_auth_code.getCode());
        return request;
    }

    /**
     * 验证手机号码
     * @param memberId
     * @param mobile
     * @param authCode
     * @return
     */
    public static VerifyMobileAuthCodeRequest createVerifyMobileAuthCodeRequest(AuthCodeRequest req) {
        VerifyMobileAuthCodeRequest request = new VerifyMobileAuthCodeRequest();
        String requestId = req.getMemberId() + System.currentTimeMillis();//请求代码
        request.setRequestId(requestId);
        request.setBizType(req.getBizType());
        request.setBizId(req.getBizId());
        request.setMobileTicket(req.getMobileTicket());
        request.setAuthCode(req.getAuthCode());//用户输入的验证码
        if (null != req.getMemberId()) {
        	request.setSign(MD5Builder.getMD5(req.getMobile() + req.getMemberId() + req.getBizType()));
        } else {
        	request.setSign(MD5Builder.getMD5(req.getMobile() + req.getBizType()));
        }
        request.setSignType("NON");
        request.setService(ServiceKind.verify_mobile_authCode.getCode());
        return request;
    }
}
