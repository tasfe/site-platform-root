package com.yongda.site.wallet.app.action.trade;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.recharge.facade.api.EwPayRechargeServiceFacade;
import com.netfinworks.recharge.facade.model.RechargeAddress;
import com.netfinworks.recharge.facade.response.Response;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.enums.BankType;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.oil.OilRechargeService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.tradeservice.facade.model.paymethod.PayMethod;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.response.PayMethodQueryResponse;
import com.netfinworks.tradeservice.facade.response.TradeDetailQueryResponse;
import com.yongda.site.domain.domain.vo.OilRechargeOrderDetailVO;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.common.util.StrUtil;
import com.yongda.site.wallet.app.util.ResponseUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/12/6-11:25<br>
 */
@Controller
@RequestMapping(value = "/trade")
public class TradeQueryDetailAction extends BaseAction {

    @Resource(name = "defaultTradeQueryService")
    private DefaultTradeQueryService defaultTradeQueryService;

    @Resource(name = "memberService")
    private MemberService memberService;

    @Resource(name="ewPayRechargeServiceFacade")
    private EwPayRechargeServiceFacade ewPayRechargeServiceFacade;

    @Resource(name = "tradeService")
    private TradeService tradeService;

    @Resource(name="oilRechargeService")
    protected OilRechargeService oilRechargeService;

