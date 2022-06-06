package org.schabi.newpipe.extractor.services.youtube.itag.format;

import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.MediaFormat;

import javax.annotation.Nonnull;

public interface ItagFormat<M extends MediaFormat> {
    int id();

    /**
     * The (container) media format, e.g. mp3 for audio streams or webm for video(+audio) streams.
     *
     * @return The (container) media format
     */
    @Nonnull
    M mediaFormat();

    @Nonnull
    ItagFormatDeliveryData deliveryData();
}
