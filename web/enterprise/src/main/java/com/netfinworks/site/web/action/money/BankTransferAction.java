package com.netfinworks.site.web.action.money;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.batchservice.facade.enums.ProductType;
import com.netfinworks.batchservice.facade.model.BankInfo;
import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.batchservice.facade.model.BatchPayDetail;
import com.netfinworks.batchservice.facade.model.MemberInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.fos.service.facade.enums.CompanyOrPersonal;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.mns.client.domain.PageInfo;
import com.netfinworks.payment.common.v2.enums.PartyRole;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.BankType;
import com.netfinworks.site.domain.enums.CardAttr;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.BankTransferForm;
import com.netfinworks.site.web.action.form.TransferBankBatchForm;
import com.netfinworks.site.web.common.constant.AuditStatus;
import com.netfinworks.site.web.common.constant.AuditSubType;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.ErrorMsg;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.common.constant.MemoType;
import com.netfinworks.site.web.common.util.ObjectUtils;
import com.netfinworks.site.web.common.vo.BankTransfer;
import com.netfinworks.site.web.common.vo.SessionPage;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.site.web.util.TradeReqestUtil;
import com.netfinworks.voucher.service.facade.domain.enums.VoucherInfoType;

/**
 * 永达互联网金融/银行转账
 * @author xuwei
 * @date 2014年7月8日
 */
@Controller
@RequestMapping("bTransfer")
public class BankTransferAction extends BaseAction {
    @Resource(name = "defaultAccountService")
    private DefaultAccountService      defaultAccountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService  defaultBankAccountService;

    @Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade          payPartyFeeFacade;

    @Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService    defaultPayPasswdService;

    @Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;

    @Resource(name = "auditService")
    private AuditServiceImpl           auditService;

    @Resource(name = "defaultWithdrawService")
    private DefaultWithdrawService     defaultWithdrawService;

    @Resource(name = "defaultSmsService")
    private DefaultSmsService          defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService          defaultUesService;

    @Resource(name = "defaultTransferService")
    private DefaultTransferServiceImpl defaultTransferService;

    @Resource(name = "fundsControlService")
    private FundsControlService        fundsControlService;

    @Resource(name = "memberService")
    private MemberService              memberService;

    @Resource(name = "voucherService")
    private VoucherService             voucherService;
    
    /** 转账到卡sheet名称 */
    private static String              TRANSFER_TO_BANK_SHEET_NAME = "Sheet1";
    
    /**
     * 进入银行转账页面
     * @return
     */
    @RequestMapping("/toTransferBank")
    public ModelAndView toTransferBank(HttpServletRequest request,
            HttpServletResponse resp, TradeEnvironment env) {
        ModelAndView mv = new ModelAndView();
        EnterpriseMember user = this.getUser(request);
        
        // 实名认证
        if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
            logger.error("登陆账户未进行实名认证");
            mv.addObject("message", "对不起，您的账户尚未实名认证，无法进行银行卡转账!");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        MemberAccount account;
        try {
            account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
            mv.addObject("avaBalance", account.getAvailableBalance());
        } catch (ServiceException e) {
            logger.error("查询账户信息失败", e);
        }
        
        mv.addObject("memberType", user.getMemberType().getCode());
        mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bank");
        return mv;
    }
    
    /**
     * 验证限额限次
     * @param request
     * @param cashingForm
     * @return
     */
    @ResponseBody
    @RequestMapping("/validateLflt")
    public RestResponse validateLflt(HttpServletRequest request, BankTransferForm form) {
        RestResponse response = new RestResponse();
        
        // 获取用户信息
        EnterpriseMember user = getUser(request);
        
        // 提现申请金额
        Money totalMoney = new Money(form.getTotalMoney());
        
        // 验证限额限次
        if (!super.validateLflt(user.getMemberId(), "", totalMoney, TradeType.PAY_TO_BANK, null, response, env)) {
            logger.error("限额限次验证失败");
            return response;
        }
        
        response.setSuccess(true);
        return response;
    }
    
