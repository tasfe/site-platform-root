package com.netfinworks.site.web.action.email;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.jdbc.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * <p>
 * 修改绑定邮箱
 * </p>
 * 
 * @author liangzhizhuang.m
 * @version $Id: MobileAction.java, v 0.1 2014年5月20日 下午4:45:48 liangzhizhuang.m
 *          Exp $
 */
@Controller
public class OrigBindPhoneCanUesOrNotAction extends BaseAction {

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "authVerifyService")
	private AuthVerifyService authVerifyService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	protected static final Logger logger = LoggerFactory.getLogger(OrigBindPhoneCanUesOrNotAction.class);

	/**
	 * 跳至"更换绑定手机"页面(原手机可用)
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/changeBindPhone.htm", method = RequestMethod.GET)
	public ModelAndView goResetEmail(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env) throws Exception {
		
		RestResponse restP = new RestResponse();
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("bizType", BizType.RESET_MOBILE.getCode());
		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_canUse1","response",restP);
	}
	
	/**
	 * 跳至"输入支付密码&证件号"页面(原手机不可用)
	 * 
	 * @param request
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/my/changeBindPhoneByLisence.htm", method = RequestMethod.GET)
	public ModelAndView goLisence(HttpServletRequest request, HttpServletResponse resP, OperationEnvironment env) throws Exception {
		
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		PersonMember user = getUser(request);
		
		if (user.getMemberId().startsWith("2")) {
			if (user.getNameVerifyStatus() == null || !user.getNameVerifyStatus().getCode().equals(user.getNameVerifyStatus().getCode())) {
				restP.setMessage("请确认实名认证是否成功!");
				restP.setSuccess(false);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response",
						restP);
			}
		}else if (user.getMemberId().startsWith("1")) {
			if (!"1".equals(user.getCertifyLevel().getCode()) &&
				!"2".equals(user.getCertifyLevel().getCode()) &&
				!"3".equals(user.getCertifyLevel().getCode())) {
				restP.setMessage("请确认实名认证是否成功!");
				restP.setSuccess(false);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response",
						restP);
			}
		}

		AuthVerifyInfo verifyInfo = getVerifyInfo(authVerifyService, request, VerifyType.EMAIL, env);
		String email = verifyInfo.getVerifyEntity();
		if(email==null){
//			email = "1";
			restP.setMessage("需要通过邮箱修改，请先绑定邮箱!");
			restP.setSuccess(false);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/mobilephoneMy/safety_phoneResult", "response", restP);
		}

//		data.put("email", email);
//		restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/emailBind/safety_phoneBind_notUse1", "response",restP);
	}
	

}
