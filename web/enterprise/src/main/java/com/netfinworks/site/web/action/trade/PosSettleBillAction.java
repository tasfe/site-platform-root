package com.netfinworks.site.web.action.trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.trade.impl.SettlementServiceImpl;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.QueryTradeForm;
import com.netfinworks.site.web.common.constant.PayChannel;
import com.netfinworks.site.web.task.BatchTask;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;
import com.netfinworks.ufs.client.UFSClient;
import com.netfinworks.ufs.client.ctx.OutputStreamFileContext;
import com.netfinworks.ufs.client.domain.FileNameInfo;

/**
 * pos结算对账单
 *
 */
@Controller
public class PosSettleBillAction extends BaseAction {
	@Resource(name = "enterpriseTradeService")
	private SettlementServiceImpl settlementServiceImpl;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

    @Resource(name="accountService")
    private AccountService accountService;
    
    @Resource(name = "ufsClient")
	private UFSClient   ufsClient;
    
	/**本地文件目录*/
	@Value("${merchantTradeFilePath}")
 	private String merchantTradeFilePath;
	
	@Resource
	private XUCache<String> xuCache;
    
    private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");

	
	//进入结算对账单页面
		@RequestMapping(value = "/my/pos-settlement1.htm", method = { RequestMethod.GET })
		public ModelAndView searchAll(HttpServletRequest req,
				HttpServletResponse resp) throws Exception {
			RestResponse restP = new RestResponse();
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/trade/PosNewSettlementOfBills", "response", restP);
		}
		
		//生成日期以及是否有下载链接标识
		@RequestMapping(value = "/my/pos-settlement1.htm", method = { RequestMethod.POST })
		public @ResponseBody JSONObject searchAll1(HttpServletRequest req,
				HttpServletResponse resp) throws Exception {
			JSONObject json = new JSONObject();
			JSONArray arr = new JSONArray();
			int year = 0;
			int month = 0;
			int days = 0;
			String id="";
			try {
				year = Integer.parseInt(req.getParameter("year"));
				month = Integer.parseInt(req.getParameter("month"));
				days = getDaysByYearMonth(year, month);
				String monthFile = "";
				if (month < 10) {
					monthFile = "0" + month;
				} else {
					monthFile = "" + month;
				}
				// 获取当前商户id
				EnterpriseMember user = getUser(req);
				id = user.getMemberId();
				// 查询当月有无对账单,有对账单，返回标识
				String monthFilePath = "batchTask"+ File.separator +id+ File.separator+"pos"+ File.separator + year + ""+ monthFile+File.separator+"month";
				List<FileNameInfo> list = ufsClient.list(monthFilePath);
				if (list != null && !list.isEmpty()) {
					json.put("existMonth", monthFilePath);
				}else{
					json.put("existMonth", null);
				}
				String dayFile = "";
				for (int i = 1; i <= days; i++) {
					dayFile = "" + i;
					if (i < 10) {
						dayFile = "0" + i;
					}
					// 查询当日有无对账单
					String dayFilePath = "batchTask"+ File.separator + id + File.separator+"pos"+ File.separator + year + ""
							+ monthFile + File.separator+ "day" +File.separator + year + "" + monthFile + ""
							+ dayFile;
					list = ufsClient.list(dayFilePath);
					if (list != null && !list.isEmpty())
						arr.add(i - 1, "1");
					else {
						arr.add(i - 1, "-1");
					}
				}
			} catch (Exception e) {
				logger.error(id+"method JSONObject searchAll1 Exception", e);
			}
			json.put("year", year);
			json.put("month", month);
			json.put("days", days);
			json.put("arr", arr.toString());
			return json;
		}

		// 计算指定年月天数
		private int getDaysByYearMonth(Integer year, Integer month) {
			Calendar a = Calendar.getInstance();
			a.set(Calendar.YEAR, year);
			a.set(Calendar.MONTH, month - 1);
			a.set(Calendar.DATE, 1);
			a.roll(Calendar.DATE, -1);
			int maxDate = a.get(Calendar.DATE);
			return maxDate;
		}

