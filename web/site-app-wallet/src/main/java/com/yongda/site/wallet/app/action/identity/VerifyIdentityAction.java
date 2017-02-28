package com.yongda.site.wallet.app.action.identity;

import com.netfinworks.basis.inf.ucs.client.CacheRespone;
import com.netfinworks.basis.inf.ucs.memcached.XUCache;
import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.cert.service.request.IdCardOCRReuqest;
import com.netfinworks.cert.service.response.IdCardOCRResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CertType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultCertAppService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.integration.member.AppCertService;
import com.netfinworks.site.ext.integration.member.CertService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.from.checkIdentityRequest;
import com.yongda.site.wallet.app.util.ConvertParamsUtil;
import com.yongda.site.wallet.app.util.ResponseUtil;
import com.yongda.site.wallet.app.util.VerifyRequestParamsUtil;
import com.yongda.site.wallet.app.validator.CommonValidator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/11/7-13:42<br>
 */
@RequestMapping(value = "/verify")
@Controller
public class VerifyIdentityAction extends BaseAction {
    private Logger logger = LoggerFactory.getLogger(VerifyIdentityAction.class);

    @Resource(name = "appCertService")
    private AppCertService appCertService;

    @Resource(name = "commonValidator")
    protected CommonValidator commonValidator;

    @Resource(name = "defaultCertService")
    private DefaultCertService defaultCertService;


    @Resource(name = "defaultCertAppService")
    private DefaultCertAppService defaultCertAppService;

    @Resource(name = "memberService")
    private MemberService memberService;

    @Value("${sendMessageNumber}")
    private String sendMessageNumber;

    @Resource(name = "xuCache")
    private XUCache<String> loginCache;

    @Resource(name = "certService")
    private CertService certService;

    private static final String TOKEN_TYPE = "ocr_token";

    private static final String VALID_DATE_END = "validDateEnd";
    /**
     * 查询身份是否认证   返回true/false
     * @param request
     * @param response
     * @param env
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/identity", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse Identity(HttpServletRequest request,
                                 HttpServletResponse response, OperationEnvironment env) throws IOException
    {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try{
            PersonMember user = getUser(request);
            PersonMember person = new PersonMember();
            person.setLoginName(user.getLoginName());
            person = memberService.queryMemberIntegratedInfo(person, env);
            logger.info("查询身份是否认证,loginName:{}",user.getLoginName());
            //根据认证类型查询认证结果
            String identitySts = PersonalCommonQueryRealName.queryRealName(person.getMemberId(),
                    StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);

            String token   = UUID.randomUUID().toString().replace("-", "");
            loginCache.set(token, user.getMemberId(), 3600);
            Map<String,Object> identityMap = new HashMap<String,Object>();
            identityMap.put("token", token);
            identityMap.put("memberName",user.getLoginName());
            if (ResultStatus.CHECK_PASS.getCode().equals(identitySts)) {
                AuthInfo info = PersonalCommonQueryRealName.queryUserInfo(person.getMemberId(),
                        StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
                //认证名
                identityMap.put("authname", info.getAuthName());
                //认证证件号
                identityMap.put("authNo",info.getAuthNo().toUpperCase());
                restP.setData(identityMap);
                logger.info("会员-{},您的身份已认证.",user.getMemberId());
                restP.setMessage("您的身份已认证");
                restP.setSuccess(true);
                return restP;
            }
            //未认证  返回手机号和token
            identityMap.put("phoneNo",user.getLoginName());
            restP.setData(identityMap);
            restP.setSuccess(true);
            restP.setMessage("您未通过身份认证！");
            logger.info("您未通过身份认证！");
            restP.setCode(CommonDefinedException.NOT_MEMBER_V0.getErrorCode());
        } catch (ServiceException e) {
            logger.error("个人身份认证：" + e.getMessage());
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR,e.getMessage());
        }catch (Exception e) {
            logger.error("个人身份认证：" + e.getMessage());
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR,e.getMessage());
        }
        return restP;
    }

    /**
     * 查询身份证信息  OCR认证
     *
     * @param request
     * @param response
     * @param env
     * @return
     */
    @RequestMapping(value = "id_card_info",method = RequestMethod.POST)
    @ResponseBody
    public RestResponse verifyIdentity(HttpServletRequest request, HttpServletResponse response, OperationEnvironment env) {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            String idCardFrontData = request.getParameter("idCardFrontData");
            String idCardBackData = request.getParameter("idCardBackData");
            restP = VerifyRequestParamsUtil.verifyOcrParams(restP,idCardFrontData,idCardBackData);
            if (!restP.isSuccess())
                return restP;

            PersonMember user = getUser(request);
            IdCardOCRReuqest idCardOCRReuqest = new IdCardOCRReuqest();
            idCardOCRReuqest.setMember_id(user.getMemberId());
            idCardOCRReuqest.setIdCardBackData(idCardBackData);
            idCardOCRReuqest.setIdCardFrontData(idCardFrontData);
            IdCardOCRResponse idcardocrResponse = appCertService.queryIdCardInfo(env, idCardOCRReuqest);
            logger.info("OCR认证响应信息：{}", ConvertParamsUtil.transBeanToMapToString(idcardocrResponse));
            if (idcardocrResponse.getIdBack_type()<0) {
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ID_CARD_VERIFICATION_ERROR, idcardocrResponse.getMessage());
                return restP;
            }

