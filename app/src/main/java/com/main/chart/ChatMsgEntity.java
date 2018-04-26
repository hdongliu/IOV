package com.main.chart;

/**
 * 一个聊天消息的JavaBean
 * 
 * @author way
 * 
 */
public class ChatMsgEntity {
	private String name;// 消息来自
	private String date;// 消息日期
	private String message;// 消息内容
//	private int img;
	private boolean isComMeg = true;// 是否为收到的消息

	public ChatMsgEntity() {

	}
	//第一个构造函数，含有显示时间的 
	public ChatMsgEntity(String name, String date, String text, boolean isComMsg) {
		super();
		this.name = name;
		this.date = date;
		this.message = text;
		this.isComMeg = isComMsg;
	}
	
	// 第二个构造函数，不含有显示时间的
	public ChatMsgEntity(String name, String text, boolean isComMsg) {
		super();
		this.name = name;
		this.message = text;
		this.isComMeg = isComMsg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean getMsgType() {
		return isComMeg;
	}

	public void setMsgType(boolean isComMsg) {
		isComMeg = isComMsg;
	}

//	public int getImg() {
//		return img;
//	}
//
//	public void setImg(int img) {
//		this.img = img;
//	}
}
