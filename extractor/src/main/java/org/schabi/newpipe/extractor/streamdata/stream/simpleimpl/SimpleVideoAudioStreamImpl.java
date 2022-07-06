package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import java.util.Objects;

import javax.annotation.Nonnull;

public class SimpleVideoAudioStreamImpl extends AbstractStreamImpl<VideoAudioMediaFormat>
        implements VideoAudioStream {

    @Nonnull
    private final VideoQualityData videoQualityData;

    private final int averageBitrate;

    public SimpleVideoAudioStreamImpl(
            @Nonnull final VideoAudioMediaFormat mediaFormat,
            @Nonnull final DeliveryData deliveryData,
            @Nonnull final VideoQualityData videoQualityData,
            final int averageBitrate
    ) {
        super(mediaFormat, deliveryData);
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
        this.averageBitrate = averageBitrate;
    }

    public SimpleVideoAudioStreamImpl(
            @Nonnull final VideoAudioMediaFormat mediaFormat,
            @Nonnull final DeliveryData deliveryData,
            @Nonnull final VideoQualityData videoQualityData
    ) {
        this(mediaFormat, deliveryData, videoQualityData, UNKNOWN_AVG_BITRATE);
    }

    @Nonnull
    @Override
    public VideoQualityData qualityData() {
        return videoQualityData;
    }

    @Override
    public int averageBitrate() {
        return averageBitrate;
    }
}
