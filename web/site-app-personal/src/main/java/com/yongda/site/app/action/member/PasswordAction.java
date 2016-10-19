package com.yongda.site.app.action.member;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.PayPasswordCheckReq;
import com.netfinworks.site.domain.domain.request.PayPasswordEditReq;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.ResponseUtil;

@Controller
@RequestMapping("/password")
public class PasswordAction extends BaseAction {

	@Resource(name = "payPasswordService")
	private PayPasswordService payPasswordService;
	
	/**
	 * 设置支付密码
	 * @param request
	 * @param resp
	 * @param env
	 * @param payPwd
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "pay/set", method =RequestMethod.POST)
	@ResponseBody
	public RestResponse setPayPwd(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@RequestParam(required=true,value="pwd") String payPwd) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try{
			PayPasswordSetReq req = new PayPasswordSetReq();
			req.setPlatformInfo(createDefaultPlatform(user.getMemberType()));
			req.setAccountId(user.getDefaultAccountId());
			req.setEnviroment(env);
			req.setOperatorId(user.getOperatorId());
			req.setPayPassword(payPwd);
			UnionmaBaseResponse reomotResp = payPasswordService.payPasswordSet(req);
			if(!reomotResp.getIsSuccess()){
				restP = ResponseUtil.buildExpResponse(reomotResp.getResponseCode(), reomotResp.getResponseMessage());
			}
		}catch(Exception e){
			logger.error("设置支付密码异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	@RequestMapping(value = "pay/edit", method =RequestMethod.POST)
	@ResponseBody
	public RestResponse editPayPwd(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@RequestParam(required=true,value="oldpwd") String oldPayPwd
			,@RequestParam(required=true,value="newpwd") String newPayPwd) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		if(StringUtils.equals(oldPayPwd, newPayPwd))
			return ResponseUtil.buildExpResponse("NEWPAYPWD_EQUALS_OLDPWD", "新旧支付密码不能一致");
		try{
			PayPasswordEditReq req = new PayPasswordEditReq();
			req.setEnviroment(env);
			req.setPlatformInfo(createDefaultPlatform(user.getMemberType()));
			req.setAccountId(user.getDefaultAccountId());
			req.setOperatorId(user.getOperatorId());
			req.setNewPayPassword(newPayPwd);
			req.setOldPayPassword(oldPayPwd);
			req.setEnsureNewPayPassword(newPayPwd);
			UnionmaBaseResponse remoteResp = payPasswordService.payPasswordEdit(req);
			if(remoteResp.getIsSuccess()){
			}else{
				restP = ResponseUtil.buildExpResponse(remoteResp.getResponseCode(), remoteResp.getResponseMessage());
			}
		}catch(Exception e){
			logger.error("修改支付密码异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}

	@RequestMapping(value = "pay/check", method =RequestMethod.POST)
	@ResponseBody
	public RestResponse checkPayPwd(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@RequestParam(required=true,value="pwd") String payPwd) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try{
			PayPasswordCheckReq req = new PayPasswordCheckReq();
			req.setPlatformInfo(createDefaultPlatform(user.getMemberType()));
			req.setAccountId(user.getDefaultAccountId());
			req.setEnviroment(env);
			req.setOperatorId(user.getOperatorId());
			req.setPayPassword(payPwd);
			UnionmaBaseResponse reomotResp = payPasswordService.payPasswordCheck(req);
			if(!reomotResp.getIsSuccess()){
				restP = ResponseUtil.buildExpResponse(reomotResp.getResponseCode(), reomotResp.getResponseMessage());
			}
		}catch(Exception e){
			logger.error("校验支付密码异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
}
