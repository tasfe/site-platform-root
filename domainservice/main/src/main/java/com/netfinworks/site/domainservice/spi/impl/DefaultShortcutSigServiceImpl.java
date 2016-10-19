package com.netfinworks.site.domainservice.spi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kjt.unionma.core.common.utils.RadomUtil;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.cert.service.facade.ICertFacade;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.cert.service.request.AuthVerifyRequest;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.ma.service.response.MemberVerifyLevelResponse;
import com.netfinworks.payment.common.v2.enums.PayMode;
import com.netfinworks.schema.cmf.enums.CertType;
import com.netfinworks.schema.cmf.enums.Dbcr;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultShortcutSigService;
import com.netfinworks.site.ext.integration.member.CertService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.ShortcutSigService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.yongda.site.service.personal.facade.request.PersonalShortcutSigRequest;
import com.yongda.site.service.personal.facade.request.PersonalSignAdvanceRequest;
import com.yongda.supermag.core.common.utils.OperatEnvironment;
import com.yongda.supermag.facade.enums.PlatformType;
import com.yongda.supermag.facade.enums.SMSSendType;
import com.yongda.supermag.facade.model.WebAccessInfo;
import com.yongda.supermag.facade.request.AgreementAdvanceRequest;
import com.yongda.supermag.facade.request.AgreementRequest;
import com.yongda.supermag.facade.response.AgreementAdvanceResponse;
import com.yongda.supermag.facade.response.AgreementResponse;

@Service("defaultShortcutSigService")
public class DefaultShortcutSigServiceImpl implements DefaultShortcutSigService{
    protected Logger log = LoggerFactory.getLogger(getClass());
    private static final String MY_PARTNERID = "ydPartnerId";
	@Resource(name = "shortcutSigService")
    private ShortcutSigService shortcutSigService;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	
	@Resource(name = "certService")
    private CertService certService;
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesService;
	
	@Resource(name = "certFacade")
	private ICertFacade certFacade;
	
