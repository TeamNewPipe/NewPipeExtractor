package org.schabi.newpipe.extractor.streamdata.stream;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.MediaFormat;

import javax.annotation.Nonnull;

public interface Stream<M extends MediaFormat> {

    /**
     * The (container) media format, e.g. mp3 for audio streams or webm for video(+audio) streams.
     *
     * @return The (container) media format
     */
    @Nonnull
    M mediaFormat();

    @Nonnull
    DeliveryData deliveryData();
}