    /**
     * 银行转账确认
     * @return
     */
    @RequestMapping("/toConfirmTransferBank")
    public ModelAndView toConfirmTransferBank(BankTransferForm form, HttpServletRequest request,
            HttpServletResponse resp, HttpSession session, TradeEnvironment env) {
        ModelAndView mv = initOcxView();
        EnterpriseMember user = this.getUser(request);
        // 软硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            mv.addObject("isCertActive", "yes");
        } else {
            mv.addObject("isCertActive", "no");
            if (StringUtils.isEmpty(user.getMobile())) {
                mv.addObject("message", "请先绑定数字证书或手机！");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        }
        
        // 校验提交参数
        String errorMsg = validator.validate(form);
        if (StringUtils.isNotEmpty(errorMsg)) {
            mv.addObject("message", errorMsg);
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        // 获得用户信息
        MemberAccount account = null;
        try {
            // 检查账户
            account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
            mv.addObject("user", user);
            mv.addObject("account", account);
            
            // 检查余额是否足够
            Money avaBalance = account.getAvailableBalance();
            Money totalMoney = new Money(form.getTotalMoney());
            
            // 验证限额限次
            if (!super.validateLflt(user.getMemberId(), "", totalMoney, TradeType.PAY_TO_BANK, mv, null, env)) {
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
            
            if (avaBalance.compareTo(totalMoney) == -1) {
                logger.error("账户[{}]余额不足", account.getAccountId());
                mv.addObject("message", "账户余额不足");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
            mv.addObject("avaBalance", avaBalance);
        } catch (Exception e) {
            logger.error("查询账户失败", e);
            mv.addObject("message", "无法获取到您的账户信息!");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        // 目标账户详细信息
        BankAcctDetailInfo bankAcctInfo = convertToAcctDetailInfo(form);
        
        if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
            // 选择其他时，获取用户输入的备注
            form.setRemark(MemoType.getDesc(form.getRemarkType()));
        }
        form.setOperLoginName(user.getOperator_login_name());
        form.setAvaBalance(account.getAvailableBalance().toString());
        form.setRecvAcctName(account.getAccountName());
        long token = RadomUtil.createRadom();
        this.setJsonAttribute(session, "bankTransferForm" + token, form);
        this.setJsonAttribute(session, "bankAcctInfo" + token, bankAcctInfo);
        
        // 将转账相关信息放入页面视图
        mv.addObject("bankAcctInfo", bankAcctInfo);
        form.setAccountNo(CommonUtils.getMaskData(form.getAccountNo()));
        mv.addObject("form", form);
        mv.addObject("token", token);
        
        try {
            //获取统一凭证号
            TradeRequestInfo tradeReqest = defaultWithdrawService.applyTransfer(user, TradeType.PAY_TO_BANK, env);
            tradeReqest.setMemo(form.getRemark());
            tradeReqest.setAmount(new Money(form.getTransMoney()));
            
            //保存原始凭证信息到session中
            setJsonAttribute(session, CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER + token, tradeReqest);
        } catch (BizException e) {
            logger.error("创建提现统一凭证号失败", e);
        }
        
        if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {
            // 有转账审核权限
            mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-confirm");
        } else {
            // 无转账审核权限
            mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-apply-confirm");
        }
        
        // 硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            mv.addObject("isCertActive", "yes");
        } else {
            mv.addObject("isCertActive", "no");
        }
        
        return mv;
    }
    
    /**
     * 确认直接转账
     * @param request
     * @param session
     * @param env
     * @return
     */
    @RequestMapping("/confirmTransferBank")
    public ModelAndView confirmTransferBank(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
        ModelAndView mv = new ModelAndView();
        
        
        // 获得用户信息
        EnterpriseMember user = this.getUser(request);
        logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_APPLY, user, env, StringUtils.EMPTY));
        
        String mobileCaptcha = request.getParameter("mobileCaptcha");
        if (StringUtils.isNotEmpty(mobileCaptcha)) {
            // 检查手机验证码
            if(!validateCaptcha(request, user, null, mv, env)) {
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        } else {
            // 验证硬证书签名
            String payPassword = request.getParameter("payPassword");
            String signedData = request.getParameter("signedData");
            try {
                if(!validateSignature(request, payPassword, signedData, null, mv, env)) {
                    mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                    return mv;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("验证证书时编码错误", e);
                mv.addObject("message", "您未插入快捷盾或证书已经过期！");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        }
        
        // 检查支付密码
        if(!validatePayPassword(request, user, null, mv)) {
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        String token = request.getParameter("token");
        // 从会话中获取到转账信息
        BankTransferForm form = this.getJsonAttribute(session, "bankTransferForm" + token, BankTransferForm.class);
        BankAcctDetailInfo bankAcctInfo = this.getJsonAttribute(session, "bankAcctInfo" + token, BankAcctDetailInfo.class);
        
        if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
            // 选择其他时，获取用户输入的备注
            form.setRemark(MemoType.getDesc(form.getRemarkType()));
        }
        
        try {
            transferToBankAccount(request, session, form, AuditStatus.AUDIT_PASSED.getCode(), bankAcctInfo, null, env, token);
            logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_EXAMINE, user, env, "通过"));

        } catch (Exception e) {
            logger.error("转账失败！", e);
            mv.addObject("success", false);
            mv.addObject("message", e.getMessage());
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            removeTransferSession(request, token);
            return mv;
        }
        
        form.setAccountNo(CommonUtils.getMaskData(form.getAccountNo()));
        mv.addObject("form", form);
        mv.addObject("bankAcctInfo", bankAcctInfo);
        mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-success");
        removeTransferSession(request, token);
        return mv;
    }
    
    /**
     * 确认批量转账
     * @param request
     * @param session
     * @param env
     * @return
     */
    @RequestMapping("/confirmBatchTransferBank")
    public ModelAndView confirmBatchTransferBank(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
        ModelAndView mv = new ModelAndView();
        
        // 获得用户信息
        EnterpriseMember user = this.getUser(request);
        
        String mobileCaptcha = request.getParameter("mobileCaptcha");
        if (StringUtils.isNotEmpty(mobileCaptcha)) {
            // 检查手机验证码
            if(!validateCaptcha(request, user, null, mv, env)) {
              mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
              return mv;
            }
        } else {
            // 验证硬证书签名
            String payPassword = request.getParameter("payPassword");
            String signedData = request.getParameter("signedData");
            try {
                if(!validateSignature(request, payPassword, signedData, null, mv, env)) {
                    mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                    return mv;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("验证证书时编码错误", e);
                mv.addObject("message", "您未插入快捷盾或证书已经过期！");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        }
        
        // 检查支付密码
        if(!validatePayPassword(request, user, null, mv)) {
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        String token = request.getParameter("token");
        // 从会话中获取转账信息
        BankTransferForm form = this.getJsonAttribute(session, "bankTransferForm" + token, BankTransferForm.class);
        List<BankTransfer> transferList = getTransferList(this.getJsonAttribute(session, "transferList" + token, List.class));
        Integer transferCount = this.getJsonAttribute(session, "transferCount" + token, Integer.class);
        String sourceBatchNo=this.getJsonAttribute(session, "sourceBatchNo" + token, String.class);
        // 提交转账
        try {
            this.submitBatchTransfer(request, transferList, form, user,sourceBatchNo, env);
        } catch (Exception e) {
        	logger.error("确认转账失败",e);
            mv.addObject("success", false);
            mv.addObject("message", e.getMessage());
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            removeTransferSession(request, token);
            return mv;
        }
        
        form.setMobile(user.getMobile());
        form.setTransferCount(String.valueOf(transferCount));
        mv.addObject("form", form);
        mv.addObject("transferList", transferList);
        mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-batch-success");
        removeTransferSession(request, token);
        return mv;
    }
    
    /**
     * 提交循环提交批量转账
     * @param request
     * @param transferList
     * @param form
     * @param env
     * @return 转账成功笔数
     */
    private void submitBatchTransfer(HttpServletRequest request, List<BankTransfer> transferList, BankTransferForm form, 
            EnterpriseMember user,String sourceBatchNo, TradeEnvironment env) throws Exception {
        if (ObjectUtils.isListEmpty(transferList)) {
            logger.debug("转账的目标账户为空");
            throw new IllegalArgumentException("转账的目标账户为空");
        }
        
        logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_APPLY_FILE, user, env,
                StringUtils.EMPTY));
        
        Money totalFee = new Money("0");

        List<BatchDetail> batchDetailList = new ArrayList<BatchDetail>();
        for (BankTransfer transfer : transferList) {
            BankAcctDetailInfo bankAcctInfo = new BankAcctDetailInfo();
            bankAcctInfo.setBankName(transfer.getBankName());
            bankAcctInfo.setBankAccountNum(transfer.getAccountNoMask());
            bankAcctInfo.setRealName(transfer.getName());
            bankAcctInfo.setBankBranch(transfer.getBranchName());
            bankAcctInfo.setProvince(transfer.getProvName());
            bankAcctInfo.setCity(transfer.getCityName());
            bankAcctInfo.setCardAttribute(transfer.getCardAttribute());
            
            // 生成批量支付详细信息
            BatchDetail batchDetail = this.generatePayDetail(transfer, request);
            batchDetailList.add(batchDetail);

            if (StringUtils.isNotEmpty(transfer.getMoney())) {
                String feeStr = super.getServiceFee(request, transfer.getMoney(),
                        TradeType.PAY_TO_BANK.getBizProductCode(), null);
                if (StringUtils.isNotEmpty(feeStr)) {
                    totalFee.addTo(new Money(feeStr));
                }
            }
        }

        // 生成批次号
        String batchVourchNo = null;
        try {
            batchVourchNo = voucherService.regist(VoucherInfoType.CONTROL.getCode());
            voucherService.record(batchVourchNo, VoucherInfoType.CONTROL.getCode(),
                    TradeType.PAY_TO_BANK.getBizProductCode(), user.getMemberId(), env);
        }
        catch (Exception e) {
            logger.error("获取凭证号失败！", e);
        }
        
        defaultTransferService.batchTransferSubmit(batchVourchNo,sourceBatchNo, ProductType.PAY, user, batchDetailList, form.getTransMoney());
        try {
            logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_EXAMINE, user, env, "通过"));
            // 保存批量审核信息
            this.saveBatchAuditInfo(request, batchVourchNo, form.getTransMoney(), user, 
                    AuditStatus.AUDIT_PASSED.getCode(), StringUtils.EMPTY, totalFee.toString(),sourceBatchNo);
        } catch (Exception e) {
            logger.error("保存批量转账到卡的审核信息失败", e);
            throw new Exception("保存批量转账到卡的审核信息失败！");
        }
    }
    
