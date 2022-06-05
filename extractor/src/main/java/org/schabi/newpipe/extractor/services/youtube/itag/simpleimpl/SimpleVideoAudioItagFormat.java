package org.schabi.newpipe.extractor.services.youtube.itag.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.VideoAudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import javax.annotation.Nonnull;

public class SimpleVideoAudioItagFormat extends SimpleVideoItagFormat
        implements VideoAudioItagFormat {
    private final int averageBitrate;

    public SimpleVideoAudioItagFormat(final int id,
                                      @Nonnull final VideoAudioMediaFormat videoAudioMediaFormat,
                                      @Nonnull final VideoQualityData videoQualityData,
                                      final int averageBitrate,
                                      @Nonnull final ItagFormatDeliveryData deliveryData) {
        super(id, videoAudioMediaFormat, videoQualityData, deliveryData);
        this.averageBitrate = averageBitrate;
    }

    public SimpleVideoAudioItagFormat(final int id,
                                      @Nonnull final VideoAudioMediaFormat videoAudioMediaFormat,
                                      @Nonnull final VideoQualityData videoQualityData,
                                      final int averageBitrate) {
        super(id, videoAudioMediaFormat, videoQualityData);
        this.averageBitrate = averageBitrate;
    }

    @Override
    public int averageBitrate() {
        return averageBitrate;
    }
}
