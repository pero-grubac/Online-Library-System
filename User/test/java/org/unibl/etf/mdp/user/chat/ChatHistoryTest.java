package org.unibl.etf.mdp.user.chat;

import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.model.ChatMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatHistoryTest {
    private final ChatHistory history = ChatHistory.getInstance();

    private static String unique(String name) {
        return name + "-" + UUID.randomUUID();
    }

    @Test
    void messagesAreGroupedByConversation() {
        String jane = unique("jane");
        String mike = unique("mike");

        history.addMessage(jane, new ChatMessage(jane, "hi", LocalDateTime.now()));
        history.addMessage(jane, new ChatMessage("me", "hello", LocalDateTime.now()));
        history.addMessage(mike, new ChatMessage(mike, "hey", LocalDateTime.now()));

        assertThat(history.getMessages(jane)).extracting(ChatMessage::getMessage).containsExactly("hi", "hello");
        assertThat(history.getMessages(mike)).hasSize(1);
        assertThat(history.getActiveConversations()).contains(jane, mike);
    }

    @Test
    void unknownConversationIsEmpty() {
        assertThat(history.getMessages(unique("nobody"))).isEmpty();
    }

    @Test
    void listenersHearWhoWroteTheNewMessage() {
        String jane = unique("jane");
        List<String> notified = new ArrayList<>();
        history.addListener(notified::add);

        history.addMessage(jane, new ChatMessage(jane, "hi", LocalDateTime.now()));

        assertThat(notified).contains(jane);
    }
}
