package com.netfinworks.site.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.fos.service.facade.domain.FundoutInfoSummary;
import com.netfinworks.site.core.common.util.MoneyUtil;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.SummaryInfo;
import com.netfinworks.site.domain.domain.info.WalletCheckInfo;
import com.netfinworks.site.domain.domain.trade.OrderSettleInfo;
import com.netfinworks.site.domain.domain.trade.PayWaterInfo;
import com.netfinworks.site.domain.domain.trade.RefundWaterInfo;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.PayChannel;
import com.netfinworks.site.web.util.ExcelParse.ExcelVersion;

public class ExcelUtil extends AbstractExcelView {
	
	private UesServiceClient uesServiceClient;
	
	public UesServiceClient getUesServiceClient() {
		return uesServiceClient;
	}
	public void setUesServiceClient(UesServiceClient uesServiceClient) {
		this.uesServiceClient = uesServiceClient;
	}
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
			/**
			 * formState==1:支付流水 2：订单结算 3：退款流水 4:收支明细 5:转账审核 6:提现审核 7:退款审核
			 * 8:代发工资 9:代付订单、10:我的待审核
			 */
			switch (formState) {
			case 1:
				// 进行转码，使其支持中文文件名

				if (startDate.equals(endDate)) {
					codedFileName = "ZF" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "ZF" + startDate.replace("-", "") + "-"
							+ endDate.replace("-", "")
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
				sheet = wb.createSheet("支付流水");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 14);
				// title行设置
				int title1Num = 0;
				cell = getCell(sheet, 0, title1Num);
				setText(cell, "钱包交易订单号");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "商户订单ID");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "订单编号");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "付款方账号");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "收款方账号");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "商品名称");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "付款金额");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "交易提交时间");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "交易时间");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "支付状态");
				cell = getCell(sheet, 0, ++title1Num);
				setText(cell, "交易类型");

				List<PayWaterInfo> strList = (List<PayWaterInfo>) list;
				int row = 1;
				for (PayWaterInfo bv : strList) {
					sheetRow = sheet.createRow(row);
					sheetRow.createCell((short) 12);
					// 数据行设置
					int dateNum = 0;
					cell = getCell(sheet, row, dateNum);
					setText(cell, bv.getWalletTradeId());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getMerchantOrderId());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getOrderCode());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getPayerAccount());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getPayeeAccount());
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getCommodityName());
					cell = getCell(sheet, row, ++dateNum);
					if (!"".equals(bv.getPaymentAmount())
							&& bv.getPaymentAmount() != null) {
						setText(cell, bv.getPaymentAmount().getAmount()
								.toString());
					}
					cell = getCell(sheet, row, ++dateNum);
					if (!"".equals(bv.getTradeSubmitTime())
							&& bv.getTradeSubmitTime() != null) {
						setText(cell, sdf.format(bv.getTradeSubmitTime()));
					}
					cell = getCell(sheet, row, ++dateNum);
					if (!"".equals(bv.getTradeTime())
							&& bv.getTradeTime() != null) {
						setText(cell, sdf.format(bv.getTradeTime()));
					}
					cell = getCell(sheet, row, ++dateNum);
					setText(cell, bv.getPaymentState());
					cell = getCell(sheet, row, ++dateNum);
					if ("11".equals(bv.getTradeType())) {
						setText(cell, "即时到账收单交易");
					} else if ("12".equals(bv.getTradeType())) {
						setText(cell, "担保收单交易");
					} else if ("13".equals(bv.getTradeType())) {
						setText(cell, "下订收单交易");
					} else {
						setText(cell, "收单退款交易");
					}
					row++;
				}
				break;
			case 2:
				if (startDate.equals(endDate)) {
					codedFileName = "DDJS" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "DDJS" + startDate.replace("-", "") + "-"
							+ endDate.replace("-", "")
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
				sheet = wb.createSheet("订单结算");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 12);
				int title2Num = 0;
				cell = getCell(sheet, 0, title2Num);
				setText(cell, "钱包交易订单号");
				cell = getCell(sheet, 0, ++title2Num);
				setText(cell, "订单编号");
				cell = getCell(sheet, 0, ++title2Num);
				setText(cell, "付款金额");
				cell = getCell(sheet, 0, ++title2Num);
				setText(cell, "实际成交金额");
				cell = getCell(sheet, 0, ++title2Num);
				setText(cell, "交易结束时间");

				List<OrderSettleInfo> orderList = (List<OrderSettleInfo>) list;
				int rowOrd = 1;
				for (OrderSettleInfo or : orderList) {
					sheetRow = sheet.createRow(rowOrd);
					sheetRow.createCell((short) 12);
					int date2Num = 0;
					cell = getCell(sheet, rowOrd, date2Num);
					setText(cell, or.getWalletTradeId());
					cell = getCell(sheet, rowOrd, ++date2Num);
					setText(cell, or.getMerchantOrderId());
					cell = getCell(sheet, rowOrd, ++date2Num);
					if (!"".equals(or.getPaymentAmount())
							&& or.getPaymentAmount() != null) {
						setText(cell, or.getPaymentAmount().getAmount()
								.toString());
					}
					cell = getCell(sheet, rowOrd, ++date2Num);
					if (!"".equals(or.getSettledAmount())
							&& or.getSettledAmount() != null) {
						setText(cell, or.getSettledAmount().getAmount()
								.toString());
					}
					cell = getCell(sheet, rowOrd, ++date2Num);
					if (!"".equals(or.getTradeOverTime())
							&& or.getTradeOverTime() != null) {
						setText(cell, sdf.format(or.getTradeOverTime()));
					}
					rowOrd++;
				}
				break;
			case 3:
				if (startDate.equals(endDate)) {
					codedFileName = "TK" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "TK" + startDate.replace("-", "") + "-"
							+ endDate.replace("-", "")
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
				sheet = wb.createSheet("退款流水");
				sheetRow = sheet.createRow(0);
				sheetRow.createCell((int) 12);
				int title3Num = 0;
				cell = getCell(sheet, 0, title3Num);
				setText(cell, "钱包交易订单号");
				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "商户退单ID");
				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "商户订单ID");
				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "商品名称");

				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "退款金额");
				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "退担保金额");
				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "退款提交时间");
				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "退款时间");
				cell = getCell(sheet, 0, ++title3Num);
				setText(cell, "退款状态");

				List<RefundWaterInfo> refList = (List<RefundWaterInfo>) list;
				int rowRef = 1;
				for (RefundWaterInfo rv : refList) {
					sheetRow = sheet.createRow(rowRef);
					sheetRow.createCell((short) 12);
					int date3Num = 0;
					cell = getCell(sheet, rowRef, date3Num);
					setText(cell, rv.getWalletTradeId());
					cell = getCell(sheet, rowRef, ++date3Num);
					setText(cell, rv.getMerchantRefundOrderId());
					cell = getCell(sheet, rowRef, ++date3Num);
					setText(cell, rv.getMerchantOrderId());
					cell = getCell(sheet, rowRef, ++date3Num);
					setText(cell, rv.getCommodityName());
					cell = getCell(sheet, rowRef, ++date3Num);
					if (!"".equals(rv.getRefundAmount())
							&& rv.getRefundAmount() != null) {
						setText(cell, rv.getRefundAmount().getAmount()
								.toString());
					}
					cell = getCell(sheet, rowRef, ++date3Num);
					if (!"".equals(rv.getRefundGuarantAmount())
							&& rv.getRefundGuarantAmount() != null) {
						setText(cell, rv.getRefundGuarantAmount().getAmount()
								.toString());
					}
					cell = getCell(sheet, rowRef, ++date3Num);
					if (!"".equals(rv.getRefundSubmitTime())
							&& rv.getRefundSubmitTime() != null) {
						setText(cell, sdf.format(rv.getRefundSubmitTime()));
					}
					cell = getCell(sheet, rowRef, ++date3Num);
					if (!"".equals(rv.getRefundTime())
							&& rv.getRefundTime() != null) {
						setText(cell, sdf.format(rv.getRefundTime()));
					}
					cell = getCell(sheet, rowRef, ++date3Num);
					setText(cell, rv.getRefundState());
					rowRef++;
				}
			case 4:
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
			case 5:// 转账审核
				if (startDate.equals(endDate)) {
					codedFileName = "转账审核记录" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "转账审核记录" + endDate.substring(0, 8).replace("-", "")
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
				sheet = wb.createSheet("转账审核");

				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期:[" + startDate + "]    记录终止日期:["
						+ endDate + "]");

				int title5Num = 0;
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 12);
				cell = getCell(sheet, 1, title5Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "商户批次号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "交易批次号");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "商户订单号");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "收款账户");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "申请操作员");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "审核操作员");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "状态");

				int row5 = 2;
				for (Audit audit : (List<Audit>) list) {
					sheetRow = sheet.createRow(row5);
					sheetRow.createCell((int) 12);
					int date4Num = 0;
					cell = getCell(sheet, row5, date4Num);
					setText(cell,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()) != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit
									.getGmtCreated()) : "");

					
					if("batch".equals(audit.getAuditSubType())){
					    cell = getCell(sheet, row5, ++date4Num);
					    setText(cell, audit.getTranSourceVoucherNo());
					    cell = getCell(sheet, row5, ++date4Num);
	                    setText(cell, audit.getTranVoucherNo());
					}else{
					    cell = getCell(sheet, row5, ++date4Num);
	                    setText(cell, "");
	                    cell = getCell(sheet, row5, ++date4Num);
	                    setText(cell, "");
					}
                    
					if("single".equals(audit.getAuditSubType())){
					    cell = getCell(sheet, row5, ++date4Num);
                        setText(cell, audit.getTranVoucherNo());
                        cell = getCell(sheet, row5, ++date4Num);
                        setText(cell, audit.getTranSourceVoucherNo());
                    }else{
                        cell = getCell(sheet, row5, ++date4Num);
                        setText(cell, "");
                        cell = getCell(sheet, row5, ++date4Num);
                        setText(cell, "");
                    }
                    
					cell = getCell(sheet, row5, ++date4Num);
					setText(cell, "转账");
					// 收款账户
					cell = getCell(sheet, row5, ++date4Num);
					if (AuditType.AUDIT_TRANSFER_KJT.getCode().equals(
							audit.getAuditType()) || AuditType.AUDIT_TRANSFER.getCode().equals(
									audit.getAuditType())) {
						String payeeNo = "";
						String payeeAccountName = "";
						
						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
							} else {
								payeeNo = arr[0];
							}
						}
						setText(cell, payeeNo);
					} else{
						String payeeNo = "";
						String payeeBankInfo = "";
						String payeeAccountName = "";
						String later = "";
						
						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
								String bankNo = uesServiceClient.getDataByTicket(payeeNo);
								later = bankNo.substring(bankNo.length() - 4,bankNo.length());
							} else {
								payeeNo = arr[0];
								String bankNo = uesServiceClient.getDataByTicket(payeeNo);
								later = bankNo.substring(bankNo.length() - 4,bankNo.length());
							}
						}
						setText(cell,
								audit.getPayeeBankInfo() != null ? audit.getPayeeBankInfo() + "  尾号:" + later : "");
					}

					cell = getCell(sheet, row5, ++date4Num);
					setText(cell, audit.getAmount().toString());

					cell = getCell(sheet, row5, ++date4Num);
					setText(cell, audit.getFee() != null ? audit.getFee()
							.toString() : "0.00");

					cell = getCell(sheet, row5, ++date4Num);
					setText(cell, audit.getOperatorName());

					cell = getCell(sheet, row5, ++date4Num);
					setText(cell, audit.getAuditorName());

					cell = getCell(sheet, row5, ++date4Num);
					if ("1".equals(audit.getStatus())) {
						setText(cell, "待审核");
					}
					if ("2".equals(audit.getStatus())) {
						setText(cell, "审核成功");
					}
					if ("3".equals(audit.getStatus())) {
						setText(cell, "审核失败");
					}
					row5++;
				}
				break;
			case 6://提现审核
				if (startDate.equals(endDate)) {
					codedFileName = "提现审核记录" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "提现审核记录" + endDate.substring(0, 8).replace("-", "")
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
				sheet = wb.createSheet("提现审核");

				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期:[" + startDate + "]    记录终止日期:["
						+ endDate + "]");

				int title6Num = 0;
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 7);
				cell = getCell(sheet, 1, title6Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, 1, ++title6Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, 1, ++title6Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, 1, ++title6Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, 1, ++title6Num);
				setText(cell, "收款账户");
				cell = getCell(sheet, 1, ++title6Num);
				setText(cell, "申请操作员");
				cell = getCell(sheet, 1, ++title6Num);
				setText(cell, "审核操作员");
				cell = getCell(sheet, 1, ++title6Num);
				setText(cell, "状态");

				List<Map<String, Object>> withdrawalList = (List<Map<String, Object>>) list;
				int row6 = 2;
				for (Audit audit : (List<Audit>) list) {
					sheetRow = sheet.createRow(row6);
					sheetRow.createCell((int) 7);
					int date5Num = 0;
					cell = getCell(sheet, row6, date5Num);
					setText(cell,
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(audit.getGmtCreated()) != null ? new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss").format(audit
									.getGmtCreated()) : "");

					cell = getCell(sheet, row6, ++date5Num);
					setText(cell, audit.getTranVoucherNo());

					cell = getCell(sheet, row6, ++date5Num);
					setText(cell, audit.getAmount().toString());

					cell = getCell(sheet, row6, ++date5Num);
					setText(cell, audit.getFee() != null ? audit.getFee()
							.toString() : "0.00");

					cell = getCell(sheet, row6, ++date5Num);
					String later;
					if (audit.getPayeeNo() != null) {
						String[] payeeNo = audit.getPayeeNo().split("\\|");
						later = payeeNo[0].substring(
								payeeNo[0].length() - 4,
								payeeNo[0].length());
					} else {
						later = "";
					}

					setText(cell,
							audit.getPayeeBankInfo() != null ? audit
									.getPayeeBankInfo() + "  尾号:" + later : "");

					cell = getCell(sheet, row6, ++date5Num);
					setText(cell, audit.getOperatorName());

					cell = getCell(sheet, row6, ++date5Num);
					setText(cell, audit.getAuditorName());

					cell = getCell(sheet, row6, ++date5Num);
					if ("1".equals(audit.getStatus())) {
						setText(cell, "审核中");
					}
					if ("2".equals(audit.getStatus())) {
						setText(cell, "审核通过");
					}
					if ("3".equals(audit.getStatus())) {
						setText(cell, "审核拒绝");
					}

					row6++;
				}
				break;

			case 7://退款审核
				if (startDate.equals(endDate)) {
					codedFileName = "退款审核记录" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "退款审核记录" + endDate.substring(0, 8).replace("-", "")
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
				sheet = wb.createSheet("退款审核");

				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期:[" + startDate + "]    记录终止日期:["
						+ endDate + "]");

				int title7Num = 0;
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 7);

				cell = getCell(sheet, 1, title7Num);
				setText(cell, "创建时间");

				cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "商户批次号");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "退款批次号");
                
				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "退款订单号");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "原交易订单号");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "原商户订单号");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "退款金额(元)");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "退还服务费");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "退款理由");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "申请操作员");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "审核操作员");

				cell = getCell(sheet, 1, ++title7Num);
				setText(cell, "状态");

				List<Map<String, Object>> refundList = (List<Map<String, Object>>) list;
				int row7 = 2;
				for (Audit audit : (List<Audit>) list) {
					sheetRow = sheet.createRow(row7);
					sheetRow.createCell((int) 7);
					int dateNum = 0;
					cell = getCell(sheet, row7, dateNum);
					setText(cell,
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(audit.getGmtCreated()) != null ? new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss").format(audit
									.getGmtCreated()) : "");

					
					if("batch".equals(audit.getAuditSubType())){
					    cell = getCell(sheet, row7, ++dateNum);
					    setText(cell, audit.getTranSourceVoucherNo());
					    cell = getCell(sheet, row7, ++dateNum);
	                    setText(cell, audit.getTranVoucherNo());
					}else{
					    cell = getCell(sheet, row7, ++dateNum);
					    setText(cell, "");
					    cell = getCell(sheet, row7, ++dateNum);
                        setText(cell, "");
					}
                    
					if("single".equals(audit.getAuditSubType())){
    					cell = getCell(sheet, row7, ++dateNum);
    					setText(cell, audit.getTranVoucherNo());
					}else{
					    cell = getCell(sheet, row7, ++dateNum);
                        setText(cell, "");
					}

					cell = getCell(sheet, row7, ++dateNum);
					setText(cell, audit.getOrigTranVoucherNo());

					cell = getCell(sheet, row7, ++dateNum);
					if(audit.getExt() != null && !audit.getExt().isEmpty()){
                        Map<String, Object> ext = new HashMap<String,Object>();
                        if(audit.getExt().indexOf(":")!=-1){
                            ext = JSONObject.parseObject(audit.getExt());
                            String serialNumber = (String)ext.get("serialNumber");
                            setText(cell, serialNumber);
                        }else{
                            setText(cell, audit.getExt());
                        }
                    }
					cell = getCell(sheet, row7, ++dateNum);
					setText(cell, audit.getAmount().toString());

					cell = getCell(sheet, row7, ++dateNum);
					setText(cell, audit.getFee() != null ? audit.getFee()
							.toString() : "0.00");

					cell = getCell(sheet, row7, ++dateNum);
					setText(cell, audit.getRemark());

					cell = getCell(sheet, row7, ++dateNum);
					setText(cell, audit.getOperatorName());

					cell = getCell(sheet, row7, ++dateNum);
					setText(cell, audit.getAuditorName());

					cell = getCell(sheet, row7, ++dateNum);
					if ("1".equals(audit.getStatus())) {
						setText(cell, "待审核");
					}
					if ("2".equals(audit.getStatus())) {
						setText(cell, "审核成功");
					}
					if ("3".equals(audit.getStatus())) {
						setText(cell, "审核失败");
					}

					row7++;
				}
				break;
			case 8:// 代发工资审核
				if (startDate.equals(endDate)) {
					codedFileName = "代发工资审核记录" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "代发工资审核记录" + endDate.substring(0, 8).replace("-", "")
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
				sheet = wb.createSheet("代发工资审核记录");

				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期:[" + startDate + "]    记录终止日期:["
						+ endDate + "]");

				int title8Num = 0;
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 8);
				cell = getCell(sheet, 1, title8Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, 1, ++title8Num);
				setText(cell, "批次号");
				cell = getCell(sheet, 1, ++title8Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, 1, ++title8Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, 1, ++title8Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, 1, ++title8Num);
				setText(cell, "申请操作员");
				cell = getCell(sheet, 1, ++title8Num);
				setText(cell, "审核操作员");
				cell = getCell(sheet, 1, ++title8Num);
				setText(cell, "状态");

				int row8 = 2;
				for (Audit audit : (List<Audit>) list) {
					sheetRow = sheet.createRow(row8);
					sheetRow.createCell((int) 8);
					int date8Num = 0;
					cell = getCell(sheet, row8, date8Num);
					setText(cell,
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(audit.getGmtCreated()) != null ? new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss").format(audit
									.getGmtCreated()) : "");

					cell = getCell(sheet, row8, ++date8Num);
					setText(cell, audit.getTranVoucherNo());

					cell = getCell(sheet, row8, ++date8Num);
					setText(cell, "代发工资");

					cell = getCell(sheet, row8, ++date8Num);
					setText(cell, audit.getAmount().toString());

					cell = getCell(sheet, row8, ++date8Num);
					setText(cell, audit.getFee() != null ? audit.getFee()
							.toString() : "0.00");

					// 申请操作员
					cell = getCell(sheet, row8, ++date8Num);
					setText(cell, audit.getOperatorName());

					// 审核操作员
					cell = getCell(sheet, row8, ++date8Num);
					setText(cell, audit.getAuditorName());

					cell = getCell(sheet, row8, ++date8Num);
					if ("1".equals(audit.getStatus())) {
						setText(cell, "待审核");
					}
					if ("2".equals(audit.getStatus())) {
						setText(cell, "审核成功");
					}
					if ("3".equals(audit.getStatus())) {
						setText(cell, "审核失败");
					}
					row8++;
				}
				break;

			case 10://待审核
				// 进行转码，使其支持中文文件名

				if (startDate.equals(endDate)) {
					codedFileName = "待审核记录" + startDate.replace("-", "")
							+ str.substring(8, str.length());
				} else {
					codedFileName = "待审核记录" + endDate.substring(0, 8).replace("-", "")
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
				sheet = wb.createSheet("我的待审核");

				sheetRow = sheet.createRow(0);
				sheetRow.createCell(1);
				cell = getCell(sheet, 0, 0);
				setText(cell, "记录起始日期:[" + startDate + "]    记录终止日期:["
						+ endDate + "]");

				int title10Num = 0;
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 8);
				cell = getCell(sheet, 1, title10Num);
				setText(cell, "创建时间");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "审核类型");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "交易订单号/批次号");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "金额(元)");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "收款账户");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "申请操作员");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "审核操作员");
				cell = getCell(sheet, 1, ++title10Num);
				setText(cell, "状态");

				int row10 = 2;
				for (Audit audit : (List<Audit>) list) {
					sheetRow = sheet.createRow(row10);
					sheetRow.createCell((int) 8);
					int date10Num = 0;
					cell = getCell(sheet, row10, date10Num);// 创建时间
					setText(cell,
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(audit.getGmtCreated()) != null ? new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss").format(audit
									.getGmtCreated()) : "");

					cell = getCell(sheet, row10, ++date10Num);// 审核类型
					if (AuditType.AUDIT_WITHDRAW.getCode().equals(
							audit.getAuditType())) {
						setText(cell, "提现");
					}
					if (AuditType.AUDIT_REFUND.getCode().equals(
							audit.getAuditType())) {
						setText(cell, "退款");
					}
					if (AuditType.AUDIT_TRANSFER_KJT.getCode().equals(
							audit.getAuditType())
							|| AuditType.AUDIT_TRANSFER_BANK.getCode().equals(
									audit.getAuditType())
									|| AuditType.AUDIT_PAY_TO_CARD.getCode().equals(
											audit.getAuditType())
											|| AuditType.AUDIT_TRANSFER.getCode().equals(
													audit.getAuditType())) {
						setText(cell, "转账");
					}
					if (AuditType.AUDIT_PAYOFF_BANK.getCode().equals(
							audit.getAuditType())
							|| AuditType.AUDIT_PAYOFF_KJT.getCode().equals(
									audit.getAuditType())) {
						setText(cell, "代发工资");
					}

					cell = getCell(sheet, row10, ++date10Num);// 交易订单号、批次号
					setText(cell, audit.getTranVoucherNo());

					cell = getCell(sheet, row10, ++date10Num);// 金额
					setText(cell, audit.getAmount().toString());

					cell = getCell(sheet, row10, ++date10Num);// 手续费
					setText(cell, audit.getFee() != null ? audit.getFee()
							.toString() : "0.00");

					cell = getCell(sheet, row10, ++date10Num);// 收款账户
					if (AuditType.AUDIT_TRANSFER_KJT.getCode().equals(
							audit.getAuditType())|| AuditType.AUDIT_TRANSFER.getCode().equals(
									audit.getAuditType())) {
						String payeeNo = "";
						String payeeAccountName = "";

						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
							} else {
								payeeNo = arr[0];
							}
						}
						setText(cell, payeeNo);
					} else if (AuditType.AUDIT_TRANSFER_BANK.getCode().equals(
							audit.getAuditType())|| AuditType.AUDIT_PAY_TO_CARD.getCode().equals(
									audit.getAuditType())) {
						String payeeNo = "";
						String payeeBankInfo = "";
						String payeeAccountName = "";
						String later = "";

						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
								later = payeeNo.substring(payeeNo.length() - 4,
										payeeNo.length());
							} else {
								payeeNo = arr[0];
								later = payeeNo.substring(payeeNo.length() - 4,
										payeeNo.length());
							}
						}
						setText(cell,
								audit.getPayeeBankInfo() != null ? audit
										.getPayeeBankInfo() + "  尾号:" + later
										: "");
					} else if (AuditType.AUDIT_WITHDRAW.getCode().equals(
							audit.getAuditType())) {
						String later;
						if (audit.getPayeeNo() != null) {
							String[] payeeNo = audit.getPayeeNo().split("\\|");
  							later = payeeNo[0].substring(
  									payeeNo[0].length() - 4,
  									payeeNo[0].length());
						} else {
							later = "";
						}

						setText(cell,
								audit.getPayeeBankInfo() != null ? audit
										.getPayeeBankInfo() + " 尾号:" + later
										: "");
					} else if (AuditType.AUDIT_PAYOFF_BANK.getCode().equals(
							audit.getAuditType())) {
						String payeeNo = "";
						String payeeAccountName = "";
						String later = "";

						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
								later = payeeNo.substring(payeeNo.length() - 4,
										payeeNo.length());
							} else {
								payeeNo = arr[0];
								later = payeeNo.substring(payeeNo.length() - 4,
										payeeNo.length());
							}
						}
						setText(cell,
								audit.getPayeeBankInfo() != null ? audit
										.getPayeeBankInfo() + "  尾号:" + later
										: "");
					} else if (AuditType.AUDIT_PAYOFF_KJT.getCode().equals(
							audit.getAuditType())) {
						String payeeNo = "";
						String payeeAccountName = "";

						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
							} else {
								payeeNo = arr[0];
							}
						}
						setText(cell, payeeNo);
					} else {
						setText(cell, "");
					}

					cell = getCell(sheet, row10, ++date10Num);// 申请操作员
					setText(cell, audit.getOperatorName());

					cell = getCell(sheet, row10, ++date10Num);// 审核操作员
					setText(cell, audit.getAuditorName());

					cell = getCell(sheet, row10, ++date10Num);// 状态
					if (audit.getStatus().equals("1")) {
						setText(cell, "审核中");
					} else if (audit.getStatus().equals("2")) {
						setText(cell, "审核通过");
					} else {
						setText(cell, "审核拒绝");
					}

					row10++;
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
	public void toExcel1(HttpServletRequest request,
			HttpServletResponse response, List list, SummaryInfo summaryInfo,
			int formState, String startDate, String endDate) {
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
					codedFileName = "结算对账单" + startDate.replace("-", "")
                            + str.substring(8, str.length());
				} else {
					codedFileName = "结算对账单" + endDate.substring(0, 8).replace("-", "")
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
				sheet = wb.createSheet("结算对账单");
				
				sheetRow = sheet.createRow(0);
                sheetRow.createCell(1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
                
//				sheetRow = sheet.createRow(1);
//				sheetRow.createCell((int) 2);
//				cell = getCell(sheet, 1, 0);
//				setText(cell, "结算起始时间");
//				cell = getCell(sheet, 1, 1);
//				setText(cell, startDate);
//				
//				cell = getCell(sheet, 1, 2);
//				setText(cell, "结算终止时间");
//				cell = getCell(sheet, 1, 3);
//				setText(cell, endDate);
//
//				sheetRow = sheet.createRow(2);
//				sheetRow.createCell((int) 4);
//				cell = getCell(sheet, 2, 0);
//				setText(cell, "交易订单总笔数");
//				cell = getCell(sheet, 2, 1);
//				setText(cell, summaryInfo.getTotalTradeCount() + "");
//				cell = getCell(sheet, 2, 2);
//				setText(cell, "交易订单总金额（元）");
//				cell = getCell(sheet, 2, 3);
//				setText(cell, summaryInfo.getTotalTradeAmount().toString());
//
//				sheetRow = sheet.createRow(3);
//				sheetRow.createCell((int) 4);
//				cell = getCell(sheet, 3, 0);
//				setText(cell, "退款订单总笔数");
//				cell = getCell(sheet, 3, 1);
//				setText(cell, summaryInfo.getTotalRefundCount() + "");
//				cell = getCell(sheet, 3, 2);
//				setText(cell, "退款订单总金额（元）");
//				cell = getCell(sheet, 3, 3);
//				setText(cell, summaryInfo.getTotalRefundAmount().toString());
//				cell = getCell(sheet, 4, 2);
//				setText(cell, "已结算金额（元）");
//				cell = getCell(sheet, 4, 3);
//				setText(cell, summaryInfo.getRealAmount().toString());

				int title5Num = 0;
				sheetRow = sheet.createRow(1);
				sheetRow.createCell((int) 8);
				cell = getCell(sheet, 1, title5Num);
				setText(cell, "交易类型");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "商户订单号");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "原商户订单号");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "下单时间");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "交易订单号");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "支付时间");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "交易金额(元)");
				cell = getCell(sheet, 1, ++title5Num);
				setText(cell, "服务费(元)");
				cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "支付渠道");
				List<BaseInfo> transList = (List<BaseInfo>) list;
				int rowRef2 = 2;
				for (BaseInfo tran : transList) {
					sheetRow = sheet.createRow(rowRef2);
					sheetRow.createCell((int) 8);
					int date3Num = 0;
					cell = getCell(sheet, rowRef2, date3Num);
					if(tran.getTradeType().equals("INSTANT_TRASFER"))
					{
					setText(cell, "普通转账交易");
					}else if(tran.getTradeType().equals("INSTANT_ACQUIRING")){
						setText(cell, "即时到账收单交易");
					}else if(tran.getTradeType().equals("ENSURE_ACQUIRING")){
						setText(cell, "担保收单交易");
					}else if(tran.getTradeType().equals("PREPAY_ACQUIRING")){
						setText(cell, "下订收单交易");
					}else if(tran.getTradeType().equals("REFUND_ACQUIRING")){
						setText(cell, "收单退款交易");
					}else{
						setText(cell, tran.getTradeType());
					}
					cell = getCell(sheet, rowRef2, ++date3Num);
					setText(cell, tran.getSerialNumber());
					cell = getCell(sheet, rowRef2, ++date3Num);
					setText(cell, tran.getOrigTradeSourceVoucherNo());
					cell = getCell(sheet, rowRef2, ++date3Num);
					setText(cell, DateUtil.getNewFormatDateString(tran
							.getGmtSubmit()));
					cell = getCell(sheet, rowRef2, ++date3Num);
					setText(cell, tran.getTradeVoucherNo());
					// 收款账户
					cell = getCell(sheet, rowRef2, ++date3Num);
					setText(cell, DateUtil.getNewFormatDateString(tran
							.getGmtpaid()));
					
					cell = getCell(sheet, rowRef2, ++date3Num);
					setText(cell, MoneyUtil.getAmount(tran.getOrderMoney()));
					cell = getCell(sheet, rowRef2, ++date3Num);
					setText(cell, MoneyUtil.getAmount(tran.getPayeeFee()));
					cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, PayChannel.getByCode(tran.getPayChannel()).getMessage());
					rowRef2++;
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
	
	//申请记录导出
	public void toExcel3(HttpServletRequest request,
			HttpServletResponse response, List list,
			int formState, String startDate, String endDate) {
		HttpSession session = request.getSession();
		session.setAttribute("state", null);
		// 生成提示信息，
		response.setContentType("application/vnd.ms-excel");
		OutputStream fOut = null;
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
			case 1://转账
                if(startDate.equals(endDate)){
                    codedFileName = "转账申请记录_" + startDate.replace("-", "")
                                          + str.substring(8, str.length());
                   }else{
                       codedFileName = "转账申请记录_" + endDate.substring(0, 8).replace("-", "")+str.substring(8, str.length());
                   }
                if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"), "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName, "utf-8");
                }
                response.setHeader("content-disposition", "attachment;filename="
                                                          + codedFileName + ".xls");
                sheet = wb.createSheet("转账申请");
                
                sheetRow = sheet.createRow(0);
                sheetRow.createCell(1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
                
                int title5Num = 0;
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 11);
                cell = getCell(sheet, 1, title5Num);
                setText(cell, "创建时间");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "交易订单号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "审核类型");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "收款账户");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "金额(元)");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "申请操作员");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "审核操作员");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "状态");


