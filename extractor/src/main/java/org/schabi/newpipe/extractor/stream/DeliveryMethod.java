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
     * Enum constant which represents the use of the DASH (Dynamic Adaptive Streaming over HTTP)
     * adaptive streaming method to fetch a {@link Stream stream}.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Dynamic_Adaptive_Streaming_over_HTTP">the
     * Dynamic Adaptive Streaming over HTTP Wikipedia page</a> and <a href="https://dashif.org/">
     * DASH Industry Forum's website</a> for more information about the DASH delivery method
     */
    DASH,

    /**
     * Enum constant which represents the use of the HLS (HTTP Live Streaming) adaptive streaming
     * method to fetch a {@link Stream stream}.
     *
     * @see <a href="https://en.wikipedia.org/wiki/HTTP_Live_Streaming">the HTTP Live Streaming
     * page</a> and <a href="https://developer.apple.com/streaming">Apple's developers website page
     * about HTTP Live Streaming</a> for more information about the HLS delivery method
     */
    HLS,

    /**
     * Enum constant which represents the use of the SmoothStreaming adaptive streaming method to
     * fetch a {@link Stream stream}.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Adaptive_bitrate_streaming
     * #Microsoft_Smooth_Streaming_(MSS)">Wikipedia's page about adaptive bitrate streaming,
     * section <i>Microsoft Smooth Streaming (MSS)</i></a> for more information about the
     * SmoothStreaming delivery method
     */
    SS,

    /**
     * Enum constant which represents the use of a torrent file to fetch a {@link Stream stream}.
     *
     * @see <a href="https://en.wikipedia.org/wiki/BitTorrent">Wikipedia's BitTorrent's page</a>,
     * <a href="https://en.wikipedia.org/wiki/Torrent_file">Wikipedia's page about torrent files
     * </a> and <a href="https://www.bittorrent.org">Bitorrent's website</a> for more information
     * about the BitTorrent protocol
     */
    TORRENT
}