    /**
     * 订单详情
     *
     * @return
     */
    @RequestMapping(value = "search/detail",method = RequestMethod.POST)
    @ResponseBody
    public RestResponse searchAll(HttpServletRequest request, HttpServletResponse resp,
                                  OperationEnvironment env)
            throws Exception {
        RestResponse restP      = ResponseUtil.buildSuccessResponse();
        try {
            String tradeVoucherNo = request.getParameter("tradeVoucherNo");
            logger.info("search/detail..start 交易凭证号：{}",tradeVoucherNo);
            if (StringUtils.isBlank(tradeVoucherNo)){
                logger.error("tradeVoucherNo为空，请重新输入");
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "订单号不能为空");
            }
            Map<String, Object> map = new HashMap<String, Object>();
            //查询交易记录
            TradeDetailQueryResponse page = defaultTradeQueryService.queryDetail(tradeVoucherNo, env);
            map.put("pageInfo", page.getTradeBasic());
            //map.put("buyerId", page.getBuyerId());
            restP.setData(map);
            restP.setSuccess(true);
            logger.info("查询订单详情交易记录,响应信息：{}",restP.getData().toString());
        } catch (Exception e) {
            logger.error("查询出错：{}",e);
            restP.setSuccess(false);
            restP.setMessage("交易查询错误：{}"+e.getMessage());
        }
        return restP;
    }


    /**
     * 订单支付方式
     *
     * @return
     */
    @RequestMapping(value = "search/paymethod",method = RequestMethod.POST)
    @ResponseBody
    public RestResponse searchMethod(HttpServletRequest request, HttpServletResponse resp,
                                  OperationEnvironment env)
            throws Exception {
        RestResponse restP      = ResponseUtil.buildSuccessResponse();
        try {
            String tradeVoucherNo = request.getParameter("tradeVoucherNo");
            logger.info("[订单支付详情..request] 交易凭证号：{}",tradeVoucherNo);
            if (StringUtils.isBlank(tradeVoucherNo)){
                logger.error("tradeVoucherNo为空，请重新输入");
                return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, "交易凭证号不能为空");
            }
            Map<String, Object> map = new HashMap<String, Object>();
            //查询支付交易记录
            PayMethodQueryResponse page = defaultTradeQueryService.queryPayMethods(tradeVoucherNo, env);
            //查询订单详细
            TradeDetailQueryResponse pageDetail = defaultTradeQueryService.queryDetail(tradeVoucherNo, env);
            TradeBasicInfo tradeBasicInfo = pageDetail.getTradeBasic();
            String bizProductCode = tradeBasicInfo.getBizProductCode();//产品id
            if (page !=null && page.getPayMethodList()!=null){
                PayMethod payMethod = page.getPayMethodList().get(0);
                JSONObject jsonOther   = JSONObject.fromObject(payMethod);
                JSONObject jsonObject  = JSONObject.fromObject(payMethod.getExtension());
                String cardNo          = (String)jsonObject.get("cardNo");
                String bankCode        = (String)jsonOther.get("bankCode");
                BankType bt = BankType.getByCode(bankCode);
                String bankName = bt.getMessage().contains("|")?
                        bt.getMessage().substring(0,bt.getMessage().indexOf("|")):bt.getMessage();
                String recharge_mobile = null;
                if (bizProductCode.equals(TradeType.phone_recharge.getBizProductCode())){//话费充值
                     recharge_mobile = (String)jsonObject.get("recharge_mobile");
                    //查询手机号码归属地
                    Response<RechargeAddress> response = ewPayRechargeServiceFacade.queryAddress(recharge_mobile);
                    if (response!=null && response.getBean()!=null){
                        map.put("address",response.getBean().getType());
                    }
                }else if (bizProductCode.equals(TradeType.PAY_INSTANT.getBizProductCode())){//及时到账  展示油卡的油卡号
                    //查询充值成功的油卡订单列表
                    List<OilRechargeOrderDetailVO> orderInfos = oilRechargeService.getOrderDetails(null,tradeBasicInfo.getTradeSourceVoucherNo(),null);
                    OilRechargeOrderDetailVO rechargeOrderInfo = orderInfos.get(0);
                    recharge_mobile  = rechargeOrderInfo.getRechargeNumber();//油卡卡号
                    map.put("address",rechargeOrderInfo.getOrgName());//运营商名称
                }
                map.put("bankName",bankName);//银行名称
                map.put("recharge_mobile",StringUtils.isNotBlank(recharge_mobile)?recharge_mobile:"");//充值号码
                map.put("bankNo", StrUtil.captureBankCard(super.getDataByTicket(cardNo)));//银行卡号
                map.put("pageInfo", page.getPayMethodList().get(0));
            }else {
                map.put("pageInfo", null);
            }

            BaseMember merchant = new BaseMember();
            //根据商户ID（即话费充值商户）查出商户账号
            merchant=memberService.queryMemberById(tradeBasicInfo.getSellerId(),env);

            map.put("gmtSubmit",tradeBasicInfo.getGmtSubmit());//订单创建时间
            map.put("bizProductCode",tradeBasicInfo.getBizProductCode());//产品id
            map.put("tradeSourceVoucherNo",tradeBasicInfo.getTradeSourceVoucherNo());//交易原始凭证号
            map.put("tradeVoucherNo",tradeBasicInfo.getTradeVoucherNo());//用户订单号
            map.put("status",searchRefundInfo(tradeVoucherNo,env)==true?"985":tradeBasicInfo.getStatus());//状态
            map.put("memberName",merchant.getMemberName());//商户名称
            map.put("sellerId",tradeBasicInfo.getSellerId());//商户号
            restP.setData(map);
            restP.setSuccess(true);
            restP.setMessage(page.getResultMessage());
            logger.info("查询订单支付方式,响应信息：{}",restP.getData().toString());
        } catch (Exception e) {
            logger.error("查询出错：{}",e);
            restP.setSuccess(false);
            restP.setMessage("交易查询错误：{}"+e.getMessage());
        }
        return restP;
    }

    /**
     * 退款详情
     *
     * @return
     */
    @RequestMapping(value = "search/refundDetail",method = RequestMethod.POST)
    @ResponseBody
    public RestResponse searchRefundDetail(HttpServletRequest request, HttpServletResponse resp,
                                     OperationEnvironment env){
        RestResponse restP      = ResponseUtil.buildSuccessResponse();
        try {
            String origTradeVoucherNo = request.getParameter("origTradeVoucherNo");//退款的原始订单号可以关联出最原始的支付订单号信息
            String tradeVoucherNo     = request.getParameter("tradeVoucherNo");//退款订单号
            String tradeSourceVoucherNo = request.getParameter("tradeSourceVoucherNo");//流水号
            String sellerId           = request.getParameter("sellerId");
            logger.info("[search..refundDetail..request] origTradeVoucherNo-{},tradeVoucherNo-{},tradeSourceVoucherNo-{},sellerId-{}",
                    origTradeVoucherNo,tradeVoucherNo,tradeSourceVoucherNo,sellerId);
            TradeDetailQueryResponse  respe = tradeService.queryRefundDetail(tradeSourceVoucherNo,sellerId,env);
            Map<String, Object> map         = new HashMap<String, Object>();
            TradeBasicInfo tradeBasicInfo   = respe.getTradeBasic();
            map.put("gmtSubmit",tradeBasicInfo.getGmtSubmit());//退款创建时间
            map.put("bizProductCode",tradeBasicInfo.getBizProductCode());//产品id
            map.put("tradeVoucherNo",tradeVoucherNo);//退款订单号
            map.put("tradeSourceVoucherNo",tradeSourceVoucherNo);//退款流水号
            map.put("status",tradeBasicInfo.getStatus());
            //查询支付交易记录
            PayMethodQueryResponse page = defaultTradeQueryService.queryPayMethods(origTradeVoucherNo, env);
            if (page !=null && page.getPayMethodList()!=null){
                PayMethod payMethod = page.getPayMethodList().get(0);
                JSONObject jsonOther   = JSONObject.fromObject(payMethod);
                JSONObject jsonObject  = JSONObject.fromObject(payMethod.getExtension());
                //String recharge_mobile = (String)jsonObject.get("recharge_mobile");
                String cardNo          = (String)jsonObject.get("cardNo");
                String bankCode        = (String)jsonOther.get("bankCode");
                BankType bt = BankType.getByCode(bankCode);
                String bankName = bt.getMessage().contains("|")?
                        bt.getMessage().substring(0,bt.getMessage().indexOf("|")):bt.getMessage();
                map.put("bankName",bankName);
                map.put("bankNo", StrUtil.captureBankCard(super.getDataByTicket(cardNo)));
                map.put("pageInfo", page.getPayMethodList().get(0));
            }else {
                map.put("pageInfo", null);
            }

            BaseMember merchant = new BaseMember();
            //根据商户ID（即话费充值商户）查出商户账号
            merchant=memberService.queryMemberById(tradeBasicInfo.getSellerId(),env);
            map.put("memberName",merchant.getMemberName());//商户名称
            map.put("sellerId",tradeBasicInfo.getSellerId());//商户号
            restP.setData(map);
            restP.setSuccess(true);
            restP.setMessage(page.getResultMessage());
            logger.info("查询退款,响应信息：{}",restP.getData().toString());
        } catch (Exception e) {
            logger.error("",e);
            restP.setSuccess(false);
            restP.setMessage("退款交易查询错误：{}"+e.getMessage());
        }
        return restP;
    }

    /**查询退款接口*****/
    private Boolean searchRefundInfo(String tradeVoucherNo, OperationEnvironment env){
        try {
            TradeRequest tradeRequest = new TradeRequest();
            tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));// 参照查询退款接口默认时间
            tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));
            // 分页信息
            QueryBase queryBase5 = new QueryBase();
            queryBase5.setCurrentPage(1);
            tradeRequest.setQueryBase(queryBase5);
            tradeRequest.setOrigTradeVoucherNo(tradeVoucherNo);
            List<String> tradeStatus = new ArrayList<String>();
            tradeStatus.add("951");
            tradeRequest.setTradeStatus(tradeStatus);
            PageResultList refundListPage = defaultTradeQueryService.queryRefundList(
                    tradeRequest, env);
            logger.info("查询退款响应信息：{}",refundListPage.getInfos());
            return refundListPage.getInfos()==null?false:true;
        } catch (ServiceException e) {
            logger.error("",e);
        }
        return null;
    }

}
