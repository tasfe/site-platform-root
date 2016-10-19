package com.netfinworks.site.web.action.form;

import java.util.ArrayList;
import java.util.List;

import com.netfinworks.site.web.action.form.KjtTransferForm;
import com.netfinworks.site.web.common.vo.Transfer;
/**
 * 
 * @author tangL
 * @date 2015-02-04
 */
public class TransferVo {
	private KjtTransferForm transForm;
	private List<Transfer> transerfers = new ArrayList<Transfer>();

	public KjtTransferForm getTransForm() {
		return transForm;
	}
	public void setTransForm(KjtTransferForm transForm) {
		this.transForm = transForm;
	}
	public List<Transfer> getTranserfers() {
		return transerfers;
	}
	public void setTranserfers(List<Transfer> transerfers) {
		this.transerfers = transerfers;
	}
	
	
}
