package com.netfinworks.site.web.action.rechage;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.OperateeTypeEnum;
import com.netfinworks.site.domainservice.trade.DefaultRechargeService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.util.LogUtil;

/**
 *
 * <p>充值action</p>
 * @author dexter.qin
 * @version $Id: RechargeAction.java, v 0.1 2013-12-24 下午1:43:12 qinde Exp $
 */
@Controller
public class RechargeAction extends BaseAction {

    @Resource(name="defaultRechargeService")
    private DefaultRechargeService defaultRechargeService;

    @Resource(name="webResource")
    private WebDynamicResource webResource;
    /**
     * 进入收银台充值页面
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/recharge.htm", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request, boolean error, ModelMap model,TradeEnvironment env)  throws Exception{
        PersonMember user = getUser(request);
        env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm?link=0101");//充值成功返回首页
        String url = defaultRechargeService.applyRecharge(user, env);
        if (logger.isInfoEnabled()) {
            logger.info(LogUtil.appLog(OperateeTypeEnum.RECHARGE.getMsg(), user, env));
		}
        return new ModelAndView("redirect:" + url);
    }

}
