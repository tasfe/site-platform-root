package com.netfinworks.site.web.action.withdraw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.form.WithDrawForm;

/**
 *
 * <p>钱包个人会员提现action</p>
 * @author qinde
 * @version $Id: WithdrawAction.java, v 0.1 2013-12-2 下午6:34:18 qinde Exp $
 */
@Controller
public class WithdrawAction extends BaseAction {

    @Resource(name = "defaultAccountService")
    private DefaultAccountService     defaultAccountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name = "defaultWithdrawService")
    private DefaultWithdrawService    defaultWithdrawService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService   defaultPayPasswdService;

    /**
     * 提现首页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/withdraw.htm", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request, HttpServletResponse resp,
                              TradeEnvironment env, ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();
        String error = request.getParameter("error");
        String remainNum = request.getParameter("remainNum");
        if (error != null) {
            if (ERROR_LOCKED.equals(error)) {
                errorMap.put("error_passwd_is_locked", "error_passwd_is_locked");
            } else {
                errorMap.put("error_passwd_not_right", "error_passwd_not_right");
            }
            errorMap.put("remainNum", remainNum);
        }
        PersonMember user = getUser(request);
        
        if (MemberLockStatus.LOCKED.equals(user.getLockStatus())) {
            restP.setSuccess(false);
            restP.setMessage("用户锁定，无法提现");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/withdrawal/withdrawal_result",
                "response", restP);
        }
        
        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),
            env);
        //余额<=0无法提现
        if (account!=null && new Money("0").compareTo(account.getWithdrawBalance())==0) {
            restP.setSuccess(false);
            restP.setMessage("账户余额为零,无法提现");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/withdraw/withdraw_result",
                "response", restP);
        }
        
        //获取提现统一凭证号
        TradeRequestInfo tradeReqest = defaultWithdrawService.applyWithdraw(user, TradeType.WITHDRAW, env);
        //保存原始凭证信息
        super.setJsonAttribute(request.getSession(), CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER, tradeReqest);
        //查询绑定银行卡
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
        for (BankAccountInfo card : list) {
            card.setBankAccountNumMask(StarUtil.mockBankCardByBlank(card.getBankAccountNumMask()));
            card.setRealName(StarUtil.mockRealName(card.getRealName()));
        }
        //String cert = defaultPkiService.getCertification();
        //String salt = defaultPkiService.getSalt();
        //request.getSession().setAttribute(CACHE_NAMESPACE_PAY_PASSWORD_SALT, salt);
        //进入提现页面
        data.put("banks", list);
        model.put("cashNum", request.getParameter("cashNum"));
        String bankCardId = request.getParameter("bankCard");
        if (StringUtils.isBlank(bankCardId)) {
            bankCardId = "";
        }
        model.put("bankCardId", bankCardId);
        restP.setData(data);
        //data.put(_base64_cert, cert);
        //data.put("salt", salt);
        data.put("mobile", user.getMobileStar());
        data.put("member", user);
        data.put("memberName", user.getMemberName());
        data.put("accountBalance", account.getWithdrawBalance());
        restP.setErrors(errorMap);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/withdraw/withdraw", "response", restP);
    }

    /**
     * 提现处理
     *
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/cach-success.htm")
    public ModelAndView cash(HttpServletRequest request, @Valid WithDrawForm form,
                             BindingResult result,OperationEnvironment env, ModelMap model) throws Exception {
        //未判断重复提交
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        PersonMember user = getUser(request);
        data.put("member", user);
        if (result.hasErrors()) {
            List<ObjectError> list = result.getAllErrors();
            Map<String, String> errors = initError(list);
            restP.setErrors(errors);
            data.put("account", user.getPlateName() + "(" + user.getMobile() + ")");
            restP.setData(data);
            return new ModelAndView("redirect:withdraw.htm", "response", restP);
        }
        
        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),env);
        
        //提现金额大于提现转账余额
        if (account!=null && new Money(form.getMoneyNum()).compareTo(account.getWithdrawBalance())==1) {
            restP.setSuccess(false);
            restP.setMessage("提现金额大于账户可用余额");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/withdraw/withdraw_result",
                "response", restP);
        }
        
        String paypasswd = decrpPassword(request, form.getPasswd());
        if (paypasswd == null) {
            model.put("error", ERROR_NOTRIGHT);
            return new ModelAndView("redirect:/my/withdraw.htm");
        }

        PayPasswdRequest payPasswdRequest = new PayPasswdRequest();
        payPasswdRequest.setAccountId(user.getDefaultAccountId());
        payPasswdRequest.setOperator(user.getOperatorId());
        payPasswdRequest.setPassword(paypasswd);
        payPasswdRequest.setValidateType(2);

        PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswdRequest);
        if (!checkResult.isSuccess()) {
            if (checkResult.isLocked()) {
                model.put("error", ERROR_LOCKED);
            } else {
                model.put("error", ERROR_NOTRIGHT);
            }
            model.put("remainNum", checkResult.getRemainNum());
            model.put("cashNum", form.getMoneyNum());
            model.put("bankCard", form.getBankcardId());
            return new ModelAndView("redirect:/my/withdraw.htm");
        }
        //获取原始凭证
        TradeRequestInfo tradeReqest = super.getJsonAttribute(request.getSession(), CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER, TradeRequestInfo.class);
        if (tradeReqest == null) {
            restP.setData(data);
            restP.setMessage("提现订单已提交过，请勿重复提交！");
            restP.setSuccess(false);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/withdraw/withdraw_result",
                "response", restP);
        }
        request.getSession().removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER);
        tradeReqest.setAmount(new Money(form.getMoneyNum()));
        //请求提现
        boolean flag = defaultWithdrawService.submitApply(tradeReqest, form.getBankcardId());
        restP.setData(data);
        restP.setSuccess(flag);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/withdraw/withdraw-success",
            "response", restP);
    }
}
