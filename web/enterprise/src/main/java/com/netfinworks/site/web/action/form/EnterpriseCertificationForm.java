/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>企业实名认证form</p>
 * @author liangzhizhuang.m
 * @version $Id: EnterpriseCertificationForm.java, v 0.1 2014年6月3日 下午6:53:24 liangzhizhuang.m Exp $
 */
public class EnterpriseCertificationForm implements Serializable {
    private static final long serialVersionUID = 5325161679649149687L;
	@NotNull(message = "corpLicenceName_not_exist")
	private String corpLicenceName; // 企业名称
	@NotNull(message = "licenceNo_not_exist")
	private String corpLicenceNo; // 营业执照号
	private String endDate; // 营业执照期限
	@NotNull(message = "organizeNo_not_exist")
	private String corpOrganizeNo1;// 组织机构代码证号
	private String icpNo; // ICP证（备案）号：
	@NotNull(message = "address_not_exist")
	private String corpAddr1;// 详细地址
	private String site;// 经营网址
	private String scope;// 经营范围
	private String province;// 省
	private String city;// 市
	private String town;// 区/县
	@NotNull(message = "imageUrl1_not_exist")
	private String imageUrl1; // 营业执照副本扫描件地址
	@NotNull(message = "imageUrl2_not_exist")
	private String imageUrl2; // 组织机构代码证扫描件地址
	@NotNull(message = "imageUrl3_not_exist")
	private String imageUrl3; // 税务登记证扫描件地址
	private String imageUrl4;

	private String registration;// 登记证件名称
	private String compType;// 公司类型 0-企业 1-其他

	private String imageUrl5; // 登记证件扫描件
	private String imageUrl6;// 组织机构代码证扫描件

	private String imageUrl4_1;
	private String imageUrl4_2;
	private String imageUrl4_3;
	private String imageUrl4_4;

	public String getCorpLicenceName() {
		return corpLicenceName;
	}

	public void setCorpLicenceName(String corpLicenceName) {
		this.corpLicenceName = corpLicenceName;
	}

	public String getCorpLicenceNo() {
		return corpLicenceNo;
	}

	public void setCorpLicenceNo(String corpLicenceNo) {
		this.corpLicenceNo = corpLicenceNo;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getCorpOrganizeNo1() {
		return corpOrganizeNo1;
	}

	public void setCorpOrganizeNo1(String corpOrganizeNo1) {
		this.corpOrganizeNo1 = corpOrganizeNo1;
	}

	public String getIcpNo() {
		return icpNo;
	}

	public void setIcpNo(String icpNo) {
		this.icpNo = icpNo;
	}

	public String getCorpAddr1() {
		return corpAddr1;
	}

	public void setCorpAddr1(String corpAddr1) {
		this.corpAddr1 = corpAddr1;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getImageUrl1() {
		return imageUrl1;
	}

	public void setImageUrl1(String imageUrl1) {
		this.imageUrl1 = imageUrl1;
	}

	public String getImageUrl2() {
		return imageUrl2;
	}

	public void setImageUrl2(String imageUrl2) {
		this.imageUrl2 = imageUrl2;
	}

	public String getImageUrl3() {
		return imageUrl3;
	}

	public void setImageUrl3(String imageUrl3) {
		this.imageUrl3 = imageUrl3;
	}

	public String getImageUrl4() {
		return imageUrl4;
	}

	public void setImageUrl4(String imageUrl4) {
		this.imageUrl4 = imageUrl4;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}

	public String getCompType() {
		return compType;
	}

	public void setCompType(String compType) {
		this.compType = compType;
	}

	public String getImageUrl5() {
		return imageUrl5;
	}

	public void setImageUrl5(String imageUrl5) {
		this.imageUrl5 = imageUrl5;
	}

	public String getImageUrl6() {
		return imageUrl6;
	}

	public void setImageUrl6(String imageUrl6) {
		this.imageUrl6 = imageUrl6;
	}

	public String getImageUrl4_1() {
		return imageUrl4_1;
	}

	public void setImageUrl4_1(String imageUrl4_1) {
		this.imageUrl4_1 = imageUrl4_1;
	}

	public String getImageUrl4_2() {
		return imageUrl4_2;
	}

	public void setImageUrl4_2(String imageUrl4_2) {
		this.imageUrl4_2 = imageUrl4_2;
	}

	public String getImageUrl4_3() {
		return imageUrl4_3;
	}

	public void setImageUrl4_3(String imageUrl4_3) {
		this.imageUrl4_3 = imageUrl4_3;
	}

	public String getImageUrl4_4() {
		return imageUrl4_4;
	}

	public void setImageUrl4_4(String imageUrl4_4) {
		this.imageUrl4_4 = imageUrl4_4;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
