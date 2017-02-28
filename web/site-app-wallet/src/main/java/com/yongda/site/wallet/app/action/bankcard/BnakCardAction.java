package com.yongda.site.wallet.app.action.bankcard;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.BankCardPayAttrType;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.integration.cashier.CashierService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.yongda.site.domain.domain.cashier.PayLimit;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.util.ResponseUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/11/9-10:42<br>
 */
@Controller
@RequestMapping("/bankcard")
public class BnakCardAction extends BaseAction {
    @Resource(name = "authVerifyService")
    private AuthVerifyService authVerifyService;

    @Resource(name = "defaultCertService")
    private DefaultCertService defaultCertService;

    @Resource(name = "maQueryService")
    private MaQueryService maQueryService;

    @Resource(name = "defaultAccountService")
    private DefaultAccountService defaultAccountService;

    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;

    @Resource(name="cashierService")
    private CashierService cashierService;

    @Resource(name = "payPasswordService")
    private PayPasswordService payPasswordService;

    @Value("${yd.h5AddCardAddress}")
    private String redirect_url;

    @Value("${dc.priKey}")
    private String priKey;

    /**
     *  查询绑定银行卡详细
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "my/{payAtt}", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public RestResponse infos(HttpServletRequest request, HttpServletResponse resp, OperationEnvironment env
            , @PathVariable("payAtt") String payAtt) throws Exception {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        PersonMember user = getUser(request);
        // 查询绑定银行卡
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        BankCardPayAttrType attrType = BankCardPayAttrType.getByCode(payAtt);
        if(attrType != null){
            req.setPayAttribute(attrType.getCode());
        }
        List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
        String defaultCard = this.getDefaultBankCard(list);
        restP.getData().put("defaultCard", defaultCard);
        restP.getData().put("cardList", list);
        restP.setSuccess(true);
        restP.setMessage("查询银行卡成功");
        logger.info("查询绑定银行卡响应结果："+restP.getData().toString());
        return restP;
    }

    @RequestMapping(value = "my/nums/{payAtt}", method =RequestMethod.GET)
    @ResponseBody
    public RestResponse nums(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
            ,@PathVariable("payAtt") String payAtt) throws Exception {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        PersonMember user = getUser(request);
        // 查询绑定银行卡
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        BankCardPayAttrType attrType = BankCardPayAttrType.getByCode(payAtt);
        if(attrType != null){
            req.setPayAttribute(attrType.getCode());
        }
        List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
        restP.getData().put("nums", CollectionUtils.isEmpty(list)?0:list.size());
        restP.setSuccess(true);
        restP.setMessage("查询银行卡数量成功");
        logger.info("查询银行卡数量成功："+restP.getData().toString());
        return restP;
    }

    @RequestMapping(value = "detail/{id}", method =RequestMethod.GET)
    @ResponseBody
    public RestResponse info(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
            ,@PathVariable(value = "id") String cardId) throws Exception {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        PersonMember user = getUser(request);
        // 查询绑定银行卡
        BankAcctDetailInfo detail = defaultBankAccountService.queryBankAccountDetail(cardId);
        restP.getData().put("detail", detail);
        if(detail != null){
            List<PayLimit> limit = cashierService.queryInstPayLimit(StringUtil.toUpperCase(detail.getBankCode()), detail.getCardType().equals(1)?"51":"52");
            restP.getData().put("limit", limit);
        }
        restP.setSuccess(true);
        restP.setMessage("查询银行卡详细成功");
        logger.info("查询银行卡详细信息响应结果：{}",restP.getData().toString());
        return restP;
    }
}
