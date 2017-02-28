package com.yongda.site.wallet.app.action.phone;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.mns.client.domain.WxNotifyParam;
import com.netfinworks.recharge.facade.api.EwPayRechargeServiceFacade;
import com.netfinworks.recharge.facade.api.RechargeCardServiceFacade;
import com.netfinworks.recharge.facade.api.RechargeQueryServiceFacade;
import com.netfinworks.recharge.facade.enums.ChannelsEnum;
import com.netfinworks.recharge.facade.enums.OrderTypeEnum;
import com.netfinworks.recharge.facade.model.RechargeOrder;
import com.netfinworks.site.core.common.CardValue;
import com.netfinworks.site.core.common.FlowValue;
import com.netfinworks.site.core.common.PhoneRechageResponse;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.*;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.integration.call.CallRechargeService;
import com.netfinworks.site.ext.integration.member.AuthVerifyService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.voucher.service.facade.domain.enums.VoucherInfoType;
import com.yongda.rec.domain.enums.RechargeCode;
import com.yongda.rec.service.facade.domain.CallRechargeOrderVo;
import com.yongda.rec.service.facade.domain.PhoneRechargeProductWrap;
import com.yongda.rec.service.facade.domain.ext.PhoneRechargeProductExtVo;
import com.yongda.rec.service.facade.response.BaseResponse;
import com.yongda.site.ext.integration.message.MessageService;
import com.yongda.site.wallet.app.WebDynamicResource;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.common.util.LogUtil;
import com.yongda.site.wallet.app.util.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yongda.rec.service.facade.response.Response;
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

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;

	@Resource(name = "callRechargeService")
	private CallRechargeService callRechargeService;

    private static String[] TYPE = {"fees","flow"};

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
    @RequestMapping(value = "/phone/recharge", method = RequestMethod.GET)
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
	    	logger.info("绑定手机号码：{}",mobile);
	    	//云+注册的账号可能会查询不到绑定的手机
	    	if(StringUtils.isBlank(mobile)){
	    		mobile = user.getLoginName();
	    	}
	    	//调话费充值服务，查询历史记录号码，
			List<String> phoneList = callRechargeService.queryLastRechargeRecord(user.getMemberId(), 3);
	        if(phoneList.size()>0){
	        	map.put("phoneList", phoneList);
			}
	        //返回充值卡面值信息
	        PhoneRechageResponse res = selectPhoneInfo(mobile);
	        if(res.isSuccess()){//查询手机号信息成功
		    	map.put("goodsInfo", JSONObject.toJSON(res));
		    	restP.setMessage("获取充值卡信息成功");
				restP.setSuccess(true);
	        }else{
	        	log.info("暂不支持该手机号充值：{}",mobile);
				restP = ResponseUtil.buildExpResponse("F001","暂不支持该手机号码充值");
	        }
			map.put("mobile", mobile);//充值号码-默认-个人用户绑定的号码-显示在文本框中
	        restP.setData(map);
		}catch(Exception e){
			logger.error("获取充值卡信息异常",e);
			restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR.getErrorCode(),e.getMessage());
		}
		
        return restP;
    }
    
    /*
     * 查询手机号码对应的归属地，面值和售价
     * 
     * */
    public PhoneRechageResponse selectPhoneInfo(String mobile) {
        PhoneRechageResponse phoneP = new PhoneRechageResponse();
    	List<CardValue> feesList =new ArrayList<CardValue>();//话费集合
    	List<FlowValue> flowList =new ArrayList<FlowValue>();//流量集合
    	try {
			com.yongda.rec.service.facade.response.Response<PhoneRechargeProductWrap> response =
					callRechargeService.getProduct(mobile);
			PhoneRechargeProductWrap rechargeAddress = response.getBean();
    		phoneP.setMobile(mobile);
			if (RechargeCode.SUCCESS == response.getCode()){
            	phoneP.setSuccess(true);
            	phoneP.setMessage(response.getMessage());
            	phoneP.setProvince(rechargeAddress.getProvince());//归属地省份
            	phoneP.setAddress(rechargeAddress.getType());//运营商类型
				DecimalFormat   df = new  DecimalFormat("#.00");
				List<PhoneRechargeProductExtVo> feesProductsList = rechargeAddress.getFeesProducts();//话费
				for (PhoneRechargeProductExtVo feesProductExtVo : feesProductsList) {
             		//话费面额
             	    CardValue cardValue = new CardValue();
             	    cardValue.setDenomination(feesProductExtVo.getCardValue().toString());//面值
             	    cardValue.setPrice(feesProductExtVo.getCardValue().toString());//原价，面值 其实是一致的
             	    cardValue.setSellPrice(df.format(feesProductExtVo.getActiveRule().getSellingPrice()));//售价
					cardValue.setProductPkId(feesProductExtVo.getId());
					feesList.add(cardValue);
             	}

				List<PhoneRechargeProductExtVo> flowProductsList = rechargeAddress.getFlowProducts();//流量
             	for (PhoneRechargeProductExtVo flowProductExtVo : flowProductsList) {
             		//流量面额
             		FlowValue flowValue = new FlowValue();
             		flowValue.setCardNo(flowProductExtVo.getFlowProductId());//产品ID
             		flowValue.setDenomination(flowProductExtVo.getProductName());//面值
             		flowValue.setPrice(flowProductExtVo.getCardValue().toString());//原价
             		flowValue.setSellPrice(df.format(flowProductExtVo.getActiveRule().getSellingPrice()));//售价
					flowValue.setProductPkId(flowProductExtVo.getId());
             		flowValue.setLocalPrice(null);//暂且都为全国流量，本地流量的费用设为null,渠道变更后再改
					flowList.add(flowValue);
             	}

             	//排序
             	Collections.sort(feesList, new Comparator<CardValue>() {
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
             	phoneP.setTypes(feesList);
        		phoneP.setFlows(flowList);
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
		String mobile = req.getParameter("phoneNo");
		try{
			Map<String,Object> map = new HashMap<String,Object>();
			if(StringUtils.isBlank(mobile)||mobile.length()!=11 || !mobile.matches(RegexRule.MOBLIE)){
				logger.info("手机号码错误,请重新输入-{}",mobile);
				restP = ResponseUtil.buildExpResponse("F002","手机号码错误,请重新输入");
				return restP;
			}
 			//返回充值卡面值信息
			PhoneRechageResponse res = selectPhoneInfo(mobile);
			if(res.isSuccess()){//查询手机号信息成功
				map.put("goodsInfo", JSONObject.toJSON(res));
				restP.setMessage("获取充值卡信息成功");
				restP.setSuccess(true);
			}else{
				log.info("暂不支持该手机号充值：{}",mobile);
				map.put("mobile", mobile);
				restP = ResponseUtil.buildExpResponse("F001","暂不支持该手机号码充值");
			}
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
		RestResponse restP          = ResponseUtil.buildSuccessResponse();
		try {
			//获取请求信息
			PersonMember user = getUser(req);
			if (logger.isInfoEnabled()) {
                logger.info(LogUtil.appLog(OperateeTypeEnum.PHONERECHARGE.getMsg(), user, env));
            }
			String tradeOrderNo = req.getParameter("tradeOrderNo");//充值表-交易订单号
			String type = req.getParameter("type"); //充值类型 :fees-话费充值  flow-流量充值
			String rechargeMoney = req.getParameter("rechargeMoney");//充值金额
			String mobile = req.getParameter("phoneNo");//充值号码
			String productId = req.getParameter("productId");//产品Id
			String reqChannel= req.getParameter("reqChannel");//请求来源
			String paymentMoney=null;//支付金额
			if (logger.isInfoEnabled()) {
                logger.info("充值请求参数:type-{},rechargeMoney-{},phoneNo-{},productId-{},reqChannel-{}",
                        type,rechargeMoney,mobile,productId,reqChannel);
            }
			restP = VerifyRequestParamsUtil.phrechage(type,mobile,rechargeMoney,productId,restP);
			if (!restP.isSuccess())
                return restP;

			//查询该号码当月累计充值次数
			Response<Integer>  res = callRechargeService.countLimitd(mobile, user.getMemberId(),type);
			logger.info("该号码当月累计充值次数为:{}",res.getBean().toString());
			if(res.getBean()>=3){
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "同一个手机号每月充值不能超过3次");
            }

			// 支付金额需要从接口重新获取
			PhoneRechageResponse rechargeResponse = selectPhoneInfo(mobile);
			if(!rechargeResponse.isSuccess()){//查询手机号信息成功
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "暂不支持该手机号码充值");
                return restP;
            }

			List<CardValue> feesList = rechargeResponse.getTypes();
			List<FlowValue> flowList = rechargeResponse.getFlows();
			String productPkId = "";//产品主键ID
			if(OrderTypeEnum.FEES.getCode().equals(type)){//话费充值
                for (CardValue cardValue : feesList) {
                    String denomination = cardValue.getDenomination();
                    if (rechargeMoney.equals(denomination)) {
                        paymentMoney = cardValue.getSellPrice();
						productPkId  = cardValue.getProductPkId();
                        break;
                    }
                }
            }else{//流量充值:默认是全国流量，以后本地流量再修改
                for (FlowValue flowValue : flowList) {
                    if (productId.equals(flowValue.getCardNo()) && rechargeMoney.equals(flowValue.getPrice())) {
                        rechargeMoney= flowValue.getPrice();
                        paymentMoney = flowValue.getSellPrice();
							productPkId  = flowValue.getProductPkId();
                        break;
                    }
                }
            }
			if (paymentMoney == null) {
                logger.info("支付金额获取失败！");
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "支付金额获取失败！");
            }

			//话费或流量充值订单入库
			tradeOrderNo = voucherService.regist(VoucherInfoType.TRADE.getCode());//生成交易订单号
			CallRechargeOrderVo callRechargeOrderVo = new CallRechargeOrderVo();
			callRechargeOrderVo.setTradeOrderNo(tradeOrderNo);//交易订单号
			callRechargeOrderVo.setProductType(type);
			callRechargeOrderVo.setPayOrderNo("");//支付订单号传空
			callRechargeOrderVo.setChannelOrderNo("");//渠道订单号传空
			callRechargeOrderVo.setRechargeNumber(mobile);//充值号码
			callRechargeOrderVo.setCardValue(new BigDecimal(rechargeMoney));//充值金额
			callRechargeOrderVo.setPayAmount(new BigDecimal(paymentMoney));//支付金额
			callRechargeOrderVo.setPayStatus(RechargeOrderPayStatus.PAYING.getCode());//支付状态
			callRechargeOrderVo.setRechargeStatus(RechargeOrderRechargeStatus.RECHARGEING.getCode());//充值状态-待充值
			callRechargeOrderVo.setMemberId(user.getMemberId());//客户编号
			callRechargeOrderVo.setRechargeDate(new Date());//订单创建时间
			callRechargeOrderVo.setProductPkId(productPkId);//产品主键ID 必传
			callRechargeOrderVo.setNotifyStatus("N");//通知状态
			callRechargeOrderVo.setOrderSource(OilOrderSource.ONGDA_PAY.getCode());//订单来源  永达支付

			callRechargeService.createOrder(callRechargeOrderVo);
			//组装参数，调网关接口
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			String submitTimeStr = format.format(new Date());
			BaseMember merchant = new BaseMember();
			merchant=memberService.queryMemberById(webResource.getRechargeEnterpriseMemberId(),env);//根据商户ID（即话费充值商户）查出商户账号
			BaseMember personal = new BaseMember();
			personal=memberService.queryMemberById(user.getMemberId(),env);//根据当前个人平台的ID查出个人账号
			StringBuilder tradeList=new StringBuilder();
			if(OrderTypeEnum.FEES.getCode().equals(type)){
                tradeList.append(tradeOrderNo+"~话费充值-永达"+rechargeMoney+"元"+mobile+"~"+paymentMoney+"~1~");
            }else{
                tradeList.append(tradeOrderNo+"~流量充值-永达"+rechargeMoney+"元"+mobile+"~"+paymentMoney+"~1~");
            }
			tradeList.append(paymentMoney+"~~"+merchant.getLoginName()+"~1~");
			tradeList.append(submitTimeStr+"~~~~");//-------------
			tradeList.append("~~"+submitTimeStr);//submitTimeStr
			String Callbackurl=webResource.getWalletrechargeReturnUrl();//网关-异步回调地址
			tradeList.append("~"+Callbackurl+"~浙江永达互联网金额信息服务有限公司");
			String ip = InetAddress.getLocalHost().getHostAddress();//操作电脑IP
			String gatewayUrl=webResource.getGatewayUrl();
			try {
                req.setCharacterEncoding("UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
			Map<String, Object> sParaTemp = new TreeMap<String, Object>();
			sParaTemp.put("service", "create_mobile_recharge_trade");//接口名称-v1.0,
			sParaTemp.put("version", "1.0");//接口版本
			sParaTemp.put("partner_id", merchant.getMemberId());//合作者身份ID/商户号--200000010039
			sParaTemp.put("_input_charset", "UTF-8");//字符集/参数编码字符集
			sParaTemp.put("sign", ""); //签名--签约合作方的钱包唯一用户号 不可空（就是下面的map）
			sParaTemp.put("sign_type", "");//签名方式--只支持ITRUSSRV不可空
			//sParaTemp.put("return_url", webResource.getWalletrechargeNotifyUrl());// 返回页面路径/页面跳转同步返回页面路径
			sParaTemp.put("memo", "话费充值-永达"+rechargeMoney+"元"+mobile);//备注
			//sParaTemp.put("memo", "-"+rechargeMoney+"-"+mobile);//备注
			sParaTemp.put("risk_item", "{\"recharge_mobile\":\""+mobile+"\"}");//充值手机号
			sParaTemp.put("request_no", tradeOrderNo);//商户网站请求号
			sParaTemp.put("trade_list", tradeList.toString());//交易列表
			sParaTemp.put("operator_id", user.getMemberId());//操作员ID
			sParaTemp.put("buyer_id", personal.getLoginName());//nimi买家"anonymous"
			sParaTemp.put("buyer_id_type", "1");//买家ID类型--用户ID平台默认值为1 不可空
			sParaTemp.put("buyer_ip", ip);//买家IP地址--可空
			sParaTemp.put("pay_method", "");//支付方式^金额^备注  可空(支付方式为空，默认跳转收银台)
			sParaTemp.put("go_cashier", "N");//是否转收银台标识 Y使用(默认值)N不使用-
			if("wap".equals(reqChannel)){
                sParaTemp.put("access_channel", "WAP");//请求渠道：WEB/WAP
            }else{
                sParaTemp.put("access_channel", "WEB");
            }
			logger.info("话费充值验签请求参数：{}", sParaTemp);
			logger.info("话费充值异步url：{}"+webResource.getWalletrechargeReturnUrl());

			//MD5加签  请求网关
			restP = HttpUtil.doPost(gatewayUrl,sParaTemp,webResource.getMagSignkey(),webResource.getTimeout(),restP);
		} catch (Exception e) {
			logger.error("话费立即充值失败：{}",e);
			return ResponseUtil.buildExpResponse(CommonDefinedException.TRADE_DATA_MATCH_ERROR, e.getMessage());
		}
		return restP;
    }



    /**
     * 网关跳转异步返回页面路径后，对支付后的订单进行处理
     * @return
     */ 
    @RequestMapping(value = "/phone/skRecharge", method = {RequestMethod.GET, RequestMethod.POST })
    public void cashierAsynReturn(HttpServletRequest req,HttpServletResponse response,TradeEnvironment env) {
    	logger.info("skRecharge...start");
		try {
			req.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("设置编码异常", e);
		}
    	
    	Map<String, Object> sParaTemp = new TreeMap<String, Object>();
    	sParaTemp.put("notify_id", req.getParameter("notify_id"));//通知ID
    	sParaTemp.put("notify_type", req.getParameter("notify_type"));//通知类型
    	sParaTemp.put("notify_time", req.getParameter("notify_time"));//通知时间
    	sParaTemp.put("_input_charset",req.getParameter("_input_charset"));//参数字符集编码
    	//sParaTemp.put("sign", req.getParameter("sign"));//签名
    	//sParaTemp.put("sign_type", req.getParameter("sign_type"));//签名方式
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
    	logger.info("wallet_trade_status:{},支付订单号：{},交易订单号：{},",tradeStatus,inner_trade_no,outer_trade_no);
		/**
		 * 对数据进行验签  先对数据按照英文字母排序
         */
		String prestr = MapUtil.createLinkString(sParaTemp, false);
		try {
			if (!MD5Util.verify(prestr,req.getParameter("sign"),webResource.getMagSignkey(),inputCharset)){
				logger.error("验签异常");
				return;
			}
		} catch (Exception e) {
			logger.error("验签异常", e);
			return;
		}

		try {
			//if (flag) {
				//按交易订单号查询订单信息
				//Response<RechargeOrder> orderInfo = rechargeQueryServiceFacade.queryById(outer_trade_no);
			    Response<CallRechargeOrderVo> orderInfo = callRechargeService.getByTradeOrderNo(outer_trade_no);
	    		if(orderInfo == null) {
	    			return;
	    		}
	    		
	    		//RechargeOrder rechargeOrder = new RechargeOrder();
			    CallRechargeOrderVo rechargeOrder = new CallRechargeOrderVo();
	    		rechargeOrder.setTradeOrderNo(outer_trade_no);//交易订单号
	    		rechargeOrder.setPayOrderNo(inner_trade_no);//支付订单号
				rechargeOrder.setUpdateDate(new Date());
				rechargeOrder.setPayDate(new Date());//支付时间
	    		if (tradeStatus.equals("WAIT_BUYER_PAY")) {//支付状态
	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.PAYING.getCode());
	    		} else if (tradeStatus.equals("TRADE_FINISHED")) {
	    			// 如果已经支付成功且在充值中或已退款则返回
					CallRechargeOrderVo rechargeOrderInfo = orderInfo.getBean();
	    			if(rechargeOrderInfo.getPayStatus().equals(RechargeOrderPayStatus.REFUNDED.getCode()) || (rechargeOrderInfo.getPayStatus().equals(RechargeOrderPayStatus.SUCCESS.getCode())  
	    					&& !rechargeOrderInfo.getRechargeStatus().equals(RechargeOrderRechargeStatus.RECHARGEING.getCode()))) {
	    				logger.info("已经支付成功且在充值中或已退款，不需要再支付：请求订单号："+outer_trade_no);
	    				response.getWriter().write("success");
	    				response.getWriter().flush();	
	    				response.getWriter().close();
	    				return;
	    			}
	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.SUCCESS.getCode());//支付状态
					callRechargeService.editOrder(rechargeOrder);//更新充值记录
	    			
	    	   		String phone  = rechargeOrderInfo.getRechargeNumber();//充值手机号
					String amount = rechargeOrderInfo.getCardValue().toString();//充值金额
					String rechargeAmount = amount.substring(0,amount.indexOf("."));
	        		//微信推送
	        		sendRechargeMessage(rechargeOrderInfo.getProductType(),phone,rechargeAmount,
	        				outer_trade_no,rechargeOrderInfo.getMemberId());
	        		String skResult=null;
					BaseResponse resp = null;
    				try{
    					 /*Response<Boolean> ewPayRechargeResult = ewPayRechargeServiceFacade.recharge(outer_trade_no, rechargeAmount,
    							 phone, rechargeOrderInfo.getProductPkId(),rechargeType);*/
						resp =  callRechargeService.recharge(outer_trade_no);
						logger.info("话费充值响应信息-{},订单号-{}",outer_trade_no,resp.toString());
    				}catch(Exception e){
    					logger.error("调用速卡充值接口异常:{}"+e);
    				}
	        		
    				//以下注释内容全部改成在
	        		if(SkNotifyRechargeStatus.FAILURE.getCode().equals(skResult))//充值失败后
	                {
	        			 logger.info("充值失败,订单号：{}",inner_trade_no);
	                }else if(SkNotifyRechargeStatus.SUCCESS.getCode().equals(skResult)){//充值成功
	                	 logger.info("充值成功,订单号：{}",inner_trade_no);
	                }
	        		response.getWriter().write("success");
	        		response.getWriter().flush();	
	        		response.getWriter().close();
	        		return;
	    			
	    		}else if (tradeStatus.equals("TRADE_CLOSED")) {
	    			rechargeOrder.setPayStatus(RechargeOrderPayStatus.FAILURE.getCode());
	    		}
	    		//rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
				callRechargeService.editOrder(rechargeOrder);//更新充值记录
	    		response.getWriter().write("success");
	    		response.getWriter().flush();	
	    		response.getWriter().close();
	    	//}
		} catch(Exception e) {
			logger.error("手机充值异常", e);
		}
    }

    public void sendRechargeMessage(String type,String rechargePhone,String rechargeAmount
            ,String payNo,String memberId){
        String rechargeName = SkRechargeType.TRADE_FEES.getMessage();
        String remark = SkRechargeType.TRADE_FEES.getRemark();
        if(SkRechargeType.TRADE_FLOW.getCode().equalsIgnoreCase(type)){//流量充值
            rechargeName = SkRechargeType.TRADE_FLOW.getMessage();
            remark       = SkRechargeType.TRADE_FLOW.getRemark();
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
            logger.info("推送消息参数,rechargePhone：{},payNo：{},rechargeAmount:{},rechargeName:{}",rechargePhone,
					payNo,rechargeAmount,rechargeName);
            messageService.sendWxMsg(memberId, phonePayTemplateId, param);
        } catch (ErrorCodeException.CommonException e) {
            logger.info("sendWxMsg推送消息失败");
        }
    }

}

