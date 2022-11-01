package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.TorrentDeliveryData;

import javax.annotation.Nonnull;

public class SimpleTorrentDeliveryDataImpl extends AbstractUrlBasedDeliveryDataImpl
        implements TorrentDeliveryData {
    public SimpleTorrentDeliveryDataImpl(@Nonnull final String url) {
        super(url);
    }
}
