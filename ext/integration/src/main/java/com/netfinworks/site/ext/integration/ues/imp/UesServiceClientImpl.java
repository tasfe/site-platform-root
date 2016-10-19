/**
 *
 */
package com.netfinworks.site.ext.integration.ues.imp;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.ues.UesClient;
import com.netfinworks.ues.crypto.model.EncryptType;
import com.netfinworks.ues.ctx.EncryptContext;
import com.netfinworks.ues.ctx.params.EchoSummary;

/**
 * <p>调用UES</p>
 * @author fjl
 * @version $Id: RemoteUesServiceFacade.java 21711 2012-10-25 08:13:29Z fangjilue $
 */
@Service("uesService")
public class UesServiceClientImpl implements UesServiceClient {
    private Logger    logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "uesClient")
    private UesClient uesClient;

    @Override
    public String encryptData(String src) throws BizException {

        if (StringUtil.isEmpty(src)) {
            return src;
        }

        EncryptData srcData = new EncryptData();
        srcData.setPlaintext(src);

        return encryptData(srcData);
    }

    @Override
    public String encryptData(EncryptData srcData) throws BizException {
        if (srcData == null || StringUtil.isEmpty(srcData.getPlaintext())) {
            return null;
        }

        try {
            //(1)构建原文
            EncryptContext ctx = new EncryptContext(srcData.getPlaintext());
            ctx.asEncryptType(EncryptType.RSA);
            ctx.withDigest();
            if (StringUtil.isNotBlank(srcData.getSummary())) {
                ctx.withSummariable(new EchoSummary(StringUtil.trim(srcData.getSummary())));
            }
            //远程调用计时开始
            long beginTime = System.currentTimeMillis();

            //(2)提交数据
            boolean rest = uesClient.saveData(ctx);
            if (!rest) {
                logger.error("ues invoke error code : {} , message : {}",
                    new Object[] { ctx.getResultCode(), ctx.getResultMessage() });
                throw new RuntimeException("ues 加密失败");
            }

            //远程调用计时结束
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                //log远程调用耗时和响应
                logger.info("远程调用：{} 耗时: {} (ms)", new Object[] { "uesServiceClient#saveData",
                        consumeTime });
            }
            return ctx.getTicket();
        } catch (Exception e) {
            logger.error("调用ues 加密出错", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDataByTicket(String ticket) throws BizException {
        if (StringUtil.isEmpty(ticket)) {
            return null;
        }
        EncryptContext context = new EncryptContext().useTicket(ticket.trim());
        //远程调用计时开始
        long beginTime = System.currentTimeMillis();
        try {
            boolean rest = uesClient.getDataByTicket(context);
            if (!rest) {
                logger.error("ues invoke error code : {} , message : {}",
                    new Object[] { context.getResultCode(), context.getResultMessage() });
            }

            if (logger.isInfoEnabled()) {
                //远程调用计时结束
                long consumeTime = System.currentTimeMillis() - beginTime;
                //log远程调用耗时和响应
                logger.info("远程调用：{} 耗时: {} (ms)", new Object[] {
                        "uesServiceClient#getDataByTicket", consumeTime });
            }
            return context.getPlainData();
        } catch (Exception e) {
            logger.error("调用ues 解密出错", e);
            throw new RuntimeException(e);
        }
    }

    public void setUesClient(UesClient uesClient) {
        this.uesClient = uesClient;
    }

}
