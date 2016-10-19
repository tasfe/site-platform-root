package com.netfinworks.site.web.action.trade;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.util.MoneyUtil;
import com.netfinworks.site.core.dal.dataobject.PosTradeDO;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.SummaryInfo;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.trade.TransferInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.web.util.ExcelParse;
import com.netfinworks.site.web.util.ExcelParse.ExcelVersion;

public class TradeExcelUtil extends AbstractExcelView {
	public void toExcel(HttpServletRequest request,
			HttpServletResponse response, List list,SummaryInfo sum,String string, int formState,
			String startDate, String endDate) {
		HttpSession session = request.getSession();
		session.setAttribute("state", null);
		// 生成提示信息，
		response.setContentType("application/vnd.ms-excel");
		OutputStream fOut = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			
			ExcelParse excel = ExcelParse.buildWrite(ExcelVersion.Excel_2003);
			
			// 产生工作簿对象
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet;
			HSSFRow sheetRow;
			HSSFCell cell;
			String codedFileName = "";
			String str = sdFormat.format(new Date());
			/**
			 * formState==1:支付流水 2：订单结算 3：退款流水
			 */
			switch (formState) {
			case 1:
			case 11:	
				String name="";
				if(formState==1){
					name="网关交易记录";
				}else if(formState==11){
					name="银行卡代扣";
				}
				codedFileName = name+"_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				excel.createSheet(name)
				.createRow().createCell("记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]", false)
				.createRow().createCell("创建时间", false).createCell("交易订单号", false)
				.createCell("商户订单号", false).createCell("交易类型", false)
				.createCell("金额(元)", false).createCell("服务费(元)", false).createCell("状态", false);
				List<BaseInfo> strList = list;
				for (BaseInfo bv : strList) {
					excel.createRow()
					.createCell(DateUtil.getNewFormatDateString(bv.getGmtSubmit()), false)
					.createCell(bv.getTradeVoucherNo(), false)
					.createCell(bv.getSerialNumber(), false);
					if(bv.getTradeType().equals("INSTANT_TRASFER")){
						excel.createCell("普通转账交易", false);
					}else if(bv.getTradeType().equals("INSTANT_ACQUIRING")){
						excel.createCell("即时到账收单交易", false);
					}else if(bv.getTradeType().equals("ENSURE_ACQUIRING")){
						excel.createCell("担保收单交易", false);
					}else if(bv.getTradeType().equals("PREPAY_ACQUIRING")){
						excel.createCell("下订收单交易", false);
					}else if(bv.getTradeType().equals("REFUND_ACQUIRING")){
						excel.createCell("收单退款交易", false);
					}else if(bv.getTradeType().equals("TRUST_COLLECT")){
						excel.createCell("银行卡代扣交易", false);
					}else{
						excel.createCell(bv.getTradeType(), false);
					}
					if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
						excel.createCell("0.00", false);
					} else {
						excel.createCell(MoneyUtil.getAmount(bv.getPayAmount()), false);
					}
					if(MoneyUtil.getAmount(bv.getPayeeFee()).equals("0.00")){
						excel.createCell(MoneyUtil.getAmount(bv.getPayeeFee()), false);
					}else {
						excel.createCell("-"+MoneyUtil.getAmount(bv.getPayeeFee()), false);
					}
					String tradeStatus = null;
					if (bv.getOrderState().equals("100")) {
						if(formState==1){
							tradeStatus = "待支付";
						}else if(formState==11){
							tradeStatus = "处理中";
						}
					} else if (bv.getOrderState().equals("110")
							|| bv.getOrderState().equals("111")
							|| bv.getOrderState().equals("121")) {
						tradeStatus = "处理中";
					} else if (bv.getOrderState().equals("201")) {
						tradeStatus = "支付成功";//付款成功=支付成功
					} else if (bv.getOrderState().equals("999")) {
							tradeStatus = "交易失败";//交易关闭视为交易失败
					}
					if (bv.getTradeType().equals("INSTANT_TRASFER")) {
						if (bv.getOrderState().equals("301")
								|| bv.getOrderState().equals("401")) {
							tradeStatus = "结算成功";//转账成功视为交易 成功
						} else if (bv.getOrderState().equals("998")) {
								tradeStatus = "交易失败";//转账失败视为交易失败
						}
					} else {
						if (bv.getOrderState().equals("301")) {
							tradeStatus = "交易成功";
						} else if (bv.getOrderState().equals("401")) {
							tradeStatus = "结算成功";
						}
					}
					excel.createCell(tradeStatus, false);
				}
				wb = (HSSFWorkbook) excel.getExcelObj();
				break;
			case 2:
				codedFileName = "充值_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				excel.createSheet("充值")
				.createRow().createCell("记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]", false)
				.createRow().createCell("创建时间", false).createCell("支付订单号", false)
				.createCell("交易类型", false).createCell("金额(元)", false)
				.createCell("支付账户", false).createCell("状态", false);
				List<DepositBasicInfo> depositInfo = list;
				for (DepositBasicInfo or : depositInfo) {
					if(!or.getPaymentStatus().equals("P")){
						excel.createRow()
						.createCell(DateUtil.getNewFormatDateString(or.getGmtPaySubmit()), false)
						.createCell(or.getPaymentVoucherNo(), false)
						.createCell("充值", false)
						.createCell(MoneyUtil.getAmount(or.getAmount()), false)
						.createCell( or.getBank(), false);
						if(or.getPaymentStatus().equals("F")){
							excel.createCell("失败", false);
						}else if(or.getPaymentStatus().equals("P")){
							excel.createCell("充值中", false);
						}else{
							excel.createCell("成功", false);
						}
					}
				}
				wb = (HSSFWorkbook) excel.getExcelObj();
				break;
			case 3:
				codedFileName = "提现_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				excel.createSheet("提现")
				.createRow().createCell("记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]", false)
				.createRow().createCell("创建时间", false).createCell("交易订单号", false)
				.createCell("交易类型", false).createCell("金额(元)", false)
				.createCell("服务费(元)", false).createCell("收款账户", false).createCell("状态", false);
				List<Fundout> fundout = list;//package com.netfinworks.site.domain.domain.fos;
				for (Fundout bv : fundout){
					excel.createRow()
					.createCell(DateUtil.getNewFormatDateString(bv.getOrderTime()), false)
					.createCell(bv.getFundoutOrderNo(), false)
					.createCell("提现", false)
					.createCell("-"+MoneyUtil.getAmount(bv.getAmount()), false);
					if(MoneyUtil.getAmount(bv.getFee()).equals("0.00")){
						excel.createCell(MoneyUtil.getAmount(bv.getFee()), false);
					}else {
						excel.createCell("-"+MoneyUtil.getAmount(bv.getFee()), false);
					}
					excel.createCell(bv.getBankName()+bv.getCardNo(), false);
					if (bv.getStatus().equals("submitted")) {
						excel.createCell("处理中", false);
					} else if (bv.getStatus().equals("failed")) {
						excel.createCell("失败", false);
					} else  {
						excel.createCell("出款成功", false);
					} 
				}
				wb = (HSSFWorkbook) excel.getExcelObj();
				break;
			case 4:
				codedFileName = "转账_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				excel.createSheet("转账")
				.createRow().createCell("记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]", false)
				.createRow().createCell("创建时间", false).createCell("支付时间", false)
				.createCell("商户批次号", false).createCell("商户订单号", false)
				.createCell("交易订单号", false).createCell("交易类型", false)
				.createCell("交易对象", false).createCell("收入金额(元)", false)
				.createCell("支出金额(元)", false).createCell("服务费(元)", false)
				.createCell("状态", false).createCell("备注", false);
				if(string.equals("2"))
				{
					List<Fundout> fundout4 = list;//package com.netfinworks.site.domain.domain.fos;
					for (Fundout bv : fundout4) {
						excel.createRow()
						.createCell(DateUtil.getNewFormatDateString(bv.getOrderTime()), false).createCell(DateUtil.getNewFormatDateString(bv.getSuccessTime()), false)
						.createCell(bv.getSourceBatchNo(), false).createCell(bv.getOutOrderNo(), false)
						.createCell(bv.getFundoutOrderNo(), false).createCell(TradeType.getByBizProductCode(bv.getProductCode())==TradeType.auto_fundout?"自动打款":"转账到银行卡", false)
						.createCell(bv.getName()+"\r\n"+bv.getBranchName()+bv.getCardNo(), false).createCell("", false)//收入金额（无收入金额）
						//支出金额--(转账到卡都是支出)
						.createCell(""+MoneyUtil.getAmount(bv.getAmount()), false).createCell(MoneyUtil.getAmount(bv.getFee()), false);//服务费
						if (bv.getStatus().equals("submitted")) {//状态
							excel.createCell("处理中", false);
						} else if (bv.getStatus().equals("failed")) {
							excel.createCell("失败", false);
						} else  {
							excel.createCell("出款成功", false);
						} 
						excel.createCell(bv.getPurpose(), false);
					}
				}else {
					List<TransferInfo> orderList = list;
					for (TransferInfo or : orderList) {
						excel.createRow()
						.createCell(DateUtil.getNewFormatDateString(or.getTransferTime()), false).createCell(DateUtil.getNewFormatDateString(or.getGmtPaid()), false)
						.createCell(or.getSourceBatchNo(), false).createCell(or.getTradeSourceVoucherNo(), false)
						.createCell(or.getOrderId(), false).createCell("转账到永达互联网金融", false);
						if (or.getPlamId().equals("1")){ //判断该商户是卖家
							excel.createCell(or.getBuyerName()+"\r\n"+or.getBuyId(), false);//交易对象(公司名+账户)
							excel.createCell(""+MoneyUtil.getAmount(or.getTransferAmount()), false);//收入金额（该商户为卖家，是收入金额）
							excel.createCell("", false);//支出金额--(无支出金额)
							excel.createCell(MoneyUtil.getAmount(or.getPayeeFee()), false);//服务费
						}else{
							excel.createCell(or.getSellerName()+"\r\n"+or.getSellId(), false);//交易对象(公司名+账户)
							excel.createCell("", false);//收入金额
							excel.createCell(""+MoneyUtil.getAmount(or.getTransferAmount()), false);//支出金额--(该商户为卖家，是支出金额)
							excel.createCell(MoneyUtil.getAmount(or.getPayerFee()), false);//服务费
						}
						if(or.getState().equals("待支付")){//状态
							excel.createCell("处理中", false);
						}else if (or.getState().equals("付款成功")) {
							excel.createCell("处理中", false);
						}else if (or.getState().equals("转账成功")) {
							excel.createCell("成功", false);
						}else if (or.getState().equals("交易完成")) {
							excel.createCell("成功", false);
						}else if (or.getState().equals("交易关闭") ||or.getState().equals("转账失败")) {
							excel.createCell("失败", false);
						}else {
							excel.createCell(or.getState(), false);
						}
						excel.createCell(or.getTradeMemo(), false);
					}
				}
				wb = (HSSFWorkbook) excel.getExcelObj();
				break;
			case 5:
				codedFileName = "退款_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				sheet = wb.createSheet("退款");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell(6);
				int title5Num = 0;
				int rows5=1;
				cell = getCell(sheet, rows5, title5Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "批次号(商户)");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "退款订单号");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "原交易订单号");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "商户订单号");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "原商户订单号");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "退款金额（元）");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "退还服务费（元）");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "状态");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "退款备注");
				List<TradeInfo> tradeInfo = list;
				int rowOrd5 = 2;
				for (TradeInfo or : tradeInfo) {
					sheetRow = sheet.createRow(rowOrd5);
					sheetRow.createCell((short) 6);
					int date2Num = 0;
					cell = getCell(sheet, rowOrd5, date2Num);
					setText(cell,DateUtil.getNewFormatDateString(or.getGmtSubmit()));
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getSourceBatchNo());
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getTradeVoucherNo());//退款
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getOrigTradeVoucherNo());//原交易
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getTradeSourceVoucherNo());//商户
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getOrigTradeSourceVoucherNo());//原商户
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, ""+MoneyUtil.getAmount(or.getPayAmount()));
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, ""+MoneyUtil.getAmount(or.getRefundPayerFee()));
					cell = getCell(sheet, rowOrd5, ++date2Num);
					if(or.getStatus().equals("100")){
						setText(cell, "待支付");
					}else if (or.getStatus().equals("100") || or.getStatus().equals("111") ||or.getStatus().equals("121")) {
						setText(cell, "支付中");
					}else if (or.getStatus().equals("201")) {
						setText(cell, "付款成功");
					}else if (or.getStatus().equals("301") ||or.getStatus().equals("401")) {
						setText(cell, "交易成功");
					}else if (or.getStatus().equals("900") ||or.getStatus().equals("901")) {
						setText(cell, "退款中");
					}else if (or.getStatus().equals("951")) {
						setText(cell, "退款成功");
					}else if (or.getStatus().equals("952")) {
						setText(cell, "退款失败");
					}else if (or.getStatus().equals("999")) {
						setText(cell, "交易关闭");
					}else {
						setText(cell, or.getStatus());
					}
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getTradeMemo());
					rowOrd5++;
				}
				break;
			case 6:
				codedFileName = "代发工资_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				sheet = wb.createSheet("代发工资");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell(6);
				// title行设置
				int title6Num = 0;
				int rows6=1;
				cell = getCell(sheet, rows6, title6Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "支付时间");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "商户订单号");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "交易对象");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "收入金额（元）");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "支出金额（元）");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "服务费（元）");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "状态");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "备注");
				
				if(string.equals("03"))//代发工资到银行卡
				{
						List<Fundout> fundout6 = list;//package com.netfinworks.site.domain.domain.fos;
						int row6 = 2;
						for (Fundout bv : fundout6) {
							sheetRow = sheet.createRow(row6);
							sheetRow.createCell((short) 6);
							// 数据行设置
							int dateNum = 0;// .getWebDateString(bv.getGmtSubmit())
							cell = getCell(sheet, row6, dateNum);
							setText(cell, DateUtil.getNewFormatDateString(bv.getOrderTime()));//创建时间
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, DateUtil.getNewFormatDateString(bv.getSuccessTime()));//支付时间--暂同创建时间
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, bv.getFundoutOrderNo());//交易订单号
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, "");//商户订单号
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, "代发工资到银行卡");//交易类型
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, bv.getName()+"\r\n"+bv.getBranchName()+bv.getCardNo());//交易对象(开户名+银行+卡号)
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, "");//收入金额（无收入金额）
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, ""+MoneyUtil.getAmount(bv.getAmount()));//支出金额--(转账到卡都是支出)
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell,MoneyUtil.getAmount(bv.getFee()));//服务费
							cell = getCell(sheet, row6, ++dateNum);
							if (bv.getStatus().equals("submitted")) {//状态
								setText(cell, "处理中");
							} else if (bv.getStatus().equals("failed")) {
								setText(cell, "失败");
							} else  {
								setText(cell, "成功");
							} 
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, "代发工资");//备注
							row6++;
					}
				}else {
					List<TransferInfo> orderList = list;
					int rowOrd6= 2;
					for (TransferInfo or : orderList) {
						sheetRow = sheet.createRow(rowOrd6);
						sheetRow.createCell((short) 6);
						int date6Num = 0;
						cell = getCell(sheet, rowOrd6, date6Num);
						setText(cell,DateUtil.getNewFormatDateString(or.getTransferTime()));//创建时间
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell,DateUtil.getNewFormatDateString(or.getGmtPaid()));//支付时间
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell, or.getOrderId());//交易订单号
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell, or.getTradeSourceVoucherNo());//商户订单号
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell, "代发工资到永达互联网金融");//交易类型
						if (or.getPlamId().equals("1")) //判断该商户是卖家
						{
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, or.getBuyerName()+"\r\n"+or.getBuyId());//交易对象(公司名+账户)--------------------
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, ""+MoneyUtil.getAmount(or.getTransferAmount()));//收入金额（该商户为卖家，是收入金额）
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, "");//支出金额--(无支出金额)
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, MoneyUtil.getAmount(or.getPayeeFee()));//服务费
						}
						else
						{
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, or.getSellerName()+"\r\n"+or.getSellId());//交易对象(公司名+账户)--------------------
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, "");//收入金额
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, ""+MoneyUtil.getAmount(or.getTransferAmount()));//支出金额--(该商户为卖家，是支出金额)
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, MoneyUtil.getAmount(or.getPayerFee()));//服务费
						}
						cell = getCell(sheet, rowOrd6, ++date6Num);
						if(or.getState().equals("待支付")){//状态
							setText(cell, "处理中");
						}
						else if (or.getState().equals("付款成功")) {
							setText(cell, "处理中");
						}else if (or.getState().equals("转账成功")) {
							setText(cell, "成功");
						}else if (or.getState().equals("交易完成")) {
							setText(cell, "成功");
						}else if (or.getState().equals("交易关闭") ||or.getState().equals("转账失败")) {
							setText(cell, "失败");
						}else {
							setText(cell, or.getState());
						}
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell, "代发工资");//备注
						rowOrd6++;
					}
				}
				break;
			case 7:
				if(string.equals("13")){
				codedFileName = "委托付款到银行卡_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				sheet = wb.createSheet("委托付款");
				}else{
					codedFileName = "委托付款到永达账户_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
					sheet = wb.createSheet("委托付款");	
				}
				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell(6);
				// title行设置
				int title7Num = 0;
				int rows7=1;
				cell = getCell(sheet, rows7, title7Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "支付时间");
				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, rows7, ++title7Num);
