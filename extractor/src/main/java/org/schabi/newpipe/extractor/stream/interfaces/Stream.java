package org.schabi.newpipe.extractor.stream.interfaces;

import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Interface describing a stream and meant be to consumed by clients.
 *
 * <p>
 * It can aggregate multiple streams in a {@link MuxedStream} or be a {@link DemuxedStream}.
 * </p>
 */
public interface Stream extends Serializable {

    /**
     * Get the {@link DeliverySource} of this stream.
     *
     * @return the non-null {@link DeliverySource}
     */
    @Nonnull
    DeliverySource getDeliverySource();

    /**
     * Get the {@link StreamingProtocol} of this stream.
     *
     * @return the non-null {@link StreamingProtocol}
     */
    @Nonnull
    StreamingProtocol getStreamingProtocol();
}
