package com.netfinworks.site.domain.domain.audit;

import java.io.Serializable;
import java.util.Date;

import com.netfinworks.common.util.money.Money;

/**
 * <p>审核domain</p>
 * @author yangshihong
 * @version $Id: Audit.java, v 0.1 2014年5月21日 下午3:17:50 hdfs Exp $
 */
public class Audit implements Serializable {
	private static final long serialVersionUID = -7027909036317971027L;

	/**ID */
    private String id;
    
    /**区分订单是单笔的还是批量的*/
    private String auditSubType;
    
    /**交易统一凭证号 */
    private String tranVoucherNo;
    
    /**支付统一凭证号 */
    private String payVoucherNo;
    
    /**类型 1.withdrawal2.transfer 3.refund */
    private String auditType;
    
    /**金额 */
    private Money amount;
    
    /**会员ID */
    private String memberId;
    
    /**操作员 */
    private String operatorName;
    
    /**操作员ID */
    private String operatorId;
    
    /**审核员 */
    private String auditorName;
    
    /**审核员ID */
    private String auditorId;
    
    /**状态 */
    private String status;
    
    /**待审核交易数据 */
    private String auditData;
    
    /**备注 */
    private String remark;
    
    /**创建时间 */
    private Date gmtCreated;
    
    /**更新时间 */
    private Date gmtModified;
    
    /**手续费  */
    private Money fee;
    
    /**扩展字段 */
    private String ext;
    
    /**收款人 银行卡/账户登录名*/
    private String payeeNo;
    
    /**收款人银行卡信息 （银行，分支行）*/
    private String  payeeBankInfo;
    
    /**提现类型（普通提现、快速提现） */
    private String  fundoutGrade;
    
    /**收款人会员id*/
    private String  payeeMemberId;
    
    /**交易原始凭证号*/
    private String tranSourceVoucherNo;
    
    /** 原交易订单号 */
    private String            origTranVoucherNo;
    
    /**
     * @return the id
     */
    public String getId() {
    return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    public Money getFee() {
        return fee;
    }

    public void setFee(Money fee) {
        this.fee = fee;
    }
    
    public String getPayeeNo() {
		return payeeNo;
	}

	public void setPayeeNo(String payeeNo) {
		this.payeeNo = payeeNo;
	}

	public String getPayeeBankInfo() {
		return payeeBankInfo;
	}

	public void setPayeeBankInfo(String payeeBankInfo) {
		this.payeeBankInfo = payeeBankInfo;
	}

	public String getFundoutGrade() {
		return fundoutGrade;
	}

	public void setFundoutGrade(String fundoutGrade) {
		this.fundoutGrade = fundoutGrade;
	}

	public String getPayeeMemberId() {
		return payeeMemberId;
	}

	public void setPayeeMemberId(String payeeMemberId) {
		this.payeeMemberId = payeeMemberId;
	}

	public String getTranSourceVoucherNo() {
		return tranSourceVoucherNo;
	}

	public void setTranSourceVoucherNo(String tranSourceVoucherNo) {
		this.tranSourceVoucherNo = tranSourceVoucherNo;
	}

	public String getAuditSubType() {
		return auditSubType;
	}

	public void setAuditSubType(String auditSubType) {
		this.auditSubType = auditSubType;
	}

	/**
     * @return the tranVoucherNo
     */
    public String getTranVoucherNo() {
        return tranVoucherNo;
    }

    /**
     * @param tranVoucherNo the tranVoucherNo to set
     */
    public void setTranVoucherNo(String tranVoucherNo) {
        this.tranVoucherNo = tranVoucherNo;
    }

    /**
     * @return the payVoucherNo
     */
    public String getPayVoucherNo() {
        return payVoucherNo;
    }

    /**
     * @param payVoucherNo the payVoucherNo to set
     */
    public void setPayVoucherNo(String payVoucherNo) {
        this.payVoucherNo = payVoucherNo;
    }

    /**
     * @return the auditType
     */
    public String getAuditType() {
        return auditType;
    }

    /**
     * @param auditType the auditType to set
     */
    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    /**
     * @return the amount
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(Money amount) {
        this.amount = amount;
    }

    /**
     * @return the memberId
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * @param memberId the memberId to set
     */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * @return the operatorName
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * @param operatorName the operatorName to set
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * @return the operatorId
     */
    public String getOperatorId() {
        return operatorId;
    }

    /**
     * @param operatorId the operatorId to set
     */
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * @return the auditorName
     */
    public String getAuditorName() {
        return auditorName;
    }

    /**
     * @param auditorName the auditorName to set
     */
    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    /**
     * @return the auditorId
     */
    public String getAuditorId() {
        return auditorId;
    }

    /**
     * @param auditorId the auditorId to set
     */
    public void setAuditorId(String auditorId) {
        this.auditorId = auditorId;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the auditData
     */
    public String getAuditData() {
        return auditData;
    }

    /**
     * @param auditData the auditData to set
     */
    public void setAuditData(String auditData) {
        this.auditData = auditData;
    }

    /**
     * @return the remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark the remark to set
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return the gmtCreated
     */
    public Date getGmtCreated() {
        return gmtCreated;
    }

    /**
     * @param gmtCreated the gmtCreated to set
     */
    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    /**
     * @return the gmtModified
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * @param gmtModified the gmtModified to set
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * @return the ext
     */
    public String getExt() {
        return ext;
    }

    /**
     * @param ext the ext to set
     */
    public void setExt(String ext) {
        this.ext = ext;
    }
    
    public String getOrigTranVoucherNo() {
        return origTranVoucherNo;
    }

    public void setOrigTranVoucherNo(String origTranVoucherNo) {
        this.origTranVoucherNo = origTranVoucherNo;
    }

}