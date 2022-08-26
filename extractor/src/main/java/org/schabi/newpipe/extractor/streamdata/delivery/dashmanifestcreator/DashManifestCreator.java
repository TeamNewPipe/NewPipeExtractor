package org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator;

import org.schabi.newpipe.extractor.downloader.Downloader;

import javax.annotation.Nonnull;

public interface DashManifestCreator {

    /**
     * Generates the DASH manifest.
     *
     * @return The dash manifest as string.
     * @throws DashManifestCreationException May throw a CreationException
     */
    @Nonnull
    String generateManifest();

    @Nonnull
    String downloadUrl();

    /**
     * See
     * {@link org.schabi.newpipe.extractor.streamdata.delivery.DownloadableDeliveryData#getExpectedContentLength(Downloader)}
     */
    long getExpectedContentLength(Downloader downloader);
}
