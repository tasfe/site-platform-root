package com.netfinworks.site.web.action.transfer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.TransferForm;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.site.web.util.TradeReqestUtil;

/**
 *
 * <p>转账</p>
 * @author Guan Xiaoxu
 * @version $Id: TransferAction.java, v 0.1 2013-11-27 上午11:00:48 Guanxiaoxu Exp $
 */
@Controller
public class TransferAction extends BaseAction {
    @Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;

    @Resource(name = "defaultTransferService")
    private DefaultTransferServiceImpl defaultTransferService;

    @Resource(name="webResource")
    private WebDynamicResource webResource;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService   defaultPayPasswdService;

    @Resource(name = "auditService")
    private AuditServiceImpl       auditService;

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
    public ModelAndView goTransfer(HttpServletRequest request,HttpServletResponse resp,
                                   TradeEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();
        
        data.put("identityNo",request.getParameter("identityNo"));
        data.put("transferNum",request.getParameter("transferNum"));
        data.put("transferInfo",request.getParameter("transferInfo"));
        data.put("msn",request.getParameter("msn"));
        
        //支付密码提示错误信息
        errorMap.putAll(passwordErrorInfo(request));
        
        String error = request.getParameter("error");

        if ("amountNot".equals(error)) {
            errorMap.put("amountNot_right", "amount_not_right");
        }

        EnterpriseMember user = getUser(request);
        checkUser(user, errorMap, restP);

        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),env);
        
        //余额<=0无法转账
        if (account!=null && new Money(0).compareTo(account.getAvailableBalance())==0) {
            restP.setSuccess(false);
            restP.setMessage("账户余额为零,无法转账");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/transfer/transfer_result",
                "response", restP);
        }
        
        if (MemberLockStatus.LOCKED.equals(user.getLockStatus())) {
            restP.setSuccess(false);
            restP.setMessage("用户锁定，无法转账");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/transfer/transfer_result",
                "response", restP);
        }

        data.put("member", user);
        data.put("account", user.getMemberName());
        data.put("member", user);
        data.put("accountBalance", account.getAvailableBalance());
        restP.setData(data);
        restP.setErrors(errorMap);
        return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.TRANSFER_INDEX.getUrl(),
            "response", restP);
    }

    /**
     * 转账付款到个人
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/doTransferToPerson.htm")
    public ModelAndView confirm(HttpServletRequest request, @Valid TransferForm form,
                                BindingResult result, TradeEnvironment env,ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        String transferInfo = request.getParameter("transferInfo");
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();
        EnterpriseMember user = getUser(request);
        checkUser(user, errorMap, restP);
        
        Money transferNum =  new Money(form.getTransferNum());
        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),env);
        //转账金额大于账户余额
        if (account!=null && account.getAvailableBalance().compareTo(transferNum)==-1) {
            restP.setSuccess(false);
            restP.setMessage("转账金额大于账户余额");
            return new ModelAndView(CommonConstant.URL_PREFIX + "/transfer/transfer_result",
                "response", restP);
        }
        
        data.put("account", user.getMemberName());
        data.put("loginName", user.getOperator_login_name());
        data.put("accountBalance", account.getAvailableBalance());
        data.put("identityNo", form.getIdentityNo());
        data.put("transferNum", transferNum);
        data.put("transferInfo", form.getTransferInfo());
        restP.setData(data);
        
        if (result.hasErrors()) {
            Map<String, String> errors = initError(result.getAllErrors());
            restP.setErrors(errors);

            logger.info("转账表单校验失败!");
            restP.setSuccess(false);
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.TRANSFER_INDEX.getUrl(), "response", restP);
        } 
        
        try {
            //支付密码解密
            String password = decrpPassword(request, request.getParameter("password"));

            PayPasswdRequest payPasswdRequest = new PayPasswdRequest();
            payPasswdRequest.setAccountId(user.getDefaultAccountId());
            //payPasswdRequest.setOperator(user.getCurrentOperatorId());
            //TODO当前用户的操作员没有登录密码，用admin的，以后再改回来
            payPasswdRequest.setOperator(user.getOperatorId());
            payPasswdRequest.setPassword(password);
            payPasswdRequest.setValidateType(2);

            PayPasswdCheck pcheck = defaultPayPasswdService.checkPayPwdToSalt(payPasswdRequest);
            if (!pcheck.isSuccess()) {
                if(pcheck.isLocked()) {
                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
                    model.put("error", ERROR_LOCKED);
                } else {
                    model.put("error", ERROR_NOTRIGHT);
                }
                model.put("identityNo", form.getIdentityNo());
                model.put("transferNum", form.getTransferNum());
                model.put("transferInfo", form.getTransferInfo());
                model.put("msn", form.getMsn());
                model.put("remainNum", pcheck.getRemainNum());
                
                return new ModelAndView("redirect:/my/transfer.htm",model);
            }

            
            String identityNo = form.getIdentityNo();
            String platformType = form.getPlatformType();

            //查询收款人
            BaseMember payeeUser = defaultMemberService.isMemberExists(identityNo, platformType, env);
            //收款人未实名认证，不允许转账
            /*if (AuthResultStatus.PASS != payeeUser.getNameVerifyStatus()) {
                logger.info("转账操作,收款人未实名认证");
                restP.setData(data);
                restP.setSuccess(false);
                restP.setMessage("transfer_account_not_verify");
                return new ModelAndView(CommonConstant.URL_PREFIX
                                        + ResourceInfo.TRANSFER_INDEX.getUrl(), "response",
                    restP);
            }*/

            //生成收款方
            logger.info("转账,生成收款方");
            PartyInfo payee = new PartyInfo();
            payee.setAccountId(payeeUser.getDefaultAccountId());
            payee.setMemberId(payeeUser.getMemberId());
            payee.setOperatorId(payeeUser.getCurrentOperatorId());
            payee.setIdentityNo(identityNo);
            payee.setMemberName(payeeUser.getMemberName());
            payee.setMemberType(payeeUser.getMemberType());

            //生成付款方
            PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
                user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
                user.getMemberName(),user.getMemberType());
            payer.setAccountName(user.getLoginName());
            payer.setEnterpriseName(user.getEnterpriseName());
            logger.info("转账,生成付款方");

            //生成交易请求
            TradeRequestInfo tradeReqest = TradeReqestUtil.createTransferTradeRequest(payer, payee,
                transferNum, form.getTransferInfo(), new Money(),form.getMsn());
            env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");

            logger.info("转账,生成交易请求");
            //封装客户端信息
            BeanUtils.copyProperties(tradeReqest, env);
            //落地凭证
            //String url = defaultTransferService.transfer(tradeReqest);
            //logger.info("转账获取收银台url:"+url);

            Map<String,Object> dataMap = new HashMap<String,Object>();
            dataMap.put("tradeReqest", tradeReqest);
            dataMap.put("transferInfo", transferInfo);

            /*落地审核信息 */
            Audit audit = new Audit();
            //audit.setTranVoucherNo(tradeReqest.getTradeVoucherNo());
            audit.setAuditType("transfer");//2.transfer
            audit.setAmount(transferNum);
            audit.setMemberId(user.getMemberId());
            audit.setOperatorName(user.getOperator_login_name());
            audit.setOperatorId(user.getCurrentOperatorId());
            audit.setStatus("1");//1.待审核
            audit.setGmtCreated(new Date());
            audit.setAuditData(JSONObject.toJSONString(dataMap));
            auditService.addAudit(audit);

            //跳转到收银台
            //return new ModelAndView("redirect:" + url);
            restP.setSuccess(true);
            request.getSession().removeAttribute("mcrypt_key");//清空session中随机因子
            return new ModelAndView(CommonConstant.URL_PREFIX + "/transfer/transfer_result",
                "response", restP);
        } catch (MemberNotExistException e) {
            logger.info("转账,会员不存在");
            data.put("member", user);
            restP.setData(data);
            restP.setSuccess(false);
            restP.setMessage("transfer_account_not_exist");
            return new ModelAndView(CommonConstant.URL_PREFIX
                                    + ResourceInfo.TRANSFER_INDEX.getUrl(), "response", restP);
        }
    }
    /**
     * 转账付款到企业--进入收银台
     *
     * @param model
     * @param request
     * @return
     */
