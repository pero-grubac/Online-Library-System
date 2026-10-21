package org.unibl.etf.mdp.discoveryserver.server;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryTest {
    /** A clock the test moves by hand. */
    static class ManualClock extends Clock {
        Instant now = Instant.parse("2024-03-15T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final ManualClock clock = new ManualClock();
    private final Registry registry = new Registry(Duration.ofSeconds(30), clock);

    @Test
    void entryExpiresWithoutHeartbeat() {
        registry.register("BookSupplier_A", 5000);
        clock.advance(Duration.ofSeconds(29));
        assertThat(registry.active()).containsKey("BookSupplier_A");

        clock.advance(Duration.ofSeconds(2));
        assertThat(registry.active()).isEmpty();
    }

    @Test
    void heartbeatKeepsTheEntryAlive() {
        registry.register("BookSupplier_A", 5000);
        for (int i = 0; i < 10; i++) {
            clock.advance(Duration.ofSeconds(10));
            registry.register("BookSupplier_A", 5000);
        }

        assertThat(registry.active()).containsEntry("BookSupplier_A", 5000);
    }

    @Test
    void onlyTheSilentEntryIsDropped() {
        registry.register("BookSupplier_A", 5000);
        clock.advance(Duration.ofSeconds(20));
        registry.register("BookSupplier_B", 5001);
        clock.advance(Duration.ofSeconds(20));

        assertThat(registry.active()).containsOnlyKeys("BookSupplier_B");
    }

    @Test
    void unregisterRemovesAtOnce() {
        registry.register("BookSupplier_A", 5000);

        assertThat(registry.unregister("BookSupplier_A")).isTrue();
        assertThat(registry.unregister("BookSupplier_A")).isFalse();
        assertThat(registry.active()).isEmpty();
    }

    @Test
    void liveNameOnAnotherPortIsRefusedUntilItExpires() {
        assertThat(registry.register("BookSupplier_A", 5000)).isTrue();
        assertThat(registry.register("BookSupplier_A", 5000)).isTrue();
        assertThat(registry.register("BookSupplier_A", 6000)).isFalse();
        assertThat(registry.active()).containsEntry("BookSupplier_A", 5000);

        clock.advance(Duration.ofSeconds(31));
        assertThat(registry.register("BookSupplier_A", 6000)).isTrue();
        assertThat(registry.active()).containsEntry("BookSupplier_A", 6000);
    }

    @Test
    void nameIsFreeAgainAfterUnregister() {
        registry.register("BookSupplier_A", 5000);
        registry.unregister("BookSupplier_A");

        assertThat(registry.register("BookSupplier_A", 6000)).isTrue();
    }

    @Test
    void listIsSortedByName() {
        registry.register("c", 3);
        registry.register("a", 1);
        registry.register("b", 2);

        assertThat(registry.active().keySet()).containsExactly("a", "b", "c");
    }
}
