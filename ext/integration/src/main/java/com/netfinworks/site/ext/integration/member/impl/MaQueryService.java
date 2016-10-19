package com.netfinworks.site.ext.integration.member.impl;

import java.util.ArrayList;
import java.util.List;















import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.meidusa.fastjson.JSON;
import com.meidusa.toolkit.common.util.StringUtil;
import com.netfinworks.cert.service.facade.ICertFacade;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.cert.service.request.QueryAuthRequest;
import com.netfinworks.cert.service.response.QueryPageResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.ma.service.facade.IAccountFacade;
import com.netfinworks.ma.service.facade.IBankAccountFacade;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.facade.IOperatorFacade;
import com.netfinworks.ma.service.facade.IPayPwdFacade;
import com.netfinworks.ma.service.request.AccountQueryRequest;
import com.netfinworks.ma.service.request.BankAccountRequest;
import com.netfinworks.ma.service.request.CompanyMemberQueryRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.request.MemberIntegratedRequest;
import com.netfinworks.ma.service.response.AccountInfo;
import com.netfinworks.ma.service.response.AccountInfoResponse;
import com.netfinworks.ma.service.response.BankAccountInfoResponse;
import com.netfinworks.ma.service.response.BankAccountResponse;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.ma.service.response.CompanyMemberResponse;
import com.netfinworks.ma.service.response.IdentityInfo;
import com.netfinworks.ma.service.response.LoginNameInfo;
import com.netfinworks.ma.service.response.LoginNameResponse;
import com.netfinworks.ma.service.response.MemberIntegratedResponse;
import com.netfinworks.ma.service.response.OperatorInfoMultiResponse;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AccountTypeKind;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;
import com.netfinworks.site.domain.exception.ExtServiceBase;
import com.netfinworks.site.ext.integration.voucher.impl.WebServiceHelper;

/**
 *
 * <p>查询MA会员信息</p>
 * @author leelun
 * @version $Id: MaQueryService.java, v 0.1 2013-11-20 下午4:50:30 lilun Exp $
 */
@Service("maQueryService")
public class MaQueryService extends ExtServiceBase {

    Logger                logger                = LoggerFactory.getLogger(MaQueryService.class);
    //@Resource
    private IMemberFacade memberFacade;
    
    private IOperatorFacade operatorFacade;
    
    //@Resource(name = "bankAccountFacade")
    private IBankAccountFacade bankAccountFacade;
    //@Resource(name = "certFacade")
    private ICertFacade certFacade;
    
    private IAccountFacade accountFacade;
    

	public void setAccountFacade(IAccountFacade accountFacade) {
		this.accountFacade = accountFacade;
	}

	public void setOperatorFacade(IOperatorFacade operatorFacade) {
        this.operatorFacade = operatorFacade;
    }
    
    public void setBankAccountFacade(IBankAccountFacade bankAccountFacade) {
        this.bankAccountFacade = bankAccountFacade;
    }

    public void setMemberFacade(IMemberFacade memberFacade) {
        this.memberFacade = memberFacade;
    }

	public void setCertFacade(ICertFacade certFacade) {
		this.certFacade = certFacade;
	}
    /**
     * 用PartnerId查询商户信息
     * @param memberId
     * @throws CommonException 
     */
    public MemberIntegratedResponse queryMemberByPartnerId(String memberId) throws CommonException {
        getExtLogger().debug("[Scf->ma.queryMemberIntegratedInfoById]" + memberId);

        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMemberIdentity(memberId);
        memberInfo.setPlatformType("0");
        MemberIntegratedResponse queryResult = new MemberIntegratedResponse();
        if (StringUtil.isEmpty(memberId)) {
            return queryResult;
        }
        OperationEnvironment paramOperationEnvironment = WebServiceHelper.buildOpEnv();

        MemberIntegratedIdRequest paramMemberIntegratedIdRequest = new MemberIntegratedIdRequest();

        paramMemberIntegratedIdRequest.setMemberId(memberId);
        //查询账号
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(true);
        List<Long> accountTypes = new ArrayList<Long>();
        accountTypes.add(AccountTypeKind.COMPANY_BASE_ACCOUNT.getCode());
        accountTypes.add(AccountTypeKind.PERSON_BASE_ACCOUNT.getCode());
        accountTypes.add(AccountTypeKind.MERCHANT_BASE_ACCOUNT.getCode());
        accountRequest.setAccountTypes(accountTypes);
        paramMemberIntegratedIdRequest.setAccountRequest(accountRequest);

        long start = System.currentTimeMillis();
        queryResult = memberFacade.queryMemberIntegratedInfoById(
            paramOperationEnvironment, paramMemberIntegratedIdRequest);
        long end = System.currentTimeMillis();

        getExtLogger().debug("[ma->Scf](耗时：" + (end - start) + ")" + queryResult.toString());
        return queryResult;
    }

