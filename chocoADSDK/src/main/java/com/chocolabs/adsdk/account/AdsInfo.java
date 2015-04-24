package com.chocolabs.adsdk.account;

public class AdsInfo {
	
	private String	advertisementType;
	private String	advertisementId;
	private String	hashKeyToken;
	private String	accountId;
	private String	lineId;
	private String	positionId;
	private String	category;
	private String	subcategory;
	private String	campaign;
	private String	tag;
	private int		progress;	
	public String getAdvertisementType() {
		return advertisementType;
	}
	public void setAdvertisementType(String advertisementType) {
		this.advertisementType = advertisementType;
	}
	public String getAdvertisementId() {
		return advertisementId;
	}
	public void setAdvertisementId(String advertisementId) {
		this.advertisementId = advertisementId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getLineId() {
		return lineId;
	}
	public void setLineId(String lineId) {
		this.lineId = lineId;
	}
	public String getPositionId() {
		return positionId;
	}
	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getSubcategory() {
		return subcategory;
	}
	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}
	public String getCampaign() {
		return campaign;
	}
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
	public String getHashKeyToken() {
		return hashKeyToken;
	}
	public void setHashKeyToken(String hashKeyToken) {
		this.hashKeyToken = hashKeyToken;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
}
