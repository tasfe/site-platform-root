/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.spi.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.meidusa.fastjson.TypeReference;
import com.netfinworks.basis.inf.ucs.support.annotation.KeyGenerator;
import com.netfinworks.basis.inf.ucs.support.annotation.UpdateCache;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.model.enums.CertValidateType;
import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.cert.service.request.CertValidateRequest;
import com.netfinworks.cert.service.response.BaseResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.ITrustUtil;
import com.netfinworks.site.domain.domain.autoFundout.AutoFundoutOrder;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.response.VerifyAmountResponse;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CertType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.autoFundout.AutoFundoutServiceImpl;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.integration.member.BankAccountService;
import com.netfinworks.site.ext.integration.member.CertService;
import com.netfinworks.site.ext.integration.member.ICertValidateService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;

/**
 * 通用说明：认证接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-21 下午12:17:27
 *
 */
@Service("defaultCertService")
public class DefaultCertServiceImpl implements DefaultCertService {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Resource(name = "certService")
    private CertService certService;

	@Resource(name = "iCertValidateService")
	private ICertValidateService iCertValidateService;

	@Resource(name = "bankAccountService")
	private BankAccountService bankAccountService;

	@Resource(name = "memberService")
	private MemberService memberService;

	@Resource(name = "uesService")
	private UesServiceClient uesService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "autoFundoutService")
	private AutoFundoutServiceImpl autoFundoutServiceImpl;

	@Value("${autoFundoutGatewayUrl}")
	private String gatewayUrl;// 网关地址

	@Value("${autoFundoutMemberId}")
	private String autoFundoutMemberId;// 自动打款使用的商户ID

	@Value("${autoFundoutMemberIdentity}")
	private String autoFundoutMemberIdentity;// 自动打款使用的商户标识

	@Value("${autoFundoutNotifyUrl}")
	private String autoFundoutNotifyUrl;// 自动打款异步通知地址

	@Value("${autoFundoutPfxFileName}")
	private String autoFundoutPfxFileName;// 自动打款商户PFX证书路径

	@Value("${autoFundoutCerFileName}")
	private String autoFundoutCerFileName;// 自动打款商户CER证书路径

	@Value("${autoFundoutKeyPassword}")
	private String autoFundoutKeyPassword;// 自动打款商户证书密码

    /**
     * 实名认证
     *
     * @param ctx
     * @return
     * @throws ServiceException
     */
    @Override
    public boolean certification(AuthInfoRequest info) throws ServiceException {
        try {
            //调认证接口
            certService.auth(info);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
			throw new ServiceException("实名认证信息提交失败！");
        }
        return true;
    }

    /**
     * 查询认证信息
     *
     * @param info
     * @return
     * @throws BizException
     */
    @Override
    public AuthInfo queryRealName(AuthInfoRequest info) throws ServiceException {
        try {
            return certService.queryRealName(info);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
			throw new ServiceException("查询实名认证信息失败！");
        }
    }

    /**
     * 查询认证状态
     *
     * @param info
     * @return
     * @throws BizException
     */
    @Override
    public AuthResultStatus queryAuthType(AuthInfoRequest info) throws ServiceException {
        try {
            return certService.queryAuthType(info);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

	/**
	 * 更改实名认证状态
	 * 
	 * @param info
	 * @return
	 * @throws BizException
	 */
	@Override
	public boolean verify(AuthInfoRequest info) throws ServiceException {
		try {
			BaseResponse response = certService.verify(info);
			if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				return true;
			}
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
		}
		return false;
	}

	/**
	 * 身份验证
	 */
	@Override
	public boolean verifyRealName(String memberId, String operatorId, String realName, String idCard, String idType,
			OperationEnvironment environment) throws ServiceException {
		// 1.提交身份认证申请
		AuthInfoRequest info = new AuthInfoRequest();
		info.setMemberId(memberId);
		info.setOperator(StringUtils.EMPTY);
		info.setAuthType(AuthType.IDENTITY);
		info.setCertType(idType);
		info.setAuthNo(idCard);
		info.setAuthName(realName);
		info.setClientIp(environment.getClientId());
		String orderNo = "";// 认证订单号
		try {
			orderNo = certService.auth(info);
		} catch (BizException e) {
			log.error("CERT服务认证失败：" + e.getMessage());
			throw new ServiceException(e.getMessage());
		}

		if (!CertType.ID_CARD.getCode().equals(idType)) {
			return true;
		}

		// 认证订单号列表，用于更改认证状态
		List<String> orderNoList = new ArrayList<String>();
		orderNoList.add(orderNo);

		// 2.调用国政通接口验证姓名和身份证号是否匹配
		CertValidateRequest cvr = new CertValidateRequest();
		cvr.setCertType(CertValidateType.ID_CARD);
		cvr.setIdentityCard(idCard);
		cvr.setRealName(realName);

		try {
			iCertValidateService.validate(environment, cvr);
		} catch (BizException e) {
			log.error("国政通接口认证失败：" + e.getMessage());
			// 国政通验证失败修改状态为checkReject
			modifyAuthStatus(orderNoList, "国政通", true, false);
			throw new ServiceException("用户名与身份证不一致!");
		}

		// 3.国政通验证通过后修改状态为checkPass
		modifyAuthStatus(orderNoList, "国政通", true, true);
		
		// 4.调用MA接口更新会员实名认证级别
		try {
			memberService.updateMemberVerifyLevel(memberId, CertifyLevel.CERTIFY_V0.getCode(), environment);
		} catch (BizException e) {
			log.error("更新会员实名认证级别失败：" + e.getMessage());
		}

		// 5.更新会员名称为真实姓名memberName
		PersonMember personMember = new PersonMember();
		personMember.setMemberId(memberId);
		personMember.setStrRealName(realName);
		personMember.setMemberName(realName);
		try {
			if (!memberService.setPersonalMemberInfo(personMember, environment)) {
				throw new ServiceException("设置真实姓名失败！");
			}
		} catch (BizException e) {
			log.error("设置真实姓名失败：" + e.getMessage());
			throw new ServiceException(e.getMessage());
		}

		return true;
	}

	/**
	 * 银行卡验证
	 */
	@Override
	public boolean verifyBankCard(BankAccRequest bankAccRequest, OperationEnvironment environment)
			throws ServiceException {
		try{

			// 银行卡验证提交次数限制
			int verifyCount = 3;// 最多提交次数
			String cacheKey = CommonConstant.VERIFY_AMOUNT + bankAccRequest.getBankAccountNum();
			if (StringUtils.isNotEmpty(payPassWordCacheService.load(cacheKey))) {
				verifyCount = Integer.valueOf(payPassWordCacheService.load(cacheKey));
			}

			// 银行卡验证提交次数超限则返回失败
			if (verifyCount == 0) {
				throw new ServiceException("银行卡验证提交次数超过限制!");
			}
			defaultCertService.saveOrUpdate(cacheKey, String.valueOf(--verifyCount));

			// 1.绑定银行卡
			// 查询银行卡是否已绑定，已绑定则解绑，为了能重复提交
			List<BankAccountInfo> list = bankAccountService.queryBankAccount(bankAccRequest);
			if ((list != null) && (list.size() > 0)) {
				bankAccRequest.setBankcardId(list.get(0).getBankcardId());
				bankAccountService.removeBankAccount(bankAccRequest);
			}
			String bankCardId = bankAccountService.addBankAccount(bankAccRequest);
			// 2.设置绑定的银行卡为默认银行卡
			if (StringUtils.isNotEmpty(bankCardId)) {
				BankAccRequest request = new BankAccRequest();
				request.setMemberId(bankAccRequest.getMemberId());
				request.setBankcardId(bankCardId);
				
                Map<String, String> map = new HashMap<String, String>();
                BankAcctDetailInfo info = bankAccountService.queryBankAccount(bankCardId);
                if (StringUtil.isNotBlank(info.getExtention())) {
                    Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                            new TypeReference<Map<String, String>>() {
                            });
                    for (String key : extMap.keySet()) {
                        map.put(key, extMap.get(key));  
                    }
                }
                map.put("isDefaultcard", "1");
				
				request.setExtInfo(JSONObject.toJSONString(map));
				bankAccountService.updateDefaultAccount(request);
			}
			// 3.提交银行卡认证申请
			AuthInfoRequest info = new AuthInfoRequest();
			info.setMemberId(bankAccRequest.getMemberId());
			info.setOperator(StringUtils.EMPTY);
			info.setAuthType(AuthType.BANK_CARD);
			info.setCertType(CertType.BANK_CARD_NO.getCode());
			info.setAuthNo(uesService.encryptData(bankAccRequest.getBankAccountNum()));
			info.setAuthName(bankAccRequest.getRealName());
			info.setClientIp(environment.getClientId());
			Map map = new HashMap();
			// 打款金额取1-100除以100
			Money money = new Money("" + (new Random().nextInt(99) + 1));
			map.put("amount", money.divide(100).toString());
			map.put("bankAccountId", bankCardId);
			info.setExt(JSONObject.toJSONString(map));

			String orderNo = "";
			try {
				orderNo = certService.auth(info);
			} catch (BizException e) {
				log.error("银行卡认证失败：" + e.getMessage());
				throw new ServiceException(e.getMessage());
			}
			// 保存认证服务返回的申请订单号
			List<String> orderNoList = new ArrayList<String>();
			orderNoList.add(orderNo);

			// 4.网关接口自动打款
			// 出款提交状态
			String status = CommonConstant.SUBMITTED_FAIL;

			// 生成外部订单号
			String outer_trade_no = UUID.randomUUID().toString().replaceAll("-", "");
			try {
				autoFundout(bankAccRequest, money, outer_trade_no);
				status = CommonConstant.WITHDRAWAL_SUBMITTED;
			} catch (Exception e) {
				log.error("自动打款调用网关接口失败：" + e.getMessage());
				modifyAuthStatus(orderNoList, "自动打款", false, false);
			}
			// 自动打款订单落地
			AutoFundoutOrder autoFundoutOrder = new AutoFundoutOrder();
			autoFundoutOrder.setOuterTradeNo(outer_trade_no);
			autoFundoutOrder.setAuthOrderNo(orderNo);
			autoFundoutOrder.setAmount(money.divide(100));
			autoFundoutOrder.setMemberId(bankAccRequest.getMemberId());
			autoFundoutOrder.setMemberIdentity(bankAccRequest.getMemberIdentity());
			autoFundoutOrder.setBankCardInfo(JSONObject.toJSONString(bankAccRequest));
			autoFundoutOrder.setWithdrawalStatus(status);
			autoFundoutOrder.setGmtCreated(new Date());
			try {
				autoFundoutServiceImpl.insertOrder(autoFundoutOrder);
			} catch (Exception e) {
				log.error("自动打款订单落地失败，商户订单号：" + outer_trade_no);
			}

		} catch (BizException e) {
			log.error("银行卡认证失败：" + e.getMessage());
			throw new ServiceException("银行卡认证失败！");
		}
		return true;
	}

	/**
	 * 打款验证
	 */
	@Override
	public VerifyAmountResponse verifyAmount(String memberId, String operatorId, String amount) throws ServiceException {
		try {
			VerifyAmountResponse response = new VerifyAmountResponse();
			// 1.查询银行卡验证信息--需在basis系统发起银行卡认证
			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(memberId);
			authInfoReq.setAuthType(AuthType.BANK_CARD);
			authInfoReq.setOperator(operatorId);

			AuthInfo info = certService.queryRealName(authInfoReq);
			if ((null == info) || StringUtils.isEmpty(info.getExtension())) {
				throw new ServiceException("未查询到银行卡认证信息！");
			}

			List<String> orderNoList = new ArrayList<String>();
			orderNoList.add(info.getOrderNo());

			// 2.检查打款验证是否已超过3次
			int verifyCount = 3;// 最多验证次数
			String cacheKey = CommonConstant.VERIFY_AMOUNT + memberId + info.getOrderNo();
			if (StringUtils.isNotEmpty(payPassWordCacheService.load(cacheKey))) {
				verifyCount = Integer.valueOf(payPassWordCacheService.load(cacheKey));
			}
			// 3.校验输入的打款金额是否正确
			Map map = JSONObject.parseObject(info.getExtension());
			String realAmount = (String) map.get("amount");
			if (!amount.equals(realAmount)) {
				if (verifyCount == 1) {
					modifyAuthStatus(orderNoList, info.getAuthOper(), true, false);
					payPassWordCacheService.clear(cacheKey);
					response.setRemainCount(verifyCount - 1);
					return response;
				}
				defaultCertService.saveOrUpdate(cacheKey, String.valueOf(--verifyCount));
				response.setRemainCount(verifyCount);
				return response;
			}
			// 4.更改银行卡认证状态为checkPass
			modifyAuthStatus(orderNoList, info.getAuthOper(), true, true);

			// 5.以前注册的会员,REAL_NAME已经通过,需要修改认证等级为3
			authInfoReq.setMemberId(memberId);
			authInfoReq.setAuthType(AuthType.REAL_NAME);
			authInfoReq.setOperator(operatorId);

			info = certService.queryRealName(authInfoReq);

			if ((null != info) && (ResultStatus.CHECK_PASS == info.getResult())) {
				try {
					memberService.updateMemberVerifyLevel(memberId, CertifyLevel.CERTIFY_V1.getCode(),
							new OperationEnvironment());
				} catch (BizException e) {
					log.error("更新会员实名认证级别失败：" + e.getMessage());
				}
			}

			response.setSuccess(true);
			return response;
		} catch (BizException e) {
			log.error("银行卡验证失败：" + e.getMessage());
			throw new ServiceException("银行卡验证失败！");
		}
	}

	/**
	 * 撤销银行卡认证
	 */
	@Override
	public boolean cancelBankVerify(String memberId, String operatorId) throws ServiceException {
		try {
			// 1.查询银行卡验证信息
			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(memberId);
			authInfoReq.setAuthType(AuthType.BANK_CARD);
			authInfoReq.setOperator(operatorId);

			AuthInfo info = certService.queryRealName(authInfoReq);
			if ((null == info) || StringUtils.isEmpty(info.getExtension())) {
				throw new ServiceException("未查询到银行卡认证信息！");
			}

			// 2.更改银行卡认证状态为checkReject
			List<String> orderNoList = new ArrayList<String>();
			orderNoList.add(info.getOrderNo());
			modifyAuthStatus(orderNoList, operatorId, true, false);

		} catch (BizException e) {
			log.error("撤销银行卡认证失败：" + e.getMessage());
			throw new ServiceException("撤销银行卡认证失败！");
		}
		return true;
	}

	/**
	 * 修改认证状态
	 * 
	 * @param orderNoList
	 * @param operatorId
	 * @param isChecked
	 * @param result
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public boolean modifyAuthStatus(List<String> orderNoList, String operatorId, boolean isChecked, boolean result)
			throws ServiceException {
		AuthInfoRequest info = new AuthInfoRequest();
		info.setOrderNoList(orderNoList);
		info.setOperator(operatorId);
		info.setChecked(isChecked);
		info.setResult(result);

		try {
			BaseResponse response = certService.verify(info);
			if (!ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				throw new ServiceException("更改认证状态失败！"+response.getReturnMessage());
			}
		} catch (BizException e) {
			log.error("更改认证状态失败：" + e.getMessage());
			throw new ServiceException(e.getMessage());
		}
		return true;
	}

	private void autoFundout(BankAccRequest bankAccRequest, Money money,String outer_trade_no) throws Exception {
		String inputCharset = "UTF-8";// 字符编码
		String signType = "ITRUSSRV";// 签名方式
		ITrustUtil iTrustUtil = new ITrustUtil(autoFundoutPfxFileName, autoFundoutCerFileName, autoFundoutKeyPassword);
		Map<String, String> sParaTemp = new TreeMap<String, String>();
		sParaTemp.put("service", "create_payment_to_card_auto");// 单笔付款到卡网关接口
		sParaTemp.put("version", "1.0");// 接口版本
		sParaTemp.put("partner_id", autoFundoutMemberId);// 合作者身份ID/商户号
		sParaTemp.put("_input_charset", inputCharset);// 字符集/参数编码字符集
		sParaTemp.put("sign", ""); // 签名--签约合作方的钱包唯一用户号 不可空
		sParaTemp.put("sign_type", signType);// 签名方式--只支持ITRUSSRV不可空
		sParaTemp.put("return_url", "");// 同步返回页面路径
		sParaTemp.put("memo", "实名认证-打款" + money.divide(100).toString() + "元 ：" + bankAccRequest.getMemberId());// 备注
		sParaTemp.put("outer_trade_no", outer_trade_no);// 商户网站唯一订单号
		sParaTemp.put("identity_no", autoFundoutMemberIdentity);// 会员标识号
		sParaTemp.put("identity_type", "1");// 会员标识平台类型固定值为1
		sParaTemp.put("payable_amount", money.divide(100).toString());// 付款金额
		sParaTemp.put("account_type", "301");// 账户类型 目前只支持基本户
		sParaTemp.put("card_no", iTrustUtil.encryptData(bankAccRequest.getBankAccountNum(), inputCharset));// 银行卡号
		sParaTemp.put("account_name", iTrustUtil.encryptData(bankAccRequest.getRealName(), inputCharset));// 银行卡账户名
		sParaTemp.put("bank_code", bankAccRequest.getBankCode());// 银行编码
		sParaTemp.put("bank_name", bankAccRequest.getBankName());// 银行名称
		sParaTemp.put("branch_name", bankAccRequest.getBranchName());// 银行分支行名称
		sParaTemp.put("bank_line_no", bankAccRequest.getBranchNo());// 银行分支行号
		sParaTemp.put("bank_prov", bankAccRequest.getProvName());// 银行所在省
		sParaTemp.put("bank_city", bankAccRequest.getCityName());// 银行所在市
		sParaTemp.put("company_or_personal", "C");// 对公对私：对公/B, 对私/C
		sParaTemp.put("fundout_grade", "1");// 到账级别：普通(0)，快速(1)
		sParaTemp.put("purpose", "实名认证自动打款");// 出款目的
		sParaTemp.put("submit_time", DateUtil.getLongDateString(new Date()));// 提交时间
		sParaTemp.put("notify_url", autoFundoutNotifyUrl);// 服务器异步通知页面路径

		Map<String, String> sParaMap = iTrustUtil.buildRequestPara(sParaTemp, signType, "", inputCharset);
		// 整理提交参数
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String key : sParaMap.keySet()) {
			params.add(new BasicNameValuePair(key, sParaMap.get(key)));
		}

		String resultString = Request.Post(gatewayUrl).bodyForm(params).execute().returnContent().asString();

		log.info("自动打款接口返回:" + resultString);

		if (StringUtils.isNotEmpty(resultString)) {
			String[] results = resultString.split("&");
			if (results[0].equals("is_success=F")) {
				throw new ServiceException("网关返回失败");
			}
		} else {
			throw new ServiceException("网关返回结果为空");
		}
	}

	@Override
	@UpdateCache(namespace = CommonConstant.WALLET_NAMESPACE_CACHE, expireSecond = 10 * 24 * 60 * 60)
	public String saveOrUpdate(@KeyGenerator String key, String member) {
		return member;

	}

}
