package com.yongda.site.app.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.util.MoneyUtil;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.info.WalletCheckInfo;
import com.netfinworks.site.domain.domain.trade.TransferInfo;

public class ExcelUtil extends AbstractExcelView {
	public void toExcel(HttpServletRequest request,
			HttpServletResponse response, List list, int formState,
			String startDate, String endDate) {
		HttpSession session = request.getSession();
		session.setAttribute("state", null);
		// 生成提示信息，
		response.setContentType("application/vnd.ms-excel");
		OutputStream fOut = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			// 产生工作簿对象
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet;
			HSSFRow sheetRow;
			HSSFCell cell;
			String codedFileName = "";
			String str = sdFormat.format(new Date());
			switch (formState) {
			case 1:
				if (startDate.equals(endDate)) {
					codedFileName = "收支明细_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "收支明细_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("收支明细");

				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期:[" + startDate + "]    记录终止日期:["
						+ endDate + "]");
				int title4Num = 0;
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 4);
				cell = getCell(sheet, 1, title4Num);
				setText(cell, "资金变动时间");
				cell = getCell(sheet, 1, ++title4Num);
				setText(cell, "收入(元)");
				cell = getCell(sheet, 1, ++title4Num);
				setText(cell, "支出(元)");
				cell = getCell(sheet, 1, ++title4Num);
				setText(cell, "账户余额(元)");

				List<WalletCheckInfo> transList = (List<WalletCheckInfo>) list;
				int rowRef1 = 2;
				BigDecimal income=new BigDecimal(Double.toString(0));
				BigDecimal outpay=new BigDecimal(Double.toString(0));
				for (WalletCheckInfo tran : transList) {
					sheetRow = sheet.createRow(rowRef1);
					sheetRow.createCell((int) 4);
					int date3Num = 0;
					cell = getCell(sheet, rowRef1, date3Num);
					setText(cell, sdf.format((Date) tran.getTxnTime()));
					
					if (tran.getTxnType().toString().equals("INCOME")) {
						cell = getCell(sheet, rowRef1, ++date3Num);
						setText(cell,
								tran.getTxnAmt().toString() != null ? tran
										.getTxnAmt().toString() : "");
						income = (tran.getTxnAmt().getAmount()).add(income);
					} else {
						cell = getCell(sheet, rowRef1, ++date3Num);
						setText(cell, "");
					}

					if (tran.getTxnType().toString().equals("PAYOUT")) {
						cell = getCell(sheet, rowRef1, ++date3Num);
						setText(cell, tran.getTxnAmt().toString() != null ? "-"
								+ tran.getTxnAmt().toString() : "");
						outpay = (tran.getTxnAmt().getAmount()).add(outpay);
					} else {
						cell = getCell(sheet, rowRef1, ++date3Num);
						setText(cell, "");
					}

					cell = getCell(sheet, rowRef1, ++date3Num);
					setText(cell, tran.getAfterAmt().toString() != null ? tran
							.getAfterAmt().toString() : "");

					rowRef1++;
				}
				DecimalFormat df = new DecimalFormat("##0.00");
				cell = getCell(sheet, rowRef1 + 1, 0);
				setText(cell, "汇总");

				cell = getCell(sheet, rowRef1 + 1, 1);
				setText(cell, df.format(income));

				cell = getCell(sheet, rowRef1 + 1, 2);
				setText(cell, "-" + df.format(outpay));
				break;
			case 2:
				if (startDate.equals(endDate)) {
					codedFileName = "充值记录_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "充值记录_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("充值记录");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 9);
				int title2Num = 0;
				int row2=1;
				cell = getCell(sheet, row2, title2Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, row2, ++title2Num);
				setText(cell, "交易订单号");
//				cell = getCell(sheet, row2, ++title2Num);
//				setText(cell, "交易类型");
				cell = getCell(sheet, row2, ++title2Num);
				setText(cell, "金额（元）");
				cell = getCell(sheet, row2, ++title2Num);
				setText(cell, "支付账户");
//				cell = getCell(sheet, row2, ++title2Num);
//				setText(cell, "操作员");
				cell = getCell(sheet, row2, ++title2Num);
				setText(cell, "状态");
				List<DepositBasicInfo> depositInfo = (List<DepositBasicInfo>) list;
				int rowOrd = 2;
				for (DepositBasicInfo or : depositInfo) {
					if(!or.getPaymentStatus().equals("P"))
					{
					sheetRow = sheet.createRow(rowOrd);
					sheetRow.createCell((short) 9);
					int date2Num = 0;
					cell = getCell(sheet, rowOrd, date2Num);
					setText(cell,
							DateUtil.getNewFormatDateString(or.getGmtPaySubmit()));
					cell = getCell(sheet, rowOrd, ++date2Num);
					setText(cell, or.getPaymentVoucherNo());
//					cell = getCell(sheet, rowOrd, ++date2Num);
//					setText(cell, "充值");
					cell = getCell(sheet, rowOrd, ++date2Num);
					setText(cell, MoneyUtil.getAmount(or.getAmount()));
					cell = getCell(sheet, rowOrd, ++date2Num);
					setText(cell, or.getBank());
//					cell = getCell(sheet, rowOrd, ++date2Num);
//					setText(cell, or.getMemberId());
					cell = getCell(sheet, rowOrd, ++date2Num);
					if(or.getPaymentStatus().equals("F"))
					{
						setText(cell, "失败");
					}
					else if(or.getPaymentStatus().equals("P"))
					{
						setText(cell, "充值中");
					}
					else
					{
						setText(cell, "成功");
					}
					rowOrd++;
					}
				}
				break;
			case 3:
				if (startDate.equals(endDate)) {
					codedFileName = "提现记录_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "提现记录_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("提现记录");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 8);
				// title行设置
				int title3Num = 0;
				int rows3=1;
				cell = getCell(sheet, rows3, title3Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, rows3, ++title3Num);
				setText(cell, "交易订单号");
//				cell = getCell(sheet, rows3, ++title3Num);
//				setText(cell, "交易类型");
				cell = getCell(sheet, rows3, ++title3Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, rows3, ++title3Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, rows3, ++title3Num);
				setText(cell, "收款账户");
//				cell = getCell(sheet, rows3, ++title3Num);
//				setText(cell, "操作员");
				cell = getCell(sheet, rows3, ++title3Num);
				setText(cell, "状态");
				List<Fundout> fundout = (List<Fundout>) list;//package com.netfinworks.site.domain.domain.fos;
				int row3 = 2;
				for (Fundout bv : fundout) {
					sheetRow = sheet.createRow(row3);
					sheetRow.createCell((short) 12);
					// 数据行设置
					int dateNum = 0;// .getNewFormatDateString(bv.getGmtSubmit())
					cell = getCell(sheet, row3, dateNum);
					setText(cell, DateUtil.getNewFormatDateString(bv.getOrderTime()));
					cell = getCell(sheet, row3, ++dateNum);
					setText(cell, bv.getFundoutOrderNo());
//					cell = getCell(sheet, row3, ++dateNum);
//					setText(cell, "提现");
					cell = getCell(sheet, row3, ++dateNum);
					setText(cell, "-"+MoneyUtil.getAmount(bv.getAmount()));
					cell = getCell(sheet, row3, ++dateNum);
					setText(cell,MoneyUtil.getAmount(bv.getFee()));
					cell = getCell(sheet, row3, ++dateNum);
					setText(cell, bv.getBankName()+bv.getCardNo());
//					cell = getCell(sheet, row3, ++dateNum);
//					setText(cell, bv.getMemberId());
					cell = getCell(sheet, row3, ++dateNum);
					if (bv.getStatus().equals("submitted")) {
						setText(cell, "处理中");
					} else if (bv.getStatus().equals("failed")) {
						setText(cell, "失败");
					} else  {
						setText(cell, "出款成功");
					} 
					row3++;
				}
				break;
			
			default:
				System.out.println("error");
				break;
			}

			fOut = response.getOutputStream();
			wb.write(fOut);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fOut.flush();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			session.setAttribute("state", "open");
		}
		System.out.println("文件生成...");
	}

	@Override
	protected void buildExcelDocument(Map<String, Object> model,
			HSSFWorkbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

	}
	
	public void toExcel1(HttpServletRequest request,
			HttpServletResponse response, List list, String memberId, int formState,
			String startDate, String endDate) {
		HttpSession session = request.getSession();
		session.setAttribute("state", null);
		// 生成提示信息，
		response.setContentType("application/vnd.ms-excel");
		OutputStream fOut = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			// 产生工作簿对象
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet;
			HSSFRow sheetRow;
			HSSFCell cell;
			String codedFileName = "";
			String str = sdFormat.format(new Date());
			switch (formState) {
			case 0:
				if (startDate.equals(endDate)) {
					codedFileName = "交易记录_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "交易记录_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("交易记录");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 10);
				// title行设置
				int titleNum = 0;
				int row=1;
				cell = getCell(sheet, row, titleNum);
				setText(cell, "创建时间");
				cell = getCell(sheet, row, ++titleNum);
				setText(cell, "商户订单号");
				cell = getCell(sheet, row, ++titleNum);
				setText(cell, "交易订单号");
				cell = getCell(sheet, row, ++titleNum);
				setText(cell, "交易类型");
				cell = getCell(sheet, row, ++titleNum);
				setText(cell, "金额(元)");
				cell = getCell(sheet, row, ++titleNum);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, row, ++titleNum);
//				setText(cell, "支付类型");
//				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "状态");
				List<TradeInfo> strList = (List<TradeInfo>) list;
				row = 2;
				for (TradeInfo bv : strList) {
					sheetRow = sheet.createRow(row);
					sheetRow.createCell((short) 12);
					// 数据行设置
					int dateNum = 0;// .getWebDateString(bv.getGmtSubmit())
//					String plusminus = null;
//					if (bv.getMemberId().equals(bv.getBuyerId())) {
//						plusminus = "-";
//					} else if (bv.getMemberId().equals(bv.getSellerId())) {
//						plusminus = "";
//					}
					cell = getCell(sheet, row, dateNum);
					setText(cell, DateUtil.getNewFormatDateString(bv.getGmtSubmit()));
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getTradeSourceVoucherNo());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getTradeVoucherNo());
					
					cell = getCell(sheet, row, ++dateNum);
					if(bv.getTradeType().toString().equals("INSTANT_TRASFER")){
						setText(cell, "普通转账交易");
					}else if(bv.getTradeType().toString().equals("INSTANT_ACQUIRING")){
						setText(cell, "即时到账收单交易");
					}else if(bv.getTradeType().toString().equals("ENSURE_ACQUIRING")){
						setText(cell, "担保收单交易");
					}else if(bv.getTradeType().toString().equals("PREPAY_ACQUIRING")){
						setText(cell, "下订收单交易");
					}else if(bv.getTradeType().toString().equals("REFUND_ACQUIRING")){
						setText(cell, "收单退款交易");
					}else{
						setText(cell, bv.getTradeType().toString());
					}
					cell = getCell(sheet, row, ++dateNum);
					if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
						setText(cell, "--");
					} else {
						if(bv.getBuyerId().equals(memberId)){
							setText(cell,"-"+MoneyUtil.getAmount(bv.getPayAmount()));
						}else{
							setText(cell,MoneyUtil.getAmount(bv.getPayAmount()));
						}
					}
