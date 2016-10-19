package com.yongda.site.app.action.turansferUserinfo;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.urm.domain.info.UserAccreditInfo;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.util.wx.WeixinUtil;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;

@Controller
public class TuransferInfoAction extends BaseAction{
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
	@Resource(name = "xuCache")
	private XUCache<String> loginCache;
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Value("${wx_appid}")
	private String wxappid;
	
	@Value("${wx_appsecret}")
	private String wxappsecret;
	
	@Value("${wx_domainUrl}")
	private String wxDomainUrl;
	
	@RequestMapping(value = "/accredit", method = RequestMethod.GET)
	@ResponseBody
	public void cheackLogin(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) throws IOException
			{
		String url = WeixinUtil.getAccreditCode(wxappid,wxDomainUrl);
		response.sendRedirect(url);
	}
	
	@RequestMapping(value = "/wxredirect")
	@ResponseBody
	public void wxRedirect(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) throws IOException
			{
		String code = request.getParameter("code");
		String url ="ydWallet/main.html?code="+code+"&golink=main";
		response.sendRedirect(url);
	}
	
	@RequestMapping(value = "/getUserInfo", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse getUserInfo(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,@CookieValue("cityWeather") String cityWeather) {
			response.setCharacterEncoding("utf8");
			RestResponse restP = new RestResponse();
			log.info("getUserInfo...start");
			try {
					Map<String,Object> map = new HashMap<String,Object>();
					String[] str = new String[2];
					str = URLDecoder.decode(cityWeather,"utf-8").split("&");
					map.put("url", StringUtils.isBlank(str[0])?"":str[0]);
					if(!StringUtils.isBlank(str[1]) && !"no_data".equals(str[1])){
						map.put("wxuserinfo", JSONObject.fromObject(str[1]));
					}else{
						map.put("wxuserinfo", StringUtils.EMPTY);
					}
					restP.setData(map);
					restP.setSuccess(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return restP;
	}
	
	@RequestMapping(value = "/getAllUserInfo", method = RequestMethod.POST)
	@ResponseBody
	public RestResponse getAllUserInfo(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env,@CookieValue("cityWeather") String cityWeather) {
			response.setCharacterEncoding("utf8");
			RestResponse restP = new RestResponse();
			log.info("getAllUserInfo...start");
			try {
				long beginTime = 0L;
	            if (logger.isInfoEnabled()) {
	                beginTime = System.currentTimeMillis();
	                logger.info("查询getAllUserInfo开始时间：{} ",beginTime);
	            }
					PersonMember user = getUser(request);
					List<UserAccreditInfo> list = personalFindBindThirdAccount
							.getUserInfo(user.getMemberId(),"weixin",null,1);
					if (CollectionUtils.isEmpty(list)) {
						restP.setMessage("用户未绑定");
						restP.setSuccess(false);
						restP.setCode("NOT_LOGIN");
						return restP;
					}
					Map<String,Object> map = new HashMap<String,Object>();
					String[] str = new String[2];
					str = URLDecoder.decode(cityWeather,"utf-8").split("&");
					/**
					 * 查询余额
					 */
					if(user!=null){
						MemberAccount accountResponse = defaultAccountService.queryAccountByMemberId(user.getMemberId(),user.getMemberType().getBaseAccount(), env);
						user.setAccount(accountResponse);
						String loginName = user.getLoginName();
						user.setLoginName(loginName.substring(0,3)+ "****" + loginName.substring(7, loginName.length()));
						map.put("userinfo", user);//会员的个人信息
					}
					map.put("url", StringUtils.isBlank(str[0])?"":str[0]);
					if(!StringUtils.isBlank(str[1]) && !"no_data".equals(str[1])){
						map.put("wxuserinfo", JSONObject.fromObject(str[1]));
					}else{
						map.put("wxuserinfo", StringUtils.EMPTY);
					}
					 logger.info("查询getAllUserInfo结束时间时间：{} ",System.currentTimeMillis());
					long consumeTime = System.currentTimeMillis() - beginTime;
		            logger.info("查询getAllUserInfo， 耗时:{} (ms); ",consumeTime);
					restP.setData(map);
					restP.setSuccess(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return restP;
	}

	//transfer
	@RequestMapping(value = "/transfer")
	@ResponseBody
	public void transfer(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) throws IOException
			{
		String code = request.getParameter("code");
		String url ="ydWallet/index.html?code="+code;
		response.sendRedirect(url);
	}
	
	@RequestMapping(value = "/discover")
	@ResponseBody
	public void discover(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) throws IOException
			{
		String url = WeixinUtil.getSilentAccreditCode(wxappid,wxDomainUrl);
		response.sendRedirect(url);
	}
	
}
