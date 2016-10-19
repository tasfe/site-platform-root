package com.netfinworks.site.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.netfinworks.site.core.common.KaptchaProducer;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.web.action.common.BaseAction;

// 生成验证码
@Controller
public class KaptchaImageAction extends BaseAction {
    protected static final Logger logger = LoggerFactory.getLogger(KaptchaImageAction.class);

    @Autowired
    private KaptchaProducer       kaptchaProducer;

    @RequestMapping("/pvc.htm")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
        // return a jpeg
        response.setContentType("image/jpeg");
        // create the text for the image
        kaptchaProducer.createRandomCode(request, response);
        logger.info("从seesion里取登录验证码：{}",
                request.getSession().getAttribute(KaptchaProducer.KAPTCHA_KEY));
    }

    public static boolean validateRandCode(HttpServletRequest request, String randCode)
            throws Exception {
        logger.info("kaptcha validate:"
                + request.getSession().getAttribute(KaptchaProducer.KAPTCHA_KEY));

        String realCode = (String) request.getSession().getAttribute(KaptchaProducer.KAPTCHA_KEY);
        if ((randCode == null) || !randCode.equalsIgnoreCase(realCode)) {
            request.getSession().setAttribute(KaptchaProducer.KAPTCHA_KEY,
                    "" + RadomUtil.createRadom());
            return false;
        }
        return true;
    }
}
