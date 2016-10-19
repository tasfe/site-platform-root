/**
 *
 */
package com.netfinworks.site.ext.integration;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * <p>客户端调用环境</p>
 * @author fjl
 * @version $Id: ClientEnvironment.java, v 0.1 2013-11-15 下午2:43:11 fjl Exp $
 */
public class ClientEnvironment {

    public static OperationEnvironment getEnv(String clientIp) {
        OperationEnvironment env = new OperationEnvironment();
        env.setClientId("site");
        env.setClientIp(clientIp);
        return env;
    }

}
