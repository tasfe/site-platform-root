package com.netfinworks.site.web.action.phone;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.recharge.facade.api.RechargeQueryServiceFacade;
import com.netfinworks.recharge.facade.model.RechargeOrder;
import com.netfinworks.recharge.facade.request.RechargeQueryRequest;
import com.netfinworks.recharge.facade.response.RechargeQueryResponse;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AccessChannel;
import com.netfinworks.site.domain.enums.RechargeOrderPayStatus;
import com.netfinworks.site.domain.enums.RechargeOrderRechargeStatus;
import com.netfinworks.site.domainservice.convert.RefundConvertor;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.tradeservice.facade.request.RefundRequest;
import com.netfinworks.tradeservice.facade.response.RefundResponse;
@Controller
public class PhoneRefundTask {
    
//    @Resource
//    private XUCache<String> xuCache;
//    
//    @Resource(name = "memberService")
//    private MemberService memberService;
//    
//    @Resource(name = "tradeService")
//    private TradeService tradeService;
//    
//    @Resource(name = "voucherService")
//    private VoucherService             voucherService;
//    
//    @Resource(name="rechargeQueryServiceFacade")
//    private RechargeQueryServiceFacade rechargeQueryServiceFacade;
//    
//    private static final Logger logger = LoggerFactory.getLogger(PhoneRefundTask.class);
//    
//    public  boolean loadCache(String methodName){       
//        String cacheKey=methodName+"_"+DateUtils.getStringDate("yyyy-MM-dd");
//        logger.info("loadCache key={}",cacheKey);
//        boolean result=xuCache.add(cacheKey, "true", 1*24*60*60);
//        return result;
//    }
//    
//    /**
//     * 话费充值未充值成功的订单退款
//     */
//    public void phoneRefundOrder(){         
//        if(this.loadCache("phoneRefund")){
//            logger.info("phoneRefund begin");
//            try{
//                phoneRefund();
//            }catch (Exception e){
//                logger.error("error",e);
//            }
//            logger.info("phoneRefund end");
//        }
//    }
//    
//    /**
//     * 测试
//     */
//    public void test(){         
//        if(this.loadCache("test")){
//            logger.info("test begin");
//            try{
//                phoneRefund();
//            }catch (Exception e){
//                logger.error("error",e);
//            }
//            logger.info("test end");
//        }
//    }
//    
//    public void phoneRefund(){
//        try {
//            //查询出前一天支付成功，但充值失败的订单
//            String secondFormat = "yyyy-MM-dd HH:mm:ss";        
//            DateFormat dateFormat = new SimpleDateFormat(secondFormat);
//            RechargeQueryRequest  rechargeReq=new RechargeQueryRequest();
//            String queryDate=DateUtils.getBeforeDate("yyyy-MM-dd");
//            rechargeReq.setStartDate(dateFormat.parse(queryDate+" 00:00:00"));
//            rechargeReq.setEndDate(dateFormat.parse(queryDate+" 23:59:59"));
//            rechargeReq.setTimeType("gmtRecharge");
//            rechargeReq.setPayStatus(RechargeOrderPayStatus.SUCCESS.getCode());
//            rechargeReq.setRechargeStatus(RechargeOrderRechargeStatus.FAILURE.getCode());
//            rechargeReq.setPageSize(50000);
//            RechargeQueryResponse orderInfo = rechargeQueryServiceFacade.query(rechargeReq);
//            
//            List<RechargeOrder> rechargeOrders = orderInfo.getRechargeOrderList();
//            TradeEnvironment env = new TradeEnvironment();
//            String ip = InetAddress.getLocalHost().getHostAddress();//操作电脑IP
//            env.setClientIp(ip);
//            for(RechargeOrder rechargeOrder : rechargeOrders){
//                BaseMember currUser = memberService.queryMemberById(rechargeOrder.getMemberId(),env);//
//                TradeRequestInfo request = RefundConvertor.convertRefundApply(currUser, env);
//                
//                // 生成退款交易凭证号
//                String voucherNo = voucherService.recordTradeVoucher(request);//TradeRequestInfo
//                RefundRequest refundRequest=new RefundRequest();
//                refundRequest.setTradeVoucherNo(voucherNo);//退款号
//                refundRequest.setTradeSourceVoucherNo(request.getTradeSourceVoucherNo());
//                refundRequest.setOrigTradeVoucherNo(rechargeOrder.getPayOrderNo());//充值表-支付订单号
//                refundRequest.setAccessChannel(AccessChannel.WEB.getCode());
//                refundRequest.setGmtSubmit(new Date());
//                refundRequest.setRefundAmount(rechargeOrder.getPayAmount());//交易金额
//                RefundResponse refundResponse =new RefundResponse();
//                try {
//                    logger.info("退款请求：{}"+refundRequest);
//                    refundResponse = tradeService.simpleRefund(refundRequest, env);//做退款
//                    logger.info("退款响应：{}"+refundResponse);
//                } catch (Exception e) {
//                    logger.error("退款异常：{}"+e);
//                }
//                if(refundResponse.isSuccess())
//                {
//                    rechargeOrder.setPayStatus(RechargeOrderPayStatus.REFUNDED.getCode());//支付状态-已退款
//                    boolean updatestatus=false;
//                    try {
//                        updatestatus = rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                        logger.info("充值状态更新已退款：退款响应={},updatestatus={}", refundResponse,updatestatus);
//                    } catch (Exception e) {
//                        logger.error("更新支付状态异常:{}",e);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("error",e);
//        }
//    }
}
