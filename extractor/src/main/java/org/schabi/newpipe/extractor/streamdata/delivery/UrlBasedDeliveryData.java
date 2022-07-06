package org.schabi.newpipe.extractor.streamdata.delivery;

import org.schabi.newpipe.extractor.downloader.Downloader;

import javax.annotation.Nonnull;

public interface UrlBasedDeliveryData extends DeliveryData {
    @Nonnull
    String url();

    @Override
    default long getExpectedContentLength(final Downloader downloader) {
        return downloader.getContentLength(url());
    }
}
