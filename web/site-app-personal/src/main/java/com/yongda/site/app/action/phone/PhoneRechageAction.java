package com.yongda.site.app.action.phone;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.mns.client.domain.WxNotifyParam;
import com.netfinworks.recharge.facade.api.EwPayRechargeServiceFacade;
import com.netfinworks.recharge.facade.api.RechargeCardServiceFacade;
import com.netfinworks.recharge.facade.api.RechargeQueryServiceFacade;
import com.netfinworks.recharge.facade.enums.ChannelsEnum;
import com.netfinworks.recharge.facade.enums.OrderTypeEnum;
import com.netfinworks.recharge.facade.model.RechargeAddress;
import com.netfinworks.recharge.facade.model.RechargeOrder;
import com.netfinworks.recharge.facade.response.RechargeQueryResponse;
import com.netfinworks.recharge.facade.response.Response;
import com.netfinworks.site.core.common.CardValue;
import com.netfinworks.site.core.common.FlowValue;
import com.netfinworks.site.core.common.PhoneRechageResponse;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.RechargeOrderPayStatus;
import com.netfinworks.site.domain.enums.RechargeOrderRechargeStatus;
import com.netfinworks.site.domain.enums.SkNotifyRechargeStatus;
import com.netfinworks.site.domain.enums.SkRechargeType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException.CommonException;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.voucher.service.facade.domain.enums.VoucherInfoType;
import com.yongda.site.app.WebDynamicResource;
import com.yongda.site.app.action.common.BaseAction;
import com.yongda.site.app.common.util.LogUtil;
import com.yongda.site.app.util.Core;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.app.verify.VerifyResult;
import com.yongda.site.app.verify.verifyClient;
import com.yongda.site.ext.integration.message.MessageService;

/*
 * 话费充值
 * */
@Controller
public class PhoneRechageAction extends BaseAction{
	protected Logger log = LoggerFactory.getLogger(getClass());
	@Resource(name="ewPayRechargeServiceFacade")
	private EwPayRechargeServiceFacade ewPayRechargeServiceFacade;
	
	@Resource(name="rechargeQueryServiceFacade")
	private RechargeQueryServiceFacade rechargeQueryServiceFacade;
	
	@Resource(name = "voucherService")
    private VoucherService             voucherService;
	
	@Resource(name = "memberService")
    private MemberService memberService;
	
	@Resource(name = "tradeService")
	private TradeService tradeService;
	
	@Resource(name = "rechargeCardServiceFacade")
	private RechargeCardServiceFacade rechargeCardServiceFacade;
	
	@Resource(name = "webResource")
	private WebDynamicResource webResource;
	
	@Resource(name = "authVerifyService")
	private AuthVerifyService       authVerifyService;
	 
	@Resource(name = "messageService")
	private MessageService messageService;
	
	private static String SIGN_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAO/6rPCvyCC+IMalLzTy3cVBz/+wamCFNiq9qKEilEBDTttP7Rd/GAS51lsfCrsISbg5td/w25+wulDfuMbjjlW9Afh0p7Jscmbo1skqIOIUPYfVQEL687B0EmJufMlljfu52b2efVAyWZF9QBG1vx/AJz1EVyfskMaYVqPiTesZAgMBAAECgYEAtVnkk0bjoArOTg/KquLWQRlJDFrPKP3CP25wHsU4749t6kJuU5FSH1Ao81d0Dn9m5neGQCOOdRFi23cV9gdFKYMhwPE6+nTAloxI3vb8K9NNMe0zcFksva9c9bUaMGH2p40szMoOpO6TrSHO9Hx4GJ6UfsUUqkFFlN76XprwE+ECQQD9rXwfbr9GKh9QMNvnwo9xxyVl4kI88iq0X6G4qVXo1Tv6/DBDJNkX1mbXKFYL5NOW1waZzR+Z/XcKWAmUT8J9AkEA8i0WT/ieNsF3IuFvrIYG4WUadbUqObcYP4Y7Vt836zggRbu0qvYiqAv92Leruaq3ZN1khxp6gZKl/OJHXc5xzQJACqr1AU1i9cxnrLOhS8m+xoYdaH9vUajNavBqmJ1mY3g0IYXhcbFm/72gbYPgundQ/pLkUCt0HMGv89tn67i+8QJBALV6UgkVnsIbkkKCOyRGv2syT3S7kOv1J+eamGcOGSJcSdrXwZiHoArcCZrYcIhOxOWB/m47ymfE1Dw/+QjzxlUCQCmnGFUO9zN862mKYjEkjDN65n1IUB9Fmc1msHkIZAQaQknmxmCIOHC75u4W0PGRyVzq8KkxpNBq62ICl7xmsPM=";//密匙，可以不需要
	