//				setText(cell, "商户订单号");
//				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "交易对象");
				cell = getCell(sheet, rows7, ++title7Num);
//				setText(cell, "收入金额（元）");
//				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "支出金额（元）");
				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "服务费（元）");
				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "状态");
				cell = getCell(sheet, rows7, ++title7Num);
				setText(cell, "备注");
				if(string.equals("13"))//资金归集到银行卡
				{
						List<Fundout> fundout6 = list;//package com.netfinworks.site.domain.domain.fos;
						int row6 = 2;
						for (Fundout bv : fundout6) {
							sheetRow = sheet.createRow(row6);
							sheetRow.createCell((short) 7);
							// 数据行设置
							int dateNum = 0;// 
							cell = getCell(sheet, row6, dateNum);
							setText(cell, DateUtil.getNewFormatDateString(bv.getOrderTime()));//创建时间
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, DateUtil.getNewFormatDateString(bv.getOrderTime()));//支付时间--暂同创建时间
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, bv.getFundoutOrderNo());//交易订单号
//							cell = getCell(sheet, row6, ++dateNum);
//							setText(cell, "");//商户订单号
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, "委托付款到银行卡");//交易类型
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, bv.getName()+"\r\n"+bv.getBranchName()+bv.getCardNo());//交易对象(开户名+银行+卡号)
//							cell = getCell(sheet, row6, ++dateNum);
//							setText(cell, "");//收入金额（无收入金额）
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell, ""+MoneyUtil.getAmount(bv.getAmount()));//支出金额--(转账到卡都是支出)
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell,MoneyUtil.getAmount(bv.getFee()));//服务费
							cell = getCell(sheet, row6, ++dateNum);
							if (bv.getStatus().equals("submitted")) {//状态
								setText(cell, "处理中");
							} else if (bv.getStatus().equals("failed")) {
								setText(cell, "失败");
							} else  {
								setText(cell, "成功");
							} 
							cell = getCell(sheet, row6, ++dateNum);
							setText(cell,bv.getPurpose());//备注
							row6++;
					}
				}else {
					List<TransferInfo> orderList = list;
					int rowOrd6= 2;
					for (TransferInfo or : orderList) {
						sheetRow = sheet.createRow(rowOrd6);
						sheetRow.createCell((short) 7);
						int date6Num = 0;
						cell = getCell(sheet, rowOrd6, date6Num);
						setText(cell,DateUtil.getNewFormatDateString(or.getTransferTime()));//创建时间
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell,DateUtil.getNewFormatDateString(or.getGmtPaid()));//支付时间
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell, or.getOrderId());//交易订单号
//						cell = getCell(sheet, rowOrd6, ++date6Num);
//						setText(cell, or.getTradeSourceVoucherNo());//商户订单号
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell, "委托付款到账户");//交易类型
						if (or.getPlamId().equals("1")) //判断该商户是卖家
						{
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, or.getBuyerName()+"\r\n"+or.getBuyId());//交易对象(公司名+账户)--------------------
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, ""+MoneyUtil.getAmount(or.getTransferAmount()));//收入金额（该商户为卖家，是收入金额）
//							cell = getCell(sheet, rowOrd6, ++date6Num);
//							setText(cell, "");//支出金额--(无支出金额)
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, MoneyUtil.getAmount(or.getPayeeFee()));//服务费
						}
						else
						{
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, or.getSellerName()+"\r\n"+or.getSellId());//交易对象(公司名+账户)--------------------
//							cell = getCell(sheet, rowOrd6, ++date6Num);
//							setText(cell, "");//收入金额
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, ""+MoneyUtil.getAmount(or.getTransferAmount()));//支出金额--(该商户为卖家，是支出金额)
							cell = getCell(sheet, rowOrd6, ++date6Num);
							setText(cell, MoneyUtil.getAmount(or.getPayerFee()));//服务费
						}
						cell = getCell(sheet, rowOrd6, ++date6Num);
						if(or.getState().equals("待支付")){//状态
							setText(cell, "处理中");
						}
						else if (or.getState().equals("付款成功")) {
							setText(cell, "处理中");
						}else if (or.getState().equals("转账成功")) {
							setText(cell, "出款成功");
						}else if (or.getState().equals("交易完成")) {
							setText(cell, "出款成功");
						}else if (or.getState().equals("交易关闭") ||or.getState().equals("转账失败")) {
							setText(cell, "失败");
						}else {
							setText(cell, or.getState());
						}
						cell = getCell(sheet, rowOrd6, ++date6Num);
						setText(cell, or.getTradeMemo());//备注
						rowOrd6++;
					}
				}
				break;
			case 8:
				codedFileName = "退票_" + endDate.replace("-", "").substring(0, 8) + str.substring(8, str.length());
				sheet = wb.createSheet("退票");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：[" + startDate + "]  记录终止日期：[" + endDate + "]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell(9);
				int title8Num = 0;
				int rows8 = 1;
				cell = getCell(sheet, rows8, title8Num);
				setText(cell, "退票时间");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "支付时间");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "商户订单号");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "交易对象");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "收入金额（元）");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "支出金额（元）");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "服务费（元）");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "状态");
				cell = getCell(sheet, rows8, ++title8Num);
				setText(cell, "备注");
				List<Fundout> fundout4 = list;// package com.netfinworks.site.domain.domain.fos;
				int row8 = 2;
				for (Fundout bv : fundout4) {
					sheetRow = sheet.createRow(row8);
					sheetRow.createCell((short) 12);
					// 数据行设置
					int dateNum = 0;//
					cell = getCell(sheet, row8, dateNum);
					setText(cell, DateUtil.getNewFormatDateString(bv.getRefundTime()));// 退票时间
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, DateUtil.getNewFormatDateString(bv.getSuccessTime()));// 支付时间--暂同审核
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, bv.getFundoutOrderNo());// 交易订单号
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, "");// 商户订单号
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, TradeType.getByBizProductCode(bv.getProductCode()).getMessage());// 交易类型
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, bv.getName() + "\r\n" + bv.getBranchName() + bv.getCardNo());// 交易对象(开户名+银行+卡号)
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, "");// 收入金额（无收入金额）
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, "" + MoneyUtil.getAmount(bv.getAmount()));// 支出金额--(转账到卡都是支出)
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, MoneyUtil.getAmount(bv.getFee()));// 服务费
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, "已退票");
					cell = getCell(sheet, row8, ++dateNum);
					setText(cell, bv.getPurpose());// 备注---------------
					row8++;
				}

				break;
			case 9:
			case 10:
				String tradeName="";
				if(formState==9){
					tradeName="保理放贷";				
				}else if(formState==10){
					tradeName="保理代扣";
				}
				codedFileName = tradeName + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
				sheet = wb.createSheet(tradeName);
				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell(9);
				int title9Num = 0;
				int rows9=1;
				cell = getCell(sheet, rows9, title9Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "支付时间");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "商户订单号");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "交易对象");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "收入金额（元）");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "支出金额（元）");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "服务费（元）");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "状态");
				cell = getCell(sheet, rows9, ++title9Num);
				setText(cell, "备注");
				List<TransferInfo> orderList = list;
				int rowOrd9 = 2;
				for (TransferInfo or : orderList) {
					sheetRow = sheet.createRow(rowOrd9);
					sheetRow.createCell((short) 9);
					int date2Num = 0;
					cell = getCell(sheet, rowOrd9, date2Num);
					setText(cell,DateUtil.getNewFormatDateString(or.getTransferTime()));//创建时间
					cell = getCell(sheet, rowOrd9, ++date2Num);
					setText(cell,DateUtil.getNewFormatDateString(or.getGmtPaid()));//支付时间
					cell = getCell(sheet, rowOrd9, ++date2Num);
					setText(cell, or.getOrderId());//交易订单号
					cell = getCell(sheet, rowOrd9, ++date2Num);
					setText(cell, or.getTradeSourceVoucherNo());//商户订单号
					cell = getCell(sheet, rowOrd9, ++date2Num);
					setText(cell, tradeName);//交易类型
					if (or.getPlamId().equals("1")) //判断该商户是卖家
					{
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, or.getBuyerName()+"\r\n"+or.getBuyId());//交易对象(公司名+账户)
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, ""+MoneyUtil.getAmount(or.getTransferAmount()));//收入金额（该商户为卖家，是收入金额）
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, "");//支出金额--(无支出金额)
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, MoneyUtil.getAmount(or.getPayeeFee()));//服务费
					}
					else
					{
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, or.getSellerName()+"\r\n"+or.getSellId());//交易对象(公司名+账户)
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, "");//收入金额
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, ""+MoneyUtil.getAmount(or.getTransferAmount()));//支出金额--(该商户为卖家，是支出金额)
						cell = getCell(sheet, rowOrd9, ++date2Num);
						setText(cell, MoneyUtil.getAmount(or.getPayerFee()));//服务费
					}
					cell = getCell(sheet, rowOrd9, ++date2Num);
					if(or.getState().equals("待支付")){//状态
						setText(cell, "处理中");
					}
					else if (or.getState().equals("付款成功")) {
						setText(cell, "处理中");
					}else if (or.getState().equals("转账成功")) {
						setText(cell, "成功");
					}else if (or.getState().equals("交易完成")) {
						setText(cell, "成功");
					}else if (or.getState().equals("交易关闭") ||or.getState().equals("转账失败")) {
						setText(cell, "失败");
					}else {
						setText(cell, or.getState());
					}
					cell = getCell(sheet, rowOrd9, ++date2Num);
					setText(cell, or.getTradeMemo());//备注
					rowOrd9++;
				}
				break;
			case 12:	
				if(string.equals("12")){
				    String posname="云+消费撤销交易记录";
					codedFileName = posname+"_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
					excel.createSheet(posname)
					.createRow().createCell("记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]", false)
					.createRow().createCell("创建时间", false)//.createCell("批次号(商户)", false)
					.createCell("撤销订单号", false).createCell("商户订单号", false)
					.createCell("原交易订单号", false).createCell("原商户订单号", false)
					.createCell("撤销金额（元）", false)//.createCell("退还服务费（元）", false)
					.createCell("状态", false);//.createCell("退款备注", false);
					List<TradeInfo> postradeInfo = list;
					for (TradeInfo pos : postradeInfo) {
						excel.createRow()
						.createCell(DateUtil.getNewFormatDateString(pos.getGmtSubmit()), false)//创建时间
					  //.createCell(pos.getSourceBatchNo(), false)
						.createCell(pos.getTradeVoucherNo(), false)//
						.createCell(pos.getTradeSourceVoucherNo(), false)
						.createCell(pos.getOrigTradeVoucherNo(), false)
						.createCell(pos.getOrigTradeSourceVoucherNo(), false)
						.createCell( ""+MoneyUtil.getAmount(pos.getPayAmount()), false);
					  //.createCell(""+MoneyUtil.getAmount(pos.getRefundPayerFee()), false);
						String tradeStatus = null;
						if(pos.getStatus().equals("900") ||pos.getStatus().equals("901")) {
							tradeStatus = "撤销中";
						}else if (pos.getStatus().equals("951")) {
							tradeStatus = "撤销成功";
						}else{
							tradeStatus = "撤销失败";
						}
						excel.createCell(tradeStatus, false);
				      //excel.createCell(pos.getTradeMemo(), false);
					}
					wb = (HSSFWorkbook) excel.getExcelObj();
					
				}else{
					String posname="云+消费交易记录";
					codedFileName = posname+"_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
					excel.createSheet(posname)
					.createRow().createCell("记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]", false)
					.createRow().createCell("创建时间", false).createCell("交易订单号", false)
					.createCell("商户订单号", false)//.createCell("交易类型", false)
					.createCell("金额(元)", false).createCell("服务费(元)", false)
					.createCell("支付方式", false)
					.createCell("备注", false).createCell("状态", false);
					List<BaseInfo> posstrList = list;
					for (BaseInfo bv : posstrList) {
						excel.createRow()
						.createCell(DateUtil.getNewFormatDateString(bv.getGmtSubmit()), false)
						.createCell(bv.getTradeVoucherNo(), false)
						.createCell(bv.getSerialNumber(), false);
//						if(bv.getTradeType().equals("ENSURE_ACQUIRING")){
//							excel.createCell("消费", false);
//						}else{
//							excel.createCell(bv.getTradeType(), false);
//						}
						if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
							excel.createCell("0.00", false);
						} else {
							excel.createCell(MoneyUtil.getAmount(bv.getPayAmount()), false);
						}
						if(MoneyUtil.getAmount(bv.getPayeeFee()).equals("0.00")){
							excel.createCell(MoneyUtil.getAmount(bv.getPayeeFee()), false);
						}else {
							excel.createCell("-"+MoneyUtil.getAmount(bv.getPayeeFee()), false);
						}
						excel.createCell(bv.getPayChannel()==null?"":bv.getPayChannel(), false).createCell(bv.getTradeMemo(), false);
						String tradeStatus = null;
						if (bv.getOrderState().equals("100")) {
							
								tradeStatus = "待支付";
							
						} else if (bv.getOrderState().equals("110")
								|| bv.getOrderState().equals("111")
								|| bv.getOrderState().equals("121")) {
							tradeStatus = "处理中";
						} else if (bv.getOrderState().equals("201")) {
							tradeStatus = "支付成功";//付款成功=支付成功
						} else if (bv.getOrderState().equals("999")) {
								tradeStatus = "交易失败";//交易关闭视为交易失败
						} else if (bv.getOrderState().equals("401")) {
								tradeStatus = "结算成功";
						}
						
						excel.createCell(tradeStatus, false);
						    
					}
					wb = (HSSFWorkbook) excel.getExcelObj();
					
					
				}
				break;
			case 13:	
				    String posname="POS交易记录";
					codedFileName = posname+"_" + endDate.replace("-", "").substring(0,8)+ str.substring(8, str.length());
					excel.createSheet(posname)
					.createRow().createCell("记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]", false)
					.createRow().createCell("成功交易总金额", false).createCell(sum.getTotalTradeAmount().toString(), false)
					.createCell("失败交易总金额", false).createCell(sum.getTotalRefundAmount().toString(), false)
					.createCell("手续费总金额", false).createCell(sum.getTotalFeeAmount().toString(), false)
					.createCell("结算总金额", false).createCell(sum.getTotalSettledAmount().toString(), false)
					.createRow().createCell("交易单号", false)
					.createCell("商户号", false).createCell("门店名称", false)
					.createCell("终端号", false).createCell("交易时间", false)
					.createCell("交易类型", false).createCell("交易账号", false)
					.createCell("卡类型", false).createCell("所属银行", false)
					.createCell("交易金额", false).createCell("手续费", false)
					.createCell("结算金额", false).createCell("处理状态", false)
					.createCell("原订单号", false).createCell("原因", false)
					.createCell("备注", false);
					List<PosTradeDO> posInfo = list;
					for (PosTradeDO pos : posInfo) {
						excel.createRow()
						.createCell(pos.getTradeNo(), false)
					    .createCell(pos.getMerchantNo(), false)
					    .createCell(pos.getMerchantName(), false)
					    .createCell(pos.getClientNo(), false)
					    .createCell(DateUtil.getNewFormatDateString(pos.getTradeTime()), false)
					    .createCell(pos.getTradeType(), false)
					    .createCell(pos.getBankNo(), false)
					    .createCell(pos.getCardType(), false)
					    .createCell(pos.getBank(), false)
					    .createCell(MoneyUtil.getAmount(pos.getTradeAmount()), false)
					    .createCell(MoneyUtil.getAmount(pos.getTradeFee()), false)
					    .createCell(MoneyUtil.getAmount(pos.getTradeSum()), false)
					    .createCell(pos.getStatus(), false)
					    .createCell(pos.getTradeSrcNo(), false)
					    .createCell(pos.getReason(), false)
					    .createCell(pos.getRemark(), false)
					    ;
					}
					wb = (HSSFWorkbook) excel.getExcelObj();
				break;
			default:
				System.out.println("error");
				break;

			}
			if(StringUtils.isNotBlank(codedFileName)){
				//中文转码
				if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,"utf-8");
				}
				response.setHeader("content-disposition","attachment;filename=" + codedFileName + ".xls");
			}
			fOut = response.getOutputStream();
			wb.write(fOut);
		}catch (Exception e) {
			logger.error("导出Excel异常", e);
		} finally {
			try {
				fOut.flush();
				fOut.close();
			} catch (IOException e) {
			}
			session.setAttribute("state", "open");
		}
		System.out.println("文件生成...");

	}

	@Override
	protected void buildExcelDocument(Map<String, Object> model,
			HSSFWorkbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
	}
}
