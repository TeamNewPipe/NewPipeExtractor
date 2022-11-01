package org.schabi.newpipe.extractor.streamdata.delivery;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreator;

import javax.annotation.Nonnull;

public interface DASHManifestDeliveryData extends DASHDeliveryData, DownloadableDeliveryData {
    @Nonnull
    DashManifestCreator dashManifestCreator();

    String getCachedDashManifestAsString();

    @Nonnull
    @Override
    default String downloadUrl() {
        return dashManifestCreator().downloadUrl();
    }

    @Override
    default long getExpectedContentLength(final Downloader downloader) {
        return dashManifestCreator().getExpectedContentLength(downloader);
    }
}
