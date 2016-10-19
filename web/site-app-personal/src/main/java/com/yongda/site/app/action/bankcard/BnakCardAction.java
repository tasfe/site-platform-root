package com.yongda.site.app.action.bankcard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.meidusa.fastjson.TypeReference;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.core.common.util.RSASignUtil;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.PayPasswordCheckReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.BankCardPayAttrType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.integration.cashier.CashierService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.netfinworks.vfsso.client.authapi.VfSsoUser;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.domain.domain.cashier.PayLimit;

/**
 * 通用说明：会员信息control
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午10:04:25
 *
 */
@Controller
@RequestMapping("/bankcard")
public class BnakCardAction extends BaseAction {

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
	
	@Resource(name="cashierService")
	private CashierService cashierService;
	
	@Resource(name = "payPasswordService")
	private PayPasswordService payPasswordService;
	
	@Value("${yd.h5AddCardAddress}")
	private String redirect_url;
	
	@Value("${dc.priKey}")
	private String priKey;
	
	/**
	 *  查询绑定银行卡详细
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "my/{payAtt}", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public RestResponse infos(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathVariable("payAtt") String payAtt) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		// 查询绑定银行卡
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());
		BankCardPayAttrType attrType = BankCardPayAttrType.getByCode(payAtt);
		if(attrType != null){
			req.setPayAttribute(attrType.getCode());
		}
		List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
		String defaultCard = this.getDefaultBankCard(list);
		restP.getData().put("defaultCard", defaultCard);
		restP.getData().put("cardList", list);
		restP.setSuccess(true);
		logger.info("查询银行卡信息成功："+restP);
		return restP;
	}
	
	@RequestMapping(value = "my/nums/{payAtt}", method =RequestMethod.GET)
	@ResponseBody
	public RestResponse nums(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathVariable("payAtt") String payAtt) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		// 查询绑定银行卡
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());
		BankCardPayAttrType attrType = BankCardPayAttrType.getByCode(payAtt);
		if(attrType != null){
			req.setPayAttribute(attrType.getCode());
		}
		List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(req);
		restP.getData().put("nums",CollectionUtils.isEmpty(list)?0:list.size());
		restP.setSuccess(true);
		logger.info("查询银行卡数量成功："+restP);
		return restP;
	}
	
	@RequestMapping(value = "detail/{id}", method =RequestMethod.GET)
	@ResponseBody
	public RestResponse info(HttpServletRequest request, HttpServletResponse resp,OperationEnvironment env
			,@PathVariable(value = "id") String cardId) throws Exception {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		// 查询绑定银行卡
		BankAcctDetailInfo detail = defaultBankAccountService.queryBankAccountDetail(cardId);
		restP.getData().put("detail", detail);
		if(detail != null){
			List<PayLimit> limit = cashierService.queryInstPayLimit(StringUtil.toUpperCase(detail.getBankCode()), detail.getCardType().equals(1)?"51":"52");
			restP.getData().put("limit", limit);
		}
		restP.setSuccess(true);
		logger.info("查询银行卡详细信息成功："+restP);
		return restP;
	}
	
	/**
	 * 设置默认银行卡
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping(value = "default/{oldDefault}-{default}", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public RestResponse setDefaultCard(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env,
			@PathVariable(value = "oldDefault") String oldDefaultCardId,
			@PathVariable(value = "default") String defaultCardId) {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		try {
			BankAccRequest req = new BankAccRequest();
			req.setMemberId(super.getMemberId(request));
			if (!oldDefaultCardId.equals("0")) {
				if (oldDefaultCardId.equals(defaultCardId)) {
					return restP;
				} else {
                    Map<String, String> map = new HashMap<String, String>();
                    BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(oldDefaultCardId);
                    if (StringUtil.isNotBlank(info.getExtention())) {
                        Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                                new TypeReference<Map<String, String>>() {
                                });
                        for (String key : extMap.keySet()) {
                            map.put(key, extMap.get(key));
                        }
                    }
                    map.put("isDefaultcard", "0");
					req.setBankcardId(oldDefaultCardId);
					req.setExtInfo(JSONObject.toJSONString(map));
					if (!defaultBankAccountService.updateDefaultAccount(req)) {
						throw new Exception("移除默认银行卡失败!");
					}
				}
			}
			
			Map<String, String> map = new HashMap<String, String>();
            BankAcctDetailInfo info = defaultBankAccountService.queryBankAccountDetail(defaultCardId);
            if (StringUtil.isNotBlank(info.getExtention())) {
                Map<String, String> extMap = JSON.parseObject(info.getExtention(),
                        new TypeReference<Map<String, String>>() {
                        });
                for (String key : extMap.keySet()) {
                    map.put(key, extMap.get(key));
                }
            }
			map.put("isDefaultcard", "1");
			req.setBankcardId(defaultCardId);
			req.setExtInfo(JSONObject.toJSONString(map));
			if (!defaultBankAccountService.updateDefaultAccount(req)) {
				throw new Exception("添加默认银行卡失败!");
			}
		} catch (Exception e) {
			logger.error("设置默认银行卡异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
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
	@RequestMapping(value = "remove/{id}", method = {RequestMethod.POST,RequestMethod.GET,RequestMethod.DELETE})
	@ResponseBody
	public RestResponse removeBankCard(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env,
			@PathVariable(value = "id") String cardId) {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try {
			PayPasswordCheckReq checkreq = new PayPasswordCheckReq();
			String payPwd = request.getParameter("payPwd");
			if(!payPwd.matches(RegexRule.PAY_PWD_VERIFY)){
				logger.error("支付密码錯誤");
				restP = ResponseUtil.buildExpResponse(CommonDefinedException.PASSWORD_ERROR);
				return restP;
			}
			checkreq.setPlatformInfo(createDefaultPlatform(user.getMemberType()));
			checkreq.setAccountId(user.getDefaultAccountId());
			checkreq.setEnviroment(env);
			checkreq.setOperatorId(user.getOperatorId());
			checkreq.setPayPassword(request.getParameter("payPwd"));
			UnionmaBaseResponse reomotResp = payPasswordService.payPasswordCheck(checkreq);
			if(!reomotResp.getIsSuccess()){
				restP = ResponseUtil.buildExpResponse(reomotResp.getResponseCode(), reomotResp.getResponseMessage());
			}else{
				BankAccRequest req = new BankAccRequest();
				req.setBankcardId(cardId);
				req.setMemberId(super.getMemberId(request));
				defaultBankAccountService.removeBankAccount(req);
			}
		} catch (ServiceException e) {
			logger.error("解除会员银行卡绑定异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}catch(Exception e){
			logger.error("校验支付密码异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	
	@RequestMapping(value = "remove", method = {RequestMethod.POST})
	@ResponseBody
	public RestResponse removeDdBankCard(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env
			) {
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		PersonMember user = getUser(request);
		try {
			PayPasswordCheckReq checkreq = new PayPasswordCheckReq();
			String payPwd = request.getParameter("pay_pwd");
			String cardId = request.getParameter("card_id");
			if(StringUtils.isBlank(cardId)){
				logger.error("参数校验失败，card_id为空");
				restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),"card_id不能为空");
				return restP;
			}
			//RSA解密
			payPwd = RSASignUtil.decode(payPwd, priKey);
			
			if(StringUtils.isBlank(payPwd)){
				logger.error("参数校验失败，payPwd解密为空");
				restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),"支付密码不能为空");
				return restP;
			}
			
			if(!payPwd.matches(RegexRule.PAY_PWD_VERIFY)){
				logger.error("支付密码錯誤");
				restP = ResponseUtil.buildExpResponse(CommonDefinedException.PASSWORD_ERROR);
				return restP;
			}
			
			checkreq.setPlatformInfo(createDefaultPlatform(user.getMemberType()));
			checkreq.setAccountId(user.getDefaultAccountId());
			checkreq.setEnviroment(env);
			checkreq.setOperatorId(user.getOperatorId());
			checkreq.setPayPassword(payPwd);
			UnionmaBaseResponse reomotResp = payPasswordService.payPasswordCheck(checkreq);
			if(!reomotResp.getIsSuccess()){
				restP = ResponseUtil.buildExpResponse(reomotResp.getResponseCode(), reomotResp.getResponseMessage());
			}else{
				BankAccRequest req = new BankAccRequest();
				req.setBankcardId(cardId);
				req.setMemberId(super.getMemberId(request));
				defaultBankAccountService.removeBankAccount(req);
				restP.setMessage("删除成功");
			}
		} catch (ServiceException e) {
			logger.error("解除会员银行卡绑定异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}catch(Exception e){
			logger.error("校验支付密码异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		return restP;
	}
	
	@RequestMapping(value = "add", method = {RequestMethod.POST})
	@ResponseBody
	public RestResponse add(HttpServletRequest request, HttpServletResponse resp, TradeEnvironment env) {
			RestResponse restP = ResponseUtil.buildSuccessResponse();
			try {
				String return_url = request.getParameter("return_url");//回调地址
				if(StringUtils.isBlank(return_url)){
					 logger.error("参数校验失败，return_url不能为空");
					 restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode(),"return_url不能为空");
					 return restP;
				}else{
					//redirect_url:h5添加银行卡地址   return_url:跳转url
					String url = redirect_url+"?returnUrl="+return_url+"&ssoToken="+VfSsoUser.getCurrentToken();
					logger.info("h5添加银行卡跳转地址：{}",url);
					restP.getData().put("redirect_url", url);
					restP.setMessage("处理成功");
				}
			} catch (Exception e) {
				logger.error("添加银行卡失败：{}",e);
			}
			return restP;
	}
	
}
