package org.schabi.newpipe.extractor.stream;

/**
 * Defines what strategy of the extractor is used for playback.
 */
public enum StreamResolvingStrategy {
    /**
     * Uses video streams (with no audio) and separate audio streams.
     * @see StreamExtractor#getVideoOnlyStreams()
     * @see StreamExtractor#getAudioStreams()
     */
    VIDEO_ONLY_AND_AUDIO_STREAMS,
    /**
     * Uses video streams that include audio data.
     *
     * @see StreamExtractor#getVideoStreams()
     */
    VIDEO_AUDIO_STREAMS,
    /**
     * Uses the HLS master playlist url.
     *
     * @see StreamExtractor#getHlsMasterPlaylistUrl()
     */
    HLS_MASTER_PLAYLIST_URL,
    /**
     * Uses the DASH MPD url.
     *
     * @see StreamExtractor#getDashMpdUrl()
     */
    DASH_MPD_URL
}
