package com.netfinworks.site.domainservice.audit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.site.core.dal.daointerface.AuditDAO;
import com.netfinworks.site.core.dal.dataobject.AuditDO;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.trade.AuditListQuery;
import com.netfinworks.site.domainservice.convert.AuditConvert;


public class AuditServiceImpl {

    private static Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Resource(name = "auditDAO")
    private AuditDAO auditDAO;

    public String addAudit(Audit audit) {
        logger.debug("addAudit:",audit);
        AuditDO record = AuditConvert.to(audit);
        auditDAO.insertSelective(record);
        return record.getId();
    }

    public boolean updAudit(Audit audit) {
        logger.debug("updAudit:",audit);
        AuditDO record = AuditConvert.to(audit);
        int count = auditDAO.updateByPrimaryKeySelective(record);
        return count>0 ? true:false;
    }

    public Audit queryAudit(String id) {
        logger.debug("queryAudit:",id);
        AuditDO AuditDO = auditDAO.selectByPrimaryKey(id);
        if(AuditDO!=null)
            return AuditConvert.from(AuditDO);
        else
            return null;
    }

    public List<Audit> queryAuditList(AuditListQuery query) {
        logger.debug("queryAuditList:{}",query);
        Map<String, Object> params = new HashMap<String, Object>();
        List<Audit> AuditList = new ArrayList<Audit>();
        QueryBase page = query.getQueryBase();
        if(page!=null){
            params.put("startRow", page.getStartRow());
            params.put("endRow", page.getEndRow());
            params.put("pageSize", page.getPageSize());
        }
        params.put("order", true);
        params.put("startDate", query.getStartDate());
        params.put("endDate", query.getEndDate());
        params.put("memberId", query.getMemberId());
        params.put("auditType", query.getAuditType());
        params.put("status", query.getStatus());
        params.put(query.getSelectType(),query.getValue());//设置查询条件
        params.put("queryByTime", query.getQueryByTime());//按时间段查询 申请时间、审核时间
        if(query.getAuditSubType()!=null){
            params.put("auditSubType", query.getAuditSubType());
        }
        if(query.getTransId()!=null){
        	params.put("tranVoucherNo", query.getTransId());
        }
        List<AuditDO> list = auditDAO.query(params);

        if(list!=null && list.size()>0){
            Audit Audit;
            for(int i=0;i<list.size();i++){
                Audit = AuditConvert.from(list.get(i));
                AuditList.add(Audit);
            }
        }
        return AuditList;
    }
    
    public int count(AuditListQuery query) {
        logger.debug("count:{}",query);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("startDate", query.getStartDate());
        params.put("endDate", query.getEndDate());
        params.put("memberId", query.getMemberId());
        params.put("auditType", query.getAuditType());
        params.put("status", query.getStatus());
        params.put("queryByTime", query.getQueryByTime());
        params.put(query.getSelectType(), query.getValue());
        params.put("auditSubType", query.getAuditSubType());
        return auditDAO.count(params);
    }
}
