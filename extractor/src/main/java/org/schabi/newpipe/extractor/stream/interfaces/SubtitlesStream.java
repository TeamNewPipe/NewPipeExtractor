package org.schabi.newpipe.extractor.stream.interfaces;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;

/**
 * Interface representing a subtitles stream as a separate file (subtitles muxed with a video
 * stream must be returned in a {@link MuxedStream}).
 *
 * <p>
 * See {@link BaseSubtitlesStream} and {@link DemuxedStream} for available methods.
 * </p>
 */
public interface SubtitlesStream extends BaseSubtitlesStream, DemuxedStream {
}
