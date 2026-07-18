package org.hik.api.events;

import java.util.Map;

/// Record that implements the extension to [MatrixRoomMessageEvent].
///
/// @param url    the URL to the file.
/// @param key    a [JSON Web Key][JWK]
/// @param hashes the 128-bit unique counter block used by AES-CTR, encoded as unpadded base64.
/// @param iv     a map from an algorithm name to a hash of the ciphertext, encoded as unpadded base64. Clients MUST
/// support the SHA-256 hash, which uses the key sha256.
/// @param v      version of the encrypted attachment’s protocol. Must be v2.
/// @see <a href="https://spec.matrix.org/v1.18/client-server-api/#extensions-to-mroommessage-msgtypes">Specification details over this extension</a>
public record EncryptedFile(String url,
                            JWK key,
                            String iv,
                            Map<String, String> hashes,
                            String v
) {


}

