package org.schabi.newpipe.extractor.streamdata.delivery;

import javax.annotation.Nonnull;

public interface DASHManifestDeliveryData extends DASHDeliveryData {
    /**
     * Returns the base url for the DashManifest.
     *
     * @return
     */
    // TODO: Check removal
    @Nonnull
    default String getBaseUrl() {
        return "";
    }

    String getManifestAsString();
}
