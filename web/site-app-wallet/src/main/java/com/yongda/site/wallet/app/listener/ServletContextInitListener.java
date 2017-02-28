/**
 *
 */
package com.yongda.site.wallet.app.listener;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import com.yongda.site.wallet.app.WebDynamicResource;


/**
 * @title
 * @description
 * @usage
 * @company		上海微汇信息技术有限公司
 * @author		TQSUMMER
 * @create		2012-8-13 下午7:00:17
 */
/**
 * @author TQSUMMER
 */
public class ServletContextInitListener implements ApplicationContextAware {

    private WebDynamicResource webResource;

	public void setWebResource(WebDynamicResource webResource) {
        this.webResource = webResource;
    }

    @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		WebApplicationContext wc = (WebApplicationContext) applicationContext;
		ServletContext sc = (ServletContext) wc.getBean(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME);
		sc.setAttribute("WEB_RESOURCE", webResource);
	}

}
