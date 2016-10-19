/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-ext-integration
 * @date 2014年7月22日
 */
package com.netfinworks.site.ext.integration.member.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.MemberContact;
import com.netfinworks.ma.service.base.model.MemberContactInfo;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.IMemberContactFacade;
import com.netfinworks.ma.service.request.MemberContactBatchRequest;
import com.netfinworks.ma.service.request.MemberContactRequest;
import com.netfinworks.ma.service.request.QueryFastMemberContactRequest;
import com.netfinworks.ma.service.request.QueryMemberContactRequest;
import com.netfinworks.ma.service.response.MemberContactBatchResponse;
import com.netfinworks.ma.service.response.MemberContactInfoResponse;
import com.netfinworks.ma.service.response.MemberContactResponse;
import com.netfinworks.ma.service.response.QueryMemberContactsResponse;
import com.netfinworks.site.ext.integration.member.MemberContactsService;

/**
 * 会员联系人服务实现
 * @author xuwei
 * @date 2014年7月22日
 */
@Service("memberContactsService")
public class MemberContactsServiceImpl implements MemberContactsService {
	private Logger logger = LoggerFactory.getLogger(MemberContactsServiceImpl.class);
	
	@Resource(name = "memberContactFacade")
	private IMemberContactFacade memberContactFacade;
	