//					cell = getCell(sheet, row, ++dateNum);
//					setText(cell, MoneyUtil.getAmount(bv.getCoinAmount()));
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, MoneyUtil.getAmount(bv.getPayeeFee()));
					cell = getCell(sheet, row, ++dateNum);
					String tradeStatus = null;
					if (bv.getStatus().equals("100")) {
						tradeStatus = "待支付";
					} else if (bv.getStatus().equals("110")
							|| bv.getStatus().equals("111")
							|| bv.getStatus().equals("121")) {
						tradeStatus = "处理中";
					} else if (bv.getStatus().equals("201")) {
						tradeStatus = "支付成功";//付款成功=支付成功
					} else if (bv.getStatus().equals("999")) {
						tradeStatus = "交易失败";//交易关闭视为交易失败
					}
					if (bv.getTradeType().toString().equals("INSTANT_TRASFER")) {
						if (bv.getStatus().equals("301")
								|| bv.getStatus().equals("401")) {
							tradeStatus = "交易成功";//转账成功视为交易 成功
						} else if (bv.getStatus().equals("998")) {
							tradeStatus = "交易失败";//转账失败视为交易失败
						}
					} else {
						if (bv.getStatus().equals("301")) {
							tradeStatus = "交易成功";
						} else if (bv.getStatus().equals("401")) {
							tradeStatus = "交易结束";
						}
					}
					setText(cell, tradeStatus);
					row++;
				}
				break;
			case 1:
				if (startDate.equals(endDate)) {
					codedFileName = "购物消费记录_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "购物消费记录_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("购物消费记录");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 10);
				// title行设置
				int title1Num = 0;
				int row1=1;
				cell = getCell(sheet, row1, title1Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "商户订单号");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, row1, ++title1Num);
