package org.unibl.etf.mdp.library.event;

public abstract class Event {
	private final long timestamp;

	protected Event() {
		this.timestamp = System.currentTimeMillis();
	}

	public long getTimestamp() {
		return timestamp;
	}
}
