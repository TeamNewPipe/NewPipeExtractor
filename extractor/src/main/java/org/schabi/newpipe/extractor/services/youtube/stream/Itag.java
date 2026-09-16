package org.schabi.newpipe.extractor.services.youtube.stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class representing common YouTube specific information for adaptive streams (i.e. between audio,
 * video and live subtitles stream types).
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public final class Itag implements Serializable {

    /**
     * The identifier given by YouTube for this stream variant.
     *
     * <p>
     * Multiple streams from the same InnerTube client may have the same ID, especially audio
     * tracks, and some streams are returned on multiple clients.
     * </p>
     */
    @Nonnull
    public final Integer id;

    /**
     * A Base 64-encoded Protobuf message containing information on the stream, such as its
     * language code or its track type.
     *
     * <p>
     * Mostly returned on audio streams of videos with multiple audio tracks and on AI upscaled
     * video streams, {@code null} in most other cases.
     * </p>
     */
    @Nullable
    public final String xtags;

    /**
     * The duration per segment for this stream on livestreams, whether they are running or they
     * are in the post-live state (i.e. served as they are without re-encoding for a few minutes or
     * hours), in seconds, which should be common for all streams.
     *
     * <p>
     * This is {@code null} for regular videos.
     * </p>
     */
    @Nullable
    public final Integer targetDurationSec;

    /**
     * The duration for which YouTube allows up to rewind on running livestreams for this stream,
     * in seconds, which should be common for all streams.
     *
     * <p>
     * On livestreams in the post-live state (i.e. served as they are without re-encoding for a few
     * minutes or hours), this is the maximum duration watchable if the livestream duration was
     * longer than this value, otherwise the livestream duration is of course the livestream
     * duration.
     * </p>
     *
     * <p>
     * Note that technically YouTube may allow to get older segments than the limit given, for
     * running livestreams.
     * </p>
     *
     * <p>
     * This is {@code null} for regular videos.
     * </p>
     */
    @Nullable
    public final Integer maxDvrDurationSec;

    /**
     * The initialization range start for DASH streams, in bytes.
     *
     * <p>
     * This is {@code null} for non-regular videos.
     * </p>
     */
    @Nullable
    public final Integer initRangeStart;

    /**
     * The initialization range end for DASH streams, in bytes.
     *
     * <p>
     * This is {@code null} for non-regular videos.
     * </p>
     */
    @Nullable
    public final Integer initRangeEnd;

    /**
     * The index range start for DASH streams, in bytes.
     *
     * <p>
     * This is {@code null} for non-regular videos.
     * </p>
     */
    @Nullable
    public final Integer indexRangeStart;

    /**
     * The index range end for DASH streams, in bytes.
     *
     * <p>
     * This is {@code null} for non-regular videos.
     * </p>
     */
    @Nullable
    public final Integer indexRangeEnd;

    /**
     * Unix timestamp representing when this stream was last modified, in microseconds.
     *
     * <p>
     * This should be {@code null} for non-regular videos.
     * </p>
     */
    @Nullable
    public final Long lastModifiedTimestamp;

    /**
     * Construct a new {@link Itag} instance.
     *
     * @param id                    see {@link #id}
     * @param xtags                 see {@link #xtags}
     * @param targetDurationSec     see {@link #targetDurationSec}
     * @param maxDvrDurationSec     see {@link #maxDvrDurationSec}
     * @param initRangeStart        see {@link #initRangeStart}
     * @param initRangeEnd          see {@link #initRangeEnd}
     * @param indexRangeStart       see {@link #indexRangeStart}
     * @param indexRangeEnd         see {@link #indexRangeEnd}
     * @param lastModifiedTimestamp see {@link #lastModifiedTimestamp}
     */
    public Itag(@Nonnull final Integer id,
                @Nullable final String xtags,
                @Nullable final Integer targetDurationSec,
                @Nullable final Integer maxDvrDurationSec,
                @Nullable final Integer initRangeStart,
                @Nullable final Integer initRangeEnd,
                @Nullable final Integer indexRangeStart,
                @Nullable final Integer indexRangeEnd,
                @Nullable final Long lastModifiedTimestamp) {
        this.id = id;
        this.xtags = xtags;
        this.targetDurationSec = targetDurationSec;
        this.maxDvrDurationSec = maxDvrDurationSec;
        this.initRangeStart = initRangeStart;
        this.initRangeEnd = initRangeEnd;
        this.indexRangeStart = indexRangeStart;
        this.indexRangeEnd = indexRangeEnd;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Itag that)) {
            return false;
        }

        return id.equals(that.id)
                && Objects.equals(xtags, that.xtags)
                && Objects.equals(targetDurationSec, that.targetDurationSec)
                && Objects.equals(maxDvrDurationSec, that.maxDvrDurationSec)
                && Objects.equals(initRangeStart, that.initRangeStart)
                && Objects.equals(initRangeEnd, that.initRangeEnd)
                && Objects.equals(indexRangeStart, that.indexRangeStart)
                && Objects.equals(indexRangeEnd, that.indexRangeEnd)
                && Objects.equals(lastModifiedTimestamp, that.lastModifiedTimestamp);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Objects.hashCode(xtags);
        result = 31 * result + Objects.hashCode(targetDurationSec);
        result = 31 * result + Objects.hashCode(maxDvrDurationSec);
        result = 31 * result + Objects.hashCode(initRangeStart);
        result = 31 * result + Objects.hashCode(initRangeEnd);
        result = 31 * result + Objects.hashCode(indexRangeStart);
        result = 31 * result + Objects.hashCode(indexRangeEnd);
        result = 31 * result + Objects.hashCode(lastModifiedTimestamp);
        return result;
    }

    @Override
    public String toString() {
        return "Itag{"
                + "id=" + id
                + ", xtags=" + xtags
                + ", targetDurationSec=" + targetDurationSec
                + ", maxDvrDurationSec=" + maxDvrDurationSec
                + ", initRangeStart=" + initRangeStart
                + ", initRangeEnd=" + initRangeEnd
                + ", indexRangeStart=" + indexRangeStart
                + ", indexRangeEnd=" + indexRangeEnd
                + ", lastModifiedTimestamp=" + lastModifiedTimestamp
                + "}";
    }
}
