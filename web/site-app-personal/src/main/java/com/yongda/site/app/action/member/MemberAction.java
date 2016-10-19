package com.yongda.site.app.action.member;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.info.AuthVerifyInfo;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.MemberPaypasswdStatus;
import com.netfinworks.site.domain.enums.VerifyType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.ResponseUtil;

/**
 * 通用说明：会员信息control
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午10:04:25
 *
 */
@Controller
@RequestMapping("/member")
public class MemberAction extends BaseAction {

    @Resource(name = "authVerifyService")
    private AuthVerifyService    authVerifyService;

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "maQueryService")
    private MaQueryService maQueryService;
	
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	/**
	 * 个人余额查询
	 * @param request
	 * @param resp
	 * @param model
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "balance", method =RequestMethod.GET)
	@ResponseBody
	public RestResponse balanceInfo(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		MemberAccount response = defaultAccountService.queryAccountByMemberId(user.getMemberId(),user.getMemberType().getBaseAccount(), env);
		restP.getData().put("balance", response.getBalance());
		return restP;
	}
	
	/**
	 * 查询基本信息
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "info", method =RequestMethod.GET)
	@ResponseBody
	public RestResponse memberInfo(HttpServletRequest request, HttpServletResponse resp, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try{
			AuthVerifyInfo authVerifyInfo = new AuthVerifyInfo();
			authVerifyInfo.setMemberId(user.getMemberId());
			List<AuthVerifyInfo> infos = authVerifyService.queryVerify(authVerifyInfo, env);
			for (int i = 0; i < infos.size(); i++) {
				authVerifyInfo = infos.get(i);
				if (VerifyType.CELL_PHONE.getCode() == authVerifyInfo.getVerifyType()) {
					restP.getData().put("verifyMobile", authVerifyInfo.getVerifyEntity());//手机号
				} 
			}
			AuthInfoRequest authInfoReq = new AuthInfoRequest();
			authInfoReq.setMemberId(user.getMemberId());
			authInfoReq.setAuthType(AuthType.IDENTITY);
			authInfoReq.setOperator(user.getOperatorId());
			AuthInfo info = defaultCertService.queryRealName(authInfoReq);
			// 屏蔽身份证号码
			if (info.getAuthNo() != null 
					&& AuthResultStatus.CHECK_PASS.getCode().equals(info.getResult().getCode()) 
					&& info.getCertType().getCode().equals("idCard")) {
				int len=info.getAuthNo().length();
				if(len==18){
					restP.getData().put("authNo", StarUtil.hideStrBySym(info.getAuthNo(), 1, 1, 16));//身份证号
					restP.getData().put("sex", getSex(info.getAuthNo()));//性别
				}else if(len==15){
					restP.getData().put("authNo", StarUtil.hideStrBySym(info.getAuthNo(), 1, 1, 13));
					restP.getData().put("sex", getSex(info.getAuthNo()));
				}
			}
	
			restP.getData().put("memberName", StarUtil.mockRealName(user.getMemberName()));//会员名
			String loginName = user.getLoginName();
			restP.getData().put("loginName", loginName.substring(0,3)+ "****" + loginName.substring(7, loginName.length()));//登录名
			
			//是否设置了支付密码
			restP.getData().put("isSetPayPwd", "true");
			if (user.getPaypasswdstatus() == MemberPaypasswdStatus.NOT_SET_PAYPASSWD) {
				restP.getData().put("isSetPayPwd", "false");
			}else if (user.getPaypasswdstatus() == MemberPaypasswdStatus.DEFAULT) {
				restP.getData().put("canChangePayPwd", "true");
			}else{
				restP.getData().put("canChangePayPwd", "false");
			}
		}catch(Exception e){
			logger.error("查询会员基本信息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}

	/**
	 * 判断性别
	 */
	protected String getSex(String identity){
		String number;
		String sex = null;
		if(identity.length()==18){
			number=identity.substring(identity.length()-2, identity.length()-1);
		}else if(identity.length()==15){
			number=identity.substring(identity.length()-1, identity.length());
		}else{
			return sex;
		}
    	sex=number.matches("[13579]")?"男":"女";
    	return sex;
    }
}
