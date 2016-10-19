package com.netfinworks.site.web.action.securityCenter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.cert.service.response.CertRenewResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.LogUtil;

@Controller
@RequestMapping("/certRenew")
public class CertificationRenewAction extends BaseAction {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	
	@ResponseBody
	@RequestMapping(value = "/certRenew.htm", method = RequestMethod.POST)
	public RestResponse certRenew(HttpServletRequest req, HttpServletResponse resp, OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String csr = req.getParameter("csr");
		String origCert = req.getParameter("origCert");
		String pkcsInfo = req.getParameter("pkcsInfo");
		String certSn = req.getParameter("certSn");
		// 获取用户信息
		EnterpriseMember user = getUser(req);
		logger.info(LogUtil.generateMsg(OperateTypeEnum.HADRCERT_UNDATE, user, env, StringUtils.EMPTY));
		String memberId = user.getMemberId();
		String operatorId = user.getCurrentOperatorId();
		 
		CertificationInfoRequest request = new CertificationInfoRequest();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCsr(csr);
		request.setCertSn(certSn);
		request.setOrigCert(origCert);
		request.setPkcsInfo(pkcsInfo);
		CertRenewResponse certRenewResponse = null;
		try {
			certRenewResponse = certificationService.certRenew(request, env);
			if(ResponseCode.SUCCESS.getCode().equals(certRenewResponse.getResultCode())){
				Map<String, String> result = JsonMapUtil.jsonToMapStr(certRenewResponse.getCertData());
				String certSerialNumber = result.get("certSerialNumber");
				String certSignBufP7 = result.get("certSignBufP7");
				data.put("certSerialNumber", certSerialNumber);
				data.put("certSignBufP7", certSignBufP7);
				restP.setData(data);
				restP.setSuccess(true);
			} else {
				if(certRenewResponse.getReturnMessage().contains("41021")) {
					logger.debug("不能重复更新证书!");
					restP.setMessage("不能重复更新证书!");
				} else {
					logger.debug("更新证书失败");
					restP.setMessage("更新证书失败");
				}
				restP.setSuccess(false);
			}
		} catch (BizException e) {
			logger.error("查询证书详情失败", e);
			e.printStackTrace();
		}
		return restP;
	}
	
	@RequestMapping(value = "/gotoUpdateDun.htm", method = RequestMethod.GET)
	public ModelAndView gotoUpdateDun(HttpServletRequest req) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/updateDun");
		return mv;
	}
}
