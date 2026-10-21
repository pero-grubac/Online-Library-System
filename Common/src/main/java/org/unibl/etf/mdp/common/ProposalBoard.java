package org.unibl.etf.mdp.common;

import org.unibl.etf.mdp.model.Proposal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Book proposals by id, newest state wins: an answer replaces the open proposal it answers. */
public class ProposalBoard {
    private final Map<String, Proposal> proposals = new LinkedHashMap<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /** @return false when nothing changed (a duplicate, or an open copy arriving after the answer) */
    public boolean update(Proposal proposal) {
        if (proposal == null || proposal.getId() == null) {
            return false;
        }
        synchronized (proposals) {
            Proposal known = proposals.get(proposal.getId());
            if (known != null && (known.isAnswered() || !proposal.isAnswered())) {
                return false;
            }
            proposals.put(proposal.getId(), proposal);
        }
        listeners.forEach(Runnable::run);
        return true;
    }

    public List<Proposal> getProposals() {
        synchronized (proposals) {
            return new ArrayList<>(proposals.values());
        }
    }
}
