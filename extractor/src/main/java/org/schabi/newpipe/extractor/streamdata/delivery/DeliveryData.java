package org.schabi.newpipe.extractor.streamdata.delivery;

import org.schabi.newpipe.extractor.downloader.Downloader;

public interface DeliveryData {
    /**
     * Returns the expected content length/size of the data.
     *
     * @param downloader The downloader that may be used for fetching (HTTP HEAD).
     * @return the expected size/content length or <code>-1</code> if unknown
     */
    long getExpectedContentLength(Downloader downloader);
}