//                List<Map<String,Object>> transferList = (List<Map<String,Object>>) list;
                int row5 = 2;
                for (Audit audit : (List<Audit>)list) {
                    sheetRow = sheet.createRow(row5);
                    sheetRow.createCell((int) 11);
                    int date4Num = 0;
                    cell = getCell(sheet, row5, date4Num);
                    setText(cell, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated())!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()):"");
                    
                    cell = getCell(sheet, row5, ++date4Num);
                    setText(cell, audit.getTranVoucherNo()!=null?audit.getTranVoucherNo():"");
                    
                    cell = getCell(sheet, row5, ++date4Num);
                  	setText(cell, "转账");
                    //收款账户
                    cell = getCell(sheet, row5, ++date4Num);
                    if (AuditType.AUDIT_TRANSFER_KJT.getCode().equals(
							audit.getAuditType()) || AuditType.AUDIT_TRANSFER.getCode().equals(
									audit.getAuditType())) {
						String payeeNo = "";
						String payeeAccountName = "";

						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
							} else {
								payeeNo = arr[0];
							}
						}
						setText(cell, payeeNo);
					} else{
						String payeeNo = "";
						String payeeBankInfo = "";
						String payeeAccountName = "";
						String later = "";

						if (audit.getPayeeNo() != null) {
							String[] arr = audit.getPayeeNo().split("\\|");
							if (arr != null && arr.length > 1) {
								payeeNo = arr[0];
								payeeAccountName = arr[1];
								later = payeeNo.substring(payeeNo.length() - 4,payeeNo.length());
							} else {
								payeeNo = arr[0];
								later = payeeNo.substring(payeeNo.length() - 4,payeeNo.length());
							}
						}
						setText(cell,
								audit.getPayeeBankInfo() != null ? audit.getPayeeBankInfo() + "  尾号:" + later : "");
					}
                    
                    cell = getCell(sheet, row5, ++date4Num);
                    setText(cell, audit.getAmount().toString());
                    
                    cell = getCell(sheet, row5, ++date4Num);
                    setText(cell, audit.getFee()!=null?audit.getFee().toString():"0.00");
                    
                    cell = getCell(sheet, row5, ++date4Num);
                    setText(cell, audit.getOperatorName()!=null?audit.getOperatorName():"");
                    
                    cell = getCell(sheet, row5, ++date4Num);
                    setText(cell, audit.getAuditorName()!=null?audit.getAuditorName():"");
                    
                    cell = getCell(sheet, row5, ++date4Num);
                    if("1".equals(audit.getStatus())){
                    	setText(cell, "待审核");
                    }
                    if("2".equals(audit.getStatus())){
                    	setText(cell, "审核成功");
                    }
                    if("3".equals(audit.getStatus())){
                    	setText(cell, "审核失败");
                    }
                    row5++;
                }
                break;
            case 2:
                if(startDate.equals(endDate)){
                    codedFileName = "提现申请记录_" + startDate.replace("-", "")
                                          + str.substring(8, str.length());
                   }else{
                       codedFileName = "提现申请记录_" + endDate.substring(0, 8).replace("-", "")+str.substring(8, str.length());
                   }
                if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"), "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName, "utf-8");
                }
                response.setHeader("content-disposition", "attachment;filename="
                                                          + codedFileName + ".xls");
                sheet = wb.createSheet("提现申请");
                
                sheetRow = sheet.createRow(0);
                sheetRow.createCell(1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
                
                int title6Num = 0;
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 7);
                cell = getCell(sheet, 1, title6Num);
                setText(cell, "创建时间");
                cell = getCell(sheet, 1, ++title6Num);
                setText(cell, "交易订单号");
                cell = getCell(sheet, 1, ++title6Num);
                setText(cell, "金额(元)");
                cell = getCell(sheet, 1, ++title6Num);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, 1, ++title6Num);
                setText(cell, "收款账户");
                cell = getCell(sheet, 1, ++title6Num);
                setText(cell, "申请操作员");
                cell = getCell(sheet, 1, ++title6Num);
                setText(cell, "审核操作员");
                cell = getCell(sheet, 1, ++title6Num);
                setText(cell, "状态");

                List<Map<String,Object>> withdrawalList = (List<Map<String,Object>>) list;
                int row6 = 2;
                for (Audit audit : (List<Audit>)list) {
                    sheetRow = sheet.createRow(row6);
                    sheetRow.createCell((int) 7);
                    int date5Num = 0;
                    cell = getCell(sheet, row6, date5Num);
                    setText(cell, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated())!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()):"");
                    
                    cell = getCell(sheet, row6, ++date5Num);
                    setText(cell, audit.getTranVoucherNo()!=null?audit.getTranVoucherNo():"");
                   
                    cell = getCell(sheet, row6, ++date5Num);
                    setText(cell, audit.getAmount().toString());
                    
                    cell = getCell(sheet, row6, ++date5Num);
                    setText(cell, audit.getFee()!=null?audit.getFee().toString():"0.00");
                    
                    cell = getCell(sheet, row6, ++date5Num);//收款账户
                    String later;
					if (audit.getPayeeNo() != null) {
						String[] payeeNo = audit.getPayeeNo().split("\\|");
						later = payeeNo[0].substring(
								payeeNo[0].length() - 4,
								payeeNo[0].length());
					} else {
						later = "";
					}

					setText(cell,
							audit.getPayeeBankInfo() != null ? audit
									.getPayeeBankInfo() + "  尾号:" + later : "");

                    cell = getCell(sheet, row6, ++date5Num);
                    setText(cell, audit.getOperatorName()!=null?audit.getOperatorName():"");
                    
                    cell = getCell(sheet, row6, ++date5Num);
                    setText(cell, audit.getAuditorName()!=null?audit.getAuditorName():"");
                    
                    cell = getCell(sheet, row6, ++date5Num);
                    if("1".equals(audit.getStatus())){
                    	setText(cell, "待审核");
                    }
                    if("2".equals(audit.getStatus())){
                    	setText(cell, "审核通过");
                    }
                    if("3".equals(audit.getStatus())){
                    	setText(cell, "审核拒绝");
                    }
                    
                    row6++;
                }
                break;
                
                
            case 3:
                if(startDate.equals(endDate)){
                    codedFileName = "退款申请记录_" + startDate.replace("-", "")
                                          + str.substring(8, str.length());
                   }else{
                       codedFileName = "退款申请记录_" + endDate.substring(0, 8).replace("-", "")+str.substring(8, str.length());
                   }
                if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"), "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName, "utf-8");
                }
                response.setHeader("content-disposition", "attachment;filename="
                                                          + codedFileName + ".xls");
                sheet = wb.createSheet("退款申请");
                
                sheetRow = sheet.createRow(0);
                sheetRow.createCell(1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
                
                int title7Num = 0;
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 7);
                
                cell = getCell(sheet, 1, title7Num);
                setText(cell, "创建时间");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "退款交易号/批次号");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "原交易订单号");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "原商户订单号");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "退款金额(元)");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "退还服务费");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "退款理由");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "申请操作员");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "审核操作员");
                
                cell = getCell(sheet, 1, ++title7Num);
                setText(cell, "状态");

                List<Map<String,Object>> refundList = (List<Map<String,Object>>) list;
                int row7 = 2;
                for (Audit audit : (List<Audit>)list) {
                    sheetRow = sheet.createRow(row7);
                    sheetRow.createCell((int) 7);
                    int dateNum = 0;
                    cell = getCell(sheet, row7, dateNum);
                    setText(cell, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated())!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()):"");
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getTranVoucherNo()!=null?audit.getTranVoucherNo():"");
                   
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getOrigTranVoucherNo()!=null?audit.getOrigTranVoucherNo():"");
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getTranSourceVoucherNo()!=null?audit.getTranSourceVoucherNo():"");
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getAmount().toString());
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getFee()!=null?audit.getFee().toString():"0.00");
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getRemark());
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getOperatorName()!=null?audit.getOperatorName():"");
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    setText(cell, audit.getAuditorName()!=null?audit.getAuditorName():"");
                    
                    cell = getCell(sheet, row7, ++dateNum);
                    if("1".equals(audit.getStatus())){
                    	setText(cell, "待审核");
                    }
                    if("2".equals(audit.getStatus())){
                    	setText(cell, "审核成功");
                    }
                    if("3".equals(audit.getStatus())){
                    	setText(cell, "审核失败");
                    }
                    
                    row7++;
                }
                break;
            case 4://代发工资
                if(startDate.equals(endDate)){
                    codedFileName = "代发工资申请记录_" + startDate.replace("-", "")
                                          + str.substring(8, str.length());
                   }else{
                       codedFileName = "代发工资申请记录_" + endDate.substring(0, 8).replace("-", "")+str.substring(8, str.length());
                   }
                if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                    codedFileName = new String(codedFileName.getBytes("UTF-8"), "ISO8859-1");
                } else {
                    codedFileName = java.net.URLEncoder.encode(codedFileName, "utf-8");
                }
                response.setHeader("content-disposition", "attachment;filename="
                                                          + codedFileName + ".xls");
                sheet = wb.createSheet("代发工资申请记录");
                
                sheetRow = sheet.createRow(0);
                sheetRow.createCell(1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
                
                int title8Num = 0;
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 8);
                cell = getCell(sheet, 1, title8Num);
                setText(cell, "创建时间");
                cell = getCell(sheet, 1, ++title8Num);
                setText(cell, "批次号");
                cell = getCell(sheet, 1, ++title8Num);
                setText(cell, "审核类型");
                cell = getCell(sheet, 1, ++title8Num);
                setText(cell, "金额(元)");
                cell = getCell(sheet, 1, ++title8Num);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, 1, ++title8Num);
                setText(cell, "申请操作员");
                cell = getCell(sheet, 1, ++title8Num);
                setText(cell, "审核操作员");
                cell = getCell(sheet, 1, ++title8Num);
                setText(cell, "状态");

                int row8 = 2;
                for (Audit audit : (List<Audit>)list) {
                    sheetRow = sheet.createRow(row8);
                    sheetRow.createCell((int) 8);
                    int date8Num = 0;
                    cell = getCell(sheet, row8, date8Num);
                    setText(cell, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated())!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()):"");
                    
                    cell = getCell(sheet, row8, ++date8Num);
                    setText(cell, audit.getTranVoucherNo()!=null?audit.getTranVoucherNo():"");
                   
                    cell = getCell(sheet, row8, ++date8Num);
                    setText(cell, "代发工资");
                    
                    cell = getCell(sheet, row8, ++date8Num);
                    setText(cell, audit.getAmount().toString());
                    
                    cell = getCell(sheet, row8, ++date8Num);
                    setText(cell, audit.getFee()!=null?audit.getFee().toString():"0.00");
                    
                    //申请操作员
                    cell = getCell(sheet, row8, ++date8Num);
                    setText(cell, audit.getOperatorName()!=null?audit.getOperatorName():"");
                    
                    //审核操作员
                    cell = getCell(sheet, row8, ++date8Num);
                    setText(cell, audit.getAuditorName()!=null?audit.getAuditorName():"");
                    
                    cell = getCell(sheet, row8, ++date8Num);
                    if("1".equals(audit.getStatus())){
                    	setText(cell, "待审核");
                    }
                    if("2".equals(audit.getStatus())){
                    	setText(cell, "审核成功");
                    }
                    if("3".equals(audit.getStatus())){
                    	setText(cell, "审核失败");
                    }
                    row8++;
                }
                break;
            case 6:
    			// 进行转码，使其支持中文文件名

    			if (startDate.equals(endDate)) {
    				codedFileName = "申请记录_" + startDate.replace("-", "")
    						+ str.substring(8, str.length());
    			} else {
    				codedFileName = "申请记录_" + endDate.substring(0, 8).replace("-", "") + str.substring(8, str.length());
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
    			sheet = wb.createSheet("申请记录");
    			
    			  sheetRow = sheet.createRow(0);
                  sheetRow.createCell(1);
                  cell = getCell(sheet, 0, 0);
                  setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
    			
                  int title10Num = 0;
                  sheetRow = sheet.createRow(1);
                  sheetRow.createCell((int) 8);
                  cell = getCell(sheet, 1, title10Num);
                  setText(cell, "创建时间");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "审核类型");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "交易订单号/批次号");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "金额(元)");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "服务费(元)");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "收款账户");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "申请操作员");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "审核操作员");
                  cell = getCell(sheet, 1, ++title10Num);
                  setText(cell, "状态");
                  
                  int row10 = 2;
                  for (Audit audit: (List<Audit>)list) {
                      sheetRow = sheet.createRow(row10);
                      sheetRow.createCell((int) 8);
                      int date10Num = 0;
                      cell = getCell(sheet, row10, date10Num);//创建时间
                      setText(cell, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated())!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()):"");
                      
                      cell = getCell(sheet, row10, ++date10Num);//审核类型
                      if(AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())){
                    	  setText(cell, "提现");
                      }
                      if(AuditType.AUDIT_REFUND.getCode().equals(audit.getAuditType())){
                    	  setText(cell, "退款");
                      }
                      if(AuditType.AUDIT_TRANSFER_KJT.getCode().equals(audit.getAuditType())
                    		  || AuditType.AUDIT_TRANSFER_BANK.getCode().equals(
  									audit.getAuditType())
  									|| AuditType.AUDIT_PAY_TO_CARD.getCode().equals(
  											audit.getAuditType())
  											|| AuditType.AUDIT_TRANSFER.getCode().equals(
  													audit.getAuditType())){
                    	  setText(cell, "转账");
                      }
                      if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())||AuditType.AUDIT_PAYOFF_KJT.getCode().equals(audit.getAuditType())){
                    	  setText(cell, "代发工资");
                      }
                      
                      cell = getCell(sheet, row10, ++date10Num);//交易订单号、批次号
                      setText(cell, audit.getTranVoucherNo()!=null?audit.getTranVoucherNo():"");
                      
                      cell = getCell(sheet, row10, ++date10Num);//金额
                      setText(cell, audit.getAmount().toString());
                      
                      cell = getCell(sheet, row10, ++date10Num);//手续费
                      setText(cell, audit.getFee()!=null?audit.getFee().toString():"0.00");
                      
                      cell = getCell(sheet, row10, ++date10Num);//收款账户
                      if (AuditType.AUDIT_TRANSFER_KJT.getCode().equals(
  							audit.getAuditType())|| AuditType.AUDIT_TRANSFER.getCode().equals(
									audit.getAuditType())) {
  						String payeeNo = "";
  						String payeeAccountName = "";

  						if (audit.getPayeeNo() != null) {
  							String[] arr = audit.getPayeeNo().split("\\|");
  							if (arr != null && arr.length > 1) {
  								payeeNo = arr[0];
  								payeeAccountName = arr[1];
  							} else {
  								payeeNo = arr[0];
  							}
  						}
  						setText(cell, payeeNo);
  					} else if (AuditType.AUDIT_TRANSFER_BANK.getCode().equals(
  							audit.getAuditType())|| AuditType.AUDIT_PAY_TO_CARD.getCode().equals(
									audit.getAuditType())) {
  						String payeeNo = "";
  						String payeeBankInfo = "";
  						String payeeAccountName = "";
  						String later = "";

  						if (audit.getPayeeNo() != null) {
  							String[] arr = audit.getPayeeNo().split("\\|");
  							if (arr != null && arr.length > 1) {
  								payeeNo = arr[0];
  								payeeAccountName = arr[1];
  								later = payeeNo.substring(payeeNo.length() - 4,
  										payeeNo.length());
  							} else {
  								payeeNo = arr[0];
  								later = payeeNo.substring(payeeNo.length() - 4,
  										payeeNo.length());
  							}
  						}
  						setText(cell,
  								audit.getPayeeBankInfo() != null ? audit
  										.getPayeeBankInfo() + "  尾号:" + later
  										: "");
  					} else if (AuditType.AUDIT_WITHDRAW.getCode().equals(
  							audit.getAuditType())) {
  						String later;
  						if (audit.getPayeeNo() != null) {
  							String[] payeeNo = audit.getPayeeNo().split("\\|");
  							later = payeeNo[0].substring(
  									payeeNo[0].length() - 4,
  									payeeNo[0].length());
  						} else {
  							later = "";
  						}

  						setText(cell,
  								audit.getPayeeBankInfo() != null ? audit
  										.getPayeeBankInfo() + "  尾号:" + later : "");
  					} else if (AuditType.AUDIT_PAYOFF_BANK.getCode().equals(
  							audit.getAuditType())) {
  						String payeeNo = "";
  						String payeeAccountName = "";
  						String later = "";

  						if (audit.getPayeeNo() != null) {
  							String[] arr = audit.getPayeeNo().split("\\|");
  							if (arr != null && arr.length > 1) {
  								payeeNo = arr[0];
  								payeeAccountName = arr[1];
  								later = payeeNo.substring(payeeNo.length() - 4,
  										payeeNo.length());
  							} else {
  								payeeNo = arr[0];
  								later = payeeNo.substring(payeeNo.length() - 4,
  										payeeNo.length());
  							}
  						}
  						setText(cell,
  								audit.getPayeeBankInfo() != null ? audit
  										.getPayeeBankInfo() + "  尾号:" + later
  										: "");
  					} else if (AuditType.AUDIT_PAYOFF_KJT.getCode().equals(
  							audit.getAuditType())) {
  						String payeeNo = "";
  						String payeeAccountName = "";

  						if (audit.getPayeeNo() != null) {
  							String[] arr = audit.getPayeeNo().split("\\|");
  							if (arr != null && arr.length > 1) {
  								payeeNo = arr[0];
  								payeeAccountName = arr[1];
  							} else {
  								payeeNo = arr[0];
  							}
  						}
  						setText(cell, payeeNo);
  					} else {
  						setText(cell, "");
  					}
                      
                      cell = getCell(sheet, row10, ++date10Num);//申请操作员
                      setText(cell, audit.getOperatorName()!=null?audit.getOperatorName():"");
                      
                      
                      cell = getCell(sheet, row10, ++date10Num);//审核操作员
                      setText(cell, audit.getAuditorName()!=null?audit.getAuditorName():"");
                      
                      cell = getCell(sheet, row10, ++date10Num);//状态
                      if(audit.getStatus().equals("1")){
                    	  setText(cell, "待审核");
                      }else if(audit.getStatus().equals("2")){
                    	  setText(cell, "审核通过");
                      }else{
                    	  setText(cell, "审核拒绝");
                      }
                      
                      row10++;
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
	
	public void toExcel4(HttpServletRequest request,
            HttpServletResponse response, List<Map<String, String>> list,
            int formState, String startDate, String endDate) {
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
                codedFileName = "转账_" +  str;
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
                sheet = wb.createSheet("转账");
                
                sheetRow = sheet.createRow(0);
                sheetRow.createCell(1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
                

                int title5Num = 0;
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 10);
                cell = getCell(sheet, 1, title5Num);
                setText(cell, "创建时间");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "批次号(商户)");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "交易订单号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "商户订单号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "交易类型");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "收款账户");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "金额(元)");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "服务费(元)");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "申请操作员");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "审核操作员");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "状态");
                int rowRef2 = 2;
                for (int i=0;i<list.size();i++) {
                    Map<String, String> listDetail = list.get(i);
                    sheetRow = sheet.createRow(rowRef2);
                    sheetRow.createCell((int) 10);
                    int date3Num = 0;
                    cell = getCell(sheet, rowRef2, date3Num);
                    setText(cell, listDetail.get("gmtCreated"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("sourceBatchNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("tradeVoucherNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("sourceDetailNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, "转账");
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("payeeAccountInfo")+" \r\n"+listDetail.get("payeeAccountNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("amount"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("fee"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("operatorName"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("auditorName"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    if(listDetail.get("status").equals("WP")){
                        setText(cell, "处理中");
                    }else if (listDetail.get("status").equals("I")){
                        setText(cell, "初始");
                    }else if (listDetail.get("status").equals("S")){
                        setText(cell, "成功");
                    }else if (listDetail.get("status").equals("F")){
                        setText(cell, "失败");
                    }
                    rowRef2++;
                }
                break;
            case 2:
                codedFileName = "退款_" + str;
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
                sheetRow.createCell(1);
                cell = getCell(sheet, 0, 0);
                setText(cell, "记录起始日期:["+startDate+"]    记录终止日期:["+endDate+"]");
                

                title5Num = 0;
                sheetRow = sheet.createRow(1);
                sheetRow.createCell((int) 10);
                cell = getCell(sheet, 1, title5Num);
                setText(cell, "创建时间");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "商户批次号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "退款订单号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "原交易订单号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "原商户订单号");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "退款金额(元)");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "退款服务费(元)");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "退款理由");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "申请操作员");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "审核操作员");
                cell = getCell(sheet, 1, ++title5Num);
                setText(cell, "状态");
                rowRef2 = 2;
                for (int i=0;i<list.size();i++) {
                    Map<String, String> listDetail = list.get(i);
                    sheetRow = sheet.createRow(rowRef2);
                    sheetRow.createCell((int) 10);
                    int date3Num = 0;
                    cell = getCell(sheet, rowRef2, date3Num);
                    setText(cell, listDetail.get("gmtCreated"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("tranSourceVoucherNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("tradeVoucherNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("origTradeVoucherNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("origOutDetailNo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("amount"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("fee"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("memo"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("operatorName"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    setText(cell, listDetail.get("auditorName"));
                    cell = getCell(sheet, rowRef2, ++date3Num);
                    if(listDetail.get("status").equals("WP")){
                        setText(cell, "处理中");
                    }else if (listDetail.get("status").equals("I")){
                        setText(cell, "初始");
                    }else if (listDetail.get("status").equals("S")){
                        setText(cell, "成功");
                    }else if (listDetail.get("status").equals("F")){
                        setText(cell, "失败");
                    }
                    rowRef2++;
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
	
		
	/**
	 * 网关交易对账单
	 * @param list 
	 * @param summaryInfo 结算汇总
	 * @param tradeTime   结算日期
	 * @return
	 * @throws Exception
	 */
	public HSSFWorkbook toExcel5(List list, SummaryInfo summaryInfo,String tradeTime) throws Exception{		  
		    // 产生工作簿对象
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet;
			HSSFRow sheetRow;
			HSSFCell cell;
		try {				
			sheet = wb.createSheet("结算对账单");				
			sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
			
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "交易订单总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalTradeCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "交易订单总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalTradeAmount()));
            
            sheetRow = sheet.createRow(2);
            sheetRow.createCell(4);
            cell = getCell(sheet, 2, 0);
            setText(cell, "交易服务费总笔数");
            cell = getCell(sheet, 2, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalFeeCount()));
            cell = getCell(sheet, 2, 2);
            setText(cell, "交易服务费总金额（元）");
            cell = getCell(sheet, 2, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalFeeAmount()));                 
            int title5Num = 0;
			sheetRow = sheet.createRow(4);
			sheetRow.createCell((int) 8);
			cell = getCell(sheet, 4, title5Num);
			setText(cell, "交易类型");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "商户订单号");				
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "下单时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易订单号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "支付时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易金额(元)");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "服务费(元)");
			cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "支付渠道");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易状态");
			List<BaseInfo> transList = (List<BaseInfo>) list;
			int rowRef2 = 5;
			for (BaseInfo tran : transList) {
				sheetRow = sheet.createRow(rowRef2);
				sheetRow.createCell((int) 8);
				int date3Num = 0;
				cell = getCell(sheet, rowRef2, date3Num);
				if(tran.getTradeType().equals("INSTANT_TRASFER"))
				{
				setText(cell, "普通转账交易");
				}else if(tran.getTradeType().equals("INSTANT_ACQUIRING")){
					setText(cell, "即时到账收单交易");
				}else if(tran.getTradeType().equals("ENSURE_ACQUIRING")){
					setText(cell, "担保收单交易");
				}else if(tran.getTradeType().equals("PREPAY_ACQUIRING")){
					setText(cell, "下订收单交易");
				}else if(tran.getTradeType().equals("REFUND_ACQUIRING")){
					setText(cell, "收单退款交易");
				}else{
					setText(cell, tran.getTradeType());
				}
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSerialNumber());					
				cell = getCell(sheet, rowRef2, ++date3Num);				
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtSubmit()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getTradeVoucherNo());
				// 收款账户
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtpaid()));				
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getOrderMoney()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getPayeeFee()));
				cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, tran.getPayChannel()==null?"":PayChannel.getByCode(tran.getPayChannel()).getMessage());
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, converOrderState(tran.getTradeType(),tran.getOrderState()));
				rowRef2++;
			}			
		  
		} catch (Exception e) {
			System.out.println(e);
			throw new Exception("生成Excel失败",e);
		} 
		return wb;
		
	}
	
	

	/**
	 *  退款对账单
	 * @param list
	 * @param summaryInfo
	 * @param tradeTime  结算日期
	 * @return
	 * @throws Exception
	 */
	public HSSFWorkbook toExcel6(List list, SummaryInfo summaryInfo,String tradeTime) throws Exception{		
		 // 产生工作簿对象
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet;
		HSSFRow sheetRow;
		HSSFCell cell;   
		try {			
			sheet = wb.createSheet("结算对账单");				
			sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
			
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "退款订单总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalRefundCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "退款订单总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalRefundAmount()));
            
            sheetRow = sheet.createRow(2);
            sheetRow.createCell(4);
            cell = getCell(sheet, 2, 0);
            setText(cell, "退还服务费总笔数");
            cell = getCell(sheet, 2, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalRfdFeeCount()));
            cell = getCell(sheet, 2, 2);
            setText(cell, "退还服务费总金额（元）");
            cell = getCell(sheet, 2, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalRfdFeeAmount()));                 
            int title5Num = 0;
			sheetRow = sheet.createRow(4);
			sheetRow.createCell((int) 8);
			cell = getCell(sheet, 4, title5Num);
			setText(cell, "交易类型");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "商户批次号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "商户订单号");				
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "原商户订单号");				
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "下单时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易订单号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "支付时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易金额(元)");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "服务费(元)");			
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易状态");
			List<BaseInfo> transList = (List<BaseInfo>) list;
			int rowRef2 = 5;
			for (BaseInfo tran : transList) {
				sheetRow = sheet.createRow(rowRef2);
				sheetRow.createCell((int) 8);
				int date3Num = 0;
				cell = getCell(sheet, rowRef2, date3Num);
				if(tran.getTradeType().equals("INSTANT_TRASFER"))
				{
				setText(cell, "普通转账交易");
				}else if(tran.getTradeType().equals("INSTANT_ACQUIRING")){
					setText(cell, "即时到账收单交易");
				}else if(tran.getTradeType().equals("ENSURE_ACQUIRING")){
					setText(cell, "担保收单交易");
				}else if(tran.getTradeType().equals("PREPAY_ACQUIRING")){
					setText(cell, "下订收单交易");
				}else if(tran.getTradeType().equals("REFUND_ACQUIRING")){
					setText(cell, "收单退款交易");
				}else{
					setText(cell, tran.getTradeType());
				}
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSourceBatchNo());	
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSerialNumber());	
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getOrigTradeSourceVoucherNo());
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtSubmit()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getTradeVoucherNo());
				// 收款账户
				cell = getCell(sheet, rowRef2, ++date3Num);				
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtpaid()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getOrderMoney()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getPayeeFee()));				
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, tran.getTradeState());
				rowRef2++;
			}			
		}  catch (Exception e) {
			throw new Exception("生成Excel失败",e);
		} 
		return wb;

	}
	
	
	/**
	 * 付款到账户对账单
	 * @param list
	 * @param summaryInfo
	 * @param tradeTime   结算日期
	 * @return
	 * @throws Exception
	 */
	public HSSFWorkbook toExcel7(List list, SummaryInfo summaryInfo,String tradeTime) throws Exception{		
		 // 产生工作簿对象
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet;
		HSSFRow sheetRow;
		HSSFCell cell;   
		try {			
			sheet = wb.createSheet("结算对账单");				
			sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
			
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "交易订单总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalTradeCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "交易订单总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalTradeAmount()));
            
            sheetRow = sheet.createRow(2);
            sheetRow.createCell(4);
            cell = getCell(sheet, 2, 0);
            setText(cell, "交易服务费总笔数");
            cell = getCell(sheet, 2, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalFeeCount()));
            cell = getCell(sheet, 2, 2);
            setText(cell, "交易服务费总金额（元）");
            cell = getCell(sheet, 2, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalFeeAmount()));                 
            int title5Num = 0;
			sheetRow = sheet.createRow(4);
			sheetRow.createCell((int) 8);
			cell = getCell(sheet, 4, title5Num);
			setText(cell, "交易类型");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "商户批次号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "商户订单号");							
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "下单时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易订单号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "支付时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易金额(元)");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "服务费(元)");			
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易状态");
			List<BaseInfo> transList = (List<BaseInfo>) list;
			int rowRef2 = 5;
			for (BaseInfo tran : transList) {
				sheetRow = sheet.createRow(rowRef2);
				sheetRow.createCell((int) 8);
				int date3Num = 0;
				cell = getCell(sheet, rowRef2, date3Num);
				if(tran.getTradeType().equals("INSTANT_TRASFER"))
				{
				setText(cell, "普通转账交易");
				}else if(tran.getTradeType().equals("INSTANT_ACQUIRING")){
					setText(cell, "即时到账收单交易");
				}else if(tran.getTradeType().equals("ENSURE_ACQUIRING")){
					setText(cell, "担保收单交易");
				}else if(tran.getTradeType().equals("PREPAY_ACQUIRING")){
					setText(cell, "下订收单交易");
				}else if(tran.getTradeType().equals("REFUND_ACQUIRING")){
					setText(cell, "收单退款交易");
				}else{
					setText(cell, tran.getTradeType());
				}
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSourceBatchNo());	
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSerialNumber());				
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtSubmit()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getTradeVoucherNo());
				// 收款账户
				cell = getCell(sheet, rowRef2, ++date3Num);				
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtpaid()));				
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getOrderMoney()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getPayeeFee()));				
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, converOrderState(tran.getTradeType(),tran.getOrderState()));
				rowRef2++;
			}			
		} catch (Exception e) {
			throw new Exception("生成Excel失败",e);
		} 
		return wb;

	}
	
	/**
	 * 付款到卡对账单
	 * @param list
	 * @param summaryInfo
	 * @param formState
	 * @param endTime
	 */
	public HSSFWorkbook toExcel8(List list, FundoutInfoSummary fundoutInfoSummary,String tradeTime) throws Exception{
		//改进
		ExcelParse excel = ExcelParse.buildWrite(ExcelVersion.Excel_2003);
		try{
			List<Fundout> transList = (List<Fundout>) list;
			excel.createSheet("结算对账单")
			.createRow().createCell("结算日期", false).createCell(tradeTime, false)
			.createRow().createCell("交易订单总笔数", false).createCell(String.valueOf(fundoutInfoSummary.getTotalFundoutCount()), false).createCell("交易订单总金额（元）", false).createCell(MoneyUtil.getAmount(fundoutInfoSummary.getTotalFundoutAmount()), false)
			.createRow().createCell("交易服务费总笔数", false).createCell(String.valueOf(fundoutInfoSummary.getTotalFeeCount()), false).createCell(MoneyUtil.getAmount(fundoutInfoSummary.getTotalFeeAmount()), false)
			.createRow()
			.createRow().createCell("交易类型", false)
			.createCell("商户批次号", false)
			.createCell("商户订单号", false)
			.createCell("下单时间", false)
			.createCell("交易订单号", false)
			.createCell("支付时间", false)
			.createCell("交易金(元)", false)
			.createCell("服务费(元)", false)
			.createCell("交易状态", false);
			for (Fundout tran : transList) {
				excel.createRow()
				.createCell("转账对卡", false)
				.createCell(tran.getSourceBatchNo(), false)
				.createCell(tran.getOutOrderNo(), false)
				.createCell(DateUtil.getNewFormatDateString(tran.getOrderTime()), false)
				.createCell(tran.getFundoutOrderNo(), false)
				.createCell(DateUtil.getNewFormatDateString(tran.getSuccessTime()), false)
				.createCell(MoneyUtil.getAmount(tran.getAmount()), false)
				.createCell(MoneyUtil.getAmount(tran.getFee()), false)
				.createCell(converStatus(tran.getStatus()), false);
			}
		}catch (Exception e) {
			throw new Exception("生成Excel失败",e);
	    }
		return (HSSFWorkbook)excel.getExcelObj();
		// 产生工作簿对象
			/*HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet;
			HSSFRow sheetRow;
			HSSFCell cell;
		try {			
			sheet = wb.createSheet("结算对账单");				
			sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
			
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "交易订单总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(fundoutInfoSummary.getTotalFundoutCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "交易订单总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(fundoutInfoSummary.getTotalFundoutAmount()));
            
            sheetRow = sheet.createRow(2);
            sheetRow.createCell(4);
            cell = getCell(sheet, 2, 0);
            setText(cell, "交易服务费总笔数");
            cell = getCell(sheet, 2, 1);
            setText(cell, String.valueOf(fundoutInfoSummary.getTotalFeeCount()));
            cell = getCell(sheet, 2, 2);
            setText(cell, "交易服务费总金额（元）");
            cell = getCell(sheet, 2, 3);
            setText(cell, MoneyUtil.getAmount(fundoutInfoSummary.getTotalFeeAmount()));                 
            int title5Num = 0;
			sheetRow = sheet.createRow(4);
			sheetRow.createCell((int) 8);
			cell = getCell(sheet, 4, title5Num);
			setText(cell, "交易类型");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "商户批次号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "商户订单号");							
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "下单时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易订单号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "支付时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易金额(元)");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "服务费(元)");			
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易状态");
			List<Fundout> transList = (List<Fundout>) list;
			int rowRef2 = 5;
			for (Fundout tran : transList) {
				sheetRow = sheet.createRow(rowRef2);
				sheetRow.createCell((int) 8);
				int date3Num = 0;
				cell = getCell(sheet, rowRef2, date3Num);				
				setText(cell, "转账对卡");		
				//商户批次号
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSourceBatchNo());	
				//商户订单号
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getOutOrderNo());	
				//下单时间
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getOrderTime()));
				//交易订单号
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getFundoutOrderNo());
				
				//支付时间
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran.getSuccessTime()));
				//交易金额(元)
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getAmount()));
				// 服务费
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getFee()));				
				//交易状态		
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, converStatus(tran.getStatus()));
				rowRef2++;
			  }				
		   } catch (Exception e) {
				throw new Exception("生成Excel失败",e);
		   } 
	      return wb;*/
	}
	
	/**
	 * 退票对账单
	 * @param list
	 * @param summaryInfo
	 * @param formState
	 * @param endTime
	 */
	public HSSFWorkbook toExcel9(List list, FundoutInfoSummary fundoutInfoSummary,String tradeTime) throws Exception {		
		 logger.info("toExcel9");
		// 产生工作簿对象
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet;
			HSSFRow sheetRow;
			HSSFCell cell;
		try {			
			sheet = wb.createSheet("结算对账单");				
			sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
			
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "退票总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(fundoutInfoSummary.getTotalFundoutCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "退票总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(fundoutInfoSummary.getTotalFundoutAmount()));
            
            sheetRow = sheet.createRow(2);
            sheetRow.createCell(4);
            cell = getCell(sheet, 2, 0);
            setText(cell, "退还服务费总笔数");
            cell = getCell(sheet, 2, 1);
            setText(cell, String.valueOf(fundoutInfoSummary.getTotalFeeCount()));
            cell = getCell(sheet, 2, 2);
            setText(cell, "退还服务费总金额（元）");
            cell = getCell(sheet, 2, 3);
            setText(cell, MoneyUtil.getAmount(fundoutInfoSummary.getTotalFeeAmount()));                 
            int title5Num = 0;
			sheetRow = sheet.createRow(4);
			sheetRow.createCell((int) 8);
			cell = getCell(sheet, 4, title5Num);
			setText(cell, "交易类型");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "原商户订单号");			
			cell = getCell(sheet, 4, ++title5Num);			
			setText(cell, "原下单时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "原交易订单号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "退票时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易金额(元)");			
			List<Fundout> transList = (List<Fundout>) list;
			int rowRef2 = 5;
			for (Fundout tran : transList) {
				sheetRow = sheet.createRow(rowRef2);
				sheetRow.createCell((int) 8);
				int date3Num = 0;
				cell = getCell(sheet, rowRef2, date3Num);				
				setText(cell, "退票");		
				//原商户订单号
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSourceBatchNo());					
				//原下单时间
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getOrderTime()));
				//原交易订单号
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getFundoutOrderNo());
				
				//退票时间
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran.getSuccessTime()));
				//交易金额(元)
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getAmount()));				
				rowRef2++;
			  }			
		
		}  catch (Exception e) {
			throw new Exception("生成Excel失败",e);
		} 
	      return wb;
	}
	
	/**
     * 银行卡代扣对账单
     * @param list 
     * @param summaryInfo 结算汇总
     * @param tradeTime   结算日期
     * @return
     * @throws Exception
     */
    public HSSFWorkbook toExcel10(List list, SummaryInfo summaryInfo,String tradeTime) throws Exception{       
            // 产生工作簿对象
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet;
            HSSFRow sheetRow;
            HSSFCell cell;
        try {               
            sheet = wb.createSheet("结算对账单");                
            sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
            
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "交易订单总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalTradeCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "交易订单总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalTradeAmount()));
            
            sheetRow = sheet.createRow(2);
            sheetRow.createCell(4);
            cell = getCell(sheet, 2, 0);
            setText(cell, "交易服务费总笔数");
            cell = getCell(sheet, 2, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalFeeCount()));
            cell = getCell(sheet, 2, 2);
            setText(cell, "交易服务费总金额（元）");
            cell = getCell(sheet, 2, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalFeeAmount()));                 
            int title5Num = 0;
            sheetRow = sheet.createRow(4);
            sheetRow.createCell((int) 8);
            cell = getCell(sheet, 4, title5Num);
            setText(cell, "交易类型");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "商户订单号");             
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "下单时间");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易订单号");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "支付时间");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易金额(元)");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "服务费(元)");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "支付渠道");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易状态");
            List<BaseInfo> transList = (List<BaseInfo>) list;
            int rowRef2 = 5;
            for (BaseInfo tran : transList) {
                sheetRow = sheet.createRow(rowRef2);
                sheetRow.createCell((int) 8);
                int date3Num = 0;
                cell = getCell(sheet, rowRef2, date3Num);
                setText(cell, "银行卡代扣");
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, tran.getSerialNumber());                  
                cell = getCell(sheet, rowRef2, ++date3Num);             
                setText(cell, DateUtil.getNewFormatDateString(tran
                        .getGmtSubmit()));
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, tran.getTradeVoucherNo());
                // 收款账户
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, DateUtil.getNewFormatDateString(tran
                        .getGmtpaid()));                
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, MoneyUtil.getAmount(tran.getOrderMoney()));
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, MoneyUtil.getAmount(tran.getPayeeFee()));
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, PayChannel.getByCode(tran.getPayChannel()).getMessage());
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, converOrderState(tran.getTradeType(),tran.getOrderState()));
                rowRef2++;
            }           
          
        } catch (Exception e) {
            System.out.println(e);
            throw new Exception("生成Excel失败",e);
        } 
        return wb;
        
    }
    /**
	 * pos消费交易对账单
	 * @param list 
	 * @param summaryInfo 结算汇总
	 * @param tradeTime   结算日期
	 * @return
	 * @throws Exception
	 */
	public HSSFWorkbook toExcel11(List list, SummaryInfo summaryInfo,String tradeTime) throws Exception{		  
		    // 产生工作簿对象
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet;
			HSSFRow sheetRow;
			HSSFCell cell;
		try {				
			sheet = wb.createSheet("结算对账单");				
			sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
			
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "交易订单总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalTradeCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "交易订单总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalTradeAmount()));
            
            sheetRow = sheet.createRow(2);
            sheetRow.createCell(4);
            cell = getCell(sheet, 2, 0);
            setText(cell, "交易服务费总笔数");
            cell = getCell(sheet, 2, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalFeeCount()));
            cell = getCell(sheet, 2, 2);
            setText(cell, "交易服务费总金额（元）");
            cell = getCell(sheet, 2, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalFeeAmount()));                 
            int title5Num = 0;
			sheetRow = sheet.createRow(4);
			sheetRow.createCell((int) 9);
			cell = getCell(sheet, 4, title5Num);
			setText(cell, "交易类型");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易订单号");				
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易订单创建时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "收银流水号");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "支付时间");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "交易金额(元)");
			cell = getCell(sheet, 4, ++title5Num);
			setText(cell, "服务费(元)");
			cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "支付方式");
            cell = getCell(sheet, 4, ++title5Num);
            setText(cell, "交易状态");
			List<BaseInfo> transList = (List<BaseInfo>) list;
			int rowRef2 = 5;
			for (BaseInfo tran : transList) {
				sheetRow = sheet.createRow(rowRef2);
				sheetRow.createCell((int) 9);
				int date3Num = 0;
				cell = getCell(sheet, rowRef2, date3Num);
				setText(cell, "消费");
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getTradeVoucherNo());					
				cell = getCell(sheet, rowRef2, ++date3Num);				
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtSubmit()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSerialNumber());
				// 收款账户
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtpaid()));				
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getOrderMoney()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getPayeeFee()));
				cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, tran.getPayChannel()==null?"":tran.getPayChannel());
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, converOrderState(tran.getTradeType(),tran.getOrderState()));
				rowRef2++;
			}			
		  
		} catch (Exception e) {
			System.out.println(e);
			throw new Exception("生成Excel失败",e);
		} 
		return wb;
		
	}
    /**
	 *  pos撤销对账单
	 * @param list
	 * @param summaryInfo
	 * @param tradeTime  结算日期
	 * @return
	 * @throws Exception
	 */
	public HSSFWorkbook toExcel12(List list, SummaryInfo summaryInfo,String tradeTime) throws Exception{		
		 // 产生工作簿对象
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet;
		HSSFRow sheetRow;
		HSSFCell cell;   
		try {			
			sheet = wb.createSheet("结算对账单");				
			sheetRow = sheet.createRow(0);
            sheetRow.createCell(1);
            cell = getCell(sheet, 0, 0);
            setText(cell, "结算日期");
            cell = getCell(sheet, 0, 1);
            setText(cell, tradeTime);
			
            sheetRow = sheet.createRow(1);
            sheetRow.createCell(4);
            cell = getCell(sheet, 1, 0);
            setText(cell, "撤销订单总笔数");
            cell = getCell(sheet, 1, 1);
            setText(cell, String.valueOf(summaryInfo.getTotalRefundCount()));
            cell = getCell(sheet, 1, 2);
            setText(cell, "撤销订单总金额（元）");
            cell = getCell(sheet, 1, 3);
            setText(cell, MoneyUtil.getAmount(summaryInfo.getTotalRefundAmount()));
            
                             
            int title5Num = 0;
			sheetRow = sheet.createRow(3);
			sheetRow.createCell((int) 8);
			cell = getCell(sheet, 3, title5Num);
			setText(cell, "交易类型");
			cell = getCell(sheet, 3, ++title5Num);
			setText(cell, "交易订单号");
			cell = getCell(sheet, 3, ++title5Num);
			setText(cell, "交易订单创建时间");				
			cell = getCell(sheet, 3, ++title5Num);
			setText(cell, "原收银流水号");				
			cell = getCell(sheet, 3, ++title5Num);
			setText(cell, "支付时间");
			cell = getCell(sheet, 3, ++title5Num);
			setText(cell, "交易金额(元)");
			cell = getCell(sheet, 3, ++title5Num);
			setText(cell, "服务费(元)");			
            cell = getCell(sheet, 3, ++title5Num);
            setText(cell, "交易状态");
			List<BaseInfo> transList = (List<BaseInfo>) list;
			int rowRef2 = 4;
			for (BaseInfo tran : transList) {
				sheetRow = sheet.createRow(rowRef2);
				sheetRow.createCell((int) 8);
				int date3Num = 0;
				cell = getCell(sheet, rowRef2, date3Num);
				setText(cell, "撤销");
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getTradeVoucherNo());	
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtSubmit()));
				//setText(cell, tran.getSerialNumber());	
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, tran.getSerialNumber());
				//setText(cell, tran.getOrigTradeSourceVoucherNo());
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, DateUtil.getNewFormatDateString(tran
						.getGmtpaid()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getOrderMoney()));
				cell = getCell(sheet, rowRef2, ++date3Num);
				setText(cell, MoneyUtil.getAmount(tran.getPayeeFee()));				
                cell = getCell(sheet, rowRef2, ++date3Num);
                setText(cell, posOrderState(tran.getOrderState()));
				rowRef2++;
			}			
		}  catch (Exception e) {
			throw new Exception("生成Excel失败",e);
		} 
		return wb;

	}
	public String converOrderState(String tradeType,String orderState){
		if(("INSTANT_TRASFER").equals(tradeType)){
			  if("301".equals(orderState) ||"401".equals(orderState)){
				  return "结算成功";
			  }else if("998".equals(orderState)){
				  return "交易失败";
			  } 
		}else{
			if("301".equals(orderState)){
				return "交易成功";
			}else if("401".equals(orderState)){
				return "结算成功";
			}            
		}
		return orderState;
	}
	public String posOrderState(String orderState){
		
			  if("401".equals(orderState)){
				  return "交易结束";
			  }else if("998".equals(orderState)){
				  return "交易失败";
			  }else if("951".equals(orderState)){
				return "撤销成功";
			  }
		return orderState;
	}
	
	public String converStatus(String status){
		if("submitted".equals(status)){
			return "处理中";
		}else if("failed".equals(status)){
			return "失败";
		}else if("success".equals(status)){
			return "出款成功";
		}
		return status;
	}
	
}