            if (idcardocrResponse.getIdFront_type()<0) {
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ID_CARD_VERIFICATION_ERROR, idcardocrResponse.getMessage());
                return restP;
            }
            String token   = TOKEN_TYPE+UUID.randomUUID().toString().replace("-", "");
            loginCache.set(token, user.getMemberId(), 3600);
            Map<String,Object> resultMap = ConvertParamsUtil.transBeanToMap(idcardocrResponse);
            resultMap.put("token", token);
            restP.setData(resultMap);
            /*session.setAttribute(VALID_DATE_END,StringUtils.isNotBlank(idcardocrResponse.getValid_date_end())?
                    idcardocrResponse.getValid_date_end():null);*/
            logger.info("OCR接口响应信息:{}", resultMap.toString());
            restP.setMessage("OCR识别完成");
        } catch (BizException e) {
            logger.error("", e);
            restP.setCode("" + e.getCode());
            restP.setMessage(e.getMessage());
            restP.setSuccess(false);
        }
        return restP;
    }


    /**
     * 身份认证  请求
     * @param request
     * @param response
     * @param env
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/identity_auth", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse checkIdentity(HttpServletRequest request,
                                      HttpServletResponse response, OperationEnvironment env)
    {
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        Map<String,Object> result = new HashMap<String,Object>();
        try{
            Map<String,Object> map = request.getParameterMap();
            PersonMember user = getUser(request);
            checkIdentityRequest pay = new checkIdentityRequest();
            BeanUtils.populate(pay, map);
            logger.info("提交身份认证申请：{}",pay.toString());
            // 校验提交参数
            String errorMsg = commonValidator.validate(pay);

            if (StringUtils.isNotEmpty(errorMsg)) {
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,errorMsg);
                logger.error("缺少必要的查询参数！" + errorMsg);
                return restP;
            }

            CacheRespone<String> cacherespone = loginCache.get(pay.getToken());
            if(StringUtils.isBlank(cacherespone.get())){
                restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT
                        .getErrorCode(), "token错误或请求未带token");
                return restP;
            }

            PersonMember person = new PersonMember();
            person.setLoginName(user.getLoginName());
            person = memberService.queryMemberIntegratedInfo(person, env);

            //根据认证类型查询认证结果
            String identitySts = PersonalCommonQueryRealName.queryRealName(person.getMemberId(),
                    StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);

            if (!pay.getToken().contains(TOKEN_TYPE) && ResultStatus.CHECK_PASS.getCode().equals(identitySts)) {

                AuthInfo info = PersonalCommonQueryRealName.queryUserInfo(person.getMemberId(),
                        StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
                String authname = info.getAuthName();//认证名
                String authNo = info.getAuthNo();//认证号
                logger.info("身份已认证，校验证件信息,认证姓名：{},证件号：{}",authname,authNo);
                if(!authname.equals(pay.getRealname())){
                    restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"用户名与身份证不一致!");
                    return restP;
                }

                if(!authNo.equalsIgnoreCase(pay.getIdcard())){
                    restP = ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"证件号与身份证不一致!");
                    return restP;
                }
                restP.setMessage("身份认证成功");
                restP.setSuccess(true);
                restP.setData(result);
                return restP;
            }
          /*  try {
                *//**判断身份证有效期**//*
                if(StringUtils.isNotBlank(pay.getValid_date_end())){
                    HttpSession session = request.getSession();
                    String validDateEnd = (String) session.getAttribute(VALID_DATE_END);
                    int resultIndex = validDateEnd.compareTo(pay.getValid_date_end());
                   if (resultIndex < 0 )
                       restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR,"身份证已过期");
                    return restP;
                }
            } catch (Exception e) {
                logger.info("有效期判断出错：{}",e);
            }*/


            AuthInfo realNameInfo = PersonalCommonQueryRealName.queryAuthInfoCheckPass(user.getMemberId(),
                    StringUtils.EMPTY, AuthType.REAL_NAME,defaultCertService,false);

            String real_name_sts = null;
            if ((realNameInfo != null) && (realNameInfo.getResult()!=null)) {
                real_name_sts = realNameInfo.getResult().getCode();
            }

            if (!StringUtils.isBlank(real_name_sts)){
                restP.setSuccess(true);
                restP.setMessage("身份认证成功！");
                logger.info("已做ocr身份认证");
                return restP;
            }

            Boolean status = defaultCertAppService.verifyRealName(
                    person.getMemberId(),StringUtils.isNotBlank(person.getOperatorId())?
                            person.getOperatorId():StringUtils.EMPTY,StringUtils.trim(pay.getRealname()),
                    StringUtils.trim(pay.getIdcard()), "idCard",pay.getValid_date_end(),env,identitySts);

            restP.setSuccess(status);
            restP.setMessage("身份认证成功！");
            logger.info("国政通身份认证成功");
            restP.setData(result);
            loginCache.delete(pay.getToken());
        } catch (ServiceException e) {
            logger.error("个人身份认证：" + e.getMessage());
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR,e.getMessage());
        }catch (Exception e) {
            restP = ResponseUtil.buildExpResponse(CommonDefinedException.SYSTEM_ERROR,e.getMessage());
        }
        return restP;
    }
}