	@Override
	public List<MemberContactInfo> queryContacts(String memberId, int contactType, OperationEnvironment env) {
		List<MemberContactInfo> mcList = null;
		
		QueryMemberContactRequest request = new QueryMemberContactRequest();
		request.setMemberId(memberId);
		if ((contactType == 0) || (contactType == 1)) {
			request.setContactType(Long.valueOf(contactType));
		}
		QueryMemberContactsResponse resp = memberContactFacade.queryContacts(env, request);
		
		if (resp != null) {
			mcList = resp.getContactInfo();
			if (mcList != null) {
				logger.debug("查询到{}个联系人", mcList.size());
			}
			
			if (!"0".equals(resp.getResponseCode())) {
				logger.error("查询联系人失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
			}
		}
		
		return mcList;
	}

	@Override
	public MemberContactInfo querySingleContact(String contactId, OperationEnvironment env) {
		MemberContactInfoResponse resp = memberContactFacade.queryContactById(env, contactId);
		if (resp != null) {
			if (!"0".equals(resp.getResponseCode())) {
				logger.error("查询联系人失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
				return null;
			}
			return resp.getContactInfo();
		}
		return null;
	}

	@Override
	public String createContact(MemberContact memberContact, OperationEnvironment env) {
		MemberContactRequest request = new MemberContactRequest();
		request.setContactInfo(memberContact);
		MemberContactResponse resp = memberContactFacade.createContact(env, request);
		logger.info("添加联系人请求：" + request);
		if (resp != null) {
			if (!"0".equals(resp.getResponseCode())) {
				logger.error("添加联系人失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
				if ("806".equals(resp.getResponseCode())) {
					return "contact_is_exist";
				}
				return null;
			}
			logger.info("添加联系人响应：" + resp);
			return resp.getContactId();
		}
		
		return null;
	}

	@Override
	public void batchCreateContact(List<MemberContact> mcList, String contactType, OperationEnvironment env)
			throws Exception {
		MemberContactBatchRequest request = new MemberContactBatchRequest();
		request.setContactInfos(mcList);
		request.setContactType(Long.valueOf(contactType));
		
		MemberContactBatchResponse resp = memberContactFacade.batchCreateContact(env, request);
		if (resp != null) {
			if (!"0".equals(resp.getResponseCode())) {
				logger.error("添加联系人失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
				Map<Integer, String> paramErrorMap = resp.getParamErrorMap();
				for (Map.Entry<Integer, String> error : paramErrorMap.entrySet()) {
					logger.error("错误Key：{}，内容：", error.getKey(), error.getValue());
				}
				throw new Exception("添加联系人失败");
			}
		}
	}

	@Override
	public Map<Integer, String> batchCreateContactWithError(List<MemberContact> mcList, String contactType,
			OperationEnvironment env) throws Exception {
		MemberContactBatchRequest request = new MemberContactBatchRequest();
		request.setContactInfos(mcList);
		request.setContactType(Long.valueOf(contactType));

		MemberContactBatchResponse resp = memberContactFacade.batchCreateContact(env, request);
		if (resp != null) {
			if (!"0".equals(resp.getResponseCode())) {
				logger.error("添加联系人失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
				Map<Integer, String> paramErrorMap = resp.getParamErrorMap();
				for (Map.Entry<Integer, String> error : paramErrorMap.entrySet()) {
					logger.error("错误Key：{}，内容：", error.getKey(), error.getValue());
				}
				return paramErrorMap;
			}
		}

		return null;
	}

	@Override
	public boolean deleteContact(String contactId, OperationEnvironment env) {
		Response resp = memberContactFacade.deleteContact(env, contactId);
		if (resp != null) {
			if (!"0".equals(resp.getResponseCode())) {
				logger.error("添加联系人失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
				return false;
			}
		}
		return true;
	}

	@Override
	public List<MemberContactInfo> fastQueryContact(String memberId,
			int contactType, int queryCode, int orderByCode, String value, OperationEnvironment env) {
		QueryFastMemberContactRequest request = new QueryFastMemberContactRequest();
		request.setMemberId(memberId);
		request.setContactType(Long.valueOf(contactType));
		request.setQueryCode(queryCode);
		request.setOrderByCode(orderByCode);
		request.setValue(value);
		
		QueryMemberContactsResponse resp = memberContactFacade.fastQueryContact(env, request);
		if (resp != null) {
			if (!"0".equals(resp.getResponseCode())) {
				logger.error("模糊查询联系人信息失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
				return null;
			}
			return resp.getContactInfo();
		}
		
		return null;
	}

	@Override
	public boolean updateContact(MemberContact memberContact,
			OperationEnvironment env) {
		MemberContactInfoResponse mcInfoResp = memberContactFacade.queryContactById(env, memberContact.getContactId());
		if (mcInfoResp != null) {
			MemberContactInfo mcInfo = mcInfoResp.getContactInfo();
			MemberContact mcBean = new MemberContact();
			try {
				BeanUtils.copyProperties(mcBean, mcInfo);
			} catch (Exception e) {
				logger.error("属性拷贝失败", e);
			}
			
			if ("0".equals(mcInfo.getContactType())) {
				mcBean.setContactIdentity(memberContact.getContactIdentity());
				mcBean.setMemo(memberContact.getMemo());
				if(!StringUtils.equals(mcInfo.getContactIdentity(), mcBean.getContactIdentity())){
					if(CollectionUtils.isNotEmpty(this.fastQueryContact(mcInfo.getMemberId(), 0, 2, 0, mcBean.getContactIdentity(), env))){
						return false;
					}
				}
			} else {
				mcBean.setAccountNo(memberContact.getAccountNo());
				mcBean.setBankName(memberContact.getBankName());
				mcBean.setProvince(memberContact.getProvince());
				mcBean.setCity(memberContact.getCity());
				mcBean.setMobile(memberContact.getMobile());
				mcBean.setBankBranch(memberContact.getBankBranch());
				if(!StringUtils.equals(mcInfo.getAccountNo(), mcBean.getAccountNo())){
					if(CollectionUtils.isNotEmpty(this.fastQueryContact(mcInfo.getMemberId(), 1, 4, 0, mcBean.getAccountNo(), env))){
						return false;
					}
				}
			}
			mcBean.setMemo(memberContact.getMemo());
			mcBean.setCardType(1);// 1-借记卡，目前只支持借记卡
			
			MemberContactRequest request = new MemberContactRequest();
			request.setContactInfo(mcBean);
			Response resp = memberContactFacade.updateContact(env, request);
			if ((resp != null) && "0".equals(resp.getResponseCode())) {
				return true;
			} else {
				logger.error("更新联系人信息失败，原因：{},错误编号：{}", resp.getResponseMessage(), resp.getResponseCode());
			}
		} else {
			logger.error("没有找到要修改的联系人");
		}
		
		return false;
	}
	
}
