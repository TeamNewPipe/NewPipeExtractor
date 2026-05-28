package org.schabi.newpipe.extractor.stream.interfaces.base;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Marker interface which represents the base of streams in the extractor.
 *
 * <p>
 * It is not meant to be directly used by clients, but by extractor sub-definitions and
 * custom implementations. See {@link org.schabi.newpipe.extractor.stream.interfaces.Stream
 * Stream} for an interface which is meant to be used.
 * </p>
 */
public interface BaseStream extends Serializable {

    /**
     * Get the ID of this stream.
     *
     * <p>
     * This should be the ID given by the service, if there is one, or {@code null} if there is no
     * one.
     * </p>
     *
     * @return the ID of this stream or {@code null}
     */
    @Nullable
    String getId();

    /**
     * Get the user-facing name of this stream.
     *
     * <p>
     * This should be the name given by the service which may be localized, if there is one, or
     * {@code null} if there is no one.
     * </p>
     *
     * @return the user-facing name of this stream, which may be localized or {@code null}
     */
    @Nullable
    String getName();
}
