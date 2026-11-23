package org.schabi.newpipe.extractor.stream.mediaformat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Enum describing subtitles media formats.
 */
public enum SubtitlesMediaFormat {

    WEBVTT("text/vtt", List.of("vtt")),
    TTML("application/ttml+xml", List.of("ttml")),
    SRT("text/srt", List.of("srt")),
    TRANSCRIPT1("text/xml", List.of("srv1")),
    TRANSCRIPT2("text/xml", List.of("srv2")),
    TRANSCRIPT3("text/xml", List.of("srv3")),
    MP4("text/mp4", List.of("mp4"));

    /**
     * The mime type associated with this media format.
     */
    @Nonnull
    public final String mimeType;

    /**
     * The suffixes/file extensions of this media format, as an unmodifiable list.
     */
    @Nonnull
    public final List<String> suffixes;

    SubtitlesMediaFormat(@Nonnull final String mimeType,
                         @Nonnull final List<String> suffixes) {
        this.mimeType = mimeType;
        this.suffixes = suffixes;
    }

    /**
     * Get the first {@link SubtitlesMediaFormat} that has the given suffix/file extension.
     *
     * @return the matching {@link SubtitlesMediaFormat} or {@code null} if no associated format
     * was found
     */
    @Nullable
    public static SubtitlesMediaFormat getFromSuffix(final String suffix) {
        return Arrays.stream(SubtitlesMediaFormat.values())
                .filter(mediaFormat -> mediaFormat.suffixes
                        .stream()
                        .anyMatch(extension -> extension.equals(suffix)))
                .findFirst()
                .orElse(null);
    }
}
