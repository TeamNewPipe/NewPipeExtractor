package org.schabi.newpipe.extractor.streamdata.delivery;

import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreator;

import javax.annotation.Nonnull;

public interface DASHManifestDeliveryData extends DASHDeliveryData {
    @Nonnull
    DashManifestCreator getDashManifestCreator();

    String getCachedDashManifestAsString();
}
