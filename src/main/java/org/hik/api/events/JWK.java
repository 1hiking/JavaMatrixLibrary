package org.hik.api.events;

import java.util.List;
import java.util.Objects;

/// A JSON Web Key (JWK) representing a symmetric key for cryptographic operations.
///
/// @param kty    The key type. Must be `oct`.
/// @param keyOps The key operations. Must at least contain `encrypt` and `decrypt`.
/// @param alg    The algorithm. Must be `A256CTR`.
/// @param k      The symmetric key, encoded as URL-safe unpadded base64.
/// @param ext    Extractable indicator. Must be `true` (W3C extension).
public record JWK(String kty,
                  List<String> keyOps,
                  String alg,
                  String k,
                  boolean ext) {

    /// A JSON Web Key (JWK) representing a symmetric key for cryptographic operations.
    ///
    /// @param kty    The key type. Must be `oct`.
    /// @param keyOps The key operations. Must at least contain `encrypt` and `decrypt`.
    /// @param alg    The algorithm. Must be `A256CTR`.
    /// @param k      The symmetric key, encoded as URL-safe unpadded base64.
    /// @param ext    Extractable indicator. Must be `true` (W3C extension).
    public JWK {
        Objects.requireNonNull(kty(), "KTY must be set");
        Objects.requireNonNull(alg(), "ALG must be set");
        Objects.requireNonNull(k(), "K must be set");

        Objects.requireNonNull(keyOps, "keyOps must not be null");
        if (!keyOps.contains("encrypt") || !keyOps.contains("decrypt")) {
            throw new IllegalArgumentException("encrypt and decrypt must be contained");
        }
    }
}