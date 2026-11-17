package org.schabi.newpipe.extractor.stream.interfaces;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;

/**
 * Interface representing a demuxed video {@link Stream} (i.e. without audio or subtitle tracks
 * which are not in the video pixels, otherwise a {@link MuxedStream} must be used).
 *
 * <p>
 * See {@link BaseVideoStream} and {@link DemuxedStream} for available methods.
 * </p>
 */
public interface VideoStream extends BaseVideoStream, DemuxedStream {
}