    /**
     * 生成付款详细信息
     * @param transfer 转账付款信息
     * @param request Http请求对象
     * @return 付款详细信息，作为服务层接口参数
     * @throws Exception 抛出生成过程中异常
     */
    private BatchDetail generatePayDetail(BankTransfer transfer, HttpServletRequest request) throws Exception {
        BatchPayDetail batchDetail = new BatchPayDetail();
        batchDetail.setTradeType(TradeType.PAY_TO_BANK.getPayCode());
        batchDetail.setSourceDetailNo(transfer.getSourceDetailNo());
        batchDetail.setAmount(new BigDecimal(transfer.getMoney()));
        batchDetail.setMemo(transfer.getRemark());
        batchDetail.setPartyInfos(generatePartyInfoList(transfer, super.getUser(request)));
     
        return batchDetail;
    }
    
    /**
     * 生成交易方信息列表
     * @param transfer 转账信息
     * @param user 登陆用户
     * @return 交易方信息列表
     * @throws Exception 
     */
    private List<com.netfinworks.batchservice.facade.model.PartyInfo> generatePartyInfoList(
            BankTransfer transfer, EnterpriseMember user) throws Exception {
        List<com.netfinworks.batchservice.facade.model.PartyInfo> partyList 
            = new ArrayList<com.netfinworks.batchservice.facade.model.PartyInfo>();

        BigDecimal amount = new BigDecimal(transfer.getMoney());
        
        // 付款方信息
        com.netfinworks.batchservice.facade.model.PartyInfo payerInfo 
            = new com.netfinworks.batchservice.facade.model.PartyInfo();
        payerInfo.setPartyRole(PartyRole.PAYER.getCode());
        payerInfo.setAmount(amount);
        payerInfo.setPaymentInfo(getPayerInfo(user));
        partyList.add(payerInfo);
        
        // 收款方信息
        com.netfinworks.batchservice.facade.model.PartyInfo payeeInfo 
            = new com.netfinworks.batchservice.facade.model.PartyInfo();
        payeeInfo.setPartyRole(PartyRole.PAYEE.getCode());
        payeeInfo.setAmount(amount);
        payeeInfo.setPaymentInfo(getBankPayeeInfo(transfer));
        partyList.add(payeeInfo);
        
        return partyList;
    }
    
    /**
     * 获取收款人信息
     * @param loginId 登陆账户名
     * @return 收款人信息
     */
    private BankInfo getBankPayeeInfo(BankTransfer transfer) throws Exception {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setName(transfer.getName());
        bankInfo.setAccountNo(defaultUesService.encryptData(transfer.getAccountNoMask()));
        BankType bankType = BankType.getByMsg(transfer.getBankName());
        bankInfo.setBankCode((bankType == null) ? StringUtils.EMPTY : bankType.getCode());
        bankInfo.setBankName(transfer.getBankName());
        bankInfo.setBranchName(transfer.getBranchName());
        bankInfo.setBankLineNo(StringUtils.EMPTY);//目前无法获取到
        bankInfo.setProvince(transfer.getProvName());
        bankInfo.setCity(transfer.getCityName());
        String companyOrPersonal = (CardAttr.PERSONAL.getCode() == transfer.getCardAttribute()) ? 
                CompanyOrPersonal.PERSONAL.getCode() : CompanyOrPersonal.COMPANY.getCode();
        bankInfo.setCompanyOrPersonal(companyOrPersonal);
        return bankInfo;
    }
    
    /**
     * 获取付款人信息
     * @param user 登陆用户
     * @return 付款人信息
     */
    private MemberInfo getPayerInfo(EnterpriseMember user) {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setPartyId(user.getMemberId());
        memberInfo.setPartyAccountNo(user.getDefaultAccountId());
        return memberInfo;
    }
    
    /**
     * 转账银行账户
     * @param request
     * @param session
     * @param form
     * @param bankAcctInfo
     * @return true-成功，false-失败
     * @throws Exception 
     */
    private void transferToBankAccount(HttpServletRequest request, HttpSession session, BankTransferForm form,
            String status, BankAcctDetailInfo bankAcctInfo, TradeRequestInfo tradeReq, TradeEnvironment env, String token) throws Exception {
        EnterpriseMember user = getUser(request);
        
        if (tradeReq == null) {
            tradeReq = super.getJsonAttribute(session, CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER + token, TradeRequestInfo.class);
            session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER + token);
        }
        
        tradeReq.setTradeType(TradeType.PAY_TO_BANK);
        
        // 提交审核
        if(!this.submitTransferAudit(request, bankAcctInfo, tradeReq.getAmount(), status, user, StringUtils.EMPTY, tradeReq, env)) {
            throw new RuntimeException("转账审核申请操作失败");
        }
        
