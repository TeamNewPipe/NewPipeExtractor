package org.schabi.newpipe.extractor.stream.impl.base;

import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

public class BaseAudioStreamImpl extends BaseDemuxedStreamImpl implements BaseAudioStream {

    @Nonnull
    private final AudioMediaFormat mediaFormat;
    @Nullable
    private final Integer averageBitrate;
    @Nullable
    private final Integer sampleRate;
    @Nullable
    private final Integer channelsCount;
    @Nullable
    private final AudioTrackType trackType;
    @Nullable
    private final Boolean isUsingDrc;
    @Nullable
    private final String codec;

    public BaseAudioStreamImpl(@Nullable final String id,
                               @Nullable final String name,
                               @Nullable final Locale locale,
                               @Nullable final Boolean isAutomaticallyServiceGenerated,
                               @Nonnull final AudioMediaFormat mediaFormat,
                               @Nullable final Integer averageBitrate,
                               @Nullable final Integer sampleRate,
                               @Nullable final Integer channelsCount,
                               @Nullable final AudioTrackType trackType,
                               @Nullable final Boolean isUsingDrc,
                               @Nullable final String codec) {
        super(id, name, locale, isAutomaticallyServiceGenerated);
        this.mediaFormat = mediaFormat;
        this.averageBitrate = averageBitrate;
        this.sampleRate = sampleRate;
        this.channelsCount = channelsCount;
        this.trackType = trackType;
        this.isUsingDrc = isUsingDrc;
        this.codec = codec;
    }

    @Nonnull
    @Override
    public AudioMediaFormat getMediaFormat() {
        return mediaFormat;
    }

    @Nullable
    @Override
    public Integer getAverageBitrate() {
        return averageBitrate;
    }

    @Nullable
    @Override
    public Integer getSampleRate() {
        return sampleRate;
    }

    @Nullable
    @Override
    public Integer getChannelsCount() {
        return channelsCount;
    }

    @Nullable
    @Override
    public AudioTrackType getTrackType() {
        return trackType;
    }

    @Nullable
    @Override
    public Boolean isUsingDrc() {
        return isUsingDrc;
    }

    @Nullable
    @Override
    public String getCodec() {
        return codec;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof BaseAudioStreamImpl that) || !super.equals(o)) {
            return false;
        }

        return mediaFormat.equals(that.mediaFormat)
                && Objects.equals(averageBitrate, that.averageBitrate)
                && Objects.equals(sampleRate, that.sampleRate)
                && Objects.equals(channelsCount, that.channelsCount)
                && trackType == that.trackType
                && Objects.equals(isUsingDrc, that.isUsingDrc)
                && Objects.equals(codec, that.codec);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mediaFormat.hashCode();
        result = 31 * result + Objects.hashCode(averageBitrate);
        result = 31 * result + Objects.hashCode(sampleRate);
        result = 31 * result + Objects.hashCode(channelsCount);
        result = 31 * result + Objects.hashCode(trackType);
        result = 31 * result + Objects.hashCode(isUsingDrc);
        result = 31 * result + Objects.hashCode(codec);
        return result;
    }

    @Override
    public String toString() {
        return "BaseAudioStreamImpl{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", locale=" + getLocale()
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated()
                + ", mediaFormat=" + mediaFormat
                + ", averageBitrate=" + averageBitrate
                + ", sampleRate=" + sampleRate
                + ", channelsCount=" + channelsCount
                + ", trackType=" + trackType
                + ", isUsingDrc=" + isUsingDrc
                + ", codec=" + codec
                + "}";
    }
}
