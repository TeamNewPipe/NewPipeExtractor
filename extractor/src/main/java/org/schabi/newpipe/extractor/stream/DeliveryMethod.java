package org.schabi.newpipe.extractor.stream;

/**
 * An enum to represent the different delivery methods of {@link Stream streams} which are returned
 * by the extractor.
 */
public enum DeliveryMethod {

    /**
     * Enum constant which represents the use of the progressive HTTP streaming method to fetch a
     * {@link Stream stream}.
     */
    PROGRESSIVE_HTTP,

    /**
     * Enum constant which represents the use of the DASH adaptive streaming method to fetch a
     * {@link Stream stream}.
     */
    DASH,

    /**
     * Enum constant which represents the use of the HLS adaptive streaming method to fetch a
     * {@link Stream stream}.
     */
    HLS,

    /**
     * Enum constant which represents the use of the SmoothStreaming adaptive streaming method to
     * fetch a {@link Stream stream}.
     */
    SS,

    /**
     * Enum constant which represents the use of a torrent to fetch a {@link Stream stream}.
     */
    TORRENT
}
