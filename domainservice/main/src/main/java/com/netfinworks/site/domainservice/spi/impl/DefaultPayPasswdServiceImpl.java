/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.spi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.FileInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.member.PayPasswdService;

/**
 * 通用说明：会员查询接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-11 下午4:08:27
 *
 */
@Service("defaultPayPasswdService")
public class DefaultPayPasswdServiceImpl implements DefaultPayPasswdService {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Resource(name = "payPasswdService")
    private PayPasswdService payPasswdService;

    /**
     * 设置支付密码
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public CommResponse setPayPassword(PayPasswdRequest req) throws ServiceException {
        try {
            return payPasswdService.setPayPassword(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public boolean resetPayPasswordLock(PayPasswdRequest req) throws ServiceException {
        try {
            return payPasswdService.resetPayPasswordLock(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 验证支付密码锁定
     * @param env
     * @param request
     * @return boolean
     */
    @Override
    public boolean isPayPwdClocked(PayPasswdRequest req) throws ServiceException {
        try {
            req.setValidateMode(2);
            return payPasswdService.validatePayPwd(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 验证支付密码是否设置
     * @param env
     * @param request
     * @return BankAccountResponse
     *
     */
    @Override
    public boolean isSetPayPwd(PayPasswdRequest req) throws ServiceException {
        try {
            req.setValidateMode(1);
            return payPasswdService.validatePayPwd(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 验证加盐支付密码
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public PayPasswdCheck checkPayPwdToSalt(PayPasswdRequest req) throws ServiceException {
        try {
            return payPasswdService.checkPayPwdToSalt(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 找回支付密码
     */

    @Override
    public String payBackPassWord(String front, String back, AuthInfoRequest authRequest)
                                                                                     throws ServiceException {
        String retn="";
        try {
            /**
             * 验证文件，封装成上传参数(身份证正面)，上传至统一文件系统
             */
//            String frontFileName = FileUploader.createFileName(authRequest.getMemberId(), front.getOriginalFilename());
//            UsfUploadFileResponse responseFront = payPasswdService.saveFile(front,frontFileName);
            /**
             * 验证文件，封装成上传参数(身份证背面)，上传至统一文件系统
             */
//            String backFileName = FileUploader.createFileName(authRequest.getMemberId(), back.getOriginalFilename());
//            UsfUploadFileResponse responseBack = payPasswdService.saveFile(back, backFileName);

            List<FileInfo> authFiles = new ArrayList<FileInfo>();
            FileInfo frontA = createAuthFile(front.substring(front.lastIndexOf("/")+1,front.length()), front,
                front.substring(front.lastIndexOf(".")+1,front.length()));
            FileInfo backA = createAuthFile(back.substring(back.lastIndexOf("/")+1,back.length()), back,
                back.substring(back.lastIndexOf(".")+1,back.length()));
            authFiles.add(frontA);
            authFiles.add(backA);
            authRequest.setAuthFiles(authFiles);
            retn=payPasswdService.payBackPassWord(authRequest);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
        return retn;
    }

    private FileInfo createAuthFile(String fileName, String filePath, String fileType) {
        FileInfo file = new FileInfo();
        file.setFileName(fileName);
        file.setFilePath(filePath);
        file.setFileType(fileType);
        return file;
    }

    @Override
    public boolean sendEmail(String mailAddress,String tplId,Map<String,Object> objParams) throws ServiceException {
        try {
            return payPasswdService.sendEmail(mailAddress, tplId, objParams);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

	@Override
	public boolean sendMobileMsg(String phoneNum, String tplId, Map<String, Object> objParams) throws ServiceException {
		try {
			return payPasswdService.sendMobileMsg(phoneNum, tplId, objParams);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}


}
