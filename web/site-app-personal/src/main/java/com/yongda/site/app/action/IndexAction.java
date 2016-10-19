package com.yongda.site.app.action;

import java.text.MessageFormat;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netfinworks.basis.service.api.BannerFacade;
import com.netfinworks.basis.service.api.FaqFacade;
import com.netfinworks.basis.service.domain.BannerVo;
import com.netfinworks.basis.service.domain.FaqTypeVo;
import com.netfinworks.basis.service.domain.RowVo;
import com.netfinworks.basis.service.enums.FaqBizEnum;
import com.netfinworks.basis.service.response.Response;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.ufs.client.UFSClient;
import com.yongda.site.app.WebDynamicResource;
import com.yongda.site.app.util.ResponseUtil;

@Controller
@RequestMapping("/index")
public class IndexAction {
	private Logger log = LoggerFactory.getLogger(IndexAction.class);
	@Resource(name = "ufsClient")
    private UFSClient           ufsClient;
	@Resource(name="bannerFacade")
	private BannerFacade bannerFacade;
	
	@Resource(name="faqFacade")
	private FaqFacade faqFacade;
	
	@Resource(name="webResource")
	private WebDynamicResource webResource;
	
	
	@RequestMapping(value="banners/{platform}" , method =RequestMethod.GET)
	@ResponseBody
	public RestResponse getBanners(HttpServletRequest request, HttpServletResponse response,OperationEnvironment env
			,@PathVariable(value="platform") String plateformType){
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		//由接口获得  图片路径list
		Response<RowVo<BannerVo>> bannerResponse=bannerFacade.getBannerList(plateformType);
    	//遍历url
		if(CollectionUtils.isNotEmpty(bannerResponse.getBean().getRows()))
			for(BannerVo banner:bannerResponse.getBean().getRows()){
				banner.setContent(StringEscapeUtils.unescapeHtml(banner.getContent()));
				banner.setImage(MessageFormat.format("{0}/image?fileName={1}",webResource.getWalletH5Address() ,banner.getImage()));
			}
		restP.getData().put("banners", bannerResponse.getBean().getRows());
    	return restP;
	}
	
	@RequestMapping(value="faq/{bizType}" , method =RequestMethod.GET)
	@ResponseBody
	public RestResponse getFaq(HttpServletRequest request, HttpServletResponse response,OperationEnvironment env
			,@PathVariable(value="bizType") String bizType){
		RestResponse restP = ResponseUtil.buildSuccessResponse();
		if(!validFaqBiz(bizType))
			restP.getData().put("faqs", null);
		else{
			Response<RowVo<FaqTypeVo>> resp = faqFacade.getFaqList(bizType);
			if(resp != null && resp.getBean() != null && CollectionUtils.isNotEmpty(resp.getBean().getRows())){
				restP.getData().put("faqs", resp.getBean().getRows());
			}else{
				restP.getData().put("faqs", null);
			}
		}
    	return restP;
	}
	
	boolean validFaqBiz(String bizType){
		if(bizType == null) return false;
		for(FaqBizEnum e:FaqBizEnum.values()){
			if(e.getCode().equals(bizType)){
				return true;
			}
		}
		return false;
	}
}