//				setText(cell, "支付类型");
//				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "状态");
				strList = (List<TradeInfo>) list;
				row = 2;
				for (TradeInfo bv : strList) {
					sheetRow = sheet.createRow(row);
					sheetRow.createCell((short) 12);
					// 数据行设置
					int dateNum = 0;// .getWebDateString(bv.getGmtSubmit())
//					String plusminus = null;
//					if (bv.getMemberId().equals(bv.getBuyerId())) {
//						plusminus = "-";
//					} else if (bv.getMemberId().equals(bv.getSellerId())) {
//						plusminus = "";
//					}
					cell = getCell(sheet, row, dateNum);
					setText(cell, DateUtil.getNewFormatDateString(bv.getGmtSubmit()));
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getTradeVoucherNo());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getTradeSourceVoucherNo());
					
					cell = getCell(sheet, row, ++dateNum);
					if(bv.getTradeType().toString().equals("INSTANT_TRASFER")){
						setText(cell, "普通转账交易");
					}else if(bv.getTradeType().toString().equals("INSTANT_ACQUIRING")){
						setText(cell, "即时到账收单交易");
					}else if(bv.getTradeType().toString().equals("ENSURE_ACQUIRING")){
						setText(cell, "担保收单交易");
					}else if(bv.getTradeType().toString().equals("PREPAY_ACQUIRING")){
						setText(cell, "下订收单交易");
					}else if(bv.getTradeType().toString().equals("REFUND_ACQUIRING")){
						setText(cell, "收单退款交易");
					}else{
						setText(cell, bv.getTradeType().toString());
					}
					cell = getCell(sheet, row, ++dateNum);
					if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
						setText(cell, "--");
					} else {
						if(bv.getBuyerId().equals(memberId)){
							setText(cell,"-"+MoneyUtil.getAmount(bv.getPayAmount()));
						}else{
							setText(cell,MoneyUtil.getAmount(bv.getPayAmount()));
						}
					}
