package com.eldersphere.webpush;

import nl.martijndwars.webpush.Utils;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link VapidKeyProvider}: real deployments must be able to pin an explicit
 * VAPID key pair via env vars, while local dev (no env vars set) must still get a *usable*
 * key pair rather than a startup failure - "usable" meaning it round-trips through this same
 * library's own encode/decode and the public/private halves actually match each other.
 */
class VapidKeyProviderTest {

    @Test
    void configuredKeys_areUsedVerbatimRatherThanGenerated() {
        VapidKeyProvider provider = new VapidKeyProvider(
                "configured-public-key", "configured-private-key", "mailto:ops@eldersphere.app");

        assertThat(provider.getPublicKey()).isEqualTo("configured-public-key");
        assertThat(provider.getPrivateKey()).isEqualTo("configured-private-key");
        assertThat(provider.getSubject()).isEqualTo("mailto:ops@eldersphere.app");
    }

    @Test
    void blankConfiguredKeys_fallsBackToGeneratingAUsableKeyPair() throws Exception {
        VapidKeyProvider provider = new VapidKeyProvider("", "", "mailto:admin@eldersphere.app");

        assertThat(provider.getPublicKey()).isNotBlank();
        assertThat(provider.getPrivateKey()).isNotBlank();
        // Standard VAPID key encoding: base64url, unpadded.
        assertThat(provider.getPublicKey()).doesNotContain("=", "+", "/");
        assertThat(provider.getPrivateKey()).doesNotContain("=", "+", "/");

        PublicKey publicKey = Utils.loadPublicKey(provider.getPublicKey());
        PrivateKey privateKey = Utils.loadPrivateKey(provider.getPrivateKey());
        assertThat(Utils.verifyKeyPair(privateKey, publicKey))
                .as("the generated public/private halves must actually correspond to the same EC key pair")
                .isTrue();
    }

    @Test
    void nullConfiguredKeys_alsoFallsBackToGenerating() {
        VapidKeyProvider provider = new VapidKeyProvider(null, null, "mailto:admin@eldersphere.app");

        assertThat(provider.getPublicKey()).isNotBlank();
        assertThat(provider.getPrivateKey()).isNotBlank();
    }

    @Test
    void noConfiguredKeys_eachInstanceGetsItsOwnFreshKeyPair() {
        VapidKeyProvider first = new VapidKeyProvider(null, null, "mailto:admin@eldersphere.app");
        VapidKeyProvider second = new VapidKeyProvider(null, null, "mailto:admin@eldersphere.app");

        assertThat(first.getPublicKey()).isNotEqualTo(second.getPublicKey());
        assertThat(first.getPrivateKey()).isNotEqualTo(second.getPrivateKey());
    }
}
