/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月16日
 */
package com.netfinworks.site.web.action.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.model.MemberContact;
import com.netfinworks.ma.service.base.model.MemberContactInfo;
import com.netfinworks.mns.client.domain.PageInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.bank.BankBranch;
import com.netfinworks.site.domain.domain.info.BankCard;
import com.netfinworks.site.domain.domain.info.Province;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.BankType;
import com.netfinworks.site.domain.enums.CardAttr;
import com.netfinworks.site.domain.enums.CropOrPersonal;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.ext.integration.member.MemberContactsService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.BankContactsForm;
import com.netfinworks.site.web.action.form.KjtContactsForm;
import com.netfinworks.site.web.common.constant.ContactsType;
import com.netfinworks.site.web.common.constant.ErrorMsg;
import com.netfinworks.site.web.common.vo.SessionPage;
import com.netfinworks.site.web.util.ObjectUtils;

/**
 * 联系人管理
 * @author xuwei
 * @date 2014年7月16日
 */
@Controller
@RequestMapping("/contacts")
public class ContactsAction extends BaseAction {
	
	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;
	
	@Resource(name = "memberContactsService")
	private MemberContactsService memberContactsService;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@RequestMapping("/toContactsHome")
	public ModelAndView toContactsHome(HttpServletRequest request, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		
		List<MemberContactInfo> kjtMcList = this.getMcList(super.getMemberId(request), 0, env);
		List<MemberContactInfo> bankMcList = this.getMcList(super.getMemberId(request), 1, env);

		List<MemberContactInfo> list = new ArrayList<MemberContactInfo>();
		if (kjtMcList != null) {
			list.addAll(kjtMcList);
		}

		if (bankMcList != null) {
			list.addAll(bankMcList);
		}

		data.put("list", list);
		restP.setData(data);

		return new ModelAndView(CommonConstant.URL_PREFIX + "/contacts/contacts-index", "response", restP);
	}
	
	@RequestMapping("/toAddContacts")
	public ModelAndView toAddContacts(HttpServletRequest request, OperationEnvironment env) {
		RestResponse restP = new RestResponse();

		return new ModelAndView(CommonConstant.URL_PREFIX + "/contacts/add-contacts-index", "response", restP);
	}
	
