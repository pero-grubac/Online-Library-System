package org.unibl.etf.mdp.common;

import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.model.Proposal;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Dispatch and de-duplication; datagrams are fed in directly, so no multicast network is needed. */
class GroupBusTest {

    @Test
    void datagramsReachTheHandlerOfTheirType() {
        GroupBus bus = new GroupBus("230.0.0.1", 20000);
        List<String> chat = new CopyOnWriteArrayList<>();
        List<String> proposals = new CopyOnWriteArrayList<>();
        bus.on("GROUP_MESSAGE", m -> chat.add(m.getBody(ChatMessage.class).getMessage()));
        bus.on("PROPOSAL", m -> proposals.add(m.getBody(Proposal.class).getTitle()));

        byte[] message = Json.toBytes(new Message("GROUP_MESSAGE", "ana",
                new ChatMessage("ana", "hello", LocalDateTime.now())));
        byte[] proposal = Json.toBytes(new Message("PROPOSAL", "ana", new Proposal("ana", "Ulysses", "Joyce", "")));
        bus.handle(message, message.length);
        bus.handle(proposal, proposal.length);
        byte[] unknown = Json.toBytes(new Message("SOMETHING_ELSE"));
        bus.handle(unknown, unknown.length);

        assertThat(chat).containsExactly("hello");
        assertThat(proposals).containsExactly("Ulysses");
    }

    @Test
    void garbageDatagramIsIgnored() {
        GroupBus bus = new GroupBus("230.0.0.1", 20000);
        AtomicInteger calls = new AtomicInteger();
        bus.on("GROUP_MESSAGE", m -> calls.incrementAndGet());
        byte[] garbage = "not json at all".getBytes(StandardCharsets.UTF_8);

        bus.handle(garbage, garbage.length);

        assertThat(calls).hasValue(0);
    }

    @Test
    void ownMessageLoopedBackIsShownOnce() {
        GroupChatHistory history = new GroupChatHistory();
        AtomicInteger notifications = new AtomicInteger();
        history.addListener(notifications::incrementAndGet);
        ChatMessage sent = new ChatMessage("ana", "hello", LocalDateTime.now());

        assertThat(history.addMessage(sent)).isTrue();
        // The copy that comes back over the network
        assertThat(history.addMessage(Json.fromJson(Json.toJson(sent), ChatMessage.class))).isFalse();

        assertThat(history.getMessages()).hasSize(1);
        assertThat(notifications).hasValue(1);
    }

    @Test
    void answerReplacesTheOpenProposalAndIsNotUndone() {
        ProposalBoard board = new ProposalBoard();
        Proposal open = new Proposal("ana", "Ulysses", "Joyce", "");
        Proposal answer = open.answered(true, "Yes", "Library");

        assertThat(board.update(open)).isTrue();
        assertThat(board.update(open)).isFalse();
        assertThat(board.update(answer)).isTrue();
        // A late copy of the open proposal must not hide the answer
        assertThat(board.update(open)).isFalse();

        assertThat(board.getProposals()).singleElement().satisfies(p -> assertThat(p.getAccepted()).isTrue());
    }
}
