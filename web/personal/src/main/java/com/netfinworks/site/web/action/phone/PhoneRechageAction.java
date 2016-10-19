package com.netfinworks.site.web.action.phone;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.common.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.recharge.facade.api.OfRechargeServiceFacade;
import com.netfinworks.recharge.facade.api.RechargeCardServiceFacade;
import com.netfinworks.recharge.facade.api.RechargeQueryServiceFacade;
import com.netfinworks.recharge.facade.api.SlsQueryAddressService;
import com.netfinworks.recharge.facade.api.SlsRechargeServiceFacade;
import com.netfinworks.recharge.facade.model.RechargeAddress;
import com.netfinworks.recharge.facade.model.RechargeOrder;
import com.netfinworks.recharge.facade.model.TelCheckInfo;
import com.netfinworks.recharge.facade.request.RechargeCardRequest;
import com.netfinworks.recharge.facade.request.RechargeQueryRequest;
import com.netfinworks.recharge.facade.response.RechargeQueryResponse;
import com.netfinworks.site.core.common.CardValue;
import com.netfinworks.site.core.common.PhoneRechageResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AccessChannel;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.OfNotifyRechargeStatus;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domain.enums.RechargeOrderPayStatus;
import com.netfinworks.site.domain.enums.RechargeOrderRechargeStatus;
import com.netfinworks.site.domain.enums.SlsNotifyRechargeStatus;
import com.netfinworks.site.domainservice.convert.RefundConvertor;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;
import com.netfinworks.site.web.pojo.VerifyResult;
import com.netfinworks.site.web.util.Core;
import com.netfinworks.site.web.verify.verifyClient;
import com.netfinworks.tradeservice.facade.request.RefundRequest;
import com.netfinworks.tradeservice.facade.response.RefundResponse;
import com.netfinworks.voucher.service.facade.domain.enums.VoucherInfoType;

/*
 * 话费充值
 * */