//					cell = getCell(sheet, row, ++dateNum);
//					setText(cell, MoneyUtil.getAmount(bv.getCoinAmount()));
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, MoneyUtil.getAmount(bv.getPayeeFee()));
					cell = getCell(sheet, row, ++dateNum);
					String tradeStatus = null;
					if (bv.getStatus().equals("100")) {
						tradeStatus = "待支付";
					} else if (bv.getStatus().equals("110")
							|| bv.getStatus().equals("111")
							|| bv.getStatus().equals("121")) {
						tradeStatus = "处理中";
					} else if (bv.getStatus().equals("201")) {
						tradeStatus = "支付成功";//付款成功=支付成功
					} else if (bv.getStatus().equals("999")) {
						tradeStatus = "交易失败";//交易关闭视为交易失败
					}
					if (bv.getTradeType().equals("INSTANT_TRASFER")) {
						if (bv.getStatus().equals("301")
								|| bv.getStatus().equals("401")) {
							tradeStatus = "交易成功";//转账成功视为交易 成功
						} else if (bv.getStatus().equals("998")) {
							tradeStatus = "交易失败";//转账失败视为交易失败
						}
					} else {
						if (bv.getStatus().equals("301")) {
							tradeStatus = "交易成功";
						} else if (bv.getStatus().equals("401")) {
							tradeStatus = "交易成功";
						}
					}
					setText(cell, tradeStatus);
					row++;
				}
				break;
			case 2:
                if (startDate.equals(endDate)) {
                    codedFileName = "转账到账户记录_" + startDate.replace("-", "")
                            + str.substring(8, str.length());
                } else {
                    codedFileName = "转账到账户记录_"
                            + endDate.substring(0, 8).replace("-", "")
                            + str.substring(8, str.length());
                }
                if (request.getHeader("User-Agent").toLowerCase()
                        .indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"),
                            "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName,
                            "utf-8");
                }
                response.setHeader("content-disposition",
                        "attachment;filename=" + codedFileName + ".xls");
                sheet = wb.createSheet("交易记录");
                sheetRow = sheet.createRow(0);
                sheetRow.createCell((int) 1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 10);
                // title行设置
                int titleNum2 = 0;
                int row2=1;
                cell = getCell(sheet, row2, titleNum2);
                setText(cell, "创建时间");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "商户订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易类型");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "金额(元)");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, row2, ++titleNum2);
