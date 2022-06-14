package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DASHManifestDeliveryData;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreator;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Note we build the manifests for YT ourself because the provided ones (according to TiA4f8R)
 * <ul>
 * <li>aren't working https://github.com/google/ExoPlayer/issues/2422#issuecomment-283080031</li>
 * <li>causes memory problems; TransactionTooLargeException: data parcel size 3174340</li>
 * <li>are not always returned, only for videos with OTF streams, or on (ended or not)
 * livestreams</li>
 * <li>Instead of downloading a 10MB manifest when you can generate one which is 1 or 2MB large</li>
 * <li>Also, this manifest isn't used at all by modern YouTube clients.</li>
 * </ul>
 */
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
    public DashManifestCreator getDashManifestCreator() {
        return dashManifestCreator;
    }

    @Override
    public String getCachedDashManifestAsString() {
        if (cachedDashManifest != null) {
            cachedDashManifest = getDashManifestCreator().generateManifest();
        }
        return cachedDashManifest;
    }
}