package org.schabi.newpipe.extractor.stream.mediaformat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Enum describing supported audio media formats.
 */
public enum AudioMediaFormat {

    M4A("audio/mp4", List.of("m4a")),
    MP3("audio/mpeg", List.of("mp3")),
    WEBM("audio/webm", List.of("webm")),
    OPUS("audio/opus", List.of("opus")),
    OGG("audio/ogg", List.of("ogg")),
    AIFF("audio/aiff", List.of("aif", "aiff")),
    WAV("audio/wav", List.of("wav", "wave")),
    FLAC("audio/flac", List.of("flac")),
    ALAC("audio/alac", List.of("alac"));

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

    AudioMediaFormat(@Nonnull final String mimeType,
                     @Nonnull final List<String> suffixes) {
        this.mimeType = mimeType;
        this.suffixes = suffixes;
    }

    /**
     * Get the first {@link AudioMediaFormat} that has the given suffix/file extension.
     *
     * @return the matching {@link AudioMediaFormat} or {@code null} if no associated format
     * was found
     */
    @Nullable
    public static AudioMediaFormat getFromSuffix(final String suffix) {
        return Arrays.stream(AudioMediaFormat.values())
                .filter(mediaFormat -> mediaFormat.suffixes
                        .stream()
                        .anyMatch(extension -> extension.equals(suffix)))
                .findFirst()
                .orElse(null);
    }
}
