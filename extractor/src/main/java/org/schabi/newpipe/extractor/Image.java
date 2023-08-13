package org.schabi.newpipe.extractor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class representing images in the extractor.
 *
 * <p>
 * An image has four properties: its URL, its height, its width and its estimated quality level.
 * </p>
 *
 * <p>
 * Depending of the services, the height, the width or both properties may be not known.
 * Implementations <b>must use</b> the relevant unknown constants in this case
 * ({@link #HEIGHT_UNKNOWN} and {@link #WIDTH_UNKNOWN}), to ensure properly the lack of knowledge
 * of one or both of these properties to extractor clients.
 * </p>
 *
 * <p>
 * They should also respect the ranges defined in the estimated image resolution levels as much as
 * possible, to ensure consistency to extractor clients.
 * </p>
 */
public final class Image implements Serializable {

    /**
     * Constant representing that the height of an {@link Image} is unknown.
     */
    public static final int HEIGHT_UNKNOWN = -1;

    /**
     * Constant representing that the width of an {@link Image} is unknown.
     */
    public static final int WIDTH_UNKNOWN = -1;

    @Nonnull
    private final String url;
    private final int height;
    private final int width;
    @Nonnull
    private final ResolutionLevel estimatedResolutionLevel;

    /**
     * Construct an {@link Image} instance.
     *
     * @param url                      the URL to the image, which should be not null or empty
     * @param height                   the image's height
     * @param width                    the image's width
     * @param estimatedResolutionLevel the image's estimated resolution level, which must not be
     *                                 null
     * @throws NullPointerException if {@code estimatedResolutionLevel} is null
     */
    public Image(@Nonnull final String url,
                 final int height,
                 final int width,
                 @Nonnull final ResolutionLevel estimatedResolutionLevel)
            throws NullPointerException {
        this.url = url;
        this.height = height;
        this.width = width;
        this.estimatedResolutionLevel = Objects.requireNonNull(
                estimatedResolutionLevel, "estimatedResolutionLevel is null");
    }

    /**
     * Get the URL of this {@link Image}.
     *
     * @return the {@link Image}'s URL.
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * Get the height of this {@link Image}.
     *
     * <p>
     * If it is unknown, {@link #HEIGHT_UNKNOWN} is returned instead.
     * </p>
     *
     * @return the {@link Image}'s height or {@link #HEIGHT_UNKNOWN}
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the width of this {@link Image}.
     *
     * <p>
     * If it is unknown, {@link #WIDTH_UNKNOWN} is returned instead.
     * </p>
     *
     * @return the {@link Image}'s width or {@link #WIDTH_UNKNOWN}
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the estimated resolution level of this image.
     *
     * <p>
     * If it is unknown, {@link ResolutionLevel#UNKNOWN} is returned instead.
     * </p>
     *
     * @return the estimated resolution level, which is never {@code null}
     * @see ResolutionLevel
     */
    @Nonnull
    public ResolutionLevel getEstimatedResolutionLevel() {
        return estimatedResolutionLevel;
    }

    /**
     * Get a string representation of this {@link Image} instance.
     *
     * <p>
     * The representation will be in the following format, where {@code url}, {@code height},
     * {@code width} and {@code estimatedResolutionLevel} represent the corresponding properties:
     * <br>
     * <br>
     * {@code Image {url=url, height='height, width=width,
     * estimatedResolutionLevel=estimatedResolutionLevel}'}
     * </p>
     *
     * @return a string representation of this {@link Image} instance
     */
    @Nonnull
    @Override
    public String toString() {
        return "Image {" + "url=" + url + ", height=" + height + ", width=" + width
                + ", estimatedResolutionLevel=" + estimatedResolutionLevel + "}";
    }

    /**
     * The estimated resolution level of an {@link Image}.
     *
     * <p>
     * Some services don't return the size of their images, but we may know for a specific image
     * type that a service returns, according to real data, an approximation of the resolution
     * level.
     * </p>
     */
    public enum ResolutionLevel {

        /**
         * The high resolution level.
         *
         * <p>
         * This level applies to images with a height greater than or equal to 720px.
         * </p>
         */
        HIGH,

        /**
         * The medium resolution level.
         *
         * <p>
         * This level applies to images with a height between 175px inclusive and 720px exclusive.
         * </p>
         */
        MEDIUM,

        /**
         * The low resolution level.
         *
         * <p>
         * This level applies to images with a height between 1px inclusive and 175px exclusive.
         * </p>
         */
        LOW,

        /**
         * The unknown resolution level.
         *
         * <p>
         * This value is returned when the extractor doesn't know what resolution level an image
         * could have, for example if the extractor loops in an array of images with different
         * resolution levels without knowing the height.
         * </p>
         */
        UNKNOWN;

        /**
         * Get a {@link ResolutionLevel} based from the given height.
         *
         * @param heightPx the height from which returning the good {@link ResolutionLevel}
         * @return the {@link ResolutionLevel} corresponding to the height provided. See the
         * {@link ResolutionLevel} values for details about what value is returned.
         */
        public static ResolutionLevel fromHeight(final int heightPx) {
            if (heightPx <= 0) {
                return UNKNOWN;
            }

            if (heightPx < 175) {
                return LOW;
            }

            if (heightPx < 720) {
                return MEDIUM;
            }

            return HIGH;
        }
    }
}
