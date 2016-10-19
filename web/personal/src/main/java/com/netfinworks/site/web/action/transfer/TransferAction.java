/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.transfer;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.form.RechargeForm;
import com.netfinworks.site.web.util.TradeReqestUtil;

/**
 * 通用说明：钱包个人会员转账
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午10:34:25
 *
 */
@Controller
public class TransferAction extends BaseAction {

    @Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;

    @Resource(name = "defaultTransferService")
    private DefaultTransferServiceImpl defaultTransferService;

    @Resource(name = "webResource")
    private WebDynamicResource         webResource;

    @Resource(name = "defaultAccountService")
    private DefaultAccountService     defaultAccountService;
    
    /**
     * 进入转账首页
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/transfer.htm")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse resp, boolean error,
                              TradeEnvironment env,ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> errorMap = null;
        PersonMember user = getUser(request);
        checkUser(user, errorMap, restP);
        
        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),env);
        
        data.put("account", user.getMobile());
        data.put("member", user);
        data.put("accountMask", user.getMobileStar());
        data.put("accountBalance", account.getAvailableBalance());
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.TRANSFER_INDEX.getUrl(),
            "response", restP);
    }

    /**
     * 转账付款--进入收银台
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/transfer-confirm.htm")
    public ModelAndView confirm(HttpServletRequest request, @Valid RechargeForm form,
                                BindingResult result, TradeEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        PersonMember user = getUser(request);
        if (result.hasErrors()) {
            Map<String, String> errors = initError(result.getAllErrors());
            throw new ServiceException(JSONObject.toJSONString(errors));
        }
        MemberAccount account = null;
        try {

            if(StringUtils.isBlank(form.getIdentityNo())) throw new IllegalAccessException("转账对方的手机/邮箱不能为空");
            //查询收款人
            String identityNo = form.getIdentityNo();
            String platformType = form.getPlatformType();
            //查询余额
            account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),env);
            
            BaseMember payeeUser = defaultMemberService.isMemberExists(identityNo, platformType, env);
            //收款人未实名认证，不允许转账
            /* 2014-06-06 暂时忽略认证状态
            if (payeeUser.getNameVerifyStatus() != AuthResultStatus.PASS) {
                logger.info("会员{},{}未实名认证,不允许转账，认证状态是{}", payeeUser.getMemberId(),
                    payeeUser.getMemberName(), payeeUser.getNameVerifyStatus().getCode());
                data.put("account", user.getMobile());
                data.put("member", user);
                data.put("accountMask", user.getMobileStar());
                data.put("accountBalance", user.getAccount().getWithdrawBalance());
                // data.put("mobile", form.getMobile());
                data.put("identityNo", form.getIdentityNo());
                data.put("transferNum", form.getTransferNum());
                data.put("transferInfo", form.getTransferInfo());
                restP.setData(data);
                restP.setSuccess(false);
                restP.setMessage("transfer_account_not_verify");
                return new ModelAndView(CommonConstant.URL_PREFIX
                                        + ResourceInfo.TRANSFER_INDEX.getUrl(), "response", restP);
            }
            */
            //生成收款方
            PartyInfo payee = TradeReqestUtil.createPay(payeeUser.getDefaultAccountId(),
                payeeUser.getMemberId(), payeeUser.getOperatorId(), "",
                payeeUser.getMemberName(), payeeUser.getMemberType());
            //生成付款方
            PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
                user.getMemberId(), user.getOperatorId(), "", user.getMemberName(),
                user.getMemberType());

            //生成交易请求
            TradeRequestInfo tradeReqest = TradeReqestUtil.createTransferTradeRequest(payer, payee,
                new Money(form.getTransferNum()), form.getTransferInfo(), new Money(),
                form.getMsn(),TradeType.TRANSFER);
            env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");
            //封装客户端信息
            BeanUtils.copyProperties(tradeReqest, env);
            //获取收银台地址
            String url = defaultTransferService.transfer(tradeReqest);
            //跳转到收银台
            return new ModelAndView("redirect:" + url);
        } catch (MemberNotExistException e) {
            data.put("account", user.getMobile());
            data.put("member", user);
            data.put("accountMask", user.getMobileStar());
            data.put("accountBalance", account.getAvailableBalance());
            // data.put("mobile", form.getMobile());
            data.put("identityNo", form.getIdentityNo());
            data.put("transferNum", form.getTransferNum());
            data.put("transferInfo", form.getTransferInfo());
            restP.setData(data);
            restP.setSuccess(false);
            restP.setMessage("transfer_account_not_exist");
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.TRANSFER_INDEX.getUrl(), "response", restP);
        }

    }

}
