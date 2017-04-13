package com.api.workflow.bean;

import java.io.Serializable;

public class SelectItem implements Serializable{

	private static final long serialVersionUID = -303509456854245418L;
	//private int keyid;
	private int selectvalue;
	private String selectname;
	private int isdefault;
	private String childitemid;
	private int cancel;
	//private int pubid;
	private int isAccordToSubCom;
	private String docCategory;
	private int maxUploadSize;
	
	public int getSelectvalue() {
		return selectvalue;
	}
	public void setSelectvalue(int selectvalue) {
		this.selectvalue = selectvalue;
	}
	public String getSelectname() {
		return selectname;
	}
	public void setSelectname(String selectname) {
		this.selectname = selectname;
	}
	public int getIsdefault() {
		return isdefault;
	}
	public void setIsdefault(int isdefault) {
		this.isdefault = isdefault;
	}
	public String getChilditemid() {
		return childitemid;
	}
	public void setChilditemid(String childitemid) {
		this.childitemid = childitemid;
	}
	public int getCancel() {
		return cancel;
	}
	public void setCancel(int cancel) {
		this.cancel = cancel;
	}
	public int getIsAccordToSubCom() {
		return isAccordToSubCom;
	}
	public void setIsAccordToSubCom(int isAccordToSubCom) {
		this.isAccordToSubCom = isAccordToSubCom;
	}
	public String getDocCategory() {
		return docCategory;
	}
	public void setDocCategory(String docCategory) {
		this.docCategory = docCategory;
	}
	public int getMaxUploadSize() {
		return maxUploadSize;
	}
	public void setMaxUploadSize(int maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}
	
	
}
