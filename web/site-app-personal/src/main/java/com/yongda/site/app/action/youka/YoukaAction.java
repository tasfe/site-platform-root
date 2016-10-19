package com.yongda.site.app.action.youka;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.RSASignUtil;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.domainservice.insurance.InsuranceService;
import com.yongda.site.ext.integration.insurance.InsuranceFacade;

/** 
* @ClassName: YoukaAction 
* @Description: 保单操作ACTION 
* @author slong
* @date 2016年5月20日 下午2:34:16 
*  
*/
@Controller
@RequestMapping("/youka")
public class YoukaAction extends BaseAction {
	
	@Autowired
	private InsuranceService insuranceService;
	
	@Resource
	private InsuranceFacade insuranceFacade;
	
	@Value(value="${yk.address}")
	private String remoteUrl;
	@Value(value="${yk.priKey}")
	private String privateKey;

	@RequestMapping(value = "my", method ={RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public RestResponse my(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		return restP;
	}
	
	@RequestMapping(value = "remote/sign", method ={RequestMethod.GET})
	@ResponseBody
	public RestResponse sign(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try {
			String sign = RSASignUtil.generateSHA1withRSASigature(privateKey,user.getMemberId());
			restP.getData().put("id",user.getMemberId());
			restP.getData().put("sign",sign);
		} catch (Exception e) {
			logger.error("油卡签名异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	@RequestMapping(value = "remote/add", method ={RequestMethod.GET})
	public void remoteAdd(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env) throws Exception {
		PersonMember user = getUser(request);
		String url = String.format("%s/gas/card?id=%s&sign=%s",remoteUrl,user.getMemberId(),RSASignUtil.generateSHA1withRSASigature(privateKey,user.getMemberId()));
		resp.sendRedirect(url);
	}
	
	@RequestMapping(value = "remote/recharge", method ={RequestMethod.GET})
	public void remoteManage(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env) throws Exception {
		PersonMember user = getUser(request);
		String url = String.format("%s/gas/home?id=%s&sign=%s",remoteUrl,user.getMemberId(),RSASignUtil.generateSHA1withRSASigature(privateKey,user.getMemberId()));
		resp.sendRedirect(url);
	}
}
