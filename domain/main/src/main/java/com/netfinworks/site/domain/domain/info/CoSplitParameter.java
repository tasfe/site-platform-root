package com.netfinworks.site.domain.domain.info;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;


/**
 * <p>分账参数</p>
 *参数详细内容请参考com.netfinworks.tradeservice.facade.model.SplitParameter;
 * @author Zhang Yun
 * @version $Id: SplitParameter.java, v 0.1 13-11-11 下午5:20 zhangyun Exp $
 */
public class CoSplitParameter {
        /** 分账的标签，用来区分是什么类型的分账，比如佣金或者金币 */
        private String splitTag;
        /** 付款方ID */
        private String payerId;
        /** 付款方账户 */
        private String payerAccountNo;
        /** 收款方ID */
        private String payeeId;
        /** 收款方账户 */
        private String payeeAccountNo;
        /** 金额 */
        private Money  amount;
        /** 备注 */
        private String memo;

        public String getPayerId() {
            return payerId;
        }

        public void setPayerId(String payerId) {
            this.payerId = payerId;
        }

        public String getPayerAccountNo() {
            return payerAccountNo;
        }

        public void setPayerAccountNo(String payerAccountNo) {
            this.payerAccountNo = payerAccountNo;
        }

        public String getPayeeId() {
            return payeeId;
        }

        public void setPayeeId(String payeeId) {
            this.payeeId = payeeId;
        }

        public String getPayeeAccountNo() {
            return payeeAccountNo;
        }

        public void setPayeeAccountNo(String payeeAccountNo) {
            this.payeeAccountNo = payeeAccountNo;
        }

        public Money getAmount() {
            return amount;
        }

        public void setAmount(Money amount) {
            this.amount = amount;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }

        public String getSplitTag() {
            return splitTag;
        }

        public void setSplitTag(String splitTag) {
            this.splitTag = splitTag;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

}
