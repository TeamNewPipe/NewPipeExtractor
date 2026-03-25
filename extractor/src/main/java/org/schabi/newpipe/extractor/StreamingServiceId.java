package org.schabi.newpipe.extractor;

import java.util.Objects;

public enum StreamingServiceId {
    NO_SERVICE_ID,
    YOUTUBE,
    SOUNDCLOUD,
    MEDIACCC,
    PEERTUBE,
    BANDCAMP;


    private static final StreamingServiceId[] VALUES = values();

    public static String nameFromId(final int serviceId) {
        try {
            return VALUES[Objects.checkIndex(serviceId + 1, VALUES.length)].name();
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid serviceId: " + serviceId, e);
        }
    }
}
