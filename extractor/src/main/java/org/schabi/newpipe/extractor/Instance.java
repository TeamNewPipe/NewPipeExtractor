package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.InvalidInstanceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Instance {

    @Nullable
    String getName();

    @Nonnull
    String getUrl();

    boolean isValid();

    /**
     * Fetch instance metadata.
     * <p>
     * You can e.g. save the name
     *
     * @throws InvalidInstanceException
     */
    void fetchInstanceMetaData() throws InvalidInstanceException;

}