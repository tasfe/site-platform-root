/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.impl;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.netfinworks.cert.service.facade.ICertFacade;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.cert.service.request.AuthRequest;
import com.netfinworks.cert.service.request.QueryAuthRequest;
import com.netfinworks.cert.service.request.VerifyRequest;
import com.netfinworks.cert.service.response.AuthResponse;
import com.netfinworks.cert.service.response.BaseResponse;
import com.netfinworks.cert.service.response.QueryPageResponse;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.response.UsfUploadFileResponse;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.member.CertService;
import com.netfinworks.site.ext.integration.member.convert.CertConvert;
import com.netfinworks.ufs.client.UFSClient;
import com.netfinworks.ufs.client.ctx.InputStreamFileContext;
import com.netfinworks.ufs.client.exception.CallFailException;

/**
 * 通用说明：认证查询接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:54:27
 *
 */
@Service("certService")
public class CertServiceImpl extends ClientEnvironment implements CertService {

    private Logger      logger = LoggerFactory.getLogger(CertServiceImpl.class);

    @Resource(name = "certFacade")
    private ICertFacade certFacade;

    @Resource(name = "ufsClient")
    private UFSClient   ufsClient;

    /**
     * 发送实名认证信息
     * @param env
     * @param request
     * @return String
     */
    @Override
    public String auth(AuthInfoRequest info) throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("查询银行卡信息，请求参数：{}", info);
            beginTime = System.currentTimeMillis();
        }
        AuthRequest request = CertConvert.createAuthRequest(info);
        AuthResponse response = certFacade.authApply(request, getEnv(info.getClientIp()));
        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("远程查询银行卡信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
        }
        if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
            return response.getOrderNo();
        } else {
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 查询认证信息
     *
     * @param info
     * @return
     * @throws BizException
     */
    @Override
    public AuthInfo queryRealName(AuthInfoRequest info) throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("查询认证信息，请求参数：{}", info);
            beginTime = System.currentTimeMillis();
        }
        QueryAuthRequest paramQueryAuthRequest = CertConvert.createCheckAuthRequest(info);
        QueryPageResponse response = certFacade.queryList(paramQueryAuthRequest,
            getEnv(info.getClientIp()));
        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("远程查询认证信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
        }
        if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
            List<AuthInfo> list = response.getAuthList();
            if ((list != null) && (list.size() > 0)) {
                return list.get(0);
            }
			return new AuthInfo();
        } else {
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }

    }

    /**
     * 同一文件系统保存文件
     * @param env
     * @param request
     * @return String
     */
    @Override
    public UsfUploadFileResponse saveFile(MultipartFile front, String frontFileName)
                                                                                    throws BizException {
        try {
            InputStreamFileContext ctx = CertConvert.createFileFileContext(front, frontFileName);
            boolean success = ufsClient.putFile(ctx);
            String url = ctx.getDownloadUrl();
            return new UsfUploadFileResponse(success, url);
        } catch (CallFailException e) {
            logger.error(e.getMessage(), e.getCause(), e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        } catch (IOException e) {
            logger.error(e.getMessage(), e.getCause(),e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        } catch (Exception e) {
            logger.error("上传文件{}失败", frontFileName);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }
    /**
     * 查询认证状态
     *
     * @param info
     * @return
     * @throws BizException
     */

    @Override
    public AuthResultStatus queryAuthType(AuthInfoRequest info) throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("查询认证信息，请求参数：{}", info);
            beginTime = System.currentTimeMillis();
        }
        AuthResultStatus status = AuthResultStatus.NOT_FOUND;
        QueryAuthRequest paramQueryAuthRequest = CertConvert.createCheckAuthRequest(info);
        QueryPageResponse response = certFacade.queryList(paramQueryAuthRequest,
            getEnv(info.getClientIp()));
        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("远程查询认证信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
        }
        if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
            List<AuthInfo> list = response.getAuthList();
            if ((list != null) && (list.size() > 0)) {
                AuthInfo auth = list.get(0);
                if(auth.getResult() != null){
                    status = AuthResultStatus.getByCode(auth.getResult().getCode());
                }
            }
			if (status == AuthResultStatus.CHECK_PASS) {
				paramQueryAuthRequest.setAuthType(AuthType.BANK_CARD.getCode());
				response = certFacade.queryList(paramQueryAuthRequest, getEnv(info.getClientIp()));
				if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
					List<AuthInfo> list2 = response.getAuthList();
					if ((list2 != null) && (list2.size() > 0)) {
						AuthInfo auth = list2.get(0);
						if ((auth != null) && (auth.getResult() == ResultStatus.CHECK_PASS)) {
							status = AuthResultStatus.PASS;
						} else if ((auth != null) && (auth.getResult() == ResultStatus.CHECK_REJECT)) {
							status = AuthResultStatus.CHECK_REJECT;
						}
					}
				}

			}
			return status;
        } else {
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

	/**
	 * 更改实名认证状态
	 * 
	 * @param info
	 * @return
	 * @throws BizException
	 */

	@Override
	public BaseResponse verify(AuthInfoRequest info) throws BizException {
		long beginTime = 0L;
		if (logger.isInfoEnabled()) {
			logger.info("更改实名认证状态，请求参数：{}", info);
			beginTime = System.currentTimeMillis();
		}
		VerifyRequest request = CertConvert.createVerifyRequest(info);
		BaseResponse response = certFacade.verify(request, getEnv(info.getClientIp()));
		if (logger.isInfoEnabled()) {
			long consumeTime = System.currentTimeMillis() - beginTime;
			logger.info("远程更改实名认证状态， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
		}
		return response;
	}

}
