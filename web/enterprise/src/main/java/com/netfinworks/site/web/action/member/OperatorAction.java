package com.netfinworks.site.web.action.member;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.authorize.ws.dataobject.OperatorRole;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.auth.AuthVO;
import com.netfinworks.site.domain.domain.auth.FunctionVO;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAndAccount;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.domain.member.TreeNode;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.OperatorLockStatusEnum;
import com.netfinworks.site.domain.enums.OperatorStatusEnum;
import com.netfinworks.site.domain.enums.OperatorTypeEnum;
import com.netfinworks.site.domain.enums.PlatformTypeEnum;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.auth.inner.MemberAuthInnerService;
import com.netfinworks.site.ext.integration.auth.inner.OperatorAuthInnerService;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.OperatorForm;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.site.web.util.TreeUtil;

@Controller
public class OperatorAction extends BaseAction {
    
    @Resource
    private OperatorService operatorService;
    @Resource
    private MemberAuthInnerService memberAuthInnerService;
    @Resource
    private OperatorAuthOuterService operatorAuthOuterService;
    @Resource
	private OperatorAuthInnerService operatorAuthInnerService;

	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
    @Resource(name = "defaultLoginPasswdService")
    private DefaultLoginPasswdService defaultLoginPasswdService;

	/**
	 * 主页支付密码验证
	 * 
	 * @param request
	 * @param operatorVO
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/operator-verify.htm", method = RequestMethod.GET)
	public ModelAndView verify(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		// 硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            data.put("isCertActive", "yes");
        } else {
            data.put("isCertActive", "no");
        }
        restP.setData(data);
		return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/oper-verify", "response", restP);
	}


	/**
	 * 主页-角色管理
	 * 
	 * @param request
	 * @param operatorVO
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/operator-index.htm", method = RequestMethod.GET)
	public ModelAndView index(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		ModelAndView mv = new ModelAndView();
		try {
			EnterpriseMember user = getUser(request);
			if (!"true".equals(request.getParameter("refresh"))) {
				String password = decrpPassword(request, request.getParameter("pay_pw"));
				deleteMcrypt(request);
				
				String signedData = request.getParameter("signedData");
				if(signedData!=""){
    	            try {
    	                if(!validateSignature(request, password, signedData, null, mv, env)) {
    	                    mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
    	                    return mv;
    	                }
    	            } catch (UnsupportedEncodingException e) {
    	                logger.error("验证证书时编码错误", e);
    	                mv.addObject("message", "您未插入快捷盾或证书已经过期！");
    	                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
    	                return mv;
    	            }
				}
				// 支付密码校验
				PayPasswdRequest payPasswsReq = new PayPasswdRequest();
				payPasswsReq.setOperator(user.getCurrentOperatorId());
				payPasswsReq.setAccountId(user.getDefaultAccountId());
				payPasswsReq.setOldPassword(password);
				payPasswsReq.setValidateType(1);
				PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(payPasswsReq);
				if (!checkResult.isSuccess()) {
					int remainNum = checkResult.getRemainNum();
					Map<String, String> errors = new HashMap<String, String>();
					errors.put("passwd_remainNum", remainNum + "");
					if (checkResult.isLocked()) {
	                    logger.info(LogUtil.generateMsg(OperateTypeEnum.LOCK_PAY_PWD, user, env,
	                            DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
						errors.put("passwd_is_locked", "error_passwd_is_locked");
					} else {
						errors.put("passwd_not_right", "error_passwd_not_right");
					}
					restP.setErrors(errors);
					return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/oper-verify", "response", restP);
				}
				request.getSession().setAttribute(CommonConstant.OPERATOR_TOKEN, CommonConstant.OPERATOR_TOKEN);
			}
			String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
			if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
				return new ModelAndView("redirect://my/operator/operator-verify.htm");
			}
			AuthVO authVO = new AuthVO();
			authVO.setMemberId(user.getMemberId());
			authVO.setSourceId(super.getSourceId());
			authVO.setOperatorType("" + OperatorTypeEnum.ENTERPRISE.getCode());
			authVO.setRequestOperator(user.getCurrentOperatorId());

			List<OperatorRole> roleList = memberAuthInnerService.getOperatorRoleListFromMember(authVO);
			
			AuthVO authVO2 = new AuthVO();
			authVO2.setMemberId(user.getMemberId());
			authVO2.setSourceId(super.getSourceId());
			authVO2.setSourceId(POS_APP);
			authVO2.setOperatorType("" + OperatorTypeEnum.ENTERPRISE.getCode());
			authVO2.setRequestOperator(user.getCurrentOperatorId());

			List<OperatorRole> posRoleList = memberAuthInnerService.getOperatorRoleListFromMember(authVO2);
			roleList.addAll(posRoleList);
			
			data.put("list", roleList);
			restP.setData(data);

		} catch (Exception e) {
			logger.error("进入操作员主页错误：{}", e);
			return new ModelAndView("redirect:/error.htm");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/oper-index", "response", restP);
	}

	/**
	 * 添加角色
	 * 
	 * @param request
	 * @param operatorVO
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/add-role.htm", method = RequestMethod.GET)
	public ModelAndView addRole(HttpServletRequest request, OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		restP.setData(initOcx());
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			return new ModelAndView("redirect://my/operator/operator-verify.htm");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/oper-add-role", "response", restP);
	}

	/**
	 * 添加角色
	 * 
	 * @param request
	 * @param operatorVO
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/do-add-role.htm", method = RequestMethod.POST)
	public ModelAndView doAddRole(HttpServletRequest request, OperationEnvironment env) throws Exception {
		try {
			String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
			if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
				return new ModelAndView("redirect://my/operator/operator-verify.htm");
			}
			
			RestResponse restP = new RestResponse();
			String role_name = request.getParameter("role_name");
			String role_des = request.getParameter("role_des");
			AuthVO authVO = new AuthVO();
			authVO.setMemberId(super.getMemberId(request));
			authVO.setOperatorRoleName(role_name);
			authVO.setMemo(role_des);
			authVO.setSourceId(super.getSourceId());
			
			OperatorRole role = operatorAuthInnerService.createOperatorRole(authVO);
			String role_add = request.getParameter("role_add");
			String roleAddPos = request.getParameter("role_add_POS_APP");
			String roleAddPosCashier = request.getParameter("role_add_POS_CASHIER_APP");
			
			logger.info(LogUtil.generateMsg(OperateTypeEnum.ADD_ROLE, getUser(request), env, 
			        "角色名称：" + role_name + " 权限名称：" + getRoleName(role_add, super.getMemberId(request))));
			
			if (null == role.getRoleId()) {
				restP.setMessage("角色名称已存在！");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/oper-add-role", "response", restP);
			}

			authVO.setOperatorRoleId(role.getRoleId());		
			//添加钱包权限
			addFunctionListToOperatorRole(authVO, role_add, super.getSourceId());
			//添加POS权限
			addFunctionListToOperatorRole(authVO, roleAddPos, POS_APP);
			//添加POS收银权限
			addFunctionListToOperatorRole(authVO, roleAddPosCashier, POS_CASHIER_APP);
			
		} catch (Exception e) {
			logger.error("添加角色失败：{}", e);
			return new ModelAndView("redirect:/error.htm");
		}

		return new ModelAndView("redirect:/my/operator/operator-index.htm?refresh=true");
	}
	
	private void addFunctionListToOperatorRole(AuthVO authVO, String role_add, String sourceId) throws BizException
	{
		String[] roleFuncs = {};
		authVO.setSourceId(sourceId);
		List<String> functionList = new ArrayList<String>();
		Set<String> functionSet = new HashSet<String>();
		if ((role_add != null) && !"".equals(role_add)) {
			roleFuncs = role_add.split(",");
			for (String roleFunc : roleFuncs) {
				functionSet.add(roleFunc);
			}
		}
		functionList.addAll(functionSet);
		authVO.setFunctionList(functionList);
		operatorAuthInnerService.addFunctionListToOperatorRole(authVO);
	}
	
	/**
	 * 根据权限缩写获取角色名称
	 * @param roleAdd
	 * @param memberId
	 * @return
	 */
	private String getRoleName(String roleAdd, String memberId) {
        AuthVO authVO = new AuthVO();
        authVO.setMemberId(memberId);
        authVO.setSourceId(super.getSourceId());
        try {
            List<FunctionVO> funcList = memberAuthInnerService.getFunctionListFromMember(authVO);
            if(CollectionUtils.isEmpty(funcList)) {
                return roleAdd;
            }
            
            Map<String, String> map = new HashMap<String, String>();
            for(FunctionVO function : funcList) {
                map.put(function.getFunctionId(), function.getFunctionName());
            }
            
            StringBuffer sb = new StringBuffer();
            String [] roleFuncs = roleAdd.split(",");
            for(String role : roleFuncs) {
                sb.append(map.containsKey(role) ? map.get(role) : role).append(",");
            }
            
            String result = sb.toString();
            if(result.length() > 0) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        }
        catch (BizException e) {
            logger.error("查询角色名称异常：" + e.getMessage(), e);
            return roleAdd;
        }
	}

