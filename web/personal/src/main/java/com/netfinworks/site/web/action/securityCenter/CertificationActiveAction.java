/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-personal
 * @date 2014年9月4日
 */
package com.netfinworks.site.web.action.securityCenter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * 证书相关
 * @author xuwei
 * @date 2014年9月4日
 */
@Controller
@RequestMapping("/certActive")
public class CertificationActiveAction extends BaseAction {
	@Resource(name = "certificationService")
	private CertificationService certificationService;
	
	/**
	 * 获取证书列表
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getCertSns.htm")
	public RestResponse getCertSns(HttpServletRequest request, OperationEnvironment env) {
		String type = request.getParameter("type");
		String status = request.getParameter("status");
		CertificationInfoRequest certRequest = new CertificationInfoRequest();
		PersonMember user = this.getUser(request);
		certRequest.setMemberId(user.getMemberId());
		certRequest.setOperatorId(user.getOperatorId());
		certRequest.setCertificationType(type);
		certRequest.setCertificationStatus(status);
		RestResponse result = null;
		try {
			 result = certificationService.getCertByCertStatus(certRequest, env);
		} catch (BizException e) {
			logger.error("获取证书列表失败");
		}
		return result;
	}
}
