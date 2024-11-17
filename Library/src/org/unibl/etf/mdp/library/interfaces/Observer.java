package org.unibl.etf.mdp.library.interfaces;

import org.unibl.etf.mdp.library.event.Event;

public interface Observer {
	public void onEvent(Event event);
}
