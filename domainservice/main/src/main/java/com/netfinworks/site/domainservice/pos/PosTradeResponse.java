package com.netfinworks.site.domainservice.pos;

import java.util.List;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.site.core.dal.dataobject.PosTradeDO;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.SummaryInfo;
import com.netfinworks.tradeservice.facade.model.BaseResponse;


public class PosTradeResponse extends BaseResponse {
	
  private List<PosTradeDO>    posList;
  
  /** 企业钱包结算汇总信息 */
  private SummaryInfo         summaryInfo;
  
  /** 分页信息 */
  private QueryBase           queryBase;
  
  private List<String>        tradeType;
  

public List<String> getTradeType() {
	return tradeType;
  }

  public void setTradeType(List<String> tradeType) {
	this.tradeType = tradeType;
  }

  public List<PosTradeDO> getPosList() {
	return posList;
  }

  public void setPosList(List<PosTradeDO> posList) {
	this.posList = posList;
  }

  public SummaryInfo getSummaryInfo() {
	return summaryInfo;
  }

  public void setSummaryInfo(SummaryInfo summaryInfo) {
	this.summaryInfo = summaryInfo;
  }

  public QueryBase getQueryBase() {
	return queryBase;
  }

  public void setQueryBase(QueryBase queryBase) {
	this.queryBase = queryBase;
  }
  
}
