package org.schabi.newpipe.extractor.stream.deliverysource.uri;

import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Interface for URI delivery sources.
 *
 * <p>
 * A URI delivery source must have at least one URI and may have multiple alternative URIs (e.g.
 * for mirror or fallback servers). See {@link UriObject} for more details.
 * </p>
 */
public interface UriDeliverySource extends DeliverySource {

    /**
     * Get the main URI for this delivery source.
     *
     * <p>
     * It must not be null.
     * </p>
     *
     * @return the main URI for this delivery source
     */
    @Nonnull
    UriObject getUri();

    /**
     * Get alternate URIs for this delivery source as a list.
     *
     * <p>
     * The returned list must be not null and should be unmodifiable, in the case there is not
     * alternate URI, an empty list must be returned. They should be sorted by priority, from the
     * better to be used to the lower one.
     * </p>
     *
     * @return the alternate URIs as a non-null list
     */
    @Nonnull
    List<UriObject> getAlternateUris();
}
