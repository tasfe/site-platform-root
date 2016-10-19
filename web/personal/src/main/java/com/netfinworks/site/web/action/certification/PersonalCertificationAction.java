package com.netfinworks.site.web.action.certification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.autoFundout.AutoFundoutOrder;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.FileInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.response.VerifyAmountResponse;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.autoFundout.AutoFundoutServiceImpl;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.BankCardForm;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * <p>
 * 个人会员实名认证
 * </p>
 * 
 * @author liuchen
 * @version 创建时间：2015-2-9 下午5:14:57
 */
@Controller
public class PersonalCertificationAction extends BaseAction {

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@Resource(name = "webResource")
	private WebDynamicResource webResource;

	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;

	@Resource(name = "autoFundoutService")
	private AutoFundoutServiceImpl autoFundoutServiceImpl;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "memberService")
	private MemberService memberService;

	/**
	 * 个人会员实名认证主页
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-personal-certification.htm", method = RequestMethod.GET)
	public ModelAndView index(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		try {
			PersonMember user = getUser(req);

			// 1.实名认证等级查询
			data.put("certifyLevel", user.getCertifyLevel().getCode());

			// 2.身份认证，银行卡认证，证件照认证单独查询
			String identitySts = queryRealName(user.getMemberId(), user.getOperatorId(), AuthType.IDENTITY);
			String bankCardSts = queryRealName(user.getMemberId(), user.getOperatorId(), AuthType.BANK_CARD);
			String realNameSts = queryRealName(user.getMemberId(), user.getOperatorId(), AuthType.REAL_NAME);

			data.put("identitySts", identitySts);
			data.put("bankCardSts", bankCardSts);
			data.put("realNameSts", realNameSts);

			return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-personal-index",
					"response", restP);
		} catch (ServiceException e) {
			logger.error("个人实名认证首页：" + e.getMessage());
			restP.setMessage(e.getMessage());
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		} catch (Exception e) {
			logger.error("个人实名认证首页：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 身份认证首页
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-personal-verifyIdentity.htm", method = RequestMethod.GET)
	public ModelAndView identityIndex(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);

			data.put("loginName", user.getLoginName());

			return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-personal-identity",
					"response", restP);

		} catch (Exception e) {
			logger.error("个人身份认证首页：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 身份认证
	 * 
	 * @param req
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/do-personal-verifyIdentity.htm", method = RequestMethod.POST)
	public ModelAndView verifyIdentity(HttpServletRequest req, HttpServletResponse resp, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);

			String identitySts = queryRealName(user.getMemberId(), user.getOperatorId(), AuthType.IDENTITY);

			if (ResultStatus.CHECK_PASS.getCode().equals(identitySts)) {
				restP.setMessage("您的身份认证已经审核通过！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}

			String realName = req.getParameter("realname");// 真实姓名
			String idType = req.getParameter("cardtype");// 证件类型
			String idCard = req.getParameter("idcard");// 证件号码

			if (StringUtils.isEmpty(realName) || StringUtils.isEmpty(idCard)) {
				restP.setMessage("缺少必要的页面元素！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}

			defaultCertService.verifyRealName(user.getMemberId(), user.getOperatorId(), StringUtils.trim(realName),
					StringUtils.trim(idCard), idType, env);

			// 更新session中的user信息
			user.setMemberName(realName);
			req.getSession().setAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PERSONAL_USER,
					JsonUtils.toJson(user));

			return new ModelAndView("redirect:/my/go-personal-certification.htm");


		} catch (ServiceException e) {
			logger.error("个人身份认证：" + e.getMessage());
			restP.setMessage(e.getMessage());
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		} catch (Exception e) {
			logger.error("个人身份认证：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 银行卡认证首页
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-personal-verifyBankCard.htm", method = RequestMethod.GET)
	public ModelAndView bankCardIndex(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);

			data.put("memberName", user.getMemberName());

			return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-personal-bankcard",
					"response", restP);

		} catch (Exception e) {
			logger.error("个人银行卡认证首页：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 银行卡验证
	 * 
	 * @param form
	 * @param req
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/do-personal-verifyBankCard.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse verifyBankCard(BankCardForm form, HttpServletRequest req, HttpServletResponse resp,
			OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			// 校验提交参数
			String errorMsg = validator.validate(form);
			if (StringUtils.isNotEmpty(errorMsg)) {
				restP.setMessage(errorMsg);
				return restP;
			}
			PersonMember user = getUser(req);

			String bankCardSts = queryRealName(user.getMemberId(), user.getOperatorId(), AuthType.BANK_CARD);

			if (!(StringUtils.EMPTY.equals(bankCardSts) || ResultStatus.AUDIT_REJECT.getCode().equals(bankCardSts) || ResultStatus.CHECK_REJECT
					.getCode().equals(bankCardSts))) {
				restP.setMessage("您的银行卡认证已经提交！");
				return restP;
			}

			// 卡bin校验
			try {
				CardBin cardBin = defaultPfsBaseService.queryByCardNo(form.getBankAccountNum(),
						CommonConstant.PERSONAL_APP_ID);
				if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
					restP.setMessage("绑定银行卡不能是信用卡!");
					return restP;
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", form.getBankAccountNum());
			}

			BankAccRequest bankReq = new BankAccRequest();
			bankReq.setMemberId(user.getMemberId());
			bankReq.setOperatorId(user.getOperatorId());
			bankReq.setRealName(user.getMemberName());
			bankReq.setBankName(form.getBankName());
			bankReq.setBankCode(form.getBankCode());
			bankReq.setBranchName(form.getBranchName());
			bankReq.setBranchNo(form.getBranchNo());
			bankReq.setCardType(Integer.valueOf(form.getCardType()));
			bankReq.setCardAttribute(Integer.valueOf(form.getCardAttribute()));
			bankReq.setPayAttribute("normal");// 支付属性：普通
			bankReq.setProvName(form.getProvName());
			bankReq.setCityName(form.getCityName());
			bankReq.setBankAccountNum(form.getBankAccountNum());
			bankReq.setIsVerified(0);// 0-未认证
			bankReq.setMemberIdentity(user.getMemberIdentity());// 会员标识

			defaultCertService.verifyBankCard(bankReq, env);

			restP.setSuccess(true);
		} catch (ServiceException e) {
			logger.error("个人银行卡认证：" + e.getMessage());
			restP.setMessage(e.getMessage());
		} catch (Exception e) {
			logger.error("个人银行卡认证：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
		}
		return restP;
	}

	/**
	 * 打款验证首页
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-personal-verifyAmount.htm", method = RequestMethod.GET)
	public ModelAndView amountIndex(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);
			BankAccRequest bankReq = new BankAccRequest();
			bankReq.setMemberId(user.getMemberId());
			bankReq.setClientIp(req.getRemoteAddr());

			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(bankReq);

			data.put("info", list.get(0));

			return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-personal-amount",
					"response", restP);

		} catch (ServiceException e) {
			logger.error("个人打款验证首页：" + e.getMessage());
			restP.setMessage(e.getMessage());
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		} catch (Exception e) {
			logger.error("个人打款验证首页：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 打款验证
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/do-personal-verifyAmount.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse verifyAmount(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);

			String amount = req.getParameter("amount");// 页面输入打款金额

			if (StringUtils.isEmpty(amount)) {
				restP.setMessage("输入的金额不能为空!");
				return restP;
			}

			String bankCardSts = queryRealName(user.getMemberId(), user.getOperatorId(), AuthType.BANK_CARD);

			if (ResultStatus.CHECK_PASS.getCode().equals(bankCardSts)) {
				restP.setMessage("您的银行卡认证已经通过！");
				return restP;
			}

			VerifyAmountResponse response = defaultCertService.verifyAmount(user.getMemberId(), user.getOperatorId(),
					amount);

			if (!response.isSuccess()) {
				data.put("remainCount", response.getRemainCount());
			} else {
				restP.setSuccess(true);
			}

		} catch (ServiceException e) {
			logger.error("个人打款验证：" + e.getMessage());
			restP.setMessage(e.getMessage());
		} catch (Exception e) {
			logger.error("个人打款验证：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
		}
		return restP;
	}

	/**
	 * 撤销银行卡验证
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/do-personal-cancelBankVerify.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse cancelBankVerify(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);

			defaultCertService.cancelBankVerify(user.getMemberId(), user.getOperatorId());

			restP.setSuccess(true);

		} catch (ServiceException e) {
			logger.error("个人撤销银行卡验证：" + e.getMessage());
			restP.setMessage(e.getMessage());
		} catch (Exception e) {
			logger.error("个人撤销银行卡验证：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
		}
		return restP;
	}

	/**
	 * 证件验证首页
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/go-personal-verifyRealName.htm", method = RequestMethod.GET)
	public ModelAndView realNameIndex(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);

			AuthInfo info = queryAuthInfo(user.getMemberId(), user.getOperatorId(), AuthType.IDENTITY);

			data.put("authNo", StarUtil.mockCommon(info.getAuthNo()));
			data.put("certType", info.getCertType().getMessage());
			data.put("realName", user.getMemberName());
			data.put("certTypeCode", info.getCertType().getCode());

			return new ModelAndView(CommonConstant.URL_PREFIX + "/certification/certification-personal-realname",
					"response", restP);

		} catch (ServiceException e) {
			logger.error("个人证件验证首页：" + e.getMessage());
			restP.setMessage(e.getMessage());
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		} catch (Exception e) {
			logger.error("个人证件验证首页：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
		}
	}

	/**
	 * 证件验证
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/do-personal-verifyRealName.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse verifyRealName(HttpServletRequest req, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);

		try {
			PersonMember user = getUser(req);

			String realNameSts = queryRealName(user.getMemberId(), user.getOperatorId(), AuthType.REAL_NAME);

			if (!(StringUtils.EMPTY.equals(realNameSts) || ResultStatus.CHECK_REJECT.getCode().equals(realNameSts))) {
				restP.setMessage("您的证件照认证已经提交！");
				return restP;
			}

			AuthInfo info = queryAuthInfo(user.getMemberId(), user.getOperatorId(), AuthType.IDENTITY);

			AuthInfoRequest authReq = new AuthInfoRequest();
			authReq.setMemberId(user.getMemberId());
			authReq.setOperator(StringUtils.EMPTY);
			authReq.setAuthType(AuthType.REAL_NAME);
			authReq.setAuthNo(info.getAuthNo());
			authReq.setAuthName(info.getAuthName());
			authReq.setCertType(req.getParameter("certTypeCode"));
			authReq.setClientIp(req.getRemoteAddr());

			List<FileInfo> authFiles = new ArrayList<FileInfo>();
			String filePath = StringUtils.trim(webResource.getUploadFilePath());
			FileInfo file1 = createAuthFile(filePath + req.getParameter("imageUrl1"));
			FileInfo file2 = createAuthFile(filePath + req.getParameter("imageUrl2"));
			authFiles.add(file1);
			authFiles.add(file2);
			authReq.setAuthFiles(authFiles);

			defaultCertService.certification(authReq);

			restP.setSuccess(true);

		} catch (ServiceException e) {
			logger.error("个人证件验证：" + e.getMessage());
			restP.setMessage(e.getMessage());
		} catch (Exception e) {
			logger.error("个人证件验证：" + e.getMessage());
			restP.setMessage("对不起，服务器繁忙，请稍后再试！");
		}
		return restP;
	}

	/**
	 * 自动出款异步通知
	 * 
	 * @param req
	 * @param resp
	 */
	@RequestMapping(value = "/my/autoFundoutNotify.htm", method = { RequestMethod.GET, RequestMethod.POST })
	public String autoFundoutNotify(HttpServletRequest req, HttpServletResponse resp) {
		String outer_trade_no = req.getParameter("outer_trade_no");// 商户订单号
		String inner_trade_no = req.getParameter("inner_trade_no");// 永达互联网金融订单号
		String withdrawal_status = req.getParameter("withdrawal_status");// 转账状态
		String fail_reason = req.getParameter("fail_reason");// 失败原因
		String gmt_withdrawal = req.getParameter("gmt_withdrawal");// 转账时间
		
		if (StringUtils.isNotEmpty(outer_trade_no)) {
			AutoFundoutOrder autoFundoutOrder = autoFundoutServiceImpl.queryOrderById(outer_trade_no);
			if (null != autoFundoutOrder) {
				List<String> orderNoList = new ArrayList<String>();
				orderNoList.add(autoFundoutOrder.getAuthOrderNo());

				try {
					if (withdrawal_status.equals(CommonConstant.WITHDRAWAL_SUCCESS)) {
						defaultCertService.modifyAuthStatus(orderNoList, "自动打款", false, true);
					} else if (withdrawal_status.equals(CommonConstant.WITHDRAWAL_FAIL)
							|| withdrawal_status.equals(CommonConstant.RETURN_TICKET)) {
						defaultCertService.modifyAuthStatus(orderNoList, "自动打款", false, false);
						autoFundMsgNotify(autoFundoutOrder.getMemberId());
					}
				} catch (ServiceException e) {
					logger.error("自动出款异步通知更改认证状态失败：" + e.getMessage());
				}

				AutoFundoutOrder order = new AutoFundoutOrder();
				order.setOuterTradeNo(outer_trade_no);
				order.setFundoutOrderNo(inner_trade_no);
				order.setWithdrawalStatus(withdrawal_status);
				order.setFailReason(fail_reason);
				order.setGmtModified(DateUtil.parseDateLongFormat(gmt_withdrawal));
				if (!autoFundoutServiceImpl.updateOrder(order)) {
					logger.error("自动出款异步通知更新出款状态失败，商户订单号：" + outer_trade_no);
				}
			}
		}

		return "success";

	}

	/**
	 * 打款认证失败通知
	 * 
	 * @param memberId
	 */
	public void autoFundMsgNotify(String memberId) {
		String mobile = "";// 绑定手机号
		String email = "";// 绑定邮箱

		EncryptData data;
		try {
			data = memberService.decipherMember(memberId, DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL,
					new OperationEnvironment());
			mobile = data.getPlaintext();

			data = memberService.decipherMember(memberId, DeciphedType.EMAIL, DeciphedQueryFlag.ALL,
					new OperationEnvironment());
			email = data.getPlaintext();
		} catch (BizException e1) {
			logger.error("获取绑定手机号码或者邮件失败,会员编号为:" + memberId);
		}

		// 发送短信提醒
		if (StringUtils.isNotEmpty(mobile)) {
			String bizType = BizType.AUTOFUNDOUT_NOTIFY_MOBILE.getCode();
			Map<String, Object> objParams = new HashMap<String, Object>();
			try {
				defaultPayPasswdService.sendMobileMsg(mobile, bizType, objParams);
			} catch (ServiceException e) {
				logger.error("银行卡认证失败通知出错:" + mobile);
			}
		}
		// 发送邮件提醒
		if (StringUtils.isNotEmpty(email)) {
			String bizType = BizType.AUTOFUNDOUT_NOTIFY_EMAIL.getCode();
			Map<String, Object> objParams = new HashMap<String, Object>();
			objParams.put("email", email);
			try {
				defaultPayPasswdService.sendEmail(email, bizType, objParams);
			} catch (ServiceException e) {
				logger.error("银行卡认证失败通知出错:" + email);
			}
		}
	}

	/**
	 * 根据认证类型查询认证结果
	 * @param memberId
	 * @param operatorId
	 * @param authType
	 * @return
	 * @throws ServiceException
	 */
	public String queryRealName(String memberId, String operatorId, AuthType authType) throws ServiceException {
		AuthInfoRequest authInfoReq = new AuthInfoRequest();
		authInfoReq.setMemberId(memberId);
		authInfoReq.setAuthType(authType);
		authInfoReq.setOperator(operatorId);

		AuthInfo info = defaultCertService.queryRealName(authInfoReq);

		if ((info != null) && (info.getResult()!=null)) {
			return info.getResult().getCode();
		}

		return StringUtils.EMPTY;
	}

	/**
	 * 根据认证类型查询认证结果-AuthInfo
	 * 
	 * @param memberId
	 * @param operatorId
	 * @param authType
	 * @return AuthInfo
	 * @throws ServiceException
	 */
	public AuthInfo queryAuthInfo(String memberId, String operatorId, AuthType authType) throws ServiceException {
		AuthInfoRequest authInfoReq = new AuthInfoRequest();
		authInfoReq.setMemberId(memberId);
		authInfoReq.setAuthType(authType);
		authInfoReq.setOperator(operatorId);

		AuthInfo info = defaultCertService.queryRealName(authInfoReq);

		if (info == null) {
			return new AuthInfo();
		}

		return info;
	}

	/**
	 * 根据URL封装文件信息
	 * 
	 * @param imageUrl
	 * @return
	 */
	private FileInfo createAuthFile(String imageUrl) {
		if (StringUtils.isEmpty(imageUrl)) {
			return null;
		}
		String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		String fileType = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
		FileInfo file = new FileInfo();
		file.setFileName(fileName);
		file.setFilePath(imageUrl);
		file.setFileType(fileType);
		return file;
	}
}