//              setText(cell, "支付类型");
//              cell = getCell(sheet, 0, ++title1Num);
                setText(cell, "状态");
                List<TradeInfo> strList2 = (List<TradeInfo>) list;
                row2 = 2;
                for (TradeInfo bv : strList2) {
                    sheetRow = sheet.createRow(row2);
                    sheetRow.createCell((short) 12);
                    // 数据行设置
                    int dateNum = 0;// .getWebDateString(bv.getGmtSubmit())
//                  String plusminus = null;
//                  if (bv.getMemberId().equals(bv.getBuyerId())) {
//                      plusminus = "-";
//                  } else if (bv.getMemberId().equals(bv.getSellerId())) {
//                      plusminus = "";
//                  }
                    cell = getCell(sheet, row2, dateNum);
                    setText(cell, DateUtil.getNewFormatDateString(bv.getGmtSubmit()));
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeSourceVoucherNo());
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeVoucherNo());
                    
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, "转账到账户");
                    cell = getCell(sheet, row2, ++dateNum);
                    if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
                        setText(cell, "--");
                    } else {
                        if(bv.getBuyerId().equals(memberId)){
                            setText(cell,"-"+MoneyUtil.getAmount(bv.getPayAmount()));
                        }else{
                            setText(cell,MoneyUtil.getAmount(bv.getPayAmount()));
                        }
                    }
//                  cell = getCell(sheet, row, ++dateNum);
//                  setText(cell, MoneyUtil.getAmount(bv.getCoinAmount()));
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, MoneyUtil.getAmount(bv.getPayeeFee()));
                    cell = getCell(sheet, row2, ++dateNum);
                    String tradeStatus = null;
                    if (bv.getStatus().equals("100")) {
                        tradeStatus = "待支付";
                    }else if (bv.getStatus().equals("401")) {
                        tradeStatus = "成功";//转账成功视为交易 成功
                    } else if (bv.getStatus().equals("998")) {
                        tradeStatus = "失败";//转账失败视为交易失败
                    }
                    
                    setText(cell, tradeStatus);
                    row2++;
                }
                break;
			case 3:
				if (startDate.equals(endDate)) {
					codedFileName = "转账到银行_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "转账到银行_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("转账到银行");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 10);
				// title行设置
				title1Num = 0;
				row1=1;
				cell = getCell(sheet, row1, title1Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "商户订单号");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, row1, ++title1Num);
				setText(cell, "状态");
				List<Fundout> fundList = (List<Fundout>) list;
				row = 2;
				for (Fundout bv : fundList) {
					sheetRow = sheet.createRow(row);
					sheetRow.createCell((short) 12);
					int dateNum = 0;
					cell = getCell(sheet, row, dateNum);
					setText(cell, DateUtil.getNewFormatDateString(bv.getOrderTime()));
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getFundoutOrderNo());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getPaymentOrderNo());
					
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, "转账到银行卡");
					cell = getCell(sheet, row, ++dateNum);
					if (MoneyUtil.getAmount(bv.getAmount()).equals("0.00")) {
						setText(cell, "--");
					} else {
						setText(cell,"-"+MoneyUtil.getAmount(bv.getAmount()));
					}
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, MoneyUtil.getAmount(bv.getFee()));
					cell = getCell(sheet, row, ++dateNum);
					String status = null;
					if(bv.getStatus().equals("submitted")){
						status="处理中";
					}else if(bv.getStatus().equals("failed")){
						status="失败";
					}else{
						status="出款成功";
					}
					setText(cell, status);
					row++;
				}
				break;
			case 4:
				if (startDate.equals(endDate)) {
					codedFileName = "代发工资_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "代发工资_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("代发工资");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 6);
				// title行设置
				int title6Num = 0;
				int rows6=1;
				cell = getCell(sheet, rows6, title6Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "服务费(元)");
