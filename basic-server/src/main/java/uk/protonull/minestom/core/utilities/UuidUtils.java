/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2021 Protonull <protonull@protonmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package uk.protonull.minestom.core.utilities;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Class of utilities relating to UUIDs.
 */
public final class UuidUtils {

    public static final UUID IDENTITY = new UUID(0L, 0L);

    /**
     * Determines whether a given UUID is null or a default 0,0 value.
     *
     * @param uuid The UUID to test.
     * @return Returns true if the given UUID is null or default.
     */
    public static boolean isNullOrIdentity(final UUID uuid) {
        return uuid == null || IDENTITY.equals(uuid);
    }

    /**
     * @param raw The stringified UUID.
     * @return Returns a valid UUID or null.
     */
    public static UUID fromString(final String raw) {
        try {
            return UUID.fromString(raw);
        }
        catch (final Throwable ignored) {
            return null;
        }
    }

    /**
     * Converts a UUID to a byte array.
     *
     * @param uuid The UUID to convert, can be null.
     * @return Returns a 16 byte array representing the UUID.
     */
    public static byte[] uuidToBytes(final UUID uuid) {
        if (isNullOrIdentity(uuid)) {
            return new byte[16];
        }
        // Creating a new ByteBuffer to almost immediately expend it just to get the bytes is a bit extra, but other
        // ways aren't as clean or readable.
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * <p>Converts a given byte array to a UUID.</p>
     *
     * <p>Note: You can provide a byte array of any length, or even null. Null or zero length arrays will return
     * {@link #IDENTITY}. Byte arrays less than 16 elements long will be used with the remainder inferred as zeroed out
     * bytes. With byte arrays larger than 16 elements, only those first 16 elements are considered.</p>
     *
     * @param bytes The byte array to create the UUID from.
     * @return Returns an instance of UUID based on the given bytes.
     */
    public static UUID bytesToUUID(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return IDENTITY;
        }
        if (bytes.length < 16) { // Support shorter UUIDs
            bytes = ArrayUtils.addAll(bytes, new byte[16 - bytes.length]);
        }
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(
                byteBuffer.getLong(),  // Most significant bits
                byteBuffer.getLong()); // Least significant bits
    }

}