	@Override
	public AgreementResponse ShortcutSig(PersonalShortcutSigRequest request, OperatEnvironment env)
			throws ServiceException {
		log.info("签约银行卡信息参数："+request.toString());
        try {
        	AgreementRequest req = createBankAccInfoRequest(request);//生成签约请求实体类
        	AgreementResponse response = shortcutSigService.sign(req, env);
        	return response;
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage());
        }
	}
	
	public AgreementRequest createBankAccInfoRequest(PersonalShortcutSigRequest request){
		AgreementRequest req = new AgreementRequest();
		WebAccessInfo webif = new WebAccessInfo();
		webif.setRequestIp("localhost");
		req.setAcessInfo(webif);//带定
		req.setCardNo(request.getCardNo());
		req.setCertNo(request.getCertNo());
		req.setCertType(CertType.ID_CARD);
		req.setCvv2(request.getCvv2());//可为空
		req.setDbcr(DbcrConvert(request.getDbcr()));
		req.setMemberId(request.getMemberId());
		req.setName(request.getName());
		req.setPartnerId(request.getPartnerId());
		req.setPayMode(PayMode.QUICKPAY);
		req.setPhoneNo(request.getPhoneNo());
		req.setPlatformType(PlatformType.DEFAULE);
		req.setRequestId(request.getRequestId());
		req.setSmsSendType(SMSSendType.PLATFORM_SEND);
		req.setTargetInstName(request.getTargetInstName());
		req.setValidDate(request.getValidDate());//可为空
		req.setTargetInst(request.getTargetInst());
		req.setOperatorId(request.getOperatorId());//可为空
		//req.setExtensions(extensions);
		//req.setGmtSubmit(gmtSubmit);
		return req;
	}
	
	public Dbcr DbcrConvert(String dbcrVal){
		if("DC".equalsIgnoreCase(dbcrVal)){
			return Dbcr.DC;
		}else if("CC".equalsIgnoreCase(dbcrVal)){
			return Dbcr.CC;
		}else if("GC".equalsIgnoreCase(dbcrVal)){
			return Dbcr.GC;
		}
		return null;
	}

	@Override
	public AgreementAdvanceResponse signAdvance(PersonalSignAdvanceRequest req,
			OperatEnvironment env) throws ServiceException {
		try {
			AgreementAdvanceRequest request = new AgreementAdvanceRequest();
			WebAccessInfo webif = new WebAccessInfo();
			webif.setRequestIp("localhost");
			request.setToken(req.getToken());
			request.setSmsCode(req.getSmsCode());
			request.setAcessInfo(webif);
			request.setRequestId(RadomUtil.getDatetimeId());
			request.setPartnerId(MY_PARTNERID);
			AgreementAdvanceResponse response = shortcutSigService.signAdvance(request, env);
			
			//查询银行卡信息
			BankAcctDetailInfo bankAcctDetailInfo = defaultBankAccountService.queryBankAccountDetail(response.getExtensions().getValue("bankCardId"));
						
		  	AuthInfoRequest authInfoReq = new AuthInfoRequest();
    		authInfoReq.setMemberId(bankAcctDetailInfo.getMemberId());
    		authInfoReq.setAuthType(AuthType.IDENTITY);
    		authInfoReq.setOperator(StringUtils.EMPTY);
    		//查询认证信息
    		AuthInfo info = defaultCertService.queryRealName(authInfoReq);
    		MemberVerifyLevelResponse verifyLevelResponse = memberService.getMemberVerifyLevel(bankAcctDetailInfo.getMemberId(),env);
    		String verify = verifyLevelResponse.getVeriyLevel();
    		String identitySts = null;
    		if ((info != null) && (info.getResult()!=null)) {
    			identitySts = info.getResult().getCode();
    		}
    		if (StringUtils.isBlank(identitySts) || (!ResultStatus.CHECK_PASS.getCode().equals(identitySts) && !ResultStatus.AUDIT_PASS.getCode().equals(identitySts))) {
    			AuthVerifyRequest authReq = new AuthVerifyRequest();
    			authReq.setMemberId(bankAcctDetailInfo.getMemberId());
    			authReq.setIdCard(uesService.getDataByTicket(bankAcctDetailInfo.getCertNum()));//证件号
    			authReq.setRealName(bankAcctDetailInfo.getRealName());//真实姓名
    			authReq.setBankCard(uesService.getDataByTicket(bankAcctDetailInfo.getBankAccountNum()));//银行卡号
    			authReq.setIdType(bankAcctDetailInfo.getCertType());//证件类型
    			authReq.setOperator("supper");
    			//设置绑定卡id
    			Map<String, String> map = new HashMap<String, String>();
    			map.put("bankAccountId", bankAcctDetailInfo.getBankcardId());
    			JSONObject jsonObject = (JSONObject) JSONObject.toJSON(map);
    			authReq.setExtension(jsonObject.toJSONString());
    			certFacade.authVerify(authReq, env);
    			return response;
			}
    		
    		if(ResultStatus.CHECK_PASS.getCode().equals(identitySts) || ResultStatus.AUDIT_PASS.getCode().equals(identitySts)){
    			AuthInfoRequest authInfo = new AuthInfoRequest();
	    		authInfo.setMemberId(bankAcctDetailInfo.getMemberId());
	    		authInfo.setOperator(StringUtils.EMPTY);
	    		authInfo.setAuthType(AuthType.IDENTITY);
    			// 认证订单号列表，用于更改认证状态
    			List<String> orderNoList = new ArrayList<String>();
    			orderNoList.add(info.getOrderNo());
    			authInfoReq.setOrderNoList(orderNoList);
    			authInfoReq.setChecked(true);
    			//审批认证 
    			defaultCertService.verify(authInfoReq);
    			
    			try {
        			memberService.updateMemberVerifyLevel(bankAcctDetailInfo.getMemberId(), CertifyLevel.CERTIFY_V1.getCode(),
        					new OperationEnvironment());
        		} catch (BizException e) {
        			log.error("更新会员实名认证级别失败：" + e.getMessage());
        		}
    		}
            return response;
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage());
		}
	}
}
