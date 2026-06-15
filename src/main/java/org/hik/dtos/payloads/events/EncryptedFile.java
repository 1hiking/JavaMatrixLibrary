package org.hik.dtos.payloads.events;

/**
 * Unsupported record
 */
public record EncryptedFile() {
    /**
     * Unsupported record
     */
    public EncryptedFile() {
        throw new UnsupportedOperationException("Encrypted File information is not yet supported");
    }
}
