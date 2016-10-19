package com.netfinworks.site.domainservice.pfs.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.ext.integration.pfs.PfsBaseService;

/**
 *
 * <p>查询省市县、分行信息</p>
 * @author qinde
 * @version $Id: DefaultPfsBaseServiceImpl.java, v 0.1 2013-11-28 上午11:10:03 qinde Exp $
 */
@Service("defaultPfsBaseService")
public class DefaultPfsBaseServiceImpl implements DefaultPfsBaseService {

    private Logger         logger = LoggerFactory.getLogger(DefaultPfsBaseServiceImpl.class);

    @Resource(name = "pfsBaseService")
    private PfsBaseService pfsBaseService;

    @Override
    public List<Province> queryProvInfos(boolean includeCityInfo, boolean includeAreaInfo)
                                                                                          throws ServiceException {
        try {
            return pfsBaseService.queryProvInfos(includeCityInfo, includeAreaInfo);
        } catch (BizException e) {
            logger.error("查询省市县信息异常:请求信息:包含市:{},包含地区:{}", includeCityInfo, includeAreaInfo);
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }
   
    /**
     * 查询可出款银行
     * @param cp
     * @return
     * @throws BizException
     */
    @Override
    public List<BankCard> queryBank(CropOrPersonal cp) throws ServiceException {
        try {
            List<BankCard> source = pfsBaseService.queryBank(cp);
            if(source != null && source.size() > 0) {
                return sortBank(source);
            } else {
                return source;
            }
        } catch (BizException e) {
            logger.error("查询可出款银行信息异常:请求信息:对公对私:{},城市代码:{}", cp);
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 查询分行信息
     *
     * @param appId
     * @param bankCode
     * @param cityId
     * @return
     * @throws BizException
     */
    @Override
    public List<BankBranch> queryBranch(String appId, String bankCode, long cityId)
                                                                                   throws ServiceException {
        try {
            return pfsBaseService.queryBranch(appId, bankCode, cityId);
        } catch (BizException e) {
            logger.error("查询省市县信息异常:请求信息:银行代码:{},城市代码:{}", bankCode, cityId);
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 查询分行信息
     *
     * @param appId
     * @param bankCode
     * @param cityId
     * @return
     * @throws BizException
     */
    @Override
    public List<BankBranch> queryBankBranch(String branchNo )throws ServiceException {
        try {
            return pfsBaseService.queryBankBranch(branchNo);
        } catch (BizException e) {
            logger.error("查询省市县信息异常:请求信息:银行代码:{},",branchNo);
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }
    /**
     * 校验卡信息
     *
     * @param appId
     * @param bankCode
     * @param cardNo
     * @return
     * @throws BizException
     */
    @Override
    public boolean cardValidate(String appId, String bankCode, String cardNo)
                                                                             throws ServiceException {
        try {
            return pfsBaseService.cardValidate(appId, bankCode, cardNo);
        } catch (BizException e) {
            logger.info("远程查询校验卡信息异常，请求参数：应用={},银行={},卡号={}", appId, bankCode, cardNo);
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Map<String,String> queryCityInfos(boolean includeCityInfo, boolean includeAreaInfo, String provId)
                                                                                                     throws ServiceException {
        try {
            return pfsBaseService.queryCityInfos(includeCityInfo, includeAreaInfo, provId);
        } catch (BizException e) {
            logger.error("查询省市县信息异常:请求信息:包含市:{},包含地区:{}", includeCityInfo, includeAreaInfo);
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public CardBin queryByCardNo(String cardNo, String appId) throws ServiceException {
        try {
            return pfsBaseService.queryByCardNo(cardNo, appId);
        } catch (BizException e) {
            logger.error("查询卡bin信息,卡号:{}", cardNo, e);
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    public List<BankCard> sortBank(List<BankCard> list) {
        List<BankCard> result = new ArrayList<BankCard>();
        Map<String, BankCard> map = new HashMap<String, BankCard>();
        for(BankCard card : list) {
            map.put(card.getBankId(), card);
        }
        if(map.get("ICBC") != null) {
            result.add(map.get("ICBC"));
            map.remove("ICBC");
        }
        if(map.get("ABC") != null) {
            result.add(map.get("ABC"));
            map.remove("ABC");
        }
        if(map.get("CMB") != null) {
            result.add(map.get("CMB"));
            map.remove("CMB");
        }
        if(map.get("COMM") != null) {
            result.add(map.get("COMM"));
            map.remove("COMM");
        }
        if(map.get("CCB") != null) {
            result.add(map.get("CCB"));
            map.remove("CCB");
        }
        if(map.get("BOC") != null) {
            result.add(map.get("BOC"));
            map.remove("BOC");
        }
        if(map.get("CEB") != null) {
            result.add(map.get("CEB"));
            map.remove("CEB");
        }
        if(map.get("PSBC") != null) {
            result.add(map.get("PSBC"));
            map.remove("PSBC");
        }
        if(map.get("SPDB") != null) {
            result.add(map.get("SPDB"));
            map.remove("SPDB");
        }
        if(map.get("CIB") != null) {
            result.add(map.get("CIB"));
            map.remove("CIB");
        }
        if(map.get("CMBC") != null) {
            result.add(map.get("CMBC"));
            map.remove("CMBC");
        }
        if(map.size() > 0) {
            for(String code : map.keySet()) {
                BankCard card = map.get(code);
                result.add(card);
            }
        }
        return result;
    }
}
