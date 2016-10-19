/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.pfs.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.netfinworks.pfs.service.basis.baseinfo.ProvQueryRequest;
import com.netfinworks.pfs.service.basis.branchinfo.BranchQueryByNoRequest;
import com.netfinworks.pfs.service.basis.branchinfo.BranchQueryByRelatedIdRequest;
import com.netfinworks.pfs.service.basis.cardbin.CardBinRequest;
import com.netfinworks.pfs.service.basis.domain.BranchInfo;
import com.netfinworks.pfs.service.basis.domain.CityInfo;
import com.netfinworks.pfs.service.basis.domain.FundOutInst;
import com.netfinworks.pfs.service.basis.domain.ProvInfo;
import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;

/**
 * 
 * <p>
 * 支付前置转换
 * </p>
 * 
 * @author qinde
 * @version $Id: BankConvertConvert.java, v 0.1 2013-11-29 下午1:11:02 qinde Exp $
 */
public class PfsConvert {

	/**
	 * 生成查询省市县request
	 * 
	 * @param includeAreaInfo
	 * @param includeCityInfo
	 * @return
	 */
	public static ProvQueryRequest createProvQueryRequest(
			boolean includeAreaInfo, boolean includeCityInfo) {
		ProvQueryRequest request = new ProvQueryRequest();
		request.setIncludeAreaInfo(includeAreaInfo);
		request.setIncludeCityInfo(includeCityInfo);
		return request;
	}

	/**
	 * 生成查询分行equest
	 * 
	 * @param appId
	 * @param bankCode
	 * @param cityId
	 * @return
	 */
	public static BranchQueryByRelatedIdRequest createBranchQueryByRelatedIdRequest(
			String appId, String bankCode, long cityId) {
		BranchQueryByRelatedIdRequest request = new BranchQueryByRelatedIdRequest();
		request.setAppId(appId);
		request.setBankCode(bankCode);
		request.setCityId(cityId);
		return request;
	}
	
	/**
	 * 生成查询分行request
	 * 
	 * @param appId
	 * @param bankCode
	 * @param cityId
	 * @return
	 */
	public static BranchQueryByNoRequest createBranchQueryByRelatedIdRequest(
			String branchNo) {
		BranchQueryByNoRequest request = new BranchQueryByNoRequest();
		request.setBranchNo(branchNo);
		return request;
	}

	/**
	 * 转换省市县信息
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<Province> convertProv(List<ProvInfo> list)
			throws IllegalAccessException, InvocationTargetException {
		if (list != null && list.size() > 0) {
			List<Province> result = new ArrayList<Province>();
			for (ProvInfo info : list) {
				Province province = new Province();
				BeanUtils.copyProperties(info, province);
				result.add(province);
			}
			return result;
		}
		return null;
	}

	/**
	 * 转换城市
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Map<String, String> convertCity(List<ProvInfo> list,
			String provId) throws IllegalAccessException,
			InvocationTargetException {
		Map<String, String> result = new HashMap<String, String>();
		if (list != null && list.size() > 0) {
			for (ProvInfo info : list) {
				if (provId.equals(String.valueOf(info.getProvId()))) {
					List<CityInfo> cityInfoList = info.getCityInfos();

					for (CityInfo cityInfo : cityInfoList) {
						result.put(String.valueOf(cityInfo.getCityId()),
								cityInfo.getCityName());
					}
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 转换银行信息
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<BankCard> convertBank(List<FundOutInst> list)
			throws IllegalAccessException, InvocationTargetException {
		if (list != null && list.size() > 0) {
			List<BankCard> result = new ArrayList<BankCard>();
			for (FundOutInst info : list) {
				BankCard bean = new BankCard();
				bean.setBankId(info.getBankCode());
				bean.setBankName(info.getBankName());
				result.add(bean);
			}
			return result;
		}
		return null;
	}

	/**
	 * 转换分行信息
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<BankBranch> convertBranch(List<BranchInfo> list)
			throws IllegalAccessException, InvocationTargetException {
		if (list != null && list.size() > 0) {
			List<BankBranch> result = new ArrayList<BankBranch>();
			for (BranchInfo info : list) {
				BankBranch bean = new BankBranch();
				String[] ignoreProperties = { "bankCode", "provName",
						"cityName" };
				BeanUtils.copyProperties(info, bean, ignoreProperties);
				result.add(bean);
			}
			return result;
		}
		return null;
	}
	/**
	 * 转换分行信息
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<BankBranch> convertBankBranch(List<BranchInfo> list)
			throws IllegalAccessException, InvocationTargetException {
		if (list != null && list.size() > 0) {
			List<BankBranch> result = new ArrayList<BankBranch>();
			for (BranchInfo info : list) {
				BankBranch bean = new BankBranch();
				String[] ignoreProperties = {"branchNo"};
				BeanUtils.copyProperties(info, bean, ignoreProperties);
				result.add(bean);
			}
			return result;
		}
		return null;
	}

	/**
	 * 生成卡bin校验请求
	 * 
	 * @param appId
	 * @param bankCode
	 * @param cityId
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static CardBinRequest convertCardBinRequest(String appId,
			String bankCode, String cardNo) {
		CardBinRequest request = new CardBinRequest();
		request.setAppId(appId);
		request.setBank(bankCode);
		request.setCardNo(cardNo);
		return request;
	}
}
