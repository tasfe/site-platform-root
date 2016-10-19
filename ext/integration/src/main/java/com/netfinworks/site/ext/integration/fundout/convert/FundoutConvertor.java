/**
 *
 */
package com.netfinworks.site.ext.integration.fundout.convert;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.netfinworks.fos.service.facade.enums.CardType;
import com.netfinworks.fos.service.facade.enums.CompanyOrPersonal;
import com.netfinworks.fos.service.facade.enums.FundoutGrade;
import com.netfinworks.fos.service.facade.request.FundoutQueryRequest;
import com.netfinworks.fos.service.facade.request.FundoutRequest;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.CardAttr;

/**
 * <p>出款服务转换</p>
 * @author fjl
 * @version $Id: FundoutConvertor.java, v 0.1 2013-11-29 下午6:01:40 fjl Exp $
 */
public class FundoutConvertor {

    public static FundoutRequest convert(TradeRequestInfo req, BankAcctDetailInfo bankCard) {
        FundoutRequest request = new FundoutRequest();
        request.setFundoutOrderNo(req.getTradeVoucherNo());
        request.setPaymentOrderNo(req.getPaymentVoucherNo());
        request.setMemberId(req.getPayer().getMemberId());
        request.setAccountNo(req.getPayer().getAccountId());
        request.setProductCode(req.getTradeType().getBizProductCode());
        request.setAmount(req.getAmount());
        request.setFee(req.getFree());
        //银行卡
        request.setCardId(bankCard.getBankcardId());
        //银行卡号为ticket
        request.setCardNo(bankCard.getBankAccountNum());
        request.setCardType(CardType.DC.getCode());
        request.setName(bankCard.getRealName());
        request.setBankCode(bankCard.getBankCode());
        request.setBankName(bankCard.getBankName());
        request.setBranchName(bankCard.getBankBranch());
        request.setBankLineNo(bankCard.getBranchNo());
        request.setProv(bankCard.getProvince());
        request.setCity(bankCard.getCity());
        CardAttr attr = CardAttr.getByCode(bankCard.getCardAttribute());
        if (CardAttr.COMPANY.equals(attr)) {
            request.setCompanyOrPersonal(CompanyOrPersonal.COMPANY.getCode());
        } else {
            request.setCompanyOrPersonal(CompanyOrPersonal.PERSONAL.getCode());
        }
        request.setPurpose(req.getMemo());
        request.setFundoutGrade(FundoutGrade.GENERAL.getCode());

        return request;
    }

    /**
     * 出款查询
     * @param req
     * @return
     */
    public static FundoutQueryRequest convertFundoutQueryRequest(FundoutQuery req) {
        FundoutQueryRequest request = new FundoutQueryRequest();
        BeanUtils.copyProperties(req, request);
        return request;
    }
}