	/**
	 * 查询该会员相关联系人
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryContacts")
	public RestResponse queryContacts(HttpServletRequest request, HttpSession session, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		
		String memberId = getMemberId(request);
		String contactType = request.getParameter("contactType");
		int cType = StringUtils.isEmpty(contactType) ? -1 : Integer.parseInt(contactType);
		List<MemberContactInfo> mcList = this.getMcList(memberId, cType, env);
		response.setMessageObj(mcList);
		return response;
	}
	
	@ResponseBody
    @RequestMapping("/querySingleContacts")
    public RestResponse querySingleContacts(HttpServletRequest request, OperationEnvironment env) {
        RestResponse response = new RestResponse();
        MemberContactInfo mc = null;
        
        String contactId = request.getParameter("contactId");
        if (StringUtils.isEmpty(contactId)) {
            response.setMessageObj(mc);
            response.setSuccess(false);
            response.setMessage("未找到该联系人的任何信息");
            return response;
        } else {
            mc = memberContactsService.querySingleContact(contactId, env);
            if (mc != null) {
                if (StringUtils.isEmpty(mc.getMemo())) {
                    mc.setMemo("");
                }
                String accountNo = this.getDataByTicket(mc.getAccountNo());
                mc.setAccountNo(accountNo);
            }
        }
        response.setSuccess(true);
        response.setMessageObj(mc);
        return response;
    }
	
	@ResponseBody
	@RequestMapping(value = "/toAddKjtContacts")
	public RestResponse toAddKjtContacts(HttpServletRequest request, KjtContactsForm form, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		String memberId = getMemberId(request);
		
		List<String> kjtAccountList = form.getKjtAccountList();
		
		if (ObjectUtils.isListEmpty(kjtAccountList)) {
			response.setSuccess(false);
			response.setMessage("上传的联系人账户不能为空");
			return response;
		}
		
		int size = kjtAccountList.size();
		List<MemberContact> mcList = new ArrayList<MemberContact>();
		for (int i = 0; i < size; i++) {
			// 检查要添加的联系人是否存在
			String kjtAccount = kjtAccountList.get(i);
			mcList.add(setKjtContactsInfo(memberId, kjtAccount, "", env));
		}
		
		response.setSuccess(true);
		response.setMessageObj(mcList);
		
		super.setJsonAttribute(request.getSession(), "mcList", mcList);
		return response;
	}
	
	private MemberContact setKjtContactsInfo(String memberId, String kjtAccount, String remark, 
			OperationEnvironment env) {
		MemberContact mc = new MemberContact();
		try {
			BaseMember member = memberService.queryMemberByName(kjtAccount, env);

			mc.setMemberId(memberId);
			mc.setContactIdentity(kjtAccount);
			mc.setContactType(ContactsType.TYPE_SYS.getCode());
			mc.setAccountName(member.getMemberName() == null ? "" : member.getMemberName());
			mc.setMemo(remark);
		} catch (Exception e) {
			logger.error("查询会员信息失败", e);
		}
		
		return mc;
	}
	
	/**
	 * 添加永达互联网金融账户联系人
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/addKjtContacts")
	public RestResponse addKjtContacts(HttpSession session, KjtContactsForm form, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		List<MemberContact> mcList = new ArrayList<MemberContact>();
		
		List<String> remarkList = form.getRemarkList();

		List<JSONObject> list = super.getJsonAttribute(session, "mcList", List.class);
		for (int i = 0; i < list.size(); i++) {
			JSONObject obj = list.get(i);
			String remark = remarkList.get(i);
			MemberContact mc = JSONArray.parseObject(obj.toJSONString(), MemberContact.class);
			mc.setMemo(remark);
			mcList.add(mc);
		}
		
		try {
			memberContactsService.batchCreateContact(mcList, ContactsType.TYPE_SYS.getCode(), env);
			response.setSuccess(true);
		} catch(Exception e) {
			logger.error("批量添加联系人失败", e);
			response.setSuccess(false);
		} finally {
			session.removeAttribute("mcList");
		}
		
		return response;
	}
	
	@ResponseBody
	@RequestMapping(value = "/toAddBankContacts")
	public RestResponse toAddBankContacts(HttpServletRequest request, BankContactsForm form) {
		RestResponse response = new RestResponse();
		String memberId = getMemberId(request);
		MemberContactInfo mc = getBankMcInfo(memberId, form);
		
		super.setJsonAttribute(request.getSession(), "memberContact", mc);
		response.setSuccess(true);
		response.setMessageObj(mc);
		return response;
	}
	
	/**
	 * 获取银行账户联系人对象
	 * @param memberId
	 * @param form
	 * @return
	 */
	private MemberContactInfo getBankMcInfo(String memberId, BankContactsForm form) {
		String accountNo = form.getAccountNo();
		MemberContactInfo mc = new MemberContactInfo();
		mc.setContactType(ContactsType.TYPE_BANK.getCode());
		mc.setMemberId(memberId);
		mc.setContactIdentity(accountNo);
		mc.setAccountName(form.getAccountName() == null ? "" : form.getAccountName());
		mc.setBankCode(form.getBankCode());
		mc.setBankName(form.getBankName());
		mc.setProvince(form.getProvince());
		mc.setCity(form.getCity());
		mc.setMemo(form.getBankRemark());
		mc.setAccountNo(accountNo);
		mc.setBankBranch(form.getBranchName());
		mc.setCardAttribute(Integer.valueOf(form.getCardAttr()));
		
		return mc;
	}
	
