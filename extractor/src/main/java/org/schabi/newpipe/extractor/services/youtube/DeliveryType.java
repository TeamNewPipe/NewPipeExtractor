package org.schabi.newpipe.extractor.services.youtube;

/**
 * Streaming format types used by YouTube in their streams.
 *
 * <p>
 * It is different from {@link org.schabi.newpipe.extractor.stream.DeliveryMethod delivery methods}!
 * </p>
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
     * YouTube's OTF delivery method which uses a sequence parameter to get segments of
     * streams.
     *
     * <p>
     * The first sequence (which can be fetched with the {@code &sq=0} parameter) contains all the
     * metadata needed to build the stream source (sidx boxes, segment length, segment count,
     * duration, ...).
     * </p>
     *
     * <p>
     * Only used for videos; mostly those with a small amount of views, or ended livestreams
     * which have just been re-encoded as normal videos.
     * </p>
     */
    OTF,

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
