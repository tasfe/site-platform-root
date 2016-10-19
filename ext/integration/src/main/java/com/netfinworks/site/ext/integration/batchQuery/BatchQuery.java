package com.netfinworks.site.ext.integration.batchQuery;

/**
 * 批量明细查询对象
 * 
 * @author admin
 * 
 */
public class BatchQuery {

	/**
	 * 批次号
	 */
	private String batchNo;

	/**
	 * 总页数
	 */
	private Integer pageNum;

	/**
	 * 每页条数
	 */
	private Integer pageSize;

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

}
