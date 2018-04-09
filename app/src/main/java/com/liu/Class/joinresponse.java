package com.liu.Class;

import java.io.Serializable;

public class joinresponse implements Serializable{

	private Boolean status;
	
	private Boolean changeheading;
	
	private String formationid;
	
	private String prevehid;//前车的id
	
	private String behvehid;//后车的ID
	
	private int membernum;
	
	public void setStatus(Boolean status) {
		this.status = status;
	}
	
	public void setChangeheading(Boolean changeheading) {
		this.changeheading = changeheading;
	}
	
	public void setFormationid(String formationid) {
		this.formationid = formationid;
	}
	
	public void setPrevehid(String prevehid) {
		this.prevehid = prevehid;
	}
	
	public void setBehvehid(String behvehid) {
		this.behvehid = behvehid;
	}
	
	public void setMembernum(int membernum) {
		this.membernum = membernum;
	}
	
	public Boolean getStatus() {
		return status;
	}
	
	public Boolean getChangeheading() {
		return changeheading;
	}
	
	public String getFormationid() {
		return formationid;
	}
	
	public String getPrevehid() {
		return prevehid;
	}
	
	public String getBehvehid() {
		return behvehid;
	}
	
	public int getMembernum() {
		return membernum;
	}
	
}
