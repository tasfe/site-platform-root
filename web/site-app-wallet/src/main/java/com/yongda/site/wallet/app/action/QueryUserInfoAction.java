package com.yongda.site.wallet.app.action;

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.util.ConvertParamsUtil;
import com.yongda.site.wallet.app.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/11/3-10:07<br>
 */
@RequestMapping(value = "/query")
@Controller
public class QueryUserInfoAction extends BaseAction {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "memberService")
    private MemberService commMemberService;

    @Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;

    @Resource(name = "defaultCertService")
    private DefaultCertService defaultCertService;

    @Resource(name = "defaultAccountService")
    private DefaultAccountService defaultAccountService;

    @RequestMapping(value = "user_info")
    @ResponseBody
    public RestResponse getUserInfo(HttpServletRequest request,
                                    HttpServletResponse response, OperationEnvironment env){
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            PersonMember user = getUser(request);
            logger.info("[get..user_info..request]");
            Map<String,Object> resultMap = ConvertParamsUtil.transBeanToMap(user);
            //获取个人用户绑定的号码
            String mobile = null;
            EncryptData encryptData = commMemberService.decipherMember(user.getMemberId(),
                    DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
            if (encryptData != null) {
                mobile = encryptData.getPlaintext();
            }

            //查询实名认证等级
            //String level = defaultMemberService.getMemberVerifyLevel(user.getMemberId(), env);
            AuthInfo info = PersonalCommonQueryRealName.queryauthInfoReq(user.getMemberId(),
                    StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);

            AuthInfo realNameInfo = PersonalCommonQueryRealName.queryAuthInfoCheckPass(user.getMemberId(),
                    StringUtils.EMPTY, AuthType.REAL_NAME,defaultCertService,false);

            //查询余额
            MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),
                    env);
            user.setBankCardCount(user.getBankCardCount() - 1);
            resultMap.put("accountBalance", account.getAvailableBalance());
            resultMap.put("balance",account.getBalance());//账号总金额
            resultMap.put("idcard_no", info.getAuthNo()!=null?info.getAuthNo().toUpperCase():null);//实名认证后的身份证号码 最后一位是字母转换成大写
            resultMap.put("authName",info.getAuthName());//实名认证的姓名
            resultMap.put("ocr_certify",realNameInfo.getAuthNo()!=null?"yes":"no");//ocr是否认证通过
           /* try {
                //查询身份证有效期
                if (realNameInfo.getExtension()!=null) {
                    String extensionExt  = realNameInfo.getExtension();
                    JSONObject objectExt = JSONObject.fromObject(extensionExt);
                    String validDateEnd  = (String) objectExt.get("validDateEnd");
                    if (!StringUtils.isBlank(validDateEnd)){
                        Date DateEnd = DateUtils.parseDate(validDateEnd);
                        resultMap.put("pastFlag",DateUtils.compareDate(DateEnd)==-1?"Y":"N");
                    }else
                        resultMap.put("pastFlag","N");
                }
            } catch (ParseException e) {
                logger.error("查询错误：{}",e);
            }*/
            //性别
            if (StringUtils.isNotBlank(info.getAuthNo())){
                String id_card = info.getAuthNo();
                int     num    = Integer.parseInt(id_card.substring(id_card.length()-2,id_card.length()-1));
                resultMap.put("gender", num % 2 == 1?"男":"女");
            }else
                resultMap.put("gender",StringUtils.EMPTY);
            resultMap.put("verify_mobile", mobile);//绑定的手机号
            restP.setData(resultMap);
            restP.setMessage("查询成功");
            logger.info("接口响应信息：{}",resultMap.toString());
        } catch (Exception e) {
            logger.error("",e);
            restP.setSuccess(false);
            restP.setMessage("操作失败:"+e.getMessage());
        }
        return restP;
    }

    /**
     * 查询默认账号的余额
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "find_Balance",method = RequestMethod.POST)
    @ResponseBody
    public RestResponse findAccountBalance(HttpServletRequest request,
                                           HttpServletResponse response, OperationEnvironment env){
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            logger.info("[查询余额....start]");
            PersonMember user = getUser(request);
            //查询余额
            MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(),
                    env);
            user.setBankCardCount(user.getBankCardCount() - 1);
            Map<String,Object> resultMap = new HashMap<String, Object>();
            resultMap.put("mobile", user.getMobile());
            resultMap.put("accountBalance", account.getAvailableBalance());//可用余额
            resultMap.put("balance",account.getBalance());//账号总金额
            restP.setData(resultMap);
            logger.info("接口响应数据：{}",resultMap.toString());
            restP.setMessage("查询成功");
        } catch (ServiceException e) {
            logger.error("",e);
            restP.setSuccess(false);
            restP.setMessage("操作失败:"+e.getMessage());
        }
        return restP;
    }

}
