/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.convert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.netfinworks.cert.service.model.AuthFileInfo;
import com.netfinworks.cert.service.request.AuthRequest;
import com.netfinworks.cert.service.request.QueryAuthRequest;
import com.netfinworks.cert.service.request.VerifyRequest;
import com.netfinworks.site.domain.domain.info.FileInfo;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.ufs.client.ctx.InputStreamFileContext;

/**
 * 通用说明：
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 上午11:11:27
 *
 */
public class CertConvert {

    /**
     * 验证统一文件上传request
     *
     * @return
     * @throws IOException
     */
    public static InputStreamFileContext createFileFileContext(MultipartFile file, String fileName)
                                                                                                   throws IOException {
        byte[] buf = file.getBytes();
        InputStream is = new ByteArrayInputStream(buf);
        return new InputStreamFileContext(fileName, is, buf.length);
    }

    /**
     * 认证服务request
     *  memberId 会员Id    Y
        authType    认证类型    Y
        certType    证件类型    N
        authNo  认证号码    Y
        authName    认证名（姓名） N
        authFiles   认证图片集   Y
        operator    操作员 Y
        extension   扩展信息    N
     * @return
     * @throws IOException
     */
    public static AuthRequest createAuthRequest(AuthInfoRequest info) {
        AuthRequest request = new AuthRequest();
        request.setAuthName(info.getAuthName());
        request.setAuthNo(info.getAuthNo());
        request.setAuthType(info.getAuthType().getCode());
        request.setCertType(info.getCertType());
        request.setMemberId(info.getMemberId());
        request.setOperator(info.getOperator());
        request.setExtension(info.getExt());
        List<FileInfo> authFiles = info.getAuthFiles();
        if ((authFiles != null) && (authFiles.size() > 0)) {
            List<AuthFileInfo> list = new ArrayList<AuthFileInfo>();
            for (FileInfo fileInfo : authFiles) {
                AuthFileInfo file = new AuthFileInfo();
                file.setFileName(fileInfo.getFileName());
                file.setFilePath(fileInfo.getFilePath());
                file.setFileType(fileInfo.getFileType());
                list.add(file);
            }
            request.setAuthFiles(list);
        }
        return request;
    }

    public static QueryAuthRequest createCheckAuthRequest(AuthInfoRequest info) {
        QueryAuthRequest request = new QueryAuthRequest();
        request.setAuthType(info.getAuthType().getCode());
        request.setMemberId(info.getMemberId());
        request.setStartRow(1);
        request.setEndRow(1);
        request.setNeedDelete(false);
        request.setNeedQeryTotal(false);
        request.setNeedQueryAll(false);
        return request;
    }

	public static VerifyRequest createVerifyRequest(AuthInfoRequest info) {
		VerifyRequest request = new VerifyRequest();
		request.setOrderNoList(info.getOrderNoList());
		request.setResult(info.getResult());
		request.setIsChecked(info.isChecked());
		request.setOperator(info.getOperator());
		return request;
	}


}
