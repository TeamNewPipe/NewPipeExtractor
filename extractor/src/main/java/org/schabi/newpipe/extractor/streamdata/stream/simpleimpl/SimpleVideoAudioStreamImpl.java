package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

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
    private final VideoQualityData videoQualityData;

    public SimpleVideoAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final AudioMediaFormat audioMediaFormat,
            final int averageBitrate,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat,
            @Nonnull final VideoQualityData videoQualityData
    ) {
        super(deliveryData);
        this.audioMediaFormat = audioMediaFormat;
        this.averageBitrate = averageBitrate;
        this.videoAudioMediaFormat = videoAudioMediaFormat;
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
    }

    public SimpleVideoAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat,
            @Nonnull final VideoQualityData videoQualityData
    ) {
        this(deliveryData, null, UNKNOWN_BITRATE, videoAudioMediaFormat, videoQualityData);
    }

    public SimpleVideoAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat
    ) {
        this(deliveryData, videoAudioMediaFormat, new VideoQualityData());
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
    public VideoQualityData videoQualityData() {
        return videoQualityData;
    }
}
