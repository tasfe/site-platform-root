package com.netfinworks.site.domainservice.pfs;

import java.util.List;
import java.util.Map;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domain.exception.BizException;

/**
 *
 * <p>查询省市县信息</p>
 *
 * @author qinde
 * @version $Id: PfsBaseService.java, v 0.1 2013-11-28 上午10:49:58 qinde Exp $
 */
public interface DefaultPfsBaseService {

    /**
     * 查询省市县信息
     *
     * @param includeCityInfo
     * @param includeAreaInfo
     * @return
     * @throws BizException
     */
    public List<Province> queryProvInfos(boolean includeCityInfo, boolean includeAreaInfo)
                                                                                          throws ServiceException;

    /**
     * 查询城市信息
     *
     * @param includeCityInfo
     * @param includeAreaInfo
     * @return
     * @throws BizException
     */
    public Map<String, String> queryCityInfos(boolean includeCityInfo, boolean includeAreaInfo,
                                              String provId) throws ServiceException;

    /**
     * 查询可出款银行
     * @param cp
     * @return
     * @throws BizException
     */
    public List<BankCard> queryBank(CropOrPersonal cp) throws ServiceException;

    /**
     * 查询分行信息
     *
     * @param appId
     * @param bankCode
     * @param cityId
     * @return
     * @throws BizException
     */
    public List<BankBranch> queryBranch(String appId, String bankCode, long cityId)
                                                                                   throws ServiceException;
    /**
     * 查询分行信息
     *
     * @param appId
     * @param bankCode
     * @param cityId
     * @return
     * @throws BizException
     */
    public List<BankBranch> queryBankBranch(String branchNo)throws ServiceException;
    /**
     * 校验卡信息
     *
     * @param appId
     * @param bankCode
     * @param cardNo
     * @return
     * @throws BizException
     */
    public boolean cardValidate(String appId, String bankCode, String cardNo)
                                                                             throws ServiceException;

    /**
     * 查询卡bin信息
     * @param cardNo
     * @param appId
     * @return
     * @throws ServiceException
     */
    public CardBin queryByCardNo(String cardNo, String appId) throws ServiceException;
}
