package org.schabi.newpipe.extractor.stream;

/**
 * Enum representing the different streaming protocols.
 */
public enum StreamingProtocol {

    /**
     * Protocol where requests points to a single file, whether they are ranged.
     */
    PROGRESSIVE,

    /**
     * Delivery Adaptive Streaming over HTTP.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Dynamic_Adaptive_Streaming_over_HTTP">
     * https://en.wikipedia.org/wiki/Dynamic_Adaptive_Streaming_over_HTTP</a>
     */
    DASH,

    /**
     * HTTP Live Streaming.
     *
     * @see <a href="https://en.wikipedia.org/wiki/HTTP_Live_Streaming">
     * https://en.wikipedia.org/wiki/HTTP_Live_Streaming</a>
     */
    HLS,

    /**
     * A custom delivery protocol.
     */
    CUSTOM
}