		/**
		 * 下载结算对账单
		 */
		@RequestMapping(value = "/my/{year}/{month}/{day}/possettlement-bill-download.htm", method = { RequestMethod.GET })
		public void download(@PathVariable int year, @PathVariable int month,
				@PathVariable int day, HttpServletRequest req,
				HttpServletResponse resp) throws Exception {
			String id = "";
			try {
				String filePath = "";
				String localPath="";
				// 获取当前商户id
				EnterpriseMember user = getUser(req);
				if(user==null){
					logger.info("user is null");
				}
				id = user.getMemberId();
				String monthFile = "";
				String dayFile = "";
				if (month < 10) {
					monthFile = "0" + month;
				} else {
					monthFile = "" + month;
				}
				String folder="";
				if (day == 0) {
					filePath ="batchTask" +File.separator+ id + File.separator+ "pos" + File.separator + year + ""
							+ monthFile+File.separator+"month";
					localPath=merchantTradeFilePath+File.separator+"batchTaskTemp" +File.separator+ id + File.separator+ "pos" + File.separator + year + ""
							+ monthFile+File.separator+"month";
					folder="云+交易月结算对账单"+year+monthFile+".rar";
				} else {
					if (day < 10)
						dayFile = "0" + day;
					else
						dayFile = "" + day;
					filePath = "batchTask" +File.separator + id + File.separator + "pos" + File.separator+ year + "" + monthFile
							+ File.separator + "day" +File.separator+year + "" + monthFile + "" + dayFile;
					localPath=merchantTradeFilePath+File.separator+"batchTask" +File.separator + id + File.separator+ "pos" + File.separator + year + "" + monthFile
							+ File.separator + "day" +File.separator+year + "" + monthFile + "" + dayFile;
					folder="云+交易日结算对账单"+year+monthFile+dayFile+".rar";
				}
				//ufs接口获取文件
				List<FileNameInfo> list = ufsClient.list(filePath);
				resp.setContentType("APPLICATION/x-download");
				resp.setHeader("Content-Disposition", "attachment;fileName="
						+ new String(folder.getBytes("gb2312"), "ISO8859-1"));
				OutputStream os = resp.getOutputStream();
				if (list != null && !list.isEmpty()) {
					ZipOutputStream zos = new ZipOutputStream(os);
					zos.setEncoding(System.getProperty("sun.jnu.encoding"));// 设置文件名编码方式
					byte[] buf = new byte[1024];
					for (FileNameInfo f : list) {
						String name = f.getName();
						
						File file=new File(localPath);
						if(!file.isDirectory()){
							file.mkdirs();
						}
						 file=new File(localPath+File.separator+name);
						 if(!file.exists()){
							 OutputStream out=new FileOutputStream(localPath+File.separator+name);
							 downFile(filePath+File.separator+name,out);
							 out.close();
						 }
						InputStream in = new FileInputStream(file);
						if(name.contains("pos")){
							name=name.replace("pos", "云+消费交易对账单");
						}else if(name.contains("refundPos")){
                            name=name.replace("refundPos", "云+消费撤销交易对账单");
                        }
						zos.putNextEntry(new ZipEntry(name));
						int len = 0;
						while ((len = in.read(buf)) != -1) {
							zos.write(buf, 0, len);
						}
						in.close();
						zos.closeEntry();
					}
					zos.close();
					os.close();
					deleteFiles(merchantTradeFilePath+File.separator+"batchTaskTemp"+File.separator+id);
				}else{
					logger.info("list is empty");
				}
			} catch (Exception e) {
				logger.error(id + "download Exception", e);
			}
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
		
		//ufs得到文件
		public String downFile(String fileName, OutputStream output){
	        OutputStreamFileContext ctx = new OutputStreamFileContext(null,fileName, output);
	        try {
	            if (!ufsClient.getFile(ctx))
	                return ufsClient.getLastErrorMessage();            
	        } catch (Throwable e) {
	            logger.error("文件读异常", e);
	        }
			return null;
		}
		
//		//进入结算对账单页面
//		@RequestMapping(value = "/my/new/{type}/{code}/index.htm")
//		public ModelAndView clear(@PathVariable String type,@PathVariable String code,HttpServletRequest req,
//				HttpServletResponse resp) throws Exception {
//			RestResponse restP = new RestResponse();
//			restP.setData(new HashMap<String, Object>());
//			String reslut="";
//			if("get".equals(type)){
//				CacheRespone<String> temp=xuCache.get(code);
//				reslut=temp.toString();
//			}if("del".equals(type)){
//				reslut=xuCache.delete(code)+"";
//			}
//			restP.getData().put("xucache", reslut);
//						
//			return new ModelAndView(CommonConstant.URL_PREFIX
//					+ "/trade/SettlementOfBills", "response", restP);
//		}		
		
		
}
