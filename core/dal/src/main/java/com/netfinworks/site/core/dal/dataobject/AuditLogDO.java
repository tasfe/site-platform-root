package com.netfinworks.site.core.dal.dataobject;

import java.util.Date;

import com.netfinworks.restx.persist.jdbc.DOBase;

/**
 * <p>审核日志DO</p>
 * @author yangshihong
 * @version $Id: AuditLogDO.java, v 0.1 2014年5月21日 下午3:16:06 hdfs Exp $
 */
public class AuditLogDO extends DOBase {

    /**ID */
    private String id;
    
    /**审核ID */
    private String auditId;
    
    /**操作类型 */
    private String operateType;
    
    /**处理结果 */
    private String processResult;
    
    /**创建时间 */
    private Date gmtCreated;
    
    /**更新时间 */
    private Date gmtModified;

    /**审核意见 */
    private String auditRemark;
    
    /**审核员ID*/
    private String auditorId;
    
    /**审核员名称 */
    private String auditorName;
    
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

    /**
     * @return the auditId
     */
    public String getAuditId() {
        return auditId;
    }

    /**
     * @param auditId the auditId to set
     */
    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }

    /**
     * @return the operateType
     */
    public String getOperateType() {
        return operateType;
    }

    /**
     * @param operateType the operateType to set
     */
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    /**
     * @return the processResult
     */
    public String getProcessResult() {
        return processResult;
    }

    /**
     * @param processResult the processResult to set
     */
    public void setProcessResult(String processResult) {
        this.processResult = processResult;
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
     * @return the auditRemark
     */
    public String getAuditRemark() {
        return auditRemark;
    }

    /**
     * @param auditRemark the auditRemark to set
     */
    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
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

}
