package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators;

import javax.annotation.Nonnull;

/**
 * Exception that is thrown when a YouTube DASH manifest creator encounters a problem
 * while creating a manifest.
 */
public final class CreationException extends RuntimeException {

    /**
     * Create a new {@link CreationException} with a detail message.
     *
     * @param message the detail message to add in the exception
     */
    public CreationException(final String message) {
        super(message);
    }

    /**
     * Create a new {@link CreationException} with a detail message and a cause.
     * @param message the detail message to add in the exception
     * @param cause   the exception cause of this {@link CreationException}
     */
    public CreationException(final String message, final Exception cause) {
        super(message, cause);
    }

    // Methods to create exceptions easily without having to use big exception messages and to
    // reduce duplication

    /**
     * Create a new {@link CreationException} with a cause and the following detail message format:
     * <br>
     * {@code "Could not add " + element + " element", cause}, where {@code element} is an element
     * of a DASH manifest.
     *
     * @param element the element which was not added to the DASH document
     * @param cause   the exception which prevented addition of the element to the DASH document
     * @return a new {@link CreationException}
     */
    @Nonnull
    public static CreationException couldNotAddElement(final String element,
                                                       final Exception cause) {
        return new CreationException("Could not add " + element + " element", cause);
    }

    /**
     * Create a new {@link CreationException} with a cause and the following detail message format:
     * <br>
     * {@code "Could not add " + element + " element: " + reason}, where {@code element} is an
     * element of a DASH manifest and {@code reason} the reason why this element cannot be added to
     * the DASH document.
     *
     * @param element the element which was not added to the DASH document
     * @param reason  the reason message of why the element has been not added to the DASH document
     * @return a new {@link CreationException}
     */
    @Nonnull
    public static CreationException couldNotAddElement(final String element, final String reason) {
        return new CreationException("Could not add " + element + " element: " + reason);
    }
}