        // 调用提现接口实现转账到银行
        defaultWithdrawService.submitBankTransfer(tradeReq, bankAcctInfo);
    }
    
    /**
     * 确认提交转账申请
     * @param request
     * @param session
     * @param env
     * @return
     */
    @RequestMapping("/confirmTransferBankApply")
    public ModelAndView confirmTransferBankApply(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
        ModelAndView mv = new ModelAndView();
        
        // 获得用户信息
        EnterpriseMember user = this.getUser(request);
        
        logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_APPLY, user, env, StringUtils.EMPTY));
        // 检查支付密码
        if(!validatePayPassword(request, user, null, mv)) {
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        String token = request.getParameter("token");
        // 从会话中获取到转账信息
        BankTransferForm form = this.getJsonAttribute(session, "bankTransferForm" + token, BankTransferForm.class);
        BankAcctDetailInfo bankAcctInfo = this.getJsonAttribute(session, "bankAcctInfo" + token, BankAcctDetailInfo.class);
        
        //获取原始凭证
        TradeRequestInfo tradeRequest = null;
        try {
            tradeRequest = this.getTradeRequest(request, user, form.getTransMoney(), token);
        } catch (Exception e) {
            logger.error("获取交易凭证失败");
            mv.addObject("message", "获取交易凭证失败");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        if (tradeRequest == null) {
            logger.error("提现订单已提交过，请勿重复提交！");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        tradeRequest.setMemo(form.getRemark());
        request.getSession().removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER + token);
        
        try {
            Money freezeAmount = null;
            if (StringUtils.isEmpty(form.getTotalMoney())) {
                throw new Exception("转账总金额不能为空");
            }
            freezeAmount = new Money(form.getTotalMoney());
            
            // 冻结金额
            String bizPaymentSeqNo = fundsControlService.freeze(user.getMemberId(), user.getDefaultAccountId(), 
                    freezeAmount, env);
            if (StringUtils.isEmpty(bizPaymentSeqNo)) {
                logger.error("资金冻结失败");
                throw new Exception("您的账户余额不足！");
            }
            
            // 提交转账审核申请
            if (!this.submitTransferAudit(request, bankAcctInfo, new Money(form.getTransMoney()), AuditStatus.AUDIT_WAITING.getCode(), 
                    user, bizPaymentSeqNo, tradeRequest, env)) {
                mv.addObject("success", false);
                mv.addObject("message", "转账审核申请操作失败");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        } catch (Exception e) {
            logger.error("无法提交转账申请", e);
            mv.addObject("message", e.getMessage());
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        form.setMobile(user.getMobile());
        mv.addObject("form", form);
        mv.addObject("bankAcctInfo", bankAcctInfo);
        
        mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-apply-success");
        return mv;
    }
    
    /**
     * 提交批量转账审核申请
     * @param request
     * @param session
     * @param env
     * @return
     */
    @RequestMapping("/confirmBatchTransferBankApply")
    public ModelAndView confirmBatchTransferBankApply(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
        ModelAndView mv = new ModelAndView();
        
        // 获得用户信息
        EnterpriseMember user = this.getUser(request);
        
        logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_APPLY_FILE, user, env,
                StringUtils.EMPTY));
        
        // 检查支付密码
        if(!validatePayPassword(request, user, null, mv)) {
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        String token = request.getParameter("token");
        // 从会话中获取转账信息
        BankTransferForm form = super.getJsonAttribute(session, "bankTransferForm" + token, BankTransferForm.class);
        List<BankTransfer> transferList = getTransferList(this.getJsonAttribute(session, "transferList" + token, List.class));
        Integer transferCount = super.getJsonAttribute(session, "transferCount" + token, Integer.class);
        String sourceBatchNo=this.getJsonAttribute(session, "sourceBatchNo" + token, String.class);
        try {
            List<BatchDetail> batchDetailList = new ArrayList<BatchDetail>();
            for (BankTransfer transfer : transferList) {
                // 生成批量支付详细信息
                BatchDetail batchDetail = this.generatePayDetail(transfer, request);
                batchDetailList.add(batchDetail);
                
            }
            
            this.submitBatchTransferAudit(request, form.getTransMoney(), AuditStatus.AUDIT_WAITING.getCode(), 
                    user, "", batchDetailList, env,sourceBatchNo);
        } catch (Exception e) {
            logger.error("批量转账失败，{}", e.getMessage(), e);
            mv.addObject("success", false);
            mv.addObject("message", e.getMessage());
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            removeTransferSession(request, token);
            return mv;
        }
        
        form.setTransferCount(String.valueOf(transferCount));
        form.setMobile(user.getMobile());
        mv.addObject("form", form);
        mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-batch-apply-success");
        removeTransferSession(request, token);
        return mv;
    }
    
    /**
     * 提交批量转账申请
     * @param request
     * @param form
     * @param bankAcctInfo
     * @param user
     * @return true-提交成功，false-失败
     * @throws Exception 
     */
    private boolean submitBatchTransferAudit(HttpServletRequest request, String amount, String status, 
            EnterpriseMember user, String freezeVourceNo, List<BatchDetail> batchDetailList, TradeEnvironment env,String sourceBatchNo) throws Exception {
        if (ObjectUtils.isListEmpty(batchDetailList)) {
            throw new Exception("批量转账信息不能为空！");
        }
        Money totalFee = new Money("0");
        for (BatchDetail batchDetail : batchDetailList) {
            if (StringUtils.isNotEmpty(batchDetail.getAmount().toString())) {
                String feeStr = super.getServiceFee(request, batchDetail.getAmount().toString(),
                        TradeType.PAY_TO_BANK.getBizProductCode(), null);
                if (StringUtils.isNotEmpty(feeStr)) {
                    totalFee.addTo(new Money(feeStr));
                }
            }
        }

        // 生成批次号
        String batchVourchNo = null;
        try {
            batchVourchNo = voucherService.regist(VoucherInfoType.CONTROL.getCode());
            voucherService.record(batchVourchNo, VoucherInfoType.CONTROL.getCode(), 
                    TradeType.PAY_TO_BANK.getBizProductCode(), user.getMemberId(), env);
        }
        catch (Exception e) {
            logger.error("获取批次号失败！", e);
        }
        
        try {
            defaultTransferService.batchTransferApply(batchVourchNo,sourceBatchNo, ProductType.PAY, user, batchDetailList, amount);
        } catch (Exception e) {
            logger.error("批量转账失败", e);
            throw new Exception(e.getMessage());
        }
        
        try {
            // 保存批量审核信息
            this.saveBatchAuditInfo(request, batchVourchNo, amount, user, AuditStatus.AUDIT_WAITING.getCode(),
                    freezeVourceNo, totalFee.toString(),sourceBatchNo);
        } catch (Exception e) {
            logger.error("保存批量转账到卡的审核信息失败", e);
            throw new Exception("保存批量转账到卡的审核信息失败！");
        }
        
        return true;
    }
    
    /**
     * 提交转账申请
     * @param request
     * @param form
     * @param bankAcctInfo
     * @param user
     * @return true-提交成功，false-失败
     */
    private boolean submitTransferAudit(HttpServletRequest request, BankAcctDetailInfo bankAcctInfo, Money amount, 
            String status, EnterpriseMember user, String freezeVourceNo, TradeRequestInfo tradeReq, TradeEnvironment env) {
        try {
            // 生成付款收款信息
            this.generatePayerInfo(tradeReq, user, bankAcctInfo, env);
            
            // 保存审核信息
            this.saveAuditInfo(request, tradeReq, amount, bankAcctInfo, user, status, freezeVourceNo);
        }
        catch (Exception e) {
            logger.error("生产付款信息或保存审核信息失败", e);
            return false;
        }
        
        return true;
    }
    
    /**
     * 生成收款付款信息
     * @param tradeReqest
     * @param user
     * @param memberId
     * @param env
     * @throws RuntimeException
     */
    private void generatePayerInfo(TradeRequestInfo tradeReqest, EnterpriseMember user, 
            BankAcctDetailInfo bankAcctInfo, OperationEnvironment env) throws RuntimeException {
        //生成收款方
        logger.info("转账,生成收款方");
        PartyInfo payee = new PartyInfo();
        payee.setAccountName(bankAcctInfo.getRealName());
        payee.setIdentityNo(bankAcctInfo.getBankAccountNumMask());

        //生成付款方
        PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
            user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
            user.getMemberName(),user.getMemberType());
        payer.setAccountName(user.getLoginName());
        payer.setEnterpriseName(user.getEnterpriseName());
        logger.info("转账,生成付款方");

        //生成交易请求
        logger.info("转账,生成交易请求");
        tradeReqest.setPayee(payee);
        tradeReqest.setPayer(payer);
    }
    
    /**
     * 查询银行转账的服务费用
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/queryServiceCharge", method = RequestMethod.POST)
    public RestResponse queryServiceCharge(HttpServletRequest request) {
        RestResponse response = new RestResponse();
        String money = request.getParameter("money");
        logger.debug("money={}", money);
        
        getServiceFee(request, money, TradeType.PAY_TO_BANK.getBizProductCode(), response);
        return response;
    }
    
    /**
     * 查询联系人
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryContacts")
    public RestResponse queryContacts() {
        RestResponse response = new RestResponse();
        String[][] contacts = new String[][]{{"熬厂长","waasdfasdf@asf.com","1"},{"熬厂长","waasdfasdf@asf.com","1"}};
        response.setMessageObj(contacts);
        return response;
    }
    
    /**
     * 获取原始凭证
     * @param request 请求
     * @param user 用户信息
     * @param money 转账金额
     * @return 原始凭证
     */
    private TradeRequestInfo getTradeRequest(HttpServletRequest request,
            EnterpriseMember user, String money, String token) throws Exception {
        TradeRequestInfo tradeReqest = getJsonAttribute(request.getSession(),
                CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER + token,
                TradeRequestInfo.class);
        if (tradeReqest == null) {
            //封装客户端信息,获取凭证号
            tradeReqest = new TradeRequestInfo();
            BeanUtils.copyProperties(tradeReqest, env);
            defaultTransferService.getTransferVoucherNo(tradeReqest);
        }
        tradeReqest.setAmount(new Money(money));
        tradeReqest.setTradeType(TradeType.PAY_TO_BANK);
        com.netfinworks.site.domain.domain.info.PartyInfo payer = tradeReqest.getPayer();
        if (payer != null) {
            payer.setAccountName(user.getLoginName());
            payer.setEnterpriseName(user.getEnterpriseName());
        }
        tradeReqest.setPayer(payer);

        return tradeReqest;
    }
    
    /**
     * 导入银行账户批量转账
     * @param request
     * @param session
     * @param batchFile
     * @param env
     * @return
     */
    @RequestMapping(value = "/importBankBatchTransfer", method = {
        RequestMethod.POST, RequestMethod.GET })
    public ModelAndView importBankBatchTransfer(HttpServletRequest request, HttpSession session, 
            @RequestParam("batchFile") MultipartFile batchFile, TradeEnvironment env) {
        ModelAndView mv = this.initOcxView();
        
        // 获得用户信息
        EnterpriseMember user = this.getUser(request);
        // 软硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            mv.addObject("isCertActive", "yes");
        } else {
            mv.addObject("isCertActive", "no");
            if (StringUtils.isEmpty(user.getMobile())) {
                mv.addObject("message", "请先绑定数字证书或手机！");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        }
        
        logger.debug("导入Excel文件[{}]", batchFile.getOriginalFilename());
        Workbook xwb = null;
        
        // 批量转账账户信息
        int transferCount = 0;
        List<TransferBankBatchForm> batchFormList = new ArrayList<TransferBankBatchForm>();
        BankTransferForm totalForm = new BankTransferForm();
        List<BankTransfer> transferList = new ArrayList<BankTransfer>();
        //批量转账账户商户订单号信息
      	List<String> sourceDetailNoList=new ArrayList<String>();
      	//外部批次号里
      	StringBuffer sourceBatchNo=new StringBuffer();; 
        try {
            xwb = WorkbookFactory.create(batchFile.getInputStream());
            
            // 循环工作表Sheet
            int numOfSheets = xwb.getNumberOfSheets();
            for (int i=0; i<numOfSheets; i++) {
                String sheetName = xwb.getSheetName(i);
                if (TRANSFER_TO_BANK_SHEET_NAME.equals(sheetName)) {
                    parseOneBankSheet(batchFormList, xwb.getSheetAt(i),sourceDetailNoList,sourceBatchNo);
                }
            }
			//判断商户订单号是否有重复			 
			for(String sourceDetailNo: sourceDetailNoList){
			    if(Collections.frequency(sourceDetailNoList, sourceDetailNo) > 1){
			    	throw new Exception("您导入的文件有重复商户订单号！");
			    }
			}
            
            // 生成转账信息
            transferCount = generateBatchTransferInfo(totalForm, this.getUser(request), batchFormList, mv, transferList, request, env);
            if (transferCount == 0) {
                throw new Exception("您导入的文件未包含有效数据！");
            }
        } catch (Exception e) {
            logger.error("导入Excel文件失败", e);
            mv.addObject("message", StringUtils.isEmpty(e.getMessage())?ErrorMsg.ERROR_EXCEL_FORMAT.getDesc():e.getMessage());
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        MemberAccount account = null;
        try {
            // 检查账户
            account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
            mv.addObject("user", user);
        } catch (Exception e) {
            logger.error("查询账户失败", e);
            mv.addObject("message", "您的账户未绑定银行卡");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        Money avaBalance = account.getAvailableBalance();
        Money maxMoney = new Money(totalForm.getMaxAmount());
        Money totalMoney = new Money(totalForm.getTotalMoney());
        
        // 验证限额限次
        if (!super.validateLflt(user.getMemberId(), "",maxMoney, TradeType.PAY_TO_BANK, mv, null, env)) {
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        
        // 检查余额是否足够
        if (avaBalance.compareTo(totalMoney) == -1) {
            logger.error("账户[{}]余额不足", account.getAccountId());
            mv.clear();
            mv.addObject("message", "账户余额不足[批量转账支出金额" + totalMoney + "大于账户余额" + avaBalance + "]");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        mv.addObject("avaBalance", avaBalance);
        
        long token = RadomUtil.createRadom();
        
        // 把相关信息放到Session中
        this.setJsonAttribute(session, "bankTransferForm" + token, totalForm);
        this.setJsonAttribute(session, "transferList" + token, transferList);
        this.setJsonAttribute(session, "transferCount" + token, transferCount);
        this.setJsonAttribute(session, "sourceBatchNo" + token, sourceBatchNo);
        mv.addObject("token", token);
        mv.addObject("sourceBatchNo", sourceBatchNo);
        
        if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {
            // 有转账审核权限
            mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-batch-confirm");
        } else {
            // 无转账审核权限
            mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-batch-apply-confirm");
        }
        
        // 硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            mv.addObject("isCertActive", "yes");
        } else {
            mv.addObject("isCertActive", "no");
        }
        
        return mv;
    }
    
    /**
     * 从会话中分页查询记录
     * @param sessionPage
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/getSessionPage")
    public RestResponse getSessionPage(HttpServletRequest request, PageInfo page, HttpSession session) {
        RestResponse response = new RestResponse();
        
        try {
            String token = request.getParameter("token");
            List<BankTransfer> transferList = getTransferList(this.getJsonAttribute(session, "transferList" + token, List.class));
            SessionPage<BankTransfer> sessionPage = new SessionPage<BankTransfer>(page, new ArrayList<BankTransfer>());
            super.setSessionPage(transferList, sessionPage);
            response.setSuccess(true);
            response.setMessageObj(sessionPage);
        } catch (Exception e) {
            logger.error("查询分页信息出错");
            response.setMessage("查询分页信息出错");
        }
        
        return response;
    }
    
    /**
     * 生成批量转账信息
     * @param totalForm 合计转账信息
     * @param batchFormList 导入批量转账原始列表
     * @param mv 视图
     * @param transferList 转账信息列表
     * @return 转账笔数
     */
    private int generateBatchTransferInfo(BankTransferForm totalForm, EnterpriseMember user, List<TransferBankBatchForm> batchFormList, 
            ModelAndView mv, List<BankTransfer> transferList, HttpServletRequest request, TradeEnvironment env) {
        int transferCount = 0;
        if (ObjectUtils.isListNotEmpty(batchFormList)) {
            // 统计总金额
            Money totalAmount = new Money();
            Money totalServiceFee = new Money();
            int size = batchFormList.size();
            
            //保存原始凭证号列表到session中
            for (int i=0; i<size; i++) {
                // 整理转账信息列表
                BankTransfer transfer = new BankTransfer();
                TransferBankBatchForm batchForm = batchFormList.get(i);
                String amount = batchForm.getTransferAmount();
                transfer.setOrderNo(i+1);
                transfer.setName(batchForm.getAccountName());
                transfer.setMoney(amount);
                transfer.setBankName(batchForm.getBankName());
                transfer.setBranchName(batchForm.getBranchBankName());
                transfer.setAccountNoMask(batchForm.getAccountNo());
                transfer.setProvName(batchForm.getBankProvince());
                transfer.setCityName(batchForm.getBankCity());
                transfer.setCardAttribute(batchForm.getAccountType());
                transfer.setRemark(batchForm.getRemark());
                transfer.setMobile(batchForm.getMobile());
                transfer.setSourceDetailNo(batchForm.getSourceDetailNo());
                transferList.add(transfer);
                
                // 更新最大金额
                setMaxAmount(batchForm.getTransferAmount(), totalForm);
                
                // 合计金额
                totalAmount.addTo(new Money(StringUtils.isEmpty(amount)?"0":amount));
                
                // 计算代发工资的服务费
                String serviceFeeStr = getServiceFee(request, amount, TradeType.PAY_TO_BANK.getBizProductCode(), null);
                Money serviceFee = new Money(serviceFeeStr);
                if (serviceFee.compareTo(new Money()) == 1) {
                    totalServiceFee.addTo(serviceFee);
                }
            }
                
            // 计算服务费
            totalForm.setServiceCharge(totalServiceFee.toString());
            totalForm.setTransMoney(totalAmount.toString());
            totalForm.setTotalMoney(totalAmount.addTo(totalServiceFee).toString());
        }
        
        transferCount = transferList.size();
        mv.addObject("transferPerCount", transferCount);
        if (MemoType.TYPE_OTHERS.getCode() != totalForm.getRemarkType()) {
            // 选择其他时，获取用户输入的备注
            totalForm.setRemark(MemoType.getDesc(totalForm.getRemarkType()));
        }
        mv.addObject("form", totalForm);
        SessionPage<BankTransfer> sessionPage = new SessionPage<BankTransfer>();
        super.setSessionPage(transferList, sessionPage);
        mv.addObject("sessionPage", sessionPage);
        
        return transferCount;
    }
    
    /**
     * 更新最大金额
     * @param curAmount 当前交易金额
     * @param totalForm 批量转账统计对象
     */
    private void setMaxAmount(String curAmount, BankTransferForm totalForm) {
        if (StringUtils.isEmpty(curAmount)) {
            return;
        }
        
        if (StringUtils.isEmpty(totalForm.getMaxAmount())) {
            totalForm.setMaxAmount(curAmount);
            return;
        }
        
        Money curMoney = new Money(curAmount);
        if (curMoney.compareTo(new Money(totalForm.getMaxAmount())) > 0) {
            totalForm.setMaxAmount(curAmount);
        }
    }
    
    /**
     * 解析sheet中的银行账户转账信息
     * @param batchFormList 转账信息列表
     * @param sheet excel中的sheet对象
     * @throws Exception 
     */
    private void parseOneBankSheet(List<TransferBankBatchForm> batchFormList, Sheet sheet,List<String> sourceDetailNoList,StringBuffer sourceBatchNo) throws Exception {
        
    	//获取批次号
    	Row batchRow = sheet.getRow(1);    
    	String sourceBatchNoTemp=getValue(batchRow.getCell(2));		
		if(!sourceBatchNoTemp.matches("^\\w{1,32}$")){
      	  throw new Exception("商户批次不能为空或格式不正确！");
        }
		sourceBatchNo.append(sourceBatchNoTemp);    	
    	// 循环行Row
        int lastRowNum = sheet.getLastRowNum();
        for (int rowNum = 3; rowNum <= lastRowNum; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                logger.debug(rowNum + "行为空");
                continue;
            }
            
            TransferBankBatchForm form = new TransferBankBatchForm();
            String accountName = getValue(row.getCell(0));
            form.setAccountName(accountName);
            logger.debug("银行户名：{}", accountName);
            
            String amount = getValue(row.getCell(6));
            form.setTransferAmount(amount);
            
            Cell bankName = row.getCell(1);
            form.setBankName(getValue(bankName));
            
            Cell bankProvince = row.getCell(2);
            form.setBankProvince(getValue(bankProvince));
            
            Cell bankCity = row.getCell(3);
            form.setBankCity(getValue(bankCity));
            
            Cell branchBankName = row.getCell(4);
            form.setBranchBankName(getValue(branchBankName));
            
            Cell accountNo = row.getCell(5);
            form.setAccountNo(getValue(accountNo));
            
            String companyOrPersonal = getValue(row.getCell(7));
            int accountType = -1;
            if (CompanyOrPersonal.COMPANY.getMessage().equals(companyOrPersonal)) {
                accountType = CardAttr.COMPANY.getCode();
            } else if (CompanyOrPersonal.PERSONAL.getMessage().equals(companyOrPersonal)) {
                accountType = CardAttr.PERSONAL.getCode();
            }
            form.setAccountType(accountType);
            
            Cell mobile = row.getCell(8);
            form.setMobile(getValue(mobile));
            
            Cell remark = row.getCell(9);
            form.setRemark(getValue(remark));
            
            Cell sourceDetailNo=row.getCell(10);
			form.setSourceDetailNo(getValue(sourceDetailNo));
            
            // 验证结果为false忽略
            if (validateForm(form, rowNum+1)) {
                batchFormList.add(form);
                sourceDetailNoList.add(getValue(sourceDetailNo));
            }
            
            if (batchFormList.size() > MAX_IMPORT_COUNT) {
                throw new RuntimeException(ErrorMsg.ERROR_TRANSFER_COUNT.getDesc());
            }
        }
    }
    
    /**
     * 验证解析上传文件的form对象
     * @param form form对象
     * @param rowNum 行号
     * @return true-校验成功，false-无效可忽略
     * @throws Exception 抛出异常信息提示用户
     */
    private boolean validateForm(TransferBankBatchForm form, int rowNum) throws Exception {
        if (form == null) {
            return false;
        }
        
        // 如果所有行都是空，为无效行，可忽略
        if (StringUtils.isEmpty(form.getAccountName()) && StringUtils.isEmpty(form.getBankName())
                && StringUtils.isEmpty(form.getBankProvince()) && StringUtils.isEmpty(form.getBankCity())
                && StringUtils.isEmpty(form.getBranchBankName()) && StringUtils.isEmpty(form.getAccountNo()) 
                && StringUtils.isEmpty(form.getTransferAmount()) && (form.getAccountType() < 0)
                && StringUtils.isEmpty(form.getMobile()) && StringUtils.isEmpty(form.getRemark()) && StringUtils.isEmpty(form.getSourceDetailNo())) {
            return false;
        }
        
        if (StringUtils.isEmpty(form.getAccountName())) {
            throw new Exception("第[" + rowNum + "]行的[收款开户名称]不能为空");
        }
        
        String amount = form.getTransferAmount();
        if (StringUtils.isEmpty(amount)) {
            throw new Exception("第[" + rowNum + "]行的[转账金额]不能为空");
        }
        
        Money validMoney = null;
        try {
            validMoney = new Money(amount);
        } catch (Exception e) {
            throw new Exception("第[" + rowNum + "]行的[转账金额]格式不正确");
        }
        if ((validMoney == null) || (validMoney.compareTo(new Money("0")) != 1)) {
            throw new Exception("第[" + rowNum + "]行的[转账金额]不能为负数");
        }
        
        if (StringUtils.isEmpty(form.getBankName())) {
            throw new Exception("第[" + rowNum + "]行的[收款银行名称]不能为空");
        }
        
        if (BankType.getByMsg(form.getBankName()) == null) {
            throw new Exception("第[" + rowNum + "]行的[收款银行名称]填写有误，系统中不存在该银行[" + form.getBankName() + "]");
        }
        
        if (StringUtils.isEmpty(form.getBankProvince())) {
            throw new Exception("第[" + rowNum + "]行的[收款银行所在省份]不能为空");
        }
        
        if (StringUtils.isEmpty(form.getBankCity())) {
            throw new Exception("第[" + rowNum + "]行的[收款银行所在市]不能为空");
        }
        
        if (StringUtils.isEmpty(form.getBranchBankName())) {
            throw new Exception("第[" + rowNum + "]行的[收款支行名称]不能为空");
        }
        
        if (StringUtils.isEmpty(form.getAccountNo())) {
            throw new Exception("第[" + rowNum + "]行的[收款银行帐号]不能为空");
        }
        
        if (StringUtils.isEmpty(form.getRemark())) {
            throw new Exception("第[" + rowNum + "]行的[转账备注]不能为空");
        }
        
        if (StringUtils.isEmpty(form.getSourceDetailNo())) {
            throw new Exception("第[" + rowNum + "]行的[商户订单号]不能为空");
        }
        
        if (form.getAccountType() < 0) {
            throw new Exception("第[" + rowNum + "]行的[对公对私]填写有误，请填写“对公”或“对私”");
        }
        
        if (!CommonUtils.validateRegex(RegexRule.AMOUNT_2_DECINALS, amount)) {
            logger.error("导入第" + rowNum + "行的[转账金额]需保留2位小数！");
            throw new RuntimeException("第" + rowNum + "行的[转账金额]需保留2位小数！");
        }
        
        if (!CommonUtils.validateRegex(RegexRule.BANKCARD_NO, form.getAccountNo())) {
            logger.error("导入第" + rowNum + "行的银行卡号长度必须是8~32位的数字！");
            throw new RuntimeException("第" + rowNum + "行的银行卡号长度必须是8~32位的数字！");
        }
        
        if (!CommonUtils.validateRegex(RegexRule.MOBLIE, form.getMobile())) {
            logger.error("导入第" + rowNum + "行的手机号码格式不正确！");
            throw new RuntimeException("第" + rowNum + "行的手机号码格式不正确！");
        }
        
        if (!CommonUtils.validateRegex(RegexRule.ACCOUNT_NAME, form.getAccountName())) {
            logger.error("导入第" + rowNum + "行的银行开户名不能超过20个字！");
            throw new RuntimeException("第" + rowNum + "行的银行开户名不能超过20个字！");
        }
        
        if(!form.getSourceDetailNo().matches("^\\w{1,32}$")){
            logger.error("导入第" + rowNum + "行的商户订单号格式不正确！");
      	    throw new Exception("第[" + rowNum + "]行输入的商户订单号格式不正确！");
        }
        
        return true;
    }
    
    /**
     * 解析Transfer的JSON字符串列表
     * @param list
     * @return
     */
    private List<BankTransfer> getTransferList(List<?> list) {
        if (ObjectUtils.isListEmpty(list)) {
            return null;
        }
        
        List<BankTransfer> transferList = new ArrayList<BankTransfer>();
        for(Object object : list) {
            BankTransfer transfer = JSONArray.parseObject(String.valueOf(object), BankTransfer.class);
            transferList.add(transfer);
        }
        
        return transferList;
    }
    
    /**
     * 得到Excel表中的值
     * @param cell Excel中的单元格
     * @return Excel中单元格的值
     */
    @SuppressWarnings("static-access")
    private String getValue(Cell cell) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
            // 返回布尔类型的值
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
            // 返回数值类型的值
            BigDecimal decimal = new BigDecimal(String.valueOf(cell.getNumericCellValue()));
            return decimal == null ? StringUtils.EMPTY : decimal.toPlainString();
        } else {
            // 返回字符串类型的值
            return String.valueOf(cell.getStringCellValue());
        }
    }
    
    /**
     * 获取账户详细信息
     * @param form
     * @return
     */
    private BankAcctDetailInfo convertToAcctDetailInfo(BankTransferForm form) {
        // 目标账户详细信息
        BankAcctDetailInfo bankAcctInfo = new BankAcctDetailInfo();
        bankAcctInfo.setBankName(form.getBankName());
        bankAcctInfo.setRealName(form.getRecvAcctName());
        bankAcctInfo.setBankAccountNum(getTicketFromData(form.getAccountNo()));
        bankAcctInfo.setBankBranch(form.getBranchName());
        bankAcctInfo.setProvince(form.getProvince());
        bankAcctInfo.setCity(form.getCity());
        bankAcctInfo.setMobileNum(form.getMobile());
        bankAcctInfo.setBranchNo(form.getBranchNo());
        bankAcctInfo.setCardAttribute(form.getAccountType());
        return bankAcctInfo;    
    }
    
//  /**
//   * 获取批量转账中每一笔转账的详细信息
//   * @param transfer 转账信息
//   * @return 目标账户明细
//   */
//  private BankAcctDetailInfo convertToAcctDetailInfo(BankTransfer transfer) {
//      // 目标账户详细信息
//      BankAcctDetailInfo bankAcctInfo = new BankAcctDetailInfo();
//      bankAcctInfo.setBankName(transfer.getBankName());
//      bankAcctInfo.setRealName(transfer.getName());
//      bankAcctInfo.setBankAccountNum(getTicketFromData(transfer.getAccountNoMask()));
//      bankAcctInfo.setBankBranch(transfer.getBranchName());
//      bankAcctInfo.setProvince(transfer.getProvName());
//      bankAcctInfo.setCity(transfer.getCityName());
//      bankAcctInfo.setMobileNum(transfer.getMobile());
//      bankAcctInfo.setCardAttribute(transfer.getCardAttribute());
//      return bankAcctInfo;    
//  }
    
    /**
     * 保存审核信息
     * @param tradeReqest
     * @param money
     * @param bankAcctInfo
     * @param user
     * @param payee
     * @param payer
     * @param status
     * @param freezeVourceNo
     */
    private void saveAuditInfo(HttpServletRequest request, TradeRequestInfo tradeReqest, Money money, BankAcctDetailInfo bankAcctInfo,
            EnterpriseMember user, String status, String freezeVourceNo) {
        // 提交审核申请
        Audit audit = new Audit();
        audit.setTranVoucherNo(tradeReqest.getTradeVoucherNo());
        audit.setAuditType(AuditType.AUDIT_TRANSFER_BANK.getCode());
        audit.setAuditSubType(AuditSubType.SINGLE.getCode());
        if (money != null) {
            String feeStr = super.getServiceFee(request, money.toString(), tradeReqest.getTradeType().getBizProductCode(), null);
            audit.setFee(new Money(StringUtils.isEmpty(feeStr) ? "0" : feeStr));
        } else {
            money = new Money("0");
        }
        audit.setAmount(money);
        audit.setMemberId(user.getMemberId());
        audit.setOperatorName(user.getOperator_login_name());
        audit.setOperatorId(user.getCurrentOperatorId());
        audit.setStatus(status);
        if(AuditStatus.AUDIT_PASSED.getCode().equals(status)){
            audit.setAuditorName(user.getOperator_login_name());
            audit.setGmtModified(new Date());
        }
        audit.setGmtCreated(new Date());
        audit.setExt(freezeVourceNo);
        audit.setPayeeNo(CommonUtils.getEmptyStr(bankAcctInfo.getBankAccountNum()) + "|" 
                + CommonUtils.getEmptyStr(bankAcctInfo.getRealName()));
        audit.setPayeeBankInfo(bankAcctInfo.getBankBranch());
        audit.setExt(StringUtils.isEmpty(freezeVourceNo) ? StringUtils.EMPTY : freezeVourceNo);
        
        // 验证银行卡有无联行号
        if (StringUtils.isEmpty(bankAcctInfo.getBankName())) {
            throw new RuntimeException("交易编号为" + audit.getTranVoucherNo() + "的银行卡没有联行号");
        }
        
        // 付款信息作为tradeReqest的属性
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("tradeReqest", tradeReqest);
        dataMap.put("payeeBankAcctInfo", bankAcctInfo);
        audit.setAuditData(JSONObject.toJSONString(dataMap));
        auditService.addAudit(audit);
    }
    
    /**
     * 保存批量审核信息
     * @param tradeReqest
     * @param money
     * @param bankAcctInfo
     * @param user
     * @param payee
     * @param payer
     * @param status
     * @param freezeVourceNo
     */
    private void saveBatchAuditInfo(HttpServletRequest request, String tradeVoucherNo, String amount, 
            EnterpriseMember user, String status, String freezeVourceNo, String totalFeeStr,String sourceBatchNo) {
        // 提交审核申请
    	Map<String,Object> dataMap = new HashMap<String,Object>();
    	dataMap.put("sourceBatchNo", sourceBatchNo);
        Audit audit = new Audit();
        audit.setTranVoucherNo(tradeVoucherNo);
        audit.setAuditType(AuditType.AUDIT_TRANSFER_BANK.getCode());
        audit.setAuditSubType(AuditSubType.BATCH.getCode());
        audit.setFee(new Money(totalFeeStr));
        audit.setAmount(new Money(amount));
        audit.setMemberId(user.getMemberId());
        audit.setOperatorName(user.getOperator_login_name());
        audit.setOperatorId(user.getCurrentOperatorId());
        audit.setStatus(status);
        if(AuditStatus.AUDIT_PASSED.getCode().equals(status)){
            audit.setAuditorName(user.getOperator_login_name());
            audit.setAuditorId(user.getCurrentOperatorId());
            audit.setGmtModified(new Date());
        }
        audit.setGmtCreated(new Date());
        audit.setExt(StringUtils.isEmpty(freezeVourceNo) ? StringUtils.EMPTY : freezeVourceNo);
        audit.setAuditData(JSONObject.toJSONString(dataMap));   
        audit.setTranSourceVoucherNo(sourceBatchNo);
        auditService.addAudit(audit);
    }
    
    @ResponseBody
    @RequestMapping("/checkPayPassword.htm")
    public RestResponse checkPayPassword(HttpServletRequest request) {
        RestResponse resp = new RestResponse();
        
        // 检查支付密码
        validatePayPassword(request, this.getUser(request), resp, null);
        return resp;
    }
    
    private void removeTransferSession(HttpServletRequest request, String token) {
        request.getSession().removeAttribute("bankTransferForm" + token);
        request.getSession().removeAttribute("transferList" + token);
        request.getSession().removeAttribute("transferCount" + token);
    }
}