	/**
	 * 保存银行账户联系人
	 * @param memberContact
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/saveBankContact")
	public RestResponse saveBankContact(HttpServletRequest request, BankContactsForm form, 
			OperationEnvironment env) {
		RestResponse response = new RestResponse();
		
		try {
			String memberId = getMemberId(request);
			MemberContactInfo mc = getBankMcInfo(memberId, form);
			MemberContact mcContact = new MemberContact();
			BeanUtils.copyProperties(mcContact, mc);
			String contactId = memberContactsService.createContact(mcContact, env);
			if (null == contactId) {
				response.setMessage("保存失败！");
				return response;
			}
		} catch (Exception e) {
			logger.error("保存联系人失败", e);
			response.setMessage("保存失败！");
			return response;
		}
		
		response.setSuccess(true);
		response.setMessage("保存成功！");
		return response;
	}
	
	/**
	 * 保存永达互联网金融账户联系人
	 * @param memberContact
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/saveKjtContact")
	public RestResponse saveKjtContact(HttpServletRequest request, MemberContact memberContact, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		logger.info("memberContact:", memberContact);
		
		try {
			MemberContact mcContact = setKjtContactsInfo(getMemberId(request), 
					memberContact.getContactIdentity(), memberContact.getMemo(), env);
			mcContact.setCardAttribute(null);
			mcContact.setCardType(null);
			List<MemberContact> mcList = new ArrayList<MemberContact>();
			mcList.add(mcContact);
			
			memberContactsService.batchCreateContact(mcList, ContactsType.TYPE_SYS.getCode(), env);
		} catch (Exception e) {
			logger.error("保存联系人失败", e);
			response.setMessage("保存失败！");
			return response;
		}
		
		response.setSuccess(true);
		response.setMessage("保存成功！");
		return response;
	}
	
	/**
	 * 添加银行账户联系人
	 * @param form
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/addBankContacts", method = RequestMethod.POST)
	public RestResponse addBankContacts(HttpSession session, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		MemberContact sessionMc = super.getJsonAttribute(session, "memberContact", MemberContact.class);
		try {
			sessionMc.setCardType(1);// 1-借记卡
			String contactId = memberContactsService.createContact(sessionMc, env);
			if (StringUtils.isNotEmpty(contactId)) {
				if (("contact_is_exist").equals(contactId)) {
					response.setMessage("联系人已存在!");
				} else {
					response.setSuccess(true);
				}

			}
		} catch(Exception e) {
			logger.error("无法添加联系人失败", e);
			response.setSuccess(false);
		} finally {
			session.removeAttribute("memberContact");
		}
		
		return response;
	}
	
	@RequestMapping("/toModifyContacts")
	public ModelAndView toModifyContacts(HttpServletRequest request, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		MemberContactInfo mc = null;

		String contactId = request.getParameter("contactId");
		String contactType = request.getParameter("contactType");
		try {
			mc = memberContactsService.querySingleContact(contactId, env);
		} catch (Exception e) {
			mc = new MemberContactInfo();
		}
		data.put("mc", mc);
		if (contactType.equals("1")) {
			return new ModelAndView(CommonConstant.URL_PREFIX + "/contacts/modify-contacts-bank", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/contacts/modify-contacts-kjt", "response", restP);
	}

	/**
	 * 修改联系人
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/modifyContact")
	public RestResponse modifyContact(HttpServletRequest request, MemberContact mContact, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		
		if (memberContactsService.updateContact(mContact, env)) {
			response.setSuccess(true);
		}
		
		return response;
	}
	
	/**
	 * 删除联系人
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/deleteContact")
	public RestResponse deleteContact(HttpServletRequest request, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		
		String contactId = request.getParameter("contactId");
		if (StringUtils.isEmpty(contactId)) {
			logger.error("联系人ID不能为空");
			response.setSuccess(false);
			response.setMessage("联系人ID不能为空");
			return response;
		}
		
		if (!memberContactsService.deleteContact(contactId, env)) {
			logger.error("删除联系人失败，联系人ID={}", contactId);
			response.setSuccess(false);
			response.setMessage("删除联系人失败！");
			return response;
		}
		
		response.setSuccess(true);
		response.setMessage("删除联系人成功！");
		
		List<MemberContactInfo> mcList = this.getMcList(getMemberId(request), -1, env);
		response.setMessageObj(mcList);
		
		return response;
	}
	
	/**
	 * 查询永达互联网金融所有合作银行
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryCorpBanks")
	public RestResponse queryCorpBanks(HttpServletRequest request, String cardAttr) {
		RestResponse response = new RestResponse();
		List<BankCard> bankList = null;
		try {
			CropOrPersonal cropOrPersonal = CropOrPersonal.PERSONAL;
			if (StringUtils.isNotEmpty(cardAttr)) {
				cropOrPersonal = CardAttr.COMPANY.getCode() == Integer.parseInt(cardAttr) ? 
						CropOrPersonal.COMPANY : CropOrPersonal.PERSONAL;
			}
			
			bankList = defaultPfsBaseService.queryBank(cropOrPersonal);
		} catch (ServiceException e) {
			logger.error("查询所有合作银行失败", e);
		}
		
		response.setSuccess(true);
		response.setMessageObj(bankList);
		return response;
	}
	
	/**
	 * 查询所有省市县
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryAllProvCities")
	public RestResponse queryAllProvinces(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		List<Province> provlist = null;
		try {
			// 查询省市县信息
			provlist = defaultPfsBaseService.queryProvInfos(true, false);
		} catch (ServiceException e) {
			logger.error("查询所有所有省市县失败", e);
		}
		
		response.setSuccess(true);
		response.setMessageObj(provlist);
		return response;
	}
	
	/**
	 * 查询某市的所有支行
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryBranches")
	public RestResponse queryBranches(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		
		String bankCode = request.getParameter("bankCode");
		String cityId = request.getParameter("cityId");
		
		List<BankBranch> branchList = null;
		try {
			branchList = defaultPfsBaseService.queryBranch(CommonConstant.ENTERPRISE_APP_ID, bankCode, Long.valueOf(cityId).longValue());
		} catch (ServiceException e) {
			logger.error("查询该市[id={}]的所有支行失败", cityId, e);
		}
		
		response.setSuccess(true);
		response.setMessageObj(branchList);
		return response;
	}
	
	/**
	 * 确认批量导入联系人
	 * @param session
	 * @param env
	 * @return
	 */
	@ResponseBody
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/confirmBatchContacts", method = RequestMethod.POST)
	public RestResponse confirmBatchContacts(HttpServletRequest request, HttpSession session, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		
		List<JSONObject> jsonList = this.getJsonAttribute(session, "memberContactList", List.class);
		if (ObjectUtils.isListEmpty(jsonList)) {
			response.setMessage("导入的联系人为空!");
			return response;
		}
		
		try {
			PersonMember user = this.getUser(request);
			List<MemberContact> mcList = getMemberContactList(jsonList, user.getMemberId());
			this.memberContactsService.batchCreateContact(mcList, ContactsType.TYPE_BANK.getCode(), env);
		} catch (Exception e) {
			logger.error("批量导入联系人失败", e);
			response.setMessage(e.getMessage());
			return response;
		}
		
		response.setSuccess(true);
		return response;
	}
	
