package org.schabi.newpipe.extractor.streamdata.delivery;

import org.schabi.newpipe.extractor.downloader.Downloader;

import javax.annotation.Nonnull;

public interface DownloadableUrlBasedDeliveryData
        extends UrlBasedDeliveryData, DownloadableDeliveryData {
    @Nonnull
    @Override
    default String downloadUrl() {
        return url();
    }

    @Override
    default long getExpectedContentLength(final Downloader downloader) {
        return downloader.getContentLength(url());
    }
}
