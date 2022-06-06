package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;

import javax.annotation.Nonnull;

public class SimpleAudioStreamImpl extends AbstractStreamImpl<AudioMediaFormat> implements AudioStream {
    private final int averageBitrate;

    public SimpleAudioStreamImpl(
            @Nonnull final AudioMediaFormat mediaFormat,
            @Nonnull final DeliveryData deliveryData,
            final int averageBitrate
    ) {
        super(mediaFormat, deliveryData);
        this.averageBitrate = averageBitrate;
    }

    public SimpleAudioStreamImpl(
            @Nonnull final AudioMediaFormat mediaFormat,
            @Nonnull final DeliveryData deliveryData
    ) {
        this(mediaFormat, deliveryData, UNKNOWN_AVG_BITRATE);
    }
    
    @Override
    public int averageBitrate() {
        return averageBitrate;
    }
}