	/**
	 * 解析MemberContact的JSON字符串列表
	 * @param list
	 * @return
	 */
	private List<MemberContact> getMemberContactList(List<?> list, String memberId) {
		if (ObjectUtils.isListEmpty(list)) {
			return null;
		}
		
		List<MemberContact> mcList = new ArrayList<MemberContact>();
		for (Object Obj : list) {
			JSONObject jsonObj = (JSONObject) Obj;
			MemberContact contact = JSONArray.parseObject(String.valueOf(jsonObj), MemberContact.class);
			contact.setMemberId(memberId);
			contact.setCardType(1);
			contact.setCardAttribute(0);
			mcList.add(contact);
		}
		
		return mcList;
	}
	
	/**
	 * 批量导入联系人Excel模板
	 * @param request
	 * @param session
	 * @param batchFile
	 * @return
	 */
	@RequestMapping(value = "/importBatchContacts", method = RequestMethod.POST)
	public ModelAndView importBatchContacts(HttpServletRequest request, HttpSession session, 
			@RequestParam("batchFile") MultipartFile batchFile) {
		ModelAndView mv = new ModelAndView();
		Workbook xwb = null;
		
		// 批量转账账户信息
		List<MemberContact> mcList = new ArrayList<MemberContact>();
		try {
			xwb = WorkbookFactory.create(batchFile.getInputStream());
			
			// 循环工作表Sheet
			int numOfSheets = xwb.getNumberOfSheets();
			for (int i=0; i<numOfSheets; i++) {
				parseOneBankContactSheet(mcList, xwb.getSheetAt(i));
			}
		} catch (Exception e) {
			logger.error("导入Excel文件失败", e);
			mv.addObject("message", StringUtils.isEmpty(e.getMessage())?ErrorMsg.ERROR_EXCEL_FORMAT.getDesc():e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		this.setJsonAttribute(session, "memberContactList", mcList);
		SessionPage<MemberContact> sessionPage = new SessionPage<MemberContact>();
		super.setSessionPage(mcList, sessionPage);
		mv.addObject("sessionPage", sessionPage);
		mv.setViewName(CommonConstant.URL_PREFIX + "/contacts/contacts-batch-confirm");
		
		return mv;
	}
	
	/**
	 * 从会话中分页查询记录
	 * @param sessionPage
	 * @param session
	 * @return
	 */
	@ResponseBody
   	@RequestMapping("/getSessionPage")
   	public RestResponse getSessionPage(HttpServletRequest request, PageInfo page, HttpSession session) {
		RestResponse response = new RestResponse();
		
		try {
			PersonMember user = this.getUser(request);
			List<MemberContact> mcList = getMemberContactList(this.getJsonAttribute(session, "memberContactList", List.class), user.getMemberId());
			SessionPage<MemberContact> sessionPage = new SessionPage<MemberContact>(page, new ArrayList<MemberContact>());
			super.setSessionPage(mcList, sessionPage);
			response.setSuccess(true);
			response.setMessageObj(sessionPage);
		} catch (Exception e) {
			logger.error("查询分页信息出错");
			response.setMessage("查询分页信息出错");
		}
		
		return response;
   	}
	
	/**
	 * 解析每一行的联系人
	 * @param mcList
	 * @param sheet
	 */
	private void parseOneBankContactSheet(List<MemberContact> mcList, Sheet sheet) {
		// 循环行Row
		for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				logger.debug(rowNum + "行为空");
				continue;
			}
			
			MemberContact contact = new MemberContact();
			String accountName = getValue(row.getCell(0));
			contact.setAccountName(accountName);
			
			String bankName = getValue(row.getCell(1));
			contact.setBankName(bankName);
			
			String accountNo = getValue(row.getCell(5));
			contact.setAccountNo(accountNo);
			
			contact.setMemo(getValue(row.getCell(6)));
			
			// 没有姓名或银行名称不能导入
			if (StringUtils.isEmpty(accountName) || StringUtils.isEmpty(bankName)
					|| StringUtils.isEmpty(accountNo)) {
				continue;
			}
			
			contact.setBankCode(BankType.getByMsg(bankName).getCode());
			contact.setProvince(getValue(row.getCell(2)));
			contact.setCity(getValue(row.getCell(3)));
			contact.setBankBranch(getValue(row.getCell(4)));
			
			mcList.add(contact);
		}
	}
	
	/**
	 * 得到Excel表中的值
	 * @param cell Excel中的单元格
	 * @return Excel中单元格的值
	 */
	@SuppressWarnings("static-access")
	private String getValue(Cell cell) {
		if (cell == null) {
			return StringUtils.EMPTY;
		}
		if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
			// 返回布尔类型的值
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
			// 返回数值类型的值
			return String.valueOf(cell.getNumericCellValue());
		} else {
			// 返回字符串类型的值
			return String.valueOf(cell.getStringCellValue());
		}
	}
	
	/**
	 * 模拟从服务层查询联系人
	 * @return
	 */
	private List<MemberContactInfo> getMcList(String memberId, int contactType, OperationEnvironment env) {
		List<MemberContactInfo> mcInfoList = memberContactsService.queryContacts(memberId, contactType, env);
		return mcInfoList;
	}
	
}
