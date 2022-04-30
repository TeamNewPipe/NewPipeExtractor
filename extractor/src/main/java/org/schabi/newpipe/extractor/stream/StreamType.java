package org.schabi.newpipe.extractor.stream;

/**
 * An enum representing the stream type of a {@link StreamInfo} extracted by a {@link
 * StreamExtractor}.
 */
public enum StreamType {

    /**
     * Placeholder to check if the stream type was checked or not. It doesn't make sense to use this
     * enum constant outside of the extractor as it will never be returned by an {@link
     * org.schabi.newpipe.extractor.Extractor} and is only used internally.
     */
    NONE,

    /**
     * A normal video stream, usually with audio. Note that the {@link StreamInfo} <strong>can also
     * provide audio-only {@link AudioStream}s</strong> in addition to video or video-only {@link
     * VideoStream}s.
     */
    VIDEO_STREAM,

    /**
     * An audio-only stream. There should be no {@link VideoStream}s available! In order to prevent
     * unexpected behaviors, when {@link StreamExtractor}s return this stream type, they should
     * ensure that no video stream is returned in {@link StreamExtractor#getVideoStreams()} and
     * {@link StreamExtractor#getVideoOnlyStreams()}.
     */
    AUDIO_STREAM,

    /**
     * A video live stream, usually with audio. Note that the {@link StreamInfo} <strong>can also
     * provide audio-only {@link AudioStream}s</strong> in addition to video or video-only {@link
     * VideoStream}s.
     */
    LIVE_STREAM,

    /**
     * An audio-only live stream. There should be no {@link VideoStream}s available! In order to
     * prevent unexpected behaviors, when {@link StreamExtractor}s return this stream type, they
     * should ensure that no video stream is returned in {@link StreamExtractor#getVideoStreams()}
     * and {@link StreamExtractor#getVideoOnlyStreams()}.
     */
    AUDIO_LIVE_STREAM,

    /**
     * A video live stream that has just ended but has not yet been encoded into a normal video
     * stream. Note that the {@link StreamInfo} <strong>can also provide audio-only {@link
     * AudioStream}s</strong> in addition to video or video-only {@link VideoStream}s.
     *
     * <p>
     * Note that most of the content of an ended live video (or audio) may be extracted as {@link
     * #VIDEO_STREAM regular video contents} (or {@link #AUDIO_STREAM regular audio contents})
     * later, because the service may encode them again later as normal video/audio streams. That's
     * the case on YouTube, for example.
     * </p>
     */
    POST_LIVE_STREAM,

    /**
     * An audio live stream that has just ended but has not yet been encoded into a normal audio
     * stream. There should be no {@link VideoStream}s available! In order to prevent unexpected
     * behaviors, when {@link StreamExtractor}s return this stream type, they should ensure that no
     * video stream is returned in {@link StreamExtractor#getVideoStreams()} and
     * {@link StreamExtractor#getVideoOnlyStreams()}.
     *
     * <p>
     * Note that most of ended live audio streams extracted with this value are processed as
     * {@link #AUDIO_STREAM regular audio streams} later, because the service may encode them
     * again later.
     * </p>
     */
    POST_LIVE_AUDIO_STREAM
}
