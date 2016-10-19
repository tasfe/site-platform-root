package com.netfinworks.site.ext.integration.voucher.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;

/**
 * <p>访问WebService的工具类</p>
 * @author leelun
 * @version $Id: WebServiceTool.java, v 0.1 2013-7-1 下午12:30:24 lilun Exp $
 */
public class WebServiceHelper {

    private static OperationEnvironment opEnv = new OperationEnvironment();

    public static OperationEnvironment buildOpEnv() {
        opEnv.setClientId("scf");
        opEnv.setClientIp(getHostIP());
        opEnv.setClientMac(getHostIP());
        return opEnv;
    }

    /**
     * 获得本机ip
     * @return
     */
    private static String getHostIP() {
        String machineIp = null;
        try {
            machineIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return StringUtil.defaultIfBlank(machineIp);
    }
}
