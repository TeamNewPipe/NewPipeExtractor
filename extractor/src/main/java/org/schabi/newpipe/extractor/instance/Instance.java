package org.schabi.newpipe.extractor.instance;

import javax.annotation.Nonnull;

public interface Instance {

    /**
     * The name of the instance.
     * <br/>
     * Note: May only be available after {@link #fetchMetadata()} was called
     *
     * @return the instance-name
     */
    String getName();

    /**
     * The base url of this instance.
     * <br/>
     * Note that the url is returned without trailing slashes.
     *
     * @return base url
     */
    @Nonnull
    String getUrl();


    /**
     * Fetch instance metadata.
     *
     * @throws InstanceMetaDataFetchException
     */
    void fetchMetadata();

    /**
     * Returns the service name, e.g. "Invidious" for an invidious instance.
     * @return service name or <code>null</code> if the name of the streamingservice should be used
     */
    default String getServiceName() {
        return null;
    }
}