//    @RequestMapping(value = "/my/transferCompSubmit.htm", method = RequestMethod.POST)
//    public ModelAndView confirmToComp(HttpServletRequest request, @Valid RechargeForm form,
//                                BindingResult result, TradeEnvironment env) throws Exception {
//        RestResponse restP = new RestResponse();
//        Map<String, Object> data = new HashMap<String, Object>();
//        Map<String, String> errorMap = new HashMap<String, String>();
//        PersonMember user = getUser(request);
//        checkUser(user, errorMap, restP);
//        if (result.hasErrors()) {
//            Map<String, String> errors = initError(result.getAllErrors());
//            restP.setErrors(errors);
//            data.put("account", user.getMemberName());
//            data.put("accountBalance", user.getAccount().getAvailableBalance());
//            data.put("mobile", form.getMobile());
//            data.put("transferNum", form.getTransferNum());
//            data.put("transferInfo", form.getTransferInfo());
//            restP.setData(data);
//            restP.setSuccess(false);
//            return new ModelAndView(CommonConstant.URL_PREFIX
//                                    + ResourceInfo.TRANSFER_INDEX.getUrl(), "response", restP);
//        } else {
//            try {
//                MemberRequest req = new MemberRequest();
//                req.setClientIp(request.getRemoteAddr());
//                req.setMobile(form.getMobile());
//                PersonMember userPayee = new PersonMember();
//                //邮箱
////                userPayee.setMobile(form.getMobile());
//                userPayee.setEmail(form.getCompEmail());
//                //查询收款人
//                PersonMember payeeUser = defaultMemberService.isMemberIntegratedExist(userPayee,
//                    req);
//                if(payeeUser.getNameVerifyStatus() != AuthResultStatus.PASS) {
//                    data.put("account", user.getMemberName());
//                    data.put("accountBalance", user.getAccount().getAvailableBalance());
//                    data.put("mobile", form.getMobile());
//                    data.put("transferNum", form.getTransferNum());
//                    data.put("transferInfo", form.getTransferInfo());
//                    restP.setData(data);
//                    restP.setSuccess(false);
//                    return new ModelAndView(CommonConstant.URL_PREFIX
//                                            + ResourceInfo.TRANSFER_INDEX.getUrl(), "response", restP);
//                }
//                //生成收款方
//                PartyInfo payee = TradeReqestUtil.createPay(payeeUser.getDefaultAccountId(),
//                    payeeUser.getMemberId(), payeeUser.getOperatorId(), payeeUser.getMobile(),
//                    payeeUser.getMemberName(),payeeUser.getMemberType());
//                //生成付款方
//                PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
//                    user.getMemberId(), user.getOperatorId(), user.getMobile(),
//                    user.getMemberName(),user.getEmail(),user.getMemberType());
//
//                //生成交易请求
//                TradeRequestInfo tradeReqest = TradeReqestUtil.createTransferTradeRequest(payer, payee,
//                    new Money(form.getTransferNum()), form.getTransferInfo(), new Money(),form.getMsn());
//                env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");
//                //封装客户端信息
//                BeanUtils.copyProperties(tradeReqest, env);
//                //获取收银台地址
//                String url = defaultTransferService.transfer(tradeReqest);
//                //跳转到收银台
//                return new ModelAndView("redirect:" + url);
//            } catch (MemberNotExistException e) {
//                data.put("account", user.getMemberName());
//                data.put("accountBalance", user.getAccount().getAvailableBalance());
//                data.put("mobile", form.getMobile());
//                data.put("transferNum", form.getTransferNum());
//                data.put("transferInfo", form.getTransferInfo());
//                data.put("email", form.getCompEmail());
//                restP.setData(data);
//                restP.setSuccess(false);
//                restP.setMessage("transfer_account_not_exist");
//                return new ModelAndView(CommonConstant.URL_PREFIX
//                                        + ResourceInfo.TRANSFER_INDEX.getUrl(), "response", restP);
//            }
//        }
//    }

    /**
     * 转账成功或失败
     * 接受收银台回调
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/transferResult.htm", method = RequestMethod.GET)
    public ModelAndView doTransfer(HttpServletRequest request, TradeEnvironment env)
                                                                                    throws Exception {
        RestResponse restP = new RestResponse();
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/transfer/transfer_success",
            "response", restP);
    }

}
