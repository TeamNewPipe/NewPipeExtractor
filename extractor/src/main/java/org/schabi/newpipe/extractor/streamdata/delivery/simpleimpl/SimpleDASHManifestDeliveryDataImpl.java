package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DASHManifestDeliveryData;

import java.util.Objects;
import java.util.function.Supplier;

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
    private final Supplier<String> dashManifestBuilder;

    public SimpleDASHManifestDeliveryDataImpl(@Nonnull final Supplier<String> dashManifestBuilder) {
        this.dashManifestBuilder = Objects.requireNonNull(dashManifestBuilder);
    }

    @Override
    public String getManifestAsString() {
        return dashManifestBuilder.get();
    }
}