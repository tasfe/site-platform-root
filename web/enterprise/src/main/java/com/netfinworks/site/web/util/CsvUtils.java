package com.netfinworks.site.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.domain.domain.trade.OrderSettleInfo;
import com.netfinworks.site.domain.domain.trade.PayWaterInfo;
import com.netfinworks.site.domain.domain.trade.RefundWaterInfo;

public class CsvUtils {
    public static void createCSVFile(HttpServletRequest request, HttpServletResponse response,
                                     List list, int formState,String startDate,String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        OutputStream o = null;
        String exportString = "";
        String codedFileName="";
        String str = sdf.format(new Date());
        try {
            /**
             * formState==1:支付流水
             * 2：订单结算
             * 3：退款流水
             */
            switch (formState) {
                case 1:
                    if(startDate.equals(endDate)){
                        codedFileName = "ZF" + startDate.replace("-", "")
                                              + str.substring(8, str.length());
                       }else{
                           codedFileName = "ZF" + startDate.replace("-", "")+"-"
                                   + endDate.replace("-", "")+str.substring(8, str.length());
                       }
                       if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                           codedFileName = new String(codedFileName.getBytes("UTF-8"), "ISO8859-1");
                       } else {
                           codedFileName = java.net.URLEncoder.encode(codedFileName, "utf-8");
                       }
                       //                      String codedFileName = java.net.URLEncoder.encode("支付流水", "utf-8");
                       response.setContentType("application/download;charset=GBK");
                       response
                           .setContentType("Content-type:application/vnd.ms-excel;charset=GBK");
                       response.setHeader("Content-disposition", "attachment;filename=\""
                                                                 + codedFileName + ".csv\"");
                    List<PayWaterInfo> strList = (List<PayWaterInfo>) list;
                    //exportString = "钱包交易订单号,商户订单ID,订单编号,付款方账号,收款方账号,商品名称,订金金额,付款金币,付款金额,交易提交时间,交易时间,支付状态,交易类型";
                    exportString = "钱包交易订单号,商户订单ID,订单编号,付款方账号,收款方账号,商品名称,付款金额,交易提交时间,交易时间,支付状态,交易类型";
                    exportString += "\r\n";
                    if (strList!=null && strList.size() > 0) {
                        for (int i = 0; i < strList.size(); i++) {
                            PayWaterInfo payWater = strList.get(i);
                            exportString += payWater.getWalletTradeId() + ",";
                            exportString += payWater.getMerchantOrderId() + ",";
                            exportString += payWater.getOrderCode() + ",";
                            exportString += payWater.getPayerAccount() + ",";
                            exportString += payWater.getPayeeAccount() + ",";
                            exportString += payWater.getCommodityName() + ",";
                            //exportString += payWater.getDepositAmount() + ",";
                            //exportString += payWater.getPaymentGold() + ",";
                            exportString += payWater.getPaymentAmount() + ",";
                            exportString += DateUtil.getNewFormatDateString(payWater.getTradeSubmitTime()) + ",";
                            exportString += DateUtil.getNewFormatDateString(payWater.getTradeTime()) + ",";
                            exportString += payWater.getPaymentState() + ",";
                            if ("11".equals(payWater.getTradeType())) {
                                exportString += "即时到账收单交易";
                            } else if ("12".equals(payWater.getTradeType())) {
                                exportString += "担保收单交易";
                            } else if ("13".equals(payWater.getTradeType())) {
                                exportString += "下订收单交易";
                            } else {
                                exportString += "收单退款交易";
                            }
                            exportString += "\r\n";
                        }
                    }
                    break;
                case 2:
                    if(startDate.equals(endDate)){
                        codedFileName = "DDJS" + startDate.replace("-", "")
                                              + str.substring(8, str.length());
                       }else{
                           codedFileName = "DDJS" + startDate.replace("-", "")+"-"
                                   + endDate.replace("-", "")+str.substring(8, str.length());
                       }
                    if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                        codedFileName = new String(codedFileName.getBytes("UTF-8"), "ISO8859-1");
                    } else {
                        codedFileName = java.net.URLEncoder.encode(codedFileName, "utf-8");
                    }
                    response.setContentType("application/download;charset=GBK");
                    response
                        .setContentType("Content-type:application/vnd.ms-excel;charset=GBK");
                    response.setHeader("Content-disposition", "attachment;filename=\""
                                                              + codedFileName + ".csv\"");
                    List<OrderSettleInfo> orderList = (List<OrderSettleInfo>) list;
                    //exportString = "钱包交易订单号,订单编号,订金金额,付款金币,付款金额,实际成交金额,佣金款,金币款,交易结束时间";
                    exportString = "钱包交易订单号,订单编号,付款金额,实际成交金额,交易结束时间";
                    exportString += "\r\n";
                    if (orderList!=null &&orderList.size() > 0) {
                        for (int i = 0; i < orderList.size(); i++) {
                            OrderSettleInfo ord = orderList.get(i);
                            exportString += ord.getWalletTradeId() + ",";
                            exportString += ord.getMerchantOrderId() + ",";
                            //exportString += ord.getDepositAmount() + ",";
                            //exportString += ord.getPaymentGold() + ",";
                            exportString += ord.getPaymentAmount() + ",";
                            exportString += ord.getSettledAmount() + ",";
                            //exportString += ord.getCommission()==null?"0.00,":ord.getCommission() + ",";
                            //exportString += ord.getGoldAmount()==null?"0.00,":ord.getGoldAmount() + ",";
                            exportString += DateUtil.getNewFormatDateString(ord.getTradeOverTime());
                            exportString += "\r\n";
                        }
                    }
                    break;
                case 3:
                    if(startDate.equals(endDate)){
                        codedFileName = "TK" + startDate.replace("-", "")
                                              + str.substring(8, str.length());
                       }else{
                           codedFileName = "TK" + startDate.replace("-", "")+"-"
                                   + endDate.replace("-", "")+str.substring(8, str.length());
                       }
                    if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                        codedFileName = new String(codedFileName.getBytes("UTF-8"), "ISO8859-1");
                    } else {
                        codedFileName = java.net.URLEncoder.encode(codedFileName, "utf-8");
                    }
                    response.setContentType("application/download;charset=GBK");
                    response
                        .setContentType("Content-type:application/vnd.ms-excel;charset=GBK");
                    response.setHeader("Content-disposition", "attachment;filename=\""
                                                              + codedFileName + ".csv\"");
                    List<RefundWaterInfo> refList = (List<RefundWaterInfo>) list;
                    //exportString = "钱包交易订单号,商户退单ID,商户订单ID,商品名称,退款金额,退担保金额,退金币金额,退订金金额,退款提交时间,退款时间,退款状态";
                    exportString = "钱包交易订单号,商户退单ID,商户订单ID,商品名称,退款金额,退担保金额,退款提交时间,退款时间,退款状态";
                    exportString += "\r\n";
                    if (refList!=null && refList.size() > 0) {
                        for (int i = 0; i < refList.size(); i++) {
                            RefundWaterInfo ref = refList.get(i);
                            exportString += ref.getWalletTradeId() + ",";
                            exportString += ref.getMerchantRefundOrderId() + ",";
                            exportString += ref.getMerchantOrderId() + ",";
                            exportString += ref.getCommodityName() + ",";
                            exportString += ref.getRefundAmount() + ",";
                            exportString += ref.getRefundGuarantAmount() + ",";
                            //exportString += ref.getRefundGoldAmount() + ",";
                            //exportString += ref.getRefundDepositAmount() + ",";
                            exportString += DateUtil.getNewFormatDateString(ref.getRefundSubmitTime()) + ",";
                            exportString += DateUtil.getNewFormatDateString(ref.getRefundTime()) + ",";
                            exportString += ref.getRefundState();
                            exportString += "\r\n";
                        }
                    }
                    break;
                default:
                    System.out.println("error");
                    break;

            }
            o = response.getOutputStream();
            o.write(exportString.toString().getBytes("GBK"));

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            if (o != null) {
                try {
                    o.flush();
                    o.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
