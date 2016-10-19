package com.netfinworks.site.core.dal.daointerface;

import com.netfinworks.site.core.dal.dataobject.PosRequest;
import com.netfinworks.site.core.dal.dataobject.PosTradeDO;

import java.util.List;

public interface PosTradeDOMapper {
    int insert(PosTradeDO record);
    
    int insertSelective(PosTradeDO record);

    List<PosTradeDO> selectByTradeNo(PosRequest req);//订单号查询
    
    List<PosTradeDO> selectByTradeSrcNo(PosRequest req);//原订单号查询
    
    List<PosTradeDO> selectTotalByTradeTime(PosRequest req);//按时间查询
    List<PosTradeDO> selectByTradeTime(PosRequest req);//分页
    
    List<PosTradeDO> selectTotalByTradeType(PosRequest req);//按交易类型查询
    List<PosTradeDO> selectByTradeType(PosRequest req);
    
    List<PosTradeDO> selectTotalByTradeStatus(PosRequest req);//按交易状态查询
    List<PosTradeDO> selectByTradeStatus(PosRequest req);
    
    List<PosTradeDO> selectTotalByTradeAll(PosRequest req);//按类型，状态查询
    List<PosTradeDO> selectByTradeAll(PosRequest req);
    
    List<String> selectTradeType(PosRequest req);//查询交易类型
}