package com.liu.Class;

import java.io.Serializable;

public class leave implements Serializable{

	private String leaveId;
	
	private int position;

	public String getLeaveId() {
		return leaveId;
	}

	public void setLeaveId(String leaveId) {
		this.leaveId = leaveId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	
}
