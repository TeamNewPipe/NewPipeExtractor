package org.schabi.newpipe.extractor.stream;

/**
 * An enum representing the stream types of stream contents returned by the extractor.
 */
public enum StreamType {

    /**
     * Placeholder to check if the stream type of stream content was checked or not.
     *
     * <p>
     * It doesn't make sense to use this enum constant outside of the extractor as it will never be
     * returned by an {@link org.schabi.newpipe.extractor.Extractor extractor} and is only used
     * internally.
     * </p>
     */
    NONE,

    /**
     * Enum constant to indicate that the stream type of stream content is a live video.
     *
     * <p>
     * Note that contents <strong>may contain audio streams</strong> even if they also contain
     * video streams (video-only or video with audio, depending of the stream/the content/the
     * service).
     * </p>
     */
    VIDEO_STREAM,

    /**
     * Enum constant to indicate that the stream type of stream content is an audio.
     *
     * <p>
     * Note that contents returned as audio streams should not return video streams.
     * </p>
     *
     * <p>
     * So, in order to prevent unexpected behaviors, stream extractors which are returning this
     * stream type for a content should ensure that no video stream is returned for this content.
     * </p>
     */
    AUDIO_STREAM,

    /**
     * Enum constant to indicate that the stream type of stream content is a video.
     *
     * <p>
     * Note that contents <strong>can contain audio live streams</strong> even if they also contain
     * live video streams (so video-only or video with audio, depending on the stream/the content/
     * the service).
     * </p>
     */
    LIVE_STREAM,

    /**
     * Enum constant to indicate that the stream type of stream content is a live audio.
     *
     * <p>
     * Note that contents returned as live audio streams should not return live video streams.
     * </p>
     *
     * <p>
     * To prevent unexpected behavior, stream extractors which are returning this stream type for a
     * content should ensure that no live video stream is returned along with it.
     * </p>
     */
    AUDIO_LIVE_STREAM,

    /**
     * Enum constant to indicate that the stream type of stream content is a video content of an
     * ended live video stream.
     *
     * <p>
     * Note that most of the content of an ended live video (or audio) may be extracted as {@link
     * #VIDEO_STREAM regular video contents} (or {@link #AUDIO_STREAM regular audio contents})
     * later, because the service may encode them again later as normal video/audio streams. That's
     * the case on YouTube, for example.
     * </p>
     *
     * <p>
     * Note that contents <strong>can contain post-live audio streams</strong> even if they also
     * contain post-live video streams (video-only or video with audio, depending of the stream/the
     * content/the service).
     * </p>
     */
    POST_LIVE_STREAM,

    /**
     * Enum constant to indicate that the stream type of stream content is an audio content of an
     * ended live audio stream.
     *
     * <p>
     * Note that most of ended live audio streams extracted with this value are processed as
     * {@link #AUDIO_STREAM regular audio streams} later, because the service may encode them
     * again later.
     * </p>
     *
     * <p>
     * Contents returned as post-live audio streams should not return post-live video streams.
     * </p>
     *
     * <p>
     * So, in order to prevent unexpected behaviors, stream extractors which are returning this
     * stream type for a content should ensure that no post-live video stream is returned for this
     * content.
     * </p>
     */
    POST_LIVE_AUDIO_STREAM,

    /**
     * Enum constant to indicate that the stream type of stream content is a file.
     */
    FILE
}
