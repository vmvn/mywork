package com.api.workflow.bean;

import java.io.Serializable;

public class FileFieldAttr implements Serializable{

	private static final long serialVersionUID = 8747989149464249467L;
	private int imgheight;
	private int imgwidth;
	
	
	public int getImgheight() {
		return imgheight;
	}
	public void setImgheight(int imgheight) {
		this.imgheight = imgheight;
	}
	public int getImgwidth() {
		return imgwidth;
	}
	public void setImgwidth(int imgwidth) {
		this.imgwidth = imgwidth;
	}
	
	
}
