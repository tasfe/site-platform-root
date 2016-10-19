package com.netfinworks.site.ext.integration.pfs;

import java.util.List;
import java.util.Map;

import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domain.exception.BizException;

/**
 *
 * <p>支付前置远程服务</p>
 *
 * @author qinde
 * @version $Id: PfsBaseService.java, v 0.1 2013-11-28 上午10:49:58 qinde Exp $
 */
public interface PfsBaseService {

    /**
     * 查询省市县信息
     *
     * @param includeCityInfo
     * @param includeAreaInfo
     * @return
     * @throws BizException
     */
    public List<Province> queryProvInfos(boolean includeCityInfo, boolean includeAreaInfo)
                                                                                          throws BizException;

    public Map<String,String> queryCityInfos(boolean includeCityInfo, boolean includeAreaInfo, String provId)
                                                                                                     throws BizException;

    /**
     * 查询可出款银行
     * @param cp
     * @return
     * @throws BizException
     */
    public List<BankCard> queryBank(CropOrPersonal cp) throws BizException;

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
                                                                                   throws BizException;
    
    
    /**
     * 查询分行信息
     *
     * 
     * @param branchNo
     * @return
     * @throws BizException
     */
      public List<BankBranch> queryBankBranch(String branchNo) throws BizException;
    /**
     * 校验卡信息
     *
     * @param appId
     * @param bankCode
     * @param cardNo
     * @return
     * @throws BizException
     */
    public boolean cardValidate(String appId, String bankCode, String cardNo) throws BizException;

    /**
     * 查询卡bin
     * @param cardNo
     * @param appId
     * @return
     * @throws BizException
     */
    public CardBin queryByCardNo(String cardNo, String appId) throws BizException;
}
