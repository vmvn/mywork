package com.api.workflow.bean;

import java.io.Serializable;

public class PageBtnInfo implements Serializable{

	private static final long serialVersionUID = -3833572511551700394L;
	private String name;
	private String title;
	private String event;
	
	public PageBtnInfo(){
	}
	
	public PageBtnInfo(String name, String title, String event){
		this.name = name;
		this.title = title;
		this.event = event;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	
}
