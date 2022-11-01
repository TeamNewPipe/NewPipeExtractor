package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DASHManifestDeliveryData;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreator;

import java.util.Objects;

import javax.annotation.Nonnull;

public class SimpleDASHManifestDeliveryDataImpl extends AbstractDeliveryDataImpl
        implements DASHManifestDeliveryData {
    @Nonnull
    private final DashManifestCreator dashManifestCreator;

    private String cachedDashManifest;

    public SimpleDASHManifestDeliveryDataImpl(
            @Nonnull final DashManifestCreator dashManifestCreator
    ) {
        this.dashManifestCreator = Objects.requireNonNull(dashManifestCreator);
    }

    @Override
    @Nonnull
    public DashManifestCreator dashManifestCreator() {
        return dashManifestCreator;
    }

    @Override
    public String getCachedDashManifestAsString() {
        if (cachedDashManifest == null) {
            cachedDashManifest = dashManifestCreator().generateManifest();
        }
        return cachedDashManifest;
    }
}
