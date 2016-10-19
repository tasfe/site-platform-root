package com.netfinworks.site.ext.integration.pfs.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.netfinworks.pfs.service.basis.baseinfo.ProvCityQueryFacade;
import com.netfinworks.pfs.service.basis.baseinfo.ProvQueryRequest;
import com.netfinworks.pfs.service.basis.branchinfo.BranchQueryByNoRequest;
import com.netfinworks.pfs.service.basis.branchinfo.BranchQueryByRelatedIdRequest;
import com.netfinworks.pfs.service.basis.branchinfo.BranchQueryFacade;
import com.netfinworks.pfs.service.basis.cardbin.CardBinRequest;
import com.netfinworks.pfs.service.basis.cardbin.CardBinResponse;
import com.netfinworks.pfs.service.basis.cardbin.CardBinValidateFacade;
import com.netfinworks.pfs.service.basis.common.QueryResult;
import com.netfinworks.pfs.service.basis.domain.BranchInfo;
import com.netfinworks.pfs.service.basis.domain.CardBinInfo;
import com.netfinworks.pfs.service.basis.domain.FundOutInst;
import com.netfinworks.pfs.service.basis.domain.ProvInfo;
import com.netfinworks.pfs.service.basis.inst.FundOutInstQueryRequest;
import com.netfinworks.pfs.service.basis.inst.InstQueryFacade;
import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.pfs.PfsBaseService;
import com.netfinworks.site.ext.integration.pfs.convert.PfsConvert;

/**
 *
 * <p>支付前置</p>
 * @author qinde
 * @version $Id: PfsBaseServiceImpl.java, v 0.1 2013-11-28 上午10:41:12 qinde Exp $
 */
@Service("pfsBaseService")
public class PfsBaseServiceImpl extends ClientEnvironment implements PfsBaseService {

    private Logger                logger = LoggerFactory.getLogger(PfsBaseServiceImpl.class);

    @Resource(name = "provCityQueryFacade")
    private ProvCityQueryFacade   provCityQueryFacade;

    @Resource(name = "branchQueryFacade")
    private BranchQueryFacade     branchQueryFacade;

    @Resource(name = "cardBinValidateFacade")
    private CardBinValidateFacade cardBinValidateFacade;
    @Resource(name = "instQueryFacade")
    private InstQueryFacade instQueryFacade;

