package com.lwansbrough.RCTCamera;

import java.security.SecureRandom;

public class ObjectIDGenerator {

    private static final int NUM_BYTES_IN_MONGO_ID = 12;
    private static final int APP_IDENTIFIER = 238;
    private final SecureRandom secureRandom;

    public static String nextID() {
        return new ObjectIDGenerator().generateID();
    }

    public ObjectIDGenerator() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a unique String of a certain number of bytes.
     * @param numBytes is the number of bytes to make the response.
     * @return a unique {@link String} of length numBytes.
     */
    public final String generateID(final int numBytes) {
        if (numBytes >= 8) {
            int unixTime = (int)(System.currentTimeMillis() / 1000);
            final byte[] bytes = new byte[numBytes];
            final StringBuilder stringBuilder = new StringBuilder();
            this.secureRandom.nextBytes(bytes);
            bytes[0] = (byte) (unixTime >> 24);
            bytes[1] = (byte) (unixTime >> 16);
            bytes[2] = (byte) (unixTime >> 8);
            bytes[3] = (byte) unixTime;
            bytes[numBytes - 1] = (byte) APP_IDENTIFIER;
            for (final byte b : bytes) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } else {
            throw new IllegalArgumentException("This method only valid when numBytes >= 8");
        }

    }

    public final String generateID() {
        return this.generateID(NUM_BYTES_IN_MONGO_ID);
    }

}