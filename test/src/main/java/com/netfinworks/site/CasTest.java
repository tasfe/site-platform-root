package com.netfinworks.site;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 *
 * <p>测试快捷通cas信息获取</p>
 * @author qinde
 * @version $Id: CasTest.java, v 0.1 2013-12-4 下午12:24:34 qinde Exp $
 */
public class CasTest {
    private static final Logger LOG                             = LoggerFactory
                                                                    .getLogger(CasTest.class);
    private static final String PARAM_PROXY_GRANTING_TICKET_IOU = "pgtIou";
    private static final String PARAM_PROXY_GRANTING_TICKET     = "pgtId";
    private static String       casServerUrlPrefix              = "https://passport.jia.com/cas/";

    private static String       serverName                      = "http://service.jia.com";
    private static String       casServerLoginUrl;
    private static String       appId                           = "400";

    static {
        ClassLoader classLoader = CasTest.class.getClassLoader();
        URL resUrl = classLoader.getResource("cas.properties");
        if (resUrl == null) {

        }
        try {
            InputStream in = resUrl.openStream();
            Properties prop = new Properties();
            prop.load(in);
            String filePath = prop.getProperty("filePath").trim();
            if ((filePath != null) && (!("".equals(filePath)))) {
                FileInputStream is = new FileInputStream(filePath);
                prop = new Properties();
                prop.load(is);
            }

            casServerUrlPrefix = prop.getProperty("casServerUrlPrefix").trim();
            if ((casServerUrlPrefix != null) && (!(casServerUrlPrefix.endsWith("/")))) {
                casServerUrlPrefix += "/";
            }

            serverName = prop.getProperty("serverName").trim();
            if ((serverName != null) && (serverName.endsWith("/"))) {
                serverName = serverName.substring(0, serverName.length() - 1);
            }
            appId = prop.getProperty("appId");
            casServerLoginUrl = casServerUrlPrefix + "login";
        } catch (Exception e) {
            LOG.error("cas.properties read error:", e);
        }
    }

    @Test
    public void shwoInfo() {
        Assert.notNull(casServerUrlPrefix);
        Assert.notNull(serverName);
        Assert.notNull(appId);
        System.out.println("casServerUrlPrefix=="+casServerUrlPrefix
            +",serverName="+serverName +",appId="+appId);
    }
}
