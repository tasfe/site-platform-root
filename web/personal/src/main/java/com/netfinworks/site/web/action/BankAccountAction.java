package com.netfinworks.site.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.bank.BankBranchShort;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.CardType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.form.BankAccountForm;

/**
 *
 * <p>
 * 通用说明：会员银行账户action
 * </p>
 *
 * @author qinde
 * @version $Id: BankAccountAction.java, v 0.1 2013-11-25 下午5:37:59 qinde Exp $
 */
@Controller
public class BankAccountAction extends BaseAction {

    protected Logger                  log = LoggerFactory.getLogger(getClass());

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name = "defaultPfsBaseService")
    private DefaultPfsBaseService     defaultPfsBaseService;

    @Resource(name = "defaultMemberService")
    private DefaultMemberService      defaultMemberService;

    @Resource(name = "defaultAccountService")
    private DefaultAccountService     defaultAccountService;

    /**
     * 添加银行卡绑定
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/addBankAccount.htm")
    public @ResponseBody
    RestResponse add(HttpServletRequest request, @Valid BankAccountForm form, BindingResult result,
                     ModelMap model) {
        RestResponse restP = new RestResponse();
        try {
            String bankAccountNum = form.getBankAccountNum();
            if (StringUtils.isBlank(bankAccountNum)) {
                restP.setMessage("银行卡不能为空!");
                restP.setSuccess(false);
                return restP;
            }
            if (StringUtils.isBlank(form.getRealName())) {
                restP.setMessage("户名不能为空!");
                restP.setSuccess(false);
                return restP;
            }
            PersonMember user = getUser(request);
            if ((user.getMemberType().getCode() == "2")
                && StringUtils.isBlank(form.getCardAttribute())) {
                restP.setMessage("卡属性不能为空!");
                restP.setSuccess(false);
                return restP;
            }

            // 查询绑定银行卡
            BankAccRequest accReq = new BankAccRequest();
            accReq.setMemberId(user.getMemberId());
            accReq.setClientIp(request.getRemoteAddr());
            accReq.setBankAccountNum(bankAccountNum);
            List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
            if ((list != null) && (list.size() > 0)) {
                restP.setMessage("对不起，此银行卡已绑定，请重新选择另一张!");
                restP.setSuccess(false);
                return restP;
            }
            BankAccRequest bankreq = new BankAccRequest();
            bankreq.setMemberId(user.getMemberId());
            bankreq.setClientIp(request.getRemoteAddr());
            String bank = form.getBankCode();
            String[] banks = null;
            if (bank.contains("@")) {
                banks = bank.split("@");
            }
            // 卡bin校验失败
            if (StringUtils.isNotBlank(form.getCardAttribute()) && form.getCardAttribute().equals("1")) {
                if (!defaultPfsBaseService.cardValidate(CommonConstant.PERSONAL_APP_ID, banks[0],
                    form.getBankAccountNum())) {
                    restP.setMessage("卡号校验失败,请确认卡号与对应的银行是否一致");
                    restP.setSuccess(false);
                    return restP;
                }
            }
            // 卡bin校验失败
            try {
                CardBin cardBin = defaultPfsBaseService.queryByCardNo(form.getBankAccountNum(),
                    CommonConstant.PERSONAL_APP_ID);
                if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
                    restP.setMessage("提现银行卡不能是信用卡!");
                    restP.setSuccess(false);
                    return restP;
                }
            } catch (Exception e) {
                logger.warn("未查询到卡bin信息，卡号={}", form.getBankAccountNum());
            }
            BankAccRequest req = new BankAccRequest();
            req.setMemberId(user.getMemberId());
            req.setCardType(Integer.valueOf(CardType.JJK.getCode()));
            if (user.getMemberType().getCode() == "2") {
                req.setCardAttribute(Integer.valueOf(form.getCardAttribute()));
            } else if (user.getMemberType().getCode() == "1") {
                req.setCardAttribute(1);
            }
            req.setPayAttribute("normal");
            req.setBankCode(banks[0]);
            req.setBankName(banks[1]);
            req.setBankAccountNum(form.getBankAccountNum());
            if (user.getMemberType().getCode() == "1") {//个人
                req.setRealName(user.getRealName().getPlaintext());
            } else {//企业
                req.setRealName(form.getRealName());
            }
            String prov = form.getProvince();
            if (prov.contains("@")) {
                String[] info = prov.split("@");
                prov = info[1];
            }
            req.setProvName(prov);
            String city = form.getCity();
            if (city.contains("@")) {
                String[] info = city.split("@");
                city = info[1];
            }
            req.setCityName(city);
            req.setCardType(Integer.valueOf(CardType.JJK.getCode()));

            String branch = form.getBranchCode();
            if (branch.contains("@")) {
                String[] info = branch.split("@");
                req.setBranchNo(info[0]);
                req.setBranchName(info[1]);
            }
            String bankCard = defaultBankAccountService.addBankAccount(req);
            user.setBankCardCount(user.getBankCardCount() + 1);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("bankCard", bankCard);
            restP.setData(data);
            restP.setSuccess(true);
            return restP;
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return restP;
        }
    }

    /**
     * 解除银行卡绑定
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/deleteBankAccount.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView delete(HttpServletRequest request, TradeEnvironment env)
                                                                                throws ServiceException {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> errorMap = null;
        PersonMember user = getUser(request);
        checkUser(user, errorMap, restP);
        String bankcardId = request.getParameter("bankcardid");
        if (StringUtils.isBlank(bankcardId)) {
            errorMap = new HashMap<String, String>();
            errorMap.put("bankcardid_is_empty", "bankcardid_is_empty");
            restP.setErrors(errorMap);
            return new ModelAndView("redirect:/my/withdraw.htm", "response", restP);
        }
        BankAccRequest bankAccRequest = new BankAccRequest();
        bankAccRequest.setBankcardId(bankcardId);
        bankAccRequest.setMemberId(user.getMemberId());
        defaultBankAccountService.removeBankAccount(bankAccRequest);
        //查询余额
        MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),
            env);
        user.setBankCardCount(user.getBankCardCount() - 1);
        data.put("account", user.getMobile());
        data.put("accountBalance", account.getAvailableBalance());
        restP.setData(data);
        return new ModelAndView("redirect:/my/withdraw.htm", "response", restP);
    }

    /**
     * 解除银行卡绑定
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/deleteBankCard.htm")
    public ModelAndView deleteManage(HttpServletRequest request) throws ServiceException {
        RestResponse restP = new RestResponse();
        PersonMember user = getUser(request);
        String bankcardId = request.getParameter("bankcardid");
        if (StringUtils.isBlank(bankcardId)) {
            throw new IllegalArgumentException("cardNo is null");
        }
        BankAccRequest bankAccRequest = new BankAccRequest();
        bankAccRequest.setBankcardId(bankcardId);
        bankAccRequest.setMemberId(user.getMemberId());
        defaultBankAccountService.removeBankAccount(bankAccRequest);
        user.setBankCardCount(user.getBankCardCount() - 1);
        return new ModelAndView("redirect:/my/bankCardManage.htm", "response", restP);
    }

    /**
     * 查询省市县、银行、实名信息
     *
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/queryProv.htm", method = { RequestMethod.POST, RequestMethod.GET })
    public @ResponseBody
    RestResponse queryProc(HttpServletRequest request, OperationEnvironment env) throws Exception {

        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        PersonMember user = getUser(request);
        try {
            // 是否实名认证
            defaultMemberService.fillMemberRealName(user, env);
            String memberTypeCode = user.getMemberType().getCode();
            if (memberTypeCode == "1") {
				if ((user.getCertifyLevel() == CertifyLevel.CERTIFY_V0)
						|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V1)
						|| (user.getCertifyLevel() == CertifyLevel.CERTIFY_V2)) {
                    // 已实名
					data.put("realName", user.getMemberName());
                } else {// 未实名
                    data.put("realName", "");
                    restP.setData(data);
                    restP.setSuccess(true);
                    data.put("memberTypeCode", memberTypeCode);
                    return restP;
                }

            }
            // 查询城市信息
            List<Province> list = defaultPfsBaseService.queryProvInfos(false, false);
            data.put("province", list);
            List<BankCard> banks = defaultPfsBaseService.queryBank(CropOrPersonal.PERSONAL);
            // 查询银行信息
            data.put("bank", banks);
            defaultMemberService.fillMemberRealName(user, env);
            restP.setSuccess(true);
            restP.setData(data);
            data.put("memberTypeCode", memberTypeCode);
            return restP;
        } catch (ServiceException e) {
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return restP;
        }
    }

    /**
     * 查询根据省查询市信息
     *
     * @param request
     * @return
     * @throws BindException
     */
    @RequestMapping(value = "/my/queryCity.htm", method = { RequestMethod.POST, RequestMethod.GET })
    public @ResponseBody
    RestResponse queryCity(HttpServletRequest request) throws Exception {

        String provId = request.getParameter("provId");
        if (provId.contains("@")) {
            String[] info = provId.split("@");
            provId = info[0];
        }
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> errorMap = null;
        PersonMember user = getUser(request);
        checkUser(user, errorMap, restP);
        try {
            // 查询城市信息
            List<Province> list = defaultPfsBaseService.queryProvInfos(true, false);
            for (Province p : list) {
                if (p.getProvId() == Long.valueOf(provId)) {
                    data.put("city", p.getCityInfos());
                }
            }
            restP.setData(data);
            restP.setSuccess(true);
            return restP;
        } catch (ServiceException e) {
            log.error(e.getMessage(), e.getCause());
            restP.setSuccess(false);
            restP.setMessage(e.getMessage());
            return restP;
        }
    }

    /**
     * 查询分行信息
     *
     * @param request
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/queryBranch.htm", method = { RequestMethod.POST, RequestMethod.GET })
    public @ResponseBody
    RestResponse queryBranch(HttpServletRequest request, HttpServletResponse response)
                                                                                      throws IOException {

        RestResponse restP = new RestResponse();
        String cityId = request.getParameter("cityId");
        String bankCode = request.getParameter("bankCode");
        try {
            if (bankCode.contains("@")) {
                String[] info = bankCode.split("@");
                bankCode = info[0];
            }
            // String provId = request.getParameter("provId");
            if (logger.isInfoEnabled()) {
                logger.info("查询分行的原始的信息,银行：{}，城市：{}", bankCode, cityId);
            }
            if (cityId.contains("@")) {
                String[] info = cityId.split("@");
                cityId = info[0];
            }
            Map<String, Object> data = new HashMap<String, Object>();
            if (logger.isInfoEnabled()) {
                logger.info("查询分行的处理后的信息,银行：{}，城市：{}", bankCode, cityId);
            }
            List<BankBranch> branchs = defaultPfsBaseService.queryBranch(
                CommonConstant.PERSONAL_APP_ID, bankCode, Long.valueOf(cityId));
            if (logger.isInfoEnabled()) {
                logger.info("查询分行的结果：{},{}", branchs != null ? branchs.size() : 0, branchs);
            }
            List<BankBranchShort> list = new ArrayList<BankBranchShort>();
            if ((branchs != null) && (branchs.size() > 0)) {
                for (BankBranch branch : branchs) {
                    BankBranchShort bshort = new BankBranchShort();
                    bshort.setName(branch.getBranchName());
                    bshort.setNo(branch.getBranchNo());
                    bshort.setsName(branch.getBranchShortName());
                    list.add(bshort);
                }
            }
            if (logger.isInfoEnabled()) {
				logger.info("查询分行的结果：{},{}", list.size());
            }
            data.put("branch", list);
			data.put("total", list.size());
            restP.setSuccess(true);
            restP.setData(data);
            return restP;
        } catch (Exception e) {
            log.error("获取分行数据出错,银行：{}，城市：{},错误:{}", bankCode, cityId, e);
            restP.setSuccess(false);
            return restP;
        }
    }

    /**
     * 银行卡管理
     *
     * @param model
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/my/bankCardManage.htm")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse resp,
                              TradeEnvironment env, ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = new HashMap<String, Object>();
        PersonMember user = getUser(request);
        // 查询绑定银行卡
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
        for (BankAccountInfo card : list) {
            card.setBankAccountNumMask(StarUtil.mockBankCardByBlank(card.getBankAccountNumMask()));
            card.setRealName(StarUtil.mockRealName(card.getRealName()));
        }
        // 进入提现页面
        data.put("banks", list);
        data.put("mobile", user.getMobileStar());
        data.put("member", user);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/bankcard/bankcard", "response", restP);
    }

}
