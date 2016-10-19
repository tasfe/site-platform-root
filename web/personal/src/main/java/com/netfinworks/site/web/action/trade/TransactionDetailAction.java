package com.netfinworks.site.web.action.trade;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.domain.trade.TransferInfo;
import com.netfinworks.site.domain.enums.AccountTypeKind;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.enums.TradeTypeRequest;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
/**
 * 查询个人钱包交易记录详情
 * 
 * @param model
 * @param request
 * @return
 */
@Controller
public class TransactionDetailAction extends BaseAction{
	
	@Resource(name = "defaultTradeQueryService")
	private DefaultTradeQueryService defaultTradeQueryService;
	
    @Resource(name = "defaultFundoutService")
    private DefaultFundoutService     defaultFundoutService;

	@Resource(name = "accountService")
	private AccountService accountService;
	
	@Resource(name = "defaultDownloadBillService")
	private DefaultDownloadBillService defaultDownloadBillService;
	
	@Resource(name = "defaultAccountService")
    private DefaultAccountService defaultAccountService;
	
	@Resource(name = "memberService")
    private MemberService memberService;
	
	private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat YmdHmsNoSign = new SimpleDateFormat("yyyyMMddHHmmss");
	
	protected OperationEnvironment env;
	@RequestMapping(value = "/my/tradeDetail.htm")
	public ModelAndView searchAll(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env,HttpSession session) throws Exception {
 		String refresh = request.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(request);
		}
		RestResponse restP = new RestResponse();

		Map<String, Object> map = new HashMap<String, Object>();
		PersonMember user = getUser(request);
		if (user.getMemberId() == null)
			throw new IllegalAccessException("illegal user error!");
		// 封装交易请求
		TradeRequest tradeRequest = new TradeRequest();
		tradeRequest.setMemberId(user.getMemberId());

