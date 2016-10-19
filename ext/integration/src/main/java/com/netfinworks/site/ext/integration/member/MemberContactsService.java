/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-ext-integration
 * @date 2014年7月22日
 */
package com.netfinworks.site.ext.integration.member;

import java.util.List;
import java.util.Map;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.MemberContact;
import com.netfinworks.ma.service.base.model.MemberContactInfo;

/**
 * 会员联系人服务
 * @author xuwei
 * @date 2014年7月22日
 */
public interface MemberContactsService {
	/**
	 * 根据会员ID查询其对应的联系人
	 * @param memberId 会员ID
	 * @return 联系人列表
	 */
	public List<MemberContactInfo> queryContacts(String memberId, int contactType, OperationEnvironment env);
	
	/**
	 * 根据联系人ID查询唯一的联系人
	 * @param contactId 联系人ID
	 * @return 联系人对象
	 */
	public MemberContactInfo querySingleContact(String contactId, OperationEnvironment env);
	
	/**
	 * 添加联系人
	 * @param memberContact 联系人对象
	 * @return 联系人ID
	 */
	public String createContact(MemberContact memberContact, OperationEnvironment env);
	
	/**
	 * 删除联系人
	 * @param contactId 联系人ID
	 * @return true-成功，false-失败
	 */
	public boolean deleteContact(String contactId, OperationEnvironment env);
	
	/**
	 * 修改联系人
	 * @param memberContact 联系人信息
	 * @return
	 */
	public boolean updateContact(MemberContact memberContact, OperationEnvironment env);
	
	/**
	 * 批量添加联系人
	 * @param mcList 联系人列表
	 * @param contactType 0-系统账户, 1-银行卡账户
	 */
	public void batchCreateContact(List<MemberContact> mcList, String contactType, OperationEnvironment env)
			throws Exception;
	
	/**
	 * 批量添加联系人-返回错误集
	 * 
	 * @param mcList 联系人列表
	 * @param contactType 0-系统账户, 1-银行卡账户
	 */
	public Map<Integer, String> batchCreateContactWithError(List<MemberContact> mcList, String contactType,
			OperationEnvironment env) throws Exception;

	/**
	 * 模糊查询接口
	 * 
	 * @param memberId 会员ID
	 * @param contactType 联系人信息类型（0 系统账户 1 银行卡账户）
	 * @param queryCode 1:永达互联网金融账户名[模糊查询]
	 *            2:永达互联网金融账号[模糊查询]
	 *            3:银行账户开户名[模糊查询]
	 *            4:银行账户账号[为保证账号信息采用精准匹配]
	 *            5:开户银行名称[模糊查询]
	 *            6:联系人拼音（默认为账户名）
	 *            0:默认[对以上关键字都进行查询匹配]
	 * @param orderByCode 0：默认排序（系统账户名或者银行账户名）1：联系人名拼音
	 * @param value 过滤条件值
	 * @return 联系人信息列表
	 */
	public List<MemberContactInfo> fastQueryContact(String memberId, int contactType, int queryCode, 
			int orderByCode, String value, OperationEnvironment env);
}
