package org.schabi.newpipe.extractor.services.youtube;

/**
 * Streaming format types used by YouTube in their streams.
 */
public enum DeliveryType {

    /**
     * YouTube's progressive delivery method, which works with HTTP range headers.
     * (Note that official clients use the corresponding parameter instead.)
     *
     * <p>
     * Initialization and index ranges are available to get metadata (the corresponding values
     * are returned in the player response).
     * </p>
     */
    PROGRESSIVE,

    /**
     * YouTube's delivery method for livestreams which uses a sequence parameter to get
     * segments of streams.
     *
     * <p>
     * Each sequence (which can be fetched with the {@code &sq=0} parameter) contains its own
     * metadata (sidx boxes, segment length, ...), which make no need of an initialization
     * segment.
     * </p>
     *
     * <p>
     * Only used for livestreams (ended or running).
     * </p>
     */
    LIVE
}
