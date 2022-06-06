package org.schabi.newpipe.extractor.services.youtube.itag.format.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.services.youtube.itag.format.VideoAudioItagFormat;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import javax.annotation.Nonnull;

public class SimpleVideoAudioItagFormat extends SimpleVideoItagFormat
        implements VideoAudioItagFormat {
    private final int averageBitrate;

    public SimpleVideoAudioItagFormat(final int id,
                                      @Nonnull final VideoAudioMediaFormat mediaFormat,
                                      @Nonnull final VideoQualityData videoQualityData,
                                      final int averageBitrate,
                                      @Nonnull final ItagFormatDeliveryData deliveryData) {
        super(id, mediaFormat, videoQualityData, deliveryData);
        this.averageBitrate = averageBitrate;
    }

    public SimpleVideoAudioItagFormat(final int id,
                                      @Nonnull final VideoAudioMediaFormat mediaFormat,
                                      @Nonnull final VideoQualityData videoQualityData,
                                      final int averageBitrate) {
        super(id, mediaFormat, videoQualityData);
        this.averageBitrate = averageBitrate;
    }

    @Override
    public int averageBitrate() {
        return averageBitrate;
    }
}
