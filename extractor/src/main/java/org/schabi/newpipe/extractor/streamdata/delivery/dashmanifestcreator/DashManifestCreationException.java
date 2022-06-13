package org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator;

import javax.annotation.Nonnull;

/**
 * Exception that is thrown when a DASH manifest creator encounters a problem
 * while creating a manifest.
 */
public class DashManifestCreationException extends RuntimeException {
    
    public DashManifestCreationException(final String message) {
        super(message);
    }

    public DashManifestCreationException(final String message, final Exception cause) {
        super(message, cause);
    }

    // Methods to create exceptions easily without having to use big exception messages and to
    // reduce duplication

    /**
     * Create a new {@link DashManifestCreationException} with a cause and the following detail
     * message format:
     * <br>
     * {@code "Could not add " + element + " element", cause}, where {@code element} is an element
     * of a DASH manifest.
     *
     * @param element the element which was not added to the DASH document
     * @param cause   the exception which prevented addition of the element to the DASH document
     * @return a new {@link DashManifestCreationException}
     */
    @Nonnull
    public static DashManifestCreationException couldNotAddElement(final String element,
                                                                   final Exception cause) {
        return new DashManifestCreationException("Could not add " + element + " element", cause);
    }

    /**
     * Create a new {@link DashManifestCreationException} with a cause and the following detail
     * message format:
     * <br>
     * {@code "Could not add " + element + " element: " + reason}, where {@code element} is an
     * element of a DASH manifest and {@code reason} the reason why this element cannot be added to
     * the DASH document.
     *
     * @param element the element which was not added to the DASH document
     * @param reason  the reason message of why the element has been not added to the DASH document
     * @return a new {@link DashManifestCreationException}
     */
    @Nonnull
    public static DashManifestCreationException couldNotAddElement(final String element,
                                                                   final String reason) {
        return new DashManifestCreationException("Could not add " + element + " element: " + reason);
    }
}
