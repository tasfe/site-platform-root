package com.netfinworks.site.web.action.withdrawal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.ext.integration.member.BankAccountService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.WithDrawForm;
import com.netfinworks.site.web.util.LogUtil;

/**
 *
 * <p>企业会员提现action</p>
 * @author Guan Xiaoxu
 * @version $Id: WithdrawalAction.java, v 0.1 2014-1-7 下午4:53:49 Guanxiaoxu Exp $
 */
@Controller
public class WithdrawalAction extends BaseAction {

    @Resource(name = "defaultAccountService")
    private DefaultAccountService     defaultAccountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name = "defaultWithdrawService")
    private DefaultWithdrawService    defaultWithdrawService;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService   defaultPayPasswdService;

    @Resource(name = "auditService")
    private AuditServiceImpl       auditService;

    @Resource
    private BankAccountService bankAccountService;

    /**
     * 提现首页
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/withdraw.htm")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse resp,
                              TradeEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();

        //支付密码提示错误信息
        errorMap.putAll(passwordErrorInfo(request));

        String error = request.getParameter("error");
        if ("nofinish".equals(error)) {
            errorMap.put("finish_bank_info", "finish_bank_info");
        }
        if ("amountNot".equals(error)) {
            errorMap.put("amountNot_right", "amount_not_right");
        }
        EnterpriseMember user = getUser(request);
        checkUser(user, errorMap, restP);

        if (MemberLockStatus.LOCKED.equals(user.getLockStatus())) {
            restP.setSuccess(false);
            restP.setMessage("用户锁定，无法提现");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/withdrawal/withdrawal_result",
                "response", restP);
        }
        
        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),env);
        
        //余额<=0无法提现
        if (account!=null && !account.getWithdrawBalance().greaterThan(new Money(0))) {
            restP.setSuccess(false);
            restP.setMessage("账户余额等于零,无法提现");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/withdrawal/withdrawal_result",
                "response", restP);
        }
        //获取提现统一凭证号
        TradeRequestInfo tradeReqest = defaultWithdrawService.applyWithdraw(user, TradeType.WITHDRAW,  env);
        //保存原始凭证信息
        super.setJsonAttribute(request.getSession(), CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER, tradeReqest);
        //查询绑定银行卡
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
        //String cert = defaultPkiService.getCertification();
        //String salt = defaultPkiService.getSalt();
        //request.getSession().setAttribute(CACHE_NAMESPACE_PAY_PASSWORD_SALT, salt);
        //进入提现页面
        data.put("banks", list);
        data.put("banksize", list != null?list.size():0);
        restP.setData(data);
        data.put("email", user.getEmail());
        data.put("memberName", user.getMemberName());
        data.put("loginName", user.getOperator_login_name());
        //data.put(_base64_cert, cert);
        //data.put("salt", salt);
        data.put("accountBalance", account.getWithdrawBalance());
        data.put("moneyNum", request.getParameter("moneyNum"));
        data.put("member", user);
        restP.setErrors(errorMap);
        restP.setData(data);
        request.getSession().removeAttribute("mcrypt_key");//清空session中随机因子
        return new ModelAndView(CommonConstant.URL_PREFIX + "/withdrawal/withdrawal", "response",
            restP);
    }

    /**
     * 提现处理
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/doWithdraw.htm")
    public ModelAndView cach(HttpServletRequest request,
                             @Valid WithDrawForm form, BindingResult result, ModelMap model, OperationEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, String> errorMap = new HashMap<String, String>();
        Map<String, Object> data = initOcx();
        EnterpriseMember user = getUser(request);
        checkUser(user, errorMap, restP);
        
        logger.info(LogUtil.generateMsg(OperateTypeEnum.WITHDRAW_APPLY, user, env, ""));
        
        String bankcardId = form.getBankcardId();
        BankAccountInfo payeeBankAcctInfo = null;
        //查询绑定银行卡,判断银行卡有无联行号
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        List<BankAccountInfo> bankInfoList = defaultBankAccountService.queryBankAccount(req);
        for (BankAccountInfo bankAccountInfo : bankInfoList) {
            if (bankcardId.equals(bankAccountInfo.getBankcardId())) {
                if (null == bankAccountInfo.getBranchNo()
                    || "".equals(bankAccountInfo.getBranchNo())) {
                    restP.setSuccess(false);
                    restP.setMessage("请完善所选银行卡信息！");
                    return new ModelAndView("redirect:/my/withdraw.htm?error=nofinish&&moneyNum="
                                            + form.getMoneyNum(), "response", restP);
                }else{
                    payeeBankAcctInfo = bankAccountInfo;
                }
            }
        }

        if (result.hasErrors()) {
            List<ObjectError> list = result.getAllErrors();
            Map<String, String> errors = initError(list);
            restP.setErrors(errors);
            data.put("account", user.getLoginName() + "(" + user.getMobile() + ")");
            restP.setData(data);
            return new ModelAndView("redirect:/my/withdraw.htm?error=amountNot&&moneyNum="
                                    + form.getMoneyNum(), "response", restP);
        }
        
        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),env);
        
        //提现金额大于提现转账余额
        if (account!=null && new Money(form.getMoneyNum()).compareTo(account.getWithdrawBalance())==1) {
            restP.setSuccess(false);
            restP.setMessage("提现金额大于账户可用余额");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/withdrawal/withdrawal_result",
                "response", restP);
        }
        
        //支付密码解密
        String password = decrpPassword(request, request.getParameter("password"));

        PayPasswdRequest payPasswdRequest = new PayPasswdRequest();
        payPasswdRequest.setAccountId(user.getDefaultAccountId());
        //payPasswdRequest.setOperator(user.getCurrentOperatorId());
        //TODO当前用户的操作员没有登录密码，用admin的，以后再改回来
        payPasswdRequest.setOperator(user.getOperatorId());
        payPasswdRequest.setPassword(password);
        payPasswdRequest.setValidateType(2);

        /*if (defaultPayPasswdService.isPayPwdClocked(payPasswdRequest)) {
            return new ModelAndView("redirect:/my/withdraw.htm?error=locked&&moneyNum="
                                    + form.getMoneyNum());
        }
*/
        PayPasswdCheck pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswdRequest);
        if (!pcheck.isSuccess()) {
            if(pcheck.isLocked()) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                        DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
                model.put("error", ERROR_LOCKED);
            } else {
                model.put("error", ERROR_NOTRIGHT);
            }
            model.put("remainNum", pcheck.getRemainNum());
            model.put("moneyNum", form.getMoneyNum());
            model.put("bankCard", form.getBankcardId());
            return new ModelAndView("redirect:/my/withdraw.htm");
        }
        //获取原始凭证
        TradeRequestInfo tradeReqest = getJsonAttribute(request.getSession(), CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER, TradeRequestInfo.class);
        tradeReqest.setAmount(new Money(form.getMoneyNum()));
        PartyInfo payer = tradeReqest.getPayer();
        if(payer!=null){
            payer.setAccountName(user.getLoginName());
            payer.setEnterpriseName(user.getEnterpriseName());
        }
        tradeReqest.setPayer(payer);

        /*落地审核信息 */
        Audit audit = new Audit();
        audit.setTranVoucherNo(tradeReqest.getTradeVoucherNo());
        audit.setAuditType("withdrawal");//提现:withdrawal
        audit.setAmount(new Money(form.getMoneyNum()));
        audit.setMemberId(user.getMemberId());
        audit.setOperatorName(user.getOperator_login_name());
        audit.setOperatorId(user.getCurrentOperatorId());
        audit.setStatus("1");//1.待审核
        audit.setGmtCreated(new Date());
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("tradeReqest", tradeReqest);//付款信息作为tradeReqest的属性
        dataMap.put("bankCardId", bankcardId);
        dataMap.put("payeeBankAcctInfo", payeeBankAcctInfo);
        audit.setAuditData(JSONObject.toJSONString(dataMap));
        auditService.addAudit(audit);
        restP.setSuccess(true);
        request.getSession().removeAttribute("mcrypt_key");//清空session中随机因子
        return new ModelAndView(CommonConstant.URL_PREFIX + "/withdrawal/withdrawal_result",
            "response", restP);
    }

}
