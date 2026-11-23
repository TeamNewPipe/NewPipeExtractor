package org.schabi.newpipe.extractor.stream.interfaces.base;

import org.schabi.newpipe.extractor.stream.mediaformat.SubtitlesMediaFormat;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Base interface for subtitles streams.
 *
 * <p>
 * It does not represent individual subtitles streams to be directly used by clients, but the ones
 * in extractor sub-definitions and custom implementations. See
 * {@link org.schabi.newpipe.extractor.stream.interfaces.SubtitlesStream SubtitlesStream} for an
 * interface to be used directly.
 * </p>
 */
public interface BaseSubtitlesStream extends BaseDemuxedStream {

    /**
     * Get the {@link SubtitlesMediaFormat} of this video stream.
     *
     * @return the {@link SubtitlesMediaFormat} of this video stream
     */
    @Nonnull
    SubtitlesMediaFormat getMediaFormat();

    /**
     * Get the list of subtitles translated from this subtitles stream.
     *
     * @return the list of subtitles translated from this subtitles stream or an empty list if
     * there is no translation
     */
    @Nonnull
    List<? extends BaseSubtitlesStream> getTranslations();
}
