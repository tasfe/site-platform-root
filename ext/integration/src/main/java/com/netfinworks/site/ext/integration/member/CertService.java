/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member;

import org.springframework.web.multipart.MultipartFile;

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.response.BaseResponse;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.response.UsfUploadFileResponse;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 通用说明：认证远程调用接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:53:51
 *
 */
public interface CertService {
    /**
     * 发送实名认证信息
     * @param env
     * @param request
     * @return String
     */
    public String auth(AuthInfoRequest info) throws BizException;

    /**
     * 查询实名认证信息
     *
     * @param info
     * @return
     * @throws BizException
     */
    public AuthInfo queryRealName(AuthInfoRequest info) throws BizException;

    /**
     * 同一文件系统保存文件
     * @param env
     * @param request
     * @return String
     */
    public UsfUploadFileResponse saveFile(MultipartFile front, String frontFileName) throws BizException;
    /**
     * 查询认证状态
     *
     * @param info
     * @return
     * @throws BizException
     */
    public AuthResultStatus queryAuthType(AuthInfoRequest info) throws BizException;

	/**
	 * 更改实名认证状态
	 * 
	 * @param info
	 * @return
	 * @throws BizException
	 */
	public BaseResponse verify(AuthInfoRequest info) throws BizException;
}
