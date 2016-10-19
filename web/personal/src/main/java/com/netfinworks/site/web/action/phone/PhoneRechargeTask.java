package com.netfinworks.site.web.action.phone;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.recharge.facade.api.OfRechargeServiceFacade;
import com.netfinworks.recharge.facade.api.RechargeQueryServiceFacade;
import com.netfinworks.recharge.facade.model.RechargeOrder;
import com.netfinworks.site.domain.enums.OfNotifyRechargeStatus;
import com.netfinworks.site.domain.enums.RechargeOrderRechargeStatus;

//public  class PhoneRechargeTask extends TimerTask { 
    
   
//    private OfRechargeServiceFacade ofRechargeServiceFacade;
//    
//    
//    private RechargeQueryServiceFacade rechargeQueryServiceFacade;
//    
//    public void setOfRechargeServiceFacade(OfRechargeServiceFacade ofRechargeServiceFacade) {
//        this.ofRechargeServiceFacade = ofRechargeServiceFacade;
//    }
//
//    public void setRechargeQueryServiceFacade(RechargeQueryServiceFacade rechargeQueryServiceFacade) {
//        this.rechargeQueryServiceFacade = rechargeQueryServiceFacade;
//    }
//
//    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
//    
//    Timer timer = new Timer(); 
//    private static int tryTimes = 1;
//    
//    private String orderNo;
//    
//    public PhoneRechargeTask(String orderNo){
//        this.orderNo = orderNo;
//    }
//    
//    @Override  
//    public void run() {  
//        logger.info("第{}次查询充值状态，商户订单号：{}",tryTimes,orderNo);
//        RechargeOrder rechargeOrder = new RechargeOrder();
//        String code=queryOrderInfo(orderNo);
//        //10分钟后查询充值状态
//        if(tryTimes == 1){
//            if(!"0".equals(code)){
//                if(OfNotifyRechargeStatus.FAILURE.getCode().equals(code))//充值失败后
//                {
//                    rechargeOrder.setGmtRecharge(new Date());
//                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.FAILURE.getCode());//充值失败
//                    boolean updatestatus=false;
//                    try {
//                        updatestatus = rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                        logger.info("充值状态更新为充值失败:updatestatus={}",updatestatus);
//                    } catch (Exception e) {
//                        logger.error("更新充值状态失败:{}"+e);
//                    }
//                }else if(OfNotifyRechargeStatus.SUCCESS.getCode().equals(code)){
//                    rechargeOrder.setGmtRecharge(new Date());
//                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.SUCCESS.getCode());//充值状态-充值成功
//                    try {
//                        boolean updatestatus= rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                        logger.info("充值状态更新充值成功：updatestatus={}", updatestatus);
//                    } catch (Exception e) {
//                        logger.error("更新充值状态失败:{}"+e);
//                    }
//                }
//                timer.cancel();
//            }else{
//                timer.schedule(new PhoneRechargeTask(orderNo), 25*60*1000);
//            }
//        }
//        //25分钟后查询充值状态
//        if(tryTimes == 2){
//            if(!"0".equals(code)){
//                if(OfNotifyRechargeStatus.FAILURE.getCode().equals(code))//充值失败后
//                {
//                    rechargeOrder.setGmtRecharge(new Date());
//                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.FAILURE.getCode());//充值失败
//                    boolean updatestatus=false;
//                    try {
//                        updatestatus = rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                        logger.info("充值状态更新为充值失败:updatestatus={}",updatestatus);
//                    } catch (Exception e) {
//                        logger.error("更新充值状态失败:{}"+e);
//                    }
//                }else if(OfNotifyRechargeStatus.SUCCESS.getCode().equals(code)){
//                    rechargeOrder.setGmtRecharge(new Date());
//                    rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.SUCCESS.getCode());//充值状态-充值成功
//                    try {
//                        boolean updatestatus= rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                        logger.info("充值状态更新充值成功：updatestatus={}", updatestatus);
//                    } catch (Exception e) {
//                        logger.error("更新充值状态失败:{}"+e);
//                    }
//                }
//                timer.cancel();
//            }else{
//                timer.schedule(new PhoneRechargeTask(orderNo), 40*60*1000);
//            }
//        }
//        //40分钟后查询充值状态
//        if(tryTimes == 3){
//            if(OfNotifyRechargeStatus.SUCCESS.getCode().equals(code)){
//                rechargeOrder.setGmtRecharge(new Date());
//                rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.SUCCESS.getCode());//充值状态-充值成功
//                try {
//                    boolean updatestatus= rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                    logger.info("充值状态更新充值成功：updatestatus={}", updatestatus);
//                } catch (Exception e) {
//                    logger.error("更新充值状态失败:{}"+e);
//                }
//            }else if(OfNotifyRechargeStatus.FAILURE.getCode().equals(code)){//充值失败后
//                rechargeOrder.setGmtRecharge(new Date());
//                rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.FAILURE.getCode());//充值失败
//                boolean updatestatus=false;
//                try {
//                    updatestatus = rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                    logger.info("充值状态更新为充值失败:updatestatus={}",updatestatus);
//                } catch (Exception e) {
//                    logger.error("更新充值状态失败:{}"+e);
//                }
//            }else if(OfNotifyRechargeStatus.PROGRESS.getCode().equals(code)){
//                rechargeOrder.setGmtRecharge(new Date());
//                rechargeOrder.setRechargeStatus(RechargeOrderRechargeStatus.FAILURE.getCode());//40分钟后查询还是充值中就将充值状态设置为失败
//                try {
//                    boolean updatestatus = rechargeQueryServiceFacade.upRechargeOrder(rechargeOrder);//更新充值记录
//                    logger.info("充值状态更新处理中：updatestatus={}", updatestatus);
//                } catch (Exception e) {
//                    logger.error("更新充值状态失败:{}"+e);
//                }
//            }
//            timer.cancel();
//        }
//        tryTimes++;
//    } 
//    
//    /**
//     * 根据订单号查询欧飞充值状态
//     */
//    public String queryOrderInfo(String orderNo) {
//        String orderInfo = null;
//        try{
//            logger.info("查询欧飞充值状态商户订单号:{},{}",orderNo,ofRechargeServiceFacade);
//            orderInfo = ofRechargeServiceFacade.queryOrderInfo(orderNo);
//            logger.info("根据商户订单号查询充值详情:{}"+orderInfo);
//        }catch(Exception e){
//            logger.error("调用欧飞充值详情查询接口异常:{}"+e);
//        }
//        JSONObject jsonObject = JSONObject.parseObject(orderInfo);
//        String code = jsonObject.getString("gameState");//如果成功将为1，澈消(充值失败)为9，充值中为0,只能当状态为9时，商户才可以退款给用户。
//        if(code==null){
//            code="9";
//        }
//        String msg = jsonObject.getString("rtnMsg");
//        logger.info("查询订单:{} 状态:{} 信息:{}", orderNo, code, msg);
//        return code;
//    }
    
//}
