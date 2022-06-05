package org.schabi.newpipe.extractor.services.youtube.itag.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.AudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;

import javax.annotation.Nonnull;

public class SimpleAudioItagFormat extends AbstractItagFormat implements AudioItagFormat {
    private final AudioMediaFormat audioMediaFormat;
    private final int averageBitrate;

    public SimpleAudioItagFormat(final int id,
                                 final AudioMediaFormat audioMediaFormat,
                                 final int averageBitrate,
                                 final ItagFormatDeliveryData deliveryData) {
        super(id, deliveryData);
        this.audioMediaFormat = audioMediaFormat;
        this.averageBitrate = averageBitrate;
    }

    public SimpleAudioItagFormat(final int id,
                                 final AudioMediaFormat audioMediaFormat,
                                 final int averageBitrate) {
        super(id);
        this.audioMediaFormat = audioMediaFormat;
        this.averageBitrate = averageBitrate;
    }

    @Nonnull
    @Override
    public AudioMediaFormat audioMediaFormat() {
        return audioMediaFormat;
    }

    @Override
    public int averageBitrate() {
        return averageBitrate;
    }
}