//				cell = getCell(sheet, rows6, ++title6Num);
//				setText(cell, "支付类型");
				cell = getCell(sheet, rows6, ++title6Num);
				setText(cell, "状态");
				
				List<TransferInfo> orderList = (List<TransferInfo>) list;
				int rowOrd6= 2;
				for (TransferInfo or : orderList) {
					sheetRow = sheet.createRow(rowOrd6);
					sheetRow.createCell((short) 6);
					int date6Num = 0;
					cell = getCell(sheet, rowOrd6, date6Num);
					setText(cell,DateUtil.getNewFormatDateString(or.getTransferTime()));
					cell = getCell(sheet, rowOrd6, ++date6Num);
					setText(cell, or.getOrderId());
					cell = getCell(sheet, rowOrd6, ++date6Num);
					setText(cell, "代发工资到永达互联网金融");
					cell = getCell(sheet, rowOrd6, ++date6Num);
					if (or.getPlamId().equals("1")) 
					{
						setText(cell, ""+MoneyUtil.getAmount(or.getTransferAmount()));
					}
					else
					{
						setText(cell, "-"+MoneyUtil.getAmount(or.getTransferAmount()));
					}
					cell = getCell(sheet, rowOrd6, ++date6Num);
					if(MoneyUtil.getAmount(or.getPayeeFee()).equals("0.00"))
					{
					setText(cell, MoneyUtil.getAmount(or.getPayeeFee()));
					}else {
						setText(cell, "-"+MoneyUtil.getAmount(or.getPayeeFee()));
					}
//					cell = getCell(sheet, rowOrd6, ++date6Num);
//					setText(cell,"余额支付");
					cell = getCell(sheet, rowOrd6, ++date6Num);
					if(or.getState().equals("待支付")){
						setText(cell, "处理中");
					}
					else if (or.getState().equals("付款成功")) {
						setText(cell, "处理中");
					}else if (or.getState().equals("转账成功")) {
						setText(cell, "出款成功");
					}else if (or.getState().equals("交易完成")) {
						setText(cell, "出款成功");
					}else if (or.getState().equals("交易关闭")) {
						setText(cell, "关闭");
					}
					rowOrd6++;
				}
				break;
			case 6:
				if (startDate.equals(endDate)) {
					codedFileName = "退款记录_" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "退款记录_"
							+ endDate.substring(0, 8).replace("-", "")
							+ str.substring(8, str.length());
				}
				if (request.getHeader("User-Agent").toLowerCase()
						.indexOf("firefox") > 0) {
					codedFileName = new String(codedFileName.getBytes("UTF-8"),
							"ISO8859-1");
				} else {
					codedFileName = java.net.URLEncoder.encode(codedFileName,
							"utf-8");
				}
				response.setHeader("content-disposition",
						"attachment;filename=" + codedFileName + ".xls");
				sheet = wb.createSheet("退款");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 6);
				int title5Num = 0;
				int rows5=1;
				cell = getCell(sheet, rows5, title5Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "退款订单号");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "原交易订单号");
//				cell = getCell(sheet, 0, ++title5Num);
//				setText(cell, "批次号");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "金额（元）");
//				cell = getCell(sheet, 0, ++title5Num);
//				setText(cell, "服务费（元）");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "发起人");
				cell = getCell(sheet, rows5, ++title5Num);
				setText(cell, "状态");
				List<TradeInfo> tradeInfo = (List<TradeInfo>) list;
				int rowOrd5 = 2;
				for (TradeInfo or : tradeInfo) {
					sheetRow = sheet.createRow(rowOrd5);
					sheetRow.createCell((short) 6);
					int date2Num = 0;
					cell = getCell(sheet, rowOrd5, date2Num);
					setText(cell,DateUtil.getNewFormatDateString(or.getGmtSubmit()));
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getTradeVoucherNo());
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, or.getTradeSourceVoucherNo());
//					cell = getCell(sheet, rowOrd5, ++date2Num);
//					setText(cell, "批次号");
					cell = getCell(sheet, rowOrd5, ++date2Num);
					setText(cell, ""+MoneyUtil.getAmount(or.getPayAmount()));
