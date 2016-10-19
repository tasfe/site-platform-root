package com.netfinworks.site.web.action.securityCenter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.info.CertificationDetail;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * 查看证书详情的action:
 * step1：判断操作员证书在本地环境十分存在
 * step2：查询证书详情
 * @author chengwen
 *
 */
@Controller
@RequestMapping("/certDetail")
public class CertificationDetailAction extends BaseAction {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	
	private static final String SOURCEPOSITION = "永达互联网金融信息服务有限公司";
	
	/**
	 * 查看证书详情
	 * @param req
	 * @param resp
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws Exception
	 */
	@RequestMapping(value = "/showCertDetail.htm", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView showCertDetail(HttpServletRequest req, HttpServletResponse resp, OperationEnvironment env) throws BizException, Exception {
		ModelAndView mv = new ModelAndView();

		String certSn = req.getParameter("certSn");
		CertificationInfoRequest request = new CertificationInfoRequest();
		CertificationDetail certificationDetail = null;
		// 获取用户信息
		EnterpriseMember user = getUser(req);
		String enterpriseName = user.getEnterpriseName();
		String memberId = user.getMemberId();
		String operatorId = user.getCurrentOperatorId();
		request.setMemberId(memberId);
		request.setOperatorId(operatorId);
		request.setCertSn(certSn);
		certificationDetail = certificationService.certDetail(request, env);
		Map<String, String> map = JsonMapUtil.jsonToMapStr(certificationDetail
				.getCertData());
		String mid = certificationDetail.getMemberId();
		String certIssuerDn = map.get("certIssuerDn");
		String date = map.get("certApproveDate");
		DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		dateFormat.setLenient(false);
		String dateStr = dateFormat.format(DateUtil.parseDateLongFormat(date));
		String certSerialNumber = map.get("certSerialNumber");
		mv.addObject("sourcePosition", SOURCEPOSITION);
		mv.addObject("mid", mid);
		mv.addObject("enterpriseName", enterpriseName);
		mv.addObject("certIssuerDn", certIssuerDn);
		mv.addObject("certSerialNumber", certSerialNumber);
		mv.addObject("dateStr", dateStr);
		mv.setViewName(CommonConstant.URL_PREFIX + "/securityCenter/cert_detail");

		return mv;
	}
}
