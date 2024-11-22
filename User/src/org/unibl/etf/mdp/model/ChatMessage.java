package org.unibl.etf.mdp.model;

import java.time.LocalDateTime;

public class ChatMessage {
	private String username; // Ko šalje poruku
	private String message; // Sadržaj poruke
	private LocalDateTime timestamp; // Vreme slanja

	public ChatMessage(String username, String message, LocalDateTime timestamp) {
		super();
		this.username = username;
		this.message = message;
		this.timestamp = timestamp;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "<" + username + "> " + "[" + timestamp + "] " + message;
	}

}
