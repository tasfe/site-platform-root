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
import com.netfinworks.ma.service.request.PayPasswordRequest;
import com.netfinworks.ma.service.request.PayPwdCheckRequest;
import com.netfinworks.ma.service.request.PayPwdLockRequest;
import com.netfinworks.ma.service.request.ValidatePayPwdRequest;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.info.FileInfo;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.ues.util.UesUtils;
import com.netfinworks.ufs.client.ctx.InputStreamFileContext;

/**
 *
 * <p>支付密码convert</p>
 * @author qinde
 * @version $Id: PaypasswdConvert.java, v 0.1 2013-11-29 下午9:23:23 qinde Exp $
 */
public class PaypasswdConvert {

    /**
     * 创建设置支付密码的request
     * @return
     */
    public static PayPasswordRequest createPayPasswordRequest(PayPasswdRequest req) {
        String newPasswd = UesUtils.hashSignContent(req.getPassword());
        PayPasswordRequest request = new PayPasswordRequest();
        request.setOperatorId(req.getOperator());// 操作员编号
        request.setAccountId(req.getAccountId());//账户编号
        request.setPaypwd(newPasswd);// 支付密码: SHA-256(密码明文）
        return request;
    }

    /**
     * 创建查询交易密码状态的request
     *
     * @return
     */
    public static ValidatePayPwdRequest createValidatePayPwdRequest(PayPasswdRequest req) {
        ValidatePayPwdRequest request = new ValidatePayPwdRequest();
        request.setAccountId(req.getAccountId());
        request.setOperatorId(req.getOperator());
        request.setValidateMode(req.getValidateMode());
        return request;
    }

    /**
     * 验证加盐支付密码request
     *
     * @return
     */
    public static PayPwdCheckRequest createPayPwdCheckRequest(PayPasswdRequest req) {
        String passwd = null;
        if(req.getValidateType() == 1) {
            passwd = req.getOldPassword();
        } else {
            passwd = req.getPassword();
        }
        long salt = RadomUtil.createRadom();
        String saltStr = String.valueOf(salt);
        String saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(passwd)
                                                     + saltStr);
        PayPwdCheckRequest request = new PayPwdCheckRequest();
        request.setAccountId(req.getAccountId());
        request.setOperatorId(req.getOperator());
        request.setPaypwd(saltPasswd);
        request.setSalt(saltStr);
        return request;
    }
    /**
     * 密码解锁request
     *
     * @return
     */
    public static PayPwdLockRequest createPayPwdLockRequest(PayPasswdRequest req) {
        PayPwdLockRequest request = new PayPwdLockRequest();
        request.setAccountId(req.getAccountId());
        request.setOperatorId(req.getOperator());
        return request;
    }

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
     * 找回支付密码的 AuthRequest
     * @param memberId 会员Id Y
     * @param authType 认证类型 Y
     * @param operator 操作员   Y
     * @param authName 认证名（姓名） N
     * @param certType 证件类型  N
     * @param authFiles 认证图片集   Y
     * @return
     */
    public static AuthRequest createPayBackPassWordRequest(AuthInfoRequest info) {
        AuthRequest request = new AuthRequest();

        request.setAuthName(info.getAuthName());
        request.setAuthType(info.getAuthType().getCode());
        request.setCertType(info.getCertType());
        request.setMemberId(info.getMemberId());
        request.setOperator(info.getOperator());
        List<FileInfo> authFiles = info.getAuthFiles();
        if (authFiles != null && authFiles.size() > 0) {
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
        request.setExtension(info.getMessage());
        return request;
    }

}