	/**
	 * 修改角色
	 * 
	 * @param request
	 * @param operatorVO
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/modify-role.htm", method = RequestMethod.POST)
	public ModelAndView modifyRole(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			return new ModelAndView("redirect://my/operator/operator-verify.htm");
		}
		RestResponse restP = new RestResponse();
		String modifyType = request.getParameter("mofifyType");
		Map<String, Object> data = initOcx();
		restP.setData(data);

		EnterpriseMember user = getUser(request);
		data.put("memberId", user.getMemberId());
		String roleId = request.getParameter("roleId");
		data.put("roleId", roleId);
		data.put("roleName", request.getParameter("roleName"));
		data.put("memo", request.getParameter("memo"));

		AuthVO authVO = new AuthVO();
		authVO.setMemberId(super.getMemberId(request));
		authVO.setOperatorRoleId(roleId);
		authVO.setSourceId(super.getSourceId());
		try {
			if (modifyType.equals("modifyFunc")) {
				List<FunctionVO> curFuncList = operatorAuthInnerService.getFunctionListFromOperatorRole(authVO);
				
				authVO.setSourceId(POS_APP);
				List<FunctionVO> posAppFuncList = operatorAuthInnerService.getFunctionListFromOperatorRole(authVO);
				curFuncList.addAll(posAppFuncList);
				
				authVO.setSourceId(POS_CASHIER_APP);
				List<FunctionVO> posCashierAppFuncList = operatorAuthInnerService.getFunctionListFromOperatorRole(authVO);
				curFuncList.addAll(posCashierAppFuncList);
				
				String curFuncStr = "";
				for (FunctionVO vo : curFuncList) {
					curFuncStr += vo.getFunctionId() + ",";
				}
				if (!"".equals(curFuncStr)) {
					curFuncStr = curFuncStr.substring(0, curFuncStr.length() - 1);
				}
				data.put("curFuncStr", curFuncStr);
			}
		} catch (Exception e) {
			logger.error("查询操作员当前功能列表失败：{}", e);
			return new ModelAndView("redirect:/error.htm");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/oper-modify-role", "response", restP);

	}

	/**
	 * 修改角色
	 * 
	 * @param request
	 * @param operatorVO
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/do-modify-role.htm", method = RequestMethod.POST)
	public ModelAndView doModifyRole(HttpServletRequest request, OperationEnvironment env) throws Exception {
		try {
			String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
			if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
				return new ModelAndView("redirect://my/operator/operator-verify.htm");
			}
			String role_name = request.getParameter("role_name");
			String role_des = request.getParameter("role_des");
			String role_id = request.getParameter("role_id");
			AuthVO authVO = new AuthVO();
			authVO.setMemberId(super.getMemberId(request));
			authVO.setOperatorRoleId(role_id);
			authVO.setOperatorRoleName(role_name);
			authVO.setMemo(role_des);
			authVO.setSourceId(super.getSourceId());

			operatorAuthInnerService.updateOperatorRoleName(authVO);
			String role_add = request.getParameter("role_add");
			String role_del = request.getParameter("role_del");
			
			String roleAddPos = request.getParameter("role_add_POS_APP");
			String roleDelPos = request.getParameter("role_del_POS_APP");
			
			String roleAddPosCashier = request.getParameter("role_add_POS_CASHIER_APP");
			String roleDelPosCashier = request.getParameter("role_del_POS_CASHIER_APP");
			
            logger.info(LogUtil.generateMsg(OperateTypeEnum.MOD_ROLE, getUser(request), env,
                    "角色名称：" + role_name 
                        + " 新增权限：" + getRoleName(role_add, super.getMemberId(request)) + " 删除权限:"
                            + getRoleName(role_del, super.getMemberId(request))));

            addAndRemoveFunctionListForOperatorRole(authVO, role_add, role_del, super.getSourceId());
            
            addAndRemoveFunctionListForOperatorRole(authVO, roleAddPos, roleDelPos, POS_APP);
            
            addAndRemoveFunctionListForOperatorRole(authVO, roleAddPosCashier, roleDelPosCashier, POS_CASHIER_APP);
			
		} catch (Exception e) {
			logger.error("修改角色失败：{}", e);
			return new ModelAndView("redirect:/error.htm");
		}
		return new ModelAndView("redirect:/my/operator/operator-index.htm?refresh=true");
	}
	
	
	private void addAndRemoveFunctionListForOperatorRole(AuthVO authVO, String role_add, String role_del, String sourceId) throws BizException
	{
		String[] roleFuncs = {};
		authVO.setSourceId(sourceId);
		List<String> addFunctionList = new ArrayList<String>();
		List<String> removeFunctionList = new ArrayList<String>();
		if ((role_add != null) && !"".equals(role_add)) {
			roleFuncs = role_add.split(",");
			for (String roleFunc : roleFuncs) {
				addFunctionList.add(roleFunc);
			}
		}
		if ((role_del != null) && !"".equals(role_del)) {
			roleFuncs = role_del.split(",");
			for (String roleFunc : roleFuncs) {
				removeFunctionList.add(roleFunc);
			}
		}
		if (addFunctionList.size() > 0) {
			authVO.setFunctionList(addFunctionList);
			operatorAuthInnerService.addFunctionListToOperatorRole(authVO);
		}

		if (removeFunctionList.size() > 0) {
			authVO.setFunctionList(removeFunctionList);
			operatorAuthInnerService.removeFunctionListFromOperatorRole(authVO);
		}
	}
	

	/**
	 * 删除角色
	 * 
	 * @param request
	 * @param operatorVO
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/delete-role.htm", method = RequestMethod.POST)
	public ModelAndView deleteRole(HttpServletRequest request, OperationEnvironment env) throws Exception {
		try {
			String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
			if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
				return new ModelAndView("redirect://my/operator/operator-verify.htm");
			}
            logger.info(LogUtil.generateMsg(OperateTypeEnum.DEL_ROLE, getUser(request), env,
                    "删除角色：" + request.getParameter("roleId")));
			AuthVO authVO = new AuthVO();
			authVO.setMemberId(super.getMemberId(request));
			authVO.setOperatorRoleId(request.getParameter("roleId"));
			authVO.setMemo("删除操作员角色");
			String sourceId = request.getParameter("sourceId");
			if (StringUtils.isEmpty(sourceId))
			{
				sourceId = super.getSourceId();
			}
			authVO.setSourceId(sourceId);
			operatorAuthInnerService.removeOperatorRole(authVO);
		} catch (Exception e) {
			logger.error("删除角色失败：{}", e);
			return new ModelAndView("redirect:/error.htm");
		}
		return new ModelAndView("redirect:/my/operator/operator-index.htm?refresh=true");

	}
	
	/**
	 * 获取成员完整功能列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/operator/getAllFunctionList.htm", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse getAllFunctionList(HttpServletRequest req) {
		RestResponse restP = new RestResponse();
		String token = (String) req.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			logger.error("令牌失效！");
			return restP;
		}

		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		AuthVO authVO = new AuthVO();
		authVO.setMemberId(super.getMemberId(req));
		String sourceId = req.getParameter("sourceId");
		if (StringUtils.isEmpty(sourceId))
		{
			sourceId = super.getSourceId();
		}
		authVO.setSourceId(sourceId);
		List<FunctionVO> funcList;
		try {
			funcList = memberAuthInnerService.getFunctionListFromMember(authVO);
			restP.setMessageObj(funcList);
			restP.setSuccess(true);
		} catch (BizException e) {
			logger.error("", e);
		}
		return restP;
	}

	/**
	 * 获取单个操作员角色
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/operator/getOperatorByRole.htm", method = RequestMethod.GET)
	@ResponseBody
	public RestResponse getOperatorByRole(HttpServletRequest req) {
		RestResponse restP = new RestResponse();
		String token = (String) req.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			logger.error("令牌失效！");
			return restP;
		}
		Map<String, Object> data = new HashMap<String, Object>();
		restP.setData(data);
		AuthVO authVO = new AuthVO();
		authVO.setMemberId(super.getMemberId(req));
		authVO.setOperatorId(req.getParameter("operId"));
		authVO.setSourceId(super.getSourceId());
		List<OperatorRole> curRoleList;
		List<OperatorRole> allRoleList;
		try {
			curRoleList = operatorAuthInnerService.getRoleListFromOperator(authVO);
			data.put("curRoleList", curRoleList);
			allRoleList = memberAuthInnerService.getOperatorRoleListFromMember(authVO);
			data.put("allRoleList", allRoleList);

			String curRoleStr = "";
			for (OperatorRole vo : curRoleList) {
				curRoleStr += vo.getRoleId() + ",";
			}
			if (!"".equals(curRoleStr)) {
				curRoleStr = curRoleStr.substring(0, curRoleStr.length() - 1);
			}
			data.put("curRoleStr", curRoleStr);

			restP.setSuccess(true);
		} catch (BizException e) {
			logger.error("", e);
		}
		return restP;
	}

	/**
	 * 分配单个操作员角色
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/operator/updateOperatorRole.htm", method = RequestMethod.GET)
	@ResponseBody
	public RestResponse updateOperatorRole(HttpServletRequest req) {

		RestResponse restP = new RestResponse();
		String token = (String) req.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			logger.error("令牌失效！");
			return restP;
		}
		String oper_id = req.getParameter("operId");
		AuthVO authVO = new AuthVO();
		authVO.setMemberId(super.getMemberId(req));
		authVO.setOperatorId(oper_id);
		authVO.setMemo("更新操作员角色");
		authVO.setSourceId(super.getSourceId());

		String role_add = req.getParameter("role_add");
		String role_del = req.getParameter("role_del");
		String[] roleFuncs = {};
		try {
			if ((role_add != null) && !"".equals(role_add)) {
				roleFuncs = role_add.split(",");
				for (String roleFunc : roleFuncs) {
					authVO.setOperatorRoleId(roleFunc);
					operatorAuthInnerService.addRoleToOperator(authVO);
				}
			}
			if ((role_del != null) && !"".equals(role_del)) {
				roleFuncs = role_del.split(",");
				for (String roleFunc : roleFuncs) {
					authVO.setOperatorRoleId(roleFunc);
					operatorAuthInnerService.removeRoleFromOperator(authVO);
				}
			}
			restP.setSuccess(true);
		} catch (BizException e) {
			logger.error("", e);
		}
		return restP;
	}

	/**
	 * 操作员管理主页
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/manage-index.htm", method = RequestMethod.GET)
	public ModelAndView operManage(HttpServletRequest request, OperationEnvironment env) throws Exception {
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			return new ModelAndView("redirect://my/operator/operator-verify.htm");
		}
		RestResponse restP = new RestResponse();
		restP.setData(initOcx());
		ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX + "/operator/manage-index","response",restP);
		return modelAndView;
	}
	
	/**
	 * 修改密码
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/setPassword.htm", method = RequestMethod.POST)
	public ModelAndView setPassword(HttpServletRequest request, OperatorForm operatorForm, BindingResult result,
			OperationEnvironment env) throws Exception {
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			return new ModelAndView("redirect://my/operator/operator-verify.htm");
		}
		EnterpriseMember user = getUser(request);
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		Map<String, String> errorMap = new HashMap<String, String>();
		restP.setData(data);
 
		try {
			
			String loginPwd = decrpPassword(request, operatorForm.getLoginPwd());
			String loginPwdConfirm = decrpPassword(request, operatorForm.getLoginPwdConfirm());
			String payPwd = decrpPassword(request, operatorForm.getPayPwd());
			String payPwdConfirm = decrpPassword(request, operatorForm.getPayPwdConfirm());

			// 登陆密码和确认密码不一致
			if (!loginPwd.equals(loginPwdConfirm)) {
				errorMap.put("op_login_pwd_re_not_equal", "op_login_pwd_re_not_equal");
				restP.setErrors(errorMap);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/manage-index", "response", restP);
			}
			// 支付密码和确认密码不一致
			if (!payPwd.equals(payPwdConfirm)) {
				errorMap.put("op_pay_pwd_re_not_equal", "op_pay_pwd_re_not_equal");
				restP.setErrors(errorMap);
				return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/manage-index", "response", restP);
			}
			
			OperatorLoginPasswdRequest req = new OperatorLoginPasswdRequest();
			req.setOperatorId(operatorForm.getOperatorId());
			req.setValidateType(1);
			req.setPassword(loginPwd);
			req.setClientIp(request.getRemoteAddr());
			
			CommResponse commRep = defaultLoginPasswdService.setLoginPasswordEnt(req);
			
			if (commRep.isSuccess()) {
				restP.setSuccess(true);
			} else {
				restP.setSuccess(false);
				logger.error("用户修改登录密码失败:{}", user);
				if (ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(commRep.getResponseCode())) {
					errorMap.put("login_password_equal_pay", "login_password_equal_pay");
					restP.setErrors(errorMap);
					return new ModelAndView(
							CommonConstant.URL_PREFIX + ResourceInfo.RESET_LOGINPASSWD.getUrl(), "response",
							restP);
				} else {
					restP.setMessage("设置登录密码失败");
				}
			}
			
			PayPasswdRequest pReq = new PayPasswdRequest();
			
			pReq.setPassword(payPwd);
			pReq.setAccountId(user.getDefaultAccountId());
			pReq.setOperator(operatorForm.getOperatorId());
			pReq.setClientIp(request.getRemoteAddr());
			
			commRep = defaultPayPasswdService.setPayPassword(pReq);
			if (!commRep.isSuccess()) {
				if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(commRep.getResponseCode())) {
					restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
				} else {
					restP.setMessage("支付密码设置失败");
				}
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}
			
		} catch (ServiceException e) {
			logger.error("", e);
			
		}
		
		return new ModelAndView("redirect:/my/operator/manage-index.htm");
	}
	
	
	/**
	 * 新增操作员
	 * 
	 * @param request
	 * @param env
	 * @return
	 * @throws BizException
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/operator/addOperator.htm", method = RequestMethod.POST)
	public ModelAndView addOperator(HttpServletRequest request, @Valid OperatorForm operatorForm, BindingResult result,
			OperationEnvironment env) throws BizException {
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			return new ModelAndView("redirect://my/operator/operator-verify.htm");
		}
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		Map<String, String> errorMap = new HashMap<String, String>();
		restP.setData(data);
		// 表单验证失败
		if (result.hasErrors()) {
			Map<String, String> errors = initError(result.getAllErrors());
			restP.setErrors(errors);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/manage-index", "response", restP);
		}
		
		String operType = request.getParameter("operType");
		String role_add = request.getParameter("role_add");
		
        if (operType.equals("update")) {
            logger.info(LogUtil.generateMsg(OperateTypeEnum.MOD_OPERATOR, getUser(request), env,
                    "修改项：" + getModItem(operatorForm)));
        }
        else {
            logger.info(LogUtil.generateMsg(
                    OperateTypeEnum.ADD_OPERATOR,
                    getUser(request), env, "帐号："
                            + operatorForm.getLoginName()
                            + " 状态："
                            + OperatorStatusEnum.getByCode(
                                    Integer.parseInt(operatorForm.getStatus())).getMsg() + " 角色："
                            + role_add));
        }

		String loginPwd = decrpPassword(request, operatorForm.getLoginPwd());
		String loginPwdConfirm = decrpPassword(request, operatorForm.getLoginPwdConfirm());
		String payPwd = decrpPassword(request, operatorForm.getPayPwd());
		String payPwdConfirm = decrpPassword(request, operatorForm.getPayPwdConfirm());

		// 登陆密码和确认密码不一致
		if (!loginPwd.equals(loginPwdConfirm)) {
			errorMap.put("op_login_pwd_re_not_equal", "op_login_pwd_re_not_equal");
			restP.setErrors(errorMap);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/manage-index", "response", restP);
		}
		// 支付密码和确认密码不一致
		if (!payPwd.equals(payPwdConfirm)) {
			errorMap.put("op_pay_pwd_re_not_equal", "op_pay_pwd_re_not_equal");
			restP.setErrors(errorMap);
			return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/manage-index", "response", restP);
		}
		OperatorVO operatorVO = new OperatorVO();
		operatorVO.setNickName(operatorForm.getNickName());
		operatorVO.setLoginName(operatorForm.getLoginName());
		operatorVO.setContact(operatorForm.getContact());
		operatorVO.setLockStatusEnum(OperatorLockStatusEnum.getByCode(Integer.parseInt(operatorForm.getStatus())));
		operatorVO.setLoginPwd(loginPwd);
		Map map = new HashMap();
		map.put("memo", operatorForm.getRemark());
		operatorVO.setExtention(JSONObject.toJSONString(map));

		String memberId = super.getMemberId(request);
		EnterpriseMember user = getUser(request);

		PayPasswdRequest req = new PayPasswdRequest();

		if (operType.equals("update")) {
			operatorVO.setOperatorId(operatorForm.getOperatorId());
			String extention = request.getParameter("extention");
			Map jsonMap = JSONObject.parseObject(extention);
			jsonMap.put("memo", operatorForm.getRemark());
			operatorVO.setExtention(JSONObject.toJSONString(jsonMap));

			operatorService.updateOperator(operatorVO, env);
			if (loginPwd != "") {
				try {
					operatorService.resetLoginPwd(operatorForm.getOperatorId(), loginPwd, env);
				} catch (Exception e) {
					restP.setMessage(e.getMessage());
					return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
				}
			}
			req.setOperator(operatorForm.getOperatorId());
		} else {
			boolean isExist = operatorService.checkLoginNameExist(memberId, operatorVO.getLoginName(),
					String.valueOf(operatorVO.getPlatformTypeEnum().getCode()), env);
			// 登陆名已经存在,返回报错
			if (isExist) {
				restP.setMessage("用户账号已存在");
				return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
			}

			operatorVO.setMemberId(memberId);
			MemberAndAccount memberAndAccount = operatorService.addOperator(operatorVO, env);
			
			req.setOperator(memberAndAccount.getOperatorId());

		}
		req.setPassword(payPwd);
		req.setAccountId(user.getDefaultAccountId());

		req.setClientIp(request.getRemoteAddr());
		if (payPwd != "") {
			CommResponse commRep;
			try {
				commRep = defaultPayPasswdService.setPayPassword(req);
				if (!commRep.isSuccess()) {
					if (ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(commRep.getResponseCode())) {
						restP.setMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
					} else {
						restP.setMessage("支付密码设置失败");
					}
					return new ModelAndView(CommonConstant.URL_PREFIX + "/error", "response", restP);
				}
			} catch (ServiceException e) {
				logger.error("", e);
			}
		}

		AuthVO authVO = new AuthVO();
		authVO.setMemberId(super.getMemberId(request));
		authVO.setOperatorId(req.getOperator());
		authVO.setMemo("分配操作员角色");
		authVO.setSourceId(super.getSourceId());
		String[] roleFuncs = {};
		try {
			if ((role_add != null) && !"".equals(role_add)) {
				roleFuncs = role_add.split(",");
				for (String roleFunc : roleFuncs) {
					authVO.setOperatorRoleId(roleFunc);
					operatorAuthInnerService.addRoleToOperator(authVO);
				}
			}
		} catch (BizException e) {
			logger.error("", e);
		}

		return new ModelAndView("redirect:/my/operator/manage-index.htm");
	}
	
    /**
     * 返回修改了哪些项
     * 
     * @param operatorForm
     * @return
     */
    private String getModItem(OperatorForm operatorForm) {
        StringBuffer sb = new StringBuffer();
        if(StringUtils.isNotEmpty(operatorForm.getOperatorId())) {
            sb.append(" 操作员[").append(operatorForm.getOperatorId()).append("]"); 
        }
        if (StringUtils.isNotEmpty(operatorForm.getLoginName())) {
            sb.append(" 用户账号 ");
        }
        
        if (StringUtils.isNotEmpty(operatorForm.getNickName())) {
            sb.append(" 用户姓名 ");
        }
        
        if (StringUtils.isNotEmpty(operatorForm.getLoginPwd())) {
            sb.append(" 登录密码 ");
        }
        if (StringUtils.isNotEmpty(operatorForm.getPayPwd())) {
            sb.append(" 支付密码 ");
        }

        if (StringUtils.isNotEmpty(operatorForm.getRemark())) {
            sb.append(" 备注 ");
        }

        return sb.toString();

    }