		user.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));
		String currentPage = request.getParameter("currentPage");
		// 分页信息
		QueryBase queryBase = new QueryBase();
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		queryBase.setCurrentPage(Integer.valueOf(currentPage));
		tradeRequest.setQueryBase(queryBase);
		String txnType = request.getParameter("txnType");// 查询类型--页面获取
		String orderId = request.getParameter("orderId");
		String tradeType = request.getParameter("tradeType");
		String bizProductCode=request.getParameter("bizProductCode");
		if(TradeType.baoli_loan.getBizProductCode().equals(bizProductCode)||TradeType.baoli_repayment.getBizProductCode().equals(bizProductCode)||TradeType.baoli_withholding.getBizProductCode().equals(bizProductCode)){
		    txnType="7";
		}else if("INSTANT_TRASFER".equals(tradeType)){
		    txnType="2";
		}
		if (txnType == null) {
			txnType = "0";// 默认值为1,<网关交易>
		}else if("7".equals(txnType)||"8".equals(txnType)||"9".equals(txnType)){
		    txnType="7";
		}
		PageResultList page = new PageResultList();
		switch (Integer.valueOf(txnType)) {
		case 0:
			tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));
			tradeRequest.setGmtEnd(new Date());
			tradeRequest.setTradeVoucherNo(orderId);
			// 查询交易记录
			page = defaultTradeQueryService.queryTradeList(
					tradeRequest, env);
			map.put("page", page);
			map.put("mobile", user.getMobileStar());
			map.put("member", user);
			map.put("startDate",
					DateUtil.getWebDateString(tradeRequest.getGmtStart()));
			map.put("endDate",
					DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
			map.put("txnType", txnType);
			restP.setData(map);
			restP.setSuccess(true);
			break;
		case 1:
			tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));
			tradeRequest.setGmtEnd(new Date());
			tradeRequest.setTradeVoucherNo(orderId);
			List<TradeTypeRequest> tradeTypeList = new ArrayList<TradeTypeRequest>();
			tradeTypeList.add(TradeTypeRequest.INSTANT_ACQUIRING);
			tradeTypeList.add(TradeTypeRequest.ENSURE_ACQUIRING);
			tradeTypeList.add(TradeTypeRequest.PREPAY_ACQUIRING);
			tradeTypeList.add(TradeTypeRequest.REFUND_ACQUIRING);
			tradeTypeList.add(TradeTypeRequest.MERGE);
			tradeRequest.setTradeType(tradeTypeList);
			// 查询交易记录
		    page = defaultTradeQueryService.queryTradeList(
					tradeRequest, env);
			map.put("page", page);
			map.put("mobile", user.getMobileStar());
			map.put("member", user);
			map.put("startDate",
					DateUtil.getWebDateString(tradeRequest.getGmtStart()));
			map.put("endDate",
					DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
			map.put("txnType", txnType);
			restP.setData(map);
			restP.setSuccess(true);
			break;
		case 2:// 转账查询
			// 设置查询时间
			tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));
			tradeRequest.setGmtEnd(new Date());
			tradeRequest.setTradeVoucherNo(orderId);
			tradeTypeList = new ArrayList<TradeTypeRequest>();
			tradeTypeList.add(TradeTypeRequest.INSTANT_TRASFER);
			tradeRequest.setTradeType(tradeTypeList);
			tradeRequest.setMemberId(user.getMemberId());

			page = defaultTradeQueryService.queryTradeList(tradeRequest, env);

			map = new HashMap<String, Object>();
			map.put("page", page);
			map.put("mobile", user.getMobileStar());
			map.put("member", user);
			map.put("startDate",
					DateUtil.getWebDateString(tradeRequest.getGmtStart()));
			map.put("endDate",
					DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
			map.put("txnType", txnType);
			restP.setData(map);
			restP.setSuccess(true);
			break;
		case 3://转账到卡
			FundoutQuery fundoutQuery = new FundoutQuery();
			fundoutQuery.setFundoutOrderNo(orderId);
			fundoutQuery.setOrderTimeStart(DateUtil.getDateNearCurrent(-366));
			fundoutQuery.setOrderTimeEnd(new Date());
			fundoutQuery.setMemberId(user.getMemberId());
			fundoutQuery.setAccountNo(user.getDefaultAccountId());
			fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
			fundoutQuery.setProductCode(TradeType.PAY_TO_BANK.getBizProductCode()+","+TradeType.PAY_TO_BANK_INSTANT.getBizProductCode());//代发工资
			PageResultList<Fundout> page1 = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
			List<Fundout> list = page1.getInfos();
			double sum = 0;
			for (Fundout s : list) {
				sum = s.getAmount().getAmount().doubleValue()
						+ s.getFee().getAmount().doubleValue();
			}
			DecimalFormat df = new DecimalFormat("##0.00");
			map = new HashMap<String, Object>();
			map.put("sum", df.format(sum));
			map.put("page", page1);
			map.put("mobile", user.getMobileStar());
			map.put("member", user);
			
			map.put("txnType", txnType);
			restP.setData(map);
			restP.setSuccess(true);
			break;
		case 4://代发工资到账户
			DownloadBillRequest reqInfo = new DownloadBillRequest();
			List<String> TraStatus = new ArrayList<String>();
			List<String> productCode=new ArrayList<String>();
			productCode.add(TradeType.PAYOFF_TO_KJT.getBizProductCode());
			reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-366));
			reqInfo.setEndTime(new Date());
			reqInfo.setTradeVoucherNo(orderId);
			reqInfo.setProductCodes(productCode);
			reqInfo.setMemberId(user.getMemberId());
			QueryBase queryBase4 = new QueryBase();
			queryBase4.setCurrentPage(Integer.parseInt(currentPage));
			reqInfo.setQueryBase(queryBase4);
			page = defaultDownloadBillService.queryTransfer(reqInfo, env);
			map = new HashMap<String, Object>();
			map.put("page", page);
			map.put("mobile", user.getMobileStar());
			map.put("member", user);
			
			map.put("txnType", txnType);
			restP.setData(map);
			restP.setSuccess(true);
			break;
		case 5://代发工资
			fundoutQuery = new FundoutQuery();
			fundoutQuery.setMemberId(user.getMemberId());
			fundoutQuery.setAccountNo(user.getDefaultAccountId());
			fundoutQuery.setProductCode(TradeType.PAYOFF_TO_BANK.getBizProductCode());//代发工资
			page = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
			map = new HashMap<String, Object>();
			map.put("page", page);
			map.put("mobile", user.getMobileStar());
			map.put("member", user);
			map.put("startDate",
					DateUtil.getWebDateString(fundoutQuery.getOrderTimeStart()));
			map.put("endDate",
					DateUtil.getWebDateString(fundoutQuery.getOrderTimeEnd()));
			map.put("txnType", txnType);
			restP.setData(map);
			restP.setSuccess(true);
			break;
		case 7:// 保理代扣查询
            // 设置查询时间
		    reqInfo = new DownloadBillRequest();
		    reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-366));
            reqInfo.setEndTime(new Date());
            reqInfo.setTradeVoucherNo(orderId);
            reqInfo.setMemberId(user.getMemberId());
            queryBase4 = new QueryBase();
            queryBase4.setCurrentPage(Integer.parseInt(currentPage));
            reqInfo.setQueryBase(queryBase4);
            page = defaultDownloadBillService.queryTransfer(reqInfo, env);
            
            List<TransferInfo> baoLiList = page.getInfos();
            this.setJsonAttribute(session, "baoLiList", baoLiList.get(0));
            sum=0;
            for(TransferInfo s:baoLiList){
                sum=s.getTransferAmount().getAmount().doubleValue()+s.getPayerFee().getAmount().doubleValue();
            }
            df = new DecimalFormat("##0.00");
            map.put("sum", df.format(sum));
            String[] accountType=new String[2];
            String buyAccountType = "";
            String sellAccountType = "";
            if(baoLiList.get(0).getExtention() !=null)
            {
                Map<String, Object> data = JSONObject.parseObject(baoLiList.get(0).getExtention());//获取
                String extention=(String)data.get("account_type");
                if(extention !=null){
                    accountType=extention.split(",");
                    buyAccountType=MemberType.getMessage(new Long(accountType[0]));
                    sellAccountType=MemberType.getMessage(new Long(accountType[1]));
                }
            }
