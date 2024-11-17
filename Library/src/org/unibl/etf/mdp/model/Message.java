package org.unibl.etf.mdp.model;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	private String type;
	private String username;
	private Object body;

	public Message(String type) {
		super();
		this.type = type;
	}

	public Message(String type, String username) {
		super();
		this.type = type;
		this.username = username;
	}

	public Message(String type, String username, Object body) {
		super();
		this.type = type;
		this.username = username;
		this.body = body;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "Message [type=" + type + ", username=" + username + ", body=" + body + "]";
	}

}