	/**
	 * 删除操作员
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/my/operator/delOperator.htm")
	public RestResponse delOperator(HttpServletRequest request, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			logger.error("令牌失效！");
			return response;
		}
		logger.info(LogUtil.generateMsg(OperateTypeEnum.DEL_OPERATOR, getUser(request), env, "操作员ID：" + request.getParameter("id")));
		try {
			operatorService.closeOperator(request.getParameter("id"), env);
			response.setSuccess(true);
		} catch (BizException e) {
			logger.error("", e);
		}

		return response;
	}

	/**
	 * 查询所有操作员
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/my/operator/queryOperators.htm")
	public RestResponse queryOperators(HttpServletRequest request, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			logger.error("令牌失效！");
			return response;
		}
		String memberId = super.getMemberId(request);
		OperatorVO operatorVO = new OperatorVO();

		operatorVO.setMemberId(memberId);
		operatorVO.setStatusEnum(OperatorStatusEnum.NORMAL);

		List<OperatorVO> operatorVOList;
		try {
			operatorVOList = operatorService.queryOperatorLoginInfos(operatorVO, env);
			response.setMessageObj(operatorVOList);
			response.setSuccess(true);
		} catch (BizException e) {
			logger.error("", e);
		}
		return response;
	}

	/**
	 * 查询单个操作员信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/my/operator/querySingleOperator.htm")
	public RestResponse querySingleOperator(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		String token = (String) request.getSession().getAttribute(CommonConstant.OPERATOR_TOKEN);
		if (!token.equals(CommonConstant.OPERATOR_TOKEN)) {
			logger.error("令牌失效！");
			return response;
		}
		String id = request.getParameter("operId");
		EnterpriseMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
		OperatorVO vo;
		try {
			vo = operatorService.getOperatorById(id, env);
			if (null == vo.getNickName()) {
				vo.setNickName("");
			}
			response.setMessageObj(vo);
			data.put("status", vo.getLockStatusEnum().getCode());
			AuthVO authVO = new AuthVO();
			authVO.setMemberId(super.getMemberId(request));
			authVO.setOperatorId(id);
			authVO.setSourceId(super.getSourceId());
			List<OperatorRole> roleList = operatorAuthInnerService.getRoleListFromOperator(authVO);
			if ((roleList != null) && (roleList.size() > 0)) {
				data.put("role", roleList.get(0).getRoleName());
			} else {
				data.put("role", "");
			}
			
			if (user.getOperatorId().equals(id)) {
				data.put("isDefault", "true");
			} else {
				data.put("isDefault", "false");
			}
			

			Map map = JSONObject.parseObject(vo.getExtention());
			String remark = (String) map.get("memo");
			data.put("remark", remark == null ? "" : remark);

			response.setData(data);
			response.setSuccess(true);

		} catch (BizException e) {
			logger.error("", e);
		}
		return response;
	}

    /**
     * 操作员管理主页
     *
     */
    @RequestMapping(value = "/my/operator/manage.htm")
    public ModelAndView manage(HttpServletRequest request,OperatorVO operatorVO,OperationEnvironment env) throws Exception {
        //鉴权
        if(!super.auth(request, "EW_OP_MENAG")){
            return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
        }
        Map<String,FunctionVO> functionVOs=super.auth(request);
        String memberId=super.getMemberId(request);
        operatorVO.setMemberId(memberId);
        operatorVO.setStatusEnum(OperatorStatusEnum.NORMAL);
        List<OperatorVO> operatorVOList=operatorService.queryOperatorLoginInfos(operatorVO,env);
        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX + "/operator/manage");
        modelAndView.addObject("operatorVOList", operatorVOList);
        modelAndView.addObject("lockStatusEnums", OperatorLockStatusEnum.values());
        EnterpriseMember user = getUser(request);
        modelAndView.addObject("member", user);
        modelAndView.addObject("queryCondition", operatorVO);
        modelAndView.addObject("functionVOs", functionVOs);
        RestResponse restP = new RestResponse();
        restP.setData(new HashMap<String, Object>());
        restP.getData().put("member", super.getUser(request));
        modelAndView.addObject("response", restP);
        return modelAndView;
    }
    
    @RequestMapping(value="/my/operator/goResetLoginPwd.htm",method = RequestMethod.GET)
    public ModelAndView goResetLoginPwd(HttpServletRequest request,String operatorId,OperationEnvironment env) throws Exception {
        //鉴权
        if(!super.auth(request, "EW_OP_RESET_LOGINPWD")){
            return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
        }
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        OperatorVO operatorVO=operatorService.getOperatorById(operatorId, env);
        data.put("operatorVO", operatorVO);
        restP.setData(data);
        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX + "/operator/resetLoginPwd","response", restP);
        return modelAndView;
    }
    
    @RequestMapping(value="/my/operator/resetLoginPwd.htm",method = RequestMethod.POST)
    public ModelAndView resetLoginPwd(HttpServletRequest request,@Valid OperatorForm operatorForm,BindingResult result,OperationEnvironment env) throws Exception {
        //鉴权
        if(!super.auth(request, "EW_OP_RESET_LOGINPWD")){
            return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
        }
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        OperatorVO operatorVO=OperatorForm.convertVO(operatorForm);
        data.put("operatorVO", operatorVO);
        restP.setData(data);
        Map<String, String> errors = new HashMap<String, String>();
        if(result.hasErrors()){
            errors = initError(result.getAllErrors());
            restP.setErrors(errors); 
            return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/resetLoginPwd","response", restP);
        }
        //登陆密码和确认密码不一致
        if(!operatorForm.getLoginPwd().equals(operatorForm.getLoginPwdConfirm())){
            errors.put("op_login_pwd_re_not_equal", "op_login_pwd_re_not_equal");
            restP.setErrors(errors);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/resetLoginPwd","response", restP);
        }
        //解密
        String newPassword=super.decrpPassword(request, operatorForm.getLoginPwd());
        //清除随机因子
        super.deleteMcrypt(request);
        if(operatorService.resetLoginPwd(operatorForm.getOperatorId(), newPassword, env)){
            return new ModelAndView("redirect:/my/operator/manage.htm");
        }else{
            errors.put("op_reset_login_pwd_fail", "op_reset_login_pwd_fail");
            restP.setErrors(errors); 
            return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/resetLoginPwd","response", restP);
        }
        
    }
    
    @RequestMapping(value="/my/operator/show.htm",method = RequestMethod.GET)
    public ModelAndView show(HttpServletRequest request,String operatorId,OperationEnvironment env) throws Exception {
        //鉴权
        if(!super.auth(request, "EW_OP_DETAIL")){
            return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
        }
        OperatorVO operatorVO=operatorService.getOperatorById(operatorId, env);
        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX + "/operator/show","operatorVO",operatorVO);
        return modelAndView;
    }
    
    @RequestMapping(value="/my/operator/goUpdate.htm",method = RequestMethod.GET)
    public ModelAndView goUpdate(HttpServletRequest request,String operatorId,OperationEnvironment env) throws Exception {
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        OperatorVO operatorVO=operatorService.getOperatorById(operatorId, env);
        data.put("operatorVO", operatorVO);
        restP.setData(data);
        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX + "/operator/update","response", restP);
        return modelAndView;
    }
    
    @RequestMapping(value="/my/operator/update.htm",method = RequestMethod.POST)
    public ModelAndView update(HttpServletRequest request,@Valid OperatorForm operatorForm,BindingResult result,OperationEnvironment env) throws BizException{
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        OperatorVO operatorVO=OperatorForm.convertVO(operatorForm);
        data.put("operatorVO", operatorVO);
        restP.setData(data);
        if(result.hasErrors()){
            Map<String, String> errors = initError(result.getAllErrors());
            restP.setErrors(errors);  
            return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/update","response", restP);
        }
        operatorService.updateNickname(operatorForm.getOperatorId(), operatorForm.getNickName(), env);
        return new ModelAndView("redirect:/my/operator/manage.htm");
    }
    
    /**
     * 跳转新增操作员页面
     * @param request
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/my/operator/toAdd.htm",method = RequestMethod.GET)
    public ModelAndView toAdd(HttpServletRequest request,OperationEnvironment env) throws Exception {
		// //鉴权
		// if(!super.auth(request, "EW_OP_ADD")){
		// return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
		// }
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		restP.setData(data);
		AuthVO authVO = new AuthVO();
		authVO.setMemberId(super.getMemberId(request));
		authVO.setSourceId(super.getSourceId());
		List<OperatorRole> allRoleList;
		try {
			allRoleList = memberAuthInnerService.getOperatorRoleListFromMember(authVO);
			data.put("allRoleList", allRoleList);
		} catch (BizException e) {
			logger.error("", e);
		}
		ModelAndView modelAndView = new ModelAndView(CommonConstant.URL_PREFIX + "/operator/add", "response", restP);
        return modelAndView;
    }
    
    /**
     * 新增操作员
     * @param request
     * @param env
     * @return
     * @throws BizException 
     * @throws Exception
     */
    @RequestMapping(value="/my/operator/add.htm",method = RequestMethod.POST)
    public ModelAndView add(HttpServletRequest request,@Valid OperatorForm operatorForm,BindingResult result,OperationEnvironment env) throws BizException{
        //鉴权
        if(!super.auth(request, "EW_OP_ADD")){
            return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
        }
        RestResponse restP = new RestResponse();
        Map<String, Object> data = initOcx();
        Map<String, String> errorMap = new HashMap<String, String>();
        data.put("loginName", operatorForm.getLoginName());
        data.put("nickName", operatorForm.getNickName());
        restP.setData(data);
        //表单验证失败
        if(result.hasErrors()){
            Map<String, String> errors = initError(result.getAllErrors());
            restP.setErrors(errors);  
            return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/add","response", restP);
        }
        //登陆密码和确认密码不一致
        if(!operatorForm.getLoginPwd().equals(operatorForm.getLoginPwdConfirm())){
            errorMap.put("op_login_pwd_re_not_equal", "op_login_pwd_re_not_equal");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/add","response", restP);
        }
        OperatorVO operatorVO=OperatorForm.convertVO(operatorForm);
        String memberId=super.getMemberId(request);
        boolean isExist=operatorService.checkLoginNameExist(memberId, operatorVO.getLoginName(), String.valueOf(operatorVO.getPlatformTypeEnum().getCode()),env);
        //登陆名已经存在,返回报错
        if(isExist){
            errorMap.put("op_login_name_is_exist", "op_login_name_is_exist");
            restP.setErrors(errorMap);
            return new ModelAndView(CommonConstant.URL_PREFIX + "/operator/add","response", restP);
        }
        operatorVO.setMemberId(memberId);
        //解密
        operatorVO.setLoginPwd(super.decrpPassword(request, operatorForm.getLoginPwd()));
        //清空随机key
        super.deleteMcrypt(request);
        MemberAndAccount memberAndAccount=operatorService.addOperator(operatorVO, env);
        //没有设置权限则跳转首页
        if(!super.auth(request, "EW_OP_SET_PERMISSION")){
            return new ModelAndView("redirect:/my/operator/manage.htm");
        }
        return new ModelAndView("redirect:/my/operator/goSetPermission.htm?operatorId="+memberAndAccount.getOperatorId());
    }
    
    @ResponseBody
    @RequestMapping(value="/my/operator/statusToggle.htm")
    public RestResponse close(HttpServletRequest request,String operatorId,String status,OperationEnvironment env){
        RestResponse restResp=new RestResponse();
        //鉴权
        if(!super.auth(request, "EW_OP_DEL")){
            restResp.setSuccess(false);
            restResp.setMessage("您没有权限做此操作");
            return restResp;
        }
        
        if(StringUtils.isEmpty(operatorId)||StringUtils.isEmpty(status)){
            restResp.setSuccess(false);
            restResp.setMessage("参数不能为空");
            return restResp;
        }
        try {
            //当前状态为正常状态，转为删除状态
            if(status.equals(String.valueOf(OperatorStatusEnum.NORMAL.getCode()))){
                operatorService.closeOperator(operatorId, env);
                restResp.setSuccess(true);
                restResp.setMessage("解锁成功");
            }
            //当前状态为删除或者未激活状态,转为正常状态
            else if(status.equals(String.valueOf(OperatorStatusEnum.NOT_ACTIVE.getCode()))
                    ||status.equals(String.valueOf(OperatorStatusEnum.CLOSE.getCode()))){
                operatorService.activateOperator(operatorId, env);
                restResp.setSuccess(true);
                restResp.setMessage("解锁成功");
            }
            else{
                restResp.setSuccess(false);
                restResp.setMessage("参数错误");
            }
        } catch (BizException e) {
            logger.error("系统异常",e);
            restResp.setSuccess(false);
            restResp.setMessage(e.getMessage());
        }
        return restResp;
    }
    
    @RequestMapping(value="/my/operator/goSetPermission.htm")
    public ModelAndView goSetPermission(HttpServletRequest request,String operatorId,OperationEnvironment env) throws BizException{
        //鉴权
        if(!super.auth(request, "EW_OP_SET_PERMISSION")){
            return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
        }
        String memberId=super.getMemberId(request);
        String sourceId=super.getSourceId();
        AuthVO authVO=new AuthVO(memberId, operatorId, "",sourceId,"");
        List<FunctionVO> allFunctions=memberAuthInnerService.getFunctionListFromMember(authVO);
		List<FunctionVO> assignFunctions = operatorAuthOuterService.getFunctionListFromOperator(authVO);
        Map<String,FunctionVO> assignFuncMap=new HashMap<String,FunctionVO>();
        if(assignFunctions!=null){
            //转化成TreeNode list
            for(FunctionVO functionVO:assignFunctions){
                assignFuncMap.put(functionVO.getFunctionId(), functionVO);
            }
        }
        List<TreeNode> treeNodeList=new ArrayList<TreeNode>();
        if(allFunctions!=null){
            //转化成TreeNode list
            for(FunctionVO functionVO:allFunctions){
                TreeNode treeNode=new TreeNode();
                treeNode.setId(functionVO.getFunctionId());
                treeNode.setName(functionVO.getFunctionName());
                treeNode.setPid(functionVO.getParentId());
                treeNode.setSort(functionVO.getSort());
                if(assignFuncMap.get(functionVO.getFunctionId())!=null){
                    treeNode.setChecked(true);
                }
                treeNodeList.add(treeNode);
            }
        }
        String treeNodes=TreeUtil.convertTreeJson(treeNodeList);
        OperatorVO operatorVO=operatorService.getOperatorById(operatorId, env);
        ModelAndView modelAndView=new ModelAndView(CommonConstant.URL_PREFIX + "/operator/setPermission","treeNodes",treeNodes);
        modelAndView.addObject("operatorVO", operatorVO);
        return modelAndView;
    }
    
    @RequestMapping(value="/my/operator/setPermission.htm")
    public ModelAndView setPermission(HttpServletRequest request,String operatorId,String changeCheckIds,String changeNoCheckIds,OperationEnvironment env) throws BizException{
        //鉴权
        if(!super.auth(request, "EW_OP_SET_PERMISSION")){
            return new ModelAndView(CommonConstant.URL_PREFIX + "/permissionReject");
        }
        String memberId=super.getMemberId(request);
        String sourceId=super.getSourceId();
        AuthVO authVO=new AuthVO(memberId, operatorId, "",sourceId,"");
        if(StringUtils.isNotBlank(changeCheckIds)){
            String[] addFunctionIds=changeCheckIds.split(",");
            for (String addFunctionId : addFunctionIds) {
                authVO.setFunctionId(addFunctionId);
                operatorAuthOuterService.addFunctionToOperator(authVO);
            }
        }
        if(StringUtils.isNotBlank(changeNoCheckIds)){
            String[] removeFunctionIds=changeNoCheckIds.split(",");
            for (String removeFunctionId : removeFunctionIds) {
                authVO.setFunctionId(removeFunctionId);
                operatorAuthOuterService.removeFunctionFromOperator(authVO);
            }
        }
        return new ModelAndView("redirect:/my/operator/manage.htm");
    }
    
    @ResponseBody
    @RequestMapping(value="/my/operator/lockToggle.htm")
    public RestResponse lockToggle(HttpServletRequest request,String operatorId,String lockStatus,OperationEnvironment env){
        RestResponse restResp=new RestResponse();
        //鉴权
        if(!super.auth(request, "EW_OP_STATUS")){
            restResp.setSuccess(false);
            restResp.setMessage("您没有权限做此操作");
            return restResp;
        }
        if(StringUtils.isEmpty(operatorId)||StringUtils.isEmpty(lockStatus)){
            restResp.setSuccess(false);
            restResp.setMessage("参数不能为空");
            return restResp;
        }
        try {
            //当前状态为锁定状态，解锁
            if(lockStatus.equals(String.valueOf(OperatorLockStatusEnum.LOCK.getCode()))){
                operatorService.unlockOperator(operatorId, env);
                restResp.setSuccess(true);
                restResp.setMessage("解锁成功");
            }else if(lockStatus.equals(String.valueOf(OperatorLockStatusEnum.UNLOCK.getCode()))){
                operatorService.lockOperator(operatorId, env);
                restResp.setSuccess(true);
                restResp.setMessage("解锁成功");
            }else{
                restResp.setSuccess(false);
                restResp.setMessage("参数错误");
            }
        } catch (BizException e) {
            logger.error("系统异常",e);
            restResp.setSuccess(false);
            restResp.setMessage(e.getMessage());
        }
        return restResp;
    }

    @ResponseBody
    @RequestMapping(value="/my/operator/checkLoginName.htm")
    public RestResponse checkLoginName(HttpServletRequest request,String loginName,OperationEnvironment env){
        RestResponse restResp=new RestResponse();
        if(StringUtils.isEmpty(loginName)){
            restResp.setSuccess(false);
			restResp.setMessage("账户名不能为空");
            return restResp;
        }
        try {
            String memberId=super.getMemberId(request);
            boolean isExist=operatorService.checkLoginNameExist(memberId, loginName, String.valueOf(PlatformTypeEnum.UID.getCode()),env);
            //登陆名已经存在,返回报错
            if(isExist){
                restResp.setSuccess(false);
                restResp.setMessage("操作员账号已经存在");
            }else{
                restResp.setSuccess(true);
                restResp.setMessage("该账号可用");
            }
        } catch (BizException e) {
            logger.error("系统异常",e);
            restResp.setSuccess(false);
            restResp.setMessage(e.getMessage());
        }
        return restResp;
    }
    
}