    /**
     * 查询MA，获取商户会员实名认证信息
     * @param bizMemberId
     * @return
     * @throws CommonException
     */
    public  CompanyMemberInfo queryCompanyMember(String bizMemberId)throws CommonException{
    	getExtLogger().debug("[Scf->ma.queryCompanyMember]" + bizMemberId);
    	CompanyMemberQueryRequest request = new CompanyMemberQueryRequest();
    	OperationEnvironment environment = new OperationEnvironment ();
    	CompanyMemberInfo memberInfo = new CompanyMemberInfo();
    	long start = System.currentTimeMillis();
    	request.setMemberId(bizMemberId);
    	CompanyMemberResponse resp =memberFacade.queryCompanyMember(environment, request);
    	long end = System.currentTimeMillis();
        getExtLogger().debug("(耗时：" + (end - start) + ")" + JSON.toJSONString(resp));
        if(resp!=null){
        	memberInfo =resp.getCompanyMemberInfo();
        }
		return memberInfo;
    	
    }
    /**
     * 查询认证状态
     *
     * @param info
     * @return
     * @throws CommonException
     */
    public AuthResultStatus queryAuthType(AuthInfoRequest info) throws CommonException {
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("查询认证信息，请求参数：{}", info);
            beginTime = System.currentTimeMillis();
        }
        AuthResultStatus status = AuthResultStatus.NOT_FOUND;
        QueryAuthRequest paramQueryAuthRequest= new QueryAuthRequest() ;
        paramQueryAuthRequest.setAuthType(info.getAuthType().getCode());
        paramQueryAuthRequest.setMemberId(info.getMemberId());
        paramQueryAuthRequest.setStartRow(1);
        paramQueryAuthRequest.setEndRow(1);
        paramQueryAuthRequest.setNeedDelete(false);
        paramQueryAuthRequest.setNeedQeryTotal(false);
        paramQueryAuthRequest.setNeedQueryAll(false);
        QueryPageResponse response = certFacade.queryList(paramQueryAuthRequest,
            getEnv(info.getClientIp()));
        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("远程查询认证信息， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
        }
        if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
            List<AuthInfo> list = response.getAuthList();
            if ((list != null) && (list.size() > 0)) {
                AuthInfo auth = list.get(0);
                if(auth.getResult() != null){
                    status = AuthResultStatus.getByCode(auth.getResult().getCode());
                }
            }
			if (status == AuthResultStatus.CHECK_PASS) {
				paramQueryAuthRequest.setAuthType(AuthType.BANK_CARD.getCode());
				response = certFacade.queryList(paramQueryAuthRequest, getEnv(info.getClientIp()));
				if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
					List<AuthInfo> list2 = response.getAuthList();
					if ((list2 != null) && (list2.size() > 0)) {
						AuthInfo auth = list2.get(0);
						if ((auth.getResult() != null) && (auth.getResult() == ResultStatus.CHECK_PASS)) {
							status = AuthResultStatus.PASS;
						}
					}
				}
			}
			return status;
        } else {
        	CommonException exp = CommonDefinedException.SYSTEM_ERROR;
        	throw exp;
        }
    }
    /**
     * 查詢MA，获取所有操作员信息
     * @param memberId
     * @return
     * @throws CommonException
     */
    public OperatorInfoMultiResponse queryOperators(String memberId)throws CommonException{
    	OperationEnvironment opEnv = WebServiceHelper.buildOpEnv();
        String opName = "";
        getExtLogger().debug("[Scf->Site.queryLoginName]" + memberId);
        long start = System.currentTimeMillis();
        OperatorInfoMultiResponse resp = operatorFacade.queryOperators(opEnv, memberId);
        long end = System.currentTimeMillis();
        getExtLogger().debug("(耗时：" + (end - start) + ")" + JSON.toJSONString(resp));
        
       if(resp==null){
            logger.error("调用queryOperators失败。" + resp.getResponseCode() + resp.getResponseMessage());
            CommonException exp = CommonDefinedException.SYSTEM_ERROR;
            exp.setMemo(resp.getResponseCode() + resp.getResponseMessage());
            throw exp;
        }
        return resp;
    	
    }
	/**
     * 用operatorId查询操作员登录名称
     * @param operatorId
     * @return
     * @throws CommonException
     */
    public String queryLoginName(String operatorId) throws CommonException {
        OperationEnvironment opEnv = WebServiceHelper.buildOpEnv();
        String opName = "";
        getExtLogger().debug("[Scf->Site.queryLoginName]" + operatorId);
        long start = System.currentTimeMillis();
        LoginNameResponse resp = operatorFacade.queryLoginNames(opEnv, operatorId);
        long end = System.currentTimeMillis();
        getExtLogger().debug("(耗时：" + (end - start) + ")" + JSON.toJSONString(resp));
        
        if("0".equals(resp.getResponseCode())){
            List<LoginNameInfo> rs = resp.getLoginNames();
            if(rs!=null&&rs.size()>0){
                opName = rs.get(0).getLoginName();
            }
        }else {
            logger.error("调用queryLoginNames失败。" + resp.getResponseCode() + resp.getResponseMessage());
            //CommonException exp = CommonDefinedException.SYSTEM_ERROR;
            //exp.setMemo(resp.getResponseCode() + resp.getResponseMessage());
            //throw exp;
        }
        return opName;
    }

    /**
     * 用memberId查询会员
     * @param memberId
     * @throws CommonException 
     */
    public MemberInfo queryMemberByMemberId(String memberId) throws CommonException {
        getExtLogger().debug("[SCF->ma.queryMemberIntegratedInfoById]" + memberId);

        MemberInfo memberInfo = new MemberInfo();
        if (StringUtil.isEmpty(memberId)) {
            memberInfo.setMemberIdentity(memberId);
            memberInfo.setPlatformType("0");
            return memberInfo;
        }
        OperationEnvironment paramOperationEnvironment = WebServiceHelper.buildOpEnv();

        MemberIntegratedIdRequest paramMemberIntegratedIdRequest = new MemberIntegratedIdRequest();

        paramMemberIntegratedIdRequest.setMemberId(memberId);
        //查询账号
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(true);
        List<Long> accountTypes = new ArrayList<Long>();
        accountTypes.add(AccountTypeKind.COMPANY_BASE_ACCOUNT.getCode());
        accountTypes.add(AccountTypeKind.PERSON_BASE_ACCOUNT.getCode());
        accountTypes.add(AccountTypeKind.MERCHANT_BASE_ACCOUNT.getCode());
        accountRequest.setAccountTypes(accountTypes);
        paramMemberIntegratedIdRequest.setAccountRequest(accountRequest);

        long start = System.currentTimeMillis();
        MemberIntegratedResponse queryResult = memberFacade.queryMemberIntegratedInfoById(
            paramOperationEnvironment, paramMemberIntegratedIdRequest);
        long end = System.currentTimeMillis();

        getExtLogger().debug("[ma->Scf](耗时：" + (end - start) + ")" + queryResult.toString());
        parsequeryResult(memberInfo, queryResult);
        return memberInfo;
    }
    
    /**
     * 用memberId查询会员标识
     * @param memberId
     * @throws CommonException 
     */
    public String queryIdentityNoByMemberId(String memberId) throws CommonException {
        getExtLogger().debug("[Scf->ma.queryMemberIntegratedInfoById]" + memberId);
        String identityNo = "";
        if (StringUtil.isEmpty(memberId)) {
            return identityNo;
        }
        OperationEnvironment paramOperationEnvironment = WebServiceHelper.buildOpEnv();

        MemberIntegratedIdRequest paramMemberIntegratedIdRequest = new MemberIntegratedIdRequest();

        paramMemberIntegratedIdRequest.setMemberId(memberId);
        //查询账号
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(false);

        long start = System.currentTimeMillis();
        MemberIntegratedResponse queryResult = memberFacade.queryMemberIntegratedInfoById(
            paramOperationEnvironment, paramMemberIntegratedIdRequest);
        long end = System.currentTimeMillis();

        getExtLogger().debug("[ma->Scf](耗时：" + (end - start) + ")" + queryResult.toString());
        if("0".equals(queryResult.getResponseCode())){
            List<IdentityInfo> list = queryResult.getBaseMemberInfo().getIdentitys();
            if(list!=null && list.size()>0){
                for(IdentityInfo identityInfo : list){
                    if(identityInfo.getIdentityType()==1){
                        identityNo = identityInfo.getIdentity();
                        return identityNo;
                    }
                }
            }
        }else if("103".equals(queryResult.getResponseCode())){
            throw CommonDefinedException.PARTNER_ID_NOT_EXIST;
        }else{
            logger.error("查询合作者身份ID对应的会员。" + queryResult.getResponseCode() + queryResult.getResponseMessage());
            CommonException exp = CommonDefinedException.SYSTEM_ERROR;
            exp.setMemo(queryResult.getResponseCode() + queryResult.getResponseMessage());
            throw exp;
        }
        return identityNo;
    }

    /**
     * 用ID查询会员
     * @param memberIdentity
     * @param platformType 平台类型：1 uid; 2 mobile; 3 loginName; 4 company_id
     * @throws CommonException 
     */
    public MemberInfo queryMemberIntegratedInfo(String memberIdentity, String platformType) throws CommonException {
//        getExtLogger().debug(
//            "[Scf->ma.queryMemberIntegratedInfo]" + memberIdentity + "," + platformType);
        logger.info("[Scf->ma.queryMemberIntegratedInfo], memberIdentity:{}, platformType:{}", memberIdentity, platformType); 
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMemberIdentity(memberIdentity);
        memberInfo.setPlatformType(platformType);

        if (StringUtil.isEmpty(memberIdentity) || StringUtil.isEmpty(platformType)) {
            return memberInfo;
        }

        OperationEnvironment paramOperationEnvironment = WebServiceHelper.buildOpEnv();
        MemberIntegratedRequest paramMemberIntegratedRequest = new MemberIntegratedRequest();

        paramMemberIntegratedRequest.setMemberIdentity(memberIdentity);
        paramMemberIntegratedRequest.setPlatformType(platformType);
        paramMemberIntegratedRequest.setRequireDefaultOperator(true);

        //查询账号
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(true);
        List<Long> accountTypes = new ArrayList<Long>();
        accountTypes.add(AccountTypeKind.COMPANY_BASE_ACCOUNT.getCode());
        accountTypes.add(AccountTypeKind.PERSON_BASE_ACCOUNT.getCode());
        accountTypes.add(AccountTypeKind.MERCHANT_BASE_ACCOUNT.getCode());
        accountTypes.add(AccountTypeKind.BAOLIHU_BASE_ACCOUNT.getCode());
        accountRequest.setAccountTypes(accountTypes);
        paramMemberIntegratedRequest.setAccountRequest(accountRequest);

        long start = System.currentTimeMillis();
        MemberIntegratedResponse queryResult = memberFacade.queryMemberIntegratedInfo(
            paramOperationEnvironment, paramMemberIntegratedRequest);
        long end = System.currentTimeMillis();

        logger.info("[ma->Scf]耗时:{} (ms); 响应结果:{} ", new Object[] { (end - start), queryResult });
        if("0".equals(queryResult.getResponseCode())){
            parsequeryResult(memberInfo, queryResult);
        }else if("103".equals(queryResult.getResponseCode())){
        	CommonException exp = CommonDefinedException.MEMBER_ID_NOT_EXIST;
        	exp.setMemo("用户" + memberIdentity + "不存在");
            throw exp;
        }else{
            logger.error("查询会员。" + queryResult.getResponseCode() + queryResult.getResponseMessage());
            CommonException exp = CommonDefinedException.SYSTEM_ERROR;
            exp.setMemo(queryResult.getResponseCode() + queryResult.getResponseMessage());
            throw exp;
        }
        return memberInfo;
    }

    private void parsequeryResult(MemberInfo memberInfo, MemberIntegratedResponse queryResult) throws CommonException {
        if (queryResult != null && "0".equals(queryResult.getResponseCode())) {
            // 查询到了
            String memberId = queryResult.getBaseMemberInfo().getMemberId();
            memberInfo.setMemberId(memberId);
            memberInfo.setMemberType(queryResult.getBaseMemberInfo().getMemberType());
            memberInfo.setMemberName(queryResult.getBaseMemberInfo().getMemberName());
            
            String platformType=queryResult.getBaseMemberInfo().getIdentitys().get(0).getPlatformType();
            String memberIdentity=queryResult.getBaseMemberInfo().getIdentitys().get(0).getIdentity();
            memberInfo.setPlatformType(platformType);
            memberInfo.setMemberIdentity(memberIdentity);
            List<AccountInfo> accountList = queryResult.getAccountInfos();
            memberInfo.setAccounts(accountList);
            if (accountList != null && accountList.size() > 0) {
                AccountInfo account = accountList.get(0);
                memberInfo.setBaseAccountId(account.getAccountId());
            }else{
                throw CommonDefinedException.USER_ACCOUNT_NOT_EXIST;
            }
            
            if(queryResult.getDefaultOperator()!=null){
                memberInfo.setOperatorId(queryResult.getDefaultOperator().getOperatorId());
            }
        }
    }

    /**
     * 用ID查询会员
     * @param memberIdentity
     * @param platformType 平台类型：1 uid; 2 mobile; 3 loginName; 4 company_id
     * @param accountType
     * @throws CommonException 
     */
    public MemberInfo queryMemberIntegratedInfo(String memberIdentity, String platformType, String accountType) throws CommonException {
        getExtLogger().debug(
            "[Scf->ma.queryMemberIntegratedInfo]" + memberIdentity + "," + platformType + "," + accountType);
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMemberIdentity(memberIdentity);
        memberInfo.setPlatformType(platformType);

        if (StringUtil.isEmpty(memberIdentity) || StringUtil.isEmpty(platformType)) {
            return memberInfo;
        }

        OperationEnvironment paramOperationEnvironment = WebServiceHelper.buildOpEnv();
        MemberIntegratedRequest paramMemberIntegratedRequest = new MemberIntegratedRequest();

        paramMemberIntegratedRequest.setMemberIdentity(memberIdentity);
        paramMemberIntegratedRequest.setPlatformType(platformType);

        //查询账号
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(false);
        if(StringUtils.isNotBlank(accountType)){
        	accountRequest.setRequireAccountInfos(true);
        	List<Long> accountTypes = new ArrayList<Long>();
        	accountTypes.add(Long.valueOf(accountType));
            accountRequest.setAccountTypes(accountTypes);
        }
        paramMemberIntegratedRequest.setAccountRequest(accountRequest);

        long start = System.currentTimeMillis();
        MemberIntegratedResponse queryResult = memberFacade.queryMemberIntegratedInfo(
            paramOperationEnvironment, paramMemberIntegratedRequest);
        long end = System.currentTimeMillis();

        getExtLogger().debug(
            "[ma.queryMemberIntegratedInfo->Scf](耗时：" + (end - start) + ")"  + queryResult.toString());

        parsequeryResult(memberInfo, queryResult, accountType);
        return memberInfo;
    }

    private void parsequeryResult(MemberInfo memberInfo, MemberIntegratedResponse queryResult, String accountType) throws CommonException {
        if (queryResult != null && "0".equals(queryResult.getResponseCode())) {
            // 查询到了
            String memberId = queryResult.getBaseMemberInfo().getMemberId();
            memberInfo.setMemberId(memberId);
            memberInfo.setMemberName(queryResult.getBaseMemberInfo().getMemberName());

            List<AccountInfo> accountList = queryResult.getAccountInfos();
            memberInfo.setAccounts(accountList);
            if (accountList != null && accountList.size() > 0) {
                // 查询指定了账号类型，只应该查询到一个
                AccountInfo account = accountList.get(0);
                memberInfo.setBaseAccountId(account.getAccountId());
                memberInfo.setAccountInfo(accountType, account.getAccountId());
            }else{
                throw CommonDefinedException.USER_ACCOUNT_NOT_EXIST;
            }
        }
    }
    
    /**
     * 查询银行卡信息
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public List<BankAccountInfo> queryBankAccount(BankAccRequest request){
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询银行卡信息，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            BankAccountRequest req = new BankAccountRequest();
            req.setMemberId(request.getMemberId());
            req.setBankAccountNum(request.getBankAccountNum());
            
            BankAccountResponse response = bankAccountFacade.queryBankAccount(
                getEnv(request.getClientIp()), req);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询银行卡信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            return response.getBankAccountInfos();
    }
    
    /**
     * 查询银行卡详细信息
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public BankAcctDetailInfo queryBankAccountDetail(BankAccRequest request,String bankcardId){
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询银行卡详细信息，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            
            BankAccountInfoResponse response = bankAccountFacade.queryBankAccountDetail(getEnv(request.getClientIp()), bankcardId);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询银行卡信息， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            return response.getBankAcctInfo();
    }
    
    public OperationEnvironment getEnv(String clientIp) {
        OperationEnvironment env = new OperationEnvironment();
        env.setClientId("Scf");
        env.setClientIp(clientIp);
        return env;
    }
    /**
     * 查询账户
     *
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public AccountInfoResponse queryAccountById(String accountId){
        long beginTime = 0L;
        if (logger.isInfoEnabled()) {
            logger.info("查询会员账户，请求参数：{}", accountId);
            beginTime = System.currentTimeMillis();
        }
        OperationEnvironment env = WebServiceHelper.buildOpEnv();
        AccountInfoResponse response = accountFacade.queryAccountById(env, accountId);
        if (logger.isInfoEnabled()) {
            long consumeTime = System.currentTimeMillis() - beginTime;
            logger.info("远程查询会员余额， 耗时:{} (ms); 响应结果:{} ",
                new Object[] { consumeTime, response });
        }
        return response;
    }
}
