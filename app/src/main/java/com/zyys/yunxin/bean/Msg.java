package com.zyys.yunxin.bean;


import java.io.Serializable;

public class Msg implements Serializable{

	public static final int TYPE_RECEIVED = 0;

	public static final int TYPE_SEND = 1;

	public static final int TYPE_OTHER = 2;

	private String content;


	private int type;


	public Msg(String content, int type) {
		this.content = content;
		this.type = type;
	}


	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Msg{" +
				"content='" + content + '\'' +
				", type=" + type +
				'}';
	}
}