@Controller
public class PhoneRechageAction extends BaseAction{
//	@Resource(name="slsQueryAddressService")
//	private SlsQueryAddressService slsQueryAddressService;
//	
//	@Resource(name="rechargeQueryServiceFacade")
//	private RechargeQueryServiceFacade rechargeQueryServiceFacade;
//	
//	@Resource(name = "voucherService")
//    private VoucherService             voucherService;
//	
//	@Resource(name = "memberService")
//    private MemberService memberService;
//	
//	@Resource(name = "slsRechargeServiceFacade")
//    private SlsRechargeServiceFacade slsRechargeServiceFacade;
//	
//	@Resource(name = "tradeService")
//	private TradeService tradeService;
//	
//	@Resource(name = "rechargeCardServiceFacade")
//	private RechargeCardServiceFacade rechargeCardServiceFacade;
//	
//	@Resource(name = "webResource")
//	private WebDynamicResource webResource;
//	
//	@Resource(name = "ofRechargeServiceFacade")
//    private OfRechargeServiceFacade ofRechargeServiceFacade;
//	
//	private static String SIGN_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAO/6rPCvyCC+IMalLzTy3cVBz/+wamCFNiq9qKEilEBDTttP7Rd/GAS51lsfCrsISbg5td/w25+wulDfuMbjjlW9Afh0p7Jscmbo1skqIOIUPYfVQEL687B0EmJufMlljfu52b2efVAyWZF9QBG1vx/AJz1EVyfskMaYVqPiTesZAgMBAAECgYEAtVnkk0bjoArOTg/KquLWQRlJDFrPKP3CP25wHsU4749t6kJuU5FSH1Ao81d0Dn9m5neGQCOOdRFi23cV9gdFKYMhwPE6+nTAloxI3vb8K9NNMe0zcFksva9c9bUaMGH2p40szMoOpO6TrSHO9Hx4GJ6UfsUUqkFFlN76XprwE+ECQQD9rXwfbr9GKh9QMNvnwo9xxyVl4kI88iq0X6G4qVXo1Tv6/DBDJNkX1mbXKFYL5NOW1waZzR+Z/XcKWAmUT8J9AkEA8i0WT/ieNsF3IuFvrIYG4WUadbUqObcYP4Y7Vt836zggRbu0qvYiqAv92Leruaq3ZN1khxp6gZKl/OJHXc5xzQJACqr1AU1i9cxnrLOhS8m+xoYdaH9vUajNavBqmJ1mY3g0IYXhcbFm/72gbYPgundQ/pLkUCt0HMGv89tn67i+8QJBALV6UgkVnsIbkkKCOyRGv2syT3S7kOv1J+eamGcOGSJcSdrXwZiHoArcCZrYcIhOxOWB/m47ymfE1Dw/+QjzxlUCQCmnGFUO9zN862mKYjEkjDN65n1IUB9Fmc1msHkIZAQaQknmxmCIOHC75u4W0PGRyVzq8KkxpNBq62ICl7xmsPM=";//密匙，可以不需要
//	
//	/**
//	 * 跳转到应用中心
//	 * 
//	 * */
//	@RequestMapping(value = "/my/appcenter.htm")
//	public ModelAndView toAPP(){
//		ModelAndView mv = new ModelAndView();
//		mv.setViewName(CommonConstant.URL_PREFIX + "/phoneRecharge/appcenter");
//		return mv;
//	}
//	
//	/**
//	 * 跳转到应用中心
//	 * 
//	 * */
//	@RequestMapping(value = "/my/toGateway.htm")
//	public ModelAndView toGateway(){
//		ModelAndView mv = new ModelAndView();
//		mv.setViewName(CommonConstant.URL_PREFIX + "/phoneRecharge/gatewayasyn");
//		return mv;
//	}
//	/**
//     * 跳转到话费充值前台界面
//     * @return
//     */
//    @RequestMapping(value = "/my/phoneRecharge.htm", method = {RequestMethod.GET, RequestMethod.POST })
//    public ModelAndView toPhoneRecharge(HttpServletRequest req,OperationEnvironment env,
//            HttpServletResponse resp) throws Exception{
//    	PersonMember user = getUser(req);
//    	ModelAndView mv = new ModelAndView();
//    	String mobile = getEncryptInfo(req, DeciphedType.CELL_PHONE, env);//获取个人用户绑定的号码
//    	RechargeQueryRequest  rechargeReq=new RechargeQueryRequest();
//    	
//    	rechargeReq.setStartDate(DateUtil.getDateNearCurrent(-365));
//    	rechargeReq.setEndDate(new Date());
//    	rechargeReq.setPageSize(50000);
//    	rechargeReq.setMemberId(user.getMemberId());
//    	
//    	RechargeQueryResponse rechargeRes =rechargeQueryServiceFacade.query(rechargeReq);
//    	List<RechargeOrder> list=rechargeRes.getRechargeOrderList();
//        List<String> phoneList = new ArrayList<String>();
//        for(RechargeOrder order : list){
//        	String phoneNo = order.getRechargePhone();
//        	if(!phoneList.contains(phoneNo)){
//        		phoneList.add(phoneNo);
//        	}
//        }
//        List<String> mobileList =new ArrayList<String>();
//        for(int i = 0;i<phoneList.size();i++){
//        	mobileList.add("\""+phoneList.get(i)+"\"");
//        }
//        if(mobileList.size()<=0)
//        {
//        	mv.addObject("phoneList", "\"\"");
//        }else {
//        	mv.addObject("phoneList", mobileList);
//		}
//        RechargeCardRequest queryInfo=new RechargeCardRequest();
//		String parValueInfo=rechargeCardServiceFacade.queryValue(queryInfo);
//    	if(StringUtils.isEmpty(mobile)){
//    		mv.addObject("pricestype", parValueInfo);
//    		mv.addObject("mobile", "");//充值号码-默认-个人用户绑定的号码-显示在文本框中
//            mv.addObject("Mobileinfo", "\"\"");//充值号码-默认-个人用户绑定的号码
//    	}else {
//    		mv.addObject("pricestype", parValueInfo);
//    		mv.addObject("mobile", mobile);//充值号码-默认-个人用户绑定的号码-显示在文本框中
//            mv.addObject("Mobileinfo", JSONObject.toJSONString(selectPhoneInfo(mobile)));//充值号码-默认-个人用户绑定的号码
//		}
//        mv.setViewName(CommonConstant.URL_PREFIX + "/phoneRecharge/appcenter_Recharge");
//        return mv;
//    }
//    
//    /*
//     * 查询手机号码对应的归属地，面值和售价
//     * 
//     * */
//    public PhoneRechageResponse selectPhoneInfo(String mobile) {
//        PhoneRechageResponse phoneP = new PhoneRechageResponse();
//    	//List<Map<String, String>> list =new ArrayList<Map<String,String>>();
//    	List<CardValue> list =new ArrayList<CardValue>();
//    	try {
//    		//RechargeAddress rechargeAddress= slsQueryAddressService.queryAddress(mobile);
//    	    //TODO
//    	    RechargeAddress rechargeAddress = ofRechargeServiceFacade.queryAddr(mobile);
//    		phoneP.setSuccess(false);
//    		phoneP.setMessage("没有查到号码相关信息");
//    		phoneP.setMobile(mobile);
//    		if("90000000".equals(rechargeAddress.getCode())){//90000000成功
//    		    if(rechargeAddress.getRechargeCard().size()==0){
//    	            phoneP.setMessage("暂不支持此号码充值");
//    		    }else{
//                	phoneP.setSuccess(true);
//                	phoneP.setMessage(rechargeAddress.getMsg());
//                	String province=rechargeAddress.getProvince();
//                	phoneP.setProvince(province);
//                	if(province.indexOf("上海") != -1){
//                		province="上海";
//                	}else if (province.indexOf("重庆") != -1) {
//                        province = "重庆";
//                    } else if (province.indexOf("北京") != -1) {
//                        province = "北京";
//                    } else if (province.indexOf("天津") != -1) {
//                        province = "天津";
//                    }
//                	phoneP.setAddress(province+rechargeAddress.getType());
//                 	for (int i = 0; i < rechargeAddress.getRechargeCard().size(); i++) {
//    //             		Map<String, String> data=new HashMap<String, String>();
//                 		String denomination= rechargeAddress.getRechargeCard().get(i).getCardValue().toString();
//    //             		data.put("denomination",denomination.substring(0, denomination.length()-3));
//    //             		data.put("price", rechargeAddress.getRechargeCard().get(i).getSellingPrice().toString());
//                 	    CardValue cardValue = new CardValue();
//                 	    cardValue.setDenomination(denomination.substring(0, denomination.length()-3));
//                 	    cardValue.setPrice(rechargeAddress.getRechargeCard().get(i).getSellingPrice().toString());
//                 	    list.add(cardValue);
//                 	}
//                 	Collections.sort(list, new Comparator<CardValue>() {
//                        @Override
//                        public int compare(CardValue b1,CardValue b2){
//                            if((null == b1.getDenomination()) || "".equals(b1.getDenomination())){
//                                b1.setDenomination("0");
//                            }
//                            if((null == b2.getDenomination()) || "".equals(b2.getDenomination())){
//                                b2.setDenomination("0");
//                            }
//                            if(Integer.parseInt(b1.getDenomination()) < Integer.parseInt(b2.getDenomination())){
//                                return -1;
//                            }else{
//                                return 1;
//                            }
//                        }
//                    });
//    		    }
//            }
//    		phoneP.setTypes(list);
//		} catch (Exception e) {
//			logger.error("查询手机号码归属地 ：手机号为" + mobile + "；错误信息为：" + 
//					e.getMessage(), e);
//		}
//        return  phoneP;
//	}
//    /**
//     * 返回手机号码对应的归属地，面值和售价
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/my/phoneNo.htm", method = {RequestMethod.GET, RequestMethod.POST })
//    public String toPhoneInfo(HttpServletRequest req) throws Exception{
//    	String mobile = req.getParameter("phoneNo");
//        return JSONObject.toJSONString(selectPhoneInfo(mobile));
//    }
//    
//    /**
//     * 立即充值
//     * @return
//     */
//    @RequestMapping(value = "/my/now-rechage.htm", method = {RequestMethod.GET, RequestMethod.POST })
//    public void toRechage(HttpServletRequest req, HttpServletResponse response,OperationEnvironment env) throws Exception{
//    	PersonMember user = getUser(req);
//    	if (logger.isInfoEnabled()) {
//            logger.info(LogUtil.appLog(OperateeTypeEnum.PHONERECHARGE.getMsg(), user, env));
//		}
//    	Date submitTime=new Date();
//    	String tradeOrderNo = req.getParameter("tradeOrderNo");//充值表-交易订单号
//    	
//    	String rechargeMoney=null;;//充值金额
//    	String paymentMoney=null;//支付金额
//    	String address=null;//归属地//话费充值
//		address = req.getParameter("address");//归属地
//		// paymentMoney = req.getParameter("paymentMoney");
//		rechargeMoney = req.getParameter("rechargeMoney");
//    	String mobile = req.getParameter("phoneNo");//充值号码
//    	String province = req.getParameter("province");
//    	
//    	//TODO 查询手机号当时是否可以充值 成功code返回1，返回其他不能充值
////    	TelCheckInfo telCheckInfo = ofRechargeServiceFacade.telCheck(mobile, rechargeMoney);
////    	if (!"1".equals(telCheckInfo.getCode())){
////    	    response.getWriter().write("该号码不支持此面额充值");
////    	    return;//未异步，返回
////    	}
//		if ((address == null) || (rechargeMoney == null))
//    	{
//    		try {
//    			response.getWriter().write("传入的手机号归属地异常");
//    		} catch (IOException e) {
//    			e.printStackTrace();
//    		}
//    		logger.info("传入的归属地异常");
//    		return;//未异步，返回
//    	}
//		// 支付金额需要从接口重新获取
//		PhoneRechageResponse rechargeResponse = selectPhoneInfo(mobile);
//		List<CardValue> list = rechargeResponse.getTypes();
//		for (CardValue cardValue : list) {
//			if (rechargeMoney.equals(cardValue.getDenomination())) {
//				paymentMoney = cardValue.getPrice();
//				break;
//			}
//		}
//		if (paymentMoney == null) {
//			logger.info("支付金额获取失败！");
//			return;
//		}
//    	tradeOrderNo = voucherService.regist(VoucherInfoType.TRADE.getCode());//生成交易订单号
////    	String rechargeOrderNo = voucherService.regist(VoucherInfoType.SIMPLE_ORDER.getCode());//生成充值订单号
//    	RechargeOrder rechargeOrder =new RechargeOrder();
//    	rechargeOrder.setTradeOrderNo(tradeOrderNo);//交易订单号
//    	rechargeOrder.setPayOrderNo("");//支付订单号
//    	rechargeOrder.setRechargeOrderNo("");//充值订单号
//    	rechargeOrder.setRechargePhone(mobile);//充值号码
//    	rechargeOrder.setRechargeAmount(new Money(rechargeMoney));//充值金额
//    	rechargeOrder.setPayAmount(new Money(paymentMoney));//支付金额
//    	rechargeOrder.setPayStatus(RechargeOrderPayStatus.PAYING.getCode());//支付状态
//    	rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.RECHARGEING.getCode());//充值状态-待充值
//    	rechargeOrder.setMemberId(user.getMemberId());//客户编号
//    	rechargeOrder.setGmtCreate(submitTime);//订单创建时间
////    	rechargeOrder.setOrgId(orgId);//运营商编码//移动：10086；联通：10010；电信：10001 
//    	rechargeOrder.setOrgId(province);//省份信息
//    	rechargeOrder.setOrgName(address);//运营商名称
//    	rechargeOrder.setChannelId("of");//充值渠道
//    	rechargeOrder.setChannelName("欧飞数码");//充值渠道名称
//    	rechargeOrder.setMemo("");//备注信息
//    	rechargeOrder.setExtension("");//扩展信息
//    	rechargeQueryServiceFacade.addRechargeOrder(rechargeOrder);//插入到充值表中
//    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//		String submitTimeStr = format.format(submitTime);
//    	BaseMember merchant=new BaseMember();
//    	merchant=memberService.queryMemberById(webResource.getRechargeEnterpriseMemberId(),env);//根据商户ID（即话费充值商户）查出商户账号
//		BaseMember personal=new BaseMember();
//		personal=memberService.queryMemberById(user.getMemberId(),env);//根据当前个人平台的ID查出个人账号
//		StringBuilder tradeList=new StringBuilder();
//		tradeList.append(tradeOrderNo+"~话费充值-充值"+rechargeMoney+".00元   "+mobile+"~"+paymentMoney+"~1~");
//		tradeList.append(paymentMoney+"~~"+merchant.getLoginName()+"~1~");
//		tradeList.append(submitTimeStr+"~~~~");//-------------
//		tradeList.append("~~"+submitTimeStr);//submitTimeStr
//		String Callbackurl=webResource.getRechargeReturnUrl();//网关-异步回调地址
//		tradeList.append("~"+Callbackurl+"~浙江海尔网络科技有限公司");//
//		
//		String ip = InetAddress.getLocalHost().getHostAddress();//操作电脑IP
//		
////    	String gatewayUrl = "http://mag.kjtpay.com/mag/gateway/receiveOrder.do";
//    	//String gatewayUrl = "http://zmag.kjtpay.com/mag/gateway/receiveOrder.do";
////        String gatewayUrl = "http://kmag.kjtpay.com/mag/gateway/receiveOrder.do";
////		String gatewayUrl = "http://192.168.20.37:8084/mag/gateway/receiveOrder.do";
//		String gatewayUrl=webResource.getGatewayUrl();
//        try {
//    	   req.setCharacterEncoding("UTF-8");
//		} catch (java.io.UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		} 
//        String  signTestMode="ITRUSSRV";
//		Map<String, String> sParaTemp = new TreeMap<String, String>();
//		sParaTemp.put("service", "create_mobile_recharge_trade");//接口名称-v1.1，允许重复提交
//		sParaTemp.put("version", "1.0");//接口版本
//		sParaTemp.put("partner_id", merchant.getMemberId());//合作者身份ID/商户号--200000010039
//		sParaTemp.put("_input_charset", "UTF-8");//字符集/参数编码字符集
//		sParaTemp.put("sign", ""); //签名--签约合作方的钱包唯一用户号 不可空（就是下面的map）
//		sParaTemp.put("sign_type", signTestMode);//签名方式--只支持ITRUSSRV不可空
//		sParaTemp.put("return_url", webResource.getRechargeNotifyUrl()+"?phoneNo="+mobile+"&tradeOrderNo="+tradeOrderNo);// 返回页面路径/页面跳转同步返回页面路径
//		sParaTemp.put("memo", "话费充值-充值"+rechargeMoney+"元   "+mobile);//备注
//		sParaTemp.put("request_no", tradeOrderNo);//商户网站请求号
//		sParaTemp.put("trade_list", tradeList.toString());//交易列表
//		sParaTemp.put("operator_id", user.getMemberId());//操作员ID 
//		sParaTemp.put("buyer_id", personal.getLoginName());//nimi买家"anonymous"
//		sParaTemp.put("buyer_id_type", "1");//买家ID类型--用户ID平台默认值为1 不可空
//		sParaTemp.put("buyer_ip", ip);//买家IP地址--可空
//		sParaTemp.put("pay_method", "");//支付方式^金额^备注  可空(支付方式为空，默认跳转收银台)
//		sParaTemp.put("go_cashier", "Y");//是否转收银台标识 Y使用(默认值)N不使用-
//		
//		
//        String signType = signTestMode;//签名方式--只支持ITRUSSRV不可空
//        String inputCharset = "UTF-8";
//        
//        logger.info("话费充值验签请求参数：{}", sParaTemp);
//        logger.info("话费充值同步url：{}"+webResource.getRechargeNotifyUrl());
//
//		//参数加密
//		Map<String, String> map = Core.buildRequestPara(sParaTemp,signType,SIGN_KEY,inputCharset);
//		response.setContentType("text/html;charset=UTF-8");   
//		response.setCharacterEncoding("UTF-8");
//		 
//		//拼装返回的信息 form表单 
//		StringBuilder html = new StringBuilder();
//		html.append("<script language=\"javascript\">window.onload=function(){document.pay_form.submit();}</script>\n");
//		html.append("<form id=\"pay_form\" name=\"pay_form\" action=\"").append(gatewayUrl).append("\" method=\"post\">\n");
//
//		for (String key : map.keySet()) {
//	        html.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value=\"" + map.get(key) + "\">\n");
//		}
//		html.append("</form>\n");
////		System.out.println(html.toString());
//		try {
//			response.getWriter().write(html.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//    }
//   
//    /**
//     * 网关跳转异步返回页面路径后，对支付后的订单进行处理
//     * @return
//     */
//    @RequestMapping(value = "/my/gatewayasyn.htm", method = {RequestMethod.GET, RequestMethod.POST })
//    public void cashierAsynReturn(HttpServletRequest req,HttpServletResponse response,TradeEnvironment env) {
////        PersonMember user = getUser(req);
//		try {
//			req.setCharacterEncoding("UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			logger.error("设置编码异常", e);
//		}
//    		
////    	logger.error("cashierAsynReturn");
//    	
//    	Map<String, String> sParaTemp = new TreeMap<String, String>();
//    	sParaTemp.put("notify_id", req.getParameter("notify_id"));//通知ID
//    	sParaTemp.put("notify_type", req.getParameter("notify_type"));//通知类型
//    	sParaTemp.put("notify_time", req.getParameter("notify_time"));//通知时间
//    	sParaTemp.put("_input_charset",req.getParameter("_input_charset"));//参数字符集编码
//    	sParaTemp.put("sign", req.getParameter("sign"));//签名
//    	sParaTemp.put("sign_type", req.getParameter("sign_type"));//签名方式
//    	sParaTemp.put("version", req.getParameter("version"));//版本号
//    	sParaTemp.put("outer_trade_no",req.getParameter("outer_trade_no"));//商户网站唯一订单号--商户网站唯一订单号
//    	sParaTemp.put("inner_trade_no",req.getParameter("inner_trade_no"));//钱包交易号-交易订单号
//    	sParaTemp.put("trade_status", req.getParameter("trade_status"));//交易状态
//    	sParaTemp.put("trade_amount", req.getParameter("trade_amount"));//交易金额
//    	sParaTemp.put("gmt_create", req.getParameter("gmt_create"));//交易创建时间
//    	sParaTemp.put("gmt_payment", req.getParameter("gmt_payment"));//交易支付时间
//    	sParaTemp.put("gmt_close", req.getParameter("gmt_close"));//交易关闭时间
//    	String inputCharset = req.getParameter("_input_charset");
//    	
//    	String outer_trade_no =req.getParameter("outer_trade_no");//充值表-交易订单号
//    	String tradeStatus = req.getParameter("trade_status");//交易状态
//    	String inner_trade_no=req.getParameter("inner_trade_no");// 充值表-支付订单号
////    	String gmt_payment=req.getParameter("gmt_payment");//支付时间
//    	String trade_amount=req.getParameter("trade_amount");//交易金额
////    	String tradeStatus = "TRADE_SUCCESS";
//    	//签名验证
//    	logger.info("话费充值回调，验证签名，参数：{}", sParaTemp);
//    	VerifyResult result = new VerifyResult();
//		try {
//			result = verifyClient.verifyBasic(
//					inputCharset,  sParaTemp);
//		} catch (Exception e) {
//			logger.error("验签异常", e);
//			return;
//		}
//		
//		try {
//			if (result.isSuccess()) {
//	    		RechargeOrder orderInfo = rechargeQueryServiceFacade.queryById(outer_trade_no);
//	    		if(orderInfo == null) {
//	    			return;
//	    		}
//	    		
//	    		RechargeOrder rechargeOrder = new RechargeOrder();
//	    		rechargeOrder.setTradeOrderNo(outer_trade_no);//交易订单号
//	    		rechargeOrder.setPayOrderNo(inner_trade_no);//支付订单号"101141700502505201704"
//	    		if (tradeStatus.equals("WAIT_BUYER_PAY")) {//支付状态
//	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.PAYING.getCode());
//	    		} else if (tradeStatus.equals("TRADE_FINISHED")) {
//	    			// 如果已经支付成功且在充值中或已退款则返回
//	    			if(orderInfo.getPayStatus().equals(RechargeOrderPayStatus.REFUNDED.getCode()) || (orderInfo.getPayStatus().equals(RechargeOrderPayStatus.SUCCESS.getCode())  
//	    					&& !orderInfo.getRechargeStatus().equals(RechargeOrderRechargeStatus.RECHARGEING.getCode()))) {
//	    				logger.info("已经支付成功且在充值中或已退款，不需要再支付：请求订单号："+outer_trade_no);
//	    				return;
//	    			}
//        			rechargeOrder.setGmtPaid(new Date());//支付状态
//	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.SUCCESS.getCode());
//	    			rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//	    			
//	    	   		String phone=orderInfo.getRechargePhone();//充值手机号
//	        		String rechargeAmount=orderInfo.getRechargeAmount().getAmount().toString();//充值金额
//	        		String ofResult=null;
//	        		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//	                String orderTime = format.format(new Date());
//    				try{
//    					//slsResult=slsRechargeServiceFacade.recharge(phone, rechargeAmount, outer_trade_no,province);//手拉手充值
//    				    //TODO
//    				    ofResult=ofRechargeServiceFacade.onlineOrder(phone, rechargeAmount, outer_trade_no, orderTime);
//    				    
//    				    logger.info("请求订单号：{}"+outer_trade_no+"调用欧飞充值：{}"+"；返回结果:{}"+ofResult);
//    				}catch(Exception e){
//    					logger.error("调用欧飞充值接口异常:{}"+e);
//    				}
//	        		JSONObject jsonObj = (JSONObject)JSONObject.parse(ofResult);//手拉手返回消息
//	        		String code=jsonObj.getString("gameState");//充值成功返回,如果成功将为1，澈消(充值失败)为9，充值中为0,只能当状态为9时，商户才可以退款给用户。
//	        		String rtnMsg=jsonObj.getString("rtnMsg");
//	        		logger.info("调用欧飞接口返回：code={}, msg={},outer_trade_no={}", code, rtnMsg,outer_trade_no);
//	        		
//	        		if(OfNotifyRechargeStatus.FAILURE.getCode().equals(code) || code==null)//充值失败后
//	                {
//	        		    rechargeOrder.setGmtRecharge(new Date());
//	                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.FAILURE.getCode());//充值失败
//	                    boolean updatestatus=false;
//	                    try {
//	                        updatestatus = rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//	                        logger.info("充值状态更新为充值失败:updatestatus={}",updatestatus);
//	                    } catch (Exception e) {
//	                        logger.error("更新充值状态失败:{}"+e);
//	                    }
//	                }else if(OfNotifyRechargeStatus.SUCCESS.getCode().equals(code)){
//	                    rechargeOrder.setGmtRecharge(new Date());
//	                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.SUCCESS.getCode());//充值状态-充值成功
//	                    try {
//	                        boolean updatestatus= rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//	                        logger.info("充值状态更新充值成功：updatestatus={}", updatestatus);
//	                    } catch (Exception e) {
//	                        logger.error("更新充值状态失败:{}"+e);
//	                    }
//	                }else if(OfNotifyRechargeStatus.PROGRESS.getCode().equals(code)){//充值中状态10分钟后查询
//	                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.PROGRESS.getCode());
//	                    try {
//                            boolean updatestatus= rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                            logger.info("充值状态更新充值中：updatestatus={}", updatestatus);
//                        } catch (Exception e) {
//                            logger.error("更新充值状态失败:{}"+e);
//                        }
//	    			    Timer timer = new Timer();  
//	    			    PhoneRechargeTask task = new PhoneRechargeTask(outer_trade_no);
//	    			    task.setOfRechargeServiceFacade(ofRechargeServiceFacade);
//	    			    task.setRechargeQueryServiceFacade(rechargeQueryServiceFacade);
//                        timer.schedule(task, 10*60*1000);
//	    			}
//	        		return;
//	    			
//	    		} 
//	    		else if (tradeStatus.equals("TRADE_CLOSED")) {
//	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.FAILURE.getCode());
////	    			System.out.println("交易状态：交易关闭");
//	    		}
//	    		rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//	    		
////	    		RechargeOrder orderInfo = rechargeQueryServiceFacade.queryById(outer_trade_no);
//	    		
//	    	}
//		} catch(Exception e) {
//			logger.error("手机充值异常", e);
//		}
//    }
//    /**
//     * 网关跳转同步返回页面路径后，对支付后的订单进行处理
//     * @return
//     */
//    @RequestMapping(value = "/my/gatewaysyn.htm",method = {RequestMethod.GET, RequestMethod.POST })
//    public ModelAndView rechargeResult(HttpServletRequest req,HttpServletResponse response) throws Exception{
//    	ModelAndView mv = new ModelAndView();
//    	String phoneNo =req.getParameter("phoneNo");//充值表-交易订单号
//    	String tradeOrderNo =req.getParameter("tradeOrderNo");//充值表-交易订单号
//    	RechargeOrder rechargeOrder = new RechargeOrder();
//    	rechargeOrder.setTradeOrderNo(tradeOrderNo);//交易订单号
//    	rechargeOrder.setPayStatus(RechargeOrderPayStatus.SUCCESS.getCode());//支付成功
////    	rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.RECHARGEING.getCode());//待充值
//    	rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//    	mv.addObject("phoneNo", phoneNo);
//    	mv.addObject("tradeOrderNo", tradeOrderNo);
//    	mv.setViewName(CommonConstant.URL_PREFIX + "/phoneRecharge/recharge-result");
//    	logger.info("话费充值同步，交易订单号："+tradeOrderNo);
//    	return mv;
//    }
//    
//    
}

