package com.yongda.site.wallet.app.action.common;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.StreamUtil;
import com.netfinworks.mns.client.INotifyClient;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.enums.BizType;
import com.yongda.site.wallet.app.util.ResponseUtil;
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
 * <br>  发送pos短信
 * 作者： zhangweijie <br>
 * 日期： 2016/12/29-15:00<br>
 */
@Controller
public class SendSmsAction extends  BaseAction{
    @Resource(name = "mnsClient")
    private INotifyClient mnsClient;

    @RequestMapping(value = "/send_sms/pos_pay", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse sendPostSms(HttpServletRequest request,
                                    HttpServletResponse response, OperationEnvironment env){
        RestResponse restP = ResponseUtil.buildSuccessResponse();
        try {
            String payMoney    = request.getParameter("payMoney");
            String phoneNum    = request.getParameter("phoneNum");
            String pay_Time    = request.getParameter("pay_Time");
            Map<String,Object> reqMap = new HashMap<String, Object>();
            reqMap.put("phoneNum",phoneNum);
            reqMap.put("pay_Time",pay_Time);
            reqMap.put("payMoney",payMoney);
            com.netfinworks.mns.client.Response responseResult = mnsClient.sendMobileMsg(phoneNum,
                    BizType.POS_PAY_SMS.getCode(),reqMap);
            logger.info("发送短信响应信息：{}",responseResult.toString());
            if (!responseResult.isSuccess()) {
                logger.info("短信发送失败！：{}",responseResult.getMsg());
                return ResponseUtil.buildExpResponse(responseResult.getCode(),responseResult.getMsg());
            }
            restP.setMessage("发送成功");
            return restP;

        } catch (Exception e) {
            logger.error("",e);
            restP.setMessage(e.getMessage());
            restP.setSuccess(false);
        }
        return restP;
    }
}