//					cell = getCell(sheet, rowOrd5, ++date2Num);
//					Money mon=or.getPayAmount().subtract(or.getTradeAmount());//相减
//					setText(cell,MoneyUtil.getAmount(mon) );
//					cell = getCell(sheet, rowOrd5, ++date2Num);
//					setText(cell, "收款账户");
					cell = getCell(sheet, rowOrd5, ++date2Num);
					if(or.getBuyerId().equals(memberId))
					{
						setText(cell, or.getSellerName());
					}
					else
					{
						setText(cell, or.getBuyerName());
					}
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
					rowOrd5++;
				}
				break;
			case 7:
                if (startDate.equals(endDate)) {
                    codedFileName = "保理放贷记录_" + startDate.replace("-", "")
                            + str.substring(8, str.length());
                } else {
                    codedFileName = "保理放贷记录_"
                            + endDate.substring(0, 8).replace("-", "")
                            + str.substring(8, str.length());
                }
                if (request.getHeader("User-Agent").toLowerCase()
                        .indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"),
                            "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName,
                            "utf-8");
                }
                response.setHeader("content-disposition",
                        "attachment;filename=" + codedFileName + ".xls");
                sheet = wb.createSheet("保理放贷记录");
                sheetRow = sheet.createRow(0);
                sheetRow.createCell((int) 1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 10);
                // title行设置
                titleNum2 = 0;
                row2=1;
                cell = getCell(sheet, row2, titleNum2);
                setText(cell, "创建时间");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "商户订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易类型");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "金额(元)");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "状态");
                strList2 = (List<TradeInfo>) list;
                row2 = 2;
                for (TradeInfo bv : strList2) {
                    sheetRow = sheet.createRow(row2);
                    sheetRow.createCell((short) 12);
                    // 数据行设置
                    int dateNum = 0;
                    cell = getCell(sheet, row2, dateNum);
                    setText(cell, DateUtil.getNewFormatDateString(bv.getGmtSubmit()));
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeSourceVoucherNo());
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeVoucherNo());
                    
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, "保理放贷");
                    cell = getCell(sheet, row2, ++dateNum);
                    if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
                        setText(cell, "--");
                    } else {
                        if(bv.getBuyerId().equals(memberId)){
                            setText(cell,"-"+MoneyUtil.getAmount(bv.getPayAmount()));
                        }else{
                            setText(cell,MoneyUtil.getAmount(bv.getPayAmount()));
                        }
                    }
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, MoneyUtil.getAmount(bv.getPayeeFee()));
                    cell = getCell(sheet, row2, ++dateNum);
                    String tradeStatus = null;
                    if (bv.getStatus().equals("100")) {
                        tradeStatus = "待支付";
                    }else if (bv.getStatus().equals("401")) {
                        tradeStatus = "成功";
                    } else if (bv.getStatus().equals("998")) {
                        tradeStatus = "失败";
                    }
                    
                    setText(cell, tradeStatus);
                    row2++;
                }
                break;
			case 8:
                if (startDate.equals(endDate)) {
                    codedFileName = "保理代扣记录_" + startDate.replace("-", "")
                            + str.substring(8, str.length());
                } else {
                    codedFileName = "保理代扣记录_"
                            + endDate.substring(0, 8).replace("-", "")
                            + str.substring(8, str.length());
                }
                if (request.getHeader("User-Agent").toLowerCase()
                        .indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"),
                            "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName,
                            "utf-8");
                }
                response.setHeader("content-disposition",
                        "attachment;filename=" + codedFileName + ".xls");
                sheet = wb.createSheet("保理代扣记录");
                sheetRow = sheet.createRow(0);
                sheetRow.createCell((int) 1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 10);
                // title行设置
                titleNum2 = 0;
                row2=1;
                cell = getCell(sheet, row2, titleNum2);
                setText(cell, "创建时间");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "商户订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易类型");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "金额(元)");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "状态");
                strList2 = (List<TradeInfo>) list;
                row2 = 2;
                for (TradeInfo bv : strList2) {
                    sheetRow = sheet.createRow(row2);
                    sheetRow.createCell((short) 12);
                    // 数据行设置
                    int dateNum = 0;
                    cell = getCell(sheet, row2, dateNum);
                    setText(cell, DateUtil.getNewFormatDateString(bv.getGmtSubmit()));
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeSourceVoucherNo());
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeVoucherNo());
                    
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, "保理代扣");
                    cell = getCell(sheet, row2, ++dateNum);
                    if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
                        setText(cell, "--");
                    } else {
                        if(bv.getBuyerId().equals(memberId)){
                            setText(cell,"-"+MoneyUtil.getAmount(bv.getPayAmount()));
                        }else{
                            setText(cell,MoneyUtil.getAmount(bv.getPayAmount()));
                        }
                    }
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, MoneyUtil.getAmount(bv.getPayeeFee()));
                    cell = getCell(sheet, row2, ++dateNum);
                    String tradeStatus = null;
                    if (bv.getStatus().equals("100")) {
                        tradeStatus = "待支付";
                    }else if (bv.getStatus().equals("401")) {
                        tradeStatus = "成功";
                    } else if (bv.getStatus().equals("998")) {
                        tradeStatus = "失败";
                    }
                    
                    setText(cell, tradeStatus);
                    row2++;
                }
                break;
			case 9:
                if (startDate.equals(endDate)) {
                    codedFileName = "保理还款记录_" + startDate.replace("-", "")
                            + str.substring(8, str.length());
                } else {
                    codedFileName = "保理还款记录_"
                            + endDate.substring(0, 8).replace("-", "")
                            + str.substring(8, str.length());
                }
                if (request.getHeader("User-Agent").toLowerCase()
                        .indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"),
                            "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName,
                            "utf-8");
                }
                response.setHeader("content-disposition",
                        "attachment;filename=" + codedFileName + ".xls");
                sheet = wb.createSheet("保理还款记录");
                sheetRow = sheet.createRow(0);
                sheetRow.createCell((int) 1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期：["+startDate+"]  记录终止日期：["+endDate+"]");
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 10);
                // title行设置
                titleNum2 = 0;
                row2=1;
                cell = getCell(sheet, row2, titleNum2);
                setText(cell, "创建时间");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "商户订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易订单号");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "交易类型");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "金额(元)");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, row2, ++titleNum2);
                setText(cell, "状态");
                strList2 = (List<TradeInfo>) list;
                row2 = 2;
                for (TradeInfo bv : strList2) {
                    sheetRow = sheet.createRow(row2);
                    sheetRow.createCell((short) 12);
                    // 数据行设置
                    int dateNum = 0;
                    cell = getCell(sheet, row2, dateNum);
                    setText(cell, DateUtil.getNewFormatDateString(bv.getGmtSubmit()));
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeSourceVoucherNo());
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, bv.getTradeVoucherNo());
                    
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, "保理还款");
                    cell = getCell(sheet, row2, ++dateNum);
                    if (MoneyUtil.getAmount(bv.getPayAmount()).equals("0.00")) {
                        setText(cell, "--");
                    } else {
                        if(bv.getBuyerId().equals(memberId)){
                            setText(cell,"-"+MoneyUtil.getAmount(bv.getPayAmount()));
                        }else{
                            setText(cell,MoneyUtil.getAmount(bv.getPayAmount()));
                        }
                    }
                    cell = getCell(sheet, row2, ++dateNum);
                    setText(cell, MoneyUtil.getAmount(bv.getPayeeFee()));
                    cell = getCell(sheet, row2, ++dateNum);
                    String tradeStatus = null;
                    if (bv.getStatus().equals("100")) {
                        tradeStatus = "待支付";
                    }else if (bv.getStatus().equals("401")) {
                        tradeStatus = "成功";
                    } else if (bv.getStatus().equals("998")) {
                        tradeStatus = "失败";
                    }
                    
                    setText(cell, tradeStatus);
                    row2++;
                }
                break;
			default:
				System.out.println("error");
				break;
			}

			fOut = response.getOutputStream();
			wb.write(fOut);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fOut.flush();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			session.setAttribute("state", "open");
		}
		System.out.println("文件生成...");
	}
}