	/**
	 * 微信模板消息的模板id
	 */
	@Value("${phone_pay_templateId}")
	private String phonePayTemplateId;
	
	@Value("${detail_url}")
	private String detailUrl;
	
	@Value("${wx_templateId_color}")
	private String templateIdColor;
	
	/**
     * 跳转到话费充值界面
     * @return
     */
    @RequestMapping(value = "/phoneRecharge", method = RequestMethod.GET)
    @ResponseBody
    public RestResponse toPhoneRecharge(HttpServletRequest request,
			HttpServletResponse response, OperationEnvironment env) throws Exception{
    	log.info("phoneRecharge..start");
    	RestResponse restP = new RestResponse();
		response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		Map<String,Object> map = new HashMap<String,Object>();
		try{
			
			PersonMember user =  getUser(request);
			//获取个人用户绑定的号码
	    	String mobile = getEncryptInfo(request, DeciphedType.CELL_PHONE, env);
	    	
	    	logger.error("绑定手机号码：{}",mobile);
	    	//云+注册的账号可能会查询不到绑定的手机
	    	if(StringUtils.isBlank(mobile))
	    	{
	    		mobile = user.getLoginName();
	    	}
	    	//调话费充值服务，查询历史记录号码，
	    	Response<RechargeQueryResponse> rechargeResponse =rechargeQueryServiceFacade.queryLastRechargeRecord(user.getMemberId(), 3);
	    	RechargeQueryResponse rechargeRes=rechargeResponse.getBean();
	    	List<RechargeOrder> list=rechargeRes.getRechargeOrderList();
	        List<String> phoneList = new ArrayList<String>();
	        for(RechargeOrder order : list){
	        	String phoneNo = order.getRechargePhone();
	        	if(!phoneList.contains(phoneNo)){
	        		if(phoneList.size()<3){
	        			phoneList.add(phoneNo);
	        		}
	        	}
	        }

	        if(phoneList.size()>0)
	        {
	        	map.put("phoneList", phoneList);
			}
	        //返回充值卡面值信息
	        PhoneRechageResponse res=selectPhoneInfo(mobile);
	        if(res.isSuccess()){//查询手机号信息成功
	        	map.put("mobile", mobile);//充值号码-默认-个人用户绑定的号码-显示在文本框中
		    	map.put("goodsInfo", JSONObject.toJSON(res));
		    	restP.setMessage("获取充值卡信息成功");
				restP.setSuccess(true);
	        }else{
	        	log.info("暂不支持该手机号充值：{}",mobile);
	        	map.put("mobile", mobile);
	        	restP.setSuccess(false);
	        	restP.setCode("F001");
	        	restP.setMessage("暂不支持该手机号码充值");
	        }
	        restP.setData(map);
		}catch(Exception e){
			logger.error("获取充值卡信息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
		}
		
        return restP;
    }
    
    /*
     * 查询手机号码对应的归属地，面值和售价
     * 
     * */
    public PhoneRechageResponse selectPhoneInfo(String mobile) {
        PhoneRechageResponse phoneP = new PhoneRechageResponse();
    	List<CardValue> list =new ArrayList<CardValue>();
    	List<FlowValue> flow =new ArrayList<FlowValue>();
    	try {
    	    Response<RechargeAddress> response = ewPayRechargeServiceFacade.queryAddress(mobile);
    	    RechargeAddress rechargeAddress=response.getBean();
    		phoneP.setMobile(mobile);
    		if("00".equals(response.getCode())){//00:成功
            	phoneP.setSuccess(true);
            	phoneP.setMessage(response.getMessage());
            	String province=rechargeAddress.getProvince();
            	phoneP.setProvince(province);
            	phoneP.setAddress(rechargeAddress.getType());
             	for (int i = 0; i < rechargeAddress.getRechargeCard().size(); i++) {
             		//面额
             		String denomination= rechargeAddress.getRechargeCard().get(i).getCardValue().toString();
             	    CardValue cardValue = new CardValue();
             	    cardValue.setDenomination(denomination.substring(0, denomination.length()-3));//面值
             	    cardValue.setPrice(denomination);//原价
             	    cardValue.setSellPrice(rechargeAddress.getRechargeCard().get(i).getSellingPrice().toString());//售价
             	    list.add(cardValue);
             	}
             	for (int i = 0; i < rechargeAddress.getFlows().size(); i++) {
             		//流量
             		FlowValue flowValue = new FlowValue();
             		//流量充值时候需要产品ID
             		flowValue.setCardNo(rechargeAddress.getFlows().get(i).getCardNo());
             		flowValue.setDenomination(rechargeAddress.getFlows().get(i).getFlowValue());//面值
             		flowValue.setPrice(rechargeAddress.getFlows().get(i).getCardValue().toString());//原价
             		flowValue.setSellPrice(rechargeAddress.getFlows().get(i).getSellingPrice().toString());//售价
             		//暂且都为全国流量，本地流量的费用设为null,渠道变更后再改
             		flowValue.setLocalPrice(null);
             	    flow.add(flowValue);
             	}
             	Collections.sort(list, new Comparator<CardValue>() {
                    @Override
                    public int compare(CardValue b1,CardValue b2){
                        if((null == b1.getDenomination()) || "".equals(b1.getDenomination())){
                            b1.setDenomination("0");
                        }
                        if((null == b2.getDenomination()) || "".equals(b2.getDenomination())){
                            b2.setDenomination("0");
                        }
                        if(Integer.parseInt(b1.getDenomination()) < Integer.parseInt(b2.getDenomination())){
                            return -1;
                        }else{
                            return 1;
                        }
                    }
                });
             	phoneP.setTypes(list);
        		phoneP.setFlows(flow);
		    }else{
		    	phoneP.setSuccess(false);
            	phoneP.setMessage(response.getMessage());
		    }
    		
		} catch (Exception e) {
			logger.error("查询手机号码归属地 ：手机号为" + mobile + "；错误信息为：" + 
					e.getMessage(), e);
		}
    	logger.info("充值卡信息:"+JSONObject.toJSON(phoneP));
        return  phoneP;
	}
    /**
     * 返回手机号码对应的归属地，面值和售价
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/phoneInfo", method = {RequestMethod.GET, RequestMethod.POST })
    public RestResponse toPhoneInfo(HttpServletRequest req,HttpServletResponse response) throws Exception{
    	RestResponse restP = new RestResponse();
    	Map<String,Object> map = new HashMap<String,Object>();
    	response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		String mobile = req.getParameter("phoneNo");
		if(StringUtils.isBlank(mobile)||mobile.length()!=11){
			logger.info("手机号码错误,请重新输入:mobile="+mobile);
			restP.setSuccess(false);
			restP.setCode("F002");
			restP.setMessage("手机号码错误,请重新输入");
			return restP;
		}
    	try{
            map.put("goodsInfo", JSONObject.toJSON(selectPhoneInfo(mobile)));
            restP.setMessage("获取充值卡信息成功");
    		restP.setSuccess(true);
    		restP.setData(map);
    	}catch(Exception e){
    		logger.error("查询手机号码归属地 ：手机号为" + mobile + "；错误信息为：" + 
					e.getMessage(), e);
    		restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR);
    	}
		return restP;
    }
    
    /**
     * 立即充值,调网关服务
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/nowRechage", method = {RequestMethod.GET, RequestMethod.POST })
    public RestResponse toRechage(HttpServletRequest req, HttpServletResponse response,OperationEnvironment env) throws Exception{
    	RestResponse restP = new RestResponse();
    	response.setCharacterEncoding("utf8");
		response.setHeader("content-type", "application/json;charset=UTF-8");

    	//获取请求信息
    	PersonMember user = getUser(req);
    	if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.PHONERECHARGE.getMsg(), user, env));
		}
    	Date submitTime=new Date();
    	String tradeOrderNo = req.getParameter("tradeOrderNo");//充值表-交易订单号
    	String type = req.getParameter("type"); //充值类型 :fees-话费充值  flow-流量充值
    	String rechargeMoney=null;;//充值金额
    	String paymentMoney=null;//支付金额
		rechargeMoney = req.getParameter("rechargeMoney");
    	String mobile = req.getParameter("phoneNo");//充值号码
    	String productId = req.getParameter("productId");//产品Id
    	String reqChannel= req.getParameter("reqChannel");//请求来源
    	if (logger.isInfoEnabled()) {
            logger.info("充值请求参数:type:"+type+",rechargeMoney:"+rechargeMoney+",phoneNo:"+mobile+",productId:"+productId+",reqChannel:"+reqChannel);
		}
    	if(StringUtils.isBlank(type)||StringUtils.isBlank(mobile)||(StringUtils.isBlank(rechargeMoney)&&StringUtils.isBlank(productId))){
			logger.info("type、mobile等请求参数不能为空");
			restP.setSuccess(false);
			restP.setCode("F004");
			restP.setMessage("亲，系统繁忙，请稍后再试~");
			return restP;
		}
    	if(StringUtils.isBlank(mobile)||mobile.length()!=11){
			logger.info("手机号码错误,请重新输入:mobile="+mobile);
			restP.setSuccess(false);
			restP.setCode("F002");
			restP.setMessage("手机号码错误,请重新输入");
			return restP;
		}
    	//查询该号码当月累计充值次数
    	Response<Integer> res =rechargeQueryServiceFacade.countLimitd(mobile, user.getMemberId(),type);
    	logger.info("该号码当月累计充值次数为:"+res.getBean().toString());
    	if(res.getBean()>=3){
			restP.setSuccess(false);
			restP.setCode("F003");
			restP.setMessage("该号码当月累计充值次数超限");
			return restP;
    	}
    	
		// 支付金额需要从接口重新获取
		PhoneRechageResponse rechargeResponse = selectPhoneInfo(mobile);
    	String address=rechargeResponse.getAddress();//归属地
		List<CardValue> list = rechargeResponse.getTypes();
		List<FlowValue> flow = rechargeResponse.getFlows();
		if(OrderTypeEnum.FEES.getCode().equals(type)){//话费充值
			for (CardValue cardValue : list) {
				if (rechargeMoney.equals(cardValue.getDenomination())) {
					paymentMoney = cardValue.getSellPrice();
					break;
				}
			}
		}else{//流量充值:默认是全国流量，以后本地流量再修改
			for (FlowValue flowValue : flow) {
				if (productId.equals(flowValue.getCardNo())) {
					rechargeMoney=flowValue.getPrice();
					paymentMoney = flowValue.getSellPrice();
					break;
				}
			}
		}
		if (paymentMoney == null) {
			logger.info("支付金额获取失败！");
			restP.setSuccess(false);
			return restP;
		}
		
		//话费或流量充值订单入库
    	tradeOrderNo = voucherService.regist(VoucherInfoType.TRADE.getCode());//生成交易订单号
    	RechargeOrder rechargeOrder =new RechargeOrder();
    	rechargeOrder.setTradeOrderNo(tradeOrderNo);//交易订单号
    	rechargeOrder.setType(type);
    	rechargeOrder.setPayOrderNo("");//支付订单号
    	rechargeOrder.setRechargeOrderNo("");//充值订单号
    	rechargeOrder.setRechargePhone(mobile);//充值号码
    	rechargeOrder.setRechargeAmount(new Money(rechargeMoney));//充值金额
    	rechargeOrder.setPayAmount(new Money(paymentMoney));//支付金额
    	rechargeOrder.setPayStatus(RechargeOrderPayStatus.PAYING.getCode());//支付状态
    	rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.RECHARGEING.getCode());//充值状态-待充值
    	rechargeOrder.setMemberId(user.getMemberId());//客户编号
    	rechargeOrder.setGmtCreate(submitTime);//订单创建时间
    	rechargeOrder.setOrgId("10086");//运营商编码
    	rechargeOrder.setOrgName(address);//运营商名称
    	if(OrderTypeEnum.FEES.getCode().equals(type)){
    		rechargeOrder.setChannelId(ChannelsEnum.EWPAY_FEES.getCode());//充值渠道
        	rechargeOrder.setChannelName(ChannelsEnum.EWPAY_FEES.getMessage());//充值渠道名称	
    	}else{
    		rechargeOrder.setProductId(productId);
    		rechargeOrder.setChannelId(ChannelsEnum.EWPAY_FLOW.getCode());
        	rechargeOrder.setChannelName(ChannelsEnum.EWPAY_FLOW.getMessage());
    	}
    	rechargeOrder.setMemo("");//备注信息
    	rechargeOrder.setExtension("");//扩展信息
    	rechargeQueryServiceFacade.addRechargeOrder(rechargeOrder);//插入到充值表中
    	
    	//组装参数，调网关接口
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String submitTimeStr = format.format(submitTime);
    	BaseMember merchant=new BaseMember();
    	merchant=memberService.queryMemberById(webResource.getRechargeEnterpriseMemberId(),env);//根据商户ID（即话费充值商户）查出商户账号
		BaseMember personal=new BaseMember();
		personal=memberService.queryMemberById(user.getMemberId(),env);//根据当前个人平台的ID查出个人账号
		StringBuilder tradeList=new StringBuilder();
		if(OrderTypeEnum.FEES.getCode().equals(type)){
			tradeList.append(tradeOrderNo+"~话费充值-充值"+rechargeMoney+".00元   "+mobile+"~"+paymentMoney+"~1~");
		}else{
			tradeList.append(tradeOrderNo+"~流量充值-充值"+rechargeMoney+"元   "+mobile+"~"+paymentMoney+"~1~");
		}
		tradeList.append(paymentMoney+"~~"+merchant.getLoginName()+"~1~");
		tradeList.append(submitTimeStr+"~~~~");//-------------
		tradeList.append("~~"+submitTimeStr);//submitTimeStr
		String Callbackurl=webResource.getRechargeReturnUrl();//网关-异步回调地址
		tradeList.append("~"+Callbackurl+"~浙江永达互联网金融信息服务有限公司");//
		String ip = InetAddress.getLocalHost().getHostAddress();//操作电脑IP
		String gatewayUrl=webResource.getGatewayUrl();
        try {
    	   req.setCharacterEncoding("UTF-8");
		} catch (java.io.UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} 
        String  signTestMode="ITRUSSRV";
		Map<String, String> sParaTemp = new TreeMap<String, String>();
		sParaTemp.put("service", "create_mobile_recharge_trade");//接口名称-v1.0,
		sParaTemp.put("version", "1.0");//接口版本
		sParaTemp.put("partner_id", merchant.getMemberId());//合作者身份ID/商户号--200000010039
		sParaTemp.put("_input_charset", "UTF-8");//字符集/参数编码字符集
		sParaTemp.put("sign", ""); //签名--签约合作方的钱包唯一用户号 不可空（就是下面的map）
		sParaTemp.put("sign_type", signTestMode);//签名方式--只支持ITRUSSRV不可空
		sParaTemp.put("return_url", webResource.getRechargeNotifyUrl());// 返回页面路径/页面跳转同步返回页面路径
		sParaTemp.put("memo", "话费充值-充值"+rechargeMoney+"元   "+mobile);//备注
		sParaTemp.put("risk_item", "{\"recharge_mobile\":\""+mobile+"\"}");//充值手机号
		sParaTemp.put("request_no", tradeOrderNo);//商户网站请求号
		sParaTemp.put("trade_list", tradeList.toString());//交易列表
		sParaTemp.put("operator_id", user.getMemberId());//操作员ID 
		sParaTemp.put("buyer_id", personal.getLoginName());//nimi买家"anonymous"
		sParaTemp.put("buyer_id_type", "1");//买家ID类型--用户ID平台默认值为1 不可空
		sParaTemp.put("buyer_ip", ip);//买家IP地址--可空
		sParaTemp.put("pay_method", "");//支付方式^金额^备注  可空(支付方式为空，默认跳转收银台)
		sParaTemp.put("go_cashier", "Y");//是否转收银台标识 Y使用(默认值)N不使用-
		if("wap".equals(reqChannel)){
			sParaTemp.put("access_channel", "WAP");//请求渠道：WEB/WAP
		}else{
			sParaTemp.put("access_channel", "WEB");
		}
		
        String signType = signTestMode;//签名方式--只支持ITRUSSRV不可空
        String inputCharset = "UTF-8";
        
        logger.info("话费充值验签请求参数：{}", sParaTemp);
        logger.info("话费充值同步url：{}"+webResource.getRechargeNotifyUrl());

		//参数加密
		Map<String, String> map = Core.buildRequestPara(sParaTemp,signType,SIGN_KEY,inputCharset);
		 
		//拼装返回的信息 form表单 
		StringBuilder html = new StringBuilder();
		//html.append("<script language=\"javascript\">window.onload=function(){document.pay_form.submit();}</script>\n");
		html.append("<form id=\"pay_form\" name=\"pay_form\" action=\"").append(gatewayUrl).append("\" method=\"post\">\n");

		for (String key : map.keySet()) {
	        html.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value=\"" + map.get(key) + "\">\n");
		}
		html.append("</form>\n");
		try {
			Map<String,Object> formRes = new HashMap<String,Object>();
			formRes.put("form", html.toString());
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("html", JSONObject.toJSON(formRes));
			restP.setSuccess(true);
			restP.setData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return restP;
    }
   
    /**
     * 网关跳转异步返回页面路径后，对支付后的订单进行处理
     * @return
     */ 
    @RequestMapping(value = "/skRecharge", method = {RequestMethod.GET, RequestMethod.POST })
    public void cashierAsynReturn(HttpServletRequest req,HttpServletResponse response,TradeEnvironment env) {
    	logger.info("skRecharge...start");
		try {
			req.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("设置编码异常", e);
		}
    	
    	Map<String, String> sParaTemp = new TreeMap<String, String>();
    	sParaTemp.put("notify_id", req.getParameter("notify_id"));//通知ID
    	sParaTemp.put("notify_type", req.getParameter("notify_type"));//通知类型
    	sParaTemp.put("notify_time", req.getParameter("notify_time"));//通知时间
    	sParaTemp.put("_input_charset",req.getParameter("_input_charset"));//参数字符集编码
    	sParaTemp.put("sign", req.getParameter("sign"));//签名
    	sParaTemp.put("sign_type", req.getParameter("sign_type"));//签名方式
    	sParaTemp.put("version", req.getParameter("version"));//版本号
    	sParaTemp.put("outer_trade_no",req.getParameter("outer_trade_no"));//商户网站唯一订单号--商户网站唯一订单号
    	sParaTemp.put("inner_trade_no",req.getParameter("inner_trade_no"));//钱包交易号-交易订单号
    	sParaTemp.put("trade_status", req.getParameter("trade_status"));//交易状态
    	sParaTemp.put("trade_amount", req.getParameter("trade_amount"));//交易金额
    	sParaTemp.put("gmt_create", req.getParameter("gmt_create"));//交易创建时间
    	sParaTemp.put("gmt_payment", req.getParameter("gmt_payment"));//交易支付时间
    	sParaTemp.put("gmt_close", req.getParameter("gmt_close"));//交易关闭时间
    	String inputCharset = req.getParameter("_input_charset");
    	
    	String outer_trade_no =req.getParameter("outer_trade_no");//充值表-交易订单号
    	String tradeStatus = req.getParameter("trade_status");//交易状态
    	String inner_trade_no=req.getParameter("inner_trade_no");// 充值表-支付订单号
    	logger.info("交易状态:"+tradeStatus);
    	//签名验证
    	logger.info("话费充值回调，验证签名，参数：{}", sParaTemp);
    	VerifyResult result = new VerifyResult();
		try {
			result = verifyClient.verifyBasic(
					inputCharset,  sParaTemp);
		} catch (Exception e) {
			logger.error("验签异常", e);
			return;
		}
		
		try {
			if (result.isSuccess()) {
				//按交易订单号查询订单信息
				Response<RechargeOrder> orderInfo = rechargeQueryServiceFacade.queryById(outer_trade_no);
	    		if(orderInfo == null) {
	    			return;
	    		}
	    		
	    		RechargeOrder rechargeOrder = new RechargeOrder();
	    		rechargeOrder.setTradeOrderNo(outer_trade_no);//交易订单号
	    		rechargeOrder.setPayOrderNo(inner_trade_no);//支付订单号"101141700502505201704"
	    		
	    		if (tradeStatus.equals("WAIT_BUYER_PAY")) {//支付状态
	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.PAYING.getCode());
	    		} else if (tradeStatus.equals("TRADE_FINISHED")) {
	    			// 如果已经支付成功且在充值中或已退款则返回
	    			RechargeOrder rechargeOrderInfo = orderInfo.getBean();
	    			if(rechargeOrderInfo.getPayStatus().equals(RechargeOrderPayStatus.REFUNDED.getCode()) || (rechargeOrderInfo.getPayStatus().equals(RechargeOrderPayStatus.SUCCESS.getCode())  
	    					&& !rechargeOrderInfo.getRechargeStatus().equals(RechargeOrderRechargeStatus.RECHARGEING.getCode()))) {
	    				logger.info("已经支付成功且在充值中或已退款，不需要再支付：请求订单号："+outer_trade_no);
	    				response.getWriter().write("success");
	    				response.getWriter().flush();	
	    				response.getWriter().close();
	    				return;
	    			}
	    			
        			rechargeOrder.setGmtPaid(new Date());//支付状态
	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.SUCCESS.getCode());
	    			rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
	    			
	    	   		String phone=rechargeOrderInfo.getRechargePhone();//充值手机号
	        		String amount = rechargeOrderInfo.getRechargeAmount().getAmount().toString();//充值金额
	        		String rechargeAmount = amount.substring(0,amount.indexOf("."));
	        		
	        		sendRechargeMessage(rechargeOrderInfo.getProductId(),rechargeOrderInfo.getType(),phone,amount,
	        				outer_trade_no,rechargeOrderInfo.getMemberId());
	        		String skResult=null;
    				try{
    				    /**
    				     * 支付成功调用速卡充值
    				     * 
    				     * productid 产品id（仅流量充值时必须填，其他填空字符串）
    				     */
    					String rechargeType = null;
    					if(SkRechargeType.TRADE_FEES.getCode().equalsIgnoreCase(rechargeOrderInfo.getType())){
    						rechargeType = ChannelsEnum.EWPAY_FEES.getCode();
    					}else{
    						rechargeType = ChannelsEnum.EWPAY_FLOW.getCode();
    					}
    					 logger.info("请求订单号：{}",outer_trade_no+",充值金额：{}",rechargeAmount+",充值号码:{}",phone+"充值商品类型:{}",
    					rechargeOrderInfo.getProductId()+"充值类型：{}",rechargeType); 
    					 Response<Boolean> ewPayRechargeResult = ewPayRechargeServiceFacade.recharge(outer_trade_no, rechargeAmount, 
    							 phone, rechargeOrderInfo.getProductId(),rechargeType);
    					 
    					 skResult = String.valueOf(ewPayRechargeResult.getBean().booleanValue());
    				     
    					 logger.info("请求订单号：{}",outer_trade_no+"调用速卡充值返回结果:{}",skResult);
    				}catch(Exception e){
    					logger.error("调用速卡充值接口异常:{}"+e);
    				}
	        		
    				//以下注释内容全部改成在
	        		if(SkNotifyRechargeStatus.FAILURE.getCode().equals(skResult))//充值失败后
	                {
	        			 logger.info("充值失败,订单号：{}",inner_trade_no);
	        		  /*  rechargeOrder.setGmtRecharge(new Date());
	                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.FAILURE.getCode());//充值失败
	                    boolean updatestatus=false;
	                    try {
	                    	Response<Boolean>  upRechargeResult= rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
	                    	updatestatus = upRechargeResult.getBean().booleanValue();
	                        logger.info("充值状态更新为充值失败:updatestatus={}",updatestatus);
	                    } catch (Exception e) {
	                        logger.error("更新充值状态失败:{}"+e);
	                    }*/
	                }else if(SkNotifyRechargeStatus.SUCCESS.getCode().equals(skResult)){//充值成功
	                	 logger.info("充值成功,订单号：{}",inner_trade_no);
	                   /* rechargeOrder.setGmtRecharge(new Date());
	                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.SUCCESS.getCode());//充值状态-充值成功
	                    boolean updatestatus=false;
	                    try {
	                    	Response<Boolean>  upRechargeResult = rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
	                    	updatestatus = upRechargeResult.getBean().booleanValue();
	                        logger.info("充值状态更新充值成功：updatestatus={}", updatestatus);
	                        logger.info("推送微信提醒参数:"+rechargeOrderInfo.getType()+","+phone+","+rechargeAmount+","+SkNotifyRechargeStatus.SUCCESS.getMessage()+","+rechargeOrderInfo.getMemberId());
	                        //推送微信提醒
	                        sendRechargeMessage(rechargeOrderInfo.getType(),phone,rechargeAmount,
	                        		SkNotifyRechargeStatus.SUCCESS.getMessage(),rechargeOrderInfo.getMemberId());
	                    } catch (Exception e) {
	                        logger.error("更新充值状态失败:{}"+e);
	                    }*/
	                }
	        		response.getWriter().write("success");
	        		response.getWriter().flush();	
	        		response.getWriter().close();
	        		return;
	    			
	    		}else if (tradeStatus.equals("TRADE_CLOSED")) {
	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.FAILURE.getCode());
	    		}
	    		rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
	    		response.getWriter().write("success");
	    		response.getWriter().flush();	
	    		response.getWriter().close();
	    	}
		} catch(Exception e) {
			logger.error("手机充值异常", e);
		}
    }
   
    public void sendRechargeMessage(String productId,String type,String rechargePhone,String rechargeAmount
    		,String payNo,String memberId){
    	String rechargeName = SkRechargeType.TRADE_FEES.getMessage();;
    	String remark = SkRechargeType.TRADE_FEES.getRemark();
    	//String denomination = StringUtils.EMPTY;
		if(SkRechargeType.TRADE_FLOW.getCode().equalsIgnoreCase(type)){//流量充值
			rechargeName = SkRechargeType.TRADE_FLOW.getMessage();
	    	remark       = SkRechargeType.TRADE_FLOW.getRemark();
	    	/*logger.info("流量充值产品Id：{}",productId);
	    	// 支付金额需要从接口重新获取
			PhoneRechageResponse rechargeResponse = selectPhoneInfo(rechargePhone);
			List<FlowValue> flow = rechargeResponse.getFlows();
			//流量充值:默认是全国流量
				for (FlowValue flowValue : flow) {
					logger.info("流量充值产品Id：{},流量充值面额：{}",flowValue.getCardNo(),flowValue.getDenomination());
					if (productId.equals(flowValue.getCardNo())) {
						denomination = flowValue.getDenomination();
						break;
					}
				}*/
		}
				
    	 //微信模板消息推送
		WxNotifyParam param = new WxNotifyParam();
		//param.setUrl(StringUtils.EMPTY);
		param.addDataParam("first", rechargeName, templateIdColor);
		param.addDataParam("keyword1", payNo, templateIdColor);
		param.addDataParam("keyword2", rechargePhone, templateIdColor);
		param.addDataParam("keyword3", rechargeAmount+"元", templateIdColor);
		param.addDataParam("remark", remark, templateIdColor);
		try {
			logger.info("微信模板参数：{}"+"rechargePhone：{}",rechargePhone+",payNo：{}",payNo+",rechargeAmount:{}",rechargeAmount+",rechargeName:{}",rechargeName);
			messageService.sendWxMsg(memberId, phonePayTemplateId, param);
		} catch (CommonException e) {
			logger.info("sendWxMsg推送微信消息失败");
		}
    }
    
}

