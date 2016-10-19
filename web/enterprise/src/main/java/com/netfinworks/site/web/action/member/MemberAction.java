package com.netfinworks.site.web.action.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.meidusa.fastjson.TypeReference;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.ma.service.response.CompanyMemberInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;

@Controller
public class MemberAction extends BaseAction {
    @Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;
    @Resource(name = "defaultBankAccountService")
    private DefaultBankAccountService defaultBankAccountService;
    @Resource(name = "authVerifyService")
    private AuthVerifyService authVerifyService;
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	@Resource
	private OperatorService operatorService;

    /**
     * 企业会员管理
     *
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/accountManage.htm", method = RequestMethod.GET)
	public ModelAndView accountManage(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        EnterpriseMember user = getUser(request);
        AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
        authVerifyInfo.setMemberId(user.getMemberId());
        List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
        for (int i = 0; i < infos.size(); i++) {
            authVerifyInfo = infos.get(i);
            if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
                data.put("verify_mobile", authVerifyInfo.getVerifyEntity());
            } else if (VerifyType.EMAIL.getCode() == authVerifyInfo.getVerifyType()) {
            	String email = authVerifyInfo.getVerifyEntity();
                int index = email.indexOf("@");
        		data.put("verify_email", StarUtil.hideStrBySym(email, 1, email.length()-index, 3));
            }
        }
		data.put("verify_name", user.getNameVerifyStatus().getCode());
        //查询绑定银行卡
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);

		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getCurrentOperatorId());
		certRequest.setCertificationType(CertificationType.HARD_CERTIFICATION.getCode());
		certRequest.setCertificationStatus(CertificationStatus.ACTIVATED.getCode());
		// 判读用户证书是否激活
		RestResponse certResponse = certificationService.getCertByCertStatus(certRequest, env);
		if (certResponse.isSuccess()) {
			data.put("cert", "true");// 已激活
		} else {
			data.put("cert", "false");// 其他
		}

		CompanyMemberInfo compInfo = defaultMemberService.queryCompanyInfo(user, env);
		data.put("summary", compInfo.getSummary());
        data.put("mobile", user.getMobileStar());
        data.put("email", user.getEmail());
        data.put("member", user);
        String loginName = user.getLoginName();
        int index = loginName.indexOf("@");
		data.put("loginName", StarUtil.hideStrBySym(loginName, 1, loginName.length()-index, 3));


		OperatorVO vo = operatorService.getOperatorById(user.getCurrentOperatorId(), env);
		data.put("memberOper", vo.getLoginName());
		data.put("list", list);
        restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/accountBaseInfo", "response",
            restP);
    }

    /**
     * 创建企业会员
     *
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/createMember.htm", method = RequestMethod.GET)
    public ModelAndView ceateMember(HttpServletRequest req,HttpServletResponse resp, ModelMap model,OperationEnvironment env) throws Exception {
        HttpSession session = req.getSession();
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        EnterpriseMember user = getUser(req);
        if (user == null) {
            return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
        }
        //创建用户
        EnterpriseMember enterprise = new EnterpriseMember();
        enterprise.setPlateFormId(user.getPlateFormId());
        enterprise.setMobile(user.getMobile());
        enterprise.setPlateName(user.getPlateName());
        enterprise.setMemberName(user.getPlateName());
        MemberAndAccount member = defaultMemberService.createEnterpriseMember(enterprise, env);
        user.setDefaultAccountId(member.getAccountId());
        user.setMemberId(member.getMemberId());
        user.setOperatorId(member.getMemberId());
        EnterpriseMember result = defaultMemberService.queryCompanyMember(user, env);
        updateSessionObject(req);
        data.put("member", user);
        data.put("userName", user.getPlateName());
        data.put("email", user.getEmail());
        data.put("merState", req.getParameter("merState"));
        data.put("salt", DEFAULT_SALT);
        restP.setData(data);
        //进入激活页面
        return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember", "response",
            restP);
    }

    /**
     * 激活企业会员
     *
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/active/avtiveMember.htm", method = RequestMethod.GET)
    public ModelAndView activeMember(HttpServletRequest req, ModelMap model) throws Exception {
        RestResponse restP = new RestResponse();
        String message = req.getParameter("message");
        Map<String, Object> data = initOcx();
        EnterpriseMember user = getUser(req);
        if (user == null) {
            return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
        }else if((user.getStatus().getCode() == MemberStatus.DEFAULT.getCode()) && (user.getPaypasswdstatus().getCode() == MemberPaypasswdStatus.DEFAULT.getCode())) {
            //已经激活，直接调整到首页
            return new ModelAndView("redirect:/index.htm");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Member: [" + user.getMemberId()
                        + " ] is not actived, go into active page!");
        }
        data.put("member", user);
        data.put("userName", user.getPlateName());
        data.put("email", user.getEmail());
        data.put("merState", req.getParameter("merState"));

        data.put("message",message);
        restP.setData(data);
        logger.info("操作状态："+req.getParameter("merState"));
        return new ModelAndView(CommonConstant.URL_PREFIX + "/member/activeMember", "response",
            restP);
    }
    /**
     * 进入完善银行卡信息页面
     * @param request
     * @param env
     * @return
     */
    @RequestMapping(value = "/my/finishCardInfo.htm", method = RequestMethod.GET)
    public ModelAndView improveCardInfo(HttpServletRequest request,HttpServletResponse resp, TradeEnvironment env){
        RestResponse restP = new RestResponse();
        Map<String, Object> data =initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();
        EnterpriseMember user = getUser(request);
        checkUser(user, errorMap, restP);
        //查询绑定银行卡
        BankAccRequest req = new BankAccRequest();
        req.setMemberId(user.getMemberId());
        req.setClientIp(request.getRemoteAddr());
        List<BankAccountInfo> list=null;
        try {
            list = defaultBankAccountService.queryBankAccount(req);
        } catch (ServiceException e) {
            logger.error("查询会员银行卡信息，调用接口异常！");
            e.printStackTrace();
        }
        //进入提现页面
        data.put("banks", list);
        data.put("member", user);
        restP.setErrors(errorMap);
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/member/improveCardInfo", "response",
            restP);

    }

	/**
	 * 银行卡管理首页
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/card-manage-index.htm", method = RequestMethod.GET)
	public ModelAndView manageCardInfo(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);

		// 查询绑定银行卡
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());
		List<BankAccountInfo> list = null;

		try {
			list = defaultBankAccountService.queryBankAccount(req);
		} catch (ServiceException e) {
			logger.error("查询会员银行卡信息，调用接口异常！");
			e.printStackTrace();
		}
		String defaultCard = this.getDefaultBankCard(list);
		
		data.put("defaultCard", defaultCard);
		data.put("list", list);
		restP.setErrors(errorMap);
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/card-manage-index", "response", restP);

	}

	/**
	 * 设置默认银行卡
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/setDefaultCard.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse setDefaultCard(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env) {
		RestResponse restP = new RestResponse();
		String bankCardId = request.getParameter("bankCardId");
		String defaultCardId = request.getParameter("defaultCardId");

		try {
			BankAccRequest req = new BankAccRequest();
			req.setMemberId(super.getMemberId(request));
			
			if ((defaultCardId != null) && !"".equals(defaultCardId)) {
				if (defaultCardId.equals(bankCardId)) {
					restP.setSuccess(true);
					return restP;
				} else {
				    Map<String, String> map = new HashMap<String, String>();
					req.setBankcardId(defaultCardId);
                    BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(defaultCardId);
                    if (StringUtil.isNotBlank(info.getExtention())) {
                        Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                                new TypeReference<Map<String, String>>() {
                                });
                        for (String key : extMap.keySet()) {
                            map.put(key, extMap.get(key));
                        }
                    }
                    map.put("isDefaultcard", "0");
                    
                    req.setExtInfo(JSONObject.toJSONString(map));
					if (!defaultBankAccountService.updateDefaultAccount(req)) {
						throw new Exception("移除默认银行卡失败");
					}
				}
			}
			Map<String, String> map = new HashMap<String, String>();
			BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(bankCardId);
            if (StringUtil.isNotBlank(info.getExtention())) {
                Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                        new TypeReference<Map<String, String>>() {
                        });
                for (String key : extMap.keySet()) {
                    map.put(key, extMap.get(key));
                }
            }
            map.put("isDefaultcard", "1");
			req.setBankcardId(bankCardId);
			req.setExtInfo(JSONObject.toJSONString(map));
			if (!defaultBankAccountService.updateDefaultAccount(req)) {
				throw new Exception("添加默认银行卡失败");
			}
			restP.setSuccess(true);

		} catch (Exception e) {
			logger.error("", e);
		}

		return restP;

	}

	/**
	 * 解除会员银行卡绑定
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/removeBankCard.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse removeBankCard(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env) {
		RestResponse restP = new RestResponse();

		EnterpriseMember user = getUser(request);
		
		logger.info(LogUtil.generateMsg(OperateTypeEnum.UNBIND_ACCOUNT, user, env, StringUtils.EMPTY));
		
		if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
			restP.setMessage("请先进行实名认证！");
			return restP;
		}

		BankAccRequest req = new BankAccRequest();
		req.setBankcardId(request.getParameter("bankCardId"));
		req.setMemberId(super.getMemberId(request));
		try {
			defaultBankAccountService.removeBankAccount(req);
			restP.setSuccess(true);
		} catch (ServiceException e) {
			logger.error("解除会员银行卡绑定，调用接口异常！");
		}
		return restP;

	}

	/**
	 * 修改银行卡主页
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/modify-bank-index.htm")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		EnterpriseMember user = getUser(request);

		data.put("username", user.getEnterpriseName());
		data.put("oldBankCardId", request.getParameter("oldBankCardId"));
		data.put("operType", request.getParameter("operType"));
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/addBankCard", "response", restP);
	}
	
	/**
	 * 打开修改预留信息界面
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/toModifySummary.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse toModifySummary(HttpServletRequest request, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		restP.setSuccess(true);
		return restP;
	}

	/**
	 * 修改预留信息
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "/my/modifySummary.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse modifySummary(HttpServletRequest request, HttpServletResponse resp) {
		RestResponse restP = new RestResponse();
		String summary = request.getParameter("summary");
		if ((summary.length() < 6) || (summary.length() > 20)) {
			restP.setMessage("预留信息位数超过限制");
			return restP;
		}
		EnterpriseMember infoMember = new EnterpriseMember();
		infoMember.setMemberId(super.getMemberId(request));
		infoMember.setSummary(summary);
		try {
			if (!defaultMemberService.setCompanyMember(infoMember, env)) {
				restP.setMessage("修改预留信息失败");
				return restP;
			}
		} catch (Exception e) {
			restP.setMessage("修改预留信息失败");
			return restP;
		}
		restP.setSuccess(true);
		return restP;

	}

	/**
	 * 签约产品
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/app-center.htm")
	public ModelAndView appProduct(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env)
			throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();

		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/member/account-appCenter", "response", restP);
	}

}