//            String sellerId = baoLiList.get(0).getSellId();
//            String buyerId = baoLiList.get(0).getBuyId();
//            String sellAccountType = defaultAccountService.queryAccountByMemberId(sellerId, env);
//            String buyAccountType = defaultAccountService.queryAccountByMemberId(buyerId, env);
            map.put("sellAccountType", sellAccountType);
            map.put("buyAccountType", buyAccountType);
            map.put("page", page);
            map.put("mobile", user.getMobileStar());
            map.put("member", user);
            map.put("startDate",
                    DateUtil.getWebDateString(tradeRequest.getGmtStart()));
            map.put("endDate",
                    DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
            map.put("txnType", txnType);
            restP.setData(map);
            restP.setSuccess(true);
            break;
		default:
			break;
		}
		if(txnType.equals("2")){
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/trade/transactionrecords_transfer_details", "response", restP);
		}else if(txnType.equals("3")){
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/trade/transactionrecords_transferBank_details", "response", restP);
		}else if(txnType.equals("4")){
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/trade/transactionrecords_payoff_details", "response", restP);
		}else if(txnType.equals("7")){
            return new ModelAndView(CommonConstant.URL_PREFIX
                    + "/trade/transactionrecords_baoli_details", "response", restP);
		}else{
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/trade/transactionrecords_trade_details", "response", restP);
		}
	}
	
	/*
     * 下载电子回单
     * */
    @RequestMapping("/downImage.htm")
    public void downImage(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
        response.setContentType("application/x-download");
        response.setHeader("content-disposition", "attachment;filename=" +"kjt" +YmdHmsNoSign.format(new Date())+".jpg");
        OutputStream fOut = null;
        try {  
            fOut = response.getOutputStream();
            String tradeType = request.getParameter("tradeType");
            String flag = request.getParameter("flag");
            BufferedImage buffImg=null;
            if(flag.equals("1")) {//flag=1（转账归集）
               buffImg=createImg_Baoli(request);//tradeType=1（到账户）
            ImageIO.write(buffImg, "jpg", fOut);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
    
    /**
     * 查看电子回单
     * 
     * */
    @RequestMapping("/SingleReceipt.htm")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
        try {
            String flag = request.getParameter("flag");
            ServletOutputStream out = response.getOutputStream();
            BufferedImage buffImg=null;
            if(flag.equals("1")) {//flag=1（保理交易）
                buffImg=createImg_Baoli(request);//tradeType=1（到账户）
            }
            // 创键编码器，用于编码内存中的图象数据。
            ImageIO.write(buffImg, "jpg", out);
            try {
                out.flush();
            } finally {
                out.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    /**
     * 处理一行太长，分成多行显示
     * str:待处理的字符
     * x：对应图片的横坐标
     * y:对应图片的纵坐标
     * g：画笔对象
     * row：每行显示多少字
     * n:行距
     * */
    public void newlines(String str,int x,int y,Graphics g,int row,int n)
    {
        int lenString=str.length();
        for (int i = 0; i <= (lenString/row); i++) {
            if(((i+1)*row)<lenString)
            {
                g.drawString(str.substring(0+(i*row), (i+1)*row),x, y+(n*i));
            }else {
                g.drawString(str.substring(0+(i*row), lenString),x, y+(n*i));
            }
        }
    } 
    
    public BufferedImage createImg_Baoli(HttpServletRequest request) {
        BufferedImage buffImg = null;
        try {
            String sign = request.getParameter("sign");
            HttpSession session = request.getSession();
            String filePath = request.getSession().getServletContext().getRealPath("/") + "/view/main/static";
            FileInputStream is = new FileInputStream(filePath+"/dzd-bl.jpg");
            buffImg = ImageIO.read(is);
            is.close();
            // 得到画笔对象
            Graphics g = buffImg.getGraphics();
            // 设置颜色。
            g.setColor(Color.BLACK);
            // 最后一个参数用来设置字体的大小
            Font textFont = new Font("宋体", Font.PLAIN, 12);
            g.setFont(textFont);
            // 生成回执单号
            TransferInfo baoLiList = this.getJsonAttribute(session, "baoLiList", TransferInfo.class);
            BaseMember BuyAccount=new BaseMember();
            BaseMember SellAccount=new BaseMember();
            String sellerId = baoLiList.getSellId();
            String buyerId = baoLiList.getBuyId();
            BuyAccount=memberService.queryMemberById(buyerId,env);
            SellAccount=memberService.queryMemberById(sellerId,env);
            
            String[] accountType=new String[2];
            String buyAccountType = "";
            String sellAccountType = "";
            if(baoLiList.getExtention() !=null)
            {
                Map<String, Object> data = JSONObject.parseObject(baoLiList.getExtention());//获取
                String extention=(String)data.get("account_type");
                if(extention !=null){
                    accountType=extention.split(",");
                    buyAccountType=MemberType.getMessage(new Long(accountType[0]));
                    sellAccountType=MemberType.getMessage(new Long(accountType[1]));
                }
            }
//            String sellAccountType = defaultAccountService.queryAccountByMemberId(sellerId, env);
//            String buyAccountType = defaultAccountService.queryAccountByMemberId(buyerId, env);
            String orderId=baoLiList.getOrderId()== null ? "" :baoLiList.getOrderId();
            g.drawString(orderId, 125, 77);// 交易流水号
            g.drawString(DateUtil.getNewFormatDateString(baoLiList.getTransferTime()), 458, 77);// 创建时间
            if(sign.equals("trade")){
                String buyerName=baoLiList.getBuyerName() == null ? "" : baoLiList.getBuyerName();
                newlines(buyerName,142,108,g,14,20);// 付款方名称
                g.drawString(buyAccountType,142,170);
                g.drawString(BuyAccount.getLoginName()== null ? "" :BuyAccount.getLoginName() , 142, 150);//付款方账号
                String sellerName=baoLiList.getSellerName() == null ? "" : baoLiList.getSellerName();
                newlines(sellerName,447,108,g,14,20);// 收款人名称
                g.drawString(sellAccountType,447,170);// 收款人名称
                g.drawString(SellAccount.getLoginName()== null ? "" :SellAccount.getLoginName(), 447, 150);// 收款人账号
                g.drawString(digitUppercase(baoLiList.getTransferAmount().getAmount().doubleValue()), 185, 271);// 转账金额大写
                g.drawString("￥" + baoLiList.getTransferAmount(), 485, 271);// 转账金额小写
                if(TradeType.baoli_loan.getBizProductCode().equals(baoLiList.getBizProductCode())){
                    g.drawString("保理放贷", 144, 305);
                }else if(TradeType.baoli_withholding.getBizProductCode().equals(baoLiList.getBizProductCode())){
                    g.drawString("保理代扣", 144, 305);
                }else if(TradeType.baoli_repayment.getBizProductCode().equals(baoLiList.getBizProductCode())){
                    g.drawString("保理还款", 144, 305);
                }
                g.drawString(orderId+"-1", 478, 460);//电子回单号
                String tradeMemo= baoLiList.getTradeMemo()== null ? "" :baoLiList.getTradeMemo();
                newlines(tradeMemo,80,348,g,21,20);// 备注
            }else if(sign.equals("fee")) {
                g.drawString("永达互联网金融信息服务有限公司", 447, 108);// 收款人姓名
                if(baoLiList.getPlamId().equals("1"))//是卖家
                {
                    String sellerName=baoLiList.getSellerName() == null ? "" : baoLiList.getSellerName();
                    newlines(sellerName,142,108,g,14,20);// 付款方名称
                    g.drawString(sellAccountType,447,170);// 收款人名称
                    g.drawString(SellAccount.getLoginName()== null ? "": SellAccount.getLoginName(), 142, 150);// 收款人账号
                    g.drawString(digitUppercase(baoLiList.getPayeeFee().getAmount().doubleValue()), 185, 271);// 转账服务费（大写）
                    g.drawString("￥" + baoLiList.getPayeeFee(), 485, 271);//
                }else {//是买家
                    String buyerName=baoLiList.getBuyerName() == null ? "" : baoLiList.getBuyerName();
                    newlines(buyerName,142,108,g,14,20);// 付款方名称
                    g.drawString(buyAccountType,142,170);
                    g.drawString(BuyAccount.getLoginName()== null ? "" : BuyAccount.getLoginName() , 142, 150);//付款方账号
                    g.drawString(digitUppercase(baoLiList.getPayerFee().getAmount().doubleValue()), 185, 271);// 转账服务费（大写）
                    g.drawString("￥" + baoLiList.getPayerFee(), 485, 271);//
                }
                if(TradeType.baoli_loan.getBizProductCode().equals(baoLiList.getBizProductCode())){
                    g.drawString("保理放贷服务费", 144, 305);
                }else if(TradeType.baoli_withholding.getBizProductCode().equals(baoLiList.getBizProductCode())){
                    g.drawString("保理代扣服务费", 144, 305);
                }else if(TradeType.baoli_repayment.getBizProductCode().equals(baoLiList.getBizProductCode())){
                    g.drawString("保理还款服务费", 144, 305);
                }
                g.drawString(orderId+"-2", 478, 460);//电子回单号
                g.drawString("服务费", 80, 348);//备注
            }
            g.dispose();

            return buffImg;

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return buffImg;
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
}
