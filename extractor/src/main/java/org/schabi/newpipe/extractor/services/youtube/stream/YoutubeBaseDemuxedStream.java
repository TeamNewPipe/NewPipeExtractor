package org.schabi.newpipe.extractor.services.youtube.stream;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseStream;

import javax.annotation.Nonnull;

/**
 * Interface representing the base of an adaptive/demuxed YouTube stream, except for subtitles
 * streams of regular videos for which properties of
 * {@link org.schabi.newpipe.extractor.stream.impl.UriSubtitlesStream UriSubtitlesStream} are
 * enough to be used directly.
 */
public interface YoutubeBaseDemuxedStream extends BaseStream {

    /**
     * Get the {@link Itag} associated with this stream.
     *
     * @return the {@link Itag} associated with this stream, which isn't null
     */
    @Nonnull
    Itag getItag();
}
