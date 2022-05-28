package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleAudioStreamImpl extends AbstractStreamImpl implements AudioStream {
    @Nullable
    private final AudioMediaFormat audioMediaFormat;
    private final int averageBitrate;

    public SimpleAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final AudioMediaFormat audioMediaFormat,
            final int averageBitrate
    ) {
        super(deliveryData);
        this.audioMediaFormat = audioMediaFormat;
        this.averageBitrate = averageBitrate;
    }

    public SimpleAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final AudioMediaFormat audioMediaFormat
    ) {
        this(deliveryData, audioMediaFormat, UNKNOWN_BITRATE);
    }

    public SimpleAudioStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            final int averageBitrate
    ) {
        this(deliveryData, null, averageBitrate);
    }

    public SimpleAudioStreamImpl(@Nonnull final DeliveryData deliveryData) {
        this(deliveryData, null);
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
}
