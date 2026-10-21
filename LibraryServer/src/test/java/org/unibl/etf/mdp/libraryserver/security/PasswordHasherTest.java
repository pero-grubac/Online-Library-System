package org.unibl.etf.mdp.libraryserver.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {
    private final PasswordHasher hasher = new PasswordHasher(1000);

    @Test
    void hashVerifiesOnlyTheRightPassword() {
        String stored = hasher.hash("correct horse");

        assertThat(hasher.verify("correct horse", stored)).isTrue();
        assertThat(hasher.verify("correct horsE", stored)).isFalse();
        assertThat(hasher.verify("", stored)).isFalse();
        assertThat(hasher.verify(null, stored)).isFalse();
    }

    @Test
    void hashNeverContainsThePasswordAndIsSaltedPerCall() {
        String first = hasher.hash("secret");
        String second = hasher.hash("secret");

        assertThat(first).startsWith("pbkdf2-sha256$1000$").doesNotContain("secret");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void iterationCountIsReadFromTheStoredHash() {
        String oldHash = new PasswordHasher(500).hash("secret");

        assertThat(new PasswordHasher(2000).verify("secret", oldHash)).isTrue();
    }

    @Test
    void plainTextAndDamagedValuesAreNotHashes() {
        assertThat(PasswordHasher.isHash("john")).isFalse();
        assertThat(hasher.verify("john", "john")).isFalse();
        assertThat(hasher.verify("x", "pbkdf2-sha256$abc$!!$??")).isFalse();
        assertThat(hasher.verify("x", "pbkdf2-sha256$1000")).isFalse();
    }
}
