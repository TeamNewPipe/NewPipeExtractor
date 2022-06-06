package org.schabi.newpipe.extractor.services.youtube.itag.format.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.services.youtube.itag.format.AudioItagFormat;
import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;

public class SimpleAudioItagFormat extends AbstractSimpleItagFormat<AudioMediaFormat>
        implements AudioItagFormat {
    private final int averageBitrate;

    public SimpleAudioItagFormat(final int id,
                                 final AudioMediaFormat mediaFormat,
                                 final int averageBitrate,
                                 final ItagFormatDeliveryData deliveryData) {
        super(id, mediaFormat, deliveryData);
        this.averageBitrate = averageBitrate;
    }

    public SimpleAudioItagFormat(final int id,
                                 final AudioMediaFormat mediaFormat,
                                 final int averageBitrate) {
        super(id, mediaFormat);
        this.averageBitrate = averageBitrate;
    }

    @Override
    public int averageBitrate() {
        return averageBitrate;
    }
}
