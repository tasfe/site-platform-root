package com.netfinworks.site.web.action.ersys;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.kinggrid.kgcore.enmu.KGServerTypeEnum;
import com.kinggrid.pdf.KGPdfHummer;
import com.kinggrid.pdf.executes.PdfSignature4KG;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.ersys.facade.request.TradeListQuery;
import com.netfinworks.ersys.facade.response.TradeOrderInfo;
import com.netfinworks.ersys.service.facade.api.ErServiceFacade;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.ErTradeForm;
import com.netfinworks.site.web.common.vo.TradeOrderInfo1;
import com.netfinworks.site.web.util.DateUtils;

@Controller
public class ErTradeAction extends BaseAction{
	@Resource(name="erServiceFacade")
	private ErServiceFacade erServiceFacade;
	
	@Resource(name = "memberService")
    private MemberService memberService;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Resource(name = "defaultPayPasswdService")
	private DefaultPayPasswdService defaultPayPasswdService;
	
    @Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	/**本地文件目录*/
	@Value("${erFile}")
 	private String erFile;
	
	private SimpleDateFormat sdfhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@RequestMapping(value = "/my/all-ertrade.htm")
	public ModelAndView searchErAll(HttpServletRequest request,
			HttpServletResponse resp,OperationEnvironment env,ErTradeForm form) throws Exception {
		try {
			EnterpriseMember user = getUser(request);
			String id = user.getMemberId();
		
			RestResponse restP = new RestResponse();
			restP.setData(new HashMap<String, Object>());
			String txnType = form.getTxnType();//查询类型
			String queryStartTime = form.getQueryStartTime();// 查询时间//5、
			String queryEndTime = form.getQueryEndTime();// 查询时间 //5、
			String tradeType = form.getTradeType();//交易类型
			String downstatus = form.getDownStatus();//交易状态
			String keyword = form.getKeyword();//关键字类型
			String keyvalue = form.getKeyvalue();//关键字内容
			String currentPage = form.getCurrentPage();// 4 String currentPage =
			if (StringUtils.isBlank(currentPage)) {// 4
				currentPage = "1";// 4 
			}
			PageInfo pageInfo = new PageInfo();
			pageInfo.setCurrentPage(Integer.parseInt(currentPage));
			TradeListQuery listQuery=new TradeListQuery();
			if (StringUtils.isNotBlank(queryStartTime)) {
				listQuery.setStartDate(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
			    queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-1)) + " 00:00";
			    listQuery.setStartDate(sdfhms.parse(queryStartTime + ":00"));
		
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				listQuery.setEndDate(DateUtils.parseDate(queryEndTime + ":59"));
		
			} else {
				queryEndTime = sdf.format(DateUtil.getDateNearCurrent(-1)) + " 23:59";
				listQuery.setEndDate(sdfhms.parse(queryEndTime + ":59"));
			}
			
			if (StringUtils.isNotBlank(downstatus)) {
				listQuery.setHasDownLoad(downstatus);
			}
			if("1".equals(keyword)&&StringUtils.isNotBlank(keyvalue)){
				listQuery.setSourceBatchNo(keyvalue);
			}else if("2".equals(keyword)&&StringUtils.isNotBlank(keyvalue)){
				listQuery.setTradeSrcVoucherNo(keyvalue);
			}
			String txnType1="";
			String status="";
			
			String[] tradeTypeCode = new String[4];
			if(StringUtils.isNotBlank(txnType)&&StringUtils.isNotBlank(tradeType)){
				if (tradeType == null) {
					tradeTypeCode[0] = "0";
					tradeTypeCode[1] = "0";
					tradeTypeCode[2] = "0";
					tradeTypeCode[3] = "0";
				} else {
					tradeTypeCode = tradeType.split(",");
				}
				if("2".equals(txnType)&&"3".equals(tradeTypeCode[0])){//转账入款
					txnType1="10310";
				}else if("2".equals(txnType)&&"1".equals(tradeTypeCode[0])){//转账到永达互联网金融 
					txnType1="10310";
				}else if("2".equals(txnType)&&"2".equals(tradeTypeCode[0])){//转账到卡
					txnType1="10220,10221";
				}else if("3".equals(txnType)&&"1".equals(tradeTypeCode[1])){//代发工资到账户
					txnType1="10231";
				}else if("3".equals(txnType)&&"2".equals(tradeTypeCode[1])){//代发工资到银行卡
					txnType1="10230";
				}else if("4".equals(txnType)&&"1".equals(tradeTypeCode[2])){//资金归集到账户
					txnType1="10241";
				}else if("4".equals(txnType)&&"2".equals(tradeTypeCode[2])){//资金归集到银行卡
					txnType1="10240";
				}else if("5".equals(txnType)){//退票
					status="refundTicket";
				}
				if ("3".equals(tradeTypeCode[0])) {
					listQuery.setSellerId(id);//入款
				}else {
					listQuery.setBuyerId(id);
				}
			}else{
				txnType1="10310";
				listQuery.setSellerId(id);//入款
			}
				
			listQuery.setTradeType(txnType1);
			
			if("refundTicket".equals(status)){
				listQuery.setStatus(status);
			}else{
				listQuery.setStatus("success");
			}
			List<TradeOrderInfo> list2 =new ArrayList<TradeOrderInfo>();
			
			try {
				 list2=erServiceFacade.queryByTradeCount(listQuery);
				 logger.info("查询电子回单记录条数:{}", list2);
			} catch (Exception e) {
				logger.info("查询电子回单记录条数异常", e);
			}
			
			int i=0;
			if(null!=list2&&list2.size()>0){
				i=list2.size();
			}
			listQuery.setQueryBase(pageInfo);
			List<TradeOrderInfo> list=new ArrayList<TradeOrderInfo>();
			try {
				 list=erServiceFacade.queryByselect(listQuery);
			} catch (Exception e) {
				logger.info("查询电子回单记录异常", e);
			}
			
			List<TradeOrderInfo1> newList=new ArrayList<TradeOrderInfo1>();
			if(null!=list&&list.size()>0){
				for(TradeOrderInfo tradeOrderInfo :list){
					TradeOrderInfo1 tblTradeOrderInfo1=new TradeOrderInfo1();
					BeanUtils.copyProperties(tblTradeOrderInfo1, tradeOrderInfo);
					tblTradeOrderInfo1.setFee1( new Money(tradeOrderInfo.getFee()));
					tblTradeOrderInfo1.setTradeAmount1( new Money(tradeOrderInfo.getTradeAmount()));
					if("1".equals(tradeOrderInfo.getSource())){//到永达互联网金融
						
					}else{//卡
						tblTradeOrderInfo1.setBuyerName(tradeOrderInfo.getName());
					}
					newList.add(tblTradeOrderInfo1);
				}
			}
			restP.getData().put("queryStartTime", queryStartTime);
		    restP.getData().put("queryEndTime", queryEndTime);
		    if(txnType1.equals("10310") && !"1".equals(tradeTypeCode[0])){
		    	txnType="2";
		    	tradeTypeCode[0]="3";
		    }
		    restP.getData().put("txnType", txnType);
		    if("2".equals(txnType)){//转账
				restP.getData().put("tradeType", tradeTypeCode[0]);
			}else if("3".equals(txnType)){//代发工资
				restP.getData().put("tradeType", tradeTypeCode[1]);
			}else if("4".equals(txnType)){//资金归集
				restP.getData().put("tradeType", tradeTypeCode[2]);
			}else if("5".equals(txnType)){//退票
				restP.getData().put("tradeType", "1");
			}
		    restP.getData().put("downStatus", downstatus);
		    restP.getData().put("insertDate",sdfhms.format(new Date()));
		    restP.getData().put("keyvalue", keyvalue);
		    restP.getData().put("keyword", keyword);
			pageInfo.setTotalItem(i);
			// 分页信息及信息列表
			PageResultList resultList = new PageResultList();
			resultList.setPageInfo(pageInfo);// 分页信息
			resultList.setInfos(newList);// list
			restP.getData().put("page",resultList);
			restP.setSuccess(true);
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/ertrade/ertrade_recode", "response", restP);
		} catch (Exception e) {
			logger.info("电子回单查询报错：",e);
		}
		return new ModelAndView();
	}
	
	@RequestMapping(value = "/my/{txnType}/{tradeType}/{tradeId}/singleDownload.htm")
	public void singleDownload(@PathVariable String txnType,
			@PathVariable String tradeType, @PathVariable String tradeId,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, DocumentException, BizException,
			MemberNotExistException {
		DecimalFormat df = new DecimalFormat("0.00");
		try {
			if (tradeId != null) {
				logger.info("single start");
				String tradeTypeName="";
				if(txnType.equals("2") &&tradeType.equals("3")){//转账入款
					tradeTypeName="转账入款";
				}else if(txnType.equals("2") &&tradeType.equals("1")){//转账到永达互联网金融 
					tradeTypeName="付款到永达互联网金融";
				}else if(txnType.equals("2")&&tradeType.equals("2")){//转账到卡
					tradeTypeName="付款到卡";
				}else if(txnType.equals("3")&&tradeType.equals("1")){//代发工资到账户
					tradeTypeName="代发工资到账户";
				}else if(txnType.equals("3")&&tradeType.equals("2")){//代发工资到银行卡
					tradeTypeName="代发工资到银行卡";
				}else if(txnType.equals("4")&&tradeType.equals("1")){//资金归集到账户
					tradeTypeName="资金归集到账户";
				}else if(txnType.equals("4")&&tradeType.equals("2")){//资金归集到银行卡
					tradeTypeName="资金归集到银行卡";
				}else if(txnType.equals("5")){//退票
					tradeTypeName="退票";
				}

				String isNeed = req.getParameter("isNeed") == null ? "false"
						: req.getParameter("isNeed");
				EnterpriseMember user = getUser(req);
				String id = user.getMemberId();
				deleteFiles(erFile + File.separator + "single" + File.separator
						+ id);
				BaseMember BuyAccount = new BaseMember();
				BaseMember SellAccount = new BaseMember();
				TradeListQuery tradeListQuery = new TradeListQuery();
				tradeListQuery.setId(tradeId);
				if ("3".equals(tradeType)) {
					tradeListQuery.setSellerId(id);
				}else {
					tradeListQuery.setBuyerId(id);
				}
				List<TradeOrderInfo> list=new ArrayList<TradeOrderInfo>();
				try {
					list = erServiceFacade
							.queryByTradeCount(tradeListQuery);
				} catch (Exception e) {
					// TODO: handle exception
				}
				String fee = "";
				String fileName = erFile + File.separator + "single.pdf";
				String filePath = erFile + File.separator + "single"
						+ File.separator + id +File.separator+System.currentTimeMillis()+ File.separator + tradeId;
				if (list != null && !list.isEmpty()) {
					logger.info("create single trade");
					// 生成电子回单
					PdfReader reader = new PdfReader(fileName);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					PdfStamper ps = new PdfStamper(reader, bos);

					BaseFont bf = BaseFont.createFont("STSong-Light",
							"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
					Font FontChinese = new Font(bf, 5, Font.NORMAL);
					AcroFields s = ps.getAcroFields();
					s.addSubstitutionFont(bf);
					for (TradeOrderInfo tif : list) {
						fee = tif.getFee() == null ? "" : tif.getFee().toString();
						s.setField("create_time", sdfhms.format(new Date()));
						s.setField("trade_no", tif.getTradeVoucherNo());
						BuyAccount = memberService.queryMemberById(tif.getBuyerId(), env);
						s.setField("f_full_name", BuyAccount.getMemberName());
						s.setField("f_acct_no", BuyAccount.getLoginName());
						if (txnType.equals("5")) {// 退票
							BigDecimal amount=new BigDecimal(tif.getTradeAmount()==null?0:tif.getTradeAmount().doubleValue());
							BigDecimal tradeFee=new BigDecimal(tif.getFee()==null?0:tif.getFee().doubleValue());
							tif.setTradeAmount(amount.add(tradeFee));
						}
						s.setField(
								"up_amt",
								"  "
										+ digitUppercase(tif.getTradeAmount() == null ? 0.00
												: tif.getTradeAmount()
														.doubleValue()));
						s.setField("low_amt",
								"  " + df.format(tif.getTradeAmount()));
						s.setField("f_company", "永达互联网金融信息服务有限公司");
						if (tradeType.equals("1") && !txnType.equals("5") || tradeType.equals("3")) {
							s.setField("remark", tif.getTradeMemo());
							s.setField("trans_type", tradeTypeName);
							s.setField(
									"pay_time",
									tif.getGmtModified() == null ? "" : sdfhms
											.format(tif.getGmtModified()));
							s.setField("t_full_name", tif.getSellerName());
							SellAccount = memberService.queryMemberById(
									tif.getSellerId(), env);
							s.setField("t_acct_no", SellAccount.getLoginName());
							s.setField("t_company", "永达互联网金融信息服务有限公司");
						} else {
							s.setField("trans_type", tradeTypeName);
							s.setField("remark", tif.getTradeMemo());
							s.setField(
									"pay_time",
									tif.getOrderTime() == null ? "" : sdfhms
											.format(tif.getOrderTime()));
							s.setField("t_full_name", tif.getName());
							String cardNo = "";
							if (tif.getCardNo() != null
									&& !tif.getCardNo().equals("")) {
								cardNo = uesServiceClient.getDataByTicket(tif
										.getCardNo());
							}
							if (StringUtils.isNotBlank(cardNo)) {
								if (isNeed.equals("true")) {
									s.setField("t_acct_no", cardNo);
								} else {
									s.setField(
											"t_acct_no",
											"****"
													+ cardNo.substring(cardNo
															.length() - 4));
								}
							}
							s.setField("t_company", tif.getBranchName());
						}
					}

					ps.setFormFlattening(true);
					ps.close();

					File file = new File(filePath);
					if (!file.isDirectory()) {
						file.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(filePath
							+ File.separator + System.currentTimeMillis()
							+ ".pdf");
					fos.write(bos.toByteArray());

				}
				if (!txnType.equals("5")) {// 不是退票
					// 生成费用回单
					if (fee != null && !fee.equals("") && !fee.equals("0")
							&& !fee.equals("0.00")) {
						logger.info("create single fee");
						PdfReader reader = new PdfReader(fileName);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						PdfStamper ps = new PdfStamper(reader, bos);
						BaseFont bf = BaseFont.createFont("STSong-Light",
								"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
						Font FontChinese = new Font(bf, 5, Font.NORMAL);
						AcroFields s = ps.getAcroFields();
						s.addSubstitutionFont(bf);
						for (TradeOrderInfo tif : list) {
							s.setField("create_time", sdfhms.format(new Date()));
							s.setField("trade_no", tif.getTradeVoucherNo());
							s.setField("f_full_name", BuyAccount.getMemberName());
							s.setField("f_acct_no", BuyAccount.getLoginName());
							s.setField("up_amt",
									"  " + digitUppercase(Double.parseDouble(fee)));
							s.setField("low_amt", "  " + df.format(tif.getFee()));
							s.setField("f_company", "永达互联网金融信息服务有限公司");
							if (tradeType.equals("1") || tradeType.equals("3")) {
								s.setField("remark", tif.getTradeMemo());
								s.setField("trans_type", tradeTypeName + "服务费");
								s.setField(
										"pay_time",
										tif.getGmtModified() == null ? "" : sdfhms
												.format(tif.getGmtModified()));
								s.setField("t_full_name", tif.getSellerName());
								if (tif.getSellerId()!=null) {
									SellAccount = memberService.queryMemberById(
											tif.getSellerId(), env);
									s.setField("t_acct_no", SellAccount.getLoginName());
								}
								s.setField("t_company", "永达互联网金融信息服务有限公司");
							} else {
								s.setField("remark", tif.getTradeMemo());
								s.setField("trans_type", tradeTypeName + "服务费");
								s.setField(
										"pay_time",
										tif.getOrderTime() == null ? "" : sdfhms
												.format(tif.getOrderTime()));
								s.setField("t_full_name", tif.getName());
								String cardNo = "";
								if (tif.getCardNo() != null
										&& !tif.getCardNo().equals("")) {
									cardNo = uesServiceClient.getDataByTicket(tif
											.getCardNo());
								}
								if (StringUtils.isNotBlank(cardNo)) {
									if (isNeed.equals("true")) {
										s.setField("t_acct_no", cardNo);
									} else {
										s.setField(
												"t_acct_no",
												"****"
														+ cardNo.substring(cardNo
																.length() - 4));
									}
								}
								s.setField("t_company", tif.getBranchName());
							}
						}
		
						ps.setFormFlattening(true);
						ps.close();
		
						File file = new File(filePath);
						if (!file.isDirectory()) {
							file.mkdirs();
						}
						FileOutputStream fos = new FileOutputStream(filePath
								+ File.separator + System.currentTimeMillis()
								+ ".pdf");
						fos.write(bos.toByteArray());
					}
				}
				// 合并
				// File file=new File(filePath);
				// File[] tempList=file.listFiles();
				List<File> listFile = getFileSort(filePath);
				String[] files = new String[listFile.size()];
				for (int n = 0; n < listFile.size(); n++) {
					files[n] = listFile.get(n).getPath();
				}
				String str = sdfhms.format(new Date()).replaceAll(" ", "")
						.replaceAll(":", "").replaceAll("-", "");
				String savepath = filePath + File.separator + "电子回单_"
						+ tradeTypeName + "_"+str+"_old" + ".pdf";
				String savepathnew = filePath + File.separator + "电子回单_"
						+ tradeTypeName + "_"+str + ".pdf";
				mergePdfFiles(files, savepath);

				signature(savepathnew, savepath);//增加电子签章
				

				// 下载
				logger.info("start single download");
				resp.setContentType("APPLICATION/x-download");
				resp.setHeader(
						"Content-Disposition",
						"attachment;fileName="
								+ new String("电子回单.rar".getBytes("gb2312"),
										"ISO8859-1"));

				OutputStream os = resp.getOutputStream();
				ZipOutputStream zos = new ZipOutputStream(os);
				zos.setEncoding("gb2312");// 设置文件名编码方式
				logger.info("system_Code is :"+System.getProperty("sun.jnu.encoding"));
				byte[] buf = new byte[1024];
				InputStream in = new FileInputStream(new File(savepathnew));
				zos.putNextEntry(new ZipEntry(new File(savepathnew).getName()));
				logger.info("the fileName is :"+new ZipEntry(new File(savepathnew).getName()));
				int len = 0;
				while ((len = in.read(buf)) != -1) {
					zos.write(buf, 0, len);
				}
				in.close();
				zos.closeEntry();
				zos.close();
				List<TradeOrderInfo> list1 = new ArrayList<TradeOrderInfo>();
				TradeOrderInfo t = new TradeOrderInfo();
				t.setId(tradeId);
				t.setHasDownload("1");
				list1.add(t);
				try {
					erServiceFacade.updateTradeOrder(list1);
				} catch (Exception e) {
					logger.info("修改下载进度发生错误");
				}
			}
		} catch (Exception e) {
			logger.info("singleDownload Exception", e);
		}
	}
	
	
	/**
     * 获取目录下所有文件(按时间排序)
     * 
     * @param path
     * @return
     */
    public static List<File> getFileSort(String path) {
 
        List<File> list = getFiles(path, new ArrayList<File>());
 
        if (list != null && list.size() > 0) {
 
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                	String fileName=file.getName().substring(0, file.getName().length()-4);
                	String newFileName=newFile.getName().substring(0, newFile.getName().length()-4);
                	Long fileDate=Long.valueOf(fileName).longValue();
                	Long newFileDate=Long.valueOf(newFileName).longValue();
                    if (fileDate > newFileDate) {
                        return 1;
                    } else if (fileDate == newFileDate) {
                        return 0;
                    } else {
                        return -1;
                    }
 
                }
            });
 
        }
 
        return list;
    }
    /**
     * 
     * 获取目录下所有文件
     * 
     * @param realpath
     * @param files
     * @return
     */
    public static List<File> getFiles(String realpath, List<File> files) {
 
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }
	
	//删除文件以及文件夹
		public void deleteFiles(String path) {
			try {
				File file = new File(path);
				if (!file.isDirectory()) {
					file.delete();
				} else {
					String[] fileList = file.list();
					for (int j = 0; j < fileList.length; j++) {
						File filessFile = new File(path + File.separator
								+ fileList[j]);
						if (!filessFile.isDirectory()) {
							filessFile.delete();
						} else {
							deleteFiles(path + File.separator + fileList[j]);
						}
					}
					file.delete();
				}
			} catch (Exception e) {
				logger.error("deleteFiles Exception", e);
			}
		}
			
	/**
     * 数字金额大写转换，思想先写个完整的然后将如零拾替换成零
     * 要用到正则表达式
     */
    public static String digitUppercase(double n){
        String fraction[] = {"角", "分"};
        String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
        String unit[][] = {{"元", "万", "亿"},
                     {"", "拾", "佰", "仟"}};
        String head = n < 0? "负": "";
        n = Math.abs(n);
        String s = "";
        for (int i = 0; i < fraction.length; i++) {
            s += (digit[(int)(Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
        }
        if(s.length()<1){
            s = "整";   
        }
        int integerPart = (int)Math.floor(n);
        for (int i = 0; (i < unit[0].length) && (integerPart > 0); i++) {
            String p ="";
            for (int j = 0; (j < unit[1].length) && (n > 0); j++) {
                p = digit[integerPart%10]+unit[1][j] + p;
                integerPart = integerPart/10;
            }
            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }
        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }
    
    
	@ResponseBody
	@RequestMapping(value = "/checkPassword.htm", method = RequestMethod.POST)
	public RestResponse checkPassword(HttpServletRequest req, OperationEnvironment env) {
		EnterpriseMember em=getUser(req);
		RestResponse restP = new RestResponse();
		boolean flag=validatePayPassword(req,em,restP,null);
		restP.setSuccess(flag);
		return restP;
	}
    
    
	 /* 合并pdf
	 * @param files
	 * @param newfile
	 * @return
	 */
    public static boolean mergePdfFiles(String[] files, String newfile) {  
        boolean retValue = false;  
        Document document = null;  
        try {  
            document = new Document(new PdfReader(files[0]).getPageSize(1));  
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(newfile));  
            document.open();  
            for (int i = 0; i < files.length; i++) {  
                PdfReader reader = new PdfReader(files[i]);  
                int n = reader.getNumberOfPages();  
                for (int j = 1; j <= n; j++) {  
                    document.newPage();  
                    PdfImportedPage page = copy.getImportedPage(reader, j);  
                    copy.addPage(page);  
                }  
            }  
            retValue = true; 
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            document.close();  
        }  
        return retValue;  
    }
    
}