    /**
     * 查询省市县信息
     *
     * @param includeCityInfo
     * @param includeAreaInfo
     * @return
     * @throws BizException
     */
    @Override
    public List<Province> queryProvInfos(boolean includeCityInfo, boolean includeAreaInfo)
                                                                                          throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("远程查询省市县信息，请求参数：是否查市={},是否查地区={}", includeCityInfo, includeAreaInfo);
            beginTime = System.currentTimeMillis();
        }

        try {
            ProvQueryRequest paramProvQueryRequest = new ProvQueryRequest();
            paramProvQueryRequest.setIncludeAreaInfo(includeAreaInfo);
            paramProvQueryRequest.setIncludeCityInfo(includeCityInfo);
            ProvQueryRequest request = PfsConvert.createProvQueryRequest(includeAreaInfo,
                includeCityInfo);
            QueryResult<ProvInfo> result = provCityQueryFacade.queryProvInfos(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger
                    .info("远程查询省市县信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
            return PfsConvert.convertProv(result.getResults());
        } catch (Exception e) {
            logger.error("查询省市县信息错误", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
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
                                                                                   throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("远程查询分行信息，请求参数：银行={},城市={}", bankCode, cityId);
            beginTime = System.currentTimeMillis();
        }

        try {
            BranchQueryByRelatedIdRequest request = PfsConvert.createBranchQueryByRelatedIdRequest(
                appId, bankCode, cityId);
            QueryResult<BranchInfo> result = branchQueryFacade.queryByRelatedId(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询分行信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
            return PfsConvert.convertBranch(result.getResults());
        } catch (Exception e) {
            logger.error("查询分行信息错误", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 查询分行信息
     *
     * @param 
     * @param 
     * @param branchNo
     * @return
     * @throws BizException
     */
       @Override
      public List<BankBranch> queryBankBranch(String branchNo) throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("远程查询分行信息，请求参数：银行={}", branchNo);
            beginTime = System.currentTimeMillis();
        }

        try {
            BranchQueryByNoRequest request = PfsConvert.createBranchQueryByRelatedIdRequest(branchNo);
            QueryResult<BranchInfo> result = branchQueryFacade.queryByNo(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询分行信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
            return PfsConvert.convertBankBranch(result.getResults());
        } catch (Exception e) {
            logger.error("查询分行信息错误", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 查询可出款机构信息
     *
     * @param appId
     * @param bankCode
     * @param cityId
     * @return
     * @throws BizException
     */
    @Override
    public List<BankCard> queryBank(CropOrPersonal cp) throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("远程查询银行信息，请求参数：对公对私={}", cp);
            beginTime = System.currentTimeMillis();
        }

        try {
            FundOutInstQueryRequest request = new FundOutInstQueryRequest();
            request.setCompanyOrPersonal(cp.getCode());
            QueryResult<FundOutInst> result = instQueryFacade.querySupportFundOutInsts(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询银行信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
            return PfsConvert.convertBank(result.getResults());
        } catch (Exception e) {
            logger.error("查询银行信息错误", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
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
    public boolean cardValidate(String appId, String bankCode, String cardNo) throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            //打印日志 去掉卡号cardNo
            logger.info("远程查询校验卡信息，请求参数：应用={},银行={}", appId, bankCode);
            beginTime = System.currentTimeMillis();
        }

        try {
            CardBinRequest request = PfsConvert.convertCardBinRequest(appId, bankCode, cardNo);
            CardBinResponse response = cardBinValidateFacade.validate(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询卡信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (response.isSuccess()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("校验卡信息错误", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public CardBin queryByCardNo(String cardNo, String appId) throws BizException {
        logger.info("通过卡号查询卡bin信息cardNo:"+cardNo.toString());
        long beginTime = 0L;
        try {
            QueryResult<CardBinInfo> response = cardBinValidateFacade.queryByCardNo(cardNo, appId);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("通过卡号查询卡bin信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (response.isSuccess()) {
                List<CardBinInfo> list = response.getResults();
                if(list != null && list.size() > 0) {
                    CardBinInfo cardBinInfo = list.get(0);
                    CardBin cardBin = new CardBin();
                    BeanUtils.copyProperties(cardBinInfo, cardBin);
                    cardBin.setCardTypeName(Dbcr.getByCode(cardBin.getCardType()).getMessage());
                    return cardBin;
                }
                return null;
            } else {
                logger.error(response.getResultMessage());
                throw new BizException(ErrorCode.SYSTEM_ERROR,response.getResultMessage());
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("通过卡号查询卡bin信息失败：cardNo={} ", cardNo, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public Map<String,String> queryCityInfos(boolean includeCityInfo, boolean includeAreaInfo, String provId)
                                                                                                     throws BizException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("远程查询城市信息，请求参数：是否查市={},是否查地区={}", includeCityInfo, includeAreaInfo);
            beginTime = System.currentTimeMillis();
        }

        try {
            ProvQueryRequest paramProvQueryRequest = new ProvQueryRequest();
            paramProvQueryRequest.setIncludeAreaInfo(includeAreaInfo);
            paramProvQueryRequest.setIncludeCityInfo(includeCityInfo);
            ProvQueryRequest request = PfsConvert.createProvQueryRequest(includeAreaInfo,
                includeCityInfo);
            QueryResult<ProvInfo> result = provCityQueryFacade.queryProvInfos(request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger
                    .info("远程查询城市信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, result });
            }
            return PfsConvert.convertCity(result.getResults(),provId);
        } catch (Exception e) {
            logger.error("查询城市信息错误", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

}
