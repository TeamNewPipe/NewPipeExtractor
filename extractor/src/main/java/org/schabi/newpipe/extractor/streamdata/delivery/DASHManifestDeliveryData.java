package org.schabi.newpipe.extractor.streamdata.delivery;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreator;

import javax.annotation.Nonnull;

public interface DASHManifestDeliveryData extends DASHDeliveryData {
    @Nonnull
    DashManifestCreator dashManifestCreator();

    String getCachedDashManifestAsString();

    @Override
    default long getExpectedContentLength(final Downloader downloader) {
        return dashManifestCreator().getExpectedContentLength(downloader);
    }
}
