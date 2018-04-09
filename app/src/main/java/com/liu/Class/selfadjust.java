package com.liu.Class;

import java.io.Serializable;

public class selfadjust implements Serializable{

	private String prevehid;
	
	private Boolean changehead;

	public String getPrevehid() {
		return prevehid;
	}

	public void setPrevehid(String prevehid) {
		this.prevehid = prevehid;
	}

	public Boolean getChangehead() {
		return changehead;
	}

	public void setChangehead(Boolean changehead) {
		this.changehead = changehead;
	}
	
	
}
