/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.convert;

import com.netfinworks.ma.service.base.model.BankAcctInfo;
import com.netfinworks.ma.service.request.BankAccInfoRequest;
import com.netfinworks.ma.service.request.BankAccRemoveRequest;
import com.netfinworks.ma.service.request.BankAccountRequest;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;

/**
 *
 * <p>银行卡</p>
 * @author qinde
 * @version $Id: BankConvertConvert.java, v 0.1 2013-11-29 下午1:11:02 qinde Exp $
 */
public class BankConvert {

    /**
     * 生成解除银行卡绑定request
     *
     * @param req
     * @return
     */
    public static BankAccRemoveRequest createBankAccRemoveRequest(BankAccRequest req) {
        BankAccRemoveRequest request = new BankAccRemoveRequest();
        request.setBankcardId(req.getBankcardId());
        request.setMemberId(req.getMemberId());
        return request;
    }

    /**
     * 生成解添加银行卡绑定request
     *
     * @param req
     * @return
     */
    public static BankAccInfoRequest createBankAccInfoRequest(BankAccRequest req) {
        BankAccInfoRequest request = new BankAccInfoRequest();
        BankAcctInfo bankInfo = new BankAcctInfo();
        bankInfo.setMemberId(req.getMemberId());
        bankInfo.setBankCode(req.getBankCode());
        bankInfo.setBankName(req.getBankName());
        bankInfo.setCardAttribute(req.getCardAttribute());
        bankInfo.setPayAttribute(req.getPayAttribute());
        bankInfo.setBankAccountNum(req.getBankAccountNum());
        bankInfo.setRealName(req.getRealName());
        bankInfo.setBankBranch(req.getBranchName());
        bankInfo.setProvince(req.getProvName());
        bankInfo.setCity(req.getCityName());
        bankInfo.setBranchNo(req.getBranchNo());
        bankInfo.setCardType(req.getCardType());
		bankInfo.setIsVerified(req.getIsVerified());
        request.setBankInfo(bankInfo);
        bankInfo.setMobileNum(req.getMobile());
        return request;
    }
    /**
     * 更新银行卡绑定request
     *
     * @param req
     * @return
     */
    public static BankAccInfoRequest updateBankAccInfoRequest(BankAccRequest req) {
        BankAccInfoRequest request = new BankAccInfoRequest();
        BankAcctInfo bankInfo = new BankAcctInfo();
        bankInfo.setMemberId(req.getMemberId());
        bankInfo.setCardAttribute(req.getCardAttribute());
        bankInfo.setPayAttribute(req.getPayAttribute());
        bankInfo.setBankBranch(req.getBranchName());
        bankInfo.setProvince(req.getProvName());
        bankInfo.setCity(req.getCityName());
        bankInfo.setBranchNo(req.getBranchNo());
        bankInfo.setCardType(req.getCardType());
        bankInfo.setBankcardId(req.getBankcardId());
        request.setBankInfo(bankInfo);
        return request;
    }

    /**
     * 生成查询银行卡绑定request
     *
     * @param req
     * @return
     */
    public static BankAccountRequest createBankAccountRequest(BankAccRequest bankInfo) {
        BankAccountRequest request = new BankAccountRequest();
        request.setMemberId(bankInfo.getMemberId());
        request.setBankAccountNum(bankInfo.getBankAccountNum());
        request.setPayAttribute(bankInfo.getPayAttribute());
        request.setStatus(bankInfo.getStatus());
        return request;
    }
}
