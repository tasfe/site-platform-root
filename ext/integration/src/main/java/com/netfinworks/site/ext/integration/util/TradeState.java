package com.netfinworks.site.ext.integration.util;

public class TradeState {
    public static String getTradeState(int stateCode){
        String returnString="";
        switch (stateCode) {
            case 100:
                returnString="待支付";
                break;
            case 110:
                returnString="买家付款提交中";
                break;
            case 111:
                returnString="买家已支付";
                break;
            case 121:
                returnString="金币已支付";
                break;
            case 201:
                returnString="付款成功";
                break;
            case 301:
                returnString="转账成功";
                break;
            case 401:
                returnString="交易完成";
                break;
            case 951:
                returnString="退款成功";
                break;
            case 952:
                returnString="退款失败";
                break;
            case 998:
                returnString="转账失败";
                break;
            case 999:
                returnString="交易关闭";
                break;

            default:
                break;
        }
        return returnString;
    }
    public static String getTradeStateForTrade(int stateCode){
        String returnString="";
        switch (stateCode) {
            case 100:
                returnString="待支付";
                break;
            case 110:
                returnString="买家付款提交中";
                break;
            case 111:
                returnString="买家已支付";
                break;
            case 121:
                returnString="金币已支付";
                break;
            case 201:
                returnString="付款成功";
                break;
            case 301:
                returnString="交易成功";
                break;
            case 401:
                returnString="交易结束";
                break;
            case 951:
                returnString="退款成功";
                break;
            case 952:
                returnString="退款失败";
                break;
            case 999:
                returnString="交易关闭";
                break;

            default:
                break;
        }
        return returnString;
    }

}
