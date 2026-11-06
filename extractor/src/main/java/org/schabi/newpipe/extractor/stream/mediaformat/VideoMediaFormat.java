package org.schabi.newpipe.extractor.stream.mediaformat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Enum describing supported video media formats.
 */
public enum VideoMediaFormat {

    MP4("video/mp4", List.of("mp4")),
    WEBM("video/webm", List.of("webm")),
    THREE_GPP("video/3gpp", List.of("3gp", "3gpp"));

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

    VideoMediaFormat(@Nonnull final String mimeType,
                             @Nonnull final List<String> suffixes) {
        this.mimeType = mimeType;
        this.suffixes = suffixes;
    }

    /**
     * Get the first {@link VideoMediaFormat} that has the given suffix/file extension.
     *
     * @return the matching {@link VideoMediaFormat} or {@code null} if no associated format
     * was found
     */
    @Nullable
    public static VideoMediaFormat getFromSuffix(final String suffix) {
        return Arrays.stream(VideoMediaFormat.values())
                .filter(mediaFormat -> mediaFormat.suffixes
                        .stream()
                        .anyMatch(extension -> extension.equals(suffix)))
                .findFirst()
                .orElse(null);
    }
}
