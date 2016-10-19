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
 * 结算对账单
 *
 */
@Controller
public class SettleBillAction extends BaseAction {
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

	@RequestMapping(value = "/my/old/all-settlement1.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchAll1(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			QueryTradeForm form) throws Exception {
		String txnType=form.getTxnType();//查询类型--页面获取
        if(txnType==null)
        {
        	txnType="1";//默认值为1,<结算对账单>
        }
		EnterpriseMember member = null;
		CoTradeQueryResponse rep = null;
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		restP.setSuccess(false);
		QueryBase queryBase = new QueryBase();
		String currentPage = form.getCurrentPage();
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}

		Map<String, String> errorMap = new HashMap<String, String>();

		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);
		// 1.查询请求：结算对账单流水明细
		CoTradeRequest listSreq = new CoTradeRequest();
		// 2.查询请求：汇总
		CoTradeRequest sumSreq = new CoTradeRequest();		
		String memberId = user.getMemberId();
		// 交易状态
		List<String> tradeStatus = new ArrayList<String>();
		String[] sumTradeStatus = new String[] { "401", "201", "301", "951",
			"901" };//201：付款成功  301：交易成功  401：交易结束  901：处理中(结算成功) 951：退款成功
		// 查询时间
		String queryStartTime = form.getQueryStartTime();
		String queryEndTime = form.getQueryEndTime();
		restP.getData().put("queryStartTime", queryStartTime);
		restP.getData().put("queryEndTime", queryEndTime);

		// 查询会员所以需要的信息
		member = defaultMemberService.queryCompanyMember(user, env);
		member.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
		
		switch (Integer.valueOf(txnType)) {
		case 1://结算对账单
			listSreq.setMemberId(memberId);
			sumSreq.setMemberId(memberId);
			// 分页
			queryBase.setCurrentPage(Integer.valueOf(currentPage));
			listSreq.setQueryBase(queryBase);
			sumSreq.setQueryBase(queryBase);
			// 需要汇总 (汇总请求 querySummary ，不在用 queryList里的汇总，所以这里值为fasle)
			listSreq.setNeedSummary(false);
			sumSreq.setNeedSummary(true);
			// 是否发生过结算
			listSreq.setHasSettled(true);
			sumSreq.setHasSettled(true);
			tradeStatus = Arrays.asList(sumTradeStatus);
			listSreq.setStatus(tradeStatus);
			sumSreq.setStatus(tradeStatus);

			if (StringUtils.isNotBlank(queryStartTime)) {
				listSreq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
				sumSreq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
				listSreq.setBeginTime(DateUtils.parseDate(DateUtils.getDateString()+" 00:00:00"));
				sumSreq.setBeginTime(DateUtils.parseDate(DateUtils.getDateString()+" 00:00:00"));
				queryStartTime = DateUtils.getDateString()+" 00:00";
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				listSreq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
				sumSreq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
			} else {
				listSreq.setEndTime(DateUtils.parseDate(sdf.format(new Date())+" 23:59:59"));
				sumSreq.setEndTime(DateUtils.parseDate(sdf.format(new Date())+" 23:59:59"));
				queryEndTime = sdf.format(new Date())+" 23:59";
			}

			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("fristDay", queryStartTime);
			restP.getData().put("lastDay", queryEndTime);
			// 页面请求Mapping
			restP.getData().put("pageReqMapping", "/my/all-settlement.htm");
			restP.getData().put("txnType", txnType);

			// 查询结算流水、汇总信息
			rep = settlementServiceImpl.queryList(listSreq, sumSreq);

			restP.setSuccess(true);

			if (rep != null) {
				if (null != rep.getBaseInfoList()) {
					for (BaseInfo info : rep.getBaseInfoList()) {
						// 转换支付渠道编码
						info.setPayChannel(PayChannel.getByCode(info.getPayChannel()).getMessage());
					}
				}
				restP.getData().put("settleList", rep.getBaseInfoList());
				restP.getData().put("summary", rep.getSummaryInfo());
				restP.getData().put("page", rep.getQueryBase());
			}
			break;
		case 2://结算服务费
			break;
		case 3://待结算
			listSreq.setMemberId(memberId);
			sumSreq.setMemberId(memberId);
			// 分页
			queryBase.setCurrentPage(Integer.valueOf(currentPage));
			listSreq.setQueryBase(queryBase);
			// 需要汇总 (汇总请求 querySummary ，不在用 queryList里的汇总，所以这里值为fasle)
			listSreq.setNeedSummary(false);
			// 是否发生过结算
			listSreq.setHasSettled(false);
			sumSreq.setHasSettled(false);
			sumTradeStatus = new String[] {"201"};
			tradeStatus = Arrays.asList(sumTradeStatus);
			listSreq.setStatus(tradeStatus);
			sumSreq.setStatus(tradeStatus);

			if (StringUtils.isNotBlank(queryStartTime)) {
				listSreq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
				sumSreq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
			} else {
				listSreq.setBeginTime(DateUtil.getDateNearCurrent(-30));
				sumSreq.setBeginTime(DateUtil.getDateNearCurrent(-30));
				queryStartTime = DateUtils.getDateString()+" 00:00";
			}
			if (StringUtils.isNotBlank(queryEndTime)) {
				listSreq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
				sumSreq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
			} else {
				listSreq.setEndTime(new Date());
				sumSreq.setEndTime(new Date());
				queryEndTime = sdf.format(new Date())+" 23:59";
			}

			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("queryStartTime", queryStartTime);
			restP.getData().put("queryEndTime", queryEndTime);
			// 页面请求Mapping
			restP.getData().put("pageReqMapping", "/my/all-settlement.htm");
			restP.getData().put("txnType", txnType);

			// 查询结算流水、汇总信息
			rep = settlementServiceImpl.queryList(listSreq, sumSreq);

			restP.setSuccess(true);

			if (rep != null) {

				restP.getData().put("settleList", rep.getBaseInfoList());
				restP.getData().put("summary", rep.getSummaryInfo());
				restP.getData().put("page", rep.getQueryBase());
			}	
			break;
		default:
			break;
			}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/SettlementOfBills", "response", restP);
	}
	//****************************************************************************************

	/**
	 * 下载结算对账单
	 */
	@RequestMapping(value = "/my/settlement-bill-download.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public void downloadToExcel(HttpServletRequest req, HttpServletResponse resp,
            boolean error, ModelMap model,QueryTradeForm form)throws Exception {
		String txnType=form.getTxnType();//查询类型--页面获取
        if(txnType==null)
        {
        	txnType="1";//默认值为1,<结算对账单>
        }
		EnterpriseMember member = null;
		CoTradeQueryResponse rep = null;
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		restP.setSuccess(false);
		QueryBase queryBase = new QueryBase();
		String currentPage = "1";
		queryBase.setPageSize(50000);

		Map<String, String> errorMap = new HashMap<String, String>();

		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);
		// 1.查询请求：结算对账单流水明细
		CoTradeRequest listSreq = new CoTradeRequest();
		// 2.查询请求：汇总
		CoTradeRequest sumSreq = new CoTradeRequest();		
		String memberId = user.getMemberId();
		// 交易状态
		List<String> tradeStatus = new ArrayList<String>();
		String[] sumTradeStatus = new String[] { "401", "201", "301", "951",
			"901" };//201：付款成功  301：交易成功  401：交易结束  901：处理中(结算成功) 951：退款成功
		// 查询时间
		String queryStartTime = form.getQueryStartTime();
		String queryEndTime = form.getQueryEndTime();
		restP.getData().put("queryStartTime", queryStartTime);
		restP.getData().put("queryEndTime", queryEndTime);

		// 查询会员所以需要的信息
		member = defaultMemberService.queryCompanyMember(user, env);
		member.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
		
		listSreq.setMemberId(memberId);
		sumSreq.setMemberId(memberId);
		// 分页
		queryBase.setCurrentPage(Integer.valueOf(currentPage));
		listSreq.setQueryBase(queryBase);
		sumSreq.setQueryBase(queryBase);
		// 需要汇总 (汇总请求 querySummary ，不在用 queryList里的汇总，所以这里值为fasle)
		listSreq.setNeedSummary(false);
		sumSreq.setNeedSummary(true);
		// 是否发生过结算
		listSreq.setHasSettled(true);
		sumSreq.setHasSettled(true);
		tradeStatus = Arrays.asList(sumTradeStatus);
		listSreq.setStatus(tradeStatus);
		sumSreq.setStatus(tradeStatus);

		if (StringUtils.isNotBlank(queryStartTime)) {
            listSreq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
            sumSreq.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
        } else {
            listSreq.setBeginTime(DateUtils.parseDate(DateUtils.getDateString()+" 00:00:00"));
            sumSreq.setBeginTime(DateUtils.parseDate(DateUtils.getDateString()+" 00:00:00"));
            queryStartTime = DateUtils.getDateString()+" 00:00";
        }
        if (StringUtils.isNotBlank(queryEndTime)) {
            listSreq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
            sumSreq.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
        } else {
            listSreq.setEndTime(DateUtils.parseDate(sdf.format(new Date())+" 23:59:59"));
            sumSreq.setEndTime(DateUtils.parseDate(sdf.format(new Date())+" 23:59:59"));
            queryEndTime = sdf.format(new Date())+" 23:59";
        }

		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("fristDay", queryStartTime);
		restP.getData().put("lastDay", queryEndTime);
		// 页面请求Mapping
		restP.getData().put("pageReqMapping", "/my/all-settlement.htm");
		restP.getData().put("txnType", txnType);

		// 查询结算流水、汇总信息
		rep = settlementServiceImpl.queryList(listSreq, sumSreq);

		restP.setSuccess(true);

		if (rep != null) {
			restP.getData().put("settleList", rep.getBaseInfoList());
			restP.getData().put("summary", rep.getSummaryInfo());
			restP.getData().put("page", rep.getQueryBase());
		}
		if(rep.getBaseInfoList()==null){
			List<BaseInfo> transList = new ArrayList<BaseInfo>();
			rep.setBaseInfoList(transList);
		}
		ExcelUtil excelUtil = new ExcelUtil();
	    excelUtil.toExcel1(req, resp, rep.getBaseInfoList(),rep.getSummaryInfo(), 1, queryStartTime, queryEndTime);

	}
	
	//**********************************新版结算对账单*****************************************************
	//进入结算对账单页面
		@RequestMapping(value = "/my/all-settlement1.htm", method = { RequestMethod.GET })
		public ModelAndView searchAll(HttpServletRequest req,
				HttpServletResponse resp) throws Exception {
			RestResponse restP = new RestResponse();
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/trade/NewSettlementOfBills", "response", restP);
		}
		
		//生成日期以及是否有下载链接标识
		@RequestMapping(value = "/my/all-settlement1.htm", method = { RequestMethod.POST })
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
				String monthFilePath = "batchTask"+ File.separator +id+ File.separator + year + ""+ monthFile+File.separator+"month";
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
					String dayFilePath = "batchTask"+ File.separator + id + File.separator + year + ""
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
		@RequestMapping(value = "/my/{year}/{month}/{day}/settlement-bill-download.htm", method = { RequestMethod.GET })
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
					filePath ="batchTask" +File.separator+ id + File.separator + year + ""
							+ monthFile+File.separator+"month";
					localPath=merchantTradeFilePath+File.separator+"batchTaskTemp" +File.separator+ id + File.separator + year + ""
							+ monthFile+File.separator+"month";
					folder=year+monthFile+"对账单.rar";
				} else {
					if (day < 10)
						dayFile = "0" + day;
					else
						dayFile = "" + day;
					filePath = "batchTask" +File.separator + id + File.separator + year + "" + monthFile
							+ File.separator + "day" +File.separator+year + "" + monthFile + "" + dayFile;
					localPath=merchantTradeFilePath+File.separator+"batchTask" +File.separator + id + File.separator + year + "" + monthFile
							+ File.separator + "day" +File.separator+year + "" + monthFile + "" + dayFile;
					folder=year+monthFile+dayFile+"对账单.rar";
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
						if(name.contains("gateway")){
							name=name.replace("gateway", "网关交易对账单");
						}else if(name.contains("bank_withholding")){
                            name=name.replace("bank_withholding", "银行卡代扣对账单");
                        }else if(name.contains("refund")){
							name=name.replace("refund", "退款对账单");
						}else if(name.contains("account")){
							name=name.replace("account", "付款到账户对账单");
						}else if(name.contains("bank")){
							name=name.replace("bank", "付款到卡对账单");
						}else if(name.contains("ticket")){
							name=name.replace("ticket", "退票对账单");
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
		
		//进入结算对账单页面
		@RequestMapping(value = "/my/new/{type}/{code}/index.htm")
		public ModelAndView clear(@PathVariable String type,@PathVariable String code,HttpServletRequest req,
				HttpServletResponse resp) throws Exception {
			RestResponse restP = new RestResponse();
			restP.setData(new HashMap<String, Object>());
			String reslut="";
			if("get".equals(type)){
				CacheRespone<String> temp=xuCache.get(code);
				reslut=temp.toString();
			}if("del".equals(type)){
				reslut=xuCache.delete(code)+"";
			}
			restP.getData().put("xucache", reslut);
						
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/trade/SettlementOfBills", "response", restP);
		}		
		
		
}
