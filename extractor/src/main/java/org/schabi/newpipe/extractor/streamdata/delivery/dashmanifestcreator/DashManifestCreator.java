package org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator;

import javax.annotation.Nonnull;

public interface DashManifestCreator {

    /**
     * Generates the DASH manifest.
     * @return The dash manifest as string.
     * @throws DashManifestCreationException May throw a CreationException
     */
    @Nonnull
    String generateManifest();
}
