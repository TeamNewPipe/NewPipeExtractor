package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleVideoAudioStreamImpl extends AbstractStreamImpl implements VideoAudioStream {
    @Nullable
    private final AudioMediaFormat audioMediaFormat;
    private final int averageBitrate;

    @Nullable
    private final VideoAudioMediaFormat videoAudioMediaFormat;
    @Nonnull
    private final String resolution;

    public SimpleVideoAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final AudioMediaFormat audioMediaFormat,
            final int averageBitrate,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat,
            @Nonnull final String resolution
    ) {
        super(deliveryData);
        this.audioMediaFormat = audioMediaFormat;
        this.averageBitrate = averageBitrate;
        this.videoAudioMediaFormat = videoAudioMediaFormat;
        this.resolution = Objects.requireNonNull(resolution);
    }

    public SimpleVideoAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat,
            @Nonnull final String resolution
    ) {
        this(deliveryData, null, UNKNOWN_BITRATE, videoAudioMediaFormat, resolution);
    }

    public SimpleVideoAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat
    ) {
        this(deliveryData, videoAudioMediaFormat, UNKNOWN_RESOLUTION);
    }

    @Nullable
    @Override
    public AudioMediaFormat audioMediaFormat() {
        return audioMediaFormat;
    }

    @Override
    public int averageBitrate() {
        return averageBitrate;
    }

    @Nullable
    @Override
    public VideoAudioMediaFormat videoMediaFormat() {
        return videoAudioMediaFormat;
    }

    @Nonnull
    @Override
    public String resolution() {
        return resolution;
    }
